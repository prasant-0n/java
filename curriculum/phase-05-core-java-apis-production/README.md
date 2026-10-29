# Phase 05: Core Java APIs You Will Use in Production

---

## 1. Phase Overview and Purpose

### 1.1 Goal
Master standard library production utilities: java.time, BigDecimal, UUID, Objects, SecureRandom, and Regular Expressions.

### 1.2 Context in Backend Engineering
Backend developers frequently write buggy date calculations or dangerous random tokens because they use legacy java.util.Date or java.util.Random instead of modern production APIs.

---

## 2. Prerequisites and Co-requisites

- **Direct Prerequisites:**
  - Phase 01 (Language Fundamentals), Phase 02 (OOP).
- **Downstream Dependent Phases:**
  - Phase 13 (SQL), Phase 15 (HTTP/REST), Phase 18 (Spring Boot).

---

## 3. Measurable Phase Learning Outcomes

By completing Phase 05, the learner will independently be able to:
1. Use java.time APIs (Instant, ZonedDateTime, Duration) safely across UTC server boundaries.
2. Perform exact financial calculations with BigDecimal, scale, and rounding modes.
3. Use Optional idiomatically as method return types without field/parameter anti-patterns.
4. Generate cryptographically secure tokens using SecureRandom vs pseudo-random Random.
5. Compile and execute regular expressions using Pattern and Matcher while preventing ReDoS.


---

## 4. Concept Inventory and Study Order

| Concept ID | Concept Title | File Name | Importance | Status |
|---|---|---|---|---|
| `P05-C001` | Java Time Architecture: Instant, LocalDate, LocalDateTime, and ZonedDateTime | [`001-java-time-api-fundamentals.md`](001-java-time-api-fundamentals.md) | Essential | COMPLETE |
| `P05-C002` | Time Zones, UTC Storage, Daylight Saving Hazards, and ZoneId | [`002-timezones-utc-and-dst.md`](002-timezones-utc-and-dst.md) | Essential | COMPLETE |
| `P05-C003` | Periods, Durations, and DateTimeFormatter Parsing Patterns | [`003-periods-durations-and-formatting.md`](003-periods-durations-and-formatting.md) | Essential | COMPLETE |
| `P05-C004` | BigDecimal and BigInteger: Precision, Scale, and Rounding Modes | [`004-bigdecimal-and-financial-math.md`](004-bigdecimal-and-financial-math.md) | Essential | COMPLETE |
| `P05-C005` | Optional<T>: Purpose, Monadic Methods, and Anti-Patterns | [`005-optional-idioms-and-anti-patterns.md`](005-optional-idioms-and-anti-patterns.md) | Essential | COMPLETE |
| `P05-C006` | UUID Generation, Structure, UUIDv4 vs UUIDv7, and Database Indexing | [`006-uuid-generation-and-indexing.md`](006-uuid-generation-and-indexing.md) | Essential | COMPLETE |
| `P05-C007` | Objects Utility Class: Null Checks, Equality, and RequireNonNull | [`007-objects-utility-class.md`](007-objects-utility-class.md) | Essential | COMPLETE |
| `P05-C008` | Random vs SecureRandom: PRNG Mechanics, Entropy, and Security Implications | [`008-random-vs-securerandom.md`](008-random-vs-securerandom.md) | Essential | COMPLETE |
| `P05-C009` | System Properties and Environment Variables in Java Applications | [`009-system-properties-and-env.md`](009-system-properties-and-env.md) | Essential | NOT_STARTED |
| `P05-C010` | Regular Expressions: Pattern, Matcher, Groups, and ReDoS Prevention | [`010-regex-pattern-matcher-redos.md`](010-regex-pattern-matcher-redos.md) | Essential | NOT_STARTED |


### Recommended Pedagogical Sequence
```mermaid
flowchart LR
    P05C001["P05-C001: Java Time Architecture: I..."] --> P05C002["P05-C002: Time Zones, UTC Storage, ..."]
    P05C002["P05-C002: Time Zones, UTC Storage, ..."] --> P05C003["P05-C003: Periods, Durations, and D..."]
    P05C003["P05-C003: Periods, Durations, and D..."] --> P05C004["P05-C004: BigDecimal and BigInteger..."]
    P05C004["P05-C004: BigDecimal and BigInteger..."] --> P05C005["P05-C005: Optional<T>: Purpose, Mon..."]
    P05C005["P05-C005: Optional<T>: Purpose, Mon..."] --> P05C006["P05-C006: UUID Generation, Structur..."]
    P05C006["P05-C006: UUID Generation, Structur..."] --> P05C007["P05-C007: Objects Utility Class: Nu..."]
    P05C007["P05-C007: Objects Utility Class: Nu..."] --> P05C008["P05-C008: Random vs SecureRandom: P..."]
    P05C008["P05-C008: Random vs SecureRandom: P..."] --> P05C009["P05-C009: System Properties and Env..."]
    P05C009["P05-C009: System Properties and Env..."] --> P05C010["P05-C010: Regular Expressions: Patt..."]

```

---

## 5. Critical Distinctions and Common Misconceptions

| Misconception / Simplification | Technical Reality from First Principles |
|---|---|
| "new BigDecimal(0.1) creates an exact 0.1 decimal value." | Passing a double literal to BigDecimal's constructor encodes the inexact binary floating point representation (0.1000000000000000055511151231257827021181583404541015625). Always use BigDecimal.valueOf(0.1) or new BigDecimal("0.1"). |
| "Optional should be used as field types in domain entities or DTOs." | Optional is not Serializable and adds reference overhead. It was designed strictly as a return type to signal the potential absence of a value. |


---

## 6. Phase Integration Project / Capstone Exercise

### Scenario Overview
Build a multi-currency tax and discount calculation engine that handles localized timestamps, UTC storage conversions, exact rounding modes, and UUIDv7 idempotency keys.

### Requirements and Acceptance Criteria
- [ ] Requirements fully implemented and demonstrated with executable Java code.
- [ ] Zero unhandled exceptions or edge-case crashes.
- [ ] Verification through automated tests and terminal execution.

---

## 7. Phase Completion Gate and Self-Assessment

Before marking Phase 05 as complete:
- [ ] All concept modules in this phase are thoroughly studied and exercises completed.
- [ ] You can explain every concept in the table from memory without AI assistance.
- [ ] The capstone exercise compiles, passes automated tests, and handles edge cases.
- [ ] Common failure modes can be diagnosed and resolved using standard tooling.

---

## 8. Status and Progress Reconciler

- **Total Planned Concepts:** `10`
- **Completed Concepts:** `8`
- **In Progress:** `0`
- **Completion Rate:** `80.0%`
- **Last Verified Date:** `2026-10-30`
