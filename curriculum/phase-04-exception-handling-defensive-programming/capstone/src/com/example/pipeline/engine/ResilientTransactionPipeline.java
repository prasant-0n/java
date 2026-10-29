package com.example.pipeline.engine;

import com.example.pipeline.exception.ErrorCode;
import com.example.pipeline.exception.LedgerLockException;
import com.example.pipeline.exception.PipelineException;
import com.example.pipeline.exception.ProblemDetailEnvelope;
import com.example.pipeline.exception.TransactionRollbackException;
import com.example.pipeline.lock.LedgerLock;
import com.example.pipeline.model.Account;
import com.example.pipeline.model.TransactionReceipt;
import com.example.pipeline.model.TransactionRequest;
import com.example.pipeline.model.TransactionState;
import com.example.pipeline.result.DomainDecline;
import com.example.pipeline.result.Result;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Resilient financial transaction pipeline synthesizing defensive preconditions,
 * monadic domain results, try-with-resources lock management, and suppressed exception handling.
 */
public class ResilientTransactionPipeline {

    private final AccountRepository repository;
    private volatile boolean simulateLockTimeout = false;
    private volatile boolean simulateProcessingFault = false;
    private volatile boolean simulateLockReleaseFault = false;

    public ResilientTransactionPipeline(AccountRepository repository) {
        this.repository = Objects.requireNonNull(repository, "Repository cannot be null");
    }

    public void setSimulateLockTimeout(boolean simulateLockTimeout) {
        this.simulateLockTimeout = simulateLockTimeout;
    }

    public void setSimulateProcessingFault(boolean simulateProcessingFault) {
        this.simulateProcessingFault = simulateProcessingFault;
    }

    public void setSimulateLockReleaseFault(boolean simulateLockReleaseFault) {
        this.simulateLockReleaseFault = simulateLockReleaseFault;
    }

    /**
     * Executes the financial transfer pipeline.
     * Expected business outcomes return Result.
     * Infrastructure faults throw PipelineException.
     */
    public Result<TransactionReceipt, DomainDecline> processTransfer(TransactionRequest request) {
        // 1. Defensive Precondition Enforcement (Fail-Fast)
        Objects.requireNonNull(request, "Transaction request cannot be null");

        // 2. Business Verification Phase (Railway-Oriented via Result)
        Optional<Account> sourceOpt = repository.findById(request.sourceAccountId());
        if (sourceOpt.isEmpty()) {
            return Result.failure(new DomainDecline.AccountNotFound(request.sourceAccountId()));
        }

        Optional<Account> targetOpt = repository.findById(request.targetAccountId());
        if (targetOpt.isEmpty()) {
            return Result.failure(new DomainDecline.AccountNotFound(request.targetAccountId()));
        }

        Account source = sourceOpt.get();
        Account target = targetOpt.get();

        if (source.isFrozen()) {
            return Result.failure(new DomainDecline.AccountFrozen(source.accountId(), "Source account under compliance freeze"));
        }
        if (target.isFrozen()) {
            return Result.failure(new DomainDecline.AccountFrozen(target.accountId(), "Target account under compliance freeze"));
        }
        if (source.balance().compareTo(request.amount()) < 0) {
            return Result.failure(new DomainDecline.InsufficientFunds(source.accountId(), request.amount(), source.balance()));
        }

        BigDecimal remainingLimit = source.dailyLimit().subtract(source.dailySpent());
        if (request.amount().compareTo(remainingLimit) > 0) {
            return Result.failure(new DomainDecline.DailyLimitExceeded(source.accountId(), request.amount(), remainingLimit));
        }

        // 3. Execution Phase with Try-With-Resources (Guaranteed Deterministic Cleanup)
        return executeTransactionalTransfer(request, source, target);
    }

    private Result<TransactionReceipt, DomainDecline> executeTransactionalTransfer(
            TransactionRequest request,
            Account initialSource,
            Account initialTarget) {

        // Check simulated lock contention
        if (simulateLockTimeout) {
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("transactionId", request.transactionId());
            ctx.put("sourceAccountId", request.sourceAccountId());
            ctx.put("targetAccountId", request.targetAccountId());
            ctx.put("timeoutMillis", 5000);
            throw new LedgerLockException("Failed to acquire distributed ledger locks within timeout limit", null, ctx);
        }

        // Try-With-Resources managing distributed locks (LIFO order: target closed first, then source)
        try (LedgerLock sourceLock = new LedgerLock(request.sourceAccountId(), request.transactionId());
             LedgerLock targetLock = new LedgerLock(request.targetAccountId(), request.transactionId())) {

            if (simulateLockReleaseFault) {
                sourceLock.setSimulateCloseFailure(true);
                targetLock.setSimulateCloseFailure(true);
            }

            // Execute in-memory state transition
            Account debitedSource = initialSource.withDebit(request.amount());
            Account creditedTarget = initialTarget.withCredit(request.amount());

            if (simulateProcessingFault) {
                // Simulate unexpected storage crash during execution
                throw new IllegalStateException("Storage engine dropped connection during journal commit");
            }

            // Save accounts atomically
            repository.save(debitedSource);
            repository.save(creditedTarget);

            TransactionReceipt receipt = new TransactionReceipt(
                    request.transactionId(),
                    debitedSource.accountId(),
                    creditedTarget.accountId(),
                    request.amount(),
                    Instant.now(),
                    TransactionState.COMMITTED
            );

            return Result.success(receipt);

        } catch (Exception ex) {
            // Rethrow or wrap, preserving suppressed exceptions attached by TWR
            if (ex instanceof PipelineException pe) {
                throw pe;
            }
            Map<String, Object> ctx = Map.of(
                    "transactionId", request.transactionId(),
                    "sourceAccountId", request.sourceAccountId(),
                    "state", TransactionState.ROLLED_BACK
            );
            TransactionRollbackException rollbackEx = new TransactionRollbackException(
                    "Transaction execution failed: " + ex.getMessage(), ex, ctx);

            // Transfer any suppressed exceptions from TWR close failures
            for (Throwable suppressed : ex.getSuppressed()) {
                rollbackEx.addSuppressed(suppressed);
            }
            throw rollbackEx;
        }
    }

    /**
     * Centralized boundary exception handler (Log OR Throw discipline).
     */
    public ProblemDetailEnvelope handleBoundaryException(PipelineException ex, String path, String traceId) {
        // Boundary logs exactly ONCE
        System.err.println("[BOUNDARY LOGGER] Caught PipelineException: " + ex.getMessage() + 
                " [ErrorCode=" + ex.getErrorCode() + ", TraceId=" + traceId + "]");
        return ProblemDetailEnvelope.fromPipelineException(ex, path, traceId);
    }
}
