# Phase Improvement Loop 43: Audio Loudness Normalization & Dynamic Peak Limiter Engine (v5.3.0)

## Overview
Audio sources across streaming tracks and local storage possess vastly divergent perceived loudness and peak dynamics, ranging from heavily compressed modern mastering (-7 to -9 LUFS) to dynamic acoustic recordings (-18 to -22 LUFS). Without loudness compensation, users experience jarring volume jumps between consecutive tracks.
Loop 43 introduces the **Audio Loudness Normalization & Dynamic Peak Limiter Engine**, providing automatic loudness leveling toward the industry standard (-14 LUFS) and soft-knee peak limiting to guarantee distortion-free playback.

## Deliverables

### 1. `:core:model`
- `LoudnessNormalizationMode`:
  - `OFF`: Direct unnormalized audio passthrough.
  - `REPLAYGAIN_TRACK`: Normalizes loudness toward target LUFS based on per-track gain.
  - `REPLAYGAIN_ALBUM`: Preserves inter-track dynamic contrast within the same album while leveling global album volume.
  - `DYNAMIC_PEAK_LIMITER`: Fast-attack soft-limiting preventing clipping on high-amplitude peaks.
- `LoudnessConfig`:
  - `mode: LoudnessNormalizationMode` (default `REPLAYGAIN_TRACK`)
  - `targetLufs: Float` (default `-14.0f` LUFS, standard streaming target)
  - `preAmpGainDb: Float` (default `0.0f` dB, range -6.0dB to +6.0dB)

### 2. `:core:domain:audio` / `:core:player:loudness`
- `AudioNormalizationEngine`:
  - `calculateGainMultiplier(trackLufs: Float?, targetLufs: Float, preAmpDb: Float, mode: LoudnessNormalizationMode): Float`
    - Computes logarithmic decibel gain adjustment: `gainDb = (targetLufs - trackLufs) + preAmpDb`
    - Converts to linear multiplier: `10^(gainDb / 20.0)`
    - Safety clamping between `0.1f` (-20dB) and `2.0f` (+6dB) to prevent extreme volume bursts.
  - `applySoftLimiter(sample: Float, threshold: Float = 0.95f): Float`:
    - Soft-knee compression: returns linear sample below threshold, and smooth saturation `threshold + (1 - threshold) * tanh((sample - threshold)/(1 - threshold))` above threshold.
  - `estimateTrackLufs(track: Track): Float`:
    - Dynamic heuristic estimating baseline LUFS when metadata tags are missing (e.g. factoring duration, stream bitrates, and peak heuristics).

### 3. `:core:datastore` & `:core:player`
- Persist `loudnessMode`, `targetLufs`, and `preAmpGainDb` in `SettingsDataStore`.
- Connect `AudioNormalizationEngine` to `ExoPlayerManager` / `PlayerController` to dynamically modulate stream volume factor in real time.

### 4. `:feature:settings`
- Dedicated "Audio Loudness Normalization" settings section in `SettingsScreen`:
  - Mode segmented buttons (`Off`, `ReplayGain`, `Peak Limiter`).
  - Target Loudness selector chips (`-11 LUFS Club`, `-14 LUFS Standard`, `-18 LUFS Dynamic`).
  - Pre-Amp slider (`-6.0 dB` to `+6.0 dB`).
  - Headroom badge showing current gain scale factor.

### 5. Test Suites
- `AudioNormalizationEngineTest` in `:core:domain`.
- `SettingsViewModelTest` loudness config verification in `:feature:settings`.
- Release qualification via `./scripts/gate.sh slice` and `./scripts/gate.sh final`.

