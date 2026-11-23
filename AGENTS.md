# AGENTS.md: NexusTune Autonomous Agent System Directory

This document serves as the top-level index for the NexusTune multi-agent autonomous engineering system.

## 1. Project Identity
- **Application Name**: NexusTune
- **Application ID / Root Package**: `com.nexus.tune`
- **Platform**: Android 12+ (minSdk 31, targetSdk 36, compileSdk 36)
- **Primary Harness**: Antigravity Autonomous Orchestrator

## 2. Directory Navigation Map
- `docs/plans/`: Phase execution plans (`phase-0.md` through `phase-9.md`).
- `docs/council/`: Council proposals, cross-examinations, and voting logs (`scope/r[1-3]-*.md`).
- `docs/reviews/ui/`: UI review audit reports per screen (`docs/reviews/ui/*.md`).
- `docs/reviews/code/`: Code quality review audits (`docs/reviews/code/*.md`).
- `docs/bugs/`: Defect tracking files (`docs/bugs/BUG-###.md`).
- `docs/ARCHITECTURE.md`: Module architecture, data flow, and dependency boundaries.
- `docs/DECISIONS.md`: Architectural Decision Records (ADRs).
- `docs/RANKED_FEATURES.md`: Feature roadmap (v1 ship, v1.1 planned, cut).
- `docs/DESIGN_LANGUAGE.md`: Material 3 Expressive motion tokens, shape schemes, and typography.
- `docs/DEVICE.md`: Live hardware status, refresh modes, and adb serialization.
- `docs/PROGRESS.md`: Current execution phase, branch tracking, and resume checkpoints.
- `docs/BLOCKERS.md`: Environment impediments and mitigation strategies.
- `docs/UNVERIFIED.md`: Catalog of assertions awaiting physical verification.
- `docs/TEST_REPORT.md`: JVM (T1), Robolectric (T2), and device (T3) test results.
- `docs/PERFORMANCE_REPORT.md`: 120Hz frame timing, cold start, and memory profiles.
- `docs/MANUAL_QA.md`: Physical device verification checklist.
- `docs/LICENSES.md`: Open-source licenses for dependencies and fonts.

## 3. Verification & Execution Commands
```bash
# Verify environment and toolchains
./scripts/gate.sh env

# Verify research and council artifacts
./scripts/gate.sh research
./scripts/gate.sh council

# Compile all modules and execute unit tests
./scripts/gate.sh scaffold
./scripts/gate.sh slice

# Execute performance benchmarks (requires device)
./scripts/gate.sh perf

# Full release verification (all tests + release APK)
./scripts/gate.sh final
```

