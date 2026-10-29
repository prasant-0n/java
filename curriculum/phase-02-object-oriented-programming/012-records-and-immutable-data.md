# Phase 02: Object-Oriented Programming
# Concept 012: Records: Immutable Data Carriers, Compact Constructors, and DTOs

---

## 1. Metadata and Learning Goals

| Metadata Field | Value |
|---|---|
| **Concept ID** | `P02-C012` |
| **Concept Title** | Records: Immutable Data Carriers, Compact Constructors, and DTOs |
| **Phase** | Phase 02 — Object-Oriented Programming |
| **Phase Directory** | `curriculum/phase-02-object-oriented-programming` |
| **Source Reference** | `java-backend-engineering-prerequisites-roadmap.md` > Phase 2 > Additional language constructs > Records and immutable data carriers |
| **Concept Type** | Language / Architecture / Concurrency |
| **Difficulty Level** | Intermediate |
| **Curriculum Importance** | Essential |
| **Java Baseline** | Java 21 LTS (with JEP 395 Records & JEP 440 Record Patterns) |
| **Framework Baseline** | Spring Boot 3.3.x / Jackson 2.17 / Hibernate 6.x |
| **Prerequisites** | [`001-classes-objects-constructors.md`](001-classes-objects-constructors.md), [`003-access-modifiers-encapsulation.md`](003-access-modifiers-encapsulation.md), [`011-nested-and-inner-classes.md`](011-nested-and-inner-classes.md) |
| **Related Concepts** | [`013-sealed-classes-and-interfaces.md`](013-sealed-classes-and-interfaces.md), [`014-pattern-matching-oop.md`](014-pattern-matching-oop.md), [`015-object-class-contracts.md`](015-object-class-contracts.md) |
| **Estimated Study Effort** | 3–4 hours |
| **Last Research Date** | 2026-10-30 |
| **Verification State** | COMPLETE |

### Measurable Learning Objectives
By the end of this concept module, you will be able to:
1. **Explain:** Explain the semantics of Java Records (JEP 395) as transparent, immutable data carriers extending `java.lang.Record`, and articulate the role of the `Record` classfile attribute.
2. **Compare:** Contrast Records with traditional POJOs, Lombok `@Value`/`@Data` annotations, and TypeScript interfaces, identifying key differences in JVM bytecode generation, encapsulation, and serialization.
3. **Implement:** Construct rich domain records with compact constructors, custom canonical validations, defensive copies for deep immutability, and custom instance methods.
4. **Debug & Diagnose:** Identify and eliminate common record traps, including shallow immutability mutations, JPA `@Entity` proxying incompatibilities, and accessor naming mismatches (`getId()` vs `id()`).
5. **Evaluate & Justify:** Articulate why Record serialization is immune to bypass attacks, and deconstruct records using Java 21 LTS Record Patterns (JEP 440).

---

## 2. Definition, Purpose, and Mental Model

### 2.1 Formal Definition
Introduced in Java 14 and standardized in Java 16 via **JEP 395** (and codified in JLS §8.10), a **Record** is a specialized, nominal class type designed to act as an **unambiguous, transparent carrier for immutable data**.

When a developer declares a record:
```java
public record UserSummary(String userId, String email, long balanceCents) {}
```
The Java compiler automatically generates:
1. A `final` class extending `java.lang.Record`.
2. `private final` fields corresponding to each component in the record header.
3. A public **canonical constructor** whose parameter list matches the component declaration.
4. Public read accessors named after the components (e.g., `userId()`, `email()`, `balanceCents()`), NOT Java Bean getters (`getUserId()`).
5. Compiler-generated, value-based implementations of `equals()`, `hashCode()`, and `toString()` backed by `invokedynamic`.

### 2.2 The Problem It Solves: The Ceremony of POJOs and Lombok Friction
Before Java Records, creating a simple 3-field Data Transfer Object (DTO) in Java required over 60 lines of boilerplate code:
- Repetitive field declarations (`private final String userId;`).
- Verbose constructor assigning parameters to fields.
- Tedious getter methods.
- Complex, error-prone `equals()` and `hashCode()` implementations that had to be manually updated whenever a field was added.
- A `toString()` implementation that had to be maintained.

Developers adopted third-party annotation processors like **Project Lombok** (`@Value`, `@Data`, `@Builder`). However, Lombok relies on non-standard internal `javac` compiler hacking, causing brittle build pipelines across major JDK upgrades, confusing IDE indexing, and zero native JVM runtime support.

**Java Records** solve this natively at the language and JVM specification levels:
- **Zero Boilerplate:** 1 line replaces 60 lines.
- **Language-Level Guarantee:** The API of a record *is* its state. There are no hidden fields or encapsulated mutations.
- **Native JVM Optimizations:** JIT compiler scalar replacement and pattern matching deconstruction work directly with records.

### 2.3 JavaScript/MERN vs Java Mental Model Gap

In TypeScript/Node.js, DTOs are defined as loose interfaces or object literals:

```typescript
// TypeScript Interface (Erased at runtime):
interface CreatePaymentRequest {
    readonly transactionId: string;
    readonly amountCents: number;
    readonly tags: string[];
}
// In JavaScript/Express:
const req = { transactionId: "tx_1", amountCents: 5000, tags: ["express"] };
req.tags.push("injected"); // RUNTIME MUTATION! 'readonly' does not exist on V8 heap!
```

In Java 21 LTS:
- A `record` is a nominal, physical `final` class on the JVM Heap.
- Fields are physically immutable in hardware via the Java Memory Model freeze barrier.
- Records enforce **compact constructors** where defensive copies (`List.copyOf`) can be made to guarantee **deep immutability**, preventing caller mutation permanently.

---

## 3. Foundations and Theory: Compiler Mechanics and Bytecode

### 3.1 Dissecting Record Bytecode with `javap`
Consider a simple record:

```java
package com.example.model;

public record CurrencyAmount(String currency, long amountCents) {}
```

Disassembling `CurrencyAmount.class` using `javap -c -p -v CurrencyAmount.class` reveals:

```java
public final class com.example.model.CurrencyAmount extends java.lang.Record {
    // 1. Immutable private final instance fields
    private final java.lang.String currency;
    private final long amountCents;

    // 2. Canonical constructor
    public com.example.model.CurrencyAmount(java.lang.String, long);
      Code:
         0: aload_0
         1: invokespecial #1 // Method java/lang/Record."<init>":()V
         4: aload_0
         5: aload_1
         6: putfield      #7 // Field currency:Ljava/lang/String;
         9: aload_0
        10: lload_2
        11: putfield      #13 // Field amountCents:J
        14: return

    // 3. Component accessor methods (NO "get" prefix!)
    public java.lang.String currency();
    public long amountCents();

    // 4. Value-based methods generated via invokedynamic!
    public final java.lang.String toString();
    public final int hashCode();
    public final boolean equals(java.lang.Object);
}
```

### 3.2 The `invokedynamic` Optimization for `equals()`, `hashCode()`, and `toString()`
Notice how `equals()`, `hashCode()`, and `toString()` are compiled. Instead of generating hundreds of bytes of repetitive bytecode comparing each field, `javac` emits an `invokedynamic` instruction linked to OpenJDK's `ObjectMethods.bootstrap(...)`:

```
invokedynamic #21,  0 // InvokeDynamic #0:equals:(Lcom/example/model/CurrencyAmount;Ljava/lang/Object;)Z
```

At class loading time, the JVM's `ObjectMethods` bootstrap method dynamically generates an optimized method handle tree that compares fields directly. This:
- Drastically reduces classfile binary size.
- Allows HotSpot to inline comparisons into single CPU register instructions.
- Guarantees that any change to the component list automatically updates `equals()`, `hashCode()`, and `toString()` without bytecode recompilation inconsistencies.

### 3.3 The `Record` Classfile Attribute
Under JLS §8.10, the compiled `.class` file contains a dedicated `Record` attribute:
```
Record:
   java.lang.String currency
   long amountCents
```
Frameworks (Jackson, Spring Data, Hibernate) query this attribute via `Class.isRecord()` and `Class.getRecordComponents()` at runtime to inspect parameter names without needing the `-parameters` compilation flag!

---

## 4. Practical Syntax, Usage, and Best Practices

### 4.1 Compact Constructors: Invariants and Normalization
Java records introduce the **Compact Constructor** syntax. It omits the parameter list entirely and allows you to validate or normalize components *before* fields are assigned:

```java
public record OrderId(String value) {

    // COMPACT CONSTRUCTOR: No parameter list, no explicit this.value = value;
    public OrderId {
        Objects.requireNonNull(value, "OrderId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("OrderId cannot be blank");
        }
        // Data normalization: Trims and uppercases input
        value = value.trim().toUpperCase(); 
        // javac automatically assigns: this.value = value; at the end of the block!
    }
}
```

### 4.2 The Deep Immutability Trap & Defensive Copying
> [!CAUTION]
> **Records are only SHALLOWLY immutable by default!**
> If a record contains a reference to a mutable object (such as an `ArrayList`, `Date`, or `byte[]`), an external caller can mutate the internal state of that object, completely destroying the record's immutability!

```java
// VULNERABLE RECORD: Leaks mutable list!
public record ShoppingCart(String cartId, List<String> items) {}
```

```java
// Corrupting code:
List<String> list = new ArrayList<>(List.of("Apple"));
ShoppingCart cart = new ShoppingCart("C-1", list);
list.add("Stolen Item"); // CART WAS MUTATED FROM OUTSIDE!
cart.items().clear();    // CART WAS EMPTIED VIA GETTER!
```

#### The Production-Grade Solution: Deep Immutability via Defensive Copying
In the compact constructor and the accessor, always defend mutable collections:

```java
public record ShoppingCart(String cartId, List<String> items) {

    public ShoppingCart {
        Objects.requireNonNull(cartId, "cartId required");
        // Defensive copy on construction:
        items = (items == null) ? List.of() : List.copyOf(items);
    }

    // Accessor override (optional here since List.copyOf produces an unmodifiable list)
    @Override
    public List<String> items() {
        return items; // Safe because List.copyOf is immutable
    }
}
```

### 4.3 Custom Instance Methods and Interfaces
Records can implement interfaces and declare custom domain methods, but they **cannot declare non-static instance fields**:

```java
public record Money(long amountCents, String currency) implements Comparable<Money> {

    // Static fields are allowed
    public static final Money ZERO_USD = new Money(0, "USD");

    public Money {
        if (amountCents < 0) throw new IllegalArgumentException("amount cannot be negative");
        Objects.requireNonNull(currency, "currency required");
    }

    // Rich domain methods
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + this.currency + " vs " + other.currency);
        }
        return new Money(Math.addExact(this.amountCents, other.amountCents), this.currency);
    }

    @Override
    public int compareTo(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare different currencies");
        }
        return Long.compare(this.amountCents, other.amountCents);
    }
}
```

---

## 5. Memory Model, Concurrency, and Runtime Characteristics

### 5.1 The Java Memory Model (JMM) Freeze Barrier Guarantee
Under JLS §17.5 (Final Field Semantics), when a record is instantiated via its canonical constructor, all fields are `final`.
The JVM emits a **freeze barrier** (store-store memory fence) at the end of the constructor.

**What does this mean for concurrency?**
If Thread A constructs a `Record` instance and publishes it to Thread B via an unsynchronized reference (e.g., a shared non-volatile variable):
- Thread B is **mathematically guaranteed** to see all record component values fully initialized and consistent.
- Thread B will **never** observe default zero/null values for record fields.
- Records provide lock-free, safe publication across concurrent threads by specification.

### 5.2 Ironclad Serialization Protocol (JVM Specification §1.13)
In traditional Java classes, deserialization is a notorious security hazard because `ObjectInputStream` instantiates objects without calling constructors, bypassing all domain validation invariants.

Records completely eliminate this vulnerability:
- The Java Object Serialization specification mandates that **records MUST be deserialized via their canonical constructor**!
- When `readObject()` deserializes a record, it extracts the component stream values and invokes:
  `canonicalConstructor.newInstance(componentValues...)`
- All validations in your compact constructor (null checks, boundary assertions) **run during deserialization**!
- Attackers cannot inject corrupted or illegal state through serialized byte streams.

---

## 6. Failure Modes, Edge Cases, and Anti-Patterns

### Anti-Pattern 1: Using Records as JPA/Hibernate `@Entity` Classes
Developers often try to use records for database entities:

```java
// DISASTER: WILL FAIL AT RUNTIME IN HIBERNATE!
@Entity
public record UserEntity(@Id Long id, String email) {}
```

#### Why JPA Entities CANNOT Be Records:
1. **Hibernate Requires Non-Final Classes:** Hibernate uses ByteBuddy/CGLIB to generate runtime dynamic proxies for lazy loading (`@ManyToOne(fetch = FetchType.LAZY)`). Proxies extend the entity class. Because records are strictly `final` (JLS §8.10), Hibernate cannot subclass them!
2. **Hibernate Requires No-Arg Constructors:** JPA specification §2.1 mandates a public or protected no-argument constructor for reflection instantiation. Records have no no-arg constructor unless explicitly defined, and fields cannot be populated post-construction.
3. **Hibernate Dirty Checking Requires Mutability:** Hibernate tracks changes to entities during a transaction and flushes changes to SQL. Records are strictly immutable.

> [!TIP]
> **Where Records Fit in JPA:** Use records for **DTO Projections**!
> ```java
> @Query("SELECT new com.example.dto.UserSummary(u.id, u.email) FROM User u WHERE u.active = true")
> List<UserSummary> findActiveSummaries();
> ```

### Anti-Pattern 2: The Java Bean Getter Naming Trap
Java Beans define accessors with a `get` prefix (`getId()`, `getEmail()`).
Records deliberately do NOT use the `get` prefix. Their accessors are simply `id()` and `email()`.

```java
UserSummary summary = new UserSummary("U1", "a@b.com", 100L);

// COMPILE ERROR:
// String id = summary.getUserId(); 

// CORRECT:
String id = summary.userId();
```

*Framework Note:* Legacy template engines (e.g., JSP, old FreeMarker) or outdated reflection utilities looking specifically for `getXXX()` methods will fail unless updated to Jackson 2.12+ or Spring 5.3+, which natively recognize records.

---

## 7. Comprehensive Code Example: High-Precision Financial Audit DTO Pipeline

The following production-grade implementation synthesizes:
- A rich `Record` modeling a financial ledger transaction.
- Compact constructor enforcing Banker's invariants and defensive copying.
- Custom non-canonical constructor delegating with defaults.
- Java 21 LTS Record Pattern deconstruction.
- Clean value equality assertions.

```java
package com.example.finance;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

// =============================================================================
// 1. VALUE OBJECT RECORD: MONEY
// =============================================================================

public record Money(long amountCents, String currency) {
    public Money {
        Objects.requireNonNull(currency, "currency must not be null");
        if (currency.length() != 3) {
            throw new IllegalArgumentException("currency must be 3-letter ISO code: " + currency);
        }
    }

    public static Money of(long cents, String currency) {
        return new Money(cents, currency);
    }
}

// =============================================================================
// 2. DOMAIN AUDIT RECORD (DEEP IMMUTABILITY & COMPACT VALIDATION)
// =============================================================================

public record AuditTransaction(
    String transactionId,
    Money grossAmount,
    Money taxAmount,
    List<String> auditTags,
    Instant timestamp
) {

    // COMPACT CONSTRUCTOR: Invariants & Defensive Copies
    public AuditTransaction {
        Objects.requireNonNull(transactionId, "transactionId required");
        Objects.requireNonNull(grossAmount, "grossAmount required");
        Objects.requireNonNull(taxAmount, "taxAmount required");

        if (!grossAmount.currency().equals(taxAmount.currency())) {
            throw new IllegalArgumentException("Currency mismatch between gross and tax");
        }

        // Deep Immutability Defense: Copy mutable list into immutable view
        auditTags = (auditTags == null) ? List.of() : List.copyOf(auditTags);

        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    // NON-CANONICAL CONSTRUCTOR: Convenience overload delegating via this(...)
    public AuditTransaction(Money grossAmount, Money taxAmount, List<String> tags) {
        this(UUID.randomUUID().toString(), grossAmount, taxAmount, tags, Instant.now());
    }

    // Rich domain calculation method
    public Money calculateNetAmount() {
        long netCents = Math.subtractExact(grossAmount.amountCents(), taxAmount.amountCents());
        return new Money(netCents, grossAmount.currency());
    }
}
```

---

## 8. Testing Strategies and Verification Suite

The following standalone verification test suite tests:
1. Compact constructor validation and invariant rejection.
2. Deep immutability defense against external list mutation.
3. Automatic value-based equality (`equals()` and `hashCode()`).
4. Java 21 Record Pattern matching deconstruction.

```java
package com.example.finance.test;

import com.example.finance.AuditTransaction;
import com.example.finance.Money;

import java.util.ArrayList;
import java.util.List;

public class RecordDomainVerificationTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("Starting Java Record Verification Tests...\n");

        testCompactConstructorValidation();
        testDeepImmutabilityDefense();
        testValueBasedEqualityAndHashCode();
        testJava21RecordPatternDeconstruction();

        System.out.println("\n--------------------------------------------------");
        System.out.printf("Test Suite Results: %d Passed, %d Failed\n", passed, failed);
        System.out.println("--------------------------------------------------");

        if (failed > 0) {
            System.err.println("TEST SUITE FAILED");
            System.exit(1);
        } else {
            System.out.println("ALL RECORD DOMAIN TESTS PASSED CLEANLY (100%).");
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

    // TEST 1: COMPACT CONSTRUCTOR ENFORCES INVARIANTS
    private static void testCompactConstructorValidation() {
        Money m1 = new Money(1000L, "USD");
        Money m2 = new Money(100L, "EUR"); // Mismatched currency

        boolean caughtMismatch = false;
        try {
            new AuditTransaction(m1, m2, List.of("tag1"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Currency mismatch")) {
                caughtMismatch = true;
            }
        }
        assertTrue(caughtMismatch, "Compact constructor rejected mismatched currencies");

        boolean caughtInvalidCurrency = false;
        try {
            new Money(500L, "INVALID_CODE");
        } catch (IllegalArgumentException e) {
            caughtInvalidCurrency = true;
        }
        assertTrue(caughtInvalidCurrency, "Money rejected non-3-letter currency code");
    }

    // TEST 2: DEEP IMMUTABILITY DEFENSE (MUTABLE LIST ATTACK)
    private static void testDeepImmutabilityDefense() {
        List<String> mutableTags = new ArrayList<>();
        mutableTags.add("audit-init");

        Money gross = new Money(5000L, "USD");
        Money tax = new Money(500L, "USD");

        AuditTransaction tx = new AuditTransaction(gross, tax, mutableTags);

        // Attack 1: Mutate the original list passed to constructor
        mutableTags.add("injected-malicious-tag");
        assertEquals(1, tx.auditTags().size(), "Record immune to external list mutation via defensive copy");

        // Attack 2: Attempt to mutate list via accessor
        boolean caughtUnsupported = false;
        try {
            tx.auditTags().add("illegal-tag");
        } catch (UnsupportedOperationException e) {
            caughtUnsupported = true;
        }
        assertTrue(caughtUnsupported, "Record accessor returns strictly unmodifiable collection");
    }

    // TEST 3: VALUE-BASED EQUALITY AND HASHCODE
    private static void testValueBasedEqualityAndHashCode() {
        Money m1 = new Money(2500L, "USD");
        Money m2 = new Money(2500L, "USD");

        assertEquals(m1, m2, "Records with identical state are equal via generated equals()");
        assertEquals(m1.hashCode(), m2.hashCode(), "Records with identical state have equal hashCode()");

        AuditTransaction tx1 = new AuditTransaction("TX-100", m1, new Money(200L, "USD"), List.of("settled"), null);
        AuditTransaction tx2 = new AuditTransaction("TX-100", m2, new Money(200L, "USD"), List.of("settled"), tx1.timestamp());

        assertEquals(tx1, tx2, "AuditTransactions with identical state are strictly equal");
    }

    // TEST 4: JAVA 21 LTS RECORD PATTERN DECONSTRUCTION
    private static void testJava21RecordPatternDeconstruction() {
        Object obj = new AuditTransaction("TX-200", new Money(10000L, "USD"), new Money(1000L, "USD"), List.of("vip"), null);

        // JAVA 21 LTS RECORD PATTERN MATCHING (JEP 440)
        String summary = "UNKNOWN";
        if (obj instanceof AuditTransaction(String id, Money(long grossCents, String curr), Money tax, var tags, var time)) {
            summary = String.format("TX [%s] Gross: %d %s, Tags: %d", id, grossCents, curr, tags.size());
        }

        assertEquals("TX [TX-200] Gross: 10000 USD, Tags: 1", summary, "Java 21 Record Pattern deconstructed nested record fields directly");
    }
}
```

---

## 9. Modern Java Evolution: Pattern Matching and Record Patterns

### 9.1 The Road to Modern Records
- **Java 14 (JEP 359):** First preview of Records.
- **Java 15 (JEP 384):** Second preview; added local records.
- **Java 16 (JEP 395):** Standard production release of Records.
- **Java 21 LTS (JEP 440):** **Record Patterns**.

### 9.2 Java 21 Record Patterns in Switch (JEP 440 / JEP 441)
In Java 21 LTS, records integrate seamlessly with switch expressions to eliminate casting and extract components in a single step:

```java
public String describeTransaction(Object obj) {
    return switch (obj) {
        case AuditTransaction(String id, Money(long gross, String curr), var tax, var tags, var ts) when gross > 1_000_000L ->
            "High-Value Transaction: " + id + " (" + (gross / 100) + " " + curr + ")";

        case AuditTransaction(String id, var gross, var tax, var tags, var ts) ->
            "Standard Transaction: " + id;

        case null -> "Null Transaction";
        default -> "Unrecognized Object";
    };
}
```

---

## 10. System Architecture, Clean Architecture, and Design Patterns

### 10.1 Where Records Belong in Hexagonal Architecture

```
+------------------------------------------------------------------------------------+
|  CORE DOMAIN LAYER                                                                 |
|  Money.java (Record - Value Object)                                                |
|  OrderId.java (Record - Strongly-Typed Identifier)                                 |
+------------------------------------------------------------------------------------+
       ▲                                                           ▲
       │                                                           │
+------------------------------+             +---------------------------------------+
|  DATABASE PERSISTENCE        |             |  WEB API / REST CONTROLLERS           |
|  UserProjectionDto.java      |             |  CreatePaymentRequest.java (Record)   |
|  (Record for SQL Queries)    |             |  PaymentResponse.java (Record)        |
+------------------------------+             +---------------------------------------+
```

1. **REST API DTOs:** Records represent HTTP request and response payloads. Jackson serializes and deserializes them natively with zero annotations.
2. **Domain-Driven Design (DDD) Value Objects:** Any object defined purely by its attributes rather than its identity (e.g., `Address`, `Money`, `DateRange`) should be modeled as a Record.
3. **Database Projections:** Clean, read-only SQL projection targets in Spring Data JPA queries.

---

## 11. Cross-Domain Integrations (Spring Boot, Jackson, Hibernate)

### 11.1 Jackson 2.12+ Native Record Support
Jackson natively detects the `Record` classfile attribute:
```java
public record UserRequest(String email, String role) {}
```
Jackson inspects the canonical constructor, reads JSON properties matching component names, and invokes the canonical constructor directly without requiring `@JsonProperty` annotations!

### 11.2 Spring Data JPA Projection Queries
```java
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // Native Record Constructor Expression
    @Query("SELECT new com.example.dto.UserSummary(u.id, u.email, u.active) FROM UserEntity u")
    List<UserSummary> findAllSummaries();
}
```

---

## 12. Comprehensive Troubleshooting & Diagnostic Runbook

| Failure Symptom | Root Cause | Exact Remediation |
| :--- | :--- | :--- |
| Record contents changed unexpectedly across threads | Record holds a reference to a mutable collection (`ArrayList`, `HashSet`) or mutable object. | In the compact constructor, wrap mutable collections with `List.copyOf(...)` or defensive copies. |
| Hibernate crashes on startup: `Cannot instantiate record as entity` | Attempted to place `@Entity` on a record class. | Entities must be standard classes. Use records solely for DTO projections or `@Embeddable` structs. |
| `NoSuchMethodError: UserSummary.getUserId()` | Code expects Java Bean standard `get` prefix. | Call `userSummary.userId()` instead of `getUserId()`. |
| Deserialization fails with `IllegalArgumentException` in compact constructor | Inbound JSON contains invalid data that violates record invariants. | Catch error in Spring `@ControllerAdvice` and translate to HTTP `400 Bad Request`. |

---

## 13. Frequently Asked Interview Questions

### Q1: Why can't a Java Record extend another class?
**Answer:** Because all records implicitly extend `java.lang.Record`. Since Java does not support multiple implementation inheritance (a class can only extend one superclass), a record cannot declare an `extends` clause for another class. However, records can implement any number of interfaces.

### Q2: What is a Compact Constructor and how does it differ from a Canonical Constructor?
**Answer:** A **canonical constructor** has a parameter list matching the record's components and assigns them to the fields (`this.x = x;`). A **compact constructor** omits the parameter list entirely (`public MyRecord { ... }`). It is used for validation and data normalization; the compiler automatically generates the field assignments (`this.x = x;`) at the conclusion of the compact constructor body.

### Q3: Why are Records preferred over Lombok `@Value` in modern Java applications?
**Answer:**
1. **JVM Native Support:** Records are part of the Java Language Specification (JLS §8.10) and JVM classfile specification (`Record` attribute).
2. **Safe Serialization:** Records use a secure, constructor-based serialization protocol that cannot be bypassed.
3. **Pattern Matching:** Java 21 Record Patterns allow direct, structural deconstruction of records in `switch` and `instanceof`.
4. **Tooling Stability:** Lombok relies on internal `javac` AST modification which frequently breaks on new JDK releases; records work natively across all tools.

---

## 14. Deliberate Practice: Hands-On Exercises

### Exercise 1 (Beginner): Strongly-Typed Email Value Object
- **Objective:** Create an immutable `EmailAddress` record.
- **Requirements:**
  1. Validate that the email contains `@` and `.`.
  2. Normalize the email by trimming whitespace and lowercasing.
  3. Verify that `new EmailAddress("  Test@Example.COM ").value()` returns `"test@example.com"`.

### Exercise 2 (Intermediate): Deeply Immutable Product Catalog DTO
- **Objective:** Implement a `ProductDto` record containing `sku`, `priceCents`, and `List<String> categories`.
- **Requirements:**
  1. Compact constructor must defensively copy `categories`.
  2. Prove that attempting to mutate the list returned by `product.categories()` throws `UnsupportedOperationException`.

### Exercise 3 (Advanced): Recursive AST Expression Deconstruction
- **Objective:** Build an algebraic calculator using sealed interfaces and records.
- **Requirements:**
  1. `sealed interface Expr permits Constant, Add, Multiply`.
  2. `record Constant(double val) implements Expr`.
  3. `record Add(Expr left, Expr right) implements Expr`.
  4. Write a recursive evaluator using Java 21 Record Patterns.

---

## 15. Concept Mastery Verification Checklist

- [x] Explain the compiler desugaring of a record into a `final` class extending `java.lang.Record`.
- [x] Articulate why records use component accessors (`x()`) rather than Java Bean getters (`getX()`).
- [x] Implement a compact constructor enforcing validation invariants and data normalization.
- [x] Defend records against the shallow immutability trap using defensive copies (`List.copyOf`).
- [x] Explain the safe publication freeze barrier guarantee in the Java Memory Model for records.
- [x] Deconstruct nested records using Java 21 LTS Record Patterns (JEP 440).
- [x] Explain why records cannot be used as JPA `@Entity` classes and where they fit in persistence architecture.

---

## 16. Authoritative Primary Sources and Further Reading

1. **OpenJDK JEP 395:** *Records* (Java 16 Final Feature).
2. **OpenJDK JEP 440:** *Record Patterns* (Java 21 LTS).
3. **James Gosling, Bill Joy, Guy Steele, Gilad Bracha, Alex Buckley, Gavin Bierman:** *The Java Language Specification (Java SE 21 Edition)*, Chapter 8.10: "Record Types".
4. **Brian Goetz (Java Language Architect):** *Data Classes and Sealed Types for Java* (Oracle Architecture Specification).
