package com.example.pipeline.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Immutable bank account model with compact constructor invariant enforcement.
 */
public record Account(
        String accountId,
        String ownerName,
        BigDecimal balance,
        BigDecimal dailyLimit,
        BigDecimal dailySpent,
        boolean isFrozen
) {
    public Account {
        Objects.requireNonNull(accountId, "Account ID cannot be null");
        Objects.requireNonNull(ownerName, "Owner name cannot be null");
        Objects.requireNonNull(balance, "Balance cannot be null");
        Objects.requireNonNull(dailyLimit, "Daily limit cannot be null");
        Objects.requireNonNull(dailySpent, "Daily spent cannot be null");

        if (accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be blank");
        }
        if (ownerName.isBlank()) {
            throw new IllegalArgumentException("Owner name cannot be blank");
        }
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative: " + balance);
        }
        if (dailyLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Daily limit cannot be negative: " + dailyLimit);
        }
        if (dailySpent.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Daily spent cannot be negative: " + dailySpent);
        }
        if (dailySpent.compareTo(dailyLimit) > 0) {
            throw new IllegalArgumentException("Daily spent cannot exceed daily limit: " + dailySpent + " > " + dailyLimit);
        }
    }

    public Account withBalance(BigDecimal newBalance) {
        return new Account(accountId, ownerName, newBalance, dailyLimit, dailySpent, isFrozen);
    }

    public Account withDebit(BigDecimal amount) {
        BigDecimal newBalance = balance.subtract(amount);
        BigDecimal newDailySpent = dailySpent.add(amount);
        return new Account(accountId, ownerName, newBalance, dailyLimit, newDailySpent, isFrozen);
    }

    public Account withCredit(BigDecimal amount) {
        return new Account(accountId, ownerName, balance.add(amount), dailyLimit, dailySpent, isFrozen);
    }
}
