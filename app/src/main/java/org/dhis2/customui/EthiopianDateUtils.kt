package org.dhis2.customui
import kotlin.math.min
import org.hisp.dhis.android.core.period.PeriodType
import java.util.*



data class EthiopianPeriod(val startDate: Date, val label: String)

object EthiopianDateUtils {
    private val ethiopianMonthNames = listOf(
        "Meskerem", "Tikimt", "Hidar", "Tahsas",
        "Tir", "Yekatit", "Megabit", "Miazia",
        "Ginbot", "Sene", "Hamle", "Nehase", "Pagume"
    )

    fun formatEthiopianDate(date: Date): String {
        val ethioDate = EthiopianDateConverter.gregorianToEthiopian(date)
        return "${ethiopianMonthNames[ethioDate.month - 1]} ${ethioDate.day}, ${ethioDate.year}"

    }

    fun getEthiopianPeriods(
        periodType: PeriodType,
        selectedDate: Date? = null,
        openFuturePeriods: Int = 0,
        minYear: Int = 1990,
        displayFromYear: Int? = null
    ): List<EthiopianPeriod> {
        val baseDate = selectedDate ?: Date()
        val baseEthioDate = EthiopianDateConverter.gregorianToEthiopian(baseDate)
        val currentEthYear = EthiopianDateConverter.gregorianToEthiopian(Date()).year

        val periods = when (periodType) {
            PeriodType.Daily -> generateDailyPeriods(baseEthioDate, currentEthYear, minYear, openFuturePeriods)
            PeriodType.Weekly -> generateWeeklyPeriods(baseEthioDate, currentEthYear, minYear, openFuturePeriods)
            PeriodType.WeeklySaturday -> generateCustomWeeklyPeriods(baseEthioDate, currentEthYear, minYear, openFuturePeriods, Calendar.SATURDAY)
            PeriodType.WeeklySunday -> generateCustomWeeklyPeriods(baseEthioDate, currentEthYear, minYear, openFuturePeriods, Calendar.SUNDAY)
            PeriodType.WeeklyWednesday -> generateCustomWeeklyPeriods(baseEthioDate, currentEthYear, minYear, openFuturePeriods, Calendar.WEDNESDAY)
            PeriodType.WeeklyThursday -> generateCustomWeeklyPeriods(baseEthioDate, currentEthYear, minYear, openFuturePeriods, Calendar.THURSDAY)
            PeriodType.BiWeekly -> generateBiWeeklyPeriods(baseEthioDate, currentEthYear, minYear, openFuturePeriods)
            PeriodType.Monthly -> generateMonthlyPeriods(baseEthioDate, currentEthYear, minYear, openFuturePeriods)
            PeriodType.BiMonthly -> generateBiMonthlyPeriods(baseEthioDate, currentEthYear, minYear, openFuturePeriods)
            PeriodType.Quarterly -> generateQuarterlyPeriods(baseEthioDate, currentEthYear, minYear, openFuturePeriods)
            PeriodType.SixMonthly -> generateSixMonthlyPeriods(baseEthioDate, currentEthYear, minYear, openFuturePeriods)
            PeriodType.SixMonthlyApril -> generateSixMonthlyAprilPeriods(baseEthioDate, currentEthYear, minYear, openFuturePeriods)
            PeriodType.SixMonthlyNov -> generateSixMonthlyNovPeriods(baseEthioDate, currentEthYear, minYear, openFuturePeriods)
            PeriodType.Yearly -> generateYearlyPeriods(currentEthYear, minYear, openFuturePeriods)
            PeriodType.FinancialApril -> generateFinancialAprilPeriods(baseEthioDate, currentEthYear, minYear, openFuturePeriods)
            PeriodType.FinancialJuly -> generateFinancialJulyPeriods(baseEthioDate, currentEthYear, minYear, openFuturePeriods)
            PeriodType.FinancialOct -> generateFinancialOctPeriods(baseEthioDate, currentEthYear, minYear, openFuturePeriods)
            PeriodType.FinancialNov -> generateFinancialNovPeriods(baseEthioDate, currentEthYear, minYear, openFuturePeriods)
            else -> generateMonthlyPeriods(baseEthioDate, currentEthYear, minYear, openFuturePeriods)
        }

        return if (displayFromYear != null) {
            periods.filter { period ->
                val yearStr = period.label.trim().split(" ").lastOrNull()
                val year = yearStr?.toIntOrNull() ?: minYear
                year >= displayFromYear
            }.reversed()
        } else {
            periods.reversed()
        }
    }

    private fun generateDailyPeriods(
        baseDate: EthiopianDate,
        currentYear: Int,
        minYear: Int,
        futureDays: Int
    ): List<EthiopianPeriod> {
        val periods = mutableListOf<EthiopianPeriod>()
        var current = EthiopianDate(minYear, 1, 1)
        val endDate = EthiopianDate(baseDate.year, baseDate.month, baseDate.day).plus(futureDays)

        while (current <= endDate) {
            val startDate = EthiopianDateConverter.ethiopianToGregorian(current.year, current.month, current.day)
            periods.add(EthiopianPeriod(
                startDate,
                "${current.day} ${ethiopianMonthNames[current.month-1]} ${current.year}"
            ))
            current = current.plus(1)
        }

        return periods
    }

    private fun generateWeeklyPeriods(
        baseDate: EthiopianDate,
        currentYear: Int,
        minYear: Int,
        futureWeeks: Int
    ): List<EthiopianPeriod> {
        val periods = mutableListOf<EthiopianPeriod>()
        val weeksPassed = ((baseDate.month - 1) * 4) + ((baseDate.day - 1) / 7)
        val endWeek = weeksPassed + futureWeeks

        for (year in minYear..currentYear) {
            val maxWeeks = if (year == currentYear) min(endWeek, 52) else 52

            for (week in 1..maxWeeks) {
                val startDate = getWeekStartDate(year, week)
                periods.add(EthiopianPeriod(startDate, "Week $week $year"))
            }
        }

        return periods
    }

    private fun generateCustomWeeklyPeriods(
        baseDate: EthiopianDate,
        currentYear: Int,
        minYear: Int,
        futureWeeks: Int,
        startDay: Int
    ): List<EthiopianPeriod> {
        val periods = mutableListOf<EthiopianPeriod>()
        val weeksPassed = ((baseDate.month - 1) * 4) + ((baseDate.day - 1) / 7)
        val endWeek = weeksPassed + futureWeeks

        for (year in minYear..currentYear) {
            val maxWeeks = if (year == currentYear) min(endWeek, 52) else 52

            for (week in 1..maxWeeks) {
                val startDate = getCustomWeekStartDate(year, week, startDay)
                periods.add(EthiopianPeriod(startDate, "Week $week $year"))
            }
        }

        return periods
    }

    private fun generateBiWeeklyPeriods(
        baseDate: EthiopianDate,
        currentYear: Int,
        minYear: Int,
        futurePeriods: Int
    ): List<EthiopianPeriod> {
        val periods = mutableListOf<EthiopianPeriod>()
        val totalBiWeeksInYear = 26 // 52 weeks / 2 = 26 biweeks
        val currentBiWeek = calculateBiWeekOfYear(baseDate)

        for (year in minYear..currentYear) {
            val maxBiWeeks = if (year == currentYear) min(currentBiWeek + futurePeriods, totalBiWeeksInYear) else totalBiWeeksInYear

            for (biWeek in 1..maxBiWeeks) {
                val startDay = (biWeek - 1) * 14 + 1
                val endDay = min(biWeek * 14, if (year % 4 == 3) 366 else 365)

                val startEthDate =  EthiopianDateConverter.dayOfYearToEthiopianDate(year, startDay)
                val endEthDate =  EthiopianDateConverter.dayOfYearToEthiopianDate(year, endDay)

                val label = buildBiWeeklyLabel(startEthDate, endEthDate, year)

                val startDate = EthiopianDateConverter.ethiopianToGregorian(
                    startEthDate.year,
                    startEthDate.month,
                    startEthDate.day
                )
                periods.add(EthiopianPeriod(startDate, label))
            }
        }

        return periods
    }

    private fun calculateBiWeekOfYear(date: EthiopianDate): Int {
        val dayOfYear = calculateDayOfYear(date)
        return (dayOfYear - 1) / 14 + 1
    }

    private fun calculateDayOfYear(date: EthiopianDate): Int {
        var dayOfYear = 0
        for (month in 1 until date.month) {
            dayOfYear += if (month == 13) {
                if (date.year % 4 == 3) 6 else 5
            } else {
                30
            }
        }
        dayOfYear += date.day
        return dayOfYear
    }

    private fun buildBiWeeklyLabel(
        startDate: EthiopianDate,
        endDate: EthiopianDate,
        year: Int
    ): String {
        return if (startDate.month == endDate.month) {
            "${ethiopianMonthNames[startDate.month - 1]} ${startDate.day}-${endDate.day}, $year"
        } else {
            "${ethiopianMonthNames[startDate.month - 1]} ${startDate.day}-" +
                    "${ethiopianMonthNames[endDate.month - 1]} ${endDate.day}, $year"
        }
    }
    private fun generateMonthlyPeriods(
        baseDate: EthiopianDate,
        currentYear: Int,
        minYear: Int,
        futureMonths: Int
    ): List<EthiopianPeriod> {
        val periods = mutableListOf<EthiopianPeriod>()
        val endMonth = if (baseDate.year == currentYear) min(baseDate.month + futureMonths, 13) else 13

        for (year in minYear..currentYear) {
            val maxMonth = if (year == currentYear) endMonth else 13

            for (month in 1..maxMonth) {
                val startDate = EthiopianDateConverter.ethiopianToGregorian(year, month, 1)
                periods.add(EthiopianPeriod(startDate, "${ethiopianMonthNames[month-1]} $year"))
            }
        }

        return periods
    }

    private fun generateBiMonthlyPeriods(
        baseDate: EthiopianDate,
        currentYear: Int,
        minYear: Int,
        futurePeriods: Int
    ): List<EthiopianPeriod> {
        val periods = mutableListOf<EthiopianPeriod>()
        val endPeriod = if (baseDate.year == currentYear) {
            min(((baseDate.month - 1) / 2) + 1 + futurePeriods, 6)
        } else 6

        for (year in minYear..currentYear) {
            val maxPeriod = if (year == currentYear) endPeriod else 6

            for (period in 1..maxPeriod) {
                val startMonth = (period - 1) * 2 + 1
                val startDate = EthiopianDateConverter.ethiopianToGregorian(year, startMonth, 1)
                val endMonth = min(startMonth + 1, 13)
                periods.add(EthiopianPeriod(
                    startDate,
                    "${ethiopianMonthNames[startMonth-1]}-${ethiopianMonthNames[endMonth-1]} $year"
                ))
            }
        }

        return periods
    }

    private fun generateQuarterlyPeriods(
        baseDate: EthiopianDate,
        currentYear: Int,
        minYear: Int,
        futureQuarters: Int
    ): List<EthiopianPeriod> {
        val periods = mutableListOf<EthiopianPeriod>()
        val currentQuarter = ((baseDate.month - 1) / 3) + 1
        val endQuarter = if (baseDate.year == currentYear) min(currentQuarter + futureQuarters, 4) else 4

        for (year in minYear..currentYear) {
            val maxQuarter = if (year == currentYear) endQuarter else 4

            for (quarter in 1..maxQuarter) {
                val startMonth = (quarter - 1) * 3 + 1
                val startDate = EthiopianDateConverter.ethiopianToGregorian(year, startMonth, 1)
                periods.add(EthiopianPeriod(startDate, "Q$quarter $year"))
            }
        }

        return periods
    }

    private fun generateSixMonthlyPeriods(
        baseDate: EthiopianDate,
        currentYear: Int,
        minYear: Int,
        futurePeriods: Int
    ): List<EthiopianPeriod> {
        val periods = mutableListOf<EthiopianPeriod>()
        val currentSemester = ((baseDate.month - 1) / 6) + 1
        val endSemester = if (baseDate.year == currentYear) min(currentSemester + futurePeriods, 2) else 2

        for (year in minYear..currentYear) {
            val maxSemester = if (year == currentYear) endSemester else 2

            for (semester in 1..maxSemester) {
                val startMonth = (semester - 1) * 6 + 1
                val startDate = EthiopianDateConverter.ethiopianToGregorian(year, startMonth, 1)
                periods.add(EthiopianPeriod(startDate, "Semester $semester $year"))
            }
        }

        return periods
    }

    private fun generateSixMonthlyAprilPeriods(
        baseDate: EthiopianDate,
        currentYear: Int,
        minYear: Int,
        futurePeriods: Int
    ): List<EthiopianPeriod> {
        val periods = mutableListOf<EthiopianPeriod>()
        val aprilMonth = 8 // Miazia (April)
        val currentPeriod = if (baseDate.month < aprilMonth) 1 else 2
        val endPeriod = if (baseDate.year == currentYear) min(currentPeriod + futurePeriods, 2) else 2

        for (year in minYear..currentYear) {
            val maxPeriod = if (year == currentYear) endPeriod else 2

            for (period in 1..maxPeriod) {
                val startMonth = if (period == 1) aprilMonth else 1
                val startYear = if (period == 1) year else year + 1
                if (startYear > currentYear && period == 2) continue

                val startDate = EthiopianDateConverter.ethiopianToGregorian(
                    if (period == 1) year else year,
                    startMonth, 1
                )
                periods.add(EthiopianPeriod(startDate, "H2 $year/${year+1}"))
            }
        }

        return periods
    }

    private fun generateSixMonthlyNovPeriods(
        baseDate: EthiopianDate,
        currentYear: Int,
        minYear: Int,
        futurePeriods: Int
    ): List<EthiopianPeriod> {
        val periods = mutableListOf<EthiopianPeriod>()
        val novemberMonth = 3 // Hidar (November)
        val currentPeriod = if (baseDate.month < novemberMonth) 1 else 2
        val endPeriod = if (baseDate.year == currentYear) min(currentPeriod + futurePeriods, 2) else 2

        for (year in minYear..currentYear) {
            val maxPeriod = if (year == currentYear) endPeriod else 2

            for (period in 1..maxPeriod) {
                val startMonth = if (period == 1) novemberMonth else 1
                val startYear = if (period == 1) year else year + 1
                if (startYear > currentYear && period == 2) continue

                val startDate = EthiopianDateConverter.ethiopianToGregorian(
                    if (period == 1) year else year,
                    startMonth, 1
                )
                periods.add(EthiopianPeriod(startDate, "H2 $year/${year+1}"))
            }
        }

        return periods
    }


    private fun generateYearlyPeriods(
        currentYear: Int,
        minYear: Int,
        futureYears: Int
    ): List<EthiopianPeriod> {
        validateYearlyParameters(currentYear, minYear, futureYears)

        return (minYear..currentYear + futureYears).map { year ->
            // Start date is always Meskerem 1 (month 1, day 1)
            val startDate = EthiopianDateConverter.ethiopianToGregorian(year, 1, 1)

            // End date is Pagume 5/6 (month 13)
            val daysInPagume = if (year % 4 == 3) 6 else 5
            val endDate = EthiopianDateConverter.ethiopianToGregorian(year, 13, daysInPagume)

            // Log the start date and year for debugging
            android.util.Log.d("EthiopianYearPeriod", "Year: $year, Start Date (Gregorian): $startDate")

            EthiopianPeriod(
                startDate = startDate,
                label = year.toString()
            )
        }
    }


    private fun validateYearlyParameters(
        currentYear: Int,
        minYear: Int,
        futureYears: Int
    ) {
        require(minYear > 0) { "Minimum year must be positive" }
        require(currentYear >= minYear) { "Current year cannot be before minimum year" }
        require(futureYears >= 0) { "Future years must be non-negative" }
    }

    private fun generateFinancialAprilPeriods(
        baseDate: EthiopianDate,
        currentYear: Int,
        minYear: Int,
        futurePeriods: Int
    ): List<EthiopianPeriod> {
        return generateFinancialPeriods(baseDate, currentYear, minYear, futurePeriods, 8) // Miazia (April)
    }

    private fun generateFinancialJulyPeriods(
        baseDate: EthiopianDate,
        currentYear: Int,
        minYear: Int,
        futurePeriods: Int
    ): List<EthiopianPeriod> {
        return generateFinancialPeriods(baseDate, currentYear, minYear, futurePeriods, 11) // Nehase (July)
    }

    private fun generateFinancialOctPeriods(
        baseDate: EthiopianDate,
        currentYear: Int,
        minYear: Int,
        futurePeriods: Int
    ): List<EthiopianPeriod> {
        return generateFinancialPeriods(baseDate, currentYear, minYear, futurePeriods, 2) // Tikimt (October)
    }

    private fun generateFinancialNovPeriods(
        baseDate: EthiopianDate,
        currentYear: Int,
        minYear: Int,
        futurePeriods: Int
    ): List<EthiopianPeriod> {
        return generateFinancialPeriods(baseDate, currentYear, minYear, futurePeriods, 3) // Hidar (November)
    }

    private fun generateFinancialPeriods(
        baseDate: EthiopianDate,
        currentYear: Int,
        minYear: Int,
        futurePeriods: Int,
        startMonth: Int
    ): List<EthiopianPeriod> {
        val periods = mutableListOf<EthiopianPeriod>()
        val currentPeriodYear = if (baseDate.month < startMonth) baseDate.year - 1 else baseDate.year
        val endYear = currentPeriodYear + futurePeriods



        for (year in minYear..endYear) {
            if (year > currentYear && year > currentPeriodYear + futurePeriods) continue

            val endMonth = if (startMonth == 1) 13 else startMonth - 1

            val startDate = EthiopianDateConverter.ethiopianToGregorian(year, startMonth, 1)
            val endDate = EthiopianDateConverter.ethiopianToGregorian(year + 1, endMonth, 30)

            val startMonthName = ethiopianMonthNames[startMonth - 1]
            val endMonthName = ethiopianMonthNames[endMonth - 1]

            val label = "$startMonthName ${year} – $endMonthName ${year + 1}"

            periods.add(EthiopianPeriod(startDate, label))
        }

        return periods
    }


    private fun getWeekStartDate(year: Int, week: Int): Date {
        val dayOfYear = (week - 1) * 7 + 1
        return getDateFromDayOfYear(year, dayOfYear)
    }

    private fun getCustomWeekStartDate(year: Int, week: Int, startDay: Int): Date {
        // For simplicity, using same as regular weeks
        // In production, this would need adjustment based on startDay
        return getWeekStartDate(year, week)
    }

    private fun getDateFromDayOfYear(year: Int, dayOfYear: Int): Date {
        var remainingDays = dayOfYear
        var month = 1

        while (remainingDays > 0) {
            val daysInMonth = if (month == 13) {
                if (year % 4 == 3) 6 else 5
            } else {
                30
            }

            if (remainingDays > daysInMonth) {
                remainingDays -= daysInMonth
                month++
                if (month > 13) {
                    month = 1
                    // Should increment year in a complete implementation
                }
            } else {
                return EthiopianDateConverter.ethiopianToGregorian(year, month, remainingDays)
            }
        }

        return EthiopianDateConverter.ethiopianToGregorian(year, 1, 1)
    }

    fun generateCompleteEthiopianDates(fromYear: Int = 1990, toYear: Int = 2017): List<String> {
        val dates = mutableListOf<String>()
        for (year in fromYear..toYear) {
            for (month in 1..13) {
                val maxDays = if (month == 13) {
                    if (year % 4 == 3) 6 else 5
                } else 30
                for (day in 1..maxDays) {
                    dates.add("$day ${ethiopianMonthNames[month - 1]} $year")
                }
            }
        }
        return dates.reversed()
    }
}