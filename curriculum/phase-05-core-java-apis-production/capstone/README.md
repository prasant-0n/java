# Phase 05 Capstone: Multi-Currency Global Billing and Invoicing Engine

---

## 1. Executive Summary & Problem Context

Enterprise SaaS platforms operating globally face immense architectural challenges when processing international invoices:
- **Precision Disasters:** IEEE 754 floating-point numbers (`double`/`float`) create fractional rounding errors that corrupt financial ledgers. Currencies require different minor-unit scales (e.g. USD/EUR use 2 decimal places, JPY uses 0).
- **Timezone and DST Hazards:** Subscriptions scheduled for daily or monthly billing at 09:00 AM local time fail or shift by an hour when crossing Daylight Saving Time (DST) boundaries if calculated using physical machine durations rather than calendar periods.
- **Database Write Collapses:** Random UUIDv4 primary keys scatter inserts across B+ Tree index leaf pages, causing 50% page fill factors, buffer pool cache eviction, and an 80% to 90% collapse in write throughput once tables exceed RAM.
- **Security & ReDoS Vulnerabilities:** Unanchored regular expressions with nested quantifiers can freeze backend CPUs during token extraction, while non-cryptographic pseudo-random generators (`java.util.Random`) generate predictable authentication tokens.

This capstone project implements **`global-billing-engine`**, a production-grade multi-currency billing and invoicing engine synthesized from first principles across all 10 concepts in **Phase 05: Core Java APIs You Will Use in Production**.

---

## 2. Architecture & Concept Synthesis

```
                                Client Invoice Request
                                          │
                                          ▼
   ┌────────────────────────────────────────────────────────────────────────┐
   │ 1. Twelve-Factor Configuration Layer (P05-C009)                        │
   │    - BillingEngineConfig resolves System Properties > ENV > Defaults   │
   │    - Normalizes dot notation to POSIX keys (billing.tax -> BILLING_TAX)│
   │    - Redacts sensitive secrets for telemetry export                    │
   └──────────────────────────────────────┬─────────────────────────────────┘
                                          │
                                          ▼
   ┌────────────────────────────────────────────────────────────────────────┐
   │ 2. Defensive Precondition & Value Construction (P05-C004, P05-C007)    │
   │    - Objects.requireNonNull with lazy error messages                   │
   │    - Money Value Object enforces currency-specific scale (USD: 2, JPY:0)│
   │    - Banker's Rounding (RoundingMode.HALF_EVEN) prevents drift         │
   └──────────────────────────────────────┬─────────────────────────────────┘
                                          │
                                          ▼
   ┌────────────────────────────────────────────────────────────────────────┐
   │ 3. Promotional Discount & Tax Pipeline (P05-C005, P05-C010)            │
   │    - Optional<String> monadic transformation (map -> filter -> orElse) │
   │    - Pre-compiled regex extracts percent via Named Capturing Groups    │
   │    - Possessive quantifiers (++) prevent catastrophic ReDoS            │
   │    - TaxRule evaluates exemptions and calculates regional VAT          │
   └──────────────────────────────────────┬─────────────────────────────────┘
                                          │
                                          ▼
   ┌────────────────────────────────────────────────────────────────────────┐
   │ 4. Localized Scheduling & UTC-at-Rest Storage (P05-C001, P05-C002, 003)│
   │    - ZonedDateTime preserves 09:00 AM wall-clock across DST via Period │
   │    - Physical elapsed time verified via Duration (23h Spring-Forward)  │
   │    - Strict parsing via DateTimeFormatter (ResolverStyle.STRICT)       │
   │    - toUtcInstant() converts to machine timeline Instant for storage   │
   └──────────────────────────────────────┬─────────────────────────────────┘
                                          │
                                          ▼
   ┌────────────────────────────────────────────────────────────────────────┐
   │ 5. Security, Identity & Indexing (P05-C006, P05-C008)                  │
   │    - RFC 9562 UUIDv7 generates K-sortable, monotonically ordered IDs   │
   │    - 16-byte binary packing (ByteBuffer) cuts storage footprint by 55% │
   │    - SecureRandom CSPRNG generates URL-safe tokens and OTPs            │
   │    - ThreadLocalRandom computes lock-free Full Jitter retry delays     │
   └────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Directory Structure

```
curriculum/phase-05-core-java-apis-production/capstone/
├── README.md                                      # Capstone architectural documentation
├── build-and-run.sh                               # Pure CLI compilation and packaging harness
├── global-billing-engine-1.0.0.jar                # Packaged standalone executable JAR
└── src/
    └── com/
        └── example/
            └── billing/
                ├── config/
                │   └── BillingEngineConfig.java       # P05-C009: 12-Factor externalized config
                ├── engine/
                │   ├── BillingScheduleEngine.java     # P05-C001, C002, C003: Java Time & DST
                │   └── TaxAndDiscountCalculator.java  # P05-C004, C005, C006: Core calculation engine
                ├── model/
                │   ├── Currency.java                  # Currency enum with official scale definitions
                │   ├── Invoice.java                   # P05-C001, C005, C006: Completed invoice record
                │   ├── Money.java                     # P05-C004, C007: Exact BigDecimal value object
                │   └── TaxRule.java                   # P05-C002, C004, C010: Regional tax & exemptions
                ├── security/
                │   ├── IdGenerator.java               # P05-C006: RFC 9562 UUIDv7 & binary packing
                │   └── SecurityTokenGenerator.java    # P05-C008: SecureRandom & ThreadLocalRandom
                ├── test/
                │   └── BillingEngineIntegrationTest.java # 10-concept full regression suite
                └── util/
                    └── CustomerInputSanitizer.java    # P05-C010: Regex named groups & ReDoS defense
```

---

## 4. Concept Traceability Matrix

| Concept ID | Concept Description | Implemented Class & Artifact | Production Synthesis Verification |
|---|---|---|---|
| `P05-C001` | Java Time Fundamentals (`Instant`, `ZonedDateTime`) | `BillingScheduleEngine.java`, `Invoice.java` | "UTC everywhere at rest" pattern; `toUtcInstant()` converts political time to timeline coordinate. |
| `P05-C002` | Time Zones, UTC Storage, DST Hazards, `ZoneId` | `BillingScheduleEngine.java`, `TaxRule.java` | Spring-Forward 23-hour day and Fall-Back 25-hour day handling; explicit geopolitical `ZoneId`. |
| `P05-C003` | Periods, Durations, and DateTimeFormatter | `BillingScheduleEngine.java` | Preserving 09:00 AM wall-clock via `Period.ofDays(1)` vs `Duration` machine seconds; thread-safe strict formatting. |
| `P05-C004` | BigDecimal Precision, Scale, Rounding Modes | `Money.java` | Proof of IEEE 754 flaw (`0.1 + 0.2 != 0.3`); Banker's Rounding (`HALF_EVEN`); non-terminating division defense. |
| `P05-C005` | Optional<T> Purpose & Monadic Methods | `TaxAndDiscountCalculator.java`, `Invoice.java` | Monadic pipeline (`filter -> map -> orElseGet`); zero intermediate null checks; safe method returns. |
| `P05-C006` | UUID Generation, Structure, UUIDv7 & Indexing | `IdGenerator.java` | Pure Java RFC 9562 UUIDv7 generator; K-sortable B-Tree locality; 16-byte binary packing via `ByteBuffer`. |
| `P05-C007` | Objects Utility Class (Null Checks, Equality) | `Money.java`, `BillingEngineConfig.java` | `Objects.requireNonNull` with lazy message suppliers; `Objects.requireNonNullElse`; `deepEquals` on byte arrays. |
| `P05-C008` | Random vs SecureRandom (Entropy, PRNG Mechanics)| `SecurityTokenGenerator.java` | `SecureRandom` CSPRNG token/OTP generation; `ThreadLocalRandom` lock-free Full Jitter backoff. |
| `P05-C009` | System Properties & Environment Variables | `BillingEngineConfig.java` | 12-Factor config precedence (`-D` > ENV > Defaults); POSIX key normalization; sensitive secret masking. |
| `P05-C010` | Regular Expressions & ReDoS Prevention | `CustomerInputSanitizer.java` | Pre-compiled `Pattern`; named capturing groups; Java 9 lambda replacer; possessive quantifiers (`++`) eliminating ReDoS. |

---

## 5. Verification & Execution Instructions

The project features a pure CLI build script with zero external dependencies, compiling under `--release 21 -parameters -encoding UTF-8 -Xlint:all -Werror`:

```bash
# Execute the automated build, packaging, and verification suite
./curriculum/phase-05-core-java-apis-production/capstone/build-and-run.sh
```

### Execution Log
```
=== Building Phase 05 Capstone: Multi-Currency Global Billing & Invoicing Engine ===
Compiling Java 21 sources with javac...
Packaging into executable JAR: /home/prasanta/java_practice/curriculum/phase-05-core-java-apis-production/capstone/global-billing-engine-1.0.0.jar...
Executing test suite from standalone JAR...
===============================================================================
Executing Phase 05 Capstone Integration Test: Global Billing & Invoicing Engine
===============================================================================
[Test 1/10] Verifying UUIDv7 K-Sortability & 16-Byte Binary Packing... PASSED
[Test 2/10] Verifying SecureRandom CSPRNG & ThreadLocalRandom Jitter... PASSED
[Test 3/10] Verifying BigDecimal Financial Math & Banker's Rounding... PASSED
[Test 4/10] Verifying Java Time UTC Storage & DST Wall-Clock Preservation... PASSED
[Test 5/10] Verifying Optional Monadic Transformation Pipeline... PASSED
[Test 6/10] Verifying Objects Null Defense & Deep Equality... PASSED
[Test 7/10] Verifying 12-Factor Configuration & Secret Masking... PASSED
[Test 8/10] Verifying Regex Named Capturing Groups & PAN Masking... PASSED
[Test 9/10] Verifying ReDoS Prevention via Possessive Quantifiers... PASSED (78 µs)
[Test 10/10] Verifying End-to-End Multi-Currency Invoice Pipeline... PASSED
===============================================================================
ALL 10 PHASE 05 CAPSTONE INTEGRATION TESTS PASSED WITH 100% SUCCESS!
===============================================================================
=== Phase 05 Capstone Build and Verification Complete! ===
```
