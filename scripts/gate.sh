#!/usr/bin/env bash
set -euo pipefail

PHASE="${1:-env}"

echo "===================================================="
echo "          NexusTune Quality Gate: $PHASE"
echo "===================================================="

fail() {
    echo "FAIL: $1" >&2
    exit 1
}

pass() {
    echo "PASS: $1"
}

skip() {
    echo "SKIPPED($2): $1"
}

check_env() {
    echo "--- Checking Environment ---"
    # JDK
    if command -v java >/dev/null 2>&1; then
        JAVA_VER=$(java -version 2>&1 | head -n 1)
        pass "Java installed ($JAVA_VER)"
    else
        fail "Java not found in PATH"
    fi

    # Android SDK
    ANDROID_SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/home/pranjal/Android/Sdk}}"
    if [ -d "$ANDROID_SDK/platforms" ]; then
        pass "Android SDK found at $ANDROID_SDK"
    else
        fail "Android SDK not found at $ANDROID_SDK"
    fi

    # Git
    if git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
        pass "Git repository initialized"
    else
        fail "Not inside a git repository"
    fi

    # Network
    if curl -s --connect-timeout 5 -I https://repo.maven.apache.org >/dev/null 2>&1; then
        pass "Maven Central reachable"
    else
        fail "Maven Central unreachable"
    fi

    if curl -s --connect-timeout 5 -I https://jitpack.io >/dev/null 2>&1; then
        pass "JitPack reachable"
    else
        fail "JitPack unreachable"
    fi

    # Device
    DEVICE_COUNT=$(adb devices 2>/dev/null | grep -v "List of devices" | grep -v "^$" | wc -l || echo 0)
    if [ "$DEVICE_COUNT" -gt 0 ]; then
        pass "Device attached ($DEVICE_COUNT device(s))"
    else
        skip "ADB device" "no device"
    fi
}

check_research() {
    echo "--- Checking Phase 1 Research Outputs ---"
    [ -f "docs/UI_REFERENCES.md" ] || fail "Missing docs/UI_REFERENCES.md"
    [ -f "docs/EXTRACTOR_CAPABILITIES.md" ] || fail "Missing docs/EXTRACTOR_CAPABILITIES.md"
    pass "Phase 1 research docs present"
}

check_council() {
    echo "--- Checking Phase 2 Council Outputs ---"
    [ -f "docs/DECISIONS.md" ] || fail "Missing docs/DECISIONS.md"
    [ -f "docs/RANKED_FEATURES.md" ] || fail "Missing docs/RANKED_FEATURES.md"
    [ -f "docs/DESIGN_LANGUAGE.md" ] || fail "Missing docs/DESIGN_LANGUAGE.md"
    pass "Phase 2 council outputs present"
}

check_scaffold() {
    echo "--- Checking Phase 3 Scaffold & Architecture ---"
    [ -f "docs/ARCHITECTURE.md" ] || fail "Missing docs/ARCHITECTURE.md"
    [ -f "AGENTS.md" ] || fail "Missing AGENTS.md"
    [ -x "./gradlew" ] || fail "./gradlew not executable or missing"
    
    echo "Compiling and running static checks..."
    ./gradlew --no-daemon assembleDebug testDebugUnitTest
    pass "Scaffold builds and compiles successfully"
}

check_slice() {
    echo "--- Checking Vertical Slice Tests ---"
    ./gradlew --no-daemon testDebugUnitTest
    pass "All JVM unit and Robolectric tests pass"
}

check_perf() {
    echo "--- Checking Performance Benchmarks ---"
    DEVICE_COUNT=$(adb devices 2>/dev/null | grep -v "List of devices" | grep -v "^$" | wc -l || echo 0)
    if [ "$DEVICE_COUNT" -gt 0 ]; then
        echo "Running on-device benchmark..."
        ./gradlew --no-daemon :benchmark:connectedCheck || fail "Benchmark tests failed on device"
        pass "Macrobenchmarks completed on device"
    else
        skip "Macrobenchmarks and 120Hz frame timing" "no device"
    fi
}

check_final() {
    echo "--- Checking Final Release Readiness ---"
    check_env
    check_research
    check_council
    [ -f "docs/ARCHITECTURE.md" ] || fail "Missing ARCHITECTURE.md"
    [ -f "docs/DESIGN_LANGUAGE.md" ] || fail "Missing DESIGN_LANGUAGE.md"
    [ -f "docs/TEST_REPORT.md" ] || fail "Missing TEST_REPORT.md"
    [ -f "docs/PERFORMANCE_REPORT.md" ] || fail "Missing PERFORMANCE_REPORT.md"
    [ -f "docs/MANUAL_QA.md" ] || fail "Missing MANUAL_QA.md"
    [ -f "docs/UNVERIFIED.md" ] || fail "Missing UNVERIFIED.md"
    [ -f "docs/PROGRESS.md" ] || fail "Missing PROGRESS.md"
    [ -f "docs/CHANGELOG.md" ] || fail "Missing CHANGELOG.md"
    [ -f "docs/LICENSES.md" ] || fail "Missing LICENSES.md"
    [ -f "README.md" ] || fail "Missing README.md"
    [ -f "AGENTS.md" ] || fail "Missing AGENTS.md"

    ./gradlew --no-daemon testDebugUnitTest assembleRelease
    pass "Final gate passed: tests and release APK built successfully"
}

case "$PHASE" in
    env|phase-0)
        check_env
        ;;
    research|phase-1)
        check_research
        ;;
    council|phase-2)
        check_council
        ;;
    scaffold|phase-3)
        check_scaffold
        ;;
    slice|phase-4)
        check_slice
        ;;
    perf|phase-7)
        check_perf
        ;;
    final|phase-9)
        check_final
        ;;
    *)
        echo "Unknown phase: $PHASE. Running env checks."
        check_env
        ;;
esac

echo "===================================================="
echo "          Gate check PASSED for: $PHASE"
echo "===================================================="
exit 0
