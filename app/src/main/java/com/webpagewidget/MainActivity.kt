package com.webpagewidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val urlInput = findViewById<EditText>(R.id.urlInput)
        val statusText = findViewById<TextView>(R.id.statusText)

        val savedUrl = prefs.getString(KEY_URL, "")
        urlInput.setText(savedUrl)

        val widgetCount = AppWidgetManager.getInstance(this)
            .getAppWidgetIds(ComponentName(this, PageWidget::class.java)).size
        statusText.text = if (widgetCount > 0)
            "Widget active ($widgetCount on screen)"
        else
            "No widget added yet. Long-press your home screen → Widgets → Web Page Widget."

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            val url = urlInput.text.toString().trim().let {
                if (it.isNotEmpty() && !it.startsWith("http")) "https://$it" else it
            }
            if (url.isEmpty()) {
                Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            prefs.edit().putString(KEY_URL, url).apply()
            urlInput.setText(url)
            triggerRefresh()
            PageWidget.scheduleHourlyRefresh(this)
            Toast.makeText(this, "Saved! Capturing screenshot…", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.refreshButton).setOnClickListener {
            val url = prefs.getString(KEY_URL, "")
            if (url.isNullOrEmpty()) {
                Toast.makeText(this, "Save a URL first", Toast.LENGTH_SHORT).show()
            } else {
                triggerRefresh()
                Toast.makeText(this, "Refreshing widget…", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun triggerRefresh() {
        val request = OneTimeWorkRequestBuilder<ScreenshotWorker>().build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            "widget_refresh_now",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    companion object {
        const val PREFS_NAME = "widget_prefs"
        const val KEY_URL = "url"
    }
}
