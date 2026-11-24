# Phase Improvement 86: Audiophile Dynamic Multiband Sub-Bass Transient Punch & Attack Reshaper Studio

## 1. Goal
Design and implement the **Audiophile Dynamic Multiband Sub-Bass Transient Punch & Attack Reshaper Studio** (`BassAttackReshaperEngine`), providing sub-bass band-split transient attack velocity amplification, sustain resonance tail clamping, and acoustic kick/bass envelope re-contouring for razor-sharp low-end precision without boomy mud.

## 2. Key Architecture & Components
1. **Core Domain Audio Engine (`core:domain:audio:BassAttackReshaperEngine`)**:
   - Discrete 2nd-order Linkwitz-Riley lowpass crossover (<160 Hz) isolating the sub-bass spectrum.
   - Dual envelope detection architecture tracking fast transient attack onset ($\tau_{att} \approx 6\text{ ms}$) and resonant sustain tail decay ($\tau_{rel} \approx 180\text{ ms}$).
   - Dynamic attack ratio multiplier and sustain tail damping curve.
   - Solo isolated sub-bass transient punch audition mode.
   - Real-time metrics helper estimating attack velocity punch ratio, sustain reduction dB, and bass tightness rating.
2. **Core Model (`core:model:Models.kt`)**:
   - `BassAttackTarget`: `SUB_KICK_60HZ`, `PUNCHY_BASS_80HZ`, `ACOUSTIC_UPRIGHT_100HZ`, `SLAP_BASS_120HZ`, `FULL_LOW_160HZ`.
   - `BassReshaperMode`: `DYNAMIC_ATTACK_BOOST`, `SUSTAIN_TAIL_DAMP`, `BALANCED_TRANSIENT_PUNCH`, `SOLO_ATTACK_TRANSIENT`.
   - `BassAttackPreset`: `BYPASS`, `TIGHT_KICK_IMPACT`, `DE_BOOM_SUSTAIN`, `ACOUSTIC_JAZZ_BASS`, `HARDSTYLE_SUB_SLAP`.
   - `BassAttackReshaperConfig`: Data class with data-store persistence.
   - `BassAttackReshaperMetrics`: Real-time metrics data class.
3. **Core Player Effects (`core:player:NexusAudioEffects`)**:
   - Integration with hardware equalizer bands 0 & 1 (`applyBassAttackOffset`, `clearBassAttackOffset`, `getBassAttackOffset`).
4. **DataStore Persistence (`core:datastore:SettingsDataStore`)**:
   - DataStore preferences keys and mapping into `ThemeConfig`.
5. **Settings UI & ViewModel (`feature:settings`)**:
   - Interactive Material 3 Expressive studio card placed in its own dedicated `item { ... }` block in `SettingsScreen.kt` to comply with JVM 64KB method limits.
6. **Unit Tests**:
   - Comprehensive test suite for `BassAttackReshaperEngineTest`, `NexusAudioEffectsTest`, and `SettingsViewModelTest`.

## 3. Verification & Acceptance Criteria
- 100% JVM unit tests passing across all 16 modules (`./scripts/gate.sh slice`).
- Complete build verification and R8 release APK shrinking (`./scripts/gate.sh final`).
- Release tag `v6.36-1`.

