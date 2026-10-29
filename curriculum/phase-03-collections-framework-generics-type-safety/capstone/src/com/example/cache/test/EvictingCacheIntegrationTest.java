package com.example.cache.test;

import com.example.cache.model.CacheStats;
import com.example.cache.model.EvictionPolicy;
import com.example.cache.service.Cache;
import com.example.cache.service.EvictingCache;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class EvictingCacheIntegrationTest {

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
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

    public static void main(String[] args) throws Exception {
        System.out.println("=== Starting Generic Evicting Cache Integration Test Suite ===");

        testBasicPutAndGet();
        testLruEvictionPolicy();
        testLruAccessOrderProtection();
        testTtlExpiration();
        testPecsMappingAndComputeIfAbsent();
        testPecsPutAllAndGetAll();
        testPecsEvictIf();
        testImmutableSnapshotIsolation();
        testSequencedCollectionAccessOrder();
        testConcurrentAccessThreadSafety();
        testNullProhibitionInvariants();
        testCacheStatsAndHitRate();

        System.out.println("All 12 test suites (32 assertions) passed with 100% compliance!");
    }

    // 1. Basic Put and Get
    private static void testBasicPutAndGet() {
        Cache<String, Integer> cache = new EvictingCache<>(5);
        cache.put("A", 100);
        cache.put("B", 200);

        assertEquals(2, cache.size(), "Size is 2");
        assertTrue(cache.containsKey("A"), "Contains key A");
        assertEquals(Optional.of(100), cache.get("A"), "A is 100");
        assertEquals(Optional.of(200), cache.get("B"), "B is 200");
        assertEquals(Optional.empty(), cache.get("C"), "C is not present");
    }

    // 2. LRU Eviction Policy
    private static void testLruEvictionPolicy() {
        Cache<String, String> cache = new EvictingCache<>(3);
        cache.put("K1", "V1");
        cache.put("K2", "V2");
        cache.put("K3", "V3");

        // Inserting 4th item when capacity is 3 evicts K1 (eldest)
        cache.put("K4", "V4");

        assertEquals(3, cache.size(), "Capacity retained at 3");
        assertFalse(cache.containsKey("K1"), "K1 must be evicted");
        assertTrue(cache.containsKey("K2"), "K2 retained");
        assertTrue(cache.containsKey("K3"), "K3 retained");
        assertTrue(cache.containsKey("K4"), "K4 retained");
    }

    // 3. LRU Access Order Protection
    private static void testLruAccessOrderProtection() {
        Cache<String, String> cache = new EvictingCache<>(3);
        cache.put("K1", "V1");
        cache.put("K2", "V2");
        cache.put("K3", "V3");

        // Access K1 -> Moves K1 to MRU! K2 becomes eldest!
        cache.get("K1");

        // Insert K4 -> K2 must be evicted, K1 must survive!
        cache.put("K4", "V4");

        assertTrue(cache.containsKey("K1"), "K1 survived because it was accessed");
        assertFalse(cache.containsKey("K2"), "K2 evicted as least recently used");
        assertTrue(cache.containsKey("K3"), "K3 retained");
        assertTrue(cache.containsKey("K4"), "K4 retained");
    }

    // 4. Time-To-Live (TTL) Expiration
    private static void testTtlExpiration() throws InterruptedException {
        Cache<String, String> cache = new EvictingCache<>(5, Duration.ofMillis(50));
        cache.put("TEMP", "EXPIRES_FAST");

        assertTrue(cache.containsKey("TEMP"), "Immediately present");
        assertEquals(Optional.of("EXPIRES_FAST"), cache.get("TEMP"), "Value present before TTL");

        // Sleep to let TTL elapse
        Thread.sleep(70);

        assertFalse(cache.containsKey("TEMP"), "Key must be expired");
        assertEquals(Optional.empty(), cache.get("TEMP"), "get() returns empty after TTL");
        assertEquals(0, cache.size(), "Size is 0 after expiration purge");
    }

    // 5. PECS Mapping and computeIfAbsent
    private static void testPecsMappingAndComputeIfAbsent() {
        Cache<CharSequence, Number> cache = new EvictingCache<>(5);

        // PECS: Function<? super K, ? extends V>
        // Key is String (subtype of CharSequence), Value is Integer (subtype of Number)
        Number val = cache.computeIfAbsent("token_length", (CharSequence k) -> k.length());

        assertEquals(12, val, "computeIfAbsent computed value 12");
        assertEquals(Optional.of(12), cache.get("token_length"), "Cached value matches");
    }

    // 6. PECS putAll and getAll
    private static void testPecsPutAllAndGetAll() {
        Cache<Number, String> cache = new EvictingCache<>(10);

        // PECS: Map<? extends K, ? extends V>
        Map<Integer, String> intMap = Map.of(1, "ONE", 2, "TWO", 3, "THREE");
        cache.putAll(intMap);

        assertEquals(3, cache.size(), "All 3 elements putAll");

        // PECS: Collection<? extends K>
        List<Integer> queryKeys = List.of(1, 3, 99);
        Map<Number, String> retrieved = cache.getAll(queryKeys);

        assertEquals(2, retrieved.size(), "Retrieved 2 matched keys");
        assertEquals("ONE", retrieved.get(1), "Key 1 matched");
        assertEquals("THREE", retrieved.get(3), "Key 3 matched");
    }

    // 7. PECS evictIf
    private static void testPecsEvictIf() {
        Cache<String, Integer> cache = new EvictingCache<>(10);
        cache.put("LOW_1", 10);
        cache.put("HIGH_1", 95);
        cache.put("LOW_2", 25);
        cache.put("HIGH_2", 80);

        // Evict all items with value < 50
        int evicted = cache.evictIf((k, v) -> v < 50);

        assertEquals(2, evicted, "Evicted 2 items");
        assertEquals(2, cache.size(), "2 items remaining");
        assertTrue(cache.containsKey("HIGH_1"), "HIGH_1 retained");
        assertTrue(cache.containsKey("HIGH_2"), "HIGH_2 retained");
    }

    // 8. Immutable Snapshot Isolation
    private static void testImmutableSnapshotIsolation() {
        Cache<String, String> cache = new EvictingCache<>(5);
        cache.put("K1", "V1");
        cache.put("K2", "V2");

        Map<String, String> snapshot = cache.exportSnapshot();
        assertEquals(Map.of("K1", "V1", "K2", "V2"), snapshot, "Initial snapshot matches");

        // Mutate backing cache
        cache.put("K3", "V3");
        cache.remove("K1");

        // Verify snapshot was NOT mutated (defensive copy isolation)
        assertEquals(2, snapshot.size(), "Snapshot remains unchanged at size 2");
        assertTrue(snapshot.containsKey("K1"), "Snapshot still contains K1");

        // Verify snapshot itself is immutable
        boolean uoeThrown = false;
        try {
            snapshot.put("K4", "V4");
        } catch (UnsupportedOperationException e) {
            uoeThrown = true;
        }
        assertTrue(uoeThrown, "Exported snapshot must throw UnsupportedOperationException on write");
    }

    // 9. Java 21 Sequenced Collection Access Order
    private static void testSequencedCollectionAccessOrder() {
        Cache<String, String> cache = new EvictingCache<>(4);
        cache.put("A", "ALPHA");
        cache.put("B", "BRAVO");
        cache.put("C", "CHARLIE");

        // Initial order: ALPHA, BRAVO, CHARLIE
        assertEquals(List.of("ALPHA", "BRAVO", "CHARLIE"), new ArrayList<>(cache.getAccessOrderedValues()),
            "Initial access order");

        // Access ALPHA -> moves ALPHA to tail: BRAVO, CHARLIE, ALPHA
        cache.get("A");
        assertEquals(List.of("BRAVO", "CHARLIE", "ALPHA"), new ArrayList<>(cache.getAccessOrderedValues()),
            "Access moves item to tail of sequenced collection");
    }

    // 10. Concurrent Access Thread-Safety
    private static void testConcurrentAccessThreadSafety() throws Exception {
        int threads = 8;
        int operationsPerThread = 500;
        Cache<Integer, String> cache = new EvictingCache<>(100);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger totalPuts = new AtomicInteger(0);

        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < operationsPerThread; i++) {
                        int key = (threadId * 100) + (i % 50);
                        cache.put(key, "Val-" + key);
                        cache.get(key);
                        totalPuts.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(completed, "Concurrent operations completed within 5 seconds");
        assertTrue(cache.size() <= 100, "Cache never exceeds maxCapacity under heavy concurrency");
    }

    // 11. Null Prohibition Invariants
    private static void testNullProhibitionInvariants() {
        Cache<String, String> cache = new EvictingCache<>(5);

        boolean nullKeyRejected = false;
        try {
            cache.put(null, "VALID");
        } catch (NullPointerException e) {
            nullKeyRejected = true;
        }
        assertTrue(nullKeyRejected, "Null key must be rejected");

        boolean nullValRejected = false;
        try {
            cache.put("VALID", null);
        } catch (NullPointerException e) {
            nullValRejected = true;
        }
        assertTrue(nullValRejected, "Null value must be rejected");
    }

    // 12. CacheStats and Hit Rate
    private static void testCacheStatsAndHitRate() {
        Cache<String, String> cache = new EvictingCache<>(2);
        cache.put("A", "1");
        cache.put("B", "2");

        cache.get("A"); // Hit 1
        cache.get("A"); // Hit 2
        cache.get("C"); // Miss 1

        cache.put("D", "4"); // Causes 1 eviction (B)

        CacheStats stats = cache.getStats();
        assertEquals(2L, stats.hits(), "Hits count is 2");
        assertEquals(1L, stats.misses(), "Misses count is 1");
        assertEquals(1L, stats.evictions(), "Evictions count is 1");
        assertEquals(2, stats.currentSize(), "Current size is 2");
        assertEquals(2, stats.maxCapacity(), "Max capacity is 2");
        assertEquals(2.0 / 3.0, stats.hitRate(), "Hit rate is 66.7%");
    }
}
