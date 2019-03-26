package org.dhis2.usescases.datasets.datasetInitial;

import android.os.Bundle;

import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DataSetInitialPresenter implements DataSetInitialContract.Presenter {

    private CompositeDisposable compositeDisposable;
    private DataSetInitialRepository dataSetInitialRepository;
    private DataSetInitialContract.View view;
    private String catCombo;
    private D2 d2;
    public DataSetInitialPresenter(DataSetInitialRepository dataSetInitialRepository, D2 d2) {
        this.dataSetInitialRepository = dataSetInitialRepository;
        this.d2 = d2;
    }


    @Override
    public void init(DataSetInitialContract.View view) {
        this.view = view;
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(
                dataSetInitialRepository.dataSet()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                dataSetInitialModel -> {
                                        catCombo = dataSetInitialModel.categoryCombo();
                                        view.setData(dataSetInitialModel);
                                    },
                                Timber::d
                        ));
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void onOrgUnitSelectorClick() {
        compositeDisposable.add(
                dataSetInitialRepository.orgUnits()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> view.showOrgUnitDialog(data),
                                Timber::d
                        )
        );
    }

    @Override
    public void onReportPeriodClick(PeriodType periodType) {
        compositeDisposable.add(
                dataSetInitialRepository.getDataInputPeriod()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                            view.showPeriodSelector(periodType, data);
                        },
                        Timber::e)
        );
    }

    @Override
    public void onCatOptionClick(String catOptionUid) {
        compositeDisposable.add(
                dataSetInitialRepository.catCombo(catOptionUid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> view.showCatComboSelector(catOptionUid, data),
                                Timber::d
                        )
        );
    }

    @Override
    public void onActionButtonClick() {
        compositeDisposable.add(
                dataSetInitialRepository.getCategoryOptionCombo(view.getSelectedCatOptions(), catCombo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(catOptCombo -> {
                            Bundle bundle = DataSetTableActivity.getBundle(
                                    view.getDataSetUid(),
                                    view.getSelectedOrgUnit().uid(),
                                    view.getSelectedOrgUnit().name(),
                                    view.getPeriodType(),
                                    DateUtils.getInstance().getPeriodUIString(PeriodType.valueOf(view.getPeriodType()), view.getSelectedPeriod(), Locale.getDefault()),
                                    d2.periodModule().periodHelper.getPeriod(PeriodType.valueOf(view.getPeriodType()), view.getPeriodDate()).periodId(),
                                    catOptCombo
                            );

                            view.startActivity(DataSetTableActivity.class, bundle, true, false, null);
                        },
                        Timber::e
                )
        );

    }


    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }
}
