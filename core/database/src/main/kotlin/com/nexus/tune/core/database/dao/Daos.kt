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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackCrossRef(ref: com.nexus.tune.core.database.entity.PlaylistTrackCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackCrossRefs(refs: List<com.nexus.tune.core.database.entity.PlaylistTrackCrossRef>)

    @Query("SELECT trackId FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY trackOrder ASC")
    fun getTrackIdsForPlaylist(playlistId: String): Flow<List<String>>

    @Query("SELECT tracks.* FROM tracks INNER JOIN playlist_tracks ON tracks.id = playlist_tracks.trackId WHERE playlist_tracks.playlistId = :playlistId ORDER BY playlist_tracks.trackOrder ASC")
    fun getTracksForPlaylist(playlistId: String): Flow<List<com.nexus.tune.core.database.entity.TrackEntity>>

    @Query("UPDATE playlists SET trackCount = :count WHERE id = :id")
    suspend fun updateTrackCount(id: String, count: Int)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun clearTracksForPlaylist(playlistId: String)
}

@Dao
interface LyricsDao {
    @Query("SELECT * FROM lyrics_cache WHERE trackId = :trackId LIMIT 1")
    suspend fun getLyrics(trackId: String): com.nexus.tune.core.database.entity.LyricsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: com.nexus.tune.core.database.entity.LyricsEntity)

    @Query("UPDATE lyrics_cache SET userOffsetMs = :offsetMs WHERE trackId = :trackId")
    suspend fun updateOffset(trackId: String, offsetMs: Long)

    @Query("SELECT userOffsetMs FROM lyrics_cache WHERE trackId = :trackId LIMIT 1")
    suspend fun getOffset(trackId: String): Long?

    @Query("DELETE FROM lyrics_cache")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM lyrics_cache")
    suspend fun getCachedCount(): Int
}

@Dao
interface TrackBookmarkDao {
    @Query("SELECT * FROM track_bookmarks WHERE trackId = :trackId ORDER BY positionMs ASC")
    fun getBookmarksForTrack(trackId: String): Flow<List<com.nexus.tune.core.database.entity.TrackBookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: com.nexus.tune.core.database.entity.TrackBookmarkEntity): Long

    @Query("DELETE FROM track_bookmarks WHERE id = :bookmarkId")
    suspend fun delete(bookmarkId: Long)

    @Query("DELETE FROM track_bookmarks WHERE trackId = :trackId")
    suspend fun deleteAllForTrack(trackId: String)

    @Query("SELECT COUNT(*) FROM track_bookmarks WHERE trackId = :trackId")
    suspend fun getBookmarkCountForTrack(trackId: String): Int
}

