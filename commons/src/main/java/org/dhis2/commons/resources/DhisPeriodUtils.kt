package org.dhis2.commons.resources

import org.apache.commons.text.WordUtils
import org.dhis2.commons.periods.data.EthiopianDateConverter
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.period.PeriodType
import java.util.*
import java.util.regex.Pattern

class DhisPeriodUtils(
    d2: D2,
    private val defaultPeriodLabel: String,
    private val defaultWeeklyLabel: String,
    private val defaultBiWeeklyLabel: String,
) {

    private val periodHelper = d2.periodModule().periodHelper()

    private val ethiopianMonths = listOf(
        "Meskerem", "Tikimt", "Hidar", "Tahsas",
        "Tir", "Yekatit", "Megabit", "Miazia",
        "Ginbot", "Sene", "Hamle", "Nehase", "Pagume"
    )

    fun getPeriodUIString(periodType: PeriodType?, date: Date, locale: Locale): String {
        var periodString = defaultPeriodLabel
        val period = periodHelper.blockingGetPeriodForPeriodTypeAndDate(periodType ?: PeriodType.Daily, date)

        // Convert start and end dates to Ethiopian
        val startEthio = EthiopianDateConverter.gregorianToEthiopian(period.startDate()!!)
        val endEthio = EthiopianDateConverter.gregorianToEthiopian(period.endDate()!!)

        val formattedDate: String = when (periodType) {
            PeriodType.Weekly,
            PeriodType.WeeklyWednesday,
            PeriodType.WeeklyThursday,
            PeriodType.WeeklySaturday,
            PeriodType.WeeklySunday -> {
                periodString = defaultWeeklyLabel
                periodString.format(
                    weekOfTheYear(periodType!!, period.periodId()!!),
                    "${ethiopianMonths[startEthio.month - 1]} ${startEthio.day}, ${startEthio.year}",
                    "${ethiopianMonths[endEthio.month - 1]} ${endEthio.day}, ${endEthio.year}",
                )
            }

            PeriodType.BiWeekly -> {
                periodString = defaultBiWeeklyLabel
                "${ethiopianMonths[startEthio.month - 1]} ${startEthio.day} - " +
                        "${ethiopianMonths[endEthio.month - 1]} ${endEthio.day}, ${endEthio.year}"
            }

            PeriodType.Monthly -> "${ethiopianMonths[startEthio.month - 1]} ${startEthio.year}"

            PeriodType.BiMonthly,
            PeriodType.Quarterly,
            PeriodType.QuarterlyNov,
            PeriodType.SixMonthly,
            PeriodType.SixMonthlyApril,
            PeriodType.FinancialApril,
            PeriodType.FinancialJuly,
            PeriodType.FinancialOct -> {
                "${ethiopianMonths[startEthio.month ]} ${startEthio.year} - " +
                        "${ethiopianMonths[endEthio.month - 1]} ${endEthio.year}"
            }

            PeriodType.Yearly -> startEthio.year.toString()

            else -> "${startEthio.day} ${ethiopianMonths[startEthio.month - 1]}, ${startEthio.year}"
        }

        return WordUtils.capitalize(formattedDate)
    }

    private fun weekOfTheYear(periodType: PeriodType, periodId: String): Int {
        val pattern = Pattern.compile(periodType.pattern)
        val matcher = pattern.matcher(periodId)
        return if (matcher.find()) matcher.group(2)?.toInt() ?: 0 else 0
    }
}
