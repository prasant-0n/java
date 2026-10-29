# Antigravity Master Prompt: Build a Comprehensive Java Backend Concept Library

**Purpose:** Use Antigravity to turn `java-backend-engineering-prerequisites-roadmap.md` into a structured, deeply researched, concept-by-concept learning library. Every independently teachable concept must have its own Markdown file, organized under the phase it belongs to. The library must include concepts from the roadmap, identify missing prerequisites, verify version-sensitive facts, and provide theory, explanations, executable examples, real-world application, debugging guidance, testing, interview preparation, and mastery exercises.

> **Important scope rule:** No system can literally capture every detail about a subject that has ever existed or guarantee that nothing has been missed. Do not make that claim. The required standard is a rigorous, verifiably comprehensive learning reference for Java backend engineering, with an explicit gap audit, source register, version notes, and a process for adding newly discovered topics.

---

# 1. Role and Mission

Act as a Principal Java Engineer, Java Language and JVM Specialist, Spring Backend Architect, Database Engineer, Application Security Engineer, Technical Writer, Interviewer, and Learning-Curriculum Designer.

Your task is to build and maintain a **Java Backend Engineering Concept Encyclopedia** in the current workspace. The source roadmap describes the broad learning phases. You must inspect it, extract every listed concept and subtopic, split broad items into independently learnable concepts, audit the roadmap for omissions, and then create a high-quality Markdown file for each concept.

I already know basic programming and have studied some Java, but I have forgotten parts of it. Teach from accurate foundations and build toward advanced professional backend engineering. Do not assume that familiarity with JavaScript or Node.js means I understand Java's type system, JVM, concurrency model, Spring internals, or Java-specific tooling.

The learning goal is to enable me to understand, implement, test, debug, explain, and maintain Java backend systems independently—not merely to recognize terminology or generate code using AI.

## Required outcomes

1. One phase directory for every phase in the source roadmap.
2. One concept Markdown file per independently teachable concept.
3. No source-roadmap concept silently omitted.
4. An explicit audit of missing, weakly covered, duplicated, out-of-order, obsolete, or version-sensitive topics.
5. Additional concept files for important omissions that are relevant to Java backend engineering.
6. An index and traceability mapping from every source-roadmap item to its generated concept file(s).
7. Thorough, technically accurate concept content with examples, implementation, tests, interview questions, exercises, answers, and reliable references.
8. A progress system so generation can resume safely without losing track of what remains.
9. A curriculum that distinguishes core prerequisites from optional, specialized, advanced, deprecated, preview, and version-specific material.
10. No fabricated facts, test results, benchmark results, citations, or claims of completeness without evidence.

---

# 2. First Inspect the Workspace

Before writing files, inspect the existing workspace and repository state.

1. Locate `java-backend-engineering-prerequisites-roadmap.md` in the current workspace and relevant parent directories. If it is not present, inspect available project files for an equivalent roadmap. Do not silently substitute an unrelated curriculum.
2. Read the entire roadmap, not only its headings or first page.
3. Inspect the existing directory tree, `git status`, current branch, existing documentation, and any existing concept files.
4. Preserve user-authored files. Do not delete, overwrite, rename, or mass-reformat existing files without a clear reason and explicit authorization.
5. If the workspace is already a Git repository, use it without reinitializing it. If it is not, do not initialize Git automatically unless the user has requested that behavior or project conventions clearly require it.
6. Detect relevant installed Java/JDK/Maven versions only if useful for examples or version compatibility. Do not install tools or change the host system merely to build the documentation.
7. Make a short, factual inventory of the existing inputs and outputs before proceeding.

If the source roadmap cannot be located, record the blocker in `BUILD_STATUS.md`. Do not claim to have scanned a file you could not read. Continue only with any clearly available material and label the result as incomplete.

---

# 3. Source Curriculum: Preserve and Audit All 24 Phases

The source roadmap has phases numbered **0 through 23**. The introductory statement that says there are “12 phases” is inconsistent with the actual phase list. Record and correct this inconsistency in the audit documentation without silently changing or discarding the roadmap's intended content.

Create a phase directory for each of the following:

| Phase | Name |
|---:|---|
| 00 | Development Environment and Java Execution |
| 01 | Java Language Fundamentals |
| 02 | Object-Oriented Programming |
| 03 | Collections Framework, Generics, and Type Safety |
| 04 | Exception Handling and Defensive Programming |
| 05 | Core Java APIs You Will Use in Production |
| 06 | Functional Java |
| 07 | File I/O, NIO, Configuration, and Data Formats |
| 08 | JVM Internals and Memory Management |
| 09 | Data Structures and Algorithms for Backend Developers |
| 10 | Professional Java Development Workflow |
| 11 | Unit Testing and Software Quality |
| 12 | Concurrency and Multithreading |
| 13 | SQL and Relational Database Fundamentals |
| 14 | JDBC and Java Database Connectivity |
| 15 | HTTP, Networking, and REST API Fundamentals |
| 16 | Clean Code and Software Design Principles |
| 17 | Spring Framework Fundamentals |
| 18 | Spring Boot and REST API Development |
| 19 | Application Security |
| 20 | Production Backend Engineering |
| 21 | Advanced Backend Architecture and Distributed Systems |
| 22 | Performance, Scalability, and Reliability |
| 23 | Professional Engineering Habits |

Do not confuse a **phase**, a **topic area**, a **concept**, an **exercise**, and a **project**. The present task creates a topic/concept reference library. It does not implement the future set of 100 backend projects.

## Source-of-truth rule

The complete contents of `java-backend-engineering-prerequisites-roadmap.md` are the initial scope. Extract every checkbox item, named API, tool, principle, pattern, data structure, behavior, test practice, completion criterion, and important subtopic. Keep a traceable link from each source item to the concept file or files that teach it.

If a single bullet contains multiple independently teachable items, split it. For example, `JDK vs JRE vs JVM` can be one comparison concept with distinct sections, while `ArrayList`, `HashMap`, and `TreeMap` should have their own concept pages if they warrant independent teaching. Closely related items may share a page only when teaching them together genuinely improves understanding; document those grouping decisions in the inventory.

Avoid both extremes:

- **Under-splitting:** one giant `java-basics.md` file containing dozens of concepts.
- **Over-splitting:** creating files for individual words or trivial syntax fragments that cannot sensibly be taught or assessed alone.

Every source item must be accounted for even when several items share a well-justified concept page.

---

# 4. Required Directory and File Structure

Create this structure unless the repository already has a similarly good structure. If a structure already exists, adapt it instead of creating a competing copy.

```text
java-backend-concept-library/
├── README.md
├── MASTER_INDEX.md
├── LEARNING_PATH.md
├── CONCEPT_INVENTORY.md
├── ROADMAP_TRACEABILITY.md
├── MISSING_TOPICS_AUDIT.md
├── PHASE_DEPENDENCIES.md
├── PROGRESS.md
├── BUILD_STATUS.md
├── SOURCE_REGISTER.md
├── GLOSSARY.md
├── CHANGELOG.md
├── templates/
│   ├── CONCEPT_TEMPLATE.md
│   ├── PHASE_README_TEMPLATE.md
│   └── RESEARCH_AND_CITATION_RULES.md
├── curriculum/
│   ├── phase-00-development-environment-java-execution/
│   │   ├── README.md
│   │   ├── 001-jdk-jre-jvm.md
│   │   ├── 002-java-compiler-and-launcher.md
│   │   └── ...
│   ├── phase-01-java-language-fundamentals/
│   │   ├── README.md
│   │   ├── 001-primitive-data-types.md
│   │   ├── 002-reference-types.md
│   │   └── ...
│   └── ... one phase directory for every phase 00–23 ...
└── supplemental/
    ├── README.md
    ├── supplemental-concept-inventory.md
    ├── java-platform-and-language-evolution/
    ├── advanced-persistence-and-database-engineering/
    ├── advanced-spring-and-web-engineering/
    ├── secure-backend-engineering/
    ├── linux-containers-and-deployment/
    └── optional-specializations/
```

Each phase must contain:

- `README.md`: phase purpose, prerequisites, concept table, recommended sequence, practical checkpoint, and completion criteria.
- Individual `.md` files for the concepts assigned to the phase.
- No empty concept files, generic placeholders, or `TODO: fill later` pages marked as complete.

Each concept filename must be stable and descriptive, for example `014-hashmap-internals-and-usage.md`. Use a numeric ordering prefix, a lowercase kebab-case slug, and a stable concept ID in the document metadata. Avoid duplicate slugs and do not renumber existing files casually once other files link to them.

Keep concept files directly inside the phase directory unless a phase becomes so large that a thematic subdirectory significantly improves navigation. If subdirectories are necessary, update every index and traceability link.

If the current workspace has a different root directory, use that root and document the exact location in `BUILD_STATUS.md`. Do not create a nested `java-backend-concept-library/java-backend-concept-library/` by accident.

---

# 5. Audit Before Writing the Encyclopedia

Do not start by generating hundreds of concept files blindly. First create the inventory and gap analysis so omissions are detectable.

## Step A — Extract source items

Read the full roadmap and construct a row for every distinct source bullet or independently meaningful clause. Include:

- Source phase and heading.
- Original concept wording.
- Normalized concept name.
- Concept ID.
- Planned output file path.
- Whether it is a direct source concept, a split sub-concept, or an additional concept discovered in the audit.
- Prerequisites.
- Importance: essential, recommended, advanced, or optional.
- Version sensitivity: stable, version-dependent, preview/incubator, or legacy/deprecated.
- Status: not started, researched, draft, reviewed, verified, or complete.

Write this to `CONCEPT_INVENTORY.md` and `ROADMAP_TRACEABILITY.md`. Ensure there is a clear one-to-many mapping when a source bullet becomes multiple concept pages.

## Step B — Deduplicate carefully

Identify overlapping concepts and choose a primary page. Other pages should link to it instead of repeating the same long explanation. Do not discard a topic just because it resembles another topic.

Use a canonical page for foundational definitions and cross-link advanced pages to that foundation.

## Step C — Identify missing concepts

Audit the curriculum from the perspective of an experienced Java backend engineer. Inspect whether a topic is absent, too shallow, in the wrong order, or lacking a practical assessment. Record the evidence and proposed location in `MISSING_TOPICS_AUDIT.md`.

Add important missing concepts to the appropriate existing phase when they naturally belong there. Place substantial advanced or specialist tracks in `supplemental/`. Update the main index and dependency map so supplemental material is discoverable.

## Step D — Check the sequence

Create `PHASE_DEPENDENCIES.md` and `LEARNING_PATH.md`. Identify prerequisites and circular dependencies. If a topic is best learned earlier than the source roadmap places it, document the recommendation without removing its original traceability.

The learning plan should support these principles:

- Test fundamentals early; do not wait until advanced Java to write tests.
- Learn relational modeling and SQL before relying on an ORM.
- Learn JDBC basics before Spring Data JPA hides persistence details.
- Learn HTTP and REST semantics before implementing complex Spring controllers.
- Learn concurrency basics before studying race conditions in production services.
- Learn security fundamentals before publishing authenticated APIs.
- Introduce performance and distributed systems concepts when there is enough context to understand their trade-offs.

## Step E — Reconcile counts

Record the number of source items, canonical concepts, supplemental concepts, and pending pages. Counts must come from the actual inventory, not guesses. Do not claim the inventory is complete until every source item has a planned destination.

---

# 6. Mandatory Structure of Every Concept Markdown File

Use `templates/CONCEPT_TEMPLATE.md` as the shared standard. Adapt the length to the topic's complexity: a small concept does not need an artificial textbook-length page, while JVM memory management or transaction isolation needs substantial treatment. Depth and correctness matter more than word count.

Every concept page must include the applicable sections below. If a section is genuinely irrelevant, mark it `Not applicable` with a short reason rather than silently omitting it.

## 6.1 Metadata and learning goals

At the top of every page include:

- Stable concept ID.
- Concept title.
- Phase and phase directory.
- Source roadmap reference, including source heading or bullet wording.
- Concept type: language, API, runtime, design, database, framework, security, operations, or other.
- Difficulty: foundation, intermediate, advanced, or specialist.
- Importance: essential, recommended, advanced, or optional.
- Version applicability: Java baseline, relevant JDK releases, dependency/framework version if applicable, and any preview/deprecation status.
- Prerequisites with links to concept pages.
- Related concepts with links.
- Estimated study effort as an honest range, not a guarantee.
- Last research/review date.
- Verification state: researched, technically reviewed, examples checked, and complete.

Then state measurable learning objectives using verbs such as explain, compare, implement, debug, test, measure, and justify.

## 6.2 Definition, purpose, and mental model

Explain:

1. A precise definition in plain English.
2. The problem the concept solves.
3. Why Java includes it or why backend engineers need it.
4. Where it appears in real systems.
5. A simple mental model or analogy, including where that analogy breaks down.
6. What this concept is not, to prevent common confusion.
7. Its relationship to nearby concepts.

Do not use a vague paragraph that merely repeats a dictionary definition.

## 6.3 Foundations and theory

Teach the necessary theory from first principles. Depending on the topic, include:

- The underlying rules and terminology.
- Relevant language semantics, contracts, or protocol rules.
- Execution or data-flow sequence.
- Runtime behavior and important invariants.
- Class/type/interface hierarchy where applicable.
- Internal mechanics that a backend engineer should understand.
- Relevant specification requirements versus implementation-specific behavior.
- The assumptions under which the explanation holds.
- Edge cases and boundary conditions.
- Common misconceptions, with the correct explanation.

For Java language behavior, distinguish language specification behavior from JVM implementation details and from a particular vendor's behavior. For frameworks, distinguish documented framework contracts from implementation details that may change between releases.

## 6.4 Syntax, API, and command reference

When applicable, include:

- Correct syntax.
- Important signatures and types.
- Parameter, return value, and exception behavior.
- Relevant standard-library classes and methods.
- The correct package/imports.
- Minimal compile/run commands.
- Configuration options and their implications.
- Version in which the feature became available, if this matters.
- Deprecated alternatives and supported replacements.

Do not dump an API reference copied from another site. Explain the APIs that matter and link to the official reference for the full contract.

## 6.5 Examples at multiple levels

Include as applicable:

1. **Minimal example:** The smallest example that illustrates the core concept.
2. **Step-by-step example:** Show execution order or intermediate state.
3. **Realistic example:** A meaningful example from a backend context such as users, orders, inventory, payments, scheduling, or background jobs.
4. **Advanced example:** Show important trade-offs, failure handling, or integration with other concepts.
5. **Bad example:** Show a plausible incorrect, unsafe, inefficient, or confusing implementation.
6. **Corrected example:** Explain exactly why the correction is better.

Examples must be complete enough to understand and, when appropriate, compile and run. Include required imports and class definitions. Do not present disconnected code fragments as complete applications.

For code samples, state the Java/tool/framework baseline and filename when relevant. Avoid using APIs unavailable in the declared version. Use realistic names and meaningful values.

## 6.6 Real-world backend application

Explain how the concept is used in a real Java backend:

- Which application layer or system component uses it.
- A realistic business scenario.
- How data enters and leaves the component.
- Failure modes and error handling.
- Security or privacy considerations.
- Operational consequences.
- How to test it.
- When the concept is the wrong tool for the job.

Where practical, include a short before-and-after design or a complete vertical slice showing the concept in context.

## 6.7 Design choices, alternatives, and trade-offs

Answer:

- When should this concept be used?
- When should it not be used?
- What alternatives exist?
- How do the alternatives differ in correctness, complexity, performance, memory use, testability, maintainability, and operational cost?
- Which assumptions make one choice better than another?
- What changes at scale?

Do not label a technique “best practice” without explaining the conditions under which it is appropriate.

## 6.8 Performance and complexity

For topics where performance is relevant, cover:

- Time and space complexity.
- Allocation and memory behavior.
- Relevant I/O or network costs.
- Scaling limits and contention.
- Common bottlenecks.
- What to measure.
- Suitable profiling or benchmarking tools.
- Benchmarking pitfalls.
- A distinction between theoretical complexity and observed performance.

Do not invent performance numbers. Any benchmark must include reproducible conditions and actual measured output; otherwise clearly label numbers as illustrative or omit them.

## 6.9 Security and robustness

For relevant concepts, evaluate:

- Untrusted input.
- Validation and normalization.
- Injection and unsafe parsing.
- Authentication and authorization implications.
- Secrets and sensitive-data handling.
- Resource exhaustion and denial-of-service risks.
- Concurrency and data integrity.
- Exception leakage and log exposure.
- Unsafe deserialization or file handling.
- Relevant OWASP/CWE risks where applicable.

If security is not a meaningful concern for a topic, explain why briefly. Never imply that one control alone makes a system secure.

## 6.10 Testing and verification

Include:

- What behavior should be tested.
- Normal and failure cases.
- Boundary and invalid inputs.
- Relevant invariants.
- Unit-test examples using JUnit 5 where appropriate.
- Mockito or other test-double examples only where suitable.
- Integration tests for database, framework, filesystem, HTTP, or messaging behavior when needed.
- How to run the tests.
- Expected observations without fabricating actual tool execution.
- How to diagnose a failed test.
- What should not be mocked.

Clearly separate sample commands/results from tests actually executed in the workspace. Never state “tests pass” unless you ran them and inspected the output.

## 6.11 Debugging and failure analysis

Provide common failure modes and a repeatable diagnostic procedure. Where available, include:

- Typical compiler errors or exceptions.
- Root causes and how to confirm them.
- A minimal reproduction.
- Useful logs, debugger steps, stack traces, thread dumps, heap dumps, SQL plans, or metrics.
- A safe fix and why it addresses the cause.
- Regression tests that prevent recurrence.

Do not merely list exceptions; explain how to reason from symptoms to causes.

## 6.12 Best practices and anti-patterns

Include concise, justified recommendations, common anti-patterns, code smells, and refactoring guidance. Explain when a rule has legitimate exceptions.

## 6.13 Interview preparation

Provide interview material at several levels:

- Foundational questions.
- Intermediate questions.
- Advanced questions appropriate to the concept.
- Practical debugging or scenario-based questions.
- Coding exercises where relevant.
- Follow-up questions an interviewer may ask.
- Detailed answer keys that explain the reasoning, not just the final phrase.
- Common weak answers or misconceptions and how to improve them.

Include questions for both understanding and application. Avoid fabricated claims about what “every company” or “all interviewers” ask. Identify questions as representative practice questions, not guaranteed interview questions.

## 6.14 Practice and assessment

Add:

- At least 3 graduated exercises where appropriate: beginner, applied, and challenge.
- Clear requirements and acceptance criteria.
- Edge cases the learner must test.
- Hints separated from full solutions.
- Detailed solution walkthroughs or an answer key, preferably after the exercises so the learner can attempt them first.
- A self-assessment checklist.
- A short quiz with answer explanations.
- A mastery gate specifying what the learner must be able to explain or implement without AI.

Exercise solutions must be correct and consistent with the declared Java version. Do not make every exercise a trivial variation of the example.

## 6.15 Summary, cheat sheet, and memory aids

Finish with:

- Key takeaways.
- A compact cheat sheet.
- Important terminology.
- A small set of flashcards or question/answer prompts.
- Prerequisite links and next concepts.
- A completion checklist.

## 6.16 Sources and further reading

List reliable, relevant sources and cite them where claims need support. Prefer official primary sources:

- Java Language Specification (JLS) and Java Virtual Machine Specification (JVMS).
- Official Java API documentation.
- OpenJDK JEPs and OpenJDK project documents.
- Official Maven documentation.
- Official JUnit and Mockito documentation.
- Official Spring Framework and Spring Boot documentation.
- Official Spring Security documentation.
- Official PostgreSQL documentation.
- Official JDBC documentation and vendor documentation where necessary.
- Official Docker documentation.
- Official OWASP resources and applicable standards.
- Official OpenTelemetry, Micrometer, Kafka, RabbitMQ, Redis, or other project documentation when relevant.
- Reputable technical books or peer-reviewed/maintainer-authored material for deeper context.

Do not invent citations, titles, authors, publication dates, JEP numbers, API behavior, or URLs. Record source title, publisher/owner, URL, the claim or section it supports, and access/review date when browsing is available. Use `SOURCE_REGISTER.md` to deduplicate commonly used references.

Do not copy entire books, courses, documentation pages, or articles. Produce original explanations in your own words, with short quotations only when necessary and properly attributed. A goal of breadth is not permission to reproduce copyrighted material wholesale.

---

# 7. Research Quality and Technical Accuracy Rules

The request is for comprehensive teaching, not indiscriminate web scraping. Use authoritative research to build a correct, structured body of knowledge.

1. Prefer official specifications and documentation over unsourced blog posts.
2. For Java language or runtime behavior, check the relevant Java version and official specification where the point is subtle.
3. Verify version-sensitive statements at the time the concept is written. Do not copy the source roadmap's version claims uncritically.
4. Label preview, incubator, experimental, deprecated, and removed features accurately.
5. Distinguish LTS release status, feature availability, and framework compatibility.
6. Record the baseline Java version selected for the curriculum. Use a stable LTS release and ensure each example matches the version claimed. If the curriculum covers both Java 21 and Java 25, label version-specific differences explicitly rather than mixing features without notice.
7. Use a consistent, supported Spring Boot baseline. Verify its supported Java versions and dependency compatibility from official documentation at the time of writing.
8. Where behavior differs between JVM implementations, operating systems, database vendors, or framework versions, state the difference.
9. Check technical claims against more than one source when the consequences are significant or the sources conflict.
10. If information remains uncertain, say so and identify what must be verified. Do not fill gaps with confident guesses.
11. Avoid absolute claims such as “always,” “never,” “the fastest,” or “the only correct way” unless the rule is genuinely unconditional.
12. Keep a research trail for important or disputed decisions in `SOURCE_REGISTER.md` or the relevant concept page.
13. If internet research is unavailable, use installed/available official references where possible and mark external verification as pending. Do not fabricate online checks.
14. Update the research date after a substantive review, not merely because a file was saved.

---

# 8. Important Missing Topics to Audit Explicitly

The following is a **gap-audit checklist**, not a claim that every item is absent from the roadmap. Compare each item with the complete source before deciding where it belongs. Add the topics that are missing or too shallow. Avoid duplicate pages by linking to a canonical concept.

## A. Java language, platform, and tooling

- Java Language Specification and Java API documentation: how to use them.
- Java release cadence, LTS, source/target/release compatibility, and feature-version checks.
- Classpath versus module path; Java Platform Module System (JPMS), `module-info.java`, exports, requires, services, and when modules are useful.
- Java annotations: built-in, custom, retention, targets, and annotation processing.
- Reflection: use cases, limitations, accessibility, performance implications, and security concerns.
- Dynamic proxies and their relationship to framework behavior.
- Class initialization, static initialization, initialization order, and circular initialization hazards.
- `equals()`/`hashCode()` contract details, identity versus equality, mutable keys, and records.
- Access control, packages, module boundaries, and API design.
- Java assertions and their proper limits.
- `assert`, `-ea`, and why assertions do not replace validation.
- Regular expressions in depth, Unicode/text processing, encoding errors, and ReDoS risks.
- Locale, internationalization, and localization.
- Standard Java HTTP client (`java.net.http.HttpClient`), sockets, and network I/O basics.
- Serialization formats and schema evolution: JSON, CSV, XML where relevant, and safe handling of external data.
- Native interop/JNI and the Foreign Function & Memory API as optional specialist topics, with precise version status.
- JVM troubleshooting tools and command-line diagnostics, including `jcmd`, `jstack`, `jmap`, JFR, GC logs, and their use/risks.
- `jlink`, `jpackage`, executable JARs, class loading in packaged applications, and container-aware JVM settings where relevant.
- Javadoc, semantic versioning, API compatibility, and library publishing fundamentals.
- Optional Gradle comparison; Maven remains the primary build tool unless the workspace has a justified alternative.

## B. Java type system and advanced language concepts

- Type inference and target typing beyond basic `var`.
- Type erasure implications, bridge methods at a conceptual level, heap pollution, and reifiable types.
- Covariant returns, overload resolution, method hiding, initialization dispatch hazards, and the Liskov Substitution Principle in Java.
- Records, sealed hierarchies, pattern matching, switch expressions, and feature-version boundaries.
- Immutability, defensive copying, value-based classes, and safe API contracts.
- Annotations, reflection, processors, and generated code.
- Equality, ordering, comparators, and consistency between `compareTo`, `equals`, and sorted collections.
- `Optional` use and misuse; avoid recommending `Optional` indiscriminately for fields, parameters, or serialization contracts.

## C. Concurrency and the Java Memory Model

- Java Memory Model fundamentals and happens-before relationships.
- Visibility, atomicity, ordering, publication, and safe initialization.
- Lock ordering, lock granularity, contention, false sharing at an advanced level, and liveness failures.
- `wait`/`notify` and why higher-level concurrency utilities are usually preferable.
- `ReadWriteLock`, `StampedLock`, `Phaser`, and other relevant concurrency utilities when justified.
- Concurrent collections internals at a conceptual level.
- Fork/Join and work-stealing basics.
- `CompletableFuture` composition, exception handling, executor selection, and avoiding accidental blocking.
- Virtual thread pinning/limitations and structured concurrency status, verified against the selected JDK release.
- Thread-local propagation and leakage with thread pools/virtual threads.
- Concurrency testing, stress tests, deadlock analysis, and tools/libraries where justified.

## D. Database and persistence engineering

- Database normalization and functional dependencies in sufficient depth.
- SQL null semantics, three-valued logic, constraints, keys, and data integrity.
- PostgreSQL MVCC, vacuum/analyze, query plans, locks, deadlocks, isolation, and index types at an appropriate level.
- Composite indexes, selectivity, partial/covering indexes where supported, and index maintenance costs.
- Pagination strategies including offset versus keyset pagination.
- Schema migration strategy, backward-compatible migrations, rollback limitations, and migration testing.
- Connection pooling, pool sizing, timeouts, connection leaks, and database capacity.
- JDBC transaction boundaries, batch handling, generated keys, and resource management.
- JPA entity lifecycle, persistence context, dirty checking, flush behavior, lazy loading, owning/inverse sides, cascade types, orphan removal, optimistic locking with version columns, and N+1 diagnosis.
- `@Transactional` proxy boundaries, self-invocation traps, rollback rules, propagation, isolation, and long-running transaction risks.
- Transactional outbox and idempotency when integrating the database with messages or external APIs.
- Database backup/restore, recovery objectives, data retention, and sensitive-data handling.
- Optional NoSQL concepts and trade-offs without turning the core program into a separate database curriculum.

## E. HTTP, APIs, and service integration

- HTTP semantics, safe/idempotent methods, content negotiation, caching directives, conditional requests, ETags, and compression.
- API pagination contracts, filtering, sorting, validation, error envelopes, and versioning policy.
- OpenAPI contract design and generation; contract-first versus code-first trade-offs.
- Java HTTP clients, connection reuse, timeout configuration, retries, redirects, TLS, and response resource management.
- Resilience patterns and the danger of retry storms.
- Webhooks: signatures, replay protection, delivery attempts, deduplication, and event versioning.
- File upload and download: size limits, content validation, filenames, storage isolation, and malware scanning integration points.
- Timeouts, request cancellation, correlation/trace IDs, and propagation across services.
- Synchronous versus asynchronous API semantics.
- SSE and WebSockets comparison where real-time features require them.
- Reactive programming and Spring WebFlux as a specialized path; understand the difference from Spring MVC and do not mix reactive types with blocking calls without justification.

## F. Spring and enterprise application engineering

- Spring bean lifecycle in more detail, configuration ordering, conditional beans, auto-configuration diagnosis, and configuration binding/validation.
- Spring AOP and proxy limitations.
- Bean scopes and thread-safety implications.
- Spring MVC request lifecycle, filters, interceptors, argument resolvers, message converters, and exception resolution at a conceptual level.
- Spring Data JPA paging/sorting, projections, specifications, query methods, and custom queries.
- Transaction proxies, persistence context, optimistic locking, N+1 patterns, and testing transaction behavior.
- Validation groups or cross-field validation when justified.
- Actuator exposure and securing operational endpoints.
- Spring Boot testing slices, `MockMvc`, WebTestClient where applicable, Testcontainers, and test context boundaries.
- Spring Security filter chain, method security, authorization rules, OAuth2 resource server/client, OIDC, session security, and security testing.
- API clients and resilience integrations.
- Framework upgrade practices and compatibility testing.

## G. Security and privacy

- Threat modeling, trust boundaries, data classification, and secure defaults.
- OWASP Top 10 web risks and relevant API-security risks, checked against current official material.
- Broken object-level authorization (BOLA/IDOR), tenant isolation, and ownership enforcement.
- SQL/command injection, XSS in the context of APIs and rendered content, CSRF, CORS misunderstandings, SSRF, path traversal, open redirects, unsafe file uploads, and insecure deserialization.
- Password hashing with established algorithms/libraries; salts, work factors, credential storage, resets, and verification workflow.
- JWT validation pitfalls, audience/issuer/expiry checks, key management, revocation trade-offs, and refresh token rotation.
- OAuth 2.0 and OIDC flows appropriate to API/resource-server work; do not invent a custom auth protocol.
- TLS/certificate validation basics, secrets rotation, key storage, and credential leakage prevention.
- Dependency scanning, software supply-chain risks, SBOMs, licenses, and dependency update policies.
- Sensitive data in logs, error messages, analytics, metrics, backups, and support dumps.
- Rate limiting and resource-consumption attacks.
- Audit logs versus application logs and tamper-resistance considerations.

## H. Testing, quality, and release engineering

- Test pyramid and test-scope selection.
- Unit, component, integration, contract, end-to-end, smoke, regression, and performance testing.
- Test doubles and when not to mock.
- Testcontainers lifecycle and reproducibility.
- Property-based testing and mutation testing as optional advanced practices.
- Architecture tests and static analysis.
- Code formatters and tools such as Checkstyle, PMD, SpotBugs, and dependency vulnerability scanning; select compatible tools rather than adding all of them blindly.
- Coverage interpretation and its limits.
- CI workflows, build caching, reproducible builds, artifact publication, environment promotion, and rollback.
- Database migration testing and deployment ordering.
- Semantic versioning and backward compatibility.
- Supply-chain verification, SBOM, license checks, and secret scanning.

## I. Observability, deployment, and operations

- Structured logs, log levels, correlation/trace IDs, and sensitive-data redaction.
- Metrics versus logs versus traces.
- OpenTelemetry context propagation, trace sampling, and instrumentation boundaries.
- Micrometer and Actuator integration.
- Health, readiness, liveness, startup checks, and graceful shutdown.
- SLI, SLO, error budgets, alerts, and actionable alert design.
- Container image design, non-root execution, minimal images, resource limits, signals, and secrets.
- Docker Compose local development and test dependencies.
- Linux process/network basics, shell commands, permissions, signals, file descriptors, and service logs.
- Reverse proxies, TLS termination, load balancers, and deployment topologies at a conceptual level.
- Cloud-neutral deployment principles; cloud-provider specifics may live in optional specialization folders.
- Kubernetes concepts only if justified for the desired project scope; do not make Kubernetes a prerequisite for the first Java backend projects.
- Backups, restore drills, disaster recovery, RPO/RTO, and operational runbooks.

## J. Distributed systems and system design

- Partial failure, latency, timeouts, retries, jitter, retry budgets, circuit breakers, and bulkheads.
- At-least-once delivery, duplicate messages, ordering, poison messages, dead-letter queues, replay, and idempotent consumers.
- Event schema evolution, compatibility, event versioning, and schema registry concepts.
- Outbox/inbox patterns, saga orchestration/choreography, and compensating actions.
- Consistency models, eventual consistency, conflict handling, and the limits of distributed locks.
- API gateway and service discovery trade-offs.
- Service ownership, bounded contexts, shared databases versus service-owned data, and service extraction costs.
- Caching patterns, invalidation, stampedes, stale data, TTL, and failure behavior.
- Rate limiting algorithms: fixed window, sliding window, token bucket, and leaky bucket.
- Multi-tenancy models, tenant context propagation, row/schema/database isolation options, and leakage testing.
- Capacity planning and graceful degradation.

## K. Domain and backend-specific topics

- Money and decimal correctness, currency and rounding rules.
- Time zones, UTC, scheduling, daylight-saving transitions, and recurring schedules.
- Identifiers, UUIDs, idempotency keys, and uniqueness constraints.
- State machines and domain invariants.
- Audit history, soft deletion trade-offs, retention, and privacy deletion requirements.
- Bulk imports, validation reports, streaming, and resumability.
- Search/filtering and index synchronization.
- Background jobs, retries, leases, cancellation, job deduplication, and recovery.
- Notification delivery, provider failure, templates, preferences, and delivery status.
- Multi-tenant authorization and organizational hierarchy rules.
- Reporting queries versus operational queries.

This gap checklist must be evaluated, not copied mechanically. Explain why each added concept matters and where it belongs.

---

# 9. Supplemental Folder Rules

The core roadmap should remain readable and ordered. Use supplemental tracks for substantial topics that are useful but not required before the first ten projects.

Potential supplemental tracks include:

1. **Java Platform and Language Evolution:** JPMS, annotation processing, reflection, advanced bytecode/JVM diagnostics, native interop, packaging, and release-specific language evolution.
2. **Advanced Persistence and Database Engineering:** advanced PostgreSQL internals, JPA internals, migration operations, and backup/recovery.
3. **Advanced Spring and Web Engineering:** Spring MVC lifecycle, WebFlux/reactive programming, advanced security integration, advanced testing, and upgrade strategy.
4. **Secure Backend Engineering:** threat modeling, OAuth/OIDC details, application security, supply-chain controls, and advanced API security.
5. **Linux, Containers, and Deployment:** process/network debugging, container hardening, reverse proxies, deployment patterns, and operational runbooks.
6. **Optional Specializations:** Kafka internals, deeper Redis, Kubernetes, cloud-provider services, native image compilation, JNI, GraalVM, and other areas after confirming their relevance and current support status.

Create one concept file per independently teachable supplemental concept too. Mark supplemental concepts as **core-extension**, **advanced**, or **optional-specialist** and identify their prerequisites.

---

# 10. Concept Inventory and Naming Rules

Assign a stable ID to every concept, for example:

- `JAVA-ENV-001`
- `JAVA-LANG-001`
- `JAVA-OOP-001`
- `JAVA-COL-001`
- `JAVA-JVM-001`
- `JAVA-CONC-001`
- `JAVA-DB-001`
- `JAVA-SPRING-001`
- `JAVA-SEC-001`
- `JAVA-OPS-001`

The ID must be unique and must not change when a file is moved or renamed. File names may include sequential numbers for reading order, but links should be checked after renames.

For each concept inventory row, include the intended Markdown filename and status. For any source topic split across multiple pages, list all generated IDs. For any page that covers multiple source topics, list every covered source item.

Create a consistency check or script if useful to verify:

- Every phase 00–23 has a directory and README.
- Every concept inventory entry has a corresponding Markdown file.
- Every source item has at least one mapped concept page.
- All internal Markdown links resolve.
- Concept IDs are unique.
- No completed concept page is an empty placeholder.
- `PROGRESS.md` counts match the inventory.

If scripts are created, document exactly how to run them. Do not add a complex build system merely to validate Markdown if simple checks suffice.

---

# 11. Required Phase README Contents

Each phase `README.md` must contain:

1. Phase name and purpose.
2. What a learner should know before starting.
3. Measurable learning outcomes.
4. Concept table with ID, concept name, file link, importance, and status.
5. Recommended study order.
6. Critical distinctions or misconceptions in the phase.
7. One or more practical exercises integrating the phase's concepts.
8. A mastery assessment and acceptance criteria.
9. Common failure patterns.
10. Links to prerequisites, downstream phases, and supplemental material.
11. A truthful status count generated from the inventory.

A phase README must serve as a navigation page, not repeat the entire contents of all its concept pages.

---

# 12. Progress Tracking and Definition of Done

Create and maintain `PROGRESS.md` and `BUILD_STATUS.md`.

For each concept, track these separate states:

- `NOT_STARTED`
- `INVENTORIED`
- `RESEARCHED`
- `DRAFTED`
- `TECHNICALLY_REVIEWED`
- `EXAMPLES_CHECKED`
- `LINKS_CHECKED`
- `COMPLETE`

Do not mark a concept `COMPLETE` merely because the file exists. A concept is complete only when:

1. It covers its assigned inventory items.
2. It follows the concept-page standard.
3. Important technical claims are supported by reliable sources or clearly labelled as unverified.
4. Examples are logically checked and, when feasible, compiled or tested using the stated environment.
5. Version-sensitive statements are labelled and checked.
6. Interview answers and exercises are technically correct.
7. Cross-links and source links have been checked.
8. It contains no unexplained placeholders or fake citations.
9. It has been reviewed for duplication, clarity, and contradictions with canonical pages.
10. Its status and review date are recorded accurately.

Track separate counts for source items mapped, concept pages planned, concept pages completed, and unresolved audit gaps. Never inflate completion percentages by counting empty files.

When tool/context limits prevent completing everything in one working session, finish a coherent batch, update the status files, list the exact next concept IDs, and preserve a resumable queue. Continue in inventory order on later execution. Do not claim all phases or concepts are complete when only a subset was created.

Do not pause for approval after every concept. Proceed through the planned queue autonomously within the available workspace and tool limits. Ask for clarification only if a genuine blocker cannot be resolved safely from the existing files. Never destructively modify user data to keep the workflow moving.

---

# 13. How to Work Through the Queue

Use this execution sequence:

## Stage 1 — Audit and planning

- Inspect the workspace and read the source roadmap completely.
- Create the folder skeleton, root documentation, inventory, traceability map, dependency map, and gap audit.
- Confirm all 24 phase directories and every source item are represented in the inventory.
- Add the missing concepts identified by the gap audit.
- Record the baseline Java, build tool, framework line, and source verification method without assuming unsupported versions.

## Stage 2 — Establish the templates and navigation

- Write the concept template and phase README template.
- Create a working `MASTER_INDEX.md` with links to all phases and supplemental tracks.
- Create `SOURCE_REGISTER.md` with official documentation families and actual sources consulted.
- Make sure the proposed concept list can be navigated before writing the full pages.

## Stage 3 — Research and write one concept at a time

For each concept in dependency order:

1. Read the concept's prerequisites and the source roadmap wording.
2. Consult official documentation and other reliable sources.
3. Create or update exactly that concept file.
4. Apply the concept-page template.
5. Validate the examples and version assumptions.
6. Add cross-links to prerequisites and related concepts.
7. Update the inventory and progress files immediately.
8. Check for duplication or contradiction with existing pages.
9. Move to the next concept only after the current file is at a coherent checkpoint.

Do not generate hundreds of shallow files from one vague prompt. Research and author them sequentially in sensible batches. The final goal remains all planned concepts; a batch boundary is not permission to leave the rest untracked.

## Stage 4 — Phase-level review

After completing each phase:

- Compare the phase README and files against every source item for that phase.
- Run the available link/structure checks.
- Review concept consistency and prerequisites.
- Add any missing pages discovered during review.
- Update the phase completion state based on evidence.

## Stage 5 — Global consistency audit

After the content is written:

- Re-check source-to-concept traceability.
- Review missing-topics audit items.
- Check for internal contradictions, outdated versions, duplicate explanations, broken links, missing sources, and code sample inconsistencies.
- Confirm the dependency map reflects the actual curriculum.
- Reconcile all progress counts.
- Record unresolved items honestly rather than inventing a resolution.

---

# 14. Standards for Examples and Runnable Code

1. Use the chosen Java baseline consistently. Mark any newer feature and show a compatible alternative when useful.
2. Ensure import statements and type names are correct.
3. Do not use pseudo-code without labelling it as pseudo-code.
4. For runnable examples, include the filename and command where appropriate.
5. For multi-file examples, show a clear directory tree and each required file.
6. For Spring Boot examples, include relevant dependencies/configuration and state assumptions about the Spring Boot version.
7. For SQL examples, state the target database, preferably PostgreSQL for the main curriculum, and flag vendor-specific syntax.
8. For concurrency examples, explain nondeterministic outcomes and provide a reproducible test strategy where possible.
9. For performance examples, avoid timing claims without proper measurement.
10. For security examples, show safe implementations and explain unsafe patterns without leaving dangerous code as the recommended solution.
11. Include tests when a behavior can reasonably be tested.
12. Never put real secrets, live credentials, personal information, or unnecessary external service dependencies into examples.
13. Keep examples small enough to teach one concept, then use realistic integrated examples to show how the concept fits into a backend.
14. Avoid pretending a tiny sample is production-ready. Identify omitted production controls explicitly.

---

# 15. Editorial Style and Learning Quality

Write in clear, technically precise English. Explain jargon the first time it appears. Use tables when comparing alternatives, diagrams when they improve understanding, and code blocks for actual code. Avoid unnecessary repetition and inflated prose.

For every difficult concept, explain both **how it works** and **why an engineer should care**. Prefer diagrams or text sequence flows for execution, transaction, request, event, and concurrency behavior when helpful.

Use progressive disclosure:

- Start with an intuitive explanation.
- Follow with the formal model and important rules.
- Show code and a concrete execution trace.
- Discuss edge cases and trade-offs.
- Finish with real-world use, testing, debugging, and interview application.

Challenge common but misleading simplifications. Examples include:

- “Java is pass-by-reference.” It is not; Java passes values, including object-reference values.
- “`volatile` makes operations atomic.” It does not generally make compound operations atomic.
- “`ConcurrentHashMap` makes all multi-step workflows atomic.” It does not unless the required operation is designed around its atomic API.
- “Adding an index always makes a query faster.” Indexes have costs and may not be selected.
- “JWT means an application is secure.” Security depends on validation, key management, authorization, lifecycle, and the surrounding design.
- “Microservices are automatically more scalable.” Distribution introduces network and operational costs.
- “High code coverage proves correctness.” Coverage does not establish that assertions are meaningful.
- “Virtual threads remove concurrency problems.” They do not eliminate shared-state races or all resource limits.

Do not repeat these examples mechanically on unrelated pages; use them where relevant.

---

# 16. Definition of a Useful Learning Library

The final deliverable is not judged by raw page count. It is judged by traceability, accuracy, pedagogical quality, useful depth, runnable examples, and the learner's ability to apply the knowledge.

The library should allow me to:

- Find a concept quickly using a global index.
- Understand it from foundational theory through advanced implications.
- See correct and incorrect examples.
- Apply it to Java backend scenarios.
- Learn the important standard-library/framework APIs.
- Test and debug implementations.
- Understand complexity, security, and operational trade-offs where relevant.
- Prepare for technical interviews with justified answers.
- Practise independently with graduated exercises.
- Follow dependencies and know what to study next.
- Identify what is version-dependent or still needs verification.
- Trace every source roadmap item to one or more concept files.
- See exactly what has and has not been completed.

Completeness must come from the inventory and review process, not from calling the material “ultimate,” “world-class,” or “covering everything ever written.”

---

# 17. Final Deliverables

When finished, the workspace must contain:

1. `README.md` — purpose, setup, navigation, and instructions for studying the library.
2. `MASTER_INDEX.md` — global clickable index of all phases and supplemental tracks.
3. `LEARNING_PATH.md` — recommended order, prerequisites, and milestone gates.
4. `CONCEPT_INVENTORY.md` — the complete canonical list of concept pages and their status.
5. `ROADMAP_TRACEABILITY.md` — mapping from every source-roadmap item to concept file(s).
6. `MISSING_TOPICS_AUDIT.md` — identified gaps, decisions, and where added topics were placed.
7. `PHASE_DEPENDENCIES.md` — prerequisite and downstream dependency graph/table.
8. `PROGRESS.md` — accurate completion state and counts.
9. `BUILD_STATUS.md` — work summary, files created, checks run, limitations, and resumable queue.
10. `SOURCE_REGISTER.md` — actual sources consulted, with reliable links and review dates.
11. `GLOSSARY.md` — deduplicated key terminology with links to canonical concepts.
12. `CHANGELOG.md` — significant curriculum and content changes.
13. `templates/CONCEPT_TEMPLATE.md` and related templates.
14. A directory and phase README for every phase 00–23.
15. A dedicated concept Markdown file for every inventoried core and supplemental concept.
16. A final consistency review or a clearly documented list of checks still pending.

---

# 18. Initial Execution Instructions

Begin now and work directly in the current workspace.

1. Inspect the repository and locate the source roadmap.
2. Read the roadmap completely.
3. Create the inventory and traceability map before writing the full concept library.
4. Create the gap audit and list the curriculum additions that are justified.
5. Create all 24 phase folders and their README files, along with supplemental tracks as needed.
6. Create the templates and root navigation files.
7. Start authoring actual concept files in prerequisite order, beginning with Phase 00, then Phase 01, and so on unless the dependency map justifies moving a prerequisite earlier.
8. Produce substantial, accurate concept content one concept at a time. Do not leave placeholder pages marked complete.
9. Update progress after every concept and run available validation checks at sensible milestones.
10. Continue through as much of the queue as the available working session and tool limits permit. When the session must stop, leave a precise resume point with the next concept IDs and remaining checks.
11. Do not implement the 100 backend projects in this task. Build the prerequisite knowledge base that will prepare me to do those projects later.

At each checkpoint, report only verified facts: number of source items inventoried, number of concept pages planned and completed, phases completed, checks run, known gaps, and the next queued concept IDs. Do not use a success label that overstates the evidence.

**Start by inspecting the workspace and the complete roadmap. Then build the inventory and gap audit before generating the detailed concept pages.**
