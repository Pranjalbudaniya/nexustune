# Phase Improvement Loop 73: True Peak Inter-Sample Clipping Guard & 4x Oversampled Reconstruction Limiter Studio

## 1. Goal
Implement the **True Peak Inter-Sample Clipping Guard Studio** (`TruePeakGuardEngine`) for NexusTune.
Digital audio files mastered hot (near 0 dBFS) routinely generate analog waveform peaks between discrete sample points during DAC reconstruction (Inter-Sample Peaks, ISP), which clip the hardware analog stage or Bluetooth psychoacoustic encoders (SBC, AAC).
This studio provides:
1. **4x Oversampled Sinc Polyphase True Peak Estimation**: Detects inter-sample peak excursions up to $+3.0\text{ dBTP}$ conforming to ITU-R BS.1770-4.
2. **Dynamic Lookahead Ceiling Clamping**: Applies transparent, sub-sample lookahead gain margin ($0.0\text{ dBTP}$, $-0.5\text{ dBTP}$, $-1.0\text{ dBTP}$, $-2.0\text{ dBTP}$) to ensure DAC reconstruction filters never exceed analog rail limits.
3. **DataStore & ViewModel State Management**: Presets (`OFF`, `EBU_R128_STANDARD`, `AUDIOPHILE_PRISTINE`, `HIGH_PRECISION_MARGIN`, `AGGRESSIVE_PEAK_TRAP`), ceiling selector, and sensitivity controls.
4. **Interactive Settings Studio Card**: Live True Peak meter (dBTP), inter-sample clipping violation counter, and DAC linearity headroom badge.

## 2. Architecture & Components
- **`:core:model`**:
  - `TruePeakCeiling` (`CEILING_0_DBTP`, `CEILING_MINUS_0_5_DBTP`, `CEILING_MINUS_1_0_DBTP`, `CEILING_MINUS_2_0_DBTP`)
  - `TruePeakMode` (`OFF`, `EBU_R128_STANDARD`, `AUDIOPHILE_PRISTINE`, `HIGH_PRECISION_MARGIN`, `AGGRESSIVE_PEAK_TRAP`)
  - `TruePeakGuardConfig`
  - `TruePeakMetrics`
  - `ThemeConfig.truePeakGuardConfig`
- **`:core:domain`**:
  - `TruePeakGuardEngine` in `com.nexus.tune.core.domain.audio`: 4x polyphase oversampling interpolation, true peak dBTP estimation, dynamic margin gain calculation, and DAC linearity ratings.
  - `TruePeakGuardEngineTest` unit tests.
- **`:core:datastore`**:
  - `SettingsDataStore` keys: `TRUE_PEAK_ENABLED`, `TRUE_PEAK_MODE`, `TRUE_PEAK_CEILING_DB`, `TRUE_PEAK_OVERSAMPLE_4X`.
- **`:feature:settings`**:
  - `SettingsViewModel`: True peak control methods.
  - `SettingsViewModelTest`: Unit test coverage.
  - `SettingsScreen`: Studio card with mode selector, ceiling chips, oversampling switch, and live True Peak headroom badge.

## 3. Verification & Acceptance Criteria
- Full mathematical unit tests in `:core:domain` for polyphase 4x sinc interpolation, ISP detection, and dBTP margin.
- Full unit tests in `:feature:settings` for ViewModel reactivity.
- `./scripts/gate.sh slice` passes without error.
- `./scripts/gate.sh final` builds release APK and passes all quality gates.
- Tag and commit `v6.23-1`.

