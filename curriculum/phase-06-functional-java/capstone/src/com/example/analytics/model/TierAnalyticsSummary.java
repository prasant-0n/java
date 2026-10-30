package com.example.analytics.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

public record TierAnalyticsSummary(
        CustomerTier tier,
        long orderCount,
        BigDecimal totalVolume,
        BigDecimal averageOrderValue,
        Set<String> uniqueCategories
) {
    public TierAnalyticsSummary {
        Objects.requireNonNull(tier, "tier required");
        Objects.requireNonNull(totalVolume, "totalVolume required");
        Objects.requireNonNull(averageOrderValue, "averageOrderValue required");
        uniqueCategories = uniqueCategories != null ? Set.copyOf(uniqueCategories) : Set.of();
    }
}
