# Post-v1.0 Improvement Loop 64: Stereo Phase Correlation & Mono Compatibility Guard Studio

## 1. Objectives & Overview
Implement an intelligent stereo phase correlation evaluation and mono protection engine (`PhaseMeterEngine`):
1. **Mathematical Pearson Correlation & Phase Coherence DSP**:
   - Compute real-time cross-channel Pearson correlation coefficient $r \in [-1.0, +1.0]$ between Left and Right stereo channels.
   - Detect destructive phase inversion and acoustic cancellation ($r < 0.0$) where vocals and bass vanish when summed to mono on smartphone speakers or single Bluetooth drivers.
2. **Dynamic Mono-Guard Anti-Cancellation DSP**:
   - Dynamically attenuate side-channel ($S = \frac{L-R}{2}$) energy when correlation dips into negative territory, preserving punch and dialogue clarity without sacrificing wide stereo image when in-phase.
3. **Guard Modes**:
   - `OFF`: Unrestricted stereo playback.
   - `INTELLIGENT_SAFEGUARD`: Smooth progressive side-channel reduction only when correlation drops below +0.15.
   - `STRICT_MONO_COMPLIANCE`: Strict broadcast compliance maintaining correlation $\ge +0.50$ at all times.
4. **DataStore Persistence & UI**:
   - Keys in `SettingsDataStore` mapped to `ThemeConfig`.
   - Interactive studio card in `SettingsScreen` with live phase correlation rating meter, mono safety status badge, and guard mode chips.

## 2. Architecture & Components
- `:core:model`:
  - `PhaseCorrelationRating` enum
  - `MonoGuardMode` enum
  - `PhaseMeterConfig` data class
  - `PhaseCorrelationMetrics` data class
- `:core:domain`:
  - `PhaseMeterEngine` in `com.nexus.tune.core.domain.audio`:
    - `calculatePearsonCorrelation(left: FloatArray, right: FloatArray): Float`
    - `evaluatePhaseRating(correlation: Float): PhaseCorrelationRating`
    - `calculateMonoGuardSideAttenuationDb(correlation: Float, guardMode: MonoGuardMode): Float`
    - `processPhaseSafeFrame(left: FloatArray, right: FloatArray, guardMode: MonoGuardMode): Pair<FloatArray, FloatArray>`
- `:core:datastore`:
  - `SettingsDataStore`: keys `PHASE_GUARD_ENABLED`, `PHASE_GUARD_MODE`, `PHASE_GUARD_THRESHOLD`.
- `:feature:settings`:
  - `SettingsViewModel` control methods.
  - Interactive "Stereo Phase Correlation & Mono Compatibility Guard" card in `SettingsScreen`.
- Unit tests:
  - `PhaseMeterEngineTest` in `:core:domain`
  - `SettingsViewModelTest` in `:feature:settings`

## 3. Verification Criteria
- `./scripts/gate.sh slice` passes with 100% test pass rate.
- `./scripts/gate.sh final` passes with release APK build.
- Tagged `v6.14-1`.

