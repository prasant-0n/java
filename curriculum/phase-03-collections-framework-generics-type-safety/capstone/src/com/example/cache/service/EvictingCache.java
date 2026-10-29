package com.example.cache.service;

import com.example.cache.model.CacheEntry;
import com.example.cache.model.CacheStats;
import com.example.cache.model.EvictionPolicy;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiPredicate;
import java.util.function.Function;

public final class EvictingCache<K, V> implements Cache<K, V> {

    private final int maxCapacity;
    private final Duration defaultTtl;
    private final EvictionPolicy evictionPolicy;
    private final ReentrantLock lock = new ReentrantLock();

    // Access-order LinkedHashMap for LRU tracking
    private final LinkedHashMap<K, CacheEntry<K, V>> map;

    // Min-heap PriorityQueue for fast TTL expiration
    private final PriorityQueue<CacheEntry<K, V>> expirationQueue;

    // Operational statistics
    private long hits = 0;
    private long misses = 0;
    private long evictions = 0;
    private long expirations = 0;

    public EvictingCache(int maxCapacity, Duration defaultTtl, EvictionPolicy evictionPolicy) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Max capacity must be strictly positive: " + maxCapacity);
        }
        this.maxCapacity = maxCapacity;
        this.defaultTtl = Objects.requireNonNull(defaultTtl, "Default TTL cannot be null");
        this.evictionPolicy = Objects.requireNonNull(evictionPolicy, "Eviction policy cannot be null");

        boolean accessOrder = (evictionPolicy == EvictionPolicy.LRU);
        this.map = new LinkedHashMap<>(maxCapacity, 0.75f, accessOrder);
        this.expirationQueue = new PriorityQueue<>();
    }

    public EvictingCache(int maxCapacity, Duration defaultTtl) {
        this(maxCapacity, defaultTtl, EvictionPolicy.LRU);
    }

    public EvictingCache(int maxCapacity) {
        this(maxCapacity, Duration.ofDays(3650), EvictionPolicy.LRU); // Effectively no TTL
    }

    @Override
    public void put(K key, V value) {
        put(key, value, defaultTtl);
    }

    @Override
    public void put(K key, V value, Duration ttl) {
        Objects.requireNonNull(key, "Cache key cannot be null");
        Objects.requireNonNull(value, "Cache value cannot be null");
        Objects.requireNonNull(ttl, "TTL cannot be null");

        lock.lock();
        try {
            purgeExpired();

            long expiresAt = ttl.isZero() ? Long.MAX_VALUE : System.currentTimeMillis() + ttl.toMillis();
            CacheEntry<K, V> existing = map.get(key);

            if (existing != null) {
                // Update existing entry
                expirationQueue.remove(existing);
                existing.setValue(value);
                existing.setExpiresAtEpochMs(expiresAt);
                existing.recordAccess();
                expirationQueue.offer(existing);
                map.put(key, existing); // Re-insert to refresh LRU access order
            } else {
                // Check capacity before inserting new entry
                if (map.size() >= maxCapacity) {
                    evictEldest();
                }
                CacheEntry<K, V> newEntry = new CacheEntry<>(key, value, expiresAt);
                map.put(key, newEntry);
                if (expiresAt != Long.MAX_VALUE) {
                    expirationQueue.offer(newEntry);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<V> get(K key) {
        Objects.requireNonNull(key, "Cache key cannot be null");

        lock.lock();
        try {
            CacheEntry<K, V> entry = map.get(key);
            long now = System.currentTimeMillis();

            if (entry == null) {
                misses++;
                return Optional.empty();
            }

            if (entry.isExpired(now)) {
                // Lazy expiration cleanup
                map.remove(key);
                expirationQueue.remove(entry);
                expirations++;
                misses++;
                return Optional.empty();
            }

            entry.recordAccess();
            hits++;
            return Optional.of(entry.getValue());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsKey(K key) {
        Objects.requireNonNull(key, "Cache key cannot be null");
        lock.lock();
        try {
            purgeExpired();
            CacheEntry<K, V> entry = map.get(key);
            if (entry == null) return false;
            if (entry.isExpired(System.currentTimeMillis())) {
                map.remove(key);
                expirationQueue.remove(entry);
                expirations++;
                return false;
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean remove(K key) {
        Objects.requireNonNull(key, "Cache key cannot be null");
        lock.lock();
        try {
            CacheEntry<K, V> removed = map.remove(key);
            if (removed != null) {
                expirationQueue.remove(removed);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            map.clear();
            expirationQueue.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            purgeExpired();
            return map.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return computeIfAbsent(key, mappingFunction, defaultTtl);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, Duration ttl) {
        Objects.requireNonNull(key, "Cache key cannot be null");
        Objects.requireNonNull(mappingFunction, "Mapping function cannot be null");
        Objects.requireNonNull(ttl, "TTL cannot be null");

        lock.lock();
        try {
            Optional<V> existing = get(key);
            if (existing.isPresent()) {
                return existing.get();
            }
            V computed = mappingFunction.apply(key);
            Objects.requireNonNull(computed, "Mapping function returned null value");
            put(key, computed, ttl);
            return computed;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> entries) {
        putAll(entries, defaultTtl);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> entries, Duration ttl) {
        Objects.requireNonNull(entries, "Entries map cannot be null");
        lock.lock();
        try {
            for (Map.Entry<? extends K, ? extends V> entry : entries.entrySet()) {
                put(entry.getKey(), entry.getValue(), ttl);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Map<K, V> getAll(Collection<? extends K> keys) {
        Objects.requireNonNull(keys, "Keys collection cannot be null");
        lock.lock();
        try {
            Map<K, V> result = new LinkedHashMap<>();
            for (K key : keys) {
                Optional<V> val = get(key);
                val.ifPresent(v -> result.put(key, v));
            }
            return Collections.unmodifiableMap(result);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int evictIf(BiPredicate<? super K, ? super V> predicate) {
        Objects.requireNonNull(predicate, "Eviction predicate cannot be null");
        lock.lock();
        try {
            purgeExpired();
            int count = 0;
            Iterator<Map.Entry<K, CacheEntry<K, V>>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<K, CacheEntry<K, V>> entry = it.next();
                if (predicate.test(entry.getKey(), entry.getValue().getValue())) {
                    expirationQueue.remove(entry.getValue());
                    it.remove(); // Safe iterator removal!
                    count++;
                    evictions++;
                }
            }
            return count;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public SequencedCollection<V> getAccessOrderedValues() {
        lock.lock();
        try {
            purgeExpired();
            List<V> values = new ArrayList<>(map.size());
            for (CacheEntry<K, V> entry : map.values()) {
                values.add(entry.getValue());
            }
            return Collections.unmodifiableList(values);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Map<K, V> exportSnapshot() {
        lock.lock();
        try {
            purgeExpired();
            Map<K, V> snapshot = new LinkedHashMap<>(map.size());
            for (Map.Entry<K, CacheEntry<K, V>> entry : map.entrySet()) {
                snapshot.put(entry.getKey(), entry.getValue().getValue());
            }
            return Map.copyOf(snapshot); // Truly immutable snapshot
        } finally {
            lock.unlock();
        }
    }

    @Override
    public CacheStats getStats() {
        lock.lock();
        try {
            return new CacheStats(hits, misses, evictions, expirations, map.size(), maxCapacity);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void purgeExpired() {
        long now = System.currentTimeMillis();
        while (!expirationQueue.isEmpty()) {
            CacheEntry<K, V> earliest = expirationQueue.peek();
            if (earliest.isExpired(now)) {
                expirationQueue.poll();
                CacheEntry<K, V> removed = map.remove(earliest.getKey());
                if (removed != null) {
                    expirations++;
                }
            } else {
                break; // Earliest unexpired item implies subsequent items are also unexpired
            }
        }
    }

    private void evictEldest() {
        Iterator<Map.Entry<K, CacheEntry<K, V>>> it = map.entrySet().iterator();
        if (it.hasNext()) {
            Map.Entry<K, CacheEntry<K, V>> eldest = it.next();
            expirationQueue.remove(eldest.getValue());
            it.remove(); // Safely removes the least recently accessed item!
            evictions++;
        }
    }
}
