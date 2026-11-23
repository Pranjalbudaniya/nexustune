# NexusTune Phase Plan: Improvement Loop 15 (v2.5.0)
## Bit-Perfect USB DAC Mode & Exclusive Audio Routing

### 1. Objectives & Scope
- **Feature**: Bit-Perfect USB DAC Mode & Exclusive Audio Routing.
- **Components**:
  1. `core:model`: `bitPerfectUsbAudioEnabled: Boolean` and `forceHiResSamplingRate: Int` in `ThemeConfig`. Extended `AudioOutputDevice` with sample rates and bit-depth metadata.
  2. `core:player`: `AudioOutputRouteManager` querying hardware DAC sample rates and capabilities (`AudioDeviceInfo.getSampleRates()`), providing bit-perfect direct output flags.
  3. `feature:player`: Hi-Res DAC fidelity badge in `PlayerScreen` showing live format (e.g., `24-bit / 96kHz Direct`).
  4. `feature:settings`: "Bit-Perfect USB DAC Mode" card and sampling rate selector in `SettingsScreen`.
- **Target Release**: `v2.5.0` (Git tag: `v2.5-1`).

### 2. Implementation Tasks & Owners
- **Task 15.1 (Domain & Model)**:
  - Add `bitPerfectUsbAudioEnabled` and `forceHiResSamplingRate` to `ThemeConfig`.
  - Add `sampleRates: List<Int>` and `bitDepth: Int` to `AudioOutputDevice`.
- **Task 15.2 (Audio Routing & Hardware Detection)**:
  - Enhance `AudioOutputRouteManager` to extract sample rate arrays and encoding formats from `AudioDeviceInfo`.
  - Add `setBitPerfectEnabled(enabled: Boolean)` and `setForcedSamplingRate(rate: Int)`.
- **Task 15.3 (UI & Presentation)**:
  - Display detailed fidelity pill (e.g. `USB DAC • 24-bit / 96kHz`) in `PlayerScreen`.
  - Add Bit-Perfect USB DAC card with mode toggle and sampling rate chips (Auto, 48kHz, 96kHz, 192kHz) in `SettingsScreen`.
- **Task 15.4 (Verification & Testing)**:
  - Unit tests in `AudioOutputRouteManagerTest` and `SettingsViewModelTest`.
  - Gate verification (`slice`, `perf`, `final`).

### 3. Acceptance Criteria
- [ ] USB Audio devices report accurate sample rate capabilities from native `AudioDeviceInfo`.
- [ ] When bit-perfect mode is enabled, UI presents direct stream format badge without resampler interference.
- [ ] Disconnecting USB DAC seamlessly falls back to system speaker/Bluetooth without audio crash or freeze.
- [ ] 100% test pass rate across all modules.

