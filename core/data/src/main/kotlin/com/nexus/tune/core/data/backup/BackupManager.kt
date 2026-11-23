package com.nexus.tune.core.data.backup

import com.nexus.tune.core.database.NexusTuneDatabase
import com.nexus.tune.core.database.entity.TrackEntity
import com.nexus.tune.core.datastore.SettingsDataStore
import com.nexus.tune.core.model.ThemeConfig
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

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
            true
        } catch (e: Exception) {
            false
        }
    }
}

