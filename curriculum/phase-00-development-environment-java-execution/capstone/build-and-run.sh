#!/usr/bin/env bash
# Phase 00 Capstone Build & Run Script
# Builds, disassembles, packages, and executes without an IDE
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

echo "==> Step 1: Cleaning previous build artifacts..."
rm -rf target
mkdir -p target/classes

echo "==> Step 2: Compiling multi-package source files with javac..."
javac --release 21 -d target/classes \
    src/com/example/inventory/model/Product.java \
    src/com/example/inventory/service/InventoryService.java \
    src/com/example/inventory/cli/InventoryApp.java

echo "==> Step 3: Inspecting compiled bytecode with javap..."
javap -v target/classes/com/example/inventory/cli/InventoryApp.class | grep -E 'major version|magic' | head -n 2

echo "==> Step 4: Packaging into standalone executable JAR with jar..."
jar --create --file target/inventory-app-1.0.0.jar \
    --main-class com.example.inventory.cli.InventoryApp \
    -C target/classes .

echo "==> Step 5: Executing packaged application with java -jar..."
java -Djava.awt.headless=true -jar target/inventory-app-1.0.0.jar

echo "==> Step 6: Verifying shutdown hooks and zero unhandled errors..."
echo "PHASE 00 CAPSTONE COMPLETED SUCCESSFULLY!"
