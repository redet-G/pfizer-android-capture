package org.dhis2.utils.filters;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.ObservableField;

import org.dhis2.R;
import org.dhis2.databinding.ItemFilterEnrollmentStatusBinding;
import org.dhis2.utils.filters.sorting.SortingItem;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;

public class StatusEnrollmentFilterHolder extends FilterHolder {

    StatusEnrollmentFilterHolder(@NonNull ItemFilterEnrollmentStatusBinding binding, ObservableField<Filters> openedFilter, ObservableField<SortingItem> sortingItem, FiltersAdapter.ProgramType programType) {
        super(binding, openedFilter, sortingItem);
        filterType = Filters.ENROLLMENT_STATUS;
        this.programType = programType;
    }

    @Override
    protected void bind() {
        super.bind();
        filterTitle.setText(R.string.filters_title_enrollment_status);
        filterIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_enrollment_status_filter));


        ItemFilterEnrollmentStatusBinding localBinding = (ItemFilterEnrollmentStatusBinding) binding;

        localBinding.filterLayout.getRoot().setAlpha(FilterManager.getInstance().isFilterActiveForWorkingList(filterType) ? 0.65f : 1f);

        localBinding.filterEnrollmentStatus.stateActive.setChecked(FilterManager.getInstance().getEnrollmentStatusFilters().contains(EnrollmentStatus.ACTIVE));
        localBinding.filterEnrollmentStatus.stateCancelled.setChecked(FilterManager.getInstance().getEnrollmentStatusFilters().contains(EnrollmentStatus.CANCELLED));
        localBinding.filterEnrollmentStatus.stateCompleted.setChecked(FilterManager.getInstance().getEnrollmentStatusFilters().contains(EnrollmentStatus.COMPLETED));

        localBinding.filterEnrollmentStatus.stateActive.setOnCheckedChangeListener((compoundButton, b) -> {
            FilterManager.getInstance().addEnrollmentStatus(!b, EnrollmentStatus.ACTIVE);
            localBinding.filterEnrollmentStatus.stateCancelled.setChecked(FilterManager.getInstance().getEnrollmentStatusFilters().contains(EnrollmentStatus.CANCELLED));
            localBinding.filterEnrollmentStatus.stateCompleted.setChecked(FilterManager.getInstance().getEnrollmentStatusFilters().contains(EnrollmentStatus.COMPLETED));
        });
        localBinding.filterEnrollmentStatus.stateCancelled.setOnCheckedChangeListener((compoundButton, b) -> {
            FilterManager.getInstance().addEnrollmentStatus(!b, EnrollmentStatus.CANCELLED);
            localBinding.filterEnrollmentStatus.stateActive.setChecked(FilterManager.getInstance().getEnrollmentStatusFilters().contains(EnrollmentStatus.ACTIVE));
            localBinding.filterEnrollmentStatus.stateCompleted.setChecked(FilterManager.getInstance().getEnrollmentStatusFilters().contains(EnrollmentStatus.COMPLETED));
        });
        localBinding.filterEnrollmentStatus.stateCompleted.setOnCheckedChangeListener((compoundButton, b) -> {
            FilterManager.getInstance().addEnrollmentStatus(!b, EnrollmentStatus.COMPLETED);
            localBinding.filterEnrollmentStatus.stateActive.setChecked(FilterManager.getInstance().getEnrollmentStatusFilters().contains(EnrollmentStatus.ACTIVE));
            localBinding.filterEnrollmentStatus.stateCancelled.setChecked(FilterManager.getInstance().getEnrollmentStatusFilters().contains(EnrollmentStatus.CANCELLED));
        });
    }
}
