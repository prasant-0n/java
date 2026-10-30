package com.example.analytics.model;

import java.math.BigDecimal;
import java.util.Objects;

public record LineItem(
        String sku,
        String category,
        int quantity,
        BigDecimal unitPrice
) {
    public LineItem {
        Objects.requireNonNull(sku, "sku required");
        Objects.requireNonNull(category, "category required");
        Objects.requireNonNull(unitPrice, "unitPrice required");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
