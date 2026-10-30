package com.example.algo.limiter;

import com.example.algo.ds.SeparateChainingMap;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * High-precision multi-tenant Sliding Window Log Rate Limiter.
 * Employs a sliding time window (e.g. 1000 ms) tracked by millisecond timestamp deques.
 * Defends against boundary burst attacks that plague fixed-window counters.
 */
public class SlidingWindowRateLimiter {
    private final int maxRequestsPerWindow;
    private final long windowSizeMs;
    private final SeparateChainingMap<String, Deque<Long>> tenantWindows = new SeparateChainingMap<>();

    public SlidingWindowRateLimiter(int maxRequestsPerWindow, long windowSizeMs) {
        if (maxRequestsPerWindow <= 0) throw new IllegalArgumentException("maxRequestsPerWindow must be positive");
        if (windowSizeMs <= 0) throw new IllegalArgumentException("windowSizeMs must be positive");
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowSizeMs = windowSizeMs;
    }

    /**
     * Checks and records an inbound request for the specified tenant at the given timestamp.
     * Returns true if allowed, false if rate limited.
     */
    public synchronized boolean allowRequest(String tenantId, long currentTimestampMs) {
        Deque<Long> window = tenantWindows.get(tenantId);
        if (window == null) {
            window = new ArrayDeque<>();
            tenantWindows.put(tenantId, window);
        }

        long threshold = currentTimestampMs - windowSizeMs;

        // Slide the window: remove timestamps older than the window boundary
        while (!window.isEmpty() && window.peekFirst() <= threshold) {
            window.pollFirst();
        }

        if (window.size() < maxRequestsPerWindow) {
            window.offerLast(currentTimestampMs);
            return true;
        }

        return false;
    }

    public synchronized int getActiveRequestCount(String tenantId, long currentTimestampMs) {
        Deque<Long> window = tenantWindows.get(tenantId);
        if (window == null) return 0;
        long threshold = currentTimestampMs - windowSizeMs;
        while (!window.isEmpty() && window.peekFirst() <= threshold) {
            window.pollFirst();
        }
        return window.size();
    }
}
