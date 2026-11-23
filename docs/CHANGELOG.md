# NexusTune Changelog

All notable changes to the NexusTune project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
