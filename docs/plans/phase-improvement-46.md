# Improvement Loop 46: Interactive DJ Audio Transformation & Sound Effects Engine (v5.6.0)

## 1. Objectives & Value
- **Real-Time Sound Transformation**: Enable listeners to transform any streaming or local track into popular audio aesthetics—including **Nightcore** (sped-up with pitched vocals), **Slowed + Reverb** (deliberate downtempo with acoustic widening), **Vintage Vinyl Lo-Fi** (warm analog flutter with narrowed mono-centric stage), **Vaporwave** (deep pitched slow-motion), and **Bass Pushed** (sub-bass resonance enhancement).
- **Mathematical Soundstage & Pitch Calibration**: Calculate precise playback speed factors ($0.50\times - 2.00\times$), semitone transposition ($\pm 6$ semitones), stereo width matrices ($0.0\times$ mono to $2.0\times$ super-wide), and effective remaining durations.
- **Interactive DJ Control Console**: Equip `PlayerScreen` with an intuitive Material 3 Expressive `ModalBottomSheet` featuring instant preset chips, tactile fine-tuning sliders, live duration/pitch indicators, and one-tap instant reset.
- **Persistence & Customization**: Integrate DJ preferences into `SettingsDataStore`, `SettingsViewModel`, and `SettingsScreen` with toggleable session persistence across tracks.

## 2. Technical Architecture
- **Model**: `DjPreset`, `DjFxConfig` in `:core:model`.
- **Domain**: `DjTransformEngine` in `:core:domain:audio`.
- **DataStore & Data**: `djPreset`, `djSpeed`, `djSemitones`, `djSoundstageWidth`, `djLoFiFilter`, `djBassBoost`, `djRememberAcrossTracks` in `SettingsDataStore`, `SettingsRepository`, and `SettingsRepositoryImpl`.
- **Player Feature**:
  - `PlayerViewModel`: state flows `djFxConfig`, controls `setDjPreset()`, `setDjSpeed()`, `setDjSemitones()`, `setDjSoundstageWidth()`, `resetDjFx()`.
  - `PlayerScreen`: top-bar DJ launch action and full `ModalBottomSheet` console.
- **Settings Feature**:
  - `SettingsViewModel` & `SettingsScreen`: dedicated DJ Sound Effects card.
- **Verification**:
  - `DjTransformEngineTest` in `:core:domain`.
  - `PlayerViewModelTest` in `:feature:player`.
  - `SettingsViewModelTest` in `:feature:settings`.
  - `./scripts/gate.sh slice` and `./scripts/gate.sh final`.
