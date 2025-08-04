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
import java.util.*

// Your own EthiopianDate data class should be defined somewhere else in the project:
// data class EthiopianDate(val year: Int, val month: Int, val day: Int)

@Composable
fun EthiopianDatePickerDialog(
    initialDate: Date = Date(),
    maxYear: Int = 2030,
    minYear: Int = 1976,
    onDateSelected: (Date, String) -> Unit,
    onDismiss: () -> Unit,
) {
    val ethiopianMonthNames = listOf(
        "Hidar", "Tahsas", "Tir", "Yekatit",
        "Megabit", "Miazia", "Ginbot", "Sene", "Hamle", "Nehase", "Pagume", "Meskerem", "Tikimt",
    )

    // Convert initial Gregorian date to Ethiopian using your own converter
    val initialEthDate = remember(initialDate) {
        val ethDate = EthiopianDateConverter.gregorianToEthiopian(initialDate)
        Log.d("EthiopianDatePicker", "Initial Gregorian: $initialDate -> Ethiopian: $ethDate")
        ethDate
    }

    // Current Ethiopian date for default maxYear logic
    val currentEthDate = remember {
        EthiopianDateConverter.gregorianToEthiopian(Date())
    }

    val startYear = if (currentEthDate.year > maxYear) maxYear else currentEthDate.year

    var selectedYear by remember { mutableStateOf(initialEthDate.year.coerceIn(minYear, maxYear)) }
    var selectedMonth by remember { mutableStateOf(initialEthDate.month) }
    var selectedDay by remember { mutableStateOf(initialEthDate.day) }

    val maxDays = remember(selectedMonth, selectedYear) {
        if (selectedMonth == 13) {
            if (isEthiopianLeapYear(selectedYear)) 6 else 5
        } else {
            30
        }
    }

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
                        selectedDay = 1
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
                val gregorianDate = EthiopianDateConverter.ethiopianToGregorian(selectedYear, selectedMonth, selectedDay)
                val ethiopianDateLabel = "${ethiopianMonthNames[selectedMonth - 1]} $selectedDay, $selectedYear"

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

// Leap year check for Ethiopian calendar based on your Julian logic
fun isEthiopianLeapYear(year: Int): Boolean {
    // Ethiopian leap year occurs every 4 years when year % 4 == 3
    return year % 4 == 3
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
        update = { picker -> picker.value = selectedMonth },
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
                setOnValueChangedListener { _, _, newVal -> onYearSelected(years[newVal]) }
                wrapSelectorWheel = false
            }
        },
        update = { picker -> picker.value = years.indexOf(selectedYear) },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    )
}
