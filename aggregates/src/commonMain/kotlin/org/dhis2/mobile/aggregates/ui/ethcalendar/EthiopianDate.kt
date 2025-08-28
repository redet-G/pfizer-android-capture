package org.dhis2.mobile.aggregates.ui.ethcalendar

data class EthiopianDate(val year: Int, val month: Int, val day: Int) {
    override fun toString(): String {
        return "%02d/%02d/%04d".format(day, month, year) // dd/MM/yyyy
    }
}
