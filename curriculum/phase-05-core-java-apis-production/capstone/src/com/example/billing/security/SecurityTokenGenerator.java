package com.example.billing.security;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Security Token and Resilient Retry Provider.
 * Synthesizes Concept P05-C008 (Random vs SecureRandom, CSPRNG vs ThreadLocalRandom).
 */
public final class SecurityTokenGenerator {

    private static final SecureRandom CSPRNG = new SecureRandom();
    private static final Base64.Encoder B64_URL = Base64.getUrlEncoder().withoutPadding();

    private SecurityTokenGenerator() {
        // Utility class
    }

    /**
     * Generates a cryptographically strong, URL-safe authentication/idempotency token.
     * Uses CSPRNG harvesting OS entropy.
     */
    public static String generateIdempotencyKey(int byteEntropy) {
        if (byteEntropy < 16) {
            throw new IllegalArgumentException("Entropy must be at least 16 bytes (128 bits)");
        }
        byte[] buffer = new byte[byteEntropy];
        CSPRNG.nextBytes(buffer);
        return B64_URL.encodeToString(buffer);
    }

    /**
     * Generates a 6-digit confirmation OTP using rejection sampling to eliminate modulo bias.
     */
    public static String generateConfirmationOtp() {
        int otp = CSPRNG.nextInt(1_000_000);
        return String.format("%06d", otp);
    }

    /**
     * Calculates Full Jitter backoff delay for payment gateway retries.
     * Uses ThreadLocalRandom for lock-free, zero-contention high-throughput performance.
     * Formula: t = random(0, min(maxDelay, baseDelay * 2^attempt))
     */
    public static long calculateFullJitterDelayMs(int attempt, long baseDelayMs, long maxDelayMs) {
        if (attempt < 0) {
            throw new IllegalArgumentException("Attempt must be non-negative");
        }
        long ceiling = baseDelayMs * (1L << Math.min(attempt, 20));
        long boundedCeiling = Math.min(ceiling, maxDelayMs);
        return ThreadLocalRandom.current().nextLong(0, boundedCeiling + 1);
    }
}
