# NexusTune Changelog

All notable changes to the NexusTune project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.1.0] - 2026-11-24

### Added
- **Per-Track Loudness Analysis & Gain Calibration**: Added per-track gain metadata (`loudnessGainDb`) to `Track` and `TrackEntity` with seamless Room database schema integration.
- **Dynamic DSP Gain Adaptation**: Enhanced `NexusAudioEffects` with `applyTrackLoudnessGain(gainDb, baseTargetGainmB)` computing target millibels with headroom clamping [-12dB, +12dB] to prevent digital clipping, wired directly to `PlayerController` active track changes.
- **Per-Track Gain Calibration Toggle**: Added user preference `perTrackGainEnabled` in `ThemeConfig` and interactive switch in Settings under Audio Optimization.

## [2.0.0] - 2026-11-24

### Added
- **Audio Output Routing Engine**: Implemented `AudioOutputRouteManager` in `core:player` with native Android `AudioManager` and `AudioDeviceCallback` integration, automatically tracking live audio routing changes across Bluetooth A2DP, USB DACs, Wired Headsets, and Phone Speakers.
- **Real-Time Hi-Res Audio Route Indicator**: Added dynamic audio route pill in `PlayerScreen` with contextual hardware icons (`Bluetooth`, `Headphones`, `VolumeUp`) and automatic Hi-Res container highlighting.

## [1.9.0] - 2026-11-24

### Added
- **Offline Storage & Cache Auto-Pruning Engine**: Created `StorageManager` in `core:data` with directory size inspection, immediate cache reclamation, and LRU pruning against user-defined storage budgets.
- **Cache Size Budget & Management UI**: Added Storage & Cache Management card in Settings with selectable cache budgets (500MB, 1GB, 2GB, 5GB, Unlimited) and one-tap stream cache clearance.

## [1.8.0] - 2026-11-24

### Added
- **Adaptive Playback Pitch & Speed Controller**: Implemented native playback speed (0.5x to 2.0x) and pitch control via Media3's `PlaybackParameters` in `PlayerController`.
- **Player Screen Speed Selection Chips**: Integrated interactive playback speed selection chips directly into `PlayerScreen`, allowing one-tap speed shifts during active playback.

## [1.7.0] - 2026-11-24

### Added
- **Custom Playlist Cover Art & Visual Personalization**: Added support for setting custom artwork URIs on personal playlists, with automated Material 3 Expressive dynamic gradient container fallbacks when no artwork is configured.
- **Library Playlist Management**: Revamped `LibraryScreen` into a 3-tab layout (`Liked`, `Playlists`, `History`) with interactive playlist creation and artwork editing dialogs.
- **Batch Queue Operations**: Implemented `addTracksNext(tracks)`, `removeTracks(trackIds)`, and `clearUpcoming()` in `QueueRepository` and `ManageQueueUseCase` for powerful batch queue manipulation.

## [1.6.0] - 2026-11-24

### Added
- **Smart Volume Normalization (ReplayGain)**: Integrated `LoudnessEnhancer` DSP into `NexusAudioEffects` and ExoPlayer audio session to normalize audio output loudness across dynamic web streams without digital clipping.
- **Configurable Target Boost**: Added target gain boost presets (+2dB, +4dB, +6dB) and live toggle in Settings screen under Audio Optimization.

## [1.5.0] - 2026-11-24

### Added
- **Silence & Non-Music Skip Segment Manager**: Added `SkipSegmentManager` in `core:player` with automatic detection and seamless seek-skipping over non-music intros, outros, sponsor blocks, and silence gaps.
- **Auto-Skip Settings Toggle**: Added an instant toggle under Audio Optimization in Settings to enable/disable automated skip segments.

## [1.4.0] - 2026-11-24

### Added
- **Synced Lyrics Manual Offset Calibration**: Added live offset calibration (+/- 100ms, reset) directly in `PlayerScreen` and `PlayerViewModel` for precision audio/lyrics synchronization.
- **Time-of-Day Listening Analytics**: Expanded `ListeningStats` and `MusicRepositoryImpl` with 4-phase day distribution (Morning, Afternoon, Evening, Night) calculated from Room playback history.
- **Settings Screen Updates**: Added Time-of-Day analytics summary in Settings dashboard and updated build version to v1.4.0.

## [1.3.0] - 2026-11-24

### Added
- **Android Auto & Automotive Head-Unit Integration**: Transitioned `NexusPlaybackService` to `MediaLibraryService` (`MediaBrowserService` compatible) with browsable root categories (`Liked Tracks`, `Recently Played`, `Playlists`) and automotive app descriptor (`automotive_app_desc.xml`).

## [1.2.0] - 2026-11-24

### Added
- **Dynamic Crossfade & Transition Smoothing**: Added configurable audio crossfading (Gapless Off, 2s, 4s, 6s, 8s, 12s) with smooth volume ramp down/up in `PlayerController` and `ThemeConfig`.
- **Home Screen Playback Widget**: Added Android AppWidget (`NexusTuneWidgetProvider`) with Material 3 styling, live track information, play/pause and next track actions, updating automatically from player session broadcasts.
- **NewPipe Backup Migration**: Added `NewPipeBackupImporter` in `core:data` allowing direct import of playlists, streams, and titles from NewPipe JSON export files into Room storage.
- **Settings Screen Expansion**: Added dedicated "Audio Crossfade & Transitions" selection chips and NewPipe backup import entry point.

## [1.1.0] - 2026-11-24

### Added
- **Sleep Timer Engine**: Added `SleepTimerManager` with preset durations (15m, 30m, 45m, 60m), countdown state flow, active timer notification banner with cancel action, and seamless End-of-Track auto-pause.
- **Hardware Equalizer DSP Presets**: 6 hardware DSP tuning presets (`FLAT`, `BASS_BOOST`, `TREBLE_BOOST`, `VOCAL`, `ELECTRONIC`, `ROCK`) applied directly to ExoPlayer's audio session ID with real-time UI switching.
- **Personalized Listening Analytics**: Real-time listening stats aggregated from Room playback history, displaying total tracks played, total listening duration, and top 5 most listened artists.
- **Enhanced Settings UI**: Revamped Settings screen with active Sleep Timer countdown card, DSP preset horizontal chip carousel, and Listening Analytics summary dashboard.

## [1.0.0] - 2026-11-24

### Added
- **Core Architecture**: Established 16-module Clean Architecture Gradle setup (`:app`, `:core:model`, `:core:common`, `:core:domain`, `:core:data`, `:core:database`, `:core:datastore`, `:core:extractor`, `:core:player`, `:core:designsystem`, `:core:ui`, `:core:testing`, `:feature:home`, `:feature:search`, `:feature:library`, `:feature:player`, `:feature:settings`, `:benchmark`).
- **Extractor Engine**: Native YouTube streaming and metadata resolution using pinned `TeamNewPipe:NewPipeExtractor:v0.27.1`.
- **Playback Engine**: Media3 ExoPlayer background playback with foreground notification service, lockscreen controls, and system audio focus integration.
- **Audio DSP**: 5-band hardware-accelerated Equalizer, Bass Boost, and Virtualizer effects via `NexusAudioEffects`.
- **Theming & Personalization**:
  - Dynamic Material 3 Expressive color schemes (Dynamic, Vibrant, Expressive, AMOLED).
  - 4 full Player screen layouts: Art Forward, Vinyl Record, Synced Lyrics, Minimalist.
  - 3 Mini-player variants: Docked Bar, Floating Pill, Compact Slim.
  - AMOLED Pure Black mode.
  - Shape presets (Rounded, Expressive, Sharp).
- **Library & Storage**: Room database for persistent Liked Tracks, Playlists, and Play History.
- **Offline & Downloads**: Background track downloading via WorkManager and OkHttp with automatic offline stream fallback.
- **Backup & Restore**: Standalone JSON export and import for user library and theming preferences.
- **Performance**: Validated 120Hz high refresh rate rendering (P99 <= 1.0ms frame time on Vivo V2518).

