package com.example.analytics.engine;

import com.example.analytics.function.AnalyticsCollector;
import com.example.analytics.function.FraudRule;
import com.example.analytics.model.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class OrderAnalyticsEngine {

    private OrderAnalyticsEngine() {}

    // 1. Custom Collector Reduction: Tier -> TierAnalyticsSummary
    public static Map<CustomerTier, TierAnalyticsSummary> aggregateTierSummaries(List<Order> orders) {
        Objects.requireNonNull(orders, "orders required");

        Map<CustomerTier, List<Order>> byTier = orders.stream()
                .collect(Collectors.groupingBy(Order::tier));

        Map<CustomerTier, TierAnalyticsSummary> summaries = new EnumMap<>(CustomerTier.class);
        for (CustomerTier tier : CustomerTier.values()) {
            List<Order> tierOrders = byTier.getOrDefault(tier, List.of());
            TierAnalyticsSummary summary = tierOrders.stream()
                    .collect(AnalyticsCollector.toTierSummary(tier));
            summaries.put(tier, summary);
        }
        return Collections.unmodifiableMap(summaries);
    }

    // 2. Multi-Level Grouping: Tier -> Currency -> Total Revenue
    public static Map<CustomerTier, Map<String, BigDecimal>> groupRevenueByTierAndCurrency(List<Order> orders) {
        Objects.requireNonNull(orders, "orders required");

        return orders.stream()
                .filter(order -> !order.flaggedFraud())
                .collect(Collectors.groupingBy(
                        Order::tier,
                        () -> new EnumMap<>(CustomerTier.class),
                        Collectors.groupingBy(
                                Order::currency,
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        Order::totalAmount,
                                        BigDecimal::add
                                )
                        )
                ));
    }

    // 3. Binary Partitioning: Clean vs Fraudulent Orders
    public static Map<Boolean, List<Order>> partitionByFraudStatus(List<Order> orders) {
        Objects.requireNonNull(orders, "orders required");

        return orders.stream()
                .collect(Collectors.partitioningBy(Order::flaggedFraud));
    }

    // 4. In-Memory Indexing with Collision Merge Function
    public static Map<String, Order> indexOrdersById(List<Order> orders) {
        Objects.requireNonNull(orders, "orders required");

        return orders.stream()
                .collect(Collectors.toMap(
                        Order::orderId,
                        order -> order,
                        (existing, replacement) -> replacement.timestampMillis() >= existing.timestampMillis()
                                ? replacement : existing, // Keep latest order version
                        LinkedHashMap::new
                ));
    }

    // 5. Relational FlatMapping: Top-Selling Product Categories by Quantity
    public static List<String> extractTopSellingCategories(List<Order> orders, int topN) {
        Objects.requireNonNull(orders, "orders required");
        if (topN <= 0) return List.of();

        Map<String, Integer> categoryTotals = orders.stream()
                .flatMap(order -> order.items().stream())
                .collect(Collectors.groupingBy(
                        LineItem::category,
                        Collectors.summingInt(LineItem::quantity)
                ));

        return categoryTotals.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .toList();
    }

    // 6. Primitive IntStream Summary Statistics
    public static IntSummaryStatistics computeProcessingLatencyStats(int[] latencyMillis) {
        Objects.requireNonNull(latencyMillis, "latencyMillis required");

        return IntStream.of(latencyMillis).summaryStatistics();
    }

    // 7. Parallel Stream in Isolated Dedicated ForkJoinPool
    public static List<Order> evaluateFraudBatchParallel(
            List<Order> orders, FraudRule combinedRule, int parallelism)
            throws InterruptedException, ExecutionException {
        Objects.requireNonNull(orders, "orders required");
        Objects.requireNonNull(combinedRule, "combinedRule required");

        ForkJoinPool customPool = new ForkJoinPool(parallelism);
        try {
            return customPool.submit(() ->
                    orders.parallelStream()
                            .filter(combinedRule::isSuspicious)
                            .map(order -> new Order(
                                    order.orderId(),
                                    order.customerId(),
                                    order.tier(),
                                    order.currency(),
                                    order.items(),
                                    order.totalAmount(),
                                    true, // Mark as fraud
                                    order.timestampMillis()
                            ))
                            .toList()
            ).get();
        } finally {
            customPool.shutdown();
            if (!customPool.awaitTermination(3, TimeUnit.SECONDS)) {
                customPool.shutdownNow();
            }
        }
    }

    // 8. Imperative Loop Advantage: Lock-Step Reconciliation
    public record ReconciliationResult(boolean balanced, int matchedCount, BigDecimal variance) {}

    public static ReconciliationResult reconcileWithBankLedger(
            List<Order> orders, List<BigDecimal> bankLedgerEntries) {
        Objects.requireNonNull(orders, "orders required");
        Objects.requireNonNull(bankLedgerEntries, "bankLedgerEntries required");

        if (orders.size() != bankLedgerEntries.size()) {
            throw new IllegalArgumentException(String.format(
                    "Mismatched count: %d orders vs %d bank entries",
                    orders.size(), bankLedgerEntries.size()
            ));
        }

        BigDecimal totalVariance = BigDecimal.ZERO;
        int matched = 0;

        // Imperative loop with direct index access and early variance tracking
        for (int i = 0; i < orders.size(); i++) {
            BigDecimal orderAmt = orders.get(i).totalAmount();
            BigDecimal bankAmt = bankLedgerEntries.get(i);
            BigDecimal diff = orderAmt.subtract(bankAmt);

            totalVariance = totalVariance.add(diff.abs());
            if (diff.compareTo(BigDecimal.ZERO) == 0) {
                matched++;
            }
        }

        return new ReconciliationResult(
                totalVariance.compareTo(BigDecimal.ZERO) == 0,
                matched,
                totalVariance
        );
    }

    // 9. Short-Circuiting Fast Lookup
    public static Optional<Order> findFirstSuspiciousOrder(List<Order> orders, FraudRule rule) {
        Objects.requireNonNull(orders, "orders required");
        Objects.requireNonNull(rule, "rule required");

        return orders.stream()
                .filter(rule::isSuspicious)
                .findFirst();
    }

    // 10. Vacuous Truth Defensive Gate
    public static boolean verifyAllOrdersPositiveValue(List<Order> orders) {
        Objects.requireNonNull(orders, "orders required");
        if (orders.isEmpty()) {
            return false; // Prevent vacuous truth pass on empty set
        }

        return orders.stream()
                .allMatch(o -> o.totalAmount().compareTo(BigDecimal.ZERO) > 0);
    }
}
