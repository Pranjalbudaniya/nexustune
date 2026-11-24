package com.nexus.tune.core.player.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WakeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == WakeAlarmManager.ACTION_WAKE_ALARM) {
            val app = context.applicationContext
            if (app is WakeAlarmTriggerable) {
                app.triggerWakeAlarm()
            }
        }
    }
}

interface WakeAlarmTriggerable {
    fun triggerWakeAlarm()
}
