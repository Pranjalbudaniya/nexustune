# Council Reconvene (Post-Slice 2) - Round 1: Power-User Skeptic

## Assessment & Attack
1. **Attack on Queue Persistence**: Make sure that when the user kills the app from recents while playing at 2 minutes 30 seconds, restarting the app restores track index AND position within 1 second.
2. **Attack on Local Audio**: Scanning MediaStore must not block the UI thread. It must be a background coroutine flow.
3. **Attack on EQ**: Equalizer presets must survive device reboot and Bluetooth reconnect.
