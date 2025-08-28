package org.dhis2.mobile.aggregates.ui.ethcalendar

import com.ibm.icu.util.Calendar
import com.ibm.icu.util.EthiopicCalendar
import java.text.SimpleDateFormat
import java.util.*

object EthiopianDateConverter {

    private const val UI_DATE_FORMAT = "dd/MM/yyyy"
    private const val STORAGE_DATE_FORMAT = "yyyy-MM-dd"

    // Convert Gregorian Date to EthiopianDate object
    fun gregorianToEthiopian(date: Date): EthiopianDate {
        val ethCal = EthiopicCalendar()
        ethCal.time = date
        val year = ethCal.get(Calendar.YEAR)
        val month = ethCal.get(Calendar.MONTH) + 1
        val day = ethCal.get(Calendar.DAY_OF_MONTH)
        return EthiopianDate(year, month, day)
    }

    // Convert EthiopianDate object to Gregorian Date
    fun ethiopianToGregorian(ethDate: EthiopianDate): Date {
        val ethCal = EthiopicCalendar()
        ethCal.set(Calendar.YEAR, ethDate.year)
        ethCal.set(Calendar.MONTH, ethDate.month - 1)
        ethCal.set(Calendar.DAY_OF_MONTH, ethDate.day)
        return ethCal.time
    }

    // Convert Gregorian Date string (yyyy-MM-dd) to Ethiopian UI string (dd/MM/yyyy)
    fun gregorianToEthiopianStringUIFormat(gregorianDateString: String?): String {
        if (gregorianDateString.isNullOrEmpty()) return ""
        return try {
            val gregDate = SimpleDateFormat(STORAGE_DATE_FORMAT, Locale.US).parse(gregorianDateString)
            if (gregDate != null) {
                val ethDate = gregorianToEthiopian(gregDate)
                ethDate.toString()
            } else ""
        } catch (e: Exception) {
            ""
        }
    }

    // Convert Ethiopian UI string (dd/MM/yyyy) to Gregorian storage string (yyyy-MM-dd)
    fun ethiopianUIFormatToGregorianStringStored(ethiopianUIString: String?): String? {
        if (ethiopianUIString.isNullOrEmpty() || ethiopianUIString.length != 10) return null
        return try {
            val day = ethiopianUIString.substring(0, 2).toInt()
            val month = ethiopianUIString.substring(3, 5).toInt()
            val year = ethiopianUIString.substring(6, 10).toInt()
            val ethDate = EthiopianDate(year, month, day)
            val gregDate = ethiopianToGregorian(ethDate)
            SimpleDateFormat(STORAGE_DATE_FORMAT, Locale.US).format(gregDate)
        } catch (e: Exception) {
            null
        }
    }

    // ✅ Get the current Ethiopian year
    fun currentEthiopianYear(): Int {
        val ethCal = EthiopicCalendar()
        ethCal.time = Date()
        return ethCal.get(Calendar.YEAR)
    }

    // ✅ Get today's Ethiopian date
    fun todayEthiopian(): EthiopianDate {
        val ethCal = EthiopicCalendar()
        ethCal.time = Date()
        return EthiopianDate(
            ethCal.get(Calendar.YEAR),
            ethCal.get(Calendar.MONTH) + 1,
            ethCal.get(Calendar.DAY_OF_MONTH)
        )
    }
}
