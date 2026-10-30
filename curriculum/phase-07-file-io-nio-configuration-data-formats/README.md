# Phase 07: File I/O, NIO, Configuration, and Data Formats

---

## 1. Phase Overview and Purpose

### 1.1 Goal
Master file handling, java.nio.file (Path, Files), byte/character streams, buffered I/O, and data serialization (JSON, CSV).

### 1.2 Context in Backend Engineering
Backend servers constantly parse configuration files, process batch CSV imports, read S3 uploads, and serialize JSON responses. Incorrect stream handling leaks file descriptors and exhausts OS limits.

---

## 2. Prerequisites and Co-requisites

- **Direct Prerequisites:**
  - Phase 04 (Exception Handling), Phase 05 (Core APIs).
- **Downstream Dependent Phases:**
  - Phase 15 (HTTP & Networking), Phase 18 (Spring Boot), Phase 20 (Production Engineering).

---

## 3. Measurable Phase Learning Outcomes

By completing Phase 07, the learner will independently be able to:
1. Use java.nio.file.Path and Files for robust cross-platform filesystem operations.
2. Distinguish between byte streams (InputStream/OutputStream) and character streams (Reader/Writer).
3. Process multi-gigabyte files efficiently using streaming buffers without OutOfMemoryError.
4. Parse and validate structured CSV and JSON payloads safely, avoiding Java native serialization.


---

## 4. Concept Inventory and Study Order

| Concept ID | Concept Title | File Name | Importance | Status |
|---|---|---|---|---|
| `P07-C001` | Legacy java.io.File Limitations vs Modern java.nio.file (Path, Files, Paths) | [`001-legacy-io-vs-modern-nio.md`](001-legacy-io-vs-modern-nio.md) | Essential | COMPLETE |
| `P07-C002` | Byte Streams vs Character Streams, Encodings, and UTF-8 Guarantees | [`002-byte-streams-vs-readers-writers.md`](002-byte-streams-vs-readers-writers.md) | Essential | NOT_STARTED |
| `P07-C003` | Buffered Streams, Buffer Sizes, Flushing, and File Descriptor Leaks | [`003-buffered-streams-and-leaks.md`](003-buffered-streams-and-leaks.md) | Essential | NOT_STARTED |
| `P07-C004` | File Operations: Reading, Writing, Atomic Moves, and File Attributes | [`004-file-operations-atomic-moves.md`](004-file-operations-atomic-moves.md) | Essential | NOT_STARTED |
| `P07-C005` | Directory Traversal: DirectoryStream, Files.walk, and Resource Cleanup | [`005-directory-traversal-and-streams.md`](005-directory-traversal-and-streams.md) | Essential | NOT_STARTED |
| `P07-C006` | Java NIO Architecture: Channels, Buffers, Selectors, and Direct Memory | [`006-nio-channels-buffers-direct-memory.md`](006-nio-channels-buffers-direct-memory.md) | Essential | NOT_STARTED |
| `P07-C007` | Large-File Streaming Processing vs In-Memory Buffering | [`007-large-file-streaming-processing.md`](007-large-file-streaming-processing.md) | Essential | NOT_STARTED |
| `P07-C008` | ClassPath Resources vs Filesystem Paths in Packaged JARs | [`008-classpath-resources-in-jars.md`](008-classpath-resources-in-jars.md) | Essential | NOT_STARTED |
| `P07-C009` | Configuration Management: Properties Files and System Environment Overrides | [`009-properties-files-configuration.md`](009-properties-files-configuration.md) | Essential | NOT_STARTED |
| `P07-C010` | Structured Data: Safe CSV Parsing, Escaping, and Validation | [`010-csv-parsing-and-validation.md`](010-csv-parsing-and-validation.md) | Essential | NOT_STARTED |
| `P07-C011` | JSON Processing in Java: Jackson Object Mapper, Streaming API, and Records | [`011-json-processing-jackson.md`](011-json-processing-jackson.md) | Essential | NOT_STARTED |
| `P07-C012` | Java Native Serialization Security Vulnerabilities and Modern Alternatives | [`012-serialization-vulnerabilities.md`](012-serialization-vulnerabilities.md) | Essential | NOT_STARTED |


### Recommended Pedagogical Sequence
```mermaid
flowchart LR
    P07C001["P07-C001: Legacy java.io.File Limit..."] --> P07C002["P07-C002: Byte Streams vs Character..."]
    P07C002["P07-C002: Byte Streams vs Character..."] --> P07C003["P07-C003: Buffered Streams, Buffer ..."]
    P07C003["P07-C003: Buffered Streams, Buffer ..."] --> P07C004["P07-C004: File Operations: Reading,..."]
    P07C004["P07-C004: File Operations: Reading,..."] --> P07C005["P07-C005: Directory Traversal: Dire..."]
    P07C005["P07-C005: Directory Traversal: Dire..."] --> P07C006["P07-C006: Java NIO Architecture: Ch..."]
    P07C006["P07-C006: Java NIO Architecture: Ch..."] --> P07C007["P07-C007: Large-File Streaming Proc..."]
    P07C007["P07-C007: Large-File Streaming Proc..."] --> P07C008["P07-C008: ClassPath Resources vs Fi..."]
    P07C008["P07-C008: ClassPath Resources vs Fi..."] --> P07C009["P07-C009: Configuration Management:..."]
    P07C009["P07-C009: Configuration Management:..."] --> P07C010["P07-C010: Structured Data: Safe CSV..."]
    P07C010["P07-C010: Structured Data: Safe CSV..."] --> P07C011["P07-C011: JSON Processing in Java: ..."]
    P07C011["P07-C011: JSON Processing in Java: ..."] --> P07C012["P07-C012: Java Native Serialization..."]

```

---

## 5. Critical Distinctions and Common Misconceptions

| Misconception / Simplification | Technical Reality from First Principles |
|---|---|
| "Files.readAllBytes or Files.readAllLines is acceptable for all file processing." | Loading large files entirely into heap memory causes OutOfMemoryError and severe GC pauses. Large files must always be streamed using BufferedReader or Files.lines(). |
| "Java native serialization (Serializable) is safe for external network communication." | Java native serialization is inherently insecure and responsible for countless remote code execution (RCE) vulnerabilities. Production systems use JSON, Protocol Buffers, or Avro. |


---

## 6. Phase Integration Project / Capstone Exercise

### Scenario Overview
Build a streaming ETL batch processor that reads a compressed CSV data dump, validates records against business rules, flags malformed lines to an error log, and writes valid records to a JSON file without exceeding 64MB of heap memory.

### Requirements and Acceptance Criteria
- [ ] Requirements fully implemented and demonstrated with executable Java code.
- [ ] Zero unhandled exceptions or edge-case crashes.
- [ ] Verification through automated tests and terminal execution.

---

## 7. Phase Completion Gate and Self-Assessment

Before marking Phase 07 as complete:
- [ ] All concept modules in this phase are thoroughly studied and exercises completed.
- [ ] You can explain every concept in the table from memory without AI assistance.
- [ ] The capstone exercise compiles, passes automated tests, and handles edge cases.
- [ ] Common failure modes can be diagnosed and resolved using standard tooling.

---

## 8. Status and Progress Reconciler

- **Phase Status:** `IN_PROGRESS`
- **Total Planned Concepts:** `12`
- **Completed Concepts:** `1`
- **In Progress:** `0`
- **Completion Rate:** `8.3%`
- **Last Verified Date:** `2026-10-30`
