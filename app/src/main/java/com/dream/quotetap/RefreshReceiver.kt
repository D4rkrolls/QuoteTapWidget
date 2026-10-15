package com.dream.quotetap

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_REFRESH || intent.action == Scheduler.ACTION_SCHEDULED_REFRESH) {
            QuoteWidget.fetch(context.applicationContext)
        }
    }

    companion object { const val ACTION_REFRESH = "com.dream.quotetap.REFRESH" }
}
