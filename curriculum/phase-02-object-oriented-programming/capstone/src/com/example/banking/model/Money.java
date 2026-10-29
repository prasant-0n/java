package com.example.banking.model;

import java.util.Objects;

public record Money(long amountCents, Currency currency) implements Comparable<Money> {
    public Money {
        Objects.requireNonNull(currency, "currency required");
        if (amountCents < 0) throw new IllegalArgumentException("Money amount cannot be negative");
    }

    public static Money of(long cents, Currency currency) {
        return new Money(cents, currency);
    }

    public static Money zero(Currency currency) {
        return new Money(0L, currency);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(Math.addExact(this.amountCents, other.amountCents), this.currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        if (this.amountCents < other.amountCents) {
            throw new IllegalArgumentException("Insufficient money for subtraction: " + this.amountCents + " < " + other.amountCents);
        }
        return new Money(Math.subtractExact(this.amountCents, other.amountCents), this.currency);
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other);
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + this.currency.code() + " vs " + other.currency.code());
        }
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return Long.compare(this.amountCents, other.amountCents);
    }

    public String format() {
        long major = amountCents / 100L;
        long minor = Math.abs(amountCents % 100L);
        return String.format("%s%d.%02d", currency.symbol(), major, minor);
    }
}
