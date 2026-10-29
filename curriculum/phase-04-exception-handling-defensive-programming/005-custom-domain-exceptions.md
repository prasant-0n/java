# Custom Domain Exceptions, Error Envelopes, and Context Propagation

---

## 1. Concept Definition & Core Purpose

In enterprise backend architectures, relying exclusively on generic standard exceptions (such as `RuntimeException`, `IllegalArgumentException`, or `IllegalStateException`) creates severe maintainability and operational hazards:
- They lack typed business semantics.
- Callers cannot distinguish between a malformed request body and an internal business invariant violation without fragile string parsing (`if (e.getMessage().contains("balance"))`).
- They omit critical diagnostic context (account IDs, correlation trace IDs, tenant IDs, shortfalls, timestamps).

**Custom Domain Exceptions** are typed, domain-specific exception classes that model discrete business failure modes within a bounded context.

When integrated with **Error Envelopes** (such as RFC 7807 / RFC 9457 *Problem Details for HTTP APIs*), custom domain exceptions provide:
1. **Typed Programmatic Handling:** Upstream handlers, circuit breakers, and retry engines catch specific typed classes rather than generic catches.
2. **Contextual Enrichment:** Structured business metadata (account ID, attempted amount, shortfall, tenant ID, trace ID) is encapsulated as strongly-typed fields.
3. **Seamless API Boundary Translation:** Centralized exception handlers (e.g., Spring `@RestControllerAdvice`) transform typed domain exceptions directly into standardized, secure HTTP JSON error envelopes without exposing internal stack traces.

```
                  Domain Exception to API Error Envelope
┌──────────────────────────────────────────────┐
│ InsufficientFundsException (Domain Layer)    │
│ - errorCode: INSUFFICIENT_FUNDS              │
│ - accountId: "ACC-101"                       │
│ - availableBalance: 150.00                   │
│ - attemptedAmount: 500.00                    │
│ - traceId: "TRC-987654"                      │
└──────────────────────┬───────────────────────┘
                       │ Handled by @RestControllerAdvice
                       ▼
┌──────────────────────────────────────────────┐
│ HTTP 422 Unprocessable Content (RFC 7807)    │
│ {                                            │
│   "type": "https://api.bank.com/errors/funds"│
│   "title": "Insufficient Account Balance",   │
│   "status": 422,                             │
│   "detail": "Account ACC-101 shortfall 350", │
│   "instance": "/api/v1/accounts/101/transfer"│
│   "traceId": "TRC-987654",                   │
│   "shortfall": 350.00                        │
│ }                                            │
└──────────────────────────────────────────────┘
```

---

## 2. Background, Context & Historical Evolution

### Java 1.0 – J2EE: Unstructured String Error Messages
In early enterprise Java, exceptions carried only a raw string: `new Exception("Error in account " + id)`.
When API layers needed to return localized messages or HTTP status codes, developers wrote fragile string-matching code:
```java
if (e.getMessage().startsWith("User not found")) {
    return 404;
}
```
If a developer fixed a typo or reworded the error message, the API status code silently flipped from 404 to 500, breaking frontend clients.

### The Rise of Error Codes and Base Domain Hierarchies (2000s)
Enterprise frameworks established root domain exceptions (`BaseDomainException extends RuntimeException`) carrying standardized machine-readable error code enums (`ErrorCode.ACCOUNT_NOT_FOUND`).

### RFC 7807 (2016) & RFC 9457 (2023): Standardized Problem Details
The Internet Engineering Task Force (IETF) published RFC 7807 (updated by RFC 9457) defining a standard JSON format for HTTP API errors:
- `type`: A URI reference identifying the error type.
- `title`: A short, human-readable summary of the problem type.
- `status`: The HTTP status code.
- `detail`: A human-readable explanation specific to this occurrence.
- `instance`: A URI reference identifying the specific occurrence of the problem.
In Java 17 and Spring Boot 3+, RFC 7807 was adopted natively as `org.springframework.http.ProblemDetail`.

---

## 3. Declarative Mechanics & Mental Model

### The 4 Standard Constructors Rule (Joshua Bloch)

Whenever you define a custom exception class, **always provide the four standard `Throwable` constructors** (unless you deliberately design a specialized constructor):

```java
public class CustomDomainException extends RuntimeException {

    // 1. Message only
    public CustomDomainException(String message) {
        super(message);
    }

    // 2. Message and Root Cause
    public CustomDomainException(String message, Throwable cause) {
        super(message, cause);
    }

    // 3. Root Cause only (message defaults to cause.toString())
    public CustomDomainException(Throwable cause) {
        super(cause);
    }

    // 4. Advanced: Message, Cause, Suppression, and Writable Stack Trace
    protected CustomDomainException(String message, Throwable cause,
                                    boolean enableSuppression,
                                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
```

---

### Designing the Base Domain Exception Hierarchy

In clean backend architectures, all domain exceptions inherit from a common base class:

```
                            java.lang.RuntimeException
                                         ▲
                                         │
                             BaseDomainException (Abstract)
                             - ErrorCode errorCode
                             - String traceId
                             - Instant timestamp
                             - Map<String, Object> context
                                         ▲
                 ┌───────────────────────┴───────────────────────┐
                 │                                               │
     EntityNotFoundException                         BusinessRuleViolationException
     - String entityName                             - double shortfall
     - String entityId                               - String ruleId
```

#### Why a Common Base?
1. **Centralized Catching:** Framework handlers can catch `BaseDomainException` and apply universal logic (audit logging, metrics incrementation, trace ID injection).
2. **Context Enrichment:** The base class provides a fluent `.withContext(key, value)` builder to attach debugging context without polluting constructor signatures.

---

## 4. HotSpot JVM Architecture & Memory Subsystem Realities

### Object Allocation and Field Layout in Custom Exceptions
When you add fields to a custom exception:
```java
public class InsufficientFundsException extends BaseDomainException {
    private final double availableBalance;  // 8 bytes
    private final double attemptedAmount;   // 8 bytes
}
```
- In HotSpot 64-bit with Compressed Oops, an ordinary `RuntimeException` occupies 32–40 bytes of heap (header + message reference + cause reference + stack trace array reference).
- Adding custom fields increases the heap footprint by 16–32 bytes.
- Because domain exceptions are thrown only on failure branches (exceptional paths), this minor heap allocation is negligible compared to the native overhead of `fillInStackTrace()`.
- **Heap Immutability:** Fields on custom domain exceptions should **always be `final`** to guarantee thread-safe read access when passed across asynchronous executor boundaries or logging threads.

---

## 5. Behavioral Matrix / Comparative Breakdown

| Approach | Machine Readability | Programmatic Inspectability | Maintenance Overhead | Standard Compliance |
|---|---|---|---|---|
| **Raw Standard Exception** (`IllegalArgumentException`)| None (unstructured string) | None (requires string parsing) | Very Low | Poor |
| **Enum Error Code on Exception** | High (`e.getErrorCode()`) | Medium | Low | Internal Only |
| **Typed Domain Hierarchy with Context**| **Very High** (typed fields + context map) | **Complete** (`e.getAvailableBalance()`) | Moderate | Clean Architecture |
| **RFC 7807 Problem Details Envelope**| **Maximum** (standardized JSON keys) | **Universal** across frontend/microservices | Low (centralized mapper)| IETF RFC 7807 / 9457 |

---

## 6. Comprehensive Code Examples (Production-Grade, Real-World)

The following production-grade implementation demonstrates:
1. Standardized business error code enumeration.
2. Base abstract domain exception with context builder.
3. Strongly-typed concrete business exceptions.
4. Translation to RFC 7807 / RFC 9457 Problem Details envelopes.

```java
package com.bank.exceptions;

import java.net.URI;
import java.time.Instant;
import java.util.*;

public final class DomainExceptionArchitecture {

    // 1. Standardized Domain Error Code Enum
    public enum ErrorCode {
        ACCOUNT_NOT_FOUND(404, "Account Not Found"),
        INSUFFICIENT_FUNDS(422, "Insufficient Account Balance"),
        ACCOUNT_LOCKED(423, "Account Locked"),
        TRANSACTION_LIMIT_EXCEEDED(400, "Transaction Limit Exceeded"),
        INTERNAL_LEDGER_FAULT(500, "Internal Ledger Fault");

        private final int httpStatus;
        private final String defaultTitle;

        ErrorCode(int httpStatus, String defaultTitle) {
            this.httpStatus = httpStatus;
            this.defaultTitle = defaultTitle;
        }

        public int getHttpStatus() { return httpStatus; }
        public String getDefaultTitle() { return defaultTitle; }
    }

    // 2. Base Abstract Domain Exception
    public static abstract class BaseDomainException extends RuntimeException {
        private final ErrorCode errorCode;
        private final String traceId;
        private final Instant timestamp;
        private final Map<String, Object> context;

        protected BaseDomainException(ErrorCode errorCode, String traceId, String message) {
            this(errorCode, traceId, message, null);
        }

        protected BaseDomainException(ErrorCode errorCode, String traceId, String message, Throwable cause) {
            super(message, cause);
            this.errorCode = Objects.requireNonNull(errorCode, "ErrorCode cannot be null");
            this.traceId = Objects.requireNonNull(traceId, "TraceId cannot be null");
            this.timestamp = Instant.now();
            this.context = new LinkedHashMap<>();
        }

        public ErrorCode getErrorCode() { return errorCode; }
        public String getTraceId() { return traceId; }
        public Instant getTimestamp() { return timestamp; }
        public Map<String, Object> getContext() { return Collections.unmodifiableMap(context); }

        public BaseDomainException withContext(String key, Object value) {
            this.context.put(Objects.requireNonNull(key), value);
            return this;
        }
    }

    // 3. Strongly-Typed Concrete Domain Exception
    public static final class InsufficientFundsException extends BaseDomainException {
        private final double availableBalance;
        private final double attemptedAmount;

        public InsufficientFundsException(String traceId, String accountId, double availableBalance, double attemptedAmount) {
            super(
                ErrorCode.INSUFFICIENT_FUNDS,
                traceId,
                String.format("Account %s has balance %.2f but attempted debit of %.2f", accountId, availableBalance, attemptedAmount)
            );
            this.availableBalance = availableBalance;
            this.attemptedAmount = attemptedAmount;
            withContext("accountId", accountId);
            withContext("availableBalance", availableBalance);
            withContext("attemptedAmount", attemptedAmount);
            withContext("shortfall", attemptedAmount - availableBalance);
        }

        public double getAvailableBalance() { return availableBalance; }
        public double getAttemptedAmount() { return attemptedAmount; }
    }

    // 4. RFC 7807 / RFC 9457 Problem Details Envelope
    public record ProblemDetails(
        URI type,
        String title,
        int status,
        String detail,
        String instance,
        String traceId,
        Map<String, Object> parameters
    ) {
        public static ProblemDetails fromDomainException(BaseDomainException ex, String requestPath) {
            return new ProblemDetails(
                URI.create("https://api.bank.com/errors/" + ex.getErrorCode().name().toLowerCase()),
                ex.getErrorCode().getDefaultTitle(),
                ex.getErrorCode().getHttpStatus(),
                ex.getMessage(),
                requestPath,
                ex.getTraceId(),
                ex.getContext()
            );
        }
    }
}
```

---

## 7. Anti-Patterns, Common Pitfalls & High-Frequency Production Bugs

### Anti-Pattern 1: The "Exception Explosion" Anti-Pattern
Creating an individual exception class for every single validation failure:
- `InvalidUsernameLengthException`
- `InvalidUsernameCharactersException`
- `UsernameStartsWithNumberException`
**Consequence:** Creates hundreds of near-empty class files with zero unique logic, bloating the codebase and ClassLoader Metaspace.
**Fix:** Consolidate related validation failures under a single `ValidationException` containing a list of `ValidationError` field records:
```java
public class ValidationException extends BaseDomainException {
    private final List<FieldError> fieldErrors;
}
```

---

### Anti-Pattern 2: String Parsing to Inspect Error Conditions
```java
// FRAGILE CODE:
catch (BaseDomainException e) {
    if (e.getMessage().contains("Insufficient")) { ... } // BROKEN if message is reworded!
}
```
**Fix:** Always query typed error codes or use Java 21 pattern matching:
```java
catch (BaseDomainException e) {
    if (e.getErrorCode() == ErrorCode.INSUFFICIENT_FUNDS) { ... }
}
```

---

### Anti-Pattern 3: Omitting the Cause Constructor
```java
// BROKEN CONSTRUCTOR:
public class OrderPersistenceException extends RuntimeException {
    public OrderPersistenceException(String message) {
        super(message); // Cause is OMITTED!
    }
}

// Caller:
try {
    dao.insert(order);
} catch (SQLException e) {
    throw new OrderPersistenceException("Failed: " + e.getMessage()); // Root cause lost!
}
```
**Consequence:** The original `SQLException` and its line-numbered stack trace are permanently discarded, destroying root cause analysis. Always support and pass the `Throwable cause` constructor!

---

## 8. Edge Cases, Corner Cases & Hidden Traps

### Trap 1: Mutable Context Leaks
If your custom exception stores a mutable map or list in its context:
```java
private final Map<String, Object> context = new HashMap<>();
public Map<String, Object> getContext() { return context; } // LEAK!
```
External callers or logging interceptors can modify the exception's internal state. Always wrap context maps with `Collections.unmodifiableMap(...)` or `Map.copyOf(...)`.

---

## 9. Performance Characteristics & Benchmark Analysis

### Memory Footprint of Custom Exceptions
- Base `Throwable`: 32 bytes (64-bit JVM Compressed OOPs).
- Custom `BaseDomainException` (+4 object references: `ErrorCode`, `String traceId`, `Instant`, `Map`): +32 bytes.
- Total heap allocation: **~64–96 bytes**.
- Compared to the 3.8 KB allocated by `fillInStackTrace()` for a deep thread stack, adding typed fields accounts for less than **2%** of the exception's total instantiation cost.

---

## 10. Ecosystem Integrations & Industry Framework Patterns

### 1. Spring Boot 3+ Native Problem Details
Spring Boot 3 natively implements RFC 7807 via `ProblemDetail`:
```java
@ExceptionHandler(InsufficientFundsException.class)
public ProblemDetail handleInsufficientFunds(InsufficientFundsException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    problem.setTitle("Insufficient Funds");
    problem.setType(URI.create("https://api.bank.com/errors/insufficient-funds"));
    problem.setProperty("traceId", ex.getTraceId());
    problem.setProperty("shortfall", ex.getContext().get("shortfall"));
    return problem;
}
```

---

## 11. Security, Resilience & Production Hardening

### Data Sanitization in Error Contexts (PII and Secret Masking)
When populating the context map of an exception (`withContext("account", acc)`):
- Never attach plaintext passwords, API keys, Social Security Numbers, or raw credit card numbers.
- If attaching account identifiers, mask them (e.g., `****-****-****-1234`).
- Any field placed in the exception context may be serialized into JSON HTTP responses or logged to centralized log aggregators (Elasticsearch, Datadog).

---

## 12. Operational Runbooks, Triage & Diagnostics

### Correlating Server Logs with Frontend Error Reports via `traceId`
When a customer reports an HTTP 422 or 500 error:
1. Ask for the `traceId` printed on their error screen (e.g., `TRC-987654`).
2. Search centralized logs for `traceId="TRC-987654"`.
3. The logs immediately reveal the exact `BaseDomainException`, its attached context parameters (`shortfall`, `accountId`), and the underlying causal chain.

---

## 13. Holistic Architectural Synthesis & System Design Context

```
                         Domain Exception Lifecycle
                                      │
            ┌─────────────────────────┴─────────────────────────┐
            ▼                                                   ▼
     Domain Core Service                                 API Gateway / Edge
  - Throws typed exception                            - Caught by Global Handler
  - Enriches with business context                    - Strips internal stack trace
  - Injects correlation traceId                       - Formats into RFC 7807 JSON
            │                                                   │
            └─────────────────────────┬─────────────────────────┘
                                      ▼
                             Client Consumption
                             - Inspects error code
                             - Renders user-friendly message
                             - Displays traceId for support
```

---

## 14. Complete Verification Suite (JUnit / Standalone OpenJDK Test Suite)

This comprehensive standalone test suite verifies custom domain exception hierarchies, context map immutability, typed field inspection, and RFC 7807 Problem Details generation on OpenJDK 21 LTS without external dependencies.

```java
package com.bank.exceptions;

import java.net.URI;
import java.time.Instant;
import java.util.*;

public final class CustomDomainExceptionsVerificationSuite {

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

    // 1. Domain Error Code Enum
    public enum ErrorCode {
        ACCOUNT_NOT_FOUND(404, "Account Not Found"),
        INSUFFICIENT_FUNDS(422, "Insufficient Account Balance"),
        ACCOUNT_LOCKED(423, "Account Locked");

        private final int httpStatus;
        private final String defaultTitle;

        ErrorCode(int httpStatus, String defaultTitle) {
            this.httpStatus = httpStatus;
            this.defaultTitle = defaultTitle;
        }

        public int getHttpStatus() { return httpStatus; }
        public String getDefaultTitle() { return defaultTitle; }
    }

    // 2. Base Abstract Domain Exception
    public static abstract class BaseDomainException extends RuntimeException {
        private final ErrorCode errorCode;
        private final String traceId;
        private final Instant timestamp;
        private final Map<String, Object> context;

        protected BaseDomainException(ErrorCode errorCode, String traceId, String message) {
            this(errorCode, traceId, message, null);
        }

        protected BaseDomainException(ErrorCode errorCode, String traceId, String message, Throwable cause) {
            super(message, cause);
            this.errorCode = Objects.requireNonNull(errorCode, "ErrorCode cannot be null");
            this.traceId = Objects.requireNonNull(traceId, "TraceId cannot be null");
            this.timestamp = Instant.now();
            this.context = new LinkedHashMap<>();
        }

        public ErrorCode getErrorCode() { return errorCode; }
        public String getTraceId() { return traceId; }
        public Instant getTimestamp() { return timestamp; }
        public Map<String, Object> getContext() { return Collections.unmodifiableMap(context); }

        public BaseDomainException withContext(String key, Object value) {
            this.context.put(key, value);
            return this;
        }
    }

    // 3. Concrete Domain Exception
    public static class InsufficientFundsException extends BaseDomainException {
        private final double availableBalance;
        private final double attemptedAmount;

        public InsufficientFundsException(String traceId, String accountId, double availableBalance, double attemptedAmount) {
            super(
                ErrorCode.INSUFFICIENT_FUNDS,
                traceId,
                String.format("Account %s has balance %.2f but attempted debit of %.2f", accountId, availableBalance, attemptedAmount)
            );
            this.availableBalance = availableBalance;
            this.attemptedAmount = attemptedAmount;
            withContext("accountId", accountId);
            withContext("availableBalance", availableBalance);
            withContext("attemptedAmount", attemptedAmount);
            withContext("shortfall", attemptedAmount - availableBalance);
        }

        public double getAvailableBalance() { return availableBalance; }
        public double getAttemptedAmount() { return attemptedAmount; }
    }

    // 4. RFC 7807 Problem Details Record
    public record ProblemDetails(
        URI type,
        String title,
        int status,
        String detail,
        String instance,
        String traceId,
        Map<String, Object> parameters
    ) {
        public static ProblemDetails fromDomainException(BaseDomainException ex, String requestPath) {
            return new ProblemDetails(
                URI.create("https://api.bank.com/errors/" + ex.getErrorCode().name().toLowerCase()),
                ex.getErrorCode().getDefaultTitle(),
                ex.getErrorCode().getHttpStatus(),
                ex.getMessage(),
                requestPath,
                ex.getTraceId(),
                ex.getContext()
            );
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Running OpenJDK 21 Custom Domain Exceptions Verification Suite ===");

        // Test 1: Instantiation with Structured Context
        String traceId = "TRC-987654";
        InsufficientFundsException ex = new InsufficientFundsException(traceId, "ACC-101", 150.00, 500.00);

        assertEquals(ErrorCode.INSUFFICIENT_FUNDS, ex.getErrorCode(), "ErrorCode is INSUFFICIENT_FUNDS");
        assertEquals(422, ex.getErrorCode().getHttpStatus(), "HTTP Status is 422");
        assertEquals(traceId, ex.getTraceId(), "Trace ID matches");
        assertEquals(150.00, ex.getAvailableBalance(), "Available balance is 150.00");
        assertEquals(500.00, ex.getAttemptedAmount(), "Attempted amount is 500.00");

        // Test 2: Structured Context Map Inspection & Immutability
        Map<String, Object> ctx = ex.getContext();
        assertEquals("ACC-101", ctx.get("accountId"), "Context accountId matches");
        assertEquals(350.00, ctx.get("shortfall"), "Shortfall matches 350.00");

        boolean uoeThrown = false;
        try {
            ctx.put("illegal", "value");
        } catch (UnsupportedOperationException e) {
            uoeThrown = true;
        }
        assertTrue(uoeThrown, "Exception context map must be unmodifiable to callers");

        // Test 3: RFC 7807 Problem Details Envelope Generation
        ProblemDetails problem = ProblemDetails.fromDomainException(ex, "/api/v1/accounts/ACC-101/transfer");
        assertEquals("https://api.bank.com/errors/insufficient_funds", problem.type().toString(), "URI matches");
        assertEquals("Insufficient Account Balance", problem.title(), "Title matches default title");
        assertEquals(422, problem.status(), "Status matches HTTP 422");
        assertEquals("/api/v1/accounts/ACC-101/transfer", problem.instance(), "Instance path matches");
        assertEquals(traceId, problem.traceId(), "TraceId matches");
        assertEquals(350.00, problem.parameters().get("shortfall"), "Parameter contains shortfall");

        System.out.println("All 3 test scenarios (13 assertions) passed with 100% compliance!");
    }
}
```

---

## 15. Key Takeaways & "Rules of Thumb"

1. **Never rely on raw string exception parsing:** Expose typed fields and standardized machine-readable error codes (`ErrorCode` enum).
2. **Always support the cause constructor:** Ensure every custom exception has a constructor that accepts `Throwable cause` to prevent severed causal chains.
3. **Use a common abstract base exception:** Group bounded context exceptions under `BaseDomainException` to centralize context enrichment, tracing, and logging.
4. **Enrich with correlation context:** Attach `traceId`, `timestamp`, and relevant business parameters to allow immediate log correlation.
5. **Map to RFC 7807 / RFC 9457 at API boundaries:** Translate domain exceptions into standardized Problem Details JSON responses without exposing raw stack traces.

---

## 16. Further Reading & Academic References

- Bloch, Joshua. *Effective Java (3rd Edition)*. Addison-Wesley, 2018. Item 73: "Throw exceptions appropriate to the abstraction", Item 75: "Include failure-capture information in detail messages".
- Nottingham, Mark, and Erik Wilde. *RFC 7807: Problem Details for HTTP APIs*. Internet Engineering Task Force (IETF), 2016. (Updated by RFC 9457, 2023).
- Evans, Eric. *Domain-Driven Design: Tackling Complexity in the Heart of Software*. Addison-Wesley, 2003. Chapter 6: "The Life Cycle of a Domain Object".
- Vernon, Vaughn. *Implementing Domain-Driven Design*. Addison-Wesley, 2013.
