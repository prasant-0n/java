package com.example.billing.model;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Completed Customer Invoice Entity.
 * Synthesizes Concept P05-C001 (Instant UTC Storage & ZonedDateTime Wall Clock),
 * Concept P05-C005 (Optional Return Types), and Concept P05-C006 (UUIDv7 Primary Key).
 */
public record Invoice(
        UUID id,
        Instant createdAtUtc,
        ZonedDateTime localBillingTime,
        String customerId,
        Money subtotal,
        Money discount,
        Money tax,
        Money total,
        Optional<String> appliedPromoCode
) {
    public Invoice {
        Objects.requireNonNull(id, "Invoice id must not be null");
        Objects.requireNonNull(createdAtUtc, "createdAtUtc must not be null");
        Objects.requireNonNull(localBillingTime, "localBillingTime must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(subtotal, "subtotal must not be null");
        Objects.requireNonNull(discount, "discount must not be null");
        Objects.requireNonNull(tax, "tax must not be null");
        Objects.requireNonNull(total, "total must not be null");
        // Ensure Optional is non-null wrapper
        appliedPromoCode = Objects.requireNonNullElse(appliedPromoCode, Optional.empty());
    }
}
