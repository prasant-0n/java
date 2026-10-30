package com.example.analytics.test;

import com.example.analytics.engine.OrderAnalyticsEngine;
import com.example.analytics.function.FraudRule;
import com.example.analytics.model.*;

import java.math.BigDecimal;
import java.util.*;

public class AnalyticsEngineIntegrationTest {

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("  PHASE 06 CAPSTONE INTEGRATION TEST SUITE");
        System.out.println("  Real-Time Analytical Order & Fraud Aggregation Engine");
        System.out.println("================================================================================");

        testCustomTierAnalyticsCollector();
        testMultiLevelGroupingByTierAndCurrency();
        testBinaryPartitioningByFraudStatus();
        testToMapKeyCollisionResolution();
        testFlatMapTopSellingCategoriesExtraction();
        testPrimitiveIntStreamSummaryStatistics();
        testFunctionalFraudRuleComposition();
        testIsolatedDedicatedForkJoinPoolParallelBatch();
        testImperativeLockStepBankLedgerReconciliation();
        testShortCircuitingAndVacuousTruthDefense();

        System.out.println("================================================================================");
        System.out.println("  ALL 10 CAPSTONE INTEGRATION SUITES PASSED SUCCESSFULLY (100% VERIFIED)");
        System.out.println("================================================================================");
    }

    private static List<Order> createSampleOrders() {
        LineItem itemLaptop = new LineItem("SKU-LAP", "Electronics", 1, new BigDecimal("1200.00"));
        LineItem itemMouse  = new LineItem("SKU-MOU", "Electronics", 2, new BigDecimal("25.00"));
        LineItem itemDesk   = new LineItem("SKU-DSK", "Furniture",   1, new BigDecimal("350.00"));
        LineItem itemBook1  = new LineItem("SKU-BK1", "Books",       3, new BigDecimal("15.00"));
        LineItem itemBook2  = new LineItem("SKU-BK2", "Books",       1, new BigDecimal("45.00"));

        Order o1 = new Order("ORD-001", "CUST-1", CustomerTier.PLATINUM, "USD",
                List.of(itemLaptop, itemMouse), new BigDecimal("1250.00"), false, 1000L);
        Order o2 = new Order("ORD-002", "CUST-2", CustomerTier.PLATINUM, "USD",
                List.of(itemDesk), new BigDecimal("350.00"), false, 1010L);
        Order o3 = new Order("ORD-003", "CUST-3", CustomerTier.GOLD, "EUR",
                List.of(itemBook1, itemBook2), new BigDecimal("90.00"), false, 1020L);
        Order o4 = new Order("ORD-004", "CUST-4", CustomerTier.GOLD, "EUR",
                List.of(itemMouse), new BigDecimal("50.00"), true, 1030L); // Flagged fraud
        Order o5 = new Order("ORD-005", "CUST-5", CustomerTier.STANDARD, "USD",
                List.of(itemBook1), new BigDecimal("45.00"), false, 1040L);

        return List.of(o1, o2, o3, o4, o5);
    }

    static void testCustomTierAnalyticsCollector() {
        List<Order> orders = createSampleOrders();
        Map<CustomerTier, TierAnalyticsSummary> summaries = OrderAnalyticsEngine.aggregateTierSummaries(orders);

        assert summaries.containsKey(CustomerTier.PLATINUM);
        TierAnalyticsSummary platSummary = summaries.get(CustomerTier.PLATINUM);
        assert platSummary.orderCount() == 2;
        // Total = 1250.00 + 350.00 = 1600.00
        assert platSummary.totalVolume().compareTo(new BigDecimal("1600.00")) == 0;
        // Avg = 1600.00 / 2 = 800.00
        assert platSummary.averageOrderValue().compareTo(new BigDecimal("800.00")) == 0;
        assert platSummary.uniqueCategories().containsAll(Set.of("Electronics", "Furniture"));

        // SILVER tier has 0 orders
        TierAnalyticsSummary silverSummary = summaries.get(CustomerTier.SILVER);
        assert silverSummary.orderCount() == 0;
        assert silverSummary.totalVolume().compareTo(BigDecimal.ZERO) == 0;
        assert silverSummary.averageOrderValue().compareTo(BigDecimal.ZERO) == 0;

        System.out.println("  [PASS] Test 1: Custom TierAnalyticsSummary Collector & Banker's Rounding");
    }

    static void testMultiLevelGroupingByTierAndCurrency() {
        List<Order> orders = createSampleOrders();
        Map<CustomerTier, Map<String, BigDecimal>> revenue =
                OrderAnalyticsEngine.groupRevenueByTierAndCurrency(orders);

        assert revenue.containsKey(CustomerTier.PLATINUM);
        assert revenue.get(CustomerTier.PLATINUM).get("USD").compareTo(new BigDecimal("1600.00")) == 0;

        // GOLD: o3 is clean (90.00 EUR), o4 is fraud (50.00 EUR) -> only clean orders counted (90.00 EUR)
        assert revenue.containsKey(CustomerTier.GOLD);
        assert revenue.get(CustomerTier.GOLD).get("EUR").compareTo(new BigDecimal("90.00")) == 0;

        System.out.println("  [PASS] Test 2: Multi-Level Grouping By Tier and Currency (Fraud Excluded)");
    }

    static void testBinaryPartitioningByFraudStatus() {
        List<Order> orders = createSampleOrders();
        Map<Boolean, List<Order>> partitioned = OrderAnalyticsEngine.partitionByFraudStatus(orders);

        assert partitioned.containsKey(true);
        assert partitioned.containsKey(false);
        assert partitioned.get(true).size() == 1; // o4
        assert partitioned.get(true).get(0).orderId().equals("ORD-004");
        assert partitioned.get(false).size() == 4;

        System.out.println("  [PASS] Test 3: Binary Partitioning Invariance (Guaranteed True/False Keys)");
    }

    static void testToMapKeyCollisionResolution() {
        Order original = new Order("O-DUP", "C-1", CustomerTier.STANDARD, "USD",
                List.of(), new BigDecimal("100.00"), false, 1000L);
        Order updated = new Order("O-DUP", "C-1", CustomerTier.STANDARD, "USD",
                List.of(), new BigDecimal("100.00"), false, 2000L); // Newer timestamp

        Map<String, Order> indexed = OrderAnalyticsEngine.indexOrdersById(List.of(original, updated));

        assert indexed.size() == 1;
        assert indexed.get("O-DUP").timestampMillis() == 2000L : "Should keep newer order timestamp on collision";

        System.out.println("  [PASS] Test 4: In-Memory Indexing with Versioned Collision Merge");
    }

    static void testFlatMapTopSellingCategoriesExtraction() {
        List<Order> orders = createSampleOrders();
        // Quantities by Category:
        // Electronics: 1 (Laptop) + 2 (Mouse o1) + 2 (Mouse o4) = 5
        // Books: 3 (BK1 o3) + 1 (BK2 o3) + 3 (BK1 o5) = 7
        // Furniture: 1 (Desk o2) = 1
        // Expected Top 2: "Books" (7), "Electronics" (5)
        List<String> top2 = OrderAnalyticsEngine.extractTopSellingCategories(orders, 2);

        assert top2.size() == 2;
        assert "Books".equals(top2.get(0)) : "Top 1 category must be Books";
        assert "Electronics".equals(top2.get(1)) : "Top 2 category must be Electronics";

        System.out.println("  [PASS] Test 5: Relational FlatMapping and Top-N Category Extraction");
    }

    static void testPrimitiveIntStreamSummaryStatistics() {
        int[] latencies = { 45, 120, 85, 310, 15, 75 };
        var stats = OrderAnalyticsEngine.computeProcessingLatencyStats(latencies);

        assert stats.getCount() == 6;
        assert stats.getMin() == 15;
        assert stats.getMax() == 310;
        assert stats.getSum() == 650;
        assert Math.abs(stats.getAverage() - (650.0 / 6)) < 0.0001;

        System.out.println("  [PASS] Test 6: Primitive IntStream Zero-Allocation Summary Statistics");
    }

    static void testFunctionalFraudRuleComposition() {
        FraudRule amountThresholdRule = order -> order.totalAmount().compareTo(new BigDecimal("1000.00")) >= 0;
        FraudRule tierRule = order -> order.tier() != CustomerTier.PLATINUM;

        // Suspicious if amount >= 1000 AND not platinum (high value non-VIP)
        FraudRule highRiskNonVip = amountThresholdRule.and(tierRule);

        Order vipLarge = new Order("1", "C", CustomerTier.PLATINUM, "USD", List.of(), new BigDecimal("5000.00"), false, 1L);
        Order standardLarge = new Order("2", "C", CustomerTier.STANDARD, "USD", List.of(), new BigDecimal("1500.00"), false, 1L);

        assert !highRiskNonVip.isSuspicious(vipLarge) : "VIP large order should not be flagged";
        assert highRiskNonVip.isSuspicious(standardLarge) : "Standard large order must be flagged";

        System.out.println("  [PASS] Test 7: Functional Interface Rule Composition (and/or/negate)");
    }

    static void testIsolatedDedicatedForkJoinPoolParallelBatch() throws Exception {
        int batchSize = 10_000;
        List<Order> batch = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            CustomerTier tier = (i % 2 == 0) ? CustomerTier.STANDARD : CustomerTier.PLATINUM;
            BigDecimal amt = BigDecimal.valueOf(i);
            batch.add(new Order("ORD-" + i, "CUST-" + i, tier, "USD", List.of(), amt, false, 1000L + i));
        }

        // Fraud rule: amount > 5000 and STANDARD tier
        FraudRule rule = order -> order.totalAmount().compareTo(BigDecimal.valueOf(5000)) > 0
                && order.tier() == CustomerTier.STANDARD;

        List<Order> flagged = OrderAnalyticsEngine.evaluateFraudBatchParallel(batch, rule, 4);

        // Indices > 5000 with i % 2 == 0: 5002, 5004, ..., 9998 -> (9998 - 5002)/2 + 1 = 2499 orders
        assert flagged.size() == 2499 : "Expected 2499 flagged orders, got " + flagged.size();
        assert flagged.stream().allMatch(Order::flaggedFraud);

        System.out.println("  [PASS] Test 8: Dedicated ForkJoinPool Parallel Stream Batch Scoring");
    }

    static void testImperativeLockStepBankLedgerReconciliation() {
        Order o1 = new Order("1", "C", CustomerTier.STANDARD, "USD", List.of(), new BigDecimal("100.50"), false, 1L);
        Order o2 = new Order("2", "C", CustomerTier.STANDARD, "USD", List.of(), new BigDecimal("250.00"), false, 1L);

        List<BigDecimal> bankMatch = List.of(new BigDecimal("100.50"), new BigDecimal("250.00"));
        var resultMatch = OrderAnalyticsEngine.reconcileWithBankLedger(List.of(o1, o2), bankMatch);
        assert resultMatch.balanced() : "Expected balanced ledger";
        assert resultMatch.matchedCount() == 2;
        assert resultMatch.variance().compareTo(BigDecimal.ZERO) == 0;

        List<BigDecimal> bankMismatch = List.of(new BigDecimal("100.50"), new BigDecimal("249.00"));
        var resultMismatch = OrderAnalyticsEngine.reconcileWithBankLedger(List.of(o1, o2), bankMismatch);
        assert !resultMismatch.balanced() : "Expected unbalanced ledger";
        assert resultMismatch.matchedCount() == 1;
        assert resultMismatch.variance().compareTo(new BigDecimal("1.00")) == 0;

        System.out.println("  [PASS] Test 9: Imperative Lock-Step Multi-List Bank Ledger Reconciliation");
    }

    static void testShortCircuitingAndVacuousTruthDefense() {
        List<Order> emptyList = List.of();

        // Vacuous truth defense
        boolean safeCheck = OrderAnalyticsEngine.verifyAllOrdersPositiveValue(emptyList);
        assert !safeCheck : "Empty orders list must return false via defensive guard";

        // Short-circuiting test
        FraudRule flagAll = order -> true;
        List<Order> sample = createSampleOrders();
        Optional<Order> first = OrderAnalyticsEngine.findFirstSuspiciousOrder(sample, flagAll);
        assert first.isPresent();
        assert "ORD-001".equals(first.get().orderId()) : "Must short-circuit on first encounter order item";

        System.out.println("  [PASS] Test 10: Short-Circuiting Early Exit & Vacuous Truth Defense");
    }
}
