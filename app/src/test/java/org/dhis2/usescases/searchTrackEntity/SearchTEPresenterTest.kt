package org.dhis2.usescases.searchTrackEntity

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.schedulers.TestScheduler
import junit.framework.TestCase.assertTrue
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.TestSchedulerProvider
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.uicomponents.map.mapper.EventToEventUiComponent
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.filters.FilterManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.program.Program
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class SearchTEPresenterTest {

    lateinit var presenter: SearchTEContractsModule.Presenter

    private val view: SearchTEContractsModule.View = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val repository: SearchRepository = mock()
    private val schedulers: TestSchedulerProvider = TestSchedulerProvider(TestScheduler())
    private val analyticsHelper: AnalyticsHelper = mock()
    private val mapTeisToFeatureCollection: MapTeisToFeatureCollection = mock()
    private val mapTeiEventsToFeatureCollection: MapTeiEventsToFeatureCollection = mock()
    private val eventToEventUiComponent: EventToEventUiComponent = mock()
    private val initialProgram = "programUid"
    private val preferenceProvider: PreferenceProvider = mock()

    @Before
    fun setUp() {
        whenever(d2.programModule().programs().uid(initialProgram).blockingGet()) doReturn
            Program.builder().uid(
                initialProgram
            )
                .displayFrontPageList(true)
                .minAttributesRequiredToSearch(0).build()

        presenter = SearchTEPresenter(
            view,
            d2,
            repository,
            schedulers,
            analyticsHelper,
            initialProgram,
            mapTeisToFeatureCollection,
            mapTeiEventsToFeatureCollection,
            eventToEventUiComponent,
            preferenceProvider
        )
    }

    @Test
    fun `Should ignore initial program spinner selection`() {
        val program = Program.builder()
            .uid("uid")
            .displayFrontPageList(true)
            .minAttributesRequiredToSearch(1)
            .build()

        presenter.setProgramForTesting(program)

        presenter.program = program

        verify(view, never()).clearList(program.uid())
        verify(view, never()).setFabIcon(true)
        verify(view, never()).clearData()
    }

    @Test
    fun `Should clear data, fab and list when another program is selected`() {
        val program = Program.builder()
            .uid("uid")
            .displayFrontPageList(true)
            .minAttributesRequiredToSearch(1)
            .build()

        val newSelectedProgram = Program.builder()
            .uid("uid2")
            .displayFrontPageList(true)
            .minAttributesRequiredToSearch(1)
            .build()

        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(newSelectedProgram.uid())
        ) doReturn mock()

        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(newSelectedProgram.uid())
                .byEnableUserAssignment()
        ) doReturn mock()

        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(newSelectedProgram.uid())
                .byEnableUserAssignment().isTrue
        ) doReturn mock()

        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(newSelectedProgram.uid())
                .byEnableUserAssignment().isTrue
                .blockingIsEmpty()
        ) doReturn false

        presenter.setProgramForTesting(program)
        presenter.program = newSelectedProgram

        verify(view).clearList(newSelectedProgram.uid())
        verify(view).setFabIcon(true)
        verify(view).clearData()
    }

    @Test
    fun `Should set fabIcon to search if displayFrontPageList and queryData is empty`() {
        presenter.setProgramForTesting(
            Program.builder()
                .uid("uid")
                .displayFrontPageList(true)
                .minAttributesRequiredToSearch(1)
                .build()
        )

        presenter.onFabClick(true)

        verify(view).clearData()
        verify(view).updateFiltersSearch(0)
        verify(view).setFabIcon(true)
    }

    @Test
    fun `Should set fabIcon to search if displayFrontPageList and minAttributes is ok`() {
        presenter.setProgramForTesting(
            Program.builder()
                .uid("uid")
                .displayFrontPageList(true)
                .minAttributesRequiredToSearch(1)
                .build()
        )
        presenter.queryData["uid"] = "value"
        presenter.onFabClick(true)

        verify(view).clearData()
        verify(view).updateFiltersSearch(1)
        verify(view).setFabIcon(false)
    }

    @Test
    fun `Should set fabIcon to add when displayFrontPageList and minAttributes is 0`() {
        presenter.setProgramForTesting(
            Program.builder()
                .uid("uid")
                .displayFrontPageList(true)
                .minAttributesRequiredToSearch(0)
                .build()
        )
        presenter.queryData["uid"] = "value"
        presenter.onFabClick(true)

        verify(view).clearData()
        verify(view).updateFiltersSearch(1)
        verify(view).setFabIcon(false)
    }

    @Test
    fun `Should set fabIcon to add if there is no program selected`() {
        presenter.setProgramForTesting(null)
        presenter.onFabClick(true)

        verify(view).clearData()
        verify(view).updateFiltersSearch(0)
        verify(view).setFabIcon(false)
    }

    @Test
    fun `Should show assign to me filter`() {
        presenter.setProgramForTesting(
            Program.builder()
                .uid("uid")
                .displayFrontPageList(true)
                .minAttributesRequiredToSearch(0)
                .build()
        )
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq("uid")
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq("uid")
                .byEnableUserAssignment()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq("uid")
                .byEnableUserAssignment().isTrue
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq("uid")
                .byEnableUserAssignment().isTrue
                .blockingIsEmpty()
        ) doReturn false
        presenter.initAssignmentFilter()
        verify(view, times(1)).showAssignmentFilter()
        verify(view, times(0)).hideAssignmentFilter()
    }

    @Test
    fun `Should not show assign to me filter if no stage is configured`() {
        presenter.setProgramForTesting(
            Program.builder()
                .uid("uid")
                .displayFrontPageList(true)
                .minAttributesRequiredToSearch(0)
                .build()
        )
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq("uid")
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq("uid")
                .byEnableUserAssignment()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq("uid")
                .byEnableUserAssignment().isTrue
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq("uid")
                .byEnableUserAssignment().isTrue
                .blockingIsEmpty()
        ) doReturn true
        presenter.initAssignmentFilter()
        verify(view, times(0)).showAssignmentFilter()
        verify(view, times(1)).hideAssignmentFilter()
    }

    @Test
    fun `Should not show assign to me filter if no program selected`() {
        presenter.setProgramForTesting(null)
        presenter.initAssignmentFilter()
        verify(view, times(0)).showAssignmentFilter()
        verify(view, times(1)).hideAssignmentFilter()
    }

    @Test
    fun `Should clear query data if program is changed to a non null object`() {
        val currentProgram = Program.builder()
            .uid("program1")
            .build()
        val selectedProgram = Program.builder()
            .uid("program2")
            .build()
        whenever(
            d2.programModule().programStages()
                .byProgramUid()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(selectedProgram.uid())
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(selectedProgram.uid())
                .byEnableUserAssignment()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(selectedProgram.uid())
                .byEnableUserAssignment().isTrue
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(selectedProgram.uid())
                .byEnableUserAssignment().isTrue
                .blockingIsEmpty()
        ) doReturn true
        presenter.setProgramForTesting(currentProgram)
        presenter.queryData["uid"] = "value"
        presenter.program = selectedProgram
        assertTrue(presenter.queryData.isEmpty())
    }

    @Test
    fun `Should clear query data if existing program is changed to null object`() {
        presenter.setProgramForTesting(
            Program.builder()
                .uid("program1")
                .build()
        )
        presenter.queryData["uid"] = "value"
        presenter.program = null
        assertTrue(presenter.queryData.isEmpty())
    }

    @Test
    fun `Should not clear query data if program is not changed`() {
        val currentProgram =
            Program.builder()
                .uid("program1")
                .build()
        val selectedProgram = Program.builder()
            .uid("program1")
            .build()
        whenever(
            d2.programModule().programStages()
                .byProgramUid()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(selectedProgram.uid())
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(selectedProgram.uid())
                .byEnableUserAssignment()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(selectedProgram.uid())
                .byEnableUserAssignment().isTrue
        ) doReturn mock()
        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(selectedProgram.uid())
                .byEnableUserAssignment().isTrue
                .blockingIsEmpty()
        ) doReturn true
        presenter.setProgramForTesting(
            currentProgram
        )
        presenter.queryData["uid"] = "value"

        presenter.program = selectedProgram
        assertTrue(presenter.queryData.isNotEmpty())
    }

    @Test
    fun `Should not clear query data if null program is not changed`() {
        presenter.setProgramForTesting(null)
        presenter.queryData["uid"] = "value"
        presenter.program = null
        assertTrue(presenter.queryData.isNotEmpty())
    }

    @Test
    fun `Should show filters if list is ok`() {
        presenter.checkFilters(true)
        verify(view, times(1)).setFiltersVisibility(true)
    }

    @Test
    fun `Should show filters if list is not ok but filters are active`() {
        FilterManager.clearAll()
        FilterManager.getInstance().setAssignedToMe(true)
        presenter.checkFilters(false)
        verify(view, times(1)).setFiltersVisibility(true)
    }

    @Test
    fun `Should not show filters if list is not ok and filters are not active`() {
        FilterManager.clearAll()
        presenter.checkFilters(false)
        verify(view, times(1)).setFiltersVisibility(false)
    }
}
