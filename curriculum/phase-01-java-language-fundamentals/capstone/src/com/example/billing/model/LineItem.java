package com.example.billing.model;

import java.util.Objects;

/**
 * Line item in a billing invoice.
 * Uses integer cents (long) to prevent IEEE 754 floating-point rounding errors.
 */
public record LineItem(String sku, String description, long unitPriceCents, int quantity, int discountPercent) {

    public LineItem {
        Objects.requireNonNull(sku, "sku must not be null");
        Objects.requireNonNull(description, "description must not be null");
        if (sku.isBlank()) {
            throw new IllegalArgumentException("sku must not be blank");
        }
        if (unitPriceCents < 0) {
            throw new IllegalArgumentException("unitPriceCents must be non-negative: " + unitPriceCents);
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be strictly positive: " + quantity);
        }
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("discountPercent must be between 0 and 100: " + discountPercent);
        }
    }
}
