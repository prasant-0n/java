# Phase 13: SQL and Relational Database Fundamentals

---

## 1. Phase Overview and Purpose

### 1.1 Goal
Master relational database modeling, normalization, PostgreSQL query execution, indexing strategies, transactions, and isolation levels.

### 1.2 Context in Backend Engineering
Relational databases are the bedrock of backend applications. Without deep SQL, indexing, and transaction knowledge, ORMs like Hibernate generate disastrously slow queries and corrupt business data.

---

## 2. Prerequisites and Co-requisites

- **Direct Prerequisites:**
  - Phase 01 (Language Fundamentals).
- **Downstream Dependent Phases:**
  - Phase 14 (JDBC), Phase 18 (Spring Boot & JPA), Phase 22 (Performance).

---

## 3. Measurable Phase Learning Outcomes

By completing Phase 13, the learner will independently be able to:
1. Design normalized relational schemas (1NF, 2NF, 3NF, BCNF) with proper integrity constraints.
2. Write complex SQL queries: joins, subqueries, aggregations, CTEs, and window functions.
3. Analyze query performance using EXPLAIN ANALYZE and design optimal B-tree indexes.
4. Explain ACID transaction guarantees and analyze isolation levels and concurrency anomalies.


---

## 4. Concept Inventory and Study Order

| Concept ID | Concept Title | File Name | Importance | Status |
|---|---|---|---|---|
| `P13-C001` | Relational Database Concepts: Schemas, Tables, Columns, Rows, and Data Types | [`001-relational-database-concepts.md`](001-relational-database-concepts.md) | Essential | NOT_STARTED |
| `P13-C002` | Keys and Constraints: Primary, Foreign, Unique, Check, and Referential Integrity | [`002-keys-and-constraints.md`](002-keys-and-constraints.md) | Essential | NOT_STARTED |
| `P13-C003` | Database Normalization (1NF, 2NF, 3NF, BCNF) vs Controlled Denormalization | [`003-normalization-and-denormalization.md`](003-normalization-and-denormalization.md) | Essential | NOT_STARTED |
| `P13-C004` | CRUD Operations: SELECT, INSERT, UPDATE, DELETE with Filtering and Sorting | [`004-sql-crud-operations.md`](004-sql-crud-operations.md) | Essential | NOT_STARTED |
| `P13-C005` | Relational Joins: INNER, LEFT, RIGHT, FULL OUTER, CROSS, and Self Joins | [`005-relational-joins-mechanics.md`](005-relational-joins-mechanics.md) | Essential | NOT_STARTED |
| `P13-C006` | Aggregations, GROUP BY, HAVING, and Three-Valued NULL Logic Traps | [`006-aggregations-and-null-logic.md`](006-aggregations-and-null-logic.md) | Essential | NOT_STARTED |
| `P13-C007` | Common Table Expressions (CTEs), Recursive Queries, and Subqueries | [`007-ctes-and-subqueries.md`](007-ctes-and-subqueries.md) | Essential | NOT_STARTED |
| `P13-C008` | Window Functions: ROW_NUMBER, RANK, DENSE_RANK, and Partitioning | [`008-window-functions-analytics.md`](008-window-functions-analytics.md) | Recommended | NOT_STARTED |
| `P13-C009` | PostgreSQL Indexing: B-Tree Internals, Composite Indexes, and Leftmost Prefix | [`009-postgres-indexing-btree.md`](009-postgres-indexing-btree.md) | Essential | NOT_STARTED |
| `P13-C010` | Specialized Indexes: Partial, Covering (INCLUDE), and GIN Indexes | [`010-specialized-indexes-covering.md`](010-specialized-indexes-covering.md) | Recommended | NOT_STARTED |
| `P13-C011` | Query Plan Analysis: EXPLAIN, EXPLAIN ANALYZE, Scans vs Index Seeks | [`011-query-plans-explain-analyze.md`](011-query-plans-explain-analyze.md) | Essential | NOT_STARTED |
| `P13-C012` | ACID Properties: Atomicity, Consistency, Isolation, and Durability | [`012-acid-properties-in-depth.md`](012-acid-properties-in-depth.md) | Essential | NOT_STARTED |
| `P13-C013` | Transaction Isolation Levels: Read Uncommitted, Read Committed, Repeatable Read, Serializable | [`013-transaction-isolation-levels.md`](013-transaction-isolation-levels.md) | Essential | NOT_STARTED |
| `P13-C014` | Concurrency Anomalies: Dirty Reads, Non-Repeatable Reads, Phantom Reads, Lost Updates | [`014-concurrency-anomalies.md`](014-concurrency-anomalies.md) | Essential | NOT_STARTED |
| `P13-C015` | PostgreSQL MVCC (Multi-Version Concurrency Control) and VACUUM Mechanics | [`015-postgres-mvcc-and-vacuum.md`](015-postgres-mvcc-and-vacuum.md) | Essential | NOT_STARTED |
| `P13-C016` | Database Locking: Shared, Exclusive, Row-Level Locks, and Deadlock Resolution | [`016-database-locking-and-deadlocks.md`](016-database-locking-and-deadlocks.md) | Essential | NOT_STARTED |


### Recommended Pedagogical Sequence
```mermaid
flowchart LR
    P13C001["P13-C001: Relational Database Conce..."] --> P13C002["P13-C002: Keys and Constraints: Pri..."]
    P13C002["P13-C002: Keys and Constraints: Pri..."] --> P13C003["P13-C003: Database Normalization (1..."]
    P13C003["P13-C003: Database Normalization (1..."] --> P13C004["P13-C004: CRUD Operations: SELECT, ..."]
    P13C004["P13-C004: CRUD Operations: SELECT, ..."] --> P13C005["P13-C005: Relational Joins: INNER, ..."]
    P13C005["P13-C005: Relational Joins: INNER, ..."] --> P13C006["P13-C006: Aggregations, GROUP BY, H..."]
    P13C006["P13-C006: Aggregations, GROUP BY, H..."] --> P13C007["P13-C007: Common Table Expressions ..."]
    P13C007["P13-C007: Common Table Expressions ..."] --> P13C008["P13-C008: Window Functions: ROW_NUM..."]
    P13C008["P13-C008: Window Functions: ROW_NUM..."] --> P13C009["P13-C009: PostgreSQL Indexing: B-Tr..."]
    P13C009["P13-C009: PostgreSQL Indexing: B-Tr..."] --> P13C010["P13-C010: Specialized Indexes: Part..."]
    P13C010["P13-C010: Specialized Indexes: Part..."] --> P13C011["P13-C011: Query Plan Analysis: EXPL..."]
    P13C011["P13-C011: Query Plan Analysis: EXPL..."] --> P13C012["P13-C012: ACID Properties: Atomicit..."]
    P13C012["P13-C012: ACID Properties: Atomicit..."] --> P13C013["P13-C013: Transaction Isolation Lev..."]
    P13C013["P13-C013: Transaction Isolation Lev..."] --> P13C014["P13-C014: Concurrency Anomalies: Di..."]
    P13C014["P13-C014: Concurrency Anomalies: Di..."] --> P13C015["P13-C015: PostgreSQL MVCC (Multi-Ve..."]
    P13C015["P13-C015: PostgreSQL MVCC (Multi-Ve..."] --> P13C016["P13-C016: Database Locking: Shared,..."]

```

---

## 5. Critical Distinctions and Common Misconceptions

| Misconception / Simplification | Technical Reality from First Principles |
|---|---|
| "Adding an index to every column in a table speeds up database performance." | Indexes speed up read queries that filter or sort on indexed columns, but every index incurs write amplification on INSERT, UPDATE, and DELETE operations and consumes disk/buffer cache. |
| "NULL = NULL evaluates to TRUE in SQL." | SQL uses three-valued logic (TRUE, FALSE, UNKNOWN). Any equality comparison with NULL evaluates to UNKNOWN. To check for null values, IS NULL or IS NOT NULL must be used. |


---

## 6. Phase Integration Project / Capstone Exercise

### Scenario Overview
Design a relational schema for an e-commerce inventory system with strict concurrency protections (pessimistic locking and isolation levels) to prevent stock overselling under simultaneous checkout requests.

### Requirements and Acceptance Criteria
- [ ] Requirements fully implemented and demonstrated with executable Java code.
- [ ] Zero unhandled exceptions or edge-case crashes.
- [ ] Verification through automated tests and terminal execution.

---

## 7. Phase Completion Gate and Self-Assessment

Before marking Phase 13 as complete:
- [ ] All concept modules in this phase are thoroughly studied and exercises completed.
- [ ] You can explain every concept in the table from memory without AI assistance.
- [ ] The capstone exercise compiles, passes automated tests, and handles edge cases.
- [ ] Common failure modes can be diagnosed and resolved using standard tooling.

---

## 8. Status and Progress Reconciler

- **Total Planned Concepts:** `16`
- **Completed Concepts:** `0`
- **In Progress:** `0`
- **Completion Rate:** `0%`
- **Last Verified Date:** `2026-10-29`
