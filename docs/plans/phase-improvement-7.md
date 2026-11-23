# Phase Execution Plan: Improvement Loop 7 (v1.7.0)

## Objective
Implement **Custom Playlist Cover Art & Visual Personalization** and **Batch Queue Operations** in NexusTune to empower users with custom library organization and advanced queue manipulation.

## Scope & Components
1. **Database & Core Data**:
   - Add `updateArtwork` to `PlaylistDao`.
   - Extend `MusicRepository` and `MusicRepositoryImpl` with `getPlaylists()`, `createPlaylist(title, artworkUrl)`, `updatePlaylistArtwork(id, artworkUrl)`, and `deletePlaylist(id)`.
   - Extend `QueueRepository` and `QueueRepositoryImpl` with batch operations: `addTracksNext(tracks)`, `removeTracks(trackIds)`, and `clearUpcoming()`.
2. **Domain Layer**:
   - Enhance `ManageQueueUseCase` with batch operations: `addTracksNext(tracks)`, `removeTracks(trackIds)`, and `clearUpcoming(currentTrackId)`.
   - Add unit tests in `ManageQueueUseCaseTest`.
3. **UI / Feature Layer**:
   - Revamp `LibraryScreen` to include a 3-tab layout: "Liked", "Playlists", and "History".
   - In "Playlists" tab:
     - Display playlists with artwork image or Material 3 Expressive dynamic gradient container.
     - "New Playlist" action button and creation dialog with title & artwork URI input.
     - Artwork update capability.
   - Extend `LibraryViewModel` with playlists state flow and playlist mutation methods.
   - Add full unit test coverage in `LibraryViewModelTest`.
4. **Verification & Release**:
   - `./scripts/gate.sh slice`
   - `./scripts/gate.sh perf`
   - `./scripts/gate.sh final`
   - Tag `v1.7-1`

## Acceptance Criteria
- [ ] `PlaylistDao` updates artwork URL for playlists.
- [ ] `MusicRepository` handles playlist CRUD with Room persistence.
- [ ] `ManageQueueUseCase` supports batch next insertions and deletions.
- [ ] `LibraryScreen` renders "Playlists" tab alongside Liked and History with creation dialog.
- [ ] Unit tests pass with 100% success rate.
- [ ] `./scripts/gate.sh slice` passes.
- [ ] `./scripts/gate.sh perf` passes on physical device.
- [ ] `./scripts/gate.sh final` passes and release APK is generated.

