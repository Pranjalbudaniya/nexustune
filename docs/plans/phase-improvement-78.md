# Phase Improvement Loop 78: Audiophile Analog Tape Flutter, Wow & Hysteresis Warmer Studio

## 1. Context & Objective
Mastering audio to 1/2-inch and 1/4-inch reel-to-reel magnetic tape machines (such as the legendary Studer A800 and Ampex ATR-102) remains the gold standard in music production for gluing mixes, adding natural low-end punch, and imparting lush harmonic body. Solid-state digital audio playback can sound sterile without the organic physical characteristics of magnetized ferric/cobalt formulations and capstan motor mechanics.

Loop 78 introduces the **Audiophile Analog Tape Flutter, Wow & Hysteresis Warmer Studio** (`TapeHysteresisEngine`). It models the electromagnetic and mechanical physics of reel-to-reel tape decks:
- **Langevin/Jiles-Atherton Magnetic Hysteresis**: Non-linear saturation transfer curve causing smooth progressive peak rounding, even/odd harmonic saturation, and natural dynamic compression without harsh clipping.
- **Electromagnetic Head Bump Resonance**: Tuned low-frequency peaking biquad ($50\dots 80\text{ Hz}$) recreating the physical acoustic flux accumulation between the playback head gap and tape boundary, delivering authoritative bass foundation.
- **Selectable Tape Formulations & Speeds**:
  - `IPS_30` (30 inches per second): Pristine studio mastering with ultra-low wow/flutter and flat extension up to 24 kHz.
  - `IPS_15` (15 inches per second): Classic rock & pop standard with robust 55 Hz head bump and warm high-frequency saturation.
  - `IPS_7_5` (7.5 inches per second): Vintage tape character with mellow high-frequency rolloff and gentle mechanical warmth.
- **Dual-LFO Wow & Flutter Modulation**: Combines slow cyclic motor drift ($0.8\text{ Hz}$ wow) and scraped-tape micro-flutter ($8.5\text{ Hz}$ flutter) with fractional delay interpolation, introducing subtle organic movement and chorus-like width.
- **Hardware DSP Linkage**: Direct coupling into `NexusAudioEffects` hardware equalizer bands 0 and 1 for tactile physical low-end warmth.

---

## 2. Work Breakdown & Ownership
| Task ID | Component | Description | Owner | Acceptance Check |
|---|---|---|---|---|
| T78.1 | `:core:model` | Add `TapeSpeed`, `TapePreset`, `TapeHysteresisConfig`, and `TapeHysteresisMetrics` in `Models.kt` and link to `ThemeConfig` | Audio Architect | Data structures compile and serialize cleanly |
| T78.2 | `:core:domain` | Implement `TapeHysteresisEngine` with magnetic hysteresis curve, head bump peaking biquad, dual-LFO fractional delay wow/flutter, and tape speed filtering | Audio DSP Engineer | Unit tests verify head bump boost, saturation curve, wow modulation, and stability |
| T78.3 | `:core:player` | Add `applyTapeWarmthOffset` and `clearTapeWarmthOffset` in `NexusAudioEffects` | Audio Engine Engineer | Band refresh applies tape warmth offset |
| T78.4 | `:core:datastore` | Add preferences keys, reactive flows, and update methods in `SettingsDataStore.kt` | Data Engineer | Flow emits updated state on change |
| T78.5 | `:feature:settings` | Add ViewModel update methods, tests, and Compose UI Tape Studio Card with live diagnostic HUD | UI Engineer | Interactive drive slider, speed chips, head bump slider, and HUD render cleanly |
| T78.6 | Verification | Execute `./scripts/gate.sh slice` and `./scripts/gate.sh final`, commit and tag `v6.28-1` | Lead Agent | All tests pass, release APK builds, clean git status |

---

## 3. Mathematical Foundations
1. **Langevin Soft Saturation Transfer Function**:
   $$f_{\text{tape}}(x) = \frac{2}{\pi} \arctan\left(\frac{\pi}{2} x \cdot (1.0 + 2.5 \cdot \text{drive})\right) \cdot \left(1.0 - 0.08 \cdot x^2 \cdot \text{sign}(x)\right)$$

2. **Head Bump Peaking Filter**:
   $$H_{\text{bump}}(s) = \frac{s^2 + \left(\frac{\omega_0}{Q} \cdot 10^{\frac{G}{40}}\right) s + \omega_0^2}{s^2 + \left(\frac{\omega_0}{Q} \cdot 10^{-\frac{G}{40}}\right) s + \omega_0^2}, \quad f_0 \approx 55\text{ Hz}, \quad Q \approx 1.2$$

3. **Wow & Flutter Dual-LFO Delay**:
   $$\Delta t[n] = D_0 + A_{\text{wow}} \sin(2\pi f_{\text{wow}} n / f_s) + A_{\text{flutter}} \sin(2\pi f_{\text{flutter}} n / f_s)$$

