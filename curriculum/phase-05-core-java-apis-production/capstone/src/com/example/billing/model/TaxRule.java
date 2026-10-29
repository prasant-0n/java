package com.example.billing.model;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Regional Tax Rule Configuration.
 * Synthesizes Concept P05-C002 (ZoneId), P05-C004 (BigDecimal), and P05-C010 (Pre-compiled Regex Pattern).
 */
public record TaxRule(
        String jurisdictionName,
        ZoneId zoneId,
        BigDecimal ratePercent,
        Pattern taxExemptCodePattern
) {
    public TaxRule {
        Objects.requireNonNull(jurisdictionName, "jurisdictionName must not be null");
        Objects.requireNonNull(zoneId, "zoneId must not be null");
        Objects.requireNonNull(ratePercent, "ratePercent must not be null");
        Objects.requireNonNull(taxExemptCodePattern, "taxExemptCodePattern must not be null");

        if (ratePercent.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tax rate cannot be negative: " + ratePercent);
        }
    }

    public boolean isExempt(String exemptionCode) {
        if (exemptionCode == null || exemptionCode.isBlank()) {
            return false;
        }
        return taxExemptCodePattern.matcher(exemptionCode.strip()).matches();
    }
}
