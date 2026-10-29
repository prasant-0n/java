# Phase 03 Capstone: Generic In-Memory Evicting Cache

---

## 1. System Overview and Purpose

The **Generic In-Memory Evicting Cache** is an enterprise-grade caching engine designed to synthesize all 15 core concepts of Phase 03 (Collections Framework, Generics, and Type Safety).

In high-throughput microservices, unconstrained memory caching causes OutOfMemoryErrors (OOM) and JVM garbage collection thrashing. This capstone implements an evicting cache governed by two complementary policies:
1. **Least Recently Used (LRU) Eviction:** Bounds maximum heap memory usage by evicting the least recently accessed elements when capacity is exceeded.
2. **Time-To-Live (TTL) Expiration:** Employs a binary min-heap priority queue to track expiration timestamps and prune stale data in $O(\log N)$ or $O(1)$ time.

---

## 2. Architecture & Design Patterns

```
                                  EvictingCache<K, V>
                                          │
                  ┌───────────────────────┴───────────────────────┐
                  ▼                                               ▼
         LinkedHashMap<K, Entry>                          PriorityQueue<Entry>
         (accessOrder = true)                             (Min-Heap by expiresAt)
                  │                                               │
                  ├─ Fast O(1) Key Lookup                         ├─ O(1) Earliest Expiry Peek
                  ├─ O(1) LRU Access Tracking                     └─ O(log N) Stale Purging
                  └─ O(1) Eldest Eviction
```

### Synthesis of Phase 03 Concepts

| Concept ID | Topic | Application in Capstone |
|---|---|---|
| `P03-C001` | Collections Framework Hierarchy | Implements custom `Cache<K, V>` contract integrating `SequencedCollection` and `Map`. |
| `P03-C002` | `ArrayList` vs `LinkedList` | Uses `ArrayList` for fast contiguous array snapshot transfers and value extractions. |
| `P03-C004` | `HashMap` Internals | Backed by hash table entry mechanics for $O(1)$ amortized lookups and updates. |
| `P03-C005` | `LinkedHashMap` Ordering | Leverages `accessOrder = true` doubly-linked pointer updates to maintain exact LRU ordering. |
| `P03-C006` | `PriorityQueue` Heap | Backed by a complete binary min-heap for efficient $O(1)$ TTL expiration checks. |
| `P03-C007` | Concurrency & Thread-Safety | Synchronized thread safety via `ReentrantLock` preventing race conditions during access-order pointer updates. |
| `P03-C008` | Immutable Collections & Views | Exports defensive immutable snapshots via `Map.copyOf(...)` and unmodifiable views. |
| `P03-C009` | `equals()` and `hashCode()` | Cache keys require deterministic `equals()` and `hashCode()` implementations to prevent ghost object leaks. |
| `P03-C010` | `Comparable` Ordering | `CacheEntry` implements `Comparable<CacheEntry<K, V>>` based on `Long.compare(expiresAt)`. |
| `P03-C011` | Iterators & Safe Removal | Implements `evictIf(BiPredicate)` and LRU eviction using safe `Iterator.remove()` without CME. |
| `P03-C012` | Generics Fundamentals | Parameterized across key `K` and value `V` without raw type compromises. |
| `P03-C013` | Bounded Type Parameters | Key and value boundaries verified at compile time with zero casts. |
| `P03-C014` | Wildcards & PECS | Implements Producer Extends (`putAll(Map<? extends K, ? extends V>)`, `getAll(Collection<? extends K>)`) and Consumer Super (`evictIf(BiPredicate<? super K, ? super V>)`, `computeIfAbsent`). |
| `P03-C015` | Type Erasure & Reification | Compile-time safety verified with `-Xlint:all -Werror`, ensuring zero heap pollution. |

---

## 3. Package Structure

```
capstone/
├── build-and-run.sh
├── generic-evicting-cache-1.0.0.jar
├── README.md
└── src/
    └── com/
        └── example/
            └── cache/
                ├── model/
                │   ├── CacheEntry.java
                │   ├── CacheStats.java
                │   └── EvictionPolicy.java
                ├── service/
                │   ├── Cache.java
                │   └── EvictingCache.java
                └── test/
                    └── EvictingCacheIntegrationTest.java
```

---

## 4. Verification & Automated Test Results

The suite tests 12 critical functional dimensions with 32 automated assertions:
1. `testBasicPutAndGet`: Verifies fundamental generic key-value storage and optional wrapping.
2. `testLruEvictionPolicy`: Verifies eviction of the least recently used item on capacity overflow.
3. `testLruAccessOrderProtection`: Proves `get()` updates access order, protecting active items.
4. `testTtlExpiration`: Proves millisecond-accurate expiration purging via the min-heap.
5. `testPecsMappingAndComputeIfAbsent`: Verifies `Function<? super K, ? extends V>` mapping.
6. `testPecsPutAllAndGetAll`: Verifies `Map<? extends K, ? extends V>` and `Collection<? extends K>`.
7. `testPecsEvictIf`: Verifies predicate-based safe eviction using `Iterator.remove()`.
8. `testImmutableSnapshotIsolation`: Verifies `Map.copyOf` defensive copy prevents mutation leakage.
9. `testSequencedCollectionAccessOrder`: Verifies Java 21 Sequenced Collection ordering.
10. `testConcurrentAccessThreadSafety`: Multi-threaded stress test with 8 worker threads and 4,000 operations.
11. `testNullProhibitionInvariants`: Confirms strict null key and value rejection.
12. `testCacheStatsAndHitRate`: Validates hit count, miss count, eviction count, and hit rate calculation.

### CLI Build and Execution:
```bash
./build-and-run.sh
```
Output:
```
=== Building Phase 03 Capstone: Generic Evicting Cache ===
Compiling Java 21 sources with javac...
Packaging into executable JAR: .../generic-evicting-cache-1.0.0.jar...
Executing test suite from standalone JAR...
=== Starting Generic Evicting Cache Integration Test Suite ===
All 12 test suites (32 assertions) passed with 100% compliance!
=== Phase 03 Capstone Build and Verification Complete! ===
```
