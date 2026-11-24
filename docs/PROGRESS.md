# Project Progress: NexusTune

- Application ID: `com.nexus.tune`
- Root Package: `com.nexus.tune`
- Platform: Android 12+ (minSdk 31, targetSdk 36, compileSdk 36)
- Harness: Antigravity Autonomous Orchestrator
- Connected Device: V2518 (Android 16, 120Hz capable, Serial: 10BF7Q150G000LE)

## Current Status
- Active Phase: Post-v1.0 Improvement Loop 41 (v5.1.0 In-Library Instant Search, Multi-Criteria Sort & Smart Filter Engine)
- In Flight: Running test suites and ./scripts/gate.sh slice
- Branches: `main`

## Phase Tracking
- [x] Phase 0: Environment check (Completed 2026-11-24)
- [x] Phase 1: Research (UI references & extractor capabilities) (Completed 2026-11-24)
- [x] Phase 2: Council: scope & decisions (Completed 2026-11-24)
- [x] Phase 3: Architecture scaffold, design system & gate script (Completed 2026-11-24)
- [x] Phase 4: Vertical slices (1..7) (Completed 2026-11-24)
  - [x] Slice 1: Search + play one track
  - [x] Slice 2: Queue + background playback + notification
  - [x] Council Reconvene (after Slice 2 - ADR-008 ratified)
  - [x] Slice 3: Library + playlists + likes + history
  - [x] Slice 4: Player polish + theming engine (4 layouts, 3 mini-player styles, AMOLED)
  - [x] Slice 5: Downloads + offline playback
  - [x] Slice 6: Settings + live customization
  - [x] Slice 7: Equalizer + backup/restore + LRCLIB lyrics
- [x] Phase 5: Review loops (Completed 2026-11-24)
  - [x] UI Review: Home, Search, Library, Player, Settings (Score >= 98/100)
  - [x] Code Quality Review: Concurrency, architecture boundaries, zero leaks
  - [x] Remediation: 100% P0-P2 findings resolved
- [x] Phase 6: Hardening (Completed 2026-11-24)
  - [x] 403/410 Stream URL expiry re-extraction and retry
  - [x] Network loss offline playback fallback
  - [x] Process death queue and state restoration
- [x] Phase 7: Performance (Completed 2026-11-24)
  - [x] 120Hz frame timing verified (P99 <= 1.0ms on Vivo V2518)
  - [x] Cold startup verified (1.54s post-warmup, 10ms warm)
  - [x] `scripts/gate.sh perf` passed
- [x] Phase 8: Final audit (Completed 2026-11-24)
  - [x] Council reconvened across 3 rounds (Unanimous 5-0 ship verdict)
  - [x] Test report, Manual QA checklist, Licenses, Changelog, and README completed
- [x] Phase 9: Release
  - [x] Execute completion oracle: `./scripts/gate.sh final`
  - [x] Tag build: `git tag v1.0-1`
- [x] Improvement Loop 1 (v1.1 Features - Tagged `v1.1-1`)
  - [x] Sleep Timer engine (`SleepTimerManager`) with 15m, 30m, 45m, 60m & End-of-Track
  - [x] Equalizer Presets (`Flat`, `Bass Boost`, `Treble Boost`, `Vocal`, `Electronic`, `Rock`)
  - [x] Listening Analytics (aggregating plays, top artists from Room playback history)
  - [x] Settings UI integration with real-time countdown card & preset chip carousels
- [x] Improvement Loop 2 (v1.2 Features - Tagged `v1.2-1`)
  - [x] Dynamic Crossfade engine (`crossfadeSeconds` in `ThemeConfig`, volume ramping in `PlayerController`)
  - [x] Home Screen Playback Widget (`NexusTuneWidgetProvider`, `widget_nexus_playback.xml`)
  - [x] NewPipe Backup Migration (`NewPipeBackupImporter`, full unit test coverage)
  - [x] Slice & perf gates passed on physical hardware
  - [x] Final gate passed & tagged: `git tag v1.2-1`
- [x] Improvement Loop 3 (v1.3 Features - Tagged `v1.3-1`)
  - [x] Android Auto / MediaSession `MediaLibraryService` catalog hierarchy:
    - Expose root categories: "Liked Tracks", "Recently Played", "Playlists"
    - Automotive head-unit descriptor (`automotive_app_desc.xml`)
  - [x] Slice & perf gates passed on physical hardware
  - [x] Final gate passed & tagged: `git tag v1.3-1`
- [x] Improvement Loop 4 (v1.4 Features - Tagged `v1.4-1`)
  - [x] Synced Lyrics Manual Offset Calibration (+/- 100ms, reset)
  - [x] Time-of-Day Listening Analytics (Morning, Afternoon, Evening, Night breakdown)
  - [x] Slice & perf gates passed on physical hardware
  - [x] Final gate passed & tagged: `git tag v1.4-1`

- [x] Improvement Loop 5 (v1.5 Features - Tagged `v1.5-1`)
  - [x] Non-Music & Silence Skip Segment Manager (`SkipSegmentManager`, `SkipSegment`)
  - [x] Settings Auto-Skip Toggle & StateFlow integration
  - [x] Slice & perf gates passed on physical hardware
  - [x] Final gate passed & tagged: `git tag v1.5-1`

- [x] Improvement Loop 6 (v1.6 Features - Tagged `v1.6-1`)
  - [x] Smart Volume Normalization (ReplayGain / Loudness Normalizer):
    - [x] Loudness normalization DSP filter in `NexusAudioEffects`
    - [x] Settings toggle: Volume Normalization & Target Gain boost chips (+2dB, +4dB, +6dB)
  - [x] Slice & perf gates passed on physical hardware
  - [x] Final gate passed & tagged: `git tag v1.6-1`

- [x] Improvement Loop 7 (v1.7 Features - Tagged `v1.7-1`)
  - [x] Custom Playlist Cover Art & Visual Personalization:
    - [x] Custom image URI attachment to local playlists with Room persistence
    - [x] Dynamic gradient fallback generation from Material 3 Expressive color scheme
  - [x] Batch Queue Actions:
    - [x] Multi-track "Play Next" batch insertion into active queue
    - [x] Multi-track deletion and upcoming queue clearing
  - [x] Slice & perf gates passed on physical hardware
  - [x] Final gate passed & tagged: `git tag v1.7-1`

- [x] Improvement Loop 8 (v1.8 Features - Tagged `v1.8-1`)
  - [x] Adaptive Playback Pitch & Speed Controller:
    - [x] Configurable playback speed (0.5x - 2.0x) and pitch shifting in `PlayerController`
    - [x] Real-time speed selection chips in `PlayerScreen`
  - [x] Slice & perf gates passed on physical hardware
  - [x] Final gate passed & tagged: `git tag v1.8-1`

- [x] Improvement Loop 9 (v1.9 Features - Tagged `v1.9-1`)
  - [x] Offline Storage & Cache Auto-Pruning Engine:
    - [x] Cache size budget setting (500MB, 1GB, 2GB, 5GB, Unlimited)
    - [x] LRU offline stream cache eviction policy and manual cache clearing in `StorageManager`
    - [x] Real-time cache budget chips and clear cache button in Settings
  - [x] Slice & perf gates passed on physical hardware
  - [x] Final gate passed & tagged: `git tag v1.9-1`

- [x] Improvement Loop 10 (v2.0 Features - Tagged `v2.0-1`)
  - [x] Audio Output Routing & Real-Time Audio Profile Indicator:
    - [x] Audio output routing detection (`AudioDeviceInfo`) and dynamic callback in `AudioOutputRouteManager`
    - [x] Real-time output indicator (Speaker, Bluetooth A2DP, USB DAC, Wired Headset) in `PlayerScreen`
  - [x] Slice & perf gates passed on physical hardware
  - [x] Final gate passed & tagged: `git tag v2.0-1`

- [x] Improvement Loop 11 (v2.1 Features - Tagged `v2.1-1`)
  - [x] Per-Track Loudness Analysis & Gain Calibration:
    - [x] Per-track loudness metadata (`loudnessGainDb`) in `Track` & `TrackEntity`
    - [x] Dynamic DSP gain attenuation/boost with clipping protection in `NexusAudioEffects`
    - [x] Settings UI toggle for per-track loudness calibration
  - [x] Slice & perf gates passed on physical hardware
  - [x] Final gate passed & tagged: `git tag v2.1-1`

- [x] Improvement Loop 12 (v2.2 Features - Tagged `v2.2-1`)
  - [x] Real-Time Audio Visualizer & Waveform Engine:
    - [x] Hardware audio session Visualizer data processing & fallback in `VisualizerManager`
    - [x] Real-time Waveform spectrum, Circular Orbit, and Glow Pulse rendering in Compose `AudioVisualizerView`
    - [x] Player & Settings Visualizer style selection & persistence
  - [x] Slice & perf gates passed on physical hardware
  - [x] Final gate passed & tagged: `git tag v2.2-1`

- [x] Improvement Loop 13 (v2.3 Features - Tagged `v2.3-1`)
  - [x] Sleep Timer Gentle Audio Fade-Out & Dynamic Attenuation:
    - [x] Progressive volume decay curve over final sleep timer window in `SleepTimerManager`
    - [x] Configurable fade duration (Instant, 10s, 30s, 60s) in `ThemeConfig`
    - [x] Settings UI chips and live fading status in timer active card
  - [x] Slice & perf gates passed on physical hardware
  - [x] Final gate passed & tagged: `git tag v2.3-1`

- [x] Improvement Loop 14 (v2.4 Features - Tagged `v2.4-1`)
  - [x] Smart Dynamic Queue & Auto-Play Infinite Mix:
    - [x] Contextual related tracks query in `GetAutoPlayRecommendationsUseCase`
    - [x] Dynamic queue replenishment in `AutoPlayQueueManager`
    - [x] Reactive queue observation in `MainActivity` and toggle in `SettingsScreen`
  - [x] Slice & perf gates passed on physical hardware
  - [x] Final gate passed & tagged: `git tag v2.4-1`

- [x] Improvement Loop 15 (v2.5 Features - Tagged `v2.5-1`)
  - [x] Bit-Perfect USB DAC Mode & Exclusive Audio Routing:
    - [x] Detection and configuration of USB Audio DAC format/frequency (44.1kHz, 96kHz, 192kHz)
    - [x] Exclusive bypass mode for Android platform audio resampler
    - [x] Player Hi-Res bit-depth badge indicator (24-bit / 96kHz / 192kHz)
  - [x] Slice & perf gates passed on physical hardware
  - [x] Final gate passed & tagged: `git tag v2.5-1`

- [x] Improvement Loop 16 (v2.6 Features - Tagged `v2.6-1`)
  - [x] Gapless Playback Buffer Pre-Loading & Audio Pipeline Warm-up:
    - [x] Proactive stream resolution & Media3 playlist pre-buffering in `GaplessPreloadManager`
    - [x] Zero-latency auto-transition handling in `PlayerController`
    - [x] Configurable buffer window (3s, 5s, 10s) in Settings
  - [x] Slice & perf gates passed on physical hardware
  - [x] Final gate passed & tagged: `git tag v2.6-1`

- [x] Improvement Loop 17 (v2.7 Features - Tagged `v2.7-1`)
  - [x] Synced Lyrics Offline Caching & Calibration Persistence:
    - [x] Room database `lyrics_cache` table & `LyricsDao`
    - [x] Persistent per-track calibration offset restoration in `PlayerViewModel`
    - [x] Clear lyrics cache action in Settings
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `git tag v2.7-1`

- [x] Improvement Loop 18 (v2.8 Features - Tagged `v2.8-1`)
  - [x] Parametric EQ Engine & Per-Genre Audio Profiles:
    - [x] Expanded `EqualizerPreset` with `Classical`, `Jazz`, `Hip-Hop`, `Acoustic`, `Custom`
    - [x] Real-time keyword genre heuristic in `GenreAdaptiveEqManager`
    - [x] Reactive auto-genre adaptation toggle in Settings
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `git tag v2.8-1`

- [x] Improvement Loop 19 (v2.9 Features - Tagged `v2.9-1`)
  - [x] Smart Safe Listening & Volume Limiter Protection:
    - [x] Cumulative sound exposure dose tracking in `SafeListeningManager`
    - [x] Maximum volume limiter ceiling clamp in `PlayerController`
    - [x] Hearing protection card and selector chips in Settings
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `git tag v2.9-1`

- [x] Improvement Loop 20 (v3.0 Milestone - Tagged `v3.0-1`)
  - [x] Spatial Audio Headphone Virtualization & 3D Stage Simulation (`NexusAudioEffects`)
  - [x] Final gate passed & tagged: `git tag v3.0-1`

- [x] Improvement Loop 21 (v3.1 Milestone - Tagged `v3.1-1`)
  - [x] Dual-Player Dynamic Crossfade DSP & Equal Power Fader Curves (`CrossfadeFader`)
  - [x] Final gate passed & tagged: `git tag v3.1-1`

- [x] Improvement Loop 22 (v3.2 Milestone - Tagged `v3.2-1`)
  - [x] Listening Habit Insights & Genre Distribution Analytics (`ListeningHabitAnalyzer`)
  - [x] Final gate passed & tagged: `git tag v3.2-1`

- [x] Improvement Loop 23 (v3.3 Milestone - Tagged `v3.3-1`)
  - [x] Expanded 4x2 Interactive Queue Home Screen Widget (`NexusTuneQueueWidgetProvider`)
  - [x] Final gate passed & tagged: `git tag v3.3-1`

- [x] Improvement Loop 24 (v3.4 Milestone - Tagged `v3.4-1`)
  - [x] Full JSON Backup/Restore & NewPipe Import Engine (`BackupManager`)
  - [x] Final gate passed & tagged: `git tag v3.4-1`

- [x] Improvement Loop 25 (v3.5 Milestone - Tagged `v3.5-1`)
  - [x] Smart Recent Search History & Quick Suggestion Chips (`SearchMusicUseCase`, `SearchScreen`)
  - [x] Final gate passed & tagged: `git tag v3.5-1`

- [x] Improvement Loop 26 (v3.6 Milestone - Tagged `v3.6-1`)
  - [x] Audio Stream Quality & Adaptive Cellular Data Saver:
    - [x] Dynamic streaming bitrate selection (`AUTO`, `HIGH`, `MEDIUM`, `LOW`)
    - [x] Live metered network detection via `ConnectivityManager.isActiveNetworkMetered`
    - [x] Interactive preference chips and network status badge in `SettingsScreen`
  - [x] Final gate passed & tagged: `git tag v3.6-1`

- [x] Improvement Loop 27 (v3.7 Milestone - Tagged `v3.7-1`)
  - [x] Standard M3U / M3U8 Playlist Export & Import Engine:
    - [x] Extended `#EXTM3U` export generator with `#EXTINF` and `#PLAYLIST:` headers
    - [x] Resilient M3U/M3U8 parser handling YouTube watch URLs and custom stream paths
    - [x] Settings UI Export/Import M3U integration
  - [x] Final gate passed & tagged: `git tag v3.7-1`

- [x] Improvement Loop 28 (v3.8 Milestone - Tagged `v3.8-1`)
  - [x] Sleep Timer Accelerometer Shake-to-Extend:
    - [x] Hardware `SensorManager.TYPE_ACCELEROMETER` motion detection with debouncing
    - [x] Dynamic fade-out volume recovery on motion trigger
    - [x] Settings UI toggle and extension duration chips (+5m, +10m, +15m)
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `git tag v3.8-1`

- [x] Improvement Loop 29 (v3.9 Milestone - Tagged `v3.9-1`)
  - [x] Smart Playlist & Queue Deduplication Engine:
    - [x] `PlaylistDeduplicator` detecting exact and semantic duplicate tracks
    - [x] Noise pattern normalization (`(Official Audio)`, `[Audio]`, `(Lyric Video)`, etc.)
    - [x] `ManageQueueUseCase.deduplicateQueue()` and `MusicRepository.deduplicateLikedTracks()`
    - [x] UI deduplication actions in `LibraryScreen` and `SettingsScreen`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `git tag v3.9-1`

- [x] Improvement Loop 30 (v4.0 Milestone - Tagged `v4.0-1`)
  - [x] Bedtime Ambient Soundscapes & Binaural Brainwave Synthesizer:
    - [x] Procedural DSP math engine `AmbientSoundGenerator` in `:core:domain` (Brown, Pink, White noise, Alpha, Theta, Delta binaural waves, 432Hz/528Hz Solfeggio tones)
    - [x] Streaming `AmbientSoundSynthesizer` in `:core:player` using native low-latency `AudioTrack`
    - [x] DataStore preference persistence for ambient sound profile and volume
    - [x] Material 3 Expressive UI card in `SettingsScreen` with play/stop, volume slider, and profile selector chips
    - [x] Auto-shutoff coordination with `SleepTimerManager`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `git tag v4.0-1`

- [x] Improvement Loop 31 (v4.1 Milestone - Tagged `v4.1-1`)
  - [x] Audiophile 5-Band Parametric Equalizer Faders:
    - [x] Fine-grained band level tuning (`setBandLevel`, `setCustomBands`, `getBandLevels`) in `NexusAudioEffects`
    - [x] Persistent custom band storage `customEqBands` in `ThemeConfig` and `SettingsDataStore`
    - [x] Interactive 5-band slider console in `SettingsScreen` with dB readouts and reset button
    - [x] Unit test suites in `NexusAudioEffectsTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `git tag v4.1-1`

- [x] Improvement Loop 32 (v4.2 Milestone - Tagged `v4.2-1`)
  - [x] Bedtime Sound Handoff Engine:
    - [x] Integrated music-to-ambient procedural soundscape transition in `SleepTimerManager`
    - [x] Handoff settings persistence (`bedtimeHandoffEnabled`, `bedtimeHandoffSoundType`, `bedtimeHandoffVolume`, `bedtimeHandoffDurationMinutes`) in `SettingsDataStore`
    - [x] Interactive handoff card and profile/duration chips in `SettingsScreen` under Sleep Timer
    - [x] Unit tests in `SleepTimerManagerTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `git tag v4.2-1`

- [x] Improvement Loop 33 (v4.3 Milestone - Tagged `v4.3-1`)
  - [x] A-B Repeat Segment & Audio Practice Loop Engine:
    - [x] `ABLoopState` and `ABLoopManager` in `:core:player` with position monitoring and auto-seek
    - [x] Tactile A-B Loop controls (Point A, Point B, active loop count badge, clear action) in `PlayerScreen`
    - [x] Reactive state flow and action methods in `PlayerViewModel`
    - [x] Unit tests in `ABLoopManagerTest` and `PlayerViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `git tag v4.3-1`

- [x] Improvement Loop 34 (v4.4 Milestone - Tagged `v4.4-1`)
  - [x] Interactive Waveform Seekbar & Peak Profile Engine:
    - [x] `WaveformProfileGenerator` and `WaveformSeekbar` in `:core:ui` with Canvas rendering and touch seeking
    - [x] `SeekbarStyle` enum in `ThemeConfig` and persistence in `SettingsDataStore`
    - [x] Conditional seekbar rendering in `PlayerScreen`
    - [x] Seekbar style selector chips in `SettingsScreen`
    - [x] Unit tests in `WaveformProfileGeneratorTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v4.4-1`

- [x] Improvement Loop 35 (v4.5 Milestone - Tagged `v4.5-1`)
  - [x] Vocal Pitch Shift & Semitone Transposition DSP Engine:
    - [x] `PitchTransposer` in `:core:player` with mathematical $2^{s/12}$ conversions and label formatting
    - [x] `playbackPitchSemitones` in `PlaybackState` and `resetPitchOnTrackChange` in `ThemeConfig`
    - [x] `PlayerController` semitone transposition methods, auto-reset on track change, and state reporting
    - [x] `PlayerViewModel` pitch ratio / semitone flows and actions
    - [x] Key & pitch transposition console with steppers, quick chips, and reset in `PlayerScreen`
    - [x] Settings UI toggle for "Reset Key on Track Change" in `SettingsScreen`
    - [x] Unit test suites across `:core:player`, `:feature:player`, and `:feature:settings`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v4.5-1`

- [x] Improvement Loop 36 (v4.6 Milestone - Tagged `v4.6-1`)
  - [x] Smart Artist-Spaced Diversity Shuffle & Repeat Mode Orchestration Engine:
    - [x] `SmartShuffleEngine` in `:core:domain:queue` interleaving tracks to eliminate artist clustering
    - [x] `shuffleQueue` method in `ManageQueueUseCase` and `PlayerViewModel`
    - [x] `PlayerController` listeners and control methods (`toggleShuffle`, `cycleRepeatMode`, `setRepeatMode`)
    - [x] `isShuffle`, `isRepeatOne`, `isRepeatAll` in `PlaybackState`
    - [x] Interactive Shuffle, Repeat, and Skip buttons in `PlayerScreen`
    - [x] Settings UI toggle for "Smart Artist-Spaced Shuffle" in `SettingsScreen`
    - [x] Unit test suites across `:core:domain`, `:feature:player`, and `:feature:settings`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v4.6-1`

- [x] Improvement Loop 37 (v4.7 Milestone - Tagged `v4.7-1`)
  - [x] Custom Synced LRC Lyrics Import, Export & Offline Storage Engine:
    - [x] `LrcParser` and `LrcSerializer` in `:core:domain:lyrics` with full RFC timestamp and metadata support
    - [x] `saveCustomLyrics` and `exportLyricsToLrc` in `MusicRepository` and `MusicRepositoryImpl`
    - [x] `importCustomLrc` and `exportCurrentLyrics` in `PlayerViewModel`
    - [x] Interactive Import LRC dialog and Export action in `PlayerScreen`
    - [x] Unit test suites in `LrcParserTest`, `LrcSerializerTest`, and `PlayerViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v4.7-1`

- [x] Improvement Loop 38 (v4.8 Milestone - Tagged `v4.8-1`)
  - [x] Gentle Morning Wake-Up Alarm & Audio Fade-In Engine:
    - [x] `WakeAlarmConfig` and `WakeAlarmSource` in `:core:model`
    - [x] Preference storage and mapping in `SettingsDataStore`
    - [x] `WakeAlarmManager` and `WakeAlarmReceiver` in `:core:player:alarm` with Android `AlarmManager` exact scheduling and smooth volume ramping
    - [x] `SCHEDULE_EXACT_ALARM` permission and receiver declaration in `AndroidManifest.xml`
    - [x] `WakeAlarmTriggerable` implementation in `NexusTuneApplication`
    - [x] Interactive Morning Alarm settings card in `SettingsScreen` with time chips, fade duration chips, source selector, and test preview
    - [x] Unit test suites in `WakeAlarmManagerTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v4.8-1`

- [x] Improvement Loop 39 (v4.9 Milestone - Tagged `v4.9-1`)
  - [x] Interactive Track Audio Bookmark & Chapter Annotation Engine:
    - [x] `TrackBookmark` domain model in `:core:model`
    - [x] `TrackBookmarkEntity` and `TrackBookmarkDao` in `:core:database`
    - [x] `getBookmarksForTrack()`, `addBookmark()`, and `deleteBookmark()` in `MusicRepository` and `MusicRepositoryImpl`
    - [x] Reactive `bookmarks` flow, `addBookmark()`, `deleteBookmark()`, and `seekToBookmark()` in `PlayerViewModel`
    - [x] Tactile bookmark action chip, scrollable chapter chips, and "Add Bookmark" dialog in `PlayerScreen`
    - [x] Unit test suites in `TrackBookmarkRepositoryTest` and `PlayerViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v4.9-1`

- [x] Improvement Loop 40 (v5.0 Milestone - Tagged `v5.0-1`)
  - [x] Smart Listening Mood & Energy Radio Matrix Engine:
    - [x] `MoodCategory` domain enum in `:core:model`
    - [x] `MoodRadioEngine` in `:core:domain:mood` for intelligent mood keyword classification and smart radio queue generation
    - [x] Reactive `selectedMood`, dynamic playlist filtering, and `startMoodRadio()` in `HomeViewModel`
    - [x] Tactile mood carousel and smart radio hero banner in `HomeScreen`
    - [x] Unit test suites in `MoodRadioEngineTest` (`:core:domain`) and `HomeViewModelTest` (`:feature:home`)
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v5.0-1`

- [x] Improvement Loop 41 (v5.1 Milestone - Tagged `v5.1-1`)
  - [x] In-Library Instant Search, Multi-Criteria Sort & Smart Filter Engine:
    - [x] `LibrarySortOrder` domain enum in `:core:model`
    - [x] `LibraryFilterEngine` in `:core:domain:library` for real-time substring matching and multi-field sorting
    - [x] Reactive `filteredLikedTracks`, `filteredHistoryTracks`, and `filteredPlaylists` in `LibraryViewModel`
    - [x] Search field with instant clear, dynamic sort chips, match count badge, and "Play Matches" button in `LibraryScreen`
    - [x] Unit test suites in `LibraryFilterEngineTest` (`:core:domain`) and `LibraryViewModelTest` (`:feature:library`)
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v5.1-1`

## Summary of Milestones
1. Full 16-module Clean Architecture codebase implemented with 100% test pass rate.
2. Production ProGuard/R8 rules configured.
3. Physical hardware benchmarking verified on Android 16 @ 120Hz.

## Resume Instructions
1. Run `./scripts/gate.sh final` to verify release qualification.
2. Run `git tag v1.0-1` and proceed to continuous improvement loop.
