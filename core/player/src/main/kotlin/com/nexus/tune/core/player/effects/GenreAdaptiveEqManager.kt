package com.nexus.tune.core.player.effects

import com.nexus.tune.core.model.EqualizerPreset
import com.nexus.tune.core.model.Track

class GenreAdaptiveEqManager(
    private val audioEffects: NexusAudioEffects = NexusAudioEffects.instance
) {
    fun detectGenrePreset(track: Track): EqualizerPreset {
        val query = "${track.title} ${track.artist} ${track.album.orEmpty()}".lowercase()
        return when {
            query.contains("remix") || query.contains("club") || query.contains("edm") ||
            query.contains("dance") || query.contains("techno") || query.contains("electro") -> EqualizerPreset.ELECTRONIC

            query.contains("acoustic") || query.contains("unplugged") || query.contains("folk") -> EqualizerPreset.ACOUSTIC

            query.contains("rock") || query.contains("metal") || query.contains("guitar") ||
            query.contains("band") -> EqualizerPreset.ROCK

            query.contains("hip hop") || query.contains("rap") || query.contains("trap") ||
            query.contains("beat") -> EqualizerPreset.HIP_HOP

            query.contains("jazz") || query.contains("blues") || query.contains("sax") ||
            query.contains("swing") -> EqualizerPreset.JAZZ

            query.contains("symphony") || query.contains("orchestra") || query.contains("piano") ||
            query.contains("classical") || query.contains("concerto") -> EqualizerPreset.CLASSICAL

            query.contains("bass") || query.contains("dubstep") -> EqualizerPreset.BASS_BOOST

            else -> EqualizerPreset.FLAT
        }
    }

    fun applyAdaptivePreset(track: Track): EqualizerPreset {
        val detected = detectGenrePreset(track)
        audioEffects.applyPreset(detected)
        return detected
    }
}
