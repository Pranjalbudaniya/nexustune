# Phase Improvement Loop 44: Listening Milestones, Daily Streaks & Time-Capsule Recap Engine (v5.4.0)

## Overview
NexusTune features a private, offline-first listening history stored in Room.
Loop 44 builds on this foundation by introducing the **Listening Milestones, Daily Streaks & Time-Capsule Recap Engine**, allowing users to celebrate their music journey without external tracking, accounts, or telemetry.

## Deliverables

### 1. `:core:model`
- `ListeningStreak`:
  - `currentStreakDays: Int`
  - `longestStreakDays: Int`
  - `lastActiveDateEpochDay: Long`
- `MilestoneType`:
  - `FIRST_BEAT`: Listened to first track
  - `NIGHT_OWL`: 50+ tracks played between 11 PM and 5 AM
  - `EARLY_BIRD`: 50+ tracks played between 5 AM and 9 AM
  - `EXPLORER`: Listened to 50+ unique artists
  - `CENTURY_CLUB`: 100+ plays of a single favorite track
  - `MARATHONER`: 2+ hours listening in a single day
  - `HI_RES_AUDIOPHILE`: 10+ hours of Hi-Res output or lossless listening
- `ListeningMilestone`:
  - `type: MilestoneType`
  - `title: String`
  - `description: String`
  - `isUnlocked: Boolean`
  - `unlockedTimestamp: Long?`
  - `progressPercent: Float`

### 2. `:core:domain:analytics`
- `MilestoneCalculator`:
  - Analyzes Room playback history records, durations, timestamps, and audio output metadata.
  - Computes streaks (consecutive calendar days with >= 1 completed track).
  - Evaluates milestone completion criteria and returns evaluated `List<ListeningMilestone>`.
- `TimeCapsuleRecap`:
  - Computes monthly and yearly listening recaps: top 5 tracks, top 5 artists, peak time of day, and total hours listened.

### 3. `:core:datastore` & `:core:data`
- Persist unlocked milestone dates and streak counters in `SettingsDataStore`.
- Expose reactive `streaks` and `milestones` in `SettingsViewModel`.

### 4. `:feature:settings`
- Dedicated "Listening Milestones & Streaks" section in `SettingsScreen`:
  - Streak banner with current and longest streak counter.
  - Interactive grid of unlocked and in-progress achievement badges.
  - "Shareable Recap Summary" card with formatted top artist and peak listening hour.

### 5. Verification
- `MilestoneCalculatorTest` in `:core:domain`.
- `SettingsViewModelTest` coverage for milestones and streak updates.
- Qualification via `./scripts/gate.sh slice` and `./scripts/gate.sh final`.
- Tag `v5.4-1`.

