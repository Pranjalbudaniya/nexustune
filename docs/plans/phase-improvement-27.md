# Phase Improvement Loop 27: Standard M3U / M3U8 Playlist Export & Import Engine

## 1. Goal
Implement standard Extended M3U and M3U8 playlist export and import capabilities in NexusTune:
1. Standard Extended M3U8 Generation:
   - Output standard `#EXTM3U` header.
   - Output optional `#PLAYLIST:<title>` metadata directive.
   - Output `#EXTINF:<durationSeconds>,<artist> - <title>`.
   - Output media URI / YouTube web URL `https://www.youtube.com/watch?v=<id>`.
2. Resilient M3U/M3U8 Parsing:
   - Support arbitrary `#EXTINF` formatting (`<seconds>,<artist> - <title>` or `<seconds>,<title>`).
   - Extract track IDs from full YouTube URLs (`?v=<id>`, `/watch?v=<id>`, `youtu.be/<id>`) or direct IDs.
   - Tolerant of comments, whitespace, and empty lines.
   - Insert imported tracks and playlist into Room database (`playlistDao` & `trackDao`).
3. ViewModel & Settings UI Integration:
   - Add `exportM3u(playlistId: String): String?` and `importM3u(content: String, fallbackTitle: String): Boolean` in `BackupManager` and `SettingsViewModel`.
   - Expose interactive Export M3U and Import M3U options in `SettingsScreen` under "Data & Backup".
4. Automated Testing & Release Verification:
   - Comprehensive unit test suite in `BackupManagerTest` verifying round-trip serialization, special characters, and partial metadata.
   - Full `./scripts/gate.sh slice` and `./scripts/gate.sh final` qualification.
   - Release tagging: `v3.7-1`.

## 2. Tasks & Owners
- **Core Data Backup**: Implement `exportPlaylistToM3u` and `importPlaylistFromM3u` in `BackupManager`.
- **Feature Settings**: Wire M3U export and import methods through `SettingsViewModel` and provide UI actions in `SettingsScreen`.
- **Testing**: Add unit tests for M3U parsing and export.
- **Verification**: Run `./scripts/gate.sh slice` and `./scripts/gate.sh final`.

## 3. Acceptance Criteria
- [ ] `BackupManager.exportPlaylistToM3u` generates valid `#EXTM3U` content with `#EXTINF`.
- [ ] `BackupManager.importPlaylistFromM3u` parses M3U content and inserts playlist + tracks into Room.
- [ ] `SettingsScreen` includes interactive M3U export and import options under "Data & Backup".
- [ ] Unit tests pass with 100% success rate.
- [ ] `./scripts/gate.sh final` passes with release APK build.
