package com.nexus.tune.core.data.importer

import com.nexus.tune.core.database.dao.PlaylistDao
import com.nexus.tune.core.database.dao.TrackDao
import com.nexus.tune.core.database.entity.PlaylistEntity
import com.nexus.tune.core.database.entity.TrackEntity
import org.json.JSONObject
import java.util.UUID

data class ImportResult(
    val playlistsImported: Int,
    val tracksImported: Int
)

class NewPipeBackupImporter(
    private val playlistDao: PlaylistDao,
    private val trackDao: TrackDao
) {

    suspend fun importFromJson(jsonString: String): ImportResult {
        var playlistCount = 0
        var trackCount = 0

        try {
            val root = JSONObject(jsonString)
            if (root.has("playlists")) {
                val playlistsArray = root.getJSONArray("playlists")
                for (i in 0 until playlistsArray.length()) {
                    val playlistObj = playlistsArray.getJSONObject(i)
                    val playlistName = playlistObj.optString("name", "Imported Playlist")
                    val playlistId = UUID.randomUUID().toString()

                    val streamsArray = playlistObj.optJSONArray("stream_list")
                    val streamCount = streamsArray?.length() ?: 0

                    val playlistEntity = PlaylistEntity(
                        id = playlistId,
                        title = playlistName,
                        author = "NewPipe Import",
                        artworkUrl = null,
                        trackCount = streamCount,
                        isUserCreated = true
                    )
                    playlistDao.insert(playlistEntity)
                    playlistCount++

                    if (streamsArray != null) {
                        for (j in 0 until streamsArray.length()) {
                            val streamObj = streamsArray.getJSONObject(j)
                            val url = streamObj.optString("url", "")
                            val videoId = if (url.contains("v=")) {
                                url.substringAfter("v=").substringBefore("&")
                            } else {
                                UUID.randomUUID().toString()
                            }
                            val title = streamObj.optString("title", "Unknown Title")
                            val uploader = streamObj.optString("uploader", "Unknown Artist")
                            val duration = streamObj.optLong("duration", 0L)

                            val trackEntity = TrackEntity(
                                id = videoId,
                                title = title,
                                artist = uploader,
                                album = playlistName,
                                durationSeconds = duration,
                                artworkUrl = null,
                                streamUrl = null,
                                isLiked = false,
                                isDownloaded = false
                            )
                            trackDao.insertOrUpdate(trackEntity)
                            trackCount++
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Graceful error handling
        }

        return ImportResult(playlistCount, trackCount)
    }
}

