# Phase 0 Plan: Environment Check

## Objective
Verify the build environment: JDK, Android SDK, Gradle wrapper, git repository, network reachability, and ADB device availability. Ensure all toolchains are operational and create the initial git repository commit.

## Tasks & Owners
| Task | Description | Owner | Acceptance Check |
|---|---|---|---|
| T0.1 | Git repo init & .gitignore configuration | Lead | `git status` clean, branch `main`, proper ignores |
| T0.2 | Android SDK & Java 21 toolchain validation | Lead | Java 21, Android SDK platform 35/36 found |
| T0.3 | Gradle wrapper setup | Lead | `./gradlew --version` executes cleanly |
| T0.4 | Network reachability validation | Lead | Maven Central, Google Maven, JitPack reachable |
| T0.5 | Device availability check & docs | Lead | `adb devices -l` checked, `docs/DEVICE.md` and `docs/BLOCKERS.md` recorded |
| T0.6 | Initial gate script (`scripts/gate.sh`) | Lead | `scripts/gate.sh env` executes and passes |

## Acceptance Criteria
- `scripts/gate.sh env` exits 0 with all checks printing PASS or SKIPPED(no device).
