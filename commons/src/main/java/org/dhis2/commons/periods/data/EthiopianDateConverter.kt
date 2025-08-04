package org.dhis2.commons.periods.data

import com.ibm.icu.util.Calendar
import com.ibm.icu.util.EthiopicCalendar
import java.text.SimpleDateFormat
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
                if (isLeapYear(newYear)) 6 else 5
            } else 30

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

    private fun isLeapYear(year: Int): Boolean {
        return (year + 1) % 4 == 0
    }
}

object EthiopianDateConverter {

    // Gregorian → Ethiopian
    fun gregorianToEthiopian(date: Date): EthiopianDate {
        val ethCal = EthiopicCalendar()
        ethCal.time = date
        val year = ethCal.get(Calendar.YEAR)
        val month = ethCal.get(Calendar.MONTH) + 1 // ICU is zero-based
        val day = ethCal.get(Calendar.DAY_OF_MONTH)
        return EthiopianDate(year, month, day)
    }

    // Ethiopian → Gregorian
    fun ethiopianToGregorian(date: EthiopianDate): Date {
        val ethCal = EthiopicCalendar()
        ethCal.set(Calendar.ERA, EthiopicCalendar.ERA)
        ethCal.set(Calendar.YEAR, date.year)
        ethCal.set(Calendar.MONTH, date.month - 1)
        ethCal.set(Calendar.DAY_OF_MONTH, date.day)
        return ethCal.time
    }

    fun isEthiopianLeapYear(year: Int): Boolean {
        return (year + 1) % 4 == 0
    }

    fun currentEthiopianYear(): Int {
        return gregorianToEthiopian(Date()).year
    }

    fun gregorianToEthiopianDate(gregorianDateString: String): EthiopianDate? {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = formatter.parse(gregorianDateString)
            date?.let { gregorianToEthiopian(it) }
        } catch (e: Exception) {
            null
        }
    }

    fun ethiopianToGregorianStringStored(ethiopianDate: EthiopianDate): String {
        val gregDate = ethiopianToGregorian(ethiopianDate)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(gregDate)
    }

    fun gregorianToEthiopianStringUIFormat(gregorianDateString: String?): String {
        if (gregorianDateString.isNullOrBlank()) return ""
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(gregorianDateString)
            if (date != null) {
                val eth = gregorianToEthiopian(date)
                "%02d%02d%04d".format(eth.day, eth.month, eth.year)
            } else ""
        } catch (e: Exception) {
            ""
        }
    }

    fun ethiopianUIFormatToGregorianStringStored(ethiopianUIString: String?): String? {
        if (ethiopianUIString.isNullOrEmpty() || ethiopianUIString.length != 8) return null
        return try {
            val day = ethiopianUIString.substring(0, 2).toInt()
            val month = ethiopianUIString.substring(2, 4).toInt()
            val year = ethiopianUIString.substring(4, 8).toInt()
            ethiopianToGregorianStringStored(EthiopianDate(year, month, day))
        } catch (e: Exception) {
            null
        }
    }
}
