# Phase 2 Plan: Council Scope, Architecture Decisions, and Design Language

## Objective
Convene the Council (Product Strategist, UX Lead, Android Architect, Power-User Skeptic, Performance/Reliability Engineer) to establish application decisions via a 3-round protocol. Produce `docs/DECISIONS.md`, `docs/RANKED_FEATURES.md`, and `docs/DESIGN_LANGUAGE.md`.

## Tasks & Owners
| Task | Description | Owner | Acceptance Check |
|---|---|---|---|
| T2.1 | Council Round 1: Independent Proposals | All 5 Council Members | `docs/council/scope/r1-*.md` written independently |
| T2.2 | Council Round 2: Cross-Examination | All 5 Council Members | `docs/council/scope/r2-*.md` with >=2 objections & >=1 adopted idea |
| T2.3 | Council Round 3: Revisions & Voting | All 5 Council Members | `docs/council/scope/r3-*.md` with explicit votes |
| T2.4 | Synthesis into ADRs & Ranked Features | Lead / Architect | `docs/DECISIONS.md` & `docs/RANKED_FEATURES.md` |
| T2.5 | Design Language Specification | UX Lead / UI Builder | `docs/DESIGN_LANGUAGE.md` defining springs, shapes, typography, iconography |
| T2.6 | Gate Check | Lead | `./scripts/gate.sh council` passes |

## Acceptance Criteria
- 5 Round 1 files, 5 Round 2 files, 5 Round 3 files recorded.
- `docs/DECISIONS.md` contains clear ADRs (Context, Decision, Consequences, Votes).
- `docs/RANKED_FEATURES.md` classifies v1 (ship), v1.1 (planned), and cut features.
- `docs/DESIGN_LANGUAGE.md` defines concrete tokens (spring stiffness/damping, shape corner scales, typography).
- `scripts/gate.sh council` passes.
