package org.dhis2.utils.filters;

import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.dhis2.R;
import org.dhis2.utils.filters.cat_opt_comb.CatOptCombFilterAdapter;
import org.dhis2.utils.filters.sorting.SortingItem;
import org.dhis2.utils.filters.sorting.SortingStatus;
import org.dhis2.utils.filters.workingLists.EventWorkingListItem;
import org.dhis2.utils.filters.workingLists.TeiWorkingListItem;
import org.dhis2.utils.filters.workingLists.WorkingListItem;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.DatePeriod;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import kotlin.Pair;

public class FilterManager implements Serializable {

    public static final int OU_TREE = 1986;

    public void publishData() {
        filterProcessor.onNext(this);
    }

    public void setCatComboAdapter(CatOptCombFilterAdapter adapter) {
        this.catComboAdapter = adapter;
    }

    public enum PeriodRequest {
        FROM_TO, OTHER
    }

    private int periodIdSelected = R.id.anytime;
    private int enrollmentPeriodIdSelected;
    private int totalSearchTeiFilter = 0;

    private CatOptCombFilterAdapter catComboAdapter;

    private List<OrganisationUnit> ouFilters;
    private MutableLiveData<List<OrganisationUnit>> liveDataOUFilter = new MutableLiveData<>();
    private List<State> stateFilters;
    private ObservableField<List<State>> observableStates = new ObservableField<>();
    private List<DatePeriod> periodFilters;
    private ObservableField<List<DatePeriod>> observablePeriodFilters = new ObservableField<>();
    private List<DatePeriod> enrollmentPeriodFilters;
    private List<CategoryOptionCombo> catOptComboFilters;
    private List<EventStatus> eventStatusFilters;
    private ObservableField<List<EventStatus>> observableEventStatus = new ObservableField<>();
    private List<EnrollmentStatus> enrollmentStatusFilters;
    private ObservableField<EnrollmentStatus> observableEnrollmentStatus = new ObservableField<>();
    private boolean assignedFilter;
    private ObservableField<Boolean> observableAssignedToMe = new ObservableField<>();
    private SortingItem sortingItem;

    private ArrayList<Filters> unsupportedFilters = new ArrayList<>();

    private ObservableField<Integer> ouFiltersApplied;
    private ObservableField<Integer> stateFiltersApplied;
    private ObservableField<Integer> periodFiltersApplied;
    private ObservableField<Integer> enrollmentPeriodFiltersApplied;
    private ObservableField<Integer> catOptCombFiltersApplied;
    private ObservableField<Integer> eventStatusFiltersApplied;
    private ObservableField<Integer> enrollmentStatusFiltersApplied;
    private ObservableField<Integer> assignedToMeApplied;

    private FlowableProcessor<FilterManager> filterProcessor;
    private FlowableProcessor<Boolean> ouTreeProcessor;
    private FlowableProcessor<Pair<PeriodRequest, Filters>> periodRequestProcessor;
    private FlowableProcessor<String> catOptComboRequestProcessor;

    private WorkingListItem currentWorkingList;
    private ObservableField<WorkingListItem> workingList;

    private static FilterManager instance;

    public static FilterManager getInstance() {
        if (instance == null)
            instance = new FilterManager();
        return instance;
    }

    private FilterManager() {
        reset();
    }

    public static void clearAll() {
        instance = null;
    }

    public void reset() {
        catComboAdapter = null;

        ouFilters = new ArrayList<>();
        stateFilters = new ArrayList<>();
        periodFilters = new ArrayList<>();
        enrollmentPeriodFilters = new ArrayList<>();
        catOptComboFilters = new ArrayList<>();
        eventStatusFilters = new ArrayList<>();
        enrollmentStatusFilters = new ArrayList<>();
        assignedFilter = false;
        sortingItem = null;

        ouFiltersApplied = new ObservableField<>(0);
        stateFiltersApplied = new ObservableField<>(0);
        periodFiltersApplied = new ObservableField<>(0);
        enrollmentPeriodFiltersApplied = new ObservableField<>(0);
        catOptCombFiltersApplied = new ObservableField<>(0);
        eventStatusFiltersApplied = new ObservableField<>(0);
        enrollmentStatusFiltersApplied = new ObservableField<>(0);
        assignedToMeApplied = new ObservableField<>(0);

        workingList = new ObservableField<>(null);

        filterProcessor = PublishProcessor.create();
        ouTreeProcessor = PublishProcessor.create();
        periodRequestProcessor = PublishProcessor.create();
        catOptComboRequestProcessor = PublishProcessor.create();
    }

    public FilterManager copy() {
        FilterManager copy = new FilterManager();
        copy.ouFilters = new ArrayList<>(getOrgUnitFilters());
        copy.stateFilters = new ArrayList<>(getStateFilters());
        copy.periodFilters = new ArrayList<>(getPeriodFilters());
        copy.enrollmentPeriodFilters = new ArrayList<>(getEnrollmentPeriodFilters());
        copy.catOptComboFilters = new ArrayList<>(getCatOptComboFilters());
        copy.eventStatusFilters = new ArrayList<>(getEventStatusFilters());
        copy.enrollmentStatusFilters = new ArrayList<>(getEnrollmentStatusFilters());
        copy.assignedFilter = getAssignedFilter();
        copy.sortingItem = getSortingItem();
        return copy;
    }

    public boolean sameFilters(FilterManager filterManager) {
        return Objects.equals(filterManager.ouFilters, this.ouFilters) &&
                Objects.equals(filterManager.stateFilters, this.stateFilters) &&
                Objects.equals(filterManager.periodFilters, this.periodFilters) &&
                Objects.equals(filterManager.enrollmentPeriodFilters, this.enrollmentPeriodFilters) &&
                Objects.equals(filterManager.catOptComboFilters, this.catOptComboFilters) &&
                Objects.equals(filterManager.eventStatusFilters, this.eventStatusFilters) &&
                Objects.equals(filterManager.enrollmentStatusFilters, this.enrollmentStatusFilters) &&
                filterManager.assignedFilter == this.assignedFilter &&
                Objects.equals(filterManager.sortingItem, this.sortingItem);
    }

    public void setPeriodIdSelected(int selected) {
        this.periodIdSelected = selected;
    }

    public void setEnrollmentPeriodIdSelected(int selected) {
        this.enrollmentPeriodIdSelected = selected;
    }

    public int getPeriodIdSelected() {
        return this.periodIdSelected;
    }

    public int getEnrollmentPeriodIdSelected() {
        return this.enrollmentPeriodIdSelected;
    }

//    region STATE FILTERS

    public void addState(boolean remove, State... states) {
        for (State stateToAdd : states) {
            if (remove)
                stateFilters.remove(stateToAdd);
            else if (!stateFilters.contains(stateToAdd))
                stateFilters.add(stateToAdd);
        }
        observableStates.set(stateFilters);

        boolean hasNotSyncedState = stateFilters.contains(State.TO_POST) &&
                stateFilters.contains(State.TO_UPDATE) &&
                stateFilters.contains(State.UPLOADING);
        boolean hasErrorState = stateFilters.contains(State.ERROR) &&
                stateFilters.contains(State.WARNING);
        boolean hasSmsState = stateFilters.contains(State.SENT_VIA_SMS) &&
                stateFilters.contains(State.SYNCED_VIA_SMS);
        int stateFiltersCount = stateFilters.size();
        if (hasNotSyncedState) {
            stateFiltersCount = stateFiltersCount - 2;
        }
        if (hasErrorState) {
            stateFiltersCount = stateFiltersCount - 1;
        }
        if (hasSmsState) {
            stateFiltersCount = stateFiltersCount - 1;
        }

        stateFiltersApplied.set(stateFiltersCount);
        filterProcessor.onNext(this);
    }

//    endregion

    public void addEventStatus(boolean remove, EventStatus... status) {
        for (EventStatus eventStatus : status) {
            if (remove)
                eventStatusFilters.remove(eventStatus);
            else if (!eventStatusFilters.contains(eventStatus))
                eventStatusFilters.add(eventStatus);
        }
        observableEventStatus.set(eventStatusFilters);
        if (eventStatusFilters.contains(EventStatus.ACTIVE)) {
            eventStatusFiltersApplied.set(eventStatusFilters.size() - 1);
        } else {
            eventStatusFiltersApplied.set(eventStatusFilters.size());
        }
        filterProcessor.onNext(this);
    }

    public void addEnrollmentStatus(boolean remove, EnrollmentStatus enrollmentStatus) {
        if (remove) {
            enrollmentStatusFilters.remove(enrollmentStatus);
        } else {
            enrollmentStatusFilters.clear();
            enrollmentStatusFilters.add(enrollmentStatus);
            observableEnrollmentStatus.set(enrollmentStatus);
        }
        enrollmentStatusFiltersApplied.set(enrollmentStatusFilters.size());
        if (!workingListActive())
            filterProcessor.onNext(this);
    }

    public void addPeriod(List<DatePeriod> datePeriod) {
        this.periodFilters = datePeriod;
        observablePeriodFilters.set(datePeriod);
        periodFiltersApplied.set(datePeriod != null && !datePeriod.isEmpty() ? 1 : 0);
        filterProcessor.onNext(this);
    }

    public void addEnrollmentPeriod(List<DatePeriod> datePeriod) {
        this.enrollmentPeriodFilters = datePeriod;

        enrollmentPeriodFiltersApplied.set(datePeriod != null && !datePeriod.isEmpty() ? 1 : 0);
        filterProcessor.onNext(this);
    }

    public void addOrgUnit(OrganisationUnit ou) {

        if (ouFilters.contains(ou))
            ouFilters.remove(ou);
        else
            ouFilters.add(ou);

        liveDataOUFilter.setValue(ouFilters);
        ouFiltersApplied.set(ouFilters.size());
        filterProcessor.onNext(this);
    }

    public void addCatOptCombo(CategoryOptionCombo catOptCombo) {
        if (catOptComboFilters.contains(catOptCombo))
            catOptComboFilters.remove(catOptCombo);
        else
            catOptComboFilters.add(catOptCombo);

        if (catComboAdapter != null) {
            catComboAdapter.notifyDataSetChanged();
        }
        catOptCombFiltersApplied.set(catOptComboFilters.size());
        filterProcessor.onNext(this);
    }


    public ObservableField<Integer> observeField(Filters filter) {
        switch (filter) {
            case ORG_UNIT:
                return ouFiltersApplied;
            case SYNC_STATE:
                return stateFiltersApplied;
            case PERIOD:
                return periodFiltersApplied;
            case ENROLLMENT_DATE:
                return enrollmentPeriodFiltersApplied;
            case CAT_OPT_COMB:
                return catOptCombFiltersApplied;
            case EVENT_STATUS:
                return eventStatusFiltersApplied;
            case ENROLLMENT_STATUS:
                return enrollmentStatusFiltersApplied;
            case ASSIGNED_TO_ME:
                return assignedToMeApplied;
            default:
                return new ObservableField<>(0);
        }
    }

    public ObservableField<WorkingListItem> observeWorkingListFilter() {
        return workingList;
    }

    public FlowableProcessor<Boolean> getOuTreeProcessor() {
        return ouTreeProcessor;
    }

    public Flowable<FilterManager> asFlowable() {
        return filterProcessor;
    }

    public FlowableProcessor<Pair<PeriodRequest, Filters>> getPeriodRequest() {
        return periodRequestProcessor;
    }

    public FlowableProcessor<String> getCatComboRequest() {
        return catOptComboRequestProcessor;
    }

    public Flowable<Boolean> ouTreeFlowable() {
        return ouTreeProcessor;
    }

    public void setUnsupportedFilters(Filters... unsupported) {
        this.unsupportedFilters.addAll(Arrays.asList(unsupported));
    }

    public void clearUnsupportedFilters() {
        this.unsupportedFilters.clear();
    }

    public int getTotalFilters() {
        int ouIsApplying = ouFilters.isEmpty() ? 0 : 1;
        int stateIsApplying = stateFilters.isEmpty() ? 0 : 1;
        int periodIsApplying = periodFilters == null || periodFilters.isEmpty() ? 0 : 1;
        int enrollmentPeriodIsApplying = unsupportedFilters.contains(Filters.ENROLLMENT_DATE) || enrollmentPeriodFilters == null || enrollmentPeriodFilters.isEmpty() ? 0 : 1;
        int eventStatusApplying = eventStatusFilters.isEmpty() ? 0 : 1;
        int enrollmentStatusApplying = unsupportedFilters.contains(Filters.ENROLLMENT_STATUS) || enrollmentStatusFilters.isEmpty() ? 0 : 1;
        int catComboApplying = catOptComboFilters.isEmpty() ? 0 : 1;
        int assignedApplying = assignedFilter ? 1 : 0;
        int sortingIsActive = sortingItem != null ? 1 : 0;
        return ouIsApplying + stateIsApplying + periodIsApplying +
                eventStatusApplying + catComboApplying +
                assignedApplying + enrollmentPeriodIsApplying + enrollmentStatusApplying +
                sortingIsActive;
    }

    public List<DatePeriod> getPeriodFilters() {
        return periodFilters != null ? periodFilters : new ArrayList<>();
    }

    public ObservableField<List<DatePeriod>> observePeriodFilter() {
        return observablePeriodFilters;
    }

    public List<DatePeriod> getEnrollmentPeriodFilters() {
        return enrollmentPeriodFilters != null ? enrollmentPeriodFilters : new ArrayList<>();
    }

    public List<OrganisationUnit> getOrgUnitFilters() {
        return ouFilters;
    }

    public LiveData<List<OrganisationUnit>> observeOrgUnitFilters() {
        return liveDataOUFilter;
    }

    public List<CategoryOptionCombo> getCatOptComboFilters() {
        return catOptComboFilters;
    }

    public List<String> getOrgUnitUidsFilters() {
        return UidsHelper.getUidsList(ouFilters);
    }

    public List<State> getStateFilters() {
        return stateFilters;
    }

    public ObservableField<List<State>> observeSyncState() {
        return observableStates;
    }

    public List<EventStatus> getEventStatusFilters() {
        return eventStatusFilters;
    }

    public ObservableField<List<EventStatus>> observeEventStatus() {
        return observableEventStatus;
    }

    public List<EnrollmentStatus> getEnrollmentStatusFilters() {
        return enrollmentStatusFilters;
    }

    public ObservableField<EnrollmentStatus> observeEnrollmentStatus() {
        return observableEnrollmentStatus;
    }

    public void addPeriodRequest(PeriodRequest periodRequest, Filters filter) {
        periodRequestProcessor.onNext(new Pair<>(periodRequest, filter));
    }

    public void addCatOptComboRequest(String catOptComboUid) {
        catOptComboRequestProcessor.onNext(catOptComboUid);
    }

    public void removeAll() {
        ouFilters = new ArrayList<>();
        liveDataOUFilter.setValue(ouFilters);
        ouFiltersApplied.set(ouFilters.size());
        filterProcessor.onNext(this);
    }

    public void addIfCan(OrganisationUnit content, boolean b) {
        if (!b) {
            if (ouFilters.contains(content)) {
                ouFilters.remove(content);
            }
        } else {
            if (ouFilters.contains(content)) {
                ouFilters.remove(content);
            }
            ouFilters.add(content);
        }
        liveDataOUFilter.setValue(ouFilters);
        ouFiltersApplied.set(ouFilters.size());
        filterProcessor.onNext(this);
    }

    public boolean exist(OrganisationUnit content) {
        return ouFilters.contains(content);
    }

    public void clearCatOptCombo() {
        catOptComboFilters.clear();
        catOptCombFiltersApplied.set(catOptComboFilters.size());
        filterProcessor.onNext(this);
    }

    public void clearEventStatus() {
        eventStatusFilters.clear();
        eventStatusFiltersApplied.set(eventStatusFilters.size());
        observableEventStatus.set(eventStatusFilters);
        filterProcessor.onNext(this);
    }

    public void clearEnrollmentStatus() {
        enrollmentStatusFilters.clear();
        observableEnrollmentStatus.set(null);
        enrollmentStatusFiltersApplied.set(enrollmentStatusFilters.size());
        filterProcessor.onNext(this);
    }

    public void clearAssignToMe() {
        if (assignedFilter) {
            assignedFilter = false;
            observableAssignedToMe.set(false);
            assignedToMeApplied.set(0);
            filterProcessor.onNext(this);
        }
    }

    public void clearEnrollmentDate() {
        if (enrollmentPeriodFilters != null) {
            enrollmentPeriodFilters.clear();
        }
        enrollmentPeriodIdSelected = 0;
        enrollmentPeriodFiltersApplied.set(enrollmentPeriodFilters == null ? 0 : enrollmentPeriodFilters.size());
        filterProcessor.onNext(this);
    }

    public void clearWorkingList() {
        if (currentWorkingList != null) {
            currentWorkingList = null;
            workingList.set(null);
        }
        filterProcessor.onNext(this);
    }

    public void clearSorting() {
        sortingItem = null;
        filterProcessor.onNext(this);
    }

    public void clearAllFilters() {
        eventStatusFilters.clear();
        observableEventStatus.set(eventStatusFilters);
        enrollmentStatusFilters.clear();
        observableEnrollmentStatus.set(null);
        catOptComboFilters.clear();
        stateFilters.clear();
        observableStates.set(stateFilters);
        ouFilters.clear();
        liveDataOUFilter.setValue(ouFilters);
        periodFilters = new ArrayList<>();
        observablePeriodFilters.set(periodFilters);
        enrollmentPeriodFilters = new ArrayList<>();
        enrollmentPeriodIdSelected = 0;
        periodIdSelected = 0;
        assignedFilter = false;
        observableAssignedToMe.set(false);
        sortingItem = null;

        eventStatusFiltersApplied.set(eventStatusFilters.size());
        enrollmentStatusFiltersApplied.set(enrollmentStatusFilters.size());
        catOptCombFiltersApplied.set(catOptComboFilters.size());
        stateFiltersApplied.set(stateFilters.size());
        ouFiltersApplied.set(ouFilters.size());
        periodFiltersApplied.set(0);
        assignedToMeApplied.set(0);

        if (!workingListActive())
            filterProcessor.onNext(this);
    }

    public int getTotalSearchTeiFilter() {
        return totalSearchTeiFilter;
    }

    public void setTotalSearchTeiFilter(int totalSearchTeiFilter) {
        this.totalSearchTeiFilter = totalSearchTeiFilter;
    }

    public boolean getAssignedFilter() {
        return assignedFilter;
    }

    public ObservableField<Boolean> observeAssignedToMe(){
        return observableAssignedToMe;
    }

    public void setAssignedToMe(boolean isChecked) {
        this.assignedFilter = isChecked;
        observableAssignedToMe.set(isChecked);
        assignedToMeApplied.set(isChecked ? 1 : 0);
        if (!workingListActive()) {
            filterProcessor.onNext(this);
        }
    }

    public void setSortingItem(SortingItem sortingItem) {
        if (sortingItem.getSortingStatus() != SortingStatus.NONE) {
            this.sortingItem = sortingItem;
        } else {
            this.sortingItem = null;
        }
        filterProcessor.onNext(this);
    }

    public SortingItem getSortingItem() {
        return sortingItem;
    }

    public void currentWorkingList(WorkingListItem workingListItem) {
        if (workingListItem != null) {
            this.currentWorkingList = workingListItem;
            this.workingList.set(currentWorkingList);
            applyWorkingListFilters();
        } else {
            clearAllFilters();
            this.currentWorkingList = null;
            this.workingList.set(null);
        }
        filterProcessor.onNext(this);
    }

    @Nullable
    public WorkingListItem currentWorkingList() {
        return currentWorkingList;
    }

    public boolean workingListActive() {
        return currentWorkingList != null;
    }

    private void applyWorkingListFilters() {
        if (currentWorkingList instanceof TeiWorkingListItem) {
            applyTeiWorkingList();
        } else {
            applyEventWorkingList();
        }

    }

    private void applyTeiWorkingList() {
        TeiWorkingListItem teiWorkingList = (TeiWorkingListItem) currentWorkingList;
        if (teiWorkingList.getEnrollentStatus() != null) {
            addEnrollmentStatus(false, teiWorkingList.getEnrollentStatus());
        } else {
            clearEnrollmentStatus();
        }
        if (teiWorkingList.getAssignedToMe() != null) {
            setAssignedToMe(teiWorkingList.getAssignedToMe());
        }else{
            clearAssignToMe();
        }
    }

    private void applyEventWorkingList() {
        EventWorkingListItem eventWorkingListItem = (EventWorkingListItem) currentWorkingList;
        if (eventWorkingListItem.getAssignedToMe() != null) {
            setAssignedToMe(eventWorkingListItem.getAssignedToMe());
        } else {
            clearAssignToMe();
        }
        if (eventWorkingListItem.getEventDatePeriod() != null) {

        } else {
        }
        if (eventWorkingListItem.getEventStatus() != null) {
            addEventStatus(false, eventWorkingListItem.getEventStatus());
        } else {
            clearEventStatus();
        }
        if (eventWorkingListItem.getOrgUnit() != null) {

        } else {
        }
    }

    public boolean isFilterActiveForWorkingList(Filters filterType) {
        switch (filterType) {
            case ENROLLMENT_STATUS:
                return currentWorkingList() != null && currentWorkingList() instanceof TeiWorkingListItem && ((TeiWorkingListItem) currentWorkingList()).getEnrollentStatus() != null;
            default:
                return false;
        }
    }
}
