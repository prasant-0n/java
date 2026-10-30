package com.example.etl.model;

import java.math.BigDecimal;

/**
 * Summary metrics aggregated across an ETL batch execution.
 */
public record ProcessingSummary(
    long totalRecordsRead,
    long validRecordsWritten,
    long deadLetterRecordsCount,
    BigDecimal totalVolumeUsd,
    long processingDurationMs
) {
    public double throughputRecordsPerSecond() {
        if (processingDurationMs <= 0) return 0.0;
        return (totalRecordsRead * 1000.0) / processingDurationMs;
    }
}
