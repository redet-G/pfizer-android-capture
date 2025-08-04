//package org.hisp.dhis.mobile.ui.component
//
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Event
//import androidx.compose.material.icons.outlined.Cancel
//import androidx.compose.material3.Icon
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.focus.FocusRequester
//import androidx.compose.ui.platform.testTag
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.input.TextFieldValue
//import org.hisp.dhis.mobile.ui.designsystem.component.SquareIconButton
//import org.dhis2.form.ui.ethcalendar.EthiopianDateConverter
//import org.dhis2.form.ui.ethcalendar.EthiopianDatePickerDialog
//import org.dhis2.form.ui.ethcalendar.EthiopianDate
//import org.dhis2.form.model.FieldUiModel
//import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
//import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
//import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
//import org.dhis2.form.ui.intent.FormIntent
//import org.dhis2.mobile.aggregates.ui.provider.ResourceManager
//
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//@Composable
//fun InputAge(
//    state: InputAgeState,
//    onValueChanged: (AgeInputType?) -> Unit,
//    onNextClicked: (() -> Unit)? = null,
//    modifier: Modifier = Modifier,
//) {
//    val focusRequester = remember { FocusRequester() }
//    var showEthiopianPicker by remember { mutableStateOf(false) }
//
//    // Convert stored Ethiopian date to display format
//    val displayDate = remember(state.inputType) {
//        when (val type = state.inputType) {
//            is AgeInputType.DateOfBirth -> type.value.text.takeIf { it.isNotBlank() }?.let {
//                formatEthiopianDateForDisplay(it)
//            } ?: ""
//            else -> ""
//        }
//    }
//
//    InputShell(
//        modifier = modifier.testTag("INPUT_AGE").focusRequester(focusRequester),
//        title = state.uiData.title,
//        state = state.inputState,
//        isRequiredField = state.uiData.isRequired,
//        inputField = {
//            when (state.inputType) {
//                AgeInputType.None -> TextButtonSelector(
//                    firstOptionText = state.uiData.dateOfBirthLabel ?: stringResource(R.string.date_birth),
//                    onClickFirstOption = { onValueChanged(AgeInputType.DateOfBirth.EMPTY) },
//                    middleText = state.uiData.orLabel ?: stringResource(R.string.or),
//                    secondOptionText = state.uiData.ageLabel ?: stringResource(R.string.age),
//                    onClickSecondOption = { onValueChanged(AgeInputType.Age.EMPTY) },
//                    enabled = state.inputState != InputShellState.DISABLED
//                )
//
//                is AgeInputType.Age -> AgeInputField(
//                    ageType = state.inputType,
//                    onValueChange = { newValue ->
//                        if (newValue.text.length <= 3) {
//                            onValueChanged(state.inputType.copy(value = newValue))
//                        }
//                    },
//                    enabled = state.inputState != InputShellState.DISABLED,
//                    state = state.inputState
//                )
//
//                is AgeInputType.DateOfBirth -> DateInputField(
//                    displayValue = displayDate,
//                    onValueChange = { /* Handle manual input if needed */ },
//                    enabled = state.inputState != InputShellState.DISABLED,
//                    state = state.inputState
//                )
//            }
//        },
//        primaryButton = {
//            if (state.inputType != AgeInputType.None && state.inputState != InputShellState.DISABLED) {
//                IconButton(
//                    icon = { Icon(Icons.Outlined.Cancel, contentDescription = "Reset") },
//                    onClick = { onValueChanged(AgeInputType.None) }
//                )
//            }
//        },
//        secondaryButton = {
//            if (state.inputType is AgeInputType.DateOfBirth) {
//                SquareIconButton(
//                    icon = { Icon(Icons.Filled.Event, contentDescription = "Open Calendar") },
//                    onClick = { showEthiopianPicker = true },
//                    enabled = state.inputState != InputShellState.DISABLED
//                )
//            }
//        },
//        supportingText = {
//            state.supportingText?.forEach { textData ->
//                SupportingText(text = textData.text, state = textData.state)
//            }
//        },
//        legend = {
//            if (state.inputType is AgeInputType.Age) {
//                TimeUnitSelector(
//                    selectedUnit = state.inputType.unit,
//                    onUnitSelected = { newUnit ->
//                        onValueChanged(state.inputType.copy(unit = newUnit))
//                    },
//                    enabled = state.inputState != InputShellState.DISABLED
//                )
//            }
//            state.legendData?.let { legend ->
//                Legend(text = legend.text, state = legend.state)
//            }
//        }
//    )
//
//    if (showEthiopianPicker) {
//        EthiopianDatePickerDialog(
//            initialDate = (state.inputType as? AgeInputType.DateOfBirth)?.value?.text?.takeIf { it.isNotBlank() }?.let {
//                try {
//                    EthiopianDateConverter.fromGregorian(
//                        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it)
//                    )
//                } catch (e: Exception) { null }
//            },
//            onDateSelected = { ethDate ->
//                val gregDate = EthiopianDateConverter.toGregorian(ethDate)
//                val storedValue = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(gregDate)
//                onValueChanged(AgeInputType.DateOfBirth(TextFieldValue(storedValue)))
//                showEthiopianPicker = false
//            },
//            onDismiss = { showEthiopianPicker = false }
//        )
//    }
//}
//
//// Supporting classes and functions
//sealed interface AgeInputType {
//    data object None : AgeInputType
//    data class DateOfBirth(val value: TextFieldValue) : AgeInputType {
//        companion object { val EMPTY = DateOfBirth(TextFieldValue()) }
//    }
//    data class Age(val value: TextFieldValue, val unit: TimeUnit = TimeUnit.YEARS) : AgeInputType {
//        companion object { val EMPTY = Age(TextFieldValue()) }
//    }
//}
//
//enum class TimeUnit { YEARS, MONTHS, DAYS }
//
//data class InputAgeState(
//    val uiData: InputAgeData,
//    val inputType: AgeInputType = AgeInputType.None,
//    val inputState: InputShellState = InputShellState.UNFOCUSED,
//    val supportingText: List<SupportingTextData>? = null,
//    val legendData: LegendData? = null
//)
//
//data class InputAgeData(
//    val title: String,
//    val inputStyle: InputStyle = InputStyle.DataInputStyle(),
//    val isRequired: Boolean = false,
//    val dateOfBirthLabel: String? = null,
//    val orLabel: String? = null,
//    val ageLabel: String? = null,
//    val cancelText: String? = null,
//    val acceptText: String? = null,
//    val selectableDates: SelectableDates? = null
//)
//
//enum class InputShellState { UNFOCUSED, FOCUSED, DISABLED, ERROR }
//
//internal fun formatEthiopianDateForDisplay(storedValue: String): String {
//    return try {
//        val gregDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(storedValue)
//        val ethDate = EthiopianDateConverter.fromGregorian(gregDate)
//        "${ethDate.day}/${ethDate.month}/${ethDate.year}"
//    } catch (e: Exception) {
//        ""
//    }
//}
//
//internal fun formatEthiopianDateForStorage(displayValue: String): String {
//    return try {
//        val parts = displayValue.split("/")
//        val ethDate = EthiopianDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
//        val gregDate = EthiopianDateConverter.toGregorian(ethDate)
//        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(gregDate)
//    } catch (e: Exception) {
//        ""
//    }
//}