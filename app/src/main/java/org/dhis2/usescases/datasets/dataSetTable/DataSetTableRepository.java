package org.dhis2.usescases.datasets.dataSetTable;

import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.datavalue.DataValue;
import org.hisp.dhis.android.core.datavalue.DataValueModel;

import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;

public interface DataSetTableRepository {
    Flowable<DataSetModel> getDataSet();

    Flowable<Map<String, List<DataElementModel>>> getDataElements();

    Flowable<Map<String, List<CategoryOptionModel>>> getCatOptions();

    Flowable<List<DataSetTableModel>> getDataValues(String orgUnitUid, String periodType, String initPeriodType, String catOptionComb);
}
