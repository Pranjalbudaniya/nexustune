# Phase Improvement Plan - Loop 55 (v6.5.0)
## Intelligent Headphone Acoustic Target Curve Compensation & Cross-Feed Engine (`HeadphoneCorrectionEngine`)

### 1. Context & Motivation
Headphones possess vastly different physical frequency responses depending on driver topology and acoustic enclosures. Audiophiles utilize standardized acoustic target curves to achieve optimal tonal balance, transparent staging, and reduced in-head stereo listening fatigue. NexusTune v6.5.0 introduces the **Intelligent Headphone Acoustic Target Curve Compensation & Cross-Feed Engine (`HeadphoneCorrectionEngine`)**. It provides calibrated parametric EQ profiles for globally recognized acoustic targets: Harman Over-Ear 2019, Harman In-Ear, Diffuse Field, Studio Neutral Reference, Warm Punch, V-Shaped Basshead, and Treble Air, paired with an acoustic Cross-Feed listening fatigue reduction engine.

### 2. Architecture & Modules
- **Core Domain Models** (`:core:model`):
  - `HeadphoneTargetCurve`: `HARMAN_OVER_EAR`, `HARMAN_IN_EAR`, `DIFFUSE_FIELD`, `STUDIO_NEUTRAL`, `WARM_PUNCH`, `V_SHAPED_BASSHEAD`, `TREBLE_AIR`.
  - `HeadphoneCorrectionConfig`: `isEnabled: Boolean`, `targetCurve: HeadphoneTargetCurve`, `crossfeedEnabled: Boolean`, `crossfeedLevelPercent: Int`.
- **Pure Kotlin Domain Engine** (`:core:domain:audio`):
  - `HeadphoneCorrectionEngine`:
    - 5-band millibel EQ correction profiles (`60Hz`, `230Hz`, `910Hz`, `3600Hz`, `14000Hz`) mapped to each acoustic target curve.
    - Cross-feed acoustic blending coefficient and spatializer stage recommendations.
    - Formatter for acoustic frequency curve description.
- **Audio DSP & Effects** (`:core:player`):
  - `NexusAudioEffects`: Added `applyHeadphoneTargetOffsets(offsets: List<Int>)` and `clearHeadphoneTargetOffsets()`, seamlessly blended into `refreshBands()`.
- **Persistence Layer** (`:core:datastore`):
  - Preference keys: `HEADPHONE_CORRECTION_ENABLED`, `HEADPHONE_TARGET_CURVE`, `HEADPHONE_CROSSFEED_ENABLED`.
  - Embedded in `ThemeConfig` and `SettingsDataStore`.
- **Presentation Layer** (`:feature:settings`):
  - `SettingsViewModel`: Methods `setHeadphoneCorrectionEnabled()`, `setHeadphoneTargetCurve()`, `setHeadphoneCrossfeedEnabled()`.
  - `SettingsScreen`: Interactive "🎧 Headphone Soundstage & Acoustic Target Compensation" card with curve selector chips, acoustic description pill, and cross-feed toggle.
- **Verification & Gates**:
  - `HeadphoneCorrectionEngineTest` in `:core:domain:audio`.
  - `SettingsViewModelTest` in `:feature:settings`.
  - Quality gates: `./scripts/gate.sh slice` and `./scripts/gate.sh final`.
  - Release tag: `v6.5-1`.

