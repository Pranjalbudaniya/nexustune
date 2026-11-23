# Council Round 2: Performance / Reliability Engineer Cross-Examination

## Objections to Other Proposals
1. **To UX Lead on Wavy Progress Bars**: Wavy animations must be implemented using pure Canvas drawing via `Modifier.drawWithCache` or `Modifier.drawBehind`, operating on hardware graphics layers without re-allocating path objects every frame. If it causes jank, it gets automatically degraded to standard linear progress.
2. **To Architect on Dual-Player Crossfade**: Two simultaneous ExoPlayer instances are vetoed for v1 on memory and decoder limits. Single-player volume fade with gapless transition is approved.

## Adopted Ideas
1. **From Power-User Skeptic**: Demanding robust offline caching and deterministic local fallback.
2. **From Architect**: `WorkManager` for background download operations.

## Flawed Assumptions Challenged
- Assumption that 120Hz can be guaranteed without explicitly setting `preferredDisplayModeId` on the window. Many OEM skins default apps to 60Hz or 90Hz unless requested.

