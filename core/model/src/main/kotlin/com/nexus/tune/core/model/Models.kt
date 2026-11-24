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
    val loudnessGainDb: Float = 0.0f,
    val description: String? = null
) {
    val durationMs: Long get() = durationSeconds * 1000L
}

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
    val format: String = "Opus", // Opus, AAC
    val bitrateKbps: Int = 160,
    val expiresAtTimestampMs: Long = 0L
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
    OFF, SPECTRUM_BARS, CIRCULAR_WAVE, GLOW_PULSE, STEREO_VU, LIQUID_WAVE
}

enum class VisualizerPalette(
    val displayName: String,
    val primaryHex: String,
    val secondaryHex: String
) {
    DYNAMIC("Dynamic Theme", "", ""),
    CYBERPUNK("Cyberpunk Neon", "#00F0FF", "#FF003C"),
    SUNSET_EMBER("Sunset Ember", "#FF5E3A", "#FF2A68"),
    AURORA("Aurora Borealis", "#00FFA3", "#00B8FF"),
    ELECTRIC_VIOLET("Electric Violet", "#A855F7", "#EC4899"),
    GOLDEN_CHAMPAGNE("Golden Spark", "#F59E0B", "#FBBF24")
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
    val harmonicMixingEnabled: Boolean = true,
    val visualizerPalette: VisualizerPalette = VisualizerPalette.DYNAMIC,
    val autoStashConfig: AutoStashConfig = AutoStashConfig(),
    val headphoneCorrectionConfig: HeadphoneCorrectionConfig = HeadphoneCorrectionConfig(),
    val stereoWidthConfig: StereoWidthConfig = StereoWidthConfig(),
    val sleepDriftProfile: SleepDriftProfile = SleepDriftProfile.OFF,
    val ditherMode: DitherMode = DitherMode.TRIANGULAR_TPDF,
    val dynamicLoudnessConfig: DynamicLoudnessConfig = DynamicLoudnessConfig(),
    val beatCrossfadeConfig: BeatCrossfadeConfig = BeatCrossfadeConfig(),
    val silenceTrimConfig: SilenceTrimConfig = SilenceTrimConfig(),
    val resamplingFilterConfig: ResamplingFilterConfig = ResamplingFilterConfig(),
    val phaseMeterConfig: PhaseMeterConfig = PhaseMeterConfig(),
    val tiltEqualizerConfig: TiltEqualizerConfig = TiltEqualizerConfig(),
    val deClickerConfig: DeClickerConfig = DeClickerConfig(),
    val midBassClarityConfig: MidBassClarityConfig = MidBassClarityConfig(),
    val crosstalkCancellationConfig: CrosstalkCancellationConfig = CrosstalkCancellationConfig(),
    val infrasonicFilterConfig: InfrasonicFilterConfig = InfrasonicFilterConfig(),
    val airBandExciterConfig: AirBandExciterConfig = AirBandExciterConfig(),
    val subHarmonicSynthConfig: SubHarmonicSynthConfig = SubHarmonicSynthConfig(),
    val deEsserConfig: DeEsserConfig = DeEsserConfig(),
    val truePeakGuardConfig: TruePeakGuardConfig = TruePeakGuardConfig(),
    val ultrasonicFilterConfig: UltrasonicFilterConfig = UltrasonicFilterConfig(),
    val tubeSaturationConfig: TubeSaturationConfig = TubeSaturationConfig(),
    val transientShaperConfig: TransientShaperConfig = TransientShaperConfig(),
    val dynamicStereoImagerConfig: DynamicStereoImagerConfig = DynamicStereoImagerConfig(),
    val tapeHysteresisConfig: TapeHysteresisConfig = TapeHysteresisConfig(),
    val mainsHumIsolatorConfig: MainsHumIsolatorConfig = MainsHumIsolatorConfig()
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

enum class VocalCutLevel(
    val label: String,
    val attenuationDb: Float,
    val description: String
) {
    OFF("Off", 0.0f, "Original stereo mix"),
    GENTLE("Gentle (-6 dB)", -6.0f, "Subtle vocal dip for sing-along"),
    MODERATE("Moderate (-12 dB)", -12.0f, "Standard karaoke vocal cut"),
    AGGRESSIVE("Strong (-18 dB)", -18.0f, "Heavy center-channel vocal reduction"),
    MAXIMUM("Full Isolation (-24 dB)", -24.0f, "Maximum vocal isolation / backing track focus")
}

enum class KaraokeReverb(val label: String, val strengthPercent: Int) {
    DRY("Off", 0),
    ROOM("Cozy Room", 25),
    STAGE("Live Stage", 50),
    CONCERT_HALL("Concert Hall", 85)
}

data class KaraokeConfig(
    val isEnabled: Boolean = false,
    val cutLevel: VocalCutLevel = VocalCutLevel.MODERATE,
    val pitchSemitones: Int = 0,
    val prompterMode: Boolean = false,
    val reverb: KaraokeReverb = KaraokeReverb.STAGE
) {
    val isActive: Boolean get() = isEnabled && cutLevel != VocalCutLevel.OFF
}

data class KaraokeLyricProgress(
    val currentLineIndex: Int = -1,
    val currentLine: LyricsLine? = null,
    val nextLine: LyricsLine? = null,
    val lineProgressFraction: Float = 0f,
    val timeRemainingInLineMs: Long = 0L,
    val isVocalActive: Boolean = false
)

enum class AutoStashStrategy(val displayName: String, val description: String) {
    BALANCED_HYBRID("Balanced Hybrid", "Mix of liked tracks, high rotation, and recent plays"),
    FAVORITES_PRIORITY("Favorites First", "Prioritizes all starred and liked library tracks"),
    HEAVY_ROTATION("Heavy Rotation", "Prioritizes songs with the highest playback frequency"),
    OFFLINE_ESSENTIALS("Max Track Stash", "Packs shorter tracks first to maximize offline song count")
}

enum class AutoStashNetworkPolicy(val label: String) {
    WIFI_ONLY("Wi-Fi Only"),
    ANY_NETWORK("Wi-Fi + Cellular")
}

data class AutoStashCandidate(
    val track: Track,
    val playCount: Int = 0,
    val isLiked: Boolean = false,
    val estimatedSizeBytes: Long = (track.durationSeconds * 20_000L).coerceAtLeast(1_000_000L), // ~160kbps Opus (~1.2MB/min)
    val priorityScore: Float = 0f,
    val reasonTag: String = "Candidate"
)

data class AutoStashConfig(
    val isEnabled: Boolean = false,
    val storageBudgetMb: Int = 1000, // 1 GB default
    val strategy: AutoStashStrategy = AutoStashStrategy.BALANCED_HYBRID,
    val wifiOnly: Boolean = true,
    val preserveStoragePercent: Int = 10
) {
    val budgetBytes: Long get() = storageBudgetMb * 1024L * 1024L
}

data class AutoStashReport(
    val stashedCount: Int = 0,
    val totalUsedBytes: Long = 0L,
    val budgetBytes: Long = 1000L * 1024L * 1024L,
    val estimatedOfflineHours: Float = 0f,
    val stashedTracks: List<Track> = emptyList(),
    val evictionTrackIds: Set<String> = emptySet(),
    val strategy: AutoStashStrategy = AutoStashStrategy.BALANCED_HYBRID
) {
    val usageRatio: Float get() = if (budgetBytes > 0L) (totalUsedBytes.toFloat() / budgetBytes.toFloat()).coerceIn(0f, 1f) else 0f
}

data class CleanMetadataResult(
    val originalTitle: String,
    val cleanedTitle: String,
    val originalArtist: String,
    val cleanedArtist: String,
    val originalAlbum: String? = null,
    val cleanedAlbum: String? = null,
    val featuredArtists: List<String> = emptyList(),
    val removedPatterns: List<String> = emptyList(),
    val isModified: Boolean = false
)

enum class HeadphoneTargetCurve(
    val displayName: String,
    val description: String,
    val tonalCharacter: String
) {
    HARMAN_OVER_EAR("Harman Over-Ear (2019)", "Industry standard acoustic target with tight sub-bass shelf and smooth pinna gain", "Warm & Balanced"),
    HARMAN_IN_EAR("Harman In-Ear", "Compensated for ear canal resonance with energetic vocal presence", "Engaging & Crisp"),
    DIFFUSE_FIELD("Diffuse Field", "Classic diffuse sound field simulation offering wide, bright staging", "Spacious & Bright"),
    STUDIO_NEUTRAL("Studio Neutral", "Ruler-flat monitor curve for mixing accuracy and authentic timbres", "Analytical Reference"),
    WARM_PUNCH("Warm & Punchy", "Rich lower-mid body with elevated punchy bass for acoustic warmth", "Cozy & Lush"),
    V_SHAPED_BASSHEAD("V-Shaped Energy", "Boosted sub-bass punch and elevated treble sparkle for EDM and pop", "Energetic & Fun"),
    TREBLE_AIR("Air & Micro-Detail", "Extended ultra-high frequency brilliance for acoustic string separation", "Airy & Transparent")
}

data class HeadphoneCorrectionConfig(
    val isEnabled: Boolean = false,
    val targetCurve: HeadphoneTargetCurve = HeadphoneTargetCurve.HARMAN_OVER_EAR,
    val crossfeedEnabled: Boolean = false,
    val crossfeedLevelPercent: Int = 30
)

enum class StereoWidthMode(val displayName: String, val description: String, val defaultWidthPercent: Int) {
    MONO("Mono (0%)", "Sums L+R channels into center mono for single speaker compatibility", 0),
    NARROW("Narrow (50%)", "Gently concentrates stereo soundstage toward center", 50),
    NATURAL("Natural (100%)", "Untouched original stereo master balance", 100),
    EXPANDED("Expanded (135%)", "Subtly expands horizontal stereo sweetspot across speakers/headphones", 135),
    SUPER_WIDE("Super-Wide (175%)", "Dramatic ultra-wide panoramic soundstage using Haas spatialization", 175),
    MID_VOCAL("Vocal Solo (Mid)", "Attenuates side audio to isolate lead vocals and center instruments", 10),
    SIDE_AMBIENT("Side Ambient", "Attenuates center vocals/drums to isolate reverb, synths, and stereo room pads", 200)
}

data class StereoWidthConfig(
    val isEnabled: Boolean = false,
    val mode: StereoWidthMode = StereoWidthMode.NATURAL,
    val customWidthPercent: Int = 100,
    val haasDelayMs: Float = 0.0f,
    val midLevelDb: Float = 0.0f,
    val sideLevelDb: Float = 0.0f
)

data class StereoPhaseRating(
    val correlation: Float,
    val rating: String,
    val isPhaseSafe: Boolean
)

data class TrackChapter(
    val index: Int,
    val title: String,
    val startTimeMs: Long,
    val endTimeMs: Long? = null,
    val durationFormatted: String = ""
)

data class ChapterProgress(
    val currentChapter: TrackChapter,
    val nextChapter: TrackChapter? = null,
    val progressFraction: Float = 0f,
    val elapsedInChapterMs: Long = 0L,
    val remainingInChapterMs: Long = 0L,
    val elapsedFormatted: String = "00:00",
    val remainingFormatted: String = "00:00"
)

enum class SleepDriftProfile(val displayName: String, val description: String) {
    OFF("Off (Standard)", "Standard volume fade without frequency or tempo modulation"),
    WARM_MELLOW("Warm Mellow", "Progressive high-frequency roll-off (-8dB) for a cozy, non-stimulating tone"),
    DEEP_DRIFT("Deep Drift", "Deep high-cut filter (-12dB) with subtle 10% tempo deceleration"),
    BINAURAL_THETA("Theta Binaural", "Warm acoustics with 6Hz Theta frequency differential for dream induction"),
    BINAURAL_DELTA("Delta Binaural", "Deep acoustic roll-off with 3Hz Delta wave differential for deep restorative sleep")
}

data class SleepDriftConfig(
    val profile: SleepDriftProfile = SleepDriftProfile.OFF,
    val tempoDecelerationEnabled: Boolean = true,
    val maxHighCutDbmB: Int = -1000
)

enum class DitherMode(val displayName: String, val description: String, val snrBonusDb: Float) {
    OFF("Off (Truncate)", "Direct integer truncation without dither", 0.0f),
    RECTANGULAR_RPDF("Rectangular (RPDF)", "Single uniform random noise (±0.5 LSB)", 1.5f),
    TRIANGULAR_TPDF("Triangular (TPDF)", "Industry reference dual-random dither with zero noise modulation", 3.0f),
    PSYCHOACOUSTIC_LIPSHITZ("Psychoacoustic Shaped", "F-weighted noise shaping pushing quantization error into inaudible high frequencies", 18.0f),
    HIGH_PASS_CONTOUR("High-Pass Contour", "High-pass filtered dither avoiding sub-bass interference", 6.0f)
}

data class DitherConfig(
    val mode: DitherMode = DitherMode.TRIANGULAR_TPDF,
    val targetBitDepth: Int = 16,
    val noiseShapingStrengthPercent: Int = 100
)

data class QuantizationNoiseMetrics(
    val theoreticalDynamicRangeDb: Float,
    val effectiveSnrDb: Float,
    val noiseShapingGainDb: Float
)

enum class DynamicLoudnessMode(val displayName: String, val description: String, val maxBassBoostDb: Float, val maxTrebleBoostDb: Float) {
    OFF("Off (Linear)", "Linear frequency response without dynamic volume compensation", 0.0f, 0.0f),
    ISO_226_REFERENCE("ISO 226 Reference", "Calibrated ISO 226 equal-loudness curve restoring low-volume bass and treble", 8.0f, 4.5f),
    SUBTLE_CONTOUR("Subtle Contour", "Gentle audiophile contour preserving pristine acoustic neutrality", 4.5f, 2.5f),
    DEEP_PUNCH("Deep Punch", "Enhanced low-frequency fullness for low-volume headphone and speaker listening", 10.0f, 5.5f),
    NIGHT_WHISPER("Night Whisper", "Intense low-volume compensation tailored for whisper-quiet bedtime playback", 12.0f, 7.0f)
}

data class DynamicLoudnessConfig(
    val isEnabled: Boolean = false,
    val mode: DynamicLoudnessMode = DynamicLoudnessMode.ISO_226_REFERENCE,
    val intensityPercent: Int = 100
)

data class LoudnessCurveOffsets(
    val bandOffsetsMb: List<Int>,
    val effectiveBassGainDb: Float,
    val effectiveTrebleGainDb: Float
)

enum class BeatCrossfadeMode(val displayName: String, val description: String, val defaultBars: Int) {
    OFF("Standard Time", "Standard fixed-second volume crossfade", 0),
    SMART_BEAT_SYNC("Smart Beat-Sync", "Dynamic 4-bar or 8-bar tempo-synchronized phrase alignment", 4),
    PHRASE_ALIGNED_8BAR("8-Bar Phrase Mix", "Strict 32-beat broadcast-style phrase blend", 8),
    PHRASE_ALIGNED_16BAR("16-Bar Club Mix", "Extended 64-beat seamless dancefloor phrase transition", 16),
    BASS_SWAP_CLUB("Bass-Swap Club", "High-energy transition with early low-end frequency swap", 8)
}

data class BeatCrossfadeConfig(
    val isEnabled: Boolean = false,
    val mode: BeatCrossfadeMode = BeatCrossfadeMode.SMART_BEAT_SYNC,
    val maxTempoBendPercent: Int = 6
)

data class BassSwapCurve(
    val outgoingBassGainDb: Float,
    val incomingBassGainDb: Float,
    val outgoingMidHighGainDb: Float,
    val incomingMidHighGainDb: Float
)

data class BeatTransitionPlan(
    val outgoingBpm: Int,
    val incomingBpm: Int,
    val targetDurationMs: Long,
    val musicalBars: Int,
    val totalBeats: Int,
    val tempoRatio: Float,
    val requiresTempoBend: Boolean,
    val effectiveRampBpm: Int
)

enum class SilenceTrimMode(val displayName: String, val description: String, val defaultThresholdDb: Float, val defaultMinDurationMs: Long) {
    OFF("Off (Passthrough)", "Preserve all silence and pauses unchanged", -60.0f, 2000L),
    NATURAL_MUSIC("Natural Music", "Preserves musical pauses up to 1.5s; removes dead air at boundaries", -45.0f, 1500L),
    FAST_TALK("Fast Talk & Podcast", "Aggressively trims silences >350ms for concise speech listening", -36.0f, 350L),
    SMART_DYNAMIC("Smart Adaptive", "Dynamically adjusts threshold based on track background floor", -42.0f, 600L),
    EXTENDED_OUTRO("Outro & Dead-Air", "Exclusively cuts long dead air and hidden track gaps >2s", -50.0f, 2000L)
}

data class SilenceTrimConfig(
    val isEnabled: Boolean = false,
    val mode: SilenceTrimMode = SilenceTrimMode.NATURAL_MUSIC,
    val thresholdDb: Float = -45.0f,
    val minDurationMs: Long = 1500L,
    val smoothEnvelopeMs: Int = 15
)

data class SilenceDetectionResult(
    val isSilent: Boolean,
    val rmsDb: Float,
    val silenceDurationMs: Long,
    val shouldSkip: Boolean
)

enum class ResamplingFilterMode(val displayName: String, val description: String, val stopbandRejectionDb: Float, val preRingingPercent: Int) {
    STANDARD_SHARP("Sharp Linear", "Steep cutoff brickwall filter with symmetric impulse response", 120.0f, 50),
    MINIMUM_PHASE_PUNCH("Minimum Phase", "Zero pre-ringing filter with immediate transient attack and natural decay", 110.0f, 0),
    LINEAR_PHASE_REFERENCE("Linear Phase", "Reference filter with linear group delay and perfect soundstage imaging", 130.0f, 50),
    SLOW_ROLL_OFF_WARM("Slow Roll-Off", "Gentle high-frequency roll-off with tube-like warm analog timbre", 90.0f, 15),
    APODIZING_ULTRASONIC("Apodizing Filter", "Rejection filter eliminating ADC pre-echo from compressed master recordings", 125.0f, 25)
}

data class ResamplingFilterConfig(
    val mode: ResamplingFilterMode = ResamplingFilterMode.MINIMUM_PHASE_PUNCH,
    val cutoffRatio: Float = 0.95f
)

data class FilterImpulseMetrics(
    val preRingingPercent: Int,
    val postRingingPercent: Int,
    val stopbandRejectionDb: Float,
    val phaseLinearityRating: String
)

enum class PhaseCorrelationRating(val displayName: String, val description: String, val isMonoSafe: Boolean) {
    PERFECT_MONO("Pure Mono (+1.0)", "Identical left and right channels; 100% mono compliant", true),
    EXCELLENT_STEREO("Excellent Stereo (+0.6 to +0.9)", "Balanced stereo field with healthy mono acoustic power", true),
    ACCEPTABLE_WIDE("Wide Stereo (+0.2 to +0.5)", "Spacious soundstage; minor volume loss on mono speakers", true),
    PHASE_WARNING("Phase Warning (-0.2 to +0.2)", "Significant out-of-phase ambiance; noticeable mono cancellation", false),
    CRITICAL_CANCELLATION("Critical Inversion (-1.0 to -0.2)", "Opposite phase channels; core audio disappears when summed to mono", false)
}

enum class MonoGuardMode(val displayName: String, val description: String, val threshold: Float) {
    OFF("Off (Unrestricted)", "No automatic phase correction or mono guard", -1.0f),
    INTELLIGENT_SAFEGUARD("Smart Safeguard", "Progressive side-channel compression when correlation drops below +0.2", 0.20f),
    STRICT_MONO_COMPLIANCE("Strict Broadcast", "Maintains minimum +0.50 correlation for flawless speaker fidelity", 0.50f)
}

data class PhaseMeterConfig(
    val isEnabled: Boolean = false,
    val guardMode: MonoGuardMode = MonoGuardMode.INTELLIGENT_SAFEGUARD,
    val minCorrelationThreshold: Float = 0.20f
)

data class PhaseCorrelationMetrics(
    val correlationCoeff: Float,
    val rating: PhaseCorrelationRating,
    val sideAttenuationDb: Float,
    val isMonoSafe: Boolean
)

enum class TiltPivotFrequency(val displayName: String, val pivotHz: Float) {
    HZ_630("630 Hz (Vocal Center)", 630.0f),
    HZ_1000("1.0 kHz (Standard)", 1000.0f),
    HZ_1600("1.6 kHz (Upper Mid)", 1600.0f)
}

data class TiltEqualizerConfig(
    val isEnabled: Boolean = false,
    val tiltDb: Float = 0.0f,
    val pivotHz: Float = 1000.0f,
    val loudnessCompensate: Boolean = true
)

data class TiltSpectralMetrics(
    val bassGainDb: Float,
    val trebleGainDb: Float,
    val pivotHz: Float,
    val apparentLoudnessOffsetDb: Float,
    val tonalBalanceDescription: String
)

data class TiltBiquadCoefficients(
    val b0: Float,
    val b1: Float,
    val b2: Float,
    val a1: Float,
    val a2: Float
)

enum class DeClickerMode(val displayName: String, val description: String, val defaultSensitivity: Float) {
    OFF("Off (Bypass)", "Disable click detection and repair", 0.0f),
    TRANSPARENT_STREAM("Stream Guard", "Repairs stream buffer glitches and packet ticks without touching transients", 6.0f),
    VINYL_RESTORATION("Vinyl & Analog", "Removes vinyl surface dust, pops, and tape clicks", 3.5f),
    HIGH_SENSITIVITY("Max Suppression", "Aggressively repairs all detected sample discontinuities", 2.0f)
}

data class DeClickerConfig(
    val isEnabled: Boolean = false,
    val mode: DeClickerMode = DeClickerMode.TRANSPARENT_STREAM,
    val sensitivityMultiplier: Float = 3.5f,
    val maxClickWidthSamples: Int = 8
)

data class DeClickerMetrics(
    val clicksRepaired: Int,
    val peakClickReductionDb: Float,
    val currentNoiseFloorRms: Float,
    val statusSummary: String
)

enum class MidBassClarityMode(val displayName: String, val description: String, val maxDipDb: Float, val defaultThresholdRatio: Float) {
    OFF("Off (Bypass)", "Disable dynamic mid-bass de-mudding", 0.0f, 0.0f),
    SUBTLE_CLARITY("Subtle Clarity", "Gentle -2.5 dB maximum dip for acoustic, jazz, and classical", -2.5f, 0.35f),
    BALANCED_DE_MUD("Balanced De-Mud", "Optimal -4.5 dB dynamic dip for pop, rock, and hip-hop", -4.5f, 0.45f),
    AGGRESSIVE_DECONGEST("Max Decongest", "Steep -6.0 dB cut for heavily crowded mixes and bassy headphones", -6.0f, 0.55f)
}

data class MidBassClarityConfig(
    val isEnabled: Boolean = false,
    val mode: MidBassClarityMode = MidBassClarityMode.BALANCED_DE_MUD,
    val centerFrequencyHz: Float = 300.0f,
    val dynamicThresholdPercent: Int = 40
)

data class MidBassClarityMetrics(
    val congestionRatio: Float,
    val dynamicAttenuationDb: Float,
    val isDecongesting: Boolean,
    val clarityRating: String
)

data class MidBassBiquadCoefficients(
    val b0: Float,
    val b1: Float,
    val b2: Float,
    val a1: Float,
    val a2: Float
)

enum class CrosstalkCancellationMode(
    val displayName: String,
    val description: String,
    val defaultDelaySamples: Int,
    val defaultIntensityFactor: Float,
    val targetSpanDegrees: Int
) {
    OFF("Off (Bypass)", "Direct stereo without crosstalk cancellation", 0, 0.0f, 30),
    PHONE_STEREO_SPEAKERS("Phone Dual Speakers", "Calibrated for 7-8 cm mobile chassis spacing (120 µs delay)", 5, 0.65f, 140),
    DESKTOP_NEARFIELD("Desktop & Tablet", "Optimized for laptop/tablet near-field speakers (220 µs delay)", 9, 0.75f, 160),
    SOUNDBAR_LIVING_ROOM("Living Room Soundbar", "Optimized for external Bluetooth soundbar & room hi-fi (340 µs delay)", 14, 0.82f, 175)
}

data class CrosstalkCancellationConfig(
    val isEnabled: Boolean = false,
    val mode: CrosstalkCancellationMode = CrosstalkCancellationMode.PHONE_STEREO_SPEAKERS,
    val delaySamples: Int = 5,
    val cancellationIntensityPercent: Int = 70,
    val headShadowDamping: Float = 0.45f
)

data class CrosstalkCancellationMetrics(
    val delayMicroseconds: Float,
    val effectiveCancellationDb: Float,
    val virtualStageSpanDegrees: Int,
    val binauralRating: String
)

enum class InfrasonicSlope(val displayName: String, val order: Int, val description: String, val dbPerOctave: Int) {
    SLOPE_12DB_OCTAVE("12 dB/oct (Gentle)", 2, "2nd-order high-pass preserving sub-bass tail", 12),
    SLOPE_18DB_OCTAVE("18 dB/oct (Butterworth)", 3, "3rd-order maximally flat passband with steep cutoff", 18),
    SLOPE_24DB_OCTAVE("24 dB/oct (Warp Brickwall)", 4, "4th-order steep high-pass eliminating vinyl warp", 24)
}

data class InfrasonicFilterConfig(
    val isEnabled: Boolean = false,
    val cutoffHz: Float = 20.0f,
    val slope: InfrasonicSlope = InfrasonicSlope.SLOPE_18DB_OCTAVE,
    val dcBlockerEnabled: Boolean = true
)

data class InfrasonicMetrics(
    val cutoffHz: Float,
    val preservedHeadroomDb: Float,
    val rumbleAttenuationAt10HzDb: Float,
    val protectionRating: String
)

data class InfrasonicBiquadCoefficients(
    val b0: Float,
    val b1: Float,
    val b2: Float,
    val a1: Float,
    val a2: Float
)

enum class AirBandFrequency(val frequencyHz: Int, val displayName: String, val description: String) {
    HZ_12000(12000, "12 kHz", "Vocal breath, acoustic guitar pick transients, and snare wire sizzle"),
    HZ_16000(16000, "16 kHz", "Pultec-style high air shelf, open cymbal sheen without harsh sibilance"),
    HZ_20000(20000, "20 kHz", "Maag AIR BAND® legendary pristine dimension and holographic 3D space"),
    HZ_24000(24000, "24 kHz", "Ultra-sonic sheen shelf, psychoacoustic air blooming across the soundstage")
}

enum class AirBandMode(val displayName: String, val boostDb: Float, val harmonicPercent: Int, val description: String) {
    OFF("Off (Bypassed)", 0.0f, 0, "Flat passband without high-frequency excitation"),
    SUBTLE_SILK("Subtle Silk (+1.5 dB)", 1.5f, 20, "Gentle high shelf with subtle even-harmonic warmth"),
    VOCAL_BREATH("Vocal Breath (+3.0 dB)", 3.0f, 35, "Silky acoustic presence and vocal intelligibility"),
    MASTERING_AIR("Mastering Air (+4.5 dB)", 4.5f, 50, "Pristine studio polish, opening up cramped lossy streams"),
    LOSSLESS_SPARKLE("Lossless Sparkle (+6.0 dB)", 6.0f, 75, "Euphoric top-end radiance and lively micro-detail transient bloom")
}

data class AirBandExciterConfig(
    val isEnabled: Boolean = false,
    val mode: AirBandMode = AirBandMode.OFF,
    val frequency: AirBandFrequency = AirBandFrequency.HZ_16000,
    val boostDb: Float = 3.0f,
    val harmonicExcitationPercent: Int = 30
)

data class AirBandMetrics(
    val frequencyHz: Int,
    val boostDb: Float,
    val harmonicDistortionPercent: Float,
    val airEnergyIndexPercent: Int,
    val sheenDescriptor: String
)

data class AirBandBiquadCoefficients(
    val b0: Float,
    val b1: Float,
    val b2: Float,
    val a1: Float,
    val a2: Float
)

enum class SubHarmonicTarget(val displayName: String, val description: String) {
    SMALL_SPEAKERS_EARBUDS("Earbuds & Phone Speakers", "Synthesizes 2nd/3rd harmonics (80-200 Hz) to create phantom deep bass without cone blowout"),
    FULL_RANGE_SUBWOOFER("Subwoofers & Planar Drivers", "Generates true octave-lower sub-bass (30-60 Hz) for visceral chest-thumping depth"),
    HYBRID_BALANCED("Hybrid Audiophile Balanced", "Blends missing fundamental upper harmonics with gentle sub-octave reinforcement")
}

enum class SubHarmonicMode(
    val displayName: String,
    val target: SubHarmonicTarget,
    val intensityPercent: Int,
    val subOctaveBlendPercent: Int,
    val description: String
) {
    OFF("Off (Bypassed)", SubHarmonicTarget.HYBRID_BALANCED, 0, 0, "Unaltered passband bass response"),
    EARBUD_MAX_PUNCH("Earbud Max Punch", SubHarmonicTarget.SMALL_SPEAKERS_EARBUDS, 70, 15, "MaxxBass-style missing fundamental for compact drivers"),
    DESKTOP_BODY("Desktop Warmth & Body", SubHarmonicTarget.HYBRID_BALANCED, 55, 35, "Rich acoustic body and warm lower mids for bookshelf monitors"),
    SUBWOOFER_SHAKE("Subwoofer Floor Shake", SubHarmonicTarget.FULL_RANGE_SUBWOOFER, 80, 75, "Octave divider synthesis delivering tactile 30-50 Hz sub-rumble"),
    CLUB_EARTHQUAKE("Club Earthquake", SubHarmonicTarget.FULL_RANGE_SUBWOOFER, 95, 90, "Massive dual-tier sub-harmonic excitation for dance & electronic music")
}

data class SubHarmonicSynthConfig(
    val isEnabled: Boolean = false,
    val mode: SubHarmonicMode = SubHarmonicMode.OFF,
    val target: SubHarmonicTarget = SubHarmonicTarget.HYBRID_BALANCED,
    val intensityPercent: Int = 50,
    val subOctaveBlendPercent: Int = 40
)

data class SubHarmonicMetrics(
    val perceivedBassGainDb: Float,
    val syntheticBandwidthHz: String,
    val phantomFundamentalRating: String,
    val excursionSafeguardActive: Boolean
)

enum class DeEsserMode(
    val displayName: String,
    val centerFrequencyHz: Float,
    val thresholdPercent: Int,
    val maxReductionDb: Float,
    val description: String
) {
    OFF("Off (Bypassed)", 6000.0f, 30, 0.0f, "Unaltered sibilant passband"),
    SUBTLE_SMOOTH("Subtle Smooth (-2.5 dB)", 6500.0f, 35, 2.5f, "Gentle softening of sharp consonants while preserving air"),
    VOCAL_INTIMATE("Vocal Intimate (-4.5 dB)", 6000.0f, 28, 4.5f, "Studio-grade split-band ducking for close-mic vocal tracks"),
    BRIGHT_HEADPHONE_TAMER("Bright Headphone Tamer (-6.0 dB)", 7000.0f, 22, 6.0f, "Attenuates harsh 6-8 kHz ear-canal resonances and driver peaks"),
    AGGRESSIVE_SPLIT_BAND("Aggressive Split-Band (-8.0 dB)", 5500.0f, 18, 8.0f, "Heavy sibilance clamp for highly compressed or sibilant recordings")
}

data class DeEsserConfig(
    val isEnabled: Boolean = false,
    val mode: DeEsserMode = DeEsserMode.OFF,
    val centerFrequencyHz: Float = 6000.0f,
    val thresholdPercent: Int = 28,
    val maxReductionDb: Float = 4.5f
)

data class DeEsserMetrics(
    val dynamicReductionDb: Float,
    val sibilanceActivityPercent: Int,
    val vocalSmoothnessRating: String,
    val fatigueReductionPercent: Int
)

data class DeEsserBiquadCoefficients(
    val b0: Float,
    val b1: Float,
    val b2: Float,
    val a1: Float,
    val a2: Float
)

enum class TruePeakCeiling(val ceilingDbTp: Float, val displayName: String, val description: String) {
    CEILING_0_DBTP(0.0f, "0.0 dBTP", "Strict digital full scale brickwall ceiling"),
    CEILING_MINUS_0_5_DBTP(-0.5f, "-0.5 dBTP", "Audiophile pristine headroom avoiding inter-sample overshoots"),
    CEILING_MINUS_1_0_DBTP(-1.0f, "-1.0 dBTP", "EBU R128 / ITU-R BS.1770 broadcast standard headroom"),
    CEILING_MINUS_2_0_DBTP(-2.0f, "-2.0 dBTP", "Extra safety margin preventing secondary Bluetooth lossy codec clipping")
}

enum class TruePeakMode(
    val displayName: String,
    val ceiling: TruePeakCeiling,
    val oversampling4x: Boolean,
    val description: String
) {
    OFF("Off (Bypassed)", TruePeakCeiling.CEILING_0_DBTP, false, "Standard discrete sample playback without ISP protection"),
    EBU_R128_STANDARD("EBU R128 Standard (-1.0 dBTP)", TruePeakCeiling.CEILING_MINUS_1_0_DBTP, true, "Broadcast standard True Peak ceiling with 4x oversampled detection"),
    AUDIOPHILE_PRISTINE("Audiophile Pristine (-0.5 dBTP)", TruePeakCeiling.CEILING_MINUS_0_5_DBTP, true, "Transparent inter-sample peak prevention preserving maximum dynamic range"),
    HIGH_PRECISION_MARGIN("Bluetooth Codec Shield (-2.0 dBTP)", TruePeakCeiling.CEILING_MINUS_2_0_DBTP, true, "Prevents lossy AAC/SBC encoder clipping and inter-sample artifacts"),
    AGGRESSIVE_PEAK_TRAP("Brickwall True Peak Trap (0.0 dBTP)", TruePeakCeiling.CEILING_0_DBTP, true, "Clamps any continuous analog reconstruction excursion above 0 dBFS")
}

data class TruePeakGuardConfig(
    val isEnabled: Boolean = false,
    val mode: TruePeakMode = TruePeakMode.OFF,
    val ceiling: TruePeakCeiling = TruePeakCeiling.CEILING_MINUS_0_5_DBTP,
    val oversample4xEnabled: Boolean = true
)

data class TruePeakMetrics(
    val maxEstimatedTruePeakDbTp: Float,
    val peakSampleDbFs: Float,
    val interSampleOvershootDb: Float,
    val appliedGuardAttenuationDb: Float,
    val dacHeadroomRating: String
)

enum class UltrasonicTopology(val displayName: String, val description: String) {
    LINEAR_PHASE_FIR("Linear Phase (Brickwall)", "Steep >96 dB stopband rejection preserving linear group delay across the audible band"),
    MINIMUM_PHASE_IIR("Minimum Phase (Zero Pre-Ring)", "Eliminates all pre-ringing artifacts for crisp transient attack punch"),
    BESSEL_CONSTANT_DELAY("Bessel (Flat Group Delay)", "Maximally flat group delay preserving micro-spatial imaging across high treble"),
    APODIZING_RING_KILLER("Apodizing (Anti-Ringing)", "Attenuates pre-ringing baked into legacy digital recordings and DAC interpolation filters")
}

enum class UltrasonicMode(
    val displayName: String,
    val topology: UltrasonicTopology,
    val cutoffHz: Float,
    val description: String
) {
    OFF("Off (Bypassed)", UltrasonicTopology.LINEAR_PHASE_FIR, 48000.0f, "Full bandwidth passband without ultrasonic attenuation"),
    AUDIOPHILE_BRICKWALL_22K("CD Nyquist Brickwall (22.05 kHz)", UltrasonicTopology.LINEAR_PHASE_FIR, 22050.0f, "Standard 22.05 kHz filter rejecting all ultrasonic digital imaging foldback"),
    HI_RES_TAMER_24K("Hi-Res IMD Shield (24.0 kHz)", UltrasonicTopology.MINIMUM_PHASE_IIR, 24000.0f, "Blocks supersonic energy above 24 kHz to eliminate analog amplifier IMD hash"),
    NATURAL_TRANSIENT_BESSEL("Natural Transient Bessel (28.0 kHz)", UltrasonicTopology.BESSEL_CONSTANT_DELAY, 28000.0f, "Gentle, organic rolloff with zero phase distortion for acoustic recordings"),
    APODIZING_RING_KILLER("Apodizing Ring Killer (20.0 kHz)", UltrasonicTopology.APODIZING_RING_KILLER, 20000.0f, "Suppresses pre-echo ringing right at the 20 kHz threshold")
}

data class UltrasonicFilterConfig(
    val isEnabled: Boolean = false,
    val mode: UltrasonicMode = UltrasonicMode.OFF,
    val topology: UltrasonicTopology = UltrasonicTopology.LINEAR_PHASE_FIR,
    val cutoffHz: Float = 22050.0f
)

data class UltrasonicMetrics(
    val cutoffHz: Float,
    val stopbandRejectionDb: Float,
    val groupDelayMicros: Float,
    val preRingingSuppression: String,
    val imdShieldRating: String
)

data class UltrasonicBiquadCoefficients(
    val b0: Float,
    val b1: Float,
    val b2: Float,
    val a1: Float,
    val a2: Float
)

enum class TubeTopology(val displayName: String, val description: String) {
    TRIODE_12AX7("Triode (12AX7 / ECC83)", "Asymmetric warm soft-clipping dominated by lush 2nd-order even harmonics"),
    PENTODE_EL34("Pentode (EL34 / 6L6)", "Dynamic push-pull saturation with punchy 3rd-order presence and bite"),
    WARM_TAPE_SATURATION("Warm Tape Core", "Magnetic hysteresis tape compression with mellow top-end smoothing"),
    CLEAN_ANALOG_CONSOLE("Analog Console (Class A)", "Ultra-subtle discrete operational amplifier warmth and air")
}

enum class TubeWarmthProfile(
    val displayName: String,
    val topology: TubeTopology,
    val drive: Float,
    val bias: Float,
    val description: String
) {
    OFF("Off (Bypassed)", TubeTopology.TRIODE_12AX7, 0.0f, 0.0f, "Pure transparent digital stream without analog coloration"),
    LUSH_TRIODE_WARMTH("Lush Triode Warmth", TubeTopology.TRIODE_12AX7, 0.35f, 0.20f, "Classic audiophile tube amp warmth enriching acoustic vocals and bass"),
    VINTAGE_TUBE_HOT("Hot Driven Pentode", TubeTopology.PENTODE_EL34, 0.60f, -0.15f, "Aggressive harmonic bloom with rich dynamic punch and presence"),
    MAGNETIC_TAPE_GLUE("Analog Tape Glue", TubeTopology.WARM_TAPE_SATURATION, 0.40f, 0.05f, "Subtle tape saturation gently gluing transients and rounding peaks"),
    SUBTLE_CONSOLE_AIR("Class A Console Air", TubeTopology.CLEAN_ANALOG_CONSOLE, 0.20f, 0.10f, "Delicate high-headroom analog console character for pristine mastering")
}

data class TubeSaturationConfig(
    val isEnabled: Boolean = false,
    val profile: TubeWarmthProfile = TubeWarmthProfile.OFF,
    val topology: TubeTopology = TubeTopology.TRIODE_12AX7,
    val drive: Float = 0.35f,
    val bias: Float = 0.20f,
    val autoMakeupGain: Boolean = true
)

data class TubeSaturationMetrics(
    val secondHarmonicDb: Float,
    val thirdHarmonicDb: Float,
    val totalHarmonicDistortionPercent: Float,
    val warmthScore: Int,
    val headroomCompensationDb: Float,
    val characterRating: String
)

enum class TransientBandFocus(val displayName: String, val description: String) {
    FULL_BAND("Full Spectrum (Broadband)", "Applies dynamic transient attack and sustain shaping across all audio frequencies"),
    BASS_PUNCH_LOW("Low-End Kick & Bass (60-350 Hz)", "Focuses punch and velocity on bass drums, synth sub, and 808 impacts"),
    MID_ATTACK_SNAP("Mid-Range Snap (1-4 kHz)", "Enhances snare crack, guitar pick plucks, and vocal attack presence"),
    HIGH_PLUCK_SHINE("High-End Crisp (>5 kHz)", "Isolates delicate hi-hat taps, acoustic guitar shimmer, and transient air")
}

enum class TransientPreset(
    val displayName: String,
    val attack: Float,
    val sustain: Float,
    val focus: TransientBandFocus,
    val description: String
) {
    OFF("Off (Bypassed)", 0.0f, 0.0f, TransientBandFocus.FULL_BAND, "Unmodified original recorded dynamic envelope"),
    CRISP_DRUM_PUNCH("Crisp Drum Punch", 0.45f, -0.15f, TransientBandFocus.FULL_BAND, "Amplifies kick/snare attack while tightening muddy room decay"),
    BASS_IMPACT_BOOST("Visceral Kick & 808 Slam", 0.60f, 0.20f, TransientBandFocus.BASS_PUNCH_LOW, "Maximum low-end transient energy for modern hip-hop and electronic beats"),
    ACOUSTIC_PICK_ARTICULATE("Articulate String & Pluck", 0.35f, 0.10f, TransientBandFocus.MID_ATTACK_SNAP, "Brings forward acoustic guitar fingerpicking and piano hammer strikes"),
    MELLOW_FATIGUE_SOFTENER("Mellow Transient Softener", -0.40f, 0.25f, TransientBandFocus.FULL_BAND, "Smooths harsh transients and extends body sustain for relaxing late-night sessions"),
    DRY_STUDIO_TIGHTENER("Ultra-Dry Studio Tightener", 0.20f, -0.50f, TransientBandFocus.FULL_BAND, "Substantially clamps sustain tails for surgical punch and zero boomy resonance")
}

data class TransientShaperConfig(
    val isEnabled: Boolean = false,
    val preset: TransientPreset = TransientPreset.OFF,
    val bandFocus: TransientBandFocus = TransientBandFocus.FULL_BAND,
    val attackBoost: Float = 0.0f,
    val sustainBoost: Float = 0.0f,
    val autoOutputLimit: Boolean = true
)

data class TransientShaperMetrics(
    val attackDeltaDb: Float,
    val sustainDeltaDb: Float,
    val crestFactorDb: Float,
    val dynamicPunchScore: Int,
    val punchRating: String
)

enum class StereoImagerPreset(
    val displayName: String,
    val stereoWidth: Float,
    val monoBassCutoffHz: Float,
    val highBandAirSpread: Float,
    val description: String
) {
    OFF("Off (Bypassed)", 1.0f, 0.0f, 0.0f, "Standard untouched dual-channel stereo stream"),
    WIDE_CONCERT_HALL("Wide Concert Soundstage", 1.45f, 160.0f, 0.50f, "Expands perceived stage width with centered mono kick/bass and natural hall reflections"),
    HEADPHONE_SPATIAL_SPHERE("Headphone Binaural Out-of-Head", 1.60f, 200.0f, 0.70f, "Projects soundstage far outside the ear canals using high-frequency decorrelation"),
    INTIMATE_VOCAL_ANCHOR("Vocal Center Anchor", 1.25f, 140.0f, 0.30f, "Locks lead vocal tightly to the center while expanding backing instruments laterally"),
    MONO_BASS_FOCUS("Solid Mono-Bass Sub Slam", 1.10f, 250.0f, 0.15f, "Collapses low frequencies to mono for maximum headroom and zero bass phase cancelation"),
    DIFFUSE_AMBIENCE_GLOW("Diffuse Ambient Glow", 1.80f, 180.0f, 0.85f, "Ultra-wide ambient immersion ideal for synthwave, chillout, and cinematic scores")
}

data class DynamicStereoImagerConfig(
    val isEnabled: Boolean = false,
    val preset: StereoImagerPreset = StereoImagerPreset.OFF,
    val stereoWidth: Float = 1.0f,
    val monoBassCutoffHz: Float = 160.0f,
    val highBandAirSpread: Float = 0.40f,
    val monoGuardActive: Boolean = true
)

data class StereoImagerMetrics(
    val correlationCoefficient: Float,
    val perceivedWidthPercent: Int,
    val monoBassCutoffHz: Float,
    val monoCompatibilityScore: Int,
    val spatialDepthRating: String
)

enum class TapeSpeed(val ips: Float, val displayName: String, val description: String) {
    IPS_30(30.0f, "30 IPS (Mastering Reel)", "Pristine transient response, ultra-low noise, and flat extension beyond 22 kHz"),
    IPS_15(15.0f, "15 IPS (Studio Standard)", "Classic golden-age rock & pop tape body with robust low-end head bump and warm saturation"),
    IPS_7_5(7.5f, "7.5 IPS (Vintage Cassette)", "Nostalgic warm tape compression with mellow high-frequency rolloff above 14 kHz")
}

enum class TapePreset(
    val displayName: String,
    val speed: TapeSpeed,
    val drive: Float,
    val headBumpGainDb: Float,
    val wowFlutter: Float,
    val description: String
) {
    OFF("Off (Bypassed)", TapeSpeed.IPS_30, 0.0f, 0.0f, 0.0f, "Direct pristine digital stream without magnetic coloration"),
    MASTER_TAPE_30_IPS("Studer A800 30-IPS Master", TapeSpeed.IPS_30, 0.25f, 1.2f, 0.08f, "Subtle mastering glue with transparent transient roundoff and punch"),
    VINTAGE_WARM_15_IPS("Ampex ATR-102 15-IPS Warmth", TapeSpeed.IPS_15, 0.45f, 2.5f, 0.25f, "Lush analog body, authoritative 55 Hz bass weight, and organic magnetic saturation"),
    HOT_SATURATED_TAPE("Hot Driven Ferric Tape", TapeSpeed.IPS_15, 0.70f, 3.2f, 0.35f, "Heavy magnetic saturation with soft peak compression and harmonic glue"),
    LO_FI_CASSETTE_7_5("Vintage Cassette Tape Glow", TapeSpeed.IPS_7_5, 0.50f, 1.8f, 0.55f, "Gentle high-end rolloff, mechanical capstan wow, and nostalgic warble"),
    SUB_BASS_HEAD_BUMP("Acoustic Head Bump Slam", TapeSpeed.IPS_15, 0.30f, 4.0f, 0.12f, "Focused sub-bass resonance giving massive low-end weight to drums and bass")
}

data class TapeHysteresisConfig(
    val isEnabled: Boolean = false,
    val preset: TapePreset = TapePreset.OFF,
    val speed: TapeSpeed = TapeSpeed.IPS_15,
    val tapeDrive: Float = 0.35f,
    val headBumpGainDb: Float = 2.0f,
    val wowFlutterDepth: Float = 0.20f,
    val tapeHissReduction: Boolean = true
)

data class TapeHysteresisMetrics(
    val saturationCompressionDb: Float,
    val headBumpFrequencyHz: Float,
    val wowFlutterPpm: Int,
    val analogWarmthScore: Int,
    val tapeCharacterRating: String
)

enum class MainsFrequency(val fundamentalHz: Float, val displayName: String, val regionDescription: String) {
    HZ_50(50.0f, "50 Hz Mains (EU / Asia / UK)", "Standard AC grid frequency across Europe, Asia, Africa, and Australia"),
    HZ_60(60.0f, "60 Hz Mains (US / Canada / Japan)", "Standard AC grid frequency across North America, parts of Japan, and Brazil"),
    AUTO_DETECT(0.0f, "Auto-Detect Harmonic Signature", "Real-time spectral analysis probes 50 Hz vs 60 Hz electrical interference")
}

enum class HumHarmonicDepth(val maxHarmonicOrder: Int, val displayName: String, val description: String) {
    FUNDAMENTAL_ONLY(1, "Fundamental Only (f₀)", "Notches only the primary 50/60 Hz powerline hum, leaving overtones intact"),
    LOW_HARMONICS(3, "Low Harmonics (f₀ + 2f₀ + 3f₀)", "Suppresses fundamental plus 2nd (100/120Hz) and 3rd (150/180Hz) rectification harmonics"),
    FULL_SPECTRUM(6, "Full Harmonic Series (up to 6f₀)", "Deep multi-order notch cascade suppressing up to the 6th harmonic (300/360Hz) buzz")
}

enum class HumIsolatorPreset(
    val displayName: String,
    val frequency: MainsFrequency,
    val harmonicDepth: HumHarmonicDepth,
    val notchQ: Float,
    val notchDepthDb: Float,
    val adaptiveMasking: Boolean,
    val dcBlocker: Boolean,
    val description: String
) {
    OFF("Off (Bypassed)", MainsFrequency.AUTO_DETECT, HumHarmonicDepth.LOW_HARMONICS, 25.0f, 0.0f, false, false, "Direct pristine signal with zero ground loop notch filtering"),
    STUDIO_CLEAN_AUTO("Studio Clean (Auto-Detect)", MainsFrequency.AUTO_DETECT, HumHarmonicDepth.LOW_HARMONICS, 30.0f, -24.0f, true, true, "Narrow precision surgical notch bank auto-locking onto grid interference"),
    USB_DAC_GROUND_LIFT("USB-C DAC Ground Lift", MainsFrequency.AUTO_DETECT, HumHarmonicDepth.FULL_SPECTRUM, 28.0f, -32.0f, true, true, "High-suppression harmonic de-buzzer targeting switch-mode charger ground loops"),
    VINTAGE_VINYL_RESTORE("Vintage Turntable 50 Hz Restore", MainsFrequency.HZ_50, HumHarmonicDepth.LOW_HARMONICS, 25.0f, -20.0f, true, true, "Gentle phono preamp ground hum elimination preserving vinyl bass weight"),
    NORTH_AMERICA_60_HZ("North America 60 Hz Purifier", MainsFrequency.HZ_60, HumHarmonicDepth.FULL_SPECTRUM, 32.0f, -30.0f, true, true, "Surgical suppression of 60 Hz powerline buzz and its 120/180/240 Hz multiples"),
    HIGH_SENSITIVITY_IEM("High-Sensitivity IEM Noise Shield", MainsFrequency.AUTO_DETECT, HumHarmonicDepth.FULL_SPECTRUM, 35.0f, -36.0f, false, true, "Maximum continuous attenuation for ultra-sensitive low-impedance in-ear monitors")
}

data class MainsHumIsolatorConfig(
    val isEnabled: Boolean = false,
    val preset: HumIsolatorPreset = HumIsolatorPreset.OFF,
    val frequency: MainsFrequency = MainsFrequency.AUTO_DETECT,
    val harmonicDepth: HumHarmonicDepth = HumHarmonicDepth.LOW_HARMONICS,
    val notchQ: Float = 25.0f,
    val notchDepthDb: Float = -24.0f,
    val adaptiveMasking: Boolean = true,
    val dcBlockerEnabled: Boolean = true
)

data class MainsHumMetrics(
    val estimatedHumLevelDb: Float,
    val attenuationAtF0Db: Float,
    val subBassPreservationRatio: Float,
    val detectedGridFrequencyHz: Float,
    val dcOffsetMv: Float,
    val isolationRating: String
)
























