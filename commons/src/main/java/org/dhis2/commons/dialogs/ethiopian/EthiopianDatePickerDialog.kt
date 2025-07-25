package org.dhis2.commons.dialogs.ethiopian

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.NumberPicker
import org.dhis2.commons.R
import org.dhis2.commons.date.EthiopianDate

class EthiopianDatePickerDialog(
    context: Context,
    private val initialYear: Int = 2015,
    private val initialMonth: Int = 1,
    private val initialDay: Int = 1,
    private val listener: (EthiopianDate) -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_ethiopian_date_picker)

        // Ethiopian months (Meskerem, Tikimt, etc.)
        val ethiopianMonths = context.resources.getStringArray(R.array.ethiopian_months)

        val yearPicker = findViewById<NumberPicker>(R.id.eth_year_picker)
        val monthPicker = findViewById<NumberPicker>(R.id.eth_month_picker)
        val dayPicker = findViewById<NumberPicker>(R.id.eth_day_picker)

        // Year range (adjust as needed)
        yearPicker.minValue = 2000
        yearPicker.maxValue = 2092
        yearPicker.value = initialYear

        // Month picker
        monthPicker.minValue = 1
        monthPicker.maxValue = 13
        monthPicker.displayedValues = ethiopianMonths
        monthPicker.value = initialMonth

        // Day picker
        dayPicker.minValue = 1
        dayPicker.maxValue = if (initialMonth == 13) 5 else 30
        dayPicker.value = initialDay

        monthPicker.setOnValueChangedListener { _, _, newMonth ->
            dayPicker.maxValue = if (newMonth == 13) 5 else 30
        }

        findViewById<Button>(R.id.btn_confirm).setOnClickListener {
            listener(
                EthiopianDate(
                    yearPicker.value,
                    monthPicker.value,
                    dayPicker.value
                )
            )
            dismiss()
        }
    }
}
