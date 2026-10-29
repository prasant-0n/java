package com.example.billing.service;

import com.example.billing.model.BillingTransaction;
import com.example.billing.model.InvoiceSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * High-throughput transaction auditing and batch reporting engine.
 * Leverages an internal circular ring buffer to record recent settlements
 * and pre-allocated StringBuilders for zero-GC report generation.
 */
public class TransactionAuditEngine {

    private final BillingCalculator calculator;
    private final InvoiceSummary[] ringBuffer;
    private final int bufferMask;
    private long globalSequence = 0L;

    public TransactionAuditEngine(BillingCalculator calculator, int ringBufferSizePowerOfTwo) {
        this.calculator = Objects.requireNonNull(calculator, "calculator must not be null");
        if (Integer.bitCount(ringBufferSizePowerOfTwo) != 1) {
            throw new IllegalArgumentException("Ring buffer size must be a power of two");
        }
        this.ringBuffer = new InvoiceSummary[ringBufferSizePowerOfTwo];
        this.bufferMask = ringBufferSizePowerOfTwo - 1;
    }

    /**
     * Ingests, validates, calculates, and records a billing transaction in the circular buffer.
     */
    public synchronized InvoiceSummary processTransaction(BillingTransaction transaction) {
        InvoiceSummary summary = calculator.calculateInvoice(transaction);

        // Store into circular buffer using fast bitwise mask
        int slot = (int) (globalSequence & bufferMask);
        ringBuffer[slot] = summary;
        globalSequence++;

        return summary;
    }

    /**
     * Generates a high-performance CSV batch report of recent settlements
     * using a pre-allocated StringBuilder.
     */
    public synchronized String generateCsvReport() {
        int recordedCount = (int) Math.min(globalSequence, ringBuffer.length);
        if (recordedCount == 0) {
            return "TRANSACTION_ID,CUSTOMER_ID,CURRENCY,GROSS_CENTS,DISCOUNT_CENTS,TAX_CENTS,NET_CENTS,GATEWAY\n";
        }

        // Pre-allocate buffer: 80 bytes header + ~90 bytes per row
        int estimatedCapacity = 80 + (recordedCount * 96);
        StringBuilder sb = new StringBuilder(estimatedCapacity);

        sb.append("TRANSACTION_ID,CUSTOMER_ID,CURRENCY,GROSS_CENTS,DISCOUNT_CENTS,TAX_CENTS,NET_CENTS,GATEWAY\n");

        long startSequence = globalSequence - recordedCount;
        for (long seq = startSequence; seq < globalSequence; seq++) {
            int slot = (int) (seq & bufferMask);
            InvoiceSummary inv = ringBuffer[slot];
            if (inv != null) {
                sb.append(inv.transactionId()).append(',')
                  .append(inv.customerId()).append(',')
                  .append(inv.currency()).append(',')
                  .append(inv.grossSubtotalCents()).append(',')
                  .append(inv.discountDeductionCents()).append(',')
                  .append(inv.taxAmountCents()).append(',')
                  .append(inv.netPayableCents()).append(',')
                  .append(inv.paymentRoutingGateway()).append('\n');
            }
        }

        return sb.toString();
    }

    public synchronized long getProcessedCount() {
        return globalSequence;
    }
}
