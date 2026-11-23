# Phase 5 Plan: Review Loops (UI Review & Code Quality)

## 1. Overview & Objectives
Phase 5 conducts exhaustive, independent quality audits across all 5 primary user-facing screens and all code modules in NexusTune.
Audits must enforce:
- Section 9.2 Anti-Slop UI Rules (no generic cards, no linear animations, no unconstrained blur, no missing empty/loading states, 48dp touch targets, RTL support, WCAG AA contrast).
- Section 14 Code Quality Standards (unidirectional data flow, clean architecture layer isolation, structured coroutine concurrency with supervisor scopes, zero memory leaks in player/services, proper state hoisting).

## 2. Review Tasks & Ownership
1. **UI Review Audit (Lead Designer & Accessibility Specialist)**:
   - Audit `HomeScreen`: Content hierarchy, recommendation carousel, quick picks, empty states. (`docs/reviews/ui/home.md`)
   - Audit `SearchScreen`: Search bar interaction, filter chips, debounce, suggestion list, history. (`docs/reviews/ui/search.md`)
   - Audit `LibraryScreen`: Liked songs, custom playlists, download status badges, sorting. (`docs/reviews/ui/library.md`)
   - Audit `PlayerScreen`: 4 layouts (Default, Vinyl, Minimalist, Visualizer), 3 mini-player variants, scrubbing smoothness, scrubber touch target. (`docs/reviews/ui/player.md`)
   - Audit `SettingsScreen`: Theming controls, shape presets, AMOLED switch, refresh rate toggle, equalizer DSP, backup/restore. (`docs/reviews/ui/settings.md`)
2. **Code Quality Audit (Staff Architect & Android Specialist)**:
   - Audit concurrency & coroutine scopes (ViewModelScope, ApplicationScope, Dispatchers.IO for I/O).
   - Audit Clean Architecture boundaries (Domain depends only on Model/Entities; Data implements Domain interfaces; Presentation depends on Domain).
   - Audit memory leak prevention (ExoPlayer release, BroadcastReceiver unregistration, MediaSession lifecycle).
   - Produce `docs/reviews/code/audit.md`.
3. **Remediation**:
   - Triage all findings (P0 blocker, P1 critical, P2 major, P3 minor).
   - Resolve 100% of P0, P1, and P2 findings immediately with unit test validation.

## 3. Acceptance Checks
- `docs/reviews/ui/home.md` present and scored.
- `docs/reviews/ui/search.md` present and scored.
- `docs/reviews/ui/library.md` present and scored.
- `docs/reviews/ui/player.md` present and scored.
- `docs/reviews/ui/settings.md` present and scored.
- `docs/reviews/code/audit.md` present with zero unresolved P0-P2 findings.
- `./scripts/gate.sh slice` passes without regressions.
