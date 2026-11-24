# Phase Improvement 87: Audiophile Dynamic Mid-Side Spatial Phase Decorrelator & Ambience Diffuser Studio

## 1. Goal
Design and implement the **Audiophile Dynamic Mid-Side Spatial Phase Decorrelator & Ambience Diffuser Studio** (`SpatialDecorrelatorEngine`), providing frequency-dependent pseudo-random all-pass phase decorrelation on stereo side channel and ambience reverberant diffusion, eliminating stereo smearing while expanding a deep, 3D holographic soundstage for headphones and nearfield studio monitors.

## 2. Key Architecture & Components
1. **Core Domain Audio Engine (`core:domain:audio:SpatialDecorrelatorEngine`)**:
   - Mid/Side matrix separation ($M = \frac{L + R}{\sqrt{2}}$, $S = \frac{L - R}{\sqrt{2}}$).
   - Frequency-dependent all-pass filter cascade with prime-number delay lengths decorrelating phase angles without frequency response coloration.
   - Dynamic mono-compatibility safeguarding preserving phantom center clarity and zero cancellation when summed to mono.
   - Real-time stereo correlation coefficient $\rho$ and decorrelation diffusion index estimation.
2. **Core Model (`core:model:Models.kt`)**:
   - `DecorrelatorStage`: `SUBTLE_HAAS_MICRO`, `DIFFUSE_CONCERT_DEPTH`, `WIDE_HOLOGRAPHIC_STAGE`, `BINAURAL_HEADPHONE_EXPANDER`.
   - `DecorrelatorMode`: `MID_SIDE_PHASE_SPLIT`, `ALLPASS_CASCADE_DIFFUSE`, `DYNAMIC_AMBIECE_SPREAD`, `SOLO_DECORRELATED_SIDE`.
   - `DecorrelatorPreset`: `OFF`, `HEADPHONE_SOUNDSTAGE_EXPAND`, `STUDIO_NEARFIELD_UNCLUTTER`, `ORCHESTRAL_HALL_AIR`, `VINYL_STEREO_RECENTER`.
   - `SpatialDecorrelatorConfig`: Data class with data-store persistence.
   - `SpatialDecorrelatorMetrics`: Real-time metrics data class.
3. **Core Player Effects (`core:player:NexusAudioEffects`)**:
   - Integration with hardware equalizer bands 1 & 3 (`applyDecorrelatorOffset`, `clearDecorrelatorOffset`, `getDecorrelatorOffset`).
4. **DataStore Persistence (`core:datastore:SettingsDataStore`)**:
   - DataStore preferences keys and mapping into `ThemeConfig`.
5. **Settings UI & ViewModel (`feature:settings`)**:
   - Interactive Material 3 Expressive studio card placed in its own dedicated `item { ... }` block in `SettingsScreen.kt` to comply with JVM 64KB method limits.
6. **Unit Tests**:
   - Comprehensive test suite for `SpatialDecorrelatorEngineTest`, `NexusAudioEffectsTest`, and `SettingsViewModelTest`.

## 3. Verification & Acceptance Criteria
- 100% JVM unit tests passing across all 16 modules (`./scripts/gate.sh slice`).
- Complete build verification and R8 release APK shrinking (`./scripts/gate.sh final`).
- Release tag `v6.37-1`.
