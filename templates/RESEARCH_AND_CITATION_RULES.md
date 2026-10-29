# Research, Verification, and Citation Standards

---

## 1. Primary Authority Hierarchy

When establishing technical facts, runtime behavior, or architectural guidance for this concept library, prioritize sources in the following strict order of authority:

1. **Tier 1: Formal Specifications (Primary Truth)**
   - Java Language Specification (JLS) — [docs.oracle.com/javase/specs](https://docs.oracle.com/javase/specs/)
   - Java Virtual Machine Specification (JVMS) — [docs.oracle.com/javase/specs](https://docs.oracle.com/javase/specs/)
   - Internet Engineering Task Force (IETF) RFCs for HTTP, URI, TLS, OAuth, etc.
   - ISO/IEC standards for SQL and character sets (UTF-8/Unicode).

2. **Tier 2: Official Reference Implementations & JEPs**
   - OpenJDK Project documentation and JDK Enhancement Proposals (JEPs) — [openjdk.org](https://openjdk.org/)
   - Official Java API Javadocs (SE 21 / SE 25) — [docs.oracle.com/en/java/javase](https://docs.oracle.com/en/java/javase/)
   - Official Spring Documentation (Spring Framework, Spring Boot) — [docs.spring.io](https://docs.spring.io/)
   - Official PostgreSQL Documentation — [postgresql.org/docs](https://www.postgresql.org/docs/)
   - OWASP Foundation Standards (ASVS, Top 10, API Security Top 10) — [owasp.org](https://owasp.org/)

3. **Tier 3: Platform Maintainers & Core Architects**
   - Articles and talks by Java Language Architects (Brian Goetz, John Rose, Alex Buckley, Ron Pressler).
   - Core committers to Spring, Netty, PostgreSQL, Linux kernel.

4. **Tier 4: Peer-Reviewed Books & Established Classics**
   - Joshua Bloch: *Effective Java* (3rd Edition).
   - Brian Goetz et al.: *Java Concurrency in Practice*.
   - Martin Kleppmann: *Designing Data-Intensive Applications*.
   - Robert C. Martin: *Clean Code* / *Clean Architecture*.

5. **Disallowed Sources (Untrusted / Secondary Rumor):**
   - Unsourced developer blog posts, generic medium articles, outdated SEO tutorials, or hallucinations without verified documentation checks.

---

## 2. Version Verification Protocol

1. **Current Baseline:** Java 21 LTS is the primary execution baseline for the local environment.
2. **Evolution Awareness:** Comparative notes for Java 25 LTS must be included when features matured or changed between 21 and 25.
3. **Status Tags:** Explicitly flag every feature with one of the following tags:
   - `[STABLE-LTS]`
   - `[PREVIEW-JEP-XXX]`
   - `[INCUBATOR]`
   - `[DEPRECATED-FOR-REMOVAL]`
   - `[LEGACY]`
4. **Third-Party Dependencies:**
   - Spring Boot: 3.3.x+ (requiring Java 17+, tested against Java 21).
   - JUnit: 5.10.x+.
   - AssertJ: 3.25.x+.
   - Testcontainers: 1.19.x+.
   - PostgreSQL Driver: 42.7.x+.

---

## 3. Citation and Reference Format

In every concept file:
- Give the full title, owning organization / author, section number, and stable canonical URL.
- State specifically which claim or table row the source substantiates.
- Avoid broken links or generic root domain links when a specific deep link exists.
- In `SOURCE_REGISTER.md`, record all central documentation families and their access dates.
