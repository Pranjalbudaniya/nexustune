# Post-v1.0 Improvement Loop 57: Intelligent Audio Track Chapters & DJ Cue Navigation Studio

## 1. Objectives & Overview
Implement an intelligent chapter parsing and navigation engine in NexusTune (`TrackChapterParser` / `TrackChapterEngine`):
1. **Chapter Extraction & Timestamp Parsing**:
   - Parse long-form stream descriptions, album descriptions, and DJ tracklists into discrete `TrackChapter` items.
   - Support timestamp formats: `HH:MM:SS Title`, `MM:SS Title`, `[MM:SS] Title`, `(MM:SS) Title`, and `Title - MM:SS`.
   - Automatically calculate chapter bounds (`startTimeMs`, `endTimeMs`, and `durationFormatted`).
2. **Real-Time Chapter Progress Tracking**:
   - Given current playback position $T$, identify current active chapter index, chapter progress fraction ($0.0 \dots 1.0$), and remaining chapter duration.
   - Calculate targets for "Skip to Next Chapter" and "Skip to Previous Chapter".
3. **Interactive Player UI & Bottom Sheet**:
   - Player screen shows active chapter pill: `📌 Ch. 3: Solarstone - Seven Cities [04:12 / 08:30]`.
   - Tapping chapter pill opens `TrackChaptersBottomSheet` featuring the complete chapter list, duration badges, playing status, and one-tap seek.
4. **Resilient Fallback**:
   - Graceful zero-overhead operation when a track has no chapter markers.

## 2. Architecture & Components
- `:core:model`:
  - `TrackChapter`: `index: Int`, `title: String`, `startTimeMs: Long`, `endTimeMs: Long?`, `durationFormatted: String`.
  - `ChapterProgress`: `currentChapter: TrackChapter?`, `nextChapter: TrackChapter?`, `progressFraction: Float`, `remainingFormatted: String`.
- `:core:domain`:
  - `TrackChapterParser` in `com.nexus.tune.core.domain.player`:
    - `parseChapters(rawText: String, totalTrackDurationMs: Long = 0L): List<TrackChapter>`
    - `resolveActiveChapter(chapters: List<TrackChapter>, positionMs: Long): TrackChapter?`
    - `calculateChapterProgress(chapter: TrackChapter, positionMs: Long): ChapterProgress`
    - `getSeekTarget(chapters: List<TrackChapter>, positionMs: Long, forward: Boolean): Long?`
- `:feature:player`:
  - `PlayerViewModel`:
    - `chapters: StateFlow<List<TrackChapter>>`
    - `activeChapterProgress: StateFlow<ChapterProgress?>`
    - `seekToChapter(chapter: TrackChapter)`
  - `PlayerScreen`:
    - Interactive chapter pill above seekbar.
    - `TrackChaptersBottomSheet` for full chapter exploration and one-tap jumping.
- Unit Tests:
  - `TrackChapterParserTest` in `:core:domain`
  - `PlayerViewModelTest` in `:feature:player`

## 3. Verification Criteria
- `./scripts/gate.sh slice` passes with 100% test pass rate.
- `./scripts/gate.sh final` passes with release APK build.
- Tagged `v6.7-1`.

