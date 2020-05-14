package org.dhis2.usescases.datasets.dataSetTable.dataSetDetail

import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.processors.PublishProcessor
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableRepository
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableRepositoryImpl
import org.hisp.dhis.android.core.dataset.DataSetInstance
import org.hisp.dhis.android.core.period.Period
import timber.log.Timber

class DataSetDetailPresenter(
    val view: DataSetDetailView,
    val repository: DataSetTableRepositoryImpl,
    val schedulers: SchedulerProvider
) {
    private val disposable = CompositeDisposable()
    private val updateProcessor = PublishProcessor.create<Boolean>()

    fun init() {
        disposable.add(
            repository.getDataSetCatComboName()
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(
                    { name ->
                        if (name.isNullOrEmpty() || name == "default") {
                            view.hideCatOptCombo()
                        } else {
                            view.setCatOptComboName(name)
                        }
                    },
                    { error -> Timber.d(error) }
                )
        )
        disposable.add(
            updateProcessor.startWith(true)
                .switchMap {
                    Flowable.combineLatest<DataSetInstance, Period, Pair<DataSetInstance, Period>>(
                        repository.dataSetInstance(),
                        repository.getPeriod().toFlowable(),
                        BiFunction { t1, t2 -> Pair(t1, t2) })
                }
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(
                    { data -> view.setDataSetDetails(data.first, data.second) },
                    { error -> Timber.d(error) }
                )
        )
        disposable.add(
            repository.getDataSet()
                .map { dataSet -> dataSet.style() }
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(
                    { style -> view.setStyle(style) },
                    { error -> Timber.d(error) }
                )
        )
    }

    fun detach() {
        disposable.clear()
    }

    fun updateData() {
        updateProcessor.onNext(true)
    }
}