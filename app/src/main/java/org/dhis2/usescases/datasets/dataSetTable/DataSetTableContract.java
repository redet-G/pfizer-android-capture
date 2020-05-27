package org.dhis2.usescases.datasets.dataSetTable;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.period.Period;
import org.hisp.dhis.android.core.validation.engine.ValidationResultViolation;

import java.util.List;

import io.reactivex.Observable;

public class DataSetTableContract {

    public interface View extends AbstractActivityContracts.View {

        void setSections(List<String> sections);

        Boolean accessDataWrite();

        String getDataSetUid();

        String getOrgUnitName();

        void renderDetails(DataSet dataSet, String catcomboName, Period period);

        Observable<Object> observeSaveButtonClicks();

        void showInfoDialog(boolean isMandatoryFields);

        void showValidationRuleDialog();

        void showSuccessValidationDialog();

        void showErrorsValidationDialog(List<ValidationResultViolation> violations);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {

        void onBackClick();

        void init(String orgUnitUid, String periodTypeName, String catCombo, String periodFinalDate, String periodId);

        String getOrgUnitUid();

        String getPeriodTypeName();

        String getPeriodFinalDate();

        String getCatCombo();

        String getPeriodId();

        void executeValidationRules();

        void completeDataSet();
    }

}
