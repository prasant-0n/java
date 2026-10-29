# Source Register and Authority Reference

---

## 1. Primary Authority Register

Every technical claim, runtime behavior, and architectural recommendation in this library must be grounded in primary authoritative documentation. Below is the deduplicated register of primary sources consulted throughout the creation and maintenance of this concept library.

| Reference ID | Source Title / Authority | Owning Organization / Authors | Canonical URL | Scope of Authority |
|---|---|---|---|---|
| `REF-JLS-21` | The Java Language Specification, Java SE 21 Edition | James Gosling, Bill Joy, Guy Steele, Gilad Bracha, Alex Buckley, Daniel Smith, Gavin Bierman (Oracle) | [docs.oracle.com/javase/specs/jls/se21/html/](https://docs.oracle.com/javase/specs/jls/se21/html/) | Java syntax, types, expressions, JMM, lexical grammar, method dispatch. |
| `REF-JVMS-21` | The Java Virtual Machine Specification, Java SE 21 Edition | Tim Lindholm, Frank Yellin, Gilad Bracha, Alex Buckley (Oracle) | [docs.oracle.com/javase/specs/jvms/se21/html/](https://docs.oracle.com/javase/specs/jvms/se21/html/) | Class file structure, bytecode opcodes, verification, class loading, runtime data areas. |
| `REF-JDK-API-21` | Java Platform, Standard Edition API Specification (JDK 21) | Oracle / OpenJDK Community | [docs.oracle.com/en/java/javase/21/docs/api/](https://docs.oracle.com/en/java/javase/21/docs/api/) | Core standard libraries: `java.lang`, `java.util`, `java.time`, `java.nio`, `java.net`. |
| `REF-JEP-330` | JEP 330: Launch Single-File Source-Code Programs | Jonathan Gibbons (OpenJDK) | [openjdk.org/jeps/330](https://openjdk.org/jeps/330) | Single-file script launching via `java Main.java`. |
| `REF-JEP-444` | JEP 444: Virtual Threads | Ron Pressler, Alan Bateman (OpenJDK) | [openjdk.org/jeps/444](https://openjdk.org/jeps/444) | Project Loom virtual threads, carrier threads, pinning behavior. |
| `REF-JEP-454` | JEP 454: Foreign Function & Memory API | Maurizio Cimadamore (OpenJDK) | [openjdk.org/jeps/454](https://openjdk.org/jeps/454) | Direct native memory access and C library interoperability. |
| `REF-SPRING-6` | Spring Framework Reference Documentation (v6.1.x) | Broadcom / Spring Maintainers | [docs.spring.io/spring-framework/reference/](https://docs.spring.io/spring-framework/reference/) | IoC container, bean lifecycle, Spring AOP, transaction management. |
| `REF-SPRING-BOOT-3` | Spring Boot Reference Documentation (v3.3.x) | Broadcom / Spring Maintainers | [docs.spring.io/spring-boot/docs/current/reference/html/](https://docs.spring.io/spring-boot/docs/current/reference/html/) | Auto-configuration, Starters, Actuator endpoints, external configuration. |
| `REF-PG-16` | PostgreSQL 16 Documentation | The PostgreSQL Global Development Group | [postgresql.org/docs/16/](https://www.postgresql.org/docs/16/) | Relational query processing, MVCC, transaction isolation, B-tree indexes, VACUUM. |
| `REF-RFC-7807` | RFC 7807: Problem Details for HTTP APIs | M. Nottingham, E. Wilde (IETF) | [datatracker.ietf.org/doc/html/rfc7807](https://datatracker.ietf.org/doc/html/rfc7807) | Standardized JSON error envelopes for HTTP APIs. |
| `REF-RFC-9110` | RFC 9110: HTTP Semantics | R. Fielding, M. Nottingham, J. Reschke (IETF) | [datatracker.ietf.org/doc/html/rfc9110](https://datatracker.ietf.org/doc/html/rfc9110) | HTTP method definitions, safe/idempotent semantics, status codes, headers. |
| `REF-OWASP-TOP10` | OWASP Top 10 Web Application Security Risks (2021/Current) | OWASP Foundation | [owasp.org/www-project-top-ten/](https://owasp.org/www-project-top-ten/) | Broken access control, injection, cryptographic failures, security misconfigurations. |
| `REF-OWASP-API` | OWASP API Security Top 10 | OWASP Foundation | [owasp.org/www-project-api-security/](https://owasp.org/www-project-api-security/) | BOLA/IDOR, broken authentication, unrestricted resource consumption. |
| `REF-EFFECTIVE-JAVA` | *Effective Java*, Third Edition | Joshua Bloch (Addison-Wesley) | Pearson Education, ISBN: 978-0-13-468599-1 | Best practices for object creation, equals/hashCode contracts, generics, exceptions. |
| `REF-JCIP` | *Java Concurrency in Practice* | Brian Goetz, Tim Peierls, Joshua Bloch, Joseph Bowbeer, David Holmes, Doug Lea | Addison-Wesley, ISBN: 978-0-321-34960-6 | Thread safety, immutability, happens-before, explicit locking, synchronization. |
| `REF-DDIA` | *Designing Data-Intensive Applications* | Martin Kleppmann (O'Reilly Media) | O'Reilly Media, ISBN: 978-1-449-37332-0 | Replication, partitioning, transactions, consistency, distributed systems. |

---

## 2. Source Verification Log

- **Audit Date:** 2026-10-29
- **Auditor:** Antigravity Principal Technical Writer & JVM Specialist
- **Verification Rule:** No unverified blog posts or AI assertions accepted without specification backing.
