package org.dhis2.usescases.searchte

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSpinnerText
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.common.viewactions.openSpinnerPopup
import org.dhis2.common.viewactions.typeChildViewWithId
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTEViewHolder
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.startsWith


fun searchTeiRobot(searchTeiRobot: SearchTeiRobot.() -> Unit) {
    SearchTeiRobot().apply {
        searchTeiRobot()
    }
}

class SearchTeiRobot : BaseRobot() {

    fun closeSearchForm () {
        waitToDebounce(2500)
        onView(withId(R.id.close_filter)).perform(click())
    }

    fun clickOnTEI(teiName: String, teiLastName: String) {
        onView(withId(R.id.scrollView)).perform(
            scrollTo<SearchTEViewHolder>(allOf(hasDescendant(withText(teiName)), hasDescendant(withText(teiLastName)))),
            actionOnItem<SearchTEViewHolder>(allOf(hasDescendant(withText(teiName)), hasDescendant(withText(teiLastName))), click())
        )
    }

    fun checkTEIsDelete(teiName: String, teiLastName: String) {
        onView(withId(R.id.scrollView))
            .check(matches(not(hasItem(allOf(hasDescendant(withText(teiName)), hasDescendant(
                withText(teiLastName)))))))
    }

    fun searchByPosition(searchWord: String, position:Int) {
        waitToDebounce(1000)
        onView(withId(R.id.form_recycler))
            .perform(
                actionOnItemAtPosition<SearchTEViewHolder>(position, typeChildViewWithId(searchWord, R.id.input_editText))
            )
    }

    fun searchByField(searchWord: String, field: String) {
        waitToDebounce(1000)
        onView(withId(R.id.form_recycler))
            .perform(
                actionOnItem<SearchTEViewHolder>(hasDescendant(withText(field)), typeChildViewWithId(searchWord, R.id.input_editText))
            )
    }

    fun clickOnDateField() {
        onView(withId(R.id.form_recycler))
            .perform(
                actionOnItemAtPosition<SearchTEViewHolder>(2, clickChildViewWithId(R.id.inputEditText))
            )
    }

    fun selectSpecificDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        onView(withId(R.id.widget_datepicker)).perform(PickerActions.setDate(year, monthOfYear, dayOfMonth))
    }

    fun acceptDate() {
        onView(withId(R.id.acceptButton)).perform(click())
    }

    fun clickOnFab() {
        onView(withId(R.id.enrollmentButton)).perform(click())
    }

    fun checkListOfSearchTEI(firstSearchWord: String, secondSearchWord: String) {
        onView(withId(R.id.scrollView))
            .check(matches(hasItem(allOf(
                hasDescendant(withText(firstSearchWord)),
                hasDescendant(withText(secondSearchWord))))))
    }

    fun checkFilterCount(filterCount: String) {
        onView(withId(R.id.filterCounterSearch))
            .check(matches(withChild(withText(filterCount))))
    }

    fun checkNoSearchResult(searchWord: String, message: String) {
        onView(withId(R.id.scrollView))
            .check(matches(not(hasItem(hasDescendant(withText(searchWord))))))

        onView(withId(R.id.message))
            .check(matches(withText(message)))
    }

    fun clickOnProgramSpinner() {
        onView(withId(R.id.program_spinner)).perform(openSpinnerPopup())
    }

    fun selectAProgram(program: String) {
        /*onView(withId(R.id.spinner_text))
            .check(matches(withSpinnerText(containsString(program))))*/

        onData(allOf(`is`(instanceOf(String::class.java)), `is`("Antenatal care visit")))
            .perform(click())

        onView(withId(R.id.spinner_text))
            .check(matches(withText(containsString(program))))
    }

}
