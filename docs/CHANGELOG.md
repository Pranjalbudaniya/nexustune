# NexusTune Changelog

All notable changes to the NexusTune project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [6.7.0] - 2026-11-24

### Added
- **Intelligent Audio Track Chapters & DJ Cue Navigation Studio (`TrackChapterParser`)**: Added `TrackChapter` and `ChapterProgress` in `:core:model`.
- **Regex Chapter Parser & Relative Progress Engine**: Implemented `TrackChapterParser` in `:core:domain:player` supporting prefix timestamps (`00:00 Title`, `03:45 Title`, `1:14:20 Title`) and suffix timestamps (`Title - 03:45`, `Title (03:45)`), computing bounded start/end times and durations, evaluating active chapter progress fractions and countdowns, and providing directional skip target calculations (next chapter vs restart current/skip previous).
- **Player Screen Chapter Pill & Interactive Sheet**: Added real-time active chapter badge above seekbar (`Ch. N: Title • MM:SS`) and `TrackChaptersBottomSheet` with full tracklist overview, active pulse indicators, and one-tap chapter seek.
- **Top Bar Integration**: Added top-bar chapter bookmarks shortcut button displayed dynamically whenever a playing track contains chapter markers.
- **Unit Test Coverage**: Comprehensive test suites in `TrackChapterParserTest` in `:core:domain` and `PlayerViewModelTest` in `:feature:player`.

## [6.6.0] - 2026-11-24

### Added
- **Mid/Side (M/S) Stereo Soundstage Widener & Haas Spatializer Engine (`StereoWidenerEngine`)**: Added `StereoWidthMode` (`MONO`, `NARROW`, `NATURAL`, `EXPANDED`, `SUPER_WIDE`, `MID_VOCAL`, `SIDE_AMBIENT`), `StereoWidthConfig`, and `StereoPhaseRating` in `:core:model`.
- **Acoustic Mid/Side Matrix & Phase Correlation DSP**: Implemented `StereoWidenerEngine` in `:core:domain:audio` with Mid/Side deconstruction/reconstruction math, Pearson stereo phase correlation calculation ($[-1.0, +1.0]$) to protect against destructive phase cancellation on mono speakers, Haas effect micro-delay sample calculations (0ms to 20ms), and hardware virtualizer strength mapping.
- **Audio Effects & Virtualizer Integration**: Added `setVirtualizerStrength` in `NexusAudioEffects` dynamically driving spatial widening according to custom width percentages without overriding spatial room mode settings.
- **DataStore Preferences & Persistence**: Added `STEREO_WIDTH_ENABLED`, `STEREO_WIDTH_MODE`, `STEREO_WIDTH_PERCENT`, `STEREO_HAAS_DELAY_MS`, `STEREO_MID_LEVEL_DB`, and `STEREO_SIDE_LEVEL_DB` in `SettingsDataStore` and mapped to `ThemeConfig`.
- **Settings Screen Soundstage Studio**: Added interactive "Stereo Field & Soundstage Studio" card in `SettingsScreen` with soundstage mode filter chips, live percentage slider (0% to 200%), soundstage width status descriptors, and Haas delay filter chips.
- **Unit Test Coverage**: Comprehensive test coverage in `StereoWidenerEngineTest` in `:core:domain` and `SettingsViewModelTest` in `:feature:settings`.

## [6.5.0] - 2026-11-24

### Added
- **Headphone Soundstage & Acoustic Target Compensation (`HeadphoneCorrectionEngine`)**: Added `HeadphoneTargetCurve` (`HARMAN_OVER_EAR`, `HARMAN_IN_EAR`, `DIFFUSE_FIELD`, `STUDIO_NEUTRAL`, `WARM_PUNCH`, `V_SHAPED_BASSHEAD`, `TREBLE_AIR`) and `HeadphoneCorrectionConfig` in `:core:model`.
- **Acoustic Target Calculation & Binaural Crossfeed Math**: Implemented `HeadphoneCorrectionEngine` in `:core:domain:audio` with calibrated 5-band millibel corrections matching Harman research and Diffuse-Field targets, bass boost driver compensations, and interaural time difference (ITD, 0.25ms - 0.40ms) / interaural level difference (ILD, -14dB to -6dB) crossfeed math to banish "in-head" headphone listening fatigue.
- **Dynamic Layered Audio Effects & Equalizer Preservation**: Added `applyHeadphoneOffsets`, `clearHeadphoneOffsets`, `setBassBoostStrength`, and non-destructive layering in `NexusAudioEffects` allowing concurrent custom user EQ, karaoke vocal notching, and headphone target curve compensation.
- **DataStore Preferences & Persistence**: Added `HEADPHONE_CORRECTION_ENABLED`, `HEADPHONE_TARGET_CURVE`, and `HEADPHONE_CROSSFEED_ENABLED` in `SettingsDataStore` and mapped to `ThemeConfig`.
- **Settings Screen Acoustic Target Studio**: Added interactive "Headphone Target & Crossfeed Compensation" card in `SettingsScreen` with curve filter chips, tonal character descriptors, and binaural crossfeed toggle.
- **Unit Test Coverage**: Comprehensive test coverage across `HeadphoneCorrectionEngineTest` in `:core:domain`, `NexusAudioEffectsTest` in `:core:player`, and `SettingsViewModelTest` in `:feature:settings`.

## [6.4.0] - 2026-11-24

### Added
- **Interactive Audio Tag & Metadata Studio (`AudioTagStudio`)**: Added `CleanMetadataResult` in `:core:model`.
- **Intelligent Metadata Normalization Engine**: Implemented `MetadataNormalizerEngine` in `:core:domain:metadata` providing regex artifact stripping (20+ patterns including `(Official Music Video)`, `[Official Audio]`, `(Lyric Video)`, `[HD]`, `(Visualizer)`), featured artist extraction (`feat. XYZ`), hyphenated artist-title parsing, smart Title Casing, and bitrate badge formatting.
- **Room Database Track Metadata Persistence**: Added `TrackDao.updateTrackMetadata()` in `:core:database` and `MusicRepository.updateTrackMetadata()` in `:core:data`.
- **Library Screen Metadata Editor Bottom Sheet**: Added `TrackMetadataEditorSheet` in `LibraryScreen` accessible via track item edit icon with one-tap "✨ Auto-Clean" action, detected artifact tag chips, editable text fields, and persistence to library.
- **Unit Test Coverage**: Comprehensive test coverage in `MetadataNormalizerEngineTest` in `:core:domain` and `LibraryViewModelTest` in `:feature:library`.

## [6.3.0] - 2026-11-24

### Added
- **Smart Offline Cache Auto-Stash & Predictive Storage Budget Optimization Engine**: Added `AutoStashStrategy` (`BALANCED_HYBRID`, `FAVORITES_PRIORITY`, `HEAVY_ROTATION`, `OFFLINE_ESSENTIALS`), `AutoStashNetworkPolicy`, `AutoStashCandidate`, `AutoStashConfig`, and `AutoStashReport` in `:core:model`.
- **Auto-Stash Scoring & Knapsack Byte Packing**: Implemented `AutoStashEvaluator` in `:core:domain:offline` scoring candidates across play frequency, favorite marks, and recency, allocating bytes to stay strictly within user storage quotas, determining eviction candidates, and calculating offline listening duration.
- **DataStore Preferences & Persistence**: Added `AUTO_STASH_ENABLED`, `AUTO_STASH_BUDGET_MB`, `AUTO_STASH_STRATEGY`, and `AUTO_STASH_WIFI_ONLY` preferences in `SettingsDataStore` and mapped to `ThemeConfig`.
- **Settings Screen Auto-Stash Control Console**: Added interactive "⚡ Smart Offline Auto-Stash" card in `SettingsScreen` featuring master switch, storage budget chip selector (500MB, 1GB, 2GB, 5GB), strategy selector, and Wi-Fi only toggle.
- **Unit Test Coverage**: Comprehensive test suites in `AutoStashEvaluatorTest` in `:core:domain` and `SettingsViewModelTest` in `:feature:settings`.

## [6.2.0] - 2026-11-24

### Added
- **Real-Time Center-Channel Vocal Attenuation & Karaoke Sing-Along Studio**: Added `VocalCutLevel` (`GENTLE`, `MODERATE`, `AGGRESSIVE`, `MAXIMUM`), `KaraokeReverb`, `KaraokeConfig`, and `KaraokeLyricProgress` in `:core:model`.
- **Karaoke Audio DSP & Notching Engine**: Implemented `KaraokeEngine` in `:core:domain:audio` with linear gain conversions, 5-band parametric vocal cut EQ profiles (attenuating speech fundamentals around 910Hz/3.6kHz while leaving sub-bass intact and boosting air brilliance), chromatic pitch transposition math, and real-time lyric progress calculation.
- **Audio Effects & Player Controller Modulations**: Added `applyKaraokeOffsets` and `clearKaraokeOffsets` in `NexusAudioEffects` and `applyKaraokeConfig` in `PlayerController` to dynamically modulate the active equalizer without corrupting user EQ presets.
- **Player Screen Karaoke Studio Console & Teleprompter**: Added top-bar `🎤` Karaoke action button and an interactive `ModalBottomSheet` Karaoke Studio Console in `PlayerScreen` featuring intensity chips, key transposition chips (-3 to +3 st), and a live scrolling lyric teleprompter with upcoming line countdowns and syllable/line progress tracking.
- **Settings Integration**: Added Karaoke Sing-Along Studio card to `SettingsScreen`.
- **Unit Test Coverage**: Comprehensive test coverage across `KaraokeEngineTest` in `:core:domain` and `PlayerViewModelTest` in `:feature:player`.

## [6.1.0] - 2026-11-24

### Added
- **Audio Visualizer Suite Expansion: Stereo VU Meter & Liquid Wave**: Expanded `VisualizerMode` with `STEREO_VU` and `LIQUID_WAVE` in `:core:model`.
- **Visualizer Color Mesh Palettes**: Introduced `VisualizerPalette` (`DYNAMIC`, `CYBERPUNK`, `SUNSET_EMBER`, `AURORA`, `ELECTRIC_VIOLET`, `GOLDEN_CHAMPAGNE`) allowing rich visual themes.
- **Calibrated Twin Stereo VU Meter**: Implemented `StereoVuVisualizer` in `:core:ui` with dual Left/Right peak VU meters featuring Green (-20dB to -6dB), Amber (-6dB to -2dB), and Red (0dB peak) segments with needle falloff animation.
- **Fluid Sinusoidal Liquid Wave Visualizer**: Implemented `LiquidWaveVisualizer` in `:core:ui` with dual-phase flowing sine curves reacting dynamically to low, mid, and high FFT magnitudes.
- **Settings Console & Player Integration**: Added visualizer style and color palette selector chips in `SettingsScreen`, DataStore persistence in `SettingsDataStore`, and live rendering in `PlayerScreen`.
- **Unit Test Coverage**: Added `setVisualizerPalette updates config` to `SettingsViewModelTest`.

## [6.0.0] - 2026-11-24

### Added
- **Acoustic Camelot Harmonic Mixing & BPM Transition Intelligence Engine**: Implemented `CamelotScale`, `CamelotKey` (24 keys spanning 1A-12A Minor and 1B-12B Major), `HarmonicRelationship` (`PERFECT_MATCH`, `RELATIVE_KEY`, `ADJACENT_STEP`, `ENERGY_BOOST`, `ENERGY_DROP`, `DISSONANT`), `TempoCompatibility`, `TrackHarmonicProfile`, and `HarmonicTransition` domain models in `:core:model`.
- **Harmonic Mixing Math & Circle of Fifths Engine**: Created `HarmonicMixingEngine` in `:core:domain:audio` evaluating Camelot step offsets, emotional scale shifts, half/double time tempo ratios, optimal crossfade ramp durations (2s to 8s), and a greedy nearest-neighbor harmonic queue reordering algorithm.
- **Player Screen Live Camelot Key & Next Track Transition Pill**: Added interactive musical key and BPM readout pill (e.g. `🎹 8A • 124 BPM`) and dynamic colored transition badge on the full player screen showing live harmonic compatibility with the next track in queue.
- **Up Next Queue Smart Harmonization Action**: Added "✨ Harmonize" one-tap action to the Up Next Queue bottom sheet, allowing listeners to reorganize upcoming tracks into a harmonically continuous musical set.
- **Comprehensive Unit Test Suites**: Created `HarmonicMixingEngineTest` in `:core:domain` and expanded `PlayerViewModelTest` in `:feature:player`.

## [5.9.0] - 2026-11-24

### Added
- **Dynamic Rule-Based Smart Playlists & Algorithmic Query Engine**: Implemented `SmartRuleField` (`TITLE`, `ARTIST`, `ALBUM`, `DURATION_SECONDS`, `IS_LIKED`, `IS_DOWNLOADED`, `PLAY_COUNT`), `SmartRuleOperator` (`EQUALS`, `CONTAINS`, `NOT_CONTAINS`, `GREATER_THAN`, `LESS_THAN`, `STARTS_WITH`), `SmartRuleMatch` (`ALL`, `ANY`), `SmartRuleSort`, `SmartPlaylistRule`, and `SmartPlaylistDefinition` domain models in `:core:model`.
- **Smart Playlist Evaluator Engine**: Implemented `SmartPlaylistEvaluator` in `:core:domain:playlist` with rule matching, string & numeric evaluations, sorting options, track limit clipping, JSON serialization/deserialization, and 5 built-in smart presets: "🔥 Heavy Rotation", "⭐ Liked Gems", "⚡ Quick Jams", "🎧 Deep Listening", and "💾 Offline Stash".
- **DataStore & Repository Persistence**: Persisted custom smart playlist definitions in `SettingsDataStore` with reactive streams and CRUD operations in `SettingsRepository` and `SettingsRepositoryImpl`.
- **Library Screen Smart Playlist Carousel & Control Sheet**: Added "⚡ Dynamic Smart Playlists" horizontal card carousel to `LibraryScreen` Playlists tab, an interactive Smart Playlist Preview `ModalBottomSheet` with live track count, "Play All", "Shuffle All", and delete actions, plus a "Create Dynamic Smart Playlist" rule builder bottom sheet.
- **Comprehensive Unit Tests**: Created `SmartPlaylistEvaluatorTest` in `:core:domain` and expanded `LibraryViewModelTest` in `:feature:library`.

## [5.8.0] - 2026-11-24

### Added
- **Smart Vibe Mix & Dynamic Algorithmic Playlist Synthesis Engine**: Implemented `VibeTheme` (`LATE_NIGHT_CHILL`, `HIGH_OCTANE_ENERGY`, `DEEP_FOCUS_STUDY`, `SUNSET_ACOUSTIC`, `CLUB_EUPHORIA`, `TIME_MACHINE_DEEP_CUTS`, `DYNAMIC_ECLECTIC`), `EnergyCurve` (`RAMP_UP`, `PEAK_SUSTAINED`, `WAVE_DYNAMIC`, `GENTLE_COOLDOWN`), `VibeMixRequest`, and `VibeMixResult` in `:core:model`.
- **Vibe Mix Generator Engine**: Created `VibePlaylistGenerator` in `:core:domain:playlist` providing keyword-scored theme matching, artist diversity spacing, session duration targeting (15m, 30m, 45m, 60m), and energy flow curves.
- **Room Cross-Ref Playlist Persistence**: Added `PlaylistTrackCrossRef` batch insertion (`insertTrackCrossRefs`), track queries (`getTracksForPlaylist`), and count updates in `PlaylistDao`, `MusicRepository`, and `MusicRepositoryImpl`.
- **Library Screen Smart Vibe Console**: Added "✨ Vibe Mix" launch button and Material 3 Expressive `ModalBottomSheet` in `LibraryScreen` featuring theme chips, duration selectors, energy curve chips, live track preview list, "Play Now" action, and "Save Playlist" Room persistence.
- **Comprehensive Unit Tests**: Created `VibePlaylistGeneratorTest` in `:core:domain` and expanded `LibraryViewModelTest` in `:feature:library`.

## [5.7.0] - 2026-11-24

### Added
- **Interactive Music Identity Card & Shareable Sonic DNA Fingerprint Engine**: Implemented `SonicArchetype` (`NOCTURNAL_VOYAGER`, `AURORA_PULSE`, `DEEP_FOCUS_SCHOLAR`, `ECLECTIC_EXPLORER`, `RHYTHMIC_ATHLETE`, `ACOUSTIC_PURIST`, `CHRONO_DISCOVERER`, `SONIC_NOMAD`) and `SonicDnaProfile` in `:core:model`.
- **Sonic DNA Intelligence Calculator**: Created `SonicDnaCalculator` in `:core:domain:analytics` analyzing playback history, genre distribution, diurnal periods, and consecutive day streaks to calculate Variety Score (10-100%), Energy Pulse (10-100%), Acoustic Depth (10-100%), archetype color gradients, and formatted shareable ASCII-art summaries.
- **Expressive Sonic DNA Card Component**: Implemented `SonicDnaCard` in `:core:ui` featuring dynamic archetype-tailored gradient brushes, circular badge icons, linear dimension progress meters, quick stats chips, and a one-tap social share action.
- **Settings Console Integration**: Embedded the interactive `SonicDnaCard` in `SettingsScreen` with clipboard export integration (`getShareableSonicDnaText()`).
- **Comprehensive Unit Tests**: Created `SonicDnaCalculatorTest` in `:core:domain` and expanded `SettingsViewModelTest` in `:feature:settings`.

## [5.6.0] - 2026-11-24

### Added
- **Interactive DJ Audio Transformation & Sound Effects Engine**: Implemented `DjPreset` (`NORMAL`, `NIGHTCORE`, `SLOWED_REVERB`, `VINTAGE_VINYL`, `VAPORWAVE`, `DEEP_BASS`, `CUSTOM`) and `DjFxConfig` in `:core:model` allowing real-time aesthetic acoustic modulation.
- **DJ Transformation Math Engine**: Implemented `DjTransformEngine` in `:core:domain:audio` calculating calibrated speed factors ($0.50\times - 2.00\times$), vocal pitch semitones ($\pm 6$ st), stereo soundstage width matrices ($0.0\times$ mono to $2.0\times$ super-wide), effective scaled duration, and closest aesthetic preset matching.
- **Interactive DJ Control Sheet in Player**: Added top-bar DJ action button and Material 3 Expressive `ModalBottomSheet` in `PlayerScreen` featuring aesthetic preset chips, speed slider, pitch transposition controls, soundstage width slider, vintage lo-fi filter toggle, live overview metrics, and one-tap reset.
- **Settings Console Integration**: Added dedicated "DJ Sound Effects & Transformation" card in `SettingsScreen` with preset selectors and toggleable "Remember DJ FX Across Tracks" persistence.
- **DataStore & Player Controller Integration**: Persisted DJ configuration in `SettingsDataStore` and mapped real-time parameters directly to `PlayerController` and `PlayerViewModel`.
- **Comprehensive Unit Test Suites**: Created `DjTransformEngineTest` in `:core:domain` and expanded `PlayerViewModelTest` (`:feature:player`) and `SettingsViewModelTest` (`:feature:settings`).

## [5.5.0] - 2026-11-24

### Added
- **Custom User Equalizer Presets & Bézier Frequency Curve Engine**: Added `CustomEqProfile` domain model to `:core:model` allowing users to save, manage, and switch custom 5-band parametric equalizer profiles.
- **Bézier Frequency Curve & Distance Engine**: Implemented `EqCurveCalculator` in `:core:domain:audio` with smooth cubic Bézier spline coordinate generation, Euclidean distance signature matching against standard presets (`Bass Boost`, `Vocal`, `Rock`, `Electronic`, `Treble Boost`, `Flat`), and human-readable dB band summaries.
- **Interactive Equalizer Curve Visualizer**: Created `EqCurveVisualizer` in `:core:ui` rendering dynamic frequency response curves, vertical gradient energy fills, interactive frequency band control nodes, and dB grid guidelines.
- **Custom Profile Management Console in Settings**: Integrated custom profiles list, closest signature match badge, custom profile selector chips, deletion controls, and a "Save Custom EQ Profile" creation dialog into `SettingsScreen`.
- **Persistent DataStore Storage**: Persisted custom EQ profiles in `SettingsDataStore` with JSON serialization handled seamlessly through `SettingsRepositoryImpl`.
- **Comprehensive Unit Tests**: Created `EqCurveCalculatorTest` in `:core:domain` and expanded `SettingsViewModelTest` in `:feature:settings`.

## [5.4.0] - 2026-11-24

### Added
- **Listening Milestones, Daily Streaks & Time-Capsule Recap Engine**: Implemented `ListeningStreak`, `MilestoneType`, `ListeningMilestone`, and `TimeCapsuleRecap` domain models in `:core:model`.
- **Milestone Calculator & Streak Engine**: Created `MilestoneCalculator` in `:core:domain:analytics` evaluating daily playback streaks (consecutive active epoch days), evaluating milestone achievements (`First Beat`, `Night Owl`, `Early Bird`, `Sonic Explorer`, `Century Club`, `Streak Master`, `Audiophile Gold`), and generating personalized music time-capsule recaps.
- **Persistent Daily Streak Tracking**: Added `listeningStreak` flow and `recordActiveDay(epochDay)` in `SettingsDataStore`, automatically logging playback sessions in `NexusTuneApplication`.
- **Settings Screen Streaks & Milestones Console**: Added dedicated "Listening Milestones & Daily Streaks" card in `SettingsScreen` with fire streak counter (`🔥 7 Day Streak`), achievement badges with progress bars, and formatted Time-Capsule recap summary.
- **Unit Test Suites**: Added `MilestoneCalculatorTest` in `:core:domain` and expanded `SettingsViewModelTest` in `:feature:settings`.

## [5.3.0] - 2026-11-24

### Added
- **Audio Loudness Normalization & Dynamic Peak Limiter Engine**: Implemented `LoudnessNormalizationMode` (`OFF`, `REPLAYGAIN_TRACK`, `REPLAYGAIN_ALBUM`, `DYNAMIC_PEAK_LIMITER`) and `LoudnessConfig` in `:core:model`.
- **Audio Normalization Engine**: Created `AudioNormalizationEngine` in `:core:domain:audio` featuring target LUFS gain multiplier derivation (`10^(gainDb/20)`), soft-knee dynamic peak limiting (`tanh` saturation knee at 0.95 threshold) to prevent digital clipping, and intelligent metadata-driven track LUFS estimation.
- **DataStore & Player Controller Integration**: Persisted loudness parameters in `SettingsDataStore`, added `setLoudnessMultiplier()` in `PlayerController` with real-time reactive volume scaling, and wired track changes in `NexusTuneApplication`.
- **Settings Screen Loudness Console**: Upgraded `SettingsScreen` with a dedicated "Audio Loudness Normalization & Dynamic Peak Limiter" card featuring mode selector chips, target LUFS buttons (`-11 Club`, `-14 Standard`, `-18 Dynamic`, `-23 EBU R128`), a `-6dB` to `+6dB` pre-amp slider with instant reset, and live limiter status readout.
- **Comprehensive Test Suites**: Created `AudioNormalizationEngineTest` in `:core:domain` and added tests in `SettingsViewModelTest` (`:feature:settings`).

## [5.2.0] - 2026-11-24

### Added
- **Interactive "Up Next" Active Queue Sheet & In-Player Reordering Engine**: Implemented reactive active queue observation (`activeQueue`, `activeQueueIndex`) and queue controls (`jumpToQueueIndex`, `removeFromQueue`, `clearUpcoming`, `deduplicateQueue`) in `PlayerViewModel`.
- **Player Screen Queue Bottom Sheet**: Added dedicated `ModalBottomSheet` accessible via top-bar queue button (`Icons.AutoMirrored.Rounded.QueueMusic`), displaying live upcoming track count, estimated remaining duration, and clear/deduplicate actions.
- **In-Queue Item Interaction**: Visual indicators for currently playing track, tap-to-jump playback to any queue track, and one-tap removal of upcoming items.
- **Expanded Test Coverage**: Added comprehensive unit tests in `PlayerViewModelTest` validating `jumpToQueueIndex`, `removeFromQueue`, and `clearUpcoming` delegation to `ManageQueueUseCase`.

## [5.1.0] - 2026-11-24

### Added
- **In-Library Instant Search, Multi-Criteria Sort & Smart Filter Engine**: Implemented `LibrarySortOrder` (`RECENT`, `TITLE_ASC`, `ARTIST_ASC`, `DURATION_DESC`) in `:core:model` and `LibraryFilterEngine` in `:core:domain:library` for real-time sub-millisecond filtering across saved tracks and playlists.
- **Library Screen Instant Search Bar**: Added an ergonomic search field with live character filtering and clear button, operating entirely offline on cached and saved tracks.
- **Multi-Field Sorting & Filtering**: Added dynamic sort chips for Liked songs and History, sorting by chronological order, song title, artist name, and track duration.
- **Batch Play Filtered Queue**: Added a "Play Matches" instant playback action streaming all filtered results into active playback.
- **Unit Test Suites**: Added `LibraryFilterEngineTest` (`:core:domain`) and expanded `LibraryViewModelTest` (`:feature:library`).

## [5.0.0] - 2026-11-24

### Added
- **Smart Listening Mood & Energy Radio Matrix Engine**: Implemented `MoodCategory` (`ALL`, `ENERGIZE`, `FOCUS`, `CHILL`, `WORKOUT`, `COMMUTE`, `SLEEP`) in `:core:model` and `MoodRadioEngine` in `:core:domain:mood` for intelligent mood classification and continuous smart radio generation.
- **Home Screen Mood Matrix Filter Carousel**: Added an interactive, tactile mood selector carousel at the top of `HomeScreen` for real-time feed filtering and curated listening.
- **Instant Mood Radio Priming**: Added "Start [Mood] Radio" action cards generating 20+ continuous energy-matched tracks from user libraries and feeds, initiating automated seamless queue playback.
- **Full Unit Test Coverage**: Added `MoodRadioEngineTest` in `:core:domain` and `HomeViewModelTest` in `:feature:home`.

## [4.9.0] - 2026-11-24

### Added
- **Interactive Track Audio Bookmark & Chapter Annotation Engine**: Implemented `TrackBookmark` domain model, Room `TrackBookmarkEntity`, and `TrackBookmarkDao` for persisting timestamped markers within any track or DJ mix.
- **Repository & State Orchestration**: Added `getBookmarksForTrack()`, `addBookmark()`, and `deleteBookmark()` in `MusicRepository` and `MusicRepositoryImpl`, with reactive `bookmarks` flow and auto-tracking in `PlayerViewModel`.
- **Interactive Player Bookmark Console**: Added tactile "Bookmark" button, quick-jump timestamp chips (e.g. `01:15 Drop`), interactive delete icon, and an "Add Bookmark" modal dialog in `PlayerScreen`.
- **Unit Test Suites**: Implemented comprehensive unit test suites in `TrackBookmarkRepositoryTest` (`:core:data`) and `PlayerViewModelTest` (`:feature:player`).

## [4.8.0] - 2026-11-24

### Added
- **Gentle Morning Wake-Up Alarm & Audio Fade-In Engine**: Implemented `WakeAlarmManager` and `WakeAlarmReceiver` in `:core:player:alarm` scheduling exact alarms using Android `AlarmManager` (`RTC_WAKEUP`) with compatibility fallbacks for Android 12+ (`SCHEDULE_EXACT_ALARM`).
- **Configurable Audio Volume Ramping**: Ascends playback volume gently from `0.0f` to user-configured `targetVolume` over customizable durations (30s, 1m, 2m, 5m) in 500ms micro-increments.
- **Multi-Source Alarm Audio**: Supports waking users to their current playback queue (`CURRENT_QUEUE`), favorite liked tracks (`LIKED_SONGS`), or soothing ambient synthesized soundscapes (`AMBIENT_SOUND`).
- **Interactive Settings Alarm Console**: Added dedicated "Gentle Morning Wake-Up Alarm" section in `SettingsScreen` with time chips, fade duration chips, source selector, target volume slider, snooze/dismiss actions, and real-time wake-up preview.
- **Application Lifecycle Orchestration**: Integrated `WakeAlarmTriggerable` into `NexusTuneApplication` for reactive background broadcast receiver execution and DataStore config synchronization.

## [4.7.0] - 2026-11-24

### Added
- **Custom Synced LRC Lyrics Import, Export & Offline Storage Engine**: Implemented `LrcParser` and `LrcSerializer` in `:core:domain:lyrics` with support for standard time tags (`[mm:ss.xx]`, `[mm:ss.xxx]`), global time offset tags (`[offset:]`), multi-timestamp line distribution, and metadata tags (`[ti:]`, `[ar:]`, `[al:]`).
- **Local LRC Persistence**: Added `saveCustomLyrics()` and `exportLyricsToLrc()` in `MusicRepository`, saving imported lyrics into `LyricsDao` (`lyrics_cache`) for full offline availability.
- **Interactive Player Lyrics Console**: Added "Import LRC" text-entry/paste dialog and "Export LRC" clipboard export action in `PlayerScreen` for seamless custom timing management even when online lyrics are unavailable.

## [4.6.0] - 2026-11-24

### Added
- **Smart Artist-Spaced Diversity Shuffle Engine**: Implemented `SmartShuffleEngine` in `:core:domain:queue` dynamically interleaving tracks by artist to prevent artist clustering (e.g. playing duplicate artists consecutively) while keeping the currently playing track pinned at index 0.
- **Repeat & Shuffle Media3 Integration**: Added `isShuffle`, `isRepeatOne`, and `isRepeatAll` state reporting in `PlaybackState`, with reactive listeners (`onShuffleModeEnabledChanged`, `onRepeatModeChanged`), `toggleShuffle()`, and `cycleRepeatMode()` in `PlayerController`.
- **Interactive Player Transport Buttons**: Wired Shuffle and Repeat controls in `PlayerScreen` with dynamic primary tinting and mode-specific iconography (`Repeat` vs `RepeatOne`), and connected Skip Previous / Skip Next actions.
- **Configurable Smart Shuffle Preference**: Added `smartShuffleEnabled` to `ThemeConfig` and `SettingsDataStore`, with a dedicated toggle in `SettingsScreen`.

## [4.5.0] - 2026-11-24

### Added
- **Vocal Pitch Shift & Semitone Transposition DSP Engine**: Implemented `PitchTransposer` in `:core:player` with mathematically precise semitone $\leftrightarrow$ pitch ratio conversions ($2^{s/12}$) spanning -6 to +6 semitones (full octave span centered on original key).
- **Interactive Key Transpose Console**: Added tactile stepper buttons (`-1`, `+1`), dynamic key readout (e.g. `Key: +2 st (1.12x)`), reset button, and quick-jump semitone chips (`-2`, `-1`, `Orig`, `+1`, `+2`) in `PlayerScreen`.
- **Automatic Track Reset Preference**: Added `resetPitchOnTrackChange` to `ThemeConfig` and `SettingsDataStore` with real-time toggle in `SettingsScreen`, automatically returning transposed tracks to their natural key when changing songs.
- **Controller & ViewModel Integration**: Integrated `playbackPitchSemitones` into `PlaybackState`, with reactive synchronization across `PlayerController` and `PlayerViewModel`.

## [4.4.0] - 2026-11-24

### Added
- **Interactive Waveform Seekbar & Peak Profile Engine**: Implemented `WaveformProfileGenerator` and `WaveformSeekbar` in `:core:ui`, rendering a 48-bar dynamic amplitude waveform visualization on Compose `Canvas` with interactive gesture seeking (tap and drag).
- **Seekbar Style Preferences**: Added `SeekbarStyle` (`CLASSIC_SLIDER`, `WAVEFORM`) to `ThemeConfig` and `SettingsDataStore`, persisted across app launches.
- **Player Screen & Settings Integration**: Added conditional rendering in `PlayerScreen` for classic smooth slider vs. dynamic waveform bars, with a dedicated selector in `SettingsScreen` under Player Customization.

## [4.3.0] - 2026-11-24

### Added
- **A-B Repeat Segment & Audio Practice Loop Engine**: Added `ABLoopState` and `ABLoopManager` in `:core:player` enabling precision looping between markers A and B with auto-rewind seeking and iteration counting.
- **Player Screen Tactile A-B Console**: Integrated A-B loop boundary chips (`Set A`, `Set B`), active loop iteration indicator (`Loop #N`), and one-tap clear button in `PlayerScreen`.
- **ViewModel Integration**: Added `setABLoopPointA()`, `setABLoopPointB()`, `toggleABLoop()`, and `clearABLoop()` to `PlayerViewModel`.

## [4.2.0] - 2026-11-24

### Added
- **Bedtime Sound Handoff Engine**: Integrated music fade-out with automatic ambient noise activation in `SleepTimerManager` when falling asleep. Smoothly hands off playback to procedural ambient soundscapes (`AmbientSoundSynthesizer`, e.g. Brown Noise or 2.5Hz Delta Waves).
- **Configurable Handoff Profiles & Night Timer**: Added configurable handoff parameters (`bedtimeHandoffSoundType`, `bedtimeHandoffVolume`, `bedtimeHandoffDurationMinutes`) persisted in `SettingsDataStore`.
- **Settings UI Controls**: Added interactive "Bedtime Sound Handoff" toggle, sound profile selector chips, volume slider, and duration chips (30m, 1h, 2h, All Night) in `SettingsScreen` under Sleep Timer.

## [4.1.0] - 2026-11-24

### Added
- **Audiophile 5-Band Parametric Equalizer Faders**: Enhanced `NexusAudioEffects` with fine-grained band level tuning (`setBandLevel`, `setCustomBands`, `getBandLevels`) across standard center frequencies (60Hz Sub-Bass, 230Hz Mid-Bass, 910Hz Midrange, 3.6kHz Presence, 14kHz Air) spanning ±12 dB (±1200 mB).
- **Interactive Equalizer Console**: Added real-time 5-band vertical slider console in `SettingsScreen` with numeric dB indicators and a one-tap "Reset Flat" button.
- **Custom Preset Persistence**: Added `customEqBands` to `ThemeConfig` and `SettingsDataStore`, automatically persisting personalized equalizer curves across launches.

## [4.0.0] - 2026-11-24

### Added
- **Bedtime Ambient Soundscapes & Binaural Brainwave Synthesizer**: Implemented procedural 16-bit PCM audio math engine in `AmbientSoundGenerator` generating 44.1kHz stereo soundscapes across 8 distinct profiles:
  - `Brown Noise`: Brownian random walk with leak factor for deep low-frequency ADHD masking and restorative sleep.
  - `Pink Noise`: Paul Kellet 3-pole IIR filter (1/f spectral density) simulating natural rainfall and rustling leaves.
  - `White Noise`: Uniform flat spectral density for high-power ambient environmental sound masking.
  - `Binaural Alpha Waves (10Hz)`: 216Hz/226Hz stereo carrier separation promoting calm alertness and focus.
  - `Binaural Theta Waves (6Hz)`: 197Hz/203Hz stereo carrier separation for meditation and REM dream states.
  - `Binaural Delta Sleep (2.5Hz)`: 148.75Hz/151.25Hz carrier separation designed for deep slow-wave restorative sleep.
  - `Solfeggio 432Hz & 528Hz`: Harmonic natural resonance tones.
- **Low-Latency AudioTrack Streaming Player**: Built `AmbientSoundSynthesizer` in `:core:player` with coroutine synthesis streaming, volume attenuation, and auto-shutoff coordination with `SleepTimerManager`.
- **Material 3 Expressive UI & DataStore Persistence**: Added Bedtime & Ambient Soundscapes card in `SettingsScreen` with live playback controls, volume slider, sound profile carousel, and persistent preferences in `SettingsDataStore`.

## [3.9.0] - 2026-11-24

### Added
- **Smart Playlist & Queue Deduplication Engine**: Added `PlaylistDeduplicator` in `:core:domain` detecting exact and semantic duplicate tracks by normalizing titles (removing noise tags like `(Official Audio)`, `[Audio]`, `(Official Music Video)`, etc.) and matching artist metadata.
- **Queue and Library Deduplication**: Integrated `deduplicateQueue()` in `ManageQueueUseCase` and `PlayerViewModel`, and `deduplicateLikedTracks()` in `MusicRepository` and `SettingsViewModel`.
- **UI Integration**: Added "Clean Duplicates" action button in `LibraryScreen` and "Clean Library Duplicates" option in `SettingsScreen`.

## [3.8.0] - 2026-11-24

### Added
- **Sleep Timer Accelerometer Shake-to-Extend**: Integrated `SensorManager.TYPE_ACCELEROMETER` motion detection with debouncing in `SleepTimerManager` to dynamically add extra minutes (+5m, +10m, +15m) when the user gently shakes their device.
- **Dynamic Fade Cancellation**: Automatically resets volume attenuation and restores 100% volume upon motion extension when falling asleep.
- **Settings UI & Configuration**: Added "Shake to Extend" toggle and extension duration chip carousel in `SettingsScreen` under Sleep Timer.

## [3.7.0] - 2026-11-24

### Added
- **Standard M3U / M3U8 Playlist Export & Import Engine**: Implemented `exportPlaylistToM3u` in `BackupManager` producing standard `#EXTM3U`, `#PLAYLIST:`, and `#EXTINF:` directives mapped to track titles, artists, durations, and streaming URLs.
- **Resilient M3U8 Stream Parsing**: Added `importPlaylistFromM3u` capable of ingesting arbitrary extended M3U/M3U8 playlists with YouTube URL / ID extraction and direct Room persistence.
- **Settings UI M3U Integration**: Added "Export Favorites as M3U" and "Import M3U / M3U8 Playlist" action items in `SettingsScreen` under Data & Backup.

## [3.6.0] - 2026-11-24

### Added
- **Audio Stream Quality & Adaptive Cellular Data Saver**: Implemented intelligent streaming bitrate tiers (`AUTO`, `HIGH`, `MEDIUM`, `LOW`) in `NewPipeMusicSource` and `FakeMusicSource`.
- **Dynamic Metered Network Introspection**: Integrated `ConnectivityManager.isActiveNetworkMetered` via `ResolveStreamUseCase` to automatically stream in Data Saver mode (~50-70 kbps) on cellular data while maintaining maximum fidelity (160-256 kbps) on unmetered Wi-Fi.
- **Settings Quality Selector & Live Connection Badge**: Added interactive quality preference chips and live network status indicators in `SettingsScreen` and `SettingsViewModel`.

## [3.5.0] - 2026-11-24

### Added
- **Recent Search History & Quick Suggestion Chips**: Implemented persistent recent search query caching in `SettingsDataStore`, automatically capturing executed searches up to 10 entries.
- **Search UI Quick Action Chips**: Rendered horizontal `InputChip` row in `SearchScreen` for instant re-queries, with individual dismiss icons and a full "Clear" action.

## [3.4.0] - 2026-11-24

### Added
- **Full JSON Backup & Restore Engine**: Enhanced `BackupManager` to serialize and restore user custom playlists alongside favorites and theme settings in versioned JSON.
- **NewPipe Backup Migration**: Added `importFromNewPipeJson` in `BackupManager` parsing NewPipe playlist export formats and importing streams into local NexusTune playlists.
- **Settings UI Integration**: Added interactive Export, Restore, and Import buttons in `SettingsScreen` under Data & Backup with real-time status messaging.

## [3.3.0] - 2026-11-24

### Added
- **Expanded 4x2 Interactive Queue Home Screen Widget**: Added `NexusTuneQueueWidgetProvider` presenting active track playback controls (Prev, Play/Pause, Next) alongside an up-next queue preview directly on Android home screen.
- **Dynamic Queue Broadcast Architecture**: Synchronized widget state updates with `MainActivity` and `PlayerController` reactive playback state flows.

## [3.2.0] - 2026-11-24

### Added
- **Listening Habit Insights & Genre Distribution Analytics**: Implemented `ListeningHabitAnalyzer` in `core:domain:analytics` evaluating track history metadata to detect user genre affinity percentages and peak listening periods.
- **Top Genres Visual Breakdown**: Added top genre affinity chips with percentages and peak period badges in `SettingsScreen` under Listening Analytics.

## [3.1.0] - 2026-11-24

### Added
- **Dual-Engine Dynamic Crossfade DSP & Curve Fader**: Implemented `CrossfadeFader` in `core:player:crossfade` calculating real-time mathematical volume attenuation for incoming and outgoing tracks across crossfade windows.
- **Acoustic Energy Preservation Curves**: Added `CrossfadeCurve` support (`EQUAL_POWER`, `LINEAR`, `EXPONENTIAL`) where `EQUAL_POWER` maintains constant energy ($\cos^2(\theta) + \sin^2(\theta) = 1.0$) eliminating center-transition dips.
- **Settings UI Controls**: Added crossfade curve selector chips in `SettingsScreen` under Audio Optimization when crossfade duration is active.

## [3.0.0] - 2026-11-24

### Added
- **Spatial Audio Headphone Virtualization & 3D Stage Simulation**: Integrated Android hardware `Virtualizer` (`android.media.audiofx.Virtualizer`) in `NexusAudioEffects` providing binaural acoustic room emulation for headphones.
- **Acoustic Stage Modes**: Added configurable stage emulation modes: `STUDIO` (250mB), `LOUNGE` (500mB), `CONCERT_HALL` (750mB), and `WIDE_ARENA` (1000mB).
- **Settings UI Controls**: Added "Spatial Audio (3D Virtualizer)" toggle and acoustic stage mode filter chips in Settings under Equalizer & Sound Effects.

## [2.9.0] - 2026-11-24

### Added
- **Smart Safe Listening & Acoustic Dose Tracking**: Implemented `SafeListeningManager` in `core:player:hearing` calculating cumulative acoustic sound dose based on WHO / ITU-T H.870 safety standards.
- **Hardware Volume Limiter Protection**: Added output volume ceiling clamping in `PlayerController` (`70%`, `80%`, `90%`, `100%`) preventing sudden high-volume spikes and protecting auditory health.
- **Hearing Protection Settings**: Added live weekly sound dose progress indicator and volume limiter selection chips in Settings under Hearing Protection.

## [2.8.0] - 2026-11-24

### Added
- **Parametric Equalizer Expansion**: Expanded `EqualizerPreset` with `CLASSICAL`, `JAZZ`, `HIP_HOP`, `ACOUSTIC`, and `CUSTOM` hardware audio DSP frequency profiles in `NexusAudioEffects`.
- **Auto-Genre Profile Adaptation**: Created `GenreAdaptiveEqManager` in `core:player` inspecting track title, artist, and album keywords to automatically calibrate equalizer curves to the music style in real time.
- **Settings UI Controls**: Added "Auto-Genre EQ Adaptation" toggle in Settings under Equalizer & Sound FX.

## [2.7.0] - 2026-11-24

### Added
- **Synced Lyrics Offline Caching & Room Persistence**: Implemented persistent local caching of downloaded plain and synced LRC lyrics in Room database (`LyricsEntity` and `LyricsDao`), enabling instant zero-network offline lyric display.
- **Per-Track Lyrics Offset Calibration Memory**: Automatically saves user manual timing calibration offsets per track ID, persisting custom synchronizations across app restarts.
- **Storage Management Controls**: Added "Clear Lyrics Cache" option in Settings Storage & Cache Management card.

## [2.6.0] - 2026-11-24

### Added
- **Gapless Playback Buffer Pre-Loading & Audio Pipeline Warm-up**: Implemented `GaplessPreloadManager` in `core:domain:preload` and `AudioPreloader` contract, priming upcoming track stream resolution and Media3 ExoPlayer's playlist buffer before the current track finishes.
- **Zero-Latency Seamless Transitions**: Configured ExoPlayer playlist pre-buffering eliminating stream resolution lag and achieving zero-millisecond gapless playback transitions across track boundaries.
- **Configurable Pre-Buffer Window**: Added toggle and threshold selector chips (`3s`, `5s (Default)`, `10s`) in Settings under Audio Optimization.

## [2.5.0] - 2026-11-24

### Added
- **Bit-Perfect USB DAC Mode & Exclusive Audio Routing**: Enhanced `AudioOutputRouteManager` in `core:player` with native USB DAC audio format introspection (24-bit/32-bit PCM, 48kHz, 96kHz, 192kHz) and bit-perfect platform resampler bypass toggle.
- **Fidelity Badge in Player Screen**: Dynamic fidelity pill displaying exact sample rate and bit-depth metadata (e.g. `24-bit / 96kHz`) for connected external DACs and audiophile headphones.
- **Resampling & Exclusive Mode Settings**: Added switches and sample rate override chips (`Auto`, `48 kHz`, `96 kHz`, `192 kHz`) in `SettingsScreen` under Hi-Res Audio & Bit-Perfect Mode.

## [2.4.0] - 2026-11-24

### Added
- **Smart Dynamic Queue & Auto-Play Infinite Mix**: Implemented `AutoPlayQueueManager` in `core:domain` proactively replenishing queue with similar artist/track discovery when queue drops to 2 or fewer remaining tracks.
- **Endless Discovery Context Engine**: Added `GetAutoPlayRecommendationsUseCase` querying related tracks and filtering duplicates against active queue and playback history.
- **Settings UI Controls**: Added "Infinite Auto-Play (Endless Mix)" toggle in Settings under Audio Optimization.

## [2.3.0] - 2026-11-24

### Added
- **Sleep Timer Gentle Audio Fade-Out**: Added progressive perceptual volume attenuation over the final countdown seconds (Instant, 10s, 30s, 60s) to avoid abrupt audio interruption.
- **Dynamic Perceptual Volume Multiplier**: Integrated smooth quadratic perceptual volume scaling in `SleepTimerManager` with automatic volume restoration on timer cancel or completion.
- **Settings UI Controls**: Added fade-out duration selector chips and live fading state indicator in Settings sleep timer card.

## [2.2.0] - 2026-11-24

### Added
- **Audio Visualizer & Waveform Engine**: Integrated native Android `Visualizer` DSP frequency analysis with fallback organic synthesis in `VisualizerManager` (`core:player`), calculating 32 frequency bands and real-time amplitude with exponential smoothing.
- **Dynamic 120Hz Visualizer Views**: Created `AudioVisualizerView` (`core:ui`) rendering real-time animated vertical `Spectrum Bars`, orbiting `Circular Wave`, and dynamic `Glow Pulse` synchronized with playback tempo and amplitude.
- **Player & Settings Visualizer Controls**: Added interactive Visualizer Mode selection chips in `PlayerScreen` and `SettingsScreen` with instant switching and persistence.

## [2.1.0] - 2026-11-24

### Added
- **Per-Track Loudness Analysis & Gain Calibration**: Added per-track gain metadata (`loudnessGainDb`) to `Track` and `TrackEntity` with seamless Room database schema integration.
- **Dynamic DSP Gain Adaptation**: Enhanced `NexusAudioEffects` with `applyTrackLoudnessGain(gainDb, baseTargetGainmB)` computing target millibels with headroom clamping [-12dB, +12dB] to prevent digital clipping, wired directly to `PlayerController` active track changes.
- **Per-Track Gain Calibration Toggle**: Added user preference `perTrackGainEnabled` in `ThemeConfig` and interactive switch in Settings under Audio Optimization.

## [2.0.0] - 2026-11-24

### Added
- **Audio Output Routing Engine**: Implemented `AudioOutputRouteManager` in `core:player` with native Android `AudioManager` and `AudioDeviceCallback` integration, automatically tracking live audio routing changes across Bluetooth A2DP, USB DACs, Wired Headsets, and Phone Speakers.
- **Real-Time Hi-Res Audio Route Indicator**: Added dynamic audio route pill in `PlayerScreen` with contextual hardware icons (`Bluetooth`, `Headphones`, `VolumeUp`) and automatic Hi-Res container highlighting.

## [1.9.0] - 2026-11-24

### Added
- **Offline Storage & Cache Auto-Pruning Engine**: Created `StorageManager` in `core:data` with directory size inspection, immediate cache reclamation, and LRU pruning against user-defined storage budgets.
- **Cache Size Budget & Management UI**: Added Storage & Cache Management card in Settings with selectable cache budgets (500MB, 1GB, 2GB, 5GB, Unlimited) and one-tap stream cache clearance.

## [1.8.0] - 2026-11-24

### Added
- **Adaptive Playback Pitch & Speed Controller**: Implemented native playback speed (0.5x to 2.0x) and pitch control via Media3's `PlaybackParameters` in `PlayerController`.
- **Player Screen Speed Selection Chips**: Integrated interactive playback speed selection chips directly into `PlayerScreen`, allowing one-tap speed shifts during active playback.

## [1.7.0] - 2026-11-24

### Added
- **Custom Playlist Cover Art & Visual Personalization**: Added support for setting custom artwork URIs on personal playlists, with automated Material 3 Expressive dynamic gradient container fallbacks when no artwork is configured.
- **Library Playlist Management**: Revamped `LibraryScreen` into a 3-tab layout (`Liked`, `Playlists`, `History`) with interactive playlist creation and artwork editing dialogs.
- **Batch Queue Operations**: Implemented `addTracksNext(tracks)`, `removeTracks(trackIds)`, and `clearUpcoming()` in `QueueRepository` and `ManageQueueUseCase` for powerful batch queue manipulation.

## [1.6.0] - 2026-11-24

### Added
- **Smart Volume Normalization (ReplayGain)**: Integrated `LoudnessEnhancer` DSP into `NexusAudioEffects` and ExoPlayer audio session to normalize audio output loudness across dynamic web streams without digital clipping.
- **Configurable Target Boost**: Added target gain boost presets (+2dB, +4dB, +6dB) and live toggle in Settings screen under Audio Optimization.

## [1.5.0] - 2026-11-24

### Added
- **Silence & Non-Music Skip Segment Manager**: Added `SkipSegmentManager` in `core:player` with automatic detection and seamless seek-skipping over non-music intros, outros, sponsor blocks, and silence gaps.
- **Auto-Skip Settings Toggle**: Added an instant toggle under Audio Optimization in Settings to enable/disable automated skip segments.

## [1.4.0] - 2026-11-24

### Added
- **Synced Lyrics Manual Offset Calibration**: Added live offset calibration (+/- 100ms, reset) directly in `PlayerScreen` and `PlayerViewModel` for precision audio/lyrics synchronization.
- **Time-of-Day Listening Analytics**: Expanded `ListeningStats` and `MusicRepositoryImpl` with 4-phase day distribution (Morning, Afternoon, Evening, Night) calculated from Room playback history.
- **Settings Screen Updates**: Added Time-of-Day analytics summary in Settings dashboard and updated build version to v1.4.0.

## [1.3.0] - 2026-11-24

### Added
- **Android Auto & Automotive Head-Unit Integration**: Transitioned `NexusPlaybackService` to `MediaLibraryService` (`MediaBrowserService` compatible) with browsable root categories (`Liked Tracks`, `Recently Played`, `Playlists`) and automotive app descriptor (`automotive_app_desc.xml`).

## [1.2.0] - 2026-11-24

### Added
- **Dynamic Crossfade & Transition Smoothing**: Added configurable audio crossfading (Gapless Off, 2s, 4s, 6s, 8s, 12s) with smooth volume ramp down/up in `PlayerController` and `ThemeConfig`.
- **Home Screen Playback Widget**: Added Android AppWidget (`NexusTuneWidgetProvider`) with Material 3 styling, live track information, play/pause and next track actions, updating automatically from player session broadcasts.
- **NewPipe Backup Migration**: Added `NewPipeBackupImporter` in `core:data` allowing direct import of playlists, streams, and titles from NewPipe JSON export files into Room storage.
- **Settings Screen Expansion**: Added dedicated "Audio Crossfade & Transitions" selection chips and NewPipe backup import entry point.

## [1.1.0] - 2026-11-24

### Added
- **Sleep Timer Engine**: Added `SleepTimerManager` with preset durations (15m, 30m, 45m, 60m), countdown state flow, active timer notification banner with cancel action, and seamless End-of-Track auto-pause.
- **Hardware Equalizer DSP Presets**: 6 hardware DSP tuning presets (`FLAT`, `BASS_BOOST`, `TREBLE_BOOST`, `VOCAL`, `ELECTRONIC`, `ROCK`) applied directly to ExoPlayer's audio session ID with real-time UI switching.
- **Personalized Listening Analytics**: Real-time listening stats aggregated from Room playback history, displaying total tracks played, total listening duration, and top 5 most listened artists.
- **Enhanced Settings UI**: Revamped Settings screen with active Sleep Timer countdown card, DSP preset horizontal chip carousel, and Listening Analytics summary dashboard.

## [1.0.0] - 2026-11-24

### Added
- **Core Architecture**: Established 16-module Clean Architecture Gradle setup (`:app`, `:core:model`, `:core:common`, `:core:domain`, `:core:data`, `:core:database`, `:core:datastore`, `:core:extractor`, `:core:player`, `:core:designsystem`, `:core:ui`, `:core:testing`, `:feature:home`, `:feature:search`, `:feature:library`, `:feature:player`, `:feature:settings`, `:benchmark`).
- **Extractor Engine**: Native YouTube streaming and metadata resolution using pinned `TeamNewPipe:NewPipeExtractor:v0.27.1`.
- **Playback Engine**: Media3 ExoPlayer background playback with foreground notification service, lockscreen controls, and system audio focus integration.
- **Audio DSP**: 5-band hardware-accelerated Equalizer, Bass Boost, and Virtualizer effects via `NexusAudioEffects`.
- **Theming & Personalization**:
  - Dynamic Material 3 Expressive color schemes (Dynamic, Vibrant, Expressive, AMOLED).
  - 4 full Player screen layouts: Art Forward, Vinyl Record, Synced Lyrics, Minimalist.
  - 3 Mini-player variants: Docked Bar, Floating Pill, Compact Slim.
  - AMOLED Pure Black mode.
  - Shape presets (Rounded, Expressive, Sharp).
- **Library & Storage**: Room database for persistent Liked Tracks, Playlists, and Play History.
- **Offline & Downloads**: Background track downloading via WorkManager and OkHttp with automatic offline stream fallback.
- **Backup & Restore**: Standalone JSON export and import for user library and theming preferences.
- **Performance**: Validated 120Hz high refresh rate rendering (P99 <= 1.0ms frame time on Vivo V2518).

