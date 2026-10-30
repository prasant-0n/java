package com.example.etl.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Externalized configuration for the streaming ETL batch processor.
 * Resolves settings via System Properties -> Environment Variables -> Defaults.
 */
public record EtlBatchConfig(
    int bufferSizeBytes,
    int maxRecordLengthChars,
    Set<String> allowedCurrencies,
    boolean sanitizeSpreadsheetFormulas
) {
    public static EtlBatchConfig loadDefault() {
        Properties defaults = new Properties();
        defaults.setProperty("buffer.size.bytes", "65536"); // 64KB buffer
        defaults.setProperty("max.record.length.chars", "16384"); // 16KB max line/record
        defaults.setProperty("allowed.currencies", "USD,EUR,GBP,CAD,JPY");
        defaults.setProperty("sanitize.formulas", "true");

        // System property overrides
        int bufferSize = Integer.parseInt(
            System.getProperty("etl.buffer.size", defaults.getProperty("buffer.size.bytes"))
        );
        int maxRecordLength = Integer.parseInt(
            System.getProperty("etl.max.record.length", defaults.getProperty("max.record.length.chars"))
        );
        String rawCurrencies = System.getProperty(
            "etl.allowed.currencies", defaults.getProperty("allowed.currencies")
        );
        boolean sanitize = Boolean.parseBoolean(
            System.getProperty("etl.sanitize.formulas", defaults.getProperty("sanitize.formulas"))
        );

        Set<String> currencies = new HashSet<>();
        for (String c : rawCurrencies.split(",")) {
            currencies.add(c.trim().toUpperCase(Locale.ROOT));
        }

        return new EtlBatchConfig(
            bufferSize,
            maxRecordLength,
            Collections.unmodifiableSet(currencies),
            sanitize
        );
    }
}
