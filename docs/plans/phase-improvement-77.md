# Phase Improvement Loop 77: Audiophile Multi-Band Dynamic Stereo Imager & Ambience Extractor Studio

## 1. Context & Objective
Listening on compact headphones and earbuds often creates an unnatural "in-head" acoustic localization where instruments either sit dead-center or are hard-panned into left/right ears without natural acoustic air, room reflections, or spatial depth. Conversely, crude stereo wideners indiscriminately amplify the side difference signal across all frequencies, obliterating mono bass punch and causing severe destructive phase cancellation when played on mono bluetooth speakers or smartphone speakers.

Loop 77 introduces the **Audiophile Multi-Band Dynamic Stereo Imager & Ambience Extractor Studio** (`DynamicStereoImagerEngine`). It performs orthogonal Mid/Side matrixing with frequency-split spatial processing:
- **Mono-Bass Centering (Mono Foldback)**: Below a configurable cutoff (80-250 Hz), side information is attenuated to collapse sub-bass and kick into solid mono, freeing up DAC dynamic headroom and ensuring punchy low-end impact.
- **Mid-Band Lead Anchor**: Preserves dead-center localization for lead vocals, bass guitar, and solo instruments while expanding lateral soundstage width.
- **High-Band Diffuse Ambience Expansion**: Utilizes all-pass phase dispersion and micro-delay decorrelation (>5 kHz) to reconstruct a three-dimensional acoustic room impression far outside the listener's head.
- **Intelligent Mono Compatibility Phase Guard**: Continuously computes the normalized Pearson correlation coefficient $\rho_{LR} \in [-1.0, 1.0]$. If phase inversion risk is detected ($\rho_{LR} < 0.25$), side gain is automatically tapered to ensure zero comb-filtering or volume collapse in mono environments.
- **Hardware Virtualizer Linkage**: Direct coupling into `NexusAudioEffects.virtualizer` and spatial stage for physical hardware coordination.

---

## 2. Work Breakdown & Ownership
| Task ID | Component | Description | Owner | Acceptance Check |
|---|---|---|---|---|
| T77.1 | `:core:model` | Add `StereoImagerPreset`, `DynamicStereoImagerConfig`, and `StereoImagerMetrics` in `Models.kt` and link to `ThemeConfig` | Audio Architect | Data structures compile and serialize cleanly |
| T77.2 | `:core:domain` | Implement `DynamicStereoImagerEngine` with Mid/Side decomposition, mono-bass highpass/lowpass biquads, all-pass decorrelation, and Pearson correlation tracking | Audio DSP Engineer | Unit tests verify mono collapse under cutoff, correlation calculation, and stereo width modulation |
| T77.3 | `:core:player` | Add `applySpatialSpreadOffset` and `clearSpatialSpreadOffset` in `NexusAudioEffects` | Audio Engine Engineer | Hardware virtualizer coordinates with stereo imager |
| T77.4 | `:core:datastore` | Add preferences keys, reactive flows, and update methods in `SettingsDataStore.kt` | Data Engineer | Flow emits updated state on change |
| T77.5 | `:feature:settings` | Add ViewModel update methods, tests, and Compose UI Stereo Imager Studio Card with correlation meter HUD | UI Engineer | Interactive width sliders, bass cutoff chips, and live correlation HUD render cleanly |
| T77.6 | Verification | Execute `./scripts/gate.sh slice` and `./scripts/gate.sh final`, commit and tag `v6.27-1` | Lead Agent | All tests pass, release APK builds, clean git status |

---

## 3. Mathematical Foundations
1. **Mid/Side Matrix Transformation**:
   $$M = \frac{L + R}{\sqrt{2}}, \quad S = \frac{L - R}{\sqrt{2}}$$
   $$L' = \frac{M + S'}{\sqrt{2}}, \quad R' = \frac{M - S'}{\sqrt{2}}$$

2. **Mono-Bass Attenuation**:
   $$S_{\text{filtered}} = S \cdot H_{\text{HP}}(f, f_{\text{cutoff}}) \cdot \text{width}$$

3. **Pearson Correlation Coefficient**:
   $$\rho_{LR} = \frac{\sum_{i=1}^N L[i] \cdot R[i]}{\sqrt{\sum_{i=1}^N L[i]^2 \cdot \sum_{i=1}^N R[i]^2} + \epsilon}$$

