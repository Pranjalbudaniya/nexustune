package com.nexus.tune.core.player

import android.app.PendingIntent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class NexusPlaybackService : MediaLibraryService() {

    private var player: ExoPlayer? = null
    private var librarySession: MediaLibrarySession? = null

    companion object {
        const val ROOT_ID = "root"
        const val LIKED_ID = "liked_tracks"
        const val HISTORY_ID = "history"
        const val PLAYLISTS_ID = "playlists"
    }

    private val callback = object : MediaLibrarySession.Callback {
        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val rootItem = MediaItem.Builder()
                .setMediaId(ROOT_ID)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("NexusTune Library")
                        .setIsPlayable(false)
                        .setIsBrowsable(true)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                        .build()
                )
                .build()
            return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val items = when (parentId) {
                ROOT_ID -> {
                    ImmutableList.of(
                        createBrowsableFolder(LIKED_ID, "Liked Tracks"),
                        createBrowsableFolder(HISTORY_ID, "Recently Played"),
                        createBrowsableFolder(PLAYLISTS_ID, "Playlists")
                    )
                }
                else -> ImmutableList.of()
            }
            return Futures.immediateFuture(LibraryResult.ofItemList(items, params))
        }

        private fun createBrowsableFolder(id: String, title: String): MediaItem {
            return MediaItem.Builder()
                .setMediaId(id)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setIsPlayable(false)
                        .setIsBrowsable(true)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS)
                        .build()
                )
                .build()
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true) // Handle audio focus automatically
            .setHandleAudioBecomingNoisy(true) // Pause on headset disconnect
            .build()

        player?.audioSessionId?.let { sessionId ->
            com.nexus.tune.core.player.effects.NexusAudioEffects.instance.attachToAudioSession(sessionId)
        }

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        librarySession = player?.let { exo ->
            MediaLibrarySession.Builder(this, exo, callback)
                .setSessionActivity(pendingIntent)
                .build()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return librarySession
    }

    override fun onDestroy() {
        com.nexus.tune.core.player.effects.NexusAudioEffects.instance.release()
        librarySession?.run {
            player.release()
            release()
            librarySession = null
        }
        player = null
        super.onDestroy()
    }
}
