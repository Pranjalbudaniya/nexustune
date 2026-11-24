# Post-v1.0 Improvement Loop 60: Dynamic Fletcher-Munson Equal-Loudness Contour Engine

## 1. Objectives & Overview
Implement a psychoacoustically calibrated dynamic equal-loudness compensation engine (`DynamicLoudnessEngine`):
1. **Fletcher-Munson / ISO 226:2003 Equal-Loudness Psychoacoustics**:
   - The human ear is significantly less sensitive to low frequencies (<200Hz) and high treble (>6kHz) at reduced listening volumes.
   - Standard static EQ cannot compensate for this without over-amplifying low/high frequencies at medium to high playback volumes.
   - Implement volume-dependent dynamic compensation that calculates inverse ISO 226 equal-loudness curves dynamically based on playback volume attenuation.
2. **Dynamic Contour Modes**:
   - `OFF`: Linear passthrough.
   - `ISO_226_REFERENCE`: Mathematically calibrated ISO 226:2003 inverted loudness contour.
   - `SUBTLE_CONTOUR`: Gentle audiophile contour (+4dB bass / +2.5dB treble max) preserving neutral timbre.
   - `DEEP_PUNCH`: Enhanced bass warmth (+8dB bass / +5dB treble max) for late-night low-volume listening.
   - `NIGHT_WHISPER`: Ultra-low volume contour (+10dB bass / +6dB treble max) ensuring full-bodied fidelity at whisper levels (<25% volume).
3. **Architecture & Audio Effects Layering**:
   - `:core:model`:
     - `DynamicLoudnessMode` enum with curve profiles, descriptions, and max boost ratings.
     - `DynamicLoudnessConfig` data class (mode, intensityPercent 0..100, referenceVolume 0..1).
     - `LoudnessCurveOffsets` data class (5-band millibel offsets).
   - `:core:domain`:
     - `DynamicLoudnessEngine` in `com.nexus.tune.core.domain.audio`:
       - `calculateContourOffsets(mode: DynamicLoudnessMode, currentVolume: Float, intensityPercent: Int): List<Int>`
       - `calculateBandOffset(frequencyHz: Int, volumeAttenuationDb: Float, maxBoostDb: Float): Float`
       - `estimatePerceivedBassLossDb(currentVolume: Float): Float`
   - `:core:player`:
     - `NexusAudioEffects`: Integrate `applyDynamicLoudnessOffsets` and `clearDynamicLoudnessOffsets` within the non-destructive multi-layer `refreshBands()` pipeline (`customBandLevels + karaokeOffsets + headphoneOffsets + sleepDriftOffsets + dynamicLoudnessOffsets`).
   - `:core:datastore`:
     - Keys: `DYNAMIC_LOUDNESS_ENABLED`, `DYNAMIC_LOUDNESS_MODE`, `DYNAMIC_LOUDNESS_INTENSITY`.
     - Mapped in `SettingsDataStore` and `ThemeConfig`.
   - `:feature:settings`:
     - `SettingsViewModel` control methods.
     - Interactive "Dynamic Fletcher-Munson Equal-Loudness" card in `SettingsScreen` with mode selector chips, intensity slider, and live contour visualization.
   - Unit tests:
     - `DynamicLoudnessEngineTest` in `:core:domain`
     - `NexusAudioEffectsTest` in `:core:player`
     - `SettingsViewModelTest` in `:feature:settings`

## 2. Verification Criteria
- `./scripts/gate.sh slice` passes with 100% test pass rate.
- `./scripts/gate.sh final` passes with release APK build.
- Tagged `v6.10-1`.

