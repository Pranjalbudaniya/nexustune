# Ranked Feature Roadmap: NexusTune

## 1. Shipping in v1 (Core Architecture & Polish)
1. **Core YouTube Music Audio Streaming**:
   - High-fidelity audio stream extraction (Opus 160kbps / AAC 256kbps).
   - Instant query search with suggestions and category filters (Songs, Albums, Artists, Playlists).
   - Resilient stream resolution with automatic 403/410 re-extraction.
2. **Robust Media3 Playback Engine**:
   - `MediaSessionService` with lockscreen controls, notification actions, and Bluetooth/headset integration.
   - Gapless playback and skip silence support.
   - 5-band equalizer with presets and bass boost.
   - Sleep timer (minutes countdown or end-of-track mode).
3. **Queue & Library Management**:
   - Full queue drag-and-drop reordering, swipe-to-remove, and multi-track additions.
   - Persistent queue in Room surviving process death and device reboot.
   - Unified Library: Device local files (`MediaStore`) merged with user favorites, custom playlists, and play history.
   - Backup & Restore (JSON export and import).
4. **Offline Listening**:
   - Background track downloading via `WorkManager`.
   - Dedicated "Downloads" offline tab with automatic offline playback fallback.
5. **Material 3 Expressive UI & 120Hz Motion**:
   - 4 Full Player layouts: Art-Forward, Vinyl Record, Synced Lyrics, and Minimalist.
   - 3 Mini-Player styles: Docked Bar, Floating Pill, and Compact Slimline.
   - Live dynamic color theming, custom color seeds, AMOLED black, and 5 shape styles.
   - Spring-based morphing transitions between mini-player and full-player.
6. **Synced Lyrics**:
   - Real-time line-by-line scrolling lyrics via LRCLIB with graceful fallback.

---

## 2. Planned for v1.1 (Enhancement Scope)
1. **Dual-Player Dynamic Crossfade**:
   - True crossfade with configurable ramp curves (2s - 12s) using twin player buffers.
2. **Listening Analytics**:
   - Detailed statistics: most listened artists, tracks, genre distribution, and listening time breakdown.
3. **Android Auto Support**:
   - MediaBrowserService integration for in-car dashboard navigation.
4. **Home Screen Widgets**:
   - Glance-based Compose widgets (4x1 compact playback and 4x2 interactive queue).
5. **NewPipe Backup Migration**:
   - Direct import of NewPipe database zip backups.

---

## 3. Explicitly Cut (With Reasons)
1. **Social / Activity Feeds**:
   - Cut to preserve user privacy, prevent algorithmic distractions, and maintain a lightweight offline-first focus.
2. **Video Streaming & Rendering**:
   - Cut to conserve bandwidth, eliminate unnecessary video decoder memory footprint, and optimize strictly for 120Hz audio fluidity.
3. **Cloud Account Synchronization**:
   - Cut because NexusTune is a private, sideloaded personal music client that requires no Google or proprietary account logins.
