package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data

import io.reactivex.Single
import org.dhis2.Bindings.primaryDate
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.tei_events.EventViewModel
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.tei_events.EventViewModelType
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

class TeiDataRepositoryImpl(
    private val d2: D2,
    private val programUid: String?,
    private val teiUid: String,
    private val enrollmentUid: String?
) : TeiDataRepository {

    override fun getTEIEnrollmentEvents(
        selectedStage: String?,
        groupedByStage: Boolean
    ): Single<List<EventViewModel>> {
        return if (groupedByStage) {
            getGroupedEvents(selectedStage)
        } else {
            getTimelineEvents()
        }
    }

    override fun getEnrollment(): Single<Enrollment> {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
    }

    override fun getEnrollmentProgram(): Single<Program> {
        return d2.programModule().programs().uid(programUid).get()
    }

    override fun getTrackedEntityInstance(): Single<TrackedEntityInstance> {
        return d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).get()
    }

    override fun enrollingOrgUnit(): Single<OrganisationUnit> {

        return if (programUid == null) {
            getTrackedEntityInstance()
                .map { it.organisationUnit() }
        } else {
            getEnrollment()
                .map { it.organisationUnit() }
        }
            .flatMap {
                d2.organisationUnitModule().organisationUnits().uid(it).get()
            }
    }

    private fun getGroupedEvents(
        selectedStage: String?
    ): Single<List<EventViewModel>> {
        val eventViewModels = mutableListOf<EventViewModel>()

        return d2.programModule().programStages()
            .byProgramUid().eq(programUid)
            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
            .get()
            .map { programStages ->
                programStages.forEach { programStage ->
                    val eventList = d2.eventModule().events()
                        .byEnrollmentUid().eq(enrollmentUid)
                        .byProgramStageUid().eq(programStage.uid())
                        .byDeleted().isFalse
                        .blockingGet()
                    eventList.sortWith(Comparator { event1, event2 ->
                        event2.primaryDate().compareTo(event1.primaryDate())
                    })

                    eventViewModels.add(
                        EventViewModel(
                            EventViewModelType.STAGE,
                            programStage,
                            null,
                            eventList.size,
                            if (eventList.isEmpty()) null else eventList[0].lastUpdated(),
                            true
                        )
                    )
                    if (selectedStage != null && selectedStage == programStage.uid()) {
                        checkEventStatus(eventList).forEach { event ->
                            eventViewModels.add(
                                EventViewModel(
                                    EventViewModelType.EVENT,
                                    programStage,
                                    event,
                                    0,
                                    null,
                                    true
                                )
                            )
                        }
                    }

                }
                eventViewModels
            }

    }

    private fun getTimelineEvents(
    ): Single<List<EventViewModel>> {
        val eventViewModels = mutableListOf<EventViewModel>()
        return d2.eventModule().events()
            .byEnrollmentUid().eq(enrollmentUid)
            .byDeleted().isFalse
            .get()
            .map { eventList ->
                eventList.sortWith(Comparator { event1, event2 ->
                    event2.primaryDate().compareTo(event1.primaryDate())
                })
                checkEventStatus(eventList).forEach { event ->
                    val stageUid = d2.programModule().programStages()
                        .uid(event.programStage())
                        .blockingGet()
                    eventViewModels.add(
                        EventViewModel(
                            EventViewModelType.EVENT,
                            stageUid,
                            event,
                            0,
                            null,
                            true
                        )
                    )
                }
                eventViewModels
            }

    }

    private fun checkEventStatus(events: List<Event>): List<Event> {
        return events.map { event ->
            if (event.status() == EventStatus.SCHEDULE &&
                event.dueDate()?.before(DateUtils.getInstance().today) == true
            ) {
                d2.eventModule().events().uid(event.uid()).setStatus(EventStatus.OVERDUE)
                d2.eventModule().events().uid(event.uid()).blockingGet()
            } else {
                event
            }
        }
    }
}