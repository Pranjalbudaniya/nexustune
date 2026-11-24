# Post-v1.0 Improvement Loop 59: Audiophile TPDF Dither & Psychoacoustic Noise Shaping Studio

## 1. Objectives & Overview
Implement an audiophile dithering and noise-shaping engine in NexusTune (`DitherShapingEngine`):
1. **Harmonic Quantization Distortion Elimination**:
   - When downsampling or truncating 24-bit or 32-bit audio streams to 16-bit output (Bluetooth A2DP, standard Android AudioTrack, or USB DACs), pure truncation creates correlated harmonic distortion spikes and grainy quantization noise at low amplitudes.
   - Implement industry-standard **Triangular Probability Density Function (TPDF)** dithering ($\pm 1.0 \text{ LSB}$) to linearize quantization error into uncorrelated gaussian-like hiss.
2. **Psychoacoustic Noise Shaping**:
   - Filter quantization error feedback through an acoustic noise-shaping matrix based on Fletcher-Munson equal loudness contours (Lipshitz/Wannamaker coefficients).
   - Relocate dither energy from the ear's most sensitive hearing zone (1kHz - 5kHz) into the ultrasonic periphery (>15kHz), yielding an apparent dynamic range exceeding 115dB on 16-bit playback hardware.
3. **Dither Modes**:
   - `OFF` (Raw integer truncation)
   - `RECTANGULAR_RPDF` (Basic uniform dither)
   - `TRIANGULAR_TPDF` (Pure mathematical linearity, zero noise modulation)
   - `PSYCHOACOUSTIC_LIPSHITZ` (Optimal human equal-loudness noise-shaping contour)
   - `HIGH_PASS_CONTOUR` (High-pass filtered dither avoiding sub-bass interference)
4. **DataStore Persistence & UI**:
   - Add `DitherMode` selection in `SettingsScreen` within the Hi-Res audio section.

## 2. Architecture & Components
- `:core:model`:
  - `DitherMode` enum
  - `DitherConfig` data class
  - `QuantizationNoiseMetrics` data class
- `:core:domain`:
  - `DitherShapingEngine` in `com.nexus.tune.core.domain.audio`:
    - `generateTpdfSample(randomSource: () -> Float): Float`
    - `quantizeSample(input: Float, targetBits: Int, mode: DitherMode, errorHistory: FloatArray): Float`
    - `calculateApparentDynamicRange(targetBits: Int, mode: DitherMode): Float`
    - `getNoiseShapingCoefficients(mode: DitherMode): FloatArray`
- `:core:datastore`:
  - Key: `DITHER_MODE` in `SettingsDataStore`.
  - Mapped to `ThemeConfig`.
- `:feature:settings`:
  - `SettingsViewModel`: `setDitherMode(mode: DitherMode)`.
  - Interactive "Audiophile Dither & Noise Shaping" card in `SettingsScreen`.
- Unit Tests:
  - `DitherShapingEngineTest` in `:core:domain`
  - `SettingsViewModelTest` in `:feature:settings`

## 3. Verification Criteria
- `./scripts/gate.sh slice` passes with 100% test pass rate.
- `./scripts/gate.sh final` passes with release APK build.
- Tagged `v6.9-1`.

