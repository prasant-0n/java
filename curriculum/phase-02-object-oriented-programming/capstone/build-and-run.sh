#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="${SCRIPT_DIR}/src"
BIN_DIR="${SCRIPT_DIR}/bin"
JAR_NAME="banking-domain-engine-1.0.0.jar"
MAIN_CLASS="com.example.banking.test.BankingEngineIntegrationTest"

echo "================================================================="
echo "Building Phase 02 Capstone: Banking Domain Engine"
echo "Target Platform: OpenJDK 21 LTS"
echo "================================================================="

rm -rf "${BIN_DIR}"
mkdir -p "${BIN_DIR}"

echo "[1/3] Compiling Java 21 sources with strict parameters..."
find "${SRC_DIR}" -name "*.java" > "${BIN_DIR}/sources.txt"
javac --release 21 \
      -encoding UTF-8 \
      -parameters \
      -d "${BIN_DIR}" \
      @"${BIN_DIR}/sources.txt"

echo "[2/3] Executing automated regression test suite..."
java -ea \
     -cp "${BIN_DIR}" \
     "${MAIN_CLASS}"

echo "[3/3] Packaging standalone executable JAR..."
jar --create \
    --file "${SCRIPT_DIR}/${JAR_NAME}" \
    --main-class "${MAIN_CLASS}" \
    -C "${BIN_DIR}" .

echo "================================================================="
echo "BUILD & VERIFICATION SUCCESSFUL"
echo "Executable Artifact: ${SCRIPT_DIR}/${JAR_NAME}"
echo "================================================================="
