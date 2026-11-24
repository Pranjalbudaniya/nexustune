# Phase Improvement 22: Listening Habit Insights & Genre Distribution Analytics (v3.2.0 Milestone)

## Goal
Implement rich **Listening Habit Insights & Genre Distribution Analytics** in `core:domain:analytics` and `MusicRepositoryImpl`. This calculates the user's top genres, play percentages, and peak listening period (Morning, Afternoon, Evening, Late Night) from their playback history, displayed via a responsive UI breakdown in Settings.

## Architecture & Design
1. **Core Model (`core:model`)**:
   - `data class GenreShare(val genre: String, val playCount: Int, val percentage: Int)`
   - Update `ListeningStats`:
     - `val topGenres: List<GenreShare> = emptyList()`
     - `val peakPeriod: String = "All Day"`

2. **Core Domain Analytics (`core:domain`)**:
   - Create `ListeningHabitAnalyzer` in `core:domain:analytics`:
     - `determinePeakPeriod(morning: Int, afternoon: Int, evening: Int, night: Int): String`
     - `analyzeGenres(tracks: List<Triple<String, String, String?>>): List<GenreShare>`
   - Unit tests in `ListeningHabitAnalyzerTest`.

3. **Core Data Repository (`core:data`)**:
   - In `MusicRepositoryImpl.getListeningStats()`:
     - Map history tracks into `ListeningHabitAnalyzer`.
     - Populate `topGenres` and `peakPeriod` in emitted `ListeningStats`.

4. **Settings & UI (`feature:settings`)**:
   - In `SettingsScreen`:
     - Render "Peak Listening Period" badge.
     - Render "Top Genres" breakdown with styled percentage chips and progress bars.
     - Update About banner to `NexusTune v3.2.0` | `Listening Habit Insights & Genre Distribution Analytics`.

5. **Verification**:
   - Run `./scripts/gate.sh slice`.
   - Run `./scripts/gate.sh final` to generate production release APK.
   - Tag release `v3.2-1`.
