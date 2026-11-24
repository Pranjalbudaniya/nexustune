# Phase Improvement Loop 26: Audio Stream Quality & Adaptive Cellular Data Saver

## 1. Goal
Implement intelligent audio streaming bitrate selection and adaptive cellular data saver mode:
1. Dynamic Audio Quality selection:
   - `AUTO` (Adaptive Data Saver): Introspects network state via `ConnectivityManager`. On unmetered networks (Wi-Fi/Ethernet), extracts highest bitrate audio streams. On metered/cellular networks, extracts data-saver streams (~50-70 kbps) to conserve mobile data quotas.
   - `HIGH` (High Fidelity): Strictly targets highest available bitrate (Opus ~160kbps / AAC 256kbps).
   - `MEDIUM` (Standard): Targets balanced ~128kbps streams for reliable streaming on standard mobile connections.
   - `LOW` (Data Saver): Targets low-bitrate streams (~50-70 kbps) for minimal bandwidth consumption and zero-buffering playback.
2. Quality-Aware Stream Extraction:
   - Update `MusicSource` & `NewPipeMusicSource` to filter and sort stream candidates according to target quality tier.
   - Cache stream URLs with composite key `"$trackId:$quality"`.
3. Live Network Awareness & Settings UI:
   - Expose current network metered state and preferred quality in `SettingsViewModel`.
   - Provide interactive `FilterChip` selection and live network status badge in `SettingsScreen`.
4. Automated Testing & Release Gate:
   - Comprehensive unit tests for stream filtering across quality tiers and network transitions.
   - Verification with `./scripts/gate.sh slice` and `./scripts/gate.sh final`.
   - Release tagging: `v3.6-1`.

## 2. Tasks & Owners
- **Core Domain & Model**: Add `AudioQualityPreset` enum and helper methods in `:core:model` and `:core:domain`.
- **Core Extractor**: Update `MusicSource`, `NewPipeMusicSource`, and `FakeMusicSource` to support quality-aware stream resolution.
- **Core Data & Player**: Pass quality preferences and network metered state into `MusicRepositoryImpl` and stream resolution.
- **Feature Settings**: Add `preferredQuality` state and live network badge in `SettingsViewModel` and `SettingsScreen`.
- **Testing**: Add unit tests for quality stream resolution and ViewModel state.
- **Verification**: Run `./scripts/gate.sh slice` and `./scripts/gate.sh final`.

## 3. Acceptance Criteria
- [ ] `AudioQualityPreset` supports `AUTO`, `HIGH`, `MEDIUM`, `LOW`.
- [ ] `NewPipeMusicSource` selects correct stream bitrate depending on quality parameter.
- [ ] `SettingsScreen` renders Audio Quality & Data Saver selector with live network status badge.
- [ ] All unit and Robolectric tests pass (`./scripts/gate.sh slice`).
- [ ] `./scripts/gate.sh final` passes with release APK build.
