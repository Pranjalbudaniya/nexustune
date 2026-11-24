# Post-v1.0 Improvement Loop 63: Audiophile Resampling & Polyphase Sinc Reconstruction Filter Studio

## 1. Objectives & Overview
Implement an audiophile sinc reconstruction filter engine (`ResamplingFilterEngine`):
1. **Digital Reconstruction & Anti-Aliasing Filtering**:
   - Digital-to-analog and rate-conversion DSP requires anti-aliasing reconstruction filters to remove ultrasonic spectral imaging.
   - Standard hardware resamplers use generic linear interpolation, which introduces high-frequency phase smearing and pre-ringing artifacts.
2. **Phase Topology & Apodizing Impulse Modeling**:
   - Minimum-Phase filters: Push all ringing energy into post-echo where it is psychoacoustically masked by forward temporal masking, delivering punchier transients and zero pre-ringing.
   - Linear-Phase filters: Provide constant group delay and zero phase shift across the audible band, preserving soundstage depth.
   - Slow Roll-Off / Warm: Gentle high-frequency attenuation mimicking analog tape and vinyl response.
   - Apodizing: Cuts ultrasonic frequencies slightly below the Nyquist rate to prevent passband ripple from legacy ADC brickwall filters.
3. **Filter Modes**:
   - `STANDARD_SHARP`: Industry-standard linear phase brickwall filter.
   - `MINIMUM_PHASE_PUNCH`: Zero pre-ringing filter with maximum percussive attack.
   - `LINEAR_PHASE_REFERENCE`: Constant group delay reference audiophile filter.
   - `SLOW_ROLL_OFF_WARM`: Gentle slope filter with smooth analog tone and low group delay.
   - `APODIZING_ULTRASONIC`: Apodizing filter preventing pre-echo ringing from compressed digital masters.

## 2. Architecture & Components
- `:core:model`:
  - `ResamplingFilterMode` enum
  - `ResamplingFilterConfig` data class
  - `FilterImpulseMetrics` data class
- `:core:domain`:
  - `ResamplingFilterEngine` in `com.nexus.tune.core.domain.audio`:
    - `calculateSincSample(x: Float): Float`
    - `generateFilterKernel(length: Int, cutoff: Float, mode: ResamplingFilterMode): FloatArray`
    - `calculateImpulseMetrics(mode: ResamplingFilterMode): FilterImpulseMetrics`
    - `computeKaiserBeta(stopbandDb: Float): Float`
- `:core:datastore`:
  - Keys: `RESAMPLING_FILTER_MODE`, `RESAMPLING_FILTER_CUTOFF` in `SettingsDataStore` mapped to `ThemeConfig`.
- `:feature:settings`:
  - `SettingsViewModel` control methods.
  - Interactive "Audiophile Resampling & Reconstruction Filter" studio card in `SettingsScreen`.
- Unit tests:
  - `ResamplingFilterEngineTest` in `:core:domain`
  - `SettingsViewModelTest` in `:feature:settings`

## 3. Verification Criteria
- `./scripts/gate.sh slice` passes with 100% test pass rate.
- `./scripts/gate.sh final` passes with release APK build.
- Tagged `v6.13-1`.

