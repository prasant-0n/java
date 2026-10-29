package com.example.banking.service;

import com.example.banking.domain.BankAccount;
import com.example.banking.model.CustomerTier;
import com.example.banking.model.Money;

public interface FeeCalculationStrategy {
    Money calculateFee(BankAccount account, Money amount);

    static FeeCalculationStrategy standardTiered() {
        return (account, amount) -> {
            CustomerTier tier = account.getCustomer().tier();
            long feeCents = switch (tier) {
                case VIP -> 0L; // Free for VIP
                case PREMIUM -> 50L; // Flat $0.50
                case STANDARD -> {
                    long percent = (amount.amountCents() * 100L) / 10_000L; // 1.00%
                    yield Math.max(percent, 100L); // Minimum $1.00
                }
            };
            return Money.of(feeCents, account.getCurrency());
        };
    }
}
