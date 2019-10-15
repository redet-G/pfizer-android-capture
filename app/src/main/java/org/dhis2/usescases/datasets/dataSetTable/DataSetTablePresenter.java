package org.dhis2.usescases.datasets.dataSetTable;

import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Pair;
import org.hisp.dhis.android.core.common.State;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.INFO_DATASET_TABLE;

public class DataSetTablePresenter implements DataSetTableContract.Presenter {

    private final DataSetTableRepository tableRepository;
    private final SchedulerProvider schedulerProvider;
    DataSetTableContract.View view;
    private CompositeDisposable compositeDisposable;

    private String orgUnitUid;
    private String periodTypeName;
    private String periodFinalDate;
    private String catCombo;
    private boolean open = true;
    private String periodId;

    public DataSetTablePresenter(DataSetTableRepository dataSetTableRepository, SchedulerProvider schedulerProvider) {
        this.tableRepository = dataSetTableRepository;
        this.schedulerProvider = schedulerProvider;
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void onSyncClick() {
        view.runSmsSubmission();
    }

    @Override
    public void init(DataSetTableContract.View view, String orgUnitUid, String periodTypeName, String catCombo,
                     String periodFinalDate, String periodId) {
        this.view = view;
        compositeDisposable = new CompositeDisposable();
        this.orgUnitUid = orgUnitUid;
        this.periodTypeName = periodTypeName;
        this.periodFinalDate = periodFinalDate;
        this.catCombo = catCombo;
        this.periodId = periodId;

        compositeDisposable.add(
                tableRepository.getSections()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(view::setSections, Timber::e)
        );

        compositeDisposable.add(
                Flowable.zip(
                        tableRepository.getDataSet(),
                        tableRepository.getCatComboName(catCombo),
                        Pair::create
                )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                data -> view.renderDetails(data.val0(), data.val1()),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                tableRepository.dataSetStatus()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(view::isDataSetOpen,
                                Timber::d
                        )
        );

        compositeDisposable.add(
                tableRepository.dataSetState()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(state -> view.isDataSetSynced(state == State.SYNCED),
                                Timber::d
                        )
        );
    }

    @Override
    public void onDettach() {
        compositeDisposable.dispose();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    public String getOrgUnitUid() {
        return orgUnitUid;
    }

    public String getPeriodTypeName() {
        return periodTypeName;
    }

    public String getPeriodFinalDate() {
        return periodFinalDate;
    }

    public String getPeriodId() {
        return periodId;
    }

    public String getCatCombo() {
        return catCombo;
    }

    @Override
    public void optionsClick() {
        view.analyticsHelper().setEvent(INFO_DATASET_TABLE, CLICK, INFO_DATASET_TABLE);
        view.showOptions(open);
        open = !open;
    }

    @Override
    public void onClickSelectTable(int numTable) {
        view.goToTable(numTable);
    }

    @Override
    public String getCatOptComboFromOptionList(List<String> catOpts) {
        return tableRepository.getCatOptComboFromOptionList(catOpts);
    }

}
