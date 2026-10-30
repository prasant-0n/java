# Phase 07 Capstone: Streaming High-Performance ETL Batch Processing Engine

---

## 1. Executive Summary & Problem Context

Modern enterprise data architectures (such as financial clearinghouses, payment gateways, and telemetry pipelines) ingest massive daily batches of transaction logs and partner records. In real-world production environments:
- **Memory Amplification & OutOfMemoryError:** Ingesting multi-gigabyte files using naive in-memory reading methods (`Files.readAllLines`, `Files.readAllBytes`, or `ObjectMapper.readValue(stream, List.class)`) imposes 3x–8x heap expansion factors, causing JVM garbage collection freezes and fatal crashes.
- **Data Corruption via Broken CSV Parsing:** Tabular data dumps contain multiline comments, escaped quotes (`""`), and commas within addresses. Naive splitting (`line.split(",")`) silently misaligns columns and corrupts core ledgers.
- **Security Vulnerabilities (CWE-1236 & CWE-502):** Unsanitized CSV cells starting with formula symbols (`=`, `+`, `-`, `@`) compromise analyst workstations via spreadsheet injection. Meanwhile, legacy binary serialization (`ObjectInputStream`) opens listening microservices to remote code execution (RCE).
- **Concurrency & Partial File Ingestion Hazards:** Writing batch outputs directly to target files exposes downstream consumers to truncated, partially written JSON files during server reboots or network interruptions.

This capstone project implements **`streaming-etl-engine`**, a production-grade, low-memory streaming ETL batch processing engine synthesized from first principles across all 12 concepts in **Phase 07: File I/O, NIO, Configuration, and Data Formats**.

---

## 2. Architecture & Concept Synthesis

```
                                  Incoming GZIP CSV Data Dump
                                               │
                                               ▼
    ┌───────────────────────────────────────────────────────────────────────────┐
    │ 1. Externalized Configuration Cascade (P07-C009)                          │
    │    - EtlBatchConfig: System Properties (-D) > Defaults                    │
    │    - Configurable buffer sizes, line bounds, and allowed currency sets    │
    └──────────────────────────────────────┬────────────────────────────────────┘
                                           │
                                           ▼
    ┌───────────────────────────────────────────────────────────────────────────┐
    │ 2. Streaming GZIP RFC 4180 Decompressor & Parser (P07-C002, P07-C007, C010│
    │    - Layered Decorator: GZIPInputStream -> BufferedInputStream -> Reader  │
    │    - Deterministic 4-State Machine: Handles quotes, commas, multiline CRLF │
    │    - Line-bomb defense: Caps field length, preventing memory exhaustion   │
    │    - Memory consumption is strictly O(1) relative to total file size      │
    └──────────────────────────────────────┬────────────────────────────────────┘
                                           │
                                           ▼
    ┌───────────────────────────────────────────────────────────────────────────┐
    │ 3. Financial Business Invariant Validation (P07-C010, P07-C012)           │
    │    - UUID parsing, positive amount checks, ISO-8601 timestamps            │
    │    - Allowed currency validation (USD, EUR, GBP, CAD, JPY)                │
    │    - Spreadsheet Formula Sanitization (CWE-1236): Prepends "'" to =, +, - │
    │    - Strong typing into immutable Java 21 TransactionRecord               │
    └──────────────────────────────────────┬────────────────────────────────────┘
                                           │
                       ┌───────────────────┴───────────────────┐
                       │                                       │
                [Valid Record]                          [Malformed Record]
                       │                                       │
                       ▼                                       ▼
    ┌───────────────────────────────────┐   ┌───────────────────────────────────┐
    │ 4. Streaming JSON Writer (P07-C011│   │ 5. Dead-Letter Queue Log (P07-C010│
    │    - Staged file writing          │   │    - Line number, failure reason, │
    │    - Token-by-token JSON array    │   │      and raw tokens recorded      │
    │    - Zero-allocation heap pipeline│   │    - Continuous flush to disk     │
    └──────────────────┬────────────────┘   └───────────────────────────────────┘
                       │
                       ▼
    ┌───────────────────────────────────────────────────────────────────────────┐
    │ 6. Crash-Safe Atomic Staging-and-Swap Replacement (P07-C001, P07-C004)    │
    │    - Writes to temporary sibling file in target directory                 │
    │    - StandardCopyOption.ATOMIC_MOVE switches file inode pointer via rename│
    │    - Eliminates partial-read windows for concurrent downstream consumers  │
    └───────────────────────────────────────────────────────────────────────────┘
```

### Traceability Across All 12 Phase Concepts

| Concept ID | Topic | Role in Capstone Engine |
|---|---|---|
| `P07-C001` | Legacy `File` vs Modern `Path` | NIO `Path` and `Files` used throughout; eliminating legacy boolean error concealment. |
| `P07-C002` | Byte Streams vs Character Streams | `GZIPInputStream` (byte stream) translated to UTF-8 character stream via `InputStreamReader`. |
| `P07-C003` | Buffered Streams & Leak Prevention | Optimal 64KB I/O buffer sizing; comprehensive try-with-resources decorator teardown. |
| `P07-C004` | File Operations & Atomic Moves | `StandardCopyOption.ATOMIC_MOVE` staging-and-swap pattern preventing partial-file reads. |
| `P07-C005` | Directory Traversal & Cleanup | Recursive bottom-up test fixture directory teardown eliminating `DirectoryNotEmptyException`. |
| `P07-C006` | NIO Architecture: Channels & Buffers | Direct binary zero-copy stream coordination and buffer sizing principles. |
| `P07-C007` | Large-File Streaming Processing | Constant $O(1)$ memory streaming; processes 10,000+ records within a strict 64MB heap ceiling. |
| `P07-C008` | ClassPath Resources in Packaged JARs | Packaged into standalone executable JAR executed directly via `java -Xmx64m -jar`. |
| `P07-C009` | Configuration & Environment Overrides | `EtlBatchConfig` cascading resolution (System Properties > Defaults). |
| `P07-C010` | Safe CSV Parsing, Escaping, & RFC 4180 | Deterministic 4-state machine parser, multiline support, CWE-1236 formula sanitization, DLQ. |
| `P07-C011` | JSON Processing & Records | Streaming JSON serialization without object array buffering; mapped to Java 21 Records. |
| `P07-C012` | Serialization Security Vulnerabilities | Replaced insecure native serialization with structured, zero-gadget JSON and DLQ streams. |

---

## 3. Project Directory Structure

```
curriculum/phase-07-file-io-nio-configuration-data-formats/capstone/
├── README.md                                 # Architecture documentation & verification report
├── build-and-run.sh                          # Compilation, packaging, and execution script
├── streaming-etl-engine-1.0.0.jar            # Standalone packaged executable JAR
└── src/
    └── com/
        └── example/
            └── etl/
                ├── config/
                │   └── EtlBatchConfig.java            # Cascading configuration resolver
                ├── csv/
                │   └── StreamingGzipCsvReader.java    # RFC 4180 state machine with GZIP streaming
                ├── engine/
                │   └── StreamingEtlProcessor.java     # Pipeline orchestrator & atomic swapper
                ├── json/
                │   └── StreamingJsonWriter.java       # Streaming JSON array emitter
                ├── model/
                │   ├── DlqRecord.java                 # Dead-Letter Queue failure entry
                │   ├── ProcessingSummary.java         # Batch execution summary metrics
                │   └── TransactionRecord.java         # Validated immutable domain record
                ├── test/
                │   └── EtlEngineIntegrationTest.java  # Comprehensive 10-assertion test suite
                └── validator/
                    └── TransactionValidator.java      # Financial invariants & formula sanitization
```

---

## 4. Verification and Benchmark Results

The capstone project was compiled targeting Java 21 LTS with strict compiler warnings (`-Xlint:all -Werror`), packaged into a standalone executable JAR, and verified under a **strict 64MB heap ceiling (`-Xmx64m`)** on Ubuntu Linux:

```
=== Building Phase 07 Capstone: Streaming High-Performance ETL Engine ===
Compiling Java 21 sources with javac...
Packaging into executable JAR: streaming-etl-engine-1.0.0.jar...
Executing test suite from standalone JAR under 64MB heap limit (-Xmx64m)...
===============================================================================
Starting Phase 07 Capstone: Streaming High-Performance ETL Engine Verification
===============================================================================
Running Tests 1 & 2: GZIP Streaming and RFC 4180 Quote Parsing...
 -> PASSED: GZIP Streaming and RFC 4180 Quoting Verified.
Running Tests 3 & 4: Formula Injection Sanitization and Multiline Records...
 -> PASSED: Formula Injection and Multiline CSV Parsing Verified.
Running Tests 5 & 6: Business Invariant Validation and DLQ Error Logging...
 -> PASSED: Schema Validation and DLQ Logging Verified.
Running Tests 7 & 8: Atomic Staging-and-Swap and Configuration Precedence...
 -> PASSED: Atomic Staging-and-Swap and Configuration Precedence Verified.
Running Tests 9 & 10: 10,000-Record Streaming Stress Batch under 64MB Heap...
   [Stress Metrics] Processed 10000 records in 329 ms (30395.1 records/sec)
   [Memory Metrics] Heap Used Delta: ~15.93 MB (Peak allowed: 64MB)
 -> PASSED: High-Throughput Constant Memory Batch Verified.

All 10 integration test assertions passed successfully!
Phase 07 Capstone Integration Project Verified Cleanly!
=== Phase 07 Capstone Build and Verification Complete! ===
```

---

## 5. Key Engineering Takeaways

1. **Constant Memory Streaming ($O(1)$) is Non-Negotiable:** Processing a 10,000-record batch yielded a heap delta of only **~15.9 MB**, comfortably within the 64MB ceiling at over **30,000 records/second**. Streaming decorators allow processing datasets of unbounded size.
2. **RFC 4180 Requires Finite State Automata:** Naive string tokenization cannot handle real-world CSV. A deterministic state machine is the only compliant mechanism for handling embedded newlines and quotes.
3. **Defense-in-Depth for Untrusted Data:** Neutralizing formula characters (`=`, `+`, `-`, `@`) and routing invalid records to a dead-letter queue ensures operational resilience and eliminates attack surfaces without interrupting batch throughput.
4. **Atomic Inode Replacement Eliminates Concurrency Races:** Using `StandardCopyOption.ATOMIC_MOVE` guarantees that downstream services will never observe partial or corrupted JSON output files.
