package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.ethcaledar



import com.ibm.icu.util.EthiopicCalendar
import java.util.Date
import java.util.Locale

object EthiopianDateFormatter {

    private val transliteratedMonths = arrayOf(
        "Meskerem", "Tikimt", "Hidar", "Tahsas", "Tir", "Yekatit",
        "Megabit", "Miazia", "Ginbot", "Sene", "Hamle", "Nehasse", "Pagumen"
    )

    /**
     * Format the given Gregorian [date] into Ethiopian date string as dd/MM/yyyy (numeric).
     */
    fun format(date: Date?): String {
        if (date == null) return ""

        val ethiopicCalendar = EthiopicCalendar(Locale.ENGLISH)
        ethiopicCalendar.time = date

        val year = ethiopicCalendar.get(EthiopicCalendar.YEAR)
        val month = ethiopicCalendar.get(EthiopicCalendar.MONTH) + 1 // months are zero-based
        val day = ethiopicCalendar.get(EthiopicCalendar.DAY_OF_MONTH)

        return "%02d/%02d/%04d".format(day, month, year)
    }

    /**
     * Format the given Gregorian [date] into Ethiopian date string with transliterated month name.
     * Example: "14 Meskerem 2015"
     */
    fun formatWithTransliteratedMonth(date: Date?): String {
        if (date == null) return ""

        val ethiopicCalendar = EthiopicCalendar(Locale.ENGLISH)
        ethiopicCalendar.time = date

        val year = ethiopicCalendar.get(EthiopicCalendar.YEAR)
        val month = ethiopicCalendar.get(EthiopicCalendar.MONTH) // zero-based
        val day = ethiopicCalendar.get(EthiopicCalendar.DAY_OF_MONTH)

        val monthName = transliteratedMonths.getOrNull(month) ?: "?"
        return "$day $monthName $year"
    }
}
