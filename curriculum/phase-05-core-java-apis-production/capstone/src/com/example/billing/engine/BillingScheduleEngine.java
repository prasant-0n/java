package com.example.billing.engine;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Localized Billing Schedule and Timezone Management Engine.
 * Synthesizes Concept P05-C001 (Java Time Architecture: Instant vs ZonedDateTime),
 * Concept P05-C002 (Time Zones, UTC Storage & Daylight Saving Hazards),
 * and Concept P05-C003 (Period vs Duration and Thread-Safe DateTimeFormatter).
 */
public final class BillingScheduleEngine {

    // Thread-safe immutable ISO DateTimeFormatter configured with ResolverStyle.STRICT
    public static final DateTimeFormatter BILLING_FORMATTER = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss z")
            .withResolverStyle(ResolverStyle.STRICT);

    private BillingScheduleEngine() {
        // Utility class
    }

    /**
     * Converts a local subscription billing time to UTC Instant for immutable database storage.
     * "Store UTC Everywhere at Rest" pattern.
     */
    public static Instant toUtcInstant(ZonedDateTime localTime) {
        Objects.requireNonNull(localTime, "localTime must not be null");
        return localTime.toInstant();
    }

    /**
     * Reconstructs local wall-clock billing time from UTC Instant and customer ZoneId.
     */
    public static ZonedDateTime toLocalBillingTime(Instant utcInstant, ZoneId customerZone) {
        Objects.requireNonNull(utcInstant, "utcInstant must not be null");
        Objects.requireNonNull(customerZone, "customerZone must not be null");
        return utcInstant.atZone(customerZone);
    }

    /**
     * Advances subscription billing by calendar periods (e.g., 1 day or 1 month).
     * CRITICAL DISTINCTION: Uses Period (not Duration) to preserve human wall-clock time
     * (e.g. 09:00 AM remains 09:00 AM even when crossing DST 23-hour or 25-hour day boundaries).
     */
    public static ZonedDateTime scheduleNextDailyBilling(ZonedDateTime currentBilling) {
        Objects.requireNonNull(currentBilling, "currentBilling must not be null");
        // Preserves 09:00 AM wall clock across DST Spring-Forward and Fall-Back transitions
        return currentBilling.plus(Period.ofDays(1));
    }

    /**
     * Computes the exact physical elapsed duration in seconds between two billing points.
     * Uses Duration (machine physical time).
     */
    public static Duration physicalElapsed(ZonedDateTime start, ZonedDateTime end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        return Duration.between(start, end);
    }

    /**
     * Calculates total calendar days elapsed between two dates.
     * Avoids the catastrophic period.getDays() component trap by using ChronoUnit.DAYS.between.
     */
    public static long totalDaysBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Formats a localized billing time into a standardized audit string.
     */
    public static String formatAudit(ZonedDateTime time) {
        Objects.requireNonNull(time, "time must not be null");
        return BILLING_FORMATTER.format(time);
    }
}
