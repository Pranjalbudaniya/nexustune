package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import kotlin.math.abs

object HarmonicMixingEngine {

    fun deriveHarmonicProfile(track: Track): TrackHarmonicProfile {
        val keys = CamelotKey.entries
        val hash = abs(track.id.hashCode() * 31 + track.title.hashCode() + track.artist.hashCode())
        val keyIndex = hash % keys.size
        val key = keys[keyIndex]

        // BPM range: 80 - 150 BPM
        val bpmSeed = abs(track.durationSeconds.hashCode() xor track.id.hashCode())
        val bpm = 85 + (bpmSeed % 55)

        // Energy level: 45 - 95
        val energySeed = abs(track.title.hashCode() xor track.artist.hashCode())
        val energy = 45 + (energySeed % 51)

        return TrackHarmonicProfile(
            trackId = track.id,
            key = key,
            bpm = bpm,
            energyLevel = energy
        )
    }

    fun calculateKeyRelationship(key1: CamelotKey, key2: CamelotKey): Pair<HarmonicRelationship, Int> {
        if (key1 == key2) {
            return Pair(HarmonicRelationship.PERFECT_MATCH, 100)
        }

        if (key1.number == key2.number && key1.scale != key2.scale) {
            return Pair(HarmonicRelationship.RELATIVE_KEY, 90)
        }

        val stepDiff = (key2.number - key1.number + 12) % 12

        if (key1.scale == key2.scale) {
            return when (stepDiff) {
                1, 11 -> Pair(HarmonicRelationship.ADJACENT_STEP, 85)
                2 -> Pair(HarmonicRelationship.ENERGY_BOOST, 80)
                10 -> Pair(HarmonicRelationship.ENERGY_DROP, 75)
                else -> Pair(HarmonicRelationship.DISSONANT, 25)
            }
        } else {
            // Cross-scale (minor to major or major to minor)
            return when (stepDiff) {
                1, 11 -> Pair(HarmonicRelationship.ADJACENT_STEP, 70)
                else -> Pair(HarmonicRelationship.DISSONANT, 25)
            }
        }
    }

    fun calculateTempoCompatibility(bpm1: Int, bpm2: Int): TempoCompatibility {
        val diff = abs(bpm1 - bpm2)
        if (diff <= 3) return TempoCompatibility.SEAMLESS
        if (diff <= 8) return TempoCompatibility.SMOOTH

        val high = maxOf(bpm1, bpm2).toDouble()
        val low = minOf(bpm1, bpm2).toDouble()
        if (low > 0) {
            val ratio = high / low
            if (ratio in 1.95..2.05) {
                return TempoCompatibility.HALF_DOUBLE
            }
        }

        if (diff <= 15) return TempoCompatibility.MODERATE_SHIFT
        return TempoCompatibility.TEMPO_JUMP
    }

    fun evaluateTransition(
        source: TrackHarmonicProfile,
        target: TrackHarmonicProfile
    ): HarmonicTransition {
        val (relationship, baseKeyScore) = calculateKeyRelationship(source.key, target.key)
        val tempoComp = calculateTempoCompatibility(source.bpm, target.bpm)

        val finalScore = when (tempoComp) {
            TempoCompatibility.SEAMLESS,
            TempoCompatibility.SMOOTH,
            TempoCompatibility.HALF_DOUBLE -> baseKeyScore
            TempoCompatibility.MODERATE_SHIFT -> (baseKeyScore * 0.9f).toInt()
            TempoCompatibility.TEMPO_JUMP -> (baseKeyScore * 0.75f).toInt()
        }.coerceIn(0, 100)

        val crossfade = when (relationship) {
            HarmonicRelationship.PERFECT_MATCH,
            HarmonicRelationship.RELATIVE_KEY -> 8
            HarmonicRelationship.ADJACENT_STEP,
            HarmonicRelationship.ENERGY_BOOST -> 6
            HarmonicRelationship.ENERGY_DROP -> 5
            HarmonicRelationship.DISSONANT -> 2 // Quick cut to avoid clashing dissonance
        }

        return HarmonicTransition(
            sourceProfile = source,
            targetProfile = target,
            relationship = relationship,
            compatibilityPercent = finalScore,
            tempoCompatibility = tempoComp,
            recommendedCrossfadeSeconds = crossfade
        )
    }

    fun harmonizeQueue(currentTrack: Track, upcomingQueue: List<Track>): List<Track> {
        if (upcomingQueue.size <= 1) return upcomingQueue

        val remaining = upcomingQueue.toMutableList()
        val result = mutableListOf<Track>()
        var currentProfile = deriveHarmonicProfile(currentTrack)

        while (remaining.isNotEmpty()) {
            var bestIndex = 0
            var highestScore = -1

            for (i in remaining.indices) {
                val candidateProfile = deriveHarmonicProfile(remaining[i])
                val transition = evaluateTransition(currentProfile, candidateProfile)
                if (transition.compatibilityPercent > highestScore) {
                    highestScore = transition.compatibilityPercent
                    bestIndex = i
                }
            }

            val bestTrack = remaining.removeAt(bestIndex)
            result.add(bestTrack)
            currentProfile = deriveHarmonicProfile(bestTrack)
        }

        return result
    }
}

