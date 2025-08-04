package org.dhis2.form.ui.provider.inputfield

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.dhis2.form.R
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.ethcalendar.EthiopianDate
import org.dhis2.form.ui.ethcalendar.EthiopianDateConverter
import org.dhis2.form.ui.ethcalendar.EthiopianDatePickerDialog
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProvideInputDate(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    onNextClicked: () -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    var displayedDate by remember { mutableStateOf<EthiopianDate?>(null) }
    var displayText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Update displayed date from stored value
    LaunchedEffect(fieldUiModel.value) {
        try {
            fieldUiModel.value?.let { storedDate ->
                val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(storedDate)
                parsedDate?.let {
                    val ethDate = EthiopianDateConverter.gregorianToEthiopian(it)
                    displayedDate = ethDate
                    displayText = formatEthiopianDate(ethDate)
                    errorMessage = null
                } ?: run {
                    displayText = ""
                    errorMessage = "Invalid date format"
                }
            } ?: run {
                displayedDate = null
                displayText = ""
                errorMessage = null
            }
        } catch (e: Exception) {
            Log.e("ProvideInputDate", "Error parsing stored date", e)
            errorMessage = "Invalid date format"
            displayText = ""
        }
    }

    // Show Ethiopian Date Picker
    if (showPicker) {
        EthiopianDatePickerDialog(
            initialDate = displayedDate ?: EthiopianDateConverter.gregorianToEthiopian(Date()),
            onDateSelected = { ethDate, gregDate ->
                try {
                    val formattedGregorian = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(gregDate)
                    displayedDate = ethDate
                    displayText = formatEthiopianDate(ethDate)
                    errorMessage = null

                    intentHandler(
                        FormIntent.OnSave(
                            uid = fieldUiModel.uid,
                            value = formattedGregorian,
                            valueType = fieldUiModel.valueType,
                            allowFutureDates = fieldUiModel.allowFutureDates,
                        )
                    )
                } catch (e: Exception) {
                    errorMessage = "Failed to save date"
                    Log.e("ProvideInputDate", "Error saving date", e)
                } finally {
                    showPicker = false
                }
            },
            onDismiss = { showPicker = false }
        )
    }

    // Determine supporting text based on field state
    val supportingTextContent = when {
        errorMessage != null -> errorMessage!!
        fieldUiModel.error != null -> fieldUiModel.error!!
        fieldUiModel.warning != null -> fieldUiModel.warning!!
        fieldUiModel.value.isNullOrEmpty() -> fieldUiModel.description ?: ""
        else -> ""
    }

    // Styled TextField with proper supporting text handling
    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .semantics { contentDescription = displayText }
            .clickable { showPicker = true },
        readOnly = true,
        value = TextFieldValue(displayText, TextRange(displayText.length)),
        onValueChange = {},
        label = { Text(fieldUiModel.label ?: "") },
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar_v2),
                    contentDescription = "Select Date",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        isError = errorMessage != null || fieldUiModel.warning != null || fieldUiModel.error != null,
        supportingText = {
            if (supportingTextContent.isNotEmpty()) {
                Text(
                    text = supportingTextContent,
                    color = when {
                        errorMessage != null || fieldUiModel.error != null ->
                            MaterialTheme.colorScheme.error
                        fieldUiModel.warning != null ->
                            MaterialTheme.colorScheme.errorContainer
                        else ->
                            MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.error
        )
    )
}

private fun formatEthiopianDate(date: EthiopianDate): String {
    val day = date.day.toString().padStart(2, '0')
    val month = date.month.toString().padStart(2, '0')
    return "$day/$month/${date.year}"
}
const val DEFAULT_MIN_DATE = "1924-09-11"
const val DEFAULT_MAX_DATE = "2124-09-11"