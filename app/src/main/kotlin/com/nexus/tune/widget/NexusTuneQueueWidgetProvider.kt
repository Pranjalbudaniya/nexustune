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

class NexusTuneQueueWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val app = context.applicationContext as? NexusTuneApplication
        when (intent.action) {
            ACTION_QUEUE_TOGGLE_PLAY_PAUSE -> {
                app?.playerController?.togglePlayPause()
            }
            ACTION_QUEUE_SKIP_NEXT -> {
                app?.playerController?.seekToNext()
            }
            ACTION_QUEUE_SKIP_PREV -> {
                app?.playerController?.seekToPrevious()
            }
            ACTION_UPDATE_QUEUE_STATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, NexusTuneQueueWidgetProvider::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                for (id in allWidgetIds) {
                    updateAppWidget(context, appWidgetManager, id)
                }
            }
        }
    }

    companion object {
        const val ACTION_QUEUE_TOGGLE_PLAY_PAUSE = "com.nexus.tune.widget.ACTION_QUEUE_TOGGLE_PLAY_PAUSE"
        const val ACTION_QUEUE_SKIP_NEXT = "com.nexus.tune.widget.ACTION_QUEUE_SKIP_NEXT"
        const val ACTION_QUEUE_SKIP_PREV = "com.nexus.tune.widget.ACTION_QUEUE_SKIP_PREV"
        const val ACTION_UPDATE_QUEUE_STATE = "com.nexus.tune.widget.ACTION_UPDATE_QUEUE_STATE"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val app = context.applicationContext as? NexusTuneApplication
            val playbackState = app?.playerController?.playbackState?.value
            val track = playbackState?.currentTrack
            val isPlaying = playbackState?.status == com.nexus.tune.core.model.PlaybackStatus.PLAYING

            val views = RemoteViews(context.packageName, R.layout.widget_nexus_queue)

            views.setTextViewText(R.id.widget_queue_track_title, track?.title ?: "NexusTune")
            views.setTextViewText(R.id.widget_queue_track_artist, track?.artist ?: "Tap to start playing")

            val playPauseIcon = if (isPlaying) {
                android.R.drawable.ic_media_pause
            } else {
                android.R.drawable.ic_media_play
            }
            views.setImageViewResource(R.id.widget_queue_play_pause_button, playPauseIcon)

            // Open app on click
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                context, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_queue_container, openAppPendingIntent)

            // Controls
            val toggleIntent = Intent(context, NexusTuneQueueWidgetProvider::class.java).apply {
                action = ACTION_QUEUE_TOGGLE_PLAY_PAUSE
            }
            val togglePendingIntent = PendingIntent.getBroadcast(
                context, 201, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_queue_play_pause_button, togglePendingIntent)

            val nextIntent = Intent(context, NexusTuneQueueWidgetProvider::class.java).apply {
                action = ACTION_QUEUE_SKIP_NEXT
            }
            val nextPendingIntent = PendingIntent.getBroadcast(
                context, 202, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_queue_next_button, nextPendingIntent)

            val prevIntent = Intent(context, NexusTuneQueueWidgetProvider::class.java).apply {
                action = ACTION_QUEUE_SKIP_PREV
            }
            val prevPendingIntent = PendingIntent.getBroadcast(
                context, 203, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_queue_prev_button, prevPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun notifyPlaybackChanged(context: Context) {
            val intent = Intent(context, NexusTuneQueueWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_QUEUE_STATE
            }
            context.sendBroadcast(intent)
        }
    }
}

