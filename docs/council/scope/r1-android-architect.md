# Council Round 1: Android Architect Proposal

## Role Mandate
Define clean module boundaries, lifecycle management, audio service architecture, and Android platform compatibility (API 31+).

## Technical Proposals

### 1. Module Structure
- `:core:model` (Pure Kotlin, immutable models)
- `:core:common` (Dispatchers, Result, Flow extensions)
- `:core:domain` (Use cases, pure repository interfaces; strictly NO Android dependencies)
- `:core:data` (Repositories, mappers, synchronization logic)
- `:core:database` (Room 2.6+, entities, DAOs, schema migrations)
- `:core:datastore` (DataStore Preferences for themes, audio, playback settings)
- `:core:extractor` (`MusicSource` abstraction, `NewPipeMusicSource`, `FakeMusicSource`)
- `:core:player` (Media3 `MediaSessionService`, ExoPlayer, effects, audio focus)
- `:core:designsystem` (M3 Expressive theme, color tokens, typography, shapes, springs)
- `:core:ui` (Shared composables, error cards, shimmer skeletons, mini-player)
- `:core:testing` (Test fixtures, fake data, coroutine test rules)
- `:feature:home`, `:feature:search`, `:feature:library`, `:feature:player`, `:feature:settings`
- `:benchmark` (Macrobenchmark target)

### 2. Media3 Service Architecture
- Run playback in a dedicated `MediaSessionService` (`NexusPlaybackService`) declaring `foregroundServiceType="mediaPlayback"`.
- Use `ResolvingDataSource` to dynamically resolve expiring stream URLs on demand.
- Room database stores playback queue and restores index and millisecond position upon process rebirth.

### 3. Android 14/15 Foreground Service Rules
- Downloads must not run as unbounded foreground services. Use `WorkManager` with `Constraints(NetworkType.CONNECTED)` and `OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST` with notification progress updates.

