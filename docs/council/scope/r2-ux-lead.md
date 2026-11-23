# Council Round 2: UX Lead Cross-Examination

## Objections to Other Proposals
1. **To Power-User Skeptic on Backup/Restore in v1**: Raw JSON export/import screens add complex file picker dialogues. It is critical for peace of mind, but must be designed as a clean, simple one-tap card in Settings, not an arcane developer menu.
2. **To Product Strategist on deferring Synced Lyrics**: Lyrics are the single most engaging feature on modern mobile music players. Cutting lyrics entirely leaves NexusTune feeling like an old MP3 player. Propose: Include LRCLIB open-source synced lyrics as an optional toggle; if unavailable or network fails, gracefully collapse the lyrics panel.

## Adopted Ideas
1. **From Performance Engineer**: Isolating the progress bar composable so the rest of the player does not recompose on position ticks.
2. **From Power-User Skeptic**: Retaining full queue drag-and-drop reordering with tactile haptic feedback.

## Flawed Assumptions Challenged
- Assumption that 4 player layouts means 4 completely disconnected codebases. They must share a single `PlayerUiState` and control contract, merely altering the composable layout topology.
