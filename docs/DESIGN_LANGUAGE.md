# NexusTune Design Language Specification

## 1. Design Vision: Kinetic, Expressive, and Human
NexusTune is designed to feel like an artisanal high-end audio instrument fused with Google's Material 3 Expressive framework. It avoids generic AI-slop visual patterns (such as monotonous grids of identical rounded rectangles or static linear fades). Every interaction responds with physical spring physics, tactile weight, and deep optical hierarchy.

---

## 2. Signature Motion Character

### Spring Physics Architecture
NexusTune uses three explicit spring tiers calibrated for responsiveness and physical delight:

| Tier | Stiffness | Damping Ratio | Use Cases |
|---|---|---|---|
| **Bouncy** | `Spring.StiffnessMediumLow` (300f) | `Spring.DampingRatioMediumBouncy` (0.65f) | Mini-player expand/collapse, FAB interactions, toggle switches |
| **Snappy** | `Spring.StiffnessMedium` (800f) | `Spring.DampingRatioNoBouncy` (1.0f) | Bottom sheet dragging, tab switching, queue drag-and-drop handles |
| **Subtle** | `Spring.StiffnessLow` (150f) | `Spring.DampingRatioNoBouncy` (1.0f) | Color scheme transitions, album art crossfades (200ms), scrim fades |

### Motion Rules
1. **Zero Linear Easing**: No standard `tween()` with linear or generic cubic easing for spatial movements. Everything uses spring physics.
2. **Shared-Element Morphing**: Moving from mini-player to full-player is a continuous bounding-box and corner-radius morph. The artwork never pops into view; it scales continuously from 48dp to full display width.
3. **Haptic Pairing**: Key actions (play/pause toggle, queue track drop, favorite heart activation) emit micro-vibrations via `HapticFeedbackType.LongPress` or `TextHandleMove`.

---

## 3. Shape Vocabulary & Morphing Schemes

NexusTune supports 5 global shape presets configured in Settings that propagate dynamically to all components:

| Shape Preset | Container Corner | Button Corner | Artwork Corner | Badge / Pill Corner |
|---|---|---|---|---|
| **Full Expressive** | 28dp (Cut/Morph) | 20dp | 20dp | 50% (Circle/Pill) |
| **Soft Rounded** (Default) | 20dp | 16dp | 16dp | 50% |
| **Gentle** | 12dp | 8dp | 10dp | 16dp |
| **Sharp** | 4dp | 4dp | 4dp | 4dp |
| **Cut-Corner** | 16dp cut | 8dp cut | 12dp cut | 8dp cut |

---

## 4. Typography Hierarchy

NexusTune bundles open-source typography designed for legibility at high density:
- **Default Display Font**: `Outfit` / `Inter` (geometric modern sans-serif with high x-height)
- **Alternative Font 1**: `Plus Jakarta Sans` (expressive, rhythmic)
- **Alternative Font 2**: `JetBrains Mono` (industrial, technical aesthetic for audio stats)

### Type Hierarchy Scale
- **Display Large**: 36sp / 44sp line-height, Weight 700 (Now Playing Track Title in Art-Forward)
- **Headline Medium**: 24sp / 32sp line-height, Weight 600 (Screen Headers, Album Titles)
- **Title Medium**: 16sp / 24sp line-height, Weight 600 (Track list item title, Category headers)
- **Body Medium**: 14sp / 20sp line-height, Weight 400 (Artist names, metadata subtitles)
- **Label Small**: 11sp / 16sp line-height, Weight 500 (Audio format badges, time indicators)

---

## 5. Iconography Rules
- **Family**: Material Symbols Rounded (filled states for active/toggled elements, stroked 2.0dp for inactive elements).
- **No Emojis**: Never use emojis as substitutes for navigation icons or playback controls.
- **Touch Target**: Every icon has a minimum touch target of 48dp x 48dp (`Modifier.minimumInteractiveComponentSize()`).

---

## 6. Spacing Scale With Intent
We discard "16dp everywhere". Spacing reflects relationship:
- `4dp`: Micro-gap (between badge and title, icon and label)
- `8dp`: Tight grouping (track title and artist subtitle)
- `12dp`: Content list spacing (between adjacent queue tracks)
- `16dp`: Standard horizontal screen margin
- `24dp`: Section separation (between Quick Picks and Recommended Albums)
- `32dp`: Hero margin (between album art and player controls)

---

## 7. What Makes the Player Screen Recognizably NexusTune
1. **Dynamic Album Art Aura**: A soft, high-performance background gradient sampled from the album cover's vibrant and muted tones, blending smoothly into surface background with zero banding.
2. **Wavy Expressive Progress Line**: During playback, the progress line gently pulses with a subtle sine-wave animation that dynamically flattens when paused.
3. **Four Distinct Architectural Layouts**:
   - *Art-Forward*: Massive 320dp curved album artwork, floating title pill, ergonomic lower controls.
   - *Vinyl Record*: Photorealistic grooved vinyl disc sliding out from the sleeve with rotational physics.
   - *Synced Lyrics*: Full-screen fluid typography with line-by-line active focus.
   - *Minimalist*: Stark typography, compact artwork badge, large tactile scrub bar.
