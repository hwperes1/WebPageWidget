package com.webpagewidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.io.File
import java.util.concurrent.TimeUnit

class PageWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
        scheduleHourlyRefresh(context)
    }

    override fun onEnabled(context: Context) {
        scheduleHourlyRefresh(context)
        val request = OneTimeWorkRequestBuilder<ScreenshotWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "widget_refresh_now",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override fun onDisabled(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("widget_hourly_refresh")
    }

    companion object {

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            val screenshotFile = File(context.filesDir, "screenshot.png")
            if (screenshotFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(screenshotFile.absolutePath)
                if (bitmap != null) {
                    views.setImageViewBitmap(R.id.widgetImage, bitmap)
                    views.setViewVisibility(R.id.placeholderText, android.view.View.GONE)
                }
            }

            val openIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetImage, pendingIntent)
            views.setOnClickPendingIntent(R.id.placeholderText, pendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, PageWidget::class.java))
            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }

        fun scheduleHourlyRefresh(context: Context) {
            val request = PeriodicWorkRequestBuilder<ScreenshotWorker>(1, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "widget_hourly_refresh",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
