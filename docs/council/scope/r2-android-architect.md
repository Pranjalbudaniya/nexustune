# Council Round 2: Android Architect Cross-Examination

## Objections to Other Proposals
1. **To UX Lead on Shared-Element Morph Transitions**: In Jetpack Compose, complex shared-bounds animations with heavy blur can drop frames on older devices. Morphing must use Compose 1.7+ `Modifier.sharedElement` within `SharedTransitionLayout` backed by hardware layers, and avoid dynamic blur during the transition window.
2. **To Power-User Skeptic on Dual-Player Crossfade**: Running two simultaneous `ExoPlayer` instances doubles memory usage, decoder instances, and audio focus contention. Propose volume ramp crossfade using a single player with gapless transition, or defer dual-player crossfade to v1.1.

## Adopted Ideas
1. **From UX Lead**: 3-destination primary navigation (`Home`, `Search`, `Library`) with persistent docked mini-player.
2. **From Product Strategist**: Focusing v1 on audio streams only (no video decoders).

## Flawed Assumptions Challenged
- Assumption that Room queue persistence is trivial. Storing a 500-item queue naively on every track change can cause SQLite disk thrashing. We must debounce queue persistence and write async on `Dispatchers.IO`.
