# Loop 42: Interactive "Up Next" Active Queue Sheet & In-Player Reordering Engine (v5.2.0 Milestone)

## Objective
Implement a full-featured, interactive "Up Next" active queue bottom sheet directly within `PlayerScreen`, allowing listeners to inspect upcoming tracks, jump to any track, remove tracks, clear upcoming songs, and shuffle without leaving the full-screen player experience.

## Scope of Work

### 1. Presentation & State Layer
- **PlayerViewModel**:
  - Expose `activeQueue: StateFlow<List<Track>>` from `manageQueueUseCase.getQueue()`.
  - Expose `activeQueueIndex: StateFlow<Int>` from `manageQueueUseCase.getQueueIndex()`.
  - Actions:
    - `jumpToQueueIndex(index: Int)`: plays the track at `index` in the current queue.
    - `removeFromQueue(index: Int)`: removes the specified item from active queue.
    - `clearUpcoming()`: clears all upcoming tracks after the currently playing song.
- **PlayerScreen**:
  - Top bar "Up Next" Queue action button (`Icons.AutoMirrored.Rounded.QueueMusic`).
  - Interactive `ModalBottomSheet`:
    - Header with "Up Next" title and total upcoming duration.
    - Action chips: "Shuffle", "Clean Duplicates", "Clear Upcoming".
    - Track list showing current playing track with "NOW PLAYING" badge, and upcoming items with thumbnail, title, artist, duration, and trailing delete action.
    - Tap to jump instantly to any upcoming track.

### 2. Verification & Testing
- Unit tests:
  - `PlayerViewModelTest` in `:feature:player` verifying `jumpToQueueIndex`, `removeFromQueue`, and `clearUpcoming`.
- Gates:
  - `./scripts/gate.sh slice`
  - `./scripts/gate.sh final`
  - Release tag `v5.2-1`.
