# Phase Improvement 85: Audiophile Dynamic Acoustic Resonator & Cabinet Horn Diffuser Studio

## 1. Goal
Design and implement the **Audiophile Dynamic Acoustic Resonator & Cabinet Horn Diffuser Studio** (`HornDiffuserEngine`), simulating Tractrix, Exponential, and Conical acoustic horn flare loading, throat cavity standing wave anti-resonance filtering, and quadratic residue horizontal dispersion for hyper-efficient, lifelike acoustic reproduction.

## 2. Key Architecture & Components
1. **Core Domain Audio Engine (`core:domain:audio:HornDiffuserEngine`)**:
   - Tractrix / Exponential impedance matching acoustic curve generator.
   - Throat reflection anti-resonance parametric biquad notch filter targeting throat cavity coloration ($1.2\dots 2.8\text{ kHz}$).
   - Horizontal QRD acoustic flare cross-channel dispersion matrix with frequency-dependent decorrelation.
   - Air particle viscosity compression driver model for effortless transient dynamics.
   - Solo horn resonance audition mode for inspecting acoustic reflections.
   - Real-time metrics helper estimating throat acoustic impedance, dispersion angle, and resonance damping rating.
2. **Core Model (`core:model:Models.kt`)**:
   - `HornFlareGeometry`: `TRACTRIX`, `EXPONENTIAL`, `CONICAL_WAVEGUIDE`, `SECTORAL_RADIAL`, `CONSTANT_DIRECTIVITY`.
   - `HornDiffuserMode`: `ACOUSTIC_LOADING`, `THROAT_ANTI_RESONANCE`, `WIDE_DISPERSION`, `SOLO_HORN_COLORATION`.
   - `HornDiffuserPreset`: `BYPASS`, `KLIPSCHORN_HERITAGE`, `WESTERN_ELECTRIC_VINTAGE`, `STUDIO_BI_RADIAL`, `ULTRA_WIDE_TRACTRIX`.
   - `HornDiffuserConfig`: Data class with data-store persistence.
   - `HornDiffuserMetrics`: Real-time metrics data class.
3. **Core Player Effects (`core:player:NexusAudioEffects`)**:
   - Integration with hardware equalizer band 2 (midrange) and band 4 (air) (`applyHornDiffuserOffset`, `clearHornDiffuserOffset`, `getHornDiffuserOffset`).
4. **DataStore Persistence (`core:datastore:SettingsDataStore`)**:
   - DataStore preferences keys and mapping into `ThemeConfig`.
5. **Settings UI & ViewModel (`feature:settings`)**:
   - Interactive Material 3 Expressive studio card placed in its own dedicated `item { ... }` block in `SettingsScreen.kt` to prevent JVM 64KB method limits.
6. **Unit Tests**:
   - Comprehensive test suite for `HornDiffuserEngineTest`, `NexusAudioEffectsTest`, and `SettingsViewModelTest`.

## 3. Verification & Acceptance Criteria
- 100% JVM unit tests passing across all 16 modules (`./scripts/gate.sh slice`).
- Complete build verification and R8 release APK shrinking (`./scripts/gate.sh final`).
- Release tag `v6.35-1`.

