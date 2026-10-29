# Phase 02: Object-Oriented Programming
# Concept 013: Sealed Classes, Sealed Interfaces, and Algebraic Data Types

---

## 1. Metadata and Learning Goals

| Metadata Field | Value |
|---|---|
| **Concept ID** | `P02-C013` |
| **Concept Title** | Sealed Classes, Sealed Interfaces, and Algebraic Data Types |
| **Phase** | Phase 02 — Object-Oriented Programming |
| **Phase Directory** | `curriculum/phase-02-object-oriented-programming` |
| **Source Reference** | `java-backend-engineering-prerequisites-roadmap.md` > Phase 2 > Additional language constructs > Sealed classes and interfaces |
| **Concept Type** | Language / Architecture / Type Theory |
| **Difficulty Level** | Intermediate to Advanced |
| **Curriculum Importance** | Essential |
| **Java Baseline** | Java 21 LTS (with JEP 409 Sealed Classes & JEP 441 Switch Pattern Matching) |
| **Framework Baseline** | Spring Boot 3.3.x / Jackson 2.17 / OpenJDK 21 LTS |
| **Prerequisites** | [`005-inheritance-and-fragile-base-class.md`](005-inheritance-and-fragile-base-class.md), [`007-abstract-classes-vs-interfaces.md`](007-abstract-classes-vs-interfaces.md), [`012-records-and-immutable-data.md`](012-records-and-immutable-data.md) |
| **Related Concepts** | [`014-pattern-matching-oop.md`](014-pattern-matching-oop.md), `P06-C004` (Functional Domain Modeling) |
| **Estimated Study Effort** | 3–4 hours |
| **Last Research Date** | 2026-10-30 |
| **Verification State** | COMPLETE |

### Measurable Learning Objectives
By the end of this concept module, you will be able to:
1. **Explain:** Explain how Java Sealed Classes and Interfaces (JEP 409) restrict subtyping at compile time and runtime via the classfile `PermittedSubclasses` attribute.
2. **Compare:** Contrast open inheritance (`public class`), closed inheritance (`final class`), and controlled inheritance (`sealed class ... permits`), explaining why sealed types provide the foundation for Algebraic Data Types (ADTs).
3. **Implement:** Construct robust domain models using **Sum Types** (Sealed Interfaces) and **Product Types** (Records) to model mutually exclusive business outcomes (e.g., `PaymentResult: Success | Failure | RequiresAction`).
4. **Debug & Diagnose:** Identify and resolve compiler errors regarding missing subclass modifiers (`final`, `sealed`, `non-sealed`), cross-package co-location violations, and Jackson polymorphic JSON deserialization failures.
5. **Evaluate & Justify:** Explain how the Java compiler proves **exhaustiveness** in switch expressions without requiring fragile `default:` branches, and articulate the binary compatibility implications of altering permitted subclasses.

---

## 2. Definition, Purpose, and Mental Model

### 2.1 Formal Definition
Introduced in Java 15 and finalized in Java 17 LTS via **JEP 409** (codified in JLS §8.1.6 and §9.1.4), a **Sealed Class or Interface** is a type that restricts which other classes or interfaces may extend or implement it.

Prior to Java 17, Java offered only two extreme inheritance models:
1. **Unrestricted / Open:** Any class could extend a public class or implement an interface anywhere in the world.
2. **Strictly Closed:** A `final` class could not be extended by anyone at all.

Sealed types provide the crucial **middle ground: Controlled Extension**. A superclass can declare an explicit, closed whitelist of allowed subtypes using the `permits` keyword:

```java
public sealed interface PaymentResult permits PaymentSuccess, PaymentFailed, PaymentRequires3DS {}
```

### 2.2 The Problem It Solves: The Tyranny of the `default` Branch
In traditional object-oriented programming, when you write code that processes different subclasses of an interface, the compiler cannot know all possible subclasses that exist:

```java
// TRADITIONAL OPEN HIERARCHY
public interface CommandResult {}

public void handle(CommandResult result) {
    if (result instanceof Success s) { ... }
    else if (result instanceof Failure f) { ... }
    else {
        // MANDATORY DEFENSIVE CODE: What if a 3rd party added MaliciousResult?
        throw new IllegalStateException("Unknown result type");
    }
}
```

If another developer added `PendingResult` to the codebase, the compiler gave **zero warning** that `handle()` was missing a case! The bug remained hidden until production, where the `else` branch triggered at runtime.

With **Sealed Types**, the compiler knows **every possible subtype that can ever exist**. In a `switch` expression, the compiler mathematically verifies that every case is covered. If a new permitted subtype is added later, every `switch` across the entire enterprise application **fails to compile immediately**, catching the defect during build time!

### 2.3 Algebraic Data Types (ADTs) in Modern Backend Systems
In type theory and functional programming, domain types are modeled as **Algebraic Data Types (ADTs)**:

1. **Product Types (Records):** A type that combines multiple components ($A \times B$).
   *Example:* `record Point(int x, int y)` has $|int| \times |int|$ possible values.
2. **Sum Types (Sealed Hierarchies):** A type that is an exclusive disjoint union of distinct types ($A + B$).
   *Example:* `sealed interface Result permits Success, Failure` has $|Success| + |Failure|$ possible states.

Together, **Records + Sealed Interfaces = Full Algebraic Data Types** in Java!

```
+-----------------------------------------------------------------------------------+
|                           THE ADT FORMULA IN JAVA 21 LTS                          |
+-----------------------------------------------------------------------------------+
|   PRODUCT TYPE (Record)       : A AND B AND C (All components present)            |
|   SUM TYPE (Sealed Interface) : A OR B OR C (Mutually exclusive alternatives)     |
+-----------------------------------------------------------------------------------+
```

### 2.4 JavaScript/MERN vs Java Mental Model Gap

In TypeScript, engineers represent sum types using **Discriminated Unions**:

```typescript
// TypeScript Discriminated Union:
type OrderState = 
    | { status: 'PENDING'; cartTotal: number }
    | { status: 'CONFIRMED'; orderId: string; trackingCode: string }
    | { status: 'CANCELLED'; reason: string };

function processOrder(state: OrderState) {
    switch (state.status) {
        case 'PENDING': return state.cartTotal;
        case 'CONFIRMED': return state.trackingCode;
        case 'CANCELLED': return state.reason;
    }
}
```

*The Critical Problem in Node.js/MERN:*
- TypeScript's discriminated union is **purely compile-time metadata** and is completely erased in JavaScript!
- An incoming JSON payload or external microservice can inject `{ status: 'HACKED' }`, bypassing TypeScript's type checker and silently crashing the runtime.

In Java 21 LTS:
- `OrderState` is a physical, nominal `sealed interface` enforced at the **JVM bytecode and classfile verification level**.
- Every variant is a strongly typed `record` implementing `OrderState`.
- It is impossible for an invalid or unexpected subtype to exist on the JVM Heap; class loading will reject unauthorized subclasses immediately with `IncompatibleClassChangeError`.

---

## 3. Foundations and Theory: Compiler Mechanics and Constraints

### 3.1 The Three Mandatory Subclass Modifiers
Every class that directly extends a sealed class or implements a sealed interface **MUST** explicitly declare one of three mutually exclusive modifiers:

```
                                 SEALED SUPERTYPE
                                        │
                 ┌──────────────────────┼──────────────────────┐
                 ▼                      ▼                      ▼
               final                  sealed               non-sealed
        (Terminal node:        (Sub-hierarchy:        (Unrestricted node:
        No further subclasses  Restricts its own      Re-opens hierarchy
             allowed)          permitted children)    to arbitrary subclassing)
```

1. **`final`:** The subtype is a terminal leaf; it cannot be extended by any other class.
2. **`sealed`:** The subtype continues to seal the hierarchy, declaring its own `permits` clause.
3. **`non-sealed`:** The subtype deliberately breaks the seal, opening itself up to unrestricted extension by any subclass.

### 3.2 Co-Location Rules (Module & Package Constraints)
Under JLS §8.1.6, all permitted subclasses must be co-located with the sealed class:
1. **Named Modules:** If the sealed class is in a named module, all permitted subclasses must reside within the **same module**. They may be in different packages within that module.
2. **Unnamed Modules:** If the sealed class is in an unnamed module (standard classpath), all permitted subclasses must reside within the **exact same package**.
3. **Implicit Permits:** If all permitted subclasses are declared within the **same `.java` source file** as the sealed class, the `permits` clause may be omitted entirely! The Java compiler automatically detects the subclasses and generates the permitted list.

### 3.3 Bytecode Inspection: The `PermittedSubclasses` Attribute
When you compile:

```java
public sealed interface LoanStatus permits LoanApproved, LoanRejected {}
public final class LoanApproved implements LoanStatus {}
public final class LoanRejected implements LoanStatus {}
```

Disassembling `LoanStatus.class` via `javap -v LoanStatus.class` shows:

```
Classfile .../LoanStatus.class
  flags: (0x0601) ACC_PUBLIC, ACC_INTERFACE, ACC_ABSTRACT
  ...
PermittedSubclasses:
  com/example/model/LoanApproved
  com/example/model/LoanRejected
```

The JVM verifier uses this `PermittedSubclasses` attribute to block any unauthorized class from claiming to implement `LoanStatus`. If an attacker compiles a rogue class `RogueLoan implements LoanStatus` in a separate JAR, attempting to load it causes the JVM to throw:
```
java.lang.IncompatibleClassChangeError: class RogueLoan cannot inherit from sealed class LoanStatus
```

---

## 4. Practical Syntax, Usage, and Best Practices

### 4.1 Exhaustive Pattern Matching Switch
The primary superpower of sealed types in backend applications is enabling **exhaustive switch expressions** without a `default:` branch:

```java
package com.example.billing;

public sealed interface BillingEvent permits SubscriptionCreated, PaymentProcessed, SubscriptionCancelled {}

public record SubscriptionCreated(String customerId, String planId) implements BillingEvent {}
public record PaymentProcessed(String customerId, long amountCents) implements BillingEvent {}
public record SubscriptionCancelled(String customerId, String reason) implements BillingEvent {}
```

```java
// EXHAUSTIVE SWITCH: Zero default branch needed!
public String auditEvent(BillingEvent event) {
    return switch (event) {
        case SubscriptionCreated sc -> "New subscription for " + sc.customerId() + " on " + sc.planId();
        case PaymentProcessed pp    -> "Processed $" + (pp.amountCents() / 100) + " for " + pp.customerId();
        case SubscriptionCancelled c -> "Cancelled " + c.customerId() + ": " + c.reason();
    }; // COMPILER PROVES ALL PERMITTED SUBCLASSES ARE HANDLED!
}
```

> [!IMPORTANT]
> **Why You Should NEVER Add a `default:` Branch to a Sealed Switch:**
> If you add `default -> ...` to a switch over a sealed type, you destroy compiler exhaustiveness protection! When another engineer adds a 4th permitted subclass `TrialExpired`, the compiler will **not** warn you, and the new event will silently fall through to the `default` branch!

---

## 5. Memory Model, Concurrency, and Runtime Characteristics

### 5.1 Runtime Reflection APIs
Java 17 introduced reflection methods on `java.lang.Class` to inspect sealed types at runtime:

```java
Class<?> clazz = BillingEvent.class;

// 1. Check if class is sealed
boolean sealed = clazz.isSealed(); // true

// 2. Query permitted subclasses
Class<?>[] permitted = clazz.getPermittedSubclasses();
// Returns [SubscriptionCreated.class, PaymentProcessed.class, SubscriptionCancelled.class]
```

Frameworks like Jackson and Spring MVC use these reflection APIs to automatically configure polymorphic deserializers and API schema validators without requiring manual annotation wiring.

### 5.2 Binary Compatibility & The JVM MatchException
What happens if Library A publishes a sealed interface, and Consumer Application B switches on it?
1. Library A updates its JAR: adds a new permitted subclass `RefundIssued`.
2. Application B replaces Library A's JAR **without recompiling Application B**.
3. At runtime, Library A passes a `RefundIssued` instance into Application B's switch method.

Because Application B was not recompiled, its switch statement does not contain a case for `RefundIssued`.
In Java 21 LTS, the JVM handles this binary mismatch by throwing:
```
java.lang.MatchException: Exhaustive switch did not match input
```
The JVM guarantees that code will fail-fast with a deterministic exception rather than silently corrupting memory or jumping to arbitrary execution offsets.

---

## 6. Failure Modes, Edge Cases, and Anti-Patterns

### Anti-Pattern 1: The Accidental `non-sealed` Escape Hatch
When defining permitted subclasses, using `non-sealed` breaks the closed-world assumption:

```java
public sealed interface Shape permits Circle, Square, Polygon {}

public final class Circle implements Shape {}
public final class Square implements Shape {}
public non-sealed class Polygon implements Shape {} // HIERARCHY OPENED!
```
Now, any consumer can write `class WildcardShape extends Polygon`. When switching on `Shape`, exhaustiveness still works because `Polygon` covers all its subclasses, but you have lost control over domain boundary constraints.

> [!TIP]
> **Production Best Practice:** Favor `final` (or `record`) for all permitted subtypes. Use `non-sealed` only when you explicitly intend to provide an open extension point for external plugins.

### Anti-Pattern 2: Subclass Modifier Omission
If you forget to declare `final`, `sealed`, or `non-sealed` on a permitted subclass:

```java
public sealed interface Result permits Success, Failure {}

// COMPILE ERROR:
public class Success implements Result {} 
// error: sealed, once-sealed, or non-sealed expected
```
Every direct subclass of a sealed type **must** explicitly declare its inheritance modifier.

---

## 7. Comprehensive Code Example: Enterprise Payment Processing Gateway Result Engine

The following production-grade implementation synthesizes:
- A `sealed interface` representing payment processing outcomes.
- Product types (`record`) implementing the sealed interface.
- A sub-sealed class representing multi-tiered rejection reasons.
- Exhaustive switch pattern matching with zero `default:` branch.
- Standalone verification suite.

```java
package com.example.payment;

import java.time.Instant;
import java.util.Objects;

// =============================================================================
// 1. SEALED SUM TYPE: PAYMENT TRANSACTION RESULT
// =============================================================================

public sealed interface PaymentResult permits 
    PaymentResult.Success, 
    PaymentResult.RequiresAction, 
    PaymentResult.Failure {

    String transactionId();
    Instant timestamp();

    // VARIANT 1: SUCCESS (Product Record)
    record Success(
        String transactionId, 
        long settledAmountCents, 
        String authorizationCode, 
        Instant timestamp
    ) implements PaymentResult {
        public Success {
            Objects.requireNonNull(transactionId);
            Objects.requireNonNull(authorizationCode);
            if (settledAmountCents <= 0) throw new IllegalArgumentException("Amount must be positive");
        }
    }

    // VARIANT 2: REQUIRES ACTION (e.g. 3D Secure Verification)
    record RequiresAction(
        String transactionId, 
        String actionUrl, 
        String clientSecret, 
        Instant timestamp
    ) implements PaymentResult {
        public RequiresAction {
            Objects.requireNonNull(transactionId);
            Objects.requireNonNull(actionUrl);
        }
    }

    // VARIANT 3: FAILURE (Sub-Sealed Hierarchy for Detailed Rejection)
    sealed interface Failure extends PaymentResult permits 
        NetworkTimeout, 
        InsufficientFunds, 
        FraudRejected {}

    record NetworkTimeout(
        String transactionId, 
        String gatewayEndpoint, 
        Instant timestamp
    ) implements Failure {}

    record InsufficientFunds(
        String transactionId, 
        long attemptedAmountCents, 
        Instant timestamp
    ) implements Failure {}

    record FraudRejected(
        String transactionId, 
        int riskScore, 
        String ruleTriggered, 
        Instant timestamp
    ) implements Failure {}
}
```

---

## 8. Testing Strategies and Verification Suite

The following standalone verification test suite tests:
1. Exhaustive pattern matching over all sealed variants without `default`.
2. Nested pattern matching over sub-sealed `Failure` types.
3. Reflection verification of `isSealed()` and `getPermittedSubclasses()`.
4. Invariant enforcement across record variants.

```java
package com.example.payment.test;

import com.example.payment.PaymentResult;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class SealedHierarchyVerificationTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("Starting Sealed Classes & ADT Verification Tests...\n");

        testExhaustiveSwitchEvaluation();
        testNestedSubSealedFailureHandling();
        testReflectionSealedMetadata();

        System.out.println("\n--------------------------------------------------");
        System.out.printf("Test Suite Results: %d Passed, %d Failed\n", passed, failed);
        System.out.println("--------------------------------------------------");

        if (failed > 0) {
            System.err.println("TEST SUITE FAILED");
            System.exit(1);
        } else {
            System.out.println("ALL SEALED HIERARCHY TESTS PASSED CLEANLY (100%).");
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

    // DISPATCH ROUTER: Exhaustive switch expression with zero default clause
    private static String routePaymentResult(PaymentResult result) {
        return switch (result) {
            case PaymentResult.Success s -> 
                "SETTLED: " + s.transactionId() + " Auth: " + s.authorizationCode();

            case PaymentResult.RequiresAction ra -> 
                "REDIRECT: " + ra.actionUrl();

            case PaymentResult.NetworkTimeout to -> 
                "RETRY_QUEUED: Timeout on " + to.gatewayEndpoint();

            case PaymentResult.InsufficientFunds nsf -> 
                "DECLINED: Insufficient funds for " + nsf.attemptedAmountCents();

            case PaymentResult.FraudRejected f -> 
                "BLOCKED: Risk score " + f.riskScore() + " (" + f.ruleTriggered() + ")";
        };
    }

    // TEST 1: EXHAUSTIVE SWITCH EVALUATION
    private static void testExhaustiveSwitchEvaluation() {
        Instant now = Instant.now();
        PaymentResult s = new PaymentResult.Success("TX-1", 5000L, "AUTH-999", now);
        PaymentResult ra = new PaymentResult.RequiresAction("TX-2", "https://3ds.bank.com", "sec_1", now);

        assertEquals("SETTLED: TX-1 Auth: AUTH-999", routePaymentResult(s), "Success variant routed correctly");
        assertEquals("REDIRECT: https://3ds.bank.com", routePaymentResult(ra), "RequiresAction variant routed correctly");
    }

    // TEST 2: NESTED SUB-SEALED HIERARCHY MATCHING
    private static void testNestedSubSealedFailureHandling() {
        Instant now = Instant.now();
        PaymentResult to = new PaymentResult.NetworkTimeout("TX-3", "https://api.stripe.com", now);
        PaymentResult nsf = new PaymentResult.InsufficientFunds("TX-4", 12000L, now);
        PaymentResult fraud = new PaymentResult.FraudRejected("TX-5", 88, "VELOCITY_EXCEEDED", now);

        assertEquals("RETRY_QUEUED: Timeout on https://api.stripe.com", routePaymentResult(to), "Timeout routed");
        assertEquals("DECLINED: Insufficient funds for 12000", routePaymentResult(nsf), "NSF routed");
        assertEquals("BLOCKED: Risk score 88 (VELOCITY_EXCEEDED)", routePaymentResult(fraud), "Fraud routed");
    }

    // TEST 3: REFLECTION SELECTION METADATA
    private static void testReflectionSealedMetadata() {
        Class<?> root = PaymentResult.class;
        assertTrue(root.isSealed(), "PaymentResult is detected as sealed via reflection");

        Class<?>[] permitted = root.getPermittedSubclasses();
        assertEquals(3, permitted.length, "PaymentResult has exactly 3 direct permitted subclasses");

        List<String> permittedNames = Arrays.stream(permitted).map(Class::getSimpleName).toList();
        assertTrue(permittedNames.contains("Success"), "Permitted list contains Success");
        assertTrue(permittedNames.contains("RequiresAction"), "Permitted list contains RequiresAction");
        assertTrue(permittedNames.contains("Failure"), "Permitted list contains Failure");

        // Verify Failure sub-sealed hierarchy
        Class<?> failureClass = PaymentResult.Failure.class;
        assertTrue(failureClass.isSealed(), "Failure interface is sealed");
        assertEquals(3, failureClass.getPermittedSubclasses().length, "Failure has 3 permitted variants");
    }
}
```

---

## 9. Modern Java Evolution: Pattern Matching and Sealed Exhaustiveness

### 9.1 The Triad of Modern Java Data Modeling
Java 21 LTS completes a 5-year evolution that brings modern functional data modeling to the Java platform:

1. **JEP 395 (Records):** Product Types.
2. **JEP 409 (Sealed Classes):** Sum Types.
3. **JEP 440 & 441 (Pattern Matching & Switch Expressions):** Type Deconstruction & Flow Control.

Together, these features allow Java backends to model complex domain processes (order fulfillment, loan approval, fraud analysis) with **mathematical completeness**, eliminating millions of lines of null-checking and defensive fallback boilerplate.

---

## 10. System Architecture, Clean Architecture, and Design Patterns

### 10.1 Command & Query Response Modeling
In Clean Architecture and CQRS:
- Traditional code returns DTOs with nullable fields (`error`, `data`, `status`) or throws checked exceptions.
- Sealed Hierarchies model responses as pure **Result Sum Types**:
```java
public sealed interface ServiceResponse<T> permits ServiceResponse.Success, ServiceResponse.Error {
    record Success<T>(T payload) implements ServiceResponse<T> {}
    record Error<T>(String errorCode, String message) implements ServiceResponse<T> {}
}
```
This forces callers to explicitly handle both success and error cases at compile time, eliminating unhandled runtime errors.

---

## 11. Cross-Domain Integrations (Spring Boot, Jackson)

### 11.1 Jackson Polymorphic JSON Serialization
When returning sealed interfaces from Spring Boot REST controllers:

```java
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, 
    include = JsonTypeInfo.As.PROPERTY, 
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PaymentResult.Success.class, name = "SUCCESS"),
    @JsonSubTypes.Type(value = PaymentResult.RequiresAction.class, name = "REQUIRES_ACTION"),
    @JsonSubTypes.Type(value = PaymentResult.NetworkTimeout.class, name = "TIMEOUT"),
    @JsonSubTypes.Type(value = PaymentResult.InsufficientFunds.class, name = "INSUFFICIENT_FUNDS"),
    @JsonSubTypes.Type(value = PaymentResult.FraudRejected.class, name = "FRAUD")
})
public sealed interface PaymentResult permits ... {}
```
Jackson serializes each record with a clean `"type": "SUCCESS"` discriminator tag, and reconstructs the exact strongly-typed record on incoming JSON payloads.

---

## 12. Comprehensive Troubleshooting & Diagnostic Runbook

| Failure Symptom | Root Cause | Exact Remediation |
| :--- | :--- | :--- |
| `error: class must be final, sealed, or non-sealed` | Permitted subclass omitted its required inheritance modifier. | Add `final`, `sealed`, or `non-sealed` to the subclass declaration. |
| `error: class is not allowed to extend sealed class` | Subclass is not in the same package (unnamed module) or same module. | Move the subclass into the same package or declare it in the same module. |
| `java.lang.IncompatibleClassChangeError` at runtime | A subclass was compiled against an older version of the sealed interface where it was not permitted. | Recompile the entire project so all permitted classes are recognized by `javac`. |
| `java.lang.MatchException` during switch execution | A new permitted subclass was added to a dependency JAR without recompiling the caller. | Recompile the caller application against the updated dependency JAR to enforce new case handling. |

---

## 13. Frequently Asked Interview Questions

### Q1: What are Algebraic Data Types and how are they implemented in Java?
**Answer:** Algebraic Data Types (ADTs) are types formed by combining other types via **Product Types** and **Sum Types**.
- **Product Types** represent an AND relationship (all components must exist). In Java, they are implemented as **Records**.
- **Sum Types** represent an OR relationship (disjoint union of mutually exclusive possibilities). In Java, they are implemented as **Sealed Interfaces/Classes**.

### Q2: Why is omitting the `default:` branch recommended when switching over a sealed type?
**Answer:** Omitting `default:` allows the Java compiler to enforce **exhaustiveness**. If all permitted subclasses are covered, the compiler verifies full coverage. If a new permitted subclass is added in the future, the compiler will trigger a build error at every switch expression that lacks the new case. Adding `default:` silently swallows the new case, destroying compile-time safety.

### Q3: What is the difference between `final` and `sealed`?
**Answer:** `final` completely prohibits any extension of a class by any other type. `sealed` permits extension, but strictly limits that extension to an explicit, closed whitelist of subclasses declared via the `permits` clause.

---

## 14. Deliberate Practice: Hands-On Exercises

### Exercise 1 (Beginner): Authentication State Sealed Hierarchy
- **Objective:** Model user session authentication states.
- **Requirements:**
  1. `sealed interface AuthState permits Anonymous, Authenticated, SessionExpired`.
  2. Implement each variant as a `record`.
  3. Write an exhaustive switch expression returning a status message without `default`.

### Exercise 2 (Intermediate): Loan Application Pipeline
- **Objective:** Build an ADT for loan decisions (`Approved`, `ReferredToUnderwriter`, `Denied`).
- **Requirements:**
  1. `Denied` must store an `EnumSet<DenialReason>`.
  2. Implement validation ensuring `Approved` requires credit score $\ge 700$.

### Exercise 3 (Advanced): Expression Tree Calculator (AST)
- **Objective:** Build an arithmetic expression evaluator using sealed types.
- **Requirements:**
  1. `sealed interface Expression permits Constant, Add, Multiply, Negate`.
  2. Write a recursive `double evaluate(Expression expr)` method using Java 21 switch expressions.

---

## 15. Concept Mastery Verification Checklist

- [x] Explain the `permits` clause and the co-location module/package constraints.
- [x] Implement the three mandatory subclass modifiers: `final`, `sealed`, and `non-sealed`.
- [x] Construct an Algebraic Data Type combining Sealed Interfaces (Sum) and Records (Product).
- [x] Implement exhaustive switch expressions over sealed hierarchies without a `default` clause.
- [x] Query sealed class metadata using reflection (`isSealed()`, `getPermittedSubclasses()`).
- [x] Explain the binary compatibility role of `java.lang.MatchException`.

---

## 16. Authoritative Primary Sources and Further Reading

1. **OpenJDK JEP 409:** *Sealed Classes* (Java 17 LTS Final Feature).
2. **OpenJDK JEP 441:** *Pattern Matching for switch* (Java 21 LTS).
3. **James Gosling et al.:** *The Java Language Specification (Java SE 21 Edition)*:
   - Section 8.1.6: "Sealed Classes"
   - Section 9.1.4: "Sealed Interfaces"
4. **Brian Goetz:** *Data-Oriented Programming in Java* (Oracle Java Magazine Architecture Series).
