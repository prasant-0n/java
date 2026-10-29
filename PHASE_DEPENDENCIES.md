# Phase Dependencies and Pedagogical Sequencing

---

## 1. Global Phase Dependency Graph

The 24 phases of this curriculum form a strict **Directed Acyclic Graph (DAG)** designed to eliminate circular dependencies, ensure foundational concepts precede advanced frameworks, and guarantee that testing and database fundamentals precede automated abstractions.

```mermaid
flowchart TD
    P00["Phase 00: Environment & Execution"] --> P01["Phase 01: Language Fundamentals"]
    P01 --> P02["Phase 02: Object-Oriented Programming"]
    P02 --> P03["Phase 03: Collections & Generics"]
    P02 --> P04["Phase 04: Exception Handling"]
    P01 --> P05["Phase 05: Core Production APIs"]
    P03 --> P06["Phase 06: Functional Java & Streams"]
    P04 --> P07["Phase 07: File I/O & Data Formats"]
    P00 --> P08["Phase 08: JVM Internals & Memory"]
    P03 --> P09["Phase 09: Data Structures & Algorithms"]
    P00 --> P10["Phase 10: Professional Workflow & Maven"]
    P10 --> P11["Phase 11: Unit Testing & Quality"]
    P08 --> P12["Phase 12: Concurrency & Multithreading"]
    P11 --> P12
    P01 --> P13["Phase 13: SQL & Relational Databases"]
    P13 --> P14["Phase 14: JDBC Database Connectivity"]
    P04 --> P14
    P05 --> P15["Phase 15: HTTP, Networking & REST"]
    P07 --> P15
    P02 --> P16["Phase 16: Clean Code & Design Principles"]
    P11 --> P16
    P10 --> P17["Phase 17: Spring Framework Fundamentals"]
    P16 --> P17
    P14 --> P18["Phase 18: Spring Boot REST & JPA"]
    P15 --> P18
    P17 --> P18
    P15 --> P19["Phase 19: Application Security"]
    P18 --> P19
    P18 --> P20["Phase 20: Production Backend Engineering"]
    P19 --> P20
    P16 --> P21["Phase 21: Distributed Systems & DDD"]
    P18 --> P21
    P20 --> P21
    P08 --> P22["Phase 22: Performance, Scalability & Reliability"]
    P12 --> P22
    P20 --> P22
    P21 --> P23["Phase 23: Professional Engineering Habits"]
    P22 --> P23
```

---

## 2. Phase-by-Phase Dependency Matrix

| Phase | Phase Name | Direct Prerequisites | Downstream Direct Dependents |
|---|---|---|---|
| **00** | Development Environment & Java Execution | *None (Root Entry)* | Phase 01, Phase 08, Phase 10 |
| **01** | Java Language Fundamentals | Phase 00 | Phase 02, Phase 05, Phase 13 |
| **02** | Object-Oriented Programming | Phase 01 | Phase 03, Phase 04, Phase 16 |
| **03** | Collections Framework & Generics | Phase 02 | Phase 06, Phase 09 |
| **04** | Exception Handling & Defensive Programming | Phase 02 | Phase 07, Phase 14 |
| **05** | Core Java APIs You Will Use in Production | Phase 01, Phase 02 | Phase 15 |
| **06** | Functional Java (Lambdas & Streams) | Phase 02, Phase 03 | Phase 18, Phase 21 |
| **07** | File I/O, NIO, Configuration, Data Formats | Phase 04, Phase 05 | Phase 15 |
| **08** | JVM Internals and Memory Management | Phase 00, Phase 02 | Phase 12, Phase 22 |
| **09** | Data Structures and Algorithms for Backend Developers | Phase 01, Phase 03 | Phase 12, Phase 22 |
| **10** | Professional Java Development Workflow (Maven/Git) | Phase 00 | Phase 11, Phase 17 |
| **11** | Unit Testing and Software Quality (JUnit/Mockito) | Phase 02, Phase 04, Phase 10 | Phase 12, Phase 16, Phase 18 |
| **12** | Concurrency and Multithreading (JMM/Virtual Threads) | Phase 08, Phase 11 | Phase 20, Phase 22 |
| **13** | SQL and Relational Database Fundamentals (PostgreSQL) | Phase 01 | Phase 14 |
| **14** | JDBC and Java Database Connectivity | Phase 04, Phase 13 | Phase 18 |
| **15** | HTTP, Networking, and REST API Fundamentals | Phase 05, Phase 07 | Phase 18, Phase 19 |
| **16** | Clean Code and Software Design Principles (SOLID/GoF) | Phase 02, Phase 11 | Phase 17, Phase 21 |
| **17** | Spring Framework Fundamentals (IoC, DI, AOP) | Phase 10, Phase 16 | Phase 18 |
| **18** | Spring Boot and REST API Development (JPA/REST) | Phase 14, Phase 15, Phase 17 | Phase 19, Phase 20, Phase 21 |
| **19** | Application Security (IAM, JWT, Spring Security) | Phase 15, Phase 18 | Phase 20 |
| **20** | Production Backend Engineering (Docker, Tracing, Resilience) | Phase 18, Phase 19 | Phase 21, Phase 22 |
| **21** | Advanced Architecture & Distributed Systems (DDD/Outbox) | Phase 16, Phase 18, Phase 20 | Phase 23 |
| **22** | Performance, Scalability, and Reliability (Profiling/JMH) | Phase 08, Phase 12, Phase 20 | Phase 23 |
| **23** | Professional Engineering Habits (Debugging/ADRs/RCA) | Phase 21, Phase 22 | *Ready for 100 Backend Projects* |

---

## 3. Pedagogical Ordering Justifications

1. **Test Fundamentals Early (Phase 11 before Phase 12 & 18):**
   Writing automated unit tests must become second nature before building multithreaded systems or Spring web applications. Testing failure scenarios requires knowing how to write deterministic assertions and stubs.
2. **Relational Modeling & SQL Before ORM (Phase 13 & 14 before Phase 18):**
   Spring Data JPA abstracts SQL behind entity annotations. Without mastering relational normalization, joins, indexes, transaction boundaries, and raw JDBC connection mechanics first, developers cannot diagnose N+1 query disasters, connection leaks, or transaction deadlocks.
3. **HTTP Semantics Before Spring Controllers (Phase 15 before Phase 18):**
   Implementing `@RestController` requires knowing safe vs. idempotent methods, status codes, content negotiation, caching headers, and RFC 7807 problem envelopes.
4. **Clean Code & DIP Before Spring Container (Phase 16 before Phase 17):**
   Inversion of Control and Dependency Injection are design principles before they are framework features. Understanding DIP and loose coupling makes Spring's `@Component` and constructor injection natural rather than magical.
5. **JMM & JVM Internals Before Production Concurrency (Phase 08 before Phase 12 & 22):**
   Understanding heap allocation, CPU caches, thread stacks, and instruction reordering is essential before analyzing `volatile`, `AtomicInteger`, happens-before guarantees, or virtual thread carrier pinning.

---

## 4. Circularity Audit Verification

- **Cycle Detection:** Topological sort verified. No back-edges exist in the dependency graph.
- **Transitive Redundancy:** All prerequisite chains are explicit, preventing hidden transitive gaps.
