# NexusTune Manual QA Verification Checklist

This document details the manual QA verification steps executed on physical hardware (Vivo V2518, Android 16).

---

## 1. First Launch & Onboarding
- [x] Launch app from clean state (no cache/data).
- [x] Home screen renders shimmer pulse skeleton, then presents curated feed cleanly.
- [x] System navigation bar and status bar edge-to-edge padding adhere to M3 specifications.

## 2. Search & Track Playback
- [x] Enter query in search bar; debounce prevents redundant requests.
- [x] Search suggestions appear in dropdown.
- [x] Tap search result: track begins streaming within < 800ms.
- [x] Mini-player expands smoothly from bottom bar.

## 3. Media Controls & Audio Focus
- [x] Play / Pause toggle works instantly with zero audio pop or artifact.
- [x] Scrubbing slider updates track position accurately.
- [x] Media notification displays track artwork, title, artist, and playback actions.
- [x] Disconnecting Bluetooth or headphones pauses playback gracefully (`AudioManager.ACTION_AUDIO_BECOMING_NOISY`).

## 4. Library & Persistence
- [x] Tap like heart icon on player; track immediately appears in Library "Liked" tab.
- [x] Played tracks automatically register in "History" tab in chronological order.
- [x] Unliking track immediately updates state without UI glitch.

## 5. Theming Engine & Display Mode
- [x] Toggle Dark Theme and Light Theme; entire color scheme transitions dynamically.
- [x] Enable "AMOLED Pure Black"; background transforms to `#000000`.
- [x] Switch between 4 Player Layouts (Art Forward, Vinyl, Synced Lyrics, Minimalist).
- [x] Switch between 3 Mini-Player styles (Docked Bar, Floating Pill, Compact Slim).
- [x] Verify 120Hz display refresh mode remains active during heavy scrolling.

## 6. Equalizer DSP & Backup/Restore
- [x] Enable 5-band equalizer; audio profile frequency response modulates in real time.
- [x] Export backup JSON; verify file contains theme preferences and liked tracks.
- [x] Import backup JSON; verify restoration of saved state.

