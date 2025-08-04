package org.dhis2.usescases.datasets.datasetInitial.periods.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.dhis2.commons.R
import org.dhis2.commons.date.toUiStringResource
import org.dhis2.commons.dialogs.bottomsheet.bottomSheetInsets
import org.dhis2.commons.dialogs.bottomsheet.bottomSheetLowerPadding
import org.dhis2.customui.EthiopianDatePickerDialog
import org.dhis2.customui.EthiopianDateUtils
import org.dhis2.customui.EthiopianPeriodSelectorContent
import org.dhis2.usescases.datasets.datasetInitial.periods.DatasetPeriodViewModel
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar
import org.dhis2.customui.EthiopianDateConverter
import java.util.Date

class DataSetPeriodDialog(
    private val dataset: String,
    private val periodType: PeriodType,
    private val selectedDate: Date?,
    private val openFuturePeriods: Int,
) : BottomSheetDialogFragment() {

    lateinit var onDateSelectedListener: (Date, String) -> Unit

    private val viewModel by viewModel<DatasetPeriodViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    @OptIn(ExperimentalMaterial3Api::class)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DHIS2Theme {
                    // Get current Ethiopian year from today
                    val today = Date()
                    val ethToday = EthiopianDateConverter.gregorianToEthiopian(today)
                    val maxYear = ethToday.year

                    if (
                        periodType == PeriodType.Daily &&
                        !viewModel.verifyIfHasDataInputPeriods(dataset)
                    ) {
                        EthiopianDatePickerDialog(
                            initialDate = selectedDate ?: Date(),
                            maxYear = maxYear,
                            onDateSelected = { date, _ ->
                                val label = EthiopianDateUtils.formatEthiopianDate(date)

                                onDateSelectedListener(date, label)

                                dismiss()
                            },
                            onDismiss = { dismiss() }
                        )

                    } else {



                        val scrollState = rememberLazyListState()

                        BottomSheetShell(
                            uiState = BottomSheetShellUIState(
                                title = getString(periodType.toUiStringResource()),
                                showTopSectionDivider = true,
                                bottomPadding = bottomSheetLowerPadding(),
                            ),
                            onDismiss = { dismiss() },
                            windowInsets = { bottomSheetInsets() },
                            contentScrollState = scrollState,
                            content = {
                                EthiopianPeriodSelectorContent(
                                    periodType = periodType,
                                    dataset = dataset,
                                    selectedDate = selectedDate,
                                    openFuturePeriods = openFuturePeriods,
                                    scrollState = scrollState,
                                    onPeriodSelected = { date, label ->
                                        onDateSelectedListener(date, label)

                                        dismiss()
                                    },
                                    onDismiss = { dismiss() }
                                )
                            },
                        )
                    }
                }
            }
        }
    }

}

