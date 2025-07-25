import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.text.SimpleDateFormat
import java.util.Locale

object EthiopianCalendarUtils {

    // Ethiopian months
    val ETHIOPIAN_MONTHS = arrayOf(
        "Meskerem", "Tikimt", "Hidar", "Tahsas", "Tir", "Yekatit",
        "Megabit", "Miyazya", "Ginbot", "Sene", "Hamle", "Nehase", "Pagume"
    )

    /**
     * Converts Gregorian Date to Ethiopian date triple (year, monthIndex, day)
     */
    fun gregorianToEthiopian(date: Date): Triple<Int, Int, Int> {
        val cal = GregorianCalendar()
        cal.time = date

        val gregorianYear = cal.get(Calendar.YEAR)
        val gregorianMonth = cal.get(Calendar.MONTH) + 1 // January = 1
        val gregorianDay = cal.get(Calendar.DAY_OF_MONTH)

        var ethYear = gregorianYear - 8
        var ethMonth: Int
        var ethDay: Int

        // Calculate Ethiopian date based on the approximate algorithm
        // Ethiopian new year falls on Sept 11 Gregorian (Sept 12 in Gregorian leap years)
        val newYearDay = if (isGregorianLeapYear(gregorianYear)) 12 else 11

        if (gregorianMonth < 9 || (gregorianMonth == 9 && gregorianDay < newYearDay)) {
            ethYear -= 1
        }

        val gregorianNewYearDate = GregorianCalendar(gregorianYear, 8, newYearDay) // Sept (8 because 0-based)
        val diff = ((date.time - gregorianNewYearDate.time.time) / (1000 * 60 * 60 * 24)).toInt()

        if (diff >= 0) {
            ethMonth = diff / 30 + 1
            ethDay = diff % 30 + 1
        } else {
            val prevYearNewYearDate = GregorianCalendar(gregorianYear - 1, 8, if (isGregorianLeapYear(gregorianYear - 1)) 12 else 11)
            val prevDiff = ((date.time - prevYearNewYearDate.time.time) / (1000 * 60 * 60 * 24)).toInt()
            ethMonth = (prevDiff / 30) + 1
            ethDay = (prevDiff % 30) + 1
        }

        // Pagume is 13th month with 5 or 6 days, just handle overflow
        if (ethMonth > 13) {
            ethMonth = 13
        }

        return Triple(ethYear, ethMonth, ethDay)
    }

    private fun isGregorianLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    /**
     * Format Ethiopian date as "dd/MM/yyyy" or localized strings
     */
    fun formatEthiopianDate(year: Int, month: Int, day: Int, locale: Locale): String {
        // For English locale, use Meskerem etc.
        val monthName = if (month in 1..13) ETHIOPIAN_MONTHS[month - 1] else "Unknown"

        return when (locale.language) {
            "en" -> "%02d %s %d".format(day, monthName, year)
            else -> "%02d/%02d/%d".format(day, month, year)  // fallback numeric format
        }
    }
}
