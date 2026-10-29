# Phase 01 Capstone Project: High-Precision Billing & Audit Engine

## 1. Project Overview

The **High-Precision Billing & Audit Engine** is the culminating integration project for **Phase 01: Java Language Fundamentals**. It synthesizes all 15 foundational concepts into an enterprise-grade backend settlement subsystem capable of evaluating international currency transactions, computing tax and discounts with zero floating-point drift, dynamically dispatching payments using Java 21 pattern-matched switch expressions, and auditing transactions using an allocation-conscious circular ring buffer.

---

## 2. Phase 01 Concept Traceability Matrix

Every single concept mastered in Phase 01 is implemented directly in this project:

| Concept ID | Topic | Capstone Implementation Location |
| :--- | :--- | :--- |
| **P01-C001** | Primitive Data Types | Integer-cent pricing (`long unitPriceCents`), quantities (`int quantity`), percentages (`int discountPercent`), and bit masks. |
| **P01-C002** | Floating-Point Types & Precision | Zero `float`/`double` in monetary paths; strict use of integer-cent scaling and `BigDecimal` Banker's Rounding (`HALF_EVEN`). |
| **P01-C003** | Reference Types vs Primitives | Separation of primitives (`long`, `int`) from reference models (`String`, `List`, `Currency`, `PaymentDetails`). |
| **P01-C004** | Variable Scopes & Lifetimes | Local variable lifespans inside arithmetic methods; heap-allocated aggregates (`BillingTransaction`). |
| **P01-C005** | Type Casting & Overflow | `Math.multiplyExact`, `Math.addExact`, and `Math.subtractExact` preventing silent arithmetic truncation and integer overflow. |
| **P01-C006** | Wrapper Classes & Autoboxing | Selective unboxing in calculation loops; memory-conscious avoidance of auto-boxed overhead in inner processing. |
| **P01-C007** | Local-Variable Type Inference (`var`) | Judicious application of `var` in local pipeline loops where types are evident from initializer expressions. |
| **P01-C008** | Operators & Short-Circuit Evaluation | Guard expressions in pattern matching (`when`), bitwise power-of-two verification (`Integer.bitCount(n) == 1`), and bitwise buffer indexing (`seq & bufferMask`). |
| **P01-C009** | Control Flow: Conditionals & Switch | Java 21 exhaustive pattern matching switch with record patterns and `when` guard clauses in `BillingCalculator`. |
| **P01-C010** | Loops & Iteration Semantics | Enhanced `for` loops across transaction items; indexed `for` loops reading circular ring buffer windows. |
| **P01-C011** | Methods, Parameters & Varargs | Defensive method signatures, modular encapsulation, and overloaded calculation helpers. |
| **P01-C012** | Pass-by-Value Semantics | Defensive copying of collections in records (`List.copyOf`) to safeguard reference immutability across method boundaries. |
| **P01-C013** | String Immutability & Pool | Interned string keys, formatted strings, and immutable text descriptors. |
| **P01-C014** | `StringBuilder` & Concatenation | Pre-allocated `StringBuilder` capacity calculations in `TransactionAuditEngine.generateCsvReport()` avoiding GC re-allocations. |
| **P01-C015** | Arrays: Memory Layout & Utilities | Contiguous 1D object array `InvoiceSummary[] ringBuffer` utilized as a high-throughput bounded circular ring buffer. |

---

## 3. Architecture & Domain Model

```
src/com/example/billing/
├── cli/
│   └── BillingApp.java               # CLI demonstration entry point
├── model/
│   ├── Currency.java                 # Enum representing supported ISO currencies & symbols
│   ├── PaymentDetails.java           # Sealed interface (CreditCard, WireTransfer, CryptoAsset)
│   ├── LineItem.java                 # Record with integer-cent price and validation rules
│   ├── BillingTransaction.java       # Aggregate record with defensive collection copying
│   └── InvoiceSummary.java           # Record of computed invoice totals and formatted receipt
├── service/
│   ├── BillingCalculator.java        # Exact math, Banker's rounding, Java 21 pattern matching
│   └── TransactionAuditEngine.java   # Circular ring buffer with bitwise masking & CSV reporting
└── test/
    └── BillingEngineTest.java        # Standalone 25-assertion regression test suite
```

### Architectural Highlights

1. **Sealed Type Hierarchy (`PaymentDetails`):**
   ```java
   public sealed interface PaymentDetails permits 
       PaymentDetails.CreditCard, 
       PaymentDetails.WireTransfer, 
       PaymentDetails.CryptoAsset { ... }
   ```
   Enables exhaustive pattern matching in Java 21 without requiring an artificial `default` branch for known sealed subtypes.

2. **Java 21 Pattern Matching with Guard Clauses (`when`):**
   ```java
   return switch (payment) {
       case null -> "FALLBACK_MANUAL_REVIEW";
       case PaymentDetails.CreditCard cc when "VISA".equalsIgnoreCase(cc.cardType()) -> 
           "GATEWAY_CYBERSOURCE_VISA_DIRECT";
       case PaymentDetails.CreditCard cc when "MASTERCARD".equalsIgnoreCase(cc.cardType()) -> 
           "GATEWAY_MASTERCARD_SEND";
       case PaymentDetails.CreditCard cc -> 
           "GATEWAY_STRIPE_GLOBAL_ACQUIRER";
       case PaymentDetails.WireTransfer wt when wt.bic().startsWith("DB") -> 
           "GATEWAY_DEUTSCHE_BANK_SEPA";
       case PaymentDetails.WireTransfer wt -> 
           "GATEWAY_SWIFT_CLEARING_HOUSE";
       case PaymentDetails.CryptoAsset ca when "ETHEREUM".equalsIgnoreCase(ca.blockchainNetwork()) -> 
           "GATEWAY_COINBASE_COMMERCE_ERC20";
       case PaymentDetails.CryptoAsset ca -> 
           "GATEWAY_BITPAY_LAYER1";
   };
   ```

3. **High-Performance Circular Ring Buffer:**
   Uses power-of-two array capacity allowing branchless, modulo-free indexing via bitwise AND:
   ```java
   int slot = (int) (globalSequence & bufferMask);
   ringBuffer[slot] = summary;
   ```

4. **Pre-Allocated Zero-GC CSV Reporting:**
   Computes required capacity up-front based on buffer window size:
   ```java
   int estimatedCapacity = 80 + (recordedCount * 96);
   StringBuilder sb = new StringBuilder(estimatedCapacity);
   ```

---

## 4. Build, Test, and Execution Instructions

The project is built entirely using standard OpenJDK 21 LTS tools (`javac`, `jar`, `java`) with zero external Maven or Gradle dependencies.

### Running via the Automated Pipeline

Execute the root script from the capstone directory:
```bash
chmod +x build-and-run.sh
./build-and-run.sh
```

### Manual Step-by-Step Execution

1. **Compilation:**
   ```bash
   mkdir -p bin dist
   find src -name "*.java" > sources.txt
   javac --release 21 -d bin @sources.txt
   rm sources.txt
   ```

2. **Run Regression Tests:**
   ```bash
   java -ea -cp bin com.example.billing.test.BillingEngineTest
   ```

3. **Package Executable JAR:**
   ```bash
   jar --create --file dist/billing-engine-1.0.0.jar --main-class com.example.billing.cli.BillingApp -C bin .
   ```

4. **Execute Packaged Application:**
   ```bash
   java -jar dist/billing-engine-1.0.0.jar
   ```

---

## 5. Verification Results

All 25 regression test cases pass with 100% compliance:
- Gross subtotal and line-item accumulation
- Banker's rounding discount deduction
- VAT tax calculation on net subtotal
- Surcharge evaluation (Amex 3%, Crypto network gas)
- Exact arithmetic overflow interception (`ArithmeticException`)
- Exhaustive payment gateway routing dispatch
- Ring buffer index rollover and eviction
- Immutability preservation via defensive copying
- Constructor constraint validations
