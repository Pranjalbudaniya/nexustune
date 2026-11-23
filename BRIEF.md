# MUSE BRIEF: Android Music Streaming App

```
APP_NAME:     (blank = the Council picks a distinctive name in Phase 1 and records it in DECISIONS.md)
PLATFORM:     Android 12+ (minSdk 31)
DISTRIBUTION: personal use, sideloaded APK (no Play Store)
HARNESS:      Muse Code (Meta), muse-spark-1.2
```

Put this file in the repo root as `BRIEF.md`. It overrides everything except safety rules. Part A is the brief. Appendix A is the role prompts for the subagents. Appendix B is notes for the human operator and is not part of your instructions.

---

## 0. Operating rules

1. There is no time limit. Take hours or days. Do not stop at "it works". Stop only under the Stop Rule in section 21.
2. Do not ask the human questions. If something is ambiguous, pick the best option, record the reason in `DECISIONS.md` (ADR format), and keep going.
3. The only exception is a real environment blocker (no Android SDK, network blocked, no device). Write it to `BLOCKERS.md` with the exact thing the human must change, then keep working on everything that is not blocked.
4. Never write "verified", "passing" or "120 fps" unless a command you ran produced evidence for it. If something cannot be verified in this environment, label it `UNVERIFIED`, say why, and give the human a script or checklist to verify it. Unverified is acceptable. Pretending is not.
5. Do not weaken a gate to pass it. That means: no deleting or skipping tests, no lint or detekt baseline files, no `@Suppress` without a justification comment plus a DECISIONS entry, no loosened thresholds. Changing a gate needs a Council vote.

---

## 1. Harness rules (how you run)

These come from how Muse Code actually behaves. Follow them or the run will silently degrade.

**1.1 You are the Orchestrator.** Subagents are one level deep: a child cannot spawn children. So every agent in section 11 is spawned by you, never by another agent. Plans that assume recursive delegation will be flattened.

**1.2 Worktree isolation.** Fan-out only isolates children in git worktrees when isolation is on and the workspace is a git repo. In a non-git workspace the flag is ignored without any error and children share your files. Before the first spawn:
- Confirm `git rev-parse --is-inside-work-tree` succeeds and there is at least one commit.
- After the first write-capable spawn, confirm a worktree exists under `.muse/worktrees/`. If it does not, stop fanning out, write it to `BLOCKERS.md`, and work serially.
- Children start from your current `HEAD` in detached HEAD state. Commit before you spawn. Each child must end its work on a named branch (`agent/<role>/<task>`). You merge sequentially, one branch at a time, running the gate after each merge.

**1.3 Concurrency.** Subagent concurrency is roughly host cores minus two. Gradle builds are memory-hungry, so run at most 3 concurrent Gradle invocations regardless of that number. Use a shared Gradle user home and the build cache so worktrees do not each re-download everything.

**1.4 Files are the shared memory, not context.** Agents cannot rely on each other's context. All handoffs go through files in `docs/` and the repo. Council rounds, critiques, votes, review findings and bug reports are files.

**1.5 Agent lifetime and drift.** Long-lived agents accumulate irrelevant context and get worse without any visible failure. Recycle every agent at each phase boundary. Re-brief the new agent from its role prompt (Appendix A) plus the relevant files, not from a conversation summary.

**1.6 Never trust self-reports.** Subagents will sometimes claim success on unfinished work. Completion is decided only by the gate script's exit code and by diffs you read yourself.
- Create `scripts/gate.sh <phase>` in Phase 3. It exits non-zero on any failure and prints a one-line result per check.
- Device-dependent checks the environment cannot run print `SKIPPED(no device)`. They never print `PASS`.

**1.7 Steering.** Cancellation is cooperative and queued messages only apply on a child's next turn. To redirect a running child, send the message with the interrupt option. Do not assume a stop request has stopped anything until you see the child's status.

**1.8 Skills and commands.**
- `/taste` is an explicit-invocation checklist of AI-slop visual defaults. UI builders invoke it before designing each screen. The UI Reviewer invokes it on every screen. It does not fire on its own.
- Do not use `/plan` or `/grilling` as blocking steps. They stop for the human, and you do not stop for the human. Plan in `docs/PLAN.md` instead.
- `.agents/`, `.muse/` and `.git` are read-only to you, so you cannot create project skills. Role prompts live in `docs/agents/<role>.md` and you paste them into each spawn objective.

**1.9 Goals.** If a goal is pinned, treat its acceptance check as the oracle, which is `scripts/gate.sh final` exiting 0. Do not close the goal on your own judgment.

**1.10 Resumability.** Update `docs/PROGRESS.md` after every completed task: phase, what is done, what is in flight, which branches exist, what is next. On any restart, read `PROGRESS.md` first. The session log may let you resume. The file is what tells you where you are.

**1.11 Safety.** Work only inside the workspace. Never commit secrets. Keystore and passwords live outside the repo (see section 19). Do not run `rm -rf` on anything outside build outputs. Hooks and MCP servers run outside your sandbox, so do not add any.

---

## 2. Product definition

A premium Android music app for personal use that streams from YouTube Music through NewPipeExtractor, with a Material 3 Expressive interface and deep live customization. The aim is "the app I would pick over the big names", not "good enough".

### 2.1 Platform floor: Android 12 (minSdk 31)

- `minSdk = 31`. `compileSdk` and `targetSdk` are the latest stable SDK installed. Check the SDK manager, do not guess from memory.
- Test matrix: API 31, 32, 33, 34, 35 and the latest. API 32 (12L) matters for foldables and tablets.
- Raising the floor to 31 gives you, with no fallbacks needed: dynamic color (wallpaper-derived Material You), `RenderEffect` and `Modifier.blur`, and the platform splash screen API (use `core-splashscreen`). Use all of them deliberately. Blur is GPU-expensive, so budget it against the frame time (section 10).
- Android 12 to 14 behaviors you must handle explicitly. Do not leave these to chance:

| Area | Rule |
|---|---|
| Foreground service from background (API 31+) | Start the playback service only through paths Media3 supports (MediaSession/MediaController connect, user action). Test starting playback from a Bluetooth or headset button while the app is backgrounded. |
| Foreground service types (API 34+) | Declare `mediaPlayback` and its permission. Downloads need their own strategy: the Architect decides between WorkManager long-running foreground work and user-initiated data transfer, taking Android 14/15 type rules and timeouts into account. Record the decision. |
| Notifications (API 33+) | `POST_NOTIFICATIONS` runtime permission, with a designed permission-denied state. Playback must still work without it. |
| Audio library permission | `READ_MEDIA_AUDIO` on API 33+, `READ_EXTERNAL_STORAGE` with `maxSdkVersion="32"` on API 31-32. |
| Exact alarms (API 31+) | Do not use them. The sleep timer is an in-service timer. |
| Bluetooth (API 31+) | `BLUETOOTH_CONNECT` is a runtime permission. Only request it if a feature actually needs it. |
| Back navigation | Support predictive back (`enableOnBackInvokedCallback`) with proper back handlers. |

---

## 3. Stack

- Kotlin, Jetpack Compose, Material 3 Expressive (latest `material3` that includes the expressive APIs: `MaterialExpressiveTheme`, the expressive motion scheme, shape morphing, wavy progress indicators, expressive buttons and navigation components). Expressive APIs have changed between releases. Verify names against the pinned library's docs or source rather than memory. If you have web access, read the official Material 3 Expressive guidance first.
- Media3 (ExoPlayer, MediaSession, `MediaSessionService`) for playback.
- NewPipeExtractor for search, metadata and stream resolution, behind a `MusicSource` interface.
- Room, DataStore, Coroutines/Flow, Hilt (KSP), WorkManager, Coil, OkHttp.
- Navigation with type-safe routes, shared-element transitions between list, detail and player.
- Gradle Kotlin DSL, version catalog (`libs.versions.toml`), convention plugins in `build-logic`.
- Testing: JUnit, MockK, Turbine, `kotlinx-coroutines-test`, Robolectric for Compose tests on the JVM, a Compose screenshot tool (Roborazzi or the official Compose Preview Screenshot Testing; the Council picks one), Macrobenchmark, JankStats.
- Quality: ktlint, detekt, Android Lint, Kotlin `allWarningsAsErrors`, an architecture-rule test library (for example Konsist), LeakCanary and StrictMode in debug builds.

---

## 4. Architecture

Multi-module clean architecture. Suggested modules (the Architect may refine and must record why):

```
:app                      wiring, navigation graph, Application, DI root
:build-logic              convention plugins
:core:model               pure Kotlin models
:core:common              utilities, dispatchers, result types
:core:domain              use cases, repository interfaces (no Android deps)
:core:data                repository implementations, mappers, sync
:core:database            Room
:core:datastore           settings, theme, preferences
:core:extractor           MusicSource interface + NewPipeMusicSource + FakeMusicSource
:core:player              Media3 service, queue, effects, resolver
:core:designsystem        theme, shapes, motion, typography, expressive components
:core:ui                  shared composables, previews, state holders
:core:testing             fakes, test rules, fixtures
:feature:home :feature:search :feature:library :feature:player
:feature:artist :feature:album :feature:playlist :feature:downloads
:feature:settings :feature:onboarding
:benchmark                Macrobenchmark
:baselineprofile          Baseline/Startup profile generator
```

Rules, enforced by architecture tests that fail the build:
- Unidirectional data flow. Immutable UI state. Events up, state down.
- Composables hold no business logic and never touch the data layer.
- `:feature:*` modules never depend on each other. They talk through `:core:*` and navigation contracts.
- `:core:domain` has no Android dependencies.
- No god classes. No file over roughly 400 lines without a reason in DECISIONS.
- KDoc on all public APIs. Consistent naming. Package-by-feature inside modules.

---

## 5. Music source: NewPipeExtractor

- Define `MusicSource` in `:core:extractor`. Provide two implementations: `NewPipeMusicSource` and `FakeMusicSource` (deterministic fixtures). All feature and unit tests run against the fake. Contract tests run against both, and the live one is opt-in (`-PliveTests`).
- Add the extractor from JitPack at a pinned release tag. Check the latest release, do not guess. Implement the extractor's `Downloader` on OkHttp and initialize it with a sensible localization and content country. Make both configurable in Settings.
- Use the extractor's YouTube Music search filters (songs, albums, artists, playlists, videos) and search suggestions. Verify what the extractor exposes for home or trending content. Where it exposes nothing, build the home screen from local signals (history, most played, likes, related streams of recent tracks, trending kiosk if available) and say so in DECISIONS.
- Stream URLs expire after a short time. Never persist them. Persist stable IDs only. Resolve at play time (Media3 `ResolvingDataSource` or equivalent), cache the resolved URL in memory with an expiry, and re-resolve on 403/410 and on expiry.
- Reliability, which the Performance/Reliability Engineer owns:
  - Typed error model: network, rate-limited, geo-blocked, age-restricted, removed, extractor-broken, unknown. Each has a designed UI state and a retry policy (exponential backoff with jitter, capped).
  - Extractor breakage is expected. Log the extractor version in About. Put "bump the extractor and rebuild" at the top of the troubleshooting section in README.
  - Optional: the Council may add a second `MusicSource` (an alternative backend) as a fallback behind the same interface.
- R8 commonly breaks reflection-heavy extractor dependencies (JS engine, HTML parser, protobuf). Ship keep rules and run a release-variant smoke test that plays a real track (a manual step if the environment has no network or device).

---

## 6. Playback

- `MediaSessionService` with notification and lock-screen controls, Bluetooth and headset buttons, audio focus (ducking, pause on call, resume rules), becoming-noisy handling, gapless playback.
- The queue is persisted in Room and restored after process death. Position is persisted periodically and on pause.
- Streamed audio cache with a size cap setting (SimpleCache or equivalent).
- Equalizer, bass boost and loudness effects attached to the player's audio session, with presets and per-device persistence.
- Crossfade is not native to ExoPlayer. The Architect decides the approach (dual-player or volume ramps) and records the tradeoffs. It must not break gapless playback when crossfade is off.
- Sleep timer (in-service, with "end of track" mode), playback speed, skip silence, repeat and shuffle that behave correctly with the queue.
- Optional: `MediaLibraryService` for Bluetooth/car/Wear browsing. The Council decides.
- Position ticks must not recompose the whole player. See section 10.

---

## 7. Features (starting point; the Council may add or cut)

Home, search with filters and suggestions, artist/album/playlist pages, full player with queue editing (reorder, swipe actions), mini player, background playback, offline downloads with quality choice, local library (MediaStore audio) merged with streamed library, user playlists, likes, history, listening stats, sleep timer, equalizer, gapless/crossfade, synced lyrics (the Council evaluates an open lyrics source and verifies it is available before committing), backup/restore (settings, library, playlists), playlist import (a YouTube playlist URL, and a NewPipe backup if the format is verifiable), share sheet, widget (optional), Android Auto (optional).

The Council must produce a ranked list: v1 (ships), v1.1 (planned), cut (with reasons). Ship a working core first (search, play, queue, background playback, library) before building breadth.

---

## 8. Customization (deep, live, everywhere)

Every setting applies instantly with no restart, flows from DataStore through a single `StateFlow` into the theme and the affected screens, and survives process death. Each has a designed default and a "reset" option.

- Color: dynamic (wallpaper), custom seed color picker, palette style (several tonal-spot, vibrant, expressive, neutral, monochrome-type variants), contrast level (standard, medium, high), album-art-derived player theming (extract on a background dispatcher, cache per track, animate the transition), AMOLED pure black, light/dark/system, optional per-surface tint strength.
- Shape: shape style presets (sharp, soft, rounded, cut-corner, full-expressive with morphing), applied through the shape scheme so every component follows.
- Typography: at least 3 bundled font families (open licenses, recorded in a LICENSES file), size scale, weight bias.
- Layout: density (compact, comfortable, spacious), reorderable and hideable home sections, at least 4 full-player layouts (for example art-forward, vinyl/disc, lyrics-forward, minimal), at least 3 mini-player styles, navigation style (bottom bar, rail, adaptive), library grid vs list, artwork corner radius and size.
- Motion: animation intensity (off, reduced, standard, expressive), always respecting the system animation scale. Haptics on/off with intensity.
- Display: refresh-rate mode (see section 10), keep-screen-on in player, always-dark player.
- Audio: stream and download quality, normalization, crossfade, gapless, skip silence, equalizer.
- Data: cache limit, download location, backup/restore, language and region for content.

Settings screen itself must be designed to the same bar as the player: grouped, searchable, with live previews for theme options.

---

## 9. Design: Material 3 Expressive, and not AI slop

### 9.1 Design language
Phase 2 produces `DESIGN_LANGUAGE.md`: the app's own identity, written before any screen is built. It defines a signature motion character (springs and their stiffness/damping tiers), a custom shape vocabulary, iconography rules (one icon family, consistent weight, no emoji), a spacing scale with intent (not "16dp everywhere"), a type hierarchy, and what makes the player screen recognizably this app. If the result could be mistaken for a reference app's clone or for a generic template, it fails review.

### 9.2 Anti-slop rules (the reviewer enforces all of these)
Reject any screen that shows:
- default Material blue/purple seed with no intent, or one gradient reused everywhere,
- a grid of identical rounded cards as the whole layout,
- emoji or text glyphs standing in for icons,
- lorem ipsum, "Welcome back!", placeholder art, hardcoded sample strings in production UI,
- uniform padding and equal visual weight (no hierarchy), shadows on everything,
- loading states that do not match the final layout, or screens missing loading/empty/error states,
- motion that is only fades and linear tweens, with no springs, no shape morphing, no shared-element continuity,
- text over images without a contrast guarantee.

Required instead: spring-based motion, shape morphing where it carries meaning, real hierarchy, album-art-driven color, and designed loading (shape-matched skeletons or expressive progress), empty, offline and error states on every screen.

### 9.3 Quality dimensions (each reviewed per screen)
Accessibility (contrast 4.5:1 text and 3:1 UI, 48dp touch targets, TalkBack order and labels, font scale up to 200%, reduce-motion), RTL mirroring, adaptive layouts (compact, medium, expanded; foldable posture; landscape), edge-to-edge with correct insets, dark/light/AMOLED, dynamic color extremes (very light and very dark wallpapers).

---

## 10. Performance: 120 Hz

### 10.1 What "120 Hz" means on Android
An app can request a refresh rate. The system decides. Battery saver, thermal state, OEM per-app settings and adaptive (LTPO) display logic can all hold the display lower, so "locked 120" is a request plus a frame-time guarantee, never a promise. So the targets are frame times, and the display request is a best effort.

### 10.2 The request
- Enumerate the display's supported modes (`Display.getSupportedModes()`), pick the highest refresh rate at the current resolution, and request it via `WindowManager.LayoutParams.preferredDisplayModeId`. This works across your whole supported range (API 31+).
- Re-apply on display changes (`DisplayManager.DisplayListener`: fold/unfold, external display, mode switch).
- On API 35+, also check the frame-rate hint APIs (for example `View.setRequestedFrameRate`) behind SDK checks, and check whether the pinned Compose version exposes frame-rate hints. Use them where they help, but they must not be required.
- Settings: Refresh rate = Auto (system-managed), Max (request highest), Battery (60). Default Auto-biased-to-max.
- Log the requested and the actual refresh rate (`display.mode.refreshRate`) and expose both in a debug overlay.

### 10.3 Frame-time targets (set on a physical 120 Hz device)
Frame budget: 8.33 ms at 120 Hz, 16.67 ms at 60 Hz.

| Scenario | Target |
|---|---|
| Home, search results and a 5,000-track library scroll | frame CPU duration P50 <= 4 ms, P90 <= 6 ms, P99 <= 8.3 ms |
| Queue drag-reorder | same |
| Mini player to full player transition (shared element + morph) | same, no dropped frame in the transition window |
| Seekbar and wavy progress animation during playback | no recomposition of the surrounding player on position ticks |
| Live theme change (seed color, shape, AMOLED toggle) | applies within 2 frames of work per frame; no full-screen recompose storm |
| Cold start | time to initial display in line with the best reference apps; justify the number you set |

These are targets. If a target is physically unreachable on the test device, record the measured value honestly and the reason in `PERFORMANCE_REPORT.md`. Do not move the target silently.

### 10.4 Tooling and technique
Baseline Profiles and Startup Profiles, Macrobenchmark with `FrameTimingMetric`, JankStats in debug builds, Compose compiler reports for stability and skippability, R8 full mode, Perfetto traces for anything that fails.
- Read state late: lambda-based modifiers (`graphicsLayer { }`, `offset { }`, `drawBehind`) and state reads in the draw phase rather than composition for anything that animates every frame.
- `LazyList` items always have stable keys and `contentType`. No heavy work in item composition.
- Position and progress come from a time source that does not trigger recomposition of siblings. Isolate the ticking composable.
- Stable or `@Immutable` UI models. Use a Compose stability configuration file for external types.
- Image work off the main thread. Request correctly sized bitmaps. Palette/color extraction on a background dispatcher, cached.
- Budget `blur`, large `RenderEffect` areas, and overdraw. Prefer scrims and tinted surfaces where a blur is not worth its cost.
- No work on the main thread that blocks input or frames: no disk, no JSON, no extractor calls.
- Measure battery and memory during 30+ minutes of background playback. No wake loops, no leaks (LeakCanary clean).

### 10.5 What the agent cannot verify
You probably have no physical 120 Hz phone attached. In that case:
1. Build everything above, including `:benchmark` and `scripts/bench-120hz.sh`.
2. In `docs/UNVERIFIED.md`, mark the 120 Hz frame-time results `UNVERIFIED(no device)` and give the human the exact commands to run.
3. Emulator timings are not representative of real frame times. Do not report them as 120 Hz evidence.

---

## 11. Agent roster

Spawned by you (the Orchestrator), one level deep. Recycle at phase boundaries.

| # | Agent | Mode | Owns | Output |
|---|---|---|---|---|
| 1 | Product Strategist | read-only | scope, priorities | council files |
| 2 | UX Lead | read-only | flows, information architecture | council files |
| 3 | Android Architect | read-only (writes architecture docs) | modules, boundaries, tech choices | ARCHITECTURE.md |
| 4 | Power-User Skeptic | read-only | challenges every proposal | council files |
| 5 | Performance/Reliability Engineer | read-only (+ perf code) | frame budget, failure modes | PERFORMANCE_REPORT.md |
| 6-8 | UI Research (x3, split the apps) | read-only, web | reference study | UI_REFERENCES.md |
| 9 | UI Builder(s) | worktree | screens, design system | code |
| 10 | UI Reviewer | read-only | audits every screen | `docs/reviews/ui/*.md` |
| 11 | Feature Builder(s) | worktree, one module each | data -> domain -> UI slices | code + tests |
| 12-14 | Feature Test agents (playback, offline/resilience, UI/accessibility) | worktree | tests, bug reports | TEST_REPORT.md |
| 15 | Code Quality | read-only (+ fix PRs) | lint, architecture, hygiene | `docs/reviews/code/*.md` |
| 16 | Performance | worktree | profiles, benchmarks, jank fixes | PERFORMANCE_REPORT.md |

Builders touch only their module. Shared files (`settings.gradle.kts`, `libs.versions.toml`, design system tokens) are owned by one agent per phase, assigned by you, to prevent merge conflicts.

Severity scale for all findings: P0 crash, data loss or broken playback. P1 major UX or performance failure. P2 polish defect. P3 nit.

---

## 12. Council protocol

Members: Product Strategist, UX Lead, Android Architect, Power-User Skeptic, Performance/Reliability Engineer. Each has a distinct mandate and must argue from it, not agree for comfort.

Mechanics (file-based, orchestrator-mediated):
1. **Round 1, independent.** Each member writes `docs/council/<topic>/r1-<role>.md` without reading the others: proposals (add / cut / defer), reasoning, risks.
2. **Round 2, cross-examination.** Each member reads all Round 1 files and writes `r2-<role>.md`: at least 2 specific objections to others' proposals, at least 1 idea they would adopt, and a list of assumptions they think are wrong.
3. **Round 3, revision and vote.** Members revise and cast votes in `r3-<role>.md`. You tally from the files, not from anyone's summary. 3 of 5 passes.
4. **Blocker.** Any member can raise a written blocker (on frame budget, reliability, or scope-creep). Overriding a blocker needs 4 of 5 and a written rebuttal.
5. Record each outcome as an ADR in `DECISIONS.md`: context, options, decision, votes, dissent, revisit trigger.

Reconvene: before architecture is frozen, after the first vertical slice, after hardening, and before release. No rubber-stamping. A round where nobody objected to anything counts as a failed round and is rerun with the Skeptic instructed to attack harder.

---

## 13. UI reference research

Before any UI is built, the three Research agents split and study 10-15 real music apps, for example Spotify, YouTube Music, Apple Music, Tidal, Deezer, Symfonium, Namida, Metrolist, OuterTune, Poweramp, Retro Music, Gramophone, Oto Music, and Google's own Material 3 Expressive apps.

- You can only study what you can reach. Use official design write-ups, store listings and screenshots, and reputable teardowns and reviews. For each app, write down the source. If you could not see something, write "no data". Do not invent observations.
- Per app, record: layout and navigation model, player design, mini-player behavior, motion and gestures, color strategy, empty/loading/error states, information density, one thing that works, one thing that does not.
- Synthesize into principles, then into `DESIGN_LANGUAGE.md`. Extract principles. Do not clone.
- Include a "differentiation" section: what this app does that none of them do, and why that is better.

---

## 14. UI build and review loop

For each screen:
1. Builder invokes `/taste`, reads `DESIGN_LANGUAGE.md`, builds in a worktree, with previews for: light, dark, AMOLED, font scale 1.0 and 2.0, compact and expanded widths, RTL, and each state (loading, empty, error, offline, content).
2. Builder generates screenshots through the screenshot-test tool and commits them.
3. UI Reviewer views the screenshots, runs `/taste`, checks against the anti-slop rules (9.2), M3 Expressive guidance, the reference findings and the quality dimensions (9.3). Writes `docs/reviews/ui/<screen>.md` with P0-P3 findings.
4. Builder fixes. Reviewer re-reviews. A screen merges only with zero open P0-P2.
5. Screenshot tests do not capture real motion or blur fidelity. Motion is reviewed from the animation specs in code, and any motion that needs a device is added to `docs/MANUAL_QA.md`.

---

## 15. Testing

Tiers (the gate states which tiers ran):

| Tier | What | Where it runs |
|---|---|---|
| T1 | Unit tests: use cases, repositories, view models, queue logic, resolver, error mapping | JVM, always required |
| T2 | Compose UI tests and screenshot tests via Robolectric | JVM, always required |
| T3 | Instrumented tests and Macrobenchmark | emulator or device, when available |
| T4 | Manual device checklist (`docs/MANUAL_QA.md`): Bluetooth, calls and audio focus, real-network extraction, 120 Hz, battery | the human |

Required coverage, end to end:
- Playback: play/pause/seek/skip, queue edit while playing, repeat/shuffle, gapless, crossfade, speed, sleep timer, audio focus loss and gain, becoming noisy, headset buttons.
- Resilience: expired stream URL (403/410 -> re-resolve), 429, timeouts, geo-block, removed track, extractor exception, network loss mid-track and recovery, airplane mode, process death with queue restore, low storage.
- Offline: download, resume after interruption, delete, offline-only mode, offline playback with no network.
- Library: playlists, likes, history, import/export round trip, migration tests for every Room schema version.
- Settings: every setting applies live; theme changes under rotation and process death.
- Accessibility: TalkBack labels and order, touch targets, font scale.
- Android version matrix: behaviors in 2.1 on API 31, 33, 34 and latest.

Test agents file bugs as `docs/bugs/BUG-###.md` (steps, expected, actual, severity, owner). The owner fixes it, the tester re-tests, and `TEST_REPORT.md` is regenerated from facts: counts, pass/fail, what was skipped and why.

---

## 16. Code quality gates

All run in `scripts/gate.sh`:
- ktlint and detekt at zero issues. No baseline files.
- Android Lint with `warningsAsErrors = true` and `abortOnError = true`.
- Kotlin `allWarningsAsErrors = true`.
- Architecture tests (section 4 rules).
- Compose compiler reports reviewed for unstable parameters on hot paths.
- Dependency hygiene: no unused dependencies, no duplicate versions, all versions in the catalog.
- Leak and misuse review: coroutine scope ownership, `GlobalScope` forbidden, no blocking calls on Main, lifecycle-aware collection, no context leaks.
- Zero `TODO`/`FIXME` in release, no hardcoded strings in UI, no dead code.

The Code Quality agent reviews every merge candidate branch against `git diff` and writes findings to `docs/reviews/code/`. It rejects branches that do not meet the bar.

---

## 17. Phases and gates

Nothing advances until its gate passes.

| # | Phase | Output | Gate (`scripts/gate.sh <name>`) |
|---|---|---|---|
| 0 | Environment check | JDK, Android SDK, Gradle, git, network reachability, device availability; `BLOCKERS.md` if needed | env check prints facts |
| 1 | Research | UI_REFERENCES.md, extractor capability notes | docs exist, sources cited |
| 2 | Council: scope | DECISIONS.md ADRs, ranked feature list, app name | votes recorded |
| 3 | Architecture + design system + scaffold | modules, convention plugins, theme, `scripts/gate.sh`, AGENTS.md (a directory page, ~100 lines, pointing to docs and listing verification commands) | `scaffold`: builds, lint/ktlint/detekt clean |
| 4 | Vertical slices | per slice: data -> domain -> UI -> tests; one slice at a time through the loop | `slice`: T1+T2 green, UI review clean, code review clean |
| 5 | Review loops | all agents, all screens | no open P0-P2 |
| 6 | Hardening | edge cases, process death, resilience, accessibility, RTL, adaptive, Android-version matrix | resilience and a11y tests green |
| 7 | Performance | profiles, benchmarks, jank fixes, 120 Hz request | `perf`: benchmarks run, or `UNVERIFIED(no device)` documented |
| 8 | Final audit | one more full pass hunting anything average; Council reconvenes | zero P0-P2 |
| 9 | Release | signed APK, README, setup | `final` |

Slice order: (1) search + play one track, (2) queue + background playback + notification, (3) library + playlists + likes, (4) player polish + theming engine, (5) downloads + offline, (6) settings + customization, (7) remaining features by ranked list. Reconvene the Council after slice 2.

---

## 18. Documents

All required, all accurate to the final code: `README.md`, `ARCHITECTURE.md`, `DECISIONS.md`, `UI_REFERENCES.md`, `DESIGN_LANGUAGE.md`, `TEST_REPORT.md`, `PERFORMANCE_REPORT.md`, `MANUAL_QA.md`, `UNVERIFIED.md`, `BLOCKERS.md` (if any), `PROGRESS.md`, `CHANGELOG.md`, `LICENSES.md` (fonts and dependencies), plus `AGENTS.md` (short, a map to the rest).

---

## 19. Release

- Release build: R8 full mode, resource shrinking, ABI splits (arm64-v8a plus a universal APK), baseline profile packaged.
- Signing: generate a release keystore with `keytool`. Store it and its passwords outside the repo (for example in `~/.android-keys/` with credentials in `~/.gradle/gradle.properties`). Commit only the Gradle config that reads them. Print the keystore location and a backup warning in README. If signing material cannot be created, fall back to the debug key, say so loudly in README, and record it in `UNVERIFIED.md`.
- Smoke test the release variant: installs, launches, searches, plays a track, notification controls work, background playback survives screen off. If the environment cannot run it, add it to `MANUAL_QA.md`.
- README includes: install via `adb install` and via file manager (Android 12 "install unknown apps"), first-run permissions, updating the extractor when playback breaks, backup/restore, known limitations.

---

## 20. Definition of done

- Clean build, all T1 and T2 tests pass, `ktlint`, `detekt`, Lint and compiler warnings at zero.
- Every screen has passed UI review with zero open P0-P2, including all states, accessibility, RTL and adaptive layouts.
- Every setting applies live and persists.
- Frame-time benchmarks meet the section 10 targets on a physical device, or are explicitly marked `UNVERIFIED(no device)` with the commands for the human to run.
- Every agent has signed off in writing (a sign-off file per agent, listing what they checked and what they did not).
- The Council has reconvened and signed off the final scope.
- A signed release APK exists, with setup instructions.
- `scripts/gate.sh final` exits 0.

---

## 21. Stop rule

Keep improving until two consecutive full review rounds (UI Reviewer, Code Quality, Performance, Test agents, and a Council pass) produce zero P0, P1 and P2 findings. Then stop. Do not invent work after that, and do not keep polishing indefinitely. When you stop, write a final summary in `PROGRESS.md`: what shipped, what was cut and why, what is `UNVERIFIED`, and what the human should check first.

---

# Appendix A: Role prompts (paste into spawn objectives)

Each one assumes the global rules in sections 0-1 and the relevant docs. Add the specific task each time.

**Product Strategist.** You decide what is in the app and what is out. Argue for the smallest feature set that makes the app better than the big names for one specific kind of listener. Propose, justify, and defend against the Skeptic. In Round 2, find the weakest assumption in each other proposal. Output only to your council files.

**UX Lead.** You own flows and information architecture: how a person gets from "open app" to "music playing" in the fewest, most delightful steps, one-handed. Propose navigation, player gestures, queue interaction, empty states. Challenge any feature that adds a tap without adding value.

**Android Architect.** You own module boundaries and technical choices. For every proposal from others, state the cost in complexity and the failure modes. Decide DI, navigation, downloads strategy (respecting Android 14/15 foreground service rules), crossfade approach, and the structure of `:core:extractor`. Record every choice with alternatives.

**Power-User Skeptic.** You assume every proposal is wrong until shown otherwise. Attack scope creep, hidden complexity, settings that nobody needs, and anything that sounds impressive but would feel average in daily use. Also defend the features power users cannot live without (queue control, gapless, EQ, offline, import). Your Round 2 must contain real objections or it is rerun.

**Performance/Reliability Engineer.** You own the frame budget (8.33 ms) and the failure model. Veto candidates: any feature or visual effect that cannot meet the budget, and any flow with no designed failure path. You can raise a blocker. Back claims with measurements or label them as estimates.

**UI Research agent.** You study your assigned apps from reachable sources only. Cite every source. Write "no data" for anything you could not see. Output: your section of `UI_REFERENCES.md` with the per-app template from section 13, plus 5 principles and 3 anti-patterns you saw.

**UI Builder.** You build assigned screens in your worktree on branch `agent/ui/<screen>`. Invoke `/taste` first. Read `DESIGN_LANGUAGE.md`. Implement every state, previews for the section 14 matrix, and screenshots. No logic in composables. Do not touch files outside your module. End with a named branch and a short note of what you did not do.

**UI Reviewer.** You are not the builder's friend. Audit screenshots and code against the anti-slop rules, M3 Expressive guidance, the reference findings, and accessibility. Run `/taste`. Write findings with P0-P3 severity and a concrete fix. Re-review after fixes. Never pass a screen with a missing state.

**Feature Builder.** You build one vertical slice in your module and worktree on branch `agent/<module>/<slice>`: data, domain, state holder, UI hookup, and tests together. Use `FakeMusicSource`. Run the gate for your module before you hand off. Report only what you verified.

**Feature Test agent.** Your job is to break the app. Write tests for your area from section 15, including failure injection. File bugs as `docs/bugs/BUG-###.md`. Re-test every fix. Report skipped tests and why. Never mark a test as passing that you did not run.

**Code Quality agent.** You review diffs for architecture violations, naming, duplication, coroutine misuse, leaks, unstable Compose parameters, dependency hygiene. You may fix small issues on a branch. You reject branches that fail the bar. You do not accept suppressions without a DECISIONS entry.

**Performance agent.** You own 120 Hz. Implement the display-mode request, JankStats, Macrobenchmarks, Baseline/Startup profiles, and `scripts/bench-120hz.sh`. Profile before you change anything. Report measurements with the device used. If there is no physical device, say so and mark the results `UNVERIFIED(no device)`.

---

# Appendix B: Operator notes (for the human, not for Muse)

These come from Muse Code's documented behavior (beta, August 2026). Flags and defaults may have changed, so check the current docs before a long run.

1. **Prerequisites.** Muse Code installs on macOS and Linux. You need git, a JDK matching your Android Gradle Plugin, and the Android SDK (cmdline-tools, a recent platform, build-tools). For a multi-day unattended run, use a disposable container or VM.
2. **Make it a git repo first.** `git init`, make an initial commit, and add `.muse/worktrees/` to `.gitignore`. Worktree isolation is opt-in and silently does nothing outside a git repo.
3. **Start it.** In the repo, run `muse` with subagent worktree isolation enabled (`--subagent-worktree-isolation`). Trusting the workspace loads its `AGENTS.md` and rules, which is fine for your own repo. Use `/plan` once yourself to approve the plan, then pin a goal whose acceptance check is that `scripts/gate.sh final` exits 0 and the release APK exists. A vague finish line lets the agent declare victory early.
4. **Approvals will stall an unattended run.** The default mode pauses on things like `rm -rf`. Either add "always allow in this workspace" rules for `./gradlew` and your common commands, or, only inside a disposable container with no credentials, turn approvals off. Never use `--yolo` on a machine that holds real credentials.
5. **Network.** The default network mode stops for review on each new host. Gradle and the extractor need: `dl.google.com`, `maven.google.com`, `repo.maven.apache.org`, `plugins.gradle.org`, `services.gradle.org`, `jitpack.io`, `github.com`, `raw.githubusercontent.com`. Live extractor tests also need YouTube reachable. Without that, Muse can only test against the fake source.
6. **Observers cost tokens.** Memory, skill and goal observers are on by default and make their own model calls. The verification observer is off by default, so consider enabling it if your build exposes the setting. Persistent subagents hold context, so a 2-day run with many agents is expensive.
7. **Device.** If you can give it a physical 120 Hz phone over `adb` (Wi-Fi debugging), the 120 Hz and Macrobenchmark results become real. If not, run `scripts/bench-120hz.sh` and the `docs/MANUAL_QA.md` checklist yourself when it finishes.
8. **Do not trust the exit code.** `muse exec` exit codes only say the run completed, not that the code is correct. Gate on `scripts/gate.sh`.
9. **Check in.** Read `docs/PROGRESS.md` and `docs/UNVERIFIED.md` rather than the chat. `muse export` gives you an auditable log of the run.
10. **Cheap contributor tier.** Its terms say usage may be used to improve Meta's products. Fine for a personal app, but read the current terms.
