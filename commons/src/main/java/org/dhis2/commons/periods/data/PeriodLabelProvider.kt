package org.dhis2.commons.periods.data

import android.util.Log
import com.ibm.icu.util.EthiopicCalendar
import org.apache.commons.text.WordUtils
import org.dhis2.commons.periods.model.FROM_TO_LABEL
import org.hisp.dhis.android.core.period.PeriodType
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class PeriodLabelProvider {

    // Ethiopian month names in English
    private val ethiopianMonthNames = listOf(
        "Meskerem", "Tikimt", "Hidar", "Tahsas",
        "Tir", "Yekatit", "Megabit", "Miazia",
        "Ginbot", "Sene", "Hamle", "Nehase", "Pagume"
    )

    // Templates
    private val defaultQuarterlyLabel = "Q%d %s (%s - %s)"
    private val defaultWeeklyLabel = "Week %d: %s - %s, %s"
    private val defaultBiWeeklyLabel = "%s - %s, %s"
    private val biWeeklyLabelBetweenYears = "%s, %s - %s, %s"

    operator fun invoke(
        periodType: PeriodType?,
        periodId: String,
        periodStartDate: Date,
        periodEndDate: Date,
        locale: Locale,
        forTags: Boolean = false,
    ): String {
        return try {
            Log.d("EthiopianDateDebug", "periodtype: $periodType → periodstartdate: $periodStartDate")
            val periodBetweenYears = periodIsBetweenYears(periodStartDate, periodEndDate)
            val formattedDate = if (forTags) {
                tagPeriodLabels(periodType, periodStartDate, periodEndDate, locale, periodBetweenYears)
            } else {
                defaultPeriodLabels(periodType, periodId, periodStartDate, periodEndDate, locale)
            }
            WordUtils.capitalize(formattedDate)
        } catch (e: Exception) {
            Log.e("PeriodLabelProvider", "Error formatting period", e)
            SimpleDateFormat("MMM d, yyyy", locale).format(periodStartDate)
        }
    }

    // ---------- Label Builders ----------

    private fun tagPeriodLabels(
        periodType: PeriodType?,
        periodStartDate: Date,
        periodEndDate: Date,
        locale: Locale,
        periodBetweenYears: Boolean,
    ): String {
        return when (periodType) {
            PeriodType.Weekly, PeriodType.WeeklyWednesday, PeriodType.WeeklyThursday,
            PeriodType.WeeklySaturday, PeriodType.WeeklySunday, PeriodType.BiWeekly -> {
                if (periodBetweenYears) {
                    FROM_TO_LABEL.format(
                        safeFormatEthiopianDate(periodStartDate),
                        safeFormatEthiopianDate(periodEndDate)
                    )
                } else {
                    FROM_TO_LABEL.format(
                        safeFormatEthiopianDateShort(periodStartDate),
                        safeFormatEthiopianDate(periodEndDate)
                    )
                }
            }

            PeriodType.Monthly -> safeFormatEthiopianMonthYearShort(periodStartDate)

            PeriodType.BiMonthly, PeriodType.SixMonthly, PeriodType.SixMonthlyApril,
            PeriodType.Quarterly, PeriodType.QuarterlyNov, PeriodType.FinancialApril,
            PeriodType.FinancialJuly, PeriodType.FinancialOct -> {
                if (periodBetweenYears) {
                    FROM_TO_LABEL.format(
                        safeFormatEthiopianMonthYearShort(periodStartDate),
                        safeFormatEthiopianMonthYearShort(periodEndDate)
                    )
                } else {
                    FROM_TO_LABEL.format(
                        safeFormatEthiopianMonthShort(periodStartDate),
                        safeFormatEthiopianMonthYearShort(periodEndDate)
                    )
                }
            }

            PeriodType.Yearly -> safeFormatEthiopianYear(periodStartDate)
            else -> safeFormatEthiopianDate(periodStartDate)
        }
    }

    private fun defaultPeriodLabels(
        periodType: PeriodType?,
        periodId: String,
        periodStartDate: Date,
        periodEndDate: Date,
        locale: Locale,
    ): String {
        return when (periodType) {

            PeriodType.Weekly, PeriodType.WeeklyWednesday, PeriodType.WeeklyThursday,
            PeriodType.WeeklySaturday, PeriodType.WeeklySunday -> {
                defaultWeeklyLabel.format(
                    weekOfTheYear(periodType!!, periodId),
                    safeFormatEthiopianDateShort(periodStartDate),
                    safeFormatEthiopianDateShort(periodEndDate),
                    safeFormatEthiopianYear(periodEndDate)
                )
            }

            PeriodType.BiWeekly -> {
                if (periodIsBetweenYears(periodStartDate, periodEndDate)) {
                    biWeeklyLabelBetweenYears.format(
                        safeFormatEthiopianDateShort(periodStartDate),
                        safeFormatEthiopianYear(periodStartDate),
                        safeFormatEthiopianDateShort(periodEndDate),
                        safeFormatEthiopianYear(periodEndDate))
                } else {
                    defaultBiWeeklyLabel.format(
                        safeFormatEthiopianDateShort(periodStartDate),
                        safeFormatEthiopianDateShort(periodEndDate),
                        safeFormatEthiopianYear(periodEndDate))
                }
            }

            PeriodType.Monthly -> safeFormatEthiopianMonthYear(periodStartDate)

            PeriodType.BiMonthly, PeriodType.SixMonthly, PeriodType.SixMonthlyApril -> {
                FROM_TO_LABEL.format(
                    safeFormatEthiopianMonth(periodStartDate),
                    safeFormatEthiopianMonthYear(periodEndDate))
            }

            PeriodType.Quarterly, PeriodType.QuarterlyNov -> {
                val startYear = safeFormatEthiopianYear(periodStartDate)
                val endYear = safeFormatEthiopianYear(periodEndDate)
                val (yearFormat, initMonthFormat) = if (startYear != endYear) {
                    Pair(
                        safeFormatEthiopianYear(periodEndDate),
                        safeFormatEthiopianMonthYear(periodStartDate)
                    )
                } else {
                    Pair(
                        safeFormatEthiopianYear(periodStartDate),
                        safeFormatEthiopianMonth(periodStartDate))
                }
                defaultQuarterlyLabel.format(
                    quarter(periodType!!, periodId),
                    yearFormat,
                    initMonthFormat,
                    safeFormatEthiopianMonth(periodEndDate))
            }

            PeriodType.FinancialApril, PeriodType.FinancialJuly, PeriodType.FinancialOct -> {
                FROM_TO_LABEL.format(
                    safeFormatEthiopianMonthYear(periodStartDate),
                    safeFormatEthiopianMonthYear(periodEndDate))
            }
            PeriodType.Yearly -> {
                Log.d("PeriodDebug", "Period Start Date (Gregorian): $periodStartDate")
                safeFormatEthiopianYear(periodStartDate)
            }

            else -> {
                Log.d("PeriodDebug", "Period Start Date (Gregorian): $periodStartDate")
                safeFormatEthiopianDate(periodStartDate)
            }

        }
    }

    // ---------- ICU4J Ethiopian Conversion ----------

    private fun gregorianToEthiopian(date: Date): Triple<Int, Int, Int> {
        val ethCal = EthiopicCalendar()
        ethCal.time = date
        val year = ethCal.get(Calendar.YEAR)
        val month = ethCal.get(Calendar.MONTH) + 1 // ICU months are 0-based
        val day = ethCal.get(Calendar.DAY_OF_MONTH)
        return Triple(year, month, day)
    }

    // ---------- Formatting Helpers ----------

    private fun safeFormatEthiopianDate(date: Date): String {
        return try {
            val (year, month, day) = gregorianToEthiopian(date)
            "${day} ${ethiopianMonthNames[month - 1]}, $year"
        } catch (e: Exception) {
            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
        }
    }

    private fun safeFormatEthiopianDateShort(date: Date): String {
        return try {
            val (_, month, day) = gregorianToEthiopian(date)
            "${day} ${ethiopianMonthNames[month - 1].take(3)}"
        } catch (e: Exception) {
            SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
        }
    }

    private fun safeFormatEthiopianMonth(date: Date): String {
        return try {
            val (_, month, _) = gregorianToEthiopian(date)
            ethiopianMonthNames[month - 1]
        } catch (e: Exception) {
            SimpleDateFormat("MMM", Locale.getDefault()).format(date)
        }
    }

    private fun safeFormatEthiopianMonthShort(date: Date): String {
        return try {
            val (_, month, _) = gregorianToEthiopian(date)
            ethiopianMonthNames[month - 1].take(3)
        } catch (e: Exception) {
            SimpleDateFormat("MMM", Locale.getDefault()).format(date)
        }
    }

    private fun safeFormatEthiopianMonthYear(date: Date): String {
        return try {
            val (year, month, _) = gregorianToEthiopian(date)
            "${ethiopianMonthNames[month - 1]} $year"
        } catch (e: Exception) {
            SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date)
        }
    }

    private fun safeFormatEthiopianMonthYearShort(date: Date): String {
        return try {
            val (year, month, _) = gregorianToEthiopian(date)
            "${ethiopianMonthNames[month - 1].take(3)} $year"
        } catch (e: Exception) {
            SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date)
        }
    }

    private fun safeFormatEthiopianYear(date: Date): String {
        return try {
            val (year, _, _) = gregorianToEthiopian(date)
            Log.d("EthiopianDateDebug", "Gregorian: $date → Ethiopian: $year")
            year.toString()
        } catch (e: Exception) {
            SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
        }
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
        return try {
            val (startYear, _, _) = gregorianToEthiopian(startDate)
            val (endYear, _, _) = gregorianToEthiopian(endDate)
            startYear != endYear
        } catch (e: Exception) {
            false
        }
    }
}
