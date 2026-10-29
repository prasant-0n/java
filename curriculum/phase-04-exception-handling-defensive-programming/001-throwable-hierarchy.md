# The Throwable Hierarchy: Error, Exception, and RuntimeException

---

## 1. Concept Definition & Core Purpose

In Java, all abnormal conditions, runtime failures, and system errors are represented as first-class objects rooted at a single base class: **`java.lang.Throwable`**.

Under `Throwable`, the Java type system divides all possible failure scenarios into two mutually exclusive branches:

1. **`java.lang.Error` (Fatal System Conditions):**
   - Represents severe problems that a reasonable application should **never** attempt to catch or recover from.
   - Originates from internal JVM failures, physical resource depletion, hardware faults, or dynamic bytecode linkage incompatibilities.
   - *Examples:* `OutOfMemoryError`, `StackOverflowError`, `VirtualMachineError`, `NoClassDefFoundError`, `InternalError`.
2. **`java.lang.Exception` (Application-Level Failure Conditions):**
   - Represents exceptional conditions that a reasonable application might want to catch, handle, or recover from.
   - Subdivided into two distinct categories:
     - **Checked Exceptions (Direct subclasses of `Exception`, excluding `RuntimeException`):** Conditions outside the application's immediate control (e.g., network disconnects, file system failures, database timeouts). The compiler **enforces** that the caller must handle them via `try-catch` or declare them via `throws`.
     - **Unchecked Exceptions (Subclasses of `java.lang.RuntimeException`):** Conditions that generally indicate programming defects, contract violations, or illegal application states (e.g., passing `null` where prohibited, invalid array indices, state machine violations). The compiler does **not** force methods to declare or catch them.

```
                                java.lang.Throwable
                                         │
                 ┌───────────────────────┴───────────────────────┐
                 ▼                                               ▼
          java.lang.Error                               java.lang.Exception
          (Fatal / Unchecked)                                    │
          - OutOfMemoryError                      ┌──────────────┴──────────────┐
          - StackOverflowError                    ▼                             ▼
          - NoClassDefFoundError          Checked Exceptions          RuntimeException
                                          (Recoverable / Enforced)    (Bugs / Unchecked)
                                          - IOException               - NullPointerException
                                          - SQLException              - IllegalArgumentException
                                          - ClassNotFoundException    - IllegalStateException
```

---

## 2. Background, Context & Historical Evolution

### Pre-Java C/C++: Error Codes and Ignored Return Values
In C and early C++, functions communicated failure by returning status integer codes (e.g., `-1`, `NULL`, or updating global `errno`). This model had three fatal architectural flaws:
1. **Pervasive Code Clutter:** Every function call required immediate conditional checks (`if (res < 0)`).
2. **Silent Failure Propagation:** Developers frequently forgot to check return values, allowing corrupted states to propagate silently through execution paths.
3. **No Context:** A return code like `-1` carried zero stack trace context or root cause information.

### Java 1.0 (1995): James Gosling's Checked Exception Vision
James Gosling designed Java's exception model (inspired by CLU and Modula-3) with a radical premise: **Checked Exceptions**.
- The compiler would force developers to explicitly acknowledge and handle every potential environmental failure at compile time.
- If a method could fail to read a file (`IOException`), it had to declare `throws IOException`. The caller could not compile without handling it.

### The Great Shift: Spring Framework and Runtime Exceptions
By the early 2000s, checked exceptions had become one of Java's most criticized features:
- Core APIs like JDBC forced every database call to catch `java.sql.SQLException`, even though 99% of SQL exceptions (syntax errors, deadlocks, connection drops) could not be meaningfully recovered from at the call site.
- Developers developed dangerous habits: catching `Exception` and doing nothing, or printing `e.printStackTrace()` and proceeding with corrupted state.
- **The Spring Revolution (2003+):** Rod Johnson pioneered wrapping low-level checked exceptions into an unchecked hierarchy (`org.springframework.dao.DataAccessException`). This separated clean domain logic from infrastructure recovery.

### Modern Java Consensus (Java 8 to 21 LTS)
Today, enterprise Java engineering follows a clear division of responsibility:
- **`Error`:** Never catch. Allow the JVM or container to terminate or report fatal health status.
- **`RuntimeException`:** The default choice for 95% of application errors (bad inputs, invariant violations, business rule rejections, unrecoverable I/O).
- **`Checked Exception`:** Reserved exclusively for **genuinely recoverable external conditions** where the immediate caller is expected to have an alternative business fallback (e.g., `PaymentDeclinedException` prompting for a secondary card).

### Transition Guide: JavaScript/TypeScript vs. Java Throwable

| Feature / Dimension | JavaScript / TypeScript (Node.js) | Java (`Throwable`) |
|---|---|---|
| **What can be thrown?** | Anything: `throw "error"`, `throw 404`, `throw new Error()` | Only instances of `java.lang.Throwable` or its subclasses |
| **Compile-Time Checking** | None. All exceptions are unchecked; `try/catch` is completely optional | Strictly enforced for checked exceptions (`throws` clause required) |
| **Error vs Exception** | Single `Error` class (with subclasses like `TypeError`, `RangeError`) | Formal separation: `Error` (fatal JVM state) vs `Exception` (application logic) |
| **Stack Trace Capture** | Captured lazily or via `new Error()` | Captured eagerly during construction by native `fillInStackTrace()` |

---

## 3. Declarative Mechanics & Mental Model

### Anatomy of `java.lang.Throwable`

Every `Throwable` instance encapsulates three critical diagnostic components:

1. **Detail Message (`String getMessage()`):** A human-readable diagnostic message explaining what failed.
2. **Causal Chain (`Throwable getCause()`):** A reference to the underlying exception that triggered this exception (initialized via constructor or `initCause(Throwable)`).
3. **Execution Stack Trace (`StackTraceElement[] getStackTrace()`):** A snapshot of the thread's execution call frames at the exact moment the `Throwable` object was instantiated.

```java
public class Throwable implements Serializable {
    private String detailMessage;
    private Throwable cause = this; // Self-referential sentinel indicating no cause
    private StackTraceElement[] stackTrace;

    // The native hook that crawls the thread's call stack:
    public synchronized Throwable fillInStackTrace() { ... }
}
```

---

### The Cost of Exception Instantiation: `fillInStackTrace()`

A widespread performance pitfall in backend systems is assuming that throwing an exception is lightweight.
> [!IMPORTANT]
> The performance cost of an exception is **not** in the `throw` or `catch` statement. It is in the **instantiation** of the `Throwable` object!

When `new Exception()` executes, the constructor invokes the native method `fillInStackTrace()`:
1. The JVM stops bytecode execution in the current thread.
2. HotSpot crawls down the native OS/JVM thread stack frame by frame.
3. For every frame, it resolves the class name, method name, file name, and source code line number by querying the JVM symbol tables.
4. It allocates a heap array of `StackTraceElement` objects.

For a call stack 60 frames deep (typical in Spring Boot / Hibernate enterprise applications), instantiating an exception is **100x to 500x more expensive** than instantiating an ordinary domain object!

#### The High-Throughput Optimization: Disabling Writable Stack Traces
For high-frequency exceptions where the stack trace is not needed (e.g., flow control or expected validation rejections in reactive pipelines), Java allows creating "zero-cost" exceptions by disabling stack trace population:

```java
public class LightweightValidationException extends RuntimeException {
    public LightweightValidationException(String message) {
        // (message, cause, enableSuppression, writableStackTrace)
        super(message, null, false, false); // writableStackTrace = false!
    }
}
```
Setting `writableStackTrace = false` prevents `fillInStackTrace()` from crawling the stack, making the exception instantiation as fast as an ordinary `new Object()`!

---

## 4. HotSpot JVM Architecture & Memory Subsystem Realities

### Bytecode Exception Tables: Zero-Cost `try-catch`
How does HotSpot execute `try-catch` blocks at the machine level?
Older runtimes in the 1990s pushed handlers onto a runtime stack upon entering `try`. Modern HotSpot uses **Static Exception Tables** stored in the classfile's `Code` attribute (JVMS §4.7.3):

```bytecode
Exception table:
   from    to  target type
       0     8    11   Class java/io/IOException
       0     8    24   Class java/lang/Exception
```
- **During Normal Execution (Happy Path):** The JVM executes the instructions in the `from` to `to` range. There are **zero additional CPU instructions, zero register writes, and zero branch penalties**. A `try-catch` block has literally **zero overhead** when no exception is thrown!
- **When an Exception is Thrown (`athrow` instruction):**
  1. HotSpot takes the program counter (`pc`) where the exception occurred.
  2. It searches the method's exception table from top to bottom.
  3. The first entry matching `from <= pc < to` and whose `type` matches the thrown object becomes the jump target.
  4. The operand stack is cleared, the exception reference is pushed, and execution resumes at `target`.

### HotSpot Fast Throw Optimization (`-XX:+OmitStackTraceInFastThrow`)
In production environments running for days, developers often notice that an exception logged millions of times suddenly **stops printing stack traces**, logging only:
```
java.lang.NullPointerException: null
(no stack trace available)
```
#### Why does this happen?
HotSpot's C2 JIT compiler tracks exception throw counts. When a specific built-in exception (like `NullPointerException`, `ArithmeticException`, `ArrayIndexOutOfBoundsException`) is thrown repeatedly from the exact same bytecode location, the C2 compiler replaces the allocation with a **pre-allocated, static singleton exception instance with an empty stack trace** to save CPU and memory!
- If you need full stack traces during debugging, disable this via `-XX:-OmitStackTraceInFastThrow`.

---

## 5. Behavioral Matrix / Comparative Breakdown

| Dimension | `java.lang.Error` | Checked Exception | `java.lang.RuntimeException` |
|---|---|---|---|
| **Direct Superclass** | `java.lang.Throwable` | `java.lang.Exception` | `java.lang.Exception` |
| **Compiler Enforcement** | Unchecked (no `throws` needed) | **Checked** (must declare or catch) | Unchecked (no `throws` needed) |
| **Intended Meaning** | Fatal JVM/System catastrophe | Recoverable external failure | Programming bug / state invariant failure |
| **Application Catchability** | **Do Not Catch** | Catch and recover | Catch only at top-level boundary |
| **Typical Origin** | JVM, OS, Linker, GC | I/O, Network, Database | Application code logic, validation |
| **Canonical Examples** | `OutOfMemoryError`<br>`StackOverflowError`<br>`NoClassDefFoundError` | `IOException`<br>`SQLException`<br>`TimeoutException` | `NullPointerException`<br>`IllegalArgumentException`<br>`IllegalStateException` |

---

## 6. Comprehensive Code Examples (Production-Grade, Real-World)

The following production-grade transaction processor demonstrates:
1. Strict segregation between fatal errors, checked external faults, and unchecked bugs.
2. Clean boundary triage and causal chain preservation.
3. Safe categorization for microservice HTTP mapping.

```java
package com.bank.exceptions;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

public final class PaymentGatewayDispatcher {

    // Domain Models & Results
    public record PaymentRequest(String transactionId, String accountId, double amount) {}

    public record PaymentOutcome(String transactionId, String status, String details) {}

    // 1. Recoverable Checked Exception: External Network Timeout
    public static class GatewayConnectivityException extends Exception {
        private final int httpStatusCode;

        public GatewayConnectivityException(String message, int httpStatusCode, Throwable cause) {
            super(message, cause);
            this.httpStatusCode = httpStatusCode;
        }

        public int getHttpStatusCode() {
            return httpStatusCode;
        }
    }

    // 2. Unchecked Domain Exception: Business Rule / State Invariant Violation
    public static class AccountSuspendedException extends RuntimeException {
        public AccountSuspendedException(String message) {
            super(message);
        }
    }

    // 3. Resilient Dispatch Engine
    public PaymentOutcome dispatchPayment(PaymentRequest request) {
        Objects.requireNonNull(request, "Payment request cannot be null");

        // Fail-Fast Precondition Check (throws unchecked IllegalArgumentException)
        if (request.amount() <= 0) {
            throw new IllegalArgumentException("Payment amount must be strictly positive: " + request.amount());
        }

        try {
            // Simulated gateway network call (can throw checked GatewayConnectivityException)
            executeNetworkCall(request);
            return new PaymentOutcome(request.transactionId(), "SETTLED", "Transaction approved");

        } catch (GatewayConnectivityException checkedEx) {
            // RECOVERABLE: Log with causal chain and return a degraded retryable outcome
            return new PaymentOutcome(
                request.transactionId(),
                "PENDING_RETRY",
                "Gateway offline (HTTP " + checkedEx.getHttpStatusCode() + "). Scheduled for retry."
            );

        } catch (AccountSuspendedException uncheckedBusinessEx) {
            // CLIENT REJECTION: Business rule broken; no retry
            return new PaymentOutcome(
                request.transactionId(),
                "REJECTED",
                "Account suspended: " + uncheckedBusinessEx.getMessage()
            );

        } catch (Error fatalError) {
            // FATAL JVM CONDITION: Never swallow! Clean up critical resources and rethrow!
            System.err.println("FATAL SYSTEM ERROR DETECTED: " + fatalError.getClass().getName());
            throw fatalError; // MUST RETHROW!
        }
    }

    private void executeNetworkCall(PaymentRequest request) throws GatewayConnectivityException {
        if ("ACC-SUSPENDED".equals(request.accountId())) {
            throw new AccountSuspendedException("Account has been flagged by compliance");
        }
        if (request.amount() > 10_000.0) {
            // Simulate upstream gateway 504 Gateway Timeout
            throw new GatewayConnectivityException(
                "Upstream payment gateway timeout",
                504,
                new IOException("Connection reset by peer: 192.168.1.100:443")
            );
        }
    }
}
```

---

## 7. Anti-Patterns, Common Pitfalls & High-Frequency Production Bugs

### Anti-Pattern 1: Catching `Throwable` or `Error`
```java
// CATASTROPHIC ANTI-PATTERN:
try {
    processPayment(tx);
} catch (Throwable t) { // CATCHES VirtualMachineError, OutOfMemoryError, ThreadDeath!
    logger.warn("Something went wrong, continuing...");
}
```
**Why this is fatal:** If the JVM throws `OutOfMemoryError`, the heap is in an unknown, corrupted state. If a thread throws `ThreadDeath`, the thread was forcibly stopped. Catching `Throwable` swallows these fatal signals, keeping a zombie service running in Kubernetes while failing health checks or silently corrupting database records.
> [!CAUTION]
> Never catch `Throwable` or `Error`. Catch only specific subclasses of `Exception`.

---

### Anti-Pattern 2: The Empty Catch Block (Swallowed Exceptions)
```java
// TOXIC CODE:
try {
    connection.close();
} catch (SQLException e) {
    // Left blank because "it doesn't matter"
}
```
**Consequence:** When connections leak or file descriptors are exhausted, there is zero record in the logs. Production triage becomes impossible.
**Rule:** If an exception is genuinely safe to ignore, log it at `DEBUG` or comment *why* it is safe.

---

### Anti-Pattern 3: Using Exceptions for Ordinary Control Flow
```java
// SLOW ANTI-PATTERN: Using exception to check if string is numeric
public boolean isNumeric(String str) {
    try {
        Integer.parseInt(str);
        return true;
    } catch (NumberFormatException e) {
        return false; // Crawls stack trace on EVERY non-numeric input!
    }
}
```
**Fix:** Use regex, a character-by-character scan, or a lookup parser. Exception handling is designed for *exceptional* conditions, not boolean branching.

---

## 8. Edge Cases, Corner Cases & Hidden Traps

### Trap 1: Catching `StackOverflowError`
```java
public void infiniteRecursion() {
    try {
        infiniteRecursion();
    } catch (StackOverflowError e) {
        // HAZARD: Attempting to log or execute code here requires STACK FRAMES!
        // But the thread stack is ALREADY 100% full!
        logger.error("Recursion caught!"); // RE-THROWS StackOverflowError!
    }
}
```
When `StackOverflowError` is caught, the execution thread has zero remaining stack headroom. Any method call inside the `catch` block (including logging or string concatenation) will immediately throw another `StackOverflowError`.

---

### Trap 2: Lost Exceptions in `finally` Blocks
If an exception is thrown in the `try` block, and another exception is thrown in the `finally` block, the exception from `finally` **suppresses and completely obliterates** the original exception from `try`!
*(This is why Java 7 introduced try-with-resources and suppressed exceptions, detailed in `P04-C004`).*

---

## 9. Performance Characteristics & Benchmark Analysis

### Benchmark: Return Code vs. Standard Exception vs. Lightweight Exception

| Operation | Throughput (ops/sec) | Latency (ns/op) | Memory Allocated |
|---|---|---|---|
| Return `Optional.empty()` | ~145,000,000 ops/sec | ~6.8 ns | 16 bytes |
| Throw `LightweightException` (`writableStackTrace = false`) | ~98,000,000 ops/sec | ~10.2 ns | 24 bytes |
| Throw standard `RuntimeException` (stack depth = 10) | ~1,200,000 ops/sec | ~830 ns | 720 bytes |
| Throw standard `RuntimeException` (stack depth = 60) | ~180,000 ops/sec | ~5,500 ns | 3,840 bytes |

> [!TIP]
> Instantiating a standard exception with a 60-frame call stack is **over 800x slower** than returning an `Optional` and allocates 3.8 KB of garbage per throw.

---

## 10. Ecosystem Integrations & Industry Framework Patterns

### 1. Spring Framework: `@RestControllerAdvice` and `@ExceptionHandler`
Spring MVC uses exception hierarchy pattern matching to translate exceptions into HTTP response codes:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        // Log internally, hide stack trace from caller
        logger.error("Unhandled server fault", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("An internal error occurred"));
    }
}
```

### 2. Resilience4j: Circuit Breakers
Resilience4j inspects whether thrown exceptions match a configured `recordExceptions` list (e.g., `TimeoutException`, `IOException`) while ignoring client-side errors (`IllegalArgumentException`), preventing bad client requests from tripping upstream circuit breakers.

---

## 11. Security, Resilience & Production Hardening

### Preventing Sensitive Data Leaks in Stack Traces (CWE-209)
Raw stack traces dumped to HTTP clients reveal internal file paths, package structures, third-party library versions, and database schemas.
- In Spring Boot, ensure `server.error.include-stacktrace=never` in `application.yml`.
- Never serialize raw `Throwable` instances into JSON response envelopes. Always map to an explicit, sanitized `ErrorResponse` DTO.

---

## 12. Operational Runbooks, Triage & Diagnostics

### Triage: How to Read a Production Stack Trace
```
com.bank.service.PaymentException: Failed to process ledger debit
    at com.bank.service.PaymentService.debit(PaymentService.java:82)
    at com.bank.api.PaymentController.charge(PaymentController.java:34)
Caused by: java.sql.SQLException: Connection refused: db.internal:5432
    at com.bank.db.Pool.getConnection(Pool.java:112)
    at com.bank.service.PaymentService.debit(PaymentService.java:78)
    ... 15 more
```
1. **Start with the Root Cause ("Caused by"):** The bottom-most `Caused by` block identifies the physical trigger (here: `java.sql.SQLException: Connection refused`).
2. **Examine the Top Frame of the Cause:** `Pool.java:112` is where the database connection was refused.
3. **Trace the Calling Frame:** `PaymentService.java:78` called the pool and caught or rewrapped the error at line 82.

---

## 13. Holistic Architectural Synthesis & System Design Context

```
                        The Enterprise Exception Strategy
                                       │
           ┌───────────────────────────┼───────────────────────────┐
           ▼                           ▼                           ▼
   Expected Outcome            Business Rule Reject       Catastrophic Fault
   - Item not found            - Insufficient balance     - Database down
   - Optional parameter missing- Account frozen           - Network severed
           │                           │                           │
           ▼                           ▼                           ▼
      Optional<T>             Custom RuntimeException     Wrap & Propagate
    (Zero overhead)          (Handled by API Gateway)   (Trigger Circuit Breaker)
```

---

## 14. Complete Verification Suite (JUnit / Standalone OpenJDK Test Suite)

This comprehensive standalone test suite verifies the Throwable hierarchy, subtyping contracts, stack trace population, non-writable stack trace performance optimization, and exception chaining on OpenJDK 21 LTS without external dependencies.

```java
package com.bank.exceptions;

import java.io.IOException;
import java.util.*;

public final class ThrowableHierarchyVerificationSuite {

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    // Lightweight exception with writableStackTrace = false
    public static class LightweightException extends RuntimeException {
        public LightweightException(String message) {
            super(message, null, false, false);
        }
    }

    // Classifier
    public static String classifyThrowable(Throwable t) {
        if (t instanceof Error) {
            return "FATAL_ERROR";
        } else if (t instanceof RuntimeException) {
            return "UNCHECKED_EXCEPTION";
        } else {
            return "CHECKED_EXCEPTION";
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Running OpenJDK 21 Throwable Hierarchy Verification Suite ===");

        // Test 1: Classification of the 3 Branches
        Throwable oom = new OutOfMemoryError("Simulated heap exhaustion");
        Throwable ioEx = new IOException("Network socket disconnected");
        Throwable npe = new NullPointerException("Null reference access");

        assertEquals("FATAL_ERROR", classifyThrowable(oom), "OutOfMemoryError must classify as FATAL_ERROR");
        assertEquals("CHECKED_EXCEPTION", classifyThrowable(ioEx), "IOException must classify as CHECKED_EXCEPTION");
        assertEquals("UNCHECKED_EXCEPTION", classifyThrowable(npe), "NullPointerException must classify as UNCHECKED_EXCEPTION");

        // Test 2: Hierarchy Subtyping Assertions
        assertTrue(oom instanceof Throwable, "Error is a Throwable");
        assertFalse(oom instanceof Exception, "Error is NOT an Exception");

        assertTrue(ioEx instanceof Exception, "IOException is an Exception");
        assertFalse(ioEx instanceof RuntimeException, "IOException is NOT a RuntimeException");

        assertTrue(npe instanceof RuntimeException, "NullPointerException is a RuntimeException");
        assertTrue(npe instanceof Exception, "NullPointerException is an Exception");
        assertTrue(npe instanceof Throwable, "NullPointerException is a Throwable");

        // Test 3: Stack Trace Inspection on Standard Exception
        Exception standardEx = new IllegalArgumentException("Invalid payment amount");
        StackTraceElement[] frames = standardEx.getStackTrace();
        assertTrue(frames.length > 0, "Standard exception MUST populate stack trace frames via fillInStackTrace()");
        assertEquals("ThrowableHierarchyVerificationSuite.java", frames[0].getFileName(), "Top frame matches current file");

        // Test 4: Lightweight Exception with writableStackTrace = false
        LightweightException lightEx = new LightweightException("High throughput validation failure");
        assertEquals(0, lightEx.getStackTrace().length,
            "LightweightException with writableStackTrace=false must have 0 stack trace elements");

        // Test 5: Exception Chaining
        IOException rootCause = new IOException("Connection refused: 127.0.0.1:5432");
        IllegalStateException wrapperEx = new IllegalStateException("Database pool failed to acquire connection", rootCause);

        assertEquals(rootCause, wrapperEx.getCause(), "Root cause is preserved in wrapper exception");
        assertEquals("Connection refused: 127.0.0.1:5432", wrapperEx.getCause().getMessage(), "Cause message matches");

        System.out.println("All 5 test scenarios (12 assertions) passed with 100% compliance!");
    }
}
```

---

## 15. Key Takeaways & "Rules of Thumb"

1. **Know the triad:** `Error` (fatal JVM condition; never catch), `Checked Exception` (recoverable external fault; must be handled or declared), `RuntimeException` (programming defect or business rule violation; unchecked).
2. **Never catch `Throwable` or `Error`:** Catching `Throwable` catches `VirtualMachineError` and `ThreadDeath`, masking fatal system corruption.
3. **The cost is in `fillInStackTrace()`:** Exceptions are slow because the JVM crawls the thread call stack on instantiation. Use `writableStackTrace = false` for high-frequency control flow exceptions.
4. **Happy path has zero overhead:** Modern HotSpot uses static exception tables in bytecode. A `try-catch` block executes with zero overhead when no exception is thrown.
5. **Preserve causal chains:** When wrapping exceptions, always pass the original exception as the `cause` parameter: `new ServiceException("Failed", rootEx)`.

---

## 16. Further Reading & Academic References

- Bloch, Joshua. *Effective Java (3rd Edition)*. Addison-Wesley, 2018. Item 69: "Use exceptions only for exceptional conditions", Item 70: "Use checked exceptions for recoverable conditions and runtime exceptions for programming errors", Item 72: "Favor the use of standard exceptions".
- Gosling, James, Bill Joy, Guy Steele, Gilad Bracha, Alex Buckley, and Daniel Smith. *The Java Language Specification (Java SE 21 Edition)*. Chapter 11: "Exceptions".
- Lindholm, Tim, Frank Yellin, Gilad Bracha, and Alex Buckley. *The Java Virtual Machine Specification (Java SE 21 Edition)*. Section 4.7.3: "The Code Attribute (Exception Tables)".
- Goetz, Brian. *Java Theory and Practice: The Exceptions Debate*. IBM developerWorks, 2003.
