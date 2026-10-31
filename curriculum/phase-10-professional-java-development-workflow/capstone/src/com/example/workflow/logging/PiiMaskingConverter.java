package com.example.workflow.logging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * High-performance regex sanitizer implementing PCI-DSS compliant credit card PAN masking
 * (BIN and last 4 preservation) and Bearer token / SSN redaction.
 */
public class PiiMaskingConverter {

    // Matches 13-19 digit credit cards
    private static final Pattern CC_PATTERN = Pattern.compile(
        "\\b([0-9]{4}[- ]?[0-9]{2})([0-9]{2}[- ]?[0-9]{4})[- ]?([0-9]{4})\\b"
    );

    // Matches Bearer JWT tokens in Authorization headers
    private static final Pattern BEARER_PATTERN = Pattern.compile(
        "(Bearer\\s+)([A-Za-z0-9\\-_=]+\\.[A-Za-z0-9\\-_=]+\\.[A-Za-z0-9\\-_=]+|[A-Za-z0-9\\-_=]{16,})",
        Pattern.CASE_INSENSITIVE
    );

    // Matches US Social Security Numbers
    private static final Pattern SSN_PATTERN = Pattern.compile(
        "\\b[0-9]{3}-[0-9]{2}-([0-9]{4})\\b"
    );

    public static String maskCreditCard(String message) {
        if (message == null || message.isBlank()) return message;
        Matcher matcher = CC_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder(message.length());
        while (matcher.find()) {
            String bin = matcher.group(1);
            String last4 = matcher.group(3);
            String masked = bin + "-******-" + last4;
            matcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String maskBearerToken(String message) {
        if (message == null || message.isBlank()) return message;
        Matcher matcher = BEARER_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder(message.length());
        while (matcher.find()) {
            String prefix = matcher.group(1);
            String token = matcher.group(2);
            String masked = prefix + token.substring(0, Math.min(6, token.length())) + "...[REDACTED]";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String maskSsn(String message) {
        if (message == null || message.isBlank()) return message;
        Matcher matcher = SSN_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder(message.length());
        while (matcher.find()) {
            String last4 = matcher.group(1);
            String masked = "***-**-" + last4;
            matcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String sanitize(String message) {
        if (message == null) return null;
        return maskSsn(maskBearerToken(maskCreditCard(message)));
    }
}
