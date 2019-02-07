package org.dhis2.data.forms.dataentry.tablefields.radiobutton;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.Row;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.FormYesNoBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

public class RadioButtonRow implements Row<RadioButtonHolder, RadioButtonViewModel> {

    private final LayoutInflater inflater;
    private final boolean isBgTransparent;

    @NonNull
    private final FlowableProcessor<RowAction> processor;
    private final String renderType;
    private boolean accessDataWrite;
    public RadioButtonRow(LayoutInflater layoutInflater, @NonNull FlowableProcessor<RowAction> processor, boolean isBgTransparent) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType = null;

    }

    public RadioButtonRow(LayoutInflater layoutInflater, FlowableProcessor<RowAction> processor, boolean isBgTransparent, String renderType, boolean accessDataWrite) {
        this.inflater = layoutInflater;
        this.processor = processor;
        this.isBgTransparent = isBgTransparent;
        this.renderType = renderType;
        this.accessDataWrite = accessDataWrite;
    }

    @NonNull
    @Override
    public RadioButtonHolder onCreate(@NonNull ViewGroup parent) {
        FormYesNoBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.form_yes_no, parent, false);
        binding.customYesNo.setIsBgTransparent(isBgTransparent);
        return new RadioButtonHolder(parent, binding, processor);
    }

    @Override
    public void onBind(@NonNull RadioButtonHolder viewHolder, @NonNull RadioButtonViewModel viewModel, String value) {
        viewHolder.update(viewModel, accessDataWrite);
    }

    @Override
    public void deAttach(@NonNull RadioButtonHolder viewHolder) {
        viewHolder.dispose();
    }


}
