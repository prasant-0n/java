package com.example.billing.engine;

import com.example.billing.model.*;
import com.example.billing.security.IdGenerator;
import com.example.billing.util.CustomerInputSanitizer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Core Multi-Currency Tax and Discount Calculation Pipeline.
 * Synthesizes Concept P05-C004 (BigDecimal & Banker's Rounding),
 * Concept P05-C005 (Optional Monadic Methods),
 * Concept P05-C006 (UUIDv7 Id Generation),
 * and Concept P05-C007 (Objects Defensive Preconditions).
 */
public final class TaxAndDiscountCalculator {

    private TaxAndDiscountCalculator() {
        // Utility class
    }

    /**
     * Executes end-to-end invoice generation:
     * 1. Evaluates optional promo code via monadic Optional pipeline
     * 2. Calculates exact discount using Banker's Rounding
     * 3. Calculates localized regional tax
     * 4. Assembles immutable Invoice with UUIDv7 ID and UTC timestamp
     */
    public static Invoice generateInvoice(
            String customerId,
            Money subtotal,
            TaxRule taxRule,
            Optional<String> promoCodeOpt,
            Optional<String> taxExemptionCodeOpt,
            ZonedDateTime localBillingTime
    ) {
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(subtotal, "subtotal must not be null");
        Objects.requireNonNull(taxRule, "taxRule must not be null");
        Objects.requireNonNull(localBillingTime, "localBillingTime must not be null");

        // Concept P05-C005: Monadic Optional pipeline for promotional discount calculation
        // Returns Optional<Money> discount with zero intermediate null checks
        Optional<Money> discountOpt = promoCodeOpt
                .filter(code -> !code.isBlank())
                .map(String::strip)
                .map(CustomerInputSanitizer::extractPromoPercent)
                .filter(percent -> percent > 0 && percent <= 100)
                .map(percent -> {
                    BigDecimal factor = BigDecimal.valueOf(percent)
                            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_EVEN);
                    return subtotal.multiply(factor);
                });

        Money discount = discountOpt.orElseGet(() -> Money.zero(subtotal.currency()));
        Money discountedSubtotal = subtotal.subtract(discount);

        // Check tax exemption
        boolean isExempt = taxExemptionCodeOpt
                .map(taxRule::isExempt)
                .orElse(false);

        Money tax;
        if (isExempt) {
            tax = Money.zero(subtotal.currency());
        } else {
            // Tax = discountedSubtotal * (ratePercent / 100)
            BigDecimal taxRateFactor = taxRule.ratePercent()
                    .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_EVEN);
            tax = discountedSubtotal.multiply(taxRateFactor);
        }

        Money total = discountedSubtotal.add(tax);

        // Concept P05-C006: Generate K-sortable UUIDv7
        UUID invoiceId = IdGenerator.generateUuidV7();
        Instant createdAtUtc = BillingScheduleEngine.toUtcInstant(localBillingTime);

        return new Invoice(
                invoiceId,
                createdAtUtc,
                localBillingTime,
                customerId,
                subtotal,
                discount,
                tax,
                total,
                promoCodeOpt
        );
    }
}
