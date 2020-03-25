package org.dhis2.usescases.teidashboard

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeiDashboardTest : BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(TeiDashboardMobileActivity::class.java, false, false)

    override fun setUp() {
        super.setUp()
        //init robot
    }

    @Test
    fun shouldSuccessfullyCreateANoteWhenClickCreateNote() {
        setupCredentials()
        prepareTeiCompletedProgrammeIntentAndLaunchActivity()

        teiDashboardRobot {
            clickOnPinTab()
        }

        noteRobot {
            checkFabDisplay()
            clickOnFabAddNewNote()
            typeNote("ThisIsJustATest")
            clickOnSaveButton()
            checkNewNoteWasCreated("ThisIsJustATest")
        }
    }

    @Test
    fun shouldNotCreateANoteWhenClickClear() {
        prepareTeiCompletedProgrammeIntentAndLaunchActivity()

        teiDashboardRobot {
            clickOnPinTab()
        }

        noteRobot {
            checkFabDisplay()
            clickOnFabAddNewNote()
            typeNote("InvalidNote")
            clickOnClearButton()
            clickYesOnAlertDialog()
            checkNoteWasNotCreated("InvalidNote")
        }
    }

    @Test
    fun shouldOpenNotesDetailed() {
        prepareTeiCompletedProgrammeIntentAndLaunchActivity()

        teiDashboardRobot {
            clickOnPinTab()
        }

        noteRobot {
            clickOnNote(0)
            //checkear que es el texto esperado/usuario/etc..
        }
    }

    private fun prepareTeiCompletedProgrammeIntentAndLaunchActivity() {
        Intent().apply {
            putExtra(CHILD_PROGRAM_UID, CHILD_PROGRAM_UID_VALUE)
            putExtra(TEI_UID, TEI_UID_VALUE)
        }.also { rule.launchActivity(it) }
    }

    private fun prepareTeiOpenedProgrammeIntentAndLaunchActivity() {

    }

    companion object{
        const val CHILD_PROGRAM_UID = "PROGRAM_UID"
        const val CHILD_PROGRAM_UID_VALUE = "IpHINAT79UW"

        const val TEI_UID = "TEI_UID"
        const val TEI_UID_VALUE = "vOxUH373fy5"
    }
}