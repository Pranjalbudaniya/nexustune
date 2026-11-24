# Phase Improvement Plan - Loop 79 (v6.29.0)
## Audiophile Dynamic Spectral De-Hummer & 50/60Hz Mains Ground Loop Isolator Studio

### 1. Context & Motivation
When listening via high-sensitivity wired headphones or USB DACs while mobile devices are connected to AC chargers, ground loops and electromagnetic coupling often induce 50 Hz or 60 Hz powerline hum and their harmonic overtones (100/120 Hz, 150/180 Hz, 200/240 Hz).
Standard broad-band EQs destroy musical bass when trying to eliminate this hum.
Loop 79 introduces a precision Multi-Band High-Q Notch Filter Bank with Adaptive Psychoacoustic Masking and DC Bias Rejection, eliminating electrical noise without sacrificing musical punch.

### 2. Architecture & Design
- **`:core:model`**:
  - `MainsFrequency`: `HZ_50`, `HZ_60`, `AUTO_DETECT`
  - `HumHarmonicDepth`: `FUNDAMENTAL_ONLY` (1st), `LOW_HARMONICS` (up to 3rd), `FULL_SPECTRUM` (up to 6th)
  - `HumIsolatorPreset`: `STUDIO_CLEAN`, `USB_DAC_GROUND_LIFT`, `VINTAGE_VINYL_RESTORE`, `HIGH_SENSITIVITY_IEM`, `CUSTOM`
  - `MainsHumIsolatorConfig`:
    - `enabled: Boolean = false`
    - `frequency: MainsFrequency = MainsFrequency.AUTO_DETECT`
    - `harmonicDepth: HumHarmonicDepth = HumHarmonicDepth.LOW_HARMONICS`
    - `notchQ: Float = 25.0f` (10.0f .. 40.0f)
    - `notchDepthDb: Float = -24.0f` (-6.0f .. -48.0f)
    - `adaptiveMasking: Boolean = true`
    - `dcBlockerEnabled: Boolean = true`
  - `MainsHumMetrics`:
    - `estimatedHumLevelDb: Float`
    - `attenuationAtF0Db: Float`
    - `subBassPreservationRatio: Float`
    - `detectedGridFrequency: Float`
    - `dcOffsetMv: Float`
  - Integrate `mainsHumIsolatorConfig` into `ThemeConfig`.

- **`:core:domain:audio`**:
  - `MainsHumIsolatorEngine`:
    - High-Q biquad notch filter calculation:
      $b_0 = 1, b_1 = -2 \cos \omega_0, b_2 = 1$
      $a_0 = 1 + \alpha / A, a_1 = -2 \cos \omega_0, a_2 = 1 - \alpha / A$ where $A = 10^{\text{depth}/40}$ and $\alpha = \frac{\sin \omega_0}{2Q}$
    - Cascaded harmonic notch stages for $f_0, 2f_0, 3f_0, 4f_0, 5f_0, 6f_0$
    - Adaptive psychoacoustic masking detection: relaxes notch depth when broad musical bass energy dominates
    - Single-pole recursive DC blocking highpass filter ($y[n] = x[n] - x[n-1] + R \cdot y[n-1]$ with $R \approx 0.9995$)
    - Grid frequency detector comparing 50 Hz vs 60 Hz spectral energy density

- **`:core:player`**:
  - `NexusAudioEffects`:
    - Hardware equalizer subtle compensation offsets (`mainsHumTrimOffset`, `applyMainsHumOffset`, `clearMainsHumOffset`, `getMainsHumOffset`) targeting Band 0

- **`:core:datastore`**:
  - Preferences keys for mains hum isolator settings
  - Flow updates in `themeConfig` and `updateThemeConfig`

- **`:feature:settings`**:
  - `SettingsViewModel`: State methods for hum isolator controls
  - `SettingsScreen`: Interactive "Mains Ground Loop & Hum Isolator Studio" Card with real-time frequency selection, harmonic depth chips, notch Q slider, notch depth slider, adaptive masking switch, and real-time noise suppression HUD

- **Tests & Verification**:
  - Domain tests: `MainsHumIsolatorEngineTest`
  - Player tests: `NexusAudioEffectsTest`
  - ViewModel tests: `SettingsViewModelTest`
  - Gates: `./scripts/gate.sh slice` and `./scripts/gate.sh final`

