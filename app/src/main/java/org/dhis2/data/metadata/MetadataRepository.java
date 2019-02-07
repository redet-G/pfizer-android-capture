package org.dhis2.data.metadata;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.ErrorMessageModel;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.relationship.RelationshipTypeModel;
import org.hisp.dhis.android.core.resource.ResourceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeModel;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;


/**
 * QUADRAM. Created by ppajuelo on 04/12/2017.
 */

public interface MetadataRepository {

    /*PROGRAMS*/
    Observable<List<ProgramModel>> getProgramModelFromEnrollmentList(List<Enrollment> enrollments);

    Observable<List<ProgramModel>> getTeiActivePrograms(String teiUid);

    Observable<ProgramModel> getProgramWithId(String programUid);

    /*TRACKED ENTITY*/

    Observable<TrackedEntityTypeModel> getTrackedEntity(String trackedEntityUid);

    Observable<TrackedEntityInstanceModel> getTrackedEntityInstance(String teiUid);

    Observable<List<TrackedEntityInstanceModel>> getTrackedEntityInstances(String programUid);


    /*CATEGORY OPTION*/

    Observable<CategoryOptionModel> getCategoryOptionWithId(String categoryOptionId);

    Observable<String> getDefaultCategoryOptionId();


    /*CATEGORY OPTION COMBO*/

    Observable<CategoryOptionComboModel> getCategoryOptionComboWithId(String categoryOptionComboId);

    Observable<List<CategoryOptionComboModel>> getCategoryComboOptions(String categoryComboId);

    void saveCatOption(String eventUid, CategoryOptionComboModel selectedOption);

    /*CATEGORY COMBO*/

    Observable<CategoryComboModel> getCategoryComboWithId(String categoryComboId);

    /*ORG UNIT*/

    Observable<OrganisationUnitModel> getOrganisationUnit(String orgUnitUid);

    Observable<OrganisationUnitModel> getTeiOrgUnit(String teiUid);

    Observable<OrganisationUnitModel> getTeiOrgUnit(@NonNull String teiUid, @Nullable String programUid);

    Observable<List<OrganisationUnitModel>> getOrgUnitForOpenAndClosedDate(String currentDate);

    /*PROGRAM TRACKED ENTITY ATTRIBUTE*/

    Observable<List<ProgramTrackedEntityAttributeModel>> getProgramTrackedEntityAttributes(String programUid);

    Observable<List<TrackedEntityAttributeValueModel>> getTEIAttributeValues(String teiUid);

    Observable<List<TrackedEntityAttributeValueModel>> getTEIAttributeValues(String programUid, String teiUid);

    Observable<TrackedEntityAttributeModel> getTrackedEntityAttribute(String teAttribute);


    /*RELATIONSHIPS*/

    Observable<RelationshipTypeModel> getRelationshipType(String programUid);

    Observable<List<RelationshipTypeModel>> getRelationshipTypeList();

    //ProgramStage

    @NonNull
    Observable<ProgramStageModel> programStage(String programStageId);

    Observable<DataElementModel> getDataElement(String dataElementUid);

    /*ENROLLMENTS*/
    Observable<List<EnrollmentModel>> getTEIEnrollments(String teiUid);

    Observable<List<ProgramModel>> getTEIProgramsToEnroll(String teiUid);

    Observable<EventModel> getEnrollmentLastEvent(String enrollmentUid);

    Observable<List<EventModel>> getEnrollmentEvents(String enrollmentUid);


    /*EVENTS*/
    Observable<Integer> getProgramStageDataElementCount(String programStageId);

    Observable<Integer> getTrackEntityDataValueCount(String programStageId);

    Observable<ProgramModel> getExpiryDateFromEvent(String eventUid);
    Observable<Boolean> isCompletedEventExpired(String eventUid);


    /*OPTION SET*/
    List<OptionModel> optionSet(String optionSetId);

    /*RESOURCE*/
    Observable<ResourceModel> getLastSync(ResourceModel.Type resourceType);

    /*SETINGS*/
    Observable<Pair<String, Integer>> getTheme();

    Observable<ObjectStyleModel> getObjectStyle(String uid);

    Observable<List<OrganisationUnitModel>> getOrganisationUnits();

    Observable<List<OrganisationUnitModel>> getSearchOrganisationUnits();

    Observable<List<Pair<String, String>>> getReserveUids();

    Observable<Boolean> hasOverdue(@Nullable String programUid, @NonNull String teiUid);


    @NonNull
    Observable<List<ResourceModel>> syncState(ProgramModel program);

    Flowable<Pair<Integer, Integer>> getDownloadedData();

    Flowable<Boolean> validateCredentials(String serverUrl, String username, String password);

    Observable<String> getServerUrl();

    void createErrorTable();

    Observable<List<ErrorMessageModel>> getSyncErrors();

    void deleteErrorLogs();

    Observable<Integer> getOrgUnitsForDataElementsCount();

    Observable<List<String>> searchOptions(String text, String idOptionSet, int page);

    int optionSetSize(String optionSet);
}