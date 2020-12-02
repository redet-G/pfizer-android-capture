package org.dhis2.data.filter

import org.dhis2.utils.filters.AssignedFilter
import org.dhis2.utils.filters.CatOptionComboFilter
import org.dhis2.utils.filters.EnrollmentDateFilter
import org.dhis2.utils.filters.EnrollmentStatusFilter
import org.dhis2.utils.filters.EventStatusFilter
import org.dhis2.utils.filters.FilterItem
import org.dhis2.utils.filters.FilterManager
import org.dhis2.utils.filters.OrgUnitFilter
import org.dhis2.utils.filters.PeriodFilter
import org.dhis2.utils.filters.SyncStateFilter
import org.dhis2.utils.filters.WorkingListFilter
import org.dhis2.utils.filters.workingLists.EventWorkingListItem
import org.dhis2.utils.filters.workingLists.TeiWorkingListItem
import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.AssignedUserMode
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSetInstanceSummaryCollectionRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQueryCollectionRepository
import java.util.Calendar
import javax.inject.Inject

class FilterRepository @Inject constructor(
    private val d2: D2,
    private val resources: ResourceManager
) {

    fun trackedEntityInstanceQueryByProgram(
        programUid: String
    ): TrackedEntityInstanceQueryCollectionRepository {
        return d2.trackedEntityModule().trackedEntityInstanceQuery()
            .byProgram().eq(programUid)
    }

    fun trackedEntityInstanceQueryByType(
        trackedEntityTypeUid: String
    ): TrackedEntityInstanceQueryCollectionRepository {
        return d2.trackedEntityModule().trackedEntityInstanceQuery()
            .byTrackedEntityType().eq(trackedEntityTypeUid)
    }

    fun rootOrganisationUnitUids(): List<String> {
        return d2.organisationUnitModule().organisationUnits()
            .byRootOrganisationUnit(true)
            .blockingGetUids()
    }

    fun applyEnrollmentStatusFilter(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        enrollmentStatuses: List<EnrollmentStatus>
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.byEnrollmentStatus().`in`(enrollmentStatuses)
    }

    fun applyEventStatusFilter(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        eventStatuses: List<EventStatus>
    ): TrackedEntityInstanceQueryCollectionRepository {
        val fromDate = Calendar.getInstance().apply {
            add(Calendar.YEAR, -1)
        }.time
        val toDate = Calendar.getInstance().apply {
            add(Calendar.YEAR, 1)
        }.time
        return repository.byEventStatus().`in`(eventStatuses)
            .byEventStartDate().eq(fromDate)
            .byEventEndDate().eq(toDate)
    }

    fun applyOrgUnitFilter(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        ouMode: OrganisationUnitMode,
        orgUnitUis: List<String>
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.byOrgUnitMode().eq(ouMode)
            .byOrgUnits().`in`(orgUnitUis)
    }

    fun applyStateFilter(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        states: List<State>
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.byStates().`in`(states)
    }

    fun applyDateFilter(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        datePeriod: DatePeriod
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.byEventStartDate().eq(datePeriod.startDate())
            .byEventEndDate().eq(datePeriod.endDate())
    }

    fun applyEnrollmentDateFilter(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        datePeriod: DatePeriod
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.byProgramStartDate().eq(datePeriod.startDate())
            .byProgramEndDate().eq(datePeriod.endDate())
    }

    fun applyAssignToMe(
        repository: TrackedEntityInstanceQueryCollectionRepository
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.byAssignedUserMode().eq(AssignedUserMode.CURRENT)
    }

    fun sortByPeriod(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.orderByEventDate().eq(orderDirection)
    }

    fun sortByOrgUnit(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.orderByOrganisationUnitName().eq(orderDirection)
    }

    fun sortByEnrollmentDate(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.orderByEnrollmentDate().eq(orderDirection)
    }

    fun sortByEnrollmentStatus(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.orderByEnrollmentStatus().eq(orderDirection)
    }

    fun eventsByProgram(programUid: String): EventCollectionRepository {
        return d2.eventModule().events()
            .byDeleted().isFalse
            .byProgramUid().eq(programUid)
    }

    fun applyOrgUnitFilter(
        repository: EventCollectionRepository,
        orgUnitUis: List<String>
    ): EventCollectionRepository {
        return repository.byOrganisationUnitUid().`in`(orgUnitUis)
    }

    fun applyStateFilter(
        repository: EventCollectionRepository,
        states: List<State>
    ): EventCollectionRepository {
        return repository.byState().`in`(states)
    }

    fun applyDateFilter(
        repository: EventCollectionRepository,
        datePeriods: List<DatePeriod>
    ): EventCollectionRepository {
        return repository.byEventDate().inDatePeriods(datePeriods)
    }

    fun applyAssignToMe(repository: EventCollectionRepository): EventCollectionRepository {
        return repository.byAssignedUser().eq(currentUserUid())
    }

    private fun currentUserUid(): String {
        return d2.userModule().user().blockingGet().uid()
    }

    fun sortByEventDate(
        repository: EventCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection
    ): EventCollectionRepository {
        return repository.orderByEventDate(orderDirection)
    }

    fun sortByOrgUnit(
        repository: EventCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection
    ): EventCollectionRepository {
        return repository.orderByOrganisationUnitName(orderDirection)
    }

    fun dataSetInstanceSummaries(): DataSetInstanceSummaryCollectionRepository {
        return d2.dataSetModule().dataSetInstanceSummaries()
    }

    fun applyOrgUnitFilter(
        repository: DataSetInstanceSummaryCollectionRepository,
        orgUnitUis: List<String>
    ): DataSetInstanceSummaryCollectionRepository {
        return repository.byOrganisationUnitUid().`in`(orgUnitUis)
    }

    fun applyStateFilter(
        repository: DataSetInstanceSummaryCollectionRepository,
        states: List<State>
    ): DataSetInstanceSummaryCollectionRepository {
        return repository.byState().`in`(states)
    }

    fun applyPeriodFilter(
        repository: DataSetInstanceSummaryCollectionRepository,
        datePeriods: List<DatePeriod>
    ): DataSetInstanceSummaryCollectionRepository {
        return repository.byPeriodStartDate().inDatePeriods(datePeriods)
    }

    fun orgUnitsByName(name: String): List<OrganisationUnit> =
        d2.organisationUnitModule()
            .organisationUnits()
            .byDisplayName().like("%$name%")
            .blockingGet()

    fun programFilters(programUid: String): List<FilterItem> {
        return d2.programModule().programs().uid(programUid).get()
            .map {
                if (it.programType() == ProgramType.WITH_REGISTRATION) {
                    getTrackerFilters(it)
                } else {
                    getEventFilters(it)
                }
            }.blockingGet()
    }

    fun trackedEntityFilters(): List<FilterItem> {
        return mutableListOf<FilterItem>().apply {
            add(
                PeriodFilter(
                    resources.filterEventDateLabel(),
                    FilterManager.getInstance().periodFilters,
                    FilterManager.getInstance().periodIdSelected
                )
            )
            add(OrgUnitFilter(FilterManager.getInstance().observeOrgUnitFilters()))
            add(SyncStateFilter(emptyList()))
            add(EnrollmentStatusFilter(FilterManager.getInstance().enrollmentStatusFilters))
            add(EventStatusFilter(emptyList()))
        }
    }

    fun dataSetFilters(dataSetUid: String): List<FilterItem> {
        return mutableListOf<FilterItem>().apply {
            add(
                PeriodFilter(
                    resources.filterPeriodLabel(),
                    FilterManager.getInstance().periodFilters,
                    FilterManager.getInstance().periodIdSelected
                )
            )
            add(OrgUnitFilter(FilterManager.getInstance().observeOrgUnitFilters()))
            add(SyncStateFilter(emptyList()))
            val dataSet = d2.dataSetModule().dataSets().uid(dataSetUid).blockingGet()
            val categoryCombo =
                d2.categoryModule().categoryCombos().uid(dataSet.categoryCombo()?.uid())
                    .blockingGet()
            if (categoryCombo.isDefault == false) {
                add(
                    CatOptionComboFilter(
                        categoryCombo,
                        d2.categoryModule().categoryOptionCombos().byCategoryComboUid()
                            .eq(categoryCombo.uid()).blockingGet(),
                        emptyList()
                    )
                )
            }
        }
    }

    fun homeFilters(): List<FilterItem> {
        return mutableListOf<FilterItem>().apply {
            add(
                PeriodFilter(
                    resources.filterDateLabel(),
                    FilterManager.getInstance().periodFilters,
                    FilterManager.getInstance().periodIdSelected
                )
            )
            add(OrgUnitFilter(FilterManager.getInstance().observeOrgUnitFilters()))
            add(SyncStateFilter(emptyList()))
            if (!d2.programModule().programStages()
                    .byEnableUserAssignment().eq(true).blockingIsEmpty()
            ) {
                add(AssignedFilter(FilterManager.getInstance().assignedFilter))
            }
        }
    }

    private fun getTrackerFilters(program: Program): List<FilterItem> {
        return mutableListOf<FilterItem>().apply {
            val workingLists = d2.trackedEntityModule().trackedEntityInstanceFilters().byProgram()
                .eq(program.uid()).blockingGet().map {
                    TeiWorkingListItem(it.uid(), it.displayName() ?: "", null)
                }
            if (workingLists.isNotEmpty()) {
                add(WorkingListFilter(workingLists, null))
            }
            add(
                PeriodFilter(
                    resources.filterEventDateLabel(),
                    FilterManager.getInstance().periodFilters,
                    FilterManager.getInstance().periodIdSelected
                )
            )
            add(
                EnrollmentDateFilter(
                    program.enrollmentDateLabel() ?: resources.filterEnrollmentDateLabel(),
                    FilterManager.getInstance().enrollmentPeriodFilters,
                    FilterManager.getInstance().enrollmentPeriodIdSelected
                )
            )
            add(OrgUnitFilter(FilterManager.getInstance().observeOrgUnitFilters()))
            add(SyncStateFilter(emptyList()))
            add(EnrollmentStatusFilter(FilterManager.getInstance().enrollmentStatusFilters))
            add(EventStatusFilter(emptyList()))
            if (!d2.programModule().programStages().byProgramUid().eq(program.uid())
                    .byEnableUserAssignment().eq(true).blockingIsEmpty()
            ) {
                add(AssignedFilter(FilterManager.getInstance().assignedFilter))
            }
        }
    }

    private fun getEventFilters(program: Program): List<FilterItem> {
        return mutableListOf<FilterItem>().apply {
            val workingLists =
                d2.eventModule().eventFilters().byProgram().eq(program.uid()).blockingGet().map {
                    EventWorkingListItem(it.uid(), it.displayName() ?: "", false, null, null, null)
                }
            if (workingLists.isNotEmpty()) {
                add(WorkingListFilter(workingLists, null))
            }
            add(
                PeriodFilter(
                    resources.filterDateLabel(),
                    FilterManager.getInstance().periodFilters,
                    FilterManager.getInstance().periodIdSelected
                )
            )
            add(OrgUnitFilter(FilterManager.getInstance().observeOrgUnitFilters()))
            add(SyncStateFilter(emptyList()))
            add(EventStatusFilter(emptyList()))
            if (!d2.programModule().programStages().byProgramUid().eq(program.uid())
                    .byEnableUserAssignment().eq(true).blockingIsEmpty()
            ) {
                add(AssignedFilter(FilterManager.getInstance().assignedFilter))
            }
            val categoryCombo =
                d2.categoryModule().categoryCombos().uid(program.categoryComboUid()).blockingGet()
            if (categoryCombo.isDefault == false) {
                add(
                    CatOptionComboFilter(
                        categoryCombo,
                        d2.categoryModule().categoryOptionCombos().byCategoryComboUid()
                            .eq(categoryCombo.uid()).blockingGet(),
                        emptyList()
                    )
                )
            }
        }
    }
}
