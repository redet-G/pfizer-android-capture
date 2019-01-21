package org.dhis2.usescases.teiDashboard.eventDetail;

import androidx.databinding.BaseObservable;

import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageDataElementModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.Date;
import java.util.List;

/**
 * QUADRAM. Created by Cristian on 08/02/2018.
 */

public class EventDetailModel extends BaseObservable {

    private final List<ProgramStageDataElementModel> dataElemets;
    private final List<ProgramStageSectionModel> stageSections;
//    private final HashMap<String, List<ProgramStageDataElementModel>> fieldsElements;
    private final ProgramStageModel programStage;
    private final List<CategoryOptionComboModel> optionComboList;
    private final ProgramModel programModel;
    private final String catComboName;
    private EventModel eventModel;
    private List<TrackedEntityDataValueModel> dataValueModelList;
    private final OrganisationUnitModel orgUnit;

    EventDetailModel(EventModel eventModel, List<TrackedEntityDataValueModel> dataValueModelList,
                     List<ProgramStageSectionModel> programStageSectionModelList, List<ProgramStageDataElementModel> programStageDataElementModelList,
                     ProgramStageModel programStage, OrganisationUnitModel orgUnit, Pair<String, List<CategoryOptionComboModel>> optionComboList, ProgramModel programModel) {
        this.eventModel = eventModel;
        this.dataValueModelList = dataValueModelList;
        this.dataElemets = programStageDataElementModelList;
        this.stageSections = programStageSectionModelList;
        this.programStage = programStage;
//        fieldsElements = new HashMap<>();
        this.orgUnit = orgUnit;
        this.catComboName = optionComboList.val0();
        this.optionComboList = optionComboList.val1();
        this.programModel = programModel;

//        setUpFields();

    }

   /* private void setUpFields() {

        ArrayList<ProgramStageDataElementModel> sectionDataElements = new ArrayList<>();
        for (ProgramStageDataElementModel de : dataElemets) {
            if (de.programStageSection() == null)
                sectionDataElements.add(de);
        }
        fieldsElements.put("null", sectionDataElements);

        for (ProgramStageSectionModel section : stageSections) {
            sectionDataElements = new ArrayList<>();
            for (ProgramStageDataElementModel de : dataElemets)
                if (de.programStageSection() != null && de.programStageSection().equals(section.uid()))
                    sectionDataElements.add(de);
            fieldsElements.put(section.uid(), sectionDataElements);
        }
    }*/

    EventModel getEventModel() {
        return eventModel;
    }
/*
    List<ProgramStageSectionModel> getStageSections() {
        return stageSections;
    }

    List<ProgramStageDataElementModel> getDataElementsForSection(String sectionUid) {
        if (sectionUid != null)
            return fieldsElements.get(sectionUid);
        else
            return dataElemets;
    }

    String getValueForDE(String dataelementUid) {
        for (TrackedEntityDataValueModel trackedEntityDataValueModel : dataValueModelList) {
            if (trackedEntityDataValueModel.dataElement().equals(dataelementUid))
                return trackedEntityDataValueModel.value();
        }
        return null;
    }*/

    public ProgramStageModel getProgramStage() {
        return programStage;
    }

    public String getCatComboName() {
        return catComboName;
    }

    public Date orgUnitOpeningDate() {
        return orgUnit.openingDate();
    }

    public Date orgUnitClosingDate() {
        return orgUnit.closedDate();
    }

    public String getOrgUnitName() {
        return orgUnit.displayName();
    }

    public List<CategoryOptionComboModel> getOptionComboList() {
        return optionComboList;
    }

    public boolean hasExpired() {
        return eventModel.completedDate() != null && DateUtils.getInstance().hasExpired(eventModel, programModel.expiryDays(), programModel.completeEventsExpiryDays(), programModel.expiryPeriodType());
    }

    public String getEventCatComboOptionName() {
        String eventCatComboOptionName = null;

        for (CategoryOptionComboModel option : optionComboList) {
            if (option.uid().equals(eventModel.attributeOptionCombo()))
                eventCatComboOptionName = option.name();
        }

        return eventCatComboOptionName;
    }

    public ProgramModel getProgram() {
        return programModel;
    }
}
