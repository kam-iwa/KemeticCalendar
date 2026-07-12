package com.kamiwa.kemeticcalendar

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.*
import java.util.concurrent.TimeUnit

class KemeticGlanceWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = KemeticGlanceWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // Natychmiastowe wypełnienie danymi przy dodaniu/odświeżeniu widgetu
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<KemeticWidgetUpdateWorker>().build()
        )
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        val request = PeriodicWorkRequestBuilder<KemeticWidgetUpdateWorker>(
            1, TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "widget_update_work",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WorkManager.getInstance(context).cancelUniqueWork("widget_update_work")
    }
}

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val request = PeriodicWorkRequestBuilder<KemeticWidgetUpdateWorker>(
                1, TimeUnit.HOURS
            ).setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "widget_update_work",
                ExistingPeriodicWorkPolicy.UPDATE, // nie KEEP – wymuś odświeżenie zamiast czekać
                request
            )

            // opcjonalnie: od razu wymuś jednorazowe odświeżenie widgetu
            WorkManager.getInstance(context).enqueue(
                OneTimeWorkRequestBuilder<KemeticWidgetUpdateWorker>().build()
            )
        }
    }
}