package org.dhis2.form.ui.ethcalendar

import android.util.Log
import com.ibm.icu.util.Calendar
import com.ibm.icu.util.EthiopicCalendar
import java.text.SimpleDateFormat
import java.util.*

object EthiopianDateConverter {

    fun currentEthiopianYear(): Int {
        return gregorianToEthiopian(Date()).year
    }

    // Converts a Gregorian Date object to an EthiopianDate object
    fun gregorianToEthiopian(date: Date): EthiopianDate {
        val ethCal = EthiopicCalendar()
        ethCal.time = date
        val year = ethCal.get(Calendar.YEAR)
        val month = ethCal.get(Calendar.MONTH) + 1 // ICU is 0-based
        val day = ethCal.get(Calendar.DAY_OF_MONTH)

        Log.d("EthiopianDateConverter", "Converted Gregorian $date -> Ethiopian $day/$month/$year")

        return EthiopianDate(year, month, day)
    }

    // Converts an EthiopianDate object to a Gregorian Date object
    fun ethiopianToGregorian(ethDate: EthiopianDate): Date {
        val ethCal = EthiopicCalendar()
        ethCal.set(Calendar.YEAR, ethDate.year)
        ethCal.set(Calendar.MONTH, ethDate.month -1) // ICU is 0-based
        ethCal.set(Calendar.DAY_OF_MONTH, ethDate.day)
        val gregDate = ethCal.time

        Log.d("EthiopianDateConverter", "Converted Ethiopian $ethDate -> Gregorian $gregDate")

        return gregDate
    }

    // --- New Methods for String Conversions ---

    // Converts a Gregorian date string (YYYY-MM-DD) to an EthiopianDate object
    fun gregorianToEthiopianDate(gregorianDateString: String): EthiopianDate? {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val gregorianDate = formatter.parse(gregorianDateString)
            if (gregorianDate != null) {
                gregorianToEthiopian(gregorianDate)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("EthiopianDateConverter", "Error parsing Gregorian date string: $gregorianDateString", e)
            null
        }
    }

    // Converts an EthiopianDate object to a Gregorian date string (YYYY-MM-DD)
    fun ethiopianToGregorianStringStored(ethiopianDate: EthiopianDate): String {
        val gregDate = ethiopianToGregorian(ethiopianDate)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(gregDate)
    }

    // Converts a Gregorian date string (YYYY-MM-DD) to Ethiopian UI format (DDMMYYYY)
    fun gregorianToEthiopianStringUIFormat(gregorianDateString: String?): String {
        if (gregorianDateString.isNullOrEmpty()) return ""
        return try {
            val gregorianDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(gregorianDateString)
            if (gregorianDate != null) {
                val ethiopianDate = gregorianToEthiopian(gregorianDate)
                String.format("%02d%02d%04d", ethiopianDate.day, ethiopianDate.month, ethiopianDate.year) // DDMMYYYY
            } else {
                "" // Return empty string for unparseable input
            }
        } catch (e: Exception) {
            Log.e("EthiopianDateConverter", "Error converting Gregorian string to Ethiopian UI format: $gregorianDateString", e)
            "" // Return empty string on error
        }
    }

    // Converts Ethiopian UI format (DDMMYYYY) string to Gregorian stored format (YYYY-MM-DD)
    fun ethiopianUIFormatToGregorianStringStored(ethiopianUIString: String?): String? {
        if (ethiopianUIString.isNullOrEmpty() || ethiopianUIString.length != ETHIOPIAN_DATE_UI_FORMAT_LENGTH) {
            return null // Invalid format, return null for partial or incorrect input
        }

        return try {
            val day = ethiopianUIString.substring(0, 2).toInt()
            val month = ethiopianUIString.substring(2, 4).toInt()
            val year = ethiopianUIString.substring(4, 8).toInt()
            val ethiopianDate = EthiopianDate(year, month, day)

            ethiopianToGregorianStringStored(ethiopianDate)
        } catch (e: Exception) {
            Log.e("EthiopianDateConverter", "Error converting Ethiopian UI string to Gregorian stored format: $ethiopianUIString", e)
            null // Return null on error
        }
    }

    // Constants for string parsing/formatting length
    const val ETHIOPIAN_DATE_UI_FORMAT_LENGTH = 8 // DDMMYYYY
}