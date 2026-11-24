# Phase Improvement 23: Expanded 4x2 Interactive Queue Home Screen Widget (v3.3.0 Milestone)

## Goal
Implement `NexusTuneQueueWidgetProvider`, an expanded **4x2 Interactive Queue Home Screen Widget** that allows users to monitor active playback with complete media controls (Play/Pause, Next, Previous) and view upcoming tracks in the dynamic playback queue directly on their Android launcher.

## Architecture & Implementation
1. **Layout & Widget Provider (`app`)**:
   - Create `app/src/main/res/layout/widget_nexus_queue.xml`:
     - 4x2 expanded layout (~250dp x 140dp).
     - Active track header: album art, title, artist, and playback control buttons (Prev, Play/Pause, Next).
     - "Up Next in Queue" list displaying the next upcoming songs.
   - Create `app/src/main/res/xml/nexus_queue_widget_info.xml`:
     - 4x2 target cells, home_screen category.
   - Create `NexusTuneQueueWidgetProvider` in `app/src/main/kotlin/com/nexus/tune/widget/`:
     - Handles `ACTION_QUEUE_TOGGLE_PLAY_PAUSE`, `ACTION_QUEUE_SKIP_NEXT`, `ACTION_QUEUE_SKIP_PREV`, and `ACTION_UPDATE_QUEUE_STATE`.
     - Populates active track and upcoming queue slots using `RemoteViews`.
     - Provides `notifyPlaybackChanged(context: Context)` companion helper.

2. **Integration (`app`)**:
   - Register `NexusTuneQueueWidgetProvider` in `AndroidManifest.xml`.
   - Update `MainActivity.kt` to trigger `NexusTuneQueueWidgetProvider.notifyPlaybackChanged` on playback state changes.
   - Update Settings About banner to `NexusTune v3.3.0` | `Expanded 4x2 Interactive Queue Home Screen Widget`.

3. **Verification**:
   - Unit tests in `NexusTuneQueueWidgetTest`.
   - Run `./scripts/gate.sh slice`.
   - Run `./scripts/gate.sh final` to generate release APK.
   - Tag release `v3.3-1`.
