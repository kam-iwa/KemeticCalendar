package com.kamiwa.kemeticcalendar

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kamiwa.kemeticcalendar.core.KemeticDate
import com.kamiwa.kemeticcalendar.core.LunarDate
import java.time.LocalDate
import java.time.LocalDateTime
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
            val kemeticMonthText = applicationContext.getString(kemeticToday.getMonth().key)
            val kemeticDayText = kemeticToday.day.toString()
            val kemeticDayTypeText = applicationContext.getString(kemeticToday.getDayType())
            val kemeticHolidaysText = kemeticToday.getHolidays()
                .map { applicationContext.getString(it.key) }
                .joinToString("; ")

            updateAllGlanceWidgets(
                applicationContext,
                gregorianDayText,
                kemeticMonthText,
                kemeticDayText,
                kemeticDayTypeText,
                kemeticHolidaysText,
                kemeticToday.decade
            )
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun updateAllGlanceWidgets(
        context: Context,
        gregorianDay: String,
        kemeticMonth: String,
        kemeticDay: String,
        kemeticDayType: String,
        kemeticHolidays: String,
        isDecade: Boolean
    ) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(KemeticGlanceWidget::class.java)

        val updated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
        val updatedText = applicationContext.getString(R.string.widget_updated_at)

        val updatedWithDate = "$updatedText $updated"

        for (glanceId in glanceIds) {
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[KemeticWidgetKeys.GREGORIAN_DAY] = gregorianDay
                prefs[KemeticWidgetKeys.KEMETIC_MONTH] = kemeticMonth
                prefs[KemeticWidgetKeys.KEMETIC_DAY] = kemeticDay
                prefs[KemeticWidgetKeys.KEMETIC_DAY_TYPE] = kemeticDayType
                prefs[KemeticWidgetKeys.KEMETIC_HOLIDAYS] = kemeticHolidays
                prefs[KemeticWidgetKeys.IS_DECADE] = isDecade
                prefs[KemeticWidgetKeys.UPDATED_AT] = updatedWithDate
            }
            KemeticGlanceWidget().update(context, glanceId)
        }
    }
}