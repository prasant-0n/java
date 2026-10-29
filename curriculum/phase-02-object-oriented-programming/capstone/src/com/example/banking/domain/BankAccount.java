package com.example.banking.domain;

import com.example.banking.model.AccountId;
import com.example.banking.model.AccountState;
import com.example.banking.model.AccountType;
import com.example.banking.model.Currency;
import com.example.banking.model.Customer;
import com.example.banking.model.Money;
import com.example.banking.model.TransactionRecord;
import com.example.banking.model.TransactionType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BankAccount {

    private final AccountId accountId;
    private final Customer customer;
    private final AccountType accountType;
    private final Currency currency;

    private AccountState state;
    private long balanceCents;
    private final List<TransactionRecord> ledger = new ArrayList<>();

    private BankAccount(Builder builder) {
        this.accountId = builder.accountId;
        this.customer = builder.customer;
        this.accountType = builder.accountType;
        this.currency = builder.currency;
        this.state = builder.initialState;
        this.balanceCents = builder.initialBalanceCents;

        if (builder.initialBalanceCents > 0) {
            Money initMoney = Money.of(builder.initialBalanceCents, currency);
            recordTransaction(TransactionType.DEPOSIT, initMoney, "Initial Opening Balance");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public AccountId getAccountId() { return accountId; }
    public Customer getCustomer() { return customer; }
    public AccountType getAccountType() { return accountType; }
    public Currency getCurrency() { return currency; }
    public synchronized AccountState getState() { return state; }
    public synchronized Money getBalance() { return Money.of(balanceCents, currency); }

    public synchronized List<TransactionRecord> getLedger() {
        return Collections.unmodifiableList(new ArrayList<>(ledger));
    }

    public synchronized void transitionState(AccountState newState) {
        Objects.requireNonNull(newState, "newState cannot be null");
        if (!state.canTransitionTo(newState)) {
            throw new IllegalStateException(
                String.format("Invalid state transition: Cannot transition from %s to %s", state, newState)
            );
        }
        this.state = newState;
    }

    public synchronized TransactionRecord deposit(Money amount, String description) {
        verifyCanTransact();
        validateCurrency(amount);
        if (amount.amountCents() <= 0) {
            throw new IllegalArgumentException("Deposit amount must be strictly positive");
        }

        this.balanceCents = Math.addExact(this.balanceCents, amount.amountCents());
        return recordTransaction(TransactionType.DEPOSIT, amount, description);
    }

    public synchronized TransactionRecord withdraw(Money amount, String description) {
        verifyCanTransact();
        validateCurrency(amount);
        if (amount.amountCents() <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be strictly positive");
        }
        if (this.balanceCents < amount.amountCents()) {
            throw new IllegalStateException(
                String.format("Insufficient funds: Available %d cents, requested %d cents", balanceCents, amount.amountCents())
            );
        }

        this.balanceCents = Math.subtractExact(this.balanceCents, amount.amountCents());
        return recordTransaction(TransactionType.WITHDRAWAL, amount, description);
    }

    public synchronized TransactionRecord applyMonthlyInterest() {
        verifyCanTransact();
        long interestCents = accountType.calculateMonthlyInterestCents(this.balanceCents);
        if (interestCents <= 0) {
            return null;
        }

        Money interestMoney = Money.of(interestCents, currency);
        this.balanceCents = Math.addExact(this.balanceCents, interestCents);
        return recordTransaction(TransactionType.INTEREST_CREDIT, interestMoney, "Monthly Yield Accrual (" + accountType.name() + ")");
    }

    private TransactionRecord recordTransaction(TransactionType type, Money amount, String description) {
        TransactionRecord record = new TransactionRecord(
            UUID.randomUUID().toString(),
            type,
            amount,
            Money.of(balanceCents, currency),
            description,
            Instant.now()
        );
        this.ledger.add(record);
        return record;
    }

    private void verifyCanTransact() {
        if (!state.allowsTransactions()) {
            throw new IllegalStateException("Account is not in active state for transactions. Current state: " + state);
        }
    }

    private void validateCurrency(Money money) {
        Objects.requireNonNull(money, "Money cannot be null");
        if (!this.currency.equals(money.currency())) {
            throw new IllegalArgumentException(
                String.format("Currency mismatch: Account currency is %s, transaction is %s", currency.code(), money.currency().code())
            );
        }
    }

    // =========================================================================
    // OBJECT SUPERCLASS CONTRACTS
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BankAccount other)) return false;
        return Objects.equals(this.accountId, other.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }

    @Override
    public String toString() {
        String rawVal = accountId.value();
        String maskedAcc = (rawVal.length() > 6)
            ? rawVal.substring(0, 4) + "****" + rawVal.substring(rawVal.length() - 2)
            : "****";

        return String.format(
            "BankAccount[id=%s, type=%s, state=%s, balance=%s, owner=%s]",
            maskedAcc,
            accountType,
            state,
            Money.of(balanceCents, currency).format(),
            customer.fullName()
        );
    }

    // =========================================================================
    // STATIC NESTED BUILDER
    // =========================================================================

    public static final class Builder {
        private AccountId accountId;
        private Customer customer;
        private AccountType accountType = AccountType.CHECKING;
        private Currency currency = Currency.USD;
        private AccountState initialState = AccountState.ACTIVE;
        private long initialBalanceCents = 0L;

        private Builder() {}

        public Builder accountId(AccountId accountId) {
            this.accountId = Objects.requireNonNull(accountId);
            return this;
        }

        public Builder customer(Customer customer) {
            this.customer = Objects.requireNonNull(customer);
            return this;
        }

        public Builder accountType(AccountType accountType) {
            this.accountType = Objects.requireNonNull(accountType);
            return this;
        }

        public Builder currency(Currency currency) {
            this.currency = Objects.requireNonNull(currency);
            return this;
        }

        public Builder initialState(AccountState initialState) {
            this.initialState = Objects.requireNonNull(initialState);
            return this;
        }

        public Builder initialBalanceCents(long initialBalanceCents) {
            if (initialBalanceCents < 0) {
                throw new IllegalArgumentException("Initial balance cannot be negative");
            }
            this.initialBalanceCents = initialBalanceCents;
            return this;
        }

        public BankAccount build() {
            if (accountId == null) throw new IllegalStateException("accountId is mandatory");
            if (customer == null) throw new IllegalStateException("customer is mandatory");
            return new BankAccount(this);
        }
    }
}
