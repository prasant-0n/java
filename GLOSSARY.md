# Java Backend Engineering Glossary

---

This glossary provides concise, unambiguous definitions from first principles for foundational Java, JVM, database, and backend terminology used across the concept library.

### A
- **ACID:** Acronym for Atomicity, Consistency, Isolation, Durability—the fundamental guarantees of relational database transactions. (See [Phase 13](curriculum/phase-13-sql-relational-database-fundamentals/README.md))
- **AOP (Aspect-Oriented Programming):** A programming paradigm that increases modularity by allowing the separation of cross-cutting concerns (e.g., logging, transactions, security) using proxy interception. (See [Phase 17](curriculum/phase-17-spring-framework-fundamentals/README.md))
- **Autoboxing:** The automatic conversion that the Java compiler makes between the primitive types and their corresponding object wrapper classes (e.g., `int` to `Integer`). (See [Phase 01](curriculum/phase-01-java-language-fundamentals/README.md))

### B
- **Bytecode:** The intermediate representation of Java programs compiled by `javac` into `.class` files, executed by the JVM interpreter and JIT compilers. (See [Phase 00](curriculum/phase-00-development-environment-java-execution/README.md) & [Phase 08](curriculum/phase-08-jvm-internals-memory-management/README.md))
- **BOLA / IDOR:** Broken Object-Level Authorization (also known as Insecure Direct Object References)—a vulnerability where an API endpoint exposes an object reference that unauthorized users can access or manipulate without ownership checks. (See [Phase 19](curriculum/phase-19-application-security/README.md))

### C
- **Classpath:** The parameter or environment setting that tells the JVM and Java compiler where to look for user-defined classes, packages, and third-party JAR libraries. (See [Phase 00](curriculum/phase-00-development-environment-java-execution/README.md))
- **CGLIB Proxy:** A bytecode-generation library used by Spring to create dynamic subclasses for classes lacking interfaces, enabling runtime proxy interception. (See [Phase 17](curriculum/phase-17-spring-framework-fundamentals/README.md))
- **Connection Pool:** A cache of persistent database connections maintained in memory (e.g., by HikariCP) so connections can be reused across HTTP requests without the expensive overhead of TCP/TLS handshakes. (See [Phase 14](curriculum/phase-14-jdbc-database-connectivity/README.md))

### D
- **Deadlock:** A liveness failure where two or more threads or database transactions are blocked indefinitely, each waiting for a lock held by the other. (See [Phase 12](curriculum/phase-12-concurrency-multithreading/README.md) & [Phase 13](curriculum/phase-13-sql-relational-database-fundamentals/README.md))
- **Dirty Checking:** An automated Hibernate / JPA mechanism that tracks mutations to persistent entities within an open persistence context and automatically generates SQL UPDATE statements at flush/commit time. (See [Phase 18](curriculum/phase-18-spring-boot-rest-api-development/README.md))
- **DTO (Data Transfer Object):** An object designed purely to carry data between processes or application layers (e.g., HTTP request bodies) without domain business logic. (See [Phase 18](curriculum/phase-18-spring-boot-rest-api-development/README.md))

### E
- **Escape Analysis:** A HotSpot JIT compiler optimization that determines if an object allocated in a method can escape outside the method frame. If not, the JVM may optimize allocation onto the stack or scalar-replace the object's fields. (See [Phase 08](curriculum/phase-08-jvm-internals-memory-management/README.md))

### F
- **Fail-Fast:** A system design principle where operations immediately abort and raise an exception upon detecting a broken invariant or concurrent modification, rather than proceeding in an undefined state. (See [Phase 03](curriculum/phase-03-collections-framework-generics-type-safety/README.md) & [Phase 04](curriculum/phase-04-exception-handling-defensive-programming/README.md))

### G
- **Garbage Collection (GC):** Automated memory management in the JVM that identifies and reclaims heap memory occupied by unreachable objects. (See [Phase 08](curriculum/phase-08-jvm-internals-memory-management/README.md))

### H
- **Happens-Before:** A formal relationship defined by the Java Memory Model (JMM) that guarantees that memory writes by one thread are guaranteed to be visible to another thread. (See [Phase 12](curriculum/phase-12-concurrency-multithreading/README.md))
- **HikariCP:** A high-performance, zero-overhead production JDBC connection pool library utilized by default in Spring Boot. (See [Phase 14](curriculum/phase-14-jdbc-database-connectivity/README.md))

### I
- **Idempotency:** A property of an operation where executing it multiple times produces the exact same system state as executing it once. (See [Phase 15](curriculum/phase-15-http-networking-rest-api-fundamentals/README.md) & [Phase 21](curriculum/phase-21-advanced-backend-architecture-distributed-systems/README.md))
- **Inversion of Control (IoC):** A software design principle in which the control of object creation, configuration, and lifecycle management is delegated to an external container or framework. (See [Phase 17](curriculum/phase-17-spring-framework-fundamentals/README.md))

### J
- **JDK (Java Development Kit):** The complete software development package containing the Java compiler (`javac`), runtime launcher (`java`), diagnostic tools (`jcmd`, `jstack`), and standard class libraries. (See [Phase 00](curriculum/phase-00-development-environment-java-execution/README.md))
- **JIT (Just-In-Time) Compiler:** A component of the JVM execution engine that compiles frequently executed bytecode sequences into native machine instructions at runtime. (See [Phase 08](curriculum/phase-08-jvm-internals-memory-management/README.md))
- **JVM (Java Virtual Machine):** The abstract computing machine that executes compiled Java bytecode, provides memory management, and enforces security boundaries. (See [Phase 00](curriculum/phase-00-development-environment-java-execution/README.md) & [Phase 08](curriculum/phase-08-jvm-internals-memory-management/README.md))

### M
- **Metaspace:** The native memory region introduced in Java 8 that stores class metadata, method structures, and runtime constant pools, replacing the legacy contiguous heap PermGen. (See [Phase 08](curriculum/phase-08-jvm-internals-memory-management/README.md))
- **MVCC (Multi-Version Concurrency Control):** A database concurrency control method where readers do not block writers and writers do not block readers, implemented by maintaining multiple snapshot versions of rows. (See [Phase 13](curriculum/phase-13-sql-relational-database-fundamentals/README.md))

### N
- **N+1 Query Problem:** A common ORM performance anti-pattern where 1 query is executed to fetch parent entities, followed by N separate queries to fetch the child associations for each parent. (See [Phase 18](curriculum/phase-18-spring-boot-rest-api-development/README.md))

### P
- **PECS:** Acronym for "Producer Extends, Consumer Super"—a rule for generic wildcards specifying that if a collection produces values (`get()`), use `? extends T`; if it consumes values (`add()`), use `? super T`. (See [Phase 03](curriculum/phase-03-collections-framework-generics-type-safety/README.md))

### R
- **Record:** A transparent, immutable carrier for a nominal tuple of values introduced as a standard language feature in Java 16. (See [Phase 02](curriculum/phase-02-object-oriented-programming/README.md))

### T
- **Transactional Outbox Pattern:** A distributed systems pattern where domain events are stored in a database table within the same ACID transaction as the business entity changes, guaranteeing reliable event delivery to message brokers. (See [Phase 21](curriculum/phase-21-advanced-backend-architecture-distributed-systems/README.md))
- **Type Erasure:** The process by which the Java compiler strips generic type annotations during compilation to ensure backward compatibility with pre-Java 5 bytecodes. (See [Phase 03](curriculum/phase-03-collections-framework-generics-type-safety/README.md))

### V
- **Virtual Thread:** A lightweight, user-mode thread managed directly by the JVM runtime (introduced in Java 21 via Project Loom) rather than the underlying operating system kernel. (See [Phase 12](curriculum/phase-12-concurrency-multithreading/README.md))
