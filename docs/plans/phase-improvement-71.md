# Phase Improvement Loop 71: Psychoacoustic Sub-Harmonic Bass & Missing Fundamental Studio

## 1. Goal
Implement the **Psychoacoustic Sub-Harmonic Bass & Missing Fundamental Studio** (`SubHarmonicSynthEngine`) for NexusTune.
Compact transducers (smartphone speakers, true-wireless earbuds, laptop/desktop monitors) cannot physically reproduce inaudible or low-frequency sub-bass fundamentals (30-60 Hz) without massive intermodulation distortion, diaphragm cone breakup, or complete silence due to steep acoustic roll-off.
This studio provides:
1. **Psychoacoustic Missing Fundamental Synthesis (MaxxBass principle)**: Dynamically generates correlated 2nd ($2f_0$) and 3rd ($3f_0$) order harmonics using Chebyshev polynomial wave shaping ($T_2(x) = 2x^2 - 1$, $T_3(x) = 4x^3 - 3x$) filtered into the reproducible 80-200 Hz spectrum. The human auditory cortex recombines these harmonic series into the perceived phantom fundamental, delivering rich, deep sub-bass illusion on portable devices without driver excursion.
2. **True Sub-Octave Frequency Generation**: For high-end planar magnetic headphones, full-range monitors, and car/home subwoofers, synthesizes octave-lower ($f_0 / 2$) sub-bass fundamentals (60-120 Hz divided down to 30-60 Hz) with envelope tracking.
3. **Hardware Equalizer Integration**: Dynamic real-time compensation on Band 0 (~60 Hz sub-bass band) in `NexusAudioEffects`.
4. **Settings DataStore, ViewModel & Interactive Studio Card**: Mode chips (`EARBUD_MAX_PUNCH`, `DESKTOP_BODY`, `SUBWOOFER_SHAKE`, `CLUB_EARTHQUAKE`), target transducer selector, intensity slider, sub-octave blend slider, and real-time phantom fundamental diagnostic badge.

## 2. Architecture & Components
- **`:core:model`**:
  - `SubHarmonicMode` (`OFF`, `EARBUD_MAX_PUNCH`, `DESKTOP_BODY`, `SUBWOOFER_SHAKE`, `CLUB_EARTHQUAKE`)
  - `SubHarmonicTarget` (`SMALL_SPEAKERS_EARBUDS`, `FULL_RANGE_SUBWOOFER`, `HYBRID_BALANCED`)
  - `SubHarmonicSynthConfig`
  - `SubHarmonicMetrics`
  - `ThemeConfig.subHarmonicSynthConfig`
- **`:core:domain`**:
  - `SubHarmonicSynthEngine` in `com.nexus.tune.core.domain.audio`: Missing fundamental harmonic synthesis, sub-octave division, hardware offset calculation, and metrics evaluation.
  - `SubHarmonicSynthEngineTest` unit tests.
- **`:core:player`**:
  - `NexusAudioEffects` integration targeting Band 0 (~60 Hz) with `applySubHarmonicBassOffset`, `clearSubHarmonicBassOffset`, and `getSubHarmonicBassOffset`.
  - `NexusAudioEffectsTest` unit tests.
- **`:core:datastore`**:
  - `SettingsDataStore` keys: `SUB_BASS_SYNTH_ENABLED`, `SUB_BASS_SYNTH_MODE`, `SUB_BASS_SYNTH_TARGET`, `SUB_BASS_SYNTH_INTENSITY`, `SUB_BASS_SYNTH_BLEND`.
- **`:feature:settings`**:
  - `SettingsViewModel`: Sub-harmonic control methods and hardware sync.
  - `SettingsViewModelTest`: Unit test coverage.
  - `SettingsScreen`: Studio card with mode selector, target chips, intensity slider, sub-harmonic mix slider, and live diagnostic badge.

## 3. Verification & Acceptance Criteria
- Full mathematical unit tests in `:core:domain` for Chebyshev wave-shaping, sub-octave generation, and hardware millibel offsets.
- Full unit tests in `:core:player` for audio effects Band 0 routing.
- Full unit tests in `:feature:settings` for ViewModel reactivity.
- `./scripts/gate.sh slice` passes without error.
- `./scripts/gate.sh final` builds release APK and passes all quality gates.
- Tag and commit `v6.21-1`.

