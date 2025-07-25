package org.dhis2.customui

import android.util.Log
import android.widget.NumberPicker
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import com.ibm.icu.util.EthiopicCalendar
import java.util.*

@Composable
fun EthiopianDatePickerDialog(
    initialDate: Date = Date(),
    maxYear: Int = 2030,
    minYear: Int = 1990,
    onDateSelected: (Date, String) -> Unit,
    onDismiss: () -> Unit,
) {
    val ethiopianMonthNames = listOf(
        "Meskerem", "Tikimt", "Hidar", "Tahsas", "Tir", "Yekatit",
        "Megabit", "Miazia", "Ginbot", "Sene", "Hamle", "Nehase", "Pagume"
    )

    // ✅ Convert initial Gregorian date → Ethiopian using ICU4J
    val initialEthDate = remember(initialDate) {
        val ethDate = gregorianToEthiopian(initialDate)
        Log.d("EthiopianDatePicker", "Initial Gregorian: $initialDate -> Ethiopian: $ethDate")
        ethDate
    }

    // ✅ Current Ethiopian date for default maxYear logic
    val currentEthDate = remember {
        gregorianToEthiopian(Date())
    }

    // Determine starting year (current or maxYear if current is beyond maxYear)
    val startYear = if (currentEthDate.year > maxYear) maxYear else currentEthDate.year

    // State management
    var selectedYear by remember { mutableStateOf(initialEthDate.year.coerceIn(minYear, maxYear)) }
    var selectedMonth by remember { mutableStateOf(initialEthDate.month) }
    var selectedDay by remember { mutableStateOf(initialEthDate.day) }

    // Calculate max days for current month/year
    val maxDays = remember(selectedMonth, selectedYear) {
        if (selectedMonth == 13) {
            if (isEthiopianLeapYear(selectedYear)) 6 else 5
        } else {
            30
        }
    }

    // Ensure selected day is valid when month/year changes
    LaunchedEffect(maxDays) {
        if (selectedDay > maxDays) {
            selectedDay = maxDays
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Ethiopian Date") },
        text = {
            Column {
                Text("Month", style = MaterialTheme.typography.labelLarge)
                EthiopianMonthPicker(
                    selectedMonth = selectedMonth,
                    monthNames = ethiopianMonthNames,
                    onMonthSelected = { month ->
                        selectedMonth = month
                        selectedDay = 1 // Reset to first day when month changes
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("Day", style = MaterialTheme.typography.labelLarge)
                EthiopianDayPicker(
                    selectedDay = selectedDay,
                    maxDays = maxDays,
                    onDaySelected = { day -> selectedDay = day }
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("Year", style = MaterialTheme.typography.labelLarge)
                EthiopianYearPicker(
                    selectedYear = selectedYear,
                    minYear = minYear,
                    maxYear = maxYear,
                    onYearSelected = { year -> selectedYear = year }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val gregorianDate = ethiopianToGregorian(selectedYear, selectedMonth, selectedDay)
                val ethiopianDateLabel = "${ethiopianMonthNames[selectedMonth - 1]} $selectedDay, $selectedYear"

                // ✅ Log the final conversion
                Log.d(
                    "EthiopianDatePicker",
                    "Selected Ethiopian: $selectedYear-$selectedMonth-$selectedDay -> Gregorian: $gregorianDate"
                )

                onDateSelected(gregorianDate, ethiopianDateLabel)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}

// ✅ Helper functions using ICU4J EthiopicCalendar

fun gregorianToEthiopian(date: Date): EthiopianDate {
    val cal = EthiopicCalendar()
    cal.time = date
    return EthiopianDate(
        cal.get(EthiopicCalendar.EXTENDED_YEAR),
        cal.get(EthiopicCalendar.MONTH) + 1, // 0-based months
        cal.get(EthiopicCalendar.DATE)
    )
}

fun ethiopianToGregorian(year: Int, month: Int, day: Int): Date {
    val cal = EthiopicCalendar().apply {
        set(EthiopicCalendar.EXTENDED_YEAR, year)
        set(EthiopicCalendar.MONTH, month - 1)
        set(EthiopicCalendar.DATE, day)
    }
    return cal.time
}

fun isEthiopianLeapYear(year: Int): Boolean {
    val cal = EthiopicCalendar().apply {
        set(EthiopicCalendar.EXTENDED_YEAR, year)
        set(EthiopicCalendar.MONTH, 12) // Pagume (13th month, zero-based index = 12)
    }
    return cal.getActualMaximum(EthiopicCalendar.DATE) == 6
}



// =======================
// NumberPicker Composables
// =======================

@Composable
private fun EthiopianMonthPicker(
    selectedMonth: Int,
    monthNames: List<String>,
    onMonthSelected: (Int) -> Unit
) {
    AndroidView(
        factory = { context ->
            NumberPicker(context).apply {
                minValue = 1
                maxValue = monthNames.size
                displayedValues = monthNames.toTypedArray()
                value = selectedMonth
                setOnValueChangedListener { _, _, newVal -> onMonthSelected(newVal) }
                wrapSelectorWheel = false
            }
        },
        update = { picker ->
            picker.value = selectedMonth
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    )
}

@Composable
private fun EthiopianDayPicker(
    selectedDay: Int,
    maxDays: Int,
    onDaySelected: (Int) -> Unit
) {
    AndroidView(
        factory = { context ->
            NumberPicker(context).apply {
                minValue = 1
                maxValue = maxDays
                value = selectedDay
                setOnValueChangedListener { _, _, newVal -> onDaySelected(newVal) }
                wrapSelectorWheel = false
            }
        },
        update = { picker ->
            picker.minValue = 1
            picker.maxValue = maxDays
            picker.value = selectedDay
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    )
}

@Composable
private fun EthiopianYearPicker(
    selectedYear: Int,
    minYear: Int,
    maxYear: Int,
    onYearSelected: (Int) -> Unit
) {
    val years = remember(minYear, maxYear) {
        (minYear..maxYear).toList().reversed()
    }

    AndroidView(
        factory = { context ->
            NumberPicker(context).apply {
                minValue = 0
                maxValue = years.size - 1
                displayedValues = years.map { it.toString() }.toTypedArray()
                value = years.indexOf(selectedYear)
                setOnValueChangedListener { _, _, newVal ->
                    onYearSelected(years[newVal])
                }
                wrapSelectorWheel = false
            }
        },
        update = { picker ->
            picker.value = years.indexOf(selectedYear)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    )
}
