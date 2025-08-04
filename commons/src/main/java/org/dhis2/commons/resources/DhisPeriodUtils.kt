package org.dhis2.commons.resources

import com.ibm.icu.util.EthiopicCalendar
import com.ibm.icu.util.ULocale
import org.apache.commons.text.WordUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.period.PeriodType
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

const val DATE_FORMAT_EXPRESSION = "yyyy-MM-dd"
const val MONTHLY_FORMAT_EXPRESSION = "MMM yyyy"
const val YEARLY_FORMAT_EXPRESSION = "yyyy"
const val SIMPLE_DATE_FORMAT = "dd/MM/yyyy"

class DhisPeriodUtils(
    d2: D2,
    private val defaultPeriodLabel: String,
    private val defaultWeeklyLabel: String,
    private val defaultBiWeeklyLabel: String,
) {

    private val periodHelper = d2.periodModule().periodHelper()

    fun getPeriodUIString(periodType: PeriodType?, date: Date, locale: Locale): String {
        val formattedDate: String
        var periodString = defaultPeriodLabel
        val period =
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(periodType ?: PeriodType.Daily, date)

        when (periodType) {
            PeriodType.Weekly,
            PeriodType.WeeklyWednesday,
            PeriodType.WeeklyThursday,
            PeriodType.WeeklySaturday,
            PeriodType.WeeklySunday -> {
                periodString = defaultWeeklyLabel
                formattedDate = periodString.format(
                    weekOfTheYear(periodType, period.periodId()!!),
                    ethiopianFormatted(period.startDate()!!),
                    ethiopianFormatted(period.endDate()!!),
                )
            }

            PeriodType.BiWeekly -> {
                periodString = defaultBiWeeklyLabel
                val firstWeekPeriod = periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                    PeriodType.Weekly,
                    period.startDate()!!,
                )
                val secondWeekPeriod = periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                    PeriodType.Weekly,
                    period.endDate()!!,
                )
                formattedDate = periodString.format(
                    weekOfTheYear(PeriodType.Weekly, firstWeekPeriod.periodId()!!),
                    getEthiopianYear(period.startDate()!!),
                    weekOfTheYear(PeriodType.Weekly, secondWeekPeriod.periodId()!!),
                    getEthiopianYear(period.endDate()!!),
                )
            }

            PeriodType.Monthly ->
                formattedDate = getEthiopianMonthYear(period.startDate()!!)

            PeriodType.BiMonthly,
            PeriodType.Quarterly,
            PeriodType.QuarterlyNov,
            PeriodType.SixMonthly,
            PeriodType.SixMonthlyApril,
            PeriodType.FinancialApril,
            PeriodType.FinancialJuly,
            PeriodType.FinancialOct -> formattedDate = periodString.format(
                getEthiopianMonthYear(period.startDate()!!),
                getEthiopianMonthYear(period.endDate()!!),
            )

            PeriodType.Yearly -> {
                val ethYear = getEthiopianYear(period.startDate()!!)
                formattedDate = (ethYear + 8).toString()  // Converts to Gregorian-style label
            }



            else ->
                formattedDate = ethiopianFormatted(period.startDate()!!)
        }

        return WordUtils.capitalize(formattedDate)
    }

    private fun weekOfTheYear(periodType: PeriodType, periodId: String): Int {
        val pattern = Pattern.compile(periodType.pattern)
        val matcher = pattern.matcher(periodId)
        return if (matcher.find()) matcher.group(2)?.toInt() ?: 0 else 0
    }

    private fun toEthiopianDate(date: Date): Triple<Int, Int, Int> {
        val calendar = EthiopicCalendar(ULocale("am_ET@calendar=ethiopic"))
        calendar.time = date
        val year = calendar.get(EthiopicCalendar.YEAR)
        val month = calendar.get(EthiopicCalendar.MONTH) + 1
        val day = calendar.get(EthiopicCalendar.DATE)
        return Triple(year, month, day)
    }

    private fun ethiopianFormatted(date: Date): String {
        val (year, month, day) = toEthiopianDate(date)
        return "$day ${getEthiopianMonthName(month)} $year"
    }

    private fun getEthiopianYear(date: Date): Int {
        return toEthiopianDate(date).first
    }

    private fun getEthiopianMonthYear(date: Date): String {
        val (year, month, _) = toEthiopianDate(date)
        return "${getEthiopianMonthName(month)} $year"
    }

    private fun getEthiopianMonthName(month: Int): String {
        return listOf(
            "Meskerem", "Tikimt", "Hidar", "Tahsas", "Tir", "Yekatit",
            "Megabit", "Miyazya", "Ginbot", "Sene", "Hamle", "Nehase", "Pagume"
        ).getOrElse(month - 1) { "Pagume" }
    }
}
