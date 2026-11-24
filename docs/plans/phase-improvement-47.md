# Improvement Loop 47: Interactive Music Identity Card & Shareable Sonic DNA Fingerprint Engine (v5.7.0)

## 1. Objectives & Value
- **Personalized Sonic Identity**: Synthesize user listening habits, epoch day streaks, milestone achievements, genre distribution, and peak diurnal listening periods into a collectible **Sonic DNA** profile.
- **8 Distinct Sonic Archetypes**: Classify listeners into unique music personalities—including **Nocturnal Voyager** (twilight acoustics), **Aurora Pulse** (sunrise rhythms), **Deep Focus Scholar** (sustained concentration), **Eclectic Explorer** (limitless genre breadth), **Rhythmic Athlete** (high-tempo kinetic drive), **Acoustic Purist** (warm analog instrumentation), **Chrono Discoverer** (daily continuous streaks), and **Sonic Nomad** (balanced all-day immersion).
- **Aesthetic Collectible Card UI**: Render an expressive, Material 3 card in `core:ui` with archetype badges, kinetic score meters (Variety, Energy Pulse, Acoustic Depth), genre chips, and dynamic gradient styling.
- **Social Sharing & Clipboard Export**: Provide one-tap formatted text summary generation for sharing across messaging and social apps.
- **Clean Architecture Integration**: Domain model in `:core:model`, calculation engine in `:core:domain:analytics`, UI card in `:core:ui`, ViewModel & Screen integration in `:feature:settings`.

## 2. Technical Architecture
- **Model**: `SonicArchetype`, `SonicDnaProfile` in `:core:model`.
- **Domain**: `SonicDnaCalculator` in `:core:domain:analytics`.
- **UI**: `SonicDnaCard` in `:core:ui`.
- **Settings Feature**:
  - `SettingsViewModel`: reactive `sonicDnaProfile: StateFlow<SonicDnaProfile>`.
  - `SettingsScreen`: embedded interactive Sonic DNA console with copy action.
- **Verification**:
  - `SonicDnaCalculatorTest` in `:core:domain`.
  - `SettingsViewModelTest` in `:feature:settings`.
  - `./scripts/gate.sh slice` and `./scripts/gate.sh final`.

