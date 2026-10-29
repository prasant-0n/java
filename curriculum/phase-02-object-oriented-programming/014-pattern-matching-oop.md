# Phase 02: Object-Oriented Programming
# Concept 014: Pattern Matching for instanceof and Switch Expressions

---

## 1. Metadata and Learning Goals

| Metadata Field | Value |
|---|---|
| **Concept ID** | `P02-C014` |
| **Concept Title** | Pattern Matching for instanceof and Switch Expressions |
| **Phase** | Phase 02 — Object-Oriented Programming |
| **Phase Directory** | `curriculum/phase-02-object-oriented-programming` |
| **Source Reference** | `java-backend-engineering-prerequisites-roadmap.md` > Phase 2 > Additional language constructs > Pattern matching basics |
| **Concept Type** | Language / Architecture / Modern Java |
| **Difficulty Level** | Intermediate |
| **Curriculum Importance** | Essential |
| **Java Baseline** | Java 21 LTS (JEP 394 Pattern Matching for `instanceof`, JEP 440 Record Patterns, JEP 441 Pattern Matching for `switch`) |
| **Framework Baseline** | Spring Boot 3.3.x / OpenJDK 21 LTS |
| **Prerequisites** | [`004-four-pillars-of-oop.md`](004-four-pillars-of-oop.md), [`006-overriding-dynamic-dispatch.md`](006-overriding-dynamic-dispatch.md), [`012-records-and-immutable-data.md`](012-records-and-immutable-data.md), [`013-sealed-classes-and-interfaces.md`](013-sealed-classes-and-interfaces.md) |
| **Related Concepts** | [`015-object-class-contracts.md`](015-object-class-contracts.md), `P06-C004` (Functional Domain Modeling) |
| **Estimated Study Effort** | 3–4 hours |
| **Last Research Date** | 2026-10-30 |
| **Verification State** | COMPLETE |

### Measurable Learning Objectives
By the end of this concept module, you will be able to:
1. **Explain:** Explain how Java 21 LTS pattern matching unifies type testing, conditional extraction, and variable scoping into a single, atomic operation at the bytecode level.
2. **Compare:** Contrast traditional Java manual casting and the Gang of Four Visitor Pattern with modern pattern matching, demonstrating how pattern matching eliminates the ceremony of double-dispatch visitor architectures.
3. **Implement:** Construct robust event-driven backend routers utilizing type patterns, record patterns, guarded patterns (`when`), and explicit `case null` handling.
4. **Debug & Diagnose:** Identify and resolve compiler errors regarding **flow scoping** (definite assignment across `&&`, `||`, and early returns) and **pattern dominance** (subtypes shadowed by supertype cases).
5. **Evaluate & Justify:** Articulate how HotSpot compiles pattern matching switch into branch tables and dynamic type checks, and explain why arrow syntax (`->`) eliminates accidental fall-through bugs.

---

## 2. Definition, Purpose, and Mental Model

### 2.1 Formal Definition
In Java 21 LTS, **Pattern Matching** is a language feature that allows developers to test whether a value has a specific shape or type, and if so, **extract (bind) its components into one or more local variables in a single step**.

Pattern matching is deployed across two primary constructs:
1. **Pattern Matching for `instanceof` (JEP 394 / Java 16):** Combines the `instanceof` type check with variable declaration.
2. **Pattern Matching for `switch` (JEP 441 / Java 21 LTS):** Extends switch statements and expressions to match against complex object types, record patterns, and guarded conditions.

### 2.2 The Problem It Solves: The Legacy "Check-Then-Cast" Ceremony
Prior to pattern matching, working with polymorphic objects required repetitive, error-prone boilerplate:

```java
// THE PRE-JAVA 16 "CHECK-THEN-CAST" ANTI-PATTERN
public void processNotification(Object event) {
    if (event instanceof EmailNotification) {
        EmailNotification email = (EmailNotification) event; // REDUNDANT CAST!
        sendEmail(email.getAddress(), email.getBody());
    } else if (event instanceof SmsNotification) {
        SmsNotification sms = (SmsNotification) event;       // REDUNDANT CAST!
        sendSms(sms.getPhoneNumber(), sms.getText());
    }
}
```

This ceremony had severe flaws:
- **Redundancy:** You typed the class name three times (`EmailNotification` in the check, the type, and the cast).
- **Silent Runtime Defects:** A copy-paste error (`if (event instanceof EmailNotification) { SmsNotification sms = (SmsNotification) event; }`) compiled cleanly but crashed at runtime with `ClassCastException`.
- **Clutter:** Business logic was buried under defensive casting plumbing.

With modern pattern matching:
```java
// MODERN JAVA 21: Concise, Atomic, and Type-Safe
if (event instanceof EmailNotification email) {
    sendEmail(email.address(), email.body());
}
```

### 2.3 JavaScript/MERN vs Java Mental Model Gap

In TypeScript, engineers rely on "Type Narrowing" (control flow analysis):

```typescript
// TypeScript Control Flow Narrowing:
function handleResponse(res: ApiResponse) {
    if ('data' in res) {
        // TypeScript narrows 'res' to SuccessResponse purely in the IDE!
        console.log(res.data);
    }
}
```
*The Trap in Node.js/MERN:*
- TypeScript type narrowing is an **erased compiler fiction**. At runtime in Node.js, `res` is an untyped JavaScript hashmap. If an attacker passes `{ data: null }` or malicious shapes, runtime crashes occur.

In Java 21 LTS:
- Pattern matching is **enforced by the JVM bytecode verifier**.
- A pattern variable is a real, nominal local variable allocated directly on the thread's stack frame.
- Scoping is governed by strict **flow scoping rules** backed by definite assignment analysis.

---

## 3. Foundations and Theory: Compiler Mechanics and Flow Scoping

### 3.1 The Mechanics of Flow Scoping (JLS §6.3)
Unlike traditional lexical block scoping (where variables live only between `{` and `}`), pattern variables use **Flow Scoping**. A pattern variable is in scope only where the compiler can mathematically prove that the pattern **matched successfully**:

```java
// 1. CONDITIONAL AND (&&): IN SCOPE ON THE RIGHT
if (obj instanceof String s && s.length() > 5) {
    // 's' is in scope here because '&&' only evaluates right side if left is true!
    System.out.println(s.toUpperCase());
}

// 2. CONDITIONAL OR (||): COMPILE ERROR!
// if (obj instanceof String s || s.length() > 5) { ... }
// COMPILE ERROR: If 'obj' was NOT a String, right side evaluates, but 's' is undefined!

// 3. INVERSION WITH EARLY RETURN: IN SCOPE FOR THE REST OF THE METHOD!
public void process(Object obj) {
    if (!(obj instanceof String s)) {
        return; // Exits method if NOT a String!
    }
    // 's' IS IN SCOPE HERE FOR THE REST OF THE METHOD!
    System.out.println("Valid string: " + s.toLowerCase());
}
```

### 3.2 Pattern Dominance Rules (JLS §14.11.1)
In a switch expression, order matters. A pattern dominates another pattern if it matches a superset of the other pattern's types:

```java
// COMPILE ERROR: PATTERN DOMINANCE VIOLATION
public String categorize(Object obj) {
    return switch (obj) {
        case CharSequence cs -> "Generic Character Sequence: " + cs.length();
        case String s       -> "Specific String"; // COMPILE ERROR: Dominant pattern CharSequence matches first!
        default             -> "Other";
    };
}
```
**Compiler Rule:** More specific subtype patterns **must precede** more general supertype patterns:
```java
// CORRECT ORDER:
return switch (obj) {
    case String s        -> "Specific String: " + s.toUpperCase();
    case CharSequence cs -> "Generic Sequence: " + cs.length();
    default              -> "Other";
};
```

### 3.3 Safe Null Handling in Switch Expressions
In traditional Java (Java 7–16), switching on a variable containing `null` unconditionally threw `NullPointerException` before evaluating any cases.

In Java 21 LTS:
- `switch` supports an explicit **`case null`**!
- You can combine null with default: `case null, default -> ...`
- If you switch over an object and do NOT declare a `case null` and input is null, it throws `NullPointerException` as before.

```java
public String describe(String input) {
    return switch (input) {
        case "A" -> "Option A";
        case "B" -> "Option B";
        case null -> "Handled null safely!";
        default  -> "Unknown option: " + input;
    };
}
```

---

## 4. Practical Syntax, Usage, and Best Practices

### 4.1 Guarded Patterns with `when`
Sometimes a type check alone is insufficient; you only want to match if a boolean condition is also satisfied. Java 21 introduces the **`when`** clause (guard):

```java
public String evaluateLoanRisk(Customer customer, long requestedAmountCents) {
    return switch (customer) {
        case BusinessCustomer bc when requestedAmountCents > 10_000_000L -> 
            "Requires Executive Board Audit for " + bc.companyName();

        case BusinessCustomer bc -> 
            "Standard Commercial Loan for " + bc.companyName();

        case RetailCustomer rc when rc.creditScore() < 600 -> 
            "Rejected: Subprime Credit Score (" + rc.creditScore() + ")";

        case RetailCustomer rc -> 
            "Approved Retail Loan for " + rc.fullName();

        case null -> 
            throw new IllegalArgumentException("Customer cannot be null");
    };
}
```

### 4.2 Record Patterns (Deconstruction) (JEP 440)
Record patterns allow nested, structural deconstruction of data:

```java
public record GeoPoint(double lat, double lon) {}
public record DeliveryAddress(String street, String city, GeoPoint coordinates) {}

public void printCoordinates(Object obj) {
    // NESTED RECORD PATTERN: Unpacks DeliveryAddress AND GeoPoint in one step!
    if (obj instanceof DeliveryAddress(var street, var city, GeoPoint(var lat, var lon))) {
        System.out.printf("Delivering to %s, %s at [%f, %f]\n", street, city, lat, lon);
    }
}
```

---

## 5. Memory Model, Concurrency, and Runtime Characteristics

### 5.1 Bytecode Optimization: HotSpot Inline Caches
When compiling pattern matching switches:
- The Java compiler translates pattern switches into a combination of `instanceof`, `checkcast`, and dynamic dispatch tables.
- At runtime, the HotSpot C2 JIT compiler applies **Polymorphic Inline Caching (PIC)** to the call site.
- If a method repeatedly sees the same 1 or 2 concrete types (e.g., 99% `SuccessRecord`), the JIT compiler generates direct assembly jumps (`cmp`, `je`), executing pattern checks in sub-nanosecond speeds with zero garbage allocation.

---

## 6. Failure Modes, Edge Cases, and Anti-Patterns

### Anti-Pattern 1: The Accidental Fall-Through with Colon Syntax
When using pattern matching in switch statements with the legacy colon (`:`) syntax instead of the modern arrow (`->`) syntax, fall-through between pattern cases is strictly prohibited if a pattern variable is declared:

```java
// COMPILE ERROR: Fall-through across pattern variables is illegal!
switch (obj) {
    case String s:
        System.out.println(s);
        // Missing break;
    case Integer i: // COMPILE ERROR: Illegal fall-through to a pattern!
        System.out.println(i);
        break;
}
```

> [!TIP]
> **Production Best Practice:** Always use the modern **arrow syntax (`->`)** for switch expressions. Arrow syntax guarantees zero fall-through and enforces clean, isolated block scopes.

---

## 7. Comprehensive Code Example: The Enterprise Financial Event Router

The following production-grade implementation synthesizes:
- Pattern matching for `instanceof` with flow scoping.
- Java 21 LTS pattern matching for `switch` with guarded patterns (`when`).
- Nested Record Patterns deconstructing internal domain values.
- Safe `case null` handling.

```java
package com.example.event;

import java.time.Instant;
import java.util.Objects;

// =============================================================================
// 1. DOMAIN MODELS (RECORDS & SEALED HIERARCHY)
// =============================================================================

public record CurrencyAmount(long cents, String currency) {
    public CurrencyAmount {
        Objects.requireNonNull(currency);
        if (cents < 0) throw new IllegalArgumentException("Amount cannot be negative");
    }
}

public sealed interface FinancialEvent permits 
    DepositEvent, 
    WithdrawalEvent, 
    TransferEvent, 
    FraudAlertEvent {}

public record DepositEvent(String accountId, CurrencyAmount amount, Instant timestamp) implements FinancialEvent {}
public record WithdrawalEvent(String accountId, CurrencyAmount amount, String atmId, Instant timestamp) implements FinancialEvent {}
public record TransferEvent(String sourceAccountId, String targetAccountId, CurrencyAmount amount, Instant timestamp) implements FinancialEvent {}
public record FraudAlertEvent(String accountId, int severityScore, String reason, Instant timestamp) implements FinancialEvent {}

// =============================================================================
// 2. FINANCIAL EVENT DISPATCHER
// =============================================================================

public class FinancialEventDispatcher {

    public String routeAndAudit(FinancialEvent event) {
        // JAVA 21 LTS EXHAUSTIVE PATTERN MATCHING SWITCH WITH GUARDS & RECORD PATTERNS
        return switch (event) {
            case null -> 
                "ERROR: Event payload is null";

            // Guarded Pattern with Nested Record Deconstruction
            case DepositEvent(var acct, CurrencyAmount(var cents, var curr), var ts) when cents >= 1_000_000L -> 
                String.format("HIGH_VALUE_DEPOSIT [Acct: %s, Amount: %d %s] - Flagged for AML Audit", acct, cents / 100, curr);

            case DepositEvent(var acct, CurrencyAmount(var cents, var curr), var ts) -> 
                String.format("STANDARD_DEPOSIT [Acct: %s, Amount: %d %s]", acct, cents / 100, curr);

            case WithdrawalEvent(var acct, CurrencyAmount(var cents, var curr), var atm, var ts) when cents > 500_000L -> 
                String.format("LARGE_ATM_WITHDRAWAL [Acct: %s, ATM: %s, Amount: %d %s] - Limit Alert", acct, atm, cents / 100, curr);

            case WithdrawalEvent(var acct, CurrencyAmount(var cents, var curr), var atm, var ts) -> 
                String.format("STANDARD_WITHDRAWAL [Acct: %s, ATM: %s, Amount: %d %s]", acct, atm, cents / 100, curr);

            case TransferEvent(var src, var tgt, CurrencyAmount(var cents, var curr), var ts) -> 
                String.format("INTERNAL_TRANSFER [From: %s -> To: %s, Amount: %d %s]", src, tgt, cents / 100, curr);

            case FraudAlertEvent(var acct, var score, var reason, var ts) when score >= 80 -> 
                String.format("CRITICAL_FRAUD [Acct: %s, Score: %d, Reason: %s] - Account Frozen", acct, score, reason);

            case FraudAlertEvent(var acct, var score, var reason, var ts) -> 
                String.format("SUSPICIOUS_ACTIVITY [Acct: %s, Score: %d, Reason: %s] - Monitoring", acct, score, reason);
        };
    }

    // Demonstrates flow scoping with instanceof
    public boolean isHighPriorityAccount(Object potentialEvent) {
        if (potentialEvent instanceof FraudAlertEvent alert && alert.severityScore() >= 80) {
            return true;
        }
        if (potentialEvent instanceof DepositEvent dep && dep.amount().cents() > 5_000_000L) {
            return true;
        }
        return false;
    }
}
```

---

## 8. Testing Strategies and Verification Suite

The following standalone verification test suite tests:
1. Flow scoping and boolean condition evaluation.
2. Guarded pattern switch dispatch accuracy across boundary thresholds.
3. Nested Record Pattern deconstruction.
4. Safe `case null` execution.

```java
package com.example.event.test;

import com.example.event.CurrencyAmount;
import com.example.event.DepositEvent;
import com.example.event.FinancialEventDispatcher;
import com.example.event.FraudAlertEvent;
import com.example.event.TransferEvent;
import com.example.event.WithdrawalEvent;

import java.time.Instant;

public class PatternMatchingVerificationTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("Starting Pattern Matching Verification Tests...\n");

        testFlowScopingInstanceof();
        testGuardedSwitchDispatch();
        testNestedRecordDeconstruction();
        testNullSafetyHandling();

        System.out.println("\n--------------------------------------------------");
        System.out.printf("Test Suite Results: %d Passed, %d Failed\n", passed, failed);
        System.out.println("--------------------------------------------------");

        if (failed > 0) {
            System.err.println("TEST SUITE FAILED");
            System.exit(1);
        } else {
            System.out.println("ALL PATTERN MATCHING TESTS PASSED CLEANLY (100%).");
        }
    }

    private static void assertEquals(Object expected, Object actual, String name) {
        if (!java.util.Objects.equals(expected, actual)) {
            System.err.printf("FAIL: %s (Expected %s, got %s)\n", name, expected, actual);
            failed++;
        } else {
            System.out.printf("PASS: %s\n", name);
            passed++;
        }
    }

    private static void assertTrue(boolean condition, String name) {
        if (!condition) {
            System.err.printf("FAIL: %s (Condition was false)\n", name);
            failed++;
        } else {
            System.out.printf("PASS: %s\n", name);
            passed++;
        }
    }

    // TEST 1: FLOW SCOPING WITH INSTANCEOF
    private static void testFlowScopingInstanceof() {
        FinancialEventDispatcher dispatcher = new FinancialEventDispatcher();
        Instant now = Instant.now();

        FraudAlertEvent criticalAlert = new FraudAlertEvent("ACC-1", 95, "DEVICE_MISMATCH", now);
        FraudAlertEvent lowAlert = new FraudAlertEvent("ACC-2", 30, "IP_CHANGE", now);
        DepositEvent normalDeposit = new DepositEvent("ACC-3", new CurrencyAmount(1000L, "USD"), now);

        assertTrue(dispatcher.isHighPriorityAccount(criticalAlert), "Critical fraud alert is high priority");
        assertTrue(!dispatcher.isHighPriorityAccount(lowAlert), "Low fraud alert is not high priority");
        assertTrue(!dispatcher.isHighPriorityAccount(normalDeposit), "Standard deposit is not high priority");
        assertTrue(!dispatcher.isHighPriorityAccount("Non-event string"), "Non-event string safely returns false");
    }

    // TEST 2: GUARDED SWITCH DISPATCH ACROSS BOUNDARIES
    private static void testGuardedSwitchDispatch() {
        FinancialEventDispatcher dispatcher = new FinancialEventDispatcher();
        Instant now = Instant.now();

        // 1. High value deposit ($10,000.00 = 1,000,000 cents) -> Guard triggers AML
        DepositEvent highDeposit = new DepositEvent("A-100", new CurrencyAmount(1_000_000L, "USD"), now);
        assertEquals("HIGH_VALUE_DEPOSIT [Acct: A-100, Amount: 10000 USD] - Flagged for AML Audit", 
            dispatcher.routeAndAudit(highDeposit), "High value deposit flagged via guard");

        // 2. Standard deposit ($50.00 = 5,000 cents) -> Standard case
        DepositEvent normalDeposit = new DepositEvent("A-101", new CurrencyAmount(5_000L, "USD"), now);
        assertEquals("STANDARD_DEPOSIT [Acct: A-101, Amount: 50 USD]", 
            dispatcher.routeAndAudit(normalDeposit), "Standard deposit routed");

        // 3. Fraud score 85 -> Critical account frozen
        FraudAlertEvent critical = new FraudAlertEvent("A-102", 85, "GEO_SPOOF", now);
        assertEquals("CRITICAL_FRAUD [Acct: A-102, Score: 85, Reason: GEO_SPOOF] - Account Frozen", 
            dispatcher.routeAndAudit(critical), "Critical fraud triggered via guard");
    }

    // TEST 3: NESTED RECORD DECONSTRUCTION
    private static void testNestedRecordDeconstruction() {
        FinancialEventDispatcher dispatcher = new FinancialEventDispatcher();
        Instant now = Instant.now();

        WithdrawalEvent withdrawal = new WithdrawalEvent("A-200", new CurrencyAmount(200_00L, "USD"), "ATM-EAST-1", now);
        assertEquals("STANDARD_WITHDRAWAL [Acct: A-200, ATM: ATM-EAST-1, Amount: 200 USD]", 
            dispatcher.routeAndAudit(withdrawal), "Withdrawal record pattern deconstructed inner currency amount");

        TransferEvent transfer = new TransferEvent("SRC-1", "TGT-2", new CurrencyAmount(1500_00L, "EUR"), now);
        assertEquals("INTERNAL_TRANSFER [From: SRC-1 -> To: TGT-2, Amount: 1500 EUR]", 
            dispatcher.routeAndAudit(transfer), "Transfer event deconstructed successfully");
    }

    // TEST 4: NULL SAFETY HANDLING
    private static void testNullSafetyHandling() {
        FinancialEventDispatcher dispatcher = new FinancialEventDispatcher();
        String nullResult = dispatcher.routeAndAudit(null);
        assertEquals("ERROR: Event payload is null", nullResult, "case null handled null input safely without NPE");
    }
}
```

---

## 9. Modern Java Evolution: Pattern Matching Roadmap

| Java Version | Feature | JEP | Status |
| :--- | :--- | :--- | :--- |
| **Java 14** | Pattern Matching for `instanceof` | JEP 305 | Preview |
| **Java 16** | Pattern Matching for `instanceof` | JEP 394 | Final / Standard |
| **Java 17** | Pattern Matching for `switch` | JEP 406 | Preview |
| **Java 21 LTS** | Pattern Matching for `switch` | JEP 441 | Final / Standard |
| **Java 21 LTS** | Record Patterns (Deconstruction) | JEP 440 | Final / Standard |
| **Java 22+** | Unnamed Variables & Patterns (`_`) | JEP 456 | Final / Standard |

---

## 10. System Architecture: Eliminating the Gang of Four Visitor Pattern

### The Collapse of the Visitor Pattern
In classical OOP, processing an object tree with different operations without polluting domain entities required the **Visitor Pattern** (double-dispatch with `accept(Visitor v)` and `visit(ConcreteElement e)` methods across every class).

**Why Pattern Matching Obsoletes the Visitor Pattern:**
1. **Zero Domain Modification:** Domain records/classes do not need `accept()` methods.
2. **Local Cohesion:** The entire traversal/processing algorithm lives in a single, readable switch expression.
3. **Compile-Time Exhaustiveness:** Sealed hierarchies guarantee that all elements are visited without visitor interface maintenance.

---

## 11. Cross-Domain Integrations (Spring Boot, Kafka Streams)

### 11.1 Polymorphic Event Routing in Kafka Consumers
```java
@KafkaListener(topics = "financial-transactions")
public void handleTransaction(Object payload) {
    switch (payload) {
        case PaymentSuccessRecord s -> metricsService.recordSuccess(s.cents());
        case PaymentFailureRecord f -> alertService.notifyOps(f.errorCode());
        case FraudFlaggedRecord ff  -> securityService.isolateUser(ff.userId());
        default -> log.warn("Unrecognized Kafka event payload: {}", payload);
    }
}
```

---

## 12. Comprehensive Troubleshooting & Diagnostic Runbook

| Failure Symptom | Root Cause | Exact Remediation |
| :--- | :--- | :--- |
| `error: cannot find symbol variable s` | Attempted to use pattern variable `s` in an expression where match success was not proven (e.g. across `||`). | Check flow scoping rules; ensure variable is only accessed where condition evaluates to true. |
| `error: this case label is dominated by a preceding case label` | Supertype pattern was placed above a subtype pattern in a switch expression. | Reorder cases so the most specific subtypes appear before general supertypes. |
| `java.lang.NullPointerException` on `switch(obj)` | `obj` is null and the switch expression lacks an explicit `case null`. | Add `case null -> ...` or `case null, default -> ...` to handle null gracefully. |

---

## 13. Frequently Asked Interview Questions

### Q1: What is "Flow Scoping" and how does it differ from traditional block scoping?
**Answer:** Traditional block scoping binds a variable to the enclosing `{ ... }` curly braces. **Flow Scoping** determines a pattern variable's scope based on compiler definite assignment analysis: the variable is in scope only where the compiler proves the pattern check evaluated to `true`. This allows pattern variables to remain in scope on the right side of `&&`, or even outside an `if` statement following an inverted condition with an early `return`.

### Q2: What is Pattern Dominance and how does the compiler enforce it?
**Answer:** Pattern Dominance occurs when a pattern matches all values matched by another pattern. For example, `case Object o` or `case CharSequence cs` dominates `case String s`. The Java compiler enforces that dominated patterns cannot appear after dominant patterns; doing so triggers a mandatory compile error to prevent dead code.

### Q3: What is the difference between a type pattern and a record pattern?
**Answer:** A **type pattern** checks the type and binds the object to a single variable (`case String s`). A **record pattern** goes a step further: it checks the type and **deconstructs** its components into individual variables in a single operation (`case Point(int x, int y)`), including recursive deconstruction of nested records.

---

## 14. Deliberate Practice: Hands-On Exercises

### Exercise 1 (Beginner): Safe JSON Value Parser
- **Objective:** Parse a loosely typed `Object` into formatted strings.
- **Requirements:**
  1. Handle `String`, `Number`, `Boolean`, and `null`.
  2. For `Number`, format as floating-point with 2 decimals.
  3. Verify that `null` returns `"null_value"`.

### Exercise 2 (Intermediate): HTTP Request Router with Guards
- **Objective:** Route an `HttpRequest` record with method and path.
- **Requirements:**
  1. Use guarded switch patterns (`when path.startsWith("/api/v1")`).
  2. Reject unauthenticated requests to `/admin` paths.

### Exercise 3 (Advanced): Expression Tree Optimizer
- **Objective:** Optimize arithmetic expressions using Record Patterns.
- **Requirements:**
  1. `sealed interface Expr permits Const, Add, Mul`.
  2. Optimize `Add(x, Const(0))` $\to$ `x`.
  3. Optimize `Mul(x, Const(1))` $\to$ `x`.

---

## 15. Concept Mastery Verification Checklist

- [x] Explain the flow scoping rules of pattern variables across `&&`, `||`, and early returns.
- [x] Implement pattern matching for `instanceof` to eliminate redundant casting.
- [x] Construct exhaustive switch expressions with guarded patterns (`when`).
- [x] Deconstruct nested records using Java 21 LTS Record Patterns (JEP 440).
- [x] Handle null inputs safely using explicit `case null` in switch expressions.
- [x] Explain why Pattern Matching renders the GoF Visitor Pattern obsolete in modern Java.

---

## 16. Authoritative Primary Sources and Further Reading

1. **OpenJDK JEP 394:** *Pattern Matching for instanceof* (Java 16 Final Feature).
2. **OpenJDK JEP 440:** *Record Patterns* (Java 21 LTS Final Feature).
3. **OpenJDK JEP 441:** *Pattern Matching for switch* (Java 21 LTS Final Feature).
4. **James Gosling et al.:** *The Java Language Specification (Java SE 21 Edition)*:
   - Section 6.3: "Scope of a Declaration (Flow Scoping)"
   - Section 14.11: "The switch Statement and Expression"
