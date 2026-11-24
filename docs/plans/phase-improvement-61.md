# Post-v1.0 Improvement Loop 61: Beat-Synchronized Phrase Crossfade & DJ Cue Transition Engine

## 1. Objectives & Overview
Implement an intelligent DJ-grade beat-matching and musical phrase-aligned crossfade engine (`BeatSyncCrossfadeEngine`):
1. **Musical Bar & Phrase Alignment**:
   - Rather than static time-based volume crossfading (which cuts off phrases mid-beat), compute musical bar/beat durations (4 bars = 16 beats, 8 bars = 32 beats, 16 bars = 64 beats) synchronized to track BPM.
2. **Dynamic Tempo Bend & Ramp**:
   - Calculate tempo alignment factor ($R = \text{BPM}_B / \text{BPM}_A$) bounded by configurable pitch/tempo bend tolerance (e.g. $\pm 4\%$ to $\pm 10\%$) to smoothly blend rhythmic speeds across transition windows.
3. **Acoustic Bass-Swap Frequency Crossover**:
   - Solve the notorious "double kick-drum phase collision" problem by implementing asymmetric frequency crossover curves: outgoing track sub-bass (<200Hz) is attenuated early ($0.0 \to 0.4$ progress) while incoming track bass enters smoothly ($0.6 \to 1.0$), eliminating mud and low-end acoustic clipping during crossfade.
4. **Transition Modes**:
   - `OFF`: Standard linear / equal-power time crossfade.
   - `SMART_BEAT_SYNC`: Dynamic bar calculation automatically choosing 4 or 8 bars based on tempo compatibility.
   - `PHRASE_ALIGNED_8BAR`: Strict 8-bar (32-beat) radio-style phrase mix.
   - `PHRASE_ALIGNED_16BAR`: Extended 16-bar (64-beat) club-style seamless phrase transition.
   - `BASS_SWAP_CLUB`: Aggressive low-end frequency swap curve preserving maximum dancefloor energy.

## 2. Architecture & Components
- `:core:model`:
  - `BeatCrossfadeMode` enum
  - `BeatTransitionPlan` data class
  - `BeatCrossfadeConfig` data class
  - `BassSwapCurve` data class
- `:core:domain`:
  - `BeatSyncCrossfadeEngine` in `com.nexus.tune.core.domain.audio`:
    - `calculatePhraseDurationMs(bpm: Int, bars: Int, beatsPerBar: Int): Long`
    - `calculateTransitionPlan(outgoingProfile: TrackHarmonicProfile, incomingProfile: TrackHarmonicProfile, mode: BeatCrossfadeMode, maxBendPercent: Int): BeatTransitionPlan`
    - `calculateBassSwapGains(progressFraction: Float): BassSwapCurve`
    - `calculateTempoRamp(progressFraction: Float, startBpm: Int, targetBpm: Int, maxBendPercent: Int): Float`
- `:core:datastore`:
  - Keys: `BEAT_CROSSFADE_ENABLED`, `BEAT_CROSSFADE_MODE`, `BEAT_CROSSFADE_MAX_BEND` in `SettingsDataStore`.
  - Mapped in `ThemeConfig`.
- `:feature:settings`:
  - `SettingsViewModel` methods.
  - Interactive "Beat-Synchronized Phrase Crossfade & DJ Cue Studio" card in `SettingsScreen`.
- Unit tests:
  - `BeatSyncCrossfadeEngineTest` in `:core:domain`
  - `SettingsViewModelTest` in `:feature:settings`

## 3. Verification Criteria
- `./scripts/gate.sh slice` passes with 100% test pass rate.
- `./scripts/gate.sh final` passes with release APK build.
- Tagged `v6.11-1`.

