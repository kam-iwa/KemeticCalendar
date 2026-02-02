package com.kamiwa.kemeticcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kamiwa.kemeticcalendar.core.CustomBottomBar
import com.kamiwa.kemeticcalendar.core.CustomTopBar
import com.kamiwa.kemeticcalendar.ui.theme.BackgroundColor
import com.kamiwa.kemeticcalendar.ui.theme.KemeticCalendarTheme
import com.kamiwa.kemeticcalendar.ui.theme.MiscColor
import com.kamiwa.kemeticcalendar.ui.theme.TextColor
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat.getString
import com.kamiwa.kemeticcalendar.core.KemeticDate
import com.kamiwa.kemeticcalendar.core.LunarDate
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Locale

class CheckActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KemeticCalendarTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CheckActivityLayout(
                        activityId = 3,
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CheckActivityLayout(
    activityId: Int,
    name: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateText by remember { mutableStateOf(getString(context, R.string.display_no_date_selected)) }
    var kemeticDateDescription by remember {mutableStateOf(getString(context, R.string.display_selected_date_label))}
    var kemeticDateTypeDescription by remember {mutableStateOf("")}
    var kemeticHolidaysDescription by remember {mutableStateOf("")}
    var lunarPhaseDescription by remember {mutableStateOf("")}
    var lunarHolidayDescription by remember {mutableStateOf("")}
    val datePickerState = rememberDatePickerState()

    val confirmEnabled = remember {
        derivedStateOf { datePickerState.selectedDateMillis != null }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundColor,
        topBar = { CustomTopBar(context) },
        bottomBar = { CustomBottomBar(context, activityId) }
    ) { padding ->

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val millis = datePickerState.selectedDateMillis
                            if (millis != null) {
                                val gregorianDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.of("UTC"))
                                    .toLocalDate()
                                val kemeticDate = KemeticDate.date(gregorianDate)
                                val lunarDate = LunarDate(gregorianDate)

                                val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                selectedDateText = formatter.format(Date(millis))
                                kemeticDateDescription = """
                                    ${getString(context, kemeticDate.getMonth().key)} ${kemeticDate.day}
                                """.trimIndent()
                                kemeticDateTypeDescription = getString(context,kemeticDate.getDayType())
                                kemeticHolidaysDescription = kemeticDate.getHolidays().map { getString(context, it.key) }.toTypedArray().joinToString("; ")
                                lunarPhaseDescription = getString(context, lunarDate.getLunarPhase().first)
                                lunarHolidayDescription = getString(context, lunarDate.getHoliday())
                            }
                            showDatePicker = false
                        },
                        enabled = confirmEnabled.value
                    ) { Text(getString(context, R.string.menu_ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text(getString(context, R.string.menu_cancel)) }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // --- LAYOUT ---
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = getString(context, R.string.display_check_date),
                style = MaterialTheme.typography.displaySmall,
                color = TextColor,
                modifier = Modifier.padding(16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "${getString(context, R.string.display_selected_date)}\n$selectedDateText",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextColor
                )

                Button(
                    onClick = { showDatePicker = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MiscColor)
                ) {
                    Text(text = getString(context, R.string.display_select_date), color = BackgroundColor)
                }
            }

            Text(
                text = kemeticDateDescription,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(12.dp),
                color = TextColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = kemeticDateTypeDescription,
                modifier = Modifier.padding(12.dp),
                color = TextColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = kemeticHolidaysDescription,
                modifier = Modifier.padding(12.dp),
                color = TextColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = lunarPhaseDescription,
                modifier = Modifier.padding(12.dp),
                color = TextColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = lunarHolidayDescription,
                modifier = Modifier.padding(12.dp),
                color = TextColor,
                textAlign = TextAlign.Center
            )
        }
    }
}