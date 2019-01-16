package org.dhis2.usescases.searchTrackEntity.adapters;

import androidx.databinding.DataBindingUtil;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.flexbox.FlexboxLayout;

import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.databinding.ItemSearchTrackedEntityBinding;
import org.dhis2.databinding.TrackEntityProgramsBinding;
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

/**
 * QUADRAM. Created by frodriguez on 11/7/2017.
 */

public class SearchTEViewHolder extends RecyclerView.ViewHolder {

    private ItemSearchTrackedEntityBinding binding;
    private CompositeDisposable compositeDisposable;

    SearchTEViewHolder(ItemSearchTrackedEntityBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        compositeDisposable = new CompositeDisposable();
    }


    public void bind(SearchTEContractsModule.Presenter presenter, SearchTeiModel searchTeiModel) {
        binding.setPresenter(presenter);
        binding.setOverdue(searchTeiModel.isHasOverdue());
        binding.setIsOnline(searchTeiModel.isOnline());
        binding.setSyncState(searchTeiModel.getTei().state());

        setEnrollment(searchTeiModel.getEnrollments());
        setTEIData(searchTeiModel.getAttributeValues());

        binding.trackedEntityImage.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.photo_temp_gray));
        binding.followUp.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_circle_red));

        binding.executePendingBindings();

        itemView.setOnClickListener(view -> presenter.onTEIClick(searchTeiModel.getTei().uid(), searchTeiModel.isOnline()));

    }

    private void setTEIData(List<TrackedEntityAttributeValueModel> trackedEntityAttributeValueModels) {
        binding.setAttribute(trackedEntityAttributeValueModels);
        binding.executePendingBindings();
    }

    private void setEnrollment(List<EnrollmentModel> enrollments) {
        binding.linearLayout.removeAllViews();
        boolean isFollowUp = false;
        for (EnrollmentModel enrollment : enrollments) {
            if (enrollment.enrollmentStatus() == EnrollmentStatus.ACTIVE && binding.linearLayout.getChildCount() < 2 &&
                    (binding.getPresenter().getProgramModel()==null || !binding.getPresenter().getProgramModel().uid().equals(enrollment.program()))) {
                TrackEntityProgramsBinding programsBinding = DataBindingUtil.inflate(
                        LayoutInflater.from(binding.linearLayout.getContext()), R.layout.track_entity_programs, binding.linearLayout, false
                );

                programsBinding.setEnrollment(enrollment);
                Bindings.setObjectStyle(programsBinding.programImage,programsBinding.programImage,enrollment.program());
                programsBinding.executePendingBindings();
                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                binding.linearLayout.addView(programsBinding.getRoot(), params);
                binding.linearLayout.invalidate();
            }

            if (enrollment.followUp() != null && enrollment.followUp())
                isFollowUp = true;
        }

        binding.setFollowUp(isFollowUp);
        binding.viewMore.setVisibility(enrollments.size() > 2 ? View.VISIBLE : View.GONE);
    }

}
