# Phase 02: Object-Oriented Programming
# Concept 008: Composition over Inheritance in Business Domain Services

---

## 1. Metadata & Header

| Field | Value |
| :--- | :--- |
| **Concept ID** | `P02-C008` |
| **Concept Title** | Composition over Inheritance in Business Domain Services |
| **Curriculum Phase** | Phase 02: Object-Oriented Programming |
| **Roadmap Topics Covered** | Composition versus inheritance; Why composition is frequently preferred for business logic; Object relationships and dependency management |
| **Direct Prerequisites** | `P02-C003` (Access Modifiers & Encapsulation), `P02-C004` (Four Pillars of OOP), `P02-C005` (Inheritance & Fragile Base Class), `P02-C007` (Abstract Classes vs Interfaces) |
| **Downstream Dependencies** | `P02-C009` (Object Relationships), `P10-C001` (Dependency Injection), `P16-C001` (SOLID Principles) |
| **Delivery Date** | 2026-10-30 |
| **Author / Status** | Antigravity Engine / Complete |

---

## 2. Conceptual Anchor: Concept Overview & Mental Model

The Gang of Four (GoF) classic design axiom states: **"Favor object composition over class inheritance."** 

In enterprise Java backend development, this is not an aesthetic preference; it is a critical architectural survival principle. Class inheritance (`extends`) binds a subclass to its superclass at compile time with the tightest coupling possible in the Java language. **Composition** (`has-a`), by contrast, connects independent components through well-defined interface contracts at runtime, enabling modularity, testability, and resilience to change.

```
+---------------------------------------------------------------------------------------------------+
|                            WHITE-BOX REUSE vs BLACK-BOX REUSE DYNAMICS                            |
+---------------------------------------------------------------------------------------------------+
|  INHERITANCE (White-Box Reuse)   | Subclass depends on internal superclass execution paths,       |
|                                  | private field layouts, and self-invocation patterns. Invariants |
|                                  | in the parent bleed into the child, creating fragile systems.  |
|----------------------------------+----------------------------------------------------------------|
|  COMPOSITION (Black-Box Reuse)   | Components interact strictly through public interface          |
|                                  | contracts. Internal implementation details are fully hidden;   |
|                                  | components can be swapped, mocked, or reconfigured dynamically.|
+---------------------------------------------------------------------------------------------------+
```

### The JavaScript/MERN Transition Gap: Mixins & Object Spread vs Nominal Composition

If your background is in Node.js, JavaScript, or TypeScript, composition in Java takes a different structural form:

```
JavaScript (Dynamic Prototypal Mixins & Object Spread):
// Dynamic property merging:
const withLogging = (target) => ({ ...target, log: () => console.log("audit") });
const service = withLogging({ process: () => true });
* In JavaScript, composition is often achieved via object spreads, functional closures, or mixins.
* Mixins merge properties dynamically into a single dictionary object.

Java (Strongly Typed Structural Composition & Dependency Injection):
public class OrderFulfillmentService {
    private final PaymentGateway paymentGateway;       // Injected dependency (Has-A)
    private final InventoryClient inventoryClient;     // Injected dependency (Has-A)
    private final AuditLogger auditLogger;             // Injected dependency (Has-A)
    
    public OrderFulfillmentService(PaymentGateway pg, InventoryClient ic, AuditLogger al) {
        this.paymentGateway = Objects.requireNonNull(pg);
        this.inventoryClient = Objects.requireNonNull(ic);
        this.auditLogger = Objects.requireNonNull(al);
    }
}
* In Java, composition is realized by assembling independent heap-allocated objects through constructor injection.
* Each component retains its own strict encapsulation, type boundaries, and independent memory scope.
* No prototype mutation or dynamic monkey-patching occurs.
```

```mermaid
flowchart TD
    subgraph InheritanceTrap["The Inheritance Combinatorial Explosion (Brittle & Rigid)"]
        Base["AbstractOrderService"]
        S1["StandardOrderService"]
        S2["AuditedOrderService"]
        S3["FraudCheckingOrderService"]
        S4["AuditedFraudCheckingOrderService"]
        S5["AuditedFraudCheckingPriorityOrderService"]
        Base --> S1
        Base --> S2
        Base --> S3
        S2 --> S4
        S4 --> S5
    end

    subgraph CompositionSolution["The Composition Matrix (Modular & Flexible)"]
        OrderService["OrderFulfillmentService\n(Orchestrator)"]
        PG["<<interface>>\nPaymentGateway"]
        FD["<<interface>>\nFraudDetector"]
        NT["<<interface>>\nNotifier"]
        OrderService o-- PG
        OrderService o-- FD
        OrderService o-- NT
    end
```

---

## 3. Historical Context & The "Why"

In the early era of object-oriented programming (1980s through mid-1990s), inheritance was marketed as the silver bullet for software engineering: *"Model the real world as a hierarchical taxonomy of classes."*

Enterprise systems built with deep inheritance hierarchies rapidly collapsed under two catastrophic design pathologies:

### 1. The Combinatorial Subclass Explosion
Suppose an enterprise order service requires three optional features: **Fraud Detection**, **Audit Logging**, and **Priority Queueing**.
- With inheritance, if you need every possible combination of features, you must author **$2^N$ subclasses**:
  - `OrderService`
  - `AuditedOrderService`
  - `FraudCheckingOrderService`
  - `PriorityOrderService`
  - `AuditedFraudCheckingOrderService`
  - `AuditedPriorityOrderService`
  - `FraudCheckingPriorityOrderService`
  - `AuditedFraudCheckingPriorityOrderService`
- Adding a fourth feature (e.g., **Tax Calculation**) requires authoring 8 additional classes, exploding the hierarchy to 16 classes!
- **With Composition:** You author exactly **4 independent strategy components** and compose them into a single `OrderService` at runtime.

### 2. The "Yo-Yo Problem"
In deep inheritance hierarchies ($5+$ levels), understanding a single method execution requires the engineer to read `ChildClass`, then jump up to `ParentClass`, then down to an overridden method in `ChildClass`, then up to `GrandparentClass`, then down to `SiblingClass`. This cognitive oscillation is known in computer science as the **Yo-Yo Problem**, and it dramatically increases maintenance costs and bug defect rates.

---

## 4. Deep Technical Architecture & JVM Execution Flow

### 4.1 Physical Heap Layout: Monolithic Inheritance vs Composed Pointer Graphs

How do Inheritance and Composition differ in physical JVM Heap memory?

```
INHERITANCE MEMORY LAYOUT (Single Monolithic Object):
[ HotSpot Object Header: 12 Bytes ]
[ Superclass Fields: 24 Bytes     ]  <--- Fixed contiguous block in memory.
[ Subclass Fields:   16 Bytes     ]  <--- Cannot be detached or recycled independently.
[ Alignment Padding: 4 Bytes      ]
Total: 56 contiguous bytes on Heap.

COMPOSITION MEMORY LAYOUT (Graph of Independent Heap Objects):
[ OrderService Object: 12B Header + 3 References (12B) + Padding (8B) = 32 Bytes ]
       |                 |                 |
       v (Pointer)       v (Pointer)       v (Pointer)
[ StripeGateway: 24B ] [ FraudGuard: 32B ] [ EmailNotifier: 16B ]
```

#### Memory & Performance Trade-Off Analysis:
1. **Cache Locality:** A monolithic inherited object has superior CPU L1/L2 cache line locality because all fields reside in contiguous memory.
2. **Allocation Overhead:** Composed objects require multiple allocations (outer object + inner dependencies) and reference pointers (4 bytes each with CompressedOops).
3. **The JIT Compiler Countermeasure (Escape Analysis & Scalar Replacement):** In modern HotSpot (C2 Compiler), if composed objects do not escape the method or thread, the JIT compiler executes **Scalar Replacement**. It breaks the composed objects apart and places their fields directly into CPU hardware registers, completely eliminating heap allocation overhead!

---

## 5. Comprehensive Code Example: Multi-Channel Order Fulfillment Engine

The following enterprise implementation demonstrates:
- Composing domain services with decoupled strategy interfaces.
- Eliminating fragile inheritance in favor of constructor injection.
- Dynamic runtime strategy swapping.
- Comprehensive unit test isolation using mocks/stubs without reflection.

```java
package com.example.order;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

// =============================================================================
// DOMAIN IMMUTABLE MODELS
// =============================================================================

public record Order(
    String orderId,
    String customerId,
    long totalAmountCents,
    String currency,
    List<String> itemSkus
) {
    public Order {
        Objects.requireNonNull(orderId, "orderId required");
        Objects.requireNonNull(customerId, "customerId required");
        Objects.requireNonNull(currency, "currency required");
        if (totalAmountCents <= 0) throw new IllegalArgumentException("totalAmountCents must be positive");
    }
}

public record FulfillmentResult(
    String orderId,
    boolean successful,
    String paymentConfirmation,
    String trackingNumber,
    String failureReason
) {}

// =============================================================================
// COMPOSED STRATEGY INTERFACES (PLUGGABLE CAPABILITIES)
// =============================================================================

public interface PaymentGateway {
    String charge(String customerId, long amountCents, String currency);
}

public interface InventoryService {
    String reserveInventory(String orderId, List<String> itemSkus);
    void releaseInventory(String orderId);
}

public interface FraudDetector {
    boolean evaluateRisk(String customerId, long amountCents);
}

public interface NotificationClient {
    void notifyCustomer(String customerId, String message);
}

// =============================================================================
// BUSINESS SERVICE: COMPOSED ORCHESTRATOR (NO INHERITANCE HIERARCHIES!)
// =============================================================================

/**
 * OrderFulfillmentService orchestrates business workflows strictly by
 * COMPOSING decoupled capabilities via Constructor Injection.
 */
public class OrderFulfillmentService {

    // All dependencies are private final: Encapsulated, immutable, thread-safe
    private final PaymentGateway paymentGateway;
    private final InventoryService inventoryService;
    private final FraudDetector fraudDetector;
    private final NotificationClient notificationClient;

    public OrderFulfillmentService(
            PaymentGateway paymentGateway,
            InventoryService inventoryService,
            FraudDetector fraudDetector,
            NotificationClient notificationClient) {

        this.paymentGateway = Objects.requireNonNull(paymentGateway, "paymentGateway required");
        this.inventoryService = Objects.requireNonNull(inventoryService, "inventoryService required");
        this.fraudDetector = Objects.requireNonNull(fraudDetector, "fraudDetector required");
        this.notificationClient = Objects.requireNonNull(notificationClient, "notificationClient required");
    }

    public FulfillmentResult fulfillOrder(Order order) {
        Objects.requireNonNull(order, "order required");

        // Step 1: Evaluate Fraud Risk via composed detector
        if (!fraudDetector.evaluateRisk(order.customerId(), order.totalAmountCents())) {
            notificationClient.notifyCustomer(order.customerId(), "Order rejected due to risk profile.");
            return new FulfillmentResult(order.orderId(), false, null, null, "REJECTED_FRAUD_RISK");
        }

        // Step 2: Reserve Inventory via composed inventory client
        String reservationTicket = inventoryService.reserveInventory(order.orderId(), order.itemSkus());
        if (reservationTicket == null) {
            notificationClient.notifyCustomer(order.customerId(), "Order out of stock.");
            return new FulfillmentResult(order.orderId(), false, null, null, "OUT_OF_STOCK");
        }

        // Step 3: Process Payment via composed gateway
        String paymentRef;
        try {
            paymentRef = paymentGateway.charge(order.customerId(), order.totalAmountCents(), order.currency());
        } catch (Exception e) {
            // Roll back inventory on payment failure!
            inventoryService.releaseInventory(order.orderId());
            notificationClient.notifyCustomer(order.customerId(), "Payment failed: " + e.getMessage());
            return new FulfillmentResult(order.orderId(), false, null, null, "PAYMENT_FAILED: " + e.getMessage());
        }

        // Step 4: Dispatch Notification
        notificationClient.notifyCustomer(order.customerId(), "Order confirmed! Ticket: " + reservationTicket);

        return new FulfillmentResult(order.orderId(), true, paymentRef, reservationTicket, null);
    }
}
```

---

## 6. Edge Cases, Traps, and Compiler Mechanics

### 6.1 The "Self-Problem" in Delegation (Loss of Polymorphic Context)
When Object $A$ composes Object $B$, and delegates a call to $B$, Object $B$ has no knowledge of Object $A$.
```java
class Window {
    private final RenderingEngine engine;
    public void draw() { engine.render(); } // engine.render() cannot access 'Window' methods!
}
```
If the component needs to invoke callbacks on the enclosing container, you must explicitly pass `this` as a parameter:
`engine.render(this);` (The Inversion of Control / Callback Pattern).

### 6.2 Circular Composition Dependencies
If `ServiceA` requires `ServiceB` in its constructor, and `ServiceB` requires `ServiceA`:
```java
class ServiceA { public ServiceA(ServiceB b) {} }
class ServiceB { public ServiceB(ServiceA a) {} }
```
- At compile time, Java allows this.
- At runtime, constructing either service is impossible (`new ServiceA(new ServiceB(new ServiceA(...)))`).
- In Spring Boot, this triggers `BeanCurrentlyInCreationException` (Circular Dependency Failure).
- *Remediation:* Extract the shared coordinating logic into a third service (`ServiceC`), or decouple via domain events.

---

## 7. Cross-Cutting Concerns

### 7.1 Testing & Unit Test Isolation
The greatest engineering advantage of composition over inheritance is **effortless testability**:
- With inheritance, testing a subclass requires spinning up the entire superclass state, including database pools, network sockets, and static initializers.
- With composition, you can test `OrderFulfillmentService` in pure memory in 1 millisecond by injecting lightweight mock/stub implementations.

### 7.2 Independent Lock Scopes (Concurrency Isolation)
In inheritance, subclass and superclass share the same object monitor lock (`synchronized(this)`). If a caller locks the subclass, it inadvertently freezes superclass internals.
With composition, each composed dependency encapsulates its own private concurrency locks:
```java
public class SafeOrderService {
    private final PaymentGateway gateway; // Has its own internal locking
    private final Object localLock = new Object(); // Isolated local locking
}
```

---

## 8. Testing & Verification Strategies

The following standalone verification program validates dynamic strategy composition, inventory rollback on payment failure, fraud rejection isolation, and successful fulfillment without external frameworks:

```java
package com.example.order.test;

import com.example.order.FraudDetector;
import com.example.order.FulfillmentResult;
import com.example.order.InventoryService;
import com.example.order.NotificationClient;
import com.example.order.Order;
import com.example.order.OrderFulfillmentService;
import com.example.order.PaymentGateway;

import java.util.ArrayList;
import java.util.List;

public class CompositionVerificationTest {

    private static int passed = 0;
    private static int failed = 0;

    // Test Stub Implementations
    static class MockPaymentGateway implements PaymentGateway {
        boolean shouldFail = false;
        @Override
        public String charge(String customerId, long amountCents, String currency) {
            if (shouldFail) throw new RuntimeException("Card declined");
            return "CH_MOCK_" + amountCents;
        }
    }

    static class MockInventoryService implements InventoryService {
        boolean released = false;
        @Override
        public String reserveInventory(String orderId, List<String> itemSkus) {
            return "RESERVED_" + orderId;
        }
        @Override
        public void releaseInventory(String orderId) {
            released = true;
        }
    }

    static class MockFraudDetector implements FraudDetector {
        boolean isSafe = true;
        @Override
        public boolean evaluateRisk(String customerId, long amountCents) {
            return isSafe;
        }
    }

    static class MockNotificationClient implements NotificationClient {
        final List<String> notifications = new ArrayList<>();
        @Override
        public void notifyCustomer(String customerId, String message) {
            notifications.add(message);
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting Composition vs Inheritance Verification Tests...\n");

        testSuccessfulFulfillmentWorkflow();
        testFraudRejectionIsolation();
        testInventoryRollbackOnPaymentFailure();

        System.out.println("\n--------------------------------------------------");
        System.out.printf("Test Suite Results: %d Passed, %d Failed\n", passed, failed);
        System.out.println("--------------------------------------------------");

        if (failed > 0) {
            System.err.println("TEST SUITE FAILED");
            System.exit(1);
        } else {
            System.out.println("ALL COMPOSITION VERIFICATION TESTS PASSED CLEANLY (100%).");
        }
    }

    private static void assertEquals(Object expected, Object actual, String name) {
        if (!expected.equals(actual)) {
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

    private static void testSuccessfulFulfillmentWorkflow() {
        MockPaymentGateway payment = new MockPaymentGateway();
        MockInventoryService inventory = new MockInventoryService();
        MockFraudDetector fraud = new MockFraudDetector();
        MockNotificationClient notification = new MockNotificationClient();

        OrderFulfillmentService service = new OrderFulfillmentService(payment, inventory, fraud, notification);
        Order order = new Order("ORD-1", "CUST-1", 5000L, "USD", List.of("SKU-A"));

        FulfillmentResult result = service.fulfillOrder(order);

        assertTrue(result.successful(), "Order fulfillment reported success");
        assertEquals("CH_MOCK_5000", result.paymentConfirmation(), "Payment confirmation recorded");
        assertEquals("RESERVED_ORD-1", result.trackingNumber(), "Inventory reservation recorded");
        assertEquals(1, notification.notifications.size(), "Customer notified of success");
    }

    private static void testFraudRejectionIsolation() {
        MockPaymentGateway payment = new MockPaymentGateway();
        MockInventoryService inventory = new MockInventoryService();
        MockFraudDetector fraud = new MockFraudDetector();
        fraud.isSafe = false; // Trigger fraud flag!
        MockNotificationClient notification = new MockNotificationClient();

        OrderFulfillmentService service = new OrderFulfillmentService(payment, inventory, fraud, notification);
        Order order = new Order("ORD-2", "CUST-RISKY", 99999L, "USD", List.of("SKU-B"));

        FulfillmentResult result = service.fulfillOrder(order);

        assertTrue(!result.successful(), "Fraudulent order rejected");
        assertEquals("REJECTED_FRAUD_RISK", result.failureReason(), "Correct fraud failure reason reported");
        assertTrue(inventory.released == false, "Inventory was never touched");
    }

    private static void testInventoryRollbackOnPaymentFailure() {
        MockPaymentGateway payment = new MockPaymentGateway();
        payment.shouldFail = true; // Trigger payment crash!
        MockInventoryService inventory = new MockInventoryService();
        MockFraudDetector fraud = new MockFraudDetector();
        MockNotificationClient notification = new MockNotificationClient();

        OrderFulfillmentService service = new OrderFulfillmentService(payment, inventory, fraud, notification);
        Order order = new Order("ORD-3", "CUST-3", 1000L, "USD", List.of("SKU-C"));

        FulfillmentResult result = service.fulfillOrder(order);

        assertTrue(!result.successful(), "Failed payment resulted in unfulfilled order");
        assertTrue(inventory.released, "Inventory was safely rolled back upon payment exception");
    }
}
```

---

## 9. Modern Java & Spring Boot Context

### 9.1 Spring Framework is Built on Composition
The foundational architecture of Spring Boot is **Dependency Injection (DI)**, which is simply object composition formalized:
```java
@Service
public class BillingService {
    private final CustomerRepository customerRepository; // Composed
    private final TaxCalculator taxCalculator;           // Composed

    // Constructor injection: Pure composition!
    public BillingService(CustomerRepository cr, TaxCalculator tc) {
        this.customerRepository = cr;
        this.taxCalculator = tc;
    }
}
```

### 9.2 The "Rule of Three" for When to Use Inheritance
While composition is preferred for business services, inheritance is still valid when:
1. **True Subtyping (IS-A):** The subtype is truly a specialized taxonomic type of the superclass (e.g., `Integer extends Number`).
2. **Framework Template Methods:** When writing framework extension points where the framework controls the lifecycle (`AbstractAuthenticationProcessingFilter` in Spring Security).
3. **Sealed Algebraic Hierarchies:** Java 17+ `sealed interface PaymentMethod permits Card, Wire`.

---

## 10. Comparison Matrix: Concepts, Runtimes, and Paradigms

| Dimension | Class Inheritance (`extends`) | Object Composition (`has-a`) | Go Struct Embedding |
| :--- | :--- | :--- | :--- |
| **Coupling Level** | Tight, compile-time, white-box coupling. | Loose, runtime, black-box coupling. | Loose compile-time composition. |
| **Modification Flexibility**| Superclass changes risk breaking subclasses. | Composed components can change independently. | Inner struct can be altered safely. |
| **Dynamic Swapping** | **Impossible** (Class hierarchy is fixed at compile time). | **Trivial** (Pass a different implementation at runtime). | Trivial via interface assignment. |
| **Combinatorial Scalability**| Explodes exponentially ($2^N$ subclasses). | Linear ($N$ independent components). | Linear. |
| **Testability** | Difficult; requires heavy superclass mock environments. | Trivial; inject lightweight interface stubs. | Trivial; inject interface stubs. |

---

## 11. Common Anti-Patterns & Refactoring

### Anti-Pattern 1: The "Everything Extends BaseService" Trap
```java
// BAD: Monolithic base class carrying 20 unrelated dependencies
public abstract class BaseService {
    protected EntityManager em;
    protected ObjectMapper mapper;
    protected KafkaTemplate kafka;
    protected RestTemplate rest;
    // Every service in the company inherits all 20 dependencies!
}
```
*Refactoring:* Delete `BaseService`. Inject only the exact 2 or 3 dependencies each service actually needs via constructor injection.

---

## 12. Comprehensive Troubleshooting & Debugging Guide

### Common Failure Symptoms & Solutions

| Error / Symptom | Root Cause | Exact Remediation |
| :--- | :--- | :--- |
| `BeanCurrentlyInCreationException` in Spring Boot | Circular composition dependency between Service A and Service B. | Refactor shared coordinating logic into a third component (Service C) or decouple via events (`ApplicationEventPublisher`). |
| `NullPointerException` during business execution | A composed dependency field was declared but not initialized in the constructor. | Mark all composed dependency fields `final` and validate with `Objects.requireNonNull()`. |
| Excessive memory consumption under load | Composed service creates thousands of transient strategy instances rather than reusing stateless singletons. | Make composed strategy components stateless singletons managed by Spring or static constants. |

---

## 13. Frequently Asked Interview Questions

### Q1: Why does the Go programming language completely omit class inheritance?
**Answer:** The designers of Go (Ken Thompson, Rob Pike) recognized from decades of C++ and Java enterprise experience that implementation inheritance creates tight coupling, the fragile base class problem, and confusing class hierarchies (the Yo-Yo problem). By omitting inheritance entirely, Go forces developers to use **Composition** (struct embedding) and **Interface Subtyping**. This results in codebases that are fundamentally more modular, predictable, decoupled, and easy to maintain over long lifecycles.

### Q2: How does HotSpot JIT optimize composed objects via Scalar Replacement?
**Answer:** The C2 JIT compiler performs **Escape Analysis** to determine if an object allocated inside a method escapes the current thread or method scope. If a composed helper object is used locally and does not escape, HotSpot performs **Scalar Replacement**: it eliminates the physical heap allocation entirely, decomposing the object's fields into raw primitive values or CPU registers. This provides the architectural cleanliness of composition with the zero-allocation performance of monolithic code.

### Q3: What is the "Yo-Yo Problem" in object-oriented programming?
**Answer:** The Yo-Yo Problem occurs in deep inheritance hierarchies where reading code requires a developer’s attention to bounce up and down across multiple levels of superclasses and subclasses to trace method execution and state mutations. It makes understanding program flow difficult and drastically increases the risk of introducing unintended bugs during maintenance.

---

## 14. Deliberate Practice & Hands-On Exercises

### Exercise 1 (Beginner): Notification Dispatcher Composition
- **Objective:** Model a user alert system using composition.
- **Requirements:**
  1. Create `AlertService` composed with `SmsSender` and `EmailSender`.
  2. Implement an `alertUser(String message, boolean urgent)` method that sends SMS only if urgent.
  3. Write a test asserting that non-urgent alerts invoke only `EmailSender`.

### Exercise 2 (Intermediate): Composed Caching Repository
- **Objective:** Refactor an inherited cache into a composed decorator.
- **Requirements:**
  1. Interface `ProductRepository` with `Product findById(String id)`.
  2. Concrete `DatabaseProductRepository` reading from simulated DB.
  3. Concrete `CachedProductRepository` composing `ProductRepository` and an in-memory `Map`.

### Exercise 3 (Advanced): Dynamic Strategy Switching Engine
- **Objective:** Build an algorithmic pricing calculator that switches tax calculation strategies at runtime.
- **Requirements:**
  1. `PricingCalculator` composed with a `TaxStrategy` interface.
  2. Provide `EuVatTaxStrategy` and `UsSalesTaxStrategy`.
  3. Verify that changing the composed strategy alters calculated prices dynamically without reconstructing the service.

---

## 15. Mastery Checklist & Verification Gate

To consider this concept mastered, you must be able to check off every item:
- [x] Recite the GoF principle "Favor object composition over class inheritance" and justify it from first principles.
- [x] Explain the Combinatorial Subclass Explosion problem ($2^N$ subclasses) and how composition resolves it linearly ($N$).
- [x] Contrast the physical heap layout of inherited monolithic objects vs composed pointer graphs.
- [x] Explain how HotSpot JIT Escape Analysis and Scalar Replacement eliminate allocation overhead for composed objects.
- [x] Identify the "Self-Problem" in delegation and the risks of circular composition dependencies.
- [x] Explain why the Go language omitted inheritance in favor of pure composition.
- [x] Demonstrate how Spring Framework's Dependency Injection pattern is the realization of object composition.

---

## 16. Authoritative Primary Sources & References

1. **Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides:** *Design Patterns: Elements of Reusable Object-Oriented Software* (Introduction: "Favor object composition over class inheritance").
2. **Joshua Bloch:** *Effective Java* (Item 18: Favor composition over inheritance).
3. **Robert C. Martin:** *Clean Architecture* (Chapter 11: Dependency Inversion Principle).
