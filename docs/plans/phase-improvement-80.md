# Phase Improvement Plan - Loop 80 (v6.30.0)
## Audiophile Dynamic Acoustic Feedback & Larsen Ringing Suppressor Studio

### 1. Context & Motivation
High-volume playback through mobile phone stereo speakers, external soundbars, or open-back headphones often triggers acoustic feedback, cavity resonance, and room standing waves. Additionally, live concert recordings frequently contain piercing mic feedback squeals (the Larsen effect).
Loop 80 introduces an adaptive, multi-order dynamic feedback hunter and precision ringing suppressor that identifies narrow-band acoustic spikes in real time and automatically deploys surgical tracking notches to eliminate ringing while leaving musical instruments untouched.

### 2. Architecture & Design
- **`:core:model`**:
  - `FeedbackHuntMode`: `CONCERT_RECORDING`, `SPEAKER_BOUNDARY`, `OPEN_BACK_HEADPHONE`, `SURGICAL_AUTO`
  - `AcousticFeedbackPreset`: `OFF`, `STUDIO_TRANSPARENT`, `LIVE_CONCERT_DE_SQUEAL`, `SPEAKER_ROOM_RESONANCE`, `OPEN_BACK_HEADPHONE_GUARD`, `CUSTOM`
  - `AcousticFeedbackConfig`:
    - `isEnabled: Boolean = false`
    - `preset: AcousticFeedbackPreset = AcousticFeedbackPreset.OFF`
    - `mode: FeedbackHuntMode = FeedbackHuntMode.SURGICAL_AUTO`
    - `sensitivityThresholdDb: Float = -28.0f` (-45.0f .. -12.0f)
    - `activeNotchesCount: Int = 3` (1 .. 4)
    - `notchSharpnessQ: Float = 35.0f` (20.0f .. 60.0f)
    - `suppressionDepthDb: Float = -24.0f` (-6.0f .. -36.0f)
    - `fastPanicClamp: Boolean = true`
  - `AcousticFeedbackMetrics`:
    - `detectedRingingFrequencyHz: Float`
    - `activeNotchCount: Int`
    - `peakFeedbackEnergyDb: Float`
    - `totalSuppressionDb: Float`
    - `safetyStatus: String`
  - Integrate `acousticFeedbackConfig` into `ThemeConfig`.

- **`:core:domain:audio`**:
  - `AcousticFeedbackEngine`:
    - Multi-band peak autocorrelation detector distinguishing narrow monotonic ringing from vibrato/musical chords
    - 4 dynamically deployable steerable biquad notch filters per channel
    - Ballistic tracking: rapid attack envelope (clamping feedback in 5 ms) and smooth musical release (350 ms)
    - Emergency runaway panic limiter protecting user hearing from sudden screeching feedback bursts

- **`:core:player`**:
  - `NexusAudioEffects`:
    - Hardware equalizer subtle trim offsets (`feedbackGuardOffset`, `applyFeedbackGuardOffset`, `clearFeedbackGuardOffset`, `getFeedbackGuardOffset`) targeting Bands 2 & 3

- **`:core:datastore`**:
  - Preferences keys for acoustic feedback suppressor settings
  - Flow updates in `themeConfig` and `updateThemeConfig`

- **`:feature:settings`**:
  - `SettingsViewModel`: State methods for feedback suppressor controls
  - `SettingsScreen`: Interactive "Dynamic Acoustic Feedback & Ringing Suppressor Studio" Card with presets, hunt mode chips, sensitivity slider, sharpness Q slider, suppression depth slider, panic clamp switch, and live feedback hunting HUD

- **Tests & Verification**:
  - Domain tests: `AcousticFeedbackEngineTest`
  - Player tests: `NexusAudioEffectsTest`
  - ViewModel tests: `SettingsViewModelTest`
  - Gates: `./scripts/gate.sh slice` and `./scripts/gate.sh final`

