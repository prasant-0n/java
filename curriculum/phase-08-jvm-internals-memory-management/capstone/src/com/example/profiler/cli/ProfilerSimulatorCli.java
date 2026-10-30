package com.example.profiler.cli;

import com.example.profiler.diagnostics.JvmMemoryTelemetryAuditor;
import com.example.profiler.leak.LeakingSessionManager;
import com.example.profiler.model.SessionPayload;
import com.example.profiler.safe.GuardedSessionManager;

import java.util.UUID;

/**
 * Command-line interactive and automated execution harness for the
 * JVM Memory & Performance Profiling Simulator.
 */
public class ProfilerSimulatorCli {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println(" JVM Memory & Performance Profiling Simulator (Phase 08 Capstone) ");
        System.out.println("==================================================================");

        var auditor = new JvmMemoryTelemetryAuditor();
        var initialSnapshot = auditor.captureSnapshot();
        printTelemetry("Initial Baseline Telemetry", initialSnapshot);

        boolean runLeak = args.length == 0 || containsArg(args, "--simulate-leak") || containsArg(args, "--all");
        boolean runGuarded = args.length == 0 || containsArg(args, "--simulate-guarded") || containsArg(args, "--all");

        if (runLeak) {
            System.out.println("\n>>> [1/2] Executing Leaking Session Manager Simulation...");
            var leakingManager = new LeakingSessionManager();

            for (int i = 0; i < 1000; i++) {
                UUID sid = UUID.randomUUID();
                var payload = SessionPayload.of(sid, "user-" + i, 101L, 4096); // 4KB each
                leakingManager.putSession(sid.toString(), payload);
            }

            var postLeakSnapshot = auditor.captureSnapshot();
            printTelemetry("Post-Leak Telemetry (1,000 Unbounded Sessions)", postLeakSnapshot);
            double velocity = auditor.calculateLeakVelocity(initialSnapshot, postLeakSnapshot);
            System.out.printf("  [LEAK REPORT] Cached Sessions: %d | Memory Growth: %+.2f KB | Rate: %.2f KB/s%n",
                    leakingManager.getCachedSessionCount(),
                    (postLeakSnapshot.heapUsedBytes() - initialSnapshot.heapUsedBytes()) / 1024.0,
                    velocity / 1024.0);
            leakingManager.clearAllForReset();
        }

        if (runGuarded) {
            System.out.println("\n>>> [2/2] Executing Guarded Session Manager Simulation (Capacity: 100)...");
            var guardedManager = new GuardedSessionManager(100);

            for (int i = 0; i < 1000; i++) {
                UUID sid = UUID.randomUUID();
                var payload = SessionPayload.of(sid, "user-" + i, 101L, 4096); // 4KB each
                guardedManager.putSession(sid.toString(), payload);
            }

            var postGuardedSnapshot = auditor.captureSnapshot();
            printTelemetry("Post-Guarded Telemetry (1,000 Inserts -> Capped at 100)", postGuardedSnapshot);
            System.out.printf("  [GUARDED REPORT] Retained Sessions: %d (Capped at exactly %d)%n",
                    guardedManager.getCachedSessionCount(), 100);
        }

        System.out.println("\nSimulation completed cleanly with exit code 0.");
    }

    private static boolean containsArg(String[] args, String target) {
        for (String arg : args) {
            if (target.equalsIgnoreCase(arg)) return true;
        }
        return false;
    }

    private static void printTelemetry(String header, JvmMemoryTelemetryAuditor.MemorySnapshot s) {
        System.out.println("------------------------------------------------------------------");
        System.out.println(" " + header);
        System.out.println("------------------------------------------------------------------");
        System.out.printf("  Heap Used:       %8.2f MB / %8.2f MB Max%n",
                s.heapUsedBytes() / (1024.0 * 1024.0),
                s.heapMaxBytes() / (1024.0 * 1024.0));
        System.out.printf("  Metaspace Used:  %8.2f MB%n",
                s.metaspaceUsedBytes() / (1024.0 * 1024.0));
        System.out.printf("  Total GC Passes: %8d | Cumulative GC Time: %d ms%n",
                s.totalGcCount(), s.totalGcTimeMs());
    }
}
