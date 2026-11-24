# Phase Improvement Loop 69: Audiophile Infrasonic Sub-Bass High-Pass & Warp Guard Studio (v6.19.0)

## 1. Overview & Motivation
Digital streaming audio and analog vinyl rips frequently carry significant energy below the threshold of human hearing (<20 Hz, down to 1-15 Hz). Sources include vinyl turntable warp rumble, mechanical microphone wind handling, DC offsets, and extreme sub-synth processing.

While completely inaudible to human ears, this subsonic energy causes severe acoustic degradation:
1. **Headroom Depletion & Intermodulation Distortion (IMD)**: Consumes massive amplifier electrical power, causing premature clipping and harmonic distortion in audible bass and midrange frequencies.
2. **Excessive Speaker Excursion ("Woofer Pumping")**: Stresses voice coils and pushes driver suspensions past linear limits on both headphones and desktop/home speakers.
3. **Dynamics Compressor Clamping**: Triggers false limiter/compressor gain reduction, pumping the audio level unmusically.

The **Audiophile Infrasonic Sub-Bass High-Pass & Warp Guard Studio (`InfrasonicFilterEngine`)** eliminates these artifacts:
1. **Configurable High-Pass Slopes**: 12 dB/oct (gentle 2nd-order), 18 dB/oct (Butterworth maximally flat), and 24 dB/oct (4th-order steep warp brickwall).
2. **Selectable Mastering Cutoffs**: 15 Hz, 20 Hz, 25 Hz, and 30 Hz.
3. **Zero-Frequency DC Offset Blocker**: Recursive single-pole DC eliminator ($R = 0.9995$) preventing bias clicks and driver drift.
4. **Restored Headroom Metrics**: Calculates dynamic headroom gain restored to the amplifier (+0.8 dB to +3.5 dB) and rumble attenuation at 10 Hz.

## 2. Target Deliverables & Modules

### A. Data Models (`:core:model`)
- `InfrasonicSlope`: `SLOPE_12DB_OCTAVE`, `SLOPE_18DB_OCTAVE`, `SLOPE_24DB_OCTAVE`
- `InfrasonicFilterConfig`:
  - `val isEnabled: Boolean = false`
  - `val cutoffHz: Float = 20.0f`
  - `val slope: InfrasonicSlope = InfrasonicSlope.SLOPE_18DB_OCTAVE`
  - `val dcBlockerEnabled: Boolean = true`
- `InfrasonicMetrics`:
  - `val cutoffHz: Float`
  - `val preservedHeadroomDb: Float`
  - `val rumbleAttenuationAt10HzDb: Float`
  - `val protectionRating: String`
- `InfrasonicBiquadCoefficients`
- Integration in `ThemeConfig`

### B. Core Domain Engine (`:core:domain:audio`)
- `InfrasonicFilterEngine`:
  - `calculateRumbleAttenuationAt10Hz(cutoffHz: Float, slope: InfrasonicSlope): Float`
  - `calculateHeadroomRestoredDb(cutoffHz: Float, slope: InfrasonicSlope): Float`
  - `calculateHighPassBiquadCoefficients(cutoffHz: Float, q: Float, sampleRate: Int): InfrasonicBiquadCoefficients`
  - `evaluateProtectionRating(cutoffHz: Float, slope: InfrasonicSlope, dcBlocked: Boolean): String`
  - `calculateMetrics(config: InfrasonicFilterConfig): InfrasonicMetrics`
  - `class InfrasonicFilterState`
  - `processFrame(left: Float, right: Float, config: InfrasonicFilterConfig, state: InfrasonicFilterState, sampleRate: Int): Pair<Float, Float>`
  - Unit tests in `InfrasonicFilterEngineTest`

### C. Persistent Storage (`:core:datastore`)
- `SettingsDataStore`:
  - Keys: `infrasonic_enabled`, `infrasonic_cutoff_hz`, `infrasonic_slope`, `infrasonic_dc_blocker`
  - Mapped inside `ThemeConfig`
  - Method: `updateInfrasonicFilterConfig`

### D. Presentation & UI (`:feature:settings`)
- `SettingsViewModel`:
  - `setInfrasonicFilterEnabled(Boolean)`
  - `setInfrasonicFilterCutoff(Float)`
  - `setInfrasonicFilterSlope(InfrasonicSlope)`
  - `setInfrasonicDcBlockerEnabled(Boolean)`
- `SettingsScreen`:
  - "Infrasonic Sub-Bass & Warp Guard" card in Hi-Res Audio section
  - Cutoff chips (15, 20, 25, 30 Hz), slope chips, DC blocker switch, and restored headroom badge
- `SettingsViewModelTest`:
  - Unit tests verifying settings persistence

## 3. Acceptance Verification
- `./scripts/gate.sh slice` passes with 100% tests
- `./scripts/gate.sh final` builds release APK and passes all quality checks
- Git commit & release tag `v6.19-1`

