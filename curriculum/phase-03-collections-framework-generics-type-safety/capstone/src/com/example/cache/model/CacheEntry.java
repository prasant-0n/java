package com.example.cache.model;

import java.time.Instant;
import java.util.Objects;

public final class CacheEntry<K, V> implements Comparable<CacheEntry<K, V>> {

    private final K key;
    private V value;
    private final long createdAtEpochMs;
    private long expiresAtEpochMs; // Long.MAX_VALUE if no TTL
    private long lastAccessedEpochMs;
    private long accessCount;

    public CacheEntry(K key, V value, long expiresAtEpochMs) {
        this.key = Objects.requireNonNull(key, "Cache entry key cannot be null");
        this.value = Objects.requireNonNull(value, "Cache entry value cannot be null");
        this.createdAtEpochMs = System.currentTimeMillis();
        this.expiresAtEpochMs = expiresAtEpochMs;
        this.lastAccessedEpochMs = this.createdAtEpochMs;
        this.accessCount = 0;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = Objects.requireNonNull(value, "Updated value cannot be null");
    }

    public long getCreatedAtEpochMs() {
        return createdAtEpochMs;
    }

    public long getExpiresAtEpochMs() {
        return expiresAtEpochMs;
    }

    public void setExpiresAtEpochMs(long expiresAtEpochMs) {
        this.expiresAtEpochMs = expiresAtEpochMs;
    }

    public long getLastAccessedEpochMs() {
        return lastAccessedEpochMs;
    }

    public long getAccessCount() {
        return accessCount;
    }

    public void recordAccess() {
        this.lastAccessedEpochMs = System.currentTimeMillis();
        this.accessCount++;
    }

    public boolean isExpired(long currentEpochMs) {
        return currentEpochMs >= expiresAtEpochMs;
    }

    @Override
    public int compareTo(CacheEntry<K, V> other) {
        // Natural ordering for TTL priority queue: earliest expiration first
        return Long.compare(this.expiresAtEpochMs, other.expiresAtEpochMs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheEntry<?, ?> other)) return false;
        return Objects.equals(this.key, other.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.key);
    }

    @Override
    public String toString() {
        return "CacheEntry{" +
            "key=" + key +
            ", value=" + value +
            ", expiresAt=" + Instant.ofEpochMilli(expiresAtEpochMs) +
            ", accessCount=" + accessCount +
            '}';
    }
}
