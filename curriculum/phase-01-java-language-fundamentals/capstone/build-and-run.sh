#!/usr/bin/env bash
set -euo pipefail

echo "================================================================================"
echo "      BUILD & RUN PIPELINE: PHASE 01 BILLING ENGINE CAPSTONE                  "
echo "================================================================================"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

BIN_DIR="bin"
DIST_DIR="dist"
JAR_NAME="billing-engine-1.0.0.jar"

echo "[1/4] Cleaning build artifacts..."
rm -rf "${BIN_DIR}" "${DIST_DIR}"
mkdir -p "${BIN_DIR}" "${DIST_DIR}"

echo "[2/4] Compiling Java source files with OpenJDK 21 LTS..."
find src -name "*.java" > sources.txt
javac --release 21 -d "${BIN_DIR}" @sources.txt
rm sources.txt
echo "       -> Compilation successful."

echo "[3/4] Running standalone regression test suite..."
java -ea -cp "${BIN_DIR}" com.example.billing.test.BillingEngineTest
echo "       -> All tests passed successfully."

echo "[4/4] Packaging executable JAR..."
jar --create --file "${DIST_DIR}/${JAR_NAME}" --main-class com.example.billing.cli.BillingApp -C "${BIN_DIR}" .
echo "       -> Created ${DIST_DIR}/${JAR_NAME}"

echo ""
echo "================================================================================"
echo "                     EXECUTING PACKAGED STANDALONE JAR                          "
echo "================================================================================"
java -jar "${DIST_DIR}/${JAR_NAME}"

echo ""
echo "================================================================================"
echo "             PHASE 01 CAPSTONE PIPELINE COMPLETED SUCCESSFULLY                  "
echo "================================================================================"
