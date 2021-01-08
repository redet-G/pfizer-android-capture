package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.ViewGroup
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.formatters.DateLabelFormatter

const val X_AXIS_MAX_PADDING = 10f
const val X_AXIS_MAX_PADDING_WITH_VALUE = 4f
const val X_AXIS_MIN_PADDING = -5f

class GraphToBarChart {
    fun map(context: Context, graph: Graph): BarChart {
        val barData = GraphToBarData().map(graph)
        return BarChart(context).apply {
            description.isEnabled = false
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            xAxis.apply {
                enableGridDashedLine(
                    DEFAULT_GRID_LINE_LENGTH,
                    DEFAULT_GRID_SPACE_LENGTH,
                    DEFAULT_GRIP_PHASE
                )
                setDrawLimitLinesBehindData(true)
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = DateLabelFormatter { graph.dateFromSteps(it) }
                granularity = DEFAULT_GRANULARITY
                axisMinimum = X_AXIS_DEFAULT_MIN
                axisMaximum = graph.numberOfStepsToLastDate() + 1f
            }

            axisLeft.apply {
                enableGridDashedLine(
                    DEFAULT_GRID_LINE_LENGTH,
                    DEFAULT_GRID_SPACE_LENGTH,
                    DEFAULT_GRIP_PHASE
                )
                axisMaximum = (graph.maxValue()?.let { it ->
                    it.fieldValue + X_AXIS_MAX_PADDING_WITH_VALUE
                } ?: DEFAULT_VALUE + X_AXIS_MAX_PADDING)
                axisMinimum = (graph.minValue()?.let { it ->
                    if (it.fieldValue < 0f) {
                        it.fieldValue + X_AXIS_MIN_PADDING
                    } else {
                        DEFAULT_VALUE
                    }
                } ?: DEFAULT_VALUE)
                setDrawLimitLinesBehindData(true)
            }
            axisRight.isEnabled = false

            animateX(DEFAULT_ANIM_TIME)

            legend.apply {
                form = Legend.LegendForm.LINE
            }

            data = barData

            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DEFAULT_CHART_HEIGHT)
        }
    }
}
