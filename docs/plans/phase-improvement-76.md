# Phase Improvement Loop 76: Audiophile Multi-Band Transient Shaper & Dynamic Punch Studio

## 1. Context & Objective
In modern audio production and mobile headphone listening, dynamic excitement is primarily dictated by **transients**—the rapid initial velocity impulses of drum hits, guitar plucks, piano hammer attacks, and percussion transients. Conventional dynamic compressors indiscriminately compress the entire audio envelope based on RMS volume thresholds, frequently squashing dynamic life and dulling transient punch.

Loop 76 introduces the **Audiophile Multi-Band Transient Shaper & Dynamic Punch Studio** (`TransientShaperEngine`). Unlike static compressors or linear equalizers, the transient shaper utilizes dual ballistic differential envelope followers ($\text{Envelope}_{\text{fast}}$ vs $\text{Envelope}_{\text{slow}}$):
- **Differential Transient Detection**: Tracks the instantaneous derivative of envelope energy $\Delta E[n] = E_{\text{fast}}[n] - E_{\text{slow}}[n]$. When $\Delta E > 0$, an attack transient occurs; when $\Delta E \le 0$, the signal is in sustain/decay.
- **Independent Attack & Sustain Manipulation**:
  - **Attack Control** (-100% to +100%): Boosts transient attack (up to +10 dB) for punchy, immediate, crisp percussive snap, or softens aggressive transients (down to -6 dB) for fatigue-free relaxing listening.
  - **Sustain Control** (-100% to +100%): Lengthens room reverberation, acoustic decay, and body bloom, or dries up boomy room rings for surgical rhythmic precision.
- **Band Focus Filtering**: Selectable focus bands (`FULL_BAND`, `BASS_PUNCH_LOW` 80-300 Hz, `MID_ATTACK_SNAP` 1-4 kHz, `HIGH_PLUCK_SHINE` >5 kHz) allowing targeted transient shaping without affecting unintended frequency spectra.
- **Hardware DSP Linkage**: Direct coupling into `NexusAudioEffects` hardware bass boost and EQ offsets for physical dynamic tactile punch.
- **Real-Time Crest Factor & Transient Diagnostics**: Continuous crest factor ($V_{\text{peak}} / V_{\text{RMS}}$), attack delta (dB), sustain ratio (dB), and dynamic punch scoring.

---

## 2. Work Breakdown & Ownership
| Task ID | Component | Description | Owner | Acceptance Check |
|---|---|---|---|---|
| T76.1 | `:core:model` | Add `TransientBandFocus`, `TransientPreset`, `TransientShaperConfig`, and `TransientShaperMetrics` in `Models.kt` and link to `ThemeConfig` | Audio Architect | Data structures compile cleanly |
| T76.2 | `:core:domain` | Implement `TransientShaperEngine` with dual ballistic envelope tracking, differential gain modulation, band focus biquads, and crest factor metrics | Audio DSP Engineer | Unit tests verify attack boost, sustain attenuation, crest factor, and boundary safety |
| T76.3 | `:core:player` | Add `applyTransientPunchOffset` and `clearTransientPunchOffset` in `NexusAudioEffects` | Audio Engine Engineer | Band refresh applies transient punch offset |
| T76.4 | `:core:datastore` | Add preferences keys, reactive flows, and update methods in `SettingsDataStore.kt` | Data Engineer | Flow emits updated state on change |
| T76.5 | `:feature:settings` | Add ViewModel update methods, tests, and Compose UI Transient Studio Card with HUD | UI Engineer | Interactive attack/sustain sliders, focus chips, and metrics render cleanly |
| T76.6 | Verification | Execute `./scripts/gate.sh slice` and `./scripts/gate.sh final`, commit and tag `v6.26-1` | Lead Agent | All tests pass, release APK builds, clean git status |

---

## 3. Mathematical Foundations
1. **Dual Ballistic Envelopes**:
   $$E_{\text{fast}}[n] = \alpha_{\text{fast}} \cdot E_{\text{fast}}[n-1] + (1 - \alpha_{\text{fast}}) \cdot |x[n]|$$
   $$E_{\text{slow}}[n] = \alpha_{\text{slow}} \cdot E_{\text{slow}}[n-1] + (1 - \alpha_{\text{slow}}) \cdot |x[n]|$$
   where $\alpha = \exp(-1 / (\tau \cdot f_s))$, $\tau_{\text{fast}} \approx 2\text{ ms}$, $\tau_{\text{slow}} \approx 30\text{ ms}$.

2. **Transient Gain Modulation**:
   $$\Delta E = E_{\text{fast}} - E_{\text{slow}}$$
   $$G[n] = 1.0 + \text{attack} \cdot \frac{\max(0, \Delta E)}{E_{\text{slow}} + \epsilon} + \text{sustain} \cdot \frac{\max(0, -\Delta E)}{E_{\text{fast}} + \epsilon}$$

3. **Crest Factor**:
   $$\text{Crest Factor (dB)} = 20 \log_{10}\left(\frac{V_{\text{peak}}}{V_{\text{RMS}} + \epsilon}\right)$$

