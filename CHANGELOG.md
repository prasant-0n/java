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
