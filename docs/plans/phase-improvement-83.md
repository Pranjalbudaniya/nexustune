# Phase Improvement 83: Audiophile Dynamic Analog Preamp Thermal Noise Profiler & Spectral Flattener Studio

## 1. Goal
Design and implement the **Audiophile Dynamic Analog Preamp Thermal Noise Profiler & Spectral Flattener Studio** (`PreampNoiseFlattenerEngine`), offering precision spectral noise profiling (Johnson-Nyquist thermal white, $1/f$ flicker pink, $1/f^2$ brownian, and blue shot noise), dynamic spectral tilt equalization, downward soft-knee noise floor expansion, and Fletcher-Munson shaped thermal dither injection for natural analog realism.

## 2. Key Architecture & Components
1. **Core Domain Audio Engine (`core:domain:audio:PreampNoiseFlattenerEngine`)**:
   - Discrete multi-band spectral energy profiler evaluating low, mid, and high frequency RMS partitions to estimate spectral tilt slope ($S = \Delta E / \Delta f$).
   - Dynamic spectral tilt equalizer with variable shelving and parametric correction curves (compensating for $+3$ dB/oct pink or $+6$ dB/oct brown tilt).
   - Soft-knee downward spectral expander with smooth attack/release ballistics to attenuate quiescent preamp thermal hiss without cutting decay tails.
   - Psychoacoustic Johnson-Nyquist thermal dither generator with Fletcher-Munson equal-loudness pre-emphasis for cold PCM stream de-quantization.
   - Solo noise audition mode for inspecting isolated noise profiles and spectral cuts.
2. **Core Model (`core:model:Models.kt`)**:
   - `PreampNoiseProfile`: `AUTO_DETECT`, `FLICKER_PINK`, `THERMAL_WHITE`, `BROWNIAN_LOW`, `SHOT_BLUE`.
   - `PreampFlattenerMode`: `DYNAMIC_FLATTEN`, `DOWNWARD_EXPAND`, `WARM_DITHER`, `SOLO_NOISE_PROFILE`.
   - `PreampNoiseFlattenerPreset`: `BYPASS`, `STUDIO_CLEAN`, `VINTAGE_PHONO`, `TUBE_CONSOLE`, `GENTLE_AIR`.
   - `PreampNoiseFlattenerConfig`: Data class with data-store persistence.
   - `PreampNoiseFlattenerMetrics`: Calculated real-time metrics data class.
3. **Core Player Effects (`core:player:NexusAudioEffects`)**:
   - Dynamic compensation offsets for hardware equalizer band 4 (treble/air) when dynamic tilt reduction is active.
4. **DataStore Persistence (`core:datastore:SettingsDataStore`)**:
   - Keys for preamp flattener enable, preset, profile, mode, flatten depth, threshold, dither level, and hf tilt.
5. **Settings UI & ViewModel (`feature:settings`)**:
   - High-fidelity Material 3 Expressive studio card placed in its own dedicated `item { ... }` block in `SettingsScreen.kt` to preserve JVM 64KB method limits.
6. **Unit Tests**:
   - Comprehensive test suite for `PreampNoiseFlattenerEngineTest`, `NexusAudioEffectsTest`, and `SettingsViewModelTest`.

## 3. Verification & Acceptance Criteria
- 100% JVM unit tests passing across all 16 modules (`./scripts/gate.sh slice`).
- Complete build verification and R8 release APK shrinking (`./scripts/gate.sh final`).
- Release tag `v6.33-1`.
