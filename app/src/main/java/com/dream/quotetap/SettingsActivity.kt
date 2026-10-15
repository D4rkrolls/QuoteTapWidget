package com.dream.quotetap

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

class SettingsActivity : Activity() {
    private val ink = 0xFF18211F.toInt()
    private val green = 0xFF31594B.toInt()
    private val muted = 0xFF5F6D65.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(Prefs.FILE, MODE_PRIVATE)
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(28), dp(24), dp(24))
            setBackgroundColor(0xFFF5F5F0.toInt())
        }
        page.addView(label("QUOTE TAP", 12, green, true))
        page.addView(label("A little perspective,\nright on your home screen.", 28, ink, true).apply {
            setPadding(0, dp(12), 0, dp(10))
        })
        page.addView(label("Choose where quotes come from and how often your widget gets a fresh one.", 15, muted))

        page.addView(sectionTitle("QUOTE SOURCE"))
        val sources = listOf("ZenQuotes", "Quotable")
        val sourceSpinner = spinner(sources).apply {
            setSelection(if (prefs.getString(Prefs.SOURCE, QuoteSources.ZEN) == QuoteSources.QUOTABLE) 1 else 0)
        }
        page.addView(sourceSpinner)
        page.addView(label("ZenQuotes (zenquotes.io) · Quotable (api.quotable.io)", 12, muted))

        page.addView(sectionTitle("AUTOMATIC REFRESH"))
        val intervals = listOf("Every 1 hour", "Every 6 hours", "Every 12 hours", "Once a day")
        val intervalHours = listOf(1, 6, 12, 24)
        val intervalSpinner = spinner(intervals).apply {
            val current = prefs.getInt(Prefs.INTERVAL, 24)
            setSelection(intervalHours.indexOf(current).takeIf { it >= 0 } ?: 3)
        }
        page.addView(intervalSpinner)
        page.addView(label("Android may adjust the exact time to save battery.", 12, muted))

        page.addView(sectionTitle("YOUR WIDGET"))
        page.addView(label("Add Quote Tap from your home screen’s Widgets menu. Resize it to fit your cover screen or inner display. Tap the quote anytime for another.", 14, ink))

        val save = Button(this).apply {
            text = "Save settings"
            isAllCaps = false
            setTextColor(-1)
            setBackgroundColor(green)
            setOnClickListener {
                val source = if (sourceSpinner.selectedItemPosition == 1) QuoteSources.QUOTABLE else QuoteSources.ZEN
                val hours = intervalHours[intervalSpinner.selectedItemPosition]
                prefs.edit().putString(Prefs.SOURCE, source).putInt(Prefs.INTERVAL, hours).apply()
                Scheduler.schedule(this@SettingsActivity)
                Toast.makeText(this@SettingsActivity, "Settings saved", Toast.LENGTH_SHORT).show()
                QuoteWidget.fetch(applicationContext)
            }
        }
        page.addView(save, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).apply {
            topMargin = dp(24)
        })
        page.addView(label("No account needed. The current quote is stored on this device.", 12, muted).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
        })
        setContentView(page)
    }

    private fun sectionTitle(text: String) = label(text, 11, green, true).apply {
        setPadding(0, dp(24), 0, dp(5))
    }

    private fun label(text: String, size: Int, color: Int, bold: Boolean = false) = TextView(this).apply {
        this.text = text
        textSize = size.toFloat()
        setTextColor(color)
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun spinner(items: List<String>) = Spinner(this).apply {
        adapter = ArrayAdapter(this@SettingsActivity, android.R.layout.simple_spinner_dropdown_item, items)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
