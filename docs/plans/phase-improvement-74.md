# Phase Improvement Loop 74: Ultrasonic Anti-Aliasing & Nyquist Image Guard Studio

## 1. Goal
Implement the **Ultrasonic Anti-Aliasing & Nyquist Image Guard Studio** (`UltrasonicFilterEngine`) for NexusTune.
High-resolution streams (48/96/192 kHz) and digital interpolation filters produce supersonic spectral images above 20 kHz. When passed into analog headphone amplifiers and mobile speaker drivers, ultrasonic content intermodulates non-linearly (IMD), creating audible noise hash in the sensitive 1-10 kHz midrange and causing voice-coil thermal stress.
This studio provides:
1. **Ultrasonic Low-Pass Biquad & Linear Phase Filtering**: Selectable cutoff boundaries (20 kHz, 22.05 kHz, 24 kHz, 28 kHz, 32 kHz) with customizable filter topologies (Linear Phase FIR, Minimum Phase IIR, Bessel Constant Delay, and Apodizing Ring Killer).
2. **Intermodulation Distortion (IMD) Shielding**: Eliminates supersonic energy above 20-24 kHz to ensure clean analog reproduction with zero IMD foldback.
3. **DataStore & ViewModel State Management**: Presets (`AUDIOPHILE_BRICKWALL_22K`, `HI_RES_TAMER_24K`, `NATURAL_TRANSIENT_BESSEL`, `APODIZING_RING_KILLER`), cutoff selector, and topology chips.
4. **Interactive Settings Studio Card**: Live stopband rejection meter, impulse pre-ringing indicator, and IMD suppression rating badge.

## 2. Architecture & Components
- **`:core:model`**:
  - `UltrasonicTopology` (`LINEAR_PHASE_FIR`, `MINIMUM_PHASE_IIR`, `BESSEL_CONSTANT_DELAY`, `APODIZING_RING_KILLER`)
  - `UltrasonicMode` (`OFF`, `AUDIOPHILE_BRICKWALL_22K`, `HI_RES_TAMER_24K`, `NATURAL_TRANSIENT_BESSEL`, `APODIZING_RING_KILLER`)
  - `UltrasonicFilterConfig`
  - `UltrasonicMetrics`
  - `UltrasonicBiquadCoefficients`
  - `ThemeConfig.ultrasonicFilterConfig`
- **`:core:domain`**:
  - `UltrasonicFilterEngine` in `com.nexus.tune.core.domain.audio`: Low-pass filter coefficient synthesis, stopband rejection calculation, group delay estimation, and IMD protection ratings.
  - `UltrasonicFilterEngineTest` unit tests.
- **`:core:datastore`**:
  - `SettingsDataStore` keys: `ULTRASONIC_ENABLED`, `ULTRASONIC_MODE`, `ULTRASONIC_TOPOLOGY`, `ULTRASONIC_CUTOFF_HZ`.
- **`:feature:settings`**:
  - `SettingsViewModel`: State methods for ultrasonic filter management.
  - `SettingsViewModelTest`: Unit test coverage.
  - `SettingsScreen`: Studio card with topology chips, cutoff selector, and real-time IMD shield badge.

## 3. Verification & Acceptance Criteria
- Full mathematical unit tests in `:core:domain` for filter stability, stopband attenuation, and group delay.
- Full unit tests in `:feature:settings` for ViewModel reactivity.
- `./scripts/gate.sh slice` passes without error.
- `./scripts/gate.sh final` builds release APK and passes all quality gates.
- Tag and commit `v6.24-1`.

