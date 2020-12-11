package org.dhis2.data.forms.dataentry.fields.unsupported;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.hisp.dhis.android.core.common.ObjectStyle;

import java.util.HashMap;

import io.reactivex.processors.FlowableProcessor;

@AutoValue
public abstract class UnsupportedViewModel extends FieldViewModel {
    public static FieldViewModel create(String id, String label, Boolean mandatory, String value, String section, Boolean editable, String description, ObjectStyle objectStyle) {
        return new AutoValue_UnsupportedViewModel(id, label, false, value, section, null, false, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.UNSUPPORTED, null, null);
    }

    public static FieldViewModel create(String id, String label, Boolean mandatory, String value, String section, Boolean editable, String description, ObjectStyle objectStyle, FlowableProcessor<RowAction> processor, FlowableProcessor<HashMap<String, Boolean>> focusProcessor) {
        return new AutoValue_UnsupportedViewModel(id, label, false, value, section, null, false, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.UNSUPPORTED, processor, focusProcessor);
    }

    @Override
    public FieldViewModel setMandatory() {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.UNSUPPORTED, processor(), focusProcessor());
    }

    @NonNull
    @Override
    public FieldViewModel withError(@NonNull String error) {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), false, optionSet(), warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.UNSUPPORTED, processor(), focusProcessor());
    }

    @NonNull
    @Override
    public FieldViewModel withWarning(@NonNull String warning) {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), false, optionSet(), warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.UNSUPPORTED, processor(), focusProcessor());
    }

    @NonNull
    @Override
    public FieldViewModel withValue(String data) {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, data, programStageSection(), allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.UNSUPPORTED, processor(), focusProcessor());
    }

    @NonNull
    @Override
    public FieldViewModel withEditMode(boolean isEditable) {
        return new AutoValue_UnsupportedViewModel(uid(), label(), false, value(), programStageSection(), allowFutureDate(), false, optionSet(), warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.UNSUPPORTED, processor(), focusProcessor());
    }

    @Override
    public int getLayoutId() {
        return R.layout.form_unsupported_custom;
    }
}
