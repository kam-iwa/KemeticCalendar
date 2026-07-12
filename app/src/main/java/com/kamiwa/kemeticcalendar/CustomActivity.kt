package com.kamiwa.kemeticcalendar

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString

import com.kamiwa.kemeticcalendar.core.CustomBottomBar
import com.kamiwa.kemeticcalendar.core.CustomTopBar
import com.kamiwa.kemeticcalendar.ui.theme.BackgroundColor
import com.kamiwa.kemeticcalendar.ui.theme.KemeticCalendarTheme
import com.kamiwa.kemeticcalendar.ui.theme.MiscColor
import com.kamiwa.kemeticcalendar.ui.theme.TextColor

import java.util.Calendar


class CustomActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        createNotificationChannel()

        setContent {
            KemeticCalendarTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CustomActivityLayout(
                        activityId = 2,
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "kemetic_custom_reminders",
                "Kemetic Calendar - custom reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders about custom dates."
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun CustomActivityLayout(
    activityId: Int,
    name: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val storage = remember { StorageManager(context) }
    var savedDates by remember { mutableStateOf(storage.load()) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { CustomTopBar(context) },
        bottomBar = { CustomBottomBar(context, activityId) },
        containerColor = BackgroundColor,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = TextColor,
                contentColor = BackgroundColor,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Dodaj datę")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = getString(context, R.string.notifications_title_activity),
                color = TextColor,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            if (savedDates.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getString(context, R.string.notifications_label_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextColor,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp),
                ) {
                    items(savedDates) { dateRecord ->
                        DateItemRow(
                            record = dateRecord,
                            onDelete = {
                                cancelAlarm(context, dateRecord)
                                val newList = savedDates.filter { it.id != dateRecord.id }
                                savedDates = newList
                                storage.save(newList)
                            }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AddDateDialog(
                onDismiss = { showDialog = false },
                onConfirm = { newName, newDesc, dayOfMonth, month, hourOfDay, minute ->
                    val newRecord = SavedDate(
                        id = System.currentTimeMillis().toInt(),
                        name = newName,
                        description = newDesc,
                        dayOfMonth = dayOfMonth,
                        month = month,
                        hourOfDay = hourOfDay,
                        minute = minute
                    )
                    val newList = savedDates + newRecord
                    savedDates = newList
                    storage.save(newList)
                    scheduleYearlyAlarm(context, newRecord)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun DateItemRow(record: SavedDate, onDelete: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MiscColor,
            contentColor = BackgroundColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                if (record.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = record.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${getString(context, R.string.notifications_yearly)} ${String.format("%02d.%02d", record.dayOfMonth, record.month + 1)} o ${String.format("%02d:%02d", record.hourOfDay, record.minute)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = BackgroundColor
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Usuń",
                    tint = BackgroundColor
                )
            }
        }
    }
}

@Composable
fun AddDateDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Int, Int, Int) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    var selectedHour by remember { mutableStateOf<Int?>(null) }
    var selectedMinute by remember { mutableStateOf<Int?>(null) }
    var selectedDateText by remember { mutableStateOf(getString(context, R.string.notifications_label_not_selected)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(getString(context, R.string.notifications_title_new)) },
        containerColor = BackgroundColor,
        titleContentColor = TextColor, // kolor tytułu
        textContentColor = TextColor, // kolor treści
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(getString(context, R.string.notifications_label_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TextColor,
                        unfocusedBorderColor = MiscColor,
                        cursorColor = TextColor,
                        focusedLabelColor = TextColor,
                        unfocusedLabelColor = MiscColor

                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(getString(context, R.string.notifications_label_desc)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TextColor,
                        unfocusedBorderColor = MiscColor,
                        cursorColor = TextColor,
                        focusedLabelColor = TextColor,
                        unfocusedLabelColor = MiscColor
                    ),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = selectedDateText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Button(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, _, month, dayOfMonth ->
                                selectedDay = dayOfMonth
                                selectedMonth = month

                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        selectedHour = hourOfDay
                                        selectedMinute = minute
                                        selectedDateText = "${getString(context, R.string.notifications_yearly)} ${String.format("%02d.%02d", dayOfMonth, month + 1)} o ${String.format("%02d:%02d", hourOfDay, minute)}"
                                    },
                                    12,
                                    0,
                                    true
                                ).show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    colors = ButtonDefaults.textButtonColors(containerColor = MiscColor, contentColor = BackgroundColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(getString(context, R.string.notifications_button_select_date))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && selectedDay != null && selectedMonth != null &&
                        selectedHour != null && selectedMinute != null) {
                        onConfirm(name, desc, selectedDay!!, selectedMonth!!, selectedHour!!, selectedMinute!!)
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = TextColor),
                enabled = name.isNotBlank() && selectedDay != null && selectedMonth != null &&
                        selectedHour != null && selectedMinute != null
            ) {
                Text(getString(context, R.string.menu_save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextColor)
            ) {
                Text(getString(context, R.string.menu_cancel))
            }
        }
    )
}


fun scheduleYearlyAlarm(context: Context, record: SavedDate) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, record.dayOfMonth)
        set(Calendar.MONTH, record.month)
        set(Calendar.HOUR_OF_DAY, record.hourOfDay)
        set(Calendar.MINUTE, record.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.YEAR, 1)
        }
    }

    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("name", record.name)
        putExtra("desc", record.description)
        putExtra("id", record.id)
        putExtra("dayOfMonth", record.dayOfMonth)
        putExtra("month", record.month)
        putExtra("hourOfDay", record.hourOfDay)
        putExtra("minute", record.minute)
        putExtra("isYearly", true)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        record.id,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
        alarmManager.setAndAllowWhileIdle(
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

fun cancelAlarm(context: Context, record: SavedDate) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        record.id,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    pendingIntent?.let {
        alarmManager.cancel(it)
        it.cancel()
    }
}
