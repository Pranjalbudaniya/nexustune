# Blockers and Mitigations

| ID | Description | Impact | Action Required | Status |
|---|---|---|---|---|
| BLK-001 | Physical device `10BF7Q150G000LE` storage is low (567MB free on `/data`), causing Android `PackageInstallerService` to reject APK installations with `INSTALL_FAILED_INSUFFICIENT_STORAGE`. Strict harness rules prevent modifying or deleting user files outside `com.nexus.tune`. | On-device macrobenchmark re-installation paused; non-device execution and release qualification (`gate.sh final`) continue normally. | User can free ~2GB on device when awake to resume on-device connected benchmarks. | Active (Mitigated) |
