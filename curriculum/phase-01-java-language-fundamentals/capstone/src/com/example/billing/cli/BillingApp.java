package com.example.billing.cli;

import com.example.billing.model.BillingTransaction;
import com.example.billing.model.Currency;
import com.example.billing.model.InvoiceSummary;
import com.example.billing.model.LineItem;
import com.example.billing.model.PaymentDetails;
import com.example.billing.service.BillingCalculator;
import com.example.billing.service.TransactionAuditEngine;

import java.time.Instant;
import java.util.List;

/**
 * Command-Line Interface demonstrating high-throughput settlement and auditing
 * using the Phase 01 High-Precision Billing Engine.
 */
public class BillingApp {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("      ENTERPRISE HIGH-PRECISION BILLING & AUDIT ENGINE (PHASE 01 CAPSTONE)      ");
        System.out.println("================================================================================\n");

        BillingCalculator calculator = new BillingCalculator();
        // Ring buffer capacity of 8 (power of two for bitwise wrapping)
        TransactionAuditEngine engine = new TransactionAuditEngine(calculator, 8);

        // 1. Transaction: US Retail Customer with VISA Card
        BillingTransaction tx1 = new BillingTransaction(
            "TX-1001",
            "CUST-US-001",
            Currency.USD,
            List.of(
                new LineItem("SKU-LAPTOP", "Developer Laptop 16GB", 129999L, 1, 10), // $1299.99 with 10% off
                new LineItem("SKU-MOUSE", "Ergonomic Optical Mouse", 4999L, 2, 0)     // 2x $49.99
            ),
            new PaymentDetails.CreditCard("************1234", "VISA", 2028),
            Instant.now()
        );

        // 2. Transaction: EU Enterprise Customer with SEPA Wire Transfer
        BillingTransaction tx2 = new BillingTransaction(
            "TX-1002",
            "CUST-EU-884",
            Currency.EUR,
            List.of(
                new LineItem("SKU-SRV-ENT", "Enterprise Cloud Instance (1 Year)", 450000L, 2, 15) // 2x €4500.00 with 15% off
            ),
            new PaymentDetails.WireTransfer("DE89370400440532013000", "DBEUS33XXXX", "INV-RECON-2026"),
            Instant.now()
        );

        // 3. Transaction: Web3 FinTech with Ethereum Crypto Asset
        BillingTransaction tx3 = new BillingTransaction(
            "TX-1003",
            "CUST-W3-512",
            Currency.USD,
            List.of(
                new LineItem("SKU-API-PRO", "High-Frequency Trading API Feed", 85000L, 1, 0) // $850.00
            ),
            new PaymentDetails.CryptoAsset("0x71C...B29", "ETHEREUM"),
            Instant.now()
        );

        // 4. Transaction: Corporate Cardholder with AMEX (3% Surcharge applies)
        BillingTransaction tx4 = new BillingTransaction(
            "TX-1004",
            "CUST-AMEX-99",
            Currency.USD,
            List.of(
                new LineItem("SKU-WKSTN", "GPU Deep Learning Rig", 500000L, 1, 5) // $5000.00 with 5% off
            ),
            new PaymentDetails.CreditCard("378282******005", "AMEX", 2027),
            Instant.now()
        );

        List<BillingTransaction> transactions = List.of(tx1, tx2, tx3, tx4);

        System.out.println("Processing Batch Invoices...\n");
        for (BillingTransaction tx : transactions) {
            InvoiceSummary summary = engine.processTransaction(tx);
            printInvoiceReceipt(tx, summary);
        }

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("                 HIGH-PERFORMANCE CSV SETTLEMENT AUDIT EXPORT                  ");
        System.out.println("--------------------------------------------------------------------------------");
        String csvReport = engine.generateCsvReport();
        System.out.print(csvReport);

        System.out.printf("\nBatch processing complete. Total transactions audited: %d\n", engine.getProcessedCount());
        System.out.println("================================================================================");
    }

    private static void printInvoiceReceipt(BillingTransaction tx, InvoiceSummary summary) {
        System.out.printf("Invoice ID:         %s\n", summary.transactionId());
        System.out.printf("Customer ID:        %s\n", summary.customerId());
        System.out.printf("Currency:           %s (%s)\n", summary.currency(), summary.currency().getSymbol());
        System.out.printf("Gross Subtotal:     %s%d.%02d\n", summary.currency().getSymbol(), summary.grossSubtotalCents() / 100, Math.abs(summary.grossSubtotalCents() % 100));
        System.out.printf("Discounts Deducted: -%s%d.%02d\n", summary.currency().getSymbol(), summary.discountDeductionCents() / 100, Math.abs(summary.discountDeductionCents() % 100));
        System.out.printf("VAT Tax (20%%):     +%s%d.%02d\n", summary.currency().getSymbol(), summary.taxAmountCents() / 100, Math.abs(summary.taxAmountCents() % 100));
        System.out.printf("Net Payable:        %s\n", summary.formatNetPayable());
        System.out.printf("Payment Gateway:    %s\n", summary.paymentRoutingGateway());
        System.out.printf("Internal Audit Log: %s\n", summary.auditLog());
        System.out.println("................................................................................");
    }
}
