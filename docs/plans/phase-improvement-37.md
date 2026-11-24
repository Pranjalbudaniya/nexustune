# Phase Improvement Loop 37: Custom Synced LRC Lyrics Import, Export & Offline Storage Engine

## 1. Overview & Objectives
While LRCLIB provides automatic lyrics lookup, many local files, indie tracks, and karaoke renditions lack online lyrics or require custom timing/translations. Loop 37 introduces:
1. `LrcParser` & `LrcSerializer` in `:core:domain:lyrics`:
   - Standard RFC LRC parser supporting `[mm:ss.xx]`, `[mm:ss.xxx]`, multiple timestamp tags, `[offset:]` adjustments, and metadata headers (`[ti:]`, `[ar:]`, `[al:]`).
   - Format serializer producing standard `.lrc` files from `TrackLyrics`.
2. Repository & Room Persistence:
   - `saveCustomLyrics(trackId, title, artist, lrcContent)`: Parses LRC text and persists it directly in `LyricsDao` (`lyrics_cache`).
   - `exportLyricsToLrc(trackId)`: Retrieves cached lyrics and serializes into standard `.lrc` format.
3. Interactive Lyrics Console in `PlayerScreen`:
   - "Import LRC" action button opening an input dialog to paste/import custom timed lyrics.
   - "Export LRC" action button copying/exporting formatted `.lrc` content.
   - Visual badge `[Custom LRC]` when user-imported lyrics are active.
4. Comprehensive Unit Tests:
   - `LrcParserTest`: Multiline parsing, offset handling, multi-tag timestamps, millisecond precision, and fallback.
   - `LrcSerializerTest`: Roundtrip conversion and tag formatting.
   - `PlayerViewModelTest`: Integration of import and export operations.

## 2. Technical Scope & Architecture Boundaries
- **`:core:domain`**:
  - `core/domain/src/main/kotlin/com/nexus/tune/core/domain/lyrics/LrcParser.kt`
  - `core/domain/src/main/kotlin/com/nexus/tune/core/domain/lyrics/LrcSerializer.kt`
  - `Repositories.kt`: Add `saveCustomLyrics` and `exportLyricsToLrc` to `MusicRepository`.
- **`:core:data`**:
  - `MusicRepositoryImpl.kt`: Implement `saveCustomLyrics` and `exportLyricsToLrc`.
- **`:feature:player`**:
  - `PlayerViewModel.kt`: Expose `importCustomLrc()` and `exportCurrentLyrics()`.
  - `PlayerScreen.kt`: Add Import & Export actions in synced lyrics view.

## 3. Verification Criteria
- [ ] `./scripts/gate.sh slice` passes 100% of unit tests.
- [ ] `./scripts/gate.sh final` passes with release APK build.
- [ ] Tag build `v4.7-1`.

