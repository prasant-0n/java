# LinkedHashMap and TreeMap: Ordering, Red-Black Trees, and LRU Caches

---

## 1. Concept Definition & Core Purpose

In standard hash tables like `HashMap`, element iteration order is non-deterministic. It depends on hash codes, table capacities, and internal treeification, and can abruptly shift whenever a table resize occurs. 

To satisfy enterprise requirements for predictable encounter order, sorted keys, and cache eviction mechanics, the Java Collections Framework (JCF) provides two specialized, ordered `Map` implementations:
1. **`java.util.LinkedHashMap<K, V>`:** Extends `HashMap<K, V>`. Maintains a doubly-linked list running through all its entries. By default, it preserves the **exact insertion order** of keys. Alternatively, it can be configured in **access order** (Least Recently Used - LRU), where querying an element relocates it to the tail of the list.
2. **`java.util.TreeMap<K, V>`:** Implements `java.util.NavigableMap<K, V>` (which extends `SortedMap<K, V>` and `SequencedMap<K, V>`). It guarantees that keys remain strictly sorted according to their natural ordering (`Comparable<K>`) or a custom `Comparator<? super K>`. It is backed by a self-balancing Red-Black binary search tree.

```
                          ┌───────────────────────┐
                          │       Map<K, V>       │
                          └───────────▲───────────┘
                                      │
              ┌───────────────────────┴───────────────────────┐
              │                                               │
    ┌─────────┴─────────┐                           ┌─────────┴─────────┐
    │   HashMap<K, V>   │                           │SequencedMap<K, V> │
    └─────────▲─────────┘                           └───▲─────────────▲─┘
              │                                         │             │
    ┌─────────┴─────────┐                               │   ┌─────────┴─────────┐
    │ LinkedHashMap<K,V>├───────────────────────────────┘   │  SortedMap<K, V>  │
    └───────────────────┘                                   └─────────▲─────────┘
                                                                      │
                                                            ┌─────────┴─────────┐
                                                            │ NavigableMap<K, V>│
                                                            └─────────▲─────────┘
                                                                      │
                                                            ┌─────────┴─────────┐
                                                            │   TreeMap<K, V>   │
                                                            └───────────────────┘
```

### Core Invariants:
- **`LinkedHashMap` Invariant:** Insertion order is immutable to re-insertion (calling `put(k, v)` on an existing key updates its value but does not alter its position). In access-order mode (`accessOrder = true`), every `get()` or `put()` relocates the target entry to the tail (MRU - Most Recently Used position).
- **`TreeMap` Invariant:** Keys are continuously balanced in a Red-Black tree of height $h \le 2 \log_2(N + 1)$. Key equality is defined **strictly by `compare(k1, k2) == 0` or `k1.compareTo(k2) == 0`**, completely bypassing `equals()` and `hashCode()`.
- **Null Key Contract:** `LinkedHashMap` permits a single `null` key (stored in bucket 0); `TreeMap` **rejects `null` keys** with `NullPointerException` (unless a custom `Comparator` with `nullsFirst`/`nullsLast` is provided).

---

## 2. Background, Context & Historical Evolution

### The Need for Deterministic Iteration (Java 1.4)
Prior to JDK 1.4, Java developers had to choose between fast, non-deterministic `HashMap` ($O(1)$) and sorted `TreeMap` ($O(\log N)$). There was no built-in data structure capable of delivering $O(1)$ lookup while preserving insertion sequence. Web applications serializing form data or caching HTTP sessions suffered from scrambled key orders. In 2002, JDK 1.4 introduced `LinkedHashMap`, providing $O(1)$ hash table performance coupled with a doubly-linked list. It also introduced `removeEldestEntry()`, making it the canonical vehicle for building in-memory LRU caches.

### Navigable Navigation (Java 6 / JSR 166x)
`SortedMap` provided basic range views (`subMap`, `headMap`, `tailMap`), but lacked proximity queries. In Java 6, Doug Lea and the JSR 166 expert group introduced `NavigableMap<K, V>`, retrofitting `TreeMap` with bidirectional proximity queries: `floorKey()`, `ceilingKey()`, `lowerKey()`, `higherKey()`, `firstEntry()`, `lastEntry()`, and `descendingMap()`.

### The Java 21 Sequenced Collections Unification (JEP 431)
Before Java 21, `LinkedHashMap` and `TreeMap` each had encounter orders, but lacked a common sequenced interface. A method accepting an ordered map had no uniform way to query `firstEntry()` or obtain a reverse view without downcasting to `TreeMap` (which excluded `LinkedHashMap`). Java 21 introduced `SequencedMap<K, V>`, unifying endpoint operations (`firstEntry()`, `lastEntry()`, `pollFirstEntry()`, `pollLastEntry()`, `putFirst()`, `putLast()`) and reversible views (`reversed()`) across both classes.

### Transition Guide: MERN/JavaScript vs. Java Ordered Maps

| Feature / Dimension | JavaScript / TypeScript (`Map`) | Java `LinkedHashMap` | Java `TreeMap` |
|---|---|---|---|
| **Encounter Order** | Insertion order guaranteed by ECMAScript spec. | Insertion order (default) OR Access order (LRU). | Natural sorted order (`Comparable`) or custom `Comparator`. |
| **LRU Cache Support** | Requires manual delete-and-reinsert (`map.delete(k); map.set(k, v);`). | Built-in via `accessOrder = true` and `removeEldestEntry()`. | Not applicable (order is strictly sorted). |
| **Proximity Searches** | None built-in; requires manual binary search over sorted keys array. | None. | Rich $O(\log N)$ proximity searches (`floorEntry`, `ceilingEntry`). |
| **Range Slices** | None; requires filtering or copying arrays. | None. | $O(\log N)$ live, bidirectionally synchronized range views (`subMap`). |
| **Concurrency Hazards** | Single-threaded event loop prevents data races on read/write. | **Severe:** `get()` on access-order `LinkedHashMap` is a **write operation**; unsafe for concurrent reads! | Standard concurrent read/write race conditions. |

---

## 3. Declarative Mechanics & Mental Model

### `LinkedHashMap` Internal Mechanics: Buckets + Doubly-Linked Sequence

`LinkedHashMap` subclasses `HashMap`. It reuses the exact same bucket array (`table`), but replaces `HashMap.Node` with `LinkedHashMap.Entry`:

```
HashMap.Node fields:
[ hash | key | value | next ]
      ▲
      │ extends
LinkedHashMap.Entry additional fields:
[ before | after ]
```

```
           Bucket Array (HashMap Table)
           [ 0 ] ──> null
           [ 1 ] ──> [ Entry: "DELTA" ]
           [ 2 ] ──> null
           [ 3 ] ──> [ Entry: "ALPHA" ] ──> [ Entry: "OMEGA" ]
           
           Internal Doubly-Linked List (Threaded through all buckets):
           ┌──────────┐      ┌──────────┐      ┌──────────┐
  HEAD ───>│ "ALPHA"  │<────>│ "DELTA"  │<────>│ "OMEGA"  │<─── TAIL
           └──────────┘      └──────────┘      └──────────┘
```

#### The Access-Order Relocation Hook:
When `accessOrder = true`:
```java
// Inside LinkedHashMap:
void afterNodeAccess(Node<K,V> e) {
    LinkedHashMap.Entry<K,V> p = (LinkedHashMap.Entry<K,V>)e;
    if (accessOrder && (last = tail) != p) {
        // Unlink p from its current position:
        LinkedHashMap.Entry<K,V> b = p.before, a = p.after;
        p.after = null;
        if (b == null) head = a; else b.after = a;
        if (a != null) a.before = b; else last = b;
        
        // Re-link p at the TAIL (MRU position):
        if (last == null) head = p;
        else {
            p.before = last;
            last.after = p;
        }
        tail = p;
        ++modCount;
    }
}
```
> [!CRITICAL]
> Notice line `++modCount;`! In an access-order `LinkedHashMap`, calling `map.get(key)` **modifies `modCount` and rewires heap pointers**. Therefore, a `get()` call is physically a **write mutation**, rendering un-synchronized concurrent reads catastrophic!

---

### `TreeMap` Internal Mechanics: The Red-Black Tree

`TreeMap` is a direct implementation of a **Red-Black Tree**—a self-balancing binary search tree satisfying the five Cormen-Leiserson-Rivest-Stein (CLRS) invariants:
1. Every node is either **RED** or **BLACK**.
2. The root node is always **BLACK**.
3. All leaf nodes (`null` sentinels) are considered **BLACK**.
4. If a node is **RED**, both of its children must be **BLACK** (no two consecutive RED nodes on any path).
5. For every node, all paths from that node to descendant leaf nodes contain the **exact same number of BLACK nodes** (the *Black-Height* invariant).

```
                      [ 50 (Black) ]  <-- Root
                       /          \
          [ 25 (Red) ]              [ 75 (Red) ]
           /        \                /        \
    [ 10 (Black) ] [ 40 (Black) ] [ 60 (Black) ] [ 90 (Black) ]
```

#### Rebalancing Dynamics (Rotations & Recoloring):
When an element is inserted:
- It is initially inserted as a **RED** leaf node at its sorted binary search position.
- If its parent is also RED, invariant #4 is violated (Red-Red conflict).
- `TreeMap.fixAfterInsertion(Entry<K,V> x)` restores balance using at most **two tree rotations** (Left-Rotate or Right-Rotate) and $O(\log N)$ node recolorings.
- **Guaranteed Depth:** The height of a Red-Black tree containing $N$ nodes is strictly bounded:
  $$h \le 2 \log_2(N + 1)$$
  This guarantees that `get()`, `put()`, `remove()`, and `containsKey()` always complete in **$O(\log N)$ time**, even under adversarial or sequentially sorted insertions that would degrade an unbalanced BST into an $O(N)$ linked list.

---

## 4. Key Concepts & Terminology

- **Encounter Order:** The deterministic sequence in which elements are returned by an iterator or stream.
- **Insertion-Order:** Encounter order matching the exact chronology of initial key insertion.
- **Access-Order (LRU):** Encounter order reflecting access history, from Least Recently Used (head) to Most Recently Used (tail).
- **Eviction Hook (`removeEldestEntry`):** A protected callback invoked by `LinkedHashMap.put()` and `putAll()` allowing automated eviction of the eldest entry when a threshold is breached.
- **Red-Black Tree:** A self-balancing binary search tree where each node carries a color bit ensuring logarithmic depth bounds.
- **Black-Height:** The number of black nodes on any simple path from a node to a leaf.
- **NavigableMap:** A `SortedMap` providing closest-match queries (`floorEntry`, `ceilingEntry`, `lowerEntry`, `higherEntry`) and reversible views (`descendingMap`).
- **Floor vs. Lower:**
  - `floorKey(k)`: Returns the greatest key $\le k$, or `null`.
  - `lowerKey(k)`: Returns the greatest key strictly $< k$, or `null`.
- **Ceiling vs. Higher:**
  - `ceilingKey(k)`: Returns the least key $\ge k$, or `null`.
  - `higherKey(k)`: Returns the least key strictly $> k$, or `null`.
- **Consistent with Equals:** The condition where `(compare(k1, k2) == 0) == (k1.equals(k2))`. If violated in `TreeMap`, the map violates the general `Map` contract by treating distinct objects as identical keys.

---

## 5. Syntax, Signatures & Compiler Rules

### Constructors

```java
// LinkedHashMap Constructors:
public LinkedHashMap() // capacity 16, loadFactor 0.75, insertion-order
public LinkedHashMap(int initialCapacity)
public LinkedHashMap(int initialCapacity, float loadFactor)
// Access-order constructor (The LRU Cache builder):
public LinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder)

// TreeMap Constructors:
public TreeMap() // Natural ordering: K must implement Comparable<? super K>
public TreeMap(Comparator<? super K> comparator) // Custom comparator
public TreeMap(Map<? extends K, ? extends V> m)
public TreeMap(SortedMap<K, ? extends V> m)
```

### Java 21 SequencedMap Signatures (JEP 431)

```java
package java.util;

public interface SequencedMap<K, V> extends Map<K, V> {
    SequencedMap<K, V> reversed();
    Map.Entry<K, V> firstEntry();
    Map.Entry<K, V> lastEntry();
    Map.Entry<K, V> pollFirstEntry();
    Map.Entry<K, V> pollLastEntry();
    V putFirst(K k, V v);
    V putLast(K k, V v);
    SequencedSet<K> sequencedKeySet();
    SequencedCollection<V> sequencedValues();
    SequencedSet<Map.Entry<K, V>> sequencedEntrySet();
}
```

### Compiler and Runtime Rules:
1. **`TreeMap` Runtime ClassCastException:**
   If `new TreeMap<>()` is instantiated without a `Comparator`, keys **must implement `Comparable`**. Generics cannot verify this at compile time due to type erasure; inserting a non-Comparable key throws `ClassCastException` at runtime on the first comparison!
2. **`TreeMap.put(null, v)` Rejection:**
   Throws `NullPointerException` on `compare()` invocation (unless an explicit `Comparator` that handles null is supplied).
3. **`TreeMap.putFirst()` / `putLast()` Rejection:**
   Throws `UnsupportedOperationException`. `TreeMap` implements `SequencedMap`, but keys cannot be manually positioned at the head or tail because order is strictly determined by the comparator.
4. **`removeEldestEntry()` Signature:**
   ```java
   protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
   ```
   Overridden in subclasses. Returns `true` to remove the eldest entry immediately after insertion, or `false` to retain it.

---

## 6. Concrete Code Examples

The following production-grade implementation showcases:
1. A **Thread-Safe Bounded LRU Cache** extending `LinkedHashMap` with automated eviction.
2. A **Financial Limit Order Book Engine** using `TreeMap` and `NavigableMap` range queries.
3. Demonstration of the Java 21 `SequencedMap` API.
4. Reproduction of the **`compareTo` vs. `equals` inconsistency trap** in `TreeMap`.

```java
package com.bank.cache;

import java.util.*;

public class OrderedMapMasteryVerificationEngine {

    // -------------------------------------------------------------
    // 1. Production-Grade Bounded LRU Cache via LinkedHashMap
    // -------------------------------------------------------------
    public static class BoundedLruCache<K, V> extends LinkedHashMap<K, V> {
        private final int maxEntries;

        public BoundedLruCache(int maxEntries) {
            // initialCapacity = maxEntries, loadFactor = 0.75f, accessOrder = true (LRU mode)
            super(maxEntries, 0.75f, true);
            if (maxEntries <= 0) {
                throw new IllegalArgumentException("maxEntries must be positive");
            }
            this.maxEntries = maxEntries;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            // Automatically evicts the eldest (least recently used) entry when size exceeds limit
            return size() > maxEntries;
        }
    }

    // -------------------------------------------------------------
    // 2. Comparable Value Object for TreeMap Order Book
    // -------------------------------------------------------------
    public record PricePoint(int priceCents, String orderType) implements Comparable<PricePoint> {
        public PricePoint {
            Objects.requireNonNull(orderType, "orderType cannot be null");
        }

        @Override
        public int compareTo(PricePoint other) {
            int cmp = Integer.compare(this.priceCents, other.priceCents);
            if (cmp != 0) return cmp;
            return this.orderType.compareTo(other.orderType);
        }
    }

    // -------------------------------------------------------------
    // 3. Flawed Key Demonstrating compareTo vs equals Inconsistency
    // -------------------------------------------------------------
    public static final class InconsistentComparableKey implements Comparable<InconsistentComparableKey> {
        private final int accountId;
        private final String routingTag;

        public InconsistentComparableKey(int accountId, String routingTag) {
            this.accountId = accountId;
            this.routingTag = Objects.requireNonNull(routingTag, "routingTag cannot be null");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InconsistentComparableKey that)) return false;
            return accountId == that.accountId && Objects.equals(routingTag, that.routingTag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, routingTag);
        }

        // FLAW: Compares ONLY accountId, ignoring routingTag!
        @Override
        public int compareTo(InconsistentComparableKey other) {
            return Integer.compare(this.accountId, other.accountId);
        }

        @Override
        public String toString() {
            return "Key(" + accountId + ", " + routingTag + ")";
        }
    }

    // Standalone assertion utilities
    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " [Expected: " + expected + ", Actual: " + actual + "]");
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Launching LinkedHashMap & TreeMap Masterclass Verification Suite ===");

        // -------------------------------------------------------------
        // PART 1: LinkedHashMap Insertion-Order Determinism
        // -------------------------------------------------------------
        Map<String, String> insertionMap = new LinkedHashMap<>();
        insertionMap.put("ZETA", "Terminal");
        insertionMap.put("ALPHA", "Origin");
        insertionMap.put("GAMMA", "Intermediate");

        // Re-putting existing key updates value but DOES NOT alter insertion position
        insertionMap.put("ALPHA", "Origin-Updated");

        List<String> recordedSequence = new ArrayList<>(insertionMap.keySet());
        assertEquals(List.of("ZETA", "ALPHA", "GAMMA"), recordedSequence, 
            "LinkedHashMap must strictly preserve original insertion sequence despite key updates");

        // -------------------------------------------------------------
        // PART 2: LinkedHashMap Access-Order & LRU Eviction Mechanics
        // -------------------------------------------------------------
        BoundedLruCache<String, String> lruCache = new BoundedLruCache<>(3);
        lruCache.put("SESSION_1", "User_A");
        lruCache.put("SESSION_2", "User_B");
        lruCache.put("SESSION_3", "User_C");

        // Access SESSION_1 -> Relocates SESSION_1 to MRU (tail) position: [SESSION_2, SESSION_3, SESSION_1]
        lruCache.get("SESSION_1");

        // Insert 4th session -> Exceeds capacity (3) -> Eldest entry (SESSION_2) must be evicted!
        lruCache.put("SESSION_4", "User_D");

        assertFalse(lruCache.containsKey("SESSION_2"), 
            "SESSION_2 must be evicted as the least recently used entry");
        assertTrue(lruCache.containsKey("SESSION_1"), 
            "SESSION_1 must be preserved because it was refreshed via get()");
        assertTrue(lruCache.containsKey("SESSION_3"), "SESSION_3 must remain present");
        assertTrue(lruCache.containsKey("SESSION_4"), "SESSION_4 must be present");

        List<String> accessSequence = new ArrayList<>(lruCache.keySet());
        assertEquals(List.of("SESSION_3", "SESSION_1", "SESSION_4"), accessSequence, 
            "Key sequence must reflect [LRU (head) -> MRU (tail)] access ordering");

        // -------------------------------------------------------------
        // PART 3: Java 21 SequencedMap API on LinkedHashMap
        // -------------------------------------------------------------
        LinkedHashMap<String, Integer> seqMap = new LinkedHashMap<>();
        seqMap.put("STAGE_1", 100);
        seqMap.put("STAGE_2", 200);
        seqMap.put("STAGE_3", 300);

        assertEquals("STAGE_1", seqMap.firstEntry().getKey(), "firstEntry() retrieves chronological head");
        assertEquals("STAGE_3", seqMap.lastEntry().getKey(), "lastEntry() retrieves chronological tail");

        // Java 21 putFirst prepends to the head of the sequence
        seqMap.putFirst("STAGE_0", 50);
        assertEquals("STAGE_0", seqMap.firstEntry().getKey(), "putFirst must position entry at head");

        // Java 21 reversed() returns a bidirectional view
        SequencedMap<String, Integer> reversedView = seqMap.reversed();
        assertEquals("STAGE_3", reversedView.firstEntry().getKey(), "reversed.firstEntry() equals original tail");
        assertEquals("STAGE_0", reversedView.lastEntry().getKey(), "reversed.lastEntry() equals original head");

        // -------------------------------------------------------------
        // PART 4: TreeMap Red-Black Tree Sorting & Proximity Searches
        // -------------------------------------------------------------
        TreeMap<PricePoint, String> orderBook = new TreeMap<>();
        orderBook.put(new PricePoint(5000, "LIMIT_BUY"), "Order-50");
        orderBook.put(new PricePoint(1000, "LIMIT_BUY"), "Order-10");
        orderBook.put(new PricePoint(9000, "LIMIT_SELL"), "Order-90");
        orderBook.put(new PricePoint(3000, "LIMIT_BUY"), "Order-30");

        List<Integer> sortedPrices = orderBook.keySet().stream().map(PricePoint::priceCents).toList();
        assertEquals(List.of(1000, 3000, 5000, 9000), sortedPrices, 
            "TreeMap keys must be maintained continuously in ascending sorted order");

        // NavigableMap Proximity Searches (O(log N))
        PricePoint probe = new PricePoint(4000, "PROBE");
        assertEquals(3000, orderBook.lowerKey(probe).priceCents(), "lowerKey(< 4000) is 3000");
        assertEquals(3000, orderBook.floorKey(new PricePoint(3000, "LIMIT_BUY")).priceCents(), 
            "floorKey(<= 3000) is 3000");
        assertEquals(5000, orderBook.higherKey(probe).priceCents(), "higherKey(> 4000) is 5000");
        assertEquals(5000, orderBook.ceilingKey(new PricePoint(5000, "LIMIT_BUY")).priceCents(), 
            "ceilingKey(>= 5000) is 5000");

        // Range Views: subMap interval extraction
        SortedMap<PricePoint, String> tradingBand = orderBook.subMap(
            new PricePoint(2000, "A"), 
            new PricePoint(6000, "Z")
        );
        List<Integer> bandPrices = tradingBand.keySet().stream().map(PricePoint::priceCents).toList();
        assertEquals(List.of(3000, 5000), bandPrices, "subMap must extract exact price band [2000, 6000]");

        // Extremes polling
        Map.Entry<PricePoint, String> lowestExecution = orderBook.pollFirstEntry();
        assertEquals(1000, lowestExecution.getKey().priceCents(), "pollFirstEntry must retrieve and remove lowest");
        Map.Entry<PricePoint, String> highestExecution = orderBook.pollLastEntry();
        assertEquals(9000, highestExecution.getKey().priceCents(), "pollLastEntry must retrieve and remove highest");
        assertEquals(2, orderBook.size(), "Order book size must be 2 after polling extremes");

        // -------------------------------------------------------------
        // PART 5: TreeMap Invariants & Exception Contracts
        // -------------------------------------------------------------
        // Invariant: Rejects null key
        try {
            orderBook.put(null, "ILLEGAL_NULL");
            throw new AssertionError("TreeMap must reject null key with NullPointerException");
        } catch (NullPointerException expected) {
            // Invariant verified
        }

        // Invariant: Rejects putFirst / putLast
        try {
            orderBook.putFirst(new PricePoint(1, "A"), "ILLEGAL_POSITION");
            throw new AssertionError("TreeMap.putFirst() must throw UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // Invariant verified: TreeMap position is strictly comparator-governed
        }

        // -------------------------------------------------------------
        // PART 6: The compareTo vs equals Inconsistency Trap in TreeMap
        // -------------------------------------------------------------
        InconsistentComparableKey key1 = new InconsistentComparableKey(404, "EAST_GATEWAY");
        InconsistentComparableKey key2 = new InconsistentComparableKey(404, "WEST_GATEWAY"); // Distinct according to equals!

        assertFalse(key1.equals(key2), "Keys have different routingTags and are NOT equal according to equals()");
        assertEquals(0, key1.compareTo(key2), "compareTo returns 0 because accountIds are identical");

        TreeMap<InconsistentComparableKey, String> trapMap = new TreeMap<>();
        trapMap.put(key1, "METRICS_EAST");
        // Catastrophic overwrite: TreeMap uses compareTo() == 0, treating key2 as key1!
        trapMap.put(key2, "METRICS_WEST");

        assertEquals(1, trapMap.size(), 
            "TreeMap treated key2 as duplicate and refused to expand size because compareTo() == 0!");
        assertEquals("METRICS_WEST", trapMap.get(key1), 
            "METRICS_EAST was silently overwritten because compareTo() was inconsistent with equals()!");

        System.out.println(">>> All 24 assertions passed successfully across LinkedHashMap and TreeMap!");
    }
}
```

---

## 7. Production Failure Modes & Anti-Patterns

### 1. Concurrency Crash on Access-Order `LinkedHashMap.get()`
In multi-threaded services (e.g., Spring Boot controllers), developers frequently wrap `new LinkedHashMap<>(100, 0.75f, true)` in an un-synchronized field, believing that `get()` is a read-only, thread-safe operation.
**The Reality:** In access-order mode, `get()` updates `before` and `after` pointers to move the node to the tail. Multiple threads executing `get()` concurrently will corrupt the doubly-linked list pointers, creating circular references (`node.after.before != node`), dropping nodes, or triggering infinite loops in traversal.
**Remedy:** Always wrap access-order `LinkedHashMap` instances in synchronization:
```java
Map<K, V> safeLru = Collections.synchronizedMap(new BoundedLruCache<>(1000));
```
Or use a battle-tested concurrent caching library such as **Caffeine** (`Caffeine.newBuilder().maximumSize(1000).build()`), which uses non-blocking Window TinyLFU eviction.

### 2. Silent Data Overwrite in `TreeMap` via Inconsistent Comparator
If two distinct domain objects produce `compare(k1, k2) == 0`:
- `TreeMap.put(k2, v2)` will find `k1` in the tree, treat it as a match, and **overwrite `k1`'s value with `v2`**, completely discarding `v1`.
- If an order matching engine sorts orders purely by price without a secondary order ID tie-breaker, all subsequent orders placed at that exact price will overwrite previous orders!
**Remedy:** In any custom `Comparable` or `Comparator`, always provide a deterministic secondary tie-breaker (e.g., timestamp, sequence number, UUID):
```java
Comparator<Order> orderComparator = Comparator
    .comparing(Order::getPriceCents)
    .thenComparing(Order::getOrderId); // Tie-breaker guarantees consistency with equals!
```

### 3. Inserting Non-Comparable Keys into `TreeMap`
If a developer initializes `new TreeMap<>()` and inserts an object of class `Customer` that does not implement `Comparable<Customer>`:
- The Java compiler will **not** flag an error due to generic erasure.
- At runtime, `TreeMap.put()` attempts to cast the key: `Comparable<? super K> k = (Comparable<? super K>) key;`.
- The application crashes in production with `java.lang.ClassCastException: class Customer cannot be cast to class java.lang.Comparable`.
**Remedy:** Either implement `Comparable<T>` on the key class or provide an explicit lambda/comparator: `new TreeMap<>(Comparator.comparing(Customer::getId))`.

### 4. Memory Retention in Live Range Views (`subMap`)
Calling `treeMap.subMap(from, to)` does not clone the entries; it returns a **live view** backed by the original `TreeMap`. Retaining a reference to a `subMap` retains the entire backing tree structure in memory, preventing garbage collection of adjacent subtrees.

---

## 8. Performance Characteristics & Complexity Analysis

### Time Complexity Comparison

| Operation | `HashMap` | `LinkedHashMap` | `TreeMap` |
|---|---|---|---|
| **`get(key)`** | $O(1)$ amortized | $O(1)$ amortized | Guaranteed $O(\log N)$ |
| **`put(key, value)`** | $O(1)$ amortized | $O(1)$ amortized | Guaranteed $O(\log N)$ |
| **`remove(key)`** | $O(1)$ amortized | $O(1)$ amortized | Guaranteed $O(\log N)$ |
| **`firstEntry()` / `lastEntry()`** | $O(N)$ (requires full scan) | $O(1)$ (Java 21 `SequencedMap`) | $O(\log N)$ (or $O(1)$ cached) |
| **Full Map Iteration** | $O(\text{Capacity} + N)$ | $O(N)$ strictly | $O(N)$ strictly |
| **Proximity Searches (`floorKey`)** | Unsupported | Unsupported | Guaranteed $O(\log N)$ |
| **Range Slices (`subMap`)** | Unsupported | Unsupported | $O(\log N)$ live view creation |

### Performance Insights from First Principles:
1. **`LinkedHashMap` Iteration Speed:** Because `LinkedHashMap` iterates directly over its doubly-linked list (`before`/`after` pointers), iteration time is strictly $O(N)$. It completely bypasses empty buckets in the hash table, outperforming `HashMap` whenever the table capacity is significantly larger than the number of stored elements.
2. **`LinkedHashMap` Mutation Overhead:** Every `put()` or `remove()` in `LinkedHashMap` requires updating both the hash bucket chain and the global doubly-linked list pointers. This incurs a ~15-25% higher constant factor overhead compared to standard `HashMap`.
3. **`TreeMap` Cache Penalty:** While $O(\log N)$ is asymptotically optimal for balanced trees, each step down the Red-Black tree dereferences a separate heap node (`left` or `right` pointer), causing CPU L1/L2 cache misses. In latency-critical workloads with fewer than ~64 elements, a sorted `ArrayList` with `Collections.binarySearch()` is often faster due to hardware memory prefetching.

---

## 9. Memory Footprint & JVM Internals

Calculations assume a standard 64-bit HotSpot JVM with Compressed OOPs enabled (`-XX:+UseCompressedOops`) and 8-byte object alignment.

```
Memory Layout Comparison (Entry Nodes):

HashMap.Node (32 bytes):
┌───────────────────────────────┬───────────────────────────────┐
│ Mark Word             (8B)    │ Klass Pointer         (4B)    │
│ int hash              (4B)    │ Key ref (compressed)  (4B)    │
│ Value ref (compressed)(4B)    │ Next Node ref (comp.) (4B)    │
│ [Padding: 4B -> 32 bytes total]                               │
└───────────────────────────────┴───────────────────────────────┘

LinkedHashMap.Entry (40 bytes - Extends HashMap.Node):
┌───────────────────────────────┬───────────────────────────────┐
│ HashMap.Node base fields      │ (32 bytes)                    │
│ Entry before ref      (4B)    │ Entry after ref       (4B)    │
│ [Padding: 0B -> 40 bytes total]                               │
└───────────────────────────────┴───────────────────────────────┘

TreeMap.Entry (40 bytes):
┌───────────────────────────────┬───────────────────────────────┐
│ Mark Word             (8B)    │ Klass Pointer         (4B)    │
│ Key ref (compressed)  (4B)    │ Value ref (compressed)(4B)    │
│ Left Child ref        (4B)    │ Right Child ref       (4B)    │
│ Parent ref            (4B)    │ boolean color         (1B)    │
│ [Padding: 7B -> 40 bytes total]                               │
└───────────────────────────────┴───────────────────────────────┘
```

### Analysis:
- **`LinkedHashMap.Entry`** adds `before` and `after` compressed references to `HashMap.Node`, expanding the entry footprint from 32 bytes to **40 bytes** (a 25% increase).
- **`TreeMap.Entry`** consumes **40 bytes** per entry. However, `TreeMap` requires **zero backing bucket arrays**, whereas `HashMap` and `LinkedHashMap` allocate an underlying `Node[]` array that is typically sized to $2\times$ capacity to satisfy the 0.75 load factor. For small maps, `TreeMap` can actually consume less total memory than a default-initialized `HashMap`.

---

## 10. Comparison Matrix

| Architectural Dimension | `HashMap` | `LinkedHashMap` | `TreeMap` |
|---|---|---|---|
| **Underlying Data Structure** | Bucket Array + Linked List / Red-Black Tree | Bucket Array + List + Global Doubly-Linked List | Red-Black Balanced Binary Search Tree |
| **Key Ordering** | Non-deterministic / unordered | Insertion-order OR Access-order (LRU) | Sorted natural order OR custom `Comparator` |
| **Java 21 `SequencedMap`** | No | **Yes** (`firstEntry`, `lastEntry`, `putFirst`, `reversed`) | **Yes** (`firstEntry`, `lastEntry`, `reversed`)* |
| **Key Equality Criterion** | `equals()` and `hashCode()` | `equals()` and `hashCode()` | **`compare() == 0` strictly** |
| **Permits Null Keys** | **Yes** (1 key at bucket 0) | **Yes** (1 key at bucket 0) | **No** (throws `NullPointerException`) |
| **Permits Null Values** | **Yes** | **Yes** | **Yes** |
| **Search Time Complexity** | Amortized $O(1)$ | Amortized $O(1)$ | Guaranteed $O(\log N)$ |
| **Iteration Time Complexity** | $O(\text{Capacity} + N)$ | $O(N)$ strictly | $O(N)$ strictly |
| **Proximity Searches** | No | No | **Yes** (`floorKey`, `ceilingKey`, `lowerKey`, `higherKey`) |
| **Range Views** | No | No | **Yes** (`subMap`, `headMap`, `tailMap`) |
| **Built-in LRU Eviction** | No | **Yes** (via `removeEldestEntry`) | No |
| **Primary Backend Use Case** | General high-throughput indexing | LRU caches, deterministic JSON serialization | Order books, sliding window timestamps, range filters |

*\* Note: `TreeMap` disables positional insertion methods (`putFirst()`, `putLast()`) with `UnsupportedOperationException`.*

---

## 11. Security, Concurrency & Thread Safety Considerations

### The Access-Order Thread-Safety Trap
The most frequent production concurrency defect involving `LinkedHashMap` stems from misunderstanding access-order mode:
```java
// BUG: Multiple threads reading from cache concurrently
public String getCachedToken(String userId) {
    return lruCache.get(userId); // get() mutates pointers! Data corruption occurs!
}
```
**Fix:** If using `LinkedHashMap` as an LRU cache across multiple threads, you must wrap all accesses (both reads and writes) in a mutex or use `Collections.synchronizedMap()`:
```java
private final Map<String, String> safeLru = Collections.synchronizedMap(new BoundedLruCache<>(500));
```

### High-Throughput Concurrent Alternatives
1. **Concurrent Replacement for `TreeMap`:**
   ```java
   NavigableMap<K, V> skipListMap = new ConcurrentSkipListMap<>();
   ```
   Backed by concurrent Skip Lists, `ConcurrentSkipListMap` provides thread-safe, lock-free $O(\log N)$ lookups, insertions, and range operations without global monitor contention.
2. **Concurrent Replacement for `LinkedHashMap` LRU:**
   Never use `Collections.synchronizedMap(new LinkedHashMap<>(...))` under high multi-threaded contention. Use **Caffeine Cache**, which implements the Window TinyLFU admission and eviction algorithm with ring-buffer concurrent write buffers.

---

## 12. Integration with Frameworks & Standard Library

### 1. Spring MVC / Jackson JSON Serialization
Jackson uses `LinkedHashMap` as its default map representation when deserializing JSON objects whose structure is unmapped or dynamic:
```java
// Spring Boot RestController:
@PostMapping("/api/v1/dynamic-event")
public ResponseEntity<Void> processEvent(@RequestBody Map<String, Object> event) {
    // Jackson instantiates event as LinkedHashMap, preserving JSON attribute order
    return ResponseEntity.ok().build();
}
```
Preserving key order is critical when generating cryptographic message digests (e.g., HMAC signatures over payload fields) or maintaining deterministic API responses for external audit logs.

### 2. Spring Cache Abstraction
Spring Cache allows developers to back `@Cacheable` with a customized LRU cache bean:
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        // Wrap synchronized LRU cache in Spring ConcurrentMapCache
        Map<Object, Object> lruMap = Collections.synchronizedMap(new BoundedLruCache<>(10_000));
        manager.setCaches(List.of(new ConcurrentMapCache("tokenCache", lruMap, false)));
        return manager;
    }
}
```

---

## 13. Practical Exercises & Hands-on Challenges

### Challenge 1: Sliding-Window Rate Limiter via `TreeMap`
**Scenario:** A payment gateway must enforce a rate limit of "no more than 5 transactions per second per merchant".
**Task:** Using `TreeMap<Instant, TransactionId>` and `NavigableMap.tailMap(oneSecondAgo)`, implement an $O(\log N)$ sliding-window rate limiter that purges expired transactions via `pollFirstEntry()` and returns whether an incoming transaction is approved.

### Challenge 2: Implement a Resizable LRU Cache
**Scenario:** A cache administrator wishes to dynamically adjust cache capacity at runtime via JMX without reallocating map structures.
**Task:** Subclass `LinkedHashMap<K, V>` to implement `DynamicLruCache<K, V>` with an `AtomicInteger maxCapacity`. Provide a thread-safe `setMaxCapacity(int newMax)` method that automatically evicts excess entries down to the new capacity upon invocation.

### Challenge 3: Inconsistent Comparator Bug Reproducer
**Scenario:** An auction platform engine is dropping bids placed at the exact same price.
**Task:** Write a unit test reproducing how a `TreeMap<Bid, String>` drops unique bids with identical amounts when `Bid.compareTo` compares only `amountCents`. Refactor the comparator to include `bidId` and verify that both bids are retained.

---

## 14. Diagnostic, Troubleshooting & Debugging Guide

### How to Diagnose Linked Pointer Corruption in `LinkedHashMap`
- **Symptom:** Thread hangs with 100% CPU usage inside `LinkedHashMap$LinkedHashIterator.nextNode()`.
- **Root Cause:** A thread modified the map (via `put`, `remove`, or access-order `get`) while an iterator was traversing. The doubly-linked pointers were corrupted into a cyclic loop: `node.after.after == node`.
- **Diagnosis:** Take a thread dump using `jcmd <PID> Thread.print`. If multiple threads are blocked or looping inside `LinkedHashIterator`, inspect whether the map is accessed across multiple threads without synchronization.

### Visualizing Red-Black Tree Imbalance in Debuggers
When inspecting a `TreeMap` in IntelliJ IDEA or Eclipse:
1. Expand the `root` field.
2. Note the `color` boolean (`false = BLACK`, `true = RED`).
3. Verify that the root is `BLACK` (`color: false`).
4. Trace any path from root to leaf: verify that no `RED` node has a `RED` child, and that the number of `BLACK` nodes along every branch is identical.

---

## 15. Frequently Asked Questions & Interview Questions

### Q1: Why does `get()` on an access-order `LinkedHashMap` cause a `ConcurrentModificationException` during iteration?
**Answer:** In access-order mode (`accessOrder = true`), invoking `get(key)` unlinks the accessed entry and relinks it at the tail of the doubly-linked list (the MRU position). This structural relocation modifies the map's internal `modCount` field (`++modCount`). If an iteration loop (such as `for (var entry : map.entrySet())`) calls `map.get()` on any key during traversal, the iterator detects `modCount != expectedModCount` on the subsequent step and immediately throws a fail-fast `ConcurrentModificationException`.

### Q2: What happens if you insert an object that does NOT implement `Comparable` into a `TreeMap` created with the default constructor?
**Answer:** The code compiles cleanly because Java's type erasure prevents generic bounds enforcement at compile time. However, on the first call to `put(key, value)`, `TreeMap` attempts to cast the key: `Comparable<? super K> k = (Comparable<? super K>) key;`. The JVM immediately crashes at runtime with a `ClassCastException`. To store non-Comparable keys in a `TreeMap`, you must supply a custom `Comparator` via the constructor: `new TreeMap<>(comparator)`.

### Q3: Why does `TreeMap` reject `null` keys, while `LinkedHashMap` permits a `null` key?
**Answer:** `LinkedHashMap` inherits its hash table architecture from `HashMap`. When a `null` key is inserted, it bypasses hashing and is placed directly into bucket index 0. In contrast, `TreeMap` must sort all keys using `compare(k1, k2)`. Invoking `k1.compareTo(null)` or `null.compareTo(k2)` would trigger a `NullPointerException`. Prior to Java 7, inserting a single `null` key was permitted if the tree was empty; however, subsequent insertions failed during comparison. In Java 7+, `TreeMap` strictly rejects `null` keys immediately on `put(null, v)` with an explicit `NullPointerException`.

### Q4: How does `LinkedHashMap` achieve $O(1)$ LRU eviction while standard arrays require $O(N)$ shifts?
**Answer:** `LinkedHashMap` combines two data structures:
1. A **Hash Table (`table`)** provides $O(1)$ amortized key lookups to locate any node directly in memory.
2. A **Doubly-Linked List (`before`/`after`)** allows unlinking a node from any position and relinking it at the tail in $O(1)$ pointer operations without shifting array elements.
When capacity is exceeded, the eldest node at `head` is evicted in $O(1)$ time via `removeNode()`.

### Q5: In Java 21, what occurs if you invoke `treeMap.putFirst(k, v)` or `treeMap.putLast(k, v)`?
**Answer:** Both methods throw `UnsupportedOperationException`. Although `TreeMap` implements `SequencedMap` because it possesses a well-defined encounter order (ascending sorted order), that order is governed exclusively by the comparator. Allowing manual insertion at the head or tail would violate the Red-Black binary search tree sorting invariant. Therefore, `TreeMap` explicitly disables `putFirst()` and `putLast()`.

---

## 16. Primary Source References & Further Reading

1. **Java Language Specification & Java SE 21 API Docs:**
   - [`java.util.LinkedHashMap`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/LinkedHashMap.html) — OpenJDK 21 specification and access-order mechanics.
   - [`java.util.TreeMap`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/TreeMap.html) — Red-Black tree implementation.
   - [`java.util.NavigableMap`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/NavigableMap.html) — Proximity and range operations.
   - [`java.util.SequencedMap`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/SequencedMap.html) — JEP 431 Sequenced Collections specification.
2. **JDK Enhancement Proposals (JEPs):**
   - **JEP 431:** [Sequenced Collections](https://openjdk.org/jeps/431) (JDK 21).
3. **Seminal Literature:**
   - Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein, *Introduction to Algorithms* (3rd Edition), MIT Press:
     - Chapter 13: *Red-Black Trees* (Rotations, Insertions, Deletions).
   - Joshua Bloch, *Effective Java* (3rd Edition), Addison-Wesley:
     - Item 14: *Consider implementing Comparable*.
