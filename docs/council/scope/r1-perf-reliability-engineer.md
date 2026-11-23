# Council Round 1: Performance / Reliability Engineer Proposal

## Role Mandate
Own the 8.33 ms frame budget (120 Hz) and typed failure models. Enforce zero main-thread disk/network I/O, isolate ticking state, and veto unconstrained graphics.

## Proposals & Non-Negotiable Budgets

### 1. Frame Budget & Recomposition Guardrails
- **Position Ticking Isolation**: Playback progress updates (e.g. every 200 ms) MUST NOT recompose the album art, track details, or player controls. The seekbar and timer must be encapsulated in an isolated composable reading position via a lambda or drawing directly to Canvas.
- **Display Mode Request**: Query `Display.supportedModes`, find 120 Hz mode ID, and set `window.attributes.preferredDisplayModeId`.
- **Image Pipeline**: Coil with disk and memory cache. Image decodes off the main thread, downscaled to target size. Blur effects must use calibrated `Modifier.blur` with fallback or scrim on low memory.

### 2. Failure Architecture
- Offline/Airplane mode must degrade gracefully to downloaded tracks with clear offline UI indicator.
- Stream expiration: 403 / 410 HTTP errors must automatically trigger a single transparent re-extraction and retry before exposing an error to the user.
- Thermal monitoring: Benchmark runs must monitor battery temperature and stop if thermal throttling occurs.

