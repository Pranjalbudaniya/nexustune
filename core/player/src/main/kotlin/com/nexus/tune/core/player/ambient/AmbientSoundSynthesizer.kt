package com.nexus.tune.core.player.ambient

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.nexus.tune.core.domain.ambient.AmbientSoundGenerator
import com.nexus.tune.core.model.AmbientSoundType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AmbientSynthesizerState(
    val isPlaying: Boolean = false,
    val soundType: AmbientSoundType = AmbientSoundType.OFF,
    val volume: Float = 0.5f
)

/**
 * Real-time streaming Ambient Soundscape & Binaural Waves Synthesizer.
 * Drives an [AudioTrack] in streaming mode using [AmbientSoundGenerator] procedural DSP algorithms.
 */
class AmbientSoundSynthesizer(
    private val generator: AmbientSoundGenerator = AmbientSoundGenerator(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    companion object {
        val instance: AmbientSoundSynthesizer by lazy { AmbientSoundSynthesizer() }
        private const val SAMPLE_RATE = 44100
        private const val CHUNK_FRAMES = 2048
    }

    private val _state = MutableStateFlow(AmbientSynthesizerState())
    val state: StateFlow<AmbientSynthesizerState> = _state.asStateFlow()

    private var audioTrack: AudioTrack? = null
    private var synthesisJob: Job? = null

    @Synchronized
    private fun initAudioTrack(): AudioTrack? {
        if (audioTrack != null) return audioTrack
        return runCatching {
            val minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(CHUNK_FRAMES * 4)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(minBuf)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            track.setVolume(_state.value.volume)
            audioTrack = track
            track
        }.getOrNull()
    }

    fun start(soundType: AmbientSoundType? = null, volume: Float? = null) {
        val targetType = soundType ?: _state.value.soundType.takeIf { it != AmbientSoundType.OFF } ?: AmbientSoundType.BROWN_NOISE
        val targetVolume = volume ?: _state.value.volume

        _state.value = _state.value.copy(
            isPlaying = true,
            soundType = targetType,
            volume = targetVolume.coerceIn(0f, 1f)
        )

        synthesisJob?.cancel()
        synthesisJob = coroutineScope.launch {
            val track = initAudioTrack()
            runCatching {
                track?.setVolume(targetVolume)
                track?.play()
            }
            generator.reset()

            while (isActive && _state.value.isPlaying) {
                val currentType = _state.value.soundType
                if (currentType == AmbientSoundType.OFF) {
                    delay(50)
                    continue
                }
                val chunk = generator.generateStereoChunk(currentType, CHUNK_FRAMES, SAMPLE_RATE)
                val written = runCatching {
                    track?.write(chunk, 0, chunk.size) ?: 0
                }.getOrDefault(0)

                if (written < 0) {
                    delay(20)
                } else if (written == 0) {
                    // Throttle in case of simulated/mocked device buffers in tests
                    delay(30)
                }
            }
        }
    }

    fun stop() {
        synthesisJob?.cancel()
        synthesisJob = null
        runCatching {
            audioTrack?.pause()
            audioTrack?.flush()
        }
        generator.reset()
        _state.value = _state.value.copy(isPlaying = false)
    }

    fun togglePlay(soundType: AmbientSoundType? = null) {
        if (_state.value.isPlaying) {
            stop()
        } else {
            start(soundType)
        }
    }

    fun setSoundType(type: AmbientSoundType) {
        _state.value = _state.value.copy(soundType = type)
        generator.reset()
        if (type == AmbientSoundType.OFF && _state.value.isPlaying) {
            stop()
        }
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _state.value = _state.value.copy(volume = clamped)
        runCatching {
            audioTrack?.setVolume(clamped)
        }
    }

    fun release() {
        stop()
        runCatching {
            audioTrack?.release()
            audioTrack = null
        }
    }
}

