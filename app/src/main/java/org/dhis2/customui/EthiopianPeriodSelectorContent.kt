package org.dhis2.customui

import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date

@Composable
fun EthiopianPeriodSelectorContent(
    periodType: PeriodType,
    dataset: String,
    selectedDate: Date?,
    openFuturePeriods: Int,
    scrollState: LazyListState,
    onPeriodSelected: (Date, String) -> Unit,
    onDismiss: () -> Unit
) {
    // ✅ Log incoming parameters
    Log.d(
        "EthiopianPeriodSelector",
        "Rendering EthiopianPeriodSelectorContent | PeriodType: $periodType | Dataset: $dataset | SelectedDate: $selectedDate | OpenFuturePeriods: $openFuturePeriods"
    )

    // Compute Ethiopian periods
    val periodItems = EthiopianDateUtils.getEthiopianPeriods(
        periodType = periodType,
        selectedDate = selectedDate,
        openFuturePeriods = openFuturePeriods
    )

    // ✅ Log computed Ethiopian periods
    Log.d(
        "EthiopianPeriodSelector",
        "Computed Ethiopian Periods: ${periodItems.joinToString { it.label + " (" + it.startDate + ")" }}"
    )

    LazyColumn(state = scrollState) {
        items(periodItems.size) { index ->
            val period = periodItems[index]

            // ✅ Log each period before rendering
            Log.d(
                "EthiopianPeriodSelector",
                "Displaying Period: ${period.label} | StartDate: ${period.startDate}"
            )

            EthiopianPeriodItem(
                label = period.label,
                onClick = {
                    // ✅ Log the selected period
                    Log.d(
                        "EthiopianPeriodSelector",
                        "User selected: ${period.label} | StartDate: ${period.startDate}"
                    )
                    onPeriodSelected(period.startDate, period.label)
                }
            )
        }
    }
}
