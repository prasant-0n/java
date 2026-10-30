package com.example.algo.model;

import java.util.Objects;

/**
 * Domain entity representing a scheduled background task.
 * Implements Comparable for priority queue dispatching ordered by scheduled execution timestamp,
 * with monotonic sequence numbering to guarantee strict FIFO tie-breaking.
 */
public record Task(
    long taskId,
    String tenantId,
    String name,
    long scheduledEpochMs,
    long sequenceNumber,
    int memoryCostMb,
    int priorityWeight,
    long executionDurationMs
) implements Comparable<Task> {

    public Task {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (memoryCostMb <= 0) {
            throw new IllegalArgumentException("memoryCostMb must be positive: " + memoryCostMb);
        }
        if (priorityWeight <= 0) {
            throw new IllegalArgumentException("priorityWeight must be positive: " + priorityWeight);
        }
    }

    @Override
    public int compareTo(Task other) {
        if (this.scheduledEpochMs != other.scheduledEpochMs) {
            return Long.compare(this.scheduledEpochMs, other.scheduledEpochMs);
        }
        // Strict FIFO tie-breaking using monotonic sequence number
        return Long.compare(this.sequenceNumber, other.sequenceNumber);
    }
}
