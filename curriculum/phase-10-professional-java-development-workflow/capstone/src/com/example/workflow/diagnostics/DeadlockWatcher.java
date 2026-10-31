package com.example.workflow.diagnostics;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * Diagnostic subsystem detecting monitor lock and JUC synchronizer deadlocks programmatically.
 */
public class DeadlockWatcher {

    public record DeadlockReport(
        long threadId,
        String threadName,
        Thread.State state,
        String waitingLockName,
        String lockOwnerName
    ) {}

    public static List<DeadlockReport> scanForDeadlocks() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long[] deadlockedIds = bean.findDeadlockedThreads();
        List<DeadlockReport> reports = new ArrayList<>();

        if (deadlockedIds == null || deadlockedIds.length == 0) {
            return reports;
        }

        ThreadInfo[] infos = bean.getThreadInfo(deadlockedIds, true, true);
        for (ThreadInfo info : infos) {
            if (info != null) {
                reports.add(new DeadlockReport(
                    info.getThreadId(),
                    info.getThreadName(),
                    info.getThreadState(),
                    info.getLockName(),
                    info.getLockOwnerName()
                ));
            }
        }
        return reports;
    }
}
