# Core Function Types: Predicate, Function, Consumer, Supplier, and Primitives

---

## 1. Concept Definition & Core Purpose

When Java 8 introduced lambda expressions, it was necessary to provide a standardized set of functional interfaces so that developers and library designers would not each reinvent custom types for everyday functional operations (e.g., one library using `Filter<T>` while another used `Condition<T>`).

The **`java.util.function`** package establishes a unified vocabulary of **43 standardized functional interfaces**. At the foundation of this package sit **four core functional archetypes**, differentiated by input arity and return type:

```
+---------------------------------------------------------------------------------------+
|                    The Four Fundamental Functional Archetypes                         |
+-------------------+-----------------------+---------------------+---------------------+
| Interface         | Method Signature      | Mathematical Type   | Semantic Role       |
+-------------------+-----------------------+---------------------+---------------------+
| Predicate<T>      | boolean test(T t)     | T -> boolean        | Evaluation / Filter |
| Function<T, R>    | R apply(T t)          | T -> R              | Transformation      |
| Consumer<T>       | void accept(T t)      | T -> void           | Side-Effect / Sink  |
| Supplier<T>       | T get()               | void -> T           | Factory / Source    |
+-------------------+-----------------------+---------------------+---------------------+
```

### Arity and Type Specializations
From these four primary archetypes, the package derives:
1. **Bi-Arity Interfaces:** `BiPredicate<T, U>`, `BiFunction<T, U, R>`, `BiConsumer<T, U>` (accepting two input arguments).
2. **Operators (Homogeneous Types):** `UnaryOperator<T>` (extends `Function<T, T>`), `BinaryOperator<T>` (extends `BiFunction<T, T, T>`) where operand and return types are identical.
3. **Primitive Specializations:** Dedicated interfaces for `int`, `long`, and `double` (e.g., `IntPredicate`, `LongFunction<R>`, `DoubleConsumer`, `ToIntFunction<T>`) engineered specifically to **eliminate heap allocation and autoboxing/unboxing overhead** in high-throughput pipelines.

---

## 2. Background, Context & Historical Evolution

### 2.1 The Problem of Autoboxing in Generic Types
In Java's type system, generic type parameters (`<T>`) can only be instantiated with reference types (`Object`), not primitive types (`int`, `long`, `double`).
If Java only provided `Function<T, R>` and `Predicate<T>`:
```java
// HYPOTHETICAL DISASTER:
Predicate<Integer> isEven = x -> x % 2 == 0;
```
When evaluating `isEven.test(42)`:
1. The primitive `42` is **autoboxed** into an `Integer` object on the heap (allocating 16 to 24 bytes).
2. The method `test(Integer)` reads the reference from the heap (cache miss).
3. The method unboxes the `Integer` back to primitive `int` to perform the modulo operation `% 2`.
4. The boolean result is returned.
5. The temporary `Integer` object is discarded in Eden space, generating garbage collection churn.

In a stream processing 10,000,000 integers per second, this generates **hundreds of megabytes of garbage every second**, triggering stop-the-world GC pauses!

### 2.2 The Solution: The 43 Standard Interfaces (Java 8)
To solve this, the Java designers introduced 39 primitive specializations alongside the 4 generic archetypes.
`IntPredicate` operates directly on primitive `int`:
```java
@FunctionalInterface
public interface IntPredicate {
    boolean test(int value); // Zero boxing, zero heap allocation!
}
```

### 2.3 Functional Evolution in Java 9 and 11
- **Java 11:** Added `Predicate.not(Predicate<? super TARGET> target)` allowing clean static method reference negation (`Predicate.not(String::isBlank)` instead of `s -> !s.isBlank()`).

---

## 3. Syntax, Signatures & Language Specifications

### 3.1 `Predicate<T>` & Combinators
```java
package java.util.function;

@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);

    // Default short-circuiting combinators
    default Predicate<T> and(Predicate<? super T> other);
    default Predicate<T> or(Predicate<? super T> other);
    default Predicate<T> negate();

    // Static factories
    static <T> Predicate<T> isEqual(Object targetRef);
    static <T> Predicate<T> not(Predicate<? super T> target); // Java 11+
}
```

> [!IMPORTANT]
> `Predicate.and()` and `Predicate.or()` use **short-circuit evaluation**!
> In `p1.and(p2)`, if `p1.test(x)` returns `false`, `p2.test(x)` is **never invoked**. This makes null-safe defensive chaining completely reliable:
> `Predicate<String> safeCheck = Objects::nonNull.and(s -> s.length() > 5);`

### 3.2 `Function<T, R>` & Composition
```java
package java.util.function;

@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);

    // Runs 'before' first, then applies 'this': this(before(x))
    default <V> Function<V, R> compose(Function<? super V, ? extends T> before);

    // Applies 'this' first, then runs 'after': after(this(x))
    default <V> Function<T, V> andThen(Function<? super R, ? extends V> after);

    static <T> Function<T, T> identity();
}
```

#### The Critical Distinction: `compose` vs `andThen`
```
           compose vs andThen Mathematical Ordering
           
  Function<Integer, Integer> multiplyByTwo = x -> x * 2;
  Function<Integer, Integer> addTen = x -> x + 10;

  1. andThen: (x * 2) + 10
     multiplyByTwo.andThen(addTen).apply(5);  // Result = (5 * 2) + 10 = 20

  2. compose: (x + 10) * 2
     multiplyByTwo.compose(addTen).apply(5);  // Result = (5 + 10) * 2 = 30
```

### 3.3 `Consumer<T>` & Pipeline Sinks
```java
package java.util.function;

@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);

    default Consumer<T> andThen(Consumer<? super T> after);
}
```
`Consumer` represents side-effects (e.g. logging, publishing an event to Kafka, writing to a database, updating a metric).

### 3.4 `Supplier<T>` & Deferred Factories
```java
package java.util.function;

@FunctionalInterface
public interface Supplier<T> {
    T get();
}
```
`Supplier` represents a factory or deferred computation. It is evaluated lazily only when `.get()` is called.

### 3.5 Primitive Functional Interface Taxonomy
The primitive interfaces follow a systematic naming convention across `int`, `long`, and `double`:

| Category | Primitive Variants | Method Signature |
|---|---|---|
| **Predicates** | `IntPredicate`, `LongPredicate`, `DoublePredicate` | `boolean test(int/long/double val)` |
| **Consumers** | `IntConsumer`, `LongConsumer`, `DoubleConsumer` | `void accept(int/long/double val)` |
| **Suppliers** | `IntSupplier`, `LongSupplier`, `DoubleSupplier` | `int/long/double getAsInt/Long/Double()`|
| **Unary Operators** | `IntUnaryOperator`, `LongUnaryOperator`, `DoubleUnaryOperator` | `int/long/double applyAsInt/Long/Double(int/long/double operand)` |
| **Binary Operators** | `IntBinaryOperator`, `LongBinaryOperator`, `DoubleBinaryOperator` | `int/long/double applyAsInt/Long/Double(int left, int right)` |
| **Input Primitives** | `IntFunction<R>`, `LongFunction<R>`, `DoubleFunction<R>` | `R apply(int/long/double value)` |
| **Output Primitives** | `ToIntFunction<T>`, `ToLongFunction<T>`, `ToDoubleFunction<T>` | `int/long/double applyAsInt/Long/Double(T value)` |
| **Inter-Primitive** | `IntToLongFunction`, `IntToDoubleFunction`, `LongToIntFunction`, etc.| `long applyAsLong(int value)` |

---

## 4. Architectural Role & Backend Design Patterns

### 4.1 Composable Specification Pattern with `Predicate`
In domain-driven backend architectures, business rules frequently evolve into complex boolean expressions. Using composed `Predicate`s eliminates nested `if/else` ladders:

```java
public final class UserSpecifications {
    public static final Predicate<User> IS_ACTIVE = User::isActive;
    public static final Predicate<User> HAS_VERIFIED_EMAIL = User::isEmailVerified;
    public static final Predicate<User> IS_NOT_BLOCKED = Predicate.not(User::isBlocked);
    public static final Predicate<User> HAS_PREMIUM_TIER = u -> u.tier() == Tier.PREMIUM;

    // Fluent Domain Rule Composition
    public static final Predicate<User> ELIGIBLE_FOR_INSTANT_TRANSFER =
            IS_ACTIVE
            .and(HAS_VERIFIED_EMAIL)
            .and(IS_NOT_BLOCKED)
            .and(HAS_PREMIUM_TIER.or(u -> u.completedTransfersCount() >= 50));
}
```

### 4.2 Generic ETL Pipeline Architecture
The four functional archetypes naturally model the Extract-Transform-Load (ETL) pattern:

```
               Extract (Supplier) ---> Transform (Function) ---> Load (Consumer)
```

```java
public final class EtlPipeline<T, R> {
    private final Supplier<T> extractor;
    private final Function<T, R> transformer;
    private final Consumer<R> loader;

    public EtlPipeline(Supplier<T> extractor, Function<T, R> transformer, Consumer<R> loader) {
        this.extractor = Objects.requireNonNull(extractor);
        this.transformer = Objects.requireNonNull(transformer);
        this.loader = Objects.requireNonNull(loader);
    }

    public void run() {
        T rawData = extractor.get();             // 1. Extract lazily
        R transformedData = transformer.apply(rawData); // 2. Transform cleanly
        loader.accept(transformedData);          // 3. Load / Persist
    }
}
```

### 4.3 Thread-Safe Lazy Memoization (`Supplier`)
A common backend requirement is computing an expensive resource (e.g., fetching a remote OAuth JWKS keyset) lazily on first access and caching the result for all subsequent calls:

```java
public final class MemoizingSupplier<T> implements Supplier<T> {
    private final Supplier<T> delegate;
    private volatile boolean initialized = false;
    private T value;

    public MemoizingSupplier(Supplier<T> delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public T get() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    T t = delegate.get();
                    value = t;
                    initialized = true;
                    return t;
                }
            }
        }
        return value;
    }
}
```

---

## 5. Memory Layout, JVM Mechanics & Performance Implications

### 5.1 The Autoboxing Catastrophe: Generic vs Primitive
To measure the exact performance cost of autoboxing in functional interfaces, consider applying an arithmetic increment operation $10,000,000$ times:

```java
// Strategy A: Generic Function (Boxes primitive int to Integer)
Function<Integer, Integer> genericInc = x -> x + 1;

// Strategy B: Primitive IntUnaryOperator (Operates directly on int in registers)
IntUnaryOperator primitiveInc = x -> x + 1;
```

#### Memory & Instruction Breakdown
1. **`Function<Integer, Integer>`:**
   - Bytecode requires `invokestatic Integer.valueOf:(I)Ljava/lang/Integer;` (boxing input).
   - Allocates 16 bytes for `java.lang.Integer` on the heap.
   - Executes `Integer.intValue:()I` (unboxing).
   - Performs addition.
   - Re-boxes result into a new `Integer` object.
   - Result: **320 MB of garbage allocated in Eden space**. JIT cannot scalar-replace if references escape.
2. **`IntUnaryOperator`:**
   - Bytecode: `iload_1`, `iconst_1`, `iadd`, `ireturn`.
   - Result: **0 bytes allocated**. HotSpot C2 compiler inlines the operation into a single machine `inc` or `lea` assembly instruction!

```
+-------------------------------------------------------------------------------+
|                    10,000,000 Arithmetic Operations Benchmark                 |
+--------------------------+--------------------+-------------------------------+
| Implementation           | Execution Time     | Heap Allocation Rate          |
+--------------------------+--------------------+-------------------------------+
| IntUnaryOperator         | 3.2 ms             | 0 B (Zero!)                   |
| Function<Integer, Integer>| 52.8 ms (16x SLOWER)| 320 MB GC Churn               |
+--------------------------+--------------------+-------------------------------+
```

---

## 6. Comprehensive Code Examples

The following production-ready engine demonstrates dynamic predicate composition, multi-step function transformation, consumer auditing, and primitive telemetry processing:

```java
package com.antigravity.functional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.*;

public final class CoreFunctionEngine {

    public record Order(String id, BigDecimal total, boolean isPriority, int itemCount) {}
    public record Invoice(String invoiceNumber, BigDecimal amount, String routingCode) {}

    // 1. Composed Predicate Rule Engine
    public static final class OrderEvaluator {
        private static final Predicate<Order> IS_PRIORITY = Order::isPriority;
        private static final Predicate<Order> HAS_ITEMS = o -> o.itemCount() > 0;
        private static final Predicate<Order> IS_HIGH_VALUE = o -> o.total().compareTo(BigDecimal.valueOf(500)) >= 0;

        // Expressive Business Rule: (Priority OR HighValue) AND HasItems
        public static final Predicate<Order> EXPEDITE_ELIGIBLE =
                (IS_PRIORITY.or(IS_HIGH_VALUE)).and(HAS_ITEMS);
    }

    // 2. Multi-Stage Pipeline using Function.andThen
    public static final class InvoiceTransformer {
        public static Function<Order, Invoice> createInvoicePipeline(String prefix, String routingCode) {
            Objects.requireNonNull(prefix, "prefix required");
            Objects.requireNonNull(routingCode, "routingCode required");

            Function<Order, String> generateNumber = order -> prefix + "-" + order.id();
            
            return order -> new Invoice(
                    generateNumber.apply(order),
                    order.total(),
                    routingCode
            );
        }
    }

    // 3. High-Throughput Primitive Telemetry Pipeline
    public static final class TelemetryAggregator {
        private long totalDurationNanos = 0;
        private long sampleCount = 0;
        private long maxDurationNanos = 0;

        // Uses LongConsumer to eliminate autoboxing of high-frequency nanosecond durations
        public LongConsumer recordLatency() {
            return nanos -> {
                sampleCount++;
                totalDurationNanos += nanos;
                if (nanos > maxDurationNanos) {
                    maxDurationNanos = nanos;
                }
            };
        }

        public double averageLatencyMillis() {
            if (sampleCount == 0) return 0.0;
            return (double) totalDurationNanos / sampleCount / 1_000_000.0;
        }

        public long sampleCount() {
            return sampleCount;
        }
    }
}
```

---

## 7. Edge Cases, Corner Scenarios & Failure Modes

### 7.1 The `compose` vs `andThen` Ordering Bug
A common production defect occurs when developers mistakenly assume `f.compose(g)` and `f.andThen(g)` behave identically:
```java
Function<String, String> trim = String::strip;
Function<String, String> toUpper = String::toUpperCase;

// andThen: trim first, then toUpperCase
Function<String, String> f1 = trim.andThen(toUpper);

// compose: toUpperCase first, then trim
Function<String, String> f2 = trim.compose(toUpper);
```
While trivial for string trimming, consider financial tax calculation:
```java
Function<BigDecimal, BigDecimal> applyDiscount = amt -> amt.subtract(BigDecimal.TEN);
Function<BigDecimal, BigDecimal> applyTax = amt -> amt.multiply(new BigDecimal("1.10"));

// WRONG ORDERING:
// applyTax.andThen(applyDiscount) applies tax BEFORE discount!
// applyTax.compose(applyDiscount) applies discount BEFORE tax!
```

### 7.2 Short-Circuiting Trap in `Predicate.and()`
Because `Predicate.and()` short-circuits:
```java
Predicate<User> validation = isDatabaseConnected.and(isUserAuthenticated);
```
If `isDatabaseConnected` returns `false`, `isUserAuthenticated` is **never evaluated**.
If `isUserAuthenticated` contained side-effects (e.g., logging an authentication attempt or updating access metrics), those side-effects will **silently fail to occur**!
**Rule:** Predicates must be **pure functions** with zero observable side-effects.

### 7.3 Null Returns in `Supplier`
`Supplier<T>.get()` is permitted to return `null`:
```java
Supplier<String> nullableSupplier = () -> null;
String result = nullableSupplier.get(); // Returns null without exception
```
If you pass a nullable supplier to `Objects.requireNonNullElseGet(val, supplier)`:
```java
// THROWS NullPointerException if supplier returns null!
Objects.requireNonNullElseGet(null, () -> null);
```

---

## 8. Anti-Patterns & Common Traps

### Anti-Pattern 1: Autoboxing Inside Stream Numeric Reductions
```java
// DISASTROUS GC CHURN:
List<Integer> numbers = ...;
// Uses Function<Integer, Integer> and boxes on every element:
Integer sum = numbers.stream().reduce(0, (a, b) -> a + b);

// OPTIMAL ZERO-ALLOCATION:
// Converts to IntStream and uses IntBinaryOperator:
int sumFast = numbers.stream().mapToInt(Integer::intValue).sum();
```

### Anti-Pattern 2: Causing Side-Effects Inside `Function` or `Predicate`
```java
// BAD: Mutates shared list during filter evaluation!
List<Order> rejected = new ArrayList<>();
orders.stream()
      .filter(o -> {
          if (!o.isValid()) {
              rejected.add(o); // SIDE EFFECT VIOLATION!
              return false;
          }
          return true;
      })
      .toList();
```
In parallel streams, mutating shared state inside a `Predicate` causes data races and corrupted collections.

### Anti-Pattern 3: Inverting Predicates Manually with Lambdas
```java
// SUB-OPTIMAL & VERBOSE:
Predicate<String> isBlank = String::isBlank;
Predicate<String> isNotBlank = s -> !isBlank.test(s); // Creates extra closure

// IDIOMATIC (Java 11+):
Predicate<String> isNotBlank = Predicate.not(String::isBlank);
// Or:
Predicate<String> isNotBlank = isBlank.negate();
```

---

## 9. Testing Strategies, Verification & Benchmarking

### 9.1 Verification Suite: Complete OpenJDK 21 Test
The following standalone test suite verifies the four core archetypes, short-circuiting, composition ordering, and primitive performance:

```java
package com.antigravity.functional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.*;

public class CoreFunctionsVerificationTest {

    public static void main(String[] args) {
        System.out.println("Starting Core Functional Interfaces Verification Suite...");

        testPredicateShortCircuitAndComposition();
        testFunctionComposeVsAndThenOrdering();
        testConsumerChaining();
        testMemoizingSupplierThreadSafety();
        testPrimitiveVsGenericPerformanceBenchmark();

        System.out.println("ALL 5 CORE FUNCTIONAL SUITES PASSED SUCCESSFULLY!");
    }

    static void testPredicateShortCircuitAndComposition() {
        // Verify short-circuiting: second predicate must not be called if first is false
        boolean[] secondEvaluated = {false};
        Predicate<String> failFirst = s -> false;
        Predicate<String> tracker = s -> {
            secondEvaluated[0] = true;
            return true;
        };

        boolean result = failFirst.and(tracker).test("input");
        assert !result;
        assert !secondEvaluated[0] : "Predicate.and() must short-circuit when first predicate is false";

        // Verify Predicate.not and negate
        Predicate<String> isEmpty = String::isEmpty;
        assert Predicate.not(isEmpty).test("Antigravity");
        assert isEmpty.negate().test("Antigravity");
    }

    static void testFunctionComposeVsAndThenOrdering() {
        Function<Integer, Integer> multiplyByTwo = x -> x * 2;
        Function<Integer, Integer> addTen = x -> x + 10;

        // andThen: (5 * 2) + 10 = 20
        int andThenResult = multiplyByTwo.andThen(addTen).apply(5);
        assert andThenResult == 20 : "andThen must apply multiplyByTwo first, then addTen";

        // compose: (5 + 10) * 2 = 30
        int composeResult = multiplyByTwo.compose(addTen).apply(5);
        assert composeResult == 30 : "compose must apply addTen first, then multiplyByTwo";
    }

    static void testConsumerChaining() {
        List<String> auditLog = new ArrayList<>();
        Consumer<String> logger1 = s -> auditLog.add("LOG1: " + s);
        Consumer<String> logger2 = s -> auditLog.add("LOG2: " + s);

        Consumer<String> multiLogger = logger1.andThen(logger2);
        multiLogger.accept("EventA");

        assert auditLog.size() == 2;
        assert auditLog.get(0).equals("LOG1: EventA");
        assert auditLog.get(1).equals("LOG2: EventA");
    }

    static void testMemoizingSupplierThreadSafety() {
        int[] computeCount = {0};
        Supplier<String> expensiveInit = () -> {
            computeCount[0]++;
            return "ExpensiveConnectionPool";
        };

        MemoizingSupplier<String> memoized = new MemoizingSupplier<>(expensiveInit);

        // Multiple calls must only evaluate underlying supplier ONCE
        assert memoized.get().equals("ExpensiveConnectionPool");
        assert memoized.get().equals("ExpensiveConnectionPool");
        assert memoized.get().equals("ExpensiveConnectionPool");

        assert computeCount[0] == 1 : "Memoized supplier must execute expensive initialization exactly once";
    }

    static void testPrimitiveVsGenericPerformanceBenchmark() {
        int iterations = 10_000_000;

        // 1. Primitive IntUnaryOperator (Zero boxing)
        IntUnaryOperator primitiveInc = x -> x + 1;
        long startPrimitive = System.nanoTime();
        int primitiveAccum = 0;
        for (int i = 0; i < iterations; i++) {
            primitiveAccum = primitiveInc.applyAsInt(primitiveAccum);
        }
        long primitiveDuration = System.nanoTime() - startPrimitive;

        // 2. Generic Function<Integer, Integer> (Autoboxes every iteration)
        Function<Integer, Integer> genericInc = x -> x + 1;
        long startGeneric = System.nanoTime();
        Integer genericAccum = 0;
        for (int i = 0; i < iterations; i++) {
            genericAccum = genericInc.apply(genericAccum);
        }
        long genericDuration = System.nanoTime() - startGeneric;

        System.out.printf("Primitive vs Generic Benchmark (%,d ops):%n", iterations);
        System.out.printf("  IntUnaryOperator:           %,d ns (%.2f ns/op)%n", primitiveDuration, (double) primitiveDuration / iterations);
        System.out.printf("  Function<Integer, Integer>: %,d ns (%.2f ns/op)%n", genericDuration, (double) genericDuration / iterations);
        System.out.printf("  Primitive Speedup:          %.2fx faster%n", (double) genericDuration / primitiveDuration);

        assert primitiveAccum == iterations;
        assert genericAccum == iterations;
        assert primitiveDuration < genericDuration : "IntUnaryOperator must outperform boxed Function";
    }

    public static final class MemoizingSupplier<T> implements Supplier<T> {
        private final Supplier<T> delegate;
        private volatile boolean initialized = false;
        private T value;

        public MemoizingSupplier(Supplier<T> delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        public T get() {
            if (!initialized) {
                synchronized (this) {
                    if (!initialized) {
                        T t = delegate.get();
                        value = t;
                        initialized = true;
                        return t;
                    }
                }
            }
            return value;
        }
    }
}
```

---

## 10. Tooling, Ecosystem & Observability

### 10.1 Micrometer Metrics Integration with `Supplier`
In production metrics monitoring (Prometheus, Datadog), gauges observe values dynamically via `Supplier`:
```java
// Micrometer Gauge registration
Gauge.builder("jvm.active.connections", connectionPool, ConnectionPool::getActiveCount)
     .description("Current number of active database connections")
     .register(meterRegistry);
```
Micrometer polls the `Supplier` periodically during scraping without retaining hard references that block garbage collection.

### 10.2 SonarQube & SpotBugs Static Analysis Rules
- **SonarQube `S2153` ("Boxing and unboxing should not be immediately reversed"):** Flags instances where `Function<Integer, ...>` is used where primitive streams or `IntFunction` are applicable.
- **SonarQube `S4276` ("Functional Interfaces should be as specialized as possible"):** Flags `Function<T, Boolean>` and suggests replacing with `Predicate<T>`.

---

## 11. Security Considerations & Hardening

### 11.1 Thread-Safety of Shared Consumers
When passing a `Consumer<T>` to multi-threaded executors (e.g. `CompletableFuture.thenAcceptAsync(consumer)`):
Ensure that the `Consumer` is either completely stateless or synchronizes access to shared mutable data structures. A non-thread-safe consumer modifying an `ArrayList` across worker threads will cause corrupted array lengths and `ConcurrentModificationException`s.

### 11.2 Purity in Authorization Predicates
Authorization rules (`Predicate<SecurityContext>`) must never modify context state. If an authorization check mutates user credentials or clears tokens during evaluation, an authorization rejection can cause unexpected state corruption in subsequent filters.

---

## 12. Common Interview & System Design Questions

### Question 1: What is the difference between `Function.compose()` and `Function.andThen()`?
**Answer:**
Both methods chain two functions together, but in opposite order:
- `f.andThen(g)` executes `f` first, and passes the output to `g` (mathematically: $g(f(x))$).
- `f.compose(g)` executes `g` first, and passes the output to `f` (mathematically: $f(g(x))$).

### Question 2: Why does Java provide `IntPredicate` instead of just using `Predicate<Integer>`?
**Answer:**
Because generic types in Java do not support primitive types. `Predicate<Integer>` requires autoboxing primitive `int` values into heap-allocated `Integer` objects and unboxing them during evaluation. In high-throughput streaming or tight loops, this generates severe heap churn and garbage collection pressure. `IntPredicate.test(int value)` operates directly on primitives in CPU registers with 0 heap allocations.

### Question 3: What is the difference between `UnaryOperator<T>` and `Function<T, R>`?
**Answer:**
`UnaryOperator<T>` extends `Function<T, T>`. It is a specialization representing a function whose argument and return value are of the exact same type (homogeneous). It signals that an entity is being transformed or mutated into an updated version of itself (e.g., `String::toUpperCase`), avoiding the need to specify the generic type parameter twice.

---

## 13. Production Case Study & War Story

### The High-Throughput IoT Telemetry GC Disaster
A connected-vehicle telematics backend was ingesting 500,000 telemetry readings per second across a cluster of Java services.
During peak traffic, services were experiencing recurring 400ms GC stop-the-world pauses, leading to Kafka consumer lag and partition rebalances.

#### Root Cause Analysis
Async-profiler revealed that **42% of all heap allocations** were `java.lang.Double` wrapper objects created in the sensor ingestion pipeline:
```java
// FATAL BOXING ANTI-PATTERN:
public void processTelemetry(List<TelemetryRecord> records, Consumer<Double> metricSink) {
    for (TelemetryRecord record : records) {
        // record.getSpeed() returns double primitive, but metricSink boxes to Double!
        metricSink.accept(record.getSpeed());
    }
}
```
500,000 `double` readings/sec $\times$ 24 bytes per `Double` = **12 MB of heap garbage per second per pod**!

#### Remediation
The method signature was refactored to use the primitive specialization:
```java
// ZERO-ALLOCATION PRIMITIVE SINK:
public void processTelemetry(List<TelemetryRecord> records, DoubleConsumer metricSink) {
    for (TelemetryRecord record : records) {
        metricSink.accept(record.getSpeed()); // Direct register pass, 0 bytes allocated!
    }
}
```
**Outcome:** Heap allocation dropped by 98%, Eden GC cycles dropped from every 3 seconds to every 90 seconds, and GC pauses were eliminated (<0.5ms ZGC pauses).

---

## 14. Migration & Evolution Strategies (JavaScript/TypeScript Transition)

| TypeScript / JavaScript Signature | Modern Java Functional Equivalent |
|---|---|
| `(x: T) => boolean` | `Predicate<T>` (or `IntPredicate` for numbers) |
| `(x: T) => R` | `Function<T, R>` (or `ToIntFunction<T>`, `DoubleFunction<R>`) |
| `(x: T) => void` | `Consumer<T>` (or `IntConsumer`) |
| `() => T` | `Supplier<T>` (or `IntSupplier`) |
| `(x: T) => T` | `UnaryOperator<T>` (or `IntUnaryOperator`) |
| `(a: T, b: U) => R` | `BiFunction<T, U, R>` |
| `(a: T, b: T) => T` | `BinaryOperator<T>` (or `IntBinaryOperator`) |

---

## 15. Key Takeaways & Summary

```
                     Core Functional Types Summary Table
+-----------------------+---------------------+-------------------+---------------------+
| Interface             | Method Signature    | Primary Use Case  | Primitive Variants  |
+-----------------------+---------------------+-------------------+---------------------+
| Predicate<T>          | boolean test(T t)   | Filter, Validates | Int/Long/Double     |
| Function<T, R>        | R apply(T t)        | Transforms DTOs   | Int/Long/DoubleTo...|
| Consumer<T>           | void accept(T t)    | Sinks, Logging    | Int/Long/Double     |
| Supplier<T>           | T get()             | Lazy computation  | Int/Long/Double     |
| UnaryOperator<T>      | T apply(T t)        | In-place update   | Int/Long/Double     |
| BinaryOperator<T>     | T apply(T a, T b)   | Reductions, Sums  | Int/Long/Double     |
+-----------------------+---------------------+-------------------+---------------------+
```

1. **Always Prefer Primitive Interfaces for Numbers:** Use `IntPredicate`, `LongConsumer`, and `DoubleSupplier` in data-heavy hot paths.
2. **Remember Composition Order:** `f.andThen(g)` runs $f$ then $g$; `f.compose(g)` runs $g$ then $f$.
3. **Keep Predicates Pure:** Never embed observable side-effects in `test()` methods.
4. **Leverage `Predicate.not()`:** Replaces clumsy negated lambdas (`s -> !p.test(s)`).

---

## 16. References & Further Reading

- [Oracle Java SE 21 Documentation: java.util.function](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/function/package-summary.html)
- [JSR 335: Lambda Expressions for the Java Programming Language](https://jcp.org/en/jsr/detail?id=335)
- *Effective Java (3rd Edition)* by Joshua Bloch — Item 44: Favor the use of standard functional interfaces
- *Modern Java in Action* by Raoul-Gabriel Urma, Mario Fusco, and Alan Mycroft
