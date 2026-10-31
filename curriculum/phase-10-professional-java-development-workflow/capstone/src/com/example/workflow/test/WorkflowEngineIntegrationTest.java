package com.example.workflow.test;

import com.example.workflow.diagnostics.DeadlockWatcher;
import com.example.workflow.diagnostics.ThreadDumpInspector;
import com.example.workflow.git.ConventionalCommitValidator;
import com.example.workflow.git.GitDagEngine;
import com.example.workflow.logging.MdcTraceContext;
import com.example.workflow.logging.PiiMaskingConverter;
import com.example.workflow.logging.StructuredJsonLogEncoder;
import com.example.workflow.model.MaskedSecret;
import com.example.workflow.model.UserSession;
import com.example.workflow.quality.EncapsulationGuard;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

/**
 * 10-Point Integration Test Suite for Phase 10 Capstone:
 * DevOps Workflow Engine.
 */
public class WorkflowEngineIntegrationTest {

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println(" DevOps Workflow Engine - 10-Point Capstone Integration Test Suite              ");
        System.out.println("================================================================================");

        test1_TypeLevelMasking();
        test2_StructuredJsonLogEncodingAndCrlfNeutralization();
        test3_MdcThreadLocalContextIsolation();
        test4_MdcAutoCloseableThreadPoolPollutionDefense();
        test5_CreditCardPanMaskingBinPreservation();
        test6_BearerTokenAndSsnSanitization();
        test7_ProgrammaticDeadlockDetection();
        test8_ThreadDumpInspectionAndStateClassification();
        test9_EncapsulationDefensiveCopy();
        test10_ConventionalCommitsSemVerAndGitDagMerges();

        System.out.println();
        System.out.println("All 10 Phase 10 Capstone integration test suites passed with assertions enabled!");
        System.out.println("================================================================================");
    }

    private static void test1_TypeLevelMasking() {
        System.out.print("[TEST 01/10] Type-level domain model masking with MaskedSecret... ");
        UserSession session = new UserSession(
            "S-1", "alice", "T-99", MaskedSecret.of("SecretToken123"), MaskedSecret.of("4111-2222-3333-4444")
        );
        String s = session.toString();
        assert !s.contains("SecretToken123") : "Token leaked!";
        assert !s.contains("4111-2222-3333-4444") : "PAN leaked!";
        assert s.contains("token=********");
        assert s.contains("card=********");
        System.out.println("PASSED");
    }

    private static void test2_StructuredJsonLogEncodingAndCrlfNeutralization() {
        System.out.print("[TEST 02/10] Structured JSON log encoding and CRLF injection defense... ");
        String maliciousMsg = "Login failed\r\n2026-10-31 [ADMIN] Fake injected log";
        StructuredJsonLogEncoder.LogRecord rec = new StructuredJsonLogEncoder.LogRecord(
            Instant.now(), "WARN", "AuthService", "main", maliciousMsg, Map.of("traceId", "T-1")
        );
        String json = StructuredJsonLogEncoder.encode(rec);
        assert !json.contains("\r\n") : "Raw CRLF newline leaked into JSON!";
        assert json.contains("\\r\\n") : "CRLF must be escaped into literal string";
        assert json.contains("\"traceId\":\"T-1\"");
        System.out.println("PASSED");
    }

    private static void test3_MdcThreadLocalContextIsolation() throws Exception {
        System.out.print("[TEST 03/10] MDC ThreadLocal context isolation across worker threads... ");
        int threads = 4;
        CountDownLatch latch = new CountDownLatch(threads);
        Map<String, String> observed = new ConcurrentHashMap<>();

        for (int i = 0; i < threads; i++) {
            final String trace = "TRACE-NODE-" + i;
            new Thread(() -> {
                try {
                    MdcTraceContext.put("traceId", trace);
                    Thread.sleep(20);
                    observed.put(Thread.currentThread().getName(), MdcTraceContext.get("traceId"));
                } catch (Exception ignored) {
                } finally {
                    MdcTraceContext.clear();
                    latch.countDown();
                }
            }, "worker-" + i).start();
        }

        latch.await(3, TimeUnit.SECONDS);
        assert observed.size() == threads;
        Set<String> uniqueTraces = new HashSet<>(observed.values());
        assert uniqueTraces.size() == threads : "MDC cross-thread pollution detected!";
        System.out.println("PASSED");
    }

    private static void test4_MdcAutoCloseableThreadPoolPollutionDefense() {
        System.out.print("[TEST 04/10] MDC try-with-resources AutoCloseable pollution defense... ");
        MdcTraceContext.clear();
        assert MdcTraceContext.get("traceId") == null;

        try (MdcTraceContext.Scope scope = MdcTraceContext.openScope("T-100", "TENANT-1", "user1")) {
            assert "T-100".equals(MdcTraceContext.get("traceId"));
            assert "TENANT-1".equals(MdcTraceContext.get("tenantId"));
        }

        assert MdcTraceContext.get("traceId") == null : "MDC leaked outside try block!";
        assert MdcTraceContext.get("tenantId") == null;
        System.out.println("PASSED");
    }

    private static void test5_CreditCardPanMaskingBinPreservation() {
        System.out.print("[TEST 05/10] PCI-DSS credit card PAN masking (BIN + last 4 preservation)... ");
        String log = "Charge $50 to 4111-2222-3333-4444 and refund to 5500001234567890";
        String masked = PiiMaskingConverter.maskCreditCard(log);
        assert !masked.contains("4111-2222-3333-4444");
        assert !masked.contains("5500001234567890");
        assert masked.contains("4111-22-******-4444") || masked.contains("411122-******-4444");
        assert masked.contains("4444");
        assert masked.contains("7890");
        System.out.println("PASSED");
    }

    private static void test6_BearerTokenAndSsnSanitization() {
        System.out.print("[TEST 06/10] Bearer JWT token and US Social Security Number sanitization... ");
        String authHeader = "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgN7s";
        String ssnMsg = "Tax file for SSN 123-45-6789";
        String sanitizedAuth = PiiMaskingConverter.maskBearerToken(authHeader);
        String sanitizedSsn = PiiMaskingConverter.maskSsn(ssnMsg);

        assert !sanitizedAuth.contains("dozjgN7s") : "JWT signature leaked!";
        assert sanitizedAuth.contains("Bearer eyJhbG...[REDACTED]");
        assert !sanitizedSsn.contains("123-45-6789") : "SSN leaked!";
        assert sanitizedSsn.contains("***-**-6789");
        System.out.println("PASSED");
    }

    private static void test7_ProgrammaticDeadlockDetection() throws Exception {
        System.out.print("[TEST 07/10] Programmatic deadlock detection via DeadlockWatcher... ");
        Object lockA = new Object();
        Object lockB = new Object();
        CountDownLatch started = new CountDownLatch(2);
        CountDownLatch latch = new CountDownLatch(1);

        Thread t1 = new Thread(() -> {
            synchronized (lockA) {
                started.countDown();
                try {
                    latch.await();
                    synchronized (lockB) {}
                } catch (InterruptedException ignored) {}
            }
        }, "Deadlock-A");

        Thread t2 = new Thread(() -> {
            synchronized (lockB) {
                started.countDown();
                try {
                    latch.await();
                    synchronized (lockA) {}
                } catch (InterruptedException ignored) {}
            }
        }, "Deadlock-B");

        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();

        started.await();
        latch.countDown();
        Thread.sleep(80);

        List<DeadlockWatcher.DeadlockReport> deadlocks = DeadlockWatcher.scanForDeadlocks();
        assert deadlocks.size() == 2 : "Expected 2 deadlocked threads, found: " + deadlocks.size();
        System.out.println("PASSED");
    }

    private static void test8_ThreadDumpInspectionAndStateClassification() {
        System.out.print("[TEST 08/10] Thread dump snapshot inspection and state classification... ");
        List<ThreadDumpInspector.ThreadSnapshot> snapshots = ThreadDumpInspector.captureSnapshot();
        assert !snapshots.isEmpty();
        boolean foundMain = false;
        for (ThreadDumpInspector.ThreadSnapshot s : snapshots) {
            if ("main".equals(s.name())) {
                foundMain = true;
                assert s.state() == Thread.State.RUNNABLE;
                assert s.stackDepth() > 0;
            }
        }
        assert foundMain : "Main thread must be present in snapshot";
        System.out.println("PASSED");
    }

    private static void test9_EncapsulationDefensiveCopy() {
        System.out.print("[TEST 09/10] Encapsulation and SpotBugs EI_EXPOSE_REP defensive copying... ");
        byte[] raw = new byte[]{10, 20, 30};
        EncapsulationGuard.GuardedBuffer guarded = new EncapsulationGuard.GuardedBuffer(raw);
        raw[0] = 99; // mutate external input
        assert guarded.getBuffer()[0] == 10 : "External mutation leaked into GuardedBuffer!";

        guarded.getBuffer()[1] = 88; // mutate getter clone
        assert guarded.getBuffer()[1] == 20 : "Getter return mutation leaked into GuardedBuffer!";
        System.out.println("PASSED");
    }

    private static void test10_ConventionalCommitsSemVerAndGitDagMerges() {
        System.out.print("[TEST 10/10] Conventional Commits, SemVer calculation, and Git DAG merges... ");
        // Conventional commit parsing
        var c1 = ConventionalCommitValidator.validate("feat(auth): support Passkeys").orElseThrow();
        assert c1.getSemVerBump() == ConventionalCommitValidator.SemVerBump.MINOR;

        var c2 = ConventionalCommitValidator.validate("fix(core)!: drop legacy serialization").orElseThrow();
        assert c2.getSemVerBump() == ConventionalCommitValidator.SemVerBump.MAJOR;

        // Git DAG merge resolution
        GitDagEngine.CommitNode c0 = new GitDagEngine.CommitNode("Initial commit");
        GitDagEngine.CommitNode master = new GitDagEngine.CommitNode("feat: setup build", c0);
        GitDagEngine.CommitNode feat = new GitDagEngine.CommitNode("feat(order): add cart", master);

        // Fast-Forward merge
        GitDagEngine.CommitNode ff = GitDagEngine.merge(master, feat, "Merge");
        assert ff.getHash().equals(feat.getHash()) : "Fast-forward merge tip mismatch";

        // 3-Way merge
        GitDagEngine.CommitNode masterDiverged = new GitDagEngine.CommitNode("fix(security): patch", master);
        GitDagEngine.CommitNode threeWay = GitDagEngine.merge(masterDiverged, feat, "Merge branch 'feat'");
        assert threeWay.getParents().size() == 2 : "3-way merge commit must have 2 parents";
        System.out.println("PASSED");
    }
}
