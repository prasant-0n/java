# Changelog

All notable changes to the Java Backend Engineering Concept Library will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to strict evidence-based curriculum versioning.

---

## [0.1.0] - 2026-10-29

### Added
- **Repository Initialization:** Initialized Git repository tracking in `/home/prasanta/java_practice`.
- **Core Governance & Templates:**
  - `templates/CONCEPT_TEMPLATE.md`: 16-section standard concept specification.
  - `templates/PHASE_README_TEMPLATE.md`: Standard navigation and capstone structure.
  - `templates/RESEARCH_AND_CITATION_RULES.md`: Authority hierarchy and verification rules.
- **Phase Directories & Phase READMEs:**
  - Established all 24 canonical phase directories (`curriculum/phase-00-...` to `curriculum/phase-23-...`).
  - Generated comprehensive `README.md` for each of the 24 phases with learning outcomes, sequence flows, misconceptions, and capstones.
- **Supplemental Tracks:**
  - Created 6 specialization tracks under `supplemental/` with index and concept inventory.
- **Curriculum Architecture & Indexes:**
  - `README.md`: Central library portal and study guide.
  - `MASTER_INDEX.md`: Global clickable index.
  - `LEARNING_PATH.md`: Four-tier study sequence and 100-project readiness gates.
  - `CONCEPT_INVENTORY.md`: 312 planned concept modules across core and supplemental tracks.
  - `ROADMAP_TRACEABILITY.md`: 712 source roadmap checklist items mapped to concept modules.
  - `MISSING_TOPICS_AUDIT.md`: Reconciliation of 12 vs 24 phase discrepancy and gap analysis across 11 master engineering domains.
  - `PHASE_DEPENDENCIES.md`: Prerequisite DAG and cycle-free topological matrix.
  - `PROGRESS.md`: Granular status and queue tracker.
  - `BUILD_STATUS.md`: Environment verification and build logs.
  - `SOURCE_REGISTER.md`: Register of primary specifications (JLS, JVMS, JEPs, RFCs).
  - `GLOSSARY.md`: Deduplicated first-principles technical glossary.
- **First Concept Module Authored & Verified:**
  - `curriculum/phase-00-development-environment-java-execution/001-jdk-jre-jvm.md`: Complete 16-section concept module covering JDK vs JRE vs JVM architecture, history, modularity, containerization, and diagnostic CLI tools.
  - Runtime verification test executed on OpenJDK 21 LTS (`exit code 0`).
- **Second Concept Module Authored & Verified:**
  - `curriculum/phase-00-development-environment-java-execution/002-javac-compiler-and-launcher.md`: Complete 16-section concept module covering the `javac` compilation pipeline (Parse, Enter, Annotate, Attribute, Flow, Desugar, Gen), the `java` launcher bootstrap sequence, `-parameters` for Spring Boot, single-file source execution (JEP 330), and programmatic compiler testing.
  - Multi-package compilation and execution verified on OpenJDK 21 LTS (`exit code 0`).
- **Third Concept Module Authored & Verified:**
  - `curriculum/phase-00-development-environment-java-execution/003-source-files-and-bytecode.md`: Complete 16-section concept module covering binary `.class` file structure, `0xCAFEBABE` magic number, Constant Pool relational architecture, stack frame execution (`max_stack`, `max_locals`), instruction-by-instruction operand stack trace, constant inlining binary incompatibility traps, and `javap` disassembly.
  - Programmatic binary inspection test verified on OpenJDK 21 LTS (`exit code 0`).
- **Fourth Concept Module Authored & Verified:**
  - `curriculum/phase-00-development-environment-java-execution/004-classpath-and-package-structure.md`: Complete 16-section concept module covering package namespaces, the 1:1 FQCN-to-path invariant, `-classpath` / `-cp` parameter behavior, JAR archives, wildcard expansion rules (`lib/*`), the three-tier Parent Delegation Model (`Bootstrap` $\to$ `Platform` $\to$ `App`), first-match-wins JAR Hell, and diagnosing `ClassNotFoundException` vs `NoClassDefFoundError`.
  - Multi-module library packaging and static initialization failure reproduction verified on OpenJDK 21 LTS (`exit code 0`).
- **Fifth Concept Module Authored & Verified:**
  - `curriculum/phase-00-development-environment-java-execution/005-entry-point-main-method.md`: Complete 16-section concept module covering `public static void main` keyword-by-keyword deconstruction, native JNI bootstrap sequence, CLI argument heap allocation, thread lifecycle (daemon vs non-daemon threads), JVM shutdown hooks, process exit codes (POSIX standards), and modern Java 21–25 evolution (Instance Main Methods & Unnamed Classes via JEP 445/463/477).
  - Thread lifecycle and shutdown hook execution verified on OpenJDK 21 LTS (`exit code 0`).
- **Sixth Concept Module Authored & Verified:**
  - `curriculum/phase-00-development-environment-java-execution/006-single-file-source-execution.md`: Complete 16-section concept module covering JEP 330 Single-File Source-Code Execution, in-memory compilation without `.class` generation on disk, Linux executable shebang scripts (`#!/usr/bin/env -S java --source 21`), the "First-Class" Rule, CLI argument demarcation (`--`), external JAR classpath resolution (`-cp`), and replacing fragile shell scripts in backend automation.
  - Subprocess single-file execution and Linux shebang script execution verified on OpenJDK 21 LTS (`exit code 0`).
- **Seventh Concept Module Authored & Verified:**
  - `curriculum/phase-00-development-environment-java-execution/007-environment-variables-java-home-path.md`: Complete 16-section concept module covering POSIX process environment inheritance (`fork`/`execve`, `/proc/$PID/environ`), `$PATH` binary search algorithm, `$JAVA_HOME` OpenJDK directory specification, Debian/Ubuntu `update-alternatives` symlink architecture, systemd service unit environment isolation (`Environment=`), containerized Dockerfile patterns, and eliminating version drift between Maven and CLI launcher.
  - Subprocess Java environment auditor and bash canonical symlink resolver verified on OpenJDK 21 LTS (`exit code 0`).
- **Eighth Concept Module Authored & Verified:**
  - `curriculum/phase-00-development-environment-java-execution/008-jdk-versions-lts-compatibility.md`: Complete 16-section concept module covering time-driven 6-month release cadence, LTS policy (Java 8, 11, 17, 21, 25), class file major version binary math ($44 + Version$), asymmetric compatibility guarantee, binary root cause of `UnsupportedClassVersionError`, compile-time API guarding with `javac --release N` (JEP 247) and `lib/ct.sym`, Preview Features (`0xFFFF`), and enterprise migration roadmaps.
  - Subprocess classfile binary header inspector and `--release` cross-compilation verified on OpenJDK 21 LTS (`exit code 0`).
- **Ninth Concept Module Authored & Verified:**
  - `curriculum/phase-00-development-environment-java-execution/009-ide-vs-terminal-development.md`: Complete 16-section concept module covering IDE internal PSI models vs deterministic CLI build engines, headless Linux server realities (`$DISPLAY` unset, `java.awt.headless=true`), classpath scope leakage traps, HotSpot Attach API internals (`/tmp/.java_pid<PID>`), JDK CLI diagnostic suite (`jcmd`, `jstack`, `jmap`, `jstat`), command-line interactive debugging with `jdb`, and secure JDWP remote debugging over SSH tunnels.
  - Subprocess headless background daemon with live `jcmd` and `jstat` metrics sampling verified on OpenJDK 21 LTS (`exit code 0`).
- **Tenth Concept Module Authored & Verified:**
  - `curriculum/phase-00-development-environment-java-execution/010-maven-fundamentals-project-structure.md`: Complete 16-section concept module covering Convention over Configuration, the Project Object Model (`pom.xml`), GAV coordinates triad, the canonical Maven Standard Directory Layout (`src/main/java`, `src/main/resources`, `src/test/java`, `target/`), the three core lifecycles (`clean`, `default`, `site`), lifecycle phases vs plugin goals, dependency scopes (`compile`, `provided`, `runtime`, `test`, `system`, `import`), the Nearest Definition transitive collision rule, multi-stage Docker build caching, and the Maven Wrapper (`mvnw`).
  - Subprocess Maven standard layout verification and zero-IDE command-line packaging verified on OpenJDK 21 LTS (`exit code 0`).
- **Eleventh Concept Module Authored & Verified (Phase 00 Complete!):**
  - `curriculum/phase-00-development-environment-java-execution/011-git-basics-for-java-developers.md`: Complete 16-section concept module covering Git object architecture (blobs, trees, commits), delta compression mechanics and why binary commits destroy repository health, the canonical enterprise Java/Maven `.gitignore` specification, the Maven Wrapper exception rule (`!.mvn/wrapper/maven-wrapper.jar`), index untracking (`git rm --cached`), Conventional Commits 1.0.0, `.gitattributes` cross-platform line-ending normalization (`eol=lf`), and automated pre-commit quality hooks.
  - Subprocess repository hygiene auditor, ignore pattern matcher, and untrack simulation verified on OpenJDK 21 LTS (`exit code 0`).
- **Phase 00 Capstone Integration Project Completed & Phase Gate Reconciled:**
  - Multi-package command-line inventory system (`com.example.inventory`) established in `curriculum/phase-00-development-environment-java-execution/capstone/`.
  - Pure CLI compilation targeting Java 21 (`javac --release 21`), bytecode disassembly (`javap -v`), packaging into executable JAR (`jar --main-class`), and headless runtime execution (`java -Djava.awt.headless=true -jar`) verified cleanly with exit code 0.
  - Phase 00 marked `COMPLETE` (11/11 modules, 100%).
- **Twelfth Concept Module Authored & Verified (Phase 01 Launch):**
  - `curriculum/phase-01-java-language-fundamentals/001-primitive-data-types.md`: Complete 16-section concept module covering the 8 Java primitives, physical bit widths and memory layouts, Two's Complement signed integer mathematics, unsigned 16-bit `char` and UTF-16 code units, JVM `boolean` stack implementation, literal radices and the Octal trap, the 30-day millisecond silent overflow trap, Binary Numeric Promotion (`byte + byte = int`), Java 8+ unsigned integer utility APIs, and relational database primary key exhaustion prevention (`INT` vs `BIGINT`).
  - Subprocess binary visualizer and bitwise packer verified on OpenJDK 21 LTS (`exit code 0`).
- **Thirteenth Concept Module Authored & Verified:**
  - `curriculum/phase-01-java-language-fundamentals/002-floating-point-types-and-precision.md`: Complete 16-section concept module covering IEEE 754 floating-point standard, bit layouts of `float` (32-bit) and `double` (64-bit), biased exponents and normalized implicit leading 1, why decimal fractions like $0.1$ and $0.2$ are infinite repeating binary fractions, the proof of `0.1 + 0.2 != 0.3`, Machine Epsilon fuzzy comparisons, special values (`Infinity`, `NaN`, signed zeroes), the unordered nature of `NaN`, Java 17 JEP 306 restoration of always-strict semantics, the Golden Rule of Financial Computing, integer cents vs `BigDecimal`, and the `new BigDecimal(double)` constructor precision trap.
  - Subprocess IEEE 754 bit-field inspector, epsilon comparator, and Banker's Rounding (`HALF_EVEN`) verified on OpenJDK 21 LTS (`exit code 0`).
