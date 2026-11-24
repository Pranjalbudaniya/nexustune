# Phase Improvement 24: Complete Backup, Restore & NewPipe Import Engine (v3.4.0 Milestone)

## Goal
Implement a robust **Complete Backup & Restore Engine** supporting full JSON exports of favorites, custom playlists, and settings, as well as native **NewPipe Backup Migration** parsing NewPipe playlist/stream structures into NexusTune Room entities.

## Architecture & Implementation
1. **Core Data Backup (`core:data`)**:
   - In `BackupManager.kt`:
     - Add `data class NewPipeImportResult(val playlistsImported: Int, val tracksImported: Int, val success: Boolean)`.
     - Implement `importFromNewPipeJson(jsonString: String): NewPipeImportResult`.
     - Expand `exportBackupJson()` to include user playlists from `database.playlistDao()`.
     - Expand `importBackupJson(jsonString: String)` to restore user playlists.
   - In `BackupManagerTest.kt`:
     - Unit tests verifying NewPipe JSON parsing, stream extraction, and playlist creation.
     - Unit tests verifying round-trip JSON backup/restore of playlists and liked tracks.

2. **Application Wiring & ViewModel (`app`, `feature:settings`)**:
   - In `NexusTuneApplication.kt`: Instantiate `backupManager = BackupManager(database, settingsDataStore)`.
   - In `SettingsViewModel.kt`:
     - Accept optional `backupManager: BackupManager?`.
     - Expose `exportBackup()`, `importBackup(jsonString: String)`, and `importFromNewPipe(jsonString: String)`.
     - Expose reactive StateFlows `backupExportResult` and `backupImportStatus`.
   - In `SettingsViewModelTest.kt`: Add unit tests for backup and NewPipe import actions.
   - In `SettingsScreen.kt`:
     - Add interactive action buttons ("Export", "Restore", "Import") and dynamic status message.
     - Update About banner to `NexusTune v3.4.0` | `Full JSON Backup/Restore & NewPipe Import Engine`.

3. **Verification**:
   - Run `./scripts/gate.sh slice`.
   - Run `./scripts/gate.sh final` to generate release APK.
   - Tag release `v3.4-1`.

