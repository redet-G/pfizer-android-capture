package org.dhis2.commons.date



data class EthiopianDate(
    val year: Int,
    val month: Int,
    val day: Int
) {
    override fun toString(): String {
        return "$day/$month/$year"
    }
}