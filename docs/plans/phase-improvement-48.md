# Improvement Loop 48: Smart Vibe Mix & Dynamic Algorithmic Playlist Synthesis Engine (v5.8.0)

## 1. Objectives & Value
- **Algorithmic Vibe Playlists**: Allow users to synthesize targeted, beautifully crafted music playlists on-demand based on acoustic vibes—including **Late-Night Lo-Fi**, **High-Octane Energy**, **Deep Focus & Flow**, **Golden Hour Acoustic**, **Club Euphoria**, **Nostalgia & Deep Cuts**, and **Eclectic Taste Mix**.
- **Energy Curve Flow Sculpting**: Shape listening sessions with mathematical tempo curves: **Ramp-Up** (gradual momentum buildup to a climax), **Sustained Peak** (high kinetic drive throughout), **Dynamic Waves** (alternating crests and troughs), and **Gentle Cooldown** (tapering down to relaxation).
- **Target Duration Calibration**: Generate mixes precisely tailored to 15, 30, 45, or 60-minute sessions based on track duration sums.
- **Library Synthesis & Room Persistence**: Add cross-reference support in `PlaylistDao` and `MusicRepository` (`addTracksToPlaylist`), enabling instant saving of synthesized mixes into the local Room database, offline caching, and instant queue playback.
- **Interactive Vibe Console in Library**: Add a "✨ Smart Vibe Mix" launch button and Material 3 Expressive `ModalBottomSheet` in `LibraryScreen`.

## 2. Technical Architecture
- **Model**: `VibeTheme`, `EnergyCurve`, `VibeMixRequest`, `VibeMixResult` in `:core:model`.
- **Database & Data**: `PlaylistTrackCrossRef` insertion and count updates in `PlaylistDao`, `MusicRepository`, and `MusicRepositoryImpl`.
- **Domain**: `VibePlaylistGenerator` in `:core:domain:playlist`.
- **Library Feature**:
  - `LibraryViewModel`: `previewVibeMix()`, `generateAndSaveVibePlaylist()`, `playVibeMixNow()`.
  - `LibraryScreen`: Smart Vibe Mix action button and `ModalBottomSheet` with theme chips, curve chips, duration selectors, and live track previews.
- **Verification**:
  - `VibePlaylistGeneratorTest` in `:core:domain`.
  - `LibraryViewModelTest` in `:feature:library`.
  - `./scripts/gate.sh slice` and `./scripts/gate.sh final`.

