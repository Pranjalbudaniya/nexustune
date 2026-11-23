package com.nexus.tune.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nexus.tune.core.model.Track

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
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
    val addedAtTimestamp: Long = System.currentTimeMillis()
)

fun TrackEntity.toModel(): Track = Track(
    id = id,
    title = title,
    artist = artist,
    artistId = artistId,
    album = album,
    albumId = albumId,
    durationSeconds = durationSeconds,
    artworkUrl = artworkUrl,
    streamUrl = streamUrl,
    isExplicit = isExplicit,
    isLiked = isLiked,
    isDownloaded = isDownloaded,
    localFilePath = localFilePath,
    loudnessGainDb = loudnessGainDb
)

fun Track.toEntity(isLikedOverride: Boolean? = null): TrackEntity = TrackEntity(
    id = id,
    title = title,
    artist = artist,
    artistId = artistId,
    album = album,
    albumId = albumId,
    durationSeconds = durationSeconds,
    artworkUrl = artworkUrl,
    streamUrl = streamUrl,
    isExplicit = isExplicit,
    isLiked = isLikedOverride ?: isLiked,
    isDownloaded = isDownloaded,
    localFilePath = localFilePath,
    loudnessGainDb = loudnessGainDb
)

