# Phase Improvement Loop 68: Loudspeaker Acoustic Crosstalk Cancellation & Stereo Dipole 3D Soundstage Studio (v6.18.0)

## 1. Overview & Motivation
When listening to music over loudspeakers (especially compact phone stereo speakers, tablets, desktop monitors, and Bluetooth soundbars), acoustic crosstalk occurs naturally:
- The left ear hears the left speaker directly, but also hears the right speaker slightly delayed ($\approx 100 - 300\ \mu\text{s}$) and diffractively shadowed by the listener's head.
- This acoustic bleed collapses the stereo soundstage to the narrow physical distance between the speakers (often only 7-8 cm on mobile phones).

The **Loudspeaker Acoustic Crosstalk Cancellation (XTC) & Stereo Dipole 3D Studio (`CrosstalkCancellationEngine`)** inverts this phenomenon:
1. **Contralateral Cancellation Synthesis**: Computes an inverted, interaurally delayed, head-shadowed cancellation wave ($C_L = -\alpha \cdot H_{shadow}(R[n - \Delta n])$) that acoustically annihilates the crosstalk wave in the air at the listener's ear canal entrance.
2. **Stereo Dipole Soundstage Expansion**: Extends the perceived acoustic field from narrow $30^\circ$ physical angle out to an expansive $140^\circ \dots 175^\circ$ holographic panoramic arch.
3. **Hardware & Mobile Geometry Profiles**: Tailored parameter presets for Phone Stereo Speakers ($\approx 7-8\text{ cm}$ spacing), Desktop/Laptop Near-Field ($\approx 30-50\text{ cm}$), and Living Room Soundbars ($\approx 1-1.5\text{ m}$).
4. **Clean Ring-Buffer DSP**: Unconditionally stable circular ring-buffer delay lines with $C^0$ head-diffraction low-pass filtering.

## 2. Target Deliverables & Modules

### A. Data Models (`:core:model`)
- `CrosstalkCancellationMode`: `OFF`, `PHONE_STEREO_SPEAKERS`, `DESKTOP_NEARFIELD`, `SOUNDBAR_LIVING_ROOM`
- `CrosstalkCancellationConfig`:
  - `val isEnabled: Boolean = false`
  - `val mode: CrosstalkCancellationMode = CrosstalkCancellationMode.PHONE_STEREO_SPEAKERS`
  - `val delaySamples: Int = 5`
  - `val cancellationIntensityPercent: Int = 70`
  - `val headShadowDamping: Float = 0.45f`
- `CrosstalkCancellationMetrics`:
  - `val delayMicroseconds: Float`
  - `val effectiveCancellationDb: Float`
  - `val virtualStageSpanDegrees: Int`
  - `val binauralRating: String`
- Integration in `ThemeConfig`

### B. Core Domain Engine (`:core:domain:audio`)
- `CrosstalkCancellationEngine`:
  - `calculateDelayMicroseconds(delaySamples: Int, sampleRate: Int): Float`
  - `calculateVirtualStageSpan(mode: CrosstalkCancellationMode, intensityPercent: Int): Int`
  - `evaluateBinauralRating(spanDegrees: Int): String`
  - `calculateMetrics(config: CrosstalkCancellationConfig, sampleRate: Int): CrosstalkCancellationMetrics`
  - `class XtcState(val delaySize: Int = 64)`
  - `processFrame(left: Float, right: Float, config: CrosstalkCancellationConfig, state: XtcState): Pair<Float, Float>`
  - Unit tests in `CrosstalkCancellationEngineTest`

### C. Persistent Storage (`:core:datastore`)
- `SettingsDataStore`:
  - Keys: `xtc_enabled`, `xtc_mode`, `xtc_delay_samples`, `xtc_intensity_percent`
  - Mapped inside `ThemeConfig`
  - Method: `updateCrosstalkCancellationConfig`

### D. Presentation & UI (`:feature:settings`)
- `SettingsViewModel`:
  - `setCrosstalkCancellationEnabled(Boolean)`
  - `setCrosstalkCancellationMode(CrosstalkCancellationMode)`
  - `setCrosstalkCancellationDelay(Int)`
  - `setCrosstalkCancellationIntensity(Int)`
- `SettingsScreen`:
  - "Loudspeaker Holographic 3D (XTC)" studio card in Soundstage section
  - Mode chips, delay sample selector, intensity slider, and live soundstage expansion degree badge
- `SettingsViewModelTest`:
  - Unit tests verifying settings persistence

## 3. Acceptance Verification
- `./scripts/gate.sh slice` passes with 100% tests
- `./scripts/gate.sh final` builds release APK and passes all quality checks
- Git commit & release tag `v6.18-1`

