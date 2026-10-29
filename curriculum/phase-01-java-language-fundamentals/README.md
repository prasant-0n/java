# Phase 01: Java Language Fundamentals

---

## 1. Phase Overview and Purpose

### 1.1 Goal
Rebuild syntax, primitive and reference types, memory representations, operators, control flow, methods, arrays, and string manipulation from first principles.

### 1.2 Context in Backend Engineering
Flawed assumptions about integer overflow, floating-point math, string immutability, or pass-by-value semantics cause silent data corruption and catastrophic financial errors in backend systems.

---

## 2. Prerequisites and Co-requisites

- **Direct Prerequisites:**
  - Phase 00 (Development Environment and Java Execution).
- **Downstream Dependent Phases:**
  - Phase 02 (Object-Oriented Programming), Phase 03 (Collections & Generics), Phase 05 (Core APIs).

---

## 3. Measurable Phase Learning Outcomes

By completing Phase 01, the learner will independently be able to:
1. Demonstrate precise knowledge of primitive data representations, byte boundaries, and arithmetic overflow.
2. Contrast primitive types with reference types and explain object references and heap allocation.
3. Use operators, control structures, and switch expressions safely with exhaustive pattern matching.
4. Explain Java's strict pass-by-value semantics and method frame execution.
5. Leverage String immutability, the String Constant Pool, and StringBuilder for high-throughput string operations.
6. Manipulate single- and multi-dimensional arrays, preventing ArrayIndexOutOfBoundsException.


---

## 4. Concept Inventory and Study Order

| Concept ID | Concept Title | File Name | Importance | Status |
|---|---|---|---|---|
| `P01-C001` | Primitive Data Types, Bit Widths, and Numeric Ranges | [`001-primitive-data-types.md`](001-primitive-data-types.md) | Essential | COMPLETE |
| `P01-C002` | Floating-Point Precision, IEEE 754, and Rounding Hazards | [`002-floating-point-types-and-precision.md`](002-floating-point-types-and-precision.md) | Essential | NOT_STARTED |
| `P01-C003` | Reference Types vs Primitive Types in Memory | [`003-reference-types-vs-primitives.md`](003-reference-types-vs-primitives.md) | Essential | NOT_STARTED |
| `P01-C004` | Variable Declaration, Scope, Lifetime, and Default Values | [`004-variable-scopes-and-lifetimes.md`](004-variable-scopes-and-lifetimes.md) | Essential | NOT_STARTED |
| `P01-C005` | Type Casting: Widening, Narrowing, and Overflow Traps | [`005-type-casting-and-overflow.md`](005-type-casting-and-overflow.md) | Essential | NOT_STARTED |
| `P01-C006` | Wrapper Classes, Autoboxing, and NullPointerExceptions | [`006-wrapper-classes-autoboxing.md`](006-wrapper-classes-autoboxing.md) | Essential | NOT_STARTED |
| `P01-C007` | Local Variable Type Inference (var) and Limitations | [`007-local-variable-type-inference-var.md`](007-local-variable-type-inference-var.md) | Recommended | NOT_STARTED |
| `P01-C008` | Operators, Precedence, and Short-Circuit Evaluation | [`008-operators-and-short-circuit-evaluation.md`](008-operators-and-short-circuit-evaluation.md) | Essential | NOT_STARTED |
| `P01-C009` | Control Flow: Conditionals, Switch Statements, and Switch Expressions | [`009-control-flow-conditionals-switch.md`](009-control-flow-conditionals-switch.md) | Essential | NOT_STARTED |
| `P01-C010` | Looping Constructs, Jump Statements, and Labels | [`010-loops-and-iteration.md`](010-loops-and-iteration.md) | Essential | NOT_STARTED |
| `P01-C011` | Methods: Signatures, Contracts, and Varargs | [`011-methods-and-varargs.md`](011-methods-and-varargs.md) | Essential | NOT_STARTED |
| `P01-C012` | Pass-by-Value Semantics in Java (Why References Are Passed by Value) | [`012-pass-by-value-semantics.md`](012-pass-by-value-semantics.md) | Essential | NOT_STARTED |
| `P01-C013` | String Immutability, String Pool, and Memory Optimization | [`013-string-immutability-and-pool.md`](013-string-immutability-and-pool.md) | Essential | NOT_STARTED |
| `P01-C014` | StringBuilder vs StringBuffer vs String Concatenation Mechanics | [`014-stringbuilder-and-concatenation.md`](014-stringbuilder-and-concatenation.md) | Essential | NOT_STARTED |
| `P01-C015` | Arrays: Memory Layout, Multi-Dimensional Arrays, and Arrays Utility | [`015-arrays-memory-layout-and-utilities.md`](015-arrays-memory-layout-and-utilities.md) | Essential | NOT_STARTED |


### Recommended Pedagogical Sequence
```mermaid
flowchart LR
    P01C001["P01-C001: Primitive Data Types, Bit..."] --> P01C002["P01-C002: Floating-Point Precision,..."]
    P01C002["P01-C002: Floating-Point Precision,..."] --> P01C003["P01-C003: Reference Types vs Primit..."]
    P01C003["P01-C003: Reference Types vs Primit..."] --> P01C004["P01-C004: Variable Declaration, Sco..."]
    P01C004["P01-C004: Variable Declaration, Sco..."] --> P01C005["P01-C005: Type Casting: Widening, N..."]
    P01C005["P01-C005: Type Casting: Widening, N..."] --> P01C006["P01-C006: Wrapper Classes, Autoboxi..."]
    P01C006["P01-C006: Wrapper Classes, Autoboxi..."] --> P01C007["P01-C007: Local Variable Type Infer..."]
    P01C007["P01-C007: Local Variable Type Infer..."] --> P01C008["P01-C008: Operators, Precedence, an..."]
    P01C008["P01-C008: Operators, Precedence, an..."] --> P01C009["P01-C009: Control Flow: Conditional..."]
    P01C009["P01-C009: Control Flow: Conditional..."] --> P01C010["P01-C010: Looping Constructs, Jump ..."]
    P01C010["P01-C010: Looping Constructs, Jump ..."] --> P01C011["P01-C011: Methods: Signatures, Cont..."]
    P01C011["P01-C011: Methods: Signatures, Cont..."] --> P01C012["P01-C012: Pass-by-Value Semantics i..."]
    P01C012["P01-C012: Pass-by-Value Semantics i..."] --> P01C013["P01-C013: String Immutability, Stri..."]
    P01C013["P01-C013: String Immutability, Stri..."] --> P01C014["P01-C014: StringBuilder vs StringBu..."]
    P01C014["P01-C014: StringBuilder vs StringBu..."] --> P01C015["P01-C015: Arrays: Memory Layout, Mu..."]

```

---

## 5. Critical Distinctions and Common Misconceptions

| Misconception / Simplification | Technical Reality from First Principles |
|---|---|
| "Java passes objects by reference." | Java is strictly pass-by-value. When an object reference is passed to a method, a copy of the reference address is passed by value, not the object itself. |
| "double can be used for financial currency calculations if formatted carefully." | IEEE 754 floating-point numbers cannot represent decimal fractions exactly, leading to compounding rounding errors. BigDecimal must always be used for currency. |


---

## 6. Phase Integration Project / Capstone Exercise

### Scenario Overview
Implement an algorithmic billing transaction validator that parses raw input streams, computes exact line item totals, validates currency and quantity bounds, and outputs audited summaries without arithmetic overflow.

### Requirements and Acceptance Criteria
- [ ] Requirements fully implemented and demonstrated with executable Java code.
- [ ] Zero unhandled exceptions or edge-case crashes.
- [ ] Verification through automated tests and terminal execution.

---

## 7. Phase Completion Gate and Self-Assessment

Before marking Phase 01 as complete:
- [ ] All concept modules in this phase are thoroughly studied and exercises completed.
- [ ] You can explain every concept in the table from memory without AI assistance.
- [ ] The capstone exercise compiles, passes automated tests, and handles edge cases.
- [ ] Common failure modes can be diagnosed and resolved using standard tooling.

---

## 8. Status and Progress Reconciler

- **Total Planned Concepts:** `15`
- **Completed Concepts:** `1`
- **In Progress:** `0`
- **Completion Rate:** `6.7%`
- **Last Verified Date:** `2026-10-29`
