package com.example.pipeline.lock;

import java.time.Instant;
import java.util.Objects;

/**
 * Distributed ledger lock implementing AutoCloseable for Try-With-Resources management.
 */
public class LedgerLock implements AutoCloseable {

    private final String accountId;
    private final String transactionId;
    private final Instant lockedAt;
    private boolean closed = false;
    private boolean simulateCloseFailure = false;

    public LedgerLock(String accountId, String transactionId) {
        this.accountId = Objects.requireNonNull(accountId, "Account ID cannot be null");
        this.transactionId = Objects.requireNonNull(transactionId, "Transaction ID cannot be null");
        this.lockedAt = Instant.now();
    }

    public void setSimulateCloseFailure(boolean simulateCloseFailure) {
        this.simulateCloseFailure = simulateCloseFailure;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Instant getLockedAt() {
        return lockedAt;
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws IllegalStateException {
        if (closed) {
            return;
        }
        closed = true;
        if (simulateCloseFailure) {
            throw new IllegalStateException("Simulated network failure releasing distributed lock for account: " + accountId);
        }
    }
}
