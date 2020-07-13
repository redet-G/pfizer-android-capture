package org.dhis2.bindings

import android.content.Context
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.Bindings.toDateSpan
import org.dhis2.Bindings.toUiText
import org.dhis2.R
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DateExtensionsTest {

    val context: Context = mock()
    private val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
    private val uiFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

    @Test
    fun `Should return empty when date is null`() {
        val date: Date? = null
        assert(date.toDateSpan(context) == "")
        assert(date.toUiText(context) == "")
    }

    @Test
    fun `Should return date format when date is after today`() {
        val date: Date? = currentCalendar().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time
        assert(date.toDateSpan(context) == dateFormat.format(date))
        assert(date.toUiText(context) == dateFormat.format(date))
    }

    @Test
    fun `Should return "now" when date is less than a minute from current date`() {
        val date: Date? = Date()
        whenever(context.getString(R.string.interval_now)) doReturn "now"
        assert(date.toDateSpan(context) == "now")
    }

    @Test
    fun `Should return "5 minutes ago" when date is 5 minutes ago from current date`() {
        val date: Date? = currentCalendar().apply {
            add(Calendar.MINUTE, -5)
        }.time
        whenever(context.getString(R.string.interval_minute_ago)) doReturn "%d min. ago"
        assert(date.toDateSpan(context) == "5 min. ago")
    }

    @Test
    fun `Should return "3 hours ago" when date is 3 hours ago from current date`() {
        val date: Date? = currentCalendar().apply {
            add(Calendar.HOUR, -3)
        }.time
        whenever(context.getString(R.string.interval_hour_ago)) doReturn "%d hours ago"
        assert(date.toDateSpan(context) == "3 hours ago")
    }

    @Test
    fun `Should return "yesterday" when date is more than 24h from current date`() {
        val date: Date? = currentCalendar().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }.time
        whenever(context.getString(R.string.interval_yesterday)) doReturn "Yesterday"
        whenever(context.getString(R.string.filter_period_yesterday)) doReturn "Yesterday"
        assert(date.toDateSpan(context) == "Yesterday")
        assert(date.toUiText(context) == "Yesterday")
    }

    @Test
    fun `Should return date format when date is more than 48h from current date`() {
        val date: Date? = currentCalendar().apply {
            add(Calendar.DAY_OF_MONTH, -3)
        }.time
        assert(date.toDateSpan(context) == dateFormat.format(date))
    }

    @Test
    fun `Should return "today" when date is less than 24h from current date`() {
        val date: Date? = currentCalendar().apply {
            add(Calendar.HOUR, -20)
        }.time
        whenever(context.getString(R.string.filter_period_today)) doReturn "Today"
        assert(date.toUiText(context) == "Today")
    }

    @Test
    fun `Should return dd MMM format when date is same year of current date`() {
        val date: Date? = currentCalendar().apply {
            add(Calendar.MONTH, -2)
        }.time
        assert(date.toUiText(context) == uiFormat.format(date))
    }

    @Test
    fun `Should return date format format when date is more than one year from current date`() {
        val date: Date? = currentCalendar().apply {
            add(Calendar.YEAR, -2)
        }.time
        assert(date.toUiText(context) == dateFormat.format(date))
    }

    private fun currentCalendar() = Calendar.getInstance().apply {
        time = Date()
    }


}
