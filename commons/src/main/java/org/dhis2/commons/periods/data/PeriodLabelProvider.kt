package org.dhis2.commons.periods.data

import android.util.Log
import org.hisp.dhis.android.core.period.PeriodType
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class PeriodLabelProvider {

    private val ethiopianMonthNames = listOf(
        "Meskerem", "Tikimt", "Hidar", "Tahsas",
        "Tir", "Yekatit", "Megabit", "Miazia",
        "Ginbot", "Sene", "Hamle", "Nehase", "Pagume"
    )

    // Templates
    private val defaultQuarterlyLabel = "Q%d %s (%s - %s)"
    private val defaultWeeklyLabel = "Week %d: %s - %s, %s"

    operator fun invoke(
        periodType: PeriodType?,
        periodId: String,
        periodStartDate: Date,
        periodEndDate: Date,
        locale: Locale,
        forTags: Boolean = false
    ): String {
        return try {
            Log.d("EthiopianDateDebug", "PeriodType: $periodType → Start: $periodStartDate, End: $periodEndDate")
            val periodBetweenYears = periodIsBetweenYears(periodStartDate, periodEndDate)
            if (forTags) {
                tagPeriodLabels(periodType, periodStartDate, periodEndDate,periodId)
            } else {
                defaultPeriodLabels(periodType, periodId, periodStartDate, periodEndDate, periodBetweenYears)
            }
        } catch (e: Exception) {
            Log.e("PeriodLabelProvider", "Error formatting period", e)
            SimpleDateFormat("MMM d, yyyy", locale).format(periodStartDate)
        }
    }

    // ---------- Label Builders ----------

    fun tagPeriodLabels(
        periodType: PeriodType?,
        startDate: Date,
        endDate: Date,
        periodId: String
    ): String {
        return when (periodType) {

            PeriodType.BiWeekly -> {
                android.util.Log.w("BiWeeklyPeriod", "periodId: $periodId")
                val match = Regex("(\\d{4})BiW(\\d{1,2})").find(periodId)
                if (match != null) {
                    val (year, index) = match.destructured
                    "Biweek $index, $year"
                } else {
                    // Fallback if no periodId match
                    val startEthio = EthiopianDateConverter.gregorianToEthiopian(startDate)
                    val endEthio = EthiopianDateConverter.gregorianToEthiopian(endDate)

                    if (startEthio.month == endEthio.month) {
                        "${ethiopianMonthNames[startEthio.month - 1]} " +
                                "${startEthio.day.toString().padStart(2, '0')}–${endEthio.day.toString().padStart(2, '0')}, ${endEthio.year}"
                    } else {
                        "${ethiopianMonthNames[startEthio.month - 1]} ${startEthio.day.toString().padStart(2, '0')} – " +
                                "${ethiopianMonthNames[endEthio.month - 1]} ${endEthio.day.toString().padStart(2, '0')}, ${endEthio.year}"
                    }
                }
            }

            PeriodType.Monthly -> safeFormatEthiopianMonthYear(startDate)

            PeriodType.BiMonthly, PeriodType.SixMonthly, PeriodType.Quarterly -> {
                "${safeFormatEthiopianMonth(startDate)} - ${safeFormatEthiopianMonthYear(endDate)}"
            }

            PeriodType.FinancialApril,
            PeriodType.FinancialJuly,
            PeriodType.FinancialOct -> {
                val startEthio = EthiopianDateConverter.gregorianToEthiopian(startDate)
                val endEthio = EthiopianDateConverter.gregorianToEthiopian(endDate)
                "${ethiopianMonthNames[startEthio.month ]} ${startEthio.year} – " +
                        "${ethiopianMonthNames[endEthio.month - 1]} ${endEthio.year}"
            }

            PeriodType.Yearly -> {
                Log.d("EthiopianDateDebug", "[TAG] Yearly → Start: $startDate")
                safeFormatEthiopianYear(startDate)
            }

            else -> safeFormatEthiopianDate(startDate)
        }
    }

    private fun defaultPeriodLabels(
        periodType: PeriodType?,
        periodId: String,
        startDate: Date,
        endDate: Date,
        periodBetweenYears: Boolean
    ): String {
        return when (periodType) {
            PeriodType.Weekly -> {
                defaultWeeklyLabel.format(
                    weekOfTheYear(periodType!!, periodId),
                    safeFormatEthiopianDateShort(startDate),
                    safeFormatEthiopianDateShort(endDate),
                    safeFormatEthiopianYear(endDate)
                )
            }



            PeriodType.Monthly -> safeFormatEthiopianMonthYear(startDate)

            PeriodType.BiMonthly, PeriodType.SixMonthly -> {
                "${safeFormatEthiopianMonth(startDate)} - ${safeFormatEthiopianMonthYear(endDate)}"
            }

            PeriodType.Quarterly -> {
                val quarterNumber = quarter(periodType!!, periodId)
                val year = safeFormatEthiopianYear(startDate)
                val startMonth = safeFormatEthiopianMonth(startDate)
                val endMonth = safeFormatEthiopianMonth(endDate)
                defaultQuarterlyLabel.format(quarterNumber, year, startMonth, endMonth)
            }

            PeriodType.FinancialApril,
            PeriodType.FinancialJuly,
            PeriodType.FinancialOct -> {
                val startEthio = EthiopianDateConverter.gregorianToEthiopian(startDate)
                val endEthio = EthiopianDateConverter.gregorianToEthiopian(endDate)
                "${ethiopianMonthNames[startEthio.month - 1]} ${startEthio.year} – " +
                        "${ethiopianMonthNames[endEthio.month - 1]} ${endEthio.year}"
            }

            PeriodType.Yearly ->

                safeFormatEthiopianYear(startDate)

            else -> safeFormatEthiopianDate(startDate)
        }
    }

    // ---------- Formatting Helpers ----------

    private fun safeFormatEthiopianDate(date: Date): String {
        val ethioDate = EthiopianDateConverter.gregorianToEthiopian(date)
        return "${ethioDate.day} ${ethiopianMonthNames[ethioDate.month - 1]}, ${ethioDate.year}"
    }

    private fun safeFormatEthiopianDateShort(date: Date): String {
        val ethioDate = EthiopianDateConverter.gregorianToEthiopian(date)
        return "${ethiopianMonthNames[ethioDate.month - 1].take(3)} ${ethioDate.day}"
    }

    private fun safeFormatEthiopianMonth(date: Date): String {
        val ethioDate = EthiopianDateConverter.gregorianToEthiopian(date)
        return ethiopianMonthNames[ethioDate.month - 1]
    }

    private fun safeFormatEthiopianMonthYear(date: Date): String {
        val ethioDate = EthiopianDateConverter.gregorianToEthiopian(date)
        return "${ethiopianMonthNames[ethioDate.month - 1]} ${ethioDate.year}"
    }

    private fun safeFormatEthiopianYear(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.YEAR).toString()
    }

    // ---------- Utilities ----------
    private fun weekOfTheYear(periodType: PeriodType, periodId: String): Int {
        val pattern = Pattern.compile(periodType.pattern)
        val matcher = pattern.matcher(periodId)
        return if (matcher.find()) matcher.group(2)?.toInt() ?: 0 else 0
    }

    private fun quarter(periodType: PeriodType, periodId: String): Int {
        val pattern = Pattern.compile(periodType.pattern)
        val matcher = pattern.matcher(periodId)
        return if (matcher.find()) matcher.group(2)?.toInt() ?: 0 else 0
    }

    private fun periodIsBetweenYears(startDate: Date, endDate: Date): Boolean {
        val startYear = EthiopianDateConverter.gregorianToEthiopian(startDate).year
        val endYear = EthiopianDateConverter.gregorianToEthiopian(endDate).year
        return startYear != endYear
    }
}