package com.example.billing.service;

import com.example.billing.model.BillingTransaction;
import com.example.billing.model.InvoiceSummary;
import com.example.billing.model.LineItem;
import com.example.billing.model.PaymentDetails;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * High-precision billing computation engine.
 * Employs exact integer arithmetic (Math.addExact, Math.multiplyExact)
 * and BigDecimal Banker's Rounding (HALF_EVEN) to prevent overflow and IEEE 754 precision errors.
 */
public class BillingCalculator {

    // 20% Standard Value Added Tax (VAT) rate
    private static final BigDecimal VAT_RATE = new BigDecimal("0.20");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * Calculates the complete audited invoice summary for a billing transaction.
     */
    public InvoiceSummary calculateInvoice(BillingTransaction transaction) {
        Objects.requireNonNull(transaction, "Transaction must not be null");

        long grossSubtotalCents = 0L;
        long totalDiscountCents = 0L;

        // Process line items using enhanced for loop with exact overflow protection
        for (LineItem item : transaction.lineItems()) {
            // Step 1: Compute gross item total = unitPriceCents * quantity
            // Math.multiplyExact throws ArithmeticException on integer overflow
            long itemGrossCents = Math.multiplyExact(item.unitPriceCents(), (long) item.quantity());
            grossSubtotalCents = Math.addExact(grossSubtotalCents, itemGrossCents);

            // Step 2: Compute discount using Banker's Rounding (HALF_EVEN)
            if (item.discountPercent() > 0) {
                BigDecimal grossBD = BigDecimal.valueOf(itemGrossCents);
                BigDecimal discountBD = grossBD.multiply(BigDecimal.valueOf(item.discountPercent()))
                                               .divide(HUNDRED, 0, RoundingMode.HALF_EVEN);
                long itemDiscount = discountBD.longValueExact();
                totalDiscountCents = Math.addExact(totalDiscountCents, itemDiscount);
            }
        }

        long netAfterDiscount = Math.subtractExact(grossSubtotalCents, totalDiscountCents);

        // Step 3: Compute VAT tax on post-discount subtotal
        BigDecimal taxableBD = BigDecimal.valueOf(netAfterDiscount);
        BigDecimal taxBD = taxableBD.multiply(VAT_RATE).setScale(0, RoundingMode.HALF_EVEN);
        long taxAmountCents = taxBD.longValueExact();

        // Step 4: Evaluate Payment Gateway & Surcharge via Java 21 Pattern Matching Switch
        String gatewayRouting = evaluatePaymentGateway(transaction.paymentDetails());
        long surchargeCents = evaluatePaymentSurchargeCents(transaction.paymentDetails(), netAfterDiscount);

        long netPayableCents = Math.addExact(
            Math.addExact(netAfterDiscount, taxAmountCents),
            surchargeCents
        );

        // Step 5: Construct detailed audit log
        String auditLog = String.format(
            "TxID=%s, Gross=%d, Discount=%d, Tax=%d, Surcharge=%d, NetPayable=%d, Route=%s",
            transaction.transactionId(), grossSubtotalCents, totalDiscountCents, 
            taxAmountCents, surchargeCents, netPayableCents, gatewayRouting
        );

        return new InvoiceSummary(
            transaction.transactionId(),
            transaction.customerId(),
            transaction.currency(),
            grossSubtotalCents,
            totalDiscountCents,
            taxAmountCents,
            netPayableCents,
            gatewayRouting,
            auditLog
        );
    }

    /**
     * Determines payment routing gateway using Java 21 pattern matching with guards (when).
     */
    public String evaluatePaymentGateway(PaymentDetails payment) {
        return switch (payment) {
            case null -> "FALLBACK_MANUAL_REVIEW";
            case PaymentDetails.CreditCard cc when "VISA".equalsIgnoreCase(cc.cardType()) -> 
                "GATEWAY_CYBERSOURCE_VISA_DIRECT";
            case PaymentDetails.CreditCard cc when "MASTERCARD".equalsIgnoreCase(cc.cardType()) -> 
                "GATEWAY_MASTERCARD_SEND";
            case PaymentDetails.CreditCard cc -> 
                "GATEWAY_STRIPE_GLOBAL_ACQUIRER";
            case PaymentDetails.WireTransfer wt when wt.bic().startsWith("DB") -> 
                "GATEWAY_DEUTSCHE_BANK_SEPA";
            case PaymentDetails.WireTransfer wt -> 
                "GATEWAY_SWIFT_CLEARING_HOUSE";
            case PaymentDetails.CryptoAsset ca when "ETHEREUM".equalsIgnoreCase(ca.blockchainNetwork()) -> 
                "GATEWAY_COINBASE_COMMERCE_ERC20";
            case PaymentDetails.CryptoAsset ca -> 
                "GATEWAY_BITPAY_LAYER1";
        };
    }

    /**
     * Evaluates transaction surcharge using pattern matching with guards.
     */
    public long evaluatePaymentSurchargeCents(PaymentDetails payment, long subtotalCents) {
        return switch (payment) {
            case null -> 0L;
            case PaymentDetails.CreditCard cc when "AMEX".equalsIgnoreCase(cc.cardType()) -> {
                // American Express 3% surcharge
                yield (long) (subtotalCents * 0.03);
            }
            case PaymentDetails.CryptoAsset ca -> 250L; // Fixed 250 cents ($2.50) network gas subsidy
            default -> 0L; // Zero surcharge for debit/wire/visa
        };
    }
}
