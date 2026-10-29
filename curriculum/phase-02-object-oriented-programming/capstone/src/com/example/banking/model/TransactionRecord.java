package com.example.banking.model;

import java.time.Instant;
import java.util.Objects;

public record TransactionRecord(
    String transactionId,
    TransactionType type,
    Money amount,
    Money resultingBalance,
    String description,
    Instant timestamp
) {
    public TransactionRecord {
        Objects.requireNonNull(transactionId, "transactionId required");
        Objects.requireNonNull(type, "type required");
        Objects.requireNonNull(amount, "amount required");
        Objects.requireNonNull(resultingBalance, "resultingBalance required");
        Objects.requireNonNull(description, "description required");
        Objects.requireNonNull(timestamp, "timestamp required");
    }
}
