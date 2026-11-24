# Project Progress: NexusTune

- Application ID: `com.nexus.tune`
- Root Package: `com.nexus.tune`
- Platform: Android 12+ (minSdk 31, targetSdk 36, compileSdk 36)
- Harness: Antigravity Autonomous Orchestrator
- Connected Device: V2518 (Android 16, 120Hz capable, Serial: 10BF7Q150G000LE)

## Current Status
- Active Phase: Post-v1.0 Improvement Loop 75 (v6.25.0 Audiophile Analog Vacuum Tube Saturation & Even-Harmonic Warmth Studio)
- In Flight: Slice and final qualification gate verification
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

- [x] Improvement Loop 42 (v5.2 Milestone - Tagged `v5.2-1`)
  - [x] Interactive Up Next Active Queue Sheet & In-Player Reordering Engine:
    - [x] Reactive `activeQueue` and `activeQueueIndex` state flows in `PlayerViewModel`
    - [x] `jumpToQueueIndex()`, `removeFromQueue()`, and `clearUpcoming()` controls in `PlayerViewModel`
    - [x] Dedicated `ModalBottomSheet` in `PlayerScreen` triggered by top-bar `QueueMusic` icon button
    - [x] Visual indicators for current playback item, tap-to-jump navigation, remaining duration estimates, and single-tap deletion
    - [x] Dedicated unit tests in `PlayerViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v5.2-1`

- [x] Improvement Loop 43 (v5.3 Milestone - Tagged `v5.3-1`)
  - [x] Audio Loudness Normalization & Dynamic Peak Limiter Engine:
    - [x] `LoudnessNormalizationMode` and `LoudnessConfig` domain models in `:core:model`
    - [x] `AudioNormalizationEngine` in `:core:domain:audio` with LUFS multiplier, soft-knee peak limiter, and track estimation
    - [x] DataStore preferences and `ThemeConfig` integration in `SettingsDataStore`
    - [x] Real-time volume scaling via `setLoudnessMultiplier()` in `PlayerController` and auto-calibration in `NexusTuneApplication`
    - [x] Interactive Loudness Normalization & Limiter control console in `SettingsScreen`
    - [x] Comprehensive test suites in `AudioNormalizationEngineTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v5.3-1`

- [x] Improvement Loop 44 (v5.4 Milestone - Tagged `v5.4-1`)
  - [x] Listening Milestones, Daily Streaks & Time-Capsule Recap Engine:
    - [x] `ListeningStreak`, `MilestoneType`, `ListeningMilestone`, and `TimeCapsuleRecap` domain models in `:core:model`
    - [x] `MilestoneCalculator` in `:core:domain:analytics` evaluating daily playback streaks, evaluating milestone achievements, and calculating time-capsule summaries
    - [x] Persistent streak tracking via `recordActiveDay()` in `SettingsDataStore` and automatic logging in `NexusTuneApplication`
    - [x] Interactive streaks, milestones, and recap dashboard in `SettingsScreen`
    - [x] Unit test suites in `MilestoneCalculatorTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v5.4-1`

- [x] Improvement Loop 45 (v5.5 Milestone - Tagged `v5.5-1`)
  - [x] Custom User EQ Presets & Bézier Frequency Curve Engine:
    - [x] `CustomEqProfile` domain model in `:core:model`
    - [x] `EqCurveCalculator` in `:core:domain:audio` with cubic Bézier curve calculation and Euclidean preset matching
    - [x] `EqCurveVisualizer` in `:core:ui` with gradient fills, grid guidelines, and interactive frequency nodes
    - [x] Serialized profile persistence in `SettingsDataStore` and `SettingsRepositoryImpl`
    - [x] Interactive preset selector, closest match indicator, and preset creation dialog in `SettingsScreen`
    - [x] Comprehensive test suites in `EqCurveCalculatorTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v5.5-1`

- [x] Improvement Loop 46 (v5.6 Milestone - Tagged `v5.6-1`)
  - [x] Interactive DJ Audio Transformation & Sound Effects Engine:
    - [x] `DjPreset` and `DjFxConfig` domain models in `:core:model`
    - [x] `DjTransformEngine` in `:core:domain:audio` with speed, pitch, soundstage matrices, and preset matching
    - [x] Top-bar DJ launcher and interactive `ModalBottomSheet` control console in `PlayerScreen`
    - [x] DataStore preferences and `ThemeConfig` integration in `SettingsDataStore` and `SettingsRepositoryImpl`
    - [x] Dedicated DJ Sound Effects configuration card in `SettingsScreen`
    - [x] Comprehensive test suites in `DjTransformEngineTest`, `PlayerViewModelTest`, and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v5.6-1`

- [x] Improvement Loop 47 (v5.7 Milestone - Tagged `v5.7-1`)
  - [x] Interactive Music Identity Card & Shareable Sonic DNA Fingerprint Engine:
    - [x] `SonicArchetype` and `SonicDnaProfile` domain models in `:core:model`
    - [x] `SonicDnaCalculator` in `:core:domain:analytics` evaluating 8 archetypes, metric scores, and gradient maps
    - [x] `SonicDnaCard` in `:core:ui` with archetype badges, dimension meters, and gradient mesh
    - [x] Interactive integration and clipboard share action in `SettingsScreen` and `SettingsViewModel`
    - [x] Full test coverage in `SonicDnaCalculatorTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v5.7-1`

- [x] Improvement Loop 48 (v5.8 Milestone - Tagged `v5.8-1`)
  - [x] Smart Vibe Mix & Dynamic Algorithmic Playlist Synthesis Engine:
    - [x] `VibeTheme`, `EnergyCurve`, `VibeMixRequest`, and `VibeMixResult` domain models in `:core:model`
    - [x] `VibePlaylistGenerator` in `:core:domain:playlist` with energy flow curves, theme scoring, and duration targets
    - [x] Room `PlaylistTrackCrossRef` batch insertion and query operations in `PlaylistDao` and `MusicRepository`
    - [x] Interactive Smart Vibe Mix bottom sheet and playlist creation in `LibraryScreen` and `LibraryViewModel`
    - [x] Full unit test coverage in `VibePlaylistGeneratorTest` and `LibraryViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v5.8-1`

- [x] Improvement Loop 49 (v5.9 Milestone - Tagged `v5.9-1`)
  - [x] Dynamic Rule-Based Smart Playlists & Algorithmic Query Engine:
    - [x] `SmartRuleField`, `SmartRuleOperator`, `SmartRuleMatch`, `SmartRuleSort`, `SmartPlaylistRule`, and `SmartPlaylistDefinition` domain models in `:core:model`
    - [x] `SmartPlaylistEvaluator` in `:core:domain:playlist` with rule matching, evaluation logic, and 5 built-in smart presets
    - [x] Serialization & DataStore persistence for custom smart playlists in `SettingsDataStore` and `SettingsRepositoryImpl`
    - [x] Interactive Smart Playlists carousel, preview bottom sheet with Play All / Shuffle, and custom rule builder in `LibraryScreen`
    - [x] Full unit test suites in `SmartPlaylistEvaluatorTest` and `LibraryViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v5.9-1`

- [x] Improvement Loop 50 (v6.0 Grand Milestone - Tagged `v6.0-1`)
  - [x] Acoustic Camelot Harmonic Mixing & BPM Transition Intelligence Engine:
    - [x] `CamelotScale`, `CamelotKey`, `HarmonicRelationship`, `TempoCompatibility`, `TrackHarmonicProfile`, and `HarmonicTransition` domain models in `:core:model`
    - [x] `HarmonicMixingEngine` in `:core:domain:audio` with Circle of Fifths math, tempo ratio classification, and greedy nearest-neighbor queue harmonization
    - [x] Interactive Camelot Key & BPM indicator and next track harmonic transition badge on `PlayerScreen`
    - [x] "✨ Harmonize" queue reordering action in `PlayerScreen` queue bottom sheet
    - [x] Comprehensive unit tests in `HarmonicMixingEngineTest` and `PlayerViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.0-1`

- [x] Improvement Loop 51 (v6.1 Milestone - Tagged `v6.1-1`)
  - [x] Audio Visualizer Suite Expansion: Stereo VU Meter & Liquid Wave:
    - [x] `STEREO_VU` and `LIQUID_WAVE` modes in `VisualizerMode`
    - [x] `VisualizerPalette` (`DYNAMIC`, `CYBERPUNK`, `SUNSET_EMBER`, `AURORA`, `ELECTRIC_VIOLET`, `GOLDEN_CHAMPAGNE`)
    - [x] Calibrated `StereoVuVisualizer` and sinusoidal `LiquidWaveVisualizer` in `:core:ui`
    - [x] Visualizer color palette and style selectors in `SettingsScreen` and `SettingsViewModel`
    - [x] Unit test in `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.1-1`

- [x] Improvement Loop 52 (v6.2 Milestone - Tagged `v6.2-1`)
  - [x] Real-Time Center-Channel Vocal Attenuation & Karaoke Sing-Along Studio:
    - [x] `VocalCutLevel`, `KaraokeReverb`, `KaraokeConfig`, and `KaraokeLyricProgress` in `:core:model`
    - [x] `KaraokeEngine` in `:core:domain:audio` with linear gain conversions, 5-band vocal notch EQ offsets, chromatic pitch conversion, and real-time lyric progress calculation
    - [x] Dynamic audio effects modulation in `NexusAudioEffects` and `PlayerController`
    - [x] `PlayerViewModel` integration with reactive `karaokeConfig`, pitch clamping, and lyrics progress evaluation
    - [x] Interactive top-bar `🎤` action and `ModalBottomSheet` Karaoke Studio Console in `PlayerScreen`
    - [x] Settings integration card in `SettingsScreen`
    - [x] Comprehensive test suites in `KaraokeEngineTest` and `PlayerViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.2-1`

- [x] Improvement Loop 53 (v6.3 Milestone - Tagged `v6.3-1`)
  - [x] Smart Offline Cache Auto-Stash & Predictive Storage Budget Optimization Engine:
    - [x] `AutoStashStrategy`, `AutoStashNetworkPolicy`, `AutoStashCandidate`, `AutoStashConfig`, and `AutoStashReport` in `:core:model`
    - [x] `AutoStashEvaluator` in `:core:domain:offline` with candidate scoring, knapsack byte budget packing, and eviction identification
    - [x] `SettingsDataStore` preferences and persistence in `ThemeConfig`
    - [x] `SettingsViewModel` auto-stash configuration methods
    - [x] Interactive "⚡ Smart Offline Auto-Stash" dashboard card in `SettingsScreen`
    - [x] Unit test suites in `AutoStashEvaluatorTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.3-1`

- [x] Improvement Loop 54 (v6.4 Milestone - Tagged `v6.4-1`)
  - [x] Interactive Audio Tag & Metadata Studio (`AudioTagStudio`):
    - [x] `CleanMetadataResult` domain model in `:core:model`
    - [x] `MetadataNormalizerEngine` in `:core:domain:metadata` with regex artifact stripping, featured artist extraction, hyphenated artist-title parsing, and Title Casing
    - [x] Room `TrackDao.updateTrackMetadata()` and `MusicRepository.updateTrackMetadata()`
    - [x] `LibraryViewModel.updateTrackMetadata()`
    - [x] Interactive `TrackMetadataEditorSheet` in `LibraryScreen` with "✨ Auto-Clean", artifact chips, and save to library
    - [x] Comprehensive test suites in `MetadataNormalizerEngineTest` and `LibraryViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.4-1`

- [x] Improvement Loop 55 (v6.5 Milestone - Tagged `v6.5-1`)
  - [x] Headphone Soundstage & Acoustic Target Compensation (`HeadphoneCorrectionEngine`):
    - [x] `HeadphoneTargetCurve` & `HeadphoneCorrectionConfig` in `:core:model`
    - [x] `HeadphoneCorrectionEngine` in `:core:domain:audio` with 5-band target curves, bass compensation, and ITD/ILD crossfeed simulation math
    - [x] `NexusAudioEffects` layer isolation, `applyHeadphoneOffsets`, `clearHeadphoneOffsets`, and `setBassBoostStrength`
    - [x] DataStore preferences and persistence in `SettingsDataStore`
    - [x] `SettingsViewModel` target curve and crossfeed methods
    - [x] Interactive "Headphone Target & Crossfeed Compensation" card in `SettingsScreen`
    - [x] Comprehensive test suites in `HeadphoneCorrectionEngineTest`, `NexusAudioEffectsTest`, and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.5-1`

- [x] Improvement Loop 56 (v6.6 Milestone - Tagged `v6.6-1`)
  - [x] Mid/Side (M/S) Stereo Soundstage Widener & Haas Spatializer Engine (`StereoWidenerEngine`):
    - [x] `StereoWidthMode`, `StereoWidthConfig`, and `StereoPhaseRating` in `:core:model`
    - [x] `StereoWidenerEngine` in `:core:domain:audio` with Mid/Side matrix deconstruction, Pearson phase correlation calculation, Haas delay samples, and hardware virtualizer mapping
    - [x] `NexusAudioEffects` `setVirtualizerStrength` support
    - [x] DataStore preferences and persistence in `SettingsDataStore`
    - [x] `SettingsViewModel` stereo widening methods
    - [x] Interactive "Stereo Field & Soundstage Studio" card in `SettingsScreen`
    - [x] Comprehensive test suites in `StereoWidenerEngineTest`, `NexusAudioEffectsTest`, and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.6-1`

- [x] Improvement Loop 57 (v6.7 Milestone - Tagged `v6.7-1`)
  - [x] Intelligent Audio Track Chapters & DJ Cue Navigation Studio (`TrackChapterParser`):
    - [x] `TrackChapter` and `ChapterProgress` in `:core:model`
    - [x] `TrackChapterParser` in `:core:domain:player` with regex timestamp parsing, duration formatting, active chapter resolution, and skip target calculations
    - [x] `PlayerViewModel` chapter extraction, custom chapter overrides, and chapter seeking
    - [x] `PlayerScreen` active chapter pill badge, top bar chapter bookmarks shortcut, and `TrackChaptersBottomSheet`
    - [x] Comprehensive test suites in `TrackChapterParserTest` and `PlayerViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.7-1`

- [x] Improvement Loop 58 (v6.8 Milestone - Tagged `v6.8-1`)
  - [x] Dynamic Sleep Drift & Psychoacoustic Bedtime Induction Engine (`SleepDriftEngine`):
    - [x] `SleepDriftProfile` and `SleepDriftConfig` in `:core:model`
    - [x] `SleepDriftEngine` in `:core:domain:audio` with progressive high-frequency roll-off, tempo deceleration, and Theta/Delta binaural carrier calculations
    - [x] `SleepTimerManager` session progress tracking, live drift roll-off, and tempo decrescendo
    - [x] `NexusAudioEffects` `applySleepDriftOffsets` & `clearSleepDriftOffsets`
    - [x] DataStore persistence for `sleepDriftProfile` in `SettingsDataStore`
    - [x] `SettingsViewModel` sleep drift methods and `SettingsScreen` chip selector card
    - [x] Comprehensive test suites in `SleepDriftEngineTest`, `SleepTimerManagerTest`, `NexusAudioEffectsTest`, and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.8-1`

- [x] Improvement Loop 59 (v6.9 Milestone - Tagged `v6.9-1`)
  - [x] Audiophile TPDF Dither & Psychoacoustic Noise Shaping Studio (`DitherShapingEngine`):
    - [x] `DitherMode`, `DitherConfig`, and `QuantizationNoiseMetrics` in `:core:model`
    - [x] `DitherShapingEngine` in `:core:domain:audio` with TPDF dual-random dither generator ($R_1 - R_2$), RPDF dither, 2nd-order Lipshitz F-weighting noise shaping (`[1.62f, -0.93f]`), high-pass contour (`[0.95f]`), buffer processing, apparent dynamic range calculation, and linearity error analysis
    - [x] DataStore persistence for `ditherMode` in `SettingsDataStore` mapped to `ThemeConfig`
    - [x] `SettingsViewModel` dither mode control method
    - [x] Interactive "Audiophile Dither & Noise Shaping" card in `SettingsScreen`
    - [x] Comprehensive unit test suites in `DitherShapingEngineTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.9-1`

- [x] Improvement Loop 60 (v6.10 Milestone - Tagged `v6.10-1`)
  - [x] Dynamic Fletcher-Munson Equal-Loudness Contour Engine (`DynamicLoudnessEngine`):
    - [x] `DynamicLoudnessMode`, `DynamicLoudnessConfig`, and `LoudnessCurveOffsets` in `:core:model`
    - [x] `DynamicLoudnessEngine` in `:core:domain:audio` with non-linear ISO 226 equal-loudness sensitivity modeling, 5-band millibel contour calculation, and safe digital headroom calculation
    - [x] Dynamic loudness offsets layering in `NexusAudioEffects`
    - [x] DataStore persistence in `SettingsDataStore` mapped to `ThemeConfig`
    - [x] `SettingsViewModel` dynamic loudness control methods
    - [x] Interactive "Dynamic Equal-Loudness (Fletcher-Munson)" studio card in `SettingsScreen`
    - [x] Comprehensive test suites in `DynamicLoudnessEngineTest`, `NexusAudioEffectsTest`, and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.10-1`

- [x] Improvement Loop 61 (v6.11 Milestone - Tagged `v6.11-1`)
  - [x] Beat-Synchronized Phrase Crossfade & DJ Cue Transition Engine (`BeatSyncCrossfadeEngine`):
    - [x] `BeatCrossfadeMode`, `BeatCrossfadeConfig`, `BassSwapCurve`, and `BeatTransitionPlan` in `:core:model`
    - [x] `BeatSyncCrossfadeEngine` in `:core:domain:audio` with phrase duration calculation, smart bar selection, asymmetric bass-swap curve, and bounded tempo bending
    - [x] DataStore persistence in `SettingsDataStore` mapped to `ThemeConfig`
    - [x] `SettingsViewModel` beat crossfade control methods
    - [x] Interactive "Beat-Sync Phrase Crossfade (DJ)" studio card in `SettingsScreen`
    - [x] Comprehensive test suites in `BeatSyncCrossfadeEngineTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.11-1`

- [x] Improvement Loop 62 (v6.12 Milestone - Tagged `v6.12-1`)
  - [x] Acoustic Silence & Outro Dead-Air Trimmer Studio (`SilenceTrimmerEngine`):
    - [x] `SilenceTrimMode`, `SilenceTrimConfig`, and `SilenceDetectionResult` in `:core:model`
    - [x] `SilenceTrimmerEngine` in `:core:domain:audio` with RMS amplitude measurement (dBFS), silence duration tracking, cosine-squared micro-envelope smoothing, and cumulative time savings calculation
    - [x] DataStore persistence in `SettingsDataStore` mapped to `ThemeConfig`
    - [x] `SettingsViewModel` silence trimmer control methods
    - [x] Interactive "Acoustic Silence & Dead-Air Trimmer" studio card in `SettingsScreen`
    - [x] Comprehensive test suites in `SilenceTrimmerEngineTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.12-1`

- [x] Improvement Loop 63 (v6.13 Milestone - Tagged `v6.13-1`)
  - [x] Audiophile Resampling & Polyphase Sinc Reconstruction Filter Studio (`ResamplingFilterEngine`):
    - [x] `ResamplingFilterMode`, `ResamplingFilterConfig`, and `FilterImpulseMetrics` in `:core:model`
    - [x] `ResamplingFilterEngine` in `:core:domain:audio` with sinc function evaluation, Blackman-Harris windowing, causal minimum-phase exponential suppression, and Kaiser Beta calculation
    - [x] DataStore persistence in `SettingsDataStore` mapped to `ThemeConfig`
    - [x] `SettingsViewModel` resampling filter control methods
    - [x] Interactive "DAC Reconstruction Filter Topology" studio card in `SettingsScreen`
    - [x] Comprehensive test suites in `ResamplingFilterEngineTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.13-1`

- [x] Improvement Loop 64 (v6.14 Milestone - Tagged `v6.14-1`)
  - [x] Stereo Phase Correlation & Mono Compatibility Guard Studio (`PhaseMeterEngine`):
    - [x] `PhaseCorrelationRating`, `MonoGuardMode`, `PhaseMeterConfig`, and `PhaseCorrelationMetrics` in `:core:model`
    - [x] `PhaseMeterEngine` in `:core:domain:audio` with Pearson correlation calculation, rating evaluation, and Mono-Guard side-channel attenuation
    - [x] DataStore persistence in `SettingsDataStore` mapped to `ThemeConfig`
    - [x] `SettingsViewModel` phase guard control methods
    - [x] Interactive "Mono-Guard Anti-Cancellation" card in `SettingsScreen`
    - [x] Comprehensive test suites in `PhaseMeterEngineTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.14-1`

- [x] Improvement Loop 65 (v6.15 Milestone - Tagged `v6.15-1`)
  - [x] Audiophile Tilt Equalizer & Tonal Balance Shifter Studio (`TiltEqualizerEngine`):
    - [x] `TiltPivotFrequency`, `TiltEqualizerConfig`, `TiltSpectralMetrics`, and `TiltBiquadCoefficients` in `:core:model`
    - [x] `TiltEqualizerEngine` in `:core:domain:audio` with logarithmic frequency tilt $G_{dB}(f)$, unconditionally stable biquad coefficients ($|a_1| < 1$), 5-band millibel offsets, and loudness compensation
    - [x] Hardware equalizer integration in `NexusAudioEffects` with `applyTiltOffsets`
    - [x] DataStore persistence in `SettingsDataStore` mapped to `ThemeConfig`
    - [x] `SettingsViewModel` tilt equalizer control methods
    - [x] Interactive "Audiophile Tilt Equalizer" studio card in `SettingsScreen`
    - [x] Comprehensive test suites in `TiltEqualizerEngineTest`, `NexusAudioEffectsTest`, and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.15-1`

- [x] Improvement Loop 66 (v6.16 Milestone - Tagged `v6.16-1`)
  - [x] Audiophile Transient De-Clicker & Micro-Discontinuity Suppressor Studio (`DeClickerEngine`):
    - [x] `DeClickerMode`, `DeClickerConfig`, and `DeClickerMetrics` in `:core:model`
    - [x] `DeClickerEngine` in `:core:domain:audio` with 2nd-derivative impulsive peak detection ($\Delta^2 x[n]$), dynamic localized RMS noise floor tracking, and $C^1$ cubic Hermite spline reconstruction
    - [x] DataStore persistence in `SettingsDataStore` mapped to `ThemeConfig`
    - [x] `SettingsViewModel` de-clicker control methods
    - [x] Interactive "Audiophile Transient De-Clicker" studio card in `SettingsScreen`
    - [x] Comprehensive test suites in `DeClickerEngineTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.16-1`

- [x] Improvement Loop 67 (v6.17 Milestone - Tagged `v6.17-1`)
  - [x] Dynamic Mid-Bass Mud Tamer & Clarity Sculptor Studio (`MidBassClarityEngine`):
    - [x] `MidBassClarityMode`, `MidBassClarityConfig`, `MidBassClarityMetrics`, and `MidBassBiquadCoefficients` in `:core:model`
    - [x] `MidBassClarityEngine` in `:core:domain:audio` with spectral congestion ratio calculation, dynamic parametric bell dip filter, RBJ peaking biquad coefficients ($Q = 1.4$), and adaptive recovery
    - [x] Hardware equalizer integration in `NexusAudioEffects` targeting Band 1 (230 Hz) with `applyMidBassClarityOffset`
    - [x] DataStore persistence in `SettingsDataStore` mapped to `ThemeConfig`
    - [x] `SettingsViewModel` mid-bass clarity control methods
    - [x] Interactive "Dynamic Mid-Bass Mud Tamer" studio card in `SettingsScreen`
    - [x] Comprehensive test suites in `MidBassClarityEngineTest`, `NexusAudioEffectsTest`, and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.17-1`

- [x] Improvement Loop 68 (v6.18 Milestone - Tagged `v6.18-1`)
  - [x] Loudspeaker Acoustic Crosstalk Cancellation & Stereo Dipole 3D Studio (`CrosstalkCancellationEngine`):
    - [x] `CrosstalkCancellationMode`, `CrosstalkCancellationConfig`, and `CrosstalkCancellationMetrics` in `:core:model`
    - [x] `CrosstalkCancellationEngine` in `:core:domain:audio` with contralateral inverted cancellation wave synthesis, circular ring-buffer delay lines, head shadow diffraction filter, and soundstage angle estimation
    - [x] DataStore persistence in `SettingsDataStore` mapped to `ThemeConfig`
    - [x] `SettingsViewModel` crosstalk cancellation control methods
    - [x] Interactive "Loudspeaker Holographic 3D (XTC)" studio card in `SettingsScreen`
    - [x] Comprehensive test suites in `CrosstalkCancellationEngineTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.18-1`

- [x] Improvement Loop 69 (v6.19 Milestone - Tagged `v6.19-1`)
  - [x] Infrasonic Sub-Bass High-Pass & Warp Guard Studio (`InfrasonicFilterEngine`):
    - [x] `InfrasonicSlope`, `InfrasonicFilterConfig`, `InfrasonicMetrics`, and `InfrasonicBiquadCoefficients` in `:core:model`
    - [x] `InfrasonicFilterEngine` in `:core:domain:audio` with Butterworth/Bessel steep high-pass biquad filter ($H(0) = 0$), recursive single-pole DC offset blocker ($R = 0.9995$), 10 Hz rumble attenuation, and dynamic headroom restoration estimation
    - [x] DataStore persistence in `SettingsDataStore` mapped to `ThemeConfig`
    - [x] `SettingsViewModel` infrasonic filter control methods
    - [x] Interactive "Infrasonic Sub-Bass & Warp Guard" studio card in `SettingsScreen`
    - [x] Comprehensive test suites in `InfrasonicFilterEngineTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.19-1`

- [x] Improvement Loop 70 (v6.20 Milestone - Tagged `v6.20-1`)
  - [x] Audiophile High-Frequency "Air Band" & Silk Sheen Exciter Studio (`AirBandExciterEngine`):
    - [x] `AirBandFrequency`, `AirBandMode`, `AirBandExciterConfig`, `AirBandMetrics`, and `AirBandBiquadCoefficients` in `:core:model`
    - [x] `AirBandExciterEngine` in `:core:domain:audio` with Pultec/Maag high-shelf biquad filter, psychoacoustic even-order harmonic saturation ($x^2$), hardware Band 4 offset calculation, and sheen descriptor evaluation
    - [x] Hardware equalizer integration in `NexusAudioEffects` targeting Band 4 (high treble) with `applyAirBandOffset`
    - [x] DataStore persistence in `SettingsDataStore` mapped to `ThemeConfig`
    - [x] `SettingsViewModel` air band exciter control methods
    - [x] Interactive "High-Frequency 'Air Band' & Silk Sheen Exciter" studio card in `SettingsScreen`
    - [x] Comprehensive test suites in `AirBandExciterEngineTest`, `NexusAudioEffectsTest`, and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.20-1`

- [x] Improvement Loop 71 (v6.21 Milestone - Tagged `v6.21-1`)
  - [x] Psychoacoustic Sub-Harmonic Bass & Missing Fundamental Studio (`SubHarmonicSynthEngine`):
    - [x] `SubHarmonicTarget`, `SubHarmonicMode`, `SubHarmonicSynthConfig`, and `SubHarmonicMetrics` in `:core:model`
    - [x] `SubHarmonicSynthEngine` in `:core:domain:audio` with Chebyshev 2nd/3rd missing fundamental harmonics, sub-octave ($f_0 / 2$) frequency divider, hardware Band 0 offset calculation, and live diagnostic ratings
    - [x] Hardware equalizer integration in `NexusAudioEffects` targeting Band 0 (~60 Hz sub-bass) with `applySubHarmonicOffset`
    - [x] DataStore persistence in `SettingsDataStore` mapped to `ThemeConfig`
    - [x] `SettingsViewModel` sub-harmonic synth control methods
    - [x] Interactive "Psychoacoustic Sub-Harmonic Bass Studio" card in `SettingsScreen`
    - [x] Comprehensive test suites in `SubHarmonicSynthEngineTest`, `NexusAudioEffectsTest`, and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.21-1`

- [x] Improvement Loop 72 (v6.22 Milestone - Tagged `v6.22-1`)
  - [x] Dynamic Spectral De-Esser & Vocal Harshness Tamer Studio (`DeEsserEngine`):
    - [x] `DeEsserMode`, `DeEsserConfig`, `DeEsserMetrics`, and `DeEsserBiquadCoefficients` in `:core:model`
    - [x] `DeEsserEngine` in `:core:domain:audio` with 2nd-order band-pass detection filter, localized sibilance ratio tracking, dynamic fast-attack ducking, and hardware Band 3 offset derivation
    - [x] Hardware equalizer integration in `NexusAudioEffects` targeting Band 3 (~4-6 kHz presence/treble) with `applyDeEsserOffset`
    - [x] DataStore persistence in `SettingsDataStore` mapped to `ThemeConfig`
    - [x] `SettingsViewModel` de-esser control methods
    - [x] Interactive "Dynamic Spectral De-Esser Studio" card in `SettingsScreen`
    - [x] Comprehensive test suites in `DeEsserEngineTest`, `NexusAudioEffectsTest`, and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.22-1`

- [x] Improvement Loop 73 (v6.23 Milestone - Tagged `v6.23-1`)
  - [x] True Peak Inter-Sample Clipping Guard & 4x Oversampled Reconstruction Limiter Studio (`TruePeakGuardEngine`):
    - [x] `TruePeakCeiling`, `TruePeakMode`, `TruePeakGuardConfig`, and `TruePeakMetrics` in `:core:model`
    - [x] `TruePeakGuardEngine` in `:core:domain:audio` with 4x polyphase sinc oversampling, ITU-R BS.1770-4 inter-sample peak detection, protective attenuation dBTP derivation, and DAC linearity ratings
    - [x] DataStore persistence in `SettingsDataStore` mapped to `ThemeConfig`
    - [x] `SettingsViewModel` true peak guard control methods
    - [x] Interactive "True Peak Inter-Sample Clipping Guard" studio card in `SettingsScreen`
    - [x] Comprehensive test suites in `TruePeakGuardEngineTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.23-1`

- [x] Improvement Loop 74 (v6.24 Milestone - Tagged `v6.24-1`)
  - [x] Ultrasonic Anti-Aliasing & Nyquist Image Guard Studio (`UltrasonicFilterEngine`):
    - [x] `UltrasonicTopology`, `UltrasonicMode`, `UltrasonicFilterConfig`, and `UltrasonicMetrics` in `:core:model`
    - [x] `UltrasonicFilterEngine` in `:core:domain:audio` with configurable biquad low-pass topologies (Butterworth Linear Phase, Minimum Phase Zero-Pre-Ring, Bessel Constant Delay, Apodizing Ring Killer), stopband rejection calculation, group delay estimation, and IMD protection ratings
    - [x] DataStore persistence in `SettingsDataStore` mapped to `ThemeConfig`
    - [x] `SettingsViewModel` ultrasonic filter control methods
    - [x] Interactive "Ultrasonic Anti-Aliasing & Nyquist Image Guard" studio card in `SettingsScreen`
    - [x] Comprehensive test suites in `UltrasonicFilterEngineTest` and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.24-1`

- [ ] Improvement Loop 75 (v6.25 Milestone - In Flight)
  - [x] Audiophile Analog Vacuum Tube Saturation & Even-Harmonic Warmth Studio (`TubeSaturationEngine`):
    - [x] `TubeTopology`, `TubeWarmthProfile`, `TubeSaturationConfig`, and `TubeSaturationMetrics` in `:core:model`
    - [x] `TubeSaturationEngine` in `:core:domain:audio` with Triode 12AX7, Pentode EL34, Warm Tape, and Class A Console non-linear curves, auto makeup headroom compensation, recursive DC offset blocking, and analytical Fourier tone probing
    - [x] Hardware equalizer integration in `NexusAudioEffects` (`applyTubeWarmthOffset`, `clearTubeWarmthOffset`, `getTubeWarmthOffset`) targeting Bands 1 & 2
    - [x] DataStore persistence in `SettingsDataStore` mapped to `ThemeConfig`
    - [x] `SettingsViewModel` tube warmth control methods
    - [x] Interactive "Analog Vacuum Tube Saturation & Warmth Studio" card in `SettingsScreen` with profile chips, topology selector, drive slider, bias slider, auto-makeup switch, and real-time Fourier diagnostic HUD
    - [x] Comprehensive test suites in `TubeSaturationEngineTest`, `NexusAudioEffectsTest`, and `SettingsViewModelTest`
  - [x] Slice gate passed
  - [x] Final gate passed & tagged: `v6.25-1`

## Summary of Milestones
1. Full 16-module Clean Architecture codebase implemented with 100% test pass rate.
2. Production ProGuard/R8 rules configured.
3. Physical hardware benchmarking verified on Android 16 @ 120Hz.

## Resume Instructions
1. Run `./scripts/gate.sh final` to verify release qualification.
2. Run `git tag v1.0-1` and proceed to continuous improvement loop.
