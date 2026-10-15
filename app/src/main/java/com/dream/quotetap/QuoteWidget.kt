package com.dream.quotetap

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

data class Quote(val text: String, val author: String)

class QuoteWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { show(context, it) }
        Scheduler.schedule(context)
        val pendingResult = goAsync()
        fetch(context.applicationContext) { pendingResult.finish() }
    }

    override fun onEnabled(context: Context) = Scheduler.schedule(context)

    companion object {
        private val executor = Executors.newSingleThreadExecutor()

        fun show(context: Context, id: Int) {
            val prefs = context.getSharedPreferences(Prefs.FILE, Context.MODE_PRIVATE)
            val views = RemoteViews(context.packageName, R.layout.widget_quote)
            views.setTextViewText(R.id.quote, prefs.getString(Prefs.TEXT, context.getString(R.string.initial_quote)))
            views.setTextViewText(R.id.author, prefs.getString(Prefs.AUTHOR, context.getString(R.string.initial_author)))
            val refresh = PendingIntent.getBroadcast(
                context, 0,
                Intent(context, RefreshReceiver::class.java).setAction(RefreshReceiver.ACTION_REFRESH),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.quote_root, refresh)
            views.setOnClickPendingIntent(R.id.quote, refresh)
            AppWidgetManager.getInstance(context).updateAppWidget(id, views)
        }

        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            manager.getAppWidgetIds(ComponentName(context, QuoteWidget::class.java)).forEach { show(context, it) }
        }

        fun fetch(context: Context, onComplete: (() -> Unit)? = null) {
            executor.execute {
                try {
                    runCatching {
                        val quote = QuoteSources.fetch(context)
                        if (quote.text.isBlank()) error("Empty quote")
                        context.getSharedPreferences(Prefs.FILE, Context.MODE_PRIVATE).edit()
                            .putString(Prefs.TEXT, quote.text)
                            .putString(Prefs.AUTHOR, if (quote.author.isBlank()) "" else "— ${quote.author}")
                            .apply()
                        refreshAll(context)
                    }
                } finally {
                    onComplete?.invoke()
                }
            }
        }
    }
}

object Prefs {
    const val FILE = "quote_preferences"
    const val TEXT = "quote_text"
    const val AUTHOR = "quote_author"
    const val SOURCE = "quote_source"
    const val INTERVAL = "refresh_interval"
}

object QuoteSources {
    const val ZEN = "zen"
    const val QUOTABLE = "quotable"

    fun fetch(context: Context): Quote {
        val source = context.getSharedPreferences(Prefs.FILE, Context.MODE_PRIVATE)
            .getString(Prefs.SOURCE, ZEN) ?: ZEN
        return when (source) {
            QUOTABLE -> fetchQuotable()
            else -> fetchZen()
        }
    }

    private fun fetchZen(): Quote {
        val item = JSONArray(request("https://zenquotes.io/api/random")).getJSONObject(0)
        return Quote(item.optString("q"), item.optString("a"))
    }

    private fun fetchQuotable(): Quote {
        val response = request("https://api.quotable.io/quotes/random?limit=1")
        val item = if (response.trimStart().startsWith("[")) {
            JSONArray(response).getJSONObject(0)
        } else {
            org.json.JSONObject(response).let { if (it.has("content")) it else it.getJSONArray("data").getJSONObject(0) }
        }
        return Quote(item.optString("content"), item.optString("author"))
    }

    private fun request(address: String): String {
        val connection = URL(address).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "QuoteTap/1.0 (Android widget)")
            if (connection.responseCode !in 200..299) throw IllegalStateException("Quote service unavailable")
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
