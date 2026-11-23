# Device Status

## Hardware Profile
- Serial: `10BF7Q150G000LE`
- Model: `V2518` (Product: `V2428iC`, Device: `V2428`)
- Android Version: `16` (API level 36 / SDK 36)
- Screen Resolution: 1080 x 2392
- Screen Density: 440 dpi

## Display Modes (`dumpsys display`)
Supported modes on Display 0:
- Mode 1: 1080 x 2392 @ 120.00001 Hz (Default mode)
- Mode 2: 1080 x 2392 @ 90.0 Hz
- Mode 3: 1080 x 2392 @ 60.0 Hz
Observed active mode at boot/inspection: Mode 2 (90.0 Hz) / Mode 1 capable (120 Hz).

## Verification Strategy
- Because a physical 120Hz-capable Android 16 device is connected:
  - T3 instrumented tests (`connectedAndroidTest`) will execute on device `10BF7Q150G000LE`.
  - Macrobenchmarks (`FrameTimingMetric`, startup, scroll, transitions) will run with target device locked.
  - Baseline profiles will be generated on this device.
  - Live NewPipe extraction tests on the phone network will be conducted with short clips.
  - All device access serialized via `.device.lock`.
  - Media volume is kept low/silent.
