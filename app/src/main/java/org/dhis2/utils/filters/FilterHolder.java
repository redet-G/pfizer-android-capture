package org.dhis2.utils.filters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.BR;
import org.dhis2.R;
import org.dhis2.utils.filters.sorting.Sorting;
import org.dhis2.utils.filters.sorting.SortingItem;
import org.dhis2.utils.filters.sorting.SortingStatus;

import java.util.List;

public abstract class FilterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    protected final View filterArrow;
    protected final ImageView filterIcon;
    protected final TextView filterTitle;
    protected final TextView filterValues;
    protected final ImageView sortingIcon;
    protected final ConstraintLayout clickableLayout;
    protected Filters filterType;
    private ObservableField<Filters> openFilter;
    private ObservableField<SortingItem> sortingItem;
    protected ProgramType programType;
    protected ViewDataBinding binding;

    FilterHolder(@NonNull ViewDataBinding binding, ObservableField<Filters> openedFilter, ObservableField<SortingItem> sortingItem) {
        super(binding.getRoot());
        this.binding = binding;
        this.openFilter = openedFilter;
        this.sortingItem = sortingItem;
        this.filterArrow = binding.getRoot().findViewById(R.id.filterArrow);
        this.filterIcon = binding.getRoot().findViewById(R.id.filterIcon);
        this.filterTitle = binding.getRoot().findViewById(R.id.filterTitle);
        this.filterValues = binding.getRoot().findViewById(R.id.filterValues);
        this.sortingIcon = binding.getRoot().findViewById(R.id.sortingIcon);
        this.clickableLayout = binding.getRoot().findViewById(R.id.filterTextLayout);
    }

    FilterHolder(@NonNull ViewDataBinding binding, ObservableField<Filters> openedFilter) {
        super(binding.getRoot());
        this.binding = binding;
        this.openFilter = openedFilter;
        this.sortingItem = new ObservableField<>();
        this.filterArrow = binding.getRoot().findViewById(R.id.filterArrow);
        this.filterIcon = binding.getRoot().findViewById(R.id.filterIcon);
        this.filterTitle = binding.getRoot().findViewById(R.id.filterTitle);
        this.filterValues = binding.getRoot().findViewById(R.id.filterValues);
        this.sortingIcon = binding.getRoot().findViewById(R.id.sortingIcon);
        this.clickableLayout = binding.getRoot().findViewById(R.id.filterTextLayout);
    }

    protected void bind() {
        binding.setVariable(BR.currentFilter, openFilter);
        binding.setVariable(BR.filterType, filterType);
        binding.setVariable(BR.filterCount, FilterManager.getInstance().observeField(filterType));
        binding.setVariable(BR.currentSortItem, sortingItem);
        binding.setVariable(BR.workingListFilter, FilterManager.getInstance().observeWorkingListFilter());
        binding.executePendingBindings();

        setSortingIcons();

        if (clickableLayout != null)
            clickableLayout.setOnClickListener(this);
        if (filterArrow != null)
            filterArrow.setOnClickListener(this);
        if (sortingIcon != null)
            sortingIcon.setOnClickListener(this);

        if (openFilter != null)
            openFilter.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    if (filterArrow != null)
                        filterArrow.animate().scaleY(openFilter.get() != filterType ? 1 : -1).setDuration(200).start();
                }
            });

        sortingItem.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (sortingItem.get().component1() != filterType) {
                    sortingIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_sort_deactivated));
                } else {
                    switch (sortingItem.get().component2()) {
                        case ASC:
                            sortingIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_sort_ascending));
                            break;
                        case DESC:
                            sortingIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_sort_descending));
                            break;
                        case NONE:
                        default:
                            sortingIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_sort_deactivated));
                            break;
                    }
                }
            }
        });
    }

    private void setSortingIcons() {
        List<Filters> sortingOptions = Sorting.getSortingOptions(programType);
        for (Filters filter : sortingOptions) {
            if (filter == filterType) {
                sortingIcon.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (!FilterManager.getInstance().isFilterActiveForWorkingList(filterType)) {
            if (view.getId() == R.id.sortingIcon) {
                SortingItem sortItem = SortingItem.create(filterType);
                if (sortingItem.get() != null && sortingItem.get().component1() == sortItem.component1()) {
                    if (sortingItem.get().component2() == SortingStatus.ASC) {
                        sortItem.setSortingStatus(SortingStatus.DESC);
                    } else if (sortingItem.get().component2() == SortingStatus.DESC) {
                        sortItem.setSortingStatus(SortingStatus.NONE);
                    } else {
                        sortItem.setSortingStatus(SortingStatus.ASC);
                    }
                } else {
                    sortItem.setSortingStatus(SortingStatus.ASC);
                }
                sortingItem.set(sortItem);
                FilterManager.getInstance().setSortingItem(sortingItem.get());
            } else {
                openFilter.set(openFilter.get() != filterType ? filterType : null);
            }
        } else {
            openFilter.set(null);
        }
    }

    public void bind(FilterItem filterItem){
        binding.setVariable(BR.filterItem, filterItem);
        bind();
    }
}
