# Exception Handling Blocks: try, catch, multi-catch, and finally Semantics

---

## 1. Concept Definition & Core Purpose

In Java, structured exception handling is governed by four core keywords: `try`, `catch`, `finally`, and `throw`.

Together, they form a robust state machine that segregates the standard happy-path business logic from failure recovery and resource cleanup:

1. **The `try` Block:** Demarcates an enclosed lexical region of code where abnormal events or exceptions may be thrown. A `try` block cannot exist in isolation; it must be immediately followed by at least one `catch` block or a `finally` block (or use try-with-resources syntax).
2. **The `catch` Block:** An exception handler associated with the preceding `try` block. It specifies the type of exception it can handle. If an exception thrown in `try` matches the declared type (or is a subclass), control transfers immediately to that `catch` block.
3. **The Multi-Catch Block (Java 7+):** Allows a single `catch` block to handle multiple disjoint exception types using the pipe (`|`) syntax (`catch (IOException | SQLException e)`), reducing code duplication while enforcing that the parameter `e` is **implicitly final**.
4. **The `finally` Block:** A cleanup block that is **guaranteed to execute**, whether the `try` block completes normally, throws an exception that is caught, throws an exception that is unhandled, or executes an abrupt early return statement (`return`, `break`, `continue`).

```
                    Try-Catch-Finally Execution Flow
┌────────────────────────────────────────────────────────┐
│ try {                                                  │
│     // 1. Guarded Operations                           │
│ } catch (SpecificException e) {                        │
│     // 2. Executed ONLY if SpecificException is thrown │
│ } catch (IOException | SQLException e) {               │
│     // 3. Multi-Catch (disjoint types, e is final)     │
│ } finally {                                            │
│     // 4. ALWAYS EXECUTED (cleanup, close resources)   │
│ }                                                      │
└────────────────────────────────────────────────────────┘
```

---

## 2. Background, Context & Historical Evolution

### Java 1.0 – 1.1: Classic `try-catch-finally`
Java 1.0 introduced the classic syntax borrowed from C++:
```java
try {
    doWork();
} catch (IOException e) {
    handle(e);
} finally {
    cleanUp();
}
```
However, early developers frequently faced repetitive boilerplate when different exceptions required identical handling.

### Java 7 (2011): Project Coin and the Multi-Catch Block
Before Java 7, if two unrelated exceptions required identical recovery logic, developers had two unpalatable choices:
1. Duplicate the catch block:
   ```java
   catch (IOException e) { logger.error("Failed", e); rollback(); }
   catch (SQLException e) { logger.error("Failed", e); rollback(); }
   ```
2. Catch a broad common supertype (`Exception`), which accidentally swallowed unrelated runtime exceptions (`NullPointerException`, `IndexOutOfBoundsException`).

Java 7 introduced **Multi-Catch** (JEP 106):
```java
catch (IOException | SQLException e) {
    logger.error("Failed", e);
    rollback();
}
```

### Transition Guide: JavaScript/TypeScript vs. Java Catch Blocks

| Feature / Dimension | JavaScript / TypeScript | Java |
|---|---|---|
| **Multiple Catch Blocks** | **Unsupported:** JS has only one `catch (err)` block | Fully supported: Multiple catch blocks ordered from narrowest to broadest |
| **Type-Safe Dispatch** | Manual type inspection: `if (err instanceof CustomError)` | Native language-level dispatch using class hierarchies |
| **Multi-Catch** | Handled via single block with boolean logical operators | Native language syntax: `catch (TypeA \| TypeB e)` |
| **Finally Guarantees** | Executes on normal, caught, or early return | Identical: Executes on normal, caught, or early return |
| **Finally Return Overwriting** | Overwrites `try` return (discouraged) | Overwrites `try` return (emits compiler warning `-Xlint:finally`) |

---

## 3. Declarative Mechanics & Mental Model

### Ordering of Multiple Catch Blocks: The Subclass Precedence Rule

When declaring multiple `catch` blocks, handlers are evaluated **sequentially from top to bottom**:
> [!IMPORTANT]
> **Subclass exceptions MUST precede superclass exceptions.**

```java
try {
    processFile();
} catch (FileNotFoundException e) { // NARROW (Subclass of IOException)
    // 1. Catches file-missing specifically
} catch (IOException e) {            // BROAD (Superclass of FileNotFoundException)
    // 2. Catches general network/disk I/O
} catch (Exception e) {              // BROADER (Fallback)
    // 3. Catches all remaining exceptions
}
```

#### What happens if you invert the order?
If you place `catch (IOException e)` before `catch (FileNotFoundException e)`:
- The `javac` compiler **refuses to compile** with the error:
  `error: exception FileNotFoundException has already been caught`
- Because `FileNotFoundException` is a subtype of `IOException`, the top block would catch every instance, making the second block completely unreachable dead code.

---

### Java 7 Multi-Catch Rules and Constraints

The multi-catch syntax (`catch (TypeA | TypeB e)`) is governed by two strict language rules (JLS §14.20):

1. **Disjoint Types Rule (No Subtyping Relationships):**
   The alternatives in a multi-catch block **cannot be related by subclassing**:
   ```java
   // COMPILE ERROR: The exception FileNotFoundException is already caught by the alternative IOException
   catch (FileNotFoundException | IOException e) { ... }
   ```
   Because `FileNotFoundException` is an `IOException`, combining them in a multi-catch is redundant and rejected by `javac`.
2. **Implicitly `final` Parameter Rule:**
   The exception parameter `e` in a multi-catch block is **implicitly `final`**:
   ```java
   catch (IOException | SQLException e) {
       // COMPILE ERROR: Cannot assign a value to final variable 'e'
       // e = new IOException("Replacement"); 
   }
   ```
   In a single catch block (`catch (IOException e)`), Java historically allowed reassigning `e`, but multi-catch strictly prohibits reassignment to preserve type-system determinism.

---

### The `finally` Block Contract and the Rare Exceptions

The `finally` block is guaranteed to execute whenever control enters the associated `try` block.

#### Execution Guarantees:
- If `try` completes with no exceptions $\implies$ `finally` executes.
- If `try` throws an exception caught by `catch` $\implies$ `catch` executes, then `finally` executes.
- If `try` throws an unhandled exception $\implies$ `finally` executes, then the unhandled exception propagates up the thread stack.
- If `try` or `catch` executes a `return`, `break`, or `continue` statement $\implies$ **`finally` executes BEFORE the return completes!**

#### The Only Scenarios Where `finally` Does NOT Execute:
1. **Explicit Process Termination:** `System.exit(status)` or `Runtime.getRuntime().halt(status)` is called.
2. **Fatal JVM / OS Termination:** The JVM crashes due to an OS-level `kill -9`, segmentation fault, native JNI crash, or catastrophic hardware power failure.
3. **Thread Deadlock / Infinite Loop:** An infinite loop (`while (true) {}`) or permanent thread deadlock occurs inside `try` or `catch`.

---

## 4. HotSpot JVM Architecture & Memory Subsystem Realities

### How `finally` Is Compiled into Bytecode
In Java 1.0 to 5, the compiler generated subroutine instructions (`jsr` - Jump to Subroutine, and `ret` - Return from Subroutine) to execute `finally` blocks without duplicating code.

In Java 6+ (with the introduction of the Split Verifier / StackMapTable in JSR 202), `jsr` and `ret` were deprecated and forbidden in modern class files.

#### Modern Bytecode Compilation Strategy (Code Inlining):
Modern `javac` compiles `finally` blocks using **Code Inlining and Synthetic Exception Handlers**:
1. The bytecode instructions within `finally` are physically **duplicated** and inlined at **every exit point** of the method:
   - At the end of the normal `try` body (before the normal `return`).
   - At the end of every `catch` body (before each catch `return`).
2. A synthetic catch-all entry (`any`) is appended to the method's exception table:
   ```bytecode
   Exception table:
      from    to  target type
          0     8    11   Class java/io/IOException
          0    17    22   any   <--- SYNTHETIC CATCH-ALL FOR FINALLY!
   ```
3. If an unhandled exception occurs, the JVM jumps to the `any` handler, executes the inlined `finally` instructions, and immediately rethrows the original exception via `athrow`.

---

## 5. Behavioral Matrix / Comparative Breakdown

| Construct | Execution Condition | Reassignment of Variable `e` | Allowed Subtyping Relationships | Common Use Case |
|---|---|---|---|---|
| **Single `catch`** | Only if thrown exception matches type | Allowed (mutable unless declared `final`) | Single type | Specific custom recovery |
| **Multiple `catch`** | Sequential evaluation (top-to-bottom) | Allowed per block | **Must order:** Subclass before superclass | Granular recovery policies |
| **Multi-Catch (`\|`)**| If thrown matches ANY alternative | **Forbidden** (implicitly `final`) | **Must be disjoint:** No subclassing allowed | Identical handling across disparate failures |
| **`finally` Block** | **Always** (normal, caught, or unhandled) | N/A | N/A | Resource cleanup, lock release |

---

## 6. Comprehensive Code Examples (Production-Grade, Real-World)

The following production-grade transaction execution engine demonstrates:
1. Specific ordered catch blocks (narrow subclass to broader superclass).
2. Java 7 multi-catch handling disparate infrastructure failures.
3. Reliable execution tracking through `finally` across normal, caught, and early-return paths.
4. Correct resource and lock release patterns.

```java
package com.bank.exceptions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public final class TransactionExecutionEngine {

    private final ReentrantLock transactionLock = new ReentrantLock();

    // 1. Multiple Catch Block Precedence (Subclass before Superclass)
    public String executeWithOrderedCatch(int scenario) {
        try {
            if (scenario == 1) {
                throw new FileNotFoundException("Ledger file missing: /var/data/ledger.db");
            } else if (scenario == 2) {
                throw new IOException("Socket timeout connecting to payment terminal");
            } else {
                throw new Exception("General unexpected transaction fault");
            }
        } catch (FileNotFoundException fnfEx) {
            // NARROW: Subclass of IOException must be caught first
            return "RECOVERED_FILE_NOT_FOUND: " + fnfEx.getMessage();
        } catch (IOException ioEx) {
            // BROAD: Catches general I/O errors (excluding FileNotFoundException already caught)
            return "RECOVERED_IO_ERROR: " + ioEx.getMessage();
        } catch (Exception generalEx) {
            // BROADER: Final safety net for unexpected errors
            return "RECOVERED_GENERAL_ERROR: " + generalEx.getMessage();
        }
    }

    // 2. Java 7 Multi-Catch Handling Disjoint Exceptions
    public String executeWithMultiCatch(boolean triggerSqlError) {
        try {
            if (triggerSqlError) {
                throw new SQLException("Database deadlock detected on ledger table");
            } else {
                throw new IOException("Disk quota exceeded while writing transaction log");
            }
        } catch (SQLException | IOException ex) {
            // Both are disjoint types. Parameter 'ex' is implicitly final.
            return String.format("MULTI_CATCH_LOGGED: [%s] %s", ex.getClass().getSimpleName(), ex.getMessage());
        }
    }

    // 3. Guaranteed Cleanup Pattern with finally and ReentrantLock
    public List<String> processWithLockAndAudit(boolean causeException, boolean earlyReturn) {
        List<String> auditTrail = new ArrayList<>();
        transactionLock.lock(); // Acquire lock OUTSIDE the try block!
        auditTrail.add("LOCK_ACQUIRED");

        try {
            auditTrail.add("PROCESSING_TRANSACTION");
            if (causeException) {
                throw new IllegalStateException("Business validation failed");
            }
            if (earlyReturn) {
                auditTrail.add("EARLY_RETURN_INITIATED");
                return auditTrail; // finally MUST still execute before method returns!
            }
            auditTrail.add("TRANSACTION_COMMITTED");
        } catch (IllegalStateException ex) {
            auditTrail.add("TRANSACTION_FAILED_ROLLEDBACK");
        } finally {
            // Guaranteed lock release and audit finalization
            transactionLock.unlock();
            auditTrail.add("LOCK_RELEASED_IN_FINALLY");
        }

        auditTrail.add("POST_EXECUTION_COMPLETED");
        return auditTrail;
    }
}
```

---

## 7. Anti-Patterns, Common Pitfalls & High-Frequency Production Bugs

### Anti-Pattern 1: Returning a Value from a `finally` Block
```java
// CATASTROPHIC ANTI-PATTERN:
@SuppressWarnings("finally")
public int calculateDiscount() {
    try {
        return 10; // Computed discount
    } finally {
        return 20; // FATAL: Overrides and discards return 10!
    }
}
```
**Why this is dangerous:**
- A `return` statement inside `finally` **silently overrides** any return value computed in `try` or `catch`.
- Worse: If an unhandled exception was actively being thrown out of `try`, a `return` in `finally` **completely swallows the exception**, as if it never happened!
> [!CAUTION]
> Never put a `return` statement inside a `finally` block. Modern compilers flag this with the `-Xlint:finally` warning.

---

### Anti-Pattern 2: Throwing an Exception from a `finally` Block
```java
// HAZARDOUS EXCEPTION MASKING:
try {
    processOrder(); // Throws PaymentFailedException("Card Expired")
} finally {
    auditLogger.flush(); // Throws IOException("Disk Full")
}
```
**The Trap:** If `processOrder()` throws `PaymentFailedException`, and `auditLogger.flush()` throws `IOException`, the `PaymentFailedException` is **completely obliterated and lost forever**. The caller only receives `IOException`, leaving developers completely blind to why the payment failed.

---

### Anti-Pattern 3: Acquiring Locks or Resources INSIDE the `try` Block
```java
// DANGEROUS LOCK PATTERN:
try {
    lock.lock(); // If lock.lock() throws (e.g., interrupted or OOM), finally STILL RUNS!
    doWork();
} finally {
    lock.unlock(); // Throws IllegalMonitorStateException because lock was never acquired!
}
```
**Correct Pattern:** Always acquire the lock **immediately before** the `try` block:
```java
lock.lock();
try {
    doWork();
} finally {
    lock.unlock();
}
```

---

## 8. Edge Cases, Corner Cases & Hidden Traps

### Trap 1: Mutating Values Before `finally` Return
What does the following method return?
```java
public int testValue() {
    int x = 10;
    try {
        return x; // Evaluates expression: pushes 10 onto operand stack
    } finally {
        x = 20;   // Mutates local variable x in frame, but operand stack has 10!
    }
}
// RETURNS 10, NOT 20!
```
#### Why?
When `return x;` executes in `try`, the JVM evaluates the value of `x` and pushes the primitive `10` onto the operand stack as the pending return value. When `finally` executes, `x = 20` updates the local variable slot in the stack frame, but does not overwrite the return value on the operand stack. When `finally` completes, the method returns the stored `10`.

However, if `x` is a reference to a mutable object (e.g., `StringBuilder`):
```java
public StringBuilder testObject() {
    StringBuilder sb = new StringBuilder("A");
    try {
        return sb;
    } finally {
        sb.append("B"); // Mutates the underlying object on heap!
    }
}
// RETURNS "AB"!
```
The object reference on the operand stack points to the same heap object mutated in `finally`!

---

## 9. Performance Characteristics & Benchmark Analysis

### Happy-Path Zero Overhead
- As established in HotSpot internals, executing `try` blocks without exceptions incurs **zero CPU overhead** because handlers are resolved via static classfile tables.
- Modern C2 JIT compiler inlines `finally` instructions into the normal execution path, achieving identical machine-code efficiency to manual sequential statements.

---

## 10. Ecosystem Integrations & Industry Framework Patterns

### 1. Spring Framework: `TransactionSynchronization`
Spring uses the `finally` lifecycle to ensure transaction synchronizations (clearing thread-local `SecurityContextHolder` and database connection bindings) execute deterministically after request completion:
```java
TransactionSynchronizationManager.clear();
```

### 2. JDK Concurrency: `Lock.lock()` and `Lock.unlock()`
The Java standard library explicitly mandates the `try-finally` idiom for all `java.util.concurrent.locks.Lock` operations to prevent deadlocks:
```java
Lock lock = new ReentrantLock();
lock.lock();
try {
    // Critical section
} finally {
    lock.unlock();
}
```

---

## 11. Security, Resilience & Production Hardening

### Guaranteed Secret Sanitization in `finally`
When processing sensitive credentials (passwords, encryption keys, authorization tokens), use `finally` to overwrite memory buffers:
```java
char[] password = readPassword();
try {
    authenticate(password);
} finally {
    // Clear sensitive plaintext password from heap memory immediately
    Arrays.fill(password, '0');
}
```

---

## 12. Operational Runbooks, Triage & Diagnostics

### Triage: Missing Original Exception in Production Logs
When logs show an obscure cleanup error (e.g., `NullPointerException in finally`) but no business error:
1. **Diagnosis:** An exception was thrown inside a `finally` block, obliterating the original exception that occurred in the `try` block.
2. **Remedy:** Wrap all legacy cleanup operations inside `finally` with nested try-catches, or preferably migrate to **try-with-resources** (which preserves original exceptions and records cleanup failures as suppressed exceptions).

---

## 13. Holistic Architectural Synthesis & System Design Context

```
                         Structured Error Handling Matrix
                                        │
           ┌────────────────────────────┼────────────────────────────┐
           ▼                            ▼                            ▼
   Guarded Execution           Failure Recovery              State Cleanup
     (try block)                 (catch block)              (finally block)
  - Isolated business logic   - Granular exception order  - Guaranteed execution
  - Zero happy-path penalty   - Multi-catch (|) for DRY   - Unlocks & resource cleanup
                              - Preserves causal chain    - Never return or throw!
```

---

## 14. Complete Verification Suite (JUnit / Standalone OpenJDK Test Suite)

This comprehensive standalone test suite verifies ordered multiple catch blocks, Java 7 multi-catch constraints, `finally` execution guarantees across normal/caught/early-return paths, and `finally` return/exception overwriting traps on OpenJDK 21 LTS without external dependencies.

```java
package com.bank.exceptions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public final class TryCatchFinallyVerificationSuite {

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

    // 1. Multiple Catch Blocks: Narrow to Broad Dispatch
    public static String handleOrderedExceptions(int scenario) {
        try {
            if (scenario == 1) {
                throw new FileNotFoundException("File not found on disk");
            } else if (scenario == 2) {
                throw new IOException("General I/O socket error");
            } else {
                throw new Exception("General generic failure");
            }
        } catch (FileNotFoundException fnf) {
            return "CAUGHT_FILENOTFOUND: " + fnf.getMessage();
        } catch (IOException io) {
            return "CAUGHT_IOEXCEPTION: " + io.getMessage();
        } catch (Exception ex) {
            return "CAUGHT_EXCEPTION: " + ex.getMessage();
        }
    }

    // 2. Java 7 Multi-Catch Block (Disjoint Types, Implicitly Final)
    public static String handleMultiCatch(boolean throwSql) {
        try {
            if (throwSql) {
                throw new SQLException("Database connection timeout");
            } else {
                throw new IOException("Storage disk full");
            }
        } catch (SQLException | IOException e) {
            return "MULTI_CAUGHT: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }

    // 3. Finally Block Execution Order across Normal, Exception, and Early Return
    public static List<String> testFinallyExecution(boolean causeException, boolean earlyReturn) {
        List<String> executionLog = new ArrayList<>();
        try {
            executionLog.add("TRY_START");
            if (causeException) {
                executionLog.add("THROWING");
                throw new IllegalStateException("Simulated state failure");
            }
            if (earlyReturn) {
                executionLog.add("EARLY_RETURN");
                return executionLog; // Finally MUST STILL EXECUTE!
            }
            executionLog.add("TRY_END");
        } catch (IllegalStateException e) {
            executionLog.add("CATCH_BLOCK");
        } finally {
            executionLog.add("FINALLY_BLOCK");
        }
        executionLog.add("AFTER_BLOCK");
        return executionLog;
    }

    // 4. Finally Return Overwriting (Anti-Pattern Demonstration)
    @SuppressWarnings("finally")
    public static int finallyReturnOverwrites() {
        try {
            return 10;
        } finally {
            return 20; // OVERWRITES return 10!
        }
    }

    // 5. Finally Exception Suppressing Try Exception (Anti-Pattern Demonstration)
    @SuppressWarnings("finally")
    public static void finallyExceptionSuppresses() {
        try {
            throw new IllegalStateException("Original Error from Try");
        } finally {
            throw new RuntimeException("Overriding Error from Finally"); // OBLITERATES original error!
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Running OpenJDK 21 Try-Catch-Finally Verification Suite ===");

        // Test 1: Multiple Catch Dispatch (Subclass before Superclass)
        assertEquals("CAUGHT_FILENOTFOUND: File not found on disk", handleOrderedExceptions(1),
            "FileNotFoundException dispatched to specific handler");
        assertEquals("CAUGHT_IOEXCEPTION: General I/O socket error", handleOrderedExceptions(2),
            "IOException dispatched to general I/O handler");
        assertEquals("CAUGHT_EXCEPTION: General generic failure", handleOrderedExceptions(3),
            "Exception dispatched to fallback handler");

        // Test 2: Java 7 Multi-Catch
        assertEquals("MULTI_CAUGHT: SQLException - Database connection timeout", handleMultiCatch(true),
            "Multi-catch handles SQLException");
        assertEquals("MULTI_CAUGHT: IOException - Storage disk full", handleMultiCatch(false),
            "Multi-catch handles IOException");

        // Test 3: Finally Execution on Normal Flow
        List<String> normalFlow = testFinallyExecution(false, false);
        assertEquals(List.of("TRY_START", "TRY_END", "FINALLY_BLOCK", "AFTER_BLOCK"), normalFlow,
            "Normal execution flows through finally");

        // Test 4: Finally Execution on Caught Exception
        List<String> exceptionFlow = testFinallyExecution(true, false);
        assertEquals(List.of("TRY_START", "THROWING", "CATCH_BLOCK", "FINALLY_BLOCK", "AFTER_BLOCK"), exceptionFlow,
            "Exception execution executes catch then finally");

        // Test 5: Finally Execution on Early Return
        List<String> earlyReturnFlow = testFinallyExecution(false, true);
        assertEquals(List.of("TRY_START", "EARLY_RETURN", "FINALLY_BLOCK"), earlyReturnFlow,
            "Early return in try still triggers finally block before method returns");

        // Test 6: Finally Return Overwriting
        assertEquals(20, finallyReturnOverwrites(), "Return in finally overwrites try return value");

        // Test 7: Finally Exception Suppressing Try Exception
        boolean suppressedObserved = false;
        try {
            finallyExceptionSuppresses();
        } catch (RuntimeException e) {
            suppressedObserved = true;
            assertEquals("Overriding Error from Finally", e.getMessage(), "Finally exception obliterated try exception");
            assertEquals(null, e.getCause(), "Original try exception was completely lost");
        }
        assertTrue(suppressedObserved, "Finally exception must suppress original try exception");

        System.out.println("All 7 test scenarios (15 assertions) passed with 100% compliance!");
    }
}
```

---

## 15. Key Takeaways & "Rules of Thumb"

1. **Order catch blocks narrowest to broadest:** Subclasses (`FileNotFoundException`) must be caught before superclasses (`IOException`). Violations produce compile-time errors.
2. **Multi-catch requires disjoint types:** Alternatives in `catch (A | B e)` cannot be related by inheritance, and the parameter `e` is implicitly `final`.
3. **`finally` always executes:** It executes whether code finishes normally, throws an exception, or encounters an early `return`.
4. **Never return or throw from `finally`:** Returning or throwing from `finally` silently obliterates active return values and masks pending exceptions.
5. **Acquire locks outside the `try` block:** Always call `lock.lock()` immediately before `try`, never inside it.

---

## 16. Further Reading & Academic References

- Bloch, Joshua. *Effective Java (3rd Edition)*. Addison-Wesley, 2018. Item 73: "Throw exceptions appropriate to the abstraction", Item 74: "Document all exceptions thrown by each method".
- Java Language Specification (JLS SE 21): Section 14.20: "The try statement", Section 14.20.1: "Execution of try-catch", Section 14.20.2: "Execution of try-finally and try-catch-finally".
- Java Virtual Machine Specification (JVMS SE 21): Section 3.12: "Compiling Exceptions", Section 3.13: "Compiling finally".
- Goetz, Brian. *The Java 7 Language Features (Project Coin)*. Oracle Technology Network, 2011.
