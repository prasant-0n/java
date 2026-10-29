package com.example.billing.test;

import com.example.billing.config.BillingEngineConfig;
import com.example.billing.engine.BillingScheduleEngine;
import com.example.billing.engine.TaxAndDiscountCalculator;
import com.example.billing.model.*;
import com.example.billing.model.Currency;
import com.example.billing.security.IdGenerator;
import com.example.billing.security.SecurityTokenGenerator;
import com.example.billing.util.CustomerInputSanitizer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Phase 05 Capstone Full Integration Test Suite.
 * Synthesizes and comprehensively verifies all 10 concepts on OpenJDK 21 LTS.
 */
public class BillingEngineIntegrationTest {

    public static void main(String[] args) {
        System.out.println("===============================================================================");
        System.out.println("Executing Phase 05 Capstone Integration Test: Global Billing & Invoicing Engine");
        System.out.println("===============================================================================");

        testUuidV7MonotonicityAndBinaryPacking();
        testSecurityTokenCsprngAndThreadLocalJitter();
        testBigDecimalBankersRoundingAndScale();
        testJavaTimeUtcEverywhereAndDstWallClockPreservation();
        testOptionalMonadicPromoCalculation();
        testObjectsPreconditionsAndDeepEquality();
        testTwelveFactorConfigHierarchyAndSanitization();
        testRegexNamedGroupsAndSensitiveCardMasking();
        testReDoSPreventionPossessiveQuantifier();
        testEndToEndMultiCurrencyInvoicePipeline();

        System.out.println("===============================================================================");
        System.out.println("ALL 10 PHASE 05 CAPSTONE INTEGRATION TESTS PASSED WITH 100% SUCCESS!");
        System.out.println("===============================================================================");
    }

    /**
     * Synthesizes Concept P05-C006: UUID Generation, UUIDv7 structure, K-sortability, and 16-byte packing.
     */
    static void testUuidV7MonotonicityAndBinaryPacking() {
        System.out.print("[Test 1/10] Verifying UUIDv7 K-Sortability & 16-Byte Binary Packing... ");
        long t1 = System.currentTimeMillis();
        UUID id1 = IdGenerator.generateUuidV7(t1);
        UUID id2 = IdGenerator.generateUuidV7(t1 + 25);

        assert id1.version() == 7 : "UUID version must be 7";
        assert id1.variant() == 2 : "UUID variant must be RFC 4122/9562 (2)";
        assert id1.compareTo(id2) < 0 : "UUIDv7 must be strictly monotonically increasing";
        assert IdGenerator.extractTimestampMillis(id1) == t1 : "Extracted timestamp must match input";

        // Test 16-byte binary roundtrip packing
        byte[] packed = IdGenerator.toBytes(id1);
        assert packed.length == 16 : "Packed UUID must be exactly 16 bytes";
        UUID unpacked = IdGenerator.fromBytes(packed);
        assert unpacked.equals(id1) : "Unpacked UUID must match original";

        System.out.println("PASSED");
    }

    /**
     * Synthesizes Concept P05-C008: SecureRandom CSPRNG vs ThreadLocalRandom Full Jitter.
     */
    static void testSecurityTokenCsprngAndThreadLocalJitter() {
        System.out.print("[Test 2/10] Verifying SecureRandom CSPRNG & ThreadLocalRandom Jitter... ");
        String idempotencyKey1 = SecurityTokenGenerator.generateIdempotencyKey(32);
        String idempotencyKey2 = SecurityTokenGenerator.generateIdempotencyKey(32);

        assert idempotencyKey1 != null && !idempotencyKey1.isBlank();
        assert !idempotencyKey1.equals(idempotencyKey2) : "CSPRNG tokens must be cryptographically unique";

        String otp = SecurityTokenGenerator.generateConfirmationOtp();
        assert otp.length() == 6 : "OTP must be 6 digits";
        assert Integer.parseInt(otp) >= 0 && Integer.parseInt(otp) < 1_000_000;

        // Verify Full Jitter exponential backoff boundaries
        long baseDelay = 100;
        long maxDelay = 2000;
        for (int attempt = 0; attempt < 10; attempt++) {
            long jitter = SecurityTokenGenerator.calculateFullJitterDelayMs(attempt, baseDelay, maxDelay);
            assert jitter >= 0 && jitter <= maxDelay : "Jitter delay out of bounds: " + jitter;
        }

        System.out.println("PASSED");
    }

    /**
     * Synthesizes Concept P05-C004: BigDecimal financial precision, scale, and Banker's Rounding (HALF_EVEN).
     */
    static void testBigDecimalBankersRoundingAndScale() {
        System.out.print("[Test 3/10] Verifying BigDecimal Financial Math & Banker's Rounding... ");
        // Prove IEEE 754 float error vs BigDecimal exactness
        assert 0.1 + 0.2 != 0.3 : "IEEE 754 floating point arithmetic must have binary precision errors";
        BigDecimal d1 = new BigDecimal("0.1");
        BigDecimal d2 = new BigDecimal("0.2");
        assert d1.add(d2).compareTo(new BigDecimal("0.3")) == 0;

        // Verify Banker's Rounding (RoundingMode.HALF_EVEN)
        // 2.5 rounds to 2 (even), 3.5 rounds to 4 (even)
        BigDecimal twoHalf = new BigDecimal("2.5").setScale(0, RoundingMode.HALF_EVEN);
        BigDecimal threeHalf = new BigDecimal("3.5").setScale(0, RoundingMode.HALF_EVEN);
        assert twoHalf.intValueExact() == 2 : "2.5 must round to 2 under HALF_EVEN";
        assert threeHalf.intValueExact() == 4 : "3.5 must round to 4 under HALF_EVEN";

        // Verify Money record scale normalization (USD scale 2, JPY scale 0)
        Money usd = Money.of("100.555", Currency.USD); // Rounds 100.555 to 100.56
        assert usd.amount().scale() == 2;
        assert usd.amount().toPlainString().equals("100.56");

        Money jpy = Money.of("100.5", Currency.JPY); // Rounds 100.5 to 100 (HALF_EVEN)
        assert jpy.amount().scale() == 0;
        assert jpy.amount().toPlainString().equals("100");

        // Verify non-terminating division safe handling
        Money divided = Money.of("100.00", Currency.USD).divide(new BigDecimal("3"));
        assert divided.amount().toPlainString().equals("33.33");

        System.out.println("PASSED");
    }

    /**
     * Synthesizes Concepts P05-C001, P05-C002, P05-C003: Java Time, UTC storage, and DST wall-clock preservation.
     */
    static void testJavaTimeUtcEverywhereAndDstWallClockPreservation() {
        System.out.print("[Test 4/10] Verifying Java Time UTC Storage & DST Wall-Clock Preservation... ");
        ZoneId nyZone = ZoneId.of("America/New_York");

        // Set up local billing time on eve of Spring-Forward DST transition (March 9, 2024 at 09:00 AM)
        ZonedDateTime eveOfDst = ZonedDateTime.of(2024, 3, 9, 9, 0, 0, 0, nyZone);
        Instant utcStorage = BillingScheduleEngine.toUtcInstant(eveOfDst);

        // Advance 1 calendar day using Period to preserve 09:00 AM wall-clock
        ZonedDateTime nextDayPeriod = BillingScheduleEngine.scheduleNextDailyBilling(eveOfDst);
        assert nextDayPeriod.getHour() == 9 : "Wall-clock hour must remain 09:00 AM after Spring-Forward";
        assert nextDayPeriod.getDayOfMonth() == 10;
        assert nextDayPeriod.getZone().equals(nyZone);

        // Physical elapsed time across Spring Forward is exactly 23 hours (lost an hour at 2:00 AM)
        Duration physicalTime = BillingScheduleEngine.physicalElapsed(eveOfDst, nextDayPeriod);
        assert physicalTime.toHours() == 23 : "Physical duration across Spring-Forward must be 23 hours";

        // Total calendar days elapsed via ChronoUnit
        long days = BillingScheduleEngine.totalDaysBetween(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 10));
        assert days == 40 : "ChronoUnit.DAYS.between must return 40 total days";

        // Thread-safe formatter audit
        String formatted = BillingScheduleEngine.formatAudit(nextDayPeriod);
        assert formatted.contains("2024-03-10 09:00:00");

        System.out.println("PASSED");
    }

    /**
     * Synthesizes Concept P05-C005: Optional monadic methods (map, filter, orElseGet).
     */
    static void testOptionalMonadicPromoCalculation() {
        System.out.print("[Test 5/10] Verifying Optional Monadic Transformation Pipeline... ");
        TaxRule dummyTax = new TaxRule("NY", ZoneId.of("America/New_York"), new BigDecimal("8.00"), Pattern.compile("^EX-.*$"));
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));

        // 1. Valid promo code ("SUM-20-PROMO2026" gives 20% discount)
        Invoice inv1 = TaxAndDiscountCalculator.generateInvoice(
                "CUST-001",
                Money.of("100.00", Currency.USD),
                dummyTax,
                Optional.of("SUM-20-PROMO2026"),
                Optional.empty(),
                now
        );
        assert inv1.discount().amount().compareTo(new BigDecimal("20.00")) == 0;
        assert inv1.appliedPromoCode().isPresent();

        // 2. Empty promo code
        Invoice inv2 = TaxAndDiscountCalculator.generateInvoice(
                "CUST-002",
                Money.of("100.00", Currency.USD),
                dummyTax,
                Optional.empty(),
                Optional.empty(),
                now
        );
        assert inv2.discount().amount().compareTo(BigDecimal.ZERO) == 0;
        assert inv2.appliedPromoCode().isEmpty();

        // 3. Invalid/blank promo code filtered out
        Invoice inv3 = TaxAndDiscountCalculator.generateInvoice(
                "CUST-003",
                Money.of("100.00", Currency.USD),
                dummyTax,
                Optional.of("   "),
                Optional.empty(),
                now
        );
        assert inv3.discount().amount().compareTo(BigDecimal.ZERO) == 0;

        System.out.println("PASSED");
    }

    /**
     * Synthesizes Concept P05-C007: Objects utility class, requireNonNull, and deep equality.
     */
    static void testObjectsPreconditionsAndDeepEquality() {
        System.out.print("[Test 6/10] Verifying Objects Null Defense & Deep Equality... ");
        // Objects.requireNonNull with lazy supplier
        boolean[] supplierCalled = {false};
        String val = Objects.requireNonNull("Initialized", () -> {
            supplierCalled[0] = true;
            return "Should not execute";
        });
        assert val.equals("Initialized");
        assert !supplierCalled[0] : "Lazy supplier must not be called on non-null";

        // Objects.requireNonNullElse
        String resolved = Objects.requireNonNullElse(null, "FallbackDefault");
        assert resolved.equals("FallbackDefault");

        // Objects.deepEquals on byte arrays
        byte[] b1 = {1, 2, 3};
        byte[] b2 = {1, 2, 3};
        assert !Objects.equals(b1, b2) : "Objects.equals on arrays is shallow reference check";
        assert Objects.deepEquals(b1, b2) : "Objects.deepEquals must perform element-wise array check";

        System.out.println("PASSED");
    }

    /**
     * Synthesizes Concept P05-C009: Twelve-Factor Config Precedence and Sensitive Key Masking.
     */
    static void testTwelveFactorConfigHierarchyAndSanitization() {
        System.out.print("[Test 7/10] Verifying 12-Factor Configuration & Secret Masking... ");
        String testProp = "billing.test.override-key";
        System.setProperty(testProp, "ActiveSystemOverride");
        try {
            assert BillingEngineConfig.get(testProp, "Default").equals("ActiveSystemOverride");
        } finally {
            System.clearProperty(testProp);
        }

        BillingEngineConfig config = new BillingEngineConfig();
        assert config.defaultCurrency().equals("USD");
        assert config.gatewayTimeout().toSeconds() == 5;

        Map<String, String> audit = config.exportSanitizedAudit();
        assert audit.get("billing.api.secret").contains("[REDACTED]") : "Secret must be masked in audit export";

        System.out.println("PASSED");
    }

    /**
     * Synthesizes Concept P05-C010: Regex Named Groups & Sensitive Card Masking.
     */
    static void testRegexNamedGroupsAndSensitiveCardMasking() {
        System.out.print("[Test 8/10] Verifying Regex Named Capturing Groups & PAN Masking... ");
        String logMsg = "Customer processed card: 4111-2222-3333-8888 for order.";
        String masked = CustomerInputSanitizer.maskSensitiveFinancialData(logMsg);

        assert masked.contains("****-****-****-8888");
        assert !masked.contains("4111-2222");

        // Named capturing group extraction
        int percent = CustomerInputSanitizer.extractPromoPercent("VIP-25-WINTER2026");
        assert percent == 25 : "Extracted percent must be 25";

        int invalidPercent = CustomerInputSanitizer.extractPromoPercent("INVALID-PROMO");
        assert invalidPercent == 0;

        System.out.println("PASSED");
    }

    /**
     * Synthesizes Concept P05-C010: ReDoS Prevention via Possessive Quantifier.
     */
    static void testReDoSPreventionPossessiveQuantifier() {
        System.out.print("[Test 9/10] Verifying ReDoS Prevention via Possessive Quantifiers... ");
        // Evil payload with 50 characters of repeating alphanumeric characters that would backtrack in greedy regex
        String evilPayload = "VIP-25-" + "A".repeat(50) + "!"; // Trailing '!' causes match failure

        long start = System.nanoTime();
        int percent = CustomerInputSanitizer.extractPromoPercent(evilPayload);
        long elapsedNs = System.nanoTime() - start;

        assert percent == 0 : "Must fail match due to invalid characters";
        assert elapsedNs < 2_000_000 : "Possessive quantifier must reject evil payload in < 2ms without backtracking";

        System.out.println("PASSED (" + (elapsedNs / 1000) + " µs)");
    }

    /**
     * Full Multi-Currency Pipeline Integration Test across USD, EUR, and JPY.
     */
    static void testEndToEndMultiCurrencyInvoicePipeline() {
        System.out.print("[Test 10/10] Verifying End-to-End Multi-Currency Invoice Pipeline... ");
        ZoneId nyZone = ZoneId.of("America/New_York");
        ZoneId tokyoZone = ZoneId.of("Asia/Tokyo");

        TaxRule usTax = new TaxRule("US-NY", nyZone, new BigDecimal("8.875"), Pattern.compile("^US-EXEMPT-\\d+$"));
        TaxRule jpTax = new TaxRule("JP-TOKYO", tokyoZone, new BigDecimal("10.00"), Pattern.compile("^JP-EXEMPT-\\d+$"));

        ZonedDateTime billingTime = ZonedDateTime.of(2026, 10, 30, 9, 0, 0, 0, nyZone);

        // 1. USD Invoice: $1000 Subtotal, 15% Discount, 8.875% Tax
        // Subtotal: 1000.00
        // Discount: 150.00
        // Discounted Subtotal: 850.00
        // Tax: 850.00 * 0.08875 = 75.4375 -> Rounds to 75.44
        // Total: 850.00 + 75.44 = 925.44
        Invoice usdInvoice = TaxAndDiscountCalculator.generateInvoice(
                "CUST-USA-99",
                Money.of("1000.00", Currency.USD),
                usTax,
                Optional.of("AUT-15-FLASH2026"),
                Optional.empty(),
                billingTime
        );

        assert usdInvoice.subtotal().amount().toPlainString().equals("1000.00");
        assert usdInvoice.discount().amount().toPlainString().equals("150.00");
        assert usdInvoice.tax().amount().toPlainString().equals("75.44");
        assert usdInvoice.total().amount().toPlainString().equals("925.44");

        // 2. JPY Invoice: ¥50000 Subtotal (Scale 0), 10% Tax, Tax Exempt Code applied!
        Invoice jpyInvoice = TaxAndDiscountCalculator.generateInvoice(
                "CUST-JPN-42",
                Money.of("50000", Currency.JPY),
                jpTax,
                Optional.empty(),
                Optional.of("JP-EXEMPT-9876"),
                ZonedDateTime.now(tokyoZone)
        );

        assert jpyInvoice.subtotal().amount().toPlainString().equals("50000");
        assert jpyInvoice.tax().amount().toPlainString().equals("0"); // Exempt!
        assert jpyInvoice.total().amount().toPlainString().equals("50000");

        System.out.println("PASSED");
    }
}
