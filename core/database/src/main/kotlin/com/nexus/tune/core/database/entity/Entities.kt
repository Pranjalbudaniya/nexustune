package com.nexus.tune.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queue")
data class QueueEntity(
    @PrimaryKey val queueOrder: Int,
    val trackId: String,
    val title: String,
    val artist: String,
    val album: String?,
    val durationSeconds: Long,
    val artworkUrl: String?,
    val streamUrl: String?
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val trackId: String,
    val title: String,
    val artist: String,
    val album: String?,
    val durationSeconds: Long,
    val artworkUrl: String?,
    val playedAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String?,
    val artworkUrl: String?,
    val trackCount: Int,
    val isUserCreated: Boolean = true,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_tracks", primaryKeys = ["playlistId", "trackId", "trackOrder"])
data class PlaylistTrackCrossRef(
    val playlistId: String,
    val trackId: String,
    val trackOrder: Int
)

