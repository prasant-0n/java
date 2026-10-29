package com.example.pipeline.test;

import com.example.pipeline.engine.AccountRepository;
import com.example.pipeline.engine.ResilientTransactionPipeline;
import com.example.pipeline.exception.ErrorCode;
import com.example.pipeline.exception.LedgerLockException;
import com.example.pipeline.exception.ProblemDetailEnvelope;
import com.example.pipeline.exception.TransactionRollbackException;
import com.example.pipeline.model.Account;
import com.example.pipeline.model.TransactionReceipt;
import com.example.pipeline.model.TransactionRequest;
import com.example.pipeline.model.TransactionState;
import com.example.pipeline.result.DomainDecline;
import com.example.pipeline.result.Result;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * End-to-end integration and regression test suite for the Resilient Transaction Pipeline.
 */
public class ResilientPipelineIntegrationTest {

    public static void main(String[] args) {
        System.out.println("===============================================================================");
        System.out.println("  STARTING RESILIENT TRANSACTION PIPELINE INTEGRATION TEST SUITE (Java 21)   ");
        System.out.println("===============================================================================");

        testHappyPathTransfer();
        testExpectedDomainDeclineInsufficientFunds();
        testExpectedDomainDeclineDailyLimitExceeded();
        testExpectedDomainDeclineFrozenAccount();
        testExpectedDomainDeclineAccountNotFound();
        testDefensivePreconditionViolations();
        testLedgerLockTimeoutWithStructuredContext();
        testTryWithResourcesSuppressedExceptions();
        testRFC7807ProblemDetailMapping();
        testDatabaseOutageChainedException();

        System.out.println("===============================================================================");
        System.out.println("  ALL 10/10 RESILIENT TRANSACTION PIPELINE INTEGRATION TESTS PASSED CLEANLY!  ");
        System.out.println("===============================================================================");
    }

    private static AccountRepository createSampleRepository() {
        AccountRepository repo = new AccountRepository();
        repo.save(new Account("ACC-101", "Alice Johnson", BigDecimal.valueOf(1000.00), BigDecimal.valueOf(500.00), BigDecimal.ZERO, false));
        repo.save(new Account("ACC-102", "Bob Smith", BigDecimal.valueOf(250.00), BigDecimal.valueOf(500.00), BigDecimal.ZERO, false));
        repo.save(new Account("ACC-FROZEN", "Charlie Brown", BigDecimal.valueOf(5000.00), BigDecimal.valueOf(1000.00), BigDecimal.ZERO, true));
        return repo;
    }

    static void testHappyPathTransfer() {
        System.out.println("\n[Test 1] Happy Path Transfer with Result.Success...");
        AccountRepository repo = createSampleRepository();
        ResilientTransactionPipeline pipeline = new ResilientTransactionPipeline(repo);

        TransactionRequest req = new TransactionRequest(
                "TX-" + UUID.randomUUID(),
                "ACC-101",
                "ACC-102",
                BigDecimal.valueOf(150.00),
                "Consulting Invoice Payment"
        );

        Result<TransactionReceipt, DomainDecline> result = pipeline.processTransfer(req);
        assert result.isSuccess() : "Expected transfer to succeed";

        TransactionReceipt receipt = result.getOrElse(null);
        assert receipt != null;
        assert receipt.state() == TransactionState.COMMITTED;
        assert receipt.amountTransferred().equals(BigDecimal.valueOf(150.00));

        // Verify repository balances
        Account alice = repo.findById("ACC-101").orElseThrow();
        Account bob = repo.findById("ACC-102").orElseThrow();
        assert alice.balance().compareTo(BigDecimal.valueOf(850.00)) == 0 : "Alice balance should be 850, was: " + alice.balance();
        assert alice.dailySpent().compareTo(BigDecimal.valueOf(150.00)) == 0 : "Alice daily spent should be 150";
        assert bob.balance().compareTo(BigDecimal.valueOf(400.00)) == 0 : "Bob balance should be 400";

        System.out.println("  ✓ Happy path verified: Balances updated, receipt issued with COMMITTED state.");
    }

    static void testExpectedDomainDeclineInsufficientFunds() {
        System.out.println("\n[Test 2] Expected Domain Decline: Insufficient Funds (Result.Failure)...");
        AccountRepository repo = createSampleRepository();
        ResilientTransactionPipeline pipeline = new ResilientTransactionPipeline(repo);

        TransactionRequest req = new TransactionRequest(
                "TX-" + UUID.randomUUID(),
                "ACC-102", // Bob has 250
                "ACC-101",
                BigDecimal.valueOf(300.00), // Exceeds balance
                "Excessive Transfer"
        );

        Result<TransactionReceipt, DomainDecline> result = pipeline.processTransfer(req);
        assert result.isFailure() : "Expected failure due to insufficient funds";

        String reason = switch (result) {
            case Result.Success<TransactionReceipt, DomainDecline> s -> "Unexpected success";
            case Result.Failure<TransactionReceipt, DomainDecline>(DomainDecline.InsufficientFunds f) -> {
                assert f.requested().equals(BigDecimal.valueOf(300.00));
                assert f.available().equals(BigDecimal.valueOf(250.00));
                yield f.reason();
            }
            case Result.Failure<TransactionReceipt, DomainDecline> other -> "Wrong failure variant";
        };
        assert reason.contains("insufficient funds");

        // Verify balances untouched
        assert repo.findById("ACC-102").orElseThrow().balance().compareTo(BigDecimal.valueOf(250.00)) == 0;
        System.out.println("  ✓ Expected domain decline handled without allocating exception stack traces.");
    }

    static void testExpectedDomainDeclineDailyLimitExceeded() {
        System.out.println("\n[Test 3] Expected Domain Decline: Daily Limit Exceeded...");
        AccountRepository repo = createSampleRepository();
        ResilientTransactionPipeline pipeline = new ResilientTransactionPipeline(repo);

        TransactionRequest req = new TransactionRequest(
                "TX-" + UUID.randomUUID(),
                "ACC-101", // Alice has 1000 balance, but daily limit is 500
                "ACC-102",
                BigDecimal.valueOf(600.00), // Exceeds limit 500
                "Large Car Purchase Downpayment"
        );

        Result<TransactionReceipt, DomainDecline> result = pipeline.processTransfer(req);
        assert result.isFailure();
        assert switch (result) {
            case Result.Failure<TransactionReceipt, DomainDecline>(DomainDecline.DailyLimitExceeded d) -> {
                assert d.remainingLimit().compareTo(BigDecimal.valueOf(500.00)) == 0;
                yield true;
            }
            default -> false;
        };

        System.out.println("  ✓ Daily limit restriction validated via Result.Failure.");
    }

    static void testExpectedDomainDeclineFrozenAccount() {
        System.out.println("\n[Test 4] Expected Domain Decline: Frozen Account...");
        AccountRepository repo = createSampleRepository();
        ResilientTransactionPipeline pipeline = new ResilientTransactionPipeline(repo);

        TransactionRequest req = new TransactionRequest(
                "TX-" + UUID.randomUUID(),
                "ACC-FROZEN",
                "ACC-101",
                BigDecimal.valueOf(100.00),
                "Frozen transfer"
        );

        Result<TransactionReceipt, DomainDecline> result = pipeline.processTransfer(req);
        assert result.isFailure();
        assert switch (result) {
            case Result.Failure<TransactionReceipt, DomainDecline>(DomainDecline.AccountFrozen f) -> true;
            default -> false;
        };

        System.out.println("  ✓ Compliance freeze validated via Result.Failure.");
    }

    static void testExpectedDomainDeclineAccountNotFound() {
        System.out.println("\n[Test 5] Expected Domain Decline: Account Not Found...");
        AccountRepository repo = createSampleRepository();
        ResilientTransactionPipeline pipeline = new ResilientTransactionPipeline(repo);

        TransactionRequest req = new TransactionRequest(
                "TX-" + UUID.randomUUID(),
                "ACC-999-NONEXISTENT",
                "ACC-101",
                BigDecimal.valueOf(10.00),
                "Ghost account transfer"
        );

        Result<TransactionReceipt, DomainDecline> result = pipeline.processTransfer(req);
        assert result.isFailure();
        assert switch (result) {
            case Result.Failure<TransactionReceipt, DomainDecline>(DomainDecline.AccountNotFound nf) -> true;
            default -> false;
        };

        System.out.println("  ✓ Missing account gracefully handled via Result.Failure.");
    }

    static void testDefensivePreconditionViolations() {
        System.out.println("\n[Test 6] Defensive Precondition Violations (Fail-Fast IllegalArgumentException)...");

        // Null request
        boolean npeCaught = false;
        try {
            AccountRepository repo = createSampleRepository();
            ResilientTransactionPipeline pipeline = new ResilientTransactionPipeline(repo);
            pipeline.processTransfer(null);
        } catch (NullPointerException e) {
            npeCaught = true;
        }
        assert npeCaught : "Must throw NPE on null request";

        // Negative transfer amount
        boolean negativeAmountCaught = false;
        try {
            new TransactionRequest("TX-1", "ACC-101", "ACC-102", BigDecimal.valueOf(-50.00), "Negative");
        } catch (IllegalArgumentException e) {
            negativeAmountCaught = true;
            assert e.getMessage().contains("strictly positive");
        }
        assert negativeAmountCaught : "Must reject negative transfer amounts";

        // Self transfer
        boolean selfTransferCaught = false;
        try {
            new TransactionRequest("TX-2", "ACC-101", "ACC-101", BigDecimal.valueOf(50.00), "Self");
        } catch (IllegalArgumentException e) {
            selfTransferCaught = true;
            assert e.getMessage().contains("distinct");
        }
        assert selfTransferCaught : "Must reject self-transfers";

        System.out.println("  ✓ Fail-fast preconditions verified (NPE, negative amount, self-transfer).");
    }

    static void testLedgerLockTimeoutWithStructuredContext() {
        System.out.println("\n[Test 7] Infrastructure Fault: Ledger Lock Timeout with Structured Context...");
        AccountRepository repo = createSampleRepository();
        ResilientTransactionPipeline pipeline = new ResilientTransactionPipeline(repo);
        pipeline.setSimulateLockTimeout(true);

        TransactionRequest req = new TransactionRequest(
                "TX-LOCK-FAIL",
                "ACC-101",
                "ACC-102",
                BigDecimal.valueOf(50.00),
                "Contended transfer"
        );

        boolean caught = false;
        try {
            pipeline.processTransfer(req);
        } catch (LedgerLockException ex) {
            caught = true;
            assert ex.getErrorCode() == ErrorCode.LEDGER_LOCK_TIMEOUT;
            assert ex.getErrorCode().getHttpStatus() == 503;
            assert ex.getContext().containsKey("timeoutMillis");
            assert ex.getContext().get("timeoutMillis").equals(5000);
            assert ex.getContext().get("sourceAccountId").equals("ACC-101");
        }
        assert caught : "Must throw LedgerLockException on lock timeout";

        System.out.println("  ✓ Ledger lock timeout properly classified with HTTP 503 and structured context.");
    }

    static void testTryWithResourcesSuppressedExceptions() {
        System.out.println("\n[Test 8] Try-With-Resources Suppressed Exceptions on Dual Fault...");
        AccountRepository repo = createSampleRepository();
        ResilientTransactionPipeline pipeline = new ResilientTransactionPipeline(repo);
        // Both processing fails AND closing the locks fails!
        pipeline.setSimulateProcessingFault(true);
        pipeline.setSimulateLockReleaseFault(true);

        TransactionRequest req = new TransactionRequest(
                "TX-DUAL-FAULT",
                "ACC-101",
                "ACC-102",
                BigDecimal.valueOf(50.00),
                "Dual fault transfer"
        );

        boolean caught = false;
        try {
            pipeline.processTransfer(req);
        } catch (TransactionRollbackException ex) {
            caught = true;
            assert ex.getErrorCode() == ErrorCode.TRANSACTION_ROLLBACK_FAILED;
            assert ex.getCause() instanceof IllegalStateException : "Primary cause must be preserved";
            assert ex.getCause().getMessage().contains("journal commit");

            // Verify that lock close errors were attached as SUPPRESSED exceptions!
            Throwable[] suppressed = ex.getSuppressed();
            assert suppressed.length > 0 : "Must have suppressed exceptions attached from TWR";
            assert suppressed[0].getMessage().contains("releasing distributed lock");
            System.out.println("    Attached suppressed exception: " + suppressed[0].getMessage());
        }
        assert caught : "Must catch TransactionRollbackException";

        System.out.println("  ✓ Try-With-Resources suppressed exception mechanics verified.");
    }

    static void testRFC7807ProblemDetailMapping() {
        System.out.println("\n[Test 9] RFC 7807 Problem Detail Envelope Boundary Mapping...");
        AccountRepository repo = createSampleRepository();
        ResilientTransactionPipeline pipeline = new ResilientTransactionPipeline(repo);

        LedgerLockException ex = new LedgerLockException("Distributed lock unavailable", null, 
                java.util.Map.of("lockId", "LOCK-123", "cluster", "us-east-1"));

        String traceId = UUID.randomUUID().toString();
        ProblemDetailEnvelope problem = pipeline.handleBoundaryException(ex, "/api/v1/transfers", traceId);

        assert problem.status() == 503;
        assert problem.title().equals("LEDGER_LOCK_TIMEOUT");
        assert problem.instance().toString().contains(traceId);
        assert problem.properties().get("lockId").equals("LOCK-123");

        System.out.println("  ✓ RFC 7807 Problem Detail Envelope generated with HTTP 503 and trace context.");
    }

    static void testDatabaseOutageChainedException() {
        System.out.println("\n[Test 10] Database Outage Chained Exception Propagation...");
        AccountRepository repo = createSampleRepository();
        repo.setSimulateDatabaseOutage(true);
        ResilientTransactionPipeline pipeline = new ResilientTransactionPipeline(repo);

        TransactionRequest req = new TransactionRequest(
                "TX-DB-DOWN",
                "ACC-101",
                "ACC-102",
                BigDecimal.valueOf(25.00),
                "DB outage test"
        );

        boolean caught = false;
        try {
            pipeline.processTransfer(req);
        } catch (AccountRepository.RepositoryUnavailableException ex) {
            caught = true;
            assert ex.getErrorCode() == ErrorCode.DATA_STORE_UNAVAILABLE;
            assert ex.getErrorCode().getHttpStatus() == 503;
        }
        assert caught : "Must throw RepositoryUnavailableException";

        System.out.println("  ✓ Chained repository outage propagated cleanly.");
    }
}
