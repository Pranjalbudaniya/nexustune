package com.nexus.tune.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.nexus.tune.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nexus_tune_settings")

class SettingsDataStore(private val context: Context) {

    private object PreferencesKeys {
        val SEED_MODE = stringPreferencesKey("theme_seed_mode")
        val CUSTOM_SEED_HEX = stringPreferencesKey("theme_custom_seed_hex")
        val IS_DARK_MODE = booleanPreferencesKey("theme_is_dark_mode")
        val IS_AMOLED = booleanPreferencesKey("theme_is_amoled")
        val SHAPE_PRESET = stringPreferencesKey("theme_shape_preset")
        val PLAYER_LAYOUT = stringPreferencesKey("theme_player_layout")
        val MINI_PLAYER_STYLE = stringPreferencesKey("theme_mini_player_style")
        val REFRESH_RATE = intPreferencesKey("display_refresh_rate")
        val CROSSFADE_SECONDS = intPreferencesKey("theme_crossfade_seconds")
        val CONTENT_COUNTRY = stringPreferencesKey("content_country")
        val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
        val EQUALIZER_ENABLED = booleanPreferencesKey("equalizer_enabled")
        val EQUALIZER_PRESET = intPreferencesKey("equalizer_preset")
        val SPATIAL_AUDIO_ENABLED = booleanPreferencesKey("spatial_audio_enabled")
        val SPATIAL_AUDIO_STAGE = stringPreferencesKey("spatial_audio_stage")
        val SPATIAL_AUDIO_STRENGTH = intPreferencesKey("spatial_audio_strength")
        val CROSSFADE_CURVE = stringPreferencesKey("crossfade_curve")
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches_csv")
        val SHAKE_TO_EXTEND = booleanPreferencesKey("sleep_timer_shake_to_extend")
        val SHAKE_EXTEND_MINUTES = intPreferencesKey("sleep_timer_shake_extend_minutes")
        val AMBIENT_SOUND_TYPE = stringPreferencesKey("ambient_sound_type")
        val AMBIENT_SOUND_VOLUME = floatPreferencesKey("ambient_sound_volume")
        val CUSTOM_EQ_BANDS = stringPreferencesKey("custom_eq_bands_csv")
        val BEDTIME_HANDOFF_ENABLED = booleanPreferencesKey("bedtime_handoff_enabled")
        val BEDTIME_HANDOFF_SOUND_TYPE = stringPreferencesKey("bedtime_handoff_sound_type")
        val BEDTIME_HANDOFF_VOLUME = floatPreferencesKey("bedtime_handoff_volume")
        val BEDTIME_HANDOFF_DURATION = intPreferencesKey("bedtime_handoff_duration_minutes")
        val SEEKBAR_STYLE = stringPreferencesKey("theme_seekbar_style")
        val RESET_PITCH_ON_TRACK_CHANGE = booleanPreferencesKey("reset_pitch_on_track_change")
        val SMART_SHUFFLE_ENABLED = booleanPreferencesKey("smart_shuffle_enabled")
        val WAKE_ALARM_ENABLED = booleanPreferencesKey("wake_alarm_enabled")
        val WAKE_ALARM_HOUR = intPreferencesKey("wake_alarm_hour")
        val WAKE_ALARM_MINUTE = intPreferencesKey("wake_alarm_minute")
        val WAKE_ALARM_FADE_SECONDS = intPreferencesKey("wake_alarm_fade_seconds")
        val WAKE_ALARM_SOURCE = stringPreferencesKey("wake_alarm_source")
        val WAKE_ALARM_VOLUME = floatPreferencesKey("wake_alarm_volume")
        val LOUDNESS_MODE = stringPreferencesKey("loudness_normalization_mode")
        val TARGET_LUFS = floatPreferencesKey("loudness_target_lufs")
        val PRE_AMP_GAIN_DB = floatPreferencesKey("loudness_preamp_gain_db")
        val STREAK_CURRENT_DAYS = intPreferencesKey("streak_current_days")
        val STREAK_LONGEST_DAYS = intPreferencesKey("streak_longest_days")
        val STREAK_LAST_ACTIVE_EPOCH_DAY = longPreferencesKey("streak_last_active_epoch_day")
        val CUSTOM_EQ_PROFILES = stringPreferencesKey("custom_eq_profiles_serialized")
        val DJ_PRESET = stringPreferencesKey("dj_preset")
        val DJ_SPEED = floatPreferencesKey("dj_speed")
        val DJ_SEMITONES = intPreferencesKey("dj_semitones")
        val DJ_SOUNDSTAGE_WIDTH = floatPreferencesKey("dj_soundstage_width")
        val DJ_LOFI_FILTER = booleanPreferencesKey("dj_lofi_filter")
        val DJ_BASS_BOOST = intPreferencesKey("dj_bass_boost")
        val DJ_REMEMBER_ACROSS_TRACKS = booleanPreferencesKey("dj_remember_across_tracks")
        val CUSTOM_SMART_PLAYLISTS = stringPreferencesKey("custom_smart_playlists_json")
        val VISUALIZER_MODE = stringPreferencesKey("theme_visualizer_mode")
        val VISUALIZER_PALETTE = stringPreferencesKey("theme_visualizer_palette")
        val AUTO_STASH_ENABLED = booleanPreferencesKey("auto_stash_enabled")
        val AUTO_STASH_BUDGET_MB = intPreferencesKey("auto_stash_budget_mb")
        val AUTO_STASH_STRATEGY = stringPreferencesKey("auto_stash_strategy")
        val AUTO_STASH_WIFI_ONLY = booleanPreferencesKey("auto_stash_wifi_only")
        val HEADPHONE_CORRECTION_ENABLED = booleanPreferencesKey("headphone_correction_enabled")
        val HEADPHONE_TARGET_CURVE = stringPreferencesKey("headphone_target_curve")
        val HEADPHONE_CROSSFEED_ENABLED = booleanPreferencesKey("headphone_crossfeed_enabled")
        val STEREO_WIDTH_ENABLED = booleanPreferencesKey("stereo_width_enabled")
        val STEREO_WIDTH_MODE = stringPreferencesKey("stereo_width_mode")
        val STEREO_WIDTH_PERCENT = intPreferencesKey("stereo_width_percent")
        val STEREO_HAAS_DELAY_MS = floatPreferencesKey("stereo_haas_delay_ms")
        val STEREO_MID_LEVEL_DB = floatPreferencesKey("stereo_mid_level_db")
        val STEREO_SIDE_LEVEL_DB = floatPreferencesKey("stereo_side_level_db")
        val SLEEP_DRIFT_PROFILE = stringPreferencesKey("sleep_drift_profile")
        val DITHER_MODE = stringPreferencesKey("dither_mode")
        val DYNAMIC_LOUDNESS_ENABLED = booleanPreferencesKey("dynamic_loudness_enabled")
        val DYNAMIC_LOUDNESS_MODE = stringPreferencesKey("dynamic_loudness_mode")
        val DYNAMIC_LOUDNESS_INTENSITY = intPreferencesKey("dynamic_loudness_intensity")
        val BEAT_CROSSFADE_ENABLED = booleanPreferencesKey("beat_crossfade_enabled")
        val BEAT_CROSSFADE_MODE = stringPreferencesKey("beat_crossfade_mode")
        val BEAT_CROSSFADE_MAX_BEND = intPreferencesKey("beat_crossfade_max_bend")
        val SILENCE_TRIM_ENABLED = booleanPreferencesKey("silence_trim_enabled")
        val SILENCE_TRIM_MODE = stringPreferencesKey("silence_trim_mode")
        val SILENCE_TRIM_THRESHOLD_DB = floatPreferencesKey("silence_trim_threshold_db")
        val SILENCE_TRIM_MIN_MS = longPreferencesKey("silence_trim_min_ms")
        val RESAMPLING_FILTER_MODE = stringPreferencesKey("resampling_filter_mode")
        val RESAMPLING_FILTER_CUTOFF = floatPreferencesKey("resampling_filter_cutoff")
        val PHASE_GUARD_ENABLED = booleanPreferencesKey("phase_guard_enabled")
        val PHASE_GUARD_MODE = stringPreferencesKey("phase_guard_mode")
        val PHASE_GUARD_THRESHOLD = floatPreferencesKey("phase_guard_threshold")
        val TILT_EQ_ENABLED = booleanPreferencesKey("tilt_eq_enabled")
        val TILT_EQ_DB = floatPreferencesKey("tilt_eq_db")
        val TILT_EQ_PIVOT_HZ = floatPreferencesKey("tilt_eq_pivot_hz")
        val TILT_EQ_LOUDNESS_COMPENSATE = booleanPreferencesKey("tilt_eq_loudness_compensate")
        val DECLICKER_ENABLED = booleanPreferencesKey("declicker_enabled")
        val DECLICKER_MODE = stringPreferencesKey("declicker_mode")
        val DECLICKER_SENSITIVITY = floatPreferencesKey("declicker_sensitivity")
        val DECLICKER_MAX_WIDTH = intPreferencesKey("declicker_max_width")
        val MID_BASS_CLARITY_ENABLED = booleanPreferencesKey("mid_bass_clarity_enabled")
        val MID_BASS_CLARITY_MODE = stringPreferencesKey("mid_bass_clarity_mode")
        val MID_BASS_CLARITY_CENTER_HZ = floatPreferencesKey("mid_bass_clarity_center_hz")
        val MID_BASS_CLARITY_THRESHOLD = intPreferencesKey("mid_bass_clarity_threshold")
        val XTC_ENABLED = booleanPreferencesKey("xtc_enabled")
        val XTC_MODE = stringPreferencesKey("xtc_mode")
        val XTC_DELAY_SAMPLES = intPreferencesKey("xtc_delay_samples")
        val XTC_INTENSITY_PERCENT = intPreferencesKey("xtc_intensity_percent")
        val INFRASONIC_ENABLED = booleanPreferencesKey("infrasonic_enabled")
        val INFRASONIC_CUTOFF_HZ = floatPreferencesKey("infrasonic_cutoff_hz")
        val INFRASONIC_SLOPE = stringPreferencesKey("infrasonic_slope")
        val INFRASONIC_DC_BLOCKER = booleanPreferencesKey("infrasonic_dc_blocker")
        val AIR_BAND_ENABLED = booleanPreferencesKey("air_band_enabled")
        val AIR_BAND_MODE = stringPreferencesKey("air_band_mode")
        val AIR_BAND_FREQ = stringPreferencesKey("air_band_freq")
        val AIR_BAND_BOOST_DB = floatPreferencesKey("air_band_boost_db")
        val AIR_BAND_HARMONICS = intPreferencesKey("air_band_harmonics")
        val SUB_BASS_SYNTH_ENABLED = booleanPreferencesKey("sub_bass_synth_enabled")
        val SUB_BASS_SYNTH_MODE = stringPreferencesKey("sub_bass_synth_mode")
        val SUB_BASS_SYNTH_TARGET = stringPreferencesKey("sub_bass_synth_target")
        val SUB_BASS_SYNTH_INTENSITY = intPreferencesKey("sub_bass_synth_intensity")
        val SUB_BASS_SYNTH_BLEND = intPreferencesKey("sub_bass_synth_blend")
        val DEESSER_ENABLED = booleanPreferencesKey("deesser_enabled")
        val DEESSER_MODE = stringPreferencesKey("deesser_mode")
        val DEESSER_CENTER_HZ = floatPreferencesKey("deesser_center_hz")
        val DEESSER_THRESHOLD = intPreferencesKey("deesser_threshold")
        val DEESSER_MAX_REDUCTION = floatPreferencesKey("deesser_max_reduction")
        val TRUE_PEAK_ENABLED = booleanPreferencesKey("true_peak_enabled")
        val TRUE_PEAK_MODE = stringPreferencesKey("true_peak_mode")
        val TRUE_PEAK_CEILING = stringPreferencesKey("true_peak_ceiling")
        val TRUE_PEAK_OVERSAMPLE_4X = booleanPreferencesKey("true_peak_oversample_4x")
        val ULTRASONIC_ENABLED = booleanPreferencesKey("ultrasonic_enabled")
        val ULTRASONIC_MODE = stringPreferencesKey("ultrasonic_mode")
        val ULTRASONIC_TOPOLOGY = stringPreferencesKey("ultrasonic_topology")
        val ULTRASONIC_CUTOFF_HZ = floatPreferencesKey("ultrasonic_cutoff_hz")
        val TUBE_SATURATION_ENABLED = booleanPreferencesKey("tube_saturation_enabled")
        val TUBE_SATURATION_PROFILE = stringPreferencesKey("tube_saturation_profile")
        val TUBE_SATURATION_TOPOLOGY = stringPreferencesKey("tube_saturation_topology")
        val TUBE_SATURATION_DRIVE = floatPreferencesKey("tube_saturation_drive")
        val TUBE_SATURATION_BIAS = floatPreferencesKey("tube_saturation_bias")
        val TUBE_SATURATION_MAKEUP_GAIN = booleanPreferencesKey("tube_saturation_makeup_gain")
        val TRANSIENT_SHAPER_ENABLED = booleanPreferencesKey("transient_shaper_enabled")
        val TRANSIENT_SHAPER_PRESET = stringPreferencesKey("transient_shaper_preset")
        val TRANSIENT_SHAPER_FOCUS = stringPreferencesKey("transient_shaper_focus")
        val TRANSIENT_SHAPER_ATTACK = floatPreferencesKey("transient_shaper_attack")
        val TRANSIENT_SHAPER_SUSTAIN = floatPreferencesKey("transient_shaper_sustain")
        val TRANSIENT_SHAPER_LIMIT = booleanPreferencesKey("transient_shaper_limit")
        val STEREO_IMAGER_ENABLED = booleanPreferencesKey("stereo_imager_enabled")
        val STEREO_IMAGER_PRESET = stringPreferencesKey("stereo_imager_preset")
        val STEREO_IMAGER_WIDTH = floatPreferencesKey("stereo_imager_width")
        val STEREO_IMAGER_BASS_CUTOFF = floatPreferencesKey("stereo_imager_bass_cutoff")
        val STEREO_IMAGER_AIR_SPREAD = floatPreferencesKey("stereo_imager_air_spread")
        val STEREO_IMAGER_MONO_GUARD = booleanPreferencesKey("stereo_imager_mono_guard")
        val TAPE_HYSTERESIS_ENABLED = booleanPreferencesKey("tape_hysteresis_enabled")
        val TAPE_HYSTERESIS_PRESET = stringPreferencesKey("tape_hysteresis_preset")
        val TAPE_HYSTERESIS_SPEED = stringPreferencesKey("tape_hysteresis_speed")
        val TAPE_HYSTERESIS_DRIVE = floatPreferencesKey("tape_hysteresis_drive")
        val TAPE_HYSTERESIS_HEAD_BUMP = floatPreferencesKey("tape_hysteresis_head_bump")
        val TAPE_HYSTERESIS_WOW_FLUTTER = floatPreferencesKey("tape_hysteresis_wow_flutter")
        val TAPE_HYSTERESIS_NOISE_REDUCTION = booleanPreferencesKey("tape_hysteresis_noise_reduction")
        val MAINS_HUM_ISOLATOR_ENABLED = booleanPreferencesKey("mains_hum_isolator_enabled")
        val MAINS_HUM_ISOLATOR_PRESET = stringPreferencesKey("mains_hum_isolator_preset")
        val MAINS_HUM_ISOLATOR_FREQUENCY = stringPreferencesKey("mains_hum_isolator_frequency")
        val MAINS_HUM_ISOLATOR_HARMONIC_DEPTH = stringPreferencesKey("mains_hum_isolator_harmonic_depth")
        val MAINS_HUM_ISOLATOR_NOTCH_Q = floatPreferencesKey("mains_hum_isolator_notch_q")
        val MAINS_HUM_ISOLATOR_NOTCH_DEPTH = floatPreferencesKey("mains_hum_isolator_notch_depth")
        val MAINS_HUM_ISOLATOR_ADAPTIVE_MASKING = booleanPreferencesKey("mains_hum_isolator_adaptive_masking")
        val MAINS_HUM_ISOLATOR_DC_BLOCKER = booleanPreferencesKey("mains_hum_isolator_dc_blocker")
        val ACOUSTIC_FEEDBACK_ENABLED = booleanPreferencesKey("acoustic_feedback_enabled")
        val ACOUSTIC_FEEDBACK_PRESET = stringPreferencesKey("acoustic_feedback_preset")
        val ACOUSTIC_FEEDBACK_MODE = stringPreferencesKey("acoustic_feedback_mode")
        val ACOUSTIC_FEEDBACK_SENSITIVITY = floatPreferencesKey("acoustic_feedback_sensitivity")
        val ACOUSTIC_FEEDBACK_NOTCHES = intPreferencesKey("acoustic_feedback_notches")
        val ACOUSTIC_FEEDBACK_NOTCH_Q = floatPreferencesKey("acoustic_feedback_notch_q")
        val ACOUSTIC_FEEDBACK_DEPTH = floatPreferencesKey("acoustic_feedback_depth")
        val ACOUSTIC_FEEDBACK_PANIC_CLAMP = booleanPreferencesKey("acoustic_feedback_panic_clamp")
        val SOUNDSTAGE_ELEVATION_ENABLED = booleanPreferencesKey("soundstage_elevation_enabled")
        val SOUNDSTAGE_ELEVATION_PRESET = stringPreferencesKey("soundstage_elevation_preset")
        val SOUNDSTAGE_ELEVATION_ANGLE = stringPreferencesKey("soundstage_elevation_angle")
        val SOUNDSTAGE_ELEVATION_INTENSITY = floatPreferencesKey("soundstage_elevation_intensity")
        val SOUNDSTAGE_ELEVATION_CEILING_MS = floatPreferencesKey("soundstage_elevation_ceiling_ms")
        val SOUNDSTAGE_ELEVATION_PINNA_DEPTH = floatPreferencesKey("soundstage_elevation_pinna_depth")
        val SOUNDSTAGE_ELEVATION_DIFFUSE_SPREAD = booleanPreferencesKey("soundstage_elevation_diffuse_spread")
        val MONO_BASS_ALIGN_ENABLED = booleanPreferencesKey("mono_bass_align_enabled")
        val MONO_BASS_ALIGN_PRESET = stringPreferencesKey("mono_bass_align_preset")
        val MONO_BASS_ALIGN_CROSSOVER = stringPreferencesKey("mono_bass_align_crossover")
        val MONO_BASS_ALIGN_SLOPE = stringPreferencesKey("mono_bass_align_slope")
        val MONO_BASS_ALIGN_AUTO_PHASE = booleanPreferencesKey("mono_bass_align_auto_phase")
        val MONO_BASS_ALIGN_PUNCH_GAIN = floatPreferencesKey("mono_bass_align_punch_gain")
        val MONO_BASS_ALIGN_SIDE_SOLO = booleanPreferencesKey("mono_bass_align_side_solo")
        val PREAMP_NOISE_FLATTENER_ENABLED = booleanPreferencesKey("preamp_noise_flattener_enabled")
        val PREAMP_NOISE_FLATTENER_PRESET = stringPreferencesKey("preamp_noise_flattener_preset")
        val PREAMP_NOISE_FLATTENER_PROFILE = stringPreferencesKey("preamp_noise_flattener_profile")
        val PREAMP_NOISE_FLATTENER_MODE = stringPreferencesKey("preamp_noise_flattener_mode")
        val PREAMP_NOISE_FLATTENER_FLATTEN_DEPTH = floatPreferencesKey("preamp_noise_flattener_flatten_depth")
        val PREAMP_NOISE_FLATTENER_THRESHOLD_DB = floatPreferencesKey("preamp_noise_flattener_threshold_db")
        val PREAMP_NOISE_FLATTENER_DITHER_LEVEL = floatPreferencesKey("preamp_noise_flattener_dither_level")
        val PREAMP_NOISE_FLATTENER_HF_TILT_DB = floatPreferencesKey("preamp_noise_flattener_hf_tilt_db")
        val SLEW_RATE_LIMITER_ENABLED = booleanPreferencesKey("slew_rate_limiter_enabled")
        val SLEW_RATE_LIMITER_PRESET = stringPreferencesKey("slew_rate_limiter_preset")
        val SLEW_RATE_LIMITER_SPEED = stringPreferencesKey("slew_rate_limiter_speed")
        val SLEW_RATE_LIMITER_MODE = stringPreferencesKey("slew_rate_limiter_mode")
        val SLEW_RATE_LIMITER_DAMPING = floatPreferencesKey("slew_rate_limiter_damping")
        val SLEW_RATE_LIMITER_PEAK_THRESHOLD = floatPreferencesKey("slew_rate_limiter_peak_threshold")
    }

    val themeConfig: Flow<ThemeConfig> = context.dataStore.data.map { prefs ->
        ThemeConfig(
            seedMode = ThemeSeedMode.valueOf(prefs[PreferencesKeys.SEED_MODE] ?: ThemeSeedMode.DYNAMIC.name),
            customSeedColorHex = prefs[PreferencesKeys.CUSTOM_SEED_HEX] ?: "#6750A4",
            isDarkMode = prefs[PreferencesKeys.IS_DARK_MODE] ?: true,
            isAmoled = prefs[PreferencesKeys.IS_AMOLED] ?: false,
            shapePreset = ShapePreset.valueOf(prefs[PreferencesKeys.SHAPE_PRESET] ?: ShapePreset.SOFT_ROUNDED.name),
            playerLayout = PlayerLayout.valueOf(prefs[PreferencesKeys.PLAYER_LAYOUT] ?: PlayerLayout.ART_FORWARD.name),
            miniPlayerStyle = MiniPlayerStyle.valueOf(prefs[PreferencesKeys.MINI_PLAYER_STYLE] ?: MiniPlayerStyle.DOCKED_BAR.name),
            refreshRatePreferred = prefs[PreferencesKeys.REFRESH_RATE] ?: 120,
            crossfadeSeconds = prefs[PreferencesKeys.CROSSFADE_SECONDS] ?: 0,
            spatialAudioEnabled = prefs[PreferencesKeys.SPATIAL_AUDIO_ENABLED] ?: false,
            spatialAudioStage = runCatching {
                SpatialAudioStage.valueOf(prefs[PreferencesKeys.SPATIAL_AUDIO_STAGE] ?: SpatialAudioStage.OFF.name)
            }.getOrDefault(SpatialAudioStage.OFF),
            spatialAudioStrength = prefs[PreferencesKeys.SPATIAL_AUDIO_STRENGTH] ?: 500,
            crossfadeCurve = runCatching {
                CrossfadeCurve.valueOf(prefs[PreferencesKeys.CROSSFADE_CURVE] ?: CrossfadeCurve.EQUAL_POWER.name)
            }.getOrDefault(CrossfadeCurve.EQUAL_POWER),
            shakeToExtendSleepTimerEnabled = prefs[PreferencesKeys.SHAKE_TO_EXTEND] ?: false,
            shakeExtendMinutes = prefs[PreferencesKeys.SHAKE_EXTEND_MINUTES] ?: 10,
            ambientSoundType = runCatching {
                AmbientSoundType.valueOf(prefs[PreferencesKeys.AMBIENT_SOUND_TYPE] ?: AmbientSoundType.OFF.name)
            }.getOrDefault(AmbientSoundType.OFF),
            ambientSoundVolume = prefs[PreferencesKeys.AMBIENT_SOUND_VOLUME] ?: 0.5f,
            customEqBands = prefs[PreferencesKeys.CUSTOM_EQ_BANDS]?.split(",")?.mapNotNull { it.trim().toIntOrNull() }?.takeIf { it.size == 5 } ?: listOf(0, 0, 0, 0, 0),
            bedtimeHandoffEnabled = prefs[PreferencesKeys.BEDTIME_HANDOFF_ENABLED] ?: false,
            bedtimeHandoffSoundType = runCatching {
                AmbientSoundType.valueOf(prefs[PreferencesKeys.BEDTIME_HANDOFF_SOUND_TYPE] ?: AmbientSoundType.BROWN_NOISE.name)
            }.getOrDefault(AmbientSoundType.BROWN_NOISE),
            bedtimeHandoffVolume = prefs[PreferencesKeys.BEDTIME_HANDOFF_VOLUME] ?: 0.3f,
            bedtimeHandoffDurationMinutes = prefs[PreferencesKeys.BEDTIME_HANDOFF_DURATION] ?: 60,
            seekbarStyle = runCatching {
                SeekbarStyle.valueOf(prefs[PreferencesKeys.SEEKBAR_STYLE] ?: SeekbarStyle.WAVEFORM.name)
            }.getOrDefault(SeekbarStyle.WAVEFORM),
            resetPitchOnTrackChange = prefs[PreferencesKeys.RESET_PITCH_ON_TRACK_CHANGE] ?: true,
            smartShuffleEnabled = prefs[PreferencesKeys.SMART_SHUFFLE_ENABLED] ?: true,
            wakeAlarmConfig = WakeAlarmConfig(
                isEnabled = prefs[PreferencesKeys.WAKE_ALARM_ENABLED] ?: false,
                hour = prefs[PreferencesKeys.WAKE_ALARM_HOUR] ?: 7,
                minute = prefs[PreferencesKeys.WAKE_ALARM_MINUTE] ?: 0,
                fadeInSeconds = prefs[PreferencesKeys.WAKE_ALARM_FADE_SECONDS] ?: 120,
                source = runCatching {
                    WakeAlarmSource.valueOf(prefs[PreferencesKeys.WAKE_ALARM_SOURCE] ?: WakeAlarmSource.LIKED_SONGS.name)
                }.getOrDefault(WakeAlarmSource.LIKED_SONGS),
                targetVolume = prefs[PreferencesKeys.WAKE_ALARM_VOLUME] ?: 0.8f
            ),
            loudnessConfig = LoudnessConfig(
                mode = runCatching {
                    LoudnessNormalizationMode.valueOf(prefs[PreferencesKeys.LOUDNESS_MODE] ?: LoudnessNormalizationMode.REPLAYGAIN_TRACK.name)
                }.getOrDefault(LoudnessNormalizationMode.REPLAYGAIN_TRACK),
                targetLufs = prefs[PreferencesKeys.TARGET_LUFS] ?: -14.0f,
                preAmpGainDb = prefs[PreferencesKeys.PRE_AMP_GAIN_DB] ?: 0.0f
            ),
            djFxConfig = DjFxConfig(
                preset = runCatching {
                    DjPreset.valueOf(prefs[PreferencesKeys.DJ_PRESET] ?: DjPreset.NORMAL.name)
                }.getOrDefault(DjPreset.NORMAL),
                speed = prefs[PreferencesKeys.DJ_SPEED] ?: 1.0f,
                semitones = prefs[PreferencesKeys.DJ_SEMITONES] ?: 0,
                soundstageWidth = prefs[PreferencesKeys.DJ_SOUNDSTAGE_WIDTH] ?: 1.0f,
                loFiFilterEnabled = prefs[PreferencesKeys.DJ_LOFI_FILTER] ?: false,
                bassBoostDb = prefs[PreferencesKeys.DJ_BASS_BOOST] ?: 0
            ),
            djRememberAcrossTracks = prefs[PreferencesKeys.DJ_REMEMBER_ACROSS_TRACKS] ?: false,
            visualizerMode = runCatching {
                VisualizerMode.valueOf(prefs[PreferencesKeys.VISUALIZER_MODE] ?: VisualizerMode.SPECTRUM_BARS.name)
            }.getOrDefault(VisualizerMode.SPECTRUM_BARS),
            visualizerPalette = runCatching {
                VisualizerPalette.valueOf(prefs[PreferencesKeys.VISUALIZER_PALETTE] ?: VisualizerPalette.DYNAMIC.name)
            }.getOrDefault(VisualizerPalette.DYNAMIC),
            autoStashConfig = AutoStashConfig(
                isEnabled = prefs[PreferencesKeys.AUTO_STASH_ENABLED] ?: false,
                storageBudgetMb = prefs[PreferencesKeys.AUTO_STASH_BUDGET_MB] ?: 1000,
                strategy = runCatching {
                    AutoStashStrategy.valueOf(prefs[PreferencesKeys.AUTO_STASH_STRATEGY] ?: AutoStashStrategy.BALANCED_HYBRID.name)
                }.getOrDefault(AutoStashStrategy.BALANCED_HYBRID),
                wifiOnly = prefs[PreferencesKeys.AUTO_STASH_WIFI_ONLY] ?: true
            ),
            headphoneCorrectionConfig = HeadphoneCorrectionConfig(
                isEnabled = prefs[PreferencesKeys.HEADPHONE_CORRECTION_ENABLED] ?: false,
                targetCurve = runCatching {
                    HeadphoneTargetCurve.valueOf(prefs[PreferencesKeys.HEADPHONE_TARGET_CURVE] ?: HeadphoneTargetCurve.HARMAN_OVER_EAR.name)
                }.getOrDefault(HeadphoneTargetCurve.HARMAN_OVER_EAR),
                crossfeedEnabled = prefs[PreferencesKeys.HEADPHONE_CROSSFEED_ENABLED] ?: true
            ),
            stereoWidthConfig = StereoWidthConfig(
                isEnabled = prefs[PreferencesKeys.STEREO_WIDTH_ENABLED] ?: false,
                mode = runCatching {
                    StereoWidthMode.valueOf(prefs[PreferencesKeys.STEREO_WIDTH_MODE] ?: StereoWidthMode.NATURAL.name)
                }.getOrDefault(StereoWidthMode.NATURAL),
                customWidthPercent = prefs[PreferencesKeys.STEREO_WIDTH_PERCENT] ?: 100,
                haasDelayMs = prefs[PreferencesKeys.STEREO_HAAS_DELAY_MS] ?: 0.0f,
                midLevelDb = prefs[PreferencesKeys.STEREO_MID_LEVEL_DB] ?: 0.0f,
                sideLevelDb = prefs[PreferencesKeys.STEREO_SIDE_LEVEL_DB] ?: 0.0f
            ),
            sleepDriftProfile = runCatching {
                SleepDriftProfile.valueOf(prefs[PreferencesKeys.SLEEP_DRIFT_PROFILE] ?: SleepDriftProfile.OFF.name)
            }.getOrDefault(SleepDriftProfile.OFF),
            ditherMode = runCatching {
                DitherMode.valueOf(prefs[PreferencesKeys.DITHER_MODE] ?: DitherMode.TRIANGULAR_TPDF.name)
            }.getOrDefault(DitherMode.TRIANGULAR_TPDF),
            dynamicLoudnessConfig = DynamicLoudnessConfig(
                isEnabled = prefs[PreferencesKeys.DYNAMIC_LOUDNESS_ENABLED] ?: false,
                mode = runCatching {
                    DynamicLoudnessMode.valueOf(prefs[PreferencesKeys.DYNAMIC_LOUDNESS_MODE] ?: DynamicLoudnessMode.ISO_226_REFERENCE.name)
                }.getOrDefault(DynamicLoudnessMode.ISO_226_REFERENCE),
                intensityPercent = prefs[PreferencesKeys.DYNAMIC_LOUDNESS_INTENSITY] ?: 100
            ),
            beatCrossfadeConfig = BeatCrossfadeConfig(
                isEnabled = prefs[PreferencesKeys.BEAT_CROSSFADE_ENABLED] ?: false,
                mode = runCatching {
                    BeatCrossfadeMode.valueOf(prefs[PreferencesKeys.BEAT_CROSSFADE_MODE] ?: BeatCrossfadeMode.SMART_BEAT_SYNC.name)
                }.getOrDefault(BeatCrossfadeMode.SMART_BEAT_SYNC),
                maxTempoBendPercent = prefs[PreferencesKeys.BEAT_CROSSFADE_MAX_BEND] ?: 6
            ),
            silenceTrimConfig = SilenceTrimConfig(
                isEnabled = prefs[PreferencesKeys.SILENCE_TRIM_ENABLED] ?: false,
                mode = runCatching {
                    SilenceTrimMode.valueOf(prefs[PreferencesKeys.SILENCE_TRIM_MODE] ?: SilenceTrimMode.NATURAL_MUSIC.name)
                }.getOrDefault(SilenceTrimMode.NATURAL_MUSIC),
                thresholdDb = prefs[PreferencesKeys.SILENCE_TRIM_THRESHOLD_DB] ?: -45.0f,
                minDurationMs = prefs[PreferencesKeys.SILENCE_TRIM_MIN_MS] ?: 1500L
            ),
            resamplingFilterConfig = ResamplingFilterConfig(
                mode = runCatching {
                    ResamplingFilterMode.valueOf(prefs[PreferencesKeys.RESAMPLING_FILTER_MODE] ?: ResamplingFilterMode.MINIMUM_PHASE_PUNCH.name)
                }.getOrDefault(ResamplingFilterMode.MINIMUM_PHASE_PUNCH),
                cutoffRatio = prefs[PreferencesKeys.RESAMPLING_FILTER_CUTOFF] ?: 0.95f
            ),
            phaseMeterConfig = PhaseMeterConfig(
                isEnabled = prefs[PreferencesKeys.PHASE_GUARD_ENABLED] ?: false,
                guardMode = runCatching {
                    MonoGuardMode.valueOf(prefs[PreferencesKeys.PHASE_GUARD_MODE] ?: MonoGuardMode.INTELLIGENT_SAFEGUARD.name)
                }.getOrDefault(MonoGuardMode.INTELLIGENT_SAFEGUARD),
                minCorrelationThreshold = prefs[PreferencesKeys.PHASE_GUARD_THRESHOLD] ?: 0.20f
            ),
            tiltEqualizerConfig = TiltEqualizerConfig(
                isEnabled = prefs[PreferencesKeys.TILT_EQ_ENABLED] ?: false,
                tiltDb = prefs[PreferencesKeys.TILT_EQ_DB] ?: 0.0f,
                pivotHz = prefs[PreferencesKeys.TILT_EQ_PIVOT_HZ] ?: 1000.0f,
                loudnessCompensate = prefs[PreferencesKeys.TILT_EQ_LOUDNESS_COMPENSATE] ?: true
            ),
            deClickerConfig = DeClickerConfig(
                isEnabled = prefs[PreferencesKeys.DECLICKER_ENABLED] ?: false,
                mode = runCatching {
                    DeClickerMode.valueOf(prefs[PreferencesKeys.DECLICKER_MODE] ?: DeClickerMode.TRANSPARENT_STREAM.name)
                }.getOrDefault(DeClickerMode.TRANSPARENT_STREAM),
                sensitivityMultiplier = prefs[PreferencesKeys.DECLICKER_SENSITIVITY] ?: 3.5f,
                maxClickWidthSamples = prefs[PreferencesKeys.DECLICKER_MAX_WIDTH] ?: 8
            ),
            midBassClarityConfig = MidBassClarityConfig(
                isEnabled = prefs[PreferencesKeys.MID_BASS_CLARITY_ENABLED] ?: false,
                mode = runCatching {
                    MidBassClarityMode.valueOf(prefs[PreferencesKeys.MID_BASS_CLARITY_MODE] ?: MidBassClarityMode.BALANCED_DE_MUD.name)
                }.getOrDefault(MidBassClarityMode.BALANCED_DE_MUD),
                centerFrequencyHz = prefs[PreferencesKeys.MID_BASS_CLARITY_CENTER_HZ] ?: 300.0f,
                dynamicThresholdPercent = prefs[PreferencesKeys.MID_BASS_CLARITY_THRESHOLD] ?: 40
            ),
            crosstalkCancellationConfig = CrosstalkCancellationConfig(
                isEnabled = prefs[PreferencesKeys.XTC_ENABLED] ?: false,
                mode = runCatching {
                    CrosstalkCancellationMode.valueOf(prefs[PreferencesKeys.XTC_MODE] ?: CrosstalkCancellationMode.PHONE_STEREO_SPEAKERS.name)
                }.getOrDefault(CrosstalkCancellationMode.PHONE_STEREO_SPEAKERS),
                delaySamples = prefs[PreferencesKeys.XTC_DELAY_SAMPLES] ?: 5,
                cancellationIntensityPercent = prefs[PreferencesKeys.XTC_INTENSITY_PERCENT] ?: 70
            ),
            infrasonicFilterConfig = InfrasonicFilterConfig(
                isEnabled = prefs[PreferencesKeys.INFRASONIC_ENABLED] ?: false,
                cutoffHz = prefs[PreferencesKeys.INFRASONIC_CUTOFF_HZ] ?: 20.0f,
                slope = runCatching {
                    InfrasonicSlope.valueOf(prefs[PreferencesKeys.INFRASONIC_SLOPE] ?: InfrasonicSlope.SLOPE_18DB_OCTAVE.name)
                }.getOrDefault(InfrasonicSlope.SLOPE_18DB_OCTAVE),
                dcBlockerEnabled = prefs[PreferencesKeys.INFRASONIC_DC_BLOCKER] ?: true
            ),
            airBandExciterConfig = AirBandExciterConfig(
                isEnabled = prefs[PreferencesKeys.AIR_BAND_ENABLED] ?: false,
                mode = runCatching {
                    AirBandMode.valueOf(prefs[PreferencesKeys.AIR_BAND_MODE] ?: AirBandMode.OFF.name)
                }.getOrDefault(AirBandMode.OFF),
                frequency = runCatching {
                    AirBandFrequency.valueOf(prefs[PreferencesKeys.AIR_BAND_FREQ] ?: AirBandFrequency.HZ_16000.name)
                }.getOrDefault(AirBandFrequency.HZ_16000),
                boostDb = prefs[PreferencesKeys.AIR_BAND_BOOST_DB] ?: 3.0f,
                harmonicExcitationPercent = prefs[PreferencesKeys.AIR_BAND_HARMONICS] ?: 30
            ),
            subHarmonicSynthConfig = SubHarmonicSynthConfig(
                isEnabled = prefs[PreferencesKeys.SUB_BASS_SYNTH_ENABLED] ?: false,
                mode = runCatching {
                    SubHarmonicMode.valueOf(prefs[PreferencesKeys.SUB_BASS_SYNTH_MODE] ?: SubHarmonicMode.OFF.name)
                }.getOrDefault(SubHarmonicMode.OFF),
                target = runCatching {
                    SubHarmonicTarget.valueOf(prefs[PreferencesKeys.SUB_BASS_SYNTH_TARGET] ?: SubHarmonicTarget.HYBRID_BALANCED.name)
                }.getOrDefault(SubHarmonicTarget.HYBRID_BALANCED),
                intensityPercent = prefs[PreferencesKeys.SUB_BASS_SYNTH_INTENSITY] ?: 50,
                subOctaveBlendPercent = prefs[PreferencesKeys.SUB_BASS_SYNTH_BLEND] ?: 40
            ),
            deEsserConfig = DeEsserConfig(
                isEnabled = prefs[PreferencesKeys.DEESSER_ENABLED] ?: false,
                mode = runCatching {
                    DeEsserMode.valueOf(prefs[PreferencesKeys.DEESSER_MODE] ?: DeEsserMode.OFF.name)
                }.getOrDefault(DeEsserMode.OFF),
                centerFrequencyHz = prefs[PreferencesKeys.DEESSER_CENTER_HZ] ?: 6000.0f,
                thresholdPercent = prefs[PreferencesKeys.DEESSER_THRESHOLD] ?: 28,
                maxReductionDb = prefs[PreferencesKeys.DEESSER_MAX_REDUCTION] ?: 4.5f
            ),
            truePeakGuardConfig = TruePeakGuardConfig(
                isEnabled = prefs[PreferencesKeys.TRUE_PEAK_ENABLED] ?: false,
                mode = runCatching {
                    TruePeakMode.valueOf(prefs[PreferencesKeys.TRUE_PEAK_MODE] ?: TruePeakMode.OFF.name)
                }.getOrDefault(TruePeakMode.OFF),
                ceiling = runCatching {
                    TruePeakCeiling.valueOf(prefs[PreferencesKeys.TRUE_PEAK_CEILING] ?: TruePeakCeiling.CEILING_MINUS_0_5_DBTP.name)
                }.getOrDefault(TruePeakCeiling.CEILING_MINUS_0_5_DBTP),
                oversample4xEnabled = prefs[PreferencesKeys.TRUE_PEAK_OVERSAMPLE_4X] ?: true
            ),
            ultrasonicFilterConfig = UltrasonicFilterConfig(
                isEnabled = prefs[PreferencesKeys.ULTRASONIC_ENABLED] ?: false,
                mode = runCatching {
                    UltrasonicMode.valueOf(prefs[PreferencesKeys.ULTRASONIC_MODE] ?: UltrasonicMode.OFF.name)
                }.getOrDefault(UltrasonicMode.OFF),
                topology = runCatching {
                    UltrasonicTopology.valueOf(prefs[PreferencesKeys.ULTRASONIC_TOPOLOGY] ?: UltrasonicTopology.LINEAR_PHASE_FIR.name)
                }.getOrDefault(UltrasonicTopology.LINEAR_PHASE_FIR),
                cutoffHz = prefs[PreferencesKeys.ULTRASONIC_CUTOFF_HZ] ?: 22050.0f
            ),
            tubeSaturationConfig = TubeSaturationConfig(
                isEnabled = prefs[PreferencesKeys.TUBE_SATURATION_ENABLED] ?: false,
                profile = runCatching {
                    TubeWarmthProfile.valueOf(prefs[PreferencesKeys.TUBE_SATURATION_PROFILE] ?: TubeWarmthProfile.OFF.name)
                }.getOrDefault(TubeWarmthProfile.OFF),
                topology = runCatching {
                    TubeTopology.valueOf(prefs[PreferencesKeys.TUBE_SATURATION_TOPOLOGY] ?: TubeTopology.TRIODE_12AX7.name)
                }.getOrDefault(TubeTopology.TRIODE_12AX7),
                drive = prefs[PreferencesKeys.TUBE_SATURATION_DRIVE] ?: 0.35f,
                bias = prefs[PreferencesKeys.TUBE_SATURATION_BIAS] ?: 0.20f,
                autoMakeupGain = prefs[PreferencesKeys.TUBE_SATURATION_MAKEUP_GAIN] ?: true
            ),
            transientShaperConfig = TransientShaperConfig(
                isEnabled = prefs[PreferencesKeys.TRANSIENT_SHAPER_ENABLED] ?: false,
                preset = runCatching {
                    TransientPreset.valueOf(prefs[PreferencesKeys.TRANSIENT_SHAPER_PRESET] ?: TransientPreset.OFF.name)
                }.getOrDefault(TransientPreset.OFF),
                bandFocus = runCatching {
                    TransientBandFocus.valueOf(prefs[PreferencesKeys.TRANSIENT_SHAPER_FOCUS] ?: TransientBandFocus.FULL_BAND.name)
                }.getOrDefault(TransientBandFocus.FULL_BAND),
                attackBoost = prefs[PreferencesKeys.TRANSIENT_SHAPER_ATTACK] ?: 0.0f,
                sustainBoost = prefs[PreferencesKeys.TRANSIENT_SHAPER_SUSTAIN] ?: 0.0f,
                autoOutputLimit = prefs[PreferencesKeys.TRANSIENT_SHAPER_LIMIT] ?: true
            ),
            dynamicStereoImagerConfig = DynamicStereoImagerConfig(
                isEnabled = prefs[PreferencesKeys.STEREO_IMAGER_ENABLED] ?: false,
                preset = runCatching {
                    StereoImagerPreset.valueOf(prefs[PreferencesKeys.STEREO_IMAGER_PRESET] ?: StereoImagerPreset.OFF.name)
                }.getOrDefault(StereoImagerPreset.OFF),
                stereoWidth = prefs[PreferencesKeys.STEREO_IMAGER_WIDTH] ?: 1.0f,
                monoBassCutoffHz = prefs[PreferencesKeys.STEREO_IMAGER_BASS_CUTOFF] ?: 160.0f,
                highBandAirSpread = prefs[PreferencesKeys.STEREO_IMAGER_AIR_SPREAD] ?: 0.40f,
                monoGuardActive = prefs[PreferencesKeys.STEREO_IMAGER_MONO_GUARD] ?: true
            ),
            tapeHysteresisConfig = TapeHysteresisConfig(
                isEnabled = prefs[PreferencesKeys.TAPE_HYSTERESIS_ENABLED] ?: false,
                preset = runCatching {
                    TapePreset.valueOf(prefs[PreferencesKeys.TAPE_HYSTERESIS_PRESET] ?: TapePreset.OFF.name)
                }.getOrDefault(TapePreset.OFF),
                speed = runCatching {
                    TapeSpeed.valueOf(prefs[PreferencesKeys.TAPE_HYSTERESIS_SPEED] ?: TapeSpeed.IPS_15.name)
                }.getOrDefault(TapeSpeed.IPS_15),
                tapeDrive = prefs[PreferencesKeys.TAPE_HYSTERESIS_DRIVE] ?: 0.35f,
                headBumpGainDb = prefs[PreferencesKeys.TAPE_HYSTERESIS_HEAD_BUMP] ?: 2.0f,
                wowFlutterDepth = prefs[PreferencesKeys.TAPE_HYSTERESIS_WOW_FLUTTER] ?: 0.20f,
                tapeHissReduction = prefs[PreferencesKeys.TAPE_HYSTERESIS_NOISE_REDUCTION] ?: true
            ),
            mainsHumIsolatorConfig = MainsHumIsolatorConfig(
                isEnabled = prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_ENABLED] ?: false,
                preset = runCatching {
                    HumIsolatorPreset.valueOf(prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_PRESET] ?: HumIsolatorPreset.OFF.name)
                }.getOrDefault(HumIsolatorPreset.OFF),
                frequency = runCatching {
                    MainsFrequency.valueOf(prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_FREQUENCY] ?: MainsFrequency.AUTO_DETECT.name)
                }.getOrDefault(MainsFrequency.AUTO_DETECT),
                harmonicDepth = runCatching {
                    HumHarmonicDepth.valueOf(prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_HARMONIC_DEPTH] ?: HumHarmonicDepth.LOW_HARMONICS.name)
                }.getOrDefault(HumHarmonicDepth.LOW_HARMONICS),
                notchQ = prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_NOTCH_Q] ?: 25.0f,
                notchDepthDb = prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_NOTCH_DEPTH] ?: -24.0f,
                adaptiveMasking = prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_ADAPTIVE_MASKING] ?: true,
                dcBlockerEnabled = prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_DC_BLOCKER] ?: true
            ),
            acousticFeedbackConfig = AcousticFeedbackConfig(
                isEnabled = prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_ENABLED] ?: false,
                preset = runCatching {
                    AcousticFeedbackPreset.valueOf(prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_PRESET] ?: AcousticFeedbackPreset.OFF.name)
                }.getOrDefault(AcousticFeedbackPreset.OFF),
                mode = runCatching {
                    FeedbackHuntMode.valueOf(prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_MODE] ?: FeedbackHuntMode.SURGICAL_AUTO.name)
                }.getOrDefault(FeedbackHuntMode.SURGICAL_AUTO),
                sensitivityThresholdDb = prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_SENSITIVITY] ?: -28.0f,
                activeNotchesCount = prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_NOTCHES] ?: 3,
                notchSharpnessQ = prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_NOTCH_Q] ?: 35.0f,
                suppressionDepthDb = prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_DEPTH] ?: -24.0f,
                fastPanicClamp = prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_PANIC_CLAMP] ?: true
            ),
            soundstageElevationConfig = SoundstageElevationConfig(
                isEnabled = prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_ENABLED] ?: false,
                preset = runCatching {
                    SoundstageElevationPreset.valueOf(prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_PRESET] ?: SoundstageElevationPreset.OFF.name)
                }.getOrDefault(SoundstageElevationPreset.OFF),
                elevationAngle = runCatching {
                    ElevationAngle.valueOf(prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_ANGLE] ?: ElevationAngle.STAGE_LIFT_15.name)
                }.getOrDefault(ElevationAngle.STAGE_LIFT_15),
                elevationIntensity = prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_INTENSITY] ?: 0.50f,
                ceilingReflectionMs = prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_CEILING_MS] ?: 4.2f,
                pinnaNotchDepthDb = prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_PINNA_DEPTH] ?: -4.5f,
                diffuseCeilingSpread = prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_DIFFUSE_SPREAD] ?: true
            ),
            monoBassAlignConfig = MonoBassAlignConfig(
                isEnabled = prefs[PreferencesKeys.MONO_BASS_ALIGN_ENABLED] ?: false,
                preset = runCatching {
                    MonoBassAlignPreset.valueOf(prefs[PreferencesKeys.MONO_BASS_ALIGN_PRESET] ?: MonoBassAlignPreset.OFF.name)
                }.getOrDefault(MonoBassAlignPreset.OFF),
                crossover = runCatching {
                    MonoBassCrossover.valueOf(prefs[PreferencesKeys.MONO_BASS_ALIGN_CROSSOVER] ?: MonoBassCrossover.HZ_80.name)
                }.getOrDefault(MonoBassCrossover.HZ_80),
                slope = runCatching {
                    MonoBassFilterSlope.valueOf(prefs[PreferencesKeys.MONO_BASS_ALIGN_SLOPE] ?: MonoBassFilterSlope.SLOPE_24DB_OCT.name)
                }.getOrDefault(MonoBassFilterSlope.SLOPE_24DB_OCT),
                autoPhaseAlign = prefs[PreferencesKeys.MONO_BASS_ALIGN_AUTO_PHASE] ?: true,
                subPunchGainDb = prefs[PreferencesKeys.MONO_BASS_ALIGN_PUNCH_GAIN] ?: 1.5f,
                sideSoloAudition = prefs[PreferencesKeys.MONO_BASS_ALIGN_SIDE_SOLO] ?: false
            ),
            preampNoiseFlattenerConfig = PreampNoiseFlattenerConfig(
                isEnabled = prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_ENABLED] ?: false,
                preset = runCatching {
                    PreampNoiseFlattenerPreset.valueOf(prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_PRESET] ?: PreampNoiseFlattenerPreset.OFF.name)
                }.getOrDefault(PreampNoiseFlattenerPreset.OFF),
                profile = runCatching {
                    PreampNoiseProfile.valueOf(prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_PROFILE] ?: PreampNoiseProfile.AUTO_DETECT.name)
                }.getOrDefault(PreampNoiseProfile.AUTO_DETECT),
                mode = runCatching {
                    PreampFlattenerMode.valueOf(prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_MODE] ?: PreampFlattenerMode.DYNAMIC_FLATTEN.name)
                }.getOrDefault(PreampFlattenerMode.DYNAMIC_FLATTEN),
                flattenDepth = prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_FLATTEN_DEPTH] ?: 0.5f,
                downwardThresholdDb = prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_THRESHOLD_DB] ?: -45.0f,
                thermalDitherLevel = prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_DITHER_LEVEL] ?: 0.0f,
                hfTiltDb = prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_HF_TILT_DB] ?: 0.0f
            ),
            slewRateLimiterConfig = SlewRateLimiterConfig(
                isEnabled = prefs[PreferencesKeys.SLEW_RATE_LIMITER_ENABLED] ?: false,
                preset = runCatching {
                    SlewRateLimiterPreset.valueOf(prefs[PreferencesKeys.SLEW_RATE_LIMITER_PRESET] ?: SlewRateLimiterPreset.OFF.name)
                }.getOrDefault(SlewRateLimiterPreset.OFF),
                speed = runCatching {
                    SlewRateSpeed.valueOf(prefs[PreferencesKeys.SLEW_RATE_LIMITER_SPEED] ?: SlewRateSpeed.STUDIO_50V_US.name)
                }.getOrDefault(SlewRateSpeed.STUDIO_50V_US),
                mode = runCatching {
                    SlewLimiterMode.valueOf(prefs[PreferencesKeys.SLEW_RATE_LIMITER_MODE] ?: SlewLimiterMode.TRANSPARENT_TIM_GUARD.name)
                }.getOrDefault(SlewLimiterMode.TRANSPARENT_TIM_GUARD),
                dampingRatio = prefs[PreferencesKeys.SLEW_RATE_LIMITER_DAMPING] ?: 0.5f,
                peakThreshold = prefs[PreferencesKeys.SLEW_RATE_LIMITER_PEAK_THRESHOLD] ?: 0.9f
            )
        )
    }

    suspend fun updateThemeConfig(config: ThemeConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SEED_MODE] = config.seedMode.name
            prefs[PreferencesKeys.CUSTOM_SEED_HEX] = config.customSeedColorHex
            prefs[PreferencesKeys.IS_DARK_MODE] = config.isDarkMode
            prefs[PreferencesKeys.IS_AMOLED] = config.isAmoled
            prefs[PreferencesKeys.SHAPE_PRESET] = config.shapePreset.name
            prefs[PreferencesKeys.PLAYER_LAYOUT] = config.playerLayout.name
            prefs[PreferencesKeys.MINI_PLAYER_STYLE] = config.miniPlayerStyle.name
            prefs[PreferencesKeys.REFRESH_RATE] = config.refreshRatePreferred
            prefs[PreferencesKeys.CROSSFADE_SECONDS] = config.crossfadeSeconds
            prefs[PreferencesKeys.SPATIAL_AUDIO_ENABLED] = config.spatialAudioEnabled
            prefs[PreferencesKeys.SPATIAL_AUDIO_STAGE] = config.spatialAudioStage.name
            prefs[PreferencesKeys.SPATIAL_AUDIO_STRENGTH] = config.spatialAudioStrength
            prefs[PreferencesKeys.CROSSFADE_CURVE] = config.crossfadeCurve.name
            prefs[PreferencesKeys.SHAKE_TO_EXTEND] = config.shakeToExtendSleepTimerEnabled
            prefs[PreferencesKeys.SHAKE_EXTEND_MINUTES] = config.shakeExtendMinutes
            prefs[PreferencesKeys.AMBIENT_SOUND_TYPE] = config.ambientSoundType.name
            prefs[PreferencesKeys.AMBIENT_SOUND_VOLUME] = config.ambientSoundVolume
            prefs[PreferencesKeys.CUSTOM_EQ_BANDS] = config.customEqBands.joinToString(",")
            prefs[PreferencesKeys.BEDTIME_HANDOFF_ENABLED] = config.bedtimeHandoffEnabled
            prefs[PreferencesKeys.BEDTIME_HANDOFF_SOUND_TYPE] = config.bedtimeHandoffSoundType.name
            prefs[PreferencesKeys.BEDTIME_HANDOFF_VOLUME] = config.bedtimeHandoffVolume
            prefs[PreferencesKeys.BEDTIME_HANDOFF_DURATION] = config.bedtimeHandoffDurationMinutes
            prefs[PreferencesKeys.SEEKBAR_STYLE] = config.seekbarStyle.name
            prefs[PreferencesKeys.RESET_PITCH_ON_TRACK_CHANGE] = config.resetPitchOnTrackChange
            prefs[PreferencesKeys.SMART_SHUFFLE_ENABLED] = config.smartShuffleEnabled
            prefs[PreferencesKeys.WAKE_ALARM_ENABLED] = config.wakeAlarmConfig.isEnabled
            prefs[PreferencesKeys.WAKE_ALARM_HOUR] = config.wakeAlarmConfig.hour
            prefs[PreferencesKeys.WAKE_ALARM_MINUTE] = config.wakeAlarmConfig.minute
            prefs[PreferencesKeys.WAKE_ALARM_FADE_SECONDS] = config.wakeAlarmConfig.fadeInSeconds
            prefs[PreferencesKeys.WAKE_ALARM_SOURCE] = config.wakeAlarmConfig.source.name
            prefs[PreferencesKeys.WAKE_ALARM_VOLUME] = config.wakeAlarmConfig.targetVolume
            prefs[PreferencesKeys.LOUDNESS_MODE] = config.loudnessConfig.mode.name
            prefs[PreferencesKeys.TARGET_LUFS] = config.loudnessConfig.targetLufs
            prefs[PreferencesKeys.PRE_AMP_GAIN_DB] = config.loudnessConfig.preAmpGainDb
            prefs[PreferencesKeys.DJ_PRESET] = config.djFxConfig.preset.name
            prefs[PreferencesKeys.DJ_SPEED] = config.djFxConfig.speed
            prefs[PreferencesKeys.DJ_SEMITONES] = config.djFxConfig.semitones
            prefs[PreferencesKeys.DJ_SOUNDSTAGE_WIDTH] = config.djFxConfig.soundstageWidth
            prefs[PreferencesKeys.DJ_LOFI_FILTER] = config.djFxConfig.loFiFilterEnabled
            prefs[PreferencesKeys.DJ_BASS_BOOST] = config.djFxConfig.bassBoostDb
            prefs[PreferencesKeys.DJ_REMEMBER_ACROSS_TRACKS] = config.djRememberAcrossTracks
            prefs[PreferencesKeys.VISUALIZER_MODE] = config.visualizerMode.name
            prefs[PreferencesKeys.VISUALIZER_PALETTE] = config.visualizerPalette.name
            prefs[PreferencesKeys.AUTO_STASH_ENABLED] = config.autoStashConfig.isEnabled
            prefs[PreferencesKeys.AUTO_STASH_BUDGET_MB] = config.autoStashConfig.storageBudgetMb
            prefs[PreferencesKeys.AUTO_STASH_STRATEGY] = config.autoStashConfig.strategy.name
            prefs[PreferencesKeys.AUTO_STASH_WIFI_ONLY] = config.autoStashConfig.wifiOnly
            prefs[PreferencesKeys.HEADPHONE_CORRECTION_ENABLED] = config.headphoneCorrectionConfig.isEnabled
            prefs[PreferencesKeys.HEADPHONE_TARGET_CURVE] = config.headphoneCorrectionConfig.targetCurve.name
            prefs[PreferencesKeys.HEADPHONE_CROSSFEED_ENABLED] = config.headphoneCorrectionConfig.crossfeedEnabled
            prefs[PreferencesKeys.STEREO_WIDTH_ENABLED] = config.stereoWidthConfig.isEnabled
            prefs[PreferencesKeys.STEREO_WIDTH_MODE] = config.stereoWidthConfig.mode.name
            prefs[PreferencesKeys.STEREO_WIDTH_PERCENT] = config.stereoWidthConfig.customWidthPercent
            prefs[PreferencesKeys.STEREO_HAAS_DELAY_MS] = config.stereoWidthConfig.haasDelayMs
            prefs[PreferencesKeys.STEREO_MID_LEVEL_DB] = config.stereoWidthConfig.midLevelDb
            prefs[PreferencesKeys.STEREO_SIDE_LEVEL_DB] = config.stereoWidthConfig.sideLevelDb
            prefs[PreferencesKeys.SLEEP_DRIFT_PROFILE] = config.sleepDriftProfile.name
            prefs[PreferencesKeys.DITHER_MODE] = config.ditherMode.name
            prefs[PreferencesKeys.DYNAMIC_LOUDNESS_ENABLED] = config.dynamicLoudnessConfig.isEnabled
            prefs[PreferencesKeys.DYNAMIC_LOUDNESS_MODE] = config.dynamicLoudnessConfig.mode.name
            prefs[PreferencesKeys.DYNAMIC_LOUDNESS_INTENSITY] = config.dynamicLoudnessConfig.intensityPercent
            prefs[PreferencesKeys.BEAT_CROSSFADE_ENABLED] = config.beatCrossfadeConfig.isEnabled
            prefs[PreferencesKeys.BEAT_CROSSFADE_MODE] = config.beatCrossfadeConfig.mode.name
            prefs[PreferencesKeys.BEAT_CROSSFADE_MAX_BEND] = config.beatCrossfadeConfig.maxTempoBendPercent
            prefs[PreferencesKeys.SILENCE_TRIM_ENABLED] = config.silenceTrimConfig.isEnabled
            prefs[PreferencesKeys.SILENCE_TRIM_MODE] = config.silenceTrimConfig.mode.name
            prefs[PreferencesKeys.SILENCE_TRIM_THRESHOLD_DB] = config.silenceTrimConfig.thresholdDb
            prefs[PreferencesKeys.SILENCE_TRIM_MIN_MS] = config.silenceTrimConfig.minDurationMs
            prefs[PreferencesKeys.RESAMPLING_FILTER_MODE] = config.resamplingFilterConfig.mode.name
            prefs[PreferencesKeys.RESAMPLING_FILTER_CUTOFF] = config.resamplingFilterConfig.cutoffRatio
            prefs[PreferencesKeys.PHASE_GUARD_ENABLED] = config.phaseMeterConfig.isEnabled
            prefs[PreferencesKeys.PHASE_GUARD_MODE] = config.phaseMeterConfig.guardMode.name
            prefs[PreferencesKeys.PHASE_GUARD_THRESHOLD] = config.phaseMeterConfig.minCorrelationThreshold
            prefs[PreferencesKeys.TILT_EQ_ENABLED] = config.tiltEqualizerConfig.isEnabled
            prefs[PreferencesKeys.TILT_EQ_DB] = config.tiltEqualizerConfig.tiltDb
            prefs[PreferencesKeys.TILT_EQ_PIVOT_HZ] = config.tiltEqualizerConfig.pivotHz
            prefs[PreferencesKeys.TILT_EQ_LOUDNESS_COMPENSATE] = config.tiltEqualizerConfig.loudnessCompensate
            prefs[PreferencesKeys.DECLICKER_ENABLED] = config.deClickerConfig.isEnabled
            prefs[PreferencesKeys.DECLICKER_MODE] = config.deClickerConfig.mode.name
            prefs[PreferencesKeys.DECLICKER_SENSITIVITY] = config.deClickerConfig.sensitivityMultiplier
            prefs[PreferencesKeys.DECLICKER_MAX_WIDTH] = config.deClickerConfig.maxClickWidthSamples
            prefs[PreferencesKeys.MID_BASS_CLARITY_ENABLED] = config.midBassClarityConfig.isEnabled
            prefs[PreferencesKeys.MID_BASS_CLARITY_MODE] = config.midBassClarityConfig.mode.name
            prefs[PreferencesKeys.MID_BASS_CLARITY_CENTER_HZ] = config.midBassClarityConfig.centerFrequencyHz
            prefs[PreferencesKeys.MID_BASS_CLARITY_THRESHOLD] = config.midBassClarityConfig.dynamicThresholdPercent
            prefs[PreferencesKeys.XTC_ENABLED] = config.crosstalkCancellationConfig.isEnabled
            prefs[PreferencesKeys.XTC_MODE] = config.crosstalkCancellationConfig.mode.name
            prefs[PreferencesKeys.XTC_DELAY_SAMPLES] = config.crosstalkCancellationConfig.delaySamples
            prefs[PreferencesKeys.XTC_INTENSITY_PERCENT] = config.crosstalkCancellationConfig.cancellationIntensityPercent
            prefs[PreferencesKeys.INFRASONIC_ENABLED] = config.infrasonicFilterConfig.isEnabled
            prefs[PreferencesKeys.INFRASONIC_CUTOFF_HZ] = config.infrasonicFilterConfig.cutoffHz
            prefs[PreferencesKeys.INFRASONIC_SLOPE] = config.infrasonicFilterConfig.slope.name
            prefs[PreferencesKeys.INFRASONIC_DC_BLOCKER] = config.infrasonicFilterConfig.dcBlockerEnabled
            prefs[PreferencesKeys.AIR_BAND_ENABLED] = config.airBandExciterConfig.isEnabled
            prefs[PreferencesKeys.AIR_BAND_MODE] = config.airBandExciterConfig.mode.name
            prefs[PreferencesKeys.AIR_BAND_FREQ] = config.airBandExciterConfig.frequency.name
            prefs[PreferencesKeys.AIR_BAND_BOOST_DB] = config.airBandExciterConfig.boostDb
            prefs[PreferencesKeys.AIR_BAND_HARMONICS] = config.airBandExciterConfig.harmonicExcitationPercent
            prefs[PreferencesKeys.SUB_BASS_SYNTH_ENABLED] = config.subHarmonicSynthConfig.isEnabled
            prefs[PreferencesKeys.SUB_BASS_SYNTH_MODE] = config.subHarmonicSynthConfig.mode.name
            prefs[PreferencesKeys.SUB_BASS_SYNTH_TARGET] = config.subHarmonicSynthConfig.target.name
            prefs[PreferencesKeys.SUB_BASS_SYNTH_INTENSITY] = config.subHarmonicSynthConfig.intensityPercent
            prefs[PreferencesKeys.SUB_BASS_SYNTH_BLEND] = config.subHarmonicSynthConfig.subOctaveBlendPercent
            prefs[PreferencesKeys.DEESSER_ENABLED] = config.deEsserConfig.isEnabled
            prefs[PreferencesKeys.DEESSER_MODE] = config.deEsserConfig.mode.name
            prefs[PreferencesKeys.DEESSER_CENTER_HZ] = config.deEsserConfig.centerFrequencyHz
            prefs[PreferencesKeys.DEESSER_THRESHOLD] = config.deEsserConfig.thresholdPercent
            prefs[PreferencesKeys.DEESSER_MAX_REDUCTION] = config.deEsserConfig.maxReductionDb
            prefs[PreferencesKeys.TRUE_PEAK_ENABLED] = config.truePeakGuardConfig.isEnabled
            prefs[PreferencesKeys.TRUE_PEAK_MODE] = config.truePeakGuardConfig.mode.name
            prefs[PreferencesKeys.TRUE_PEAK_CEILING] = config.truePeakGuardConfig.ceiling.name
            prefs[PreferencesKeys.TRUE_PEAK_OVERSAMPLE_4X] = config.truePeakGuardConfig.oversample4xEnabled
            prefs[PreferencesKeys.ULTRASONIC_ENABLED] = config.ultrasonicFilterConfig.isEnabled
            prefs[PreferencesKeys.ULTRASONIC_MODE] = config.ultrasonicFilterConfig.mode.name
            prefs[PreferencesKeys.ULTRASONIC_TOPOLOGY] = config.ultrasonicFilterConfig.topology.name
            prefs[PreferencesKeys.ULTRASONIC_CUTOFF_HZ] = config.ultrasonicFilterConfig.cutoffHz
            prefs[PreferencesKeys.TUBE_SATURATION_ENABLED] = config.tubeSaturationConfig.isEnabled
            prefs[PreferencesKeys.TUBE_SATURATION_PROFILE] = config.tubeSaturationConfig.profile.name
            prefs[PreferencesKeys.TUBE_SATURATION_TOPOLOGY] = config.tubeSaturationConfig.topology.name
            prefs[PreferencesKeys.TUBE_SATURATION_DRIVE] = config.tubeSaturationConfig.drive
            prefs[PreferencesKeys.TUBE_SATURATION_BIAS] = config.tubeSaturationConfig.bias
            prefs[PreferencesKeys.TUBE_SATURATION_MAKEUP_GAIN] = config.tubeSaturationConfig.autoMakeupGain
            prefs[PreferencesKeys.TRANSIENT_SHAPER_ENABLED] = config.transientShaperConfig.isEnabled
            prefs[PreferencesKeys.TRANSIENT_SHAPER_PRESET] = config.transientShaperConfig.preset.name
            prefs[PreferencesKeys.TRANSIENT_SHAPER_FOCUS] = config.transientShaperConfig.bandFocus.name
            prefs[PreferencesKeys.TRANSIENT_SHAPER_ATTACK] = config.transientShaperConfig.attackBoost
            prefs[PreferencesKeys.TRANSIENT_SHAPER_SUSTAIN] = config.transientShaperConfig.sustainBoost
            prefs[PreferencesKeys.TRANSIENT_SHAPER_LIMIT] = config.transientShaperConfig.autoOutputLimit
            prefs[PreferencesKeys.STEREO_IMAGER_ENABLED] = config.dynamicStereoImagerConfig.isEnabled
            prefs[PreferencesKeys.STEREO_IMAGER_PRESET] = config.dynamicStereoImagerConfig.preset.name
            prefs[PreferencesKeys.STEREO_IMAGER_WIDTH] = config.dynamicStereoImagerConfig.stereoWidth
            prefs[PreferencesKeys.STEREO_IMAGER_BASS_CUTOFF] = config.dynamicStereoImagerConfig.monoBassCutoffHz
            prefs[PreferencesKeys.STEREO_IMAGER_AIR_SPREAD] = config.dynamicStereoImagerConfig.highBandAirSpread
            prefs[PreferencesKeys.STEREO_IMAGER_MONO_GUARD] = config.dynamicStereoImagerConfig.monoGuardActive
            prefs[PreferencesKeys.TAPE_HYSTERESIS_ENABLED] = config.tapeHysteresisConfig.isEnabled
            prefs[PreferencesKeys.TAPE_HYSTERESIS_PRESET] = config.tapeHysteresisConfig.preset.name
            prefs[PreferencesKeys.TAPE_HYSTERESIS_SPEED] = config.tapeHysteresisConfig.speed.name
            prefs[PreferencesKeys.TAPE_HYSTERESIS_DRIVE] = config.tapeHysteresisConfig.tapeDrive
            prefs[PreferencesKeys.TAPE_HYSTERESIS_HEAD_BUMP] = config.tapeHysteresisConfig.headBumpGainDb
            prefs[PreferencesKeys.TAPE_HYSTERESIS_WOW_FLUTTER] = config.tapeHysteresisConfig.wowFlutterDepth
            prefs[PreferencesKeys.TAPE_HYSTERESIS_NOISE_REDUCTION] = config.tapeHysteresisConfig.tapeHissReduction
            prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_ENABLED] = config.mainsHumIsolatorConfig.isEnabled
            prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_PRESET] = config.mainsHumIsolatorConfig.preset.name
            prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_FREQUENCY] = config.mainsHumIsolatorConfig.frequency.name
            prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_HARMONIC_DEPTH] = config.mainsHumIsolatorConfig.harmonicDepth.name
            prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_NOTCH_Q] = config.mainsHumIsolatorConfig.notchQ
            prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_NOTCH_DEPTH] = config.mainsHumIsolatorConfig.notchDepthDb
            prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_ADAPTIVE_MASKING] = config.mainsHumIsolatorConfig.adaptiveMasking
            prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_DC_BLOCKER] = config.mainsHumIsolatorConfig.dcBlockerEnabled
            prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_ENABLED] = config.acousticFeedbackConfig.isEnabled
            prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_PRESET] = config.acousticFeedbackConfig.preset.name
            prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_MODE] = config.acousticFeedbackConfig.mode.name
            prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_SENSITIVITY] = config.acousticFeedbackConfig.sensitivityThresholdDb
            prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_NOTCHES] = config.acousticFeedbackConfig.activeNotchesCount
            prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_NOTCH_Q] = config.acousticFeedbackConfig.notchSharpnessQ
            prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_DEPTH] = config.acousticFeedbackConfig.suppressionDepthDb
            prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_PANIC_CLAMP] = config.acousticFeedbackConfig.fastPanicClamp
            prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_ENABLED] = config.soundstageElevationConfig.isEnabled
            prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_PRESET] = config.soundstageElevationConfig.preset.name
            prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_ANGLE] = config.soundstageElevationConfig.elevationAngle.name
            prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_INTENSITY] = config.soundstageElevationConfig.elevationIntensity
            prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_CEILING_MS] = config.soundstageElevationConfig.ceilingReflectionMs
            prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_PINNA_DEPTH] = config.soundstageElevationConfig.pinnaNotchDepthDb
            prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_DIFFUSE_SPREAD] = config.soundstageElevationConfig.diffuseCeilingSpread
            prefs[PreferencesKeys.MONO_BASS_ALIGN_ENABLED] = config.monoBassAlignConfig.isEnabled
            prefs[PreferencesKeys.MONO_BASS_ALIGN_PRESET] = config.monoBassAlignConfig.preset.name
            prefs[PreferencesKeys.MONO_BASS_ALIGN_CROSSOVER] = config.monoBassAlignConfig.crossover.name
            prefs[PreferencesKeys.MONO_BASS_ALIGN_SLOPE] = config.monoBassAlignConfig.slope.name
            prefs[PreferencesKeys.MONO_BASS_ALIGN_AUTO_PHASE] = config.monoBassAlignConfig.autoPhaseAlign
            prefs[PreferencesKeys.MONO_BASS_ALIGN_PUNCH_GAIN] = config.monoBassAlignConfig.subPunchGainDb
            prefs[PreferencesKeys.MONO_BASS_ALIGN_SIDE_SOLO] = config.monoBassAlignConfig.sideSoloAudition
            prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_ENABLED] = config.preampNoiseFlattenerConfig.isEnabled
            prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_PRESET] = config.preampNoiseFlattenerConfig.preset.name
            prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_PROFILE] = config.preampNoiseFlattenerConfig.profile.name
            prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_MODE] = config.preampNoiseFlattenerConfig.mode.name
            prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_FLATTEN_DEPTH] = config.preampNoiseFlattenerConfig.flattenDepth
            prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_THRESHOLD_DB] = config.preampNoiseFlattenerConfig.downwardThresholdDb
            prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_DITHER_LEVEL] = config.preampNoiseFlattenerConfig.thermalDitherLevel
            prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_HF_TILT_DB] = config.preampNoiseFlattenerConfig.hfTiltDb
            prefs[PreferencesKeys.SLEW_RATE_LIMITER_ENABLED] = config.slewRateLimiterConfig.isEnabled
            prefs[PreferencesKeys.SLEW_RATE_LIMITER_PRESET] = config.slewRateLimiterConfig.preset.name
            prefs[PreferencesKeys.SLEW_RATE_LIMITER_SPEED] = config.slewRateLimiterConfig.speed.name
            prefs[PreferencesKeys.SLEW_RATE_LIMITER_MODE] = config.slewRateLimiterConfig.mode.name
            prefs[PreferencesKeys.SLEW_RATE_LIMITER_DAMPING] = config.slewRateLimiterConfig.dampingRatio
            prefs[PreferencesKeys.SLEW_RATE_LIMITER_PEAK_THRESHOLD] = config.slewRateLimiterConfig.peakThreshold
        }
    }

    suspend fun setDitherMode(mode: DitherMode) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DITHER_MODE] = mode.name
        }
    }

    suspend fun updateDynamicLoudnessConfig(config: DynamicLoudnessConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DYNAMIC_LOUDNESS_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.DYNAMIC_LOUDNESS_MODE] = config.mode.name
            prefs[PreferencesKeys.DYNAMIC_LOUDNESS_INTENSITY] = config.intensityPercent
        }
    }

    suspend fun updateBeatCrossfadeConfig(config: BeatCrossfadeConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.BEAT_CROSSFADE_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.BEAT_CROSSFADE_MODE] = config.mode.name
            prefs[PreferencesKeys.BEAT_CROSSFADE_MAX_BEND] = config.maxTempoBendPercent
        }
    }

    suspend fun updateSilenceTrimConfig(config: SilenceTrimConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SILENCE_TRIM_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.SILENCE_TRIM_MODE] = config.mode.name
            prefs[PreferencesKeys.SILENCE_TRIM_THRESHOLD_DB] = config.thresholdDb
            prefs[PreferencesKeys.SILENCE_TRIM_MIN_MS] = config.minDurationMs
        }
    }

    suspend fun updateResamplingFilterConfig(config: ResamplingFilterConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.RESAMPLING_FILTER_MODE] = config.mode.name
            prefs[PreferencesKeys.RESAMPLING_FILTER_CUTOFF] = config.cutoffRatio
        }
    }

    suspend fun updatePhaseMeterConfig(config: PhaseMeterConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.PHASE_GUARD_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.PHASE_GUARD_MODE] = config.guardMode.name
            prefs[PreferencesKeys.PHASE_GUARD_THRESHOLD] = config.minCorrelationThreshold
        }
    }

    suspend fun updateTiltEqualizerConfig(config: TiltEqualizerConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.TILT_EQ_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.TILT_EQ_DB] = config.tiltDb
            prefs[PreferencesKeys.TILT_EQ_PIVOT_HZ] = config.pivotHz
            prefs[PreferencesKeys.TILT_EQ_LOUDNESS_COMPENSATE] = config.loudnessCompensate
        }
    }

    suspend fun updateDeClickerConfig(config: DeClickerConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DECLICKER_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.DECLICKER_MODE] = config.mode.name
            prefs[PreferencesKeys.DECLICKER_SENSITIVITY] = config.sensitivityMultiplier
            prefs[PreferencesKeys.DECLICKER_MAX_WIDTH] = config.maxClickWidthSamples
        }
    }

    suspend fun updateMidBassClarityConfig(config: MidBassClarityConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.MID_BASS_CLARITY_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.MID_BASS_CLARITY_MODE] = config.mode.name
            prefs[PreferencesKeys.MID_BASS_CLARITY_CENTER_HZ] = config.centerFrequencyHz
            prefs[PreferencesKeys.MID_BASS_CLARITY_THRESHOLD] = config.dynamicThresholdPercent
        }
    }

    suspend fun updateCrosstalkCancellationConfig(config: CrosstalkCancellationConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.XTC_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.XTC_MODE] = config.mode.name
            prefs[PreferencesKeys.XTC_DELAY_SAMPLES] = config.delaySamples
            prefs[PreferencesKeys.XTC_INTENSITY_PERCENT] = config.cancellationIntensityPercent
        }
    }

    suspend fun updateInfrasonicFilterConfig(config: InfrasonicFilterConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.INFRASONIC_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.INFRASONIC_CUTOFF_HZ] = config.cutoffHz
            prefs[PreferencesKeys.INFRASONIC_SLOPE] = config.slope.name
            prefs[PreferencesKeys.INFRASONIC_DC_BLOCKER] = config.dcBlockerEnabled
        }
    }

    suspend fun updateAirBandExciterConfig(config: AirBandExciterConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.AIR_BAND_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.AIR_BAND_MODE] = config.mode.name
            prefs[PreferencesKeys.AIR_BAND_FREQ] = config.frequency.name
            prefs[PreferencesKeys.AIR_BAND_BOOST_DB] = config.boostDb
            prefs[PreferencesKeys.AIR_BAND_HARMONICS] = config.harmonicExcitationPercent
        }
    }

    suspend fun updateSubHarmonicSynthConfig(config: SubHarmonicSynthConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SUB_BASS_SYNTH_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.SUB_BASS_SYNTH_MODE] = config.mode.name
            prefs[PreferencesKeys.SUB_BASS_SYNTH_TARGET] = config.target.name
            prefs[PreferencesKeys.SUB_BASS_SYNTH_INTENSITY] = config.intensityPercent
            prefs[PreferencesKeys.SUB_BASS_SYNTH_BLEND] = config.subOctaveBlendPercent
        }
    }

    suspend fun updateDeEsserConfig(config: DeEsserConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DEESSER_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.DEESSER_MODE] = config.mode.name
            prefs[PreferencesKeys.DEESSER_CENTER_HZ] = config.centerFrequencyHz
            prefs[PreferencesKeys.DEESSER_THRESHOLD] = config.thresholdPercent
            prefs[PreferencesKeys.DEESSER_MAX_REDUCTION] = config.maxReductionDb
        }
    }

    suspend fun updateTruePeakGuardConfig(config: TruePeakGuardConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.TRUE_PEAK_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.TRUE_PEAK_MODE] = config.mode.name
            prefs[PreferencesKeys.TRUE_PEAK_CEILING] = config.ceiling.name
            prefs[PreferencesKeys.TRUE_PEAK_OVERSAMPLE_4X] = config.oversample4xEnabled
        }
    }

    suspend fun updateUltrasonicFilterConfig(config: UltrasonicFilterConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ULTRASONIC_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.ULTRASONIC_MODE] = config.mode.name
            prefs[PreferencesKeys.ULTRASONIC_TOPOLOGY] = config.topology.name
            prefs[PreferencesKeys.ULTRASONIC_CUTOFF_HZ] = config.cutoffHz
        }
    }

    suspend fun updateTubeSaturationConfig(config: TubeSaturationConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.TUBE_SATURATION_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.TUBE_SATURATION_PROFILE] = config.profile.name
            prefs[PreferencesKeys.TUBE_SATURATION_TOPOLOGY] = config.topology.name
            prefs[PreferencesKeys.TUBE_SATURATION_DRIVE] = config.drive
            prefs[PreferencesKeys.TUBE_SATURATION_BIAS] = config.bias
            prefs[PreferencesKeys.TUBE_SATURATION_MAKEUP_GAIN] = config.autoMakeupGain
        }
    }

    suspend fun updateTransientShaperConfig(config: TransientShaperConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.TRANSIENT_SHAPER_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.TRANSIENT_SHAPER_PRESET] = config.preset.name
            prefs[PreferencesKeys.TRANSIENT_SHAPER_FOCUS] = config.bandFocus.name
            prefs[PreferencesKeys.TRANSIENT_SHAPER_ATTACK] = config.attackBoost
            prefs[PreferencesKeys.TRANSIENT_SHAPER_SUSTAIN] = config.sustainBoost
            prefs[PreferencesKeys.TRANSIENT_SHAPER_LIMIT] = config.autoOutputLimit
        }
    }

    suspend fun updateDynamicStereoImagerConfig(config: DynamicStereoImagerConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.STEREO_IMAGER_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.STEREO_IMAGER_PRESET] = config.preset.name
            prefs[PreferencesKeys.STEREO_IMAGER_WIDTH] = config.stereoWidth
            prefs[PreferencesKeys.STEREO_IMAGER_BASS_CUTOFF] = config.monoBassCutoffHz
            prefs[PreferencesKeys.STEREO_IMAGER_AIR_SPREAD] = config.highBandAirSpread
            prefs[PreferencesKeys.STEREO_IMAGER_MONO_GUARD] = config.monoGuardActive
        }
    }

    suspend fun updateTapeHysteresisConfig(config: TapeHysteresisConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.TAPE_HYSTERESIS_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.TAPE_HYSTERESIS_PRESET] = config.preset.name
            prefs[PreferencesKeys.TAPE_HYSTERESIS_SPEED] = config.speed.name
            prefs[PreferencesKeys.TAPE_HYSTERESIS_DRIVE] = config.tapeDrive
            prefs[PreferencesKeys.TAPE_HYSTERESIS_HEAD_BUMP] = config.headBumpGainDb
            prefs[PreferencesKeys.TAPE_HYSTERESIS_WOW_FLUTTER] = config.wowFlutterDepth
            prefs[PreferencesKeys.TAPE_HYSTERESIS_NOISE_REDUCTION] = config.tapeHissReduction
        }
    }

    suspend fun updateMainsHumIsolatorConfig(config: MainsHumIsolatorConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_PRESET] = config.preset.name
            prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_FREQUENCY] = config.frequency.name
            prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_HARMONIC_DEPTH] = config.harmonicDepth.name
            prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_NOTCH_Q] = config.notchQ
            prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_NOTCH_DEPTH] = config.notchDepthDb
            prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_ADAPTIVE_MASKING] = config.adaptiveMasking
            prefs[PreferencesKeys.MAINS_HUM_ISOLATOR_DC_BLOCKER] = config.dcBlockerEnabled
        }
    }

    suspend fun updateAcousticFeedbackConfig(config: AcousticFeedbackConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_PRESET] = config.preset.name
            prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_MODE] = config.mode.name
            prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_SENSITIVITY] = config.sensitivityThresholdDb
            prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_NOTCHES] = config.activeNotchesCount
            prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_NOTCH_Q] = config.notchSharpnessQ
            prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_DEPTH] = config.suppressionDepthDb
            prefs[PreferencesKeys.ACOUSTIC_FEEDBACK_PANIC_CLAMP] = config.fastPanicClamp
        }
    }

    suspend fun updateSoundstageElevationConfig(config: SoundstageElevationConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_PRESET] = config.preset.name
            prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_ANGLE] = config.elevationAngle.name
            prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_INTENSITY] = config.elevationIntensity
            prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_CEILING_MS] = config.ceilingReflectionMs
            prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_PINNA_DEPTH] = config.pinnaNotchDepthDb
            prefs[PreferencesKeys.SOUNDSTAGE_ELEVATION_DIFFUSE_SPREAD] = config.diffuseCeilingSpread
        }
    }

    suspend fun updateMonoBassAlignConfig(config: MonoBassAlignConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.MONO_BASS_ALIGN_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.MONO_BASS_ALIGN_PRESET] = config.preset.name
            prefs[PreferencesKeys.MONO_BASS_ALIGN_CROSSOVER] = config.crossover.name
            prefs[PreferencesKeys.MONO_BASS_ALIGN_SLOPE] = config.slope.name
            prefs[PreferencesKeys.MONO_BASS_ALIGN_AUTO_PHASE] = config.autoPhaseAlign
            prefs[PreferencesKeys.MONO_BASS_ALIGN_PUNCH_GAIN] = config.subPunchGainDb
            prefs[PreferencesKeys.MONO_BASS_ALIGN_SIDE_SOLO] = config.sideSoloAudition
        }
    }

    suspend fun updatePreampNoiseFlattenerConfig(config: PreampNoiseFlattenerConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_PRESET] = config.preset.name
            prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_PROFILE] = config.profile.name
            prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_MODE] = config.mode.name
            prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_FLATTEN_DEPTH] = config.flattenDepth
            prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_THRESHOLD_DB] = config.downwardThresholdDb
            prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_DITHER_LEVEL] = config.thermalDitherLevel
            prefs[PreferencesKeys.PREAMP_NOISE_FLATTENER_HF_TILT_DB] = config.hfTiltDb
        }
    }

    suspend fun updateSlewRateLimiterConfig(config: SlewRateLimiterConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SLEW_RATE_LIMITER_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.SLEW_RATE_LIMITER_PRESET] = config.preset.name
            prefs[PreferencesKeys.SLEW_RATE_LIMITER_SPEED] = config.speed.name
            prefs[PreferencesKeys.SLEW_RATE_LIMITER_MODE] = config.mode.name
            prefs[PreferencesKeys.SLEW_RATE_LIMITER_DAMPING] = config.dampingRatio
            prefs[PreferencesKeys.SLEW_RATE_LIMITER_PEAK_THRESHOLD] = config.peakThreshold
        }
    }

    val preferredContentCountry: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.CONTENT_COUNTRY] ?: "US"
    }

    suspend fun setPreferredContentCountry(country: String) {
        context.dataStore.edit { it[PreferencesKeys.CONTENT_COUNTRY] = country }
    }

    val preferredQuality: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.AUDIO_QUALITY] ?: "HIGH"
    }

    suspend fun setPreferredQuality(quality: String) {
        context.dataStore.edit { it[PreferencesKeys.AUDIO_QUALITY] = quality }
    }

    val equalizerEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.EQUALIZER_ENABLED] ?: true
    }

    suspend fun setEqualizerEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.EQUALIZER_ENABLED] = enabled }
    }

    val equalizerPreset: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.EQUALIZER_PRESET] ?: 0
    }

    suspend fun setEqualizerPreset(preset: Int) {
        context.dataStore.edit { it[PreferencesKeys.EQUALIZER_PRESET] = preset }
    }

    val recentSearches: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[PreferencesKeys.RECENT_SEARCHES] ?: ""
        if (raw.isBlank()) emptyList() else raw.split("|||")
    }

    suspend fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        context.dataStore.edit { prefs ->
            val existing = (prefs[PreferencesKeys.RECENT_SEARCHES] ?: "")
                .split("|||")
                .filter { it.isNotBlank() && it != trimmed }
            val updated = (listOf(trimmed) + existing).take(10)
            prefs[PreferencesKeys.RECENT_SEARCHES] = updated.joinToString("|||")
        }
    }

    suspend fun removeRecentSearch(query: String) {
        context.dataStore.edit { prefs ->
            val existing = (prefs[PreferencesKeys.RECENT_SEARCHES] ?: "")
                .split("|||")
                .filter { it.isNotBlank() && it != query }
            prefs[PreferencesKeys.RECENT_SEARCHES] = existing.joinToString("|||")
        }
    }

    val listeningStreak: Flow<ListeningStreak> = context.dataStore.data.map { prefs ->
        ListeningStreak(
            currentStreakDays = prefs[PreferencesKeys.STREAK_CURRENT_DAYS] ?: 0,
            longestStreakDays = prefs[PreferencesKeys.STREAK_LONGEST_DAYS] ?: 0,
            lastActiveDateEpochDay = prefs[PreferencesKeys.STREAK_LAST_ACTIVE_EPOCH_DAY] ?: 0L
        )
    }

    suspend fun recordActiveDay(epochDay: Long) {
        context.dataStore.edit { prefs ->
            val lastDay = prefs[PreferencesKeys.STREAK_LAST_ACTIVE_EPOCH_DAY] ?: 0L
            val currentStreak = prefs[PreferencesKeys.STREAK_CURRENT_DAYS] ?: 0
            val longestStreak = prefs[PreferencesKeys.STREAK_LONGEST_DAYS] ?: 0

            if (epochDay == lastDay) {
                return@edit
            }

            val newCurrent = if (epochDay == lastDay + 1L) {
                currentStreak + 1
            } else {
                1
            }
            val newLongest = maxOf(longestStreak, newCurrent)

            prefs[PreferencesKeys.STREAK_CURRENT_DAYS] = newCurrent
            prefs[PreferencesKeys.STREAK_LONGEST_DAYS] = newLongest
            prefs[PreferencesKeys.STREAK_LAST_ACTIVE_EPOCH_DAY] = epochDay
        }
    }

    suspend fun clearRecentSearches() {
        context.dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.RECENT_SEARCHES)
        }
    }

    val customEqProfilesRaw: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.CUSTOM_EQ_PROFILES] ?: ""
    }

    suspend fun saveCustomEqProfilesRaw(raw: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.CUSTOM_EQ_PROFILES] = raw
        }
    }

    val djFxConfig: Flow<DjFxConfig> = context.dataStore.data.map { prefs ->
        DjFxConfig(
            preset = runCatching {
                DjPreset.valueOf(prefs[PreferencesKeys.DJ_PRESET] ?: DjPreset.NORMAL.name)
            }.getOrDefault(DjPreset.NORMAL),
            speed = prefs[PreferencesKeys.DJ_SPEED] ?: 1.0f,
            semitones = prefs[PreferencesKeys.DJ_SEMITONES] ?: 0,
            soundstageWidth = prefs[PreferencesKeys.DJ_SOUNDSTAGE_WIDTH] ?: 1.0f,
            loFiFilterEnabled = prefs[PreferencesKeys.DJ_LOFI_FILTER] ?: false,
            bassBoostDb = prefs[PreferencesKeys.DJ_BASS_BOOST] ?: 0
        )
    }

    suspend fun updateDjFxConfig(config: DjFxConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DJ_PRESET] = config.preset.name
            prefs[PreferencesKeys.DJ_SPEED] = config.speed
            prefs[PreferencesKeys.DJ_SEMITONES] = config.semitones
            prefs[PreferencesKeys.DJ_SOUNDSTAGE_WIDTH] = config.soundstageWidth
            prefs[PreferencesKeys.DJ_LOFI_FILTER] = config.loFiFilterEnabled
            prefs[PreferencesKeys.DJ_BASS_BOOST] = config.bassBoostDb
        }
    }

    val djRememberAcrossTracks: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DJ_REMEMBER_ACROSS_TRACKS] ?: false
    }

    suspend fun setDjRememberAcrossTracks(remember: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DJ_REMEMBER_ACROSS_TRACKS] = remember
        }
    }

    val customSmartPlaylistsRaw: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.CUSTOM_SMART_PLAYLISTS] ?: ""
    }

    suspend fun saveCustomSmartPlaylistsRaw(raw: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.CUSTOM_SMART_PLAYLISTS] = raw
        }
    }
}

