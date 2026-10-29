package com.example.banking.service;

import com.example.banking.domain.BankAccount;
import com.example.banking.model.Money;
import com.example.banking.model.TransactionRecord;
import com.example.banking.model.TransactionResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

public class BankingCoreEngine {

    private final RiskEngine riskEngine;
    private final FeeCalculationStrategy feeStrategy;

    public BankingCoreEngine(RiskEngine riskEngine, FeeCalculationStrategy feeStrategy) {
        this.riskEngine = Objects.requireNonNull(riskEngine);
        this.feeStrategy = Objects.requireNonNull(feeStrategy);
    }

    public BankingCoreEngine() {
        this(
            (account, amount) -> amount.amountCents() > account.getCustomer().tier().getDailyTransferLimitCents(),
            FeeCalculationStrategy.standardTiered()
        );
    }

    public TransactionResult processDeposit(BankAccount account, Money amount, String description) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(amount);

        try {
            TransactionRecord record = account.deposit(amount, description);
            return new TransactionResult.Success(record, "DEP-" + UUID.randomUUID().toString().substring(0, 8), Instant.now());
        } catch (Exception e) {
            return new TransactionResult.Rejected("DEPOSIT_REJECTED", e.getMessage(), Instant.now());
        }
    }

    public TransactionResult processWithdrawal(BankAccount account, Money amount, String description) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(amount);

        if (riskEngine.requiresChallenge(account, amount)) {
            return new TransactionResult.RequiresChallenge(
                UUID.randomUUID().toString(),
                "STEP_UP_2FA_SMS",
                amount,
                Instant.now().plus(15, ChronoUnit.MINUTES)
            );
        }

        try {
            TransactionRecord record = account.withdraw(amount, description);
            return new TransactionResult.Success(record, "WTH-" + UUID.randomUUID().toString().substring(0, 8), Instant.now());
        } catch (Exception e) {
            return new TransactionResult.Rejected("WITHDRAWAL_REJECTED", e.getMessage(), Instant.now());
        }
    }

    public TransactionResult processTransfer(BankAccount source, BankAccount target, Money amount, String description) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);
        Objects.requireNonNull(amount);

        if (source.equals(target)) {
            return new TransactionResult.Rejected("SELF_TRANSFER_PROHIBITED", "Source and target accounts must be distinct", Instant.now());
        }

        if (!source.getCurrency().equals(target.getCurrency())) {
            return new TransactionResult.Rejected("CURRENCY_MISMATCH", "Cross-currency transfers not supported", Instant.now());
        }

        if (riskEngine.requiresChallenge(source, amount)) {
            return new TransactionResult.RequiresChallenge(
                UUID.randomUUID().toString(),
                "HIGH_VALUE_WIRE_AUTHORIZATION",
                amount,
                Instant.now().plus(30, ChronoUnit.MINUTES)
            );
        }

        Money fee = feeStrategy.calculateFee(source, amount);
        Money totalDebit = amount.add(fee);

        // Atomic lock ordering to avoid deadlock
        BankAccount firstLock = source.getAccountId().value().compareTo(target.getAccountId().value()) < 0 ? source : target;
        BankAccount secondLock = firstLock == source ? target : source;

        synchronized (firstLock) {
            synchronized (secondLock) {
                if (source.getBalance().amountCents() < totalDebit.amountCents()) {
                    return new TransactionResult.Rejected("INSUFFICIENT_FUNDS", "Insufficient funds including fee: " + fee.format(), Instant.now());
                }

                try {
                    TransactionRecord debitRecord = source.withdraw(totalDebit, "Transfer to " + target.getAccountId() + " (Fee: " + fee.format() + "): " + description);
                    target.deposit(amount, "Transfer from " + source.getAccountId() + ": " + description);

                    return new TransactionResult.Success(debitRecord, "XFR-" + UUID.randomUUID().toString().substring(0, 8), Instant.now());
                } catch (Exception e) {
                    return new TransactionResult.Rejected("TRANSFER_FAILED", e.getMessage(), Instant.now());
                }
            }
        }
    }
}
