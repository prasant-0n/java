# Master Index: Java Backend Engineering Concept Library

---

## Quick Navigation

- [Curriculum Architecture & Overview](#curriculum-architecture--overview)
- [Core Learning Phases (Phases 00–23)](#core-learning-phases-phases-0023)
- [Supplemental Tracks & Advanced Specializations](#supplemental-tracks--advanced-specializations)
- [Templates & Governance Documents](#templates--governance-documents)

---

## Curriculum Architecture & Overview

| Navigation Document | Purpose |
|---|---|
| [`README.md`](README.md) | Library entry point, setup instructions, study guidelines |
| [`LEARNING_PATH.md`](LEARNING_PATH.md) | Progressive study order and 100-project readiness milestones |
| [`CONCEPT_INVENTORY.md`](CONCEPT_INVENTORY.md) | Granular inventory of all 312 planned and completed modules |
| [`ROADMAP_TRACEABILITY.md`](ROADMAP_TRACEABILITY.md) | Bidirectional audit matrix mapping source roadmap bullets to files |
| [`MISSING_TOPICS_AUDIT.md`](MISSING_TOPICS_AUDIT.md) | Exhaustive gap audit and architectural decisions |
| [`PHASE_DEPENDENCIES.md`](PHASE_DEPENDENCIES.md) | Prerequisite DAG, dependency graph, and cycle verification |
| [`PROGRESS.md`](PROGRESS.md) | Granular completion progress tracker and queue |
| [`BUILD_STATUS.md`](BUILD_STATUS.md) | Build state, environment checks, verification log |
| [`SOURCE_REGISTER.md`](SOURCE_REGISTER.md) | Primary authorities, specifications, and JEP references |
| [`GLOSSARY.md`](GLOSSARY.md) | Deduplicated technical terminology from first principles |
| [`CHANGELOG.md`](CHANGELOG.md) | Historical record of library updates and milestones |

---

## Core Learning Phases (Phases 00–23)

### [Phase 00: Development Environment and Java Execution](curriculum/phase-00-development-environment-java-execution/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P00-C001` | JDK vs JRE vs JVM Architecture and Evolution | [`001-jdk-jre-jvm.md`](curriculum/phase-00-development-environment-java-execution/001-jdk-jre-jvm.md) | `COMPLETE` |
| `P00-C002` | Java Compiler (javac) vs Launcher (java) Workflow | [`002-javac-compiler-and-launcher.md`](curriculum/phase-00-development-environment-java-execution/002-javac-compiler-and-launcher.md) | `COMPLETE` |
| `P00-C003` | Source Files (.java) and Bytecode (.class) Disassembly | [`003-source-files-and-bytecode.md`](curriculum/phase-00-development-environment-java-execution/003-source-files-and-bytecode.md) | `COMPLETE` |
| `P00-C004` | Compilation, Execution, and Classpath Mechanics | [`004-classpath-and-package-structure.md`](curriculum/phase-00-development-environment-java-execution/004-classpath-and-package-structure.md) | `COMPLETE` |
| `P00-C005` | Java Entry Point: public static void main Signature and Evolution | [`005-entry-point-main-method.md`](curriculum/phase-00-development-environment-java-execution/005-entry-point-main-method.md) | `COMPLETE` |
| `P00-C006` | Single-File Source-Code Launching and Shebang Scripts | [`006-single-file-source-execution.md`](curriculum/phase-00-development-environment-java-execution/006-single-file-source-execution.md) | `COMPLETE` |
| `P00-C007` | Environment Variables: JAVA_HOME and PATH in Linux/Production | [`007-environment-variables-java-home-path.md`](curriculum/phase-00-development-environment-java-execution/007-environment-variables-java-home-path.md) | `COMPLETE` |
| `P00-C008` | JDK Versions, Release Cadence, LTS, and Bytecode Compatibility | [`008-jdk-versions-lts-compatibility.md`](curriculum/phase-00-development-environment-java-execution/008-jdk-versions-lts-compatibility.md) | `COMPLETE` |
| `P00-C009` | IDEs vs Headless Terminal Development in Server Environments | [`009-ide-vs-terminal-development.md`](curriculum/phase-00-development-environment-java-execution/009-ide-vs-terminal-development.md) | `COMPLETE` |
| `P00-C010` | Maven Fundamentals and Standard Directory Layout | [`010-maven-fundamentals-project-structure.md`](curriculum/phase-00-development-environment-java-execution/010-maven-fundamentals-project-structure.md) | `COMPLETE` |
| `P00-C011` | Git Hygiene and .gitignore for Java/Maven Projects | [`011-git-basics-for-java-developers.md`](curriculum/phase-00-development-environment-java-execution/011-git-basics-for-java-developers.md) | `COMPLETE` |

### [Phase 01: Java Language Fundamentals](curriculum/phase-01-java-language-fundamentals/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P01-C001` | Primitive Data Types, Bit Widths, and Numeric Ranges | [`001-primitive-data-types.md`](curriculum/phase-01-java-language-fundamentals/001-primitive-data-types.md) | `COMPLETE` |
| `P01-C002` | Floating-Point Precision, IEEE 754, and Rounding Hazards | [`002-floating-point-types-and-precision.md`](curriculum/phase-01-java-language-fundamentals/002-floating-point-types-and-precision.md) | `COMPLETE` |
| `P01-C003` | Reference Types vs Primitive Types in Memory | [`003-reference-types-vs-primitives.md`](curriculum/phase-01-java-language-fundamentals/003-reference-types-vs-primitives.md) | `COMPLETE` |
| `P01-C004` | Variable Declaration, Scope, Lifetime, and Default Values | [`004-variable-scopes-and-lifetimes.md`](curriculum/phase-01-java-language-fundamentals/004-variable-scopes-and-lifetimes.md) | `COMPLETE` |
| `P01-C005` | Type Casting: Widening, Narrowing, and Overflow Traps | [`005-type-casting-and-overflow.md`](curriculum/phase-01-java-language-fundamentals/005-type-casting-and-overflow.md) | `COMPLETE` |
| `P01-C006` | Wrapper Classes, Autoboxing, and NullPointerExceptions | [`006-wrapper-classes-autoboxing.md`](curriculum/phase-01-java-language-fundamentals/006-wrapper-classes-autoboxing.md) | `COMPLETE` |
| `P01-C007` | Local Variable Type Inference (var) and Limitations | [`007-local-variable-type-inference-var.md`](curriculum/phase-01-java-language-fundamentals/007-local-variable-type-inference-var.md) | `COMPLETE` |
| `P01-C008` | Operators, Precedence, and Short-Circuit Evaluation | [`008-operators-and-short-circuit-evaluation.md`](curriculum/phase-01-java-language-fundamentals/008-operators-and-short-circuit-evaluation.md) | `COMPLETE` |
| `P01-C009` | Control Flow: Conditionals, Switch Statements, and Switch Expressions | [`009-control-flow-conditionals-switch.md`](curriculum/phase-01-java-language-fundamentals/009-control-flow-conditionals-switch.md) | `COMPLETE` |
| `P01-C010` | Looping Constructs, Jump Statements, and Labels | [`010-loops-and-iteration.md`](curriculum/phase-01-java-language-fundamentals/010-loops-and-iteration.md) | `COMPLETE` |
| `P01-C011` | Methods: Signatures, Contracts, and Varargs | [`011-methods-and-varargs.md`](curriculum/phase-01-java-language-fundamentals/011-methods-and-varargs.md) | `COMPLETE` |
| `P01-C012` | Pass-by-Value Semantics in Java (Why References Are Passed by Value) | [`012-pass-by-value-semantics.md`](curriculum/phase-01-java-language-fundamentals/012-pass-by-value-semantics.md) | `COMPLETE` |
| `P01-C013` | String Immutability, String Pool, and Memory Optimization | [`013-string-immutability-and-pool.md`](curriculum/phase-01-java-language-fundamentals/013-string-immutability-and-pool.md) | `COMPLETE` |
| `P01-C014` | StringBuilder vs StringBuffer vs String Concatenation Mechanics | [`014-stringbuilder-and-concatenation.md`](curriculum/phase-01-java-language-fundamentals/014-stringbuilder-and-concatenation.md) | `COMPLETE` |
| `P01-C015` | Arrays: Memory Layout, Multi-Dimensional Arrays, and Arrays Utility | [`015-arrays-memory-layout-and-utilities.md`](curriculum/phase-01-java-language-fundamentals/015-arrays-memory-layout-and-utilities.md) | `COMPLETE` |

### [Phase 02: Object-Oriented Programming](curriculum/phase-02-object-oriented-programming/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P02-C001` | Classes, Objects, Fields, and Constructor Chaining | [`001-classes-objects-constructors.md`](curriculum/phase-02-object-oriented-programming/001-classes-objects-constructors.md) | `COMPLETE` |
| `P02-C002` | Static State, Class Loading, and Initialization Order Hazards | [`002-static-members-and-initialization.md`](curriculum/phase-02-object-oriented-programming/002-static-members-and-initialization.md) | `COMPLETE` |
| `P02-C003` | Access Modifiers, Encapsulation, and Package-Private Boundaries | [`003-access-modifiers-encapsulation.md`](curriculum/phase-02-object-oriented-programming/003-access-modifiers-encapsulation.md) | `COMPLETE` |
| `P02-C004` | The Four Pillars of OOP in Java Backend Design | [`004-four-pillars-of-oop.md`](curriculum/phase-02-object-oriented-programming/004-four-pillars-of-oop.md) | `COMPLETE` |
| `P02-C005` | Inheritance (extends) vs Subtyping vs Fragile Base Class Problem | [`005-inheritance-and-fragile-base-class.md`](curriculum/phase-02-object-oriented-programming/005-inheritance-and-fragile-base-class.md) | `COMPLETE` |
| `P02-C006` | Method Overriding, Upcasting/Downcasting, and Dynamic Dispatch | [`006-overriding-dynamic-dispatch.md`](curriculum/phase-02-object-oriented-programming/006-overriding-dynamic-dispatch.md) | `COMPLETE` |
| `P02-C007` | Abstract Classes vs Interfaces: Contracts and Default Methods | [`007-abstract-classes-vs-interfaces.md`](curriculum/phase-02-object-oriented-programming/007-abstract-classes-vs-interfaces.md) | `COMPLETE` |
| `P02-C008` | Composition over Inheritance in Business Domain Services | [`008-composition-over-inheritance.md`](curriculum/phase-02-object-oriented-programming/008-composition-over-inheritance.md) | `COMPLETE` |
| `P02-C009` | Object Relationships: Association, Aggregation, and Composition | [`009-object-relationships.md`](curriculum/phase-02-object-oriented-programming/009-object-relationships.md) | `COMPLETE` |
| `P02-C010` | Enums as Rich Domain Objects and Singleton Patterns | [`010-enums-and-domain-state.md`](curriculum/phase-02-object-oriented-programming/010-enums-and-domain-state.md) | `COMPLETE` |
| `P02-C011` | Nested, Inner, and Anonymous Classes | [`011-nested-and-inner-classes.md`](curriculum/phase-02-object-oriented-programming/011-nested-and-inner-classes.md) | `COMPLETE` |
| `P02-C012` | Records: Immutable Data Carriers, Compact Constructors, and DTOs | [`012-records-and-immutable-data.md`](curriculum/phase-02-object-oriented-programming/012-records-and-immutable-data.md) | `COMPLETE` |
| `P02-C013` | Sealed Classes, Sealed Interfaces, and Algebraic Data Types | [`013-sealed-classes-and-interfaces.md`](curriculum/phase-02-object-oriented-programming/013-sealed-classes-and-interfaces.md) | `COMPLETE` |
| `P02-C014` | Pattern Matching for instanceof and Switch Expressions | [`014-pattern-matching-oop.md`](curriculum/phase-02-object-oriented-programming/014-pattern-matching-oop.md) | `COMPLETE` |
| `P02-C015` | The Object Superclass Contract: equals(), hashCode(), toString(), clone() | [`015-object-class-contracts.md`](curriculum/phase-02-object-oriented-programming/015-object-class-contracts.md) | `COMPLETE` |

### [Phase 03: Collections Framework, Generics, and Type Safety](curriculum/phase-03-collections-framework-generics-type-safety/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P03-C001` | Collections Framework Architecture: Iterable, Collection, and Map | [`001-collections-framework-hierarchy.md`](curriculum/phase-03-collections-framework-generics-type-safety/001-collections-framework-hierarchy.md) | `NOT_STARTED` |
| `P03-C002` | List Implementations: ArrayList vs LinkedList Performance and Memory | [`002-arraylist-vs-linkedlist.md`](curriculum/phase-03-collections-framework-generics-type-safety/002-arraylist-vs-linkedlist.md) | `NOT_STARTED` |
| `P03-C003` | Set Implementations: HashSet, LinkedHashSet, and TreeSet | [`003-set-implementations.md`](curriculum/phase-03-collections-framework-generics-type-safety/003-set-implementations.md) | `NOT_STARTED` |
| `P03-C004` | HashMap Internals: Hash Codes, Buckets, Treeification, and Rehashing | [`004-hashmap-internals-and-mechanics.md`](curriculum/phase-03-collections-framework-generics-type-safety/004-hashmap-internals-and-mechanics.md) | `NOT_STARTED` |
| `P03-C005` | LinkedHashMap and TreeMap: Ordering, Red-Black Trees, and LRU Caches | [`005-linkedhashmap-treemap-ordering.md`](curriculum/phase-03-collections-framework-generics-type-safety/005-linkedhashmap-treemap-ordering.md) | `NOT_STARTED` |
| `P03-C006` | Queue and Deque Implementations: ArrayDeque vs PriorityQueue | [`006-queues-and-deques.md`](curriculum/phase-03-collections-framework-generics-type-safety/006-queues-and-deques.md) | `NOT_STARTED` |
| `P03-C007` | Concurrent Collections Introduction: ConcurrentHashMap vs Collections.synchronizedMap | [`007-concurrenthashmap-overview.md`](curriculum/phase-03-collections-framework-generics-type-safety/007-concurrenthashmap-overview.md) | `NOT_STARTED` |
| `P03-C008` | Immutable Collections (List.of, Set.of, Map.of) vs Unmodifiable Views | [`008-immutable-collections-and-views.md`](curriculum/phase-03-collections-framework-generics-type-safety/008-immutable-collections-and-views.md) | `NOT_STARTED` |
| `P03-C009` | The equals() and hashCode() Contract, Hash Collisions, and Mutable Keys | [`009-equals-hashcode-contract.md`](curriculum/phase-03-collections-framework-generics-type-safety/009-equals-hashcode-contract.md) | `NOT_STARTED` |
| `P03-C010` | Comparable and Comparator: Natural Ordering, Custom Sorts, and Null Safety | [`010-comparable-and-comparator.md`](curriculum/phase-03-collections-framework-generics-type-safety/010-comparable-and-comparator.md) | `NOT_STARTED` |
| `P03-C011` | Iterators, Fail-Fast vs Fail-Safe Mechanics, and Safe Removal | [`011-iterators-fail-fast-mechanics.md`](curriculum/phase-03-collections-framework-generics-type-safety/011-iterators-fail-fast-mechanics.md) | `NOT_STARTED` |
| `P03-C012` | Generics Fundamentals: Type Parameters, Generic Classes, and Methods | [`012-generics-classes-and-methods.md`](curriculum/phase-03-collections-framework-generics-type-safety/012-generics-classes-and-methods.md) | `NOT_STARTED` |
| `P03-C013` | Bounded Type Parameters and Multiple Bounds | [`013-bounded-type-parameters.md`](curriculum/phase-03-collections-framework-generics-type-safety/013-bounded-type-parameters.md) | `NOT_STARTED` |
| `P03-C014` | Wildcards and PECS (Producer Extends, Consumer Super) Principle | [`014-wildcards-and-pecs.md`](curriculum/phase-03-collections-framework-generics-type-safety/014-wildcards-and-pecs.md) | `NOT_STARTED` |
| `P03-C015` | Type Erasure, Bridge Methods, Heap Pollution, and Reifiable Types | [`015-type-erasure-and-heap-pollution.md`](curriculum/phase-03-collections-framework-generics-type-safety/015-type-erasure-and-heap-pollution.md) | `NOT_STARTED` |

### [Phase 04: Exception Handling and Defensive Programming](curriculum/phase-04-exception-handling-defensive-programming/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P04-C001` | The Throwable Hierarchy: Error, Exception, and RuntimeException | [`001-throwable-hierarchy.md`](curriculum/phase-04-exception-handling-defensive-programming/001-throwable-hierarchy.md) | `NOT_STARTED` |
| `P04-C002` | Checked vs Unchecked Exceptions: Architectural Philosophy and Debates | [`002-checked-vs-unchecked-exceptions.md`](curriculum/phase-04-exception-handling-defensive-programming/002-checked-vs-unchecked-exceptions.md) | `NOT_STARTED` |
| `P04-C003` | Exception Handling Blocks: try, catch, multi-catch, and finally Semantics | [`003-try-catch-finally-semantics.md`](curriculum/phase-04-exception-handling-defensive-programming/003-try-catch-finally-semantics.md) | `NOT_STARTED` |
| `P04-C004` | Try-With-Resources, AutoCloseable, and Suppressed Exceptions | [`004-try-with-resources-autocloseable.md`](curriculum/phase-04-exception-handling-defensive-programming/004-try-with-resources-autocloseable.md) | `NOT_STARTED` |
| `P04-C005` | Custom Domain Exceptions, Error Envelopes, and Context Propagation | [`005-custom-domain-exceptions.md`](curriculum/phase-04-exception-handling-defensive-programming/005-custom-domain-exceptions.md) | `NOT_STARTED` |
| `P04-C006` | Exception Chaining, Root Causes, and Preserving Stack Traces | [`006-exception-chaining-and-causes.md`](curriculum/phase-04-exception-handling-defensive-programming/006-exception-chaining-and-causes.md) | `NOT_STARTED` |
| `P04-C007` | Defensive Programming: Preconditions, Invariants, and Fail-Fast Design | [`007-defensive-programming-fail-fast.md`](curriculum/phase-04-exception-handling-defensive-programming/007-defensive-programming-fail-fast.md) | `NOT_STARTED` |
| `P04-C008` | Anti-Patterns: Swallowed Exceptions, Catching Throwable, and Log-and-Throw | [`008-exception-anti-patterns.md`](curriculum/phase-04-exception-handling-defensive-programming/008-exception-anti-patterns.md) | `NOT_STARTED` |
| `P04-C009` | Exceptions vs Result/Either Types for Expected Business Failures | [`009-exceptions-vs-result-types.md`](curriculum/phase-04-exception-handling-defensive-programming/009-exceptions-vs-result-types.md) | `NOT_STARTED` |

### [Phase 05: Core Java APIs You Will Use in Production](curriculum/phase-05-core-java-apis-production/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P05-C001` | Java Time Architecture: Instant, LocalDate, LocalDateTime, and ZonedDateTime | [`001-java-time-api-fundamentals.md`](curriculum/phase-05-core-java-apis-production/001-java-time-api-fundamentals.md) | `NOT_STARTED` |
| `P05-C002` | Time Zones, UTC Storage, Daylight Saving Hazards, and ZoneId | [`002-timezones-utc-and-dst.md`](curriculum/phase-05-core-java-apis-production/002-timezones-utc-and-dst.md) | `NOT_STARTED` |
| `P05-C003` | Periods, Durations, and DateTimeFormatter Parsing Patterns | [`003-periods-durations-and-formatting.md`](curriculum/phase-05-core-java-apis-production/003-periods-durations-and-formatting.md) | `NOT_STARTED` |
| `P05-C004` | BigDecimal and BigInteger: Precision, Scale, and Rounding Modes | [`004-bigdecimal-and-financial-math.md`](curriculum/phase-05-core-java-apis-production/004-bigdecimal-and-financial-math.md) | `NOT_STARTED` |
| `P05-C005` | Optional<T>: Purpose, Monadic Methods, and Anti-Patterns | [`005-optional-idioms-and-anti-patterns.md`](curriculum/phase-05-core-java-apis-production/005-optional-idioms-and-anti-patterns.md) | `NOT_STARTED` |
| `P05-C006` | UUID Generation, Structure, UUIDv4 vs UUIDv7, and Database Indexing | [`006-uuid-generation-and-indexing.md`](curriculum/phase-05-core-java-apis-production/006-uuid-generation-and-indexing.md) | `NOT_STARTED` |
| `P05-C007` | Objects Utility Class: Null Checks, Equality, and RequireNonNull | [`007-objects-utility-class.md`](curriculum/phase-05-core-java-apis-production/007-objects-utility-class.md) | `NOT_STARTED` |
| `P05-C008` | Random vs SecureRandom: PRNG Mechanics, Entropy, and Security Implications | [`008-random-vs-securerandom.md`](curriculum/phase-05-core-java-apis-production/008-random-vs-securerandom.md) | `NOT_STARTED` |
| `P05-C009` | System Properties and Environment Variables in Java Applications | [`009-system-properties-and-env.md`](curriculum/phase-05-core-java-apis-production/009-system-properties-and-env.md) | `NOT_STARTED` |
| `P05-C010` | Regular Expressions: Pattern, Matcher, Groups, and ReDoS Prevention | [`010-regex-pattern-matcher-redos.md`](curriculum/phase-05-core-java-apis-production/010-regex-pattern-matcher-redos.md) | `NOT_STARTED` |

### [Phase 06: Functional Java](curriculum/phase-06-functional-java/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P06-C001` | Functional Interfaces and the @FunctionalInterface Contract | [`001-functional-interfaces-core.md`](curriculum/phase-06-functional-java/001-functional-interfaces-core.md) | `NOT_STARTED` |
| `P06-C002` | Core Function Types: Predicate, Function, Consumer, Supplier, and Primitives | [`002-core-functional-interfaces.md`](curriculum/phase-06-functional-java/002-core-functional-interfaces.md) | `NOT_STARTED` |
| `P06-C003` | Lambda Expressions: Syntax, Scope, Lexical Scoping, and Effectively Final | [`003-lambda-expressions-mechanics.md`](curriculum/phase-06-functional-java/003-lambda-expressions-mechanics.md) | `NOT_STARTED` |
| `P06-C004` | Method References: Static, Bound Instance, Unbound Instance, and Constructor | [`004-method-references.md`](curriculum/phase-06-functional-java/004-method-references.md) | `NOT_STARTED` |
| `P06-C005` | Stream API Fundamentals: Source, Intermediate, and Terminal Operations | [`005-stream-pipeline-lifecycle.md`](curriculum/phase-06-functional-java/005-stream-pipeline-lifecycle.md) | `NOT_STARTED` |
| `P06-C006` | Intermediate Operations: filter, map, flatMap, distinct, sorted, peek | [`006-intermediate-stream-operations.md`](curriculum/phase-06-functional-java/006-intermediate-stream-operations.md) | `NOT_STARTED` |
| `P06-C007` | Terminal Operations: forEach, collect, reduce, matchers, findFirst | [`007-terminal-stream-operations.md`](curriculum/phase-06-functional-java/007-terminal-stream-operations.md) | `NOT_STARTED` |
| `P06-C008` | Collectors: groupingBy, partitioningBy, toMap, and Downstream Collectors | [`008-collectors-grouping-partitioning.md`](curriculum/phase-06-functional-java/008-collectors-grouping-partitioning.md) | `NOT_STARTED` |
| `P06-C009` | Primitive Streams: IntStream, LongStream, DoubleStream and Performance | [`009-primitive-streams-performance.md`](curriculum/phase-06-functional-java/009-primitive-streams-performance.md) | `NOT_STARTED` |
| `P06-C010` | Stream Laziness, Short-Circuit Evaluation, and Side-Effect Dangers | [`010-stream-laziness-side-effects.md`](curriculum/phase-06-functional-java/010-stream-laziness-side-effects.md) | `NOT_STARTED` |
| `P06-C011` | Sequential vs Parallel Streams: Spliterators, ForkJoinPool, and Overhead | [`011-parallel-streams-and-forkjoin.md`](curriculum/phase-06-functional-java/011-parallel-streams-and-forkjoin.md) | `NOT_STARTED` |
| `P06-C012` | Stream vs Imperative Loops: Readability, Debuggability, and Benchmarks | [`012-stream-vs-imperative-benchmarks.md`](curriculum/phase-06-functional-java/012-stream-vs-imperative-benchmarks.md) | `NOT_STARTED` |

### [Phase 07: File I/O, NIO, Configuration, and Data Formats](curriculum/phase-07-file-io-nio-configuration-data-formats/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P07-C001` | Legacy java.io.File Limitations vs Modern java.nio.file (Path, Files, Paths) | [`001-legacy-io-vs-modern-nio.md`](curriculum/phase-07-file-io-nio-configuration-data-formats/001-legacy-io-vs-modern-nio.md) | `NOT_STARTED` |
| `P07-C002` | Byte Streams vs Character Streams, Encodings, and UTF-8 Guarantees | [`002-byte-streams-vs-readers-writers.md`](curriculum/phase-07-file-io-nio-configuration-data-formats/002-byte-streams-vs-readers-writers.md) | `NOT_STARTED` |
| `P07-C003` | Buffered Streams, Buffer Sizes, Flushing, and File Descriptor Leaks | [`003-buffered-streams-and-leaks.md`](curriculum/phase-07-file-io-nio-configuration-data-formats/003-buffered-streams-and-leaks.md) | `NOT_STARTED` |
| `P07-C004` | File Operations: Reading, Writing, Atomic Moves, and File Attributes | [`004-file-operations-atomic-moves.md`](curriculum/phase-07-file-io-nio-configuration-data-formats/004-file-operations-atomic-moves.md) | `NOT_STARTED` |
| `P07-C005` | Directory Traversal: DirectoryStream, Files.walk, and Resource Cleanup | [`005-directory-traversal-and-streams.md`](curriculum/phase-07-file-io-nio-configuration-data-formats/005-directory-traversal-and-streams.md) | `NOT_STARTED` |
| `P07-C006` | Java NIO Architecture: Channels, Buffers, Selectors, and Direct Memory | [`006-nio-channels-buffers-direct-memory.md`](curriculum/phase-07-file-io-nio-configuration-data-formats/006-nio-channels-buffers-direct-memory.md) | `NOT_STARTED` |
| `P07-C007` | Large-File Streaming Processing vs In-Memory Buffering | [`007-large-file-streaming-processing.md`](curriculum/phase-07-file-io-nio-configuration-data-formats/007-large-file-streaming-processing.md) | `NOT_STARTED` |
| `P07-C008` | ClassPath Resources vs Filesystem Paths in Packaged JARs | [`008-classpath-resources-in-jars.md`](curriculum/phase-07-file-io-nio-configuration-data-formats/008-classpath-resources-in-jars.md) | `NOT_STARTED` |
| `P07-C009` | Configuration Management: Properties Files and System Environment Overrides | [`009-properties-files-configuration.md`](curriculum/phase-07-file-io-nio-configuration-data-formats/009-properties-files-configuration.md) | `NOT_STARTED` |
| `P07-C010` | Structured Data: Safe CSV Parsing, Escaping, and Validation | [`010-csv-parsing-and-validation.md`](curriculum/phase-07-file-io-nio-configuration-data-formats/010-csv-parsing-and-validation.md) | `NOT_STARTED` |
| `P07-C011` | JSON Processing in Java: Jackson Object Mapper, Streaming API, and Records | [`011-json-processing-jackson.md`](curriculum/phase-07-file-io-nio-configuration-data-formats/011-json-processing-jackson.md) | `NOT_STARTED` |
| `P07-C012` | Java Native Serialization Security Vulnerabilities and Modern Alternatives | [`012-serialization-vulnerabilities.md`](curriculum/phase-07-file-io-nio-configuration-data-formats/012-serialization-vulnerabilities.md) | `NOT_STARTED` |

### [Phase 08: JVM Internals and Memory Management](curriculum/phase-08-jvm-internals-memory-management/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P08-C001` | JVM Architecture Overview: Class Loader, Runtime Data Areas, and Execution Engine | [`001-jvm-architecture-overview.md`](curriculum/phase-08-jvm-internals-memory-management/001-jvm-architecture-overview.md) | `NOT_STARTED` |
| `P08-C002` | Class Loading Subsystem: Loading, Linking, Initialization, and Hierarchies | [`002-class-loading-subsystem.md`](curriculum/phase-08-jvm-internals-memory-management/002-class-loading-subsystem.md) | `NOT_STARTED` |
| `P08-C003` | Runtime Data Areas: Thread Stacks, Frame Structure, and Program Counter | [`003-thread-stacks-and-stack-frames.md`](curriculum/phase-08-jvm-internals-memory-management/003-thread-stacks-and-stack-frames.md) | `NOT_STARTED` |
| `P08-C004` | JVM Heap Memory Layout: Young Gen, Old Gen, and Object Allocation | [`004-heap-memory-layout-generations.md`](curriculum/phase-08-jvm-internals-memory-management/004-heap-memory-layout-generations.md) | `NOT_STARTED` |
| `P08-C005` | Metaspace Architecture vs Legacy PermGen, Dynamic Class Generation Risks | [`005-metaspace-architecture-permgen.md`](curriculum/phase-08-jvm-internals-memory-management/005-metaspace-architecture-permgen.md) | `NOT_STARTED` |
| `P08-C006` | Interpreter vs JIT Compilation: Tiered Compilation, C1, C2, and On-Stack Replacement | [`006-jit-compiler-tiered-compilation.md`](curriculum/phase-08-jvm-internals-memory-management/006-jit-compiler-tiered-compilation.md) | `NOT_STARTED` |
| `P08-C007` | Object Reachability, GC Roots, and Reference Types (Soft, Weak, Phantom) | [`007-gc-roots-and-reference-types.md`](curriculum/phase-08-jvm-internals-memory-management/007-gc-roots-and-reference-types.md) | `NOT_STARTED` |
| `P08-C008` | Garbage Collection Fundamentals: Mark-Sweep, Compact, Generational Hypothesis | [`008-garbage-collection-fundamentals.md`](curriculum/phase-08-jvm-internals-memory-management/008-garbage-collection-fundamentals.md) | `NOT_STARTED` |
| `P08-C009` | Modern Garbage Collectors: G1GC, ZGC, Parallel GC, and Tuning Baselines | [`009-modern-collectors-g1-zgc.md`](curriculum/phase-08-jvm-internals-memory-management/009-modern-collectors-g1-zgc.md) | `NOT_STARTED` |
| `P08-C010` | Memory Leaks in Java: Static References, Unclosed Resources, and ThreadLocals | [`010-java-memory-leaks-patterns.md`](curriculum/phase-08-jvm-internals-memory-management/010-java-memory-leaks-patterns.md) | `NOT_STARTED` |
| `P08-C011` | Diagnosing OutOfMemoryError and StackOverflowError: Heap Dumps and Analysis | [`011-oom-and-stack-overflow-diagnosis.md`](curriculum/phase-08-jvm-internals-memory-management/011-oom-and-stack-overflow-diagnosis.md) | `NOT_STARTED` |
| `P08-C012` | JVM Diagnostic CLI Tooling: jcmd, jstack, jmap, and Java Flight Recorder (JFR) | [`012-jvm-cli-diagnostics-jfr.md`](curriculum/phase-08-jvm-internals-memory-management/012-jvm-cli-diagnostics-jfr.md) | `NOT_STARTED` |

### [Phase 09: Data Structures and Algorithms for Backend Developers](curriculum/phase-09-data-structures-algorithms-backend/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P09-C001` | Algorithmic Complexity: Big-O, Big-Omega, Big-Theta, and Amortized Analysis | [`001-big-o-complexity-amortized.md`](curriculum/phase-09-data-structures-algorithms-backend/001-big-o-complexity-amortized.md) | `NOT_STARTED` |
| `P09-C002` | Dynamic Arrays and Resizing Mechanics: Capacity vs Size and Growth Factors | [`002-dynamic-arrays-resizing.md`](curriculum/phase-09-data-structures-algorithms-backend/002-dynamic-arrays-resizing.md) | `NOT_STARTED` |
| `P09-C003` | Linked Lists: Singly, Doubly, Circular Lists, and Pointer Manipulation | [`003-linked-lists-implementation.md`](curriculum/phase-09-data-structures-algorithms-backend/003-linked-lists-implementation.md) | `NOT_STARTED` |
| `P09-C004` | Stacks and Queues: LIFO, FIFO, Monotonic Stacks, and Array-Backed Buffers | [`004-stacks-and-queues.md`](curriculum/phase-09-data-structures-algorithms-backend/004-stacks-and-queues.md) | `NOT_STARTED` |
| `P09-C005` | Hash Tables: Hash Functions, Bucket Arrays, Open Addressing vs Chaining | [`005-hash-tables-collision-resolution.md`](curriculum/phase-09-data-structures-algorithms-backend/005-hash-tables-collision-resolution.md) | `NOT_STARTED` |
| `P09-C006` | Binary Trees and Binary Search Trees (BST): Insertion, Deletion, and Balancing | [`006-binary-search-trees.md`](curriculum/phase-09-data-structures-algorithms-backend/006-binary-search-trees.md) | `NOT_STARTED` |
| `P09-C007` | Heaps and Priority Queues: Binary Heaps, Sift-Up/Sift-Down, and Top-K | [`007-heaps-and-priority-queues.md`](curriculum/phase-09-data-structures-algorithms-backend/007-heaps-and-priority-queues.md) | `NOT_STARTED` |
| `P09-C008` | Graph Representations: Adjacency Matrix vs Adjacency List and Edge Lists | [`008-graph-representations.md`](curriculum/phase-09-data-structures-algorithms-backend/008-graph-representations.md) | `NOT_STARTED` |
| `P09-C009` | Linear Search vs Binary Search and Bisect Techniques | [`009-binary-search-techniques.md`](curriculum/phase-09-data-structures-algorithms-backend/009-binary-search-techniques.md) | `NOT_STARTED` |
| `P09-C010` | Comparison Sorts: QuickSort, MergeSort, TimSort, and Space-Time Trade-offs | [`010-comparison-sorting-algorithms.md`](curriculum/phase-09-data-structures-algorithms-backend/010-comparison-sorting-algorithms.md) | `NOT_STARTED` |
| `P09-C011` | Algorithm Patterns: Two Pointers and Sliding Window for Stream Processing | [`011-two-pointers-sliding-window.md`](curriculum/phase-09-data-structures-algorithms-backend/011-two-pointers-sliding-window.md) | `NOT_STARTED` |
| `P09-C012` | Graph Algorithms: Breadth-First Search (BFS) and Depth-First Search (DFS) | [`012-bfs-dfs-graph-traversal.md`](curriculum/phase-09-data-structures-algorithms-backend/012-bfs-dfs-graph-traversal.md) | `NOT_STARTED` |
| `P09-C013` | Dynamic Programming Fundamentals: Memoization, Tabulation, and Subproblems | [`013-dynamic-programming-fundamentals.md`](curriculum/phase-09-data-structures-algorithms-backend/013-dynamic-programming-fundamentals.md) | `NOT_STARTED` |

### [Phase 10: Professional Java Development Workflow](curriculum/phase-10-professional-java-development-workflow/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P10-C001` | Maven Build Lifecycle: clean, compile, test, package, install, and deploy | [`001-maven-build-lifecycle.md`](curriculum/phase-10-professional-java-development-workflow/001-maven-build-lifecycle.md) | `NOT_STARTED` |
| `P10-C002` | Maven Dependency Management: Coordinates, Scopes (compile, test, provided, runtime) | [`002-maven-dependency-scopes.md`](curriculum/phase-10-professional-java-development-workflow/002-maven-dependency-scopes.md) | `NOT_STARTED` |
| `P10-C003` | Transitive Dependencies, Conflict Resolution (Nearest Wins), and Exclusions | [`003-transitive-dependencies-conflicts.md`](curriculum/phase-10-professional-java-development-workflow/003-transitive-dependencies-conflicts.md) | `NOT_STARTED` |
| `P10-C004` | Maven Plugins: Compiler, Surefire, Failsafe, Jar, and Shade Plugins | [`004-maven-plugins-and-surefire.md`](curriculum/phase-10-professional-java-development-workflow/004-maven-plugins-and-surefire.md) | `NOT_STARTED` |
| `P10-C005` | Maven Wrapper (mvnw) for Deterministic Environment Builds | [`005-maven-wrapper-reproducible-builds.md`](curriculum/phase-10-professional-java-development-workflow/005-maven-wrapper-reproducible-builds.md) | `NOT_STARTED` |
| `P10-C006` | Structured Logging with SLF4J and Logback: Appenders, Encoders, and Layouts | [`006-slf4j-and-logback-architecture.md`](curriculum/phase-10-professional-java-development-workflow/006-slf4j-and-logback-architecture.md) | `NOT_STARTED` |
| `P10-C007` | Logging Best Practices: Log Levels, Parameterized Logging, and MDC Context | [`007-logging-best-practices-mdc.md`](curriculum/phase-10-professional-java-development-workflow/007-logging-best-practices-mdc.md) | `NOT_STARTED` |
| `P10-C008` | Preventing Sensitive Data (PII/Tokens) Leaks in Log Streams | [`008-preventing-pii-log-leaks.md`](curriculum/phase-10-professional-java-development-workflow/008-preventing-pii-log-leaks.md) | `NOT_STARTED` |
| `P10-C009` | Advanced Debugging Techniques: Conditional Breakpoints, Watchpoints, and Thread Dumps | [`009-ide-debugging-techniques.md`](curriculum/phase-10-professional-java-development-workflow/009-ide-debugging-techniques.md) | `NOT_STARTED` |
| `P10-C010` | Static Code Analysis: Checkstyle, SpotBugs, and Error Prone Integration | [`010-static-analysis-spotbugs.md`](curriculum/phase-10-professional-java-development-workflow/010-static-analysis-spotbugs.md) | `NOT_STARTED` |
| `P10-C011` | Professional Git Workflows: Semantic Commits, Branching, and Hygiene | [`011-git-workflows-and-hygiene.md`](curriculum/phase-10-professional-java-development-workflow/011-git-workflows-and-hygiene.md) | `NOT_STARTED` |

### [Phase 11: Unit Testing and Software Quality](curriculum/phase-11-unit-testing-software-quality/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P11-C001` | Testing Pyramid: Unit, Component, Integration, Contract, and E2E Tests | [`001-testing-pyramid-and-scopes.md`](curriculum/phase-11-unit-testing-software-quality/001-testing-pyramid-and-scopes.md) | `NOT_STARTED` |
| `P11-C002` | JUnit 5 Architecture: Jupiter, Vintage, and Platform | [`002-junit-5-architecture.md`](curriculum/phase-11-unit-testing-software-quality/002-junit-5-architecture.md) | `NOT_STARTED` |
| `P11-C003` | JUnit 5 Lifecycle: @Test, @BeforeEach, @AfterEach, @BeforeAll, @AfterAll | [`003-junit-5-lifecycle-annotations.md`](curriculum/phase-11-unit-testing-software-quality/003-junit-5-lifecycle-annotations.md) | `NOT_STARTED` |
| `P11-C004` | Fluent Assertions with AssertJ: Collections, Exceptions, and Custom Assertions | [`004-assertj-fluent-assertions.md`](curriculum/phase-11-unit-testing-software-quality/004-assertj-fluent-assertions.md) | `NOT_STARTED` |
| `P11-C005` | Parameterized Tests in JUnit 5: @ValueSource, @CsvSource, @MethodSource | [`005-parameterized-tests.md`](curriculum/phase-11-unit-testing-software-quality/005-parameterized-tests.md) | `NOT_STARTED` |
| `P11-C006` | Testing Exceptions and Boundary Conditions Defensively | [`006-testing-exceptions-and-boundaries.md`](curriculum/phase-11-unit-testing-software-quality/006-testing-exceptions-and-boundaries.md) | `NOT_STARTED` |
| `P11-C007` | Test Isolation, Determinism, and Flaky Test Prevention | [`007-test-isolation-and-flakiness.md`](curriculum/phase-11-unit-testing-software-quality/007-test-isolation-and-flakiness.md) | `NOT_STARTED` |
| `P11-C008` | Mockito Fundamentals: Mocks, Spies, Stubs, and when()/thenReturn() | [`008-mockito-fundamentals.md`](curriculum/phase-11-unit-testing-software-quality/008-mockito-fundamentals.md) | `NOT_STARTED` |
| `P11-C009` | Argument Matching, Verification, and ArgumentCaptor Mechanics | [`009-argument-matching-and-verification.md`](curriculum/phase-11-unit-testing-software-quality/009-argument-matching-and-verification.md) | `NOT_STARTED` |
| `P11-C010` | Over-Mocking Anti-Pattern: What to Mock and What NOT to Mock | [`010-over-mocking-anti-patterns.md`](curriculum/phase-11-unit-testing-software-quality/010-over-mocking-anti-patterns.md) | `NOT_STARTED` |
| `P11-C011` | Integration Testing with Real Databases: Testcontainers Fundamentals | [`011-testcontainers-fundamentals.md`](curriculum/phase-11-unit-testing-software-quality/011-testcontainers-fundamentals.md) | `NOT_STARTED` |
| `P11-C012` | Code Coverage vs Mutation Testing: Jacoco, PIT, and Assertion Quality | [`012-code-coverage-and-mutation-testing.md`](curriculum/phase-11-unit-testing-software-quality/012-code-coverage-and-mutation-testing.md) | `NOT_STARTED` |

### [Phase 12: Concurrency and Multithreading](curriculum/phase-12-concurrency-multithreading/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P12-C001` | Processes vs Threads, OS Scheduling, and Java Thread Lifecycle | [`001-processes-vs-threads-lifecycle.md`](curriculum/phase-12-concurrency-multithreading/001-processes-vs-threads-lifecycle.md) | `NOT_STARTED` |
| `P12-C002` | Thread Creation: Thread, Runnable, Callable, and start() vs run() | [`002-thread-creation-callable.md`](curriculum/phase-12-concurrency-multithreading/002-thread-creation-callable.md) | `NOT_STARTED` |
| `P12-C003` | Thread Interruption Mechanics, Cooperative Cancellation, and Clean Shutdown | [`003-thread-interruption-mechanics.md`](curriculum/phase-12-concurrency-multithreading/003-thread-interruption-mechanics.md) | `NOT_STARTED` |
| `P12-C004` | Race Conditions, Critical Sections, and Shared Mutable State Hazards | [`004-race-conditions-and-critical-sections.md`](curriculum/phase-12-concurrency-multithreading/004-race-conditions-and-critical-sections.md) | `NOT_STARTED` |
| `P12-C005` | Java Memory Model (JMM): Visibility, Ordering, Atomicity, and Happens-Before | [`005-jmm-happens-before-contract.md`](curriculum/phase-12-concurrency-multithreading/005-jmm-happens-before-contract.md) | `NOT_STARTED` |
| `P12-C006` | The volatile Keyword: Visibility, Instruction Reordering, and Limits | [`006-volatile-keyword-mechanics.md`](curriculum/phase-12-concurrency-multithreading/006-volatile-keyword-mechanics.md) | `NOT_STARTED` |
| `P12-C007` | Intrinsic Locks and synchronized Blocks/Methods: Reentrancy and Monitors | [`007-synchronized-and-intrinsic-locks.md`](curriculum/phase-12-concurrency-multithreading/007-synchronized-and-intrinsic-locks.md) | `NOT_STARTED` |
| `P12-C008` | Explicit Locks: ReentrantLock, Condition Variables, and ReadWriteLock | [`008-reentrantlock-and-conditions.md`](curriculum/phase-12-concurrency-multithreading/008-reentrantlock-and-conditions.md) | `NOT_STARTED` |
| `P12-C009` | Non-Blocking Synchronization: Atomic Variables, CAS, and ABA Problem | [`009-atomic-variables-and-cas.md`](curriculum/phase-12-concurrency-multithreading/009-atomic-variables-and-cas.md) | `NOT_STARTED` |
| `P12-C010` | Liveness Failures: Deadlocks, Livelocks, Starvation, and Lock Ordering | [`010-deadlocks-and-liveness-failures.md`](curriculum/phase-12-concurrency-multithreading/010-deadlocks-and-liveness-failures.md) | `NOT_STARTED` |
| `P12-C011` | Executor Framework: ExecutorService, ThreadPoolExecutor, and Sizing Rules | [`011-executorservice-thread-pools.md`](curriculum/phase-12-concurrency-multithreading/011-executorservice-thread-pools.md) | `NOT_STARTED` |
| `P12-C012` | Asynchronous Programming with CompletableFuture: Chaining and Error Handling | [`012-completablefuture-async-pipelines.md`](curriculum/phase-12-concurrency-multithreading/012-completablefuture-async-pipelines.md) | `NOT_STARTED` |
| `P12-C013` | Concurrent Synchronization Helpers: CountDownLatch, CyclicBarrier, Semaphore | [`013-synchronization-helpers-latches.md`](curriculum/phase-12-concurrency-multithreading/013-synchronization-helpers-latches.md) | `NOT_STARTED` |
| `P12-C014` | Producer-Consumer Pattern with BlockingQueue Implementations | [`014-producer-consumer-blockingqueue.md`](curriculum/phase-12-concurrency-multithreading/014-producer-consumer-blockingqueue.md) | `NOT_STARTED` |
| `P12-C015` | Virtual Threads (Project Loom): Platform Threads vs Virtual Threads Architecture | [`015-virtual-threads-loom-architecture.md`](curriculum/phase-12-concurrency-multithreading/015-virtual-threads-loom-architecture.md) | `NOT_STARTED` |
| `P12-C016` | Virtual Thread Pinning (synchronized vs ReentrantLock) and Tuning Rules | [`016-virtual-thread-pinning-limits.md`](curriculum/phase-12-concurrency-multithreading/016-virtual-thread-pinning-limits.md) | `NOT_STARTED` |
| `P12-C017` | ThreadLocal State, Memory Leaks in Pools, and Scoped Values | [`017-threadlocal-and-scoped-values.md`](curriculum/phase-12-concurrency-multithreading/017-threadlocal-and-scoped-values.md) | `NOT_STARTED` |

### [Phase 13: SQL and Relational Database Fundamentals](curriculum/phase-13-sql-relational-database-fundamentals/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P13-C001` | Relational Database Concepts: Schemas, Tables, Columns, Rows, and Data Types | [`001-relational-database-concepts.md`](curriculum/phase-13-sql-relational-database-fundamentals/001-relational-database-concepts.md) | `NOT_STARTED` |
| `P13-C002` | Keys and Constraints: Primary, Foreign, Unique, Check, and Referential Integrity | [`002-keys-and-constraints.md`](curriculum/phase-13-sql-relational-database-fundamentals/002-keys-and-constraints.md) | `NOT_STARTED` |
| `P13-C003` | Database Normalization (1NF, 2NF, 3NF, BCNF) vs Controlled Denormalization | [`003-normalization-and-denormalization.md`](curriculum/phase-13-sql-relational-database-fundamentals/003-normalization-and-denormalization.md) | `NOT_STARTED` |
| `P13-C004` | CRUD Operations: SELECT, INSERT, UPDATE, DELETE with Filtering and Sorting | [`004-sql-crud-operations.md`](curriculum/phase-13-sql-relational-database-fundamentals/004-sql-crud-operations.md) | `NOT_STARTED` |
| `P13-C005` | Relational Joins: INNER, LEFT, RIGHT, FULL OUTER, CROSS, and Self Joins | [`005-relational-joins-mechanics.md`](curriculum/phase-13-sql-relational-database-fundamentals/005-relational-joins-mechanics.md) | `NOT_STARTED` |
| `P13-C006` | Aggregations, GROUP BY, HAVING, and Three-Valued NULL Logic Traps | [`006-aggregations-and-null-logic.md`](curriculum/phase-13-sql-relational-database-fundamentals/006-aggregations-and-null-logic.md) | `NOT_STARTED` |
| `P13-C007` | Common Table Expressions (CTEs), Recursive Queries, and Subqueries | [`007-ctes-and-subqueries.md`](curriculum/phase-13-sql-relational-database-fundamentals/007-ctes-and-subqueries.md) | `NOT_STARTED` |
| `P13-C008` | Window Functions: ROW_NUMBER, RANK, DENSE_RANK, and Partitioning | [`008-window-functions-analytics.md`](curriculum/phase-13-sql-relational-database-fundamentals/008-window-functions-analytics.md) | `NOT_STARTED` |
| `P13-C009` | PostgreSQL Indexing: B-Tree Internals, Composite Indexes, and Leftmost Prefix | [`009-postgres-indexing-btree.md`](curriculum/phase-13-sql-relational-database-fundamentals/009-postgres-indexing-btree.md) | `NOT_STARTED` |
| `P13-C010` | Specialized Indexes: Partial, Covering (INCLUDE), and GIN Indexes | [`010-specialized-indexes-covering.md`](curriculum/phase-13-sql-relational-database-fundamentals/010-specialized-indexes-covering.md) | `NOT_STARTED` |
| `P13-C011` | Query Plan Analysis: EXPLAIN, EXPLAIN ANALYZE, Scans vs Index Seeks | [`011-query-plans-explain-analyze.md`](curriculum/phase-13-sql-relational-database-fundamentals/011-query-plans-explain-analyze.md) | `NOT_STARTED` |
| `P13-C012` | ACID Properties: Atomicity, Consistency, Isolation, and Durability | [`012-acid-properties-in-depth.md`](curriculum/phase-13-sql-relational-database-fundamentals/012-acid-properties-in-depth.md) | `NOT_STARTED` |
| `P13-C013` | Transaction Isolation Levels: Read Uncommitted, Read Committed, Repeatable Read, Serializable | [`013-transaction-isolation-levels.md`](curriculum/phase-13-sql-relational-database-fundamentals/013-transaction-isolation-levels.md) | `NOT_STARTED` |
| `P13-C014` | Concurrency Anomalies: Dirty Reads, Non-Repeatable Reads, Phantom Reads, Lost Updates | [`014-concurrency-anomalies.md`](curriculum/phase-13-sql-relational-database-fundamentals/014-concurrency-anomalies.md) | `NOT_STARTED` |
| `P13-C015` | PostgreSQL MVCC (Multi-Version Concurrency Control) and VACUUM Mechanics | [`015-postgres-mvcc-and-vacuum.md`](curriculum/phase-13-sql-relational-database-fundamentals/015-postgres-mvcc-and-vacuum.md) | `NOT_STARTED` |
| `P13-C016` | Database Locking: Shared, Exclusive, Row-Level Locks, and Deadlock Resolution | [`016-database-locking-and-deadlocks.md`](curriculum/phase-13-sql-relational-database-fundamentals/016-database-locking-and-deadlocks.md) | `NOT_STARTED` |

### [Phase 14: JDBC and Java Database Connectivity](curriculum/phase-14-jdbc-database-connectivity/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P14-C001` | JDBC Architecture: Driver Types, DriverManager, and Connection Interfaces | [`001-jdbc-architecture-drivers.md`](curriculum/phase-14-jdbc-database-connectivity/001-jdbc-architecture-drivers.md) | `NOT_STARTED` |
| `P14-C002` | Database Connection Configuration, URL Schemes, and Connection Lifecycles | [`002-connection-configuration-urls.md`](curriculum/phase-14-jdbc-database-connectivity/002-connection-configuration-urls.md) | `NOT_STARTED` |
| `P14-C003` | Statement vs PreparedStatement vs CallableStatement Internals | [`003-statement-vs-preparedstatement.md`](curriculum/phase-14-jdbc-database-connectivity/003-statement-vs-preparedstatement.md) | `NOT_STARTED` |
| `P14-C004` | SQL Injection Anatomy, Attack Vectors, and Prevention via Parameter Binding | [`004-sql-injection-prevention.md`](curriculum/phase-14-jdbc-database-connectivity/004-sql-injection-prevention.md) | `NOT_STARTED` |
| `P14-C005` | ResultSet Traversal, Cursor Positioning, and Data Type Mapping | [`005-resultset-mapping-types.md`](curriculum/phase-14-jdbc-database-connectivity/005-resultset-mapping-types.md) | `NOT_STARTED` |
| `P14-C006` | Manual JDBC Transaction Management: AutoCommit, Commit, Rollback, and Savepoints | [`006-jdbc-transaction-boundaries.md`](curriculum/phase-14-jdbc-database-connectivity/006-jdbc-transaction-boundaries.md) | `NOT_STARTED` |
| `P14-C007` | JDBC Batch Processing: addBatch, executeBatch, and Tuning Insertion Speeds | [`007-jdbc-batch-processing.md`](curriculum/phase-14-jdbc-database-connectivity/007-jdbc-batch-processing.md) | `NOT_STARTED` |
| `P14-C008` | SQLException Hierarchy, SQLState Codes, and Error Translation | [`008-sqlexception-and-error-codes.md`](curriculum/phase-14-jdbc-database-connectivity/008-sqlexception-and-error-codes.md) | `NOT_STARTED` |
| `P14-C009` | Deterministic Resource Cleanup in JDBC: Nested Try-With-Resources | [`009-jdbc-resource-cleanup.md`](curriculum/phase-14-jdbc-database-connectivity/009-jdbc-resource-cleanup.md) | `NOT_STARTED` |
| `P14-C010` | Connection Pooling Fundamentals: Connection Handshake Cost and Leaks | [`010-connection-pooling-fundamentals.md`](curriculum/phase-14-jdbc-database-connectivity/010-connection-pooling-fundamentals.md) | `NOT_STARTED` |
| `P14-C011` | HikariCP Architecture, Sizing Formulas, Timeout Configurations, and Metrics | [`011-hikaricp-tuning-and-metrics.md`](curriculum/phase-14-jdbc-database-connectivity/011-hikaricp-tuning-and-metrics.md) | `NOT_STARTED` |
| `P14-C012` | Data Access Object (DAO) and Repository Patterns with Raw JDBC | [`012-dao-repository-pattern-jdbc.md`](curriculum/phase-14-jdbc-database-connectivity/012-dao-repository-pattern-jdbc.md) | `NOT_STARTED` |

### [Phase 15: HTTP, Networking, and REST API Fundamentals](curriculum/phase-15-http-networking-rest-api-fundamentals/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P15-C001` | Networking Fundamentals: TCP/IP Stack, Sockets, Ports, and Handshakes | [`001-tcp-ip-and-socket-basics.md`](curriculum/phase-15-http-networking-rest-api-fundamentals/001-tcp-ip-and-socket-basics.md) | `NOT_STARTED` |
| `P15-C002` | TLS/HTTPS Handshake, Certificates, Encryption in Transit, and Cipher Suites | [`002-tls-https-handshake.md`](curriculum/phase-15-http-networking-rest-api-fundamentals/002-tls-https-handshake.md) | `NOT_STARTED` |
| `P15-C003` | HTTP Protocol Anatomy: Request Line, Headers, Body, and Chunked Transfer | [`003-http-protocol-anatomy.md`](curriculum/phase-15-http-networking-rest-api-fundamentals/003-http-protocol-anatomy.md) | `NOT_STARTED` |
| `P15-C004` | HTTP Methods: GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS Semantics | [`004-http-methods-semantics.md`](curriculum/phase-15-http-networking-rest-api-fundamentals/004-http-methods-semantics.md) | `NOT_STARTED` |
| `P15-C005` | Safety and Idempotency in HTTP Methods and API Contracts | [`005-safety-and-idempotency.md`](curriculum/phase-15-http-networking-rest-api-fundamentals/005-safety-and-idempotency.md) | `NOT_STARTED` |
| `P15-C006` | HTTP Status Codes: 2xx Success, 3xx Redirect, 4xx Client Error, 5xx Server Error | [`006-http-status-codes.md`](curriculum/phase-15-http-networking-rest-api-fundamentals/006-http-status-codes.md) | `NOT_STARTED` |
| `P15-C007` | HTTP Headers: Content-Type, Accept, Caching (ETag, Cache-Control), and Authorization | [`007-http-headers-and-caching.md`](curriculum/phase-15-http-networking-rest-api-fundamentals/007-http-headers-and-caching.md) | `NOT_STARTED` |
| `P15-C008` | REST Architectural Constraints and Richardson Maturity Model | [`008-rest-constraints-richardson-model.md`](curriculum/phase-15-http-networking-rest-api-fundamentals/008-rest-constraints-richardson-model.md) | `NOT_STARTED` |
| `P15-C009` | REST Resource URI Design: Nouns vs Verbs, Hierarchies, and Consistency | [`009-rest-uri-design-guidelines.md`](curriculum/phase-15-http-networking-rest-api-fundamentals/009-rest-uri-design-guidelines.md) | `NOT_STARTED` |
| `P15-C010` | API Pagination Patterns: Offset-Based vs Keyset (Cursor-Based) Pagination | [`010-api-pagination-offset-vs-keyset.md`](curriculum/phase-15-http-networking-rest-api-fundamentals/010-api-pagination-offset-vs-keyset.md) | `NOT_STARTED` |
| `P15-C011` | Filtering, Sorting, and Standard Error Envelopes (RFC 7807 Problem Details) | [`011-filtering-sorting-rfc7807.md`](curriculum/phase-15-http-networking-rest-api-fundamentals/011-filtering-sorting-rfc7807.md) | `NOT_STARTED` |
| `P15-C012` | API Versioning Strategies: URI Path, Header, and Content Negotiation | [`012-api-versioning-strategies.md`](curriculum/phase-15-http-networking-rest-api-fundamentals/012-api-versioning-strategies.md) | `NOT_STARTED` |
| `P15-C013` | Modern Java HttpClient (java.net.http): Synchronous, Asynchronous, and Virtual Threads | [`013-java-http-client-api.md`](curriculum/phase-15-http-networking-rest-api-fundamentals/013-java-http-client-api.md) | `NOT_STARTED` |
| `P15-C014` | Connection Timeouts, Read Timeouts, Retries, and Circuit Breaker Baselines | [`014-timeouts-retries-circuit-breakers.md`](curriculum/phase-15-http-networking-rest-api-fundamentals/014-timeouts-retries-circuit-breakers.md) | `NOT_STARTED` |
| `P15-C015` | OpenAPI (Swagger) Specifications: Contract-First vs Code-First API Design | [`015-openapi-contract-design.md`](curriculum/phase-15-http-networking-rest-api-fundamentals/015-openapi-contract-design.md) | `NOT_STARTED` |

### [Phase 16: Clean Code and Software Design Principles](curriculum/phase-16-clean-code-software-design-principles/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P16-C001` | Single Responsibility Principle (SRP): Cohesion, Actors, and Reasons to Change | [`001-single-responsibility-principle.md`](curriculum/phase-16-clean-code-software-design-principles/001-single-responsibility-principle.md) | `NOT_STARTED` |
| `P16-C002` | Open/Closed Principle (OCP): Extension via Interfaces and Polymorphism | [`002-open-closed-principle.md`](curriculum/phase-16-clean-code-software-design-principles/002-open-closed-principle.md) | `NOT_STARTED` |
| `P16-C003` | Liskov Substitution Principle (LSP): Subtyping Contracts and Behavioral Invariants | [`003-liskov-substitution-principle.md`](curriculum/phase-16-clean-code-software-design-principles/003-liskov-substitution-principle.md) | `NOT_STARTED` |
| `P16-C004` | Interface Segregation Principle (ISP): Lean Contracts and Role Interfaces | [`004-interface-segregation-principle.md`](curriculum/phase-16-clean-code-software-design-principles/004-interface-segregation-principle.md) | `NOT_STARTED` |
| `P16-C005` | Dependency Inversion Principle (DIP): Inverting Control Across Boundaries | [`005-dependency-inversion-principle.md`](curriculum/phase-16-clean-code-software-design-principles/005-dependency-inversion-principle.md) | `NOT_STARTED` |
| `P16-C006` | Creational Patterns: Factory Method, Abstract Factory, and Builder Pattern | [`006-creational-patterns-builder-factory.md`](curriculum/phase-16-clean-code-software-design-principles/006-creational-patterns-builder-factory.md) | `NOT_STARTED` |
| `P16-C007` | Structural Patterns: Adapter, Decorator, Proxy, and Facade Patterns | [`007-structural-patterns-decorator-adapter.md`](curriculum/phase-16-clean-code-software-design-principles/007-structural-patterns-decorator-adapter.md) | `NOT_STARTED` |
| `P16-C008` | Behavioral Patterns: Strategy, Observer, Template Method, and Command | [`008-behavioral-patterns-strategy-observer.md`](curriculum/phase-16-clean-code-software-design-principles/008-behavioral-patterns-strategy-observer.md) | `NOT_STARTED` |
| `P16-C009` | State Pattern: Modeling Complex Domain State Transitions Safely | [`009-state-pattern-state-machines.md`](curriculum/phase-16-clean-code-software-design-principles/009-state-pattern-state-machines.md) | `NOT_STARTED` |
| `P16-C010` | Recognizing Overengineering, Speculative Generality, and Clean Code Smells | [`010-overengineering-code-smells.md`](curriculum/phase-16-clean-code-software-design-principles/010-overengineering-code-smells.md) | `NOT_STARTED` |

### [Phase 17: Spring Framework Fundamentals](curriculum/phase-17-spring-framework-fundamentals/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P17-C001` | Inversion of Control (IoC) and Dependency Injection (DI) Foundations | [`001-ioc-and-di-foundations.md`](curriculum/phase-17-spring-framework-fundamentals/001-ioc-and-di-foundations.md) | `NOT_STARTED` |
| `P17-C002` | Spring Bean Container: BeanFactory vs ApplicationContext Architectures | [`002-beanfactory-vs-applicationcontext.md`](curriculum/phase-17-spring-framework-fundamentals/002-beanfactory-vs-applicationcontext.md) | `NOT_STARTED` |
| `P17-C003` | Stereotype Annotations: @Component, @Service, @Repository, @Controller | [`003-stereotype-annotations.md`](curriculum/phase-17-spring-framework-fundamentals/003-stereotype-annotations.md) | `NOT_STARTED` |
| `P17-C004` | Dependency Injection Strategies: Constructor vs Setter vs Field Injection (Why Field Injection is Bad) | [`004-dependency-injection-strategies.md`](curriculum/phase-17-spring-framework-fundamentals/004-dependency-injection-strategies.md) | `NOT_STARTED` |
| `P17-C005` | Java Configuration: @Configuration, @Bean, and Conditional Bean Registration | [`005-java-configuration-and-conditional-beans.md`](curriculum/phase-17-spring-framework-fundamentals/005-java-configuration-and-conditional-beans.md) | `NOT_STARTED` |
| `P17-C006` | Spring Bean Lifecycle: BeanPostProcessors, InitializingBean, and @PostConstruct | [`006-spring-bean-lifecycle.md`](curriculum/phase-17-spring-framework-fundamentals/006-spring-bean-lifecycle.md) | `NOT_STARTED` |
| `P17-C007` | Spring Bean Scopes: Singleton, Prototype, Request, Session, and Thread Safety | [`007-spring-bean-scopes.md`](curriculum/phase-17-spring-framework-fundamentals/007-spring-bean-scopes.md) | `NOT_STARTED` |
| `P17-C008` | Configuration Properties, @Value, Profiles, and Environment Abstraction | [`008-configuration-properties-profiles.md`](curriculum/phase-17-spring-framework-fundamentals/008-configuration-properties-profiles.md) | `NOT_STARTED` |
| `P17-C009` | Spring AOP Fundamentals: Aspects, Pointcuts, JoinPoints, and Advices | [`009-spring-aop-fundamentals.md`](curriculum/phase-17-spring-framework-fundamentals/009-spring-aop-fundamentals.md) | `NOT_STARTED` |
| `P17-C010` | JDK Dynamic Proxies vs CGLIB Proxies and the Self-Invocation Trap | [`010-proxy-mechanics-self-invocation.md`](curriculum/phase-17-spring-framework-fundamentals/010-proxy-mechanics-self-invocation.md) | `NOT_STARTED` |
| `P17-C011` | Spring Framework vs Spring Boot: Auto-Configuration and Opinionated Defaults | [`011-spring-framework-vs-spring-boot.md`](curriculum/phase-17-spring-framework-fundamentals/011-spring-framework-vs-spring-boot.md) | `NOT_STARTED` |

### [Phase 18: Spring Boot and REST API Development](curriculum/phase-18-spring-boot-rest-api-development/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P18-C001` | Spring Boot Architecture: Auto-Configuration, Starters, and @SpringBootApplication | [`001-spring-boot-auto-configuration.md`](curriculum/phase-18-spring-boot-rest-api-development/001-spring-boot-auto-configuration.md) | `NOT_STARTED` |
| `P18-C002` | REST Controllers: @RestController, Request Mappings, Path Variables, and Parameters | [`002-rest-controllers-mappings.md`](curriculum/phase-18-spring-boot-rest-api-development/002-rest-controllers-mappings.md) | `NOT_STARTED` |
| `P18-C003` | DTO Pattern: Request/Response Separation, Entity Isolation, and Mapping Strategies | [`003-dto-pattern-entity-isolation.md`](curriculum/phase-18-spring-boot-rest-api-development/003-dto-pattern-entity-isolation.md) | `NOT_STARTED` |
| `P18-C004` | Bean Validation (Jakarta Validation): Constraints, Groups, and Custom Validators | [`004-bean-validation-jakarta.md`](curriculum/phase-18-spring-boot-rest-api-development/004-bean-validation-jakarta.md) | `NOT_STARTED` |
| `P18-C005` | Centralized Exception Handling: @ExceptionHandler, @RestControllerAdvice, RFC 7807 | [`005-centralized-exception-handling.md`](curriculum/phase-18-spring-boot-rest-api-development/005-centralized-exception-handling.md) | `NOT_STARTED` |
| `P18-C006` | Spring Data JPA Architecture: Repositories, JpaRepository, and Query Derivation | [`006-spring-data-jpa-repositories.md`](curriculum/phase-18-spring-boot-rest-api-development/006-spring-data-jpa-repositories.md) | `NOT_STARTED` |
| `P18-C007` | Hibernate & JPA Fundamentals: Persistence Context, Entity Lifecycle, and Dirty Checking | [`007-jpa-entity-lifecycle-dirty-checking.md`](curriculum/phase-18-spring-boot-rest-api-development/007-jpa-entity-lifecycle-dirty-checking.md) | `NOT_STARTED` |
| `P18-C008` | Entity Relationships: @OneToOne, @OneToMany, @ManyToOne, @ManyToMany and Ownership | [`008-entity-relationships-ownership.md`](curriculum/phase-18-spring-boot-rest-api-development/008-entity-relationships-ownership.md) | `NOT_STARTED` |
| `P18-C009` | Fetch Types (LAZY vs EAGER) and the Infamous N+1 Query Problem | [`009-fetch-types-and-n-plus-one.md`](curriculum/phase-18-spring-boot-rest-api-development/009-fetch-types-and-n-plus-one.md) | `NOT_STARTED` |
| `P18-C010` | Eliminating N+1 Queries: JOIN FETCH, @EntityGraph, and DTO Projections | [`010-eliminating-n-plus-one-queries.md`](curriculum/phase-18-spring-boot-rest-api-development/010-eliminating-n-plus-one-queries.md) | `NOT_STARTED` |
| `P18-C011` | Declarative Transactions: @Transactional, Propagation Rules, and Rollback Hazards | [`011-transactional-propagation-rollback.md`](curriculum/phase-18-spring-boot-rest-api-development/011-transactional-propagation-rollback.md) | `NOT_STARTED` |
| `P18-C012` | Pagination and Sorting in Spring Data JPA: Pageable, Slice, and Page Performance | [`012-pagination-and-sorting-jpa.md`](curriculum/phase-18-spring-boot-rest-api-development/012-pagination-and-sorting-jpa.md) | `NOT_STARTED` |
| `P18-C013` | Database Schema Migrations with Flyway: Versioned Scripts and Rollback Hygiene | [`013-flyway-schema-migrations.md`](curriculum/phase-18-spring-boot-rest-api-development/013-flyway-schema-migrations.md) | `NOT_STARTED` |
| `P18-C014` | OpenAPI Documentation Integration with SpringDoc and Swagger UI | [`014-springdoc-openapi-integration.md`](curriculum/phase-18-spring-boot-rest-api-development/014-springdoc-openapi-integration.md) | `NOT_STARTED` |
| `P18-C015` | Testing Spring Boot APIs: @WebMvcTest, @DataJpaTest, @SpringBootTest, and MockMvc | [`015-spring-boot-testing-slices.md`](curriculum/phase-18-spring-boot-rest-api-development/015-spring-boot-testing-slices.md) | `NOT_STARTED` |

### [Phase 19: Application Security](curriculum/phase-19-application-security/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P19-C001` | Authentication vs Authorization and Identity & Access Management (IAM) Principles | [`001-authentication-vs-authorization.md`](curriculum/phase-19-application-security/001-authentication-vs-authorization.md) | `NOT_STARTED` |
| `P19-C002` | Cryptographic Password Hashing: Salts, Work Factors, BCrypt, and Argon2 | [`002-password-hashing-bcrypt-argon2.md`](curriculum/phase-19-application-security/002-password-hashing-bcrypt-argon2.md) | `NOT_STARTED` |
| `P19-C003` | Spring Security Architecture: SecurityFilterChain, AuthenticationManager, and Context | [`003-spring-security-filter-chain.md`](curriculum/phase-19-application-security/003-spring-security-filter-chain.md) | `NOT_STARTED` |
| `P19-C004` | Role-Based Access Control (RBAC) vs Attribute-Based Access Control (ABAC) | [`004-rbac-vs-abac-authorization.md`](curriculum/phase-19-application-security/004-rbac-vs-abac-authorization.md) | `NOT_STARTED` |
| `P19-C005` | Method Security: @PreAuthorize, @PostAuthorize, and SpEL Expressions | [`005-method-security-preauthorize.md`](curriculum/phase-19-application-security/005-method-security-preauthorize.md) | `NOT_STARTED` |
| `P19-C006` | JSON Web Tokens (JWT) Architecture: Header, Payload, Signature, and Claims | [`006-jwt-architecture-claims.md`](curriculum/phase-19-application-security/006-jwt-architecture-claims.md) | `NOT_STARTED` |
| `P19-C007` | JWT Security Pitfalls: Weak Keys, 'none' Algorithm, Token Expiry, and Revocation | [`007-jwt-security-pitfalls-revocation.md`](curriculum/phase-19-application-security/007-jwt-security-pitfalls-revocation.md) | `NOT_STARTED` |
| `P19-C008` | OAuth 2.0 and OpenID Connect (OIDC) Protocols: Authorization Code Flow and Scopes | [`008-oauth2-oidc-protocols.md`](curriculum/phase-19-application-security/008-oauth2-oidc-protocols.md) | `NOT_STARTED` |
| `P19-C009` | CORS vs CSRF: Browser Security Mechanisms, SameSite Cookies, and API Security | [`009-cors-vs-csrf-mechanics.md`](curriculum/phase-19-application-security/009-cors-vs-csrf-mechanics.md) | `NOT_STARTED` |
| `P19-C010` | Broken Object-Level Authorization (BOLA/IDOR) and Multi-Tenant Isolation | [`010-bola-idor-tenant-isolation.md`](curriculum/phase-19-application-security/010-bola-idor-tenant-isolation.md) | `NOT_STARTED` |
| `P19-C011` | Security Logging, Audit Trails, Rate Limiting, and Secret Management | [`011-security-logging-rate-limiting.md`](curriculum/phase-19-application-security/011-security-logging-rate-limiting.md) | `NOT_STARTED` |
| `P19-C012` | Dependency Vulnerability Scanning: OWASP Dependency-Check, Snyk, and SBOMs | [`012-dependency-scanning-sboms.md`](curriculum/phase-19-application-security/012-dependency-scanning-sboms.md) | `NOT_STARTED` |

### [Phase 20: Production Backend Engineering](curriculum/phase-20-production-backend-engineering/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P20-C001` | Docker Fundamentals: Images, Layers, Containers, and Daemon Architecture | [`001-docker-fundamentals-images.md`](curriculum/phase-20-production-backend-engineering/001-docker-fundamentals-images.md) | `NOT_STARTED` |
| `P20-C002` | Dockerfile Optimization: Multi-Stage Builds, Layer Caching, and Non-Root Users | [`002-dockerfile-multi-stage-builds.md`](curriculum/phase-20-production-backend-engineering/002-dockerfile-multi-stage-builds.md) | `NOT_STARTED` |
| `P20-C003` | Containerizing Java Runtimes: Memory Limits, cgroups v2, and JVM Ergonomics | [`003-containerizing-java-cgroups.md`](curriculum/phase-20-production-backend-engineering/003-containerizing-java-cgroups.md) | `NOT_STARTED` |
| `P20-C004` | Docker Compose for Local Development Environments | [`004-docker-compose-local-environments.md`](curriculum/phase-20-production-backend-engineering/004-docker-compose-local-environments.md) | `NOT_STARTED` |
| `P20-C005` | Spring Boot Actuator: Health Checks, Liveness, Readiness, and Endpoints Security | [`005-actuator-health-readiness-liveness.md`](curriculum/phase-20-production-backend-engineering/005-actuator-health-readiness-liveness.md) | `NOT_STARTED` |
| `P20-C006` | Application Metrics with Micrometer, Prometheus, and Grafana Dashboards | [`006-micrometer-prometheus-metrics.md`](curriculum/phase-20-production-backend-engineering/006-micrometer-prometheus-metrics.md) | `NOT_STARTED` |
| `P20-C007` | Distributed Tracing: OpenTelemetry, W3C TraceContext, and Trace Correlation | [`007-distributed-tracing-opentelemetry.md`](curriculum/phase-20-production-backend-engineering/007-distributed-tracing-opentelemetry.md) | `NOT_STARTED` |
| `P20-C008` | Graceful Shutdown: Draining Traffic, Active Request Threads, and Termination Signals | [`008-graceful-shutdown-mechanics.md`](curriculum/phase-20-production-backend-engineering/008-graceful-shutdown-mechanics.md) | `NOT_STARTED` |
| `P20-C009` | Resilience Patterns: Circuit Breakers, Retries, Exponential Backoff, and Jitter | [`009-resilience-circuit-breakers-jitter.md`](curriculum/phase-20-production-backend-engineering/009-resilience-circuit-breakers-jitter.md) | `NOT_STARTED` |
| `P20-C010` | Resilience4j Integration in Spring Boot: Bulkheads, Rate Limiters, Timeouts | [`010-resilience4j-spring-boot.md`](curriculum/phase-20-production-backend-engineering/010-resilience4j-spring-boot.md) | `NOT_STARTED` |
| `P20-C011` | Caching Strategies with Redis: Cache-Aside, Write-Through, Invalidation, and TTL | [`011-redis-caching-strategies.md`](curriculum/phase-20-production-backend-engineering/011-redis-caching-strategies.md) | `NOT_STARTED` |
| `P20-C012` | Message Queuing Fundamentals: RabbitMQ vs Apache Kafka Architecture | [`012-messaging-rabbitmq-kafka-basics.md`](curriculum/phase-20-production-backend-engineering/012-messaging-rabbitmq-kafka-basics.md) | `NOT_STARTED` |

### [Phase 21: Advanced Backend Architecture and Distributed Systems](curriculum/phase-21-advanced-backend-architecture-distributed-systems/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P21-C001` | Monoliths, Modular Monoliths, and Microservices: Trade-Off Analysis | [`001-modular-monoliths-vs-microservices.md`](curriculum/phase-21-advanced-backend-architecture-distributed-systems/001-modular-monoliths-vs-microservices.md) | `NOT_STARTED` |
| `P21-C002` | Domain-Driven Design (DDD) Strategic Design: Bounded Contexts and Context Mapping | [`002-ddd-strategic-design-contexts.md`](curriculum/phase-21-advanced-backend-architecture-distributed-systems/002-ddd-strategic-design-contexts.md) | `NOT_STARTED` |
| `P21-C003` | DDD Tactical Design: Entities, Value Objects, Aggregates, and Domain Invariants | [`003-ddd-tactical-design-aggregates.md`](curriculum/phase-21-advanced-backend-architecture-distributed-systems/003-ddd-tactical-design-aggregates.md) | `NOT_STARTED` |
| `P21-C004` | Hexagonal Architecture (Ports and Adapters) & Clean Architecture Implementation | [`004-hexagonal-ports-and-adapters.md`](curriculum/phase-21-advanced-backend-architecture-distributed-systems/004-hexagonal-ports-and-adapters.md) | `NOT_STARTED` |
| `P21-C005` | Distributed Systems Realities: Fallacies of Distributed Computing, CAP, and PACELC | [`005-distributed-systems-fallacies-cap.md`](curriculum/phase-21-advanced-backend-architecture-distributed-systems/005-distributed-systems-fallacies-cap.md) | `NOT_STARTED` |
| `P21-C006` | Event-Driven Architecture: Event Sourcing, Domain Events, and Pub/Sub | [`006-event-driven-architecture-domain-events.md`](curriculum/phase-21-advanced-backend-architecture-distributed-systems/006-event-driven-architecture-domain-events.md) | `NOT_STARTED` |
| `P21-C007` | Dual-Write Problem and the Transactional Outbox Pattern | [`007-dual-write-problem-transactional-outbox.md`](curriculum/phase-21-advanced-backend-architecture-distributed-systems/007-dual-write-problem-transactional-outbox.md) | `NOT_STARTED` |
| `P21-C008` | Idempotent Message Processing: De-duplication Keys and Idempotent Consumers | [`008-idempotent-message-consumers.md`](curriculum/phase-21-advanced-backend-architecture-distributed-systems/008-idempotent-message-consumers.md) | `NOT_STARTED` |
| `P21-C009` | Distributed Transactions: The Saga Pattern (Orchestration vs Choreography) | [`009-saga-pattern-orchestration-choreography.md`](curriculum/phase-21-advanced-backend-architecture-distributed-systems/009-saga-pattern-orchestration-choreography.md) | `NOT_STARTED` |
| `P21-C010` | Distributed Locking: Redis Redlock, Database Advisories, and Pitfalls | [`010-distributed-locking-redlock-pitfalls.md`](curriculum/phase-21-advanced-backend-architecture-distributed-systems/010-distributed-locking-redlock-pitfalls.md) | `NOT_STARTED` |
| `P21-C011` | API Gateways, Service Discovery, and Database-Per-Service Boundaries | [`011-api-gateways-service-boundaries.md`](curriculum/phase-21-advanced-backend-architecture-distributed-systems/011-api-gateways-service-boundaries.md) | `NOT_STARTED` |

### [Phase 22: Performance, Scalability, and Reliability](curriculum/phase-22-performance-scalability-reliability/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P22-C001` | Latency vs Throughput, Response Time Distributions, and Percentiles (p95, p99, p99.9) | [`001-latency-throughput-percentiles.md`](curriculum/phase-22-performance-scalability-reliability/001-latency-throughput-percentiles.md) | `NOT_STARTED` |
| `P22-C002` | CPU-Bound vs I/O-Bound Workload Diagnosis and System Resource Limits | [`002-cpu-vs-io-bound-diagnosis.md`](curriculum/phase-22-performance-scalability-reliability/002-cpu-vs-io-bound-diagnosis.md) | `NOT_STARTED` |
| `P22-C003` | Java Microbenchmark Harness (JMH): Benchmarking Pitfalls and JIT Dead Code Elimination | [`003-jmh-microbenchmarking-pitfalls.md`](curriculum/phase-22-performance-scalability-reliability/003-jmh-microbenchmarking-pitfalls.md) | `NOT_STARTED` |
| `P22-C004` | Profiling Java Applications: CPU & Allocation Profiling with async-profiler and JFR | [`004-profiling-async-profiler-jfr.md`](curriculum/phase-22-performance-scalability-reliability/004-profiling-async-profiler-jfr.md) | `NOT_STARTED` |
| `P22-C005` | Database Bottlenecks: Indexing Strategy, Connection Starvation, and Query Optimization | [`005-database-bottlenecks-query-tuning.md`](curriculum/phase-22-performance-scalability-reliability/005-database-bottlenecks-query-tuning.md) | `NOT_STARTED` |
| `P22-C006` | Load Testing and Stress Testing Methodology: k6, Gatling, and Coordinated Omission | [`006-load-testing-coordinated-omission.md`](curriculum/phase-22-performance-scalability-reliability/006-load-testing-coordinated-omission.md) | `NOT_STARTED` |
| `P22-C007` | Capacity Planning, Scalability Models, and Little's Law | [`007-capacity-planning-littles-law.md`](curriculum/phase-22-performance-scalability-reliability/007-capacity-planning-littles-law.md) | `NOT_STARTED` |
| `P22-C008` | Reliability Engineering: SLIs, SLOs, SLAs, Error Budgets, and Alerting Design | [`008-slis-slos-error-budgets.md`](curriculum/phase-22-performance-scalability-reliability/008-slis-slos-error-budgets.md) | `NOT_STARTED` |

### [Phase 23: Professional Engineering Habits](curriculum/phase-23-professional-engineering-habits/README.md)

| Concept ID | Concept Title | Document Link | Status |
|---|---|---|---|
| `P23-C001` | Scientific Debugging: Hypothesis Generation, Controlled Isolation, and Root Cause Analysis | [`001-scientific-debugging-methodology.md`](curriculum/phase-23-professional-engineering-habits/001-scientific-debugging-methodology.md) | `NOT_STARTED` |
| `P23-C002` | Reading and Navigating Legacy Codebases and External Open-Source Libraries | [`002-reading-legacy-codebases.md`](curriculum/phase-23-professional-engineering-habits/002-reading-legacy-codebases.md) | `NOT_STARTED` |
| `P23-C003` | Writing Architecture Decision Records (ADRs) to Document Technical Trade-Offs | [`003-architecture-decision-records-adrs.md`](curriculum/phase-23-professional-engineering-habits/003-architecture-decision-records-adrs.md) | `NOT_STARTED` |
| `P23-C004` | Code Review Excellence: Reviewing for Correctness, Security, Performance, and Empathy | [`004-code-review-standards.md`](curriculum/phase-23-professional-engineering-habits/004-code-review-standards.md) | `NOT_STARTED` |
| `P23-C005` | Incident Management: Severity Classification, Operational Runbooks, and Blameless Postmortems | [`005-incident-management-runbooks.md`](curriculum/phase-23-professional-engineering-habits/005-incident-management-runbooks.md) | `NOT_STARTED` |
| `P23-C006` | Dependency Lifecycle Management: Upgrades, Deprecation Horizons, and Security CVEs | [`006-dependency-lifecycle-cves.md`](curriculum/phase-23-professional-engineering-habits/006-dependency-lifecycle-cves.md) | `NOT_STARTED` |
| `P23-C007` | Autonomous Engineering Habits: Thinking from First Principles Without AI Hallucinations | [`007-autonomous-engineering-habits.md`](curriculum/phase-23-professional-engineering-habits/007-autonomous-engineering-habits.md) | `NOT_STARTED` |

---

## Supplemental Tracks & Advanced Specializations

| Track Directory | Subject Area | Index Link |
|---|---|---|
| [`supplemental/java-platform-and-language-evolution/`](supplemental/java-platform-and-language-evolution/) | JPMS Modules, Reflection, Annotation Processing, FFM API, GraalVM | [Track README](supplemental/README.md) |
| [`supplemental/advanced-persistence-and-database-engineering/`](supplemental/advanced-persistence-and-database-engineering/) | PostgreSQL WAL Internals, Sharding, Multi-Tenancy Architecture | [Track README](supplemental/README.md) |
| [`supplemental/advanced-spring-and-web-engineering/`](supplemental/advanced-spring-and-web-engineering/) | Project Reactor (Mono/Flux), Spring WebFlux, R2DBC Reactive DBs | [Track README](supplemental/README.md) |
| [`supplemental/secure-backend-engineering/`](supplemental/secure-backend-engineering/) | STRIDE Threat Modeling, Java Cryptography (AES-GCM), mTLS | [Track README](supplemental/README.md) |
| [`supplemental/linux-containers-and-deployment/`](linux-containers-and-deployment/) | Linux Process Internals (/proc, signals), Kubernetes Topologies | [Track README](supplemental/README.md) |
| [`supplemental/optional-specializations/`](optional-specializations/) | Apache Kafka Internals, gRPC & Protocol Buffers, Elasticsearch | [Track README](supplemental/README.md) |

---

## Templates & Governance Documents

- [`templates/CONCEPT_TEMPLATE.md`](templates/CONCEPT_TEMPLATE.md) — 16-section standard concept authoring template
- [`templates/PHASE_README_TEMPLATE.md`](templates/PHASE_README_TEMPLATE.md) — Standard structure for Phase README navigation pages
- [`templates/RESEARCH_AND_CITATION_RULES.md`](templates/RESEARCH_AND_CITATION_RULES.md) — Source hierarchy, authority tiers, and citation rules
