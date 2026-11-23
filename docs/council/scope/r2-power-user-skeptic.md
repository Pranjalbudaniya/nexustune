# Council Round 2: Power-User Skeptic Cross-Examination

## Objections to Other Proposals
1. **To UX Lead on Synced Lyrics**: Scraping lyrics from unverified open APIs creates fragile failure points and potential copyright takedowns. If included, it MUST fail gracefully and remain completely decoupled from core playback. If lyrics fail to load, playback must not be impacted by even 1 millisecond.
2. **To Product Strategist on Deferring Equalizer to 5 bands**: 5-band EQ is acceptable for v1 IF it uses the system AudioEffect API reliably and includes customizable presets that persist per audio device.

## Adopted Ideas
1. **From UX Lead**: One-handed lower-screen control placement.
2. **From Android Architect**: Debounced asynchronous queue persistence to prevent disk contention.

## Flawed Assumptions Challenged
- Assumption that streaming users don't care about offline files. Network outages happen on flights and subways; offline caching must be rock solid.

