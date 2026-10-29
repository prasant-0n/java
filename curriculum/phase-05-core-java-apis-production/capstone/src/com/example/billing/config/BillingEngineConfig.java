package com.example.billing.config;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

/**
 * Twelve-Factor App Configuration Provider.
 * Synthesizes Concept P05-C009 (System Properties & Environment Variables).
 * Precedence: System Property (-D) > Environment Variable > Hardcoded Default.
 */
public final class BillingEngineConfig {

    private static final Set<String> SENSITIVE_KEYWORDS = Set.of(
            "SECRET", "KEY", "TOKEN", "PASSWORD", "PRIVATE"
    );

    private final String defaultCurrency;
    private final BigDecimal defaultTaxRate;
    private final Duration gatewayTimeout;
    private final int maxRetries;
    private final String apiSecret;

    public BillingEngineConfig() {
        this.defaultCurrency = get("billing.currency.default", "USD");
        this.defaultTaxRate = new BigDecimal(get("billing.tax.default-rate", "0.0825"));
        this.gatewayTimeout = Duration.parse(get("billing.gateway.timeout", "PT5S"));
        this.maxRetries = Integer.parseInt(get("billing.gateway.max-retries", "3"));
        this.apiSecret = get("billing.api.secret", "default-insecure-dev-secret-key");
    }

    public static String get(String dotKey, String defaultValue) {
        Objects.requireNonNull(dotKey, "dotKey must not be null");

        // 1. System Property
        String sysProp = System.getProperty(dotKey);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp.strip();
        }

        // 2. Normalized Environment Variable
        String envKey = dotKey.replace('.', '_').replace('-', '_').toUpperCase(Locale.ROOT);
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isBlank()) {
            return envVal.strip();
        }

        // 3. Fallback Default
        return defaultValue;
    }

    public String defaultCurrency() {
        return defaultCurrency;
    }

    public BigDecimal defaultTaxRate() {
        return defaultTaxRate;
    }

    public Duration gatewayTimeout() {
        return gatewayTimeout;
    }

    public int maxRetries() {
        return maxRetries;
    }

    public String apiSecret() {
        return apiSecret;
    }

    public Map<String, String> exportSanitizedAudit() {
        Map<String, String> map = new TreeMap<>();
        map.put("billing.currency.default", defaultCurrency);
        map.put("billing.tax.default-rate", defaultTaxRate.toPlainString());
        map.put("billing.gateway.timeout", gatewayTimeout.toString());
        map.put("billing.gateway.max-retries", String.valueOf(maxRetries));
        map.put("billing.api.secret", mask(apiSecret));
        return Collections.unmodifiableMap(map);
    }

    private static String mask(String secret) {
        if (secret == null || secret.length() < 8) {
            return "******** [REDACTED]";
        }
        return secret.substring(0, 4) + "..." + secret.substring(secret.length() - 4) + " [REDACTED]";
    }
}
