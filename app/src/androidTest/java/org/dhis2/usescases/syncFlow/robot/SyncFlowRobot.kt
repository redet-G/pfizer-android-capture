import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailViewHolder
import org.dhis2.usescases.programEventDetail.ProgramEventDetailViewHolder
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTEViewHolder
import org.hamcrest.Matchers.allOf

fun syncFlowRobot(syncFlowRobot: SyncFlowRobot.() -> Unit) {
    SyncFlowRobot().apply {
        syncFlowRobot()
    }
}

class SyncFlowRobot : BaseRobot() {

    fun clickOnSyncTei(teiName: String, teiLastName: String) {
        onView(withId(R.id.scrollView)).perform(
            scrollTo<SearchTEViewHolder>(allOf(hasDescendant(withText(teiName)), hasDescendant(withText(teiLastName)))),
            actionOnItem<SearchTEViewHolder>(allOf(hasDescendant(withText(teiName)), hasDescendant(withText(teiLastName))), clickChildViewWithId(R.id.syncState))
        )
    }

    fun clickOnSyncButton() {
        onView(withId(R.id.syncButton)).perform(click())
    }

    fun checkSyncWasSuccessfully() {
        onView(withId(R.id.noConflictMessage)).check(matches(withText(R.string.no_conflicts_synced_message)))
    }

    fun checkSyncFailed() {
        onView(withId(R.id.noConflictMessage)).check(matches(withText(R.string.no_conflicts_update_message)))
    }

    fun clickOnEventToSync(position: Int) {
        onView(withId(R.id.recycler))
            .perform(
                actionOnItemAtPosition<ProgramEventDetailViewHolder>(position, clickChildViewWithId(R.id.sync_icon))
            )
    }

    fun clickOnDataSetToSync(position: Int) {
        onView(withId(R.id.recycler))
            .perform(actionOnItemAtPosition<DataSetDetailViewHolder>(position, clickChildViewWithId(R.id.sync_icon)))
    }
} 