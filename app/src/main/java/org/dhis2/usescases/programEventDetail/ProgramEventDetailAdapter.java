package org.dhis2.usescases.programEventDetail;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemProgramEventBinding;
import org.hisp.dhis.android.core.event.EventModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */

public class ProgramEventDetailAdapter extends RecyclerView.Adapter<ProgramEventDetailViewHolder> {

    private ProgramEventDetailContract.Presenter presenter;
    private List<EventModel> events;

    ProgramEventDetailAdapter(ProgramEventDetailContract.Presenter presenter) {
        this.presenter = presenter;
        this.events = new ArrayList<>();
    }

    @NonNull
    @Override
    public ProgramEventDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemProgramEventBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_program_event, parent, false);
        return new ProgramEventDetailViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramEventDetailViewHolder holder, int position) {
        holder.bind(presenter, events.get(position));
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    public void setEvents(List<EventModel> events, int currentPage) {

        if (currentPage == 0)
            this.events = new ArrayList<>();

        this.events.addAll(events);

        notifyDataSetChanged();

    }

    public void clearData() {
        this.events.clear();
    }
}
