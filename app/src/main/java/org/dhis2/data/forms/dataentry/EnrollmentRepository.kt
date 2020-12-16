package org.dhis2.data.forms.dataentry

import androidx.annotation.VisibleForTesting
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.processors.FlowableProcessor
import java.util.ArrayList
import org.dhis2.Bindings.userFriendlyValue
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory
import org.dhis2.data.forms.dataentry.fields.RowAction
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel
import org.dhis2.data.forms.dataentry.fields.datetime.DateTimeViewModel
import org.dhis2.data.forms.dataentry.fields.optionset.OptionSetViewModel
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitViewModel
import org.dhis2.usescases.enrollment.EnrollmentActivity
import org.dhis2.utils.DateUtils
import org.dhis2.utils.DhisTextUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramSection
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import timber.log.Timber

class EnrollmentRepository(
    private val fieldFactory: FieldViewModelFactory,
    private val enrollmentUid: String,
    private val d2: D2,
    private val dhisEnrollmentUtils: DhisEnrollmentUtils,
    private val enrollmentMode: EnrollmentActivity.EnrollmentMode,
    private val enrollmentDataSectionLabel: String,
    private val singleSectionLabel: String,
    private val enrollmentOrgUnitLabel: String,
    private val teiCoordinatesLabel: String,
    private val enrollmentCoordinatesLabel: String,
    private val reservedValuesWarning: String,
    private val enrollmentDateDefaultLabel: String,
    private val incidentDateDefaultLabel: String,
    private val onRowActionProccesor: FlowableProcessor<RowAction>,
    private val focusProcessor: FlowableProcessor<Pair<String, Boolean>>
) : DataEntryRepository {

    private val enrollmentRepository: EnrollmentObjectRepository =
        d2.enrollmentModule().enrollments().uid(enrollmentUid)

    private val canEditAttributes: Boolean

    init {
        canEditAttributes = getAttributeAccess()
    }

    private fun getAttributeAccess(): Boolean {
        val selectedProgram = d2.programModule().programs().uid(
            enrollmentRepository.blockingGet().program()
        ).blockingGet()
        val programAccess =
            selectedProgram.access().data().write() != null && selectedProgram.access().data()
                .write()
        val teTypeAccess = d2.trackedEntityModule().trackedEntityTypes().uid(
            selectedProgram.trackedEntityType()?.uid()
        ).blockingGet().access().data().write()
        return programAccess && teTypeAccess
    }

    override fun enrollmentSectionUids(): Flowable<MutableList<String>> {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
            .flatMap { enrollment ->
                d2.programModule().programSections().byProgramUid().eq(enrollment.program()).get()
            }.map { programSections ->
                val sectionUids = mutableListOf(ENROLLMENT_DATA_SECTION_UID)
                sectionUids.addAll(programSections.map { it.uid() })
                sectionUids
            }.toFlowable()
    }

    override fun list(): Flowable<MutableList<FieldViewModel>> {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
            .flatMap { enrollment ->
                d2.programModule().programs().uid(enrollment.program()).get()
            }
            .flatMap { program ->
                d2.programModule().programSections().byProgramUid().eq(program.uid())
                    .withAttributes().get()
                    .flatMap { programSections ->
                        if (programSections.isEmpty()) {
                            getFieldsForSingleSection(program.uid())
                                .map { singleSectionList ->
                                    val list = getSingleSectionList()
                                    list.addAll(singleSectionList)
                                    list
                                }
                        } else {
                            getFieldsForMultipleSections(
                                programSections,
                                program.uid()
                            )
                        }
                    }.map { list ->
                        val fields = getEnrollmentData(program)
                        fields.addAll(list)
                        fields.add(fieldFactory.createClosingSection())
                        fields
                    }
            }.toFlowable()
    }

    @VisibleForTesting
    fun getFieldsForSingleSection(programUid: String): Single<List<FieldViewModel>> {
        return d2.programModule().programTrackedEntityAttributes().withRenderType()
            .byProgram().eq(programUid).orderBySortOrder(RepositoryScope.OrderByDirection.ASC).get()
            .toFlowable()
            .flatMapIterable { programTrackedEntityAttributes -> programTrackedEntityAttributes }
            .map { transform(it) }
            .toList()
            .map {
                val finalFieldList = mutableListOf<FieldViewModel>()
                for (field in it) {
                    if (field is OptionSetViewModel) {
                        val options =
                            d2.optionModule().options().byOptionSetUid().eq(field.optionSet())
                                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                                .blockingGet()
                        finalFieldList.add(field.withOptions(options))
                    } else {
                        finalFieldList.add(field)
                    }
                }
                finalFieldList
            }
    }

    @VisibleForTesting
    fun getFieldsForMultipleSections(
        programSections: List<ProgramSection>,
        programUid: String
    ): Single<List<FieldViewModel>> {
        val fields = ArrayList<FieldViewModel>()
        for (section in programSections) {
            fields.add(transformSection(section))
            for (attribute in section.attributes()!!) {
                val programTrackedEntityAttribute =
                    d2.programModule().programTrackedEntityAttributes()
                        .withRenderType()
                        .byProgram().eq(programUid)
                        .byTrackedEntityAttribute().eq(attribute.uid())
                        .one().blockingGet()
                fields.add(transform(programTrackedEntityAttribute!!, section.uid()))
            }
        }
        return Single.just(fields)
    }

    override fun getOrgUnits(): Observable<List<OrganisationUnit>> {
        return d2.organisationUnitModule().organisationUnits()
            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).get()
            .toObservable()
    }

    private fun transform(
        programTrackedEntityAttribute: ProgramTrackedEntityAttribute,
        sectionUid: String? = SINGLE_SECTION_UID
    ): FieldViewModel {
        val attribute = d2.trackedEntityModule().trackedEntityAttributes()
            .uid(programTrackedEntityAttribute.trackedEntityAttribute()!!.uid())
            .blockingGet()
        val attrValueRepository = d2.trackedEntityModule().trackedEntityAttributeValues()
            .value(
                attribute!!.uid(),
                enrollmentRepository.blockingGet()!!.trackedEntityInstance()
            )

        val valueType = attribute.valueType()
        var mandatory = programTrackedEntityAttribute.mandatory()!!
        val optionSet =
            if (attribute.optionSet() != null) attribute.optionSet()!!.uid() else null
        val generated = attribute.generated()!!

        val orgUnitUid = enrollmentRepository.blockingGet()!!.organisationUnit()

        var dataValue: String? = if (attrValueRepository.blockingExists()) {
            attrValueRepository.blockingGet().userFriendlyValue(d2)
        } else {
            null
        }

        var optionCount = 0
        if (!DhisTextUtils.isEmpty(optionSet)) {
            optionCount =
                d2.optionModule().options().byOptionSetUid().eq(optionSet).blockingCount()
        }

        var warning: String? = null

        if (generated && dataValue == null) {
            mandatory = true
            val result = handleAutogeneratedValue(attribute, orgUnitUid!!)
            dataValue = result.first
            warning = result.second
            if (!DhisTextUtils.isEmpty(dataValue)) {
                attrValueRepository.blockingSet(dataValue)
            }
        }

        val conflicts = d2.importModule().trackerImportConflicts()
            .byEnrollmentUid().eq(enrollmentUid)
            .blockingGet()

        val conflict = conflicts
            .find { it.trackedEntityAttribute() == attribute.uid() }

        val error = conflict?.let {
            if (it.value() == dataValue) {
                it.displayDescription()
            } else {
                null
            }
        }

        if (valueType == ValueType.ORGANISATION_UNIT && !DhisTextUtils.isEmpty(dataValue)) {
            dataValue = attrValueRepository.blockingGet().value() + "_ou_" + dataValue
        }

        val fieldViewModel = fieldFactory.create(
            attribute.uid(),
            attribute.displayName() ?: "",
            valueType!!,
            mandatory,
            optionSet,
            dataValue,
            sectionUid,
            programTrackedEntityAttribute.allowFutureDate() ?: false,
            !generated && canEditAttributes,
            null,
            attribute.displayDescription(),
            programTrackedEntityAttribute.renderType()?.mobile(),
            optionCount,
            attribute.style(),
            attribute.fieldMask(),
            onRowActionProccesor,
            null,
            focusProcessor
        )

        return if (!error.isNullOrEmpty()) {
            fieldViewModel.withError(error)
        } else if (warning != null) {
            fieldViewModel.withWarning(warning)
        } else {
            fieldViewModel
        }
    }

    private fun handleAutogeneratedValue(
        attr: TrackedEntityAttribute,
        orgUnitUid: String
    ): Pair<String?, String?> {
        var warning: String? = null
        var dataValue: String? = null
        try {
            val teiUid = enrollmentRepository.blockingGet()!!.trackedEntityInstance()

            if (teiUid != null) {
                try {
                    dataValue = d2.trackedEntityModule().reservedValueManager()
                        .blockingGetValue(attr.uid(), orgUnitUid)
                } catch (e: Exception) {
                    dataValue = null
                    warning = reservedValuesWarning
                }

                if (attr.valueType() == ValueType.NUMBER) {
                    while (dataValue!!.startsWith("0")) {
                        dataValue = d2.trackedEntityModule().reservedValueManager()
                            .blockingGetValue(attr.uid(), orgUnitUid)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            warning = reservedValuesWarning
        }

        return Pair(dataValue, warning)
    }

    private fun getSingleSectionList(): MutableList<FieldViewModel> {
        val tei = d2.trackedEntityModule().trackedEntityInstances()
            .uid(enrollmentRepository.blockingGet().trackedEntityInstance())
            .blockingGet()
        val teiType = d2.trackedEntityModule().trackedEntityTypes()
            .uid(tei.trackedEntityType()).blockingGet()
        return mutableListOf(
            fieldFactory.createSingleSection(
                String.format(
                    singleSectionLabel,
                    teiType.displayName()
                )
            )
        )
    }

    private fun getEnrollmentData(program: Program): MutableList<FieldViewModel> {
        val enrollmentDataList = ArrayList<FieldViewModel>()
        enrollmentDataList.add(getEnrollmentDataSection(program.description()))

        enrollmentDataList.add(
            getEnrollmentDateField(
                program.enrollmentDateLabel() ?: enrollmentDateDefaultLabel,
                program.selectEnrollmentDatesInFuture()
            )
        )
        if (program.displayIncidentDate()!!) {
            enrollmentDataList.add(
                getIncidentDateField(
                    program.incidentDateLabel() ?: incidentDateDefaultLabel,
                    program.selectIncidentDatesInFuture()
                )
            )
        }
        val orgUnits = d2.organisationUnitModule().organisationUnits()
            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
            .byProgramUids(listOf(enrollmentRepository.blockingGet().program())).blockingCount()
        enrollmentDataList.add(
            getOrgUnitField(enrollmentMode == EnrollmentActivity.EnrollmentMode.NEW && orgUnits > 1)
        )

        val teiType =
            d2.trackedEntityModule().trackedEntityTypes()
                .uid(program.trackedEntityType()!!.uid())
                .blockingGet()
        if (teiType!!.featureType() != null && teiType.featureType() != FeatureType.NONE) {
            enrollmentDataList.add(getTeiCoordinatesField(teiType.featureType()))
        }

        if (program.featureType() != null && program.featureType() != FeatureType.NONE) {
            enrollmentDataList.add(
                getEnrollmentCoordinatesField(
                    program.featureType()
                )
            )
        }

        return enrollmentDataList
    }

    private fun getEnrollmentDataSection(description: String?): FieldViewModel {
        return fieldFactory.createSection(
            ENROLLMENT_DATA_SECTION_UID,
            enrollmentDataSectionLabel,
            description,
            false,
            0,
            0,
            ProgramStageSectionRenderingType.LISTING.name
        )
    }

    private fun getEnrollmentDateField(
        enrollmentDateLabel: String,
        allowFutureDates: Boolean?
    ): FieldViewModel {
        return DateTimeViewModel.create(
            ENROLLMENT_DATE_UID,
            enrollmentDateLabel,
            true,
            ValueType.DATE,
            when (val date = enrollmentRepository.blockingGet()!!.enrollmentDate()) {
                null -> null
                else -> DateUtils.databaseDateFormat().format(date)
            },
            ENROLLMENT_DATA_SECTION_UID,
            allowFutureDates,
            true,
            null,
            ObjectStyle.builder().build(),
            true,
            false,
            onRowActionProccesor,
            focusProcessor
        )
    }

    private fun getIncidentDateField(
        incidentDateLabel: String,
        allowFutureDates: Boolean?
    ): FieldViewModel {
        return DateTimeViewModel.create(
            INCIDENT_DATE_UID,
            incidentDateLabel,
            true,
            ValueType.DATE,
            DateUtils.databaseDateFormat().format(
                enrollmentRepository.blockingGet()!!.incidentDate()
            ),
            ENROLLMENT_DATA_SECTION_UID,
            allowFutureDates,
            true,
            null,
            ObjectStyle.builder().build(),
            true,
            false,
            onRowActionProccesor,
            focusProcessor
        )
    }

    private fun getOrgUnitField(editable: Boolean): FieldViewModel {
        return OrgUnitViewModel.create(
            ORG_UNIT_UID,
            enrollmentOrgUnitLabel,
            true,
            getOrgUnitValue(enrollmentRepository.blockingGet()!!.organisationUnit()),
            ENROLLMENT_DATA_SECTION_UID,
            editable,
            null,
            ObjectStyle.builder().build(),
            true,
            ProgramStageSectionRenderingType.LISTING.name
        )
    }

    private fun getTeiCoordinatesField(
        featureType: FeatureType?
    ): FieldViewModel {
        val tei = d2.trackedEntityModule().trackedEntityInstances()
            .uid(
                enrollmentRepository.blockingGet()!!.trackedEntityInstance()
            ).blockingGet()
        val teiType = d2.trackedEntityModule().trackedEntityTypes()
            .uid(tei.trackedEntityType()).blockingGet()
        return CoordinateViewModel.create(
            TEI_COORDINATES_UID,
            "$teiCoordinatesLabel ${teiType.displayName()}",
            false,
            if (tei!!.geometry() != null) tei.geometry()!!.coordinates() else null,
            ENROLLMENT_DATA_SECTION_UID,
            true, null,
            ObjectStyle.builder().build(),
            featureType,
            true,
            false
        )
    }

    private fun getEnrollmentCoordinatesField(
        featureType: FeatureType?
    ): FieldViewModel {
        return CoordinateViewModel.create(
            ENROLLMENT_COORDINATES_UID,
            enrollmentCoordinatesLabel,
            false,
            if (enrollmentRepository.blockingGet()!!.geometry() != null) {
                enrollmentRepository.blockingGet()!!.geometry()!!.coordinates()
            } else {
                null
            },
            ENROLLMENT_DATA_SECTION_UID,
            true, null,
            ObjectStyle.builder().build(),
            featureType,
            true,
            false
        )
    }

    private fun transformSection(programSection: ProgramSection): FieldViewModel {
        return fieldFactory.createSection(
            programSection.uid(),
            programSection.displayName(),
            programSection.description(),
            false,
            0,
            0,
            ProgramStageSectionRenderingType.LISTING.name
        )
    }

    private fun getOrgUnitValue(currentValueUid: String?): String? {
        return if (currentValueUid != null) {
            currentValueUid + "_ou_" + d2.organisationUnitModule().organisationUnits().uid(
                currentValueUid
            ).blockingGet()!!.displayName()
        } else {
            null
        }
    }

    fun hasEventsGeneratedByEnrollmentDate(): Boolean {
        return dhisEnrollmentUtils.hasEventsGeneratedByEnrollmentDate(
            enrollmentRepository.blockingGet()
        )
    }

    fun hasEventsGeneratedByIncidentDate(): Boolean {
        return dhisEnrollmentUtils.hasEventsGeneratedByIncidentDate(
            enrollmentRepository.blockingGet()
        )
    }

    companion object {

        const val ENROLLMENT_DATA_SECTION_UID = "ENROLLMENT_DATA_SECTION_UID"
        const val SINGLE_SECTION_UID = "SINGLE_SECTION_UID"
        const val ENROLLMENT_DATE_UID = "ENROLLMENT_DATE_UID"
        const val INCIDENT_DATE_UID = "INCIDENT_DATE_UID"
        const val ORG_UNIT_UID = "ORG_UNIT_UID"
        const val TEI_COORDINATES_UID = "TEI_COORDINATES_UID"
        const val ENROLLMENT_COORDINATES_UID = "ENROLLMENT_COORDINATES_UID"
    }
}
