package org.dhis2.customui

import java.util.*

data class EthiopianDate(val year: Int, val month: Int, val day: Int) : Comparable<EthiopianDate> {
    override fun compareTo(other: EthiopianDate): Int {
        return when {
            year != other.year -> year - other.year
            month != other.month -> month - other.month
            else -> day - other.day
        }
    }

    fun plus(days: Int): EthiopianDate {
        var newYear = year
        var newMonth = month
        var newDay = day + days

        while (true) {
            val maxDays = if (newMonth == 13) {
                if (newYear % 4 == 3) 6 else 5
            } else {
                30
            }

            if (newDay <= maxDays) break

            newDay -= maxDays
            newMonth++
            if (newMonth > 13) {
                newMonth = 1
                newYear++
            }
        }

        return EthiopianDate(newYear, newMonth, newDay)
    }
}

object EthiopianDateConverter {
    private const val ETHIOPIAN_EPOCH = 1724220.0 // JD for Meskerem 1, Year 1

    /**
     * Converts Gregorian date to Ethiopian date
     */
    fun gregorianToEthiopian(date: Date): EthiopianDate {
        val cal = GregorianCalendar().apply { time = date }
        val jd = gregorianToJD(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        return jdToEthiopian(jd)
    }

    /**
     * Converts Ethiopian date to Gregorian date
     */
    fun ethiopianToGregorian(year: Int, month: Int, day: Int): Date {
        validateEthiopianDate(year, month, day)
        val jd = ethiopianToJD(year, month, day)
        val (gYear, gMonth, gDay) = jdToGregorian(jd)
        return GregorianCalendar(gYear, gMonth - 1, gDay).time
    }

    private fun validateEthiopianDate(year: Int, month: Int, day: Int) {
        require(month in 1..13) { "Month must be between 1-13" }
        require(day > 0) { "Day must be positive" }

        val maxDays = if (month == 13) {
            if (year % 4 == 3) 6 else 5
        } else {
            30
        }
        require(day <= maxDays) { "Month $month has maximum $maxDays days" }
    }

    /**
     * Converts Gregorian date to Julian Day
     */
    private fun gregorianToJD(year: Int, month: Int, day: Int): Double {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045.0
    }

    /**
     * Converts Julian Day to Gregorian date
     */
    private fun jdToGregorian(jd: Double): Triple<Int, Int, Int> {
        val z = jd + 0.5
        val w = ((z - 1867216.25) / 36524.25).toInt()
        val x = (w / 4).toInt()
        val a = z + 1 + w - x
        val b = a + 1524
        val c = ((b - 122.1) / 365.25).toInt()
        val d = (365.25 * c).toInt()
        val e = ((b - d) / 30.6001).toInt()
        val f = (30.6001 * e).toInt()
        val day = (b - d - f).toInt()
        val month = if (e < 14) e - 1 else e - 13
        val year = if (month > 2) c - 4716 else c - 4715
        return Triple(year, month, day)
    }

    /**
     * Converts Julian Day to Ethiopian date
     */
    private fun jdToEthiopian(jd: Double): EthiopianDate {
        val days = jd - ETHIOPIAN_EPOCH
        val fourYearCycle = (days / 1461).toInt()
        val remainingDays = (days % 1461).toInt()

        val yearInCycle = remainingDays / 365
        val year = fourYearCycle * 4 + yearInCycle + 1
        val dayOfYear = remainingDays % 365

        val month = (dayOfYear / 30) + 1
        val day = (dayOfYear % 30) + 1

        return EthiopianDate(year, month, day)
    }

    /**
     * Converts Ethiopian date to Julian Day
     */
    private fun ethiopianToJD(year: Int, month: Int, day: Int): Double {
        val n = (month - 1) * 30 + (day - 1)
        return ETHIOPIAN_EPOCH + 365 * (year - 1) + (year / 4) + n
    }

    /**
     * Converts Ethiopian day of year to EthiopianDate
     */
    fun dayOfYearToEthiopianDate(year: Int, dayOfYear: Int): EthiopianDate {
        var remainingDays = dayOfYear
        var month = 1

        while (true) {
            val maxDays = if (month == 13) {
                if (year % 4 == 3) 6 else 5
            } else {
                30
            }

            if (remainingDays <= maxDays) {
                return EthiopianDate(year, month, remainingDays)
            }

            remainingDays -= maxDays
            month++
            if (month > 13) {
                return EthiopianDate(year, 13, maxDays)
            }
        }
    }
}