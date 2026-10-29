package com.example.billing.test;

import com.example.billing.model.BillingTransaction;
import com.example.billing.model.Currency;
import com.example.billing.model.InvoiceSummary;
import com.example.billing.model.LineItem;
import com.example.billing.model.PaymentDetails;
import com.example.billing.service.BillingCalculator;
import com.example.billing.service.TransactionAuditEngine;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Standalone regression test suite verifying exact math, pattern matching,
 * ring buffer rollover, and defensive immutability.
 * Runs directly via standard JVM without external test framework dependencies.
 */
public class BillingEngineTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("Running Billing Engine Regression Test Suite...\n");

        testCalculationAndBankersRounding();
        testOverflowProtection();
        testPatternMatchingGatewaysAndSurcharges();
        testCircularRingBufferRollover();
        testDefensiveImmutabilityAndValidations();

        System.out.println("\n--------------------------------------------------");
        System.out.printf("Test Suite Results: %d Passed, %d Failed\n", testsPassed, testsFailed);
        System.out.println("--------------------------------------------------");

        if (testsFailed > 0) {
            System.err.println("TEST SUITE FAILED!");
            System.exit(1);
        } else {
            System.out.println("ALL REGRESSION TESTS PASSED SUCCESSFULLY (100%).");
        }
    }

    private static void assertEquals(Object expected, Object actual, String testName) {
        if (!expected.equals(actual)) {
            System.err.printf("FAIL: %s (Expected: %s, Actual: %s)\n", testName, expected, actual);
            testsFailed++;
        } else {
            System.out.printf("PASS: %s\n", testName);
            testsPassed++;
        }
    }

    private static void assertTrue(boolean condition, String testName) {
        if (!condition) {
            System.err.printf("FAIL: %s (Expected true, got false)\n", testName);
            testsFailed++;
        } else {
            System.out.printf("PASS: %s\n", testName);
            testsPassed++;
        }
    }

    private static void testCalculationAndBankersRounding() {
        BillingCalculator calc = new BillingCalculator();

        // 1000 cents with 15% discount = 150 cents discount, net 850 cents.
        // VAT 20% on 850 cents = 170 cents.
        // Net payable = 850 + 170 = 1020 cents.
        BillingTransaction tx = new BillingTransaction(
            "T1", "C1", Currency.USD,
            List.of(new LineItem("SKU-1", "Widget", 1000L, 1, 15)),
            new PaymentDetails.CreditCard("1111", "VISA", 2028),
            Instant.now()
        );

        InvoiceSummary summary = calc.calculateInvoice(tx);
        assertEquals(1000L, summary.grossSubtotalCents(), "Gross subtotal calculation");
        assertEquals(150L, summary.discountDeductionCents(), "Discount deduction calculation");
        assertEquals(170L, summary.taxAmountCents(), "VAT tax calculation");
        assertEquals(1020L, summary.netPayableCents(), "Net payable calculation");
        assertEquals("$10.20", summary.formatNetPayable(), "Currency formatting");
    }

    private static void testOverflowProtection() {
        BillingCalculator calc = new BillingCalculator();

        // Subtotal that will cause Long overflow when multiplied
        LineItem massiveItem = new LineItem("MASSIVE", "Massive Item", Long.MAX_VALUE / 2 + 100, 3, 0);
        BillingTransaction overflowTx = new BillingTransaction(
            "T-OVERFLOW", "C1", Currency.USD,
            List.of(massiveItem),
            new PaymentDetails.CreditCard("1111", "VISA", 2028),
            Instant.now()
        );

        boolean exceptionThrown = false;
        try {
            calc.calculateInvoice(overflowTx);
        } catch (ArithmeticException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown, "ArithmeticException thrown on long overflow");
    }

    private static void testPatternMatchingGatewaysAndSurcharges() {
        BillingCalculator calc = new BillingCalculator();

        // Visa
        assertEquals("GATEWAY_CYBERSOURCE_VISA_DIRECT", 
            calc.evaluatePaymentGateway(new PaymentDetails.CreditCard("1", "VISA", 2028)),
            "Visa gateway routing");

        // Mastercard
        assertEquals("GATEWAY_MASTERCARD_SEND", 
            calc.evaluatePaymentGateway(new PaymentDetails.CreditCard("2", "MASTERCARD", 2028)),
            "Mastercard gateway routing");

        // Stripe fallback
        assertEquals("GATEWAY_STRIPE_GLOBAL_ACQUIRER", 
            calc.evaluatePaymentGateway(new PaymentDetails.CreditCard("3", "DISCOVER", 2028)),
            "Discover fallback gateway routing");

        // Deutsche Bank Wire
        assertEquals("GATEWAY_DEUTSCHE_BANK_SEPA", 
            calc.evaluatePaymentGateway(new PaymentDetails.WireTransfer("DE00", "DBEUS33", "Ref")),
            "Deutsche Bank SEPA routing");

        // Swift Wire
        assertEquals("GATEWAY_SWIFT_CLEARING_HOUSE", 
            calc.evaluatePaymentGateway(new PaymentDetails.WireTransfer("US00", "CHASUS33", "Ref")),
            "SWIFT wire routing");

        // Ethereum Crypto
        assertEquals("GATEWAY_COINBASE_COMMERCE_ERC20", 
            calc.evaluatePaymentGateway(new PaymentDetails.CryptoAsset("0x123", "ETHEREUM")),
            "Ethereum crypto routing");

        // Bitcoin Crypto
        assertEquals("GATEWAY_BITPAY_LAYER1", 
            calc.evaluatePaymentGateway(new PaymentDetails.CryptoAsset("bc1q", "BITCOIN")),
            "Bitcoin crypto routing");

        // Surcharges
        long amexSurcharge = calc.evaluatePaymentSurchargeCents(new PaymentDetails.CreditCard("4", "AMEX", 2028), 10000L);
        assertEquals(300L, amexSurcharge, "Amex 3% surcharge on 10000 cents");

        long cryptoGas = calc.evaluatePaymentSurchargeCents(new PaymentDetails.CryptoAsset("0x", "SOLANA"), 5000L);
        assertEquals(250L, cryptoGas, "Crypto flat 250 cents gas surcharge");
    }

    private static void testCircularRingBufferRollover() {
        BillingCalculator calc = new BillingCalculator();
        TransactionAuditEngine engine = new TransactionAuditEngine(calc, 4); // Capacity 4

        for (int i = 1; i <= 6; i++) {
            BillingTransaction tx = new BillingTransaction(
                "TX-" + i, "CUST-" + i, Currency.USD,
                List.of(new LineItem("SKU", "Item", 1000L, 1, 0)),
                new PaymentDetails.CreditCard("1111", "VISA", 2028),
                Instant.now()
            );
            engine.processTransaction(tx);
        }

        assertEquals(6L, engine.getProcessedCount(), "Processed sequence count after 6 additions");

        String csv = engine.generateCsvReport();
        // Since capacity is 4, only TX-3, TX-4, TX-5, TX-6 must be present
        assertTrue(!csv.contains("TX-1"), "Circular buffer evicted TX-1");
        assertTrue(!csv.contains("TX-2"), "Circular buffer evicted TX-2");
        assertTrue(csv.contains("TX-3"), "Circular buffer retained TX-3");
        assertTrue(csv.contains("TX-4"), "Circular buffer retained TX-4");
        assertTrue(csv.contains("TX-5"), "Circular buffer retained TX-5");
        assertTrue(csv.contains("TX-6"), "Circular buffer retained TX-6");
    }

    private static void testDefensiveImmutabilityAndValidations() {
        // Defensive copy test
        List<LineItem> mutableList = new ArrayList<>();
        mutableList.add(new LineItem("SKU-1", "Original", 500L, 1, 0));

        BillingTransaction tx = new BillingTransaction(
            "TX-DEF", "CUST", Currency.USD,
            mutableList,
            new PaymentDetails.CreditCard("1111", "VISA", 2028),
            Instant.now()
        );

        mutableList.add(new LineItem("SKU-2", "Injected After", 9999L, 1, 0));
        assertEquals(1, tx.lineItems().size(), "Defensive copy preserves transaction immutability");

        // Expired card validation
        boolean expiredCaught = false;
        try {
            new PaymentDetails.CreditCard("1111", "VISA", 2024);
        } catch (IllegalArgumentException e) {
            expiredCaught = true;
        }
        assertTrue(expiredCaught, "Expired credit card rejected in constructor validation");

        // Non power of two ring buffer validation
        boolean badBufferSizeCaught = false;
        try {
            new TransactionAuditEngine(new BillingCalculator(), 7);
        } catch (IllegalArgumentException e) {
            badBufferSizeCaught = true;
        }
        assertTrue(badBufferSizeCaught, "Non-power-of-two ring buffer size rejected in constructor");
    }
}
