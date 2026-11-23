# Phase 7 Plan: Performance & On-Device Profiling

## 1. Overview & Objectives
Phase 7 benchmarks NexusTune on physical hardware (Vivo V2518 / MTK, Android 16, 120Hz display, Serial `10BF7Q150G000LE`) to verify:
- Cold startup time <= 800ms (target: <= 500ms).
- 120Hz frame rendering timing during continuous scroll and player animations:
  - 120Hz target: P50 <= 4ms, P90 <= 6ms, P99 <= 8.33ms (zero dropped frames).
- Memory footprint: steady state <= 150MB PSS, zero unbounded growth during extended playback.
- Execution of `./scripts/gate.sh perf`.

## 2. Execution Tasks
1. Assemble debug APK: `./gradlew assembleDebug`.
2. Install onto physical device `10BF7Q150G000LE`:
   `adb -s 10BF7Q150G000LE install -r app/build/outputs/apk/debug/app-debug.apk`.
3. Launch app and warm up ART JIT/profile:
   `adb -s 10BF7Q150G000LE shell am start -W -n com.nexus.tune/.MainActivity`.
4. Measure cold startup times across 3 consecutive trials.
5. Exercise UI scroll and animations, profile frame rendering times using `dumpsys gfxinfo com.nexus.tune`.
6. Inspect PSS memory distribution via `dumpsys meminfo com.nexus.tune`.
7. Generate `docs/PERFORMANCE_REPORT.md` detailing measured hardware metrics.
8. Execute `./scripts/gate.sh perf`.

## 3. Acceptance Checks
- `docs/PERFORMANCE_REPORT.md` populated with real on-device data.
- `./scripts/gate.sh perf` passes.
