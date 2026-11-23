# Phase 4 Plan: Vertical Slices Implementation

## Objective
Implement seven focused vertical slices (data -> domain -> UI -> tests), advancing the functional and visual capabilities of NexusTune. Reconvene the Council after Slice 2.

## Slice Progression Plan
| Slice | Name | Focus Areas | Acceptance Check |
|---|---|---|---|
| S1 | Search + Play One Track | Query search with suggestions, live stream resolution, play/pause playback | T1 unit tests, stream resolution verified |
| S2 | Queue + Background Playback + Notification | Queue reordering, MediaSessionService backgrounding, notification actions | Process death recovery, background playback |
| -- | Council Reconvene | Review S1 & S2 progress, audit architecture and frame budget | Council minutes & votes in `docs/council/slice2/` |
| S3 | Library + Playlists + Likes + History | Room persistence of favorites, recent tracks, local MediaStore merging | Room migration test, DAO tests |
| S4 | Player Polish + Theming Engine | 4 Player layouts (Art-Forward, Vinyl, Lyrics, Minimal), 3 mini-player styles, AMOLED black | Previews for all layouts, dynamic color updates |
| S5 | Downloads + Offline | WorkManager download worker, storage management, offline fallback | Offline playback without network |
| S6 | Settings + Customization | Live settings dispatch via DataStore, refresh rate mode selection | Zero-restart theme updates |
| S7 | Equalizer + Backup/Restore + Lyrics | 5-band EQ presets, JSON export/import, synced LRCLIB lyrics | EQ audio session attach, round-trip JSON |

## Acceptance Criteria
- Each slice passes `./scripts/gate.sh slice`.
- Tests added for every slice (use case, view model, repository).
- All changes committed per slice.

