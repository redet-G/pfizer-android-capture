package org.dhis2.commons.dialogs.calendarpicker

import org.dhis2.commons.prefs.PreferenceProvider
import javax.inject.Inject

class CalendarPickerRepositoryImpl @Inject constructor(
    private val preferenceProvider: PreferenceProvider
) : CalendarPickerRepository {

    companion object {
        private const val PREF_PICKER_STYLE = "date_picker_style"
        private const val PREF_ETHIOPIAN_ENABLED = "ethiopian_enabled"
    }

    override fun isDatePickerStyle(): Boolean {
        return preferenceProvider.getBoolean(PREF_PICKER_STYLE, true)
    }

    override fun setPickerStyle(isDatePicker: Boolean) {
        preferenceProvider.setValue(PREF_PICKER_STYLE, isDatePicker)
    }

    override fun isEthiopianEnabled(): Boolean {
        return preferenceProvider.getBoolean(PREF_ETHIOPIAN_ENABLED, true)
    }

    override fun setEthiopianEnabled(enabled: Boolean) {
        preferenceProvider.setValue(PREF_ETHIOPIAN_ENABLED, enabled)
    }
}
