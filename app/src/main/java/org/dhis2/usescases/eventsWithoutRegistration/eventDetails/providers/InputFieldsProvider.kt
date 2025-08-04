package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers

import androidx.compose.foundation.clickable
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.dhis2.R
import org.dhis2.commons.dialogs.bottomsheet.bottomSheetInsets
import org.dhis2.commons.dialogs.bottomsheet.bottomSheetLowerPadding
import org.dhis2.commons.extensions.inDateRange
import org.dhis2.commons.extensions.inOrgUnit
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.model.UiEventType
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.ui.ethcalendar.EthiopianDate
import org.dhis2.form.ui.ethcalendar.EthiopianDateConverter
import org.dhis2.form.ui.ethcalendar.EthiopianDatePickerDialog
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatComboUiModel
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventInputDateUiModel
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventOrgUnit
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.mobile.ui.designsystem.component.Coordinates
import org.hisp.dhis.mobile.ui.designsystem.component.DateTimeActionType
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownInputField
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.InputCoordinate
import org.hisp.dhis.mobile.ui.designsystem.component.InputDropDown
import org.hisp.dhis.mobile.ui.designsystem.component.InputOrgUnit
import org.hisp.dhis.mobile.ui.designsystem.component.InputPolygon
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTimeVisualTransformation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp





import org.dhis2.form.ui.ethcalendar.EthiopianDatePickerDialog


@Composable
fun ProvideInputDate(
    uiModel: EventInputDateUiModel,
    modifier: Modifier = Modifier,
) {
    if (!uiModel.showField) return

    val textSelection = TextRange(uiModel.eventDate.dateValue?.length ?: 0)

    var value by remember(uiModel.eventDate.dateValue) {
        mutableStateOf(
            TextFieldValue(
                uiModel.eventDate.dateValue?.let { storedValue ->
                    try {
                        val gregDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(storedValue)
                        val ethDate = EthiopianDateConverter.gregorianToEthiopian(gregDate)
                        "${ethDate.day.toString().padStart(2, '0')}/${ethDate.month.toString().padStart(2, '0')}/${ethDate.year}"
                    } catch (e: Exception) {
                        ""
                    }
                } ?: "",
                textSelection
            )
        )
    }

    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        val initialDate = uiModel.eventDate.dateValue?.let {
            try {
                val gregDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it)
                EthiopianDateConverter.gregorianToEthiopian(gregDate)
            } catch (e: Exception) {
                EthiopianDateConverter.gregorianToEthiopian(Date())
            }
        } ?: EthiopianDateConverter.gregorianToEthiopian(Date())

        EthiopianDatePickerDialog(
            initialDate = initialDate,
            onDateSelected = { ethDate, gregDate ->
                val displayText = "${ethDate.day.toString().padStart(2, '0')}/${ethDate.month.toString().padStart(2, '0')}/${ethDate.year}"

                val cal = Calendar.getInstance().apply { time = gregDate }
                val inputDateValues = InputDateValues(
                    day = cal.get(Calendar.DAY_OF_MONTH),
                    month = cal.get(Calendar.MONTH) + 1,
                    year = cal.get(Calendar.YEAR)
                )

                value = TextFieldValue(displayText)
                uiModel.onDateSelected?.invoke(inputDateValues)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }

    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        // Input field with fixed width or fill max width minus icon width
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = { },
            modifier = Modifier
                .weight(1f)   // fill available space except icon
                .clickable { showPicker = true },
            label = { androidx.compose.material3.Text(text = uiModel.eventDate.label ?: "Date") },
            readOnly = true,
            enabled = uiModel.detailsEnabled,
            singleLine = true,
        )

        // Spacing between input and icon
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(10.dp))


        // Icon outside the text field
        androidx.compose.material3.IconButton(onClick = { showPicker = true }) {
            androidx.compose.material3.Icon(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_calendar_v2),
                contentDescription = "Open Ethiopian Date Picker",
                tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
            )
        }
    }
}

data class InputDateValues(val day: Int, val month: Int, val year: Int)

class EthiopianDateTransformation : DateTimeVisualTransformation {
    override val maskLength: Int
        get() = 10 // Format: dd/MM/yyyy → 10 characters

    override fun filter(text: AnnotatedString): TransformedText {
        // No transformation; return input text as-is
        return TransformedText(text, OffsetMapping.Identity)
    }
}

@Composable
fun ProvideOrgUnit(
    orgUnit: EventOrgUnit,
    detailsEnabled: Boolean,
    onOrgUnitClick: () -> Unit,
    resources: ResourceManager,
    onClear: () -> Unit,
    required: Boolean = false,
    showField: Boolean = true,
) {
    if (showField) {
        val state = getInputState(detailsEnabled && orgUnit.enable && orgUnit.orgUnits.size > 1)

        var inputFieldValue by remember(orgUnit.selectedOrgUnit) {
            mutableStateOf(orgUnit.selectedOrgUnit?.displayName())
        }

        InputOrgUnit(
            title = resources.getString(R.string.org_unit),
            state = state,
            inputText = inputFieldValue ?: "",
            onValueChanged = {
                inputFieldValue = it
                if (it.isNullOrEmpty()) {
                    onClear()
                }
            },
            onOrgUnitActionCLicked = onOrgUnitClick,
            isRequiredField = required,
        )
    }
}

@Composable
fun ProvideCategorySelector(
    modifier: Modifier = Modifier,
    eventCatComboUiModel: EventCatComboUiModel,
) {
    var selectedItem by with(eventCatComboUiModel) {
        remember(this) {
            mutableStateOf(
                eventCatCombo.selectedCategoryOptions[category.uid]?.displayName()
                    ?: eventCatCombo.categoryOptions?.get(category.uid)?.displayName(),
            )
        }
    }

    val selectableOptions = eventCatComboUiModel.category.options
        .filter { option ->
            option.access().data().write()
        }.filter { option ->
            option.inDateRange(eventCatComboUiModel.currentDate)
        }.filter { option ->
            option.inOrgUnit(eventCatComboUiModel.selectedOrgUnit)
        }
    val dropdownItems = selectableOptions.map { DropdownItem(it.displayName() ?: it.code() ?: "") }

    if (selectableOptions.isNotEmpty()) {
        InputDropDown(
            modifier = modifier,
            windowInsets = { bottomSheetInsets() },
            bottomSheetLowerPadding = bottomSheetLowerPadding(),
            title = eventCatComboUiModel.category.name,
            state = getInputState(eventCatComboUiModel.detailsEnabled),
            selectedItem = DropdownItem(selectedItem ?: ""),
            onResetButtonClicked = {
                selectedItem = null
                eventCatComboUiModel.onClearCatCombo(eventCatComboUiModel.category)
            },
            onItemSelected = { _, newSelectedDropdownItem ->
                selectedItem = newSelectedDropdownItem.label
                eventCatComboUiModel.onOptionSelected(
                    selectableOptions.firstOrNull { it.displayName() == newSelectedDropdownItem.label }
                )
            },
            fetchItem = { index -> dropdownItems[index] },
            itemCount = dropdownItems.size,
            onSearchOption = { /*no-op*/ },
            loadOptions = { /*no-op*/ },
            useDropDown = dropdownItems.size < 15,
            isRequiredField = eventCatComboUiModel.required,
        )
    } else {
        ProvideEmptyCategorySelector(
            modifier = modifier,
            name = eventCatComboUiModel.category.name,
            option = eventCatComboUiModel.noOptionsText,
        )
    }
}

@Composable
fun ProvidePeriodSelector(
    modifier: Modifier = Modifier,
    uiModel: EventInputDateUiModel,
) {
    var selectedItem by with(uiModel) {
        remember(this) {
            mutableStateOf(
                uiModel.eventDate.dateValue,
            )
        }
    }
    val state = getInputState(uiModel.detailsEnabled)

    DropdownInputField(
        modifier = modifier,
        title = uiModel.eventDate.label ?: "",
        state = state,
        selectedItem = DropdownItem(selectedItem ?: ""),
        onResetButtonClicked = {
            selectedItem = null
            uiModel.onClear?.let { it() }
        },
        onDropdownIconClick = {
            uiModel.onDateClick?.invoke()
        },
        isRequiredField = uiModel.required,
        legendData = null,
        onFocusChanged = {},
        supportingTextData = null,
        focusRequester = remember { FocusRequester() },
        expanded = false,
    )
}

@Composable
fun ProvideEmptyCategorySelector(
    modifier: Modifier = Modifier,
    name: String,
    option: String,
) {
    var selectedItem by remember { mutableStateOf("") }

    InputDropDown(
        windowInsets = { bottomSheetInsets() },
        modifier = modifier,
        title = name,
        state = InputShellState.UNFOCUSED,
        selectedItem = DropdownItem(selectedItem),
        onResetButtonClicked = {
            selectedItem = ""
        },
        onItemSelected = { _, newSelectedDropdownItem ->
            selectedItem = newSelectedDropdownItem.label
        },
        fetchItem = { DropdownItem(option) },
        itemCount = 1,
        onSearchOption = { /*no-op*/ },
        loadOptions = { /*no-op*/ },
        isRequiredField = false,
    )
}

@Composable
fun ProvideCoordinates(
    coordinates: EventCoordinates,
    detailsEnabled: Boolean,
    resources: ResourceManager,
    showField: Boolean = true,
) {
    if (showField) {
        when (coordinates.model?.renderingType) {
            UiRenderType.POLYGON, UiRenderType.MULTI_POLYGON -> {
                InputPolygon(
                    title = resources.getString(R.string.polygon),
                    state = getInputState(detailsEnabled && coordinates.model.editable),
                    polygonAdded = !coordinates.model.value.isNullOrEmpty(),
                    onResetButtonClicked = { coordinates.model.onClear() },
                    onUpdateButtonClicked = {
                        coordinates.model.invokeUiEvent(UiEventType.REQUEST_LOCATION_BY_MAP)
                    },
                )
            }
            else -> {
                InputCoordinate(
                    title = resources.getString(R.string.coordinates),
                    state = getInputState(detailsEnabled && coordinates.model?.editable == true),
                    coordinates = mapGeometry(coordinates.model?.value, FeatureType.POINT),
                    latitudeText = resources.getString(R.string.latitude),
                    longitudeText = resources.getString(R.string.longitude),
                    addLocationBtnText = resources.getString(R.string.add_location),
                    onResetButtonClicked = {
                        coordinates.model?.onClear()
                    },
                    onUpdateButtonClicked = {
                        coordinates.model?.invokeUiEvent(UiEventType.REQUEST_LOCATION_BY_MAP)
                    },
                )
            }
        }
    }
}

private fun getInputState(enabled: Boolean) = if (enabled) {
    InputShellState.UNFOCUSED
} else {
    InputShellState.DISABLED
}

fun mapGeometry(value: String?, featureType: FeatureType): Coordinates? {
    return value?.let {
        val geometry = Geometry.builder()
            .coordinates(it)
            .type(featureType)
            .build()

        Coordinates(
            latitude = GeometryHelper.getPoint(geometry)[1],
            longitude = GeometryHelper.getPoint(geometry)[0],
        )
    }
}

fun willShowCalendar(periodType: PeriodType?): Boolean {
    return (periodType == null || periodType == PeriodType.Daily)
}

// Constants
private const val INPUT_EVENT_INITIAL_DATE = "INPUT_EVENT_INITIAL_DATE"
private const val EMPTY_CATEGORY_SELECTOR = "EMPTY_CATEGORY_SELECTOR"
private const val CATEGORY_SELECTOR = "CATEGORY_SELECTOR"
const val DEFAULT_MAX_DATE = "01011917" // 01/01/1917 EC
const val DEFAULT_MIN_DATE = "30132100" // 30/13/2100 EC
private const val ETHIOPIAN_MIN_YEAR = 1917
private const val ETHIOPIAN_MAX_YEAR = 2100