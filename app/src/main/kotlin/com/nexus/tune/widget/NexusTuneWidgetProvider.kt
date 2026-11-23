package com.nexus.tune.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nexus.tune.MainActivity
import com.nexus.tune.NexusTuneApplication
import com.nexus.tune.R

class NexusTuneWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TOGGLE_PLAY_PAUSE -> {
                val app = context.applicationContext as? NexusTuneApplication
                app?.playerController?.togglePlayPause()
            }
            ACTION_UPDATE_STATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, NexusTuneWidgetProvider::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                for (id in allWidgetIds) {
                    updateAppWidget(context, appWidgetManager, id)
                }
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE_PLAY_PAUSE = "com.nexus.tune.widget.ACTION_TOGGLE_PLAY_PAUSE"
        const val ACTION_UPDATE_STATE = "com.nexus.tune.widget.ACTION_UPDATE_STATE"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val app = context.applicationContext as? NexusTuneApplication
            val playbackState = app?.playerController?.playbackState?.value
            val track = playbackState?.currentTrack
            val isPlaying = playbackState?.status == com.nexus.tune.core.model.PlaybackStatus.PLAYING

            val views = RemoteViews(context.packageName, R.layout.widget_nexus_playback)

            views.setTextViewText(R.id.widget_track_title, track?.title ?: "NexusTune")
            views.setTextViewText(R.id.widget_track_artist, track?.artist ?: "Tap to open")

            val playPauseIcon = if (isPlaying) {
                android.R.drawable.ic_media_pause
            } else {
                android.R.drawable.ic_media_play
            }
            views.setImageViewResource(R.id.widget_play_pause_button, playPauseIcon)

            // Click on widget opens app
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                context, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, openAppPendingIntent)

            // Play/Pause pending intent
            val toggleIntent = Intent(context, NexusTuneWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_PLAY_PAUSE
            }
            val togglePendingIntent = PendingIntent.getBroadcast(
                context, 1, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_play_pause_button, togglePendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun notifyPlaybackChanged(context: Context) {
            val intent = Intent(context, NexusTuneWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_STATE
            }
            context.sendBroadcast(intent)
        }
    }
}

