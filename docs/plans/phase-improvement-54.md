# Phase Improvement Plan - Loop 54 (v6.4.0)
## Interactive Audio Tag & Metadata Studio with Intelligent Tag Normalization (`AudioTagStudio`)

### 1. Context & Motivation
Streaming extractors and web audio sources frequently produce cluttered track metadata with bracketed artifacts, video labels, and unseparated artist-song names (e.g. `Song Title (Official Music Video) [HD]`, `Artist - Song [Lyric Video]`). NexusTune v6.4.0 introduces the **Interactive Audio Tag & Metadata Studio (`AudioTagStudio`)** powered by the `MetadataNormalizerEngine`. It offers one-tap regex-based artifact stripping, featured artist extraction (`feat. XYZ`), smart Title Casing, custom metadata editing, and Room database persistence across library tracks.

### 2. Architecture & Modules
- **Core Models** (`:core:model`):
  - `CleanMetadataResult`: Original & cleaned title/artist/album, list of featured artists, removed pattern tags, modification flag.
- **Pure Kotlin Domain Engine** (`:core:domain:metadata`):
  - `MetadataNormalizerEngine`:
    - Regex pattern matching for 20+ video/audio suffixes (`(Official Video)`, `[Official Audio]`, `(Lyric Video)`, `(4K 60FPS)`, `[HD]`, `(Visualizer)`, etc.).
    - Featured artist parser extracting `(feat. XYZ)` into separate artists list while cleaning title.
    - Hyphenated fallback separator for `Artist - Title` strings when artist is absent or generic.
    - Smart Title Casing adhering to standard English capitalization guidelines.
    - Bitrate quality badge formatter.
- **Database & Data Layer** (`:core:database`, `:core:data`):
  - `TrackDao.updateTrackMetadata(id, title, artist, album)` in Room.
  - `MusicRepository.updateTrackMetadata(trackId, title, artist, album)`.
- **Presentation Layer** (`:feature:library`):
  - `LibraryViewModel.updateTrackMetadata(trackId, title, artist, album)`.
  - `TrackMetadataEditorSheet`: Interactive `ModalBottomSheet` in `LibraryScreen` pre-filled with track data, showing detected noise pattern chips, a "✨ Smart Auto-Clean" action button, editable text fields, and "Save to Library".
- **Verification & Gates**:
  - `MetadataNormalizerEngineTest` in `:core:domain:metadata`.
  - `LibraryViewModelTest` in `:feature:library`.
  - Quality gates: `./scripts/gate.sh slice` and `./scripts/gate.sh final`.
  - Release tag: `v6.4-1`.

