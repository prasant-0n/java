package com.example.pipeline.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Immutable transaction transfer request with strict validation invariants.
 */
public record TransactionRequest(
        String transactionId,
        String sourceAccountId,
        String targetAccountId,
        BigDecimal amount,
        String description
) {
    public TransactionRequest {
        Objects.requireNonNull(transactionId, "Transaction ID cannot be null");
        Objects.requireNonNull(sourceAccountId, "Source account ID cannot be null");
        Objects.requireNonNull(targetAccountId, "Target account ID cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");

        if (transactionId.isBlank()) {
            throw new IllegalArgumentException("Transaction ID cannot be blank");
        }
        if (sourceAccountId.isBlank()) {
            throw new IllegalArgumentException("Source account ID cannot be blank");
        }
        if (targetAccountId.isBlank()) {
            throw new IllegalArgumentException("Target account ID cannot be blank");
        }
        if (sourceAccountId.equals(targetAccountId)) {
            throw new IllegalArgumentException("Source and target accounts must be distinct: " + sourceAccountId);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be strictly positive: " + amount);
        }
    }
}
