# Java Backend Engineering Prerequisites Roadmap

Before starting the 100 backend projects, you should complete a **Java Backend Engineering Prerequisites Roadmap**. The purpose is to rebuild your forgotten fundamentals, develop a strong understanding of Java, and prepare you to implement increasingly complex backend systems without depending on AI to explain every error.

Given that you already know basic programming concepts but have forgotten some Java, I would not recommend restarting with a beginner course and spending months watching videos. Follow a structured, implementation-driven curriculum.

There is also an important distinction: **mastering Java is not the same as mastering backend engineering with Java.** You need both.

- **Core Java:** Understand the language, its type system, object model, collections, exceptions, concurrency, and JVM.
- **Backend engineering:** Understand HTTP, REST, databases, transactions, security, testing, application architecture, and deployment.
- **Professional engineering:** Be able to debug production problems, evaluate trade-offs, test failure scenarios, and maintain complex systems.

Your preparation should cover all three areas before you begin the 100-project program.

# Java Backend Engineering Prerequisites Roadmap

I recommend dividing your preparation into 12 phases. Complete them sequentially, with coding exercises and explicit completion criteria.

## Phase 0 — Development Environment and Java Execution

**Goal:** Understand how Java source code becomes an executable program and establish a reliable development environment.

Since you have already been running Java programs on Linux, verify your existing installation instead of reinstalling everything.

### Topics to master

- [ ] JDK vs JRE vs JVM.
- [ ] Java compiler (`javac`) vs Java launcher (`java`).
- [ ] Source files (`.java`) and bytecode (`.class`).
- [ ] Compilation and execution.
- [ ] Classpath and package paths.
- [ ] Java entry point: `public static void main(String[] args)`.
- [ ] How Java source-file launching works.
- [ ] Environment variables: `JAVA_HOME` and `PATH`.
- [ ] JDK versions and compatibility.
- [ ] IDEs vs terminal-based development.
- [ ] Maven fundamentals and project structure.
- [ ] Git basics for tracking code changes.

### Commands you must understand

```bash
java -version
javac -version
which java
which javac
echo "$JAVA_HOME"
javac Main.java
java Main
```

Understand why `java Main.java` and `javac Main.java` followed by `java Main` represent different execution workflows.

Also understand why the main method must have the correct signature when the JVM launches a conventional Java application.

**Java version:** As of October 29, 2026, Java 25 is the latest LTS release. For your learning environment, use an OpenJDK distribution with Java 25 LTS, provided your selected libraries support it. Java 21 LTS is also a reasonable choice when following courses or tutorials built around that version. You do not need to learn every feature in the latest non-LTS release. ([oracle.com](https://www.oracle.com/java/technologies/downloads/?utm_source=chatgpt.com))

**Completion criterion:** You can create a Java project from scratch, compile it, execute it, diagnose common compilation errors, and explain the basic execution process without AI assistance.

## Phase 1 — Java Language Fundamentals

**Objective:** Rebuild the syntax and programming fundamentals you have forgotten.

### 1. Variables and data types

- [ ] Primitive types: `byte`, `short`, `int`, `long`.
- [ ] Floating-point types: `float`, `double`.
- [ ] `char` and `boolean`.
- [ ] Reference types.
- [ ] Variable declaration, initialization, and assignment.
- [ ] Local variables, instance variables, and static variables.
- [ ] Default values and initialization rules.
- [ ] Type casting: widening and narrowing.
- [ ] Integer overflow and numeric precision.
- [ ] Wrapper classes: `Integer`, `Long`, `Double`, and others.
- [ ] Autoboxing and unboxing.
- [ ] `var` and local variable type inference.
- [ ] `null` and reference initialization.

### 2. Operators

- [ ] Arithmetic operators.
- [ ] Relational operators.
- [ ] Logical operators.
- [ ] Assignment operators.
- [ ] Unary operators.
- [ ] Increment and decrement.
- [ ] Ternary operator.
- [ ] Bitwise operators.
- [ ] Shift operators.
- [ ] Operator precedence and associativity.
- [ ] Short-circuit evaluation.
- [ ] Equality operators versus object equality.

### 3. Control flow

- [ ] `if`, `else if`, and `else`.
- [ ] `switch` statements and expressions.
- [ ] Traditional `for` loops.
- [ ] Enhanced `for` loops.
- [ ] `while` and `do-while`.
- [ ] `break` and `continue`.
- [ ] Nested loops and loop control.

### 4. Methods

- [ ] Method declaration and invocation.
- [ ] Parameters and return types.
- [ ] Method overloading.
- [ ] Variable-length arguments using varargs.
- [ ] Parameter passing in Java.
- [ ] Scope and lifetime of variables.
- [ ] Recursion.
- [ ] Static methods versus instance methods.
- [ ] Method contracts, preconditions, and postconditions.

### 5. Strings

- [ ] String immutability.
- [ ] String pool and string interning.
- [ ] `String` comparison and equality.
- [ ] Common string operations.
- [ ] `StringBuilder` and when to use it.
- [ ] `StringBuffer` and how it differs from `StringBuilder`.
- [ ] Efficient string concatenation.
- [ ] Text parsing and formatting.

### 6. Arrays

- [ ] One-dimensional arrays.
- [ ] Multidimensional arrays.
- [ ] Array initialization and default values.
- [ ] Arrays of objects.
- [ ] Arrays of interface references.
- [ ] Array traversal and modification.
- [ ] Array copying.
- [ ] `Arrays` utility class.
- [ ] Sorting and searching arrays.
- [ ] Array bounds errors and `ArrayIndexOutOfBoundsException`.

**Completion test:** Write programs using methods, loops, arrays, strings, and input validation without copying an implementation from AI.

You should understand what happens when code executes, not just recognize syntax.

## Phase 2 — Object-Oriented Programming

**Goal:** Understand the object model well enough to design maintainable applications.

This phase is particularly important because Java backend applications depend heavily on classes, objects, interfaces, and object relationships.

### Classes and objects

- [ ] Classes and objects.
- [ ] Fields, methods, and constructors.
- [ ] Default and parameterized constructors.
- [ ] Constructor overloading.
- [ ] Constructor chaining using `this()`.
- [ ] `this` keyword.
- [ ] `static` members and class-level state.
- [ ] Initialization blocks and initialization order.
- [ ] Access modifiers: `public`, `protected`, package-private, and `private`.
- [ ] Encapsulation and data hiding.
- [ ] Getters and setters, and when not to create them.
- [ ] Object lifecycle and object references.

### The four OOP principles

- [ ] Encapsulation.
- [ ] Abstraction.
- [ ] Inheritance.
- [ ] Polymorphism.

### Inheritance and polymorphism

- [ ] `extends` and `implements`.
- [ ] Method overriding.
- [ ] Method overloading versus overriding.
- [ ] Upcasting and downcasting.
- [ ] Runtime method dispatch.
- [ ] The `super` keyword.
- [ ] The `final` keyword on variables, methods, and classes.
- [ ] Abstract classes and abstract methods.
- [ ] Interfaces and implementation contracts.
- [ ] Multiple interface implementation.
- [ ] Interface default and static methods.
- [ ] Interface inheritance.
- [ ] Abstract classes versus interfaces.
- [ ] Composition versus inheritance.
- [ ] Why composition is frequently preferred for business logic.

### Object relationships

- [ ] Association.
- [ ] Aggregation.
- [ ] Composition.
- [ ] One-to-one relationships.
- [ ] One-to-many relationships.
- [ ] Many-to-many relationships.
- [ ] Dependency relationships.
- [ ] Object references and aliasing.

### Additional language constructs

- [ ] Enums.
- [ ] Nested classes.
- [ ] Inner classes.
- [ ] Static nested classes.
- [ ] Anonymous classes.
- [ ] Records and immutable data carriers.
- [ ] Sealed classes and interfaces.
- [ ] Pattern matching basics.
- [ ] `Object` methods: `toString()`, `equals()`, and `hashCode()`.

**Completion test:** Design a small banking application using classes and interfaces. Explain why each class exists, which objects depend on other objects, and which parts of the design should change independently.

Do not advance by simply demonstrating inheritance. You must be able to decide when inheritance is appropriate and when composition or an interface is better.

## Phase 3 — Collections Framework, Generics, and Type Safety

**Goal:** Learn the data structures used extensively in real Java applications.

### Collections hierarchy

- [ ] `Iterable` and `Iterator`.
- [ ] `Collection` interface.
- [ ] `List`, `Set`, `Queue`, and `Deque`.
- [ ] `Map` and why it is not a subtype of `Collection`.

### Important implementations

- [ ] `ArrayList`.
- [ ] `LinkedList`.
- [ ] `HashSet`.
- [ ] `LinkedHashSet`.
- [ ] `TreeSet`.
- [ ] `HashMap`.
- [ ] `LinkedHashMap`.
- [ ] `TreeMap`.
- [ ] `PriorityQueue`.
- [ ] `ArrayDeque`.
- [ ] `ConcurrentHashMap`.
- [ ] Immutable and unmodifiable collections.

### Collection behavior

- [ ] Choosing an appropriate collection for a problem.
- [ ] Ordering, uniqueness, and duplicate handling.
- [ ] Hash-based collections and hashing.
- [ ] `equals()` and `hashCode()` contracts.
- [ ] Mutable objects used as map keys.
- [ ] Natural ordering using `Comparable`.
- [ ] Custom ordering using `Comparator`.
- [ ] Sorting collections.
- [ ] Iteration and safe removal.
- [ ] Fail-fast iterators and their limitations.
- [ ] Collection copying and defensive copying.
- [ ] Time complexity of common collection operations.

### Generics

- [ ] Generic classes.
- [ ] Generic interfaces.
- [ ] Generic methods.
- [ ] Type parameters and bounded type parameters.
- [ ] Multiple type bounds.
- [ ] Wildcards: `?`, `? extends T`, and `? super T`.
- [ ] Upper and lower bounds.
- [ ] PECS: producer extends, consumer super.
- [ ] Generic invariance.
- [ ] Type erasure.
- [ ] Raw types and unchecked operations.
- [ ] Generic restrictions and interoperability.

**Completion test:** Build a reusable in-memory repository that stores objects by ID, retrieves them, updates them, deletes them, and supports different domain object types without using `Object` everywhere or duplicating the implementation.

## Phase 4 — Exception Handling and Defensive Programming

**Goal:** Handle failures explicitly instead of allowing errors to spread through an application.

- [ ] The `Throwable` hierarchy.
- [ ] `Error` versus `Exception`.
- [ ] Checked versus unchecked exceptions.
- [ ] `try`, `catch`, and `finally`.
- [ ] Multiple catch blocks.
- [ ] Multi-catch.
- [ ] `throw` versus `throws`.
- [ ] Creating custom exceptions.
- [ ] Exception chaining and causal exceptions.
- [ ] Try-with-resources.
- [ ] Suppressed exceptions.
- [ ] Resource cleanup.
- [ ] Input validation.
- [ ] Fail-fast validation.
- [ ] Exception propagation through method calls.
- [ ] Avoiding swallowed exceptions.
- [ ] Avoiding empty catch blocks.
- [ ] Designing meaningful error messages.
- [ ] When to use exceptions versus return values.
- [ ] Defensive programming without excessive validation duplication.

**Completion test:** Build a transaction-like bank transfer operation with validation and meaningful failure handling. Verify that an invalid operation does not accidentally leave the two account balances inconsistent.

## Phase 5 — Core Java APIs You Will Use in Production

**Goal:** Learn standard library facilities so you do not reinvent functionality already provided by Java.

### Date and time

- [ ] `LocalDate`.
- [ ] `LocalTime`.
- [ ] `LocalDateTime`.
- [ ] `Instant`.
- [ ] `ZonedDateTime`.
- [ ] `Duration` and `Period`.
- [ ] `ZoneId` and time-zone conversions.
- [ ] Date parsing and formatting.
- [ ] Time-zone-safe handling of timestamps.

### Numeric types

- [ ] Integer and floating-point representations.
- [ ] Overflow and precision.
- [ ] `BigInteger`.
- [ ] `BigDecimal`.
- [ ] Rounding modes.
- [ ] Scale and precision.
- [ ] Why `double` is generally unsuitable for financial calculations.

### Other important APIs

- [ ] `Objects` utility methods.
- [ ] `Optional`.
- [ ] `UUID`.
- [ ] `Random` and `SecureRandom`, including their different use cases.
- [ ] `Math`.
- [ ] `Collections`.
- [ ] `Arrays`.
- [ ] `Properties`.
- [ ] System properties and environment variables.
- [ ] Locale-aware formatting.
- [ ] Regular expressions using `Pattern` and `Matcher`.

**Completion test:** Implement a basic expense calculation module that validates amounts, uses appropriate numeric types, handles dates correctly, and formats output consistently.

## Phase 6 — Functional Java

**Goal:** Understand the functional programming features used heavily in modern Java libraries and backend code.

- [ ] Functional interfaces.
- [ ] `Predicate`.
- [ ] `Function`.
- [ ] `Consumer`.
- [ ] `Supplier`.
- [ ] Lambda expressions.
- [ ] Method references.
- [ ] Functional composition.
- [ ] Built-in versus custom functional interfaces.
- [ ] Stream creation.
- [ ] Intermediate versus terminal operations.
- [ ] `filter`, `map`, and `flatMap`.
- [ ] `sorted`, `distinct`, and `limit`.
- [ ] `reduce`.
- [ ] `collect`.
- [ ] Collectors and grouping.
- [ ] Partitioning and aggregation.
- [ ] Primitive streams.
- [ ] Optional values and safe mapping.
- [ ] Stream laziness.
- [ ] Stream side effects.
- [ ] Short-circuit operations.
- [ ] Stream debugging.
- [ ] Sequential versus parallel streams.
- [ ] When a normal loop is clearer than a stream.

**Completion test:** Process a collection of orders to calculate totals, group orders by customer, find the highest-value order, and filter orders by status.

You should be able to explain the result of each pipeline and avoid unnecessary side effects.

## Phase 7 — File I/O, NIO, Configuration, and Data Formats

**Goal:** Learn how Java reads, writes, and processes external data.

- [ ] `File` and its limitations.
- [ ] `Path` and `Files`.
- [ ] Input streams and output streams.
- [ ] Readers and writers.
- [ ] Buffered I/O.
- [ ] Byte-oriented versus character-oriented I/O.
- [ ] File creation, reading, updating, and deletion.
- [ ] Directory traversal.
- [ ] File metadata and permissions.
- [ ] Try-with-resources.
- [ ] NIO fundamentals.
- [ ] Channels and buffers at a conceptual level.
- [ ] Temporary files.
- [ ] Large-file processing.
- [ ] File encodings and UTF-8.
- [ ] Properties files and external configuration.
- [ ] CSV parsing and validation.
- [ ] JSON serialization and deserialization.
- [ ] Resource files and classpath resources.
- [ ] Why Java native serialization should not be treated as a universal data interchange format.
- [ ] Secure handling of file uploads and untrusted input.

**Completion test:** Build a CSV importer that reads a file, validates every record, reports rejected rows, and produces a summary without loading an arbitrarily large file entirely into memory.

## Phase 8 — JVM Internals and Memory Management

**Goal:** Understand enough of the runtime to reason about memory, performance, and application failures.

### JVM architecture

- [ ] JVM, JRE terminology, and JDK components.
- [ ] Source code, bytecode, and execution.
- [ ] Class loading.
- [ ] Linking and initialization.
- [ ] Class loaders.
- [ ] Bytecode verification.
- [ ] Interpreter and JIT compilation.
- [ ] Runtime data areas.
- [ ] Heap and thread stacks.
- [ ] Metaspace.
- [ ] Program counter and native method stacks at a conceptual level.

### Memory management

- [ ] Object allocation.
- [ ] Object reachability.
- [ ] Strong and weak references.
- [ ] Soft and phantom references at a conceptual level.
- [ ] Garbage collection fundamentals.
- [ ] Common garbage collector terminology.
- [ ] Memory leaks in garbage-collected applications.
- [ ] Retained objects and unwanted references.
- [ ] `OutOfMemoryError`.
- [ ] `StackOverflowError`.
- [ ] Boxing and allocation overhead.
- [ ] Object lifetime and allocation patterns.
- [ ] Heap dumps and thread dumps.

### Debugging and performance

- [ ] Read Java stack traces.
- [ ] Interpret exceptions and causes.
- [ ] Understand GC logs at a basic level.
- [ ] Use Java Flight Recorder and Java Mission Control where available.
- [ ] Understand what a profiler measures.
- [ ] Distinguish CPU-bound from I/O-bound workloads.
- [ ] Understand why premature optimization is dangerous.
- [ ] Learn basic benchmarking principles.

**Completion test:** Diagnose a deliberately introduced memory-retention problem and explain the root cause. You do not need to become a JVM engineer before starting backend projects, but you should understand these fundamentals.

## Phase 9 — Data Structures and Algorithms for Backend Developers

**Goal:** Develop sufficient problem-solving ability to implement algorithms, select data structures, and reason about performance.

### Complexity

- [ ] Time complexity.
- [ ] Space complexity.
- [ ] Big-O notation.
- [ ] Best, average, and worst cases.
- [ ] Amortized complexity at a basic level.

### Data structures

- [ ] Arrays and dynamic arrays.
- [ ] Linked lists.
- [ ] Stacks and queues.
- [ ] Hash tables.
- [ ] Sets and maps.
- [ ] Trees and binary search trees.
- [ ] Heaps and priority queues.
- [ ] Graph fundamentals.
- [ ] Tries at a basic level.

### Algorithms

- [ ] Linear search.
- [ ] Binary search.
- [ ] Elementary sorting algorithms.
- [ ] Efficient comparison sorting.
- [ ] Recursion.
- [ ] Two-pointer patterns.
- [ ] Sliding windows.
- [ ] Frequency counting.
- [ ] Hash-based lookup.
- [ ] Basic tree traversal.
- [ ] Breadth-first and depth-first search.
- [ ] Basic graph traversal.
- [ ] Introductory dynamic programming.

**Completion test:** Solve common easy-level programming problems independently and explain why your selected algorithm and data structure are appropriate.

You do not need to complete hundreds of competitive programming problems before backend development. Focus on sound fundamentals and regularly practise problem-solving.

## Phase 10 — Professional Java Development Workflow

**Goal:** Work on Java projects in a maintainable, reproducible, team-friendly way.

- [ ] Git branching and merge workflows.
- [ ] Meaningful commit messages.
- [ ] Pull requests and code review principles.
- [ ] Maven lifecycle and common commands.
- [ ] Maven dependency management.
- [ ] Dependency scopes.
- [ ] Transitive dependencies.
- [ ] Plugins and build configuration.
- [ ] Maven wrapper.
- [ ] Version compatibility and dependency upgrades.
- [ ] Environment-specific configuration.
- [ ] Logging with SLF4J and a logging implementation.
- [ ] Avoiding sensitive information in logs.
- [ ] Breakpoints and step-through debugging.
- [ ] Reading and reproducing bugs.
- [ ] Formatting and static analysis.
- [ ] Basic clean-code principles.
- [ ] Naming and method design.
- [ ] Cohesion and coupling.
- [ ] Separation of concerns.
- [ ] Avoiding duplicated logic.
- [ ] Writing useful README and setup instructions.
- [ ] Documenting design decisions.
- [ ] Keeping local configuration and secrets out of Git.

**Completion test:** Check out your Java repository into a fresh directory, build it using documented commands, run its tests, and explain how another developer can reproduce the result.

## Phase 11 — Unit Testing and Software Quality

**Goal:** Learn to prove that your code works instead of relying on manual verification.

### Testing fundamentals

- [ ] Unit, integration, and end-to-end testing.
- [ ] Test cases and expected outcomes.
- [ ] Positive and negative test cases.
- [ ] Boundary-value analysis.
- [ ] Equivalence partitioning.
- [ ] Arrange–Act–Assert.
- [ ] Test isolation and repeatability.
- [ ] Test fixtures and test data.
- [ ] Deterministic tests.
- [ ] Testing exceptions and validation failures.

### JUnit

- [ ] JUnit 5 fundamentals.
- [ ] Test lifecycle annotations.
- [ ] Assertions.
- [ ] Parameterized tests.
- [ ] Exception assertions.
- [ ] Nested tests.
- [ ] Test naming and organization.

### Mocking and integration

- [ ] Mockito fundamentals.
- [ ] Stubs and mocks.
- [ ] Verifying interactions.
- [ ] Testing service logic in isolation.
- [ ] Understanding when mocking is inappropriate.
- [ ] Integration testing.
- [ ] Test database setup and teardown.
- [ ] Testcontainers fundamentals.
- [ ] Code coverage and its limitations.

### Testing principles

- [ ] Do not test implementation details unnecessarily.
- [ ] Avoid tests that pass regardless of behavior.
- [ ] Do not mock every dependency automatically.
- [ ] Test failure scenarios.
- [ ] Avoid shared mutable test state.
- [ ] Understand the difference between coverage and correctness.

**Completion test:** Every important business rule in your practice applications has automated tests. You can introduce a bug deliberately and demonstrate that an appropriate test detects it.

## Phase 12 — Concurrency and Multithreading

**Goal:** Understand how Java executes work across multiple threads and how concurrency bugs arise.

This phase is especially important for backend engineering, where multiple requests can modify shared state simultaneously.

### Fundamentals

- [ ] Processes versus threads.
- [ ] Creating threads.
- [ ] Thread lifecycle.
- [ ] `Runnable` and `Callable`.
- [ ] `Thread` basics.
- [ ] `start()` versus `run()`.
- [ ] Thread interruption.
- [ ] Daemon threads.
- [ ] Shared mutable state.
- [ ] Race conditions.
- [ ] Atomicity, visibility, and ordering.
- [ ] Critical sections.
- [ ] Thread safety.

### Synchronization

- [ ] `synchronized` methods and blocks.
- [ ] Intrinsic locks.
- [ ] `volatile` and its limitations.
- [ ] Atomic variables.
- [ ] `AtomicInteger`, `AtomicLong`, and compare-and-set concepts.
- [ ] `ReentrantLock`.
- [ ] Deadlocks.
- [ ] Starvation and livelock.
- [ ] Lock granularity.
- [ ] Safe publication.
- [ ] Immutable objects and concurrency safety.

### Concurrency utilities

- [ ] Executor framework.
- [ ] `ExecutorService`.
- [ ] Thread pools.
- [ ] `Future` and `Callable`.
- [ ] `CompletableFuture`.
- [ ] `CountDownLatch`.
- [ ] `Semaphore`.
- [ ] `BlockingQueue`.
- [ ] Producer-consumer pattern.
- [ ] Concurrent collections.
- [ ] `ConcurrentHashMap`.
- [ ] Scheduled executors.
- [ ] Executor shutdown and resource cleanup.
- [ ] Backpressure concepts.

### Modern Java concurrency

- [ ] Virtual threads.
- [ ] Virtual threads versus platform threads.
- [ ] Appropriate use cases for virtual threads.
- [ ] Blocking and non-blocking operations.
- [ ] Structured concurrency concepts.
- [ ] Thread-local state and its implications.

Do not assume that introducing virtual threads automatically makes an application faster or eliminates synchronization requirements.

**Completion test:** Build a concurrent ticket-booking simulator. Demonstrate how two threads attempting to reserve the same remaining seat can produce an incorrect result, then correct the underlying race condition.

## Phase 13 — SQL and Relational Database Fundamentals

**Goal:** Understand relational databases before using an ORM to abstract database operations.

Learn PostgreSQL first. The SQL and relational concepts will also transfer to other relational database systems.

### Database fundamentals

- [ ] Tables, rows, columns, and data types.
- [ ] Primary keys and foreign keys.
- [ ] Candidate and composite keys.
- [ ] Unique, check, not-null, and foreign-key constraints.
- [ ] Relationships and cardinality.
- [ ] One-to-one, one-to-many, and many-to-many relationships.
- [ ] Database normalization.
- [ ] Denormalization and its trade-offs.
- [ ] Logical versus physical schema design.

### SQL

- [ ] `SELECT`, `INSERT`, `UPDATE`, and `DELETE`.
- [ ] Filtering and sorting.
- [ ] Aggregate functions.
- [ ] `GROUP BY` and `HAVING`.
- [ ] Inner, outer, cross, and self joins.
- [ ] Subqueries.
- [ ] Common table expressions.
- [ ] Window functions.
- [ ] Set operations.
- [ ] Views.
- [ ] Indexes and composite indexes.
- [ ] Transactions.
- [ ] Isolation levels.
- [ ] Deadlocks.
- [ ] Optimistic and pessimistic locking concepts.
- [ ] Execution plans and `EXPLAIN`.
- [ ] Query performance fundamentals.

### Transaction fundamentals

- [ ] ACID properties.
- [ ] Commit and rollback.
- [ ] Transaction boundaries.
- [ ] Dirty reads, non-repeatable reads, and phantom reads.
- [ ] Lost updates.
- [ ] Concurrent modifications.
- [ ] Transaction isolation and its trade-offs.
- [ ] Database constraints as correctness safeguards.

**Completion test:** Design a small order-management schema, write its essential queries, implement appropriate constraints, and demonstrate how transactions protect data consistency.

## Phase 14 — JDBC and Java Database Connectivity

**Goal:** Understand what happens between Java code and a relational database.

- [ ] JDBC architecture.
- [ ] JDBC drivers.
- [ ] Database connection configuration.
- [ ] `Connection`.
- [ ] `PreparedStatement`.
- [ ] `ResultSet`.
- [ ] Parameter binding.
- [ ] CRUD operations with JDBC.
- [ ] Mapping rows to Java objects.
- [ ] Transactions with JDBC.
- [ ] Auto-commit.
- [ ] Commit and rollback.
- [ ] Batch operations.
- [ ] SQL exceptions.
- [ ] Resource cleanup with try-with-resources.
- [ ] SQL injection prevention.
- [ ] Connection pooling.
- [ ] Connection leaks and pool exhaustion.
- [ ] Database credentials and external configuration.
- [ ] Repository abstraction.

**Completion test:** Build a Java application that performs database CRUD operations using JDBC, manages transactions correctly, and uses parameterized SQL throughout.

You should understand JDBC before relying on Spring Data JPA to perform persistence operations automatically.

## Phase 15 — HTTP, Networking, and REST API Fundamentals

**Goal:** Understand the protocols and API conventions used by backend applications.

### Networking

- [ ] Client-server architecture.
- [ ] IP addresses, ports, and sockets.
- [ ] DNS basics.
- [ ] TCP versus UDP at a conceptual level.
- [ ] TLS and HTTPS fundamentals.
- [ ] Connection timeouts.
- [ ] Read and write timeouts.
- [ ] Network failures and retries.
- [ ] Synchronous versus asynchronous communication.

### HTTP

- [ ] HTTP request and response structure.
- [ ] Request methods.
- [ ] HTTP status codes.
- [ ] Request headers and response headers.
- [ ] Request bodies.
- [ ] JSON.
- [ ] Content types.
- [ ] Cookies.
- [ ] Authentication headers.
- [ ] Caching headers.
- [ ] Idempotent versus non-idempotent operations.
- [ ] HTTP connection behavior.
- [ ] API timeouts and error responses.

### REST API design

- [ ] Resource-oriented URL design.
- [ ] Resource identifiers.
- [ ] GET, POST, PUT, PATCH, and DELETE semantics.
- [ ] Correct status-code selection.
- [ ] Request DTOs and response DTOs.
- [ ] Input validation.
- [ ] Pagination.
- [ ] Sorting and filtering.
- [ ] Consistent error responses.
- [ ] API versioning.
- [ ] Idempotency keys.
- [ ] OpenAPI documentation.
- [ ] Backward compatibility.
- [ ] Rate limiting concepts.

**Completion test:** Design the endpoints for a task-management API before implementing them. Explain the expected methods, status codes, payloads, validation rules, and failure responses.

## Phase 16 — Clean Code and Software Design Principles

**Goal:** Design applications whose code remains understandable as requirements grow.

### Principles

- [ ] Single Responsibility Principle.
- [ ] Open/Closed Principle.
- [ ] Liskov Substitution Principle.
- [ ] Interface Segregation Principle.
- [ ] Dependency Inversion Principle.
- [ ] Separation of concerns.
- [ ] Composition over inheritance.
- [ ] Encapsulation of business rules.
- [ ] High cohesion and low coupling.
- [ ] Avoiding premature abstraction.
- [ ] Recognizing overengineering.

### Design patterns

- [ ] Strategy.
- [ ] Factory Method and simple factory approaches.
- [ ] Builder.
- [ ] Adapter.
- [ ] Decorator.
- [ ] Observer.
- [ ] Template Method.
- [ ] Command.
- [ ] State.
- [ ] Repository.
- [ ] Dependency injection.

Learn the motivation and trade-offs behind each pattern. Do not memorize pattern definitions and then insert them into applications whether they fit or not.

**Completion test:** Refactor a poorly structured application into smaller components with clear responsibilities. Explain which improvements genuinely simplify the code and which would add unnecessary complexity.

## Phase 17 — Spring Framework Fundamentals

**Goal:** Understand the framework that supports much of modern Java backend development.

Complete this phase after establishing a solid foundation in core Java and basic database concepts.

- [ ] Inversion of Control (IoC).
- [ ] Dependency injection (DI).
- [ ] The Spring container.
- [ ] Beans and bean lifecycle.
- [ ] Component scanning.
- [ ] `@Component`.
- [ ] `@Service`.
- [ ] `@Repository`.
- [ ] `@Configuration`.
- [ ] `@Bean`.
- [ ] Constructor-based dependency injection.
- [ ] Bean scopes.
- [ ] Configuration properties.
- [ ] Profiles and environment-specific configuration.
- [ ] Property resolution.
- [ ] Application lifecycle.
- [ ] Proxy-based behavior at a conceptual level.
- [ ] Aspect-oriented programming fundamentals.
- [ ] Spring Framework versus Spring Boot.
- [ ] Auto-configuration.
- [ ] Spring Boot starters.
- [ ] Externalized configuration.

**Completion test:** Create a small Spring application in which services receive dependencies through the Spring container. Explain how the application works without manually constructing every dependency.

## Phase 18 — Spring Boot and REST API Development

**Goal:** Implement maintainable HTTP services using modern Spring Boot.

- [ ] Spring Boot project structure.
- [ ] Controllers and request mappings.
- [ ] Request parameters and path variables.
- [ ] Request-body handling.
- [ ] DTOs.
- [ ] Bean Validation.
- [ ] Custom validation.
- [ ] Centralized exception handling.
- [ ] `@RestControllerAdvice`.
- [ ] Service-layer business logic.
- [ ] Repository-layer persistence.
- [ ] Spring Data JPA.
- [ ] Hibernate fundamentals.
- [ ] Entity relationships.
- [ ] Fetch types and loading behavior.
- [ ] Transactions using `@Transactional`.
- [ ] Transaction propagation and isolation basics.
- [ ] Pagination and sorting.
- [ ] Specifications or other suitable filtering approaches.
- [ ] Database migrations with Flyway or Liquibase.
- [ ] OpenAPI integration.
- [ ] Health and readiness endpoints.
- [ ] Graceful shutdown.
- [ ] Externalized configuration.
- [ ] Logging and correlation IDs.
- [ ] Avoiding N+1 query problems.
- [ ] Avoiding accidental entity exposure.
- [ ] Testing REST endpoints.

**Completion test:** Build an API with create, read, update, and delete operations; PostgreSQL persistence; migrations; validation; predictable error responses; pagination; and automated tests.

## Phase 19 — Application Security

**Goal:** Understand how to protect backend services and their data.

- [ ] Authentication versus authorization.
- [ ] Identity and access management concepts.
- [ ] Password hashing.
- [ ] Secure password verification.
- [ ] Spring Security filter chain.
- [ ] Authentication providers.
- [ ] Authorities and roles.
- [ ] Role-based access control (RBAC).
- [ ] Attribute-based access control (ABAC).
- [ ] JWT structure and validation.
- [ ] Access and refresh tokens.
- [ ] Token expiry, rotation, and revocation strategies.
- [ ] OAuth 2.0 fundamentals.
- [ ] OpenID Connect fundamentals.
- [ ] Session-based authentication.
- [ ] CORS and CSRF.
- [ ] SQL injection.
- [ ] Broken access control.
- [ ] Resource ownership checks.
- [ ] Tenant isolation.
- [ ] Rate limiting.
- [ ] Secret management.
- [ ] Security logging.
- [ ] Dependency vulnerability scanning.
- [ ] Threat modeling.

**Completion test:** Secure a Spring Boot API so unauthenticated users cannot access protected resources, users cannot access other users' private records, and privileged operations require explicit authorization.

## Phase 20 — Production Backend Engineering

**Goal:** Learn the operational concerns required to run reliable applications.

- [ ] Docker fundamentals.
- [ ] Dockerfiles and image layers.
- [ ] Multi-stage builds.
- [ ] Container networking.
- [ ] Environment variables and secrets.
- [ ] Docker Compose for local dependencies.
- [ ] Configuration management.
- [ ] Structured logging.
- [ ] Metrics and tracing fundamentals.
- [ ] Spring Boot Actuator.
- [ ] Micrometer.
- [ ] OpenTelemetry fundamentals.
- [ ] Health checks.
- [ ] Readiness and liveness.
- [ ] Graceful shutdown.
- [ ] CI/CD pipelines.
- [ ] Automated build and test stages.
- [ ] Deployment rollback strategies.
- [ ] Database migration safety.
- [ ] External API timeouts and retries.
- [ ] Retry limits and backoff.
- [ ] Circuit breakers.
- [ ] Idempotent processing.
- [ ] Caching and cache invalidation.
- [ ] Redis fundamentals.
- [ ] Message queues and event-driven processing.
- [ ] Kafka or RabbitMQ fundamentals.
- [ ] Distributed transaction challenges.
- [ ] Observability-driven debugging.
- [ ] Load testing and performance analysis.
- [ ] Backup and recovery fundamentals.

These are advanced prerequisites for later projects, not requirements for your first Java program.

## Phase 21 — Advanced Backend Architecture and Distributed Systems

**Goal:** Understand the additional complexity introduced when applications become large, stateful, or distributed.

### Application architecture

- [ ] Layered architecture.
- [ ] Modular monoliths.
- [ ] Domain-driven design fundamentals.
- [ ] Domain models and business invariants.
- [ ] Aggregates and aggregate boundaries.
- [ ] Application services and domain services.
- [ ] Repository abstractions and their trade-offs.
- [ ] Ports and adapters (hexagonal architecture).
- [ ] Clean Architecture fundamentals.
- [ ] Dependency direction.
- [ ] Architectural decision records.
- [ ] Bounded contexts and service boundaries.

### Distributed systems

- [ ] Why distributed systems are difficult.
- [ ] Network partitions and partial failures.
- [ ] Latency and unreliable communication.
- [ ] Timeouts and retry behavior.
- [ ] At-most-once, at-least-once, and effectively-once processing semantics.
- [ ] Idempotent operations.
- [ ] Eventual consistency.
- [ ] Distributed locking and its limitations.
- [ ] Message ordering and duplicate delivery.
- [ ] Transactional outbox pattern.
- [ ] Saga pattern.
- [ ] Service discovery.
- [ ] API gateways.
- [ ] Circuit breakers, bulkheads, and backpressure.
- [ ] Distributed tracing.
- [ ] Data ownership between services.
- [ ] Database-per-service trade-offs.
- [ ] Why distributed transactions are difficult.
- [ ] Service decomposition.
- [ ] Microservices versus a modular monolith.

**Completion test:** Design a system containing multiple business modules and explain which boundaries should remain within one application and which, if any, justify separate services.

Do not begin with microservices simply because they sound more advanced. A well-designed modular monolith is frequently a better starting point.

## Phase 22 — Performance, Scalability, and Reliability

**Goal:** Develop the ability to identify bottlenecks and improve the correct part of a system.

- [ ] Latency versus throughput.
- [ ] Average latency versus percentiles such as p95 and p99.
- [ ] CPU-bound and I/O-bound workloads.
- [ ] Connection-pool sizing.
- [ ] Database connection exhaustion.
- [ ] Query plans and index selection.
- [ ] N+1 query behavior.
- [ ] Cache hit rate and cache invalidation.
- [ ] Memory profiling.
- [ ] CPU profiling.
- [ ] Thread dumps and deadlock investigation.
- [ ] Garbage collection analysis.
- [ ] Load testing.
- [ ] Stress testing.
- [ ] Soak testing.
- [ ] Capacity planning.
- [ ] Queues and backpressure.
- [ ] Resource limits and graceful degradation.
- [ ] Horizontal versus vertical scaling.
- [ ] Performance baselines and regression detection.
- [ ] Reliability objectives and service-level indicators.
- [ ] Failure recovery and data integrity.

**Completion test:** Measure a workload, identify an actual bottleneck, implement a targeted improvement, and compare measurements under equivalent conditions.

Do not claim performance improvements based only on code inspection. Measure them.

## Phase 23 — Professional Engineering Habits

**Goal:** Work independently, explain your decisions, and maintain code over time.

- [ ] Read existing code before modifying it.
- [ ] Reproduce bugs before attempting fixes.
- [ ] Read documentation and dependency release notes.
- [ ] Distinguish confirmed causes from hypotheses.
- [ ] Write small, reviewable changes.
- [ ] Maintain tests when refactoring.
- [ ] Review pull requests critically.
- [ ] Recognize inappropriate abstractions.
- [ ] Identify security and data-consistency risks.
- [ ] Document architectural trade-offs.
- [ ] Separate required fixes from optional improvements.
- [ ] Keep dependencies and tooling maintainable.
- [ ] Avoid hardcoding environment-specific values.
- [ ] Use automated checks consistently.
- [ ] Explain unfamiliar code before changing it.
- [ ] Investigate root causes rather than suppressing errors.
- [ ] Maintain a record of mistakes and lessons learned.
- [ ] Explain design decisions without relying on AI-generated explanations.

**Completion test:** Given an unfamiliar Java application, you can run it, identify its architecture, trace an operation across layers, reproduce a bug, implement a fix, and prove through tests that the fix works.

# 7. In What Order Should You Complete These Phases?

Do not study all 23 phases in isolation and postpone coding until the end. Use this sequence to determine when you are ready for each part of the 100-project program.

| Learning stage | Complete these phases first | What you are ready to practise |
|---|---|---|
| Core Java | 0–5, plus basic Git and testing from 10–11 | Projects 1–10 |
| Advanced core Java | 6–9, 10–12 | Projects 11–20 |
| Database programming | 13–14 | Projects 21–30 |
| Web fundamentals | 15, 17–18 | Projects 31–40 |
| Application security | 19 | Projects 41–50 |
| Advanced application design | 16 and relevant database and architecture topics | Projects 51–60 |
| Caching and asynchronous processing | Concurrency fundamentals and relevant parts of Phase 20 | Projects 61–70 |
| Distributed systems | 20–21 | Projects 71–80 |
| Production engineering | 11, 20, 22–23 and the relevant preceding phases | Projects 81–100 |

**Important:** These are prerequisites, not rigid completion barriers. For example, you should understand the fundamentals of concurrency before building a multithreaded application, but you can deepen that understanding by implementing the application.

Likewise, learn Spring Boot before building Spring Boot applications—not before writing your first Java program.

# 8. How to Study Each Topic with Antigravity AI

Since Antigravity will be part of your learning process, establish a consistent method for every topic.

**Step 1: Understand the concept.**

Ask Antigravity to explain a topic from first principles, including its purpose, syntax, execution behavior, common mistakes, and practical use cases.

**Step 2: Implement a small example.**

Write the code yourself first. Use Antigravity to review the implementation and identify mistakes.

**Step 3: Test your understanding.**

Ask Antigravity to give you exercises without providing the solutions immediately. Solve the exercises independently.

**Step 4: Debug deliberately.**

Introduce errors or incorrect assumptions into your code. Predict the result, execute the program, and investigate any differences.

**Step 5: Implement from memory.**

Close the reference implementation and recreate the solution. If you cannot do this, revisit the underlying concept.

**Step 6: Record completion evidence.**

Update a learning checklist with the topic, exercises completed, test results, and concepts that still require practice.

Do not mark a topic complete simply because Antigravity generated working code. Mark it complete when you can explain it, implement it, test it, and debug it.

# 9. Your Definition of Java Proficiency

Before considering yourself ready to tackle the advanced projects, you should be comfortable performing the following tasks.

| Skill | Expected capability |
|---|---|
| Core Java | Write programs using classes, interfaces, collections, exceptions, and generics |
| OOP | Design classes and object relationships without unnecessary inheritance |
| Collections | Select appropriate data structures and understand their performance characteristics |
| Functional Java | Use lambdas, streams, and functional interfaces appropriately |
| JVM | Explain basic memory management, garbage collection, and runtime behavior |
| Concurrency | Identify race conditions and implement suitable synchronization |
| Problem-solving | Select appropriate algorithms and reason about complexity |
| Testing | Write automated tests and investigate failures |
| Build tooling | Build, test, and package applications using Maven |
| Database programming | Design relational schemas, write SQL, and manage transactions |
| HTTP | Design resource-oriented APIs and interpret HTTP semantics |
| Spring | Build applications with dependency injection, persistence, validation, and security |
| Software design | Maintain clear component boundaries and explain architecture decisions |
| Production engineering | Diagnose failures, inspect logs, measure performance, and deploy applications |

You do not need equal expertise in all these areas at the beginning. The expected depth increases as you progress.

# 10. When Are You Ready to Start?

For your immediate objective, prioritize these topics:

- [ ] Java execution and development environment.
- [ ] Variables, data types, operators, loops, and conditionals.
- [ ] Methods, arrays, and strings.
- [ ] Classes, objects, and constructors.
- [ ] Encapsulation, inheritance, polymorphism, and interfaces.
- [ ] Composition and object relationships.
- [ ] Collections and generics.
- [ ] Exception handling and validation.
- [ ] Basic file operations.
- [ ] Git fundamentals.
- [ ] Basic JUnit testing.

Once you can implement small programs using these concepts without copying complete solutions, you are ready to begin the first projects.

Continue learning advanced core Java, SQL, concurrency, Spring Boot, security, and distributed systems as the corresponding projects require them.

**My recommendation:** Treat this checklist as your Java Backend Engineering Curriculum and the 100 projects as your practical training program. The checklist establishes what you need to learn; the projects develop your ability to apply that knowledge to increasingly difficult engineering problems.
