package com.nexus.tune.core.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String? = null,
    val album: String? = null,
    val albumId: String? = null,
    val durationSeconds: Long = 0L,
    val artworkUrl: String? = null,
    val streamUrl: String? = null,
    val isExplicit: Boolean = false,
    val isLiked: Boolean = false,
    val isDownloaded: Boolean = false,
    val localFilePath: String? = null,
    val loudnessGainDb: Float = 0.0f
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String? = null,
    val artworkUrl: String? = null,
    val year: Int? = null,
    val trackCount: Int = 0,
    val tracks: List<Track> = emptyList()
)

data class Artist(
    val id: String,
    val name: String,
    val thumbnailUrl: String? = null,
    val topTracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList()
)

data class Playlist(
    val id: String,
    val title: String,
    val author: String? = null,
    val artworkUrl: String? = null,
    val trackCount: Int = 0,
    val isUserCreated: Boolean = false,
    val tracks: List<Track> = emptyList()
)

data class AudioStream(
    val url: String,
    val format: String, // Opus, AAC
    val bitrateKbps: Int,
    val expiresAtTimestampMs: Long
)

enum class PlaybackStatus {
    IDLE, BUFFERING, PLAYING, PAUSED, ENDED, ERROR
}

data class PlaybackState(
    val currentTrack: Track? = null,
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffle: Boolean = false,
    val isRepeatOne: Boolean = false,
    val isRepeatAll: Boolean = false,
    val queueIndex: Int = 0,
    val queue: List<Track> = emptyList(),
    val errorMessage: String? = null,
    val playbackSpeed: Float = 1.0f,
    val playbackPitch: Float = 1.0f,
    val playbackPitchSemitones: Int = 0
)

enum class ThemeSeedMode {
    DYNAMIC, VIBRANT, EXPRESSIVE, AMOLED, CUSTOM
}

enum class ShapePreset {
    SOFT_ROUNDED, FULL_EXPRESSIVE, GENTLE, SHARP, CUT_CORNER
}

enum class PlayerLayout {
    ART_FORWARD, VINYL_RECORD, SYNCED_LYRICS, MINIMALIST
}

enum class MiniPlayerStyle {
    DOCKED_BAR, FLOATING_PILL, COMPACT_SLIM
}

enum class VisualizerMode {
    OFF, SPECTRUM_BARS, CIRCULAR_WAVE, GLOW_PULSE
}

enum class SeekbarStyle {
    CLASSIC_SLIDER,
    WAVEFORM
}

enum class WakeAlarmSource {
    LIKED_SONGS, CURRENT_QUEUE, AMBIENT_SOUND
}

data class WakeAlarmConfig(
    val isEnabled: Boolean = false,
    val hour: Int = 7,
    val minute: Int = 0,
    val fadeInSeconds: Int = 120,
    val source: WakeAlarmSource = WakeAlarmSource.LIKED_SONGS,
    val targetVolume: Float = 0.8f
)

enum class SpatialAudioStage {
    OFF, STUDIO, LOUNGE, CONCERT_HALL, WIDE_ARENA
}

enum class CrossfadeCurve {
    EQUAL_POWER, LINEAR, EXPONENTIAL
}

enum class AudioQualityPreset(val label: String, val description: String) {
    AUTO("Auto", "Adaptive Data Saver on Cellular"),
    HIGH("High", "Max Fidelity (160-256 kbps)"),
    MEDIUM("Standard", "Balanced (128 kbps)"),
    LOW("Data Saver", "Low Bandwidth (48-70 kbps)")
}

data class VisualizerData(
    val magnitudes: FloatArray = FloatArray(32) { 0f },
    val amplitude: Float = 0f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VisualizerData
        return magnitudes.contentEquals(other.magnitudes) && amplitude == other.amplitude
    }

    override fun hashCode(): Int {
        var result = magnitudes.contentHashCode()
        result = 31 * result + amplitude.hashCode()
        return result
    }
}

data class ThemeConfig(
    val seedMode: ThemeSeedMode = ThemeSeedMode.DYNAMIC,
    val customSeedColorHex: String = "#6750A4",
    val isDarkMode: Boolean = true,
    val isAmoled: Boolean = false,
    val shapePreset: ShapePreset = ShapePreset.SOFT_ROUNDED,
    val playerLayout: PlayerLayout = PlayerLayout.ART_FORWARD,
    val miniPlayerStyle: MiniPlayerStyle = MiniPlayerStyle.DOCKED_BAR,
    val refreshRatePreferred: Int = 120, // 60, 90, 120
    val crossfadeSeconds: Int = 0, // 0 = gapless, 2..12s
    val autoSkipSegments: Boolean = true,
    val volumeNormalizationEnabled: Boolean = false,
    val targetGainmB: Int = 0,
    val cacheBudgetMb: Int = 1000,
    val perTrackGainEnabled: Boolean = true,
    val visualizerMode: VisualizerMode = VisualizerMode.SPECTRUM_BARS,
    val sleepTimerFadeSeconds: Int = 30,
    val infiniteAutoPlayEnabled: Boolean = true,
    val bitPerfectUsbAudioEnabled: Boolean = false,
    val forceHiResSamplingRate: Int = 0,
    val gaplessPreloadEnabled: Boolean = true,
    val gaplessPreloadSeconds: Int = 5,
    val autoGenreEqEnabled: Boolean = false,
    val volumeLimitPercent: Int = 100,
    val safeListeningAlertEnabled: Boolean = true,
    val spatialAudioEnabled: Boolean = false,
    val spatialAudioStage: SpatialAudioStage = SpatialAudioStage.OFF,
    val spatialAudioStrength: Int = 500,
    val crossfadeCurve: CrossfadeCurve = CrossfadeCurve.EQUAL_POWER,
    val shakeToExtendSleepTimerEnabled: Boolean = false,
    val shakeExtendMinutes: Int = 10,
    val ambientSoundType: AmbientSoundType = AmbientSoundType.OFF,
    val ambientSoundVolume: Float = 0.5f,
    val customEqBands: List<Int> = listOf(0, 0, 0, 0, 0),
    val bedtimeHandoffEnabled: Boolean = false,
    val bedtimeHandoffSoundType: AmbientSoundType = AmbientSoundType.BROWN_NOISE,
    val bedtimeHandoffVolume: Float = 0.3f,
    val bedtimeHandoffDurationMinutes: Int = 60,
    val seekbarStyle: SeekbarStyle = SeekbarStyle.WAVEFORM,
    val resetPitchOnTrackChange: Boolean = true,
    val smartShuffleEnabled: Boolean = true,
    val wakeAlarmConfig: WakeAlarmConfig = WakeAlarmConfig()
)

data class SoundDoseInfo(
    val dosePercent: Float = 0f,
    val isOverExposed: Boolean = false,
    val listeningMinutes: Long = 0L
)

data class StorageUsage(
    val cacheBytes: Long = 0L,
    val downloadBytes: Long = 0L
) {
    val totalBytes: Long get() = cacheBytes + downloadBytes
    val formattedTotal: String get() {
        val mb = totalBytes / (1024 * 1024.0)
        return if (mb >= 1024) {
            String.format(java.util.Locale.US, "%.1f GB", mb / 1024.0)
        } else {
            String.format(java.util.Locale.US, "%.1f MB", mb)
        }
    }
}

data class SearchResult(
    val tracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList()
)

data class LyricsLine(
    val startTimeMs: Long,
    val text: String
)

data class TrackLyrics(
    val trackId: String,
    val lines: List<LyricsLine> = emptyList(),
    val isSynced: Boolean = true
)

enum class EqualizerPreset {
    FLAT, BASS_BOOST, TREBLE_BOOST, VOCAL, ELECTRONIC, ROCK, CLASSICAL, JAZZ, HIP_HOP, ACOUSTIC, CUSTOM
}

data class GenreShare(
    val genre: String,
    val playCount: Int,
    val percentage: Int
)

data class ListeningStats(
    val totalPlays: Int = 0,
    val topArtists: List<String> = emptyList(),
    val morningPlays: Int = 0,
    val afternoonPlays: Int = 0,
    val eveningPlays: Int = 0,
    val nightPlays: Int = 0,
    val topGenres: List<GenreShare> = emptyList(),
    val peakPeriod: String = "All Day"
)

data class SkipSegment(
    val category: String, // "music_offtopic", "sponsor", "intro", "outro"
    val startMs: Long,
    val endMs: Long
)

data class ABLoopState(
    val isEnabled: Boolean = false,
    val startMs: Long? = null,
    val endMs: Long? = null,
    val loopCount: Int = 0
)

enum class AudioDeviceType {
    BUILTIN_SPEAKER,
    BLUETOOTH_A2DP,
    WIRED_HEADSET,
    USB_DAC,
    OTHER
}

data class AudioOutputDevice(
    val type: AudioDeviceType = AudioDeviceType.BUILTIN_SPEAKER,
    val name: String = "Phone Speaker",
    val isHighRes: Boolean = false,
    val sampleRates: List<Int> = emptyList(),
    val bitDepth: Int = 16,
    val isBitPerfectSupported: Boolean = false
) {
    val fidelityDescription: String get() {
        return if (isHighRes && sampleRates.isNotEmpty()) {
            val maxRateKHz = sampleRates.maxOrNull()?.let { it / 1000 } ?: 48
            "${bitDepth}-bit / ${maxRateKHz}kHz"
        } else if (isHighRes) {
            "${bitDepth}-bit Hi-Res"
        } else {
            "Standard Audio"
        }
    }
}

enum class AmbientSoundType(val displayName: String, val description: String) {
    OFF("Off", "Ambient generator disabled"),
    BROWN_NOISE("Brown Noise", "Deep low-frequency rumble ideal for sleep & ADHD masking"),
    PINK_NOISE("Pink Noise", "Gentle natural frequency decay like soothing rainfall"),
    WHITE_NOISE("White Noise", "Crisp full-spectrum sound to mask ambient disturbances"),
    BINAURAL_ALPHA("Alpha Waves (10Hz)", "Binaural beat for relaxed focus and flow (use headphones)"),
    BINAURAL_THETA("Theta Waves (6Hz)", "Binaural beat for meditation and REM sleep (use headphones)"),
    BINAURAL_DELTA("Delta Sleep (2.5Hz)", "Binaural beat for deep restorative sleep (use headphones)"),
    SOLFEGGIO_432HZ("432Hz Harmony", "Natural harmonic resonance frequency"),
    SOLFEGGIO_528HZ("528Hz Miracle", "Transformation and clarity tone")
}

data class TrackBookmark(
    val id: Long = 0,
    val trackId: String,
    val title: String,
    val positionMs: Long,
    val createdAtTimestamp: Long = System.currentTimeMillis()
) {
    val formattedPosition: String get() {
        val totalSecs = (positionMs / 1000).coerceAtLeast(0)
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format("%02d:%02d", mins, secs)
    }
}

enum class MoodCategory(
    val displayName: String,
    val description: String
) {
    ALL("All", "Everything in your collection"),
    ENERGIZE("Energize", "High-energy beats & motivational anthems"),
    FOCUS("Focus", "Instrumental & ambient concentration"),
    CHILL("Chill", "Relaxed acoustic, lofi & soulful melodies"),
    WORKOUT("Workout", "Bass-driven, high-tempo workout tracks"),
    COMMUTE("Commute", "Upbeat popular favorites & sing-alongs"),
    SLEEP("Sleep", "Gentle lullabies, ambient tones & slow decay")
}

enum class LibrarySortOrder(val displayName: String) {
    RECENT("Recently Added"),
    TITLE_ASC("Title (A-Z)"),
    ARTIST_ASC("Artist (A-Z)"),
    DURATION_DESC("Duration (Longest)")
}

