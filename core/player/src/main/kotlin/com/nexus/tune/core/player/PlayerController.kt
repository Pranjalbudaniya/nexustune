package com.nexus.tune.core.player

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.nexus.tune.core.model.PlaybackState
import com.nexus.tune.core.model.PlaybackStatus
import com.nexus.tune.core.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(UnstableApi::class)
class PlayerController(private val context: Context) {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    init {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, NexusPlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                controller = controllerFuture?.get()
                setupPlayerListener()
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun setupPlayerListener() {
        controller?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updateState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateState()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateState()
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                _playbackState.value = _playbackState.value.copy(
                    status = PlaybackStatus.ERROR,
                    errorMessage = error.localizedMessage ?: "Playback error: ${error.errorCodeName}"
                )
            }
        })
        updateState()
    }

    fun retryWithStream(newStreamUrl: String) {
        val current = _playbackState.value.currentTrack ?: return
        playTrack(current, newStreamUrl)
    }

    fun playTrack(track: Track, resolvedStreamUrl: String? = null) {
        val mediaUri = resolvedStreamUrl ?: track.streamUrl ?: return
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setArtworkUri(track.artworkUrl?.let { android.net.Uri.parse(it) })
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(mediaUri)
            .setMediaMetadata(metadata)
            .build()

        controller?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
        _playbackState.value = _playbackState.value.copy(
            currentTrack = track,
            status = PlaybackStatus.PLAYING
        )
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) {
            c.pause()
        } else {
            c.play()
        }
        updateState()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        updateState()
    }

    private fun updateState() {
        val c = controller ?: return
        val currentMediaId = c.currentMediaItem?.mediaId
        val status = when {
            c.playbackState == Player.STATE_BUFFERING -> PlaybackStatus.BUFFERING
            c.playbackState == Player.STATE_ENDED -> PlaybackStatus.ENDED
            c.isPlaying -> PlaybackStatus.PLAYING
            c.playbackState == Player.STATE_READY -> PlaybackStatus.PAUSED
            else -> PlaybackStatus.IDLE
        }

        _playbackState.value = _playbackState.value.copy(
            status = status,
            positionMs = c.currentPosition.coerceAtLeast(0L),
            durationMs = c.duration.coerceAtLeast(0L)
        )
    }

    fun release() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}

