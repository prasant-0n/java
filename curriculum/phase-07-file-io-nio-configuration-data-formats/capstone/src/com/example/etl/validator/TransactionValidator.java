package com.example.etl.validator;

import com.example.etl.config.EtlBatchConfig;
import com.example.etl.model.TransactionRecord;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Validates raw CSV records against strict financial business rules.
 */
public final class TransactionValidator {

    private final EtlBatchConfig config;

    public TransactionValidator(EtlBatchConfig config) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
    }

    /**
     * Parses and validates a raw CSV row.
     * Expected columns:
     * 0: transaction_id (UUID)
     * 1: customer_id (Non-empty String)
     * 2: amount (Positive BigDecimal)
     * 3: currency (Allowed ISO code, e.g. USD)
     * 4: timestamp (ISO-8601 Instant)
     * 5: category (String)
     * 6: description (String)
     */
    public TransactionRecord validate(long lineNumber, List<String> fields) {
        if (fields.size() < 7) {
            throw new IllegalArgumentException(String.format(
                "Malformed row: expected at least 7 columns, found %d", fields.size()));
        }

        // 1. Transaction ID
        String rawId = fields.get(0).trim();
        UUID transactionId;
        try {
            transactionId = UUID.fromString(rawId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid transaction UUID: '" + rawId + "'", e);
        }

        // 2. Customer ID
        String customerId = fields.get(1).trim();
        if (customerId.isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be blank");
        }

        // 3. Amount
        String rawAmount = fields.get(2).trim();
        BigDecimal amount;
        try {
            amount = new BigDecimal(rawAmount);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be strictly positive: " + amount);
            }
            if (amount.scale() > 4) {
                throw new IllegalArgumentException("Amount exceeds maximum scale of 4 decimal places: " + amount);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric amount: '" + rawAmount + "'", e);
        }

        // 4. Currency
        String currency = fields.get(3).trim().toUpperCase();
        if (!config.allowedCurrencies().contains(currency)) {
            throw new IllegalArgumentException("Unsupported currency: '" + currency + 
                "'. Allowed: " + config.allowedCurrencies());
        }

        // 5. Timestamp
        String rawTimestamp = fields.get(4).trim();
        Instant timestamp;
        try {
            timestamp = Instant.parse(rawTimestamp);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid ISO-8601 timestamp: '" + rawTimestamp + "'", e);
        }

        // 6. Category
        String category = fields.get(5).trim();
        if (category.isEmpty()) {
            category = "GENERAL";
        }

        // 7. Description & Formula Sanitization
        String description = fields.get(6);
        if (config.sanitizeSpreadsheetFormulas()) {
            description = sanitizeFormula(description);
        }

        return new TransactionRecord(
            transactionId,
            customerId,
            amount,
            currency,
            timestamp,
            category,
            description
        );
    }

    private static String sanitizeFormula(String val) {
        if (val == null || val.isEmpty()) {
            return val;
        }
        char first = val.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
            return "'" + val;
        }
        return val;
    }
}
