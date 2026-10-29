package com.example.banking.service;

import com.example.banking.domain.BankAccount;
import com.example.banking.model.Money;

@FunctionalInterface
public interface RiskEngine {
    boolean requiresChallenge(BankAccount account, Money amount);
}
