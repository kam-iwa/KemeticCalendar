package com.kamiwa.kemeticcalendar

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

data class SavedDate(
    val id: Int,
    val name: String,
    val description: String,
    val dayOfMonth: Int,     // Dzień miesiąca (1-31)
    val month: Int,          // Miesiąc (0-11, gdzie 0=styczeń)
    val hourOfDay: Int,      // Godzina (0-23)
    val minute: Int          // Minuta (0-59)
)

class StorageManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("kemetic_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "saved_yearly_dates"

    fun save(list: List<SavedDate>) {
        prefs.edit().putString(key, gson.toJson(list)).apply()
    }

    fun load(): List<SavedDate> {
        val json = prefs.getString(key, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<SavedDate>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra("name") ?: "Przypomnienie"
        val description = intent.getStringExtra("desc") ?: ""
        val id = intent.getIntExtra("id", 0)
        val isYearly = intent.getBooleanExtra("isYearly", false)

        showNotification(context, id, name, description)

        if (isYearly) {
            val dayOfMonth = intent.getIntExtra("dayOfMonth", 1)
            val month = intent.getIntExtra("month", 0)
            val hourOfDay = intent.getIntExtra("hourOfDay", 12)
            val minute = intent.getIntExtra("minute", 0)

            scheduleNextYearAlarm(context, id, name, description, dayOfMonth, month, hourOfDay, minute)
        }
    }

    private fun scheduleNextYearAlarm(
        context: Context,
        id: Int,
        name: String,
        description: String,
        dayOfMonth: Int,
        month: Int,
        hourOfDay: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.MONTH, month)
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.YEAR, 1) // Dodaj rok
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("name", name)
            putExtra("desc", description)
            putExtra("id", id)
            putExtra("dayOfMonth", dayOfMonth)
            putExtra("month", month)
            putExtra("hourOfDay", hourOfDay)
            putExtra("minute", minute)
            putExtra("isYearly", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun showNotification(context: Context, id: Int, title: String, text: String) {
        val channelId = "kemetic_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tworzenie kanału dla Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Przypomnienia Kalendarz",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Powiadomienia o zapisanych datach"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Budowanie powiadomienia
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(id, notification)
    }
}