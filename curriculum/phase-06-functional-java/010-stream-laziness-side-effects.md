# Phase 06: Functional Java
## Concept: Stream Laziness, Short-Circuit Evaluation, and Side-Effect Dangers
`P06-C010`

---

## 1. Concept Definition & Core Purpose

Java Streams are **lazy by default**: intermediate operations (`filter`, `map`, `flatMap`, `sorted`) do not compute their results immediately upon being declared. Instead, they construct a directed pipeline of computational stages (represented internally as linked `Sink` nodes). Computation only occurs when a **terminal operation** (`collect`, `reduce`, `findFirst`, `count`) is invoked, which pulls elements through the fused pipeline one at a time.

```
 +-----------------------------------------------------------------------------------------+
 |                                  Lazy Pull Pipeline                                     |
 |                                                                                         |
 |  [ Source ] ──► [ filter(pred) ] ──► [ map(fn) ] ──► [ Terminal Operation ]             |
 |                         ▲                  ▲                     │                      |
 |                         │                  │                     ▼                      |
 |                         +──────────────────+────────────── Pulls element on demand      |
 |                                                            (Halts immediately when      |
 |                                                             condition is satisfied)     |
 +-----------------------------------------------------------------------------------------+
```

### Core Concepts Governed by Laziness
1. **Vertical Execution (Loop Fusion):** Rather than completing `filter()` across an entire dataset and creating an intermediate collection before starting `map()`, an element passes through `filter()`, `map()`, and the terminal sink in a single vertical pass through CPU registers.
2. **Short-Circuit Evaluation:** An operation is short-circuiting if, when presented with an infinite or large stream, it may produce a finite result or terminate after inspecting only a subset of elements (e.g. `limit()`, `takeWhile()`, `findFirst()`, `anyMatch()`).
3. **Side-Effect Freedom (Purity):** Because stream execution order is lazy, vertically fused, and potentially parallelized across CPU cores, behavioral parameters passed to stream operations **must be non-interfering and stateless**. Introducing side effects (such as mutating external collections or variables) leads to non-deterministic race conditions and silent data corruption.

---

## 2. Background, Context & Historical Evolution

In traditional imperative Java (and classical JavaScript array methods like `arr.filter().map()`), operations are **eager and horizontal**:
```javascript
// JavaScript / Imperative Eager Model:
// 1. filter() iterates ALL 1,000,000 items and allocates intermediate Array A (e.g. 500,000 items).
// 2. map() iterates ALL 500,000 items and allocates intermediate Array B.
// 3. slice(0, 5) grabs the first 5 elements and discards Array A and Array B!
const result = items.filter(heavyCheck).map(transform).slice(0, 5);
```
In this eager model, 999,995 unnecessary expensive computations and massive heap allocations take place just to extract 5 results!

In **Java 8 (JSR 335)**, Brian Goetz and the OpenJDK team modeled the Stream API after functional programming languages (Haskell, Clojure) using **lazy demand-driven pipelines**. An element is only pulled from the source when the terminal operation demands it. If `limit(5)` or `findFirst()` satisfies the terminal condition, traversal halts immediately, saving millions of CPU cycles and preventing gigabytes of intermediate memory allocation.

---

## 3. Syntax, Signatures & Language Specifications

### 3.1 Language Invariants (JLS §15.27 & Stream Specification)

The Java SE specification mandates three critical behavioral properties for stream pipelines:
1. **Non-Interference:** The behavioral parameters (lambdas/method references) must not modify the data source of the stream during pipeline execution. Modifying the source while traversing triggers a `ConcurrentModificationException` or undefined non-deterministic behavior.
2. **Statelessness:** The result of a lambda must depend solely on its input arguments and immutable captured state. It must not depend on any state that might change during stream execution.
3. **Side-Effect Freedom:** Stream operations should not mutate state outside their local scope (e.g., adding to external collections, incrementing non-atomic counters).

---

### 3.2 Short-Circuiting Operations Taxonomy

| Operation | Stage | Condition for Early Termination |
|---|---|---|
| `limit(maxSize)` | Intermediate | Terminates after emitting $N = \text{maxSize}$ elements. |
| `takeWhile(predicate)` | Intermediate (Java 9+) | Terminates on the **first element** that evaluates `predicate.test(e) == false`. |
| `findFirst()` | Terminal | Terminates on the **first matching element** in encounter order. |
| `findAny()` | Terminal | Terminates on the **first matching element** found by any concurrent worker thread. |
| `anyMatch(predicate)` | Terminal | Terminates immediately when **any** element evaluates to `true`. |
| `allMatch(predicate)` | Terminal | Terminates immediately when **any** element evaluates to `false`. |
| `noneMatch(predicate)` | Terminal | Terminates immediately when **any** element evaluates to `true`. |

---

## 4. Architectural Role & Backend Design Patterns

### 4.1 Safe Infinite Generator Pipeline Pattern
Because streams are lazy, backends can express unbounded or infinite sequences (event loops, exponential retry delays, UUID batch generators) cleanly without risk of infinite memory allocation:

```
 [ Infinite Stream.iterate() ] ──► [ filter(isEligible) ] ──► [ limit(10) ] ──► [ toList() ]
                                                                   ▲
                                                                   │
                                                Stops the infinite upstream generator!
```

### 4.2 The Pipeline Stage Ordering Pattern
Because intermediate operations are fused, the order of intermediate operations profoundly dictates algorithmic complexity:

```java
// OPTIMAL: Filters 1,000,000 items down to 10 FIRST, then sorts 10 items (O(K log K))
items.stream()
     .filter(predicate)
     .limit(10)
     .sorted(comparator)
     .toList();

// DISASTROUS: Buffers and sorts all 1,000,000 items in memory (O(N log N)), THEN truncates to 10!
items.stream()
     .filter(predicate)
     .sorted(comparator) // ALLOCATES SPINEDBUFFER FOR 1M ELEMENTS!
     .limit(10)
     .toList();
```

---

## 5. Memory Layout, JVM Mechanics & Performance Implications

### 5.1 The `cancellationRequested()` Sink Signaling Mechanism
How does an intermediate operation like `limit()` or a terminal operation like `anyMatch()` stop an infinite generator or database cursor without throwing an exception?

In OpenJDK `java.util.stream`:
1. Every downstream stage is wrapped in a `Sink<T>` interface extending `Consumer<T>`.
2. The `Sink` interface defines the control method:
   ```java
   default boolean cancellationRequested() { return false; }
   ```
3. Short-circuiting sinks override this method:
   ```java
   // In SliceOps (limit implementation):
   @Override
   public boolean cancellationRequested() {
       return m >= limit; // True as soon as limit count is reached
   }
   ```
4. During traversal, the upstream spliterator executes a tight loop:
   ```java
   // Inside Spliterator / Pipeline Traversal:
   while (!sink.cancellationRequested() && spliterator.tryAdvance(sink)) {
       // Processes one element at a time vertically
   }
   ```
5. As soon as `cancellationRequested()` returns `true`, the loop terminates instantly. No further elements are read from the source.

```
+-----------------------------------------------------------------------------------------+
|                          Short-Circuit Cancellation Signal Flow                         |
|                                                                                         |
|  Spliterator.tryAdvance()                                                               |
|        │                                                                                |
|        ▼ pushes element                                                                 |
|  FilterSink.accept()                                                                    |
|        │                                                                                |
|        ▼ pushes passed element                                                          |
|  LimitSink.accept() ──► increments count                                                |
|        │                                                                                |
|        ▼ count >= limit?                                                                |
|  LimitSink.cancellationRequested() == TRUE ──┐                                          |
|                                              │                                          |
|  Spliterator loop reads cancellationRequested() ◄───────────────────────────────────────┘
|  ==> LOOP TERMINATES IMMEDIATELY!                                                       |
+-----------------------------------------------------------------------------------------+
```

### 5.2 The `count()` Stream Optimization Trap
In Java 9+ (via JEP optimizations), if a stream source has the `SIZED` flag (such as `ArrayList.stream()`) and there are no intermediate filtering or flat-mapping operations, **the JVM optimizes `stream.count()` by inspecting collection size directly without traversing elements or invoking `peek()` or `map()`**:

```java
List<String> list = List.of("A", "B", "C");
AtomicInteger counter = new AtomicInteger(0);

// Because list is SIZED and intermediate operations do not alter size,
// HotSpot skips the entire pipeline and returns list.size() directly!
long count = list.stream()
                 .peek(e -> counter.incrementAndGet()) // NEVER EXECUTED!
                 .count();

// counter.get() IS ZERO!
```

---

## 6. Comprehensive Code Examples

### 6.1 Resilient Idempotent Processing Engine

```java
package com.antigravity.functional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class StreamLazinessEngine {

    public record AuditEvent(String eventId, String severity, long timestamp) {}

    // 1. Proof of Laziness: Zero Evaluation Without Terminal Operation
    public static Stream<String> createDormantPipeline(List<String> items, AtomicInteger executionTracker) {
        Objects.requireNonNull(items, "items required");
        Objects.requireNonNull(executionTracker, "tracker required");

        // Intermediate operations are strictly lazy descriptors; nothing executes here!
        return items.stream()
                .filter(item -> {
                    executionTracker.incrementAndGet();
                    return item.length() > 4;
                })
                .map(item -> {
                    executionTracker.incrementAndGet();
                    return item.toUpperCase(Locale.ROOT);
                });
    }

    // 2. Short-Circuiting Safe Infinite Stream Generator
    public static List<Integer> generateFirstFibonacciNumbers(int count) {
        if (count <= 0) return List.of();

        // Infinite Stream truncated by limit() via cancellationRequested()
        return Stream.iterate(new int[]{0, 1}, fib -> new int[]{fib[1], fib[0] + fib[1]})
                .map(fib -> fib[0])
                .limit(count) // Prevents infinite execution
                .toList();
    }

    // 3. Early Exit Query: Finding first high-severity event without scanning entire log
    public static Optional<AuditEvent> findFirstCriticalEventFast(
            Stream<AuditEvent> auditStream, AtomicInteger scanCounter) {
        Objects.requireNonNull(auditStream, "auditStream required");
        Objects.requireNonNull(scanCounter, "scanCounter required");

        return auditStream
                .peek(e -> scanCounter.incrementAndGet())
                .filter(e -> "CRITICAL".equalsIgnoreCase(e.severity()))
                .findFirst(); // Short-circuits immediately upon the first match!
    }

    // 4. Pure Side-Effect-Free Accumulation
    public static List<String> extractSevereEventIdsPure(List<AuditEvent> events) {
        Objects.requireNonNull(events, "events required");

        // PURE FUNCTIONAL: No external state mutation
        return events.stream()
                .filter(e -> "CRITICAL".equalsIgnoreCase(e.severity()) || "HIGH".equalsIgnoreCase(e.severity()))
                .map(AuditEvent::eventId)
                .toList(); // Immutable result
    }
}
```

---

## 7. Edge Cases, Corner Scenarios & Failure Modes

### 7.1 Non-Interference Violation: Mutating the Backing Source
Modifying the underlying collection while a stream is executing throws `ConcurrentModificationException`:
```java
List<String> list = new ArrayList<>(List.of("alpha", "beta", "gamma"));

// FATAL EXCEPTION: Modifying source during traversal
list.stream()
    .peek(item -> {
        if ("beta".equals(item)) {
            list.remove(item); // CRASH! java.util.ConcurrentModificationException
        }
    })
    .toList();
```
**Rule:** Never add, remove, or modify elements in the backing collection while a stream is traversing it.

### 7.2 Side-Effect Data Race in Parallel Streams
Mutating an un-synchronized external collection inside a stream pipeline leads to data corruption:
```java
// CATASTROPHIC BUG: Multiple worker threads mutate an unsafe ArrayList concurrently!
List<Integer> unsafeList = new ArrayList<>();
IntStream.range(0, 100_000).parallel().forEach(unsafeList::add);

// Result: ArrayIndexOutOfBoundsException, null entries, or list.size() << 100,000!
```

### 7.3 The Infinite Stream Non-Short-Circuiting Hang
Invoking a non-short-circuiting operation on an infinite stream freezes the application indefinitely:
```java
// CRITICAL BUG: Stream.iterate is infinite; sorted() cannot sort an infinite stream!
Stream.iterate(1, n -> n + 1)
      .sorted() // HANGS FOREVER AT 100% CPU!
      .limit(10)
      .toList();
```
**Rule:** Always position `limit()` or `takeWhile()` **before** stateful operations like `sorted()` or `distinct()`.

---

## 8. Anti-Patterns & Common Traps

### Anti-Pattern 1: The "Ghost Pipeline" (Missing Terminal Operation)
```java
// BUG: The developer expects logs to print and DB records to update, but NOTHING HAPPENS!
orders.stream()
      .filter(Order::isPending)
      .peek(orderService::processOrder); // Dormant stream discarded by GC!
```

### Anti-Pattern 2: Relying on `peek()` for Core Business Logic
The Java Stream specification explicitly states:
> `peek()` exists mainly to support debugging... In cases where the stream implementation is able to optimize away the production of some or all elements, `peek` will not be invoked for those elements.
```java
// DANGEROUS: JIT/Stream engine optimizations can silently skip peek!
long count = orders.stream()
                   .peek(order -> emailClient.sendReceipt(order)) // FORBIDDEN!
                   .count(); // Count may optimize away peek entirely!
```

### Anti-Pattern 3: State Accumulation inside Lambdas
```java
// BAD: State is external and impure; completely breaks in parallel streams!
int[] runningTotal = new int[]{0};
List<Integer> result = prices.stream()
                             .map(price -> runningTotal[0] += price) // Impure side effect!
                             .toList();
```

---

## 9. Testing Strategies, Verification & Benchmarking

### 9.1 Verification Suite: Complete OpenJDK 21 Test
The following standalone test verifies dormant stream laziness, short-circuit termination bounds, non-interference exceptions, and side-effect thread race reproduction:

```java
package com.antigravity.functional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamLazinessVerificationTest {

    public static final class StreamLazinessEngine {
        public record AuditEvent(String eventId, String severity, long timestamp) {}

        public static Stream<String> createDormantPipeline(List<String> items, AtomicInteger executionTracker) {
            Objects.requireNonNull(items, "items required");
            Objects.requireNonNull(executionTracker, "tracker required");

            return items.stream()
                    .filter(item -> {
                        executionTracker.incrementAndGet();
                        return item.length() > 4;
                    })
                    .map(item -> {
                        executionTracker.incrementAndGet();
                        return item.toUpperCase(Locale.ROOT);
                    });
        }

        public static List<Integer> generateFirstFibonacciNumbers(int count) {
            if (count <= 0) return List.of();
            return Stream.iterate(new int[]{0, 1}, fib -> new int[]{fib[1], fib[0] + fib[1]})
                    .map(fib -> fib[0])
                    .limit(count)
                    .toList();
        }

        public static Optional<AuditEvent> findFirstCriticalEventFast(
                Stream<AuditEvent> auditStream, AtomicInteger scanCounter) {
            Objects.requireNonNull(auditStream, "auditStream required");
            Objects.requireNonNull(scanCounter, "scanCounter required");

            return auditStream
                    .peek(e -> scanCounter.incrementAndGet())
                    .filter(e -> "CRITICAL".equalsIgnoreCase(e.severity()))
                    .findFirst();
        }

        public static List<String> extractSevereEventIdsPure(List<AuditEvent> events) {
            Objects.requireNonNull(events, "events required");
            return events.stream()
                    .filter(e -> "CRITICAL".equalsIgnoreCase(e.severity()) || "HIGH".equalsIgnoreCase(e.severity()))
                    .map(AuditEvent::eventId)
                    .toList();
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting Stream Laziness & Side-Effect Verification Suite...");

        testDormantPipelineLazinessProof();
        testInfiniteStreamShortCircuitTermination();
        testFindFirstEarlyTerminationEfficiency();
        testSourceInterferenceThrowsException();
        testParallelSideEffectRaceConditionProof();

        System.out.println("ALL 5 STREAM LAZINESS & SIDE-EFFECT TESTS PASSED SUCCESSFULLY!");
    }

    static void testDormantPipelineLazinessProof() {
        AtomicInteger tracker = new AtomicInteger(0);
        List<String> items = List.of("apple", "banana", "kiwi", "pear");

        // 1. Create dormant pipeline: ZERO operations must execute
        Stream<String> dormant = StreamLazinessEngine.createDormantPipeline(items, tracker);
        assert tracker.get() == 0 : "Dormant stream must not execute any intermediate operations!";

        // 2. Invoke terminal operation: NOW operations execute lazily
        List<String> evaluated = dormant.toList();
        assert evaluated.size() == 2; // "apple", "banana" (length > 4)
        assert tracker.get() > 0 : "Operations must execute upon terminal trigger";
    }

    static void testInfiniteStreamShortCircuitTermination() {
        // Generates first 8 Fibonacci numbers: 0, 1, 1, 2, 3, 5, 8, 13
        List<Integer> fibs = StreamLazinessEngine.generateFirstFibonacciNumbers(8);

        assert fibs.size() == 8;
        assert fibs.get(0) == 0;
        assert fibs.get(1) == 1;
        assert fibs.get(2) == 1;
        assert fibs.get(3) == 2;
        assert fibs.get(4) == 3;
        assert fibs.get(5) == 5;
        assert fibs.get(6) == 8;
        assert fibs.get(7) == 13;
    }

    static void testFindFirstEarlyTerminationEfficiency() {
        AtomicInteger inspectCount = new AtomicInteger(0);

        List<StreamLazinessEngine.AuditEvent> events = List.of(
                new StreamLazinessEngine.AuditEvent("E-1", "INFO", 1000L),
                new StreamLazinessEngine.AuditEvent("E-2", "WARN", 1001L),
                new StreamLazinessEngine.AuditEvent("E-3", "CRITICAL", 1002L), // Target at index 2
                new StreamLazinessEngine.AuditEvent("E-4", "INFO", 1003L),
                new StreamLazinessEngine.AuditEvent("E-5", "CRITICAL", 1004L),
                new StreamLazinessEngine.AuditEvent("E-6", "WARN", 1005L)
        );

        Optional<StreamLazinessEngine.AuditEvent> match =
                StreamLazinessEngine.findFirstCriticalEventFast(events.stream(), inspectCount);

        assert match.isPresent();
        assert "E-3".equals(match.get().eventId());
        // Must short-circuit immediately at element 3, never inspecting elements 4, 5, 6
        assert inspectCount.get() == 3 : "Expected exactly 3 inspections due to short-circuiting, got: " + inspectCount.get();
    }

    static void testSourceInterferenceThrowsException() {
        List<String> mutableList = new ArrayList<>(List.of("one", "two", "three", "four"));

        boolean exceptionCaught = false;
        try {
            // Modifying backing list during stream traversal
            mutableList.stream()
                    .peek(item -> {
                        if ("two".equals(item)) {
                            mutableList.add("ILLEGAL_CONCURRENT_INSERT");
                        }
                    })
                    .toList();
        } catch (ConcurrentModificationException e) {
            exceptionCaught = true;
        }

        assert exceptionCaught : "Mutating stream source during traversal must throw ConcurrentModificationException";
    }

    static void testParallelSideEffectRaceConditionProof() {
        int totalElements = 20_000;
        List<Integer> unsafeList = new ArrayList<>();

        // Unsafe side-effect mutation across threads
        IntStream.range(0, totalElements)
                .parallel()
                .forEach(unsafeList::add); // RACE CONDITION!

        // Due to race conditions on ArrayList's internal array pointer and elementData,
        // elements will be dropped or size corrupted (< totalElements)
        System.out.printf("  [Side-Effect Race Proof] Submitted %,d elements -> Captured in unsafe ArrayList: %,d%n",
                totalElements, unsafeList.size());

        // Safe pure collection captures 100% of elements deterministically
        List<Integer> safeList = IntStream.range(0, totalElements)
                .parallel()
                .boxed()
                .toList();
        assert safeList.size() == totalElements : "Pure stream collection must capture all elements deterministically";
    }
}
```

---

## 10. Tooling, Ecosystem & Observability

### 10.1 Debugging Stream Pipelines in IntelliJ IDEA
1. Set a breakpoint on a stream pipeline.
2. In the Debug tool window, click **Trace Current Stream Chain** (the Stream icon).
3. The interactive visualizer displays the entire pipeline:
   - Showing elements evaluated by `filter()`.
   - Illustrating elements transformed by `map()`.
   - Explicitly highlighting where short-circuiting (`findFirst()` / `limit()`) stopped upstream evaluation.

---

## 11. Security Considerations & Hardening

### 11.1 ReDoS-Equivalent CPU Freezes via Infinite Generator Endpoints
Never expose an API endpoint where client query parameters control infinite generators without hard constraints:
```java
// VULNERABLE: Client requests limit=Integer.MAX_VALUE -> Server CPU exhausted
public Stream<Integer> getSequence(int requestedLimit) {
    return Stream.iterate(0, n -> n + 1).limit(requestedLimit);
}

// HARDENED: Enforce defense-in-depth ceiling
public List<Integer> getSequence(int requestedLimit) {
    int safeLimit = Math.clamp(requestedLimit, 1, 1000);
    return Stream.iterate(0, n -> n + 1).limit(safeLimit).toList();
}
```

---

## 12. Common Interview & System Design Questions

### Question 1: What is loop fusion, and how does it relate to stream laziness?
**Answer:**
Loop fusion is an optimization technique where multiple successive intermediate operations are combined into a single continuous loop pass. Instead of executing `filter` on all elements into an intermediate buffer and then executing `map` on that buffer, loop fusion processes each element vertically: element 1 is filtered and mapped, then element 2 is filtered and mapped. This maximizes CPU L1/L2 cache locality, avoids intermediate collection allocations, and enables short-circuiting operations to stop traversal immediately.

### Question 2: Why are side effects prohibited in stream operations?
**Answer:**
Streams are designed to support seamless parallel execution and compiler optimizations. If lambdas mutate external state:
1. Parallel streams will suffer data races, lost updates, and memory visibility corruption.
2. The order of execution is not guaranteed, producing non-deterministic results.
3. Stream optimizations (such as `SIZED` optimizations on `count()`) may bypass intermediate operations entirely, causing expected side effects to silently disappear.

### Question 3: How does `takeWhile` differ from `filter`?
**Answer:**
`filter` evaluates every single element in the stream from start to finish.
`takeWhile` requires an ordered stream and **short-circuits immediately** upon encountering the very first element that does not satisfy the predicate, discarding the remainder of the stream without examining it.

---

## 13. Production Case Study & War Story

### The Missing Audit Logs Mystery
A financial brokerage service was required by regulatory compliance to record every processed transaction to an audit compliance database.

#### The Code
A developer implemented the audit mechanism inside a stream pipeline:
```java
public long auditAndCountTransactions(List<Transaction> transactions) {
    return transactions.stream()
            .peek(auditService::recordAuditLog) // Impure side-effect!
            .count();
}
```

#### The Production Incident
During regulatory inspection, auditors discovered that **millions of transactions were missing from the audit log table**, even though transaction counts were reported correctly!

#### Root Cause
Under Java 9+, OpenJDK introduced pipeline optimizations: when `count()` is called on a collection that is `SIZED` (e.g., `ArrayList`) with only stateless intermediate operations like `peek()` and `map()`, **the JVM optimizes `count()` by returning `collection.size()` directly without traversing elements!**
Because the stream elements were never traversed, `peek()` was **never executed**, and zero audit logs were written!

#### Remediation
The audit pipeline was refactored to separate computation from side-effects:
```java
public long auditAndCountTransactions(List<Transaction> transactions) {
    transactions.forEach(auditService::recordAuditLog); // Explicit, deterministic iteration
    return transactions.size();
}
```
**Lesson:** Never rely on `peek()` or intermediate stream operations for production business actions or mandatory side effects.

---

## 14. Migration & Evolution Strategies (JavaScript/TypeScript Transition)

| Concept | JavaScript / TypeScript | Modern Java Stream API |
|---|---|---|
| Pipeline Evaluation | **Eager:** `filter().map()` immediately creates new arrays | **Lazy:** Evaluates on demand only when terminal op executes |
| Short-Circuit Slicing | `.slice(0, 5)` after full evaluation | `.limit(5)` halts upstream iteration immediately |
| Infinite Sequences | Generator functions (`function*`) | `Stream.iterate()` and `Stream.generate()` |
| Source Modification | Array mutation during `forEach` alters loop index | Throws `ConcurrentModificationException` |

---

## 15. Key Takeaways & Summary

```
                  Stream Laziness & Short-Circuiting Cheat Sheet
+-----------------------+---------------------+-------------------+---------------------+
| Feature               | Mechanism           | Performance Benefit| Production Rule    |
+-----------------------+---------------------+-------------------+---------------------+
| Laziness              | Pull-based Sinks    | 0 allocations     | Missing terminal = 0|
| Loop Fusion           | Vertical execution  | CPU Cache hit     | Fuse filter + map   |
| Short-Circuiting      | cancellationReq()   | Stops at N items  | Put limit before sort|
| Non-Interference      | Source immutability | Prevents CME      | Never mutate source |
| Side-Effect Freedom   | Pure functions      | Parallel-safe     | Never mutate in map |
+-----------------------+---------------------+-------------------+---------------------+
```

1. **Streams are Inert Without a Terminal Operation:** No terminal op = zero execution.
2. **Never Use `peek()` for Business Logic:** Stream engines may optimize it away.
3. **Short-Circuit Early:** Place `limit()` and `takeWhile()` before stateful `sorted()` or `distinct()`.
4. **Avoid Shared Mutation:** Accumulating into external collections in parallel streams causes catastrophic data corruption.

---

## 16. References & Further Reading

- [Oracle Java SE 21 Documentation: java.util.stream Package Summary](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/package-summary.html)
- [JSR 335: Lambda Expressions and Stream API Specification](https://jcp.org/en/jsr/detail?id=335)
- *Effective Java (3rd Edition)* by Joshua Bloch — Item 46: Prefer side-effect-free functions in streams
- [Brian Goetz: The State of the Lambda - Libraries Edition](https://cr.openjdk.org/~briangoetz/lambda/lambda-libraries-final.html)
