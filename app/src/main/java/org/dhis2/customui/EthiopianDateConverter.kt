package org.dhis2.customui

import com.ibm.icu.util.EthiopicCalendar
import java.util.Date
import java.util.GregorianCalendar

/**
 * Ethiopian Date model
 */
data class EthiopianDate(val year: Int, val month: Int, val day: Int) : Comparable<EthiopianDate> {

    override fun compareTo(other: EthiopianDate): Int {
        return when {
            year != other.year -> year - other.year
            month != other.month -> month - other.month
            else -> day - other.day
        }
    }

    /**
     * Add days to current EthiopianDate using ICU4J
     */
    fun plus(days: Int): EthiopianDate {
        val calendar = EthiopicCalendar().apply {
            set(EthiopicCalendar.EXTENDED_YEAR, year)
            set(EthiopicCalendar.MONTH, month - 1) // ICU months are 0-based
            set(EthiopicCalendar.DATE, day)
            add(EthiopicCalendar.DATE, days)
        }
        return EthiopianDate(
            calendar.get(EthiopicCalendar.EXTENDED_YEAR),
            calendar.get(EthiopicCalendar.MONTH) + 1,
            calendar.get(EthiopicCalendar.DATE)
        )
    }
}

/**
 * Ethiopian Calendar Converter using ICU4J
 */
object EthiopianDateConverter {

    /**
     * Converts Gregorian Date -> Ethiopian Date
     */
    fun gregorianToEthiopian(date: Date): EthiopianDate {
        val ethiopicCalendar = EthiopicCalendar()
        ethiopicCalendar.time = date
        return EthiopianDate(
            ethiopicCalendar.get(EthiopicCalendar.EXTENDED_YEAR),
            ethiopicCalendar.get(EthiopicCalendar.MONTH) + 1,
            ethiopicCalendar.get(EthiopicCalendar.DATE)
        )
    }

    /**
     * Converts Ethiopian Date -> Gregorian Date
     */
    fun ethiopianToGregorian(year: Int, month: Int, day: Int): Date {
        val ethiopicCalendar = EthiopicCalendar().apply {
            set(EthiopicCalendar.EXTENDED_YEAR, year)
            set(EthiopicCalendar.MONTH, month - 1)
            set(EthiopicCalendar.DATE, day)
        }
        val gregorianCalendar = GregorianCalendar().apply {
            time = ethiopicCalendar.time
        }
        return gregorianCalendar.time
    }

    /**
     * Add years to Ethiopian date
     */
    fun addYears(year: Int, month: Int, day: Int, yearsToAdd: Int): EthiopianDate {
        val ethiopicCalendar = EthiopicCalendar().apply {
            set(EthiopicCalendar.EXTENDED_YEAR, year)
            set(EthiopicCalendar.MONTH, month - 1)
            set(EthiopicCalendar.DATE, day)
            add(EthiopicCalendar.YEAR, yearsToAdd)
        }
        return EthiopianDate(
            ethiopicCalendar.get(EthiopicCalendar.EXTENDED_YEAR),
            ethiopicCalendar.get(EthiopicCalendar.MONTH) + 1,
            ethiopicCalendar.get(EthiopicCalendar.DATE)
        )
    }

    /**
     * Convert Day of Year to EthiopianDate
     */
    fun dayOfYearToEthiopianDate(year: Int, dayOfYear: Int): EthiopianDate {
        val calendar = EthiopicCalendar().apply {
            set(EthiopicCalendar.EXTENDED_YEAR, year)
            set(EthiopicCalendar.MONTH, 0) // Meskerem
            set(EthiopicCalendar.DATE, 1)
            add(EthiopicCalendar.DATE, dayOfYear - 1)
        }
        return EthiopianDate(
            calendar.get(EthiopicCalendar.EXTENDED_YEAR),
            calendar.get(EthiopicCalendar.MONTH) + 1,
            calendar.get(EthiopicCalendar.DATE)
        )
    }
}
