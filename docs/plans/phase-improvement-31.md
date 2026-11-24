# Phase Improvement Loop 31: Audiophile 5-Band Parametric Equalizer Faders (v4.1.0 Milestone)

## 1. Goal
Implement customizable 5-band parametric equalizer controls with real-time hardware audio effect synchronization and persistent storage:
1. Multi-Band Equalizer Hardware & DSP Synchronization (`:core:player`):
   - Add fine-grained band tuning to `NexusAudioEffects`: `setBandLevel(band, levelmB)`, `setCustomBands(bands)`, `getBandLevels()`.
   - Support ±12 dB (±1200 mB) tuning across 5 standard center frequencies:
     - 60 Hz (Sub-Bass)
     - 230 Hz (Mid-Bass)
     - 910 Hz (Midrange)
     - 3.6 kHz (High-Mid / Presence)
     - 14 kHz (Treble / Air)
   - Synchronize with Android framework `android.media.audiofx.Equalizer` while maintaining in-memory fallbacks when unattached.
2. DataStore Persistence (`:core:model`, `:core:datastore`):
   - Add `customEqBands: List<Int>` to `ThemeConfig`.
   - Store and deserialize CSV string `CUSTOM_EQ_BANDS` in `SettingsDataStore`.
3. Material 3 Expressive Equalizer Console (`:feature:settings`):
   - Interactive 5-band vertical slider console rendered when `EqualizerPreset.CUSTOM` is selected.
   - Live numeric dB gain indicators (`+4 dB`, `0 dB`, `-6 dB`).
   - Center frequency labels (`60 Hz`, `230 Hz`, `910 Hz`, `3.6 kHz`, `14 kHz`).
   - "Reset to Flat" button.
4. Testing & Verification:
   - Unit tests in `NexusAudioEffectsTest` verifying band level clamping, custom band persistence, and preset recall.
   - Unit tests in `SettingsViewModelTest` verifying `setCustomEqBandLevel` and `resetCustomEqBands`.
   - Release qualification via `./scripts/gate.sh slice` and `./scripts/gate.sh final`.
   - Tag release: `v4.1-1`.

## 2. Tasks & Owners
- **Core Model**: Add `customEqBands` field to `ThemeConfig`.
- **Core DataStore**: Persist and read `customEqBands` in `SettingsDataStore`.
- **Core Player Effects**: Enhance `NexusAudioEffects` with `setBandLevel`, `setCustomBands`, and `getBandLevels`.
- **Feature Settings**: Add custom band methods to `SettingsViewModel` and render interactive 5-band faders in `SettingsScreen`.
- **Testing**: Add unit tests for band adjustments and ViewModel integration.
- **Verification**: Run `./scripts/gate.sh slice` and `./scripts/gate.sh final`.

## 3. Acceptance Criteria
- [ ] Individual bands can be independently adjusted from -1200 mB to +1200 mB.
- [ ] Custom band settings are persisted in DataStore across app restarts.
- [ ] Selecting `CUSTOM` or adjusting a slider switches to Custom preset mode.
- [ ] "Reset to Flat" resets all 5 bands to 0 dB.
- [ ] `./scripts/gate.sh slice` and `./scripts/gate.sh final` pass 100%.
