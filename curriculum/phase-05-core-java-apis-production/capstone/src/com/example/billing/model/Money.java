package com.example.billing.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable Monetary Value Object.
 * Synthesizes Concept P05-C004 (BigDecimal Precision, Scale, RoundingMode.HALF_EVEN)
 * and Concept P05-C007 (Objects Preconditions and Deep Equality).
 */
public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {

    public Money {
        Objects.requireNonNull(amount, "Money amount must not be null");
        Objects.requireNonNull(currency, "Money currency must not be null");

        // Normalize scale to the currency's official minor-unit scale using Banker's Rounding
        amount = amount.setScale(currency.defaultScale(), RoundingMode.HALF_EVEN);
    }

    public static Money of(String amountStr, Currency currency) {
        Objects.requireNonNull(amountStr, "amountStr must not be null");
        return new Money(new BigDecimal(amountStr), currency);
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(BigDecimal factor) {
        Objects.requireNonNull(factor, "factor must not be null");
        BigDecimal result = this.amount.multiply(factor).setScale(currency.defaultScale(), RoundingMode.HALF_EVEN);
        return new Money(result, this.currency);
    }

    public Money divide(BigDecimal divisor) {
        Objects.requireNonNull(divisor, "divisor must not be null");
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Division by zero");
        }
        // Guarantees non-terminating division ArithmeticException defense by specifying scale & rounding
        BigDecimal result = this.amount.divide(divisor, currency.defaultScale(), RoundingMode.HALF_EVEN);
        return new Money(result, this.currency);
    }

    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    private void validateSameCurrency(Money other) {
        Objects.requireNonNull(other, "other money must not be null");
        if (this.currency != other.currency) {
            throw new IllegalArgumentException(
                    "Currency mismatch: cannot operate on " + this.currency + " and " + other.currency
            );
        }
    }

    @Override
    public int compareTo(Money o) {
        validateSameCurrency(o);
        return this.amount.compareTo(o.amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money other)) return false;
        // Compare using compareTo to ignore internal scale differences, along with currency equality
        return this.currency == other.currency && this.amount.compareTo(other.amount) == 0;
    }

    @Override
    public int hashCode() {
        // Strip trailing zeros for consistent hash code matching compareTo
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }

    @Override
    public String toString() {
        return currency.symbol() + amount.toPlainString() + " " + currency.name();
    }
}
