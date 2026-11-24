# Phase Improvement 25: Smart Recent Search History & Quick Suggestion Chips (v3.5.0 Milestone)

## Goal
Implement a persistent **Recent Search History & Quick Suggestion Chips Engine** in `core:datastore`, `feature:search`, and `app`. This records user search queries, provides instant one-tap retrieval via interactive chips when the search bar is idle or empty, and supports individual dismissal and full clear actions.

## Architecture & Implementation
1. **Core DataStore Persistence (`core:datastore`)**:
   - In `SettingsDataStore.kt`:
     - `recentSearches: Flow<List<String>>` (persisting up to 10 distinct search queries).
     - `addRecentSearch(query: String)` (deduplicated, newest first).
     - `removeRecentSearch(query: String)`.
     - `clearRecentSearches()`.

2. **Search Feature (`feature:search`)**:
   - In `SearchViewModel.kt`:
     - Expose `recentSearches: StateFlow<List<String>>`.
     - Automatically record executed queries into `SettingsDataStore.addRecentSearch`.
     - Expose `removeRecentSearch(query: String)` and `clearRecentSearches()`.
   - In `SearchViewModelTest.kt`:
     - Unit tests for adding, removing, and clearing recent searches.
   - In `SearchScreen.kt`:
     - Render "Recent Searches" header with "Clear" action.
     - Render interactive horizontal chips with tap-to-search and trailing dismiss icons.
     - Update About banner to `NexusTune v3.5.0` | `Recent Search History & Quick Suggestion Chips`.

3. **Application Wiring (`app`)**:
   - In `MainActivity.kt`: Pass `settingsDataStore = app.settingsDataStore` into `SearchViewModel`.

4. **Verification**:
   - Run `./scripts/gate.sh slice`.
   - Run `./scripts/gate.sh final` to generate production release APK.
   - Tag release `v3.5-1`.
