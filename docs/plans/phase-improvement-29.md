# Phase Improvement Loop 29: Smart Playlist & Queue Deduplication Engine

## 1. Goal
Implement intelligent duplicate detection and one-tap deduplication for queues, custom playlists, and user library:
1. Semantic and Exact Duplicate Detection:
   - Provide `PlaylistDeduplicator` in `:core:domain:playlist`.
   - Detect exact duplicates by track ID.
   - Detect semantic duplicates by normalizing song titles (stripping noise patterns like `(Official Audio)`, `[Audio]`, `(Official Music Video)`, `(Lyric Video)`, `(HD)`) and matching against normalized artist names.
   - Generate `DuplicateReport(duplicateCount, duplicateTracks, deduplicatedTracks)` preserving original playlist ordering.
2. Queue & Library Deduplication Actions:
   - Add `deduplicateQueue()` in `ManageQueueUseCase` and `PlayerViewModel`, maintaining active playback pointer.
   - Add `deduplicateLikedTracks()` in `MusicRepository` and `LibraryViewModel`.
3. UI Integration:
   - Add "Clean Duplicates" action chip in `QueueScreen` (feature:player) with toast feedback.
   - Add "Deduplicate Library" button in `SettingsScreen` under "Storage & Cache Management".
4. Automated Testing & Release Verification:
   - Unit test suite `PlaylistDeduplicatorTest` verifying regex title stripping, exact and semantic deduplication, and order preservation.
   - Full `./scripts/gate.sh slice` and `./scripts/gate.sh final` verification.
   - Release tagging: `v3.9-1`.

## 2. Tasks & Owners
- **Core Domain Playlist**: Implement `PlaylistDeduplicator` and `DuplicateReport`.
- **Core Domain UseCase**: Add `deduplicateQueue` to `ManageQueueUseCase`.
- **Core Data Repository**: Implement `deduplicateLikedTracks` in `MusicRepositoryImpl`.
- **Feature Player & Library & Settings**: Wire deduplication actions into ViewModels and UI.
- **Testing**: Add unit tests for deduplication heuristics.
- **Verification**: Run `./scripts/gate.sh slice` and `./scripts/gate.sh final`.

## 3. Acceptance Criteria
- [ ] `PlaylistDeduplicator` normalizes noise tags and identifies duplicate tracks accurately.
- [ ] Queue deduplication preserves current track index and removes duplicates.
- [ ] Liked tracks deduplication updates database.
- [ ] All unit and Robolectric tests pass (`./scripts/gate.sh slice`).
- [ ] `./scripts/gate.sh final` passes with release APK build.
