# Supplemental Tracks and Advanced Specializations

---

## 1. Purpose of the Supplemental Section

The core 24-phase curriculum (`curriculum/phase-00-...` through `curriculum/phase-23-...`) is carefully sequenced to prepare developers for real-world Java backend projects without unnecessary friction or overwhelming cognitive overload.

However, enterprise backend engineering often requires deeper dives into specific advanced tracks, runtime internals, specialized protocols, or optional architectures. The `supplemental/` directory hosts these structured modules.

---

## 2. Supplemental Track Directory Layout

| Track Directory | Focus Area | Core Prerequisites |
|---|---|---|
| [`java-platform-and-language-evolution/`](java-platform-and-language-evolution/) | JPMS (Modules), Annotation Processors, Reflection Internals, Foreign Function & Memory (FFM) API (JEP 454), GraalVM Native Image | Phase 08 (JVM Internals) |
| [`advanced-persistence-and-database-engineering/`](advanced-persistence-and-database-engineering/) | Advanced PostgreSQL Internals, WAL, Logical Replication, Multi-Tenancy (Schema vs Row), Database Sharding & Partitioning | Phase 13 (SQL), Phase 14 (JDBC) |
| [`advanced-spring-and-web-engineering/`](advanced-spring-and-web-engineering/) | Spring WebFlux & Project Reactor, Reactive Database Connectivity (R2DBC), Custom Starters, Spring Cloud Architecture | Phase 17 (Spring), Phase 18 (Spring Boot) |
| [`secure-backend-engineering/`](secure-backend-engineering/) | Threat Modeling (STRIDE), Cryptography (AES-GCM, RSA), Hardware Security Modules, Mutual TLS (mTLS), Secure Software Supply Chain | Phase 19 (Security) |
| [`linux-containers-and-deployment/`](linux-containers-and-deployment/) | Linux Namespaces, Cgroups, eBPF Basics, Kubernetes Deployment Topology, Helm Charts, Service Mesh (Envoy/Istio) | Phase 20 (Production Backend) |
| [`optional-specializations/`](optional-specializations/) | Apache Kafka Deep Dive (Partitioning, ISRs, Consumer Groups), Redis Cluster, gRPC & Protocol Buffers, Elasticsearch | Phase 20 & 21 |

---

## 3. Inventory and Linking

See [`supplemental-concept-inventory.md`](supplemental-concept-inventory.md) for the planned concept breakdown, prerequisites, and status tracking for all supplemental topics.
