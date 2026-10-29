package com.example.pipeline.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable transaction receipt issued upon successful transfer execution.
 */
public record TransactionReceipt(
        String transactionId,
        String sourceAccountId,
        String targetAccountId,
        BigDecimal amountTransferred,
        Instant executedAt,
        TransactionState state
) {
    public TransactionReceipt {
        Objects.requireNonNull(transactionId, "Transaction ID cannot be null");
        Objects.requireNonNull(sourceAccountId, "Source account ID cannot be null");
        Objects.requireNonNull(targetAccountId, "Target account ID cannot be null");
        Objects.requireNonNull(amountTransferred, "Amount cannot be null");
        Objects.requireNonNull(executedAt, "ExecutedAt timestamp cannot be null");
        Objects.requireNonNull(state, "Transaction state cannot be null");
    }
}
