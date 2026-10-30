#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="${SCRIPT_DIR}/src"
OUT_DIR="${SCRIPT_DIR}/out"
JAR_FILE="${SCRIPT_DIR}/analytical-order-aggregator-1.0.0.jar"

echo "=== Building Phase 06 Capstone: Real-Time Analytical Order & Fraud Aggregation Engine ==="

# Clean previous build artifacts
rm -rf "${OUT_DIR}" "${JAR_FILE}"
mkdir -p "${OUT_DIR}"

# 1. Compile all Java sources targeting Java 21
echo "Compiling Java 21 sources with javac..."
javac --release 21 \
      -parameters \
      -encoding UTF-8 \
      -Xlint:all \
      -Werror \
      -d "${OUT_DIR}" \
      $(find "${SRC_DIR}" -name "*.java")

# 2. Package into Standalone Executable JAR
echo "Packaging into executable JAR: ${JAR_FILE}..."
jar --create \
    --file "${JAR_FILE}" \
    --main-class com.example.analytics.test.AnalyticsEngineIntegrationTest \
    -C "${OUT_DIR}" .

# 3. Execute the Standalone Verification Suite
echo "Executing test suite from standalone JAR..."
java -ea -jar "${JAR_FILE}"

echo "=== Phase 06 Capstone Build and Verification Complete! ==="
