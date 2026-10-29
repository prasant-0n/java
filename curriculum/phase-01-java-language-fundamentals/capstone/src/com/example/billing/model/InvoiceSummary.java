package com.example.billing.model;

import java.util.Objects;

/**
 * Immutable audited billing settlement summary.
 */
public record InvoiceSummary(
    String transactionId,
    String customerId,
    Currency currency,
    long grossSubtotalCents,
    long discountDeductionCents,
    long taxAmountCents,
    long netPayableCents,
    String paymentRoutingGateway,
    String auditLog
) {
    public InvoiceSummary {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(paymentRoutingGateway, "paymentRoutingGateway must not be null");
        Objects.requireNonNull(auditLog, "auditLog must not be null");
    }

    public String formatNetPayable() {
        long whole = netPayableCents / 100;
        long cents = Math.abs(netPayableCents % 100);
        return String.format("%s%d.%02d", currency.getSymbol(), whole, cents);
    }
}
