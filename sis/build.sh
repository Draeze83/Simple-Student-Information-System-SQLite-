#!/usr/bin/env bash
# =============================================================================
#  build.sh  –  Student Information System (Linux/macOS build wrapper)
#  Delegates all compilation and packaging to Maven.
#  Usage:
#    ./build.sh              # compile + test + package
#    ./build.sh -DskipTests  # skip tests (faster)
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== Student Information System – Maven Build (Linux/macOS) ==="

# ── Locate mvn or ./mvnw ─────────────────────────────────────────────────────
if command -v mvn &>/dev/null; then
    MVN="mvn"
elif [ -x "./mvnw" ]; then
    MVN="./mvnw"
else
    echo "ERROR: Maven (mvn) not found on PATH and no ./mvnw wrapper present."
    echo "       Install Maven: https://maven.apache.org/download.cgi"
    echo "       Or on Debian/Ubuntu: sudo apt install maven"
    exit 1
fi

echo "Using: $MVN"
echo ""

$MVN clean package "$@"

echo ""
echo "Build complete!"
echo "  JAR:              target/student-information-system-1.0.0-shaded.jar"
echo "  Run:              java -jar target/student-information-system-1.0.0-shaded.jar"
echo "  Override DB path: java -Ddb.path=/data/sis.db -jar target/student-information-system-1.0.0-shaded.jar"
echo "  Logs directory:   ./logs/sis.log  (created on first run)"
