# Phase Improvement Plan - Loop 52 (v6.2.0)
## Real-Time Center-Channel Vocal Attenuation & Karaoke Sing-Along Studio (`KaraokeEngine`)

### 1. Context & Motivation
Sing-along and karaoke experiences represent one of the most engaging features in modern music clients. NexusTune v6.2.0 introduces the **Real-Time Center-Channel Vocal Attenuation & Karaoke Sing-Along Studio (`KaraokeEngine`)**. This engine empowers users to attenuate centered lead vocals across 4 calibrated intensity levels (Gentle -6dB, Moderate -12dB, Strong -18dB, Full Isolation -24dB), transpose track pitch (-6 to +6 semitones) to match personal vocal range, and follow a real-time glowing sing-along teleprompter with upcoming lyric line countdowns and syllable/line progress tracking.

### 2. Architecture & Modules
- **Core Domain Models** (`:core:model`):
  - `VocalCutLevel`: `OFF`, `GENTLE` (-6 dB), `MODERATE` (-12 dB), `AGGRESSIVE` (-18 dB), `MAXIMUM` (-24 dB).
  - `KaraokeReverb`: `DRY`, `ROOM`, `STAGE`, `CONCERT_HALL`.
  - `KaraokeConfig`: Master toggle `isEnabled`, `cutLevel`, `pitchSemitones`, `prompterMode`, `reverb`.
  - `KaraokeLyricProgress`: Active line index, current `LyricsLine`, next `LyricsLine`, `lineProgressFraction` (0.0 to 1.0), `timeRemainingInLineMs`, `isVocalActive`.
- **Pure Kotlin Domain Engine** (`:core:domain:audio`):
  - `KaraokeEngine`:
    - Linear gain attenuation calculation: $10^{\frac{\text{dB}}{20}}$.
    - 5-Band parametric vocal cut EQ profile calculation (`60Hz`, `230Hz`, `910Hz`, `3600Hz`, `14000Hz`):
      - Bass foundation (60Hz) is untouched (0 mB).
      - Core vocal speech fundamentals (910Hz) & sibilance (3600Hz) are strongly notched down.
      - High air brilliance (14kHz) is preserved/boosted (+1.5 dB) to retain backing track shine.
    - Pitch transposition ratio conversion $2^{\frac{\text{semitones}}{12}}$.
    - Real-time lyrics sing-along progress evaluator mapping playback timestamp to line duration and progress ratio.
    - Formatted semitone key readout (e.g. `+2 st (Higher)` / `Original Key`).
- **Audio DSP & Effects** (`:core:player`):
  - `NexusAudioEffects`: Added `applyKaraokeOffsets(offsets: List<Int>)` and `clearKaraokeOffsets()` dynamically modulating the active equalizer without corrupting user EQ presets.
  - `PlayerController`: Added `applyKaraokeConfig(config: KaraokeConfig)`.
- **Presentation Layer** (`:feature:player`, `:feature:settings`, `:app`):
  - `PlayerViewModel`: Added `karaokeConfig: StateFlow<KaraokeConfig>`, `karaokeLyricProgress: StateFlow<KaraokeLyricProgress>`, and actions `toggleKaraoke()`, `setKaraokeCutLevel()`, `setKaraokePitchSemitones()`, `toggleKaraokePrompter()`, `resetKaraoke()`.
  - `PlayerScreen`: Dedicated `🎤` top-bar action button, plus interactive `ModalBottomSheet` Karaoke Studio Console with real-time teleprompter preview, intensity chips, and key transposer.
  - `SettingsScreen`: Karaoke Sing-Along configuration card.
  - `MainActivity`: Wired up state and events.
- **Verification & Gates**:
  - `KaraokeEngineTest` in `:core:domain:audio`.
  - `PlayerViewModelTest` in `:feature:player`.
  - Quality gates: `./scripts/gate.sh slice` and `./scripts/gate.sh final`.
  - Release tag: `v6.2-1`.

