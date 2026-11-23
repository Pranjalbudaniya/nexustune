# Phase 1 Plan: Research (UI References & Extractor Capabilities)

## Objective
Perform systematic research on UI references and NewPipeExtractor capabilities to inform the design system, architecture, and Council decisions.

## Tasks & Owners
| Task | Description | Owner | Acceptance Check |
|---|---|---|---|
| T1.1 | UI Reference Study across leading music apps | UI Research Team | `docs/UI_REFERENCES.md` with 12+ apps, design principles, anti-patterns, and differentiation |
| T1.2 | NewPipeExtractor capabilities & dependency audit | Android Architect | `docs/EXTRACTOR_CAPABILITIES.md` with pinned tag, API surface (search, stream, trending), error models, and R8 rules |
| T1.3 | Quality Gate Check | Lead | `./scripts/gate.sh research` passes |

## Acceptance Criteria
- `docs/UI_REFERENCES.md` exists with cited sources, layout models, mini-player behaviors, motion, and anti-slop principles.
- `docs/EXTRACTOR_CAPABILITIES.md` exists with concrete OkHttp Downloader structure, stream URL expiry rules, and typed errors.
- `scripts/gate.sh research` exits 0.

