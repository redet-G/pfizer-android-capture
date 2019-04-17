package org.dhis2.usescases.datasets.dataSetTable;

import android.os.Bundle;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialRepository;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.Locale;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DataSetTablePresenter implements DataSetTableContract.Presenter {

    private final DataSetTableRepository tableRepository;
    DataSetTableContract.View view;
    private CompositeDisposable compositeDisposable;

    private String orgUnitUid;
    private String periodTypeName;
    private String periodFinalDate;
    private String catCombo;
    private boolean open = true;
    private String periodId;

    public DataSetTablePresenter(DataSetTableRepository dataSetTableRepository) {
        this.tableRepository = dataSetTableRepository;
    }

    @Override
    public void onBackClick() {
        view.back();
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
                Flowable.zip(
                        tableRepository.getDataElements(),
                        tableRepository.getCatOptions(),
                        Pair::create
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                    view.setDataElements(data.val0(), data.val1());
                                },
                                Timber::e
                        )
        );
        compositeDisposable.add(
                Flowable.zip(
                        tableRepository.getDataSet(),
                        tableRepository.getCatComboName(catCombo),
                        Pair::create
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                    view.renderDetails(data.val0(), data.val1());
                                },
                                Timber::e
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

    public String getPeriodId(){
        return periodId;
    }

    public String getCatCombo() {
        return catCombo;
    }

    @Override
    public void optionsClick() {
        view.showOptions(open);
        open =!open;
    }

    @Override
    public void onClickSelectTable(int numTable) {
        view.goToTable(numTable);
    }

}
