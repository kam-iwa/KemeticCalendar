package com.kamiwa.kemeticcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kamiwa.kemeticcalendar.core.KemeticDate
import com.kamiwa.kemeticcalendar.ui.theme.BackgroundColor
import com.kamiwa.kemeticcalendar.ui.theme.KemeticCalendarTheme
import com.kamiwa.kemeticcalendar.ui.theme.TextColor
import com.kamiwa.kemeticcalendar.ui.theme.TextDecadeColor
import java.time.LocalDate
import com.kamiwa.kemeticcalendar.core.CustomBottomBar
import com.kamiwa.kemeticcalendar.core.CustomTopBar
import com.kamiwa.kemeticcalendar.core.LunarDate
import java.time.format.DateTimeFormatter

class MonthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val gregorianToday = LocalDate.now()
        val kemeticToday = KemeticDate.today()

        setContent {
            KemeticCalendarTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MonthActivityLayout(
                        activityId = 1,
                        modifier = Modifier
                            .padding(innerPadding),
                        gregorianDate = gregorianToday,
                        kemeticDate = kemeticToday
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthActivityLayout(
    activityId: Int,
    modifier: Modifier,
    gregorianDate: LocalDate,
    kemeticDate: KemeticDate
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
            CustomBottomBar(context, activityId)
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .background(BackgroundColor)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            //verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(kemeticDate.getMonth().key),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displaySmall,
                color = TextColor,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Column(
                modifier = modifier
                    .fillMaxWidth(0.95f)
            ) {
                repeat(3) { rowIndex ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        repeat(10) { columnIndex ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .then(
                                        if ((columnIndex + 1) + (rowIndex * 10) == kemeticDate.day){
                                            Modifier.border(2.5.dp, TextColor)
                                        } else {
                                            Modifier
                                        }
                                    )
                                    ,
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${(columnIndex + 1) + (rowIndex * 10)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (((columnIndex + 1) + (rowIndex * 10)) % 10 == 0) TextDecadeColor else TextColor,
                                    fontWeight = if (((columnIndex + 1) + (rowIndex * 10)) % 10 == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "Następne święto: \n${stringResource(kemeticDate.getNextHoliday().getMonth().key)} ${
                    kemeticDate.getNextHoliday().day}\n${kemeticDate.getNextHoliday().getHolidays().map { stringResource(id = it.key) }
                    .toTypedArray()
                    .joinToString("; ")
                    }\n",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                color = TextColor,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Text(//stringResource(id = it.second) to it.first
                text = "Następne święta lunarne: \n${LunarDate(gregorianDate).getNextHolidays().map{
                    (date, stringResId) -> "${stringResource(stringResId)}: ${date.format(
                    DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
                }.toTypedArray().joinToString("\n")}",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                color = TextColor,
                modifier = Modifier
                    .fillMaxWidth()
            )

        }

    }
}

