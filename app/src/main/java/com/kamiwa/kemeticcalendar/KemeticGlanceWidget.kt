package com.kamiwa.kemeticcalendar

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.LocalContext
import com.kamiwa.kemeticcalendar.ui.theme.BackgroundColor
import com.kamiwa.kemeticcalendar.ui.theme.TextColor
import com.kamiwa.kemeticcalendar.ui.theme.TextDecadeColor

object KemeticWidgetKeys {
    val GREGORIAN_DAY = stringPreferencesKey("gregorian_day")
    val KEMETIC_MONTH = stringPreferencesKey("kemetic_month")
    val KEMETIC_DAY = stringPreferencesKey("kemetic_day")
    val KEMETIC_DAY_TYPE = stringPreferencesKey("kemetic_day_type")
    val KEMETIC_HOLIDAYS = stringPreferencesKey("kemetic_holidays")
    val IS_DECADE = booleanPreferencesKey("is_decade")
    val UPDATED_AT = stringPreferencesKey("updated_at")
}

class KemeticGlanceWidget : GlanceAppWidget() {

    // Responsywność "z pudełka" — Glance sam wybierze najbliższy pasujący rozmiar
    override val sizeMode = SizeMode.Responsive(
        setOf(
            androidx.compose.ui.unit.DpSize(120.dp, 80.dp),
            androidx.compose.ui.unit.DpSize(180.dp, 110.dp),
            androidx.compose.ui.unit.DpSize(250.dp, 150.dp),
            androidx.compose.ui.unit.DpSize(300.dp, 200.dp)
        )
    )

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent()
        }
    }

    @Composable
    private fun WidgetContent() {
        val prefs = currentState<Preferences>()
        val gregorianDay = prefs[KemeticWidgetKeys.GREGORIAN_DAY] ?: ""
        val kemeticMonth = prefs[KemeticWidgetKeys.KEMETIC_MONTH] ?: ""
        val kemeticDay = prefs[KemeticWidgetKeys.KEMETIC_DAY] ?: ""
        val kemeticDayType = prefs[KemeticWidgetKeys.KEMETIC_DAY_TYPE] ?: ""
        val kemeticHolidays = prefs[KemeticWidgetKeys.KEMETIC_HOLIDAYS] ?: ""
        val isDecade = prefs[KemeticWidgetKeys.IS_DECADE] ?: false
        val updatedAt = prefs[KemeticWidgetKeys.UPDATED_AT] ?: ""

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(BackgroundColor, BackgroundColor))
                .padding(8.dp)
                .clickable(actionStartActivity(
                    Intent(
                        LocalContext.current,
                        MainActivity::class.java
                    )
                )),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = gregorianDay,
                style = TextStyle(fontSize = 10.sp, color = ColorProvider(TextColor, TextColor))
            )
            Text(
                text = kemeticMonth,
                style = TextStyle(fontSize = 18.sp, color = ColorProvider(TextColor, TextColor))
            )
            Text(
                text = kemeticDay,
                style = TextStyle(
                    fontSize = 32.sp,
                    color = if (isDecade) ColorProvider(TextDecadeColor, TextDecadeColor) else ColorProvider(TextColor, TextColor)
                )
            )
            Text(
                text = kemeticDayType,
                style = TextStyle(fontSize = 14.sp, color = ColorProvider(TextColor, TextColor))
            )
            Text(
                text = kemeticHolidays,
                style = TextStyle(fontSize = 12.sp, color = ColorProvider(TextColor, TextColor))
            )
            Text(
                text = updatedAt,
                style = TextStyle(fontSize = 10.sp, color = ColorProvider(TextColor, TextColor))
            )

        }
    }
}