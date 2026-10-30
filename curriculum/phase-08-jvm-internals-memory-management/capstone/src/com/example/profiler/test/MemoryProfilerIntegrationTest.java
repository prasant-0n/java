package com.example.profiler.test;

import com.example.profiler.diagnostics.JvmMemoryTelemetryAuditor;
import com.example.profiler.leak.LeakingSessionManager;
import com.example.profiler.model.SessionPayload;
import com.example.profiler.safe.GuardedSessionManager;

import java.util.UUID;
import java.util.concurrent.*;

/**
 * Regression integration test suite validating the JVM Memory & Performance Profiling Simulator.
 * Verifies all 10 core architectural and diagnostic requirements with assertions enabled (-ea).
 */
public class MemoryProfilerIntegrationTest {

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================================");
        System.out.println(" Starting Phase 08 Capstone 10-Point Integration Test Suite       ");
        System.out.println("==================================================================");

        testLeakingManagerUnboundedGrowth();
        System.out.println("[PASS] 01: LeakingSessionManager retains all 500 entries without bounds.");

        testGuardedManagerCapacityCapping();
        System.out.println("[PASS] 02: GuardedSessionManager strictly caps size to configured limit (50).");

        testGuardedLruEvictionOrder();
        System.out.println("[PASS] 03: GuardedSessionManager evicts oldest unaccessed entries in LRU order.");

        testThreadLocalLeakOnPooledThread();
        System.out.println("[PASS] 04: ThreadLocal without remove() leaks state across pooled worker tasks.");

        testGuardedThreadSessionScopeAutoCloseable();
        System.out.println("[PASS] 05: Guarded ThreadSessionScope deterministically unbinds via try-with-resources.");

        testMutableKeyHashCodeContractViolationFailure();
        System.out.println("[PASS] 06: Mutating key status breaks hashCode() contract creating zombie entries.");

        testImmutableRecordKeyRemovalDeterminism();
        System.out.println("[PASS] 07: Immutable record key guarantees deterministic lookup and removal.");

        testStrongListenerRetention();
        System.out.println("[PASS] 08: Strong listener registry prevents subscriber collection.");

        testWeakListenerAutoPruning();
        System.out.println("[PASS] 09: Weak listener registry automatically prunes collected subscribers.");

        testTelemetryAuditorLeakVelocityCalculation();
        System.out.println("[PASS] 10: JvmMemoryTelemetryAuditor tracks heap delta and leak velocity.");

        System.out.println("\nAll 10 integration test assertions PASSED with exit code 0!");
    }

    // 01
    static void testLeakingManagerUnboundedGrowth() {
        var manager = new LeakingSessionManager();
        for (int i = 0; i < 500; i++) {
            UUID sid = UUID.randomUUID();
            manager.putSession(sid.toString(), SessionPayload.of(sid, "user-" + i, 1L, 1024));
        }
        assert manager.getCachedSessionCount() == 500 : "Leaking manager must retain all 500 sessions";
        manager.clearAllForReset();
    }

    // 02
    static void testGuardedManagerCapacityCapping() {
        var manager = new GuardedSessionManager(50);
        for (int i = 0; i < 500; i++) {
            UUID sid = UUID.randomUUID();
            manager.putSession(sid.toString(), SessionPayload.of(sid, "user-" + i, 1L, 1024));
        }
        assert manager.getCachedSessionCount() == 50 :
                "Guarded manager must cap cache strictly at 50, but was " + manager.getCachedSessionCount();
    }

    // 03
    static void testGuardedLruEvictionOrder() {
        var manager = new GuardedSessionManager(3);
        manager.putSession("k1", SessionPayload.of(UUID.randomUUID(), "u1", 1L, 10));
        manager.putSession("k2", SessionPayload.of(UUID.randomUUID(), "u2", 1L, 10));
        manager.putSession("k3", SessionPayload.of(UUID.randomUUID(), "u3", 1L, 10));

        // Access k1 to make it most recently used
        assert manager.getSession("k1") != null;

        // Add k4 -> should evict k2 (oldest unaccessed)
        manager.putSession("k4", SessionPayload.of(UUID.randomUUID(), "u4", 1L, 10));

        assert manager.containsSession("k1") : "k1 was accessed; must be retained";
        assert !manager.containsSession("k2") : "k2 was eldest unaccessed; must be evicted";
        assert manager.containsSession("k3") : "k3 must be retained";
        assert manager.containsSession("k4") : "k4 must be retained";
    }

    // 04
    static void testThreadLocalLeakOnPooledThread() throws Exception {
        var manager = new LeakingSessionManager();
        ExecutorService pool = Executors.newSingleThreadExecutor();

        UUID sid = UUID.randomUUID();
        pool.submit(() -> manager.bindThreadSession(SessionPayload.of(sid, "leaked-user", 99L, 100))).get();

        // Subsequent task on same thread detects leaked state
        Future<SessionPayload> leakedResult = pool.submit(manager::getThreadSession);
        SessionPayload leaked = leakedResult.get();
        assert leaked != null : "ThreadLocal must leak across tasks on pooled thread";
        assert "leaked-user".equals(leaked.userId()) : "Leaked payload user mismatch";

        pool.shutdown();
        assert pool.awaitTermination(2, TimeUnit.SECONDS);
    }

    // 05
    static void testGuardedThreadSessionScopeAutoCloseable() throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();

        pool.submit(() -> {
            var payload = SessionPayload.of(UUID.randomUUID(), "guarded-user", 99L, 100);
            try (var scope = GuardedSessionManager.ThreadSessionScope.bind(payload)) {
                assert scope != null;
                assert GuardedSessionManager.ThreadSessionScope.current().isPresent() : "Scope must be active";
            }
        }).get();

        // Verify thread context is now empty
        Future<Boolean> contextCheck = pool.submit(() ->
                GuardedSessionManager.ThreadSessionScope.current().isEmpty()
        );
        assert contextCheck.get() : "Thread context must be cleaned upon try-with-resources exit";

        pool.shutdown();
        assert pool.awaitTermination(2, TimeUnit.SECONDS);
    }

    // 06
    static void testMutableKeyHashCodeContractViolationFailure() {
        var manager = new LeakingSessionManager();
        var key = new LeakingSessionManager.MutableSessionKey("sess-alpha", 1);
        manager.registerKey(key);

        assert manager.containsKey(key) : "Key must initially be found";

        // Mutate status contributing to hashCode()
        key.setStatus(2);

        // Removal fails because bucket index changed!
        boolean removed = manager.removeKey(key);
        assert !removed : "removeKey must fail when hashCode() changed";
        assert !manager.containsKey(key) : "containsKey must fail looking in new bucket";
        assert manager.getKeyCount() == 1 : "Zombie entry remains stranded in internal table!";
    }

    // 07
    static void testImmutableRecordKeyRemovalDeterminism() {
        var manager = new GuardedSessionManager(10);
        UUID sid = UUID.randomUUID();
        var key = new GuardedSessionManager.ImmutableSessionKey(sid, 1);
        manager.registerKey(key);

        assert manager.containsKey(key) : "Record key must be found";
        boolean removed = manager.removeKey(key);
        assert removed : "Record key must be successfully removed";
        assert manager.getKeyCount() == 0 : "Index must be empty";
    }

    // 08
    static void testStrongListenerRetention() {
        var manager = new LeakingSessionManager();
        LeakingSessionManager.SessionEventListener listener = (t, s) -> {};
        manager.registerListener(listener);
        assert manager.getListenerCount() == 1 : "Strong listener registered";
        listener = null; // Caller clears reference
        assert manager.getListenerCount() == 1 : "Strong reference inside manager retains listener";
    }

    // 09
    static void testWeakListenerAutoPruning() {
        var manager = new GuardedSessionManager(10);
        GuardedSessionManager.SessionEventListener listener = new GuardedSessionManager.SessionEventListener() {
            private final int instanceTag = 42;
            @Override public void onSessionEvent(String type, String sid) {}
        };
        manager.registerListener(listener);
        assert manager.getActiveListenerCount() == 1 : "Weak listener registered";

        // Nullify strong reference and invoke GC
        listener = null;
        for (int i = 0; i < 5; i++) {
            System.gc();
            try { Thread.sleep(20); } catch (InterruptedException ignored) {}
        }

        assert manager.getActiveListenerCount() == 0 :
                "Weak listener must be pruned after garbage collection";
    }

    // 10
    static void testTelemetryAuditorLeakVelocityCalculation() {
        var auditor = new JvmMemoryTelemetryAuditor();
        var s1 = auditor.captureSnapshot();
        assert s1.heapUsedBytes() > 0 : "Heap used must be positive";
        assert s1.metaspaceUsedBytes() > 0 : "Metaspace used must be positive";

        // Simulate time and memory progression
        var s2 = new JvmMemoryTelemetryAuditor.MemorySnapshot(
                s1.timestampMs() + 2000,
                s1.heapUsedBytes() + 1024 * 1024, // +1MB
                s1.heapMaxBytes(),
                s1.metaspaceUsedBytes(),
                s1.oldGenUsedBytes(),
                s1.totalGcCount(),
                s1.totalGcTimeMs()
        );

        double velocity = auditor.calculateLeakVelocity(s1, s2);
        assert velocity > 500_000.0 && velocity < 600_000.0 :
                "Leak velocity should be ~524,288 bytes/sec, got " + velocity;
    }
}
