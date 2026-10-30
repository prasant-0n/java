#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="${SCRIPT_DIR}/src"
OUT_DIR="${SCRIPT_DIR}/out"
JAR_FILE="${SCRIPT_DIR}/streaming-etl-engine-1.0.0.jar"

echo "=== Building Phase 07 Capstone: Streaming High-Performance ETL Engine ==="

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
    --main-class com.example.etl.test.EtlEngineIntegrationTest \
    -C "${OUT_DIR}" .

# 3. Execute the Standalone Verification Suite under strict 64MB heap ceiling
echo "Executing test suite from standalone JAR under 64MB heap limit (-Xmx64m)..."
java -Xmx64m -ea -jar "${JAR_FILE}"

# 4. Clean temporary compiler output directory to preserve repository hygiene
rm -rf "${OUT_DIR}"

echo "=== Phase 07 Capstone Build and Verification Complete! ==="
