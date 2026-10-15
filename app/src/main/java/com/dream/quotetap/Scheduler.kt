package com.dream.quotetap

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock

object Scheduler {
    private const val REQUEST_CODE = 81

    fun schedule(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, RefreshReceiver::class.java).setAction(ACTION_SCHEDULED_REFRESH)
        val pending = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarm.cancel(pending)
        val hours = context.getSharedPreferences(Prefs.FILE, Context.MODE_PRIVATE)
            .getInt(Prefs.INTERVAL, 24)
        val delay = hours.coerceAtLeast(1) * 60L * 60L * 1000L
        alarm.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + delay,
            delay,
            pending
        )
    }

    const val ACTION_SCHEDULED_REFRESH = "com.dream.quotetap.SCHEDULED_REFRESH"
}
