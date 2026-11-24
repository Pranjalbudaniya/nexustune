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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(UnstableApi::class)
class PlayerController(private val context: Context) {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    private val _preloadedTrack = MutableStateFlow<Track?>(null)
    val preloadedTrack: StateFlow<Track?> = _preloadedTrack.asStateFlow()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

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
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    val preloaded = _preloadedTrack.value
                    if (preloaded != null && mediaItem?.mediaId == preloaded.id) {
                        _playbackState.value = _playbackState.value.copy(
                            currentTrack = preloaded,
                            status = PlaybackStatus.PLAYING
                        )
                        _preloadedTrack.value = null
                    }
                }
                updateState()
            }

            override fun onPlaybackParametersChanged(playbackParameters: androidx.media3.common.PlaybackParameters) {
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

        _preloadedTrack.value = null
        controller?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
        if (track.loudnessGainDb != 0.0f) {
            com.nexus.tune.core.player.effects.NexusAudioEffects.instance.applyTrackLoudnessGain(track.loudnessGainDb)
        }
        _playbackState.value = _playbackState.value.copy(
            currentTrack = track,
            status = PlaybackStatus.PLAYING
        )
    }

    fun preloadNextTrack(track: Track, resolvedStreamUrl: String? = null) {
        val mediaUri = resolvedStreamUrl ?: track.streamUrl ?: return
        val c = controller ?: return
        if (c.mediaItemCount <= 1) {
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

            c.addMediaItem(mediaItem)
            _preloadedTrack.value = track
        }
    }

    fun clearUpcomingPreload() {
        val c = controller ?: return
        if (c.mediaItemCount > 1) {
            c.removeMediaItems(1, c.mediaItemCount)
        }
        _preloadedTrack.value = null
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

    fun fadeOutAndPause(durationMs: Long = 1000L, onComplete: () -> Unit = {}) {
        val c = controller ?: return
        coroutineScope.launch {
            val steps = 10
            val stepDuration = (durationMs / steps).coerceAtLeast(10L)
            for (i in steps downTo 0) {
                c.volume = i / steps.toFloat()
                delay(stepDuration)
            }
            c.pause()
            c.volume = 1.0f
            onComplete()
            updateState()
        }
    }

    fun fadeInAndPlay(track: Track, resolvedStreamUrl: String? = null, durationMs: Long = 1000L) {
        val c = controller ?: return
        c.volume = 0.0f
        playTrack(track, resolvedStreamUrl)
        coroutineScope.launch {
            val steps = 10
            val stepDuration = (durationMs / steps).coerceAtLeast(10L)
            for (i in 0..steps) {
                c.volume = i / steps.toFloat()
                delay(stepDuration)
            }
            c.volume = 1.0f
        }
    }

    private var volumeLimitMultiplier: Float = 1.0f

    fun setVolumeLimit(percent: Int) {
        volumeLimitMultiplier = (percent.coerceIn(10, 100) / 100f)
        val c = controller ?: return
        if (c.volume > volumeLimitMultiplier) {
            c.volume = volumeLimitMultiplier
        }
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0.0f, volumeLimitMultiplier)
        controller?.volume = clamped
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        updateState()
    }

    fun setPlaybackSpeed(speed: Float) {
        val c = controller ?: return
        val currentPitch = c.playbackParameters.pitch
        val clampedSpeed = speed.coerceIn(0.25f, 3.0f)
        c.playbackParameters = androidx.media3.common.PlaybackParameters(clampedSpeed, currentPitch)
        updateState()
    }

    fun setPlaybackPitch(pitch: Float) {
        val c = controller ?: return
        val currentSpeed = c.playbackParameters.speed
        val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
        c.playbackParameters = androidx.media3.common.PlaybackParameters(currentSpeed, clampedPitch)
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

        val params = c.playbackParameters

        _playbackState.value = _playbackState.value.copy(
            status = status,
            positionMs = c.currentPosition.coerceAtLeast(0L),
            durationMs = c.duration.coerceAtLeast(0L),
            playbackSpeed = params.speed,
            playbackPitch = params.pitch
        )
        com.nexus.tune.core.player.visualizer.VisualizerManager.instance.updatePlayback(c.isPlaying)
    }

    fun release() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}

