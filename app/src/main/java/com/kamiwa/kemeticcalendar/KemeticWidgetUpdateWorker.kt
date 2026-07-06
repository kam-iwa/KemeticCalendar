package com.kamiwa.kemeticcalendar

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.core.content.ContextCompat.getString
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kamiwa.kemeticcalendar.core.KemeticDate
import com.kamiwa.kemeticcalendar.core.LunarDate
import com.kamiwa.kemeticcalendar.ui.theme.TextColor
import com.kamiwa.kemeticcalendar.ui.theme.TextDecadeColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class KemeticWidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val gregorianToday = LocalDate.now()
            val kemeticToday = KemeticDate.today()
            val lunarToday = LunarDate(gregorianToday)

            val gregorianDayText = gregorianToday.format(
                DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault())
            )
            val kemeticMonthText = getString(applicationContext, kemeticToday.getMonth().key)
            val kemeticDayText = kemeticToday.day.toString()
            val kemeticDayTypeText = getString(applicationContext, kemeticToday.getDayType())
            val kemeticHolidaysText = kemeticToday.getHolidays().map { getString(applicationContext, it.key) }
                .toTypedArray()
                .joinToString("; ")

            updateKemeticWidget(applicationContext, gregorianDayText, kemeticMonthText, kemeticDayText,
                kemeticDayTypeText, kemeticHolidaysText, kemeticToday)
            Result.success()
        } catch (e: Exception) {
            // W razie błędu WorkManager sam ponowi próbę
            Result.retry()
        }
    }

    private fun updateKemeticWidget(context: Context, gregorian_day: String, kemetic_month: String,
                                    kemetic_day: String, kemetic_day_type: String, kemetic_holidays: String,
                                    kemetic_date: KemeticDate) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(
            ComponentName(context, KemeticWidgetProvider::class.java)
        )

        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.kemetic_widget)
            views.setTextViewText(R.id.gregorian_date_text_view, gregorian_day)
            views.setTextViewText(R.id.kemetic_month_text_view, kemetic_month)
            views.setTextViewText(R.id.kemetic_day_text_view, kemetic_day)
            views.setInt(R.id.kemetic_day_text_view, "setTextColor", if (kemetic_date.decade) R.color.text_decade else R.color.text)
            views.setTextViewText(R.id.kemetic_day_type_text_view, kemetic_day_type)
            views.setTextViewText(R.id.kemetic_holiday_text_view, kemetic_holidays)
            manager.updateAppWidget(id, views)
        }
    }
}