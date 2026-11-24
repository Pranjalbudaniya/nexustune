# Loop 39: Interactive Track Audio Bookmark & Chapter Annotation Engine (v4.9.0)

## Objective
Implement an interactive audio bookmark and chapter annotation system in NexusTune allowing listeners to save, label, persist, and jump to specific timestamps within long DJ mixes, live sets, concert recordings, podcasts, and study tracks.

## Scope of Work

### 1. Data & Domain Layer
- **Model**: Add `TrackBookmark` data class to `:core:model`:
  - `id: Long = 0`
  - `trackId: String`
  - `title: String`
  - `positionMs: Long`
  - `createdAtTimestamp: Long`
- **Database**:
  - Add `TrackBookmarkEntity` in `:core:database:entity`.
  - Add `TrackBookmarkDao` with:
    - `getBookmarksForTrack(trackId: String): Flow<List<TrackBookmarkEntity>>`
    - `insertBookmark(bookmark: TrackBookmarkEntity): Long`
    - `deleteBookmark(bookmarkId: Long)`
    - `deleteAllBookmarksForTrack(trackId: String)`
  - Register `TrackBookmarkEntity` and `trackBookmarkDao()` in `NexusTuneDatabase`.
- **Repository**:
  - Add `getBookmarksForTrack(trackId: String): Flow<List<TrackBookmark>>`, `addBookmark(trackId: String, title: String, positionMs: Long): TrackBookmark`, and `deleteBookmark(bookmarkId: Long)` to `MusicRepository` and `MusicRepositoryImpl`.

### 2. Feature & Presentation Layer
- **PlayerViewModel**:
  - Reactive `currentTrackBookmarks: StateFlow<List<TrackBookmark>>` switching automatically when active playing track changes.
  - Actions:
    - `addBookmark(title: String, positionMs: Long? = null)` (defaults to current position if null).
    - `deleteBookmark(bookmarkId: Long)`.
    - `seekToBookmark(bookmark: TrackBookmark)` -> calls `playerController.seekTo(bookmark.positionMs)`.
- **PlayerScreen**:
  - In full-player screens, provide an interactive "Bookmarks" action / sheet.
  - Interactive bookmark chips along player controls or bottom sheet displaying timestamp (`mm:ss`) and label (e.g. "03:45 - Guitar Solo").
  - Tapping a bookmark chip instantly seeks playback to that position.
  - "Add Bookmark at [current position]" dialog with auto-suggested or custom title.
  - Delete bookmark swipe or trailing icon.

### 3. Verification & Testing
- Unit tests:
  - `TrackBookmarkDaoTest` in `:core:database` verifying insertion, sorting by `positionMs`, and deletion.
  - `MusicRepositoryImplTest` in `:core:data` verifying mapping between entity and domain model.
  - `PlayerViewModelTest` in `:feature:player` verifying adding, seeking, and deleting bookmarks.
- Gates:
  - `./scripts/gate.sh slice`
  - `./scripts/gate.sh final`
  - Release tag `v4.9-1`.

