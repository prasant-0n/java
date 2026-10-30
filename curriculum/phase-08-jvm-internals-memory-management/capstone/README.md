# Phase 08 Capstone: JVM Memory & Performance Profiling Simulator (`jvm-profiling-simulator`)

---

## 1. Executive Summary & Architecture Overview

The **JVM Memory & Performance Profiling Simulator** is a production-grade benchmarking and diagnostics engine synthesizing all 12 core concepts of **Phase 08 (JVM Internals and Memory Management)**.

The project models an enterprise session state subsystem and provides side-by-side execution harnesses contrasting defective, leaking architectures against hardened, leak-resistant patterns with real-time JVM telemetry and JFR event emission.

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                            Application Process Space                             │
│                                                                                  │
│   ┌────────────────────────────────┐       ┌─────────────────────────────────┐   │
│   │ LeakingSessionManager          │       │ GuardedSessionManager           │   │
│   │  • Unbounded static cache      │  VS   │  • Bounded LRU cache (eviction) │   │
│   │  • ThreadLocal leak on pool    │       │  • AutoCloseable guarded scope  │   │
│   │  • Mutable key hashCode breaks │       │  • Immutable record keys        │   │
│   │  • Strong listener retention   │       │  • WeakReference listener bus   │   │
│   └────────────────────────────────┘       └─────────────────────────────────┘   │
│                   │                                         │                    │
│                   └────────────────────┬────────────────────┘                    │
│                                        ▼                                         │
│                      ┌───────────────────────────────────┐                       │
│                      │ JvmMemoryTelemetryAuditor         │                       │
│                      │  • MemoryMXBean / Pool telemetry  │                       │
│                      │  • GarbageCollectorMXBean stats   │                       │
│                      │  • Custom JFR Event Emission      │                       │
│                      │  • dM/dt Leak Velocity Calculator │                       │
│                      └───────────────────────────────────┘                       │
└────────────────────────────────────────┬─────────────────────────────────────────┘
                                         ▼
                 ┌───────────────────────────────────────────────┐
                 │ HotSpot 64-Bit VM Runtime Data Areas          │
                 │ Heap (Eden/Survivor/Old) | Metaspace | JFR    │
                 └───────────────────────────────────────────────┘
```

---

## 2. Synthesis of Phase 08 Concepts

| Concept ID | Topic | Integration in Capstone Project |
|---|---|---|
| `P08-C001` | JVM Architecture Overview | Runtime Data Areas telemetry inspection (Heap vs Non-Heap Metaspace separation). |
| `P08-C002` | Class Loading Subsystem | Type safety isolation and ClassLoader leak risk prevention. |
| `P08-C003` | Thread Stacks & Frames | Stack depth isolation and execution on single-thread worker pools. |
| `P08-C004` | Heap Memory Layout | Generational pool monitoring (Old Gen vs Eden) and allocation pressure. |
| `P08-C005` | Metaspace Architecture | Native off-heap Metaspace usage auditing via `MemoryPoolMXBean`. |
| `P08-C006` | JIT Tiered Compilation | Zero-overhead, highly optimized execution paths for cache eviction and lookups. |
| `P08-C007` | GC Roots & References | `WeakReference` event bus allowing garbage collection of unreferenced subscribers. |
| `P08-C008` | GC Fundamentals | Garbage collection pause monitoring (`GarbageCollectorMXBean`) and mutator overhead. |
| `P08-C009` | Modern Collectors | SLA-compliant memory bounds preventing Old Generation fragmentation. |
| `P08-C010` | Java Memory Leaks | Comprehensive reproduction and resolution of the 5 canonical memory leak archetypes. |
| `P08-C011` | OOM & SOE Diagnosis | Heap growth velocity calculation ($L_r = \Delta M / \Delta t$) predicting time-to-exhaustion. |
| `P08-C012` | JVM CLI & JFR Tooling | Emission of custom JFR diagnostic events (`com.example.profiler.MemoryAuditJfrEvent`). |

---

## 3. Package Structure

```
capstone/
├── README.md                                 # Architecture documentation and execution guide
├── jvm-profiling-simulator-1.0.0.jar         # Standalone executable JAR
├── out/                                      # Compiled classfile directory
└── src/
    └── com/example/profiler/
        ├── cli/
        │   └── ProfilerSimulatorCli.java     # Terminal CLI execution harness
        ├── diagnostics/
        │   └── JvmMemoryTelemetryAuditor.java # MXBean telemetry and custom JFR event emitter
        ├── leak/
        │   └── LeakingSessionManager.java    # Defective memory-leaking manager
        ├── model/
        │   └── SessionPayload.java           # Domain session record entity
        ├── safe/
        │   └── GuardedSessionManager.java    # Hardened, leak-resistant bounded manager
        └── test/
            └── MemoryProfilerIntegrationTest.java # 10-point regression verification suite
```

---

## 4. Build, Test, and Execution Instructions

### Pure CLI Compilation
Targeting Java 21 with all linter warnings treated as errors:
```bash
mkdir -p out
javac --release 21 -parameters -encoding UTF-8 -Xlint:all -Werror -d out $(find src -name "*.java")
```

### Standalone Packaging
```bash
jar --create --file jvm-profiling-simulator-1.0.0.jar --main-class com.example.profiler.cli.ProfilerSimulatorCli -C out .
```

### Run Regression Integration Test Suite
```bash
java -ea -cp out com.example.profiler.test.MemoryProfilerIntegrationTest
```

### Run Packaged Executable Application
```bash
java -ea -jar jvm-profiling-simulator-1.0.0.jar
```

---

## 5. Automated Verification Results

```
==================================================================
 Starting Phase 08 Capstone 10-Point Integration Test Suite       
==================================================================
[PASS] 01: LeakingSessionManager retains all 500 entries without bounds.
[PASS] 02: GuardedSessionManager strictly caps size to configured limit (50).
[PASS] 03: GuardedSessionManager evicts oldest unaccessed entries in LRU order.
[PASS] 04: ThreadLocal without remove() leaks state across pooled worker tasks.
[PASS] 05: Guarded ThreadSessionScope deterministically unbinds via try-with-resources.
[PASS] 06: Mutating key status breaks hashCode() contract creating zombie entries.
[PASS] 07: Immutable record key guarantees deterministic lookup and removal.
[PASS] 08: Strong listener registry prevents subscriber collection.
[PASS] 09: Weak listener registry automatically prunes collected subscribers.
[PASS] 10: JvmMemoryTelemetryAuditor tracks heap delta and leak velocity.

All 10 integration test assertions PASSED with exit code 0!
```
