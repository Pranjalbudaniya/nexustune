package com.nexus.tune.core.domain.preload

import com.nexus.tune.core.model.Track

interface AudioPreloader {
    fun preloadTrack(track: Track, resolvedStreamUrl: String)
    fun clearPreload()
}

