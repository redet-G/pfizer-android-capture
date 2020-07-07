package org.dhis2.usescases.teidashboard

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.R
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.searchTeiRobot
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teidashboard.entity.EnrollmentUIModel
import org.dhis2.usescases.teidashboard.entity.UpperEnrollmentUIModel
import org.dhis2.usescases.teidashboard.robot.enrollmentRobot
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.dhis2.usescases.teidashboard.robot.indicatorsRobot
import org.dhis2.usescases.teidashboard.robot.noteRobot
import org.dhis2.usescases.teidashboard.robot.relationshipRobot
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeiDashboardTest : BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(TeiDashboardMobileActivity::class.java, false, false)

    @get:Rule
    val ruleSearch = ActivityTestRule(SearchTEActivity::class.java, false, false)

    @Test
    fun shouldSuccessfullyCreateANoteWhenClickCreateNote() {
        setupCredentials()

        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnNotesTab()
        }

        noteRobot {
            clickOnFabAddNewNote()
            typeNote(NOTE_VALID)
            clickOnSaveButton()
            checkNewNoteWasCreated(NOTE_VALID)
        }
    }

    @Test
    fun shouldNotCreateANoteWhenClickClear() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnNotesTab()
        }

        noteRobot {
            clickOnFabAddNewNote()
            typeNote(NOTE_INVALID)
            clickOnClearButton()
            clickYesOnAlertDialog()
            checkNoteWasNotCreated(NOTE_INVALID)
        }
    }

    @Test
    fun shouldOpenNotesDetailsWhenClickOnNote() {
        prepareTeiWithExistingNoteAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnNotesTab()
        }

        noteRobot {
            clickOnNoteWithPosition(0)
            checkNoteDetails("@$USER", NOTE_EXISTING_TEXT)
        }
    }

    @Test
    fun shouldReactivateTEIWhenClickReOpen() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnMenuReOpen()
            checkUnlockIconIsDisplay()
            checkCanAddEvent()
        }
    }

    @Test
    fun shouldDeactivateTEIWhenClickOpen() {
        prepareTeiOpenedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnMenuDeactivate()
            checkLockIconIsDisplay()
            checkCanNotAddEvent()
        }
    }

    @Test
    fun shouldCompleteTEIWhenClickOpen() {
        prepareTeiOpenedForCompleteProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnMenuComplete()
            checkLockCompleteIconIsDisplay()
            checkCanNotAddEvent()
        }
    }

    @Test
    fun shouldShowQRWhenClickOnShare() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnShareButton()
            clickOnNextQR()
        }
    }

    @Test
    fun shouldMakeAReferral() {
        prepareTeiOpenedForReferralProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnFab()
            clickOnReferral()
            clickOnFirstReferralEvent()
            clickOnReferralOption()
            clickOnReferralNextButton()
            checkEventCreatedToastIsShown()
            checkEventWasCreated(LAB_MONITORING)
        }
    }

    @Test
    fun shouldSuccessfullyScheduleAnEvent() {
        prepareTeiOpenedWithNoPreviousEventProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnFab()
            clickOnScheduleNew()
            clickOnFirstReferralEvent()
            clickOnReferralNextButton()
            checkEventCreatedToastIsShown()
            checkEventWasCreated(LAB_MONITORING)
        }
    }

    @Test
    fun shouldNotBeAbleToCreateNewEventsWhenFull() {
        prepareTeiOpenedWithFullEventsAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnFab()
            clickOnReferral()
            checkCannotAddMoreEventToastIsShown()
        }
    }

    @Test
    fun shouldOpenEventAndSaveSuccessfully() {
        setupCredentials()

        prepareTeiOpenedProgrammeAndLaunchActivity(rule)

        val babyPostNatal = 0
        teiDashboardRobot {
            clickOnEventWithPosition(babyPostNatal)
        }

        eventRobot {
            scrollToBottomForm()
            clickOnFormFabButton()
            clickOnFinish()
        }
    }

    @Test
    fun shouldShowCorrectInfoWhenOpenTEI() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        val upperInformation = createExpectedUpperInformation()

        teiDashboardRobot {
            checkUpperInfo(upperInformation)
        }
    }

    @Test
    fun shouldShowTEIDetailsWhenClickOnSeeDetails() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        val enrollmentFullDetails = createExpectedEnrollmentInformation()

        teiDashboardRobot {
            clickOnSeeDetails()
            checkFullDetails(enrollmentFullDetails)
        }
    }

    @Test
    fun shouldShowIndicatorsDetailsWhenClickOnIndicatorsTab() {
        prepareTeiCompletedProgrammeAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnIndicatorsTab()
        }

        indicatorsRobot {
            checkDetails("0", "4817")
        }
    }

    @Test
    fun shouldSuccessfullyCreateANewEvent() {
        prepareTeiToCreateANewEventAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnFab()
            clickOnCreateNewEvent()
            clickOnFirstReferralEvent()
            clickOnReferralNextButton()
            waitToDebounce(600)
        }

        eventRobot {
            fillRadioButtonForm(4)
            clickOnFormFabButton()
            clickOnFinish()
        }

        teiDashboardRobot {
            checkEventWasCreatedAndOpen(LAB_MONITORING, 0)
        }
    }

    @Test
    fun shouldOpenEventEditAndSaveSuccessfully() {
        prepareTeiOpenedToEditAndLaunchActivity(rule)

        val labMonitoring = 2

        teiDashboardRobot {
            clickOnEventWithPosition(labMonitoring)
            waitToDebounce(600)
        }

        eventRobot {
            clickOnChangeDate()
            clickOnEditDate()
            acceptUpdateEventDate()
            clickOnUpdate()
            waitToDebounce(600)
            fillRadioButtonForm(4)
            clickOnFormFabButton()
            clickOnFinishAndComplete()
            waitToDebounce(600)
        }

        teiDashboardRobot {
            checkEventWasCreatedAndClosed(LAB_MONITORING, 3)
        }
    }

    @Test
    fun shouldEnrollToOtherProgramWhenClickOnProgramEnrollments() {
        val womanProgram = "MNCH / PNC (Adult Woman)"
        val personAttribute = context.getString(R.string.enrollment_single_section_label).replace("%s","")
        val visitPNCEvent = "PNC Visit"
        val deliveryEvent = "Delivery"
        val visitANCEvent = "ANC Visit (2-4+)"
        val firstANCVisitEvent = "ANC 1st visit"

        prepareTeiToEnrollToOtherProgramAndLaunchActivity(rule)

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnMenuProgramEnrollments()
        }

        enrollmentRobot {
            clickOnAProgramForEnrollment(womanProgram)
            clickOnAcceptEnrollmentDate()
            clickOnPersonAttributes(personAttribute)
            clickOnCalendarItem()
            clickOnAcceptEnrollmentDate()
            scrollToBottomProgramForm()
            clickOnSaveEnrollment()
        }

        teiDashboardRobot {
            checkEventWasScheduled(visitPNCEvent, 0)
            checkEventWasScheduled(deliveryEvent, 1)
            checkEventWasScheduled(visitANCEvent, 2)
            checkEventWasCreatedAndOpen(firstANCVisitEvent, 3)
        }
    }

    @Test
    @Ignore
    fun shouldSuccessfullyCreateRelationshipWhenClickAdd() {
        val teiName = "Tim"
        val teiLastName = "Johnson"
        val relationshipName = "Filona"
        val relationshipLastName = "Ryder"
        val completeName = "Ryder Filona"

        setupCredentials()
        prepareChildProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot {
            closeSearchForm()
            Thread.sleep(4000)
            clickOnTEI(teiName, teiLastName)
        }

        teiDashboardRobot {
            clickOnRelationshipTab()
        }

        relationshipRobot {
            clickOnFabAdd()
            clickOnRelationshipType()
        }

        searchTeiRobot {
            closeSearchForm()
            Thread.sleep(4000)
            clickOnTEI(relationshipName, relationshipLastName)
        }

        relationshipRobot {
            checkRelationshipWasCreated(0, completeName)
        }
    }

    @Ignore("Check test. Sometimes it fails")
    @Test
    fun shouldDeleteTeiSuccessfully() {
        val teiName = "Anthony"
        val teiLastName = "Banks"

        setupCredentials()
        prepareChildProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot {
            closeSearchForm()
            Thread.sleep(4000)
            clickOnTEI(teiName, teiLastName)
        }

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnMenuDeleteTEI()
        }

        searchTeiRobot {
            Thread.sleep(4000)
            checkTEIsDelete(teiName, teiLastName)
        }
    }

    @Ignore("Check test. Sometimes it fails")
    @Test
    fun shouldDeleteEnrollmentSuccessfully() {

        val teiName = "Anna"
        val teiLastName = "Jones"

        setupCredentials()
        prepareChildProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot {
            closeSearchForm()
            Thread.sleep(4000)
            clickOnTEI(teiName, teiLastName)
        }

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnMenuDeleteEnrollment()
        }

        searchTeiRobot {
            Thread.sleep(4000)
            checkTEIsDelete(teiName, teiLastName)
        }
    }

    private fun createExpectedUpperInformation() =
        UpperEnrollmentUIModel(
            "2021-01-10",
            "2021-01-10",
            "Ngelehun CHC"
        )

    private fun createExpectedEnrollmentInformation() =
        EnrollmentUIModel(
            "2021-01-10",
            "2021-01-10",
            "Ngelehun CHC",
            "40.48713205295354",
            "-3.6847423830882633",
            "Filona",
            "Ryder",
            "Female"
        )

    companion object {
        const val NOTE_VALID = "ThisIsJustATest"
        const val NOTE_INVALID = "InvalidNote"
        const val NOTE_EXISTING_TEXT = "This is a note test"
        const val USER = "android"

        const val LAB_MONITORING = "Lab monitoring"

        const val API_TEI_1_RESPONSE_OK = "mocks/teilist/teilist_1.json"
        const val API_TEI_2_RESPONSE_OK = "mocks/teilist/teilist_2.json"
        const val API_TEI_3_RESPONSE_OK = "mocks/teilist/teilist_3.json"
    }
}
