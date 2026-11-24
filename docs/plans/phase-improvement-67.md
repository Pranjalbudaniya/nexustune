# Phase Improvement Loop 67: Dynamic Mid-Bass Mud Tamer & Clarity Sculptor Studio (v6.17.0)

## 1. Overview & Motivation
The 200 Hz to 450 Hz octave is universally known as the "mud zone" (spectral congestion pocket). Multiple instruments (kick body, bass guitar fundamentals, vocal chest resonance, acoustic guitar warmth, snare lower body) frequently clash here, producing a boomy, boxy, veiled, and fatigue-inducing acoustic signature on headphones and car speakers.

The **Dynamic Mid-Bass Mud Tamer & Clarity Sculptor Studio (`MidBassClarityEngine`)** remedies this in real time:
1. **Real-time Spectral Congestion Detector**: Continuously measures the localized energy ratio between the crowded mid-bass pocket ($220\text{ Hz} - 400\text{ Hz}$) and the reference broad spectrum.
2. **Dynamic Parametric Bell Dip Filter**: When congestion exceeds the calibrated threshold, it applies an inverted parametric bell attenuation (up to -6.0 dB at 300 Hz) with rapid attack (5ms) and smooth release (80ms).
3. **Adaptive Dynamic Recovery**: When the track transitions into sparse acoustic, vocal-only, or solo sections, the filter smoothly returns to 0 dB, maintaining full natural warmth and acoustic body without thinning the track.
4. **Dual Software / Hardware Routing**:
   - Precise software biquad filtering for real-time PCM sample processing.
   - Direct hardware OpenSL ES equalizer layer integration targeting Band 1 (230 Hz) in `NexusAudioEffects`.

## 2. Target Deliverables & Modules

### A. Data Models (`:core:model`)
- `MidBassClarityMode`: `OFF`, `SUBTLE_CLARITY`, `BALANCED_DE_MUD`, `AGGRESSIVE_DECONGEST`
- `MidBassClarityConfig`:
  - `val isEnabled: Boolean = false`
  - `val mode: MidBassClarityMode = MidBassClarityMode.BALANCED_DE_MUD`
  - `val centerFrequencyHz: Float = 300.0f`
  - `val dynamicThresholdPercent: Int = 40`
- `MidBassClarityMetrics`:
  - `val congestionRatio: Float`
  - `val dynamicAttenuationDb: Float`
  - `val isDecongesting: Boolean`
  - `val clarityRating: String`
- Integration in `ThemeConfig`

### B. Core Domain Engine (`:core:domain:audio`)
- `MidBassClarityEngine`:
  - `calculateCongestionRatio(midBassEnergy: Float, totalEnergy: Float): Float`
  - `calculateDynamicAttenuationDb(congestionRatio: Float, threshold: Float, maxDipDb: Float): Float`
  - `calculateHardwareBandOffset(attenuationDb: Float): Int`
  - `calculateBiquadDipCoefficients(centerHz: Float, attenuationDb: Float, q: Float, sampleRate: Int): MidBassBiquadCoefficients`
  - `evaluateClarityRating(congestionRatio: Float, attenuationDb: Float): String`
  - `processFrame(left: Float, right: Float, config: MidBassClarityConfig, state: MidBassFilterState, sampleRate: Int): Pair<Float, Float>`
  - Unit tests in `MidBassClarityEngineTest`

### C. Hardware Audio Effects (`:core:player`)
- In `NexusAudioEffects`:
  - `applyMidBassClarityOffset(offset: Int)`
  - `clearMidBassClarityOffset()`
  - `getMidBassClarityOffset(): Int`
  - Target Band 1 (230 Hz) in `refreshBands()`
- In `NexusAudioEffectsTest`:
  - Unit tests for mid-bass clarity hardware offsets

### D. Persistent Storage (`:core:datastore`)
- `SettingsDataStore`:
  - Keys: `mid_bass_clarity_enabled`, `mid_bass_clarity_mode`, `mid_bass_clarity_center_hz`, `mid_bass_clarity_threshold`
  - Mapped inside `ThemeConfig`
  - Method: `updateMidBassClarityConfig`

### E. Presentation & UI (`:feature:settings`)
- `SettingsViewModel`:
  - `setMidBassClarityEnabled(Boolean)`
  - `setMidBassClarityMode(MidBassClarityMode)`
  - `setMidBassClarityCenterHz(Float)`
  - `setMidBassClarityThreshold(Int)`
- `SettingsScreen`:
  - "Dynamic Mid-Bass Mud Tamer & Clarity Sculptor" card in Soundstage section
  - Mode chips, center frequency chips (250 Hz, 300 Hz, 360 Hz), threshold slider, and live congestion preview badge
- `SettingsViewModelTest`:
  - Unit tests verifying settings persistence

## 3. Acceptance Verification
- `./scripts/gate.sh slice` passes with 100% tests
- `./scripts/gate.sh final` builds release APK and passes all quality checks
- Git commit & release tag `v6.17-1`

