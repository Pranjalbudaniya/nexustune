# Phase Improvement Loop 75: Audiophile Analog Vacuum Tube Saturation & Even-Harmonic Warmth Studio

## 1. Context & Objective
Digital audio reproduction is inherently linear and pristine, yet human psychoacoustics deeply favors the euphonic warmth, natural compression, and spatial depth provided by analog vacuum tube amplifiers (such as Class-A 12AX7 triode preamplifiers and EL34 pentode push-pull power stages). In solid-state and digital signal chains, high frequencies can sound clinical or sterile.

Loop 75 introduces the **Audiophile Analog Vacuum Tube Saturation & Even-Harmonic Warmth Studio** (`TubeSaturationEngine`). It models the non-linear physics of vacuum tube grid conduction, plate saturation, and magnetic hysteresis:
- **Triode Mode (12AX7 / ECC83)**: Asymmetrical soft clipping generating rich 2nd-order even harmonics ($2f$), injecting acoustic warmth and full-bodied presence into vocals, acoustic guitars, and low-mids.
- **Pentode Mode (EL34 / 6L6)**: Symmetrical compression with alternating 3rd-order odd harmonics ($3f$), adding dynamic bite, snap, and punch to drums and modern electronic mixes.
- **Warm Tape Saturation**: Soft magnetic hysteresis rounding transients and gluing dense mixes.
- **Clean Analog Console (Class A)**: High-headroom discrete operational amplifier saturation imparting air and delicate saturation.
- **Dynamic DC Offset Suppression**: Automatic recursive DC rejection eliminating DC bias produced by asymmetric transfer functions.
- **Auto Makeup Headroom Compensation**: Real-time gain scaling to preserve perceived volume parity while enriching harmonic density.
- **Hardware DSP Linkage**: Direct coupling with `NexusAudioEffects` hardware equalizer bands 1 & 2 for physical acoustic reinforcement.

---

## 2. Work Breakdown & Ownership
| Task ID | Component | Description | Owner | Acceptance Check |
|---|---|---|---|---|
| T75.1 | `:core:model` | Add `TubeTopology`, `TubeWarmthProfile`, `TubeSaturationConfig`, and `TubeSaturationMetrics` to `Models.kt` and link to `ThemeConfig` | Audio Architect | Data structures compile and serialize cleanly |
| T75.2 | `:core:domain` | Implement `TubeSaturationEngine` with non-linear transfer curves, harmonic analysis, and auto-makeup headroom | Audio DSP Engineer | Unit tests confirm dominant 2nd harmonic in triode, DC blocker, and THD calculation |
| T75.3 | `:core:player` | Link `tubeWarmthOffset` into `NexusAudioEffects` band 1 & 2 EQ refresh | Audio Engine Engineer | Band refresh correctly applies offset |
| T75.4 | `:core:datastore` | Add preferences keys, reactive flows, and update functions in `SettingsDataStore.kt` | Data Engineer | Flow emits updated state on change |
| T75.5 | `:feature:settings` | Add ViewModel update methods, tests, and compose UI Studio Card with HUD | UI Engineer | Interactive sliders, profile chips, and metrics render cleanly |
| T75.6 | Verification | Execute `./scripts/gate.sh slice` and `./scripts/gate.sh final`, commit and tag `v6.25-1` | Lead Agent | All tests pass, release APK builds, clean git status |

---

## 3. Mathematical Foundations
1. **Triode Asymmetric Transfer Curve**:
   For driven signal $v = x \cdot (1.0 + 3.0 \cdot \text{drive}) + 0.3 \cdot \text{bias}$:
   $$f_{\text{triode}}(v) = \begin{cases}
   \frac{v - 0.25 \cdot \text{drive} \cdot v^2}{1.0 + 0.5 \cdot |v|}, & v \ge 0 \\
   \frac{v + 0.12 \cdot \text{drive} \cdot v^2}{1.0 + 0.7 \cdot |v|}, & v < 0
   \end{cases}$$
   Subtracting quiescent bias offset $f_{\text{triode}}(v_0)$ ensures $y(0) = 0$.

2. **Auto Makeup Gain**:
   $$\text{Gain}_{\text{comp}} = \frac{1}{\sqrt{1.0 + 1.2 \cdot \text{drive}^2}}$$

3. **Harmonic Metrics Analysis**:
   Probe with reference sinusoid $x(t) = A \sin(\omega t)$:
   $$H_2 = 20 \log_{10}\left(\frac{A_2}{A_1}\right), \quad H_3 = 20 \log_{10}\left(\frac{A_3}{A_1}\right), \quad \text{THD\%} = \frac{\sqrt{A_2^2 + A_3^2}}{A_1} \times 100\%$$

