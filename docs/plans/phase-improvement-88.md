# Phase Improvement 88: Audiophile Dynamic Analog Tape Flutter & Wow Harmonic Modulation Studio

## 1. Goal
Design and implement the **Audiophile Dynamic Analog Tape Flutter & Wow Harmonic Modulation Studio** (`TapeModulationEngine`), providing cyclic reel-to-reel motor capstan wow pitch drift ($0.2\dots 1.5\text{ Hz}$), fast high-frequency scrape flutter speed fluctuations ($15\dots 60\text{ Hz}$), tape head scrape resonance damping, and asymmetric magnetic hysteresis soft-clipping for vintage studio warmth.

## 2. Key Architecture & Components
1. **Core Domain Audio Engine (`core:domain:audio:TapeModulationEngine`)**:
   - Fractional pitch delay line with cubic Hermite polynomial interpolation.
   - Dual-LFO modulation generator: slow Capstan Wow LFO ($0.5\dots 1.2\text{ Hz}$) and fast Scrape Flutter LFO ($25\dots 55\text{ Hz}$) with pseudo-random jitter.
   - Tape head scrape anti-resonance notch biquad ($3.2\dots 4.8\text{ kHz}$).
   - Real-time wow/flutter percentage and mechanical drift metrics estimation.
2. **Core Model (`core:model:Models.kt`)**:
   - `TapeModulationMachine`: `STUDIO_MASTER_A800`, `PORTABLE_CASSETTE_4TRACK`, `VINTAGE_TUBE_REEL_350`, `WARPED_VINYL_PRESSING`.
   - `TapeModulationMode`: `TRANSPARENT_ANALOG_WARMTH`, `VINTAGE_LO_FI_DRIFT`, `SCRAPE_FLUTTER_SOLO`, `SUBTLE_MASTERING_FLUTTER`.
   - `TapeModulationPreset`: `OFF`, `STUDIO_A800_MASTER`, `WARM_CASSETTE_CHORUS`, `PSYCHEDELIC_TAPE_WARP`, `SUBTLE_MASTER_GLUE`.
   - `TapeModulationConfig`: Data class with data-store persistence.
   - `TapeModulationMetrics`: Real-time metrics data class.
3. **Core Player Effects (`core:player:NexusAudioEffects`)**:
   - Integration with hardware equalizer bands 2 & 4 (`applyTapeModulationOffset`, `clearTapeModulationOffset`, `getTapeModulationOffset`).
4. **DataStore Persistence (`core:datastore:SettingsDataStore`)**:
   - DataStore preferences keys and mapping into `ThemeConfig`.
5. **Settings UI & ViewModel (`feature:settings`)**:
   - Interactive Material 3 Expressive studio card placed in its own dedicated `item { ... }` block in `SettingsScreen.kt` to comply with JVM 64KB method limits.
6. **Unit Tests**:
   - Comprehensive test suite for `TapeModulationEngineTest`, `NexusAudioEffectsTest`, and `SettingsViewModelTest`.

## 3. Verification & Acceptance Criteria
- 100% JVM unit tests passing across all 16 modules (`./scripts/gate.sh slice`).
- Complete build verification and R8 release APK shrinking (`./scripts/gate.sh final`).
- Release tag `v6.38-1`.
