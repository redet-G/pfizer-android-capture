package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import androidx.recyclerview.widget.RecyclerView
import org.dhis2.data.analytics.AnalyticsModel
import org.dhis2.databinding.ItemSectionTittleBinding

class SectionTitleViewHolder(
    val binding: ItemSectionTittleBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(sectionTitle: AnalyticsModel.SectionTitle) {
        binding.title = sectionTitle.title
    }
}
