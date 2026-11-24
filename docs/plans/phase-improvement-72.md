# Phase Improvement Loop 72: Dynamic Spectral De-Esser & Vocal Harshness Tamer Studio

## 1. Goal
Implement the **Dynamic Spectral De-Esser & Vocal Harshness Tamer Studio** (`DeEsserEngine`) for NexusTune.
High-frequency vocal sibilants ("s", "sh", "ch", "t", "z") in lossy compressed streams and modern bright mastering routinely produce piercing, fatiguing energy spikes between 4.5 kHz and 8.5 kHz, exacerbated by headphone treble peaks.
Conventional static equalizers muffle vocal presence when cutting treble.
This studio provides:
1. **Dynamic Sibilance Detection & Split-Band Attenuation**: A sidechain band-pass monitor ($Q \approx 2.0$) tracks localized energy ratio against wideband RMS ($R_{sib}$). When sibilance exceeds threshold, dynamic gain reduction ($GR_{dB} \in [-8\text{ dB}, 0\text{ dB}]$) engages with rapid attack (1 ms) and fast release (25 ms), ducking sibilance cleanly and returning to unity transparently.
2. **Hardware Equalizer Integration**: Dynamic offset routing on Band 3 (presence / treble band, ~4-6 kHz) in `NexusAudioEffects`.
3. **DataStore & ViewModel State Management**: Presets (`SUBTLE_SMOOTH`, `VOCAL_INTIMATE`, `BRIGHT_HEADPHONE_TAMER`, `AGGRESSIVE_SPLIT_BAND`), center frequency chips (5 kHz, 6 kHz, 7 kHz, 8 kHz), threshold and max reduction controls.
4. **Interactive Settings Studio Card**: Live sibilance activity indicator, gain reduction meter, and ear fatigue reduction badge.

## 2. Architecture & Components
- **`:core:model`**:
  - `DeEsserMode` (`OFF`, `SUBTLE_SMOOTH`, `VOCAL_INTIMATE`, `BRIGHT_HEADPHONE_TAMER`, `AGGRESSIVE_SPLIT_BAND`)
  - `DeEsserConfig`
  - `DeEsserMetrics`
  - `DeEsserBiquadCoefficients`
  - `ThemeConfig.deEsserConfig`
- **`:core:domain`**:
  - `DeEsserEngine` in `com.nexus.tune.core.domain.audio`: Bandpass detection biquad coefficients, dynamic gain reduction calculation, hardware offset derivation, and smoothness ratings.
  - `DeEsserEngineTest` unit tests.
- **`:core:player`**:
  - `NexusAudioEffects` integration targeting Band 3 (~4-6 kHz) with `applyDeEsserOffset`, `clearDeEsserOffset`, and `getDeEsserOffset`.
  - `NexusAudioEffectsTest` unit tests.
- **`:core:datastore`**:
  - `SettingsDataStore` keys: `DEESSER_ENABLED`, `DEESSER_MODE`, `DEESSER_CENTER_HZ`, `DEESSER_THRESHOLD`, `DEESSER_MAX_REDUCTION`.
- **`:feature:settings`**:
  - `SettingsViewModel`: State methods for De-Esser control and hardware offset sync.
  - `SettingsViewModelTest`: Unit test coverage.
  - `SettingsScreen`: Studio card with preset chips, frequency selectors, threshold slider, reduction slider, and live diagnostic badge.

## 3. Verification & Acceptance Criteria
- Full mathematical unit tests in `:core:domain` for sibilance ratio, dynamic reduction, and biquad stability.
- Full unit tests in `:core:player` for audio effects Band 3 routing.
- Full unit tests in `:feature:settings` for ViewModel reactivity.
- `./scripts/gate.sh slice` passes without error.
- `./scripts/gate.sh final` builds release APK and passes all quality gates.
- Tag and commit `v6.22-1`.

