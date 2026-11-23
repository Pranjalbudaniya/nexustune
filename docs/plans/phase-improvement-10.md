# Phase Execution Plan: Improvement Loop 10 (v2.0.0)

## Objective
Implement **Audio Output Routing & Real-Time Audio Profile Indicator** in NexusTune, dynamically detecting output audio devices (Bluetooth A2DP, USB DAC, Wired Headset, Built-in Speaker) using Android's native `AudioManager` and `AudioDeviceCallback`, and displaying live high-fidelity audio output indicators.

## Scope & Components
1. **Model**:
   - `AudioDeviceType` (`BUILTIN_SPEAKER`, `BLUETOOTH_A2DP`, `WIRED_HEADSET`, `USB_DAC`, `OTHER`).
   - `AudioOutputDevice(type: AudioDeviceType, name: String, isHighRes: Boolean)`.
2. **Player Engine & Audio Routing**:
   - `AudioOutputRouteManager` in `core:player`:
     - Inspects current active audio devices via `AudioManager`.
     - Registers `AudioDeviceCallback` to stream output routing updates.
     - Exposes `currentOutputDevice: StateFlow<AudioOutputDevice>`.
3. **UI Integration**:
   - In `PlayerViewModel` (`feature:player`):
     - Expose `audioOutputDevice: StateFlow<AudioOutputDevice>`.
   - In `PlayerScreen` (`feature:player`):
     - Display live output routing pill in player header with contextual icon (`Bluetooth`, `Headphones`, `Speaker`).
4. **Unit Tests & Quality Gates**:
   - Unit tests in `core:player:testDebugUnitTest`.
   - Unit tests in `feature:player:testDebugUnitTest`.
   - `./scripts/gate.sh slice`
   - `./scripts/gate.sh perf` on attached device
   - `./scripts/gate.sh final` release verification
   - Tag `v2.0-1`

## Acceptance Criteria
- [ ] `AudioOutputRouteManager` detects current audio sink dynamically.
- [ ] `PlayerScreen` displays live audio route indicator.
- [ ] 100% test pass rate across all modules.
- [ ] `./scripts/gate.sh slice` passes.
- [ ] `./scripts/gate.sh perf` passes on device.
- [ ] `./scripts/gate.sh final` passes.
