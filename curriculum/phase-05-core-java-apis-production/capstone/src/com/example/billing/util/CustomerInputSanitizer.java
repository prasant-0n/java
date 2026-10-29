package com.example.billing.util;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Customer Input and Audit Sanitizer.
 * Synthesizes Concept P05-C010 (Regular Expressions, Named Groups, Lambda Replacer, ReDoS Prevention)
 * and Concept P05-C007 (Objects Bounds & Preconditions).
 */
public final class CustomerInputSanitizer {

    // Matches credit card numbers with optional dashes or spaces, capturing the last 4 digits
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile(
            "\\b(?<prefix>\\d{4})[- ]?(?<mid1>\\d{4})[- ]?(?<mid2>\\d{4})[- ]?(?<last4>\\d{4})\\b"
    );

    // Strict Promo Code Pattern: PREFIX(3)-PERCENT(2)-SALT(alphanumeric possessive to prevent ReDoS)
    // Example: "SUM-20-PROMO2026"
    private static final Pattern PROMO_CODE_PATTERN = Pattern.compile(
            "^(?<prefix>[A-Z]{3})-(?<percent>\\d{2})-(?<salt>[A-Za-z0-9]++)$"
    );

    private CustomerInputSanitizer() {
        // Utility class
    }

    /**
     * Masks credit card numbers in payloads for safe PCI-DSS compliant logging.
     * Uses Java 9 Matcher.replaceAll(Function<MatchResult, String>) with named groups.
     */
    public static String maskSensitiveFinancialData(String rawPayload) {
        if (rawPayload == null) {
            return null;
        }

        Matcher matcher = CREDIT_CARD_PATTERN.matcher(rawPayload);
        return matcher.replaceAll(mr -> "****-****-****-" + mr.group("last4"));
    }

    /**
     * Extracts discount percentage from promotional code using named capturing groups.
     * Uses possessive quantifier ([A-Za-z0-9]++) to guarantee zero-backtracking ReDoS immunity.
     */
    public static int extractPromoPercent(String promoCode) {
        Objects.requireNonNull(promoCode, "promoCode must not be null");

        // Length limit defense against ReDoS
        if (promoCode.length() > 64) {
            return 0;
        }

        Matcher matcher = PROMO_CODE_PATTERN.matcher(promoCode.strip());
        if (!matcher.matches()) {
            return 0;
        }

        return Integer.parseInt(matcher.group("percent"));
    }
}
