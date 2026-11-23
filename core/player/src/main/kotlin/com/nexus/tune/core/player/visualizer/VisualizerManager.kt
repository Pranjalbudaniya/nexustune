package com.nexus.tune.core.player.visualizer

import android.media.audiofx.Visualizer
import android.util.Log
import com.nexus.tune.core.model.VisualizerData
import com.nexus.tune.core.model.VisualizerMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class VisualizerManager private constructor() {

    private val _visualizerData = MutableStateFlow(VisualizerData())
    val visualizerData: StateFlow<VisualizerData> = _visualizerData.asStateFlow()

    private var visualizerMode: VisualizerMode = VisualizerMode.SPECTRUM_BARS
    private var nativeVisualizer: Visualizer? = null
    private var currentSessionId: Int = 0
    private var isPlaying: Boolean = false

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var simulationJob: Job? = null

    private val smoothedMagnitudes = FloatArray(NUM_BANDS) { 0f }

    companion object {
        const val NUM_BANDS = 32
        val instance: VisualizerManager by lazy { VisualizerManager() }
    }

    init {
        startUpdateLoop()
    }

    fun setMode(mode: VisualizerMode) {
        visualizerMode = mode
        if (mode == VisualizerMode.OFF) {
            _visualizerData.value = VisualizerData()
        }
    }

    fun updatePlayback(playing: Boolean) {
        isPlaying = playing
    }

    fun attachToAudioSession(sessionId: Int) {
        if (sessionId == 0 || sessionId == currentSessionId) return
        releaseNativeVisualizer()
        currentSessionId = sessionId

        try {
            val captureSizeRange = Visualizer.getCaptureSizeRange()
            val captureSize = 256.coerceIn(captureSizeRange[0], captureSizeRange[1])
            nativeVisualizer = Visualizer(sessionId).apply {
                setCaptureSize(captureSize)
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            // Waveform capture if needed
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            if (fft != null && visualizerMode != VisualizerMode.OFF) {
                                processFft(fft)
                            }
                        }
                    },
                    Visualizer.getMaxCaptureRate() / 2,
                    false,
                    true
                )
                enabled = true
            }
        } catch (e: Throwable) {
            try {
                Log.w("VisualizerManager", "Native Visualizer init not supported/permitted: ${e.message}")
            } catch (_: Throwable) {
                // Ignore log failure in unit tests
            }
            nativeVisualizer = null
        }
    }

    private fun processFft(fft: ByteArray) {
        val n = fft.size
        if (n < 4) return

        val rawBands = FloatArray(NUM_BANDS) { 0f }
        val halfN = n / 2
        val step = (halfN / NUM_BANDS).coerceAtLeast(1)

        var totalAmp = 0f
        for (i in 0 until NUM_BANDS) {
            val k = (i * step).coerceAtMost(halfN - 1)
            val r = fft[2 * k].toFloat()
            val im = fft[2 * k + 1].toFloat()
            val magnitude = hypot(r, im) / 128f
            rawBands[i] = magnitude.coerceIn(0f, 1f)
            totalAmp += rawBands[i]
        }

        // Apply exponential moving average smoothing
        for (i in 0 until NUM_BANDS) {
            smoothedMagnitudes[i] = smoothedMagnitudes[i] * 0.6f + rawBands[i] * 0.4f
        }

        val avgAmp = (totalAmp / NUM_BANDS).coerceIn(0f, 1f)
        _visualizerData.value = VisualizerData(
            magnitudes = smoothedMagnitudes.copyOf(),
            amplitude = avgAmp
        )
    }

    private fun startUpdateLoop() {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            var phase = 0.0
            while (isActive) {
                delay(33L) // ~30Hz update loop for smoothing / fallback simulation
                if (visualizerMode == VisualizerMode.OFF) {
                    continue
                }

                if (!isPlaying) {
                    // Decay to zero when paused
                    var active = false
                    for (i in 0 until NUM_BANDS) {
                        smoothedMagnitudes[i] *= 0.85f
                        if (smoothedMagnitudes[i] > 0.01f) active = true
                    }
                    if (active) {
                        _visualizerData.value = VisualizerData(
                            magnitudes = smoothedMagnitudes.copyOf(),
                            amplitude = _visualizerData.value.amplitude * 0.85f
                        )
                    }
                    continue
                }

                // If native visualizer is not producing or unavailable, run organic rhythmic simulation
                if (nativeVisualizer == null) {
                    phase += 0.15
                    val beatPulse = (sin(phase * 1.5) * 0.5 + 0.5).toFloat()
                    val midPulse = (cos(phase * 2.3) * 0.5 + 0.5).toFloat()
                    val highFlutter = (sin(phase * 4.1) * 0.5 + 0.5).toFloat()

                    var totalAmp = 0f
                    for (i in 0 until NUM_BANDS) {
                        val freqFactor = (NUM_BANDS - i).toFloat() / NUM_BANDS
                        val targetMag = when {
                            i < 6 -> beatPulse * 0.85f * freqFactor + 0.15f
                            i < 18 -> midPulse * 0.7f * freqFactor + 0.1f
                            else -> highFlutter * 0.5f * freqFactor + 0.05f
                        }.coerceIn(0f, 1f)

                        smoothedMagnitudes[i] = smoothedMagnitudes[i] * 0.7f + targetMag * 0.3f
                        totalAmp += smoothedMagnitudes[i]
                    }

                    _visualizerData.value = VisualizerData(
                        magnitudes = smoothedMagnitudes.copyOf(),
                        amplitude = (totalAmp / NUM_BANDS).coerceIn(0f, 1f)
                    )
                }
            }
        }
    }

    fun releaseNativeVisualizer() {
        try {
            nativeVisualizer?.enabled = false
            nativeVisualizer?.release()
        } catch (e: Throwable) {
            try {
                Log.w("VisualizerManager", "Error releasing Visualizer: ${e.message}")
            } catch (_: Throwable) {
                // Ignore log failure in unit tests
            }
        } finally {
            nativeVisualizer = null
            currentSessionId = 0
        }
    }

    fun release() {
        releaseNativeVisualizer()
        simulationJob?.cancel()
        _visualizerData.value = VisualizerData()
    }
}
