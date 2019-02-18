package org.dhis2.data.forms.dataentry;

import androidx.annotation.NonNull;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.hisp.dhis.android.core.option.OptionModel;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

interface DataEntryView {

    @NonNull
    Flowable<RowAction> rowActions();

    @NonNull
    Consumer<List<FieldViewModel>> showFields();

    void removeSection(String sectionUid);

    void messageOnComplete(String message, boolean canComplete);

    Flowable<Trio<String, String, Integer>> optionSetActions();

    void setListOptions(List<OptionModel> options);

    void showMessage(int messageId);
}
