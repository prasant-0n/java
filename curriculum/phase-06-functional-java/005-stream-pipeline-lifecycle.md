# Stream API Fundamentals: Source, Intermediate, and Terminal Operations

---

## 1. Concept Definition & Core Purpose

In modern Java backend engineering, data processing pipelines require querying, transforming, filtering, and aggregating complex domain collections. Before Java 8, this was achieved via imperative loops and external iterators, which mixed iteration mechanics with business logic and were difficult to parallelize.

The **Java Stream API** (`java.util.stream.Stream`, introduced in Java 8 via JSR 335) represents a **declarative, pipeline-oriented abstraction** for processing sequences of elements.

### The Fundamental Axiom: Collections vs Streams
```
+---------------------------------------------------------------------------------------+
|                               Collection vs Stream                                    |
+--------------------------+----------------------------+-------------------------------+
| Dimension                | Collection (Data)          | Stream (Computation)          |
+--------------------------+----------------------------+-------------------------------+
| Primary Focus            | Data storage in memory     | Computation and transformation|
| Materialization          | Holds all elements in RAM  | Computes on demand (Lazy)     |
| Traversal                | Iterated multiple times    | Consumed ONCE (Single-use)    |
| Execution                | Eager (immediate)          | Lazy (triggered by terminal)  |
| Mutation                 | Adds/removes elements      | Never mutates underlying data |
+--------------------------+----------------------------+-------------------------------+
```

### The Three-Stage Stream Pipeline Lifecycle
Every stream execution consists of strictly three phases:

```
  1. Source (Creation)    --->    2. Intermediate (Transformation)    --->    3. Terminal (Consumption)
  [Collection, Array, I/O]         [filter, map, flatMap, sorted]              [collect, forEach, toList]
                                         (LAZY - Zero Execution)                 (EAGER - Triggers Execution)
```

1. **Source:** Obtains or generates a stream from a data structure (e.g. `list.stream()`, `Files.lines(path)`).
2. **Intermediate Operations:** Zero or more lazy transformations. They take a `Stream<T>` and return another `Stream<R>`. **No computation occurs** when an intermediate operation is declared.
3. **Terminal Operation:** Exactly one eager terminal operation. It initiates computation, pulls elements through the pipeline, terminates the stream, and produces a non-stream result (e.g., `List`, `Long`, `Optional`) or side-effect.

---

## 2. Background, Context & Historical Evolution

### 2.1 The Imperative Era (External Iteration)
Prior to Java 8, filtering and transforming a collection of transactions required imperative external iteration:

```java
// Pre-Java 8 Imperative External Iteration
List<String> highValueIds = new ArrayList<>();
for (Transaction tx : transactions) {
    if (tx.getAmount().compareTo(THRESHOLD) > 0) {
        if ("USD".equals(tx.getCurrency())) {
            highValueIds.add(tx.getId());
            if (highValueIds.size() == 10) {
                break; // Short-circuit logic intertwined with business logic
            }
        }
    }
}
```
#### Problems with External Iteration:
- **Boilerplate & Noise:** Business logic is buried inside indexing, nested `if` statements, and accumulator mutations.
- **Accidental State Mutation:** Modifying local variables or collections introduces thread-safety risks.
- **Parallelization Complexity:** Parallelizing this loop requires thread pools, synchronization locks, chunking algorithms, and manual thread joins.

### 2.2 The Stream Revolution (Internal Iteration)
Streams invert control using **Internal Iteration**. The runtime controls the traversal, scheduling, and loop optimization; the developer specifies only the declarative business rules:

```java
// Modern Java 8+ Declarative Stream Pipeline
List<String> highValueIds = transactions.stream()
        .filter(tx -> tx.getAmount().compareTo(THRESHOLD) > 0)
        .filter(tx -> "USD".equals(tx.getCurrency()))
        .map(Transaction::getId)
        .limit(10)
        .toList(); // Java 16+ unmodifiable list
```

---

## 3. Syntax, Signatures & Language Specifications

### 3.1 Sources: How to Create Streams

```java
// 1. From Collections
List<String> list = List.of("A", "B", "C");
Stream<String> s1 = list.stream();
Stream<String> s2 = list.parallelStream();

// 2. From Arrays
String[] array = {"X", "Y", "Z"};
Stream<String> s3 = Arrays.stream(array);
Stream<String> s4 = Stream.of(array);

// 3. Static Factories
Stream<Integer> s5 = Stream.of(1, 2, 3);
Stream<Object> empty = Stream.empty();
Stream<String> built = Stream.<String>builder().add("1").add("2").build();

// 4. Infinite & Generated Streams
Stream<Double> randoms = Stream.generate(Math::random).limit(5);

// 5. Java 9 Bounded Iterate: (seed, hasNextPredicate, nextUnaryOperator)
Stream<Integer> evens = Stream.iterate(0, n -> n < 100, n -> n + 2);

// 6. I/O Channels (AutoCloseable!)
try (Stream<String> lines = Files.lines(Path.of("/var/log/app.log"))) {
    // Process stream...
}

// 7. Regex Pattern Splitting
Stream<String> tokens = Pattern.compile(",").splitAsStream("apple,banana,orange");
```

### 3.2 Intermediate Operations (Lazy)
Intermediate operations return a new `Stream` and are classified into two categories:

| Intermediate Operation | Classification | Description |
|---|---|---|
| `filter(Predicate<T>)` | **Stateless** | Discards elements that do not match the predicate. |
| `map(Function<T, R>)` | **Stateless** | Transforms element from type $T$ to type $R$. |
| `flatMap(Function<T, Stream<R>>)`| **Stateless** | Flattens nested streams/collections into a single stream. |
| `peek(Consumer<T>)` | **Stateless** | Performs side-effect action on each element (primarily for debugging). |
| `distinct()` | **Stateful** | Removes duplicate elements (requires tracking seen items in a set). |
| `sorted()` / `sorted(Comparator)`| **Stateful** | Sorts elements (must buffer all upstream elements before emitting). |
| `limit(long maxSize)` | **Stateful** | Short-circuiting: truncates stream to maximum size. |
| `skip(long n)` | **Stateful** | Discards the first $n$ elements. |
| `takeWhile(Predicate<T>)` (Java 9)| **Stateful** | Short-circuiting: takes elements while predicate is true, then stops. |
| `dropWhile(Predicate<T>)` (Java 9)| **Stateful** | Drops elements while predicate is true, then emits remaining. |

### 3.3 Terminal Operations (Eager)
A terminal operation initiates execution and closes the stream:

| Terminal Operation | Return Type | Category |
|---|---|---|
| `collect(Collector)` | `R` (e.g. `List`, `Map`) | Reduction / Aggregation |
| `toList()` (Java 16+) | `List<T>` | Unmodifiable List Reduction |
| `reduce(BinaryOperator)` | `Optional<T>` / `T` | Associative Reduction |
| `count()` | `long` | Aggregation |
| `min(Comparator)` / `max(...)` | `Optional<T>` | Extreme Value Search |
| `forEach(Consumer)` | `void` | Side-Effect Iteration |
| `findFirst()` / `findAny()` | `Optional<T>` | Short-Circuiting Retrieval |
| `anyMatch(Predicate)` | `boolean` | Short-Circuiting Boolean Check |
| `allMatch(Predicate)` | `boolean` | Short-Circuiting Boolean Check |
| `noneMatch(Predicate)` | `boolean` | Short-Circuiting Boolean Check |

---

## 4. Architectural Role & Backend Design Patterns

### 4.1 The Single-Use Rule (Stream Invariant)
A `Stream` object **cannot be reused**. Once a terminal operation has commenced, the stream is considered consumed and closed.

```java
Stream<String> stream = List.of("A", "B", "C").stream();

stream.forEach(System.out::println); // Terminal op 1: Consumes stream

// THROWS IllegalStateException: stream has already been operated upon or closed!
long count = stream.count();
```
**Architectural Rule:** Never store a `Stream` in a field or pass it across long-lived architecture layers. Streams should be created, operated on, and terminated locally within a single method invocation.

### 4.2 AutoCloseable I/O Streams (File Descriptor Defense)
Streams backed by file or network I/O (such as `Files.lines()`, `Files.walk()`, or `Files.find()`) implement **`java.lang.AutoCloseable`**.
If an exception occurs or if short-circuiting terminates the stream early, the underlying OS file handle remains open unless closed!

```java
// CRITICAL ARCHITECTURAL PATTERN:
// Always wrap I/O-backed streams in Try-With-Resources!
public List<String> findErrorLines(Path logPath) throws IOException {
    try (Stream<String> lines = Files.lines(logPath)) {
        return lines.filter(line -> line.contains("ERROR"))
                    .limit(50)
                    .toList();
    } // File descriptor reliably closed here
}
```

---

## 5. Memory Layout, JVM Mechanics & Performance Implications

### 5.1 Loop Fusion (Vertical Execution Order)
A common misconception is that intermediate operations run "horizontally" across the entire collection before the next operation begins:
```
Horizontal Misconception (False):
  [Element 1, 2, 3] ---> filter all 3 ---> [1, 3] ---> map both ---> [A, C]
```
**How Streams Actually Execute (Loop Fusion / Vertical Execution):**
The JVM fuses intermediate operations into a single continuous pipeline. Each element moves **vertically** down the entire chain before the next element is fetched from the source:

```
  Source Item 1 ---> filter(true)  ---> map("A") ---> collect (Added to List)
  Source Item 2 ---> filter(false) ---> [Skipped, discarded immediately!]
  Source Item 3 ---> filter(true)  ---> map("C") ---> collect (Added to List)
```
**Performance Benefit:** Loop fusion eliminates intermediate temporary collections and maximizes CPU L1/L2 cache locality.

### 5.2 Short-Circuiting Efficiency
Because streams execute vertically, short-circuit operations like `limit()` or `findFirst()` stop upstream evaluation immediately:

```java
// Even with 10,000,000 elements, this prints ONLY ONCE and terminates immediately!
Stream.of(1, 2, 3, 4, 5)
      .peek(x -> System.out.println("Processing: " + x))
      .findFirst();
// Output: "Processing: 1"
```

### 5.3 StreamOpFlag Bitmask Optimizations
Every stream pipeline stage maintains an integer bitmask (`StreamOpFlag`):
- `SIZED`: Size of the source is known in advance (e.g. `ArrayList`).
- `DISTINCT`: Elements are guaranteed unique (e.g. `HashSet`).
- `SORTED`: Elements are in natural/comparator order (e.g. `TreeSet`).
- `ORDERED`: Sequence has meaningful order (e.g. `List`).

If a stream is created from a `TreeSet` (which has the `SORTED` flag set) and you invoke `.sorted()`, **the Stream engine inspects the flags and skips sorting entirely**, saving an $O(n \log n)$ operation!

---

## 6. Comprehensive Code Examples

The following production-ready engine demonstrates the complete stream lifecycle, I/O streaming, infinite stream generation, and loop fusion:

```java
package com.antigravity.functional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public final class StreamLifecycleEngine {

    public record LedgerEntry(String id, BigDecimal amount, String type, Instant timestamp) {}

    // 1. Production Financial Pipeline with Loop Fusion
    public static List<String> processHighValueCredits(List<LedgerEntry> entries, BigDecimal threshold) {
        Objects.requireNonNull(entries, "entries required");
        Objects.requireNonNull(threshold, "threshold required");

        return entries.stream()
                .filter(e -> "CREDIT".equals(e.type()))                // Intermediate Stateless
                .filter(e -> e.amount().compareTo(threshold) >= 0)     // Intermediate Stateless
                .map(LedgerEntry::id)                                  // Intermediate Stateless
                .limit(5)                                              // Intermediate Stateful Short-Circuit
                .toList();                                             // Terminal Reduction
    }

    // 2. Safe I/O Log Processing with Try-With-Resources
    public static long countOccurrences(Path logFile, String targetKeyword) throws IOException {
        Objects.requireNonNull(logFile, "logFile required");
        Objects.requireNonNull(targetKeyword, "targetKeyword required");

        // Guarantees OS file handle is closed even if stream terminates early
        try (Stream<String> lines = Files.lines(logFile)) {
            return lines.filter(line -> line.contains(targetKeyword))
                        .count();
        }
    }

    // 3. Infinite Bounded Sequence Generator (Java 9 Bounded Iterate)
    public static List<Integer> generateFibonacciSequence(int maxTermValue) {
        // Generates pairs: [current, next]
        return Stream.iterate(new int[]{0, 1}, pair -> pair[0] <= maxTermValue, pair -> new int[]{pair[1], pair[0] + pair[1]})
                .map(pair -> pair[0])
                .toList();
    }
}
```

---

## 7. Edge Cases, Corner Scenarios & Failure Modes

### 7.1 Forgetting the Terminal Operation (The Invisible Pipeline Bug)
Intermediate operations are strictly lazy. If you forget to invoke a terminal operation, **nothing happens**:
```java
// BUG: Nothing is logged, no processing occurs!
transactions.stream()
            .filter(Transaction::isValid)
            .peek(tx -> logger.info("Processing tx: {}", tx.getId())); 
// Returns a Stream<Transaction> that is silently discarded!
```

### 7.2 The Infinite Stream Stateful Hang Trap
Stateful intermediate operations like `sorted()` or `distinct()` require inspecting all upstream elements before they can emit the first element.
If invoked on an infinite stream without a preceding `limit()`:
```java
// FATAL THREAD HANG:
Stream.iterate(0, x -> x + 1)
      .sorted()          // HANGS FOREVER! Tries to sort infinite numbers in memory!
      .limit(10)
      .toList();

// CORRECT ORDER:
Stream.iterate(0, x -> x + 1)
      .limit(10)         // Truncate to finite size FIRST
      .sorted()          // Now safe to sort
      .toList();
```

### 7.3 Calling Terminal Operations on Closed Streams
```java
Stream<String> s = Stream.of("A", "B");
s.close();
// THROWS IllegalStateException: stream has already been operated upon or closed
s.forEach(System.out::println);
```

---

## 8. Anti-Patterns & Common Traps

### Anti-Pattern 1: Mutating Collections via `forEach()`
```java
// BAD (Imperative thinking in functional code):
List<String> results = new ArrayList<>();
orders.stream()
      .filter(Order::isShipped)
      .map(Order::getId)
      .forEach(results::add); // SIDE-EFFECT MUTATION! Thread-unsafe in parallel!

// GOOD: Collect cleanly into an immutable list
List<String> results = orders.stream()
      .filter(Order::isShipped)
      .map(Order::getId)
      .toList();
```

### Anti-Pattern 2: Using `peek()` for Production Business Logic
```java
// DANGEROUS: JLS allows the stream engine to optimize away operations!
// If terminal operation does not evaluate all elements, peek is silently skipped!
orders.stream()
      .peek(orderService::sendEmailNotification) // FORBIDDEN! Peek is for DEBUGGING ONLY
      .count(); // Compiler/Engine can optimize count() by skipping peek entirely!
```

### Anti-Pattern 3: Passing Unbound Streams Across Layer Boundaries
Passing `Stream<Entity>` out of repository methods (e.g. `UserRepository.findUsers()`) requires maintaining open database transactions and database cursor locks across service layers, leading to connection pool exhaustion and transaction timeouts.
**Rule:** Materialize streams into collections (`List`, `Set`) before returning across architectural boundaries.

---

## 9. Testing Strategies, Verification & Benchmarking

### 9.1 Verification Suite: Complete OpenJDK 21 Test
The following standalone test suite verifies stream lifecycle stages, lazy evaluation proof, loop fusion mechanics, single-use constraints, and infinite sequence generation:

```java
package com.antigravity.functional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class StreamLifecycleVerificationTest {

    public static void main(String[] args) throws IOException {
        System.out.println("Starting Stream API Fundamentals Verification Suite...");

        testStreamLifecyclePipeline();
        testStreamSingleUseInvariant();
        testLazyEvaluationAndLoopFusionProof();
        testJava9BoundedIterate();
        testAutoCloseableIoStreamHandling();

        System.out.println("ALL 5 STREAM LIFECYCLE SUITES PASSED SUCCESSFULLY!");
    }

    static void testStreamLifecyclePipeline() {
        List<StreamLifecycleEngine.LedgerEntry> entries = List.of(
                new StreamLifecycleEngine.LedgerEntry("1", new BigDecimal("100"), "CREDIT", Instant.now()),
                new StreamLifecycleEngine.LedgerEntry("2", new BigDecimal("500"), "DEBIT", Instant.now()),
                new StreamLifecycleEngine.LedgerEntry("3", new BigDecimal("800"), "CREDIT", Instant.now()),
                new StreamLifecycleEngine.LedgerEntry("4", new BigDecimal("1200"), "CREDIT", Instant.now())
        );

        List<String> highValueCreditIds = StreamLifecycleEngine.processHighValueCredits(entries, new BigDecimal("500"));

        assert highValueCreditIds.size() == 2;
        assert highValueCreditIds.contains("3");
        assert highValueCreditIds.contains("4");
    }

    static void testStreamSingleUseInvariant() {
        Stream<String> stream = Stream.of("Alpha", "Beta", "Gamma");
        long firstCount = stream.count();
        assert firstCount == 3;

        // Second terminal operation must throw IllegalStateException
        boolean caughtException = false;
        try {
            stream.count();
        } catch (IllegalStateException e) {
            caughtException = true;
            assert e.getMessage().contains("stream has already been operated upon or closed");
        }
        assert caughtException : "Stream must enforce single-use invariant";
    }

    static void testLazyEvaluationAndLoopFusionProof() {
        List<String> items = List.of("apple", "banana", "cherry", "date", "elderberry");
        List<String> operationsLog = new ArrayList<>();

        // Prove Laziness: Declaring stream does ZERO work
        Stream<String> pipeline = items.stream()
                .filter(s -> {
                    operationsLog.add("FILTER: " + s);
                    return s.length() > 5;
                })
                .map(s -> {
                    operationsLog.add("MAP: " + s);
                    return s.toUpperCase(Locale.ROOT);
                });

        assert operationsLog.isEmpty() : "Intermediate operations must not execute without terminal operation";

        // Terminal operation with short-circuit limit(2)
        // Only elements needed to satisfy limit(2) should be processed (Loop Fusion!)
        List<String> result = pipeline.limit(2).toList();

        assert result.size() == 2;
        assert result.get(0).equals("BANANA");
        assert result.get(1).equals("CHERRY");

        // Verify that "date" and "elderberry" were NEVER processed!
        assert !operationsLog.contains("FILTER: date") : "Loop fusion must short-circuit after satisfying limit";
        assert !operationsLog.contains("FILTER: elderberry");
    }

    static void testJava9BoundedIterate() {
        List<Integer> fibs = StreamLifecycleEngine.generateFibonacciSequence(50);
        // Expected: 0, 1, 1, 2, 3, 5, 8, 13, 21, 34
        assert fibs.size() == 10;
        assert fibs.get(fibs.size() - 1) == 34;
    }

    static void testAutoCloseableIoStreamHandling() throws IOException {
        Path tempFile = Files.createTempFile("stream-test", ".txt");
        try {
            Files.write(tempFile, List.of("Line 1", "ERROR: DB Connection Lost", "Line 3", "ERROR: Disk Full"));

            long errorCount = StreamLifecycleEngine.countOccurrences(tempFile, "ERROR");
            assert errorCount == 2;
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
```

---

## 10. Tooling, Ecosystem & Observability

### 10.1 IntelliJ IDEA Stream Debugger
IntelliJ IDEA provides a dedicated **Stream Visualizer** in its debugger:
1. Set a breakpoint on a stream chain.
2. Click the **Trace Current Stream Chain** icon in the debugger toolbar.
3. The IDE renders a visual multi-tab flowchart showing every element transitioning across each intermediate operation stage.

### 10.2 OpenJDK `StreamOpFlag` Introspection
Engineers can inspect stream flags by enabling JVM logging or attaching a debugger to inspect `AbstractPipeline.sourceFlags` to verify if intermediate sorting or sizing optimizations are engaged.

---

## 11. Security Considerations & Hardening

### 11.1 Resource Exhaustion via Unbounded Streams
Never expose endpoints where client input controls infinite stream generation without hard bounds:
```java
// DENIAL OF SERVICE RISK:
public Stream<String> generateTokens(int requestedLimit) {
    // If client passes Integer.MAX_VALUE, memory/CPU is exhausted!
    int safeLimit = Math.min(requestedLimit, 100); // Enforce defense-in-depth ceiling
    return Stream.generate(this::produceToken).limit(safeLimit);
}
```

### 11.2 OS File Descriptor Leaks
Failure to close `Files.lines()` streams will leak underlying OS file descriptors (`EMFILE: Too many open files`). In high-traffic services, this causes catastrophic cascading outages as network sockets fail to open.

---

## 12. Common Interview & System Design Questions

### Question 1: What is the difference between an intermediate and a terminal stream operation?
**Answer:**
An intermediate operation (e.g. `filter`, `map`) transforms a stream into another stream lazily. It does not perform any computation until a terminal operation is called.
A terminal operation (e.g. `collect`, `forEach`, `reduce`) triggers the execution of the entire pipeline, consumes the elements vertically (via loop fusion), closes the stream, and produces a final non-stream result or side-effect.

### Question 2: Why can a Stream be traversed only once?
**Answer:**
Streams are computational pipelines, not data structures. They do not store elements in memory; they consume elements from an underlying source and pass them through intermediate stages. Once a terminal operation starts, the source iterator or channel is traversed and the internal `linkedOrConsumed` flag is set to `true`. Re-traversing would require buffering all processed elements in memory, which would defeat the memory efficiency of streaming.

### Question 3: How does loop fusion optimize stream performance?
**Answer:**
Loop fusion combines multiple intermediate operations into a single vertical pass. Instead of executing `filter` on all 1,000,000 elements, creating a temporary list, and then executing `map`, each element is filtered and mapped in a single pass through CPU registers, minimizing memory allocations and maximizing CPU cache locality.

---

## 13. Production Case Study & War Story

### The Kubernetes Pod Crash: `Too many open files`
An enterprise billing service deployed on Kubernetes was crashing every 4 hours with `java.io.IOException: Too many open files`.

#### Root Cause Analysis
Developers implemented an audit log scanner:
```java
// PRODUCTION OUTAGE CAUSE:
public boolean containsSecurityAuditViolation(Path auditFile) throws IOException {
    // Files.lines() opens an underlying file channel!
    return Files.lines(auditFile).anyMatch(line -> line.contains("SECURITY_VIOLATION"));
}
```
`anyMatch()` short-circuits as soon as a match is found. Because `Files.lines()` was not enclosed in a try-with-resources block, **the file descriptor was never closed** when `anyMatch()` exited!
With 5,000 audit checks per hour, the Linux kernel process file descriptor limit (1024) was exhausted rapidly, causing all subsequent TCP socket connections to fail.

#### Remediation
The method was refactored with Try-With-Resources:
```java
public boolean containsSecurityAuditViolation(Path auditFile) throws IOException {
    try (Stream<String> lines = Files.lines(auditFile)) {
        return lines.anyMatch(line -> line.contains("SECURITY_VIOLATION"));
    } // File descriptor deterministically closed upon method return
}
```
**Outcome:** File descriptor leaks dropped to zero, and pod uptime stabilized at 100%.

---

## 14. Migration & Evolution Strategies (JavaScript/TypeScript Transition)

| Concept / Behavior | JavaScript / TypeScript | Modern Java Stream API |
|---|---|---|
| Execution Model | **Eager / Horizontal:** `arr.filter().map()` creates intermediate arrays on each step | **Lazy / Vertical:** Computations are fused; 0 intermediate arrays |
| Traversal Limit | Arrays can be mapped repeatedly | Streams are single-use; throws `IllegalStateException` |
| Infinite Sequences | Custom generator functions (`function*`) | `Stream.iterate()` and `Stream.generate()` |
| File Streaming | Node.js Readable Streams (`fs.createReadStream`) | `Files.lines(path)` with `AutoCloseable` |

---

## 15. Key Takeaways & Summary

```
                      Stream Pipeline Lifecycle Summary
+-----------------------+-------------------------+-------------------------------------+
| Phase                 | Method Examples         | Behavioral Contract                 |
+-----------------------+-------------------------+-------------------------------------+
| 1. Source             | list.stream(), of(), lines() | Generates data sequence; can be I/O |
| 2. Intermediate       | filter, map, sorted, limit   | Lazy; fuses into single loop pass   |
| 3. Terminal           | collect, count, toList       | Eager; executes pipeline; single use|
+-----------------------+-------------------------+-------------------------------------+
```

1. **Remember the Lifecycle:** Source $\rightarrow$ Intermediate (Lazy) $\rightarrow$ Terminal (Eager).
2. **Never Reuse Streams:** Reusing throws `IllegalStateException`.
3. **Always Close I/O Streams:** Wrap `Files.lines()` in try-with-resources.
4. **Beware Infinite Statefulness:** Place `limit()` before `sorted()` or `distinct()` on infinite streams.

---

## 16. References & Further Reading

- [Oracle Java SE 21 Documentation: java.util.stream.Stream](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Stream.html)
- [JSR 335: Lambda Expressions & Stream API](https://jcp.org/en/jsr/detail?id=335)
- [Brian Goetz: The State of the Lambda - Libraries Edition](https://cr.openjdk.org/~briangoetz/lambda/lambda-libraries-final.html)
- *Effective Java (3rd Edition)* by Joshua Bloch — Item 45: Use streams judiciously
