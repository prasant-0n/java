package com.example.algo.model;

/**
 * Metric snapshot for historical task execution auditing and binary search queries.
 */
public record ExecutionMetrics(
    long taskId,
    long executedTimestampMs,
    long durationNanos,
    boolean succeeded
) implements Comparable<ExecutionMetrics> {

    @Override
    public int compareTo(ExecutionMetrics other) {
        return Long.compare(this.executedTimestampMs, other.executedTimestampMs);
    }
}
