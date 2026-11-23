# Unverified Items and Verification Plan

Per BRIEF.md Section 0 (Rule 4), no claim is made without empirical verification.

## On-Device Verification Plan (Device: V2518, Android 16, Serial: 10BF7Q150G000LE)
With the physical device attached, the following tests will be executed and measured in Phase 7 and final gates:
1. **Frame-Time Targets (8.33 ms / 120 Hz or 11.1 ms / 90 Hz or 16.6 ms / 60 Hz)**:
   - Measured via `Macrobenchmark` with `FrameTimingMetric` and `JankStats`.
   - Verified via `scripts/bench-120hz.sh` and recorded in `docs/PERFORMANCE_REPORT.md`.
2. **Cold Startup & Transition Performance**:
   - Time to initial display and morph animations measured via Macrobenchmark on device.
3. **Hardware Audio Session & Focus**:
   - MediaSessionService playback, lockscreen controls, and audio focus transitions validated on device.
