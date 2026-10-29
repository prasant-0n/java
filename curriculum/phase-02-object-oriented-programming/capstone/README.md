# Phase 02 Capstone Integration Project: Clean Architecture Banking Domain Engine

---

## 1. Project Overview & Architectural Intent

This capstone project synthesizes all 15 concepts mastered in **Phase 02 (Object-Oriented Programming)**. It implements an enterprise-grade banking transaction and account management domain engine according to Clean Architecture and Domain-Driven Design (DDD) principles.

The engine demonstrates:
1. **Encapsulation & Aggregate Roots:** `BankAccount` strictly encapsulates internal state (`balanceCents`, `ledger`, `state`) and enforces boundary invariants. Mutating the transaction history directly from outside is impossible.
2. **Four Pillars of OOP:**
   - **Encapsulation:** Private state, defensive copying (`getLedger()` returns unmodifiable collection), package-private boundaries.
   - **Abstraction:** Clean contracts (`RiskEngine`, `FeeCalculationStrategy`) decoupling policy from implementation.
   - **Inheritance vs Subtyping:** Interface-driven subtyping without fragile base class inheritance.
   - **Polymorphism:** Constant-specific class bodies in `AccountType` and `AccountState` executing dynamic dispatch without conditional statements.
3. **Rich Domain Enums:**
   - `AccountState`: A Finite State Machine (FSM) controlling transitions (`ACTIVE -> FROZEN -> CLOSED`) and blocking transactions on non-active accounts.
   - `AccountType`: Implements polymorphic yield calculation (`SAVINGS` yields 3.5% APY, `CHECKING` yields 0%).
   - `CustomerTier`: Encapsulates transaction limits and authorization rules.
4. **Records & Value Objects:**
   - `Money`: Immutable value object with arithmetic safety, rounding, and currency checks.
   - `Currency`: Immutable ISO-4217 standard representation.
   - `AccountId`: Strongly typed value object enforcing regex format (`ACC-XXXX`).
   - `TransactionRecord`: Immutable audit ledger entry.
5. **Sealed Hierarchies & Algebraic Data Types:**
   - `TransactionResult`: Sealed sum type (`Success | RequiresChallenge | Rejected`) enabling exhaustive Java 21 switch pattern matching without `default:` branches.
6. **Object Superclass Contracts:**
   - `equals()` and `hashCode()` based on natural business keys (`AccountId`).
   - `toString()` masking account numbers (PCI-DSS compliance).
7. **Concurrency & Deadlock Prevention:**
   - Multi-account transfers acquire locks in a globally deterministic order (comparing `AccountId` values) to eliminate circular wait deadlocks.

---

## 2. Directory Structure

```
curriculum/phase-02-object-oriented-programming/capstone/
├── README.md
├── build-and-run.sh
├── banking-domain-engine-1.0.0.jar
└── src/
    └── com/
        └── example/
            └── banking/
                ├── model/
                │   ├── AccountId.java
                │   ├── AccountState.java
                │   ├── AccountType.java
                │   ├── Currency.java
                │   ├── Customer.java
                │   ├── CustomerTier.java
                │   ├── Money.java
                │   ├── TransactionRecord.java
                │   ├── TransactionResult.java
                │   └── TransactionType.java
                ├── domain/
                │   └── BankAccount.java
                ├── service/
                │   ├── BankingCoreEngine.java
                │   ├── FeeCalculationStrategy.java
                │   └── RiskEngine.java
                └── test/
                    └── BankingEngineIntegrationTest.java
```

---

## 3. How to Build and Run

### Prerequisites
- Linux / macOS / Windows with OpenJDK 21 LTS installed.
- Bash shell.

### Execution Command
From the repository root:
```bash
./curriculum/phase-02-object-oriented-programming/capstone/build-and-run.sh
```

Or execute the packaged JAR directly:
```bash
java -jar curriculum/phase-02-object-oriented-programming/capstone/banking-domain-engine-1.0.0.jar
```

---

## 4. Test Suite Coverage & Verification Results

The test suite `BankingEngineIntegrationTest` executes 31 assertions covering:
- [x] Static Nested Builder invariant enforcement.
- [x] Defensive collection immutability (`UnsupportedOperationException` on external mutation).
- [x] Deposit, withdrawal, and overdraft rejection mechanics.
- [x] Finite state machine account lifecycle transitions.
- [x] Polymorphic yield calculations across account types.
- [x] Atomic transfer execution with tiered fee computation.
- [x] Risk engine evaluation triggering high-value step-up challenges.
- [x] Object contracts (`equals`, `hashCode`, `toString` masking).

**Result: 31/31 assertions passing cleanly (100%).**
