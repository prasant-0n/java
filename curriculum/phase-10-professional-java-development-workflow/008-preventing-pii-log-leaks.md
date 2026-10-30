# P10-C008: Preventing Sensitive Data (PII/Tokens) Leaks in Log Streams

---

## 1. Metadata and Learning Goals

| Metadata Field | Value |
|---|---|
| **Concept ID** | `P10-C008` |
| **Concept Title** | Preventing Sensitive Data (PII/Tokens) Leaks in Log Streams |
| **Phase** | Phase 10 — Professional Java Development Workflow |
| **Phase Directory** | `curriculum/phase-10-professional-java-development-workflow` |
| **Source Reference** | Section 10: Professional Java Development Workflow — Preventing Sensitive Data (PII/Tokens) Leaks in Log Streams |
| **Concept Type** | Security / Architecture / API / Compliance |
| **Difficulty Level** | Intermediate |
| **Curriculum Importance** | Essential |
| **Java Baseline** | Java 21 LTS (with Java 25 LTS comparative notes where applicable) |
| **Framework Baseline** | Logback 1.5.x / SLF4J 2.0.x / Jackson 2.17.x / Spring Boot 3.3.x |
| **Prerequisites** | [`P10-C006: Structured Logging with SLF4J and Logback`](006-slf4j-and-logback-architecture.md), [`P10-C007: Logging Best Practices and MDC`](007-logging-best-practices-mdc.md) |
| **Related Concepts** | [`P19-C001: Threat Modeling & OWASP Top 10`](../phase-19-application-security/001-threat-modeling.md), [`P19-C009: Data Protection, Encryption, and Masking`](../phase-19-application-security/009-data-protection-and-masking.md) |
| **Estimated Study Effort** | 3–4 hours |
| **Last Research Date** | 2026-10-31 |
| **Verification State** | COMPLETE |

### Measurable Learning Objectives
By the end of this concept module, you will be able to:
1. **Explain:** Identify the statutory legal frameworks (GDPR Art. 32/34, PCI-DSS v4.0 Req 3.4/10.2, HIPAA, SOC 2) mandating strict exclusion of Personally Identifiable Information (PII), Primary Account Numbers (PAN), and authentication secrets from diagnostic logs.
2. **Compare:** Contrast the five layers of defense-in-depth masking (Type-Level Value Wrappers, Custom Layout Converters, Logback TurboFilters, Jackson JSON Serializers, and Ingestion-Tier Scrubbing) regarding performance cost and leak prevention guarantees.
3. **Implement:** Construct a zero-leak domain model using type-level wrappers (`MaskedSecret`) and develop a Logback `CompositeConverter` that dynamically redacts credit card numbers, US Social Security numbers, and Bearer authorization tokens.
4. **Debug & Diagnose:** Identify and eliminate Regular Expression Denial of Service (ReDoS) vulnerabilities in log sanitizers that cause CPU starvation during high-throughput traffic spikes.
5. **Evaluate & Justify:** Formulate an enterprise data masking policy that balances diagnostic fidelity (preserving card BIN and last 4 digits for transaction disputes) with strict cryptographic compliance.

---

## 2. Definition, Purpose, and Mental Model

### 2.1 Formal Definition
**PII and Secret Log Leakage** occurs when sensitive, regulated data—including Personally Identifiable Information (names, national identification numbers, addresses), Cardholder Data (credit card PANs, CVVs), and Authentication Credentials (passwords, JWTs, API tokens, cryptographic keys)—is inadvertently written into unencrypted or broadly accessible application log streams.
**Log Sanitization and Masking** is the systematic application of structural transformations, pattern-matching replacements, and domain type boundaries to redact, hash, or tokenize sensitive data elements before log events cross application boundaries into disk storage or log ingestion platforms (Elasticsearch, Loki, CloudWatch).

### 2.2 The Problem It Solves
Log files are notoriously the most insecure data repositories in an enterprise:
1. **Permissive Access Permissions:** Developers, QA engineers, automated monitors, and third-party contractors who are strictly forbidden from viewing production database rows frequently have direct read access to production Kibana dashboards or log files.
2. **Secondary Storage Violations:** Log files are replicated, compressed, backed up to cold storage (e.g., AWS S3 buckets), and retained for months or years. A single unmasked credit card or password logged during an HTTP filter debugging session replicates across dozens of servers, instantly invalidating PCI-DSS compliance and creating a catastrophic data breach vector.
3. **Compliance Fines:** Under GDPR, exposing unencrypted citizen identifiers carries penalties of up to €20 million or 4% of global annual turnover. Under PCI-DSS v4.0, storing unmasked cardholder CVVs or full PANs results in immediate loss of payment gateway processing privileges and mandatory forensic audits.

### 2.3 Why Backend Engineers Need It
Every enterprise backend engineer must understand that:
- **Default `toString()` is a Security Hazard:** Default IDE-generated `toString()` methods and standard Java `record` implementations automatically concatenate all internal fields, outputting passwords and secrets into logs whenever an object is passed to `logger.info("Created user: {}", user)`.
- **Exception Messages are Uncontrolled:** Database drivers often echo SQL statements with parameter values inside `SQLException`, and HTTP clients dump raw URL strings (containing sensitive query parameters) into error messages.
- **Masking Must Be Fast and Safe:** Log scrubbing runs on every single emitted log line in the critical path. A poorly designed regex with polynomial backtracking will cause catastrophic CPU spikes, stalling backend throughput.

### 2.4 Concrete Mental Model & Metaphor
Think of logging as a **public postal postcard system**:
- A private letter sealed in an envelope is your encrypted database column. Only the recipient with the key can read it.
- A **log line is a postcard**. Anyone handling the postcard (mail carrier, sorter, sorting machine, bystander) can read every word written on it.
- Writing a customer's raw credit card or password onto a postcard because "the delivery driver might need to debug a problem" is an egregious violation.
- **Log Masking** is the **permanent black marker** applied by the sender before dropping the postcard in the mailbox. It leaves just enough context (`4111-22-******-4444`) to know which transaction occurred, while rendering the secret completely unrecoverable to unauthorized observers.

---

## 3. Architectural and Conceptual Foundations

### 3.1 Five-Layer Defense-in-Depth Architecture

Relying on a single masking mechanism (such as a single regex filter in Logback) is guaranteed to fail when developers invent new logging patterns. Enterprise backend security requires a **defense-in-depth architecture**:

```mermaid
flowchart TD
    subgraph Layer1 ["Layer 1: Type-Level Domain Boundaries"]
        L1["MaskedSecret wrapper types<br/>@ToString.Exclude (Lombok)<br/>Record toString() override"]
    end

    subgraph Layer2 ["Layer 2: Framework DTO Serializers"]
        L2["Jackson Custom Serializers<br/>@JsonIgnore / @JsonProperty(access=WRITE_ONLY)"]
    end

    subgraph Layer3 ["Layer 3: Logging Framework TurboFilters"]
        L3["Logback TurboFilter<br/>Drops or redirects sensitive loggers before formatting"]
    end

    subgraph Layer4 ["Layer 4: Pattern Converters / Encoders"]
        L4["Logback CompositeConverter<br/>High-performance regex scrubbing on formatted strings"]
    end

    subgraph Layer5 ["Layer 5: Ingestion Pipeline / SIEM"]
        L5["Logstash / Vector / FluentBit<br/>Network-level regex masking before writing to disk/ELK"]
    end

    SRC["Application Code<br/>logger.info('User: {}', user)"] --> L1
    L1 --> L2
    L2 --> L3
    L3 --> L4
    L4 --> L5
    L5 --> DISK[("Compliant Encrypted Storage<br/>(Elasticsearch, CloudWatch)")]
```

1. **Layer 1 (Domain Model):** Wrap sensitive values in dedicated types whose `toString()` method returns `********`. Even if a developer writes `logger.info("{}", secret)`, nothing leaks.
2. **Layer 2 (Serialization Layer):** Mark DTO fields with `@JsonProperty(access = Access.WRITE_ONLY)` or custom Jackson masking serializers so automated JSON serialization strips passwords.
3. **Layer 3 (TurboFilter):** Evaluate events before full string formatting. If an unauthorized logger emits an event at an improper level, intercept and scrub.
4. **Layer 4 (Pattern Converter):** A custom Logback `CompositeConverter` intercepts the final message pattern and executes high-speed regex token replacements.
5. **Layer 5 (Log Aggregator / SIEM):** Edge collectors (Vector, Logstash, FluentBit) apply secondary regex redaction before indexing into central Elasticsearch clusters.

---

## 4. Language, Syntax, and API Mechanics

### 4.1 Type-Level Masking with Java Records
The simplest and most performant leak prevention mechanism is enforcing type-level safety in domain entities:

```java
public final class MaskedSecret {
    private final String rawSecret;

    private MaskedSecret(String rawSecret) {
        this.rawSecret = rawSecret != null ? rawSecret : "";
    }

    public static MaskedSecret of(String secret) {
        return new MaskedSecret(secret);
    }

    public String getUnmaskedValue() {
        return rawSecret;
    }

    @Override
    public String toString() {
        return "********"; // Guarantees zero leakage in logs and debugger views
    }
}
```

When building Java records or POJOs:
```java
public record CreateUserRequest(
    String username,
    String email,
    MaskedSecret password,
    MaskedSecret taxId
) {}
// System.out.println(req) prints:
// CreateUserRequest[username=alice, email=a@b.com, password=********, taxId=********]
```

---

### 4.2 Logback Custom Masking CompositeConverter

In Logback, you can register a custom `CompositeConverter` that parses the rendered log message and applies regular expression masking to all text matching sensitive data patterns:

```java
package com.example.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaskingMessageConverter extends MessageConverter {

    // Matches Visa/Mastercard/Amex/Discover PANs (13 to 19 digits)
    private static final Pattern CC_PATTERN = Pattern.compile(
        "\\b([0-9]{4}[- ]?[0-9]{2})[0-9]{2}[- ]?[0-9]{4}[- ]?([0-9]{4})\\b"
    );

    // Matches common JSON sensitive fields
    private static final Pattern JSON_PATTERN = Pattern.compile(
        "\"(password|token|secret|apiKey|cvv)\"\\s*:\\s*\"([^\"]+)\"",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public String convert(ILoggingEvent event) {
        String message = super.convert(event);
        if (message == null || message.isEmpty()) {
            return message;
        }

        // Apply masking passes
        message = maskCreditCards(message);
        message = maskJsonSecrets(message);
        return message;
    }

    private String maskCreditCards(String input) {
        Matcher matcher = CC_PATTERN.matcher(input);
        return matcher.replaceAll("$1-******-$2");
    }

    private String maskJsonSecrets(String input) {
        Matcher matcher = JSON_PATTERN.matcher(input);
        return matcher.replaceAll("\"$1\":\"********\"");
    }
}
```

#### Registering in `logback.xml`:
```xml
<configuration>
    <!-- Register custom conversion rule -->
    <conversionRule conversionWord="maskMsg" 
                    converterClass="com.example.logging.MaskingMessageConverter" />

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <!-- Use %maskMsg instead of raw %msg -->
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %maskMsg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

---

## 5. Execution Mechanics, JVM Lifecycle, and Memory Internals

### 5.1 The Cost of Masking: Allocation & CPU Overhead
Every log line passed through a converter undergoes regular expression matching:
1. **String Immutability:** In Java, strings are immutable. Every `matcher.replaceAll()` creates a new `StringBuilder` and allocates a new `String` object in the Young Generation heap space.
2. **Matcher State:** `Pattern.matcher(CharSequence)` allocates a `Matcher` instance tracking group offsets and cursor state.
3. **Garbage Collection Impact:** If an application emits 20,000 log lines per second and runs 3 distinct regex replacements per line, that generates **60,000 temporary string allocations per second**.

### 5.2 The Regular Expression Denial of Service (ReDoS) Vulnerability
When designing log masking regexes, naive patterns with nested quantifiers cause **catastrophic polynomial or exponential backtracking** in the Nondeterministic Finite Automaton (NFA) engine of `java.util.regex`:

#### Dangerous Pattern (Catastrophic Backtracking):
```java
// VULNERABLE: Nested repetition with ambiguity: ([0-9]+)+
Pattern bad = Pattern.compile("Card: ([0-9]+)+$");
```
If an adversary submits `Card: 12345678901234567890!` (non-matching at the very end), the regex engine evaluates $2^{20} > 1,000,000$ backtracking paths, locking a CPU core at 100% for several seconds.

#### Safe Pattern (Deterministic Linear Bounding):
```java
// SAFE: Fixed character counts with word boundary assertions
Pattern safe = Pattern.compile("\\b[0-9]{4}[- ]?[0-9]{6}[- ]?[0-9]{4,5}\\b");
```
Fixed-length digit matches eliminate ambiguity, guaranteeing $O(N)$ linear scanning time.

---

## 6. Concurrency, Thread-Safety, and Scalability

### 6.1 `Pattern` vs `Matcher` Thread-Safety
- **`java.util.regex.Pattern`:** Fully **thread-safe** and immutable once compiled. Always compile patterns once as `private static final Pattern`.
- **`java.util.regex.Matcher`:** **NOT thread-safe**. Matchers maintain internal mutable state (`first`, `last`, `groups`). Never share a `Matcher` across concurrent threads!

```java
// THREAD-SAFE CONCURRENCY PATTERN
public class FastSanitizer {
    // Compiled ONCE, shared across all worker threads safely
    private static final Pattern SSN_PATTERN = Pattern.compile("\\b[0-9]{3}-[0-9]{2}-[0-9]{4}\\b");

    public String sanitize(String input) {
        // Matcher is allocated on thread call stack; thread-safe by stack confinement
        Matcher matcher = SSN_PATTERN.matcher(input);
        return matcher.replaceAll("***-**-****");
    }
}
```

---

## 7. Anti-Patterns and Common Developer Traps

### Anti-Pattern 1: Logging Unsanitized User DTOs via `toString()`
```java
// INCORRECT: DTO contains plain-text password and social security number
@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody UserRegistrationDto dto) {
    logger.info("Registering new user: {}", dto); // LEAKS PASSWORD IN LOGS!
    userService.create(dto);
    return ResponseEntity.ok().build();
}
```
```java
// CORRECT: Log only safe identifiers, or use type-level masking
@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody UserRegistrationDto dto) {
    logger.info("Registering new user with username: {} email: {}", dto.getUsername(), dto.getEmail());
    userService.create(dto);
    return ResponseEntity.ok().build();
}
```

---

### Anti-Pattern 2: Logging Full HTTP Request Headers
```java
// INCORRECT: Dumps Bearer JWTs and session cookies directly into log stream
public void logHeaders(HttpServletRequest req) {
    Enumeration<String> names = req.getHeaderNames();
    while (names.hasMoreElements()) {
        String name = names.nextElement();
        logger.debug("Header {}: {}", name, req.getHeader(name)); // Dumps Authorization: Bearer ey...
    }
}
```
```java
// CORRECT: Explicit header allow-list / block-list filtering
private static final Set<String> SENSITIVE_HEADERS = Set.of("authorization", "cookie", "set-cookie", "x-api-key");

public void logHeaders(HttpServletRequest req) {
    Enumeration<String> names = req.getHeaderNames();
    while (names.hasMoreElements()) {
        String name = names.nextElement();
        String val = SENSITIVE_HEADERS.contains(name.toLowerCase()) ? "[REDACTED]" : req.getHeader(name);
        logger.debug("Header {}: {}", name, val);
    }
}
```

---

### Anti-Pattern 3: Complete Redaction Leaving Zero Debuggability
```java
// INCORRECT: Masking the entire card number destroys operational dispute triage
logger.info("Processed charge on card: [REDACTED]"); // On-call engineer cannot match customer dispute!
```
```java
// CORRECT: Mask middle digits; retain first 6 (BIN) and last 4 (Dispute identifier)
// Compliant with PCI-DSS v4.0 Requirement 3.4
logger.info("Processed charge on card: {}", maskPanPreservingBinAndLast4(rawCard));
// Outputs: 4111-22-******-4444
```

---

### Anti-Pattern 4: Logging Raw SQL Exceptions with Bound Parameters
```java
// INCORRECT: Database connection URL contains credentials or parameter values
try {
    dataSource.getConnection();
} catch (SQLException ex) {
    logger.error("DB connection error to jdbc:postgresql://user:SecretPass123@db.prod.internal:5432/orders", ex);
}
```
```java
// CORRECT: Sanitize JDBC URLs before logging connection failures
try {
    dataSource.getConnection();
} catch (SQLException ex) {
    logger.error("Database connection failure to endpoint: {}", sanitizeJdbcUrl(dbEndpoint), ex);
}
```

---

### Anti-Pattern 5: Storing Secrets in MDC Context
```java
// INCORRECT: MDC properties are attached to EVERY subsequent log line!
MDC.put("authToken", userAuthToken);
logger.info("User authenticated"); // Every log line now includes authToken in JSON metadata!
```
```java
// CORRECT: Only place non-sensitive correlation identifiers in MDC
MDC.put("userId", user.getId().toString());
MDC.put("traceId", traceId);
```

---

## 8. Failure Modes, Edge Cases, and Troubleshooting

| Failure Symptom | Underlying Root Cause | Verification & Diagnostic Technique | Engineering Resolution |
|---|---|---|---|
| **CPU spikes to 100% on specific log messages.** | Regex masking rule suffers from catastrophic backtracking (ReDoS). | Take thread dump (`jcmd <pid> Thread.dump_to_file`); inspect threads stuck in `java.util.regex.Pattern$Loop.match`. | Rewrite regex to eliminate nested repetitions and ambiguity. Test with worst-case adversarial input. |
| **Credit cards still appear in logs despite masking converter.** | Appender is using `%msg` or raw layout instead of the custom conversion word (`%maskMsg`). | Inspect `logback.xml` pattern string for `%msg` vs `%maskMsg`. | Replace `%msg` with `%maskMsg` across all active console and file appenders. |
| **Stack traces leak raw passwords in method argument frames.** | An exception occurred during a method call that accepted a plain string password. | Check log lines with `Exception in thread... at AuthService.authenticate(user, "MySecretPass")`. | Pass `char[]` or `MaskedSecret` rather than raw `String` to prevent argument reflection leaks. |
| **JSON payload logs have broken syntax after regex masking.** | Regex replacement injected invalid quotes or unescaped characters into JSON string values. | Validate JSON schema on output log stream using a JSON linter. | Use `Matcher.quoteReplacement()` to ensure replacement text does not trigger quote syntax corruption. |
| **Memory usage climbs steadily in Young Gen under high load.** | Matcher allocation and intermediate String allocations during high-frequency log scrubbing. | Profile allocations with async-profiler (`-e alloc`). | Cache compiled patterns, optimize regex passes, and combine patterns where possible. |

---

## 9. Security Considerations & Compliance Frameworks

### 9.1 PCI-DSS v4.0 (Requirement 3.4 & 10.2)
- **Requirement 3.4:** Primary Account Numbers (PAN) must be rendered unreadable anywhere they are stored (including log files). The maximum permissible display is **BIN (first 6 digits) and last 4 digits**.
- **Requirement 3.3.1:** Sensitive Authentication Data (SAD)—including full track data, card verification codes (CVV/CVC), and PIN blocks—must **NEVER** be stored after authorization, even if encrypted. Logging CVVs is an automatic immediate compliance failure.

### 9.2 GDPR (General Data Protection Regulation)
- Under GDPR Article 32, logs containing European citizen personal identifiers (names, IP addresses, email addresses, national tax IDs) are considered **processing of personal data**.
- Unprotected logs accessible by engineers violate data minimization principles and must be scrubbed or subjected to automated retention expiration (e.g., 30-day purge).

---

## 10. Performance Optimization and Tuning

### 10.1 Multi-Pattern Consolidation vs Sequential Passes
Instead of executing 5 separate regex search-and-replace passes across every log string:
```java
// SLOW: 5 full string traversals + 5 intermediate StringBuilder allocations
msg = maskCreditCards(msg);
msg = maskSsn(msg);
msg = maskPasswords(msg);
msg = maskTokens(msg);
msg = maskApiKeys(msg);
```

Consolidate into a single composite regex or token scanning engine:
```java
// FAST: Single-pass matcher covering multiple sensitive token types
private static final Pattern COMPOSITE_PII_PATTERN = Pattern.compile(
    "\\b[0-9]{4}[- ]?[0-9]{6}[- ]?[0-9]{4,5}\\b" +   // Credit card
    "|\\b[0-9]{3}-[0-9]{2}-[0-9]{4}\\b" +             // SSN
    "|(Bearer\\s+[A-Za-z0-9\\-_=.]+)"                  // Bearer token
);
```

### 10.2 Fast-Path Guard Check
Before executing regex matching on a message, perform a cheap substring check (`input.indexOf('"')` or checking if string length is under 16 characters) to bypass regex scanning for trivial log statements:
```java
public String mask(String input) {
    if (input == null || input.length() < 10) {
        return input; // Impossible for 16-digit credit card or SSN to fit
    }
    // Proceed to regex
    ...
}
```

---

## 11. Real-World Backend Scenario & Production Case Study

### Production Incident: The Payment Gateway Audit Finding
An enterprise e-commerce platform processing $50M/month underwent an annual PCI-DSS level-1 audit. The auditor searched the company's Elasticsearch cluster:
```text
message: /4[0-9]{15}/
```
Over 14,000 log entries matched. An investigation revealed:
1. Two weeks prior, an engineer added diagnostic logging to debug an intermittent payment gateway 502 error:
   ```java
   logger.debug("Received payload from checkout: {}", rawJsonRequest);
   ```
2. The `rawJsonRequest` included the unmasked credit card PAN and CVV.
3. Although `DEBUG` was supposed to be off, a junior DevOps engineer had set the root log level to `DEBUG` during an unrelated deployment issue and forgotten to revert it.
4. The auditor issued an immediate critical finding, halting payment processing until the Elasticsearch indices were deleted and a multi-layer masking architecture was deployed.

#### Remediation Implemented:
1. Replaced raw string DTOs with `MaskedSecret` wrapped fields.
2. Implemented `MaskingMessageConverter` in `logback-spring.xml` enforcing BIN + last 4 preservation.
3. Deployed a pre-commit Git hook that scans for PAN and secret patterns before code can be pushed to version control.

---

## 12. Standalone, Self-Contained Verification Suite

The following standalone verification program requires **zero external libraries** and validates:
1. **Type-Level Domain Masking:** Proving that `MaskedSecret` never leaks raw values in `toString()`.
2. **Credit Card PAN Masking:** Validating PCI-DSS compliant BIN (first 6) and last 4 preservation.
3. **Structured JSON Field Sanitization:** Verifying automated masking of `password`, `ssn`, and `apiKey` fields.
4. **HTTP Header & Bearer Token Scrubbing:** Verifying truncation and redaction of JWTs.
5. **ReDoS Defense & Linear Throughput:** Stress testing the sanitizer against a 75,000-character adversarial string to guarantee execution in under 250ms without polynomial backtracking.

```java
package curriculum.phase10;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Standalone Verification Suite for P10-C008:
 * Preventing Sensitive Data (PII/Tokens) Leaks in Log Streams.
 *
 * Simulates:
 * 1. Type-level domain masking with secure SecretWrapper.
 * 2. Credit Card PAN masking (retaining first 6 BIN and last 4 digits).
 * 3. Structured JSON field masking for passwords, SSNs, and API keys.
 * 4. HTTP header sanitization (Bearer tokens and Cookies).
 * 5. ReDoS-safe linear-time scanning under adversarial payload input.
 */
public class PreventingPiiLogLeaksDemo {

    // =========================================================================
    // 1. TYPE-LEVEL MASKING WRAPPER
    // =========================================================================

    public static final class MaskedSecret {
        private final String rawSecret;

        private MaskedSecret(String rawSecret) {
            this.rawSecret = rawSecret != null ? rawSecret : "";
        }

        public static MaskedSecret of(String secret) {
            return new MaskedSecret(secret);
        }

        public String getUnmaskedValue() {
            return rawSecret;
        }

        @Override
        public String toString() {
            return "********";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MaskedSecret that)) return false;
            return Objects.equals(rawSecret, that.rawSecret);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rawSecret);
        }
    }

    public record UserAccountRecord(
        String username,
        String email,
        MaskedSecret passwordHash,
        MaskedSecret ssn
    ) {
        @Override
        public String toString() {
            return "UserAccountRecord[username=" + username + ", email=" + email +
                   ", passwordHash=" + passwordHash + ", ssn=" + ssn + "]";
        }
    }

    // =========================================================================
    // 2. HIGH-PERFORMANCE LOG SANITIZER ENGINE
    // =========================================================================

    public static class PiiLogSanitizer {

        // Regex for 13-19 digit credit card numbers (handles optional dashes/spaces)
        // Groups: 1 = first 6 digits (BIN), 2 = middle digits, 3 = last 4 digits
        private static final Pattern CC_PATTERN = Pattern.compile(
            "\\b([0-9]{4}[- ]?[0-9]{2})([0-9]{2}[- ]?[0-9]{4})[- ]?([0-9]{4})\\b"
        );

        // Regex for JSON fields: "password" : "...", "ssn" : "...", etc.
        private static final Pattern JSON_SENSITIVE_FIELD_PATTERN = Pattern.compile(
            "\"(password|pwd|ssn|secret|token|apiKey|creditCard|cvv)\"\\s*:\\s*\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE
        );

        // Regex for Bearer tokens in Authorization headers: Bearer <token>
        private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile(
            "(Bearer\\s+)([A-Za-z0-9\\-_=]+\\.[A-Za-z0-9\\-_=]+\\.[A-Za-z0-9\\-_=]+|[A-Za-z0-9\\-_=]{16,})",
            Pattern.CASE_INSENSITIVE
        );

        // Regex for US Social Security Numbers: 000-00-0000
        private static final Pattern SSN_PATTERN = Pattern.compile(
            "\\b[0-9]{3}-[0-9]{2}-([0-9]{4})\\b"
        );

        public static String maskCreditCards(String input) {
            if (input == null || input.isBlank()) return input;
            Matcher matcher = CC_PATTERN.matcher(input);
            StringBuilder sb = new StringBuilder(input.length());
            while (matcher.find()) {
                String bin = matcher.group(1);
                String last4 = matcher.group(3);
                String masked = bin + "-******-" + last4;
                matcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
            }
            matcher.appendTail(sb);
            return sb.toString();
        }

        public static String maskJsonFields(String jsonInput) {
            if (jsonInput == null || jsonInput.isBlank()) return jsonInput;
            Matcher matcher = JSON_SENSITIVE_FIELD_PATTERN.matcher(jsonInput);
            StringBuilder sb = new StringBuilder(jsonInput.length());
            while (matcher.find()) {
                String fieldName = matcher.group(1);
                String replacement = "\"" + fieldName + "\":\"********\"";
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            return sb.toString();
        }

        public static String maskBearerTokens(String headerInput) {
            if (headerInput == null || headerInput.isBlank()) return headerInput;
            Matcher matcher = BEARER_TOKEN_PATTERN.matcher(headerInput);
            StringBuilder sb = new StringBuilder(headerInput.length());
            while (matcher.find()) {
                String prefix = matcher.group(1);
                String token = matcher.group(2);
                String masked = prefix + token.substring(0, Math.min(6, token.length())) + "...[REDACTED]";
                matcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
            }
            matcher.appendTail(sb);
            return sb.toString();
        }

        public static String maskSsn(String input) {
            if (input == null || input.isBlank()) return input;
            Matcher matcher = SSN_PATTERN.matcher(input);
            StringBuilder sb = new StringBuilder(input.length());
            while (matcher.find()) {
                String last4 = matcher.group(1);
                String masked = "***-**-" + last4;
                matcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
            }
            matcher.appendTail(sb);
            return sb.toString();
        }

        public static String sanitizeAll(String logMessage) {
            if (logMessage == null) return null;
            String s = maskJsonFields(logMessage);
            s = maskBearerTokens(s);
            s = maskCreditCards(s);
            s = maskSsn(s);
            return s;
        }
    }

    // =========================================================================
    // 3. TEST SUITES
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println(" Preventing Sensitive Data Leaks Standalone Verification Suite ");
        System.out.println("===============================================================");

        test1_TypeLevelMaskingWithSecretWrapper();
        test2_CreditCardPanMasking();
        test3_StructuredJsonFieldSanitization();
        test4_HttpHeaderAndBearerTokenSanitization();
        test5_ReDosSafetyAndPerformanceUnderAdversarialInput();

        System.out.println();
        System.out.println("All 5 verification test suites passed successfully with assertions enabled!");
    }

    private static void test1_TypeLevelMaskingWithSecretWrapper() {
        System.out.print("[TEST 1/5] Type-level domain model masking with MaskedSecret... ");

        UserAccountRecord user = new UserAccountRecord(
            "alice_dev",
            "alice@example.com",
            MaskedSecret.of("SuperSecretPassword!99#"),
            MaskedSecret.of("123-45-6789")
        );

        String serialized = user.toString();

        // The serialized output must NEVER contain the raw plaintext secret
        assert !serialized.contains("SuperSecretPassword!99#") : "Password leaked in toString()!";
        assert !serialized.contains("123-45-6789") : "SSN leaked in toString()!";
        assert serialized.contains("passwordHash=********") : "Expected masked password in toString()";
        assert serialized.contains("ssn=********") : "Expected masked SSN in toString()";

        // Programmatic access remains intact for cryptographic hashing or authorized transmission
        assert "SuperSecretPassword!99#".equals(user.passwordHash().getUnmaskedValue());
        assert "123-45-6789".equals(user.ssn().getUnmaskedValue());

        System.out.println("PASSED");
    }

    private static void test2_CreditCardPanMasking() {
        System.out.print("[TEST 2/5] Credit Card PAN masking (BIN + last 4 preservation)... ");

        String rawLog = "Charged card 4111-2222-3333-4444 for $150.00 and refund to 5500001234567890.";
        String masked = PiiLogSanitizer.maskCreditCards(rawLog);

        assert !masked.contains("4111-2222-3333-4444") : "Raw Visa PAN leaked!";
        assert !masked.contains("5500001234567890") : "Raw Mastercard PAN leaked!";
        assert masked.contains("4111-22-******-4444") || masked.contains("411122-******-4444") : "Visa card masked improperly: " + masked;
        assert masked.contains("4444") : "Last 4 digits must be preserved for dispute resolution";
        assert masked.contains("7890") : "Last 4 digits of second card must be preserved";

        System.out.println("PASSED");
    }

    private static void test3_StructuredJsonFieldSanitization() {
        System.out.print("[TEST 3/5] Structured JSON field masking for passwords, SSNs, and tokens... ");

        String jsonPayload = """
            {
              "username": "bob_corp",
              "password": "ClearTextPassword123",
              "apiKey": "ak_live_891238912938129",
              "ssn": "987-65-4321",
              "status": "ACTIVE"
            }
            """;

        String sanitized = PiiLogSanitizer.maskJsonFields(jsonPayload);

        assert !sanitized.contains("ClearTextPassword123") : "JSON password leaked!";
        assert !sanitized.contains("ak_live_891238912938129") : "API Key leaked!";
        assert !sanitized.contains("987-65-4321") : "SSN leaked in JSON!";
        assert sanitized.contains("\"password\":\"********\"") : "Expected masked password field";
        assert sanitized.contains("\"apiKey\":\"********\"") : "Expected masked apiKey field";
        assert sanitized.contains("\"ssn\":\"********\"") : "Expected masked ssn field";
        assert sanitized.contains("\"status\": \"ACTIVE\"") : "Non-sensitive field mutated improperly";

        System.out.println("PASSED");
    }

    private static void test4_HttpHeaderAndBearerTokenSanitization() {
        System.out.print("[TEST 4/5] HTTP Authorization header and Bearer token scrubbing... ");

        String rawHeader = "Outgoing request to payment partner: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgN7s";
        String sanitized = PiiLogSanitizer.maskBearerTokens(rawHeader);

        assert !sanitized.contains("dozjgN7s") : "JWT signature leaked in log stream!";
        assert sanitized.contains("Bearer eyJhbG...[REDACTED]") : "Bearer token not redacted correctly: " + sanitized;

        // Also test SSN standalone masking
        String ssnLog = "Tax document filed for citizen with SSN 012-34-5678.";
        String ssnSanitized = PiiLogSanitizer.maskSsn(ssnLog);
        assert !ssnSanitized.contains("012-34-5678") : "SSN leaked!";
        assert ssnSanitized.contains("***-**-5678") : "SSN mask format incorrect: " + ssnSanitized;

        System.out.println("PASSED");
    }

    private static void test5_ReDosSafetyAndPerformanceUnderAdversarialInput() {
        System.out.print("[TEST 5/5] ReDoS safety and linear-time throughput under adversarial input... ");

        // Create an adversarial string: thousands of repeated digits that do not quite match the card pattern
        StringBuilder adversarial = new StringBuilder(50000);
        for (int i = 0; i < 5000; i++) {
            adversarial.append("4111-2222-3333 "); // Deliberately 14 chars with trailing space
        }
        adversarial.append("4111-2222-3333-4444"); // Valid card at the very end

        long start = System.nanoTime();
        String result = PiiLogSanitizer.sanitizeAll(adversarial.toString());
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assert !result.contains("4111-2222-3333-4444") : "Final card number was not masked!";
        // Ensure that 75,000 characters were processed in under 250ms without ReDoS polynomial catastrophic backtracking
        assert elapsedMs < 250 : "Catastrophic backtracking detected! Execution time: " + elapsedMs + "ms";

        System.out.println("PASSED (" + elapsedMs + "ms for 75k chars)");
    }
}
```

### Verification Command
Execute the standalone test suite on OpenJDK 21 LTS with assertions enabled:
```bash
javac --release 21 curriculum/phase-10-professional-java-development-workflow/PreventingPiiLogLeaksDemo.java
java -ea curriculum.phase10.PreventingPiiLogLeaksDemo
```

---

## 13. Comparative Analysis & Decision Framework

### 13.1 Masking Strategies Compared

| Strategy | Where It Runs | Implementation Complexity | Performance Overhead | Leak Prevention Guarantee |
|---|---|---|---|---|
| **Type-Level Wrapper (`MaskedSecret`)** | Domain Layer (JVM Heap) | Low (class design) | **Near-Zero (0 allocs)** | **100% Guaranteed** against unintentional `toString()` logging. |
| **Jackson Custom Serializer** | DTO Serialization Layer | Low to Medium | Low (runs during JSON serialization) | Protects structured JSON logs; does **not** protect raw string logs. |
| **Logback `TurboFilter`** | Logging Filter Pipeline | Medium | Very Low (evaluates before event creation) | Can drop or reroute; cannot mutate message content directly. |
| **Logback `CompositeConverter`** | Pattern Layout Formatter | Medium (Regex maintenance) | Medium (~10–30 μs per message) | Global coverage across all messages; risk of ReDoS if regex is poorly written. |
| **Ingestion Pipeline (Logstash / Vector)** | Out-of-process Sidecar / Edge | High (infrastructure config) | Zero impact on Java JVM | Last line of defense; does not protect local container logs before export. |

---

## 14. Practical Exercises and Hands-On Challenges

### Exercise 1: Build a Luhn-Validating Credit Card Masker
- **Objective:** Simple 16-digit regexes often match random sequence numbers or tracking codes. Implement a post-match Luhn Algorithm validator that only masks strings if they pass the credit card checksum test.
- **Requirements:**
  1. Extract potential digit sequences.
  2. Execute `boolean isLuhnValid(String digits)`.
  3. If valid, mask as `BIN-******-LAST4`; if invalid, leave unchanged.

### Exercise 2: Jackson Sensitive Field Annotation
- **Objective:** Create a custom Java annotation `@Sensitive` and a custom Jackson `StdSerializer<String>` that masks any annotated field as `"********"` during serialization.
- **Requirements:**
  1. Define `@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Sensitive {}`.
  2. Register serializer with `ObjectMapper`.
  3. Verify that serializing a record with `@Sensitive String apiKey` outputs `"apiKey":"********"`.

### Exercise 3: Automated Git Pre-Commit Secret Scanner
- **Objective:** Write a bash shell script suitable for `.git/hooks/pre-commit` that scans staged `.java`, `.xml`, and `.properties` files for raw AWS access keys (`AKIA[0-9A-Z]{16}`) and private RSA keys (`BEGIN RSA PRIVATE KEY`).
- **Requirements:**
  1. Reject commit with non-zero exit code if secrets are detected.
  2. Print the exact file name and line number of the violation.

---

## 15. Cross-Phase Connections and Unified Mental Graph

```mermaid
flowchart TD
    P10C06["P10-C006: SLF4J & Logback<br/>(Appenders & Encoders)"]
    P10C07["P10-C007: Logging Best Practices<br/>(MDC & Log Levels)"]
    P10C08["P10-C008: Preventing PII Leaks<br/>(Masking & Sanitization)"]
    P10C10["P10-C010: Static Analysis<br/>(SpotBugs / Checkstyle Secret Detection)"]
    P10C11["P10-C011: Professional Git<br/>(Pre-commit Secret Scanning)"]
    P19C01["Phase 19: Security<br/>(OWASP A09: Security Logging Failures)"]
    P19C09["Phase 19: Security<br/>(Cryptographic Data Protection)"]

    P10C06 -->|"Custom CompositeConverter"| P10C08
    P10C07 -->|"MDC context scrubbing"| P10C08
    P10C08 -->|"SpotBugs rule enforcement"| P10C10
    P10C08 -->|"Git hook rejection"| P10C11
    P10C08 -->|"Satisfies OWASP A09"| P19C01
    P10C08 -->|"Integrates with field encryption"| P19C09
```

- **Upstream Connection (P10-C006 & P10-C007):** P10-C006 and P10-C007 establish the Logback appender pipeline and MDC context; P10-C008 inserts the sanitization barrier between event emission and disk writing.
- **Downstream Connection (P10-C010 & P10-C011):** Static analysis (SpotBugs / ErrorProne) and Git hygiene hooks prevent developers from committing hardcoded tokens or plain-text password `toString()` methods into the repository.
- **Security Capstone Connection (Phase 19):** Satisfies OWASP Top 10 A09:2021 (Security Logging and Monitoring Failures) and provides the foundation for PCI-DSS cryptographic audit compliance.

---

## 16. Verification and Self-Assessment Checklist

Before marking this module complete, verify your comprehension against each criterion:
- [x] Can I explain why application logs are often the primary vulnerability target in enterprise security breaches?
- [x] Do I know the maximum permissible credit card PAN display under PCI-DSS v4.0 Requirement 3.4 (BIN and last 4 digits)?
- [x] Do I understand why standard Java `record` and IDE-generated `toString()` implementations are dangerous for domain entities containing secrets?
- [x] Can I implement a `MaskedSecret` type-level wrapper that ensures `toString()` outputs `********`?
- [x] Can I describe how a Logback `CompositeConverter` intercepts and sanitizes log messages in `logback.xml`?
- [x] Do I understand the difference between `Pattern` (thread-safe) and `Matcher` (thread-unsafe) in Java regex?
- [x] Can I explain how Regular Expression Denial of Service (ReDoS) occurs and how to write linear-time, non-backtracking patterns?
- [x] Do I know why sensitive tokens should never be stored in the SLF4J `MDC` context map?
- [x] Can I list the 5 layers of defense-in-depth masking?
- [x] Have I compiled and executed `PreventingPiiLogLeaksDemo` on OpenJDK 21 LTS with assertions enabled (`-ea`) and verified all 5 test suites pass?
