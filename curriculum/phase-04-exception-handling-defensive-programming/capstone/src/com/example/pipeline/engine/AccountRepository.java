package com.example.pipeline.engine;

import com.example.pipeline.exception.ErrorCode;
import com.example.pipeline.exception.PipelineException;
import com.example.pipeline.model.Account;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe account storage with fault injection capabilities.
 */
public class AccountRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private volatile boolean simulateDatabaseOutage = false;

    public void setSimulateDatabaseOutage(boolean simulateDatabaseOutage) {
        this.simulateDatabaseOutage = simulateDatabaseOutage;
    }

    public Optional<Account> findById(String accountId) {
        checkHealth();
        return Optional.ofNullable(accounts.get(accountId));
    }

    public void save(Account account) {
        checkHealth();
        accounts.put(account.accountId(), account);
    }

    private void checkHealth() {
        if (simulateDatabaseOutage) {
            throw new RepositoryUnavailableException("Database cluster connection refused (simulated outage)");
        }
    }

    @SuppressWarnings("serial")
    public static class RepositoryUnavailableException extends PipelineException {
        public RepositoryUnavailableException(String message) {
            super(ErrorCode.DATA_STORE_UNAVAILABLE, message);
        }
    }
}
