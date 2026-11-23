# Council Round 1: UX Lead Proposal

## Role Mandate
Own information architecture and user flows. Ensure any track can be found and played in under 3 taps, fully operable one-handed, with fluid physical continuity between all navigation states.

## Proposals

### Navigation & Information Architecture
1. **3-Destination Navigation**:
   - `Home`: Quick Picks, Recently Played, Top Artists, and Curated Explore cards.
   - `Search`: Instant search bar with real-time suggestions, search category chips, and clear history.
   - `Library`: Tabs for Playlists, Liked Songs, Downloaded, and Local Files with quick sort/filter pills.
2. **Mini-Player to Full-Player Continuity**:
   - The mini-player is not an overlay or a pop-up. It is a persistent surface container docked above navigation.
   - Expanding it triggers a shared-bounds transform where album art, title, and playback controls morph into the full player screen with spring physics.
3. **One-Handed Ergonomics**:
   - All interactive controls (play, pause, next, seekbar, queue button) placed in the lower 45% of the screen.
   - Swipe gestures on album art for skip forward/backward. Downward fling to dismiss player to mini-player.

### Visual Polish & Motion
- Zero linear transitions: Everything uses damped springs.
- Wavy progress indicator on seekbar during active playback.
- Previews for Light, Dark, AMOLED, and dynamic wallpaper color schemes.
