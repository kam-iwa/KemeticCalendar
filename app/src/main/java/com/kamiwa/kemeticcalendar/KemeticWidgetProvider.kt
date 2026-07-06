package com.kamiwa.kemeticcalendar

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class KemeticWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Pierwsze wyświetlenie / ręczne odświeżenie z systemu
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<KemeticWidgetUpdateWorker>().build()
        )
    }

    override fun onEnabled(context: Context) {
        // Wywoływane, gdy pierwszy egzemplarz widgetu trafia na ekran
        val request = PeriodicWorkRequestBuilder<KemeticWidgetUpdateWorker>(
            1, TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // zmień jeśli potrzebujesz internetu
                .build()
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "widget_update_work",
            ExistingPeriodicWorkPolicy.KEEP, // nie duplikuj zadania przy kolejnych widgetach
            request
        )
    }

    override fun onDisabled(context: Context) {
        // Wywoływane, gdy usunięto ostatni egzemplarz widgetu z ekranu
        WorkManager.getInstance(context).cancelUniqueWork("widget_update_work")
    }
}