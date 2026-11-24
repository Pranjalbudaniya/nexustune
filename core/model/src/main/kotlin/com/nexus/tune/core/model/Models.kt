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
    val playbackPitchSemitones: Int = 0,
    val loudnessMultiplier: Float = 1.0f
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

enum class LoudnessNormalizationMode(val displayName: String, val description: String) {
    OFF("Off", "Direct unaltered audio output"),
    REPLAYGAIN_TRACK("ReplayGain (Track)", "Standardizes loudness to target LUFS per individual track"),
    REPLAYGAIN_ALBUM("ReplayGain (Album)", "Preserves intra-album dynamic range while leveling volume"),
    DYNAMIC_PEAK_LIMITER("Dynamic Peak Limiter", "Soft-knee limiter preventing digital peak distortion")
}

data class LoudnessConfig(
    val mode: LoudnessNormalizationMode = LoudnessNormalizationMode.REPLAYGAIN_TRACK,
    val targetLufs: Float = -14.0f,
    val preAmpGainDb: Float = 0.0f
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
    val wakeAlarmConfig: WakeAlarmConfig = WakeAlarmConfig(),
    val loudnessConfig: LoudnessConfig = LoudnessConfig(),
    val djFxConfig: DjFxConfig = DjFxConfig(),
    val djRememberAcrossTracks: Boolean = false,
    val harmonicMixingEnabled: Boolean = true
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

data class CustomEqProfile(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val bands: List<Int> = listOf(0, 0, 0, 0, 0),
    val createdAtTimestamp: Long = System.currentTimeMillis()
)

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

data class ListeningStreak(
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val lastActiveDateEpochDay: Long = 0L
)

enum class MilestoneType(
    val title: String,
    val description: String,
    val targetThreshold: Int
) {
    FIRST_BEAT("First Beat", "Completed playback of your first song", 1),
    NIGHT_OWL("Night Owl", "50+ tracks played late at night (12 AM - 6 AM)", 50),
    EARLY_BIRD("Early Bird", "50+ tracks played in the morning (6 AM - 12 PM)", 50),
    EXPLORER("Sonic Explorer", "Discovered 50+ unique artists", 50),
    CENTURY_CLUB("Century Club", "Played 100+ total tracks in your collection", 100),
    STREAK_MASTER("Streak Master", "Maintained a 7-day continuous listening streak", 7),
    AUDIOPHILE("Audiophile Gold", "Listened in Hi-Res, 3D Spatial Audio, or with custom DSP EQ", 1)
}

data class ListeningMilestone(
    val type: MilestoneType,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val progressPercent: Float,
    val currentCount: Int,
    val targetCount: Int
)

data class TimeCapsuleRecap(
    val periodName: String = "Personal Music Time-Capsule",
    val totalPlays: Int = 0,
    val totalMinutesListened: Long = 0L,
    val topArtists: List<String> = emptyList(),
    val topGenres: List<GenreShare> = emptyList(),
    val peakPeriod: String = "All Day",
    val streakDays: Int = 0
)

enum class DjPreset(val displayName: String) {
    NORMAL("Original"),
    NIGHTCORE("Nightcore"),
    SLOWED_REVERB("Slowed + Reverb"),
    VINTAGE_VINYL("Vintage Lo-Fi"),
    VAPORWAVE("Vaporwave"),
    DEEP_BASS("Bass Pushed"),
    CUSTOM("Custom DJ")
}

data class DjFxConfig(
    val preset: DjPreset = DjPreset.NORMAL,
    val speed: Float = 1.0f,
    val semitones: Int = 0,
    val soundstageWidth: Float = 1.0f,
    val loFiFilterEnabled: Boolean = false,
    val bassBoostDb: Int = 0,
    val isEqualizerLinked: Boolean = true
)

enum class SonicArchetype(val title: String, val tagline: String, val badgeIcon: String) {
    NOCTURNAL_VOYAGER("Nocturnal Voyager", "Guided by late-night melodies and twilight acoustics", "🌙"),
    AURORA_PULSE("Aurora Pulse", "Awakened by morning beats and vibrant sunrise rhythms", "🌅"),
    DEEP_FOCUS_SCHOLAR("Deep Focus Scholar", "Immersed in sustained concentration and reflective soundscapes", "🧠"),
    ECLECTIC_EXPLORER("Eclectic Explorer", "Crosses genre boundaries with ceaseless curiosity", "🧭"),
    RHYTHMIC_ATHLETE("Rhythmic Athlete", "Fueled by high-energy pulse and relentless drive", "⚡"),
    ACOUSTIC_PURIST("Acoustic Purist", "Cherishes authentic instruments, warmth, and audiophile depth", "🎻"),
    CHRONO_DISCOVERER("Chrono Discoverer", "Unbroken daily listening streaks weaving music into life", "🔥"),
    SONIC_NOMAD("Sonic Nomad", "Harmonious sound tracking through every hour of the day", "🎧")
}

data class SonicDnaProfile(
    val archetype: SonicArchetype = SonicArchetype.SONIC_NOMAD,
    val dominantGenre: String = "All Genres",
    val genreDistribution: List<GenreShare> = emptyList(),
    val peakPeriod: String = "All Day",
    val varietyScorePercent: Int = 50,
    val energyScorePercent: Int = 50,
    val acousticScorePercent: Int = 50,
    val totalTracksPlayed: Int = 0,
    val estimatedMinutes: Long = 0L,
    val topArtists: List<String> = emptyList(),
    val streakDays: Int = 0,
    val gradientStartHex: String = "#4A1D96",
    val gradientEndHex: String = "#7C3AED"
)

enum class VibeTheme(val displayName: String, val tagline: String, val icon: String) {
    LATE_NIGHT_CHILL("Late-Night Lo-Fi", "Gentle downtempo & relaxing twilight atmosphere", "🌙"),
    HIGH_OCTANE_ENERGY("High-Octane Energy", "Kinetic beats, electronic drive & workout fuel", "⚡"),
    DEEP_FOCUS_STUDY("Deep Focus & Flow", "Immersive study, classical & minimal distraction", "🧠"),
    SUNSET_ACOUSTIC("Golden Hour Acoustic", "Warm analog strings, folk & authentic resonance", "🌅"),
    CLUB_EUPHORIA("Club Euphoria", "Heavy sub-bass, dance anthems & night rhythm", "🪩"),
    TIME_MACHINE_DEEP_CUTS("Nostalgia & Deep Cuts", "Rediscover forgotten favorites & timeless gems", "⏳"),
    DYNAMIC_ECLECTIC("Eclectic Taste Mix", "Curated surprise spanning across your music universe", "✨")
}

enum class EnergyCurve(val displayName: String, val description: String) {
    RAMP_UP("Ramp-Up", "Starts mellow and builds toward a high-energy climax"),
    PEAK_SUSTAINED("Sustained Peak", "High kinetic energy from first beat to last"),
    WAVE_DYNAMIC("Dynamic Waves", "Alternating crests and troughs of energy"),
    GENTLE_COOLDOWN("Gentle Cooldown", "Starts energetic and gradually winds down")
}

data class VibeMixRequest(
    val theme: VibeTheme = VibeTheme.LATE_NIGHT_CHILL,
    val targetDurationMinutes: Int = 30,
    val energyCurve: EnergyCurve = EnergyCurve.RAMP_UP,
    val includeLikedTracks: Boolean = true,
    val includeHistory: Boolean = true
)

data class VibeMixResult(
    val title: String = "Smart Vibe Mix",
    val theme: VibeTheme = VibeTheme.LATE_NIGHT_CHILL,
    val tracks: List<Track> = emptyList(),
    val totalDurationSeconds: Long = 0L,
    val formattedDuration: String = "0 min",
    val curve: EnergyCurve = EnergyCurve.RAMP_UP
)

enum class SmartRuleField(val displayName: String) {
    TITLE("Track Title"),
    ARTIST("Artist Name"),
    ALBUM("Album Title"),
    DURATION_SECONDS("Duration (seconds)"),
    IS_LIKED("Is Favorite"),
    IS_DOWNLOADED("Is Downloaded"),
    PLAY_COUNT("Play Count")
}

enum class SmartRuleOperator(val symbol: String, val displayName: String) {
    EQUALS("==", "Equals"),
    CONTAINS("contains", "Contains"),
    NOT_CONTAINS("!contains", "Does Not Contain"),
    GREATER_THAN(">", "Greater Than"),
    LESS_THAN("<", "Less Than"),
    STARTS_WITH("^=", "Starts With")
}

enum class SmartRuleMatch(val displayName: String) {
    ALL("Match ALL Rules (AND)"),
    ANY("Match ANY Rule (OR)")
}

enum class SmartRuleSort(val displayName: String) {
    DEFAULT("Default (Library Order)"),
    TITLE_ASC("Title (A-Z)"),
    ARTIST_ASC("Artist (A-Z)"),
    DURATION_ASC("Shortest First"),
    DURATION_DESC("Longest First"),
    PLAY_COUNT_DESC("Most Played First")
}

data class SmartPlaylistRule(
    val id: String = java.util.UUID.randomUUID().toString(),
    val field: SmartRuleField = SmartRuleField.PLAY_COUNT,
    val operator: SmartRuleOperator = SmartRuleOperator.GREATER_THAN,
    val value: String = "1"
)

data class SmartPlaylistDefinition(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val icon: String = "✨",
    val description: String = "",
    val matchType: SmartRuleMatch = SmartRuleMatch.ALL,
    val rules: List<SmartPlaylistRule> = emptyList(),
    val sortBy: SmartRuleSort = SmartRuleSort.DEFAULT,
    val limit: Int = 50,
    val isBuiltIn: Boolean = false
)

enum class CamelotScale(val letter: String) {
    MINOR("A"),
    MAJOR("B")
}

enum class CamelotKey(val number: Int, val scale: CamelotScale, val pitchName: String) {
    KEY_1A(1, CamelotScale.MINOR, "Ab Minor / G# Minor"),
    KEY_1B(1, CamelotScale.MAJOR, "B Major"),
    KEY_2A(2, CamelotScale.MINOR, "Eb Minor / D# Minor"),
    KEY_2B(2, CamelotScale.MAJOR, "F# Major / Gb Major"),
    KEY_3A(3, CamelotScale.MINOR, "Bb Minor / A# Minor"),
    KEY_3B(3, CamelotScale.MAJOR, "Db Major / C# Major"),
    KEY_4A(4, CamelotScale.MINOR, "F Minor"),
    KEY_4B(4, CamelotScale.MAJOR, "Ab Major"),
    KEY_5A(5, CamelotScale.MINOR, "C Minor"),
    KEY_5B(5, CamelotScale.MAJOR, "Eb Major"),
    KEY_6A(6, CamelotScale.MINOR, "G Minor"),
    KEY_6B(6, CamelotScale.MAJOR, "Bb Major"),
    KEY_7A(7, CamelotScale.MINOR, "D Minor"),
    KEY_7B(7, CamelotScale.MAJOR, "F Major"),
    KEY_8A(8, CamelotScale.MINOR, "A Minor"),
    KEY_8B(8, CamelotScale.MAJOR, "C Major"),
    KEY_9A(9, CamelotScale.MINOR, "E Minor"),
    KEY_9B(9, CamelotScale.MAJOR, "G Major"),
    KEY_10A(10, CamelotScale.MINOR, "B Minor"),
    KEY_10B(10, CamelotScale.MAJOR, "D Major"),
    KEY_11A(11, CamelotScale.MINOR, "F# Minor"),
    KEY_11B(11, CamelotScale.MAJOR, "A Major"),
    KEY_12A(12, CamelotScale.MINOR, "Db Minor / C# Minor"),
    KEY_12B(12, CamelotScale.MAJOR, "E Major");

    val code: String get() = "$number${scale.letter}"
}

enum class HarmonicRelationship(
    val title: String,
    val description: String,
    val baseCompatibilityPercent: Int,
    val colorHex: String
) {
    PERFECT_MATCH("Perfect Key Match", "Harmonically identical key", 100, "#10B981"),
    RELATIVE_KEY("Relative Shift", "Major/Minor emotional shift", 90, "#3B82F6"),
    ADJACENT_STEP("Harmonic Step (±1)", "Smooth dominant/subdominant shift", 85, "#8B5CF6"),
    ENERGY_BOOST("Energy Lift (+2)", "Euphoric harmonic lift", 80, "#F59E0B"),
    ENERGY_DROP("Mellow Cool-Down (-2)", "Subtle relaxation step", 75, "#06B6D4"),
    DISSONANT("Key Friction", "Harmonic clash / tonal friction", 25, "#EF4444")
}

enum class TempoCompatibility(val label: String, val isGoodMix: Boolean) {
    SEAMLESS("Seamless Tempo (±3 BPM)", true),
    SMOOTH("Smooth Tempo (±8 BPM)", true),
    HALF_DOUBLE("Half / Double Beat Sync", true),
    MODERATE_SHIFT("Moderate Shift (±15 BPM)", false),
    TEMPO_JUMP("Tempo Jump (>15 BPM)", false)
}

data class TrackHarmonicProfile(
    val trackId: String,
    val key: CamelotKey = CamelotKey.KEY_8A,
    val bpm: Int = 120,
    val energyLevel: Int = 75
) {
    val displayKey: String get() = key.code
    val fullLabel: String get() = "${key.code} • ${key.pitchName} • $bpm BPM"
}

data class HarmonicTransition(
    val sourceProfile: TrackHarmonicProfile,
    val targetProfile: TrackHarmonicProfile,
    val relationship: HarmonicRelationship,
    val compatibilityPercent: Int,
    val tempoCompatibility: TempoCompatibility,
    val recommendedCrossfadeSeconds: Int
)



