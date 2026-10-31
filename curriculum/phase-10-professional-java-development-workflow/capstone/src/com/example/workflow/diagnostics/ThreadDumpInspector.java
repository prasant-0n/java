package com.example.workflow.diagnostics;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * Diagnostic subsystem capturing thread stack traces and thread state distribution.
 */
public class ThreadDumpInspector {

    public record ThreadSnapshot(
        String name,
        long id,
        Thread.State state,
        int stackDepth,
        String topFrame
    ) {}

    public static List<ThreadSnapshot> captureSnapshot() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] infos = bean.dumpAllThreads(false, false);
        List<ThreadSnapshot> snapshots = new ArrayList<>();

        for (ThreadInfo info : infos) {
            if (info == null) continue;
            StackTraceElement[] stack = info.getStackTrace();
            String top = stack.length > 0 ? stack[0].toString() : "none";
            snapshots.add(new ThreadSnapshot(
                info.getThreadName(),
                info.getThreadId(),
                info.getThreadState(),
                stack.length,
                top
            ));
        }
        return snapshots;
    }
}
