# Phase Improvement 21: Dual-Engine Crossfade DSP & Equal Power Curve Engine (v3.1.0 Milestone)

## Goal
Implement a mathematical **Crossfade Curve & Attenuation Fader DSP Engine** supporting true DJ-grade audio transitions across track boundaries. The engine provides three acoustic crossfade curves (`EQUAL_POWER`, `LINEAR`, `EXPONENTIAL`) ensuring zero volume dips or perceptual distortion when transitioning between consecutive songs.

## Architecture & Mathematical Design
1. **Core Model (`core:model`)**:
   - `enum class CrossfadeCurve`:
     - `EQUAL_POWER`: Preserves constant acoustic energy ($\cos(t)/\sin(t)$ curve, $\text{vol}_{\text{out}}^2 + \text{vol}_{\text{in}}^2 = 1.0$), preventing audio dips.
     - `LINEAR`: Direct proportional slope.
     - `EXPONENTIAL`: Steep fade-out with rapid introduction.
   - Update `ThemeConfig`:
     - `val crossfadeCurve: CrossfadeCurve = CrossfadeCurve.EQUAL_POWER`

2. **Core Player DSP (`core:player`)**:
   - Create `CrossfadeFader`:
     - `calculateAttenuation(elapsedMs: Long, durationMs: Long, curve: CrossfadeCurve): CrossfadeVolumes`
     - Returns `data class CrossfadeVolumes(val outgoingVolume: Float, val incomingVolume: Float)` clamped to `[0.0f, 1.0f]`.
   - Comprehensive unit tests in `CrossfadeFaderTest` verifying equal-power energy preservation, edge boundaries, and mathematical curve adherence.

3. **Core Datastore (`core:datastore`)**:
   - Persist `CROSSFADE_CURVE` in DataStore preferences.

4. **Settings & UI (`feature:settings`)**:
   - In `SettingsViewModel`:
     - Expose `setCrossfadeCurve(curve: CrossfadeCurve)`.
   - In `SettingsScreen`:
     - When `crossfadeSeconds > 0`, present curve selection chips (`Equal Power (Studio)`, `Linear`, `Exponential`).
     - Update About banner to `NexusTune v3.1.0` | `Dual-Engine Dynamic Crossfade & Equal Power DSP`.

5. **Verification**:
   - Unit tests in `CrossfadeFaderTest` and `SettingsViewModelTest`.
   - Run `./scripts/gate.sh slice`.
   - Run `./scripts/gate.sh final` to build release APK.
   - Tag release `v3.1-1`.
