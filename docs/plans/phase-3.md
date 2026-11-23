# Phase 3 Plan: Architecture Scaffold, Design System, and Quality Gate

## Objective
Establish the multi-module project scaffold, Gradle Kotlin DSL configuration, version catalog, design system tokens, initial core modules, and comprehensive `scripts/gate.sh scaffold`.

## Tasks & Owners
| Task | Description | Owner | Acceptance Check |
|---|---|---|---|
| T3.1 | Version Catalog (`gradle/libs.versions.toml`) | Android Architect | Complete catalog with pinned stable dependencies |
| T3.2 | Multi-module settings & root build script | Android Architect | `settings.gradle.kts` registers all modules |
| T3.3 | Architecture Specification (`docs/ARCHITECTURE.md`) | Android Architect | Full module graph, dependency rules, and data flow documented |
| T3.4 | Directory Map (`AGENTS.md`) | Lead | Short ~100-line directory map with verification commands |
| T3.5 | Core Modules Scaffold (`:core:*` & `:feature:*`) | Android Architect & UI Builder | Compiling module skeletons with clean boundaries |
| T3.6 | Design System Tokens & M3 Expressive Theme | UI Builder | Color schemes, shape schemes, typography, spring tokens |
| T3.7 | Gate Script Update & Execution | Performance / Lead | `./scripts/gate.sh scaffold` passes |

## Acceptance Criteria
- Project compiles cleanly via `./gradlew --no-daemon assembleDebug testDebugUnitTest`.
- `AGENTS.md` exists and satisfies requirements.
- `docs/ARCHITECTURE.md` accurately documents the system.
- `./scripts/gate.sh scaffold` exits 0.

