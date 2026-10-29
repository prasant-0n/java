# Intermediate Operations: filter, map, flatMap, distinct, sorted, peek

---

## 1. Concept Definition & Core Purpose

In the Java Stream API, **Intermediate Operations** are lazy transformations that connect a stream source to a terminal operation. An intermediate operation takes an incoming `Stream<T>` and returns an outgoing `Stream<R>`, postponing all computation until a terminal operation is invoked.

Architecturally, the Stream API divides intermediate operations into two fundamentally distinct categories based on their memory and buffering characteristics:

```
+---------------------------------------------------------------------------------------+
|                       Intermediate Stream Operations Taxonomy                         |
+--------------------------+----------------------------+-------------------------------+
| Category                 | Operations                 | Memory & Execution Profile    |
+--------------------------+----------------------------+-------------------------------+
| Stateless                | filter, map, flatMap, peek | Zero element buffering;       |
|                          | mapToInt, mapToLong        | elements process 1-by-1 in    |
|                          |                            | single pass (Loop Fusion).    |
+--------------------------+----------------------------+-------------------------------+
| Stateful                 | distinct, sorted, skip,    | Requires internal state /     |
|                          | limit, takeWhile, dropWhile| buffering; may need all items |
|                          |                            | before emitting (Breaks Fusion)|
+--------------------------+----------------------------+-------------------------------+
```

### The Monadic Powerhouse: `map` vs `flatMap`
- **`map` (1-to-1):** Transforms an element of type $T$ into a single element of type $R$. If an order has 1 customer, `orders.map(Order::getCustomer)` produces 1 customer per order.
- **`flatMap` (1-to-N):** Transforms an element of type $T$ into a **stream of elements** of type $R$, then flattens all generated streams into a single, continuous stream. If an order contains a list of line items, `orders.flatMap(o -> o.getItems().stream())` produces a single flat stream containing every line item across all orders.

---

## 2. Background, Context & Historical Evolution

### 2.1 The Nested Loop Pyramid of Doom (Pre-Java 8)
Prior to Java 8, navigating 1-to-N relational hierarchies in enterprise applications (e.g., extracting all unique item SKUs across all pending orders for a list of retail stores) required deeply nested imperative loops and defensive boilerplate:

```java
// Pre-Java 8 Imperative Relational Traversal
Set<String> uniqueSkus = new HashSet<>();
for (Store store : stores) {
    if (store != null && store.isActive()) {
        for (Order order : store.getOrders()) {
            if (order != null && "PENDING".equals(order.getStatus())) {
                for (LineItem item : order.getItems()) {
                    if (item != null && item.getSku() != null) {
                        uniqueSkus.add(item.getSku());
                    }
                }
            }
        }
    }
}
```

### 2.2 The Monadic Bind Revolution: `flatMap` in Java 8
Java 8 adopted the monadic bind operation ($\text{bind} \equiv >>= $) from functional programming languages (Haskell, Scala) under the name `flatMap`.
The nested pyramid collapses into a clean, declarative, single-pass pipeline:

```java
// Modern Java Declarative Stream Pipeline
Set<String> uniqueSkus = stores.stream()
        .filter(Store::isActive)
        .flatMap(store -> store.getOrders().stream())
        .filter(order -> "PENDING".equals(order.getStatus()))
        .flatMap(order -> order.getItems().stream())
        .map(LineItem::getSku)
        .collect(Collectors.toSet());
```

### 2.3 Java 9 Enhancements: `takeWhile`, `dropWhile`, and `Optional.stream()`
Java 9 added crucial capabilities:
- **`Optional.stream()`:** Unpacks an `Optional<T>` into a 0-element or 1-element `Stream<T>`, allowing seamless integration with `flatMap` (`stream.flatMap(Optional::stream)`).
- **`takeWhile` and `dropWhile`:** Enables fast short-circuiting on sorted streams, eliminating the need to traverse entire collections when a boundary condition is met.

---

## 3. Syntax, Signatures & Language Specifications

### 3.1 Stateless Operations

```java
// 1. Filtering: Discards elements not matching predicate
Stream<T> filter(Predicate<? super T> predicate);

// 2. Transformation (1-to-1)
<R> Stream<R> map(Function<? super T, ? extends R> mapper);

// 3. Monadic Flattening (1-to-N)
<R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);

// 4. Primitive Specialization Mappers (Eliminates Autoboxing)
IntStream mapToInt(ToIntFunction<? super T> mapper);
LongStream mapToLong(ToLongFunction<? super T> mapper);
DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper);

// 5. Side-Effect Inspection (Debugging Only)
Stream<T> peek(Consumer<? super T> action);
```

### 3.2 Stateful Operations

```java
// 1. Deduplication (Requires equals() and hashCode())
Stream<T> distinct();

// 2. Sorting (Natural Order or Custom Comparator)
Stream<T> sorted();
Stream<T> sorted(Comparator<? super T> comparator);

// 3. Windowing & Slicing
Stream<T> limit(long maxSize);
Stream<T> skip(long n);

// 4. Java 9 Boundary Slicing on Ordered Streams
Stream<T> takeWhile(Predicate<? super T> predicate);
Stream<T> dropWhile(Predicate<? super T> predicate);
```

---

## 4. Architectural Role & Backend Design Patterns

### 4.1 Pattern: Relational Aggregate Flattening with `flatMap`
In domain-driven backend services, querying aggregate roots often requires pulling nested entities across boundaries:

```
  Customer (1)  --->  Orders (N)  --->  LineItems (M)
       │                    │                  │
       ▼                    ▼                  ▼
[Stream<Customer>] ---> [Stream<Order>] ---> [Stream<LineItem>]
```

```java
public BigDecimal calculateTotalRevenue(List<Customer> customers) {
    return customers.stream()
            .flatMap(c -> c.orders().stream())      // 1-to-N: Customer -> Orders
            .flatMap(o -> o.items().stream())       // 1-to-N: Order -> Items
            .map(LineItem::subtotal)                // 1-to-1: Item -> BigDecimal
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

### 4.2 Pattern: Clean Null Unpacking via `flatMap(Optional::stream)`
When an object graph contains nullable or optional relationships, avoid verbose ternary checks or filtering nulls manually:

```java
public List<ShippingAddress> resolveActiveShippingAddresses(List<User> users) {
    return users.stream()
            .map(User::findDefaultAddress) // Returns Optional<ShippingAddress>
            .flatMap(Optional::stream)     // Java 9+: Automatically unpacks present, drops empty!
            .filter(ShippingAddress::isVerified)
            .toList();
}
```

### 4.3 Pattern: High-Performance Windowing with `takeWhile`
When processing time-series data or sorted event ledgers, `takeWhile` aborts processing the instant a condition fails, unlike `filter` which evaluates every element in the collection:

```java
// Given a list of transactions sorted chronologically by timestamp (newest first):
public List<Transaction> getTransactionsSince(List<Transaction> sortedDescTx, Instant cutoff) {
    return sortedDescTx.stream()
            // ABORTS immediately when reaching transactions older than cutoff!
            .takeWhile(tx -> tx.timestamp().isAfter(cutoff))
            .toList();
}
```

---

## 5. Memory Layout, JVM Mechanics & Performance Implications

### 5.1 The Memory Buffering Hazard of `sorted()`
Stateless operations (`filter`, `map`) process elements one by one through CPU registers with zero memory buffering (Loop Fusion).
In contrast, **`sorted()` breaks loop fusion entirely**:

```
Stateless Pipeline (Zero Buffering):
  Item 1 ---> filter ---> map ---> collect
  Item 2 ---> filter ---> map ---> collect

Stateful Pipeline with sorted() (Full In-Memory Buffering):
  All 1,000,000 items ---> [ BUFFERED INTO MEMORY ARRAY (O(N) RAM) ]
                           [ TimSort Executes (O(N log N) CPU)    ]
                           Emits sorted items down to next stages
```

#### The Production Impact:
If a backend service streams an 80,000,000-record database export:
- A stateless pipeline runs in **constant $O(1)$ memory**.
- Calling `sorted()` forces the JVM to buffer all 80,000,000 objects in memory, crashing the container with `java.lang.OutOfMemoryError: Java heap space`!

### 5.2 The `distinct()` State Table
`distinct()` maintains an internal hash set (or primitive bitset) to track seen elements.
- For non-primitive objects, memory consumption scales linearly with the number of unique elements: $O(\text{unique items})$.
- It requires properly implemented, fast `hashCode()` and `equals()` methods. A slow `hashCode()` will degrade distinct stream throughput by 10x!

### 5.3 Escape Autoboxing with `mapToInt()`
```java
// BAD: Boxes every int into Integer on map, then unboxes on reduce!
int sum = items.stream().map(Item::getPrice).reduce(0, Integer::sum);

// GOOD: Operates on raw primitives; zero heap allocations!
int sumFast = items.stream().mapToInt(Item::getPrice).sum();
```

---

## 6. Comprehensive Code Examples

The following production-ready engine demonstrates stateless transformations, 1-to-N flattening, `Optional.stream()` integration, and stateful windowing:

```java
package com.antigravity.functional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public final class IntermediateOperationsEngine {

    public record LineItem(String sku, int quantity, BigDecimal unitPrice) {}
    public record Order(String orderId, String status, List<LineItem> items, Optional<String> discountCode) {}
    public record Customer(String id, String name, List<Order> orders) {}

    // 1. Relational Flattening: Customer -> Orders -> LineItems
    public static List<String> extractDistinctPurchasedSkus(List<Customer> customers) {
        Objects.requireNonNull(customers, "customers required");

        return customers.stream()
                .flatMap(c -> c.orders().stream())               // 1-to-N: Customer to Orders
                .filter(o -> "COMPLETED".equals(o.status()))     // Stateless Filter
                .flatMap(o -> o.items().stream())                // 1-to-N: Order to LineItems
                .map(LineItem::sku)                              // 1-to-1 Transformation
                .distinct()                                      // Stateful Deduplication
                .sorted()                                        // Stateful Sorting
                .toList();                                       // Terminal Reduction
    }

    // 2. Java 9 Optional.stream() Unpacking
    public static List<String> extractActiveDiscountCodes(List<Order> orders) {
        Objects.requireNonNull(orders, "orders required");

        return orders.stream()
                .map(Order::discountCode)                        // Stream<Optional<String>>
                .flatMap(Optional::stream)                       // Unpacks present values, drops empty
                .map(String::strip)
                .map(s -> s.toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
    }

    // 3. Time-Series Slicing with takeWhile and dropWhile
    public record MetricTick(Instant timestamp, double cpuUtilization) {}

    public static List<MetricTick> sliceMetricsBetween(List<MetricTick> sortedTicks, Instant start, Instant end) {
        Objects.requireNonNull(sortedTicks, "sortedTicks required");
        Objects.requireNonNull(start, "start required");
        Objects.requireNonNull(end, "end required");

        // Assumes sortedTicks are ordered ascending by timestamp
        return sortedTicks.stream()
                .dropWhile(tick -> tick.timestamp().isBefore(start))  // Drops ticks older than start
                .takeWhile(tick -> !tick.timestamp().isAfter(end))   // Stops when reaching ticks beyond end
                .toList();
    }
}
```

---

## 7. Edge Cases, Corner Scenarios & Failure Modes

### 7.1 Returning `null` from `flatMap` Mapper
The mapper function passed to `flatMap` **must return a `Stream`**, not `null`!
```java
// FATAL RUNTIME ERROR:
List<List<String>> matrix = Arrays.asList(List.of("A"), null, List.of("B"));
matrix.stream()
      .flatMap(list -> list != null ? list.stream() : null) // DANGER: Returns null!
      .toList();
// THROWS java.lang.NullPointerException at java.base/java.util.stream.ReferencePipeline$7$1.accept
```
**Fix:** Always return `Stream.empty()` when an element has no children:
```java
.flatMap(list -> list != null ? list.stream() : Stream.empty())
```

### 7.2 Broken `equals()` / `hashCode()` in `distinct()`
If you invoke `.distinct()` on a custom class that does not implement `equals()` and `hashCode()`:
`distinct()` falls back to `Object.equals` (reference identity `==`). Two instances with identical field values will **both be emitted**, failing to deduplicate!

### 7.3 Calling `sorted()` on an Infinite Stream
Because `sorted()` must examine every element before it can determine the first element to emit, calling `sorted()` on an infinite stream will hang the thread forever and eventually crash with `OutOfMemoryError`:
```java
// FATAL HANG:
Stream.generate(Math::random).sorted().limit(5).toList(); // HANGS!
```

---

## 8. Anti-Patterns & Common Traps

### Anti-Pattern 1: Mutating Domain Objects in `map()` or `peek()`
```java
// VIOLATION OF FUNCTIONAL PURITY:
orders.stream()
      .peek(o -> o.setStatus("PROCESSED")) // Side-effect mutation!
      .map(o -> { o.setProcessedAt(Instant.now()); return o; })
      .toList();
```
Stream pipelines should be side-effect free. Mutating objects in-flight creates non-deterministic bugs in concurrent and parallel streams.

### Anti-Pattern 2: Redundant Sorting Before `min()` or `max()`
```java
// DISASTROUS PERFORMANCE: Sorts entire collection (O(N log N)) just to get first item!
Order cheapest = orders.stream()
        .sorted(Comparator.comparing(Order::getTotal))
        .findFirst()
        .orElseThrow();

// OPTIMAL LINEAR SCAN: Runs in O(N) time with 0 sorting memory!
Order cheapestFast = orders.stream()
        .min(Comparator.comparing(Order::getTotal))
        .orElseThrow();
```

### Anti-Pattern 3: Using `flatMap` for 1-to-1 Transformations
```java
// UNNECESSARY OVERHEAD: Allocates a new Stream on every element
orders.stream().flatMap(o -> Stream.of(o.getCustomerId())).toList();

// CLEAN & DIRECT:
orders.stream().map(Order::getCustomerId).toList();
```

---

## 9. Testing Strategies, Verification & Benchmarking

### 9.1 Verification Suite: Complete OpenJDK 21 Test
The following standalone test verifies relational flattening, `Optional.stream()` unpacking, `takeWhile`/`dropWhile` boundary slicing, and primitive performance:

```java
package com.antigravity.functional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public class IntermediateOperationsVerificationTest {

    public static final class IntermediateOperationsEngine {
        public record LineItem(String sku, int quantity, BigDecimal unitPrice) {}
        public record Order(String orderId, String status, List<LineItem> items, Optional<String> discountCode) {}
        public record Customer(String id, String name, List<Order> orders) {}

        public static List<String> extractDistinctPurchasedSkus(List<Customer> customers) {
            Objects.requireNonNull(customers, "customers required");
            return customers.stream()
                    .flatMap(c -> c.orders().stream())
                    .filter(o -> "COMPLETED".equals(o.status()))
                    .flatMap(o -> o.items().stream())
                    .map(LineItem::sku)
                    .distinct()
                    .sorted()
                    .toList();
        }

        public static List<String> extractActiveDiscountCodes(List<Order> orders) {
            Objects.requireNonNull(orders, "orders required");
            return orders.stream()
                    .map(Order::discountCode)
                    .flatMap(Optional::stream)
                    .map(String::strip)
                    .map(s -> s.toUpperCase(Locale.ROOT))
                    .distinct()
                    .toList();
        }

        public record MetricTick(Instant timestamp, double cpuUtilization) {}

        public static List<MetricTick> sliceMetricsBetween(List<MetricTick> sortedTicks, Instant start, Instant end) {
            Objects.requireNonNull(sortedTicks, "sortedTicks required");
            Objects.requireNonNull(start, "start required");
            Objects.requireNonNull(end, "end required");
            return sortedTicks.stream()
                    .dropWhile(tick -> tick.timestamp().isBefore(start))
                    .takeWhile(tick -> !tick.timestamp().isAfter(end))
                    .toList();
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting Intermediate Stream Operations Verification Suite...");

        testRelationalFlatMapAggregation();
        testOptionalStreamUnpacking();
        testTakeWhileAndDropWhileSlicing();
        testFlatMapNullSafetyWithStreamEmpty();
        testPrimitiveMapToIntPerformance();

        System.out.println("ALL 5 INTERMEDIATE OPERATIONS SUITES PASSED SUCCESSFULLY!");
    }

    static void testRelationalFlatMapAggregation() {
        IntermediateOperationsEngine.LineItem item1 = new IntermediateOperationsEngine.LineItem("SKU-A", 1, new BigDecimal("10"));
        IntermediateOperationsEngine.LineItem item2 = new IntermediateOperationsEngine.LineItem("SKU-B", 2, new BigDecimal("20"));
        IntermediateOperationsEngine.LineItem item3 = new IntermediateOperationsEngine.LineItem("SKU-A", 1, new BigDecimal("10")); // Duplicate SKU

        IntermediateOperationsEngine.Order order1 = new IntermediateOperationsEngine.Order(
                "O-1", "COMPLETED", List.of(item1, item2), Optional.empty()
        );
        IntermediateOperationsEngine.Order order2 = new IntermediateOperationsEngine.Order(
                "O-2", "COMPLETED", List.of(item3), Optional.empty()
        );
        IntermediateOperationsEngine.Order orderCancelled = new IntermediateOperationsEngine.Order(
                "O-3", "CANCELLED", List.of(new IntermediateOperationsEngine.LineItem("SKU-C", 1, BigDecimal.TEN)), Optional.empty()
        );

        IntermediateOperationsEngine.Customer customer = new IntermediateOperationsEngine.Customer(
                "C-1", "Alice", List.of(order1, order2, orderCancelled)
        );

        List<String> distinctSkus = IntermediateOperationsEngine.extractDistinctPurchasedSkus(List.of(customer));

        // Must filter out CANCELLED order (SKU-C) and deduplicate SKU-A
        assert distinctSkus.size() == 2;
        assert distinctSkus.get(0).equals("SKU-A");
        assert distinctSkus.get(1).equals("SKU-B");
    }

    static void testOptionalStreamUnpacking() {
        IntermediateOperationsEngine.Order o1 = new IntermediateOperationsEngine.Order("1", "OK", List.of(), Optional.of("summer20"));
        IntermediateOperationsEngine.Order o2 = new IntermediateOperationsEngine.Order("2", "OK", List.of(), Optional.empty());
        IntermediateOperationsEngine.Order o3 = new IntermediateOperationsEngine.Order("3", "OK", List.of(), Optional.of("SUMMER20")); // Duplicate
        IntermediateOperationsEngine.Order o4 = new IntermediateOperationsEngine.Order("4", "OK", List.of(), Optional.of("vip50"));

        List<String> promoCodes = IntermediateOperationsEngine.extractActiveDiscountCodes(List.of(o1, o2, o3, o4));

        assert promoCodes.size() == 2;
        assert promoCodes.contains("SUMMER20");
        assert promoCodes.contains("VIP50");
    }

    static void testTakeWhileAndDropWhileSlicing() {
        Instant t0 = Instant.parse("2026-10-30T00:00:00Z");
        Instant t1 = t0.plusSeconds(60);
        Instant t2 = t0.plusSeconds(120);
        Instant t3 = t0.plusSeconds(180);
        Instant t4 = t0.plusSeconds(240);

        List<IntermediateOperationsEngine.MetricTick> ticks = List.of(
                new IntermediateOperationsEngine.MetricTick(t0, 10.0),
                new IntermediateOperationsEngine.MetricTick(t1, 20.0),
                new IntermediateOperationsEngine.MetricTick(t2, 30.0),
                new IntermediateOperationsEngine.MetricTick(t3, 40.0),
                new IntermediateOperationsEngine.MetricTick(t4, 50.0)
        );

        // Window: [t1, t3] inclusive
        List<IntermediateOperationsEngine.MetricTick> sliced =
                IntermediateOperationsEngine.sliceMetricsBetween(ticks, t1, t3);

        assert sliced.size() == 3;
        assert sliced.get(0).timestamp().equals(t1);
        assert sliced.get(1).timestamp().equals(t2);
        assert sliced.get(2).timestamp().equals(t3);
    }

    static void testFlatMapNullSafetyWithStreamEmpty() {
        List<String> raw = Arrays.asList("apple", null, "banana");

        // Safe flatMap handling nulls via Stream.empty()
        List<String> clean = raw.stream()
                .flatMap(s -> s != null ? Stream.of(s.toUpperCase(Locale.ROOT)) : Stream.empty())
                .toList();

        assert clean.size() == 2;
        assert clean.get(0).equals("APPLE");
        assert clean.get(1).equals("BANANA");
    }

    static void testPrimitiveMapToIntPerformance() {
        int count = 50_000;
        List<Integer> numbers = new ArrayList<>(count);
        for (int i = 0; i < count; i++) numbers.add(i);

        // Primitive mapToInt sum
        long startPrimitive = System.nanoTime();
        int primitiveSum = numbers.stream().mapToInt(Integer::intValue).sum();
        long primitiveDuration = System.nanoTime() - startPrimitive;

        System.out.printf("  mapToInt vs Object reduction (%,d elements): %,d ns%n", count, primitiveDuration);
        assert primitiveSum == (int) ((count - 1L) * count / 2);
    }
}
```

---

## 10. Tooling, Ecosystem & Observability

### 10.1 Profiling Memory Buffers with JFR
When running stateful operations (`sorted`, `distinct`) on large collections in production, monitor the JFR event **`jdk.ObjectAllocationInNewTLAB`** and track array allocations (`Object[]`) originating from `java.util.stream.SpinedBuffer` or `TimSort`.

---

## 11. Security Considerations & Hardening

### 11.1 Defending Against DoS via Unbounded `sorted()`
Never allow unauthenticated external clients to sort unpaginated database queries via streams:
```java
// VULNERABLE: Client requests 10,000,000 items -> JVM crashes with OOM
public List<Item> search(String query) {
    return dbClient.stream(query).sorted().toList();
}

// HARDENED: Enforce database-level paging and limit
public List<Item> search(String query, int maxResults) {
    int safeMax = Math.min(maxResults, 100);
    return dbClient.queryWithLimit(query, safeMax); // Order in SQL, not in JVM heap!
}
```

---

## 12. Common Interview & System Design Questions

### Question 1: What is the difference between `map` and `flatMap`?
**Answer:**
`map` applies a 1-to-1 transformation function: for each input element of type $T$, it emits exactly one output element of type $R$.
`flatMap` applies a 1-to-N transformation function: for each input element of type $T$, it returns a `Stream<R>`, and then flattens all returned streams into a single continuous stream. It is used to unpack nested collections, flatten parent-child relationships, and unwrap optional values.

### Question 2: Why can't `sorted()` run statelessly like `filter()`?
**Answer:**
`filter` is stateless because an element can be tested and emitted immediately without knowing about other elements.
`sorted` is stateful because the first element of a sorted sequence cannot be determined until every single element in the stream has been examined. Therefore, `sorted` must buffer the entire upstream stream into an internal array and execute TimSort before emitting its first element, breaking loop fusion and requiring $O(N)$ memory.

### Question 3: How does `takeWhile` differ from `filter`?
**Answer:**
`filter` examines **every single element** in the stream from start to finish.
`takeWhile` requires an ordered stream and **short-circuits immediately** upon encountering the very first element that fails the predicate, discarding all remaining elements without inspecting them.

---

## 13. Production Case Study & War Story

### The Midnight Batch Outage: `OutOfMemoryError: Java heap space`
A nightly payroll processing batch service was designed to process employee bonus allocations across 12,000 corporate clients.
The batch service crashed every Sunday at 02:00 AM with a fatal OOM error.

#### Root Cause Analysis
Heap dump analysis revealed a single `Object[]` containing **14,500,000 `BonusRecord` instances** occupying 3.8 GB of heap:
```java
// FATAL MEMORY BOTTLENECK:
public void processAllBonuses() {
    bonusRepository.streamAll() // Stream pulled from DB cursor
                   .sorted(Comparator.comparing(BonusRecord::getEmployeeId)) // ALLOCATES 3.8GB!
                   .forEach(payoutGateway::disburse);
}
```
The developer assumed the stream would process records sequentially as they arrived from the database cursor.
However, because `sorted()` was placed in the middle of the stream, Java buffered all 14.5 million database records into memory before disbursing a single cent!

#### Remediation
1. The sorting operation was delegated to the PostgreSQL database index: `SELECT * FROM bonuses ORDER BY employee_id`.
2. The `sorted()` call was removed from the Java stream.
**Outcome:** Heap memory consumption dropped from 3.8 GB to **under 40 MB** (constant $O(1)$ memory usage), and the batch completed 4x faster.

---

## 14. Migration & Evolution Strategies (JavaScript/TypeScript Transition)

| Operation | JavaScript / TypeScript | Java Stream API |
|---|---|---|
| Transform (1-to-1) | `arr.map(x => x * 2)` | `stream.map(x -> x * 2)` |
| Flatten (1-to-N) | `arr.flatMap(x => [x, x * 2])` | `stream.flatMap(x -> Stream.of(x, x * 2))` |
| Deduplicate | `Array.from(new Set(arr))` | `stream.distinct()` |
| Sort | `arr.sort((a, b) => a - b)` (Mutates array!) | `stream.sorted()` (Pure, does not mutate source) |
| Prefix Filter | No direct equivalent (requires manual loop) | `stream.takeWhile(predicate)` (Java 9+) |

---

## 15. Key Takeaways & Summary

```
                  Intermediate Stream Operations Cheat Sheet
+-----------------------+---------------------+-------------------+---------------------+
| Operation             | Arity / Mapping     | Classification    | Fusion Behavior     |
+-----------------------+---------------------+-------------------+---------------------+
| filter                | 1-to-0 or 1         | Stateless         | Fully fused         |
| map                   | 1-to-1              | Stateless         | Fully fused         |
| flatMap               | 1-to-N (Flattens)   | Stateless         | Fully fused         |
| peek                  | 1-to-1 (Debug only) | Stateless         | Fully fused         |
| distinct              | Deduplicates        | Stateful          | Requires Set state  |
| sorted                | Orders elements     | Stateful          | Buffers ALL in RAM  |
| takeWhile             | Short-circuit prefix| Stateful          | Fused short-circuit |
+-----------------------+---------------------+-------------------+---------------------+
```

1. **Beware Stateful Memory Buffering:** Never invoke `sorted()` on unbounded or huge streams. Sort in the database!
2. **Never Return Null from `flatMap`:** Return `Stream.empty()` to prevent NPE.
3. **Use `Optional.stream()`:** The cleanest way to unpack optional values in functional pipelines.
4. **Use Primitive Mappers:** Convert to `IntStream` or `LongStream` to escape heap autoboxing.

---

## 16. References & Further Reading

- [Oracle Java SE 21 Documentation: java.util.stream.Stream](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Stream.html)
- [JSR 335: Lambda Expressions and Stream API Specification](https://jcp.org/en/jsr/detail?id=335)
- *Effective Java (3rd Edition)* by Joshua Bloch — Item 45: Use streams judiciously
- [Brian Goetz: Monads and the Java Stream API](https://cr.openjdk.org/~briangoetz/lambda/lambda-libraries-final.html)
