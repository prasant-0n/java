package com.example.cache.model;

public record CacheStats(
    long hits,
    long misses,
    long evictions,
    long expirations,
    int currentSize,
    int maxCapacity
) {
    public double hitRate() {
        long total = hits + misses;
        if (total == 0) return 0.0;
        return (double) hits / total;
    }
}
