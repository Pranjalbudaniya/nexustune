# Phase Improvement Loop 30: Bedtime Ambient Soundscapes & Binaural Brainwave Synthesizer (v4.0.0 Milestone)

## 1. Goal
Implement a real-time procedural DSP Ambient Soundscapes and Binaural Brainwave Synthesizer for sleep, focus, and relaxation:
1. Real-Time Procedural DSP Synthesis (`:core:domain:ambient`):
   - Pure Kotlin procedural audio math engine generating 16-bit PCM 44.1kHz stereo audio.
   - Support for 8 sound types:
     - `BROWN_NOISE`: Integrated Brownian random walk with leakage (1/f^2 spectrum) for ADHD masking and deep sleep.
     - `PINK_NOISE`: 3-pole IIR Kellet filter (1/f spectrum) simulating gentle rainfall and rustling leaves.
     - `WHITE_NOISE`: Uniform flat spectral density masking harsh environmental disturbances.
     - `BINAURAL_ALPHA`: 10Hz binaural beat difference (216Hz L / 226Hz R) for relaxed focus.
     - `BINAURAL_THETA`: 6Hz binaural beat difference (197Hz L / 203Hz R) for meditation and REM sleep.
     - `BINAURAL_DELTA`: 2.5Hz binaural beat difference (148.75Hz L / 151.25Hz R) for deep non-REM sleep.
     - `SOLFEGGIO_432HZ`: 432 Hz organic harmonic sine wave.
     - `SOLFEGGIO_528HZ`: 528 Hz transformational Solfeggio frequency.
2. Low-Latency AudioTrack Streaming Player (`:core:player:ambient`):
   - `AmbientSoundSynthesizer` providing streaming playback via Android `AudioTrack` with coroutine synthesis loop.
   - Support independent playback or layered background playback alongside music.
   - Volume adjustment and auto-stop integration with `SleepTimerManager`.
3. Preferences & Persistence (`:core:model`, `:core:datastore`):
   - Persist ambient sound preferences (`ambientSoundType`, `ambientSoundVolume`) in DataStore.
4. Material 3 Expressive UI Integration (`:feature:settings`):
   - Bedtime & Ambient Soundscapes card in `SettingsScreen`.
   - Play/Pause toggle, volume slider, horizontal sound profile chips, and sound benefit descriptions.
5. Automated Testing & Release Verification:
   - `AmbientSoundGeneratorTest`: Verify buffer sizes, amplitude limits, binaural stereo separation, and DSP spectrum properties.
   - Run `./scripts/gate.sh slice` and `./scripts/gate.sh final`.
   - Tag release: `v4.0-1`.

## 2. Tasks & Owners
- **Core Domain**: `AmbientSoundType` enum and `AmbientSoundGenerator` procedural math engine.
- **Core Model & DataStore**: Add ambient fields to `ThemeConfig` and `SettingsDataStore`.
- **Core Player**: `AmbientSoundSynthesizer` with `AudioTrack` streaming and `SleepTimerManager` hook.
- **Feature Settings**: `SettingsViewModel` methods and `SettingsScreen` UI card.
- **Testing**: Unit tests in `:core:domain` and `:core:player`.
- **Verification**: Full `./scripts/gate.sh final` gate pass and release tag `v4.0-1`.

## 3. Acceptance Criteria
- [ ] Procedural math generates clean, non-clipping 16-bit PCM audio across all 8 sound profiles.
- [ ] Binaural beats create distinct Left/Right frequency sinusoids for stereo brainwave stimulation.
- [ ] DataStore saves and restores ambient sound type and volume.
- [ ] UI provides play/pause, volume control, and sound selector chips.
- [ ] `./scripts/gate.sh slice` passes all tests.
- [ ] `./scripts/gate.sh final` passes and release APK is generated.

