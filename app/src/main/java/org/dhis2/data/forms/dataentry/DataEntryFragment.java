package org.dhis2.data.forms.dataentry;

import android.content.Context;
import androidx.databinding.ObservableBoolean;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.forms.FormFragment;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.custom_views.OptionSetDialog;
import org.dhis2.utils.Preconditions;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

public final class DataEntryFragment extends FragmentGlobalAbstract implements DataEntryView {
    private static final String ARGUMENTS = "args";

    @Inject
    DataEntryPresenter dataEntryPresenter;

    DataEntryAdapter dataEntryAdapter;

    RecyclerView recyclerView;
    private Fragment formFragment;
    private String section;

    @NonNull
    public static DataEntryFragment create(@NonNull DataEntryArguments arguments) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARGUMENTS, arguments);

        DataEntryFragment dataEntryFragment = new DataEntryFragment();
        dataEntryFragment.setArguments(bundle);

        return dataEntryFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        formFragment = ((ActivityGlobalAbstract) context).getSupportFragmentManager().getFragments().get(0);
        DataEntryArguments args = Preconditions.isNull(getArguments()
                .getParcelable(ARGUMENTS), "dataEntryArguments == null");

        this.section = args.section();

        ((App) context.getApplicationContext())
                .formComponent()
                .plus(new DataEntryModule(context, args), new DataEntryStoreModule(args))
                .inject(this);
    }

    public String getSection() {
        return section;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data_entry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.recyclerview_data_entry);
        if (dataEntryPresenter == null)
            dataEntryPresenter.onAttach(this);
        setUpRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        dataEntryPresenter.onAttach(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        dataEntryPresenter.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @NonNull
    @Override
    public Flowable<RowAction> rowActions() {
        return dataEntryAdapter.asFlowable();
    }

    @NonNull
    @Override
    public Flowable<Trio<String, String, Integer>> optionSetActions() {
        return dataEntryAdapter.asFlowableOption();
    }

    @NonNull
    @Override
    public Consumer<List<FieldViewModel>> showFields() {
        return updates -> dataEntryAdapter.swap(updates);
    }

    @Override
    public void removeSection(String sectionUid) {
        if (formFragment instanceof FormFragment) {
            ((FormFragment) formFragment).hideSections(sectionUid);
        }
    }

    @Override
    public void messageOnComplete(String message, boolean canComplete) {
        //TODO: When event/enrollment ends if there is a message it should be shown. Only if canComplete, user can finish
    }

    public boolean checkMandatory() {
        return dataEntryAdapter.mandatoryOk();
    }

    private void setUpRecyclerView() {
        DataEntryArguments arguments = getArguments().getParcelable(ARGUMENTS);
        dataEntryAdapter = new DataEntryAdapter(LayoutInflater.from(getActivity()),
                getChildFragmentManager(), arguments,
                dataEntryPresenter.getOrgUnits(),
                new ObservableBoolean(true));
//        dataEntryAdapter.setHasStableIds(true);

        RecyclerView.LayoutManager layoutManager;
        if (arguments.renderType() != null && arguments.renderType().equals(ProgramStageSectionRenderingType.MATRIX.name())) {
            layoutManager = new GridLayoutManager(getActivity(), 2);
        } else
            layoutManager = new LinearLayoutManager(getActivity(),
                    LinearLayoutManager.VERTICAL, false);
        recyclerView.setAdapter(dataEntryAdapter);
        recyclerView.setLayoutManager(layoutManager);
        /*recyclerView.addItemDecoration(new DividerItemDecoration(
                recyclerView.getContext(), DividerItemDecoration.VERTICAL));*/
    }

    public boolean checkErrors() {
        return dataEntryAdapter.hasError();
    }

    @Override
    public void setListOptions(List<String> options) {
        OptionSetDialog.newInstance().setOptions(options);
    }

    @Override
    public void showMessage(int messageId) {
        showInfoDialog(getString(R.string.error), getString(R.string.unique_warning));
    }
}
