package org.dhis2.mobile.aggregates.ui.ethcalendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Ethiopian Date Input component
 * Displays and stores Ethiopian calendar date in "yyyy-MM-dd" format.
 */
@Composable
fun InputEthiopianDate(
    title: String,
    value: String?, // Ethiopian date "yyyy-MM-dd"
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    enabled: Boolean = true,
    supportingText: String? = null,
    visualTransformation: ((String) -> String)? = null,
    onValueChanged: (String?) -> Unit, // returns Ethiopian date as string in "yyyy-MM-dd"
) {
    var showPicker by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Show Ethiopian date as text
    val ethDisplayText = value ?: ""

    // Always update text value when ethDisplayText changes
    val textValue = remember(ethDisplayText) {
        TextFieldValue(ethDisplayText, TextRange(ethDisplayText.length))
    }

    val customTransformation = visualTransformation?.let { vt ->
        VisualTransformation { text ->
            val transformed = vt(text.text)
            androidx.compose.ui.text.input.TransformedText(
                AnnotatedString(transformed),
                androidx.compose.ui.text.input.OffsetMapping.Identity
            )
        }
    } ?: VisualTransformation.None

    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .clickable(enabled = enabled) { if (enabled) showPicker = true },
        value = textValue,
        onValueChange = {}, // read-only, picker only
        readOnly = true,
        enabled = enabled,
        label = { Text(title + if (isRequired) " *" else "") },
        trailingIcon = {
            Row {
                if (ethDisplayText.isNotEmpty()) {
                    IconButton(onClick = {
                        onValueChanged(null)
                    }) {
                        Icon(Icons.Outlined.Cancel, contentDescription = "Clear")
                    }
                }
                IconButton(onClick = { showPicker = true }) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = "Pick Ethiopian Date")
                }
            }
        },
        isError = supportingText != null,
        supportingText = {
            if (!supportingText.isNullOrBlank()) Text(supportingText, color = MaterialTheme.colorScheme.error)
        },
        singleLine = true,
        visualTransformation = customTransformation
    )

    if (showPicker) {
        EthiopianDatePickerDialog(
            initialDate = ethDisplayText.takeIf { it.length == 10 }?.let {
                val parts = it.split("-")
                EthiopianDate(
                    year = parts[0].toIntOrNull() ?: 2000,
                    month = parts[1].toIntOrNull() ?: 1,
                    day = parts[2].toIntOrNull() ?: 1
                )
            },
            onDateSelected = { ethDate, _ ->
                // Format Ethiopian date as "yyyy-MM-dd"
                val formattedEthiopian = "%04d-%02d-%02d".format(
                    ethDate.year, ethDate.month, ethDate.day
                )
                onValueChanged(formattedEthiopian)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}