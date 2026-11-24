package com.nexus.tune.core.data.backup

import com.nexus.tune.core.database.NexusTuneDatabase
import com.nexus.tune.core.database.entity.PlaylistEntity
import com.nexus.tune.core.database.entity.TrackEntity
import com.nexus.tune.core.datastore.SettingsDataStore
import com.nexus.tune.core.model.ThemeConfig
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

data class NewPipeImportResult(
    val playlistsImported: Int = 0,
    val tracksImported: Int = 0,
    val success: Boolean = false
)

class BackupManager(
    private val database: NexusTuneDatabase,
    private val settingsDataStore: SettingsDataStore
) {

    suspend fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val theme = settingsDataStore.themeConfig.first()
        val themeObj = JSONObject().apply {
            put("seedMode", theme.seedMode.name)
            put("isDarkMode", theme.isDarkMode)
            put("isAmoled", theme.isAmoled)
            put("shapePreset", theme.shapePreset.name)
            put("playerLayout", theme.playerLayout.name)
            put("miniPlayerStyle", theme.miniPlayerStyle.name)
            put("refreshRate", theme.refreshRatePreferred)
        }
        root.put("theme", themeObj)

        val liked = database.trackDao().getLikedTracks().first()
        val likedArray = JSONArray()
        liked.forEach { track ->
            val trackObj = JSONObject().apply {
                put("id", track.id)
                put("title", track.title)
                put("artist", track.artist)
                put("album", track.album)
                put("artworkUrl", track.artworkUrl)
            }
            likedArray.put(trackObj)
        }
        root.put("likedTracks", likedArray)

        val playlists = database.playlistDao().getPlaylists().first()
        val playlistArray = JSONArray()
        playlists.forEach { p ->
            val pObj = JSONObject().apply {
                put("id", p.id)
                put("title", p.title)
                put("author", p.author)
                put("artworkUrl", p.artworkUrl)
                put("trackCount", p.trackCount)
            }
            playlistArray.put(pObj)
        }
        root.put("playlists", playlistArray)

        return root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val version = root.optInt("version", 1)
            if (version < 1) return false

            if (root.has("theme")) {
                val themeObj = root.getJSONObject("theme")
                val current = settingsDataStore.themeConfig.first()
                val updated = current.copy(
                    isDarkMode = themeObj.optBoolean("isDarkMode", current.isDarkMode),
                    isAmoled = themeObj.optBoolean("isAmoled", current.isAmoled)
                )
                settingsDataStore.updateThemeConfig(updated)
            }

            if (root.has("likedTracks")) {
                val likedArray = root.getJSONArray("likedTracks")
                for (i in 0 until likedArray.length()) {
                    val trackObj = likedArray.getJSONObject(i)
                    val entity = TrackEntity(
                        id = trackObj.getString("id"),
                        title = trackObj.getString("title"),
                        artist = trackObj.getString("artist"),
                        album = if (trackObj.has("album") && !trackObj.isNull("album")) trackObj.getString("album") else null,
                        artworkUrl = if (trackObj.has("artworkUrl") && !trackObj.isNull("artworkUrl")) trackObj.getString("artworkUrl") else null,
                        isLiked = true
                    )
                    database.trackDao().insertOrUpdate(entity)
                }
            }

            if (root.has("playlists")) {
                val playlistArray = root.getJSONArray("playlists")
                for (i in 0 until playlistArray.length()) {
                    val pObj = playlistArray.getJSONObject(i)
                    val playlistEntity = PlaylistEntity(
                        id = pObj.getString("id"),
                        title = pObj.getString("title"),
                        author = if (pObj.has("author") && !pObj.isNull("author")) pObj.getString("author") else null,
                        artworkUrl = if (pObj.has("artworkUrl") && !pObj.isNull("artworkUrl")) pObj.getString("artworkUrl") else null,
                        trackCount = pObj.optInt("trackCount", 0),
                        isUserCreated = true
                    )
                    database.playlistDao().insert(playlistEntity)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun importFromNewPipeJson(jsonString: String): NewPipeImportResult {
        return try {
            val root = JSONObject(jsonString)
            var playlistCount = 0
            var trackCount = 0

            if (root.has("playlists")) {
                val playlistsArray = root.getJSONArray("playlists")
                for (i in 0 until playlistsArray.length()) {
                    val pObj = playlistsArray.getJSONObject(i)
                    val playlistName = pObj.optString("name", "NewPipe Playlist ${i + 1}")
                    val playlistId = "newpipe_${System.currentTimeMillis()}_$i"
                    val streams = pObj.optJSONArray("streams") ?: JSONArray()

                    val playlistEntity = PlaylistEntity(
                        id = playlistId,
                        title = playlistName,
                        author = "NewPipe Import",
                        artworkUrl = null,
                        trackCount = streams.length(),
                        isUserCreated = true
                    )
                    database.playlistDao().insert(playlistEntity)
                    playlistCount++

                    for (j in 0 until streams.length()) {
                        val sObj = streams.getJSONObject(j)
                        val trackUrl = sObj.optString("url", "")
                        val trackTitle = sObj.optString("title", "Unknown Title")
                        val uploader = sObj.optString("uploader", "Unknown Artist")
                        val trackId = if (trackUrl.contains("watch?v=")) {
                            trackUrl.substringAfter("watch?v=").substringBefore("&")
                        } else {
                            "np_${System.currentTimeMillis()}_${i}_$j"
                        }

                        val trackEntity = TrackEntity(
                            id = trackId,
                            title = trackTitle,
                            artist = uploader,
                            album = playlistName,
                            artworkUrl = null,
                            isLiked = false
                        )
                        database.trackDao().insertOrUpdate(trackEntity)
                        trackCount++
                    }
                }
            }
            NewPipeImportResult(
                playlistsImported = playlistCount,
                tracksImported = trackCount,
                success = playlistCount > 0 || trackCount > 0
            )
        } catch (e: Exception) {
            NewPipeImportResult(success = false)
        }
    }
}
