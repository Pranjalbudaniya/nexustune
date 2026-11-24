# Phase Plan: Improvement Loop 17 (v2.7.0)

## Objective
Implement **Synced Lyrics Offline Caching & Calibration Persistence**:
1. Persistently cache fetched plain and synced LRC lyrics into Room database (`lyrics_cache` table) to enable immediate, zero-network offline lyric rendering.
2. Persist user manual offset calibrations (`userOffsetMs`) per track ID across app restarts and playback sessions.
3. Add storage management action to clear cached lyrics in Settings.

## Architecture & Modules
- `core:database`:
  - `LyricsEntity`: Room entity with `trackId`, `title`, `artist`, `plainLyrics`, `syncedLyricsRaw`, `userOffsetMs`, `cachedAtTimestamp`.
  - `LyricsDao`: queries for `getLyrics`, `insertOrUpdate`, `updateOffset`, `clearAll`, and `getCachedCount`.
  - Add `LyricsEntity` and `lyricsDao()` to `NexusTuneDatabase`.
- `core:domain`:
  - Add `saveLyricsOffset(trackId: String, offsetMs: Long)`, `getLyricsOffset(trackId: String): Long`, and `clearLyricsCache()` to `MusicRepository`.
- `core:data`:
  - Implement caching in `MusicRepositoryImpl.getLyrics`:
    - Check Room `lyricsDao.getLyrics(trackId)`. If present, deserialize and return immediately.
    - If not present, query `musicSource.getLyrics(trackId, title, artist, durationSeconds)`.
    - On success, serialize lines and insert into Room `lyricsDao`.
  - Implement `saveLyricsOffset`, `getLyricsOffset`, and `clearLyricsCache` in `MusicRepositoryImpl`.
- `feature:player`:
  - In `PlayerViewModel`:
    - Auto-load persistent `lyricsOffsetMs` when track lyrics are loaded.
    - When `adjustLyricsOffset(deltaMs)` or `resetLyricsOffset()` is invoked, persist the updated offset to `MusicRepository`.
- `feature:settings`:
  - Add `clearLyricsCache()` in `SettingsViewModel`.
  - In `SettingsScreen`:
    - Add "Clear Lyrics Cache" item in Storage & Cache Management.
    - Update About banner to `NexusTune v2.7.0` | `Offline Lyrics Cache & Calibration Memory`.

## Tasks & Owners
1. **Task 17.1: Room Database Schema & DAO**
   - Create `LyricsEntity` and `LyricsDao`. Update `NexusTuneDatabase`.
2. **Task 17.2: Repository Caching & Calibration Persistence**
   - Update `MusicRepository` interface and `MusicRepositoryImpl` with lyrics caching and offset persistence.
3. **Task 17.3: Player & Settings UI Integration**
   - Wire `PlayerViewModel` to auto-restore and persist offset.
   - Wire `SettingsViewModel` and `SettingsScreen` with lyrics cache clearing.
4. **Task 17.4: Unit Tests & Release Qualification**
   - Unit tests for lyrics caching and offset persistence in `MusicRepositoryImplTest` and `PlayerViewModelTest`.
   - Unit tests in `SettingsViewModelTest`.
   - Run `./scripts/gate.sh slice`.
   - Run `./scripts/gate.sh final` and tag `v2.7-1`.
