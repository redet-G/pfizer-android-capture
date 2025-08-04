package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.ethcaledar

import com.ibm.icu.util.Calendar
import com.ibm.icu.util.EthiopicCalendar
import java.text.SimpleDateFormat
import java.util.*

fun toEthiopianDateString(gregorianDate: Date?): String {
    if (gregorianDate == null) return "N/A"

    // Convert Gregorian Date to EthiopianCalendar
    val ethiopianCalendar = EthiopicCalendar()
    ethiopianCalendar.time = gregorianDate

    val year = ethiopianCalendar.get(Calendar.YEAR)
    val month = ethiopianCalendar.get(Calendar.MONTH) + 1 // Months are 0-based
    val day = ethiopianCalendar.get(Calendar.DAY_OF_MONTH)

    // Format as "yyyy-MM-dd" or any format you want
    return String.format("%02d/%02d/%04d", day, month, year)
}
