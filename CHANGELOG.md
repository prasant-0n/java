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
