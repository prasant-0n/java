# Phase 04 Capstone: Resilient Financial Transfer Transaction Pipeline

---

## 1. Executive Summary & Problem Context

In high-throughput enterprise financial architectures, unhandled exceptions, swallowed errors, and premature logging create silent data corruption, cascading outages, and monitoring alert floods.

This capstone project implements **`resilient-transaction-pipeline`**, a mission-critical financial transaction engine synthesized from first principles across all 9 concepts in **Phase 04: Exception Handling and Defensive Programming**.

### The Core Architectural Mandates
1. **Defensive Invariant Enforcement:** Java record compact constructors guarantee non-null, non-negative, and strictly validated domain state before object allocation.
2. **Failure Classification Discipline:**
   - **Expected Domain Outcomes** (insufficient funds, daily limit exceeded, frozen accounts) return typed **`Result<TransactionReceipt, DomainDecline>`** records. Zero stack traces are allocated.
   - **Exceptional Infrastructure Faults** (ledger lock timeouts, storage disconnection, rollback faults) throw typed **`PipelineException`** subclasses carrying machine-readable `ErrorCode` and structured context maps.
3. **Deterministic Resource Management:** Distributed ledger locks implement `AutoCloseable` managed via **Try-With-Resources**, guaranteeing LIFO release and capturing **Suppressed Exceptions** when cleanup and primary business logic fail simultaneously.
4. **Log-or-Throw Discipline:** Zero intermediate logging. Only the boundary exception handler emits structured logs and translates exceptions to **RFC 7807 Problem Detail** HTTP envelopes.

---

## 2. Architecture & Concept Synthesis

```
                                  Client Request
                                         │
                                         ▼
   ┌────────────────────────────────────────────────────────────────────────┐
   │ 1. Defensive Precondition Gate                                         │
   │    - Compact Constructor validation in TransactionRequest              │
   │    - Objects.requireNonNull(request) (Fail-Fast)                       │
   └─────────────────────────────────────┬──────────────────────────────────┘
                                         │ Valid Request
                                         ▼
   ┌────────────────────────────────────────────────────────────────────────┐
   │ 2. Domain Business Validation (Railway-Oriented via Result<T, E>)      │
   │    - Accounts exist?                                                   │
   │    - Accounts frozen? (Compliance check)                               │
   │    - Source balance >= amount?                                         │
   │    - Daily limit remaining >= amount?                                  │
   └───────────────────────┬──────────────────────────────────┬─────────────┘
                           │ Expected Decline                 │ Approved
                           ▼                                  ▼
           ┌──────────────────────────────┐   ┌─────────────────────────────┐
           │ Return Result.Failure(...)   │   │ 3. Distributed Ledger Locks │
           │ - Zero Exception Overhead    │   │    Try-With-Resources (LIFO)│
           │ - Sealed ADT Pattern Match   │   └──────────────┬──────────────┘
           └──────────────────────────────┘                  │ Locks Held
                                                             ▼
                                              ┌─────────────────────────────┐
                                              │ 4. Transaction Journaling   │
                                              │    - Atomic Debit / Credit  │
                                              │    - In-Memory Commit       │
                                              └──────────────┬──────────────┘
                                                             │
                                     ┌───────────────────────┴───────────────────────┐
                                     │ Success                                       │ Failure (Simulated)
                                     ▼                                               ▼
                     ┌───────────────────────────────┐               ┌───────────────────────────────┐
                     │ Return Result.Success(Receipt)│               │ Rollback & Preserve Causes    │
                     │ - State: COMMITTED            │               │ - Primary cause preserved     │
                     └───────────────────────────────┘               │ - Suppressed close errors     │
                                                                     │ - Throw PipelineException     │
                                                                     └───────────────┬───────────────┘
                                                                                     │
                                                                                     ▼
                                                                     ┌───────────────────────────────┐
                                                                     │ 5. Boundary Exception Handler │
                                                                     │    - Single Structured Log    │
                                                                     │    - RFC 7807 Problem Details │
                                                                     └───────────────────────────────┘
```

---

## 3. Concept Mapping Matrix

| Concept ID | Concept Title | Application in Capstone |
|---|---|---|
| `P04-C001` | The `Throwable` Hierarchy | Distinguishes fatal JVM errors from recoverable pipeline exceptions. Avoids catching `Throwable`. |
| `P04-C002` | Checked vs Unchecked Exceptions | Uses unchecked `PipelineException` for automatic rollback propagation without polluting functional APIs. |
| `P04-C003` | Exception Handling Blocks | Multi-catch handling, strict catch hierarchy ordering, and deterministic execution guarantees. |
| `P04-C004` | Try-With-Resources & AutoCloseable | `LedgerLock` implements `AutoCloseable`. Dual lock cleanup via LIFO order. Preserves suppressed exceptions on dual faults. |
| `P04-C005` | Custom Domain Exceptions & Problem Details | `PipelineException` base hierarchy, `ErrorCode` enum with HTTP status codes, structured context maps, RFC 7807 serialization. |
| `P04-C006` | Exception Chaining & Root Causes | Multi-tier causal chains preserving low-level storage or timeout causes; avoids severed stack traces. |
| `P04-C007` | Defensive Programming & Fail-Fast | Java record compact constructors (`Account`, `TransactionRequest`), `Objects.requireNonNull`, state machine transitions. |
| `P04-C008` | Anti-Patterns: Swallowed & Log-and-Throw | Zero empty catch blocks. Strict "Log OR Throw" discipline enforced: only `handleBoundaryException` logs. |
| `P04-C009` | Exceptions vs Result Types | Expected domain failures (insufficient funds, daily limit) return sealed `Result<TransactionReceipt, DomainDecline>`. |

---

## 4. Package Structure

```
curriculum/phase-04-exception-handling-defensive-programming/capstone/
├── build-and-run.sh
├── README.md
├── resilient-transaction-pipeline-1.0.0.jar
└── src/
    └── com/example/pipeline/
        ├── model/
        │   ├── Account.java
        │   ├── TransactionReceipt.java
        │   ├── TransactionRequest.java
        │   └── TransactionState.java
        ├── result/
        │   ├── DomainDecline.java
        │   └── Result.java
        ├── exception/
        │   ├── ErrorCode.java
        │   ├── LedgerLockException.java
        │   ├── PipelineException.java
        │   ├── ProblemDetailEnvelope.java
        │   └── TransactionRollbackException.java
        ├── lock/
        │   └── LedgerLock.java
        ├── engine/
        │   ├── AccountRepository.java
        │   └── ResilientTransactionPipeline.java
        └── test/
            └── ResilientPipelineIntegrationTest.java
```

---

## 5. Verification & Test Suite Execution

The capstone is verified via an automated test suite containing 10 assertions testing normal and degraded scenarios:

```bash
$ ./build-and-run.sh
=== Building Phase 04 Capstone: Resilient Transaction Pipeline ===
Compiling Java 21 sources with javac...
Packaging into executable JAR: .../resilient-transaction-pipeline-1.0.0.jar...
Executing test suite from standalone JAR...
===============================================================================
  STARTING RESILIENT TRANSACTION PIPELINE INTEGRATION TEST SUITE (Java 21)   
===============================================================================

[Test 1] Happy Path Transfer with Result.Success...
  ✓ Happy path verified: Balances updated, receipt issued with COMMITTED state.

[Test 2] Expected Domain Decline: Insufficient Funds (Result.Failure)...
  ✓ Expected domain decline handled without allocating exception stack traces.

[Test 3] Expected Domain Decline: Daily Limit Exceeded...
  ✓ Daily limit restriction validated via Result.Failure.

[Test 4] Expected Domain Decline: Frozen Account...
  ✓ Compliance freeze validated via Result.Failure.

[Test 5] Expected Domain Decline: Account Not Found...
  ✓ Missing account gracefully handled via Result.Failure.

[Test 6] Defensive Precondition Violations (Fail-Fast IllegalArgumentException)...
  ✓ Fail-fast preconditions verified (NPE, negative amount, self-transfer).

[Test 7] Infrastructure Fault: Ledger Lock Timeout with Structured Context...
  ✓ Ledger lock timeout properly classified with HTTP 503 and structured context.

[Test 8] Try-With-Resources Suppressed Exceptions on Dual Fault...
    Attached suppressed exception: Simulated network failure releasing distributed lock for account: ACC-102
  ✓ Try-With-Resources suppressed exception mechanics verified.

[Test 9] RFC 7807 Problem Detail Envelope Boundary Mapping...
[BOUNDARY LOGGER] Caught PipelineException: Distributed lock unavailable [ErrorCode=LEDGER_LOCK_TIMEOUT, TraceId=...]
  ✓ RFC 7807 Problem Detail Envelope generated with HTTP 503 and trace context.

[Test 10] Database Outage Chained Exception Propagation...
  ✓ Chained repository outage propagated cleanly.
===============================================================================
  ALL 10/10 RESILIENT TRANSACTION PIPELINE INTEGRATION TESTS PASSED CLEANLY!  
===============================================================================
=== Phase 04 Capstone Build and Verification Complete! ===
```
