#!/usr/bin/env bash
# =============================================================================
# test-build.sh  –  Compile and run unit tests
# =============================================================================
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== SIS – Unit Tests ==="

# ── Validate test JAR ─────────────────────────────────────────────────────────
JUNIT_JAR="lib/test/junit-platform-console-standalone-1.10.2.jar"
if [ ! -f "$JUNIT_JAR" ]; then
    echo "ERROR: $JUNIT_JAR not found."
    echo "  Download from:"
    echo "  https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar"
    echo "  Place it in:  sis/lib/test/"
    exit 1
fi

MAIN_CP="lib/sqlite-jdbc-3.45.3.0.jar:lib/slf4j-api-1.7.36.jar:lib/logback-classic-1.2.12.jar:lib/logback-core-1.2.12.jar"
CP="$MAIN_CP:$JUNIT_JAR"

# ── Compile ───────────────────────────────────────────────────────────────────
echo "[1/3] Compiling main sources..."
rm -rf out-test && mkdir -p out-test
find src/main/java -name "*.java" > sources-test.txt
javac -cp "$CP" -d out-test @sources-test.txt

echo "[2/3] Compiling test sources..."
find src/test/java -name "*.java" >> sources-test.txt
javac -cp "out-test:$CP" -d out-test $(find src/test/java -name "*.java")
rm sources-test.txt

# ── Run ───────────────────────────────────────────────────────────────────────
echo "[3/3] Running tests..."
echo ""
java -cp "out-test:$CP" \
  org.junit.platform.console.standalone.ConsoleLauncher \
  --scan-class-path=out-test \
  --include-package=com.sis.service \
  --details=verbose
