package com.example.profiler.diagnostics;

import jdk.jfr.*;

import java.lang.management.*;
import java.util.List;

/**
 * JVM memory telemetry auditor that harvests runtime memory pools,
 * evaluates garbage collection statistics, emits JFR events, and
 * detects memory leak trajectories via Old Gen slope analysis.
 */
public class JvmMemoryTelemetryAuditor {

    // Custom JFR Event
    @Name("com.example.profiler.MemoryAuditJfrEvent")
    @Label("Memory Audit Telemetry Event")
    @Category({"JVM", "Diagnostics"})
    @Description("Emits periodic JVM heap, metaspace, and garbage collection metrics")
    @StackTrace(false)
    public static class MemoryAuditJfrEvent extends Event {
        @Label("Heap Used Bytes")
        private long heapUsedBytes;

        @Label("Heap Max Bytes")
        private long heapMaxBytes;

        @Label("Metaspace Used Bytes")
        private long metaspaceUsedBytes;

        @Label("Old Gen Used Bytes")
        private long oldGenUsedBytes;

        @Label("GC Total Pauses Ms")
        private long gcTotalPauseMs;

        public void setHeapUsedBytes(long bytes) { this.heapUsedBytes = bytes; }
        public void setHeapMaxBytes(long bytes) { this.heapMaxBytes = bytes; }
        public void setMetaspaceUsedBytes(long bytes) { this.metaspaceUsedBytes = bytes; }
        public void setOldGenUsedBytes(long bytes) { this.oldGenUsedBytes = bytes; }
        public void setGcTotalPauseMs(long ms) { this.gcTotalPauseMs = ms; }
    }

    public record MemorySnapshot(
            long timestampMs,
            long heapUsedBytes,
            long heapMaxBytes,
            long metaspaceUsedBytes,
            long oldGenUsedBytes,
            long totalGcCount,
            long totalGcTimeMs
    ) {}

    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final List<MemoryPoolMXBean> poolBeans = ManagementFactory.getMemoryPoolMXBeans();
    private final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();

    public MemorySnapshot captureSnapshot() {
        long now = System.currentTimeMillis();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

        long oldGenUsed = 0;
        for (MemoryPoolMXBean pool : poolBeans) {
            String name = pool.getName().toLowerCase();
            if (name.contains("old") || name.contains("tenured") || name.contains("zheap")) {
                oldGenUsed = pool.getUsage().getUsed();
                break;
            }
        }

        long totalGcCount = 0;
        long totalGcTime = 0;
        for (GarbageCollectorMXBean gc : gcBeans) {
            long count = gc.getCollectionCount();
            long time = gc.getCollectionTime();
            if (count > 0) totalGcCount += count;
            if (time > 0) totalGcTime += time;
        }

        // Emit JFR event
        MemoryAuditJfrEvent event = new MemoryAuditJfrEvent();
        event.setHeapUsedBytes(heapUsage.getUsed());
        event.setHeapMaxBytes(heapUsage.getMax());
        event.setMetaspaceUsedBytes(nonHeapUsage.getUsed());
        event.setOldGenUsedBytes(oldGenUsed);
        event.setGcTotalPauseMs(totalGcTime);
        event.commit();

        return new MemorySnapshot(
                now,
                heapUsage.getUsed(),
                heapUsage.getMax(),
                nonHeapUsage.getUsed(),
                oldGenUsed,
                totalGcCount,
                totalGcTime
        );
    }

    /**
     * Calculates the memory leak velocity (slope dM/dt) between two snapshots.
     * @return rate of memory growth in bytes per second.
     */
    public double calculateLeakVelocity(MemorySnapshot start, MemorySnapshot end) {
        long timeDeltaMs = end.timestampMs() - start.timestampMs();
        if (timeDeltaMs <= 0) return 0.0;
        long memoryDeltaBytes = end.heapUsedBytes() - start.heapUsedBytes();
        return (double) memoryDeltaBytes / (timeDeltaMs / 1000.0);
    }
}
