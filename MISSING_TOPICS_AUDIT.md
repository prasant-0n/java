# Curriculum Audit and Gap Analysis

---

## 1. Source Document Inconsistencies and Scope Reconciliation

### 1.1 The "12 Phases" vs "24 Phases" Reconcilement
- **Source Roadmap Text:** Line 17 of `java-backend-engineering-prerequisites-roadmap.md` states: *"I recommend dividing your preparation into 12 phases. Complete them sequentially, with coding exercises and explicit completion criteria."*
- **Observed Roadmap Structure:** The roadmap immediately proceeds to define phases numbered **Phase 0 through Phase 23** (a total of 24 distinct phases), plus an introductory section and three concluding synthesis sections.
- **Audit Decision:** Treat Phase 00 through Phase 23 as **24 canonical phases**. No phase, heading, or topic has been merged or discarded to satisfy the introductory "12 phases" sentence. The introductory phrasing represents an editorial legacy from an earlier draft of the roadmap, whereas the 24 phases represent the complete technical curriculum.

### 1.2 Java Version Baseline and LTS Cadence
- **Roadmap Claim:** Phase 0 text states: *"As of October 29, 2026, Java 25 is the latest LTS release. For your learning environment, use an OpenJDK distribution with Java 25 LTS... Java 21 LTS is also a reasonable choice..."*
- **Local Environment Fact:** The current Linux development environment has **OpenJDK 21.0.12.1 LTS** installed (`openjdk version "21.0.12.1"`).
- **Audit Decision:**
  - Standardize on **Java 21 LTS** as the concrete executable code and verification baseline.
  - Provide explicit comparative notes for **Java 25 LTS** whenever discussing language or JVM evolution (e.g., unnamed classes and instance main methods, Scoped Values, Structured Concurrency status).
  - All runnable examples in core phases must compile and execute on Java 21 LTS without requiring preview flags unless explicitly designated as preview evaluations.

### 1.3 Spring Baseline
- Standardize on **Spring Boot 3.3.x / Spring Framework 6.1.x** (the current standard for Java 21 LTS backend systems).
- Explicitly flag any features requiring Jakarta EE 10 namespaces (`jakarta.*` replacing `javax.*`).

---

## 2. Gap Audit Across Engineering Domains (Master Prompt Section 8)

The following matrix documents the evaluation of the 11 audit domains against the source roadmap, specifying where missing topics were added to core phases or assigned to the `supplemental/` track.

| Domain Area | Evaluated Topics from Audit Checklist | Finding in Source Roadmap | Placement & Architectural Decision |
|---|---|---|---|
| **A. Language & Platform Tooling** | JPMS (`module-info.java`), reflection accessibility, dynamic proxies, Java assertions vs validation, JNI vs FFM API (JEP 454), `jcmd`/`jstack`/`jmap`/JFR diagnostics. | Partially mentioned (JFR, reflection). JPMS, assertions, FFM API, and CLI memory tools were shallow or missing. | Added `P08-C012` (JVM CLI diagnostics) and `P17-C010` (Proxy mechanics) to core. Placed JPMS (`SUP-LANG-001`), Reflection/MethodHandles (`SUP-LANG-002`), Annotation Processing (`SUP-LANG-003`), and FFM (`SUP-LANG-004`) in `supplemental/java-platform-and-language-evolution/`. |
| **B. Type System & Advanced Language** | Type inference beyond `var`, type erasure implications, heap pollution, reifiable types, covariance/contravariance, sealed hierarchies, Records, `Optional` anti-patterns. | Mentions generics, records, sealed classes, optional. Lacked deep coverage of heap pollution, bridge methods, and `Optional` field anti-patterns. | Enriched core concepts: `P01-C007` (var limits), `P02-C012` (Records DTOs), `P02-C013` (Sealed hierarchies), `P03-C014` (PECS), `P03-C015` (Type erasure & heap pollution), and `P05-C005` (Optional anti-patterns). |
| **C. Concurrency & JMM** | Java Memory Model (happens-before), instruction reordering, safe publication, lock granularity, `StampedLock`, `CompletableFuture` thread pool hazards, virtual thread pinning (`synchronized` vs `ReentrantLock`), thread-local leakage. | Covered threads, synchronization, virtual threads. Missing detailed JMM reordering rules, virtual thread pinning traps, and ThreadLocal pool leaks. | Added `P12-C005` (JMM happens-before), `P12-C012` (CompletableFuture async pipelines), `P12-C016` (Virtual thread pinning traps), and `P12-C017` (ThreadLocal leaks and Scoped Values) directly to Phase 12. |
| **D. Database & Persistence** | PostgreSQL MVCC, VACUUM, query plans (`EXPLAIN ANALYZE`), composite indexes, keyset vs offset pagination, Flyway migrations, HikariCP pool sizing, N+1 query elimination, transactional outbox. | Covered SQL, transactions, JDBC. Lacked MVCC mechanics, query plan reading, connection pool sizing math, and keyset pagination. | Added `P13-C011` (EXPLAIN ANALYZE), `P13-C015` (PostgreSQL MVCC & VACUUM), `P14-C011` (HikariCP sizing math), `P15-C010` (Offset vs Keyset pagination), and `P18-C010` (Eliminating N+1 queries). Placed WAL recovery (`SUP-DB-001`) and partitioning (`SUP-DB-002`) in `supplemental/advanced-persistence-and-database-engineering/`. |
| **E. HTTP & API Design** | Safe vs idempotent methods, RFC 7807 problem details, API versioning, `java.net.http.HttpClient`, timeouts, retry storms, OpenAPI contract-first design. | Mentions HTTP methods, status codes, REST design. Lacked safe/idempotent contracts, RFC 7807 error envelopes, modern Java HttpClient, and retry storms. | Added `P15-C005` (Safety & Idempotency), `P15-C011` (Filtering, sorting, RFC 7807), `P15-C013` (Java HttpClient), `P15-C014` (Timeouts & retry storms), and `P15-C015` (OpenAPI contract design) to Phase 15. Placed WebFlux/R2DBC (`SUP-SPRING-001`/`002`) in `supplemental/`. |
| **F. Spring & Enterprise Systems** | Bean lifecycle stages, self-invocation proxy bypass, bean scopes and thread-safety, Spring Data JPA dirty checking, transaction rollback traps (checked exceptions), Spring Boot test slices. | Covered Spring basics, controllers, JPA. Lacked self-invocation traps, transaction rollback rules for checked exceptions, and test slice boundaries. | Added `P17-C006` (Bean lifecycle), `P17-C010` (CGLIB/JDK dynamic proxy & self-invocation), `P18-C007` (JPA dirty checking), `P18-C011` (Transaction rollback rules), and `P18-C015` (Spring Boot test slices: `@WebMvcTest`, `@DataJpaTest`). |
| **G. Security & Privacy** | Threat modeling (STRIDE), BOLA/IDOR object-level authorization, password hashing work factors, stateless JWT revocation hazards, CORS vs CSRF browser rules, SBOMs. | Mentioned authentication, authorization, JWT, SQL injection. Lacked BOLA/IDOR ownership enforcement, JWT revocation realities, and browser security mechanics. | Added `P19-C002` (BCrypt/Argon2 work factors), `P19-C007` (JWT security pitfalls & revocation), `P19-C009` (CORS vs CSRF), and `P19-C010` (BOLA/IDOR tenant isolation). Placed STRIDE threat modeling (`SUP-SEC-001`) and mTLS (`SUP-SEC-003`) in `supplemental/`. |
| **H. Testing & Quality** | Testing pyramid, AssertJ fluent assertions, parameterized tests, Testcontainers, mutation testing, what NOT to mock. | Covered JUnit and Mockito basics. Lacked AssertJ fluency, parameterized test strategies, over-mocking antipatterns, and mutation testing concepts. | Added `P11-C004` (AssertJ assertions), `P11-C005` (Parameterized tests), `P11-C010` (Over-mocking anti-pattern), and `P11-C011` (Testcontainers) directly to Phase 11. |
| **I. Observability & Operations** | Structured logging, MDC correlation IDs, sensitive data leaks, Micrometer Prometheus metrics, OpenTelemetry distributed tracing, Docker multi-stage builds, non-root containers. | Covered Docker basics, Actuator, metrics. Lacked MDC correlation, PII redaction in logs, Docker multi-stage non-root optimization, and OpenTelemetry trace propagation. | Added `P10-C007` (MDC correlation), `P10-C008` (Preventing PII log leaks), `P20-C002` (Multi-stage non-root Dockerfiles), `P20-C006` (Micrometer metrics), and `P20-C007` (OpenTelemetry tracing) to core phases. |
| **J. Distributed Systems** | Fallacies of distributed computing, transactional outbox pattern, idempotent consumers, saga orchestration/choreography, distributed locking pitfalls (Redlock). | Covered microservices vs monolith, message queues. Lacked transactional outbox, idempotent consumers, Saga pattern details, and distributed lock limits. | Added `P21-C005` (Fallacies & CAP/PACELC), `P21-C007` (Transactional outbox), `P21-C008` (Idempotent consumers), `P21-C009` (Saga pattern), and `P21-C010` (Distributed lock pitfalls) to Phase 21. |
| **K. Domain & Backend Topics** | Money and decimal correctness, time zones and daylight savings, UUIDv7 indexing, state machine modeling, rate limiting algorithms. | Mentioned BigDecimal, dates, UUIDs. Lacked explicit UTC conversion hazards, UUID indexing degradation, and state machine invariants. | Added `P05-C002` (UTC & Daylight Saving hazards), `P05-C004` (BigDecimal scale & rounding), `P05-C006` (UUIDv4 vs UUIDv7 indexing), and `P16-C009` (State pattern domain modeling). |

---

## 3. Deprecated and Obsolete Technologies Audited

The following outdated tools or patterns frequently seen in legacy Java courses have been audited and explicitly marked for exclusion or historical contrast:

1. **Standalone JRE:** Removed in Java 9. Applications package custom modular runtimes via `jlink` or deploy containerized JDKs.
2. **`Vector` and `Hashtable`:** Legacy synchronized collections replaced by `ArrayList`, `HashMap`, and `ConcurrentHashMap`.
3. **`java.util.Date` and `Calendar`:** Mutability, confusing 0-indexed months, and thread-safety bugs. Replaced by `java.time.*` (JSR-310).
4. **Java Native Object Serialization (`java.io.Serializable` for external APIs):** Inherent RCE attack surface. Replaced by JSON (Jackson) or Protocol Buffers.
5. **Spring XML Configuration:** Verbose and error-prone. Replaced by Java Configuration (`@Configuration`, `@Bean`) and Spring Boot auto-configuration.
6. **Field Injection (`@Autowired` on private fields):** Impairs testability, violates immutability, and hides cyclic dependencies. Replaced by constructor injection.
7. **Thread.stop(), suspend(), resume():** Deprecated and unsafe. Replaced by cooperative thread interruption (`Thread.interrupt()`).
