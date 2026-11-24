package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import kotlin.math.*

object BeatSyncCrossfadeEngine {

    /**
     * Calculates the exact duration in milliseconds for a musical phrase given tempo and bars.
     * Default standard 4/4 meter has 4 beats per bar.
     */
    fun calculatePhraseDurationMs(bpm: Int, bars: Int = 4, beatsPerBar: Int = 4): Long {
        val clampedBpm = bpm.coerceIn(40, 240)
        val totalBeats = bars.coerceAtLeast(1) * beatsPerBar.coerceAtLeast(1)
        val msPerBeat = 60_000.0 / clampedBpm.toDouble()
        return (totalBeats * msPerBeat).roundToLong()
    }

    /**
     * Synthesizes an intelligent transition plan aligning bar boundaries and tempo matching.
     */
    fun calculateTransitionPlan(
        outgoingProfile: TrackHarmonicProfile?,
        incomingProfile: TrackHarmonicProfile?,
        mode: BeatCrossfadeMode,
        maxBendPercent: Int = 6
    ): BeatTransitionPlan {
        val bpmA = outgoingProfile?.bpm ?: 120
        val bpmB = incomingProfile?.bpm ?: 120

        if (mode == BeatCrossfadeMode.OFF) {
            return BeatTransitionPlan(
                outgoingBpm = bpmA,
                incomingBpm = bpmB,
                targetDurationMs = 4000L,
                musicalBars = 0,
                totalBeats = 0,
                tempoRatio = bpmB.toFloat() / bpmA.toFloat(),
                requiresTempoBend = false,
                effectiveRampBpm = bpmA
            )
        }

        val bars = when (mode) {
            BeatCrossfadeMode.SMART_BEAT_SYNC -> {
                val diff = abs(bpmA - bpmB)
                if (diff <= 6) 8 else if (diff <= 16) 4 else 2
            }
            BeatCrossfadeMode.PHRASE_ALIGNED_8BAR,
            BeatCrossfadeMode.BASS_SWAP_CLUB -> 8
            BeatCrossfadeMode.PHRASE_ALIGNED_16BAR -> 16
            BeatCrossfadeMode.OFF -> 0
        }

        val totalBeats = bars * 4
        val avgBpm = (bpmA + bpmB) / 2
        val durationMs = calculatePhraseDurationMs(avgBpm, bars, 4)

        val tempoRatio = bpmB.toFloat() / bpmA.toFloat()
        val percentDiff = (abs(bpmA - bpmB).toFloat() / bpmA.toFloat()) * 100f
        val canBend = percentDiff in 0.2f..(maxBendPercent.toFloat())

        return BeatTransitionPlan(
            outgoingBpm = bpmA,
            incomingBpm = bpmB,
            targetDurationMs = durationMs,
            musicalBars = bars,
            totalBeats = totalBeats,
            tempoRatio = tempoRatio,
            requiresTempoBend = canBend,
            effectiveRampBpm = if (canBend) bpmB else bpmA
        )
    }

    /**
     * Calculates asymmetric bass-swap frequency crossover curves and equal-power mid/high curves.
     * Prevents muddy double-kick frequency cancellation at the 50% transition midpoint.
     */
    fun calculateBassSwapGains(progressFraction: Float): BassSwapCurve {
        val p = progressFraction.coerceIn(0f, 1f)

        // Outgoing bass: drops steeply between 40% and 60% progress
        val outgoingBass = when {
            p < 0.40f -> 0.0f
            p > 0.60f -> -48.0f
            else -> {
                val t = (p - 0.40f) / 0.20f
                (-48.0f * t).coerceIn(-48.0f, 0.0f)
            }
        }

        // Incoming bass: rises steeply between 50% and 70% progress
        val incomingBass = when {
            p < 0.50f -> -48.0f
            p > 0.70f -> 0.0f
            else -> {
                val t = (p - 0.50f) / 0.20f
                (-48.0f * (1.0f - t)).coerceIn(-48.0f, 0.0f)
            }
        }

        // Equal-power mid/high frequency curves
        val outLinear = cos(p * (Math.PI.toFloat() / 2f)).coerceAtLeast(0.0001f)
        val inLinear = sin(p * (Math.PI.toFloat() / 2f)).coerceAtLeast(0.0001f)

        val outgoingMidHigh = (20.0f * log10(outLinear)).coerceIn(-48.0f, 0.0f)
        val incomingMidHigh = (20.0f * log10(inLinear)).coerceIn(-48.0f, 0.0f)

        return BassSwapCurve(
            outgoingBassGainDb = outgoingBass,
            incomingBassGainDb = incomingBass,
            outgoingMidHighGainDb = outgoingMidHigh,
            incomingMidHighGainDb = incomingMidHigh
        )
    }

    /**
     * Interpolates smooth playback speed multiplier to match tempo across the transition.
     */
    fun calculateTempoRamp(
        progressFraction: Float,
        startBpm: Int,
        targetBpm: Int,
        maxBendPercent: Int = 6
    ): Float {
        val p = progressFraction.coerceIn(0f, 1f)
        val percentDiff = (abs(startBpm - targetBpm).toFloat() / startBpm.toFloat()) * 100f
        if (percentDiff > maxBendPercent) {
            return 1.0f
        }

        val totalMultiplier = targetBpm.toFloat() / startBpm.toFloat()
        return 1.0f + ((totalMultiplier - 1.0f) * p)
    }
}

