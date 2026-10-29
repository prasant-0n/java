# Phase 00: Development Environment and Java Execution

---

## 1. Phase Overview and Purpose

### 1.1 Goal
Understand how Java source code becomes an executable program, establish a reliable development environment, and master command-line JDK tooling.

### 1.2 Context in Backend Engineering
Backend systems run on headless Linux servers, in Docker containers, and via CI/CD pipelines where graphical IDEs do not exist. Mastering the compiler, JVM runtime, classpath, and environment configuration is mandatory for debugging deployment and runtime failures.

---

## 2. Prerequisites and Co-requisites

- **Direct Prerequisites:**
  - None (Foundational Entry Phase).
- **Downstream Dependent Phases:**
  - Phase 01 (Language Fundamentals), Phase 08 (JVM Internals), Phase 10 (Build Workflow).

---

## 3. Measurable Phase Learning Outcomes

By completing Phase 00, the learner will independently be able to:
1. Distinguish between JDK, JRE, and JVM components and understand the post-Java 9 modular runtime.
2. Compile and execute Java programs using command-line javac and java with classpath specifications.
3. Explain source-file execution (JEP 330) versus traditional compile-then-launch workflows.
4. Configure JAVA_HOME, PATH, and evaluate JDK version compatibility for backend environments.


---

## 4. Concept Inventory and Study Order

| Concept ID | Concept Title | File Name | Importance | Status |
|---|---|---|---|---|
| `P00-C001` | JDK vs JRE vs JVM Architecture and Evolution | [`001-jdk-jre-jvm.md`](001-jdk-jre-jvm.md) | Essential | COMPLETE |
| `P00-C002` | Java Compiler (javac) vs Launcher (java) Workflow | [`002-javac-compiler-and-launcher.md`](002-javac-compiler-and-launcher.md) | Essential | COMPLETE |
| `P00-C003` | Source Files (.java) and Bytecode (.class) Disassembly | [`003-source-files-and-bytecode.md`](003-source-files-and-bytecode.md) | Essential | COMPLETE |
| `P00-C004` | Compilation, Execution, and Classpath Mechanics | [`004-classpath-and-package-structure.md`](004-classpath-and-package-structure.md) | Essential | COMPLETE |
| `P00-C005` | Java Entry Point: public static void main Signature and Evolution | [`005-entry-point-main-method.md`](005-entry-point-main-method.md) | Essential | COMPLETE |
| `P00-C006` | Single-File Source-Code Launching and Shebang Scripts | [`006-single-file-source-execution.md`](006-single-file-source-execution.md) | Recommended | COMPLETE |
| `P00-C007` | Environment Variables: JAVA_HOME and PATH in Linux/Production | [`007-environment-variables-java-home-path.md`](007-environment-variables-java-home-path.md) | Essential | COMPLETE |
| `P00-C008` | JDK Versions, Release Cadence, LTS, and Bytecode Compatibility | [`008-jdk-versions-lts-compatibility.md`](008-jdk-versions-lts-compatibility.md) | Essential | COMPLETE |
| `P00-C009` | IDEs vs Headless Terminal Development in Server Environments | [`009-ide-vs-terminal-development.md`](009-ide-vs-terminal-development.md) | Recommended | NOT_STARTED |
| `P00-C010` | Maven Fundamentals and Standard Directory Layout | [`010-maven-fundamentals-project-structure.md`](010-maven-fundamentals-project-structure.md) | Essential | NOT_STARTED |
| `P00-C011` | Git Hygiene and .gitignore for Java/Maven Projects | [`011-git-basics-for-java-developers.md`](011-git-basics-for-java-developers.md) | Essential | NOT_STARTED |


### Recommended Pedagogical Sequence
```mermaid
flowchart LR
    P00C001["P00-C001: JDK vs JRE vs JVM Archite..."] --> P00C002["P00-C002: Java Compiler (javac) vs ..."]
    P00C002["P00-C002: Java Compiler (javac) vs ..."] --> P00C003["P00-C003: Source Files (.java) and ..."]
    P00C003["P00-C003: Source Files (.java) and ..."] --> P00C004["P00-C004: Compilation, Execution, a..."]
    P00C004["P00-C004: Compilation, Execution, a..."] --> P00C005["P00-C005: Java Entry Point: public ..."]
    P00C005["P00-C005: Java Entry Point: public ..."] --> P00C006["P00-C006: Single-File Source-Code L..."]
    P00C006["P00-C006: Single-File Source-Code L..."] --> P00C007["P00-C007: Environment Variables: JA..."]
    P00C007["P00-C007: Environment Variables: JA..."] --> P00C008["P00-C008: JDK Versions, Release Cad..."]
    P00C008["P00-C008: JDK Versions, Release Cad..."] --> P00C009["P00-C009: IDEs vs Headless Terminal..."]
    P00C009["P00-C009: IDEs vs Headless Terminal..."] --> P00C010["P00-C010: Maven Fundamentals and St..."]
    P00C010["P00-C010: Maven Fundamentals and St..."] --> P00C011["P00-C011: Git Hygiene and .gitignor..."]

```

---

## 5. Critical Distinctions and Common Misconceptions

| Misconception / Simplification | Technical Reality from First Principles |
|---|---|
| "You need a standalone JRE to run Java applications in modern production." | Since Java 9, standalone JRE distributions are deprecated and removed. Modern production builds use custom runtime images via jlink or package a full JDK / container base image. |
| "Java is purely an interpreted language." | Java is compiled to platform-independent bytecode by javac, which is then interpreted and dynamically compiled to native machine code by HotSpot JIT (C1/C2) compilers at runtime. |


---

## 6. Phase Integration Project / Capstone Exercise

### Scenario Overview
Build, compile, package, and execute a multi-package command-line application purely using bash, javac, jar, and java commands without an IDE, inspecting the generated bytecode with javap.

### Requirements and Acceptance Criteria
- [ ] Requirements fully implemented and demonstrated with executable Java code.
- [ ] Zero unhandled exceptions or edge-case crashes.
- [ ] Verification through automated tests and terminal execution.

---

## 7. Phase Completion Gate and Self-Assessment

Before marking Phase 00 as complete:
- [ ] All concept modules in this phase are thoroughly studied and exercises completed.
- [ ] You can explain every concept in the table from memory without AI assistance.
- [ ] The capstone exercise compiles, passes automated tests, and handles edge cases.
- [ ] Common failure modes can be diagnosed and resolved using standard tooling.

---

## 8. Status and Progress Reconciler

- **Total Planned Concepts:** `11`
- **Completed Concepts:** `8`
- **In Progress:** `0`
- **Completion Rate:** `72.7%`
- **Last Verified Date:** `2026-10-29`
