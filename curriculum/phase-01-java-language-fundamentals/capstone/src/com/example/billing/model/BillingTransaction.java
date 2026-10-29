package com.example.billing.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Immutable billing transaction aggregate root.
 */
public record BillingTransaction(
    String transactionId,
    String customerId,
    Currency currency,
    List<LineItem> lineItems,
    PaymentDetails paymentDetails,
    Instant timestamp
) {
    public BillingTransaction {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(lineItems, "lineItems must not be null");
        Objects.requireNonNull(paymentDetails, "paymentDetails must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");

        if (lineItems.isEmpty()) {
            throw new IllegalArgumentException("Transaction must contain at least one line item");
        }

        // Defensive copy ensures caller cannot mutate internal list
        lineItems = List.copyOf(lineItems);
    }
}
