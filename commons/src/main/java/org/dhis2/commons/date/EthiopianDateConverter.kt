package org.dhis2.commons.date

import java.util.Calendar

object EthiopianDateConverter {
    // Ethiopian calendar constants
    private const val ETH_TO_GREG_OFFSET = 8
    private const val ETH_MONTH_OFFSET = 3
    private const val ETH_NEW_YEAR = 11 // Ethiopian New Year is September 11/12

    fun toGregorian(ethYear: Int, ethMonth: Int, ethDay: Int): Calendar {
        val gregDate = Calendar.getInstance().apply {
            // Convert Ethiopian date to Gregorian
            val gregYear = ethYear + ETH_TO_GREG_OFFSET
            val gregMonth = (ethMonth + ETH_MONTH_OFFSET) % 12
            val gregDay = ethDay

            set(Calendar.YEAR, gregYear)
            set(Calendar.MONTH, if (gregMonth == 0) Calendar.SEPTEMBER else gregMonth - 1)
            set(Calendar.DAY_OF_MONTH, gregDay)
        }
        return gregDate
    }

    fun toEthiopian(gregDate: Calendar): Triple<Int, Int, Int> {
        // Convert Gregorian date to Ethiopian
        val gregYear = gregDate.get(Calendar.YEAR)
        val ethYear = gregYear - ETH_TO_GREG_OFFSET

        val gregMonth = gregDate.get(Calendar.MONTH) + 1
        val ethMonth = (gregMonth - ETH_MONTH_OFFSET + 12) % 12
        val ethDay = gregDate.get(Calendar.DAY_OF_MONTH)

        return Triple(ethYear, if (ethMonth == 0) 12 else ethMonth, ethDay)
    }
}