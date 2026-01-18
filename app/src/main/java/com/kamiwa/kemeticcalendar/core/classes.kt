package com.kamiwa.kemeticcalendar.core

import android.content.Context
import android.util.Log
import com.kamiwa.kemeticcalendar.R
import dev.jamesyox.kastro.luna.LunarEvent
import dev.jamesyox.kastro.luna.LunarPhase
import dev.jamesyox.kastro.luna.LunarPhase.Intermediate.WaxingCrescent
import dev.jamesyox.kastro.luna.LunarPhase.Intermediate.WaxingGibbous
import dev.jamesyox.kastro.luna.LunarPhase.Intermediate.WaningGibbous
import dev.jamesyox.kastro.luna.LunarPhase.Intermediate.WaningCrescent
import dev.jamesyox.kastro.luna.LunarPhaseSequence
import dev.jamesyox.kastro.luna.LunarState
import dev.jamesyox.kastro.luna.calculateLunarIllumination
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import io.github.cosinekitty.astronomy.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.time.LocalTime
import kotlin.collections.mutableListOf
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.hours

class KemeticDate(val day: Int, val month: Int){
    val decade = day % 10 == 0

    fun getMonth(): KemeticMonth{
        return KEMETIC_MONTHS.elementAt(this.month)
    }

    fun getDayType(): Int{
        return if (this.getHolidays().isEmpty()) {
            if (decade) {
                R.string.kemetic_day_type_decade
            } else {
                R.string.kemetic_day_type_regular
            }
        } else {
            if (decade) {
                R.string.kemetic_day_type_decade_with_holiday
            } else {
                R.string.kemetic_day_type_holiday
            }
        }
    }

    fun getHolidays(): MutableList<KemeticHoliday>{
        val holidaysList = mutableListOf<KemeticHoliday>()
        for (holiday in KEMETIC_HOLIDAYS){
           if (holiday.month == this.month && holiday.day == this.day) {
                holidaysList.add(holiday)
           }
        }

        return holidaysList
    }

    fun getNextHoliday(): KemeticDate{
        val holidaysWithDecades = KEMETIC_HOLIDAYS + getDecadesHolidays()
        val sortedHoliday = holidaysWithDecades.sortedWith(compareBy<KemeticHoliday>{it.month}.thenBy { it.day })
        val nextHolidays = sortedHoliday.filter { it.month > this.month || (it.month == this.month && it.day > this.day) }

        val nextHoliday: KemeticHoliday
        if (nextHolidays.isEmpty()) {
            nextHoliday = sortedHoliday[0]
        }
        else {
            nextHoliday = nextHolidays[0]
        }

        return KemeticDate(nextHoliday.day, nextHoliday.month)

    }

    companion object {
        fun date(date: LocalDate): KemeticDate {
            var leapModifier = 0
            if (date.isLeapYear) {
                if (LocalDate.of(date.year, 2, 29) <= date &&
                    date < LocalDate.of(date.year, 3, 15)
                ) {
                    leapModifier = 1
                }
            }

            for ((idx, month) in KEMETIC_MONTHS.withIndex()) {
                var startDate = LocalDate.of(date.year, month.startMonth, month.startDay)
                var endDate = LocalDate.of(date.year, month.endMonth, month.endDay)

                if (month.startMonth > month.endMonth) {
                    if (date.month.value == 1) {
                        startDate = LocalDate.of(date.year - 1, month.startMonth, month.startDay)
                    } else {
                        endDate = LocalDate.of(date.year + 1, month.endMonth, month.startDay)
                    }
                }

                if (startDate <= date && date <= endDate) {
                    var day = (ChronoUnit.DAYS.between(startDate, date) + 1) - leapModifier
                    return KemeticDate(day.toInt(), idx)
                }


            }

            return KemeticDate(0, 0)

        }

        fun today(): KemeticDate{
            return date(LocalDate.now())
        }

        fun getDecadesHolidays(): MutableList<KemeticHoliday>{
            var decades = mutableListOf<KemeticHoliday>()

            for (month in 0..12){
                for (day in 10..30 step 10){
                    decades.add(KemeticHoliday(month, day, R.string.kemetic_day_type_decade))
                }
            }

            return decades
        }
    }

}

class LunarDate(val gregorianDate: LocalDate){

    fun getLunarPhase(): Pair<Int, Double>{
        val dayInstant = LocalDateTime(gregorianDate.year, gregorianDate.month, gregorianDate.dayOfMonth, 0, 0, 0).toInstant(TimeZone.currentSystemDefault())

        val lunarPhase = LunarPhaseSequence(
            start = dayInstant,
            limit = 24.hours,
        ).firstOrNull()

        if (lunarPhase == null) {
            val illumination = dayInstant.calculateLunarIllumination()
            val lunarPhaseAngle = LunarPhase.lunarPhase(illumination.illuminationAngle).midpointAngle

            when (lunarPhaseAngle) {
                WaxingCrescent.midpointAngle -> {
                    return  Pair<Int, Double>(R.string.lunar_phase_waxing_crescent_moon, lunarPhaseAngle)
                }
                WaxingGibbous.midpointAngle -> {
                    return Pair<Int, Double>(R.string.lunar_phase_waxing_gibbous_moon, lunarPhaseAngle)
                }
                WaningGibbous.midpointAngle -> {
                    return Pair<Int, Double>(R.string.lunar_phase_waning_gibbous_moon, lunarPhaseAngle)
                }
                WaningCrescent.midpointAngle -> {
                    return Pair<Int, Double>(R.string.lunar_phase_waning_crescent_moon, lunarPhaseAngle)
                }
            }

        }
        else {
            when (lunarPhase) {
                is LunarEvent.PhaseEvent.NewMoon -> {
                    return Pair<Int, Double>(R.string.lunar_phase_new_moon, LunarEvent.PhaseEvent.NewMoon.phase)
                }
                is LunarEvent.PhaseEvent.FirstQuarter -> {
                    return Pair<Int, Double>(R.string.lunar_phase_first_quarter, LunarEvent.PhaseEvent.FirstQuarter.phase)
                }
                is LunarEvent.PhaseEvent.FullMoon-> {
                    return Pair<Int, Double>(R.string.lunar_phase_full_moon, LunarEvent.PhaseEvent.FullMoon.phase)
                }
                is LunarEvent.PhaseEvent.LastQuarter -> {
                    return Pair<Int, Double>(R.string.lunar_phase_third_quarter, LunarEvent.PhaseEvent.LastQuarter.phase)
                }

                else -> {return Pair<Int, Double>(R.string.error_invalid_moon_phase, -9999.99)}
            }
        }

        return Pair<Int, Double>(R.string.error_invalid_moon_phase, -9999.99)
    }

    fun getNextHolidays(): MutableList<Pair<LocalDate, Int>>{
        val nextHolidays = mutableListOf<Pair<LocalDate, Int>>()
        val dayInstant = LocalDateTime(gregorianDate.year, gregorianDate.month, gregorianDate.dayOfMonth, 0, 0, 0).toInstant(TimeZone.currentSystemDefault())
        var tempHolidayDate: LocalDateTime

        for (holiday in LUNAR_HOLIDAYS){
            if (holiday.phase == "new_moon"){
                tempHolidayDate = LunarPhaseSequence(
                    start = dayInstant,
                    requestedLunarPhases = listOf(LunarEvent.PhaseEvent.NewMoon)
                ).first().time.toLocalDateTime(TimeZone.currentSystemDefault())
            }
            else if (holiday.phase == "full_moon"){
                tempHolidayDate = LunarPhaseSequence(
                    start = dayInstant,
                    requestedLunarPhases = listOf(LunarEvent.PhaseEvent.FullMoon)
                ).first().time.toLocalDateTime(TimeZone.currentSystemDefault())
            }
            else if (holiday.phase == "last_quarter"){
                tempHolidayDate = LunarPhaseSequence(
                    start = dayInstant,
                    requestedLunarPhases = listOf(LunarEvent.PhaseEvent.LastQuarter)
                ).first().time.toLocalDateTime(TimeZone.currentSystemDefault())
            }
            else {
                tempHolidayDate = LunarPhaseSequence(
                    start = dayInstant,
                    requestedLunarPhases = listOf(LunarEvent.PhaseEvent.FirstQuarter)
                ).first().time.toLocalDateTime(TimeZone.currentSystemDefault())
            }

            var holidayDate = LocalDate.of(tempHolidayDate.year, tempHolidayDate.month, tempHolidayDate.dayOfMonth)
            holidayDate = holidayDate.plusDays(holiday.delta.toLong())
            nextHolidays.add(Pair(holidayDate, holiday.key))
        }

        return nextHolidays
    }

    fun getHoliday(): Int{
        val dayInstant = LocalDateTime(gregorianDate.year, gregorianDate.month, gregorianDate.dayOfMonth, 0, 0, 0).toInstant(TimeZone.currentSystemDefault())
        var tempHolidayDate: LocalDateTime

        for (holiday in LUNAR_HOLIDAYS){
            if (holiday.phase == "new_moon"){
                tempHolidayDate = LunarPhaseSequence(
                    start = dayInstant,
                    requestedLunarPhases = listOf(LunarEvent.PhaseEvent.NewMoon)
                ).first().time.toLocalDateTime(TimeZone.currentSystemDefault())
            }
            else if (holiday.phase == "full_moon"){
                tempHolidayDate = LunarPhaseSequence(
                    start = dayInstant,
                    requestedLunarPhases = listOf(LunarEvent.PhaseEvent.FullMoon)
                ).first().time.toLocalDateTime(TimeZone.currentSystemDefault())
            }
            else if (holiday.phase == "last_quarter"){
                tempHolidayDate = LunarPhaseSequence(
                    start = dayInstant,
                    requestedLunarPhases = listOf(LunarEvent.PhaseEvent.LastQuarter)
                ).first().time.toLocalDateTime(TimeZone.currentSystemDefault())
            }
            else {
                tempHolidayDate = LunarPhaseSequence(
                    start = dayInstant,
                    requestedLunarPhases = listOf(LunarEvent.PhaseEvent.FirstQuarter)
                ).first().time.toLocalDateTime(TimeZone.currentSystemDefault())
            }

            var holidayDate = LocalDate.of(tempHolidayDate.year, tempHolidayDate.month, tempHolidayDate.dayOfMonth)
            holidayDate = holidayDate.plusDays(holiday.delta.toLong())

            if (this.gregorianDate.isEqual(holidayDate)) {
                return holiday.key
            }

        }

        return R.string.lunar_holiday_none
    }

}