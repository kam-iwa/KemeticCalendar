package com.kamiwa.kemeticcalendar.core

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.kamiwa.kemeticcalendar.MainActivity
import com.kamiwa.kemeticcalendar.R
import com.kamiwa.kemeticcalendar.ui.theme.BackgroundColor
import com.kamiwa.kemeticcalendar.ui.theme.TextColor
import com.kamiwa.kemeticcalendar.ui.theme.TextDecadeColor
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class KemeticGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent(context)
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun WidgetContent(context: Context) {
        val gregorianToday = LocalDate.now()
        val kemeticToday = KemeticDate.today()
        val lunarToday = LunarDate(gregorianToday)

        val gregorianDayText = gregorianToday.format(
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault())
        )
        val kemeticMonthText = context.getString(kemeticToday.getMonth().key)
        val kemeticDayText = kemeticToday.day.toString()
        val kemeticDayTypeText = context.getString(kemeticToday.getDayType())
        val kemeticHolidaysText = kemeticToday.getHolidays().joinToString("; ") { context.getString(it.key) }

        val lunarPhaseInfo = lunarToday.getLunarPhase()
        val lunarPhaseText = context.getString(lunarPhaseInfo.first)
        val lunarPhasePercent = lunarPhaseInfo.third * 100
        val lunarHolidayText = context.getString(lunarToday.getHoliday())

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(BackgroundColor))
                .padding(8.dp)
                .clickable(actionStartActivity<MainActivity>()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = gregorianDayText,
                style = TextStyle(
                    color = ColorProvider(color = TextColor),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = kemeticMonthText,
                style = TextStyle(
                    color = ColorProvider(TextColor),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = kemeticDayText,
                style = TextStyle(
                    color = ColorProvider(if (kemeticToday.decade) TextDecadeColor else TextColor),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = kemeticDayTypeText,
                style = TextStyle(
                    color = ColorProvider(TextColor),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            )
            if (kemeticHolidaysText.isNotEmpty()) {
                Text(
                    text = kemeticHolidaysText,
                    style = TextStyle(
                        color = ColorProvider(TextColor),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
            Text(
                text = String.format(Locale.getDefault(), "%s (%.0f%%)", lunarPhaseText, lunarPhasePercent),
                style = TextStyle(
                    color = ColorProvider(TextColor),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            )
            if (lunarHolidayText != context.getString(R.string.lunar_holiday_none)) {
                Text(
                    text = lunarHolidayText,
                    style = TextStyle(
                        color = ColorProvider(TextColor),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }


    class Receiver : GlanceAppWidgetReceiver() {
        override val glanceAppWidget: GlanceAppWidget = KemeticGlanceWidget()

        override fun onEnabled(context: Context) {
            super.onEnabled(context)
            scheduleNextUpdate(context)
        }

        override fun onDisabled(context: Context) {
            super.onDisabled(context)
            cancelUpdate(context)
        }

        override fun onReceive(context: Context, intent: Intent) {
            super.onReceive(context, intent)
            if (intent.action == "com.kamiwa.kemeticcalendar.GLANCE_MINUTE_UPDATE" ||
                intent.action == Intent.ACTION_BOOT_COMPLETED ||
                intent.action == Intent.ACTION_TIME_CHANGED ||
                intent.action == Intent.ACTION_TIMEZONE_CHANGED
            ) {
                MainScope().launch {
                    KemeticGlanceWidget().updateAll(context)
                }
                scheduleNextUpdate(context)
            }
        }

        private fun scheduleNextUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, Receiver::class.java).apply {
                action = "com.kamiwa.kemeticcalendar.GLANCE_MINUTE_UPDATE"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1234,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

//            val now = System.currentTimeMillis()
//            val nextAlarm: Long = now + 900000
            val calendar = java.util.Calendar.getInstance().apply {
                val minutes = get(java.util.Calendar.MINUTE)
                val nextQuarter = ((minutes / 15) + 1) * 15
                set(java.util.Calendar.MINUTE, nextQuarter)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }

            val nextAlarm = calendar.timeInMillis

            try {
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarm,
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarm,
                    pendingIntent
                )
            }
        }

        private fun cancelUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, Receiver::class.java).apply {
                action = "com.kamiwa.kemeticcalendar.GLANCE_MINUTE_UPDATE"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1234,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        }
    }
}
