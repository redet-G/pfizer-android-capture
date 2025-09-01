package org.dhis2.mobile.aggregates.ui.ethcalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.util.*

@Composable
fun EthiopianDatePickerDialog(
    initialDate: EthiopianDate? = null,
    onDateSelected: (EthiopianDate, Date) -> Unit,
    onDismiss: () -> Unit
) {
    val currentEthDate = initialDate ?: EthiopianDateConverter.gregorianToEthiopian(Date())
    val currentYear = EthiopianDateConverter.currentEthiopianYear()
    val yearList = (currentYear + 10 downTo currentYear - 100).toList() // descending list

    var selectedYear by remember { mutableStateOf(currentEthDate.year) }
    var selectedMonth by remember { mutableStateOf(currentEthDate.month) }
    var selectedDay by remember { mutableStateOf(currentEthDate.day) }

    var expandedYearDropdown by remember { mutableStateOf(false) }

    val monthNames = listOf(
        "Meskerem", "Tikimt", "Hidar", "Tahsas",
        "Tir", "Yekatit", "Megabit", "Miazia",
        "Ginbot", "Sene", "Hamle", "Nehase", "Pagume"
    )

    val isLeapYear = ((selectedYear + 1) % 4 == 0)
    val maxDays = if (selectedMonth == 13) {
        if (isLeapYear) 6 else 5
    } else 30

    val firstDayGregorian = EthiopianDateConverter.ethiopianToGregorian(EthiopianDate(selectedYear, selectedMonth, 1))
    val offset = Calendar.getInstance().apply { time = firstDayGregorian }.get(Calendar.DAY_OF_WEEK).let {
        (it - Calendar.SUNDAY + 7) % 7
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Top Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (selectedMonth == 1) {
                            selectedMonth = 13
                            selectedYear--
                        } else {
                            selectedMonth--
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = monthNames[selectedMonth - 1],
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Box {
                            Text(
                                text = selectedYear.toString(),
                                modifier = Modifier
                                    .clickable { expandedYearDropdown = true }
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(6.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            DropdownMenu(
                                expanded = expandedYearDropdown,
                                onDismissRequest = { expandedYearDropdown = false },
                                modifier = Modifier.heightIn(max = 300.dp)
                            ) {
                                yearList.forEach { year ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = year.toString(),
                                                fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            selectedYear = year
                                            expandedYearDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    IconButton(onClick = {
                        if (selectedMonth == 13) {
                            selectedMonth = 1
                            selectedYear++
                        } else {
                            selectedMonth++
                        }
                    }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Day of week headers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(40.dp).padding(4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Days grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Center
                ) {
                    items(offset) {
                        Box(modifier = Modifier.size(40.dp).padding(4.dp))
                    }

                    items(maxDays) { index ->
                        val day = index + 1
                        val isSelected = day == selectedDay
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(4.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedDay = day },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                color = if (isSelected) Color.White else Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val ethDate = EthiopianDate(selectedYear, selectedMonth, selectedDay)
                        val gregDate = EthiopianDateConverter.ethiopianToGregorian(ethDate)
                        onDateSelected(ethDate, gregDate)
                    }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}


/**
 * Converter utility
 */
