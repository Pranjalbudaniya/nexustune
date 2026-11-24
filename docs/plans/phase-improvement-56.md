# Post-v1.0 Improvement Loop 56: Mid/Side (M/S) Stereo Soundstage Widener & Haas Spatializer Engine

## 1. Objectives & Overview
Implement an acoustic soundstage manipulation and spatial stereo widening studio in NexusTune (`StereoWidenerEngine`):
1. **Mid/Side Matrix Transformation**:
   - Deconstruct stereo channels into Mid (mono/center) and Side (stereo difference/ambience) components:
     $$M = \frac{L + R}{\sqrt{2}}, \quad S = \frac{L - R}{\sqrt{2}}$$
   - Reconstruct widened or focused stereo with variable width coefficient $W \in [0.0, 2.0]$:
     $$L' = \frac{M + W \cdot S}{\sqrt{2}}, \quad R' = \frac{M - W \cdot S}{\sqrt{2}}$$
2. **Haas Effect Acoustic Psychoacoustics**:
   - Model micro-delays (0ms to 20ms) between stereo channels simulating natural acoustic precedence and enlarged room perception without comb-filtering.
3. **Stereo Phase Correlation & Mono Compatibility Guard**:
   - Calculate phase correlation $r \in [-1.0, +1.0]$ between stereo channels to prevent destructive phase cancellation when played on mono bluetooth speakers or smartphones.
4. **Interactive Settings & Soundstage Studio**:
   - Preset chips (`MONO`, `NARROW`, `NATURAL`, `EXPANDED`, `SUPER_WIDE`, `MID_VOCAL`, `SIDE_AMBIENT`), custom width percentage slider (0% - 200%), Haas delay selector, and DataStore persistence.

## 2. Architecture & Components
- `:core:model`:
  - `StereoWidthMode` enum
  - `StereoWidthConfig` data class
  - `StereoPhaseRating` data class
- `:core:domain`:
  - `StereoWidenerEngine` object in `com.nexus.tune.core.domain.audio`:
    - `calculateStereoGains(widthPercent: Int, midGainDb: Float, sideGainDb: Float)`
    - `calculatePhaseCorrelation(leftSamples: FloatArray, rightSamples: FloatArray)`
    - `evaluatePhaseRating(correlation: Float)`
    - `calculateHaasDelaySamples(delayMs: Float, sampleRate: Int)`
    - `getPresetConfig(mode: StereoWidthMode)`
- `:core:datastore`:
  - Keys in `SettingsDataStore`: `STEREO_WIDTH_ENABLED`, `STEREO_WIDTH_MODE`, `STEREO_WIDTH_PERCENT`, `STEREO_HAAS_DELAY_MS`.
  - Mapped into `ThemeConfig`.
- `:feature:settings`:
  - `SettingsViewModel` methods: `setStereoWidthEnabled`, `setStereoWidthMode`, `setStereoWidthPercent`, `setStereoHaasDelayMs`.
  - Interactive "Stereo Field & Soundstage Studio" card in `SettingsScreen`.
- Unit Tests:
  - `StereoWidenerEngineTest` in `:core:domain`
  - `SettingsViewModelTest` in `:feature:settings`

## 3. Verification Criteria
- `./scripts/gate.sh slice` passes with 100% test pass rate.
- `./scripts/gate.sh final` passes with release APK compilation.
- Tagged `v6.6-1`.

