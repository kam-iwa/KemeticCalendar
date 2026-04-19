package com.kamiwa.kemeticcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.kamiwa.kemeticcalendar.core.KemeticDate
import com.kamiwa.kemeticcalendar.core.LunarDate
import com.kamiwa.kemeticcalendar.ui.theme.BackgroundColor
import com.kamiwa.kemeticcalendar.ui.theme.KemeticCalendarTheme
import com.kamiwa.kemeticcalendar.ui.theme.TextColor
import com.kamiwa.kemeticcalendar.ui.theme.TextDecadeColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.kamiwa.kemeticcalendar.core.CustomBottomBar
import com.kamiwa.kemeticcalendar.core.CustomTopBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val gregorianToday = LocalDate.now()
        val kemeticToday = KemeticDate.today()
        val lunarToday = LunarDate(gregorianToday)

        val gregorianDayText = gregorianToday.format(
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault())
        )
        val kemeticMonthText = getString(kemeticToday.getMonth().key)
        val kemeticDayText = kemeticToday.day.toString()
        val kemeticDayTypeText = getString((kemeticToday.getDayType()))
        val kemeticHolidaysText = kemeticToday.getHolidays().map { getString(it.key) }
            .toTypedArray()
            .joinToString("; ")

        val lunarPhaseText = getString(lunarToday.getLunarPhase().first)
        val lunarPhasePercent = lunarToday.getLunarPhase().third * 100
        val lunarHolidayText = getString(lunarToday.getHoliday())

        setContent {
            KemeticCalendarTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainActivityLayout(
                        activityId = 0,
                        modifier = Modifier
                            .padding(innerPadding),
                        kemeticDate = kemeticToday,
                        gregorianDayText = gregorianDayText,
                        kemeticMonthText = kemeticMonthText,
                        kemeticDayText = kemeticDayText,
                        kemeticDayTypeText = kemeticDayTypeText,
                        kemeticHolidaysText = kemeticHolidaysText,
                        lunarPhaseText = lunarPhaseText,
                        lunarPhasePercent = lunarPhasePercent,
                        lunarHolidayText = lunarHolidayText

                    )
                }
            }
        }
    }
}

@Composable
fun MainActivityLayout(
    activityId: Int,
    modifier: Modifier,
    kemeticDate: KemeticDate,
    gregorianDayText: String,
    kemeticMonthText: String,
    kemeticDayText: String,
    kemeticDayTypeText: String,
    kemeticHolidaysText: String,
    lunarPhaseText: String,
    lunarPhasePercent: Double,
    lunarHolidayText: String
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
        containerColor = BackgroundColor,
        topBar = {
            CustomTopBar(context)
        },
        bottomBar = {
            CustomBottomBar(context,activityId)
        }
    ) { padding ->
        Column(
                modifier = modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(BackgroundColor)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = gregorianDayText,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextColor,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Text(
                    text = kemeticMonthText,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displaySmall,
                    color = TextColor,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Text(
                    text = kemeticDayText,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displayLarge,
                    color = if (kemeticDate.decade) TextDecadeColor else TextColor,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Text(
                    text = kemeticDayTypeText,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextColor,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Text(
                    text = kemeticHolidaysText,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextColor,
                    modifier = Modifier
                        .fillMaxWidth()
                )

                Text(
                    text = String.format("$lunarPhaseText (%.0f %%)", lunarPhasePercent),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextColor,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Text(
                    text = lunarHolidayText,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextColor,
                    modifier = Modifier
                        .fillMaxWidth()
                )

            }

        }

}

