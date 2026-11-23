# Phase 6 Plan: Hardening & Failure Injection

## 1. Overview & Objectives
Phase 6 subjects NexusTune to rigorous edge-case testing, simulated network failures, stream URL expiration, rate-limiting, and process death recovery to guarantee rock-solid stability in unpredictable mobile environments.

## 2. Failure Scenarios to Test
1. **Stream URL Expiry (HTTP 403 / 410)**:
   - When a previously extracted YouTube streaming audio URL expires, playback fails.
   - The player engine must intercept the 403/410 error, re-trigger stream URL extraction via `ResolveStreamUseCase`, update the active ExoPlayer MediaItem URI, and resume playback seamlessly.
2. **Network Loss & Offline Playback Fallback**:
   - When network drops or airplane mode is activated, track streaming falls back to local storage if downloaded (`downloadedFilePath` present in database).
   - If not downloaded, the UI cleanly renders `ErrorBanner` without crashing.
3. **Rate Limiting & Backoff (HTTP 429)**:
   - When NewPipeExtractor encounters rate limits or CAPTCHA triggers, gracefully catch `ParsingException` / `ReCaptchaException` and surface human-readable rate limit status without unhandled exceptions.
4. **Process Death Queue Restoration**:
   - Store current playlist and playback position index in persistent storage.
   - When process restarts, restore the last active queue state.

## 3. Implementation & Test Tasks
- Create `StreamExpiryRecoveryTest` in `core/data` or `core/domain`.
- Create `OfflineFallbackTest` in `core/data`.
- Create `ProcessDeathRestorationTest` in `core/player`.
- Any discovered defects documented in `docs/bugs/BUG-###.md` and immediately remediated.

## 4. Acceptance Checks
- All hardening and failure injection tests pass.
- `./scripts/gate.sh slice` passes.
