package com.example.analytics.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record Order(
        String orderId,
        String customerId,
        CustomerTier tier,
        String currency,
        List<LineItem> items,
        BigDecimal totalAmount,
        boolean flaggedFraud,
        long timestampMillis
) {
    public Order {
        Objects.requireNonNull(orderId, "orderId required");
        Objects.requireNonNull(customerId, "customerId required");
        Objects.requireNonNull(tier, "tier required");
        Objects.requireNonNull(currency, "currency required");
        Objects.requireNonNull(totalAmount, "totalAmount required");
        items = items != null ? List.copyOf(items) : List.of();
    }
}
