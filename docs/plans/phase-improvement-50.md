# Phase Improvement Plan - Loop 50 (v6.0.0 Grand Milestone)
## Acoustic Camelot Harmonic Mixing & BPM Transition Intelligence Engine

### 1. Context & Motivation
In professional DJ performance and high-fidelity audio curation, **Camelot Harmonic Mixing** (the Circle of Fifths transposed to a 1-12 A/B numerical matrix) is the standard for eliminating harmonic clash between consecutive songs. NexusTune v6.0.0 introduces a native **Acoustic Camelot Harmonic Mixing & BPM Transition Intelligence Engine** that turns listening sessions into continuous, key-matched musical journeys.

### 2. Architecture & Modules

#### 2.1 Core Models (`:core:model`)
- `CamelotScale`: `MINOR` ("A"), `MAJOR` ("B")
- `CamelotKey`: Value class / enum mapping 1A through 12A and 1B through 12B, musical pitch names (e.g., `8A` -> "A minor", `8B` -> "C Major").
- `HarmonicRelationship`:
  - `PERFECT_MATCH`: Same Camelot key (100% compatibility, `#10B981`)
  - `RELATIVE_KEY`: Relative major/minor modulation (90%, `#3B82F6`)
  - `ADJACENT_STEP`: Dominant or Subdominant $\pm 1$ step (85%, `#8B5CF6`)
  - `ENERGY_BOOST`: $+2$ Camelot steps euphoric lift (80%, `#F59E0B`)
  - `ENERGY_DROP`: $-2$ Camelot steps relaxation (75%, `#06B6D4`)
  - `DISSONANT`: Acoustic clash / key friction (25%, `#EF4444`)
- `TempoCompatibility`: `SEAMLESS`, `SMOOTH`, `HALF_DOUBLE_TIME`, `MODERATE_SHIFT`, `TEMPO_JUMP`.
- `TrackHarmonicProfile`:
  - `trackId: String`, `camelotKey: CamelotKey`, `bpm: Int`, `musicalPitchName: String`, `energyLevel: Int`
- `HarmonicTransition`:
  - `sourceTrack: Track`, `targetTrack: Track`, `relationship: HarmonicRelationship`, `compatibilityPercent: Int`, `tempoCompatibility: TempoCompatibility`, `recommendedCrossfadeSeconds: Int`
- `ThemeConfig`:
  - `harmonicMixingEnabled: Boolean = true`
  - `autoHarmonizeQueue: Boolean = false`

#### 2.2 Computation Engine (`:core:domain:audio`)
- `HarmonicMixingEngine`:
  - `deriveHarmonicProfile(track: Track): TrackHarmonicProfile`: Deterministic acoustic hashing deriving Camelot key (1A-12B), BPM (75-165), and energy score.
  - `evaluateTransition(source: TrackHarmonicProfile, target: TrackHarmonicProfile): HarmonicTransition`
  - `calculateCompatibility(key1: CamelotKey, key2: CamelotKey): Pair<HarmonicRelationship, Int>`
  - `calculateTempoCompatibility(bpm1: Int, bpm2: Int): TempoCompatibility`
  - `harmonizeQueue(currentTrack: Track, upcomingQueue: List<Track>): List<Track>`: Greedy nearest-neighbor harmonic path optimization guaranteeing maximal acoustic flow and minimal key clashes.

#### 2.3 Presentation Layer (`:feature:player` & `:feature:settings`)
- `PlayerViewModel`:
  - Exposes `currentHarmonicProfile: StateFlow<TrackHarmonicProfile?>`
  - Exposes `nextTrackHarmonicTransition: StateFlow<HarmonicTransition?>`
  - Function `harmonizeUpcomingQueue()`
- `PlayerScreen`:
  - Interactive Camelot Key & BPM pill on full player (e.g. `[8A • A Minor • 124 BPM]`)
  - Harmonic Match indicator badge for next track with transition tag (e.g. `✨ 90% Relative Key`)
  - "Harmonize Queue" action button in the Queue bottom sheet to align upcoming tracks harmonically.
- `SettingsScreen`:
  - "Harmonic Mixing & DJ Key Intelligence" settings card with toggle for harmonic indicators and automatic queue alignment recommendation.

#### 2.4 Verification & Release Gates
- Unit tests:
  - `HarmonicMixingEngineTest` in `core:domain`.
  - `PlayerViewModelTest` in `feature:player`.
- Quality gates:
  - `./scripts/gate.sh slice`
  - `./scripts/gate.sh final`
  - Release tag: `v6.0-1`

