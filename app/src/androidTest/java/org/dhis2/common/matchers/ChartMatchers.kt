package org.dhis2.common.matchers

import android.view.View
import androidx.annotation.NonNull
import androidx.test.espresso.matcher.BoundedMatcher
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import dhis2.org.analytics.charts.data.ChartType
import org.hamcrest.Description
import org.hamcrest.Matcher

class ChartMatchers {
    companion object {
        fun hasChartType(
            @NonNull chartType: ChartType
        ): Matcher<View> {
            return object : BoundedMatcher<View, View>(View::class.java) {
                override fun describeTo(description: Description) {
                    description.appendText("has item: ")
                }

                override fun matchesSafely(view: View): Boolean {
                    return when (chartType){
                        ChartType.LINE_CHART -> view is LineChart
                        ChartType.BAR_CHART -> view is BarChart
                    }
                }
            }
        }
    }
}