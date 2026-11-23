package com.nexus.tune.core.database.dao

import androidx.room.*
import com.nexus.tune.core.database.entity.HistoryEntity
import com.nexus.tune.core.database.entity.PlaylistEntity
import com.nexus.tune.core.database.entity.QueueEntity
import com.nexus.tune.core.database.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks WHERE isLiked = 1 ORDER BY addedAtTimestamp DESC")
    fun getLikedTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isDownloaded = 1")
    fun getDownloadedTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: String): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(track: TrackEntity)

    @Query("UPDATE tracks SET isLiked = :isLiked WHERE id = :id")
    suspend fun updateLiked(id: String, isLiked: Boolean)
}

@Dao
interface QueueDao {
    @Query("SELECT * FROM queue ORDER BY queueOrder ASC")
    fun getQueue(): Flow<List<QueueEntity>>

    @Query("SELECT * FROM queue ORDER BY queueOrder ASC")
    suspend fun getQueueSnapshot(): List<QueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(queue: List<QueueEntity>)

    @Query("DELETE FROM queue")
    suspend fun clearQueue()
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY playedAtTimestamp DESC LIMIT 100")
    fun getHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntity)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAtTimestamp DESC")
    fun getPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE playlists SET artworkUrl = :artworkUrl WHERE id = :id")
    suspend fun updateArtwork(id: String, artworkUrl: String?)
}

