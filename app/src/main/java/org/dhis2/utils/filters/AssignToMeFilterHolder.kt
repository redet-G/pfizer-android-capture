package org.dhis2.utils.filters

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.ObservableField
import org.dhis2.R
import org.dhis2.databinding.ItemFilterAssignedBinding

internal class AssignToMeFilterHolder(
    private val mBinding: ItemFilterAssignedBinding,
    openedFilter: ObservableField<Filters>,
    programType: FiltersAdapter.ProgramType
) : FilterHolder(mBinding, openedFilter) {

    init {
        filterType = Filters.ASSIGNED_TO_ME
        this.programType = programType
        filterArrow.visibility = View.INVISIBLE
        sortingIcon.visibility = View.GONE
    }

    public override fun bind() {
        super.bind()
        filterIcon.setImageDrawable(
            AppCompatResources.getDrawable(
                itemView.context,
                R.drawable.ic_assignment
            )
        )

        filterTitle.setText(R.string.filters_title_assigned)

        mBinding.filterSwitch.apply {
            isChecked = FilterManager.getInstance().assignedFilter
            setOnCheckedChangeListener { _, isChecked ->
                FilterManager.getInstance().setAssignedToMe(isChecked)
            }
        }
    }
}
