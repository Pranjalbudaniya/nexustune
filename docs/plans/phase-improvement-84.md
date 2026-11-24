# Phase Improvement 84: Audiophile Dynamic Transient Peak Slew-Rate Limiter & Electrostatic Diaphragm Protector Studio

## 1. Goal
Design and implement the **Audiophile Dynamic Transient Peak Slew-Rate Limiter & Electrostatic Diaphragm Protector Studio** (`SlewRateLimiterEngine`), offering precision voltage rate-of-change damping ($\frac{dV}{dt}$), Transient Intermodulation (TIM) distortion suppression, electrostatic stator arcing prevention, and soft-curve membrane recovery for ultra-fast planar magnetic and electrostatic headphones.

## 2. Key Architecture & Components
1. **Core Domain Audio Engine (`core:domain:audio:SlewRateLimiterEngine`)**:
   - Discrete sample rate-of-change tracker ($\Delta s = s[n] - s[n-1]$) with configurable maximum slew rate threshold.
   - Transient Intermodulation (TIM) distortion detector calculating second-order sample acceleration ($\Delta^2 s = s[n] - 2s[n-1] + s[n-2]$).
   - Diaphragm protection soft-knee curve modeling electrostatic stator charge dispersal.
   - Solo clamped transients audition mode for inspecting clipped slew edges.
   - Real-time metrics helper calculating peak slew rate, TIM distortion index, and membrane safety status.
2. **Core Model (`core:model:Models.kt`)**:
   - `SlewRateSpeed`: `FAST_100V_US`, `STUDIO_50V_US`, `ELECTROSTATIC_25V_US`, `PLANAR_15V_US`, `VINTAGE_5V_US`.
   - `SlewLimiterMode`: `TRANSPARENT_TIM_GUARD`, `STATOR_ARC_PROTECT`, `ANALOG_WARMTH_ROLLOFF`, `SOLO_CLAMPED_EDGES`.
   - `SlewRateLimiterPreset`: `BYPASS`, `ELECTROSTATIC_STAX_GUARD`, `PLANAR_MAGNETIC_SAFE`, `STUDIO_MASTER_TIM_CURE`, `WARM_TAPE_ROLLOFF`.
   - `SlewRateLimiterConfig`: Data class with data-store persistence.
   - `SlewRateLimiterMetrics`: Real-time metrics data class.
3. **Core Player Effects (`core:player:NexusAudioEffects`)**:
   - Integration with hardware equalizer bands 3 & 4 (`applySlewRateOffset`, `clearSlewRateOffset`, `getSlewRateOffset`).
4. **DataStore Persistence (`core:datastore:SettingsDataStore`)**:
   - DataStore preferences keys and mapping into `ThemeConfig`.
5. **Settings UI & ViewModel (`feature:settings`)**:
   - Interactive Material 3 Expressive studio card placed in its own dedicated `item { ... }` block in `SettingsScreen.kt` to comply with JVM 64KB method limits.
6. **Unit Tests**:
   - Comprehensive test suite for `SlewRateLimiterEngineTest`, `NexusAudioEffectsTest`, and `SettingsViewModelTest`.

## 3. Verification & Acceptance Criteria
- 100% JVM unit tests passing across all 16 modules (`./scripts/gate.sh slice`).
- Complete build verification and R8 release APK shrinking (`./scripts/gate.sh final`).
- Release tag `v6.34-1`.

