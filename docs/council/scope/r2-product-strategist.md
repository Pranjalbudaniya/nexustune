# Council Round 2: Product Strategist Cross-Examination

## Objections to Other Proposals
1. **To Power-User Skeptic on 10-band Equalizer**: A full 10-band parametric EQ in v1 introduces extensive DSP state management that risks audio glitches on some hardware. Propose adopting standard 5-band Android audio effect presets in v1, deferring custom 10-band parametric curves to v1.1.
2. **To UX Lead on Wavy Progress Indicator during active playback**: Wavy canvas animations ticking every frame can eat into the 8.33ms frame budget if not strictly isolated. Must require Performance Engineer's sign-off on the shader/draw modifier.

## Adopted Ideas
1. **From Android Architect**: Strict separation of `:core:domain` without Android dependencies, and `WorkManager` for downloads.
2. **From Performance Engineer**: Automatic 403/410 stream re-resolution in `ResolvingDataSource` so users never see random streaming cuts.

## Flawed Assumptions Challenged
- Assumption that users want local and streamed music strictly partitioned. Users want a single unified Search and Library experience where local and online tracks coexist seamlessly.

