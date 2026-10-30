package com.example.etl.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable domain record representing a validated financial transaction.
 */
public record TransactionRecord(
    UUID transactionId,
    String customerId,
    BigDecimal amount,
    String currency,
    Instant timestamp,
    String category,
    String description
) {
    public TransactionRecord {
        Objects.requireNonNull(transactionId, "transactionId cannot be null");
        Objects.requireNonNull(customerId, "customerId cannot be null");
        Objects.requireNonNull(amount, "amount cannot be null");
        Objects.requireNonNull(currency, "currency cannot be null");
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
        Objects.requireNonNull(category, "category cannot be null");
        Objects.requireNonNull(description, "description cannot be null");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be strictly positive: " + amount);
        }
    }
}
