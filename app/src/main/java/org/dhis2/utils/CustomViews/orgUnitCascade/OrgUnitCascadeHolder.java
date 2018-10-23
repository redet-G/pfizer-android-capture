package org.dhis2.utils.CustomViews.orgUnitCascade;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;

import org.dhis2.data.tuples.Quartet;
import org.dhis2.data.tuples.Quintet;
import org.dhis2.databinding.OrgUnitCascadeLevelItemBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 22/10/2018.
 */

class OrgUnitCascadeHolder extends RecyclerView.ViewHolder {
    private final OrgUnitCascadeLevelItemBinding binding;
    private List<Quartet<String, String, String, Boolean>> levelOrgUnit;
    private String selectedUid;
    private PopupMenu menu;

    public OrgUnitCascadeHolder(@NonNull OrgUnitCascadeLevelItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(List<Quartet<String, String, String, Boolean>> organisationUnitModels, String parent, Quintet<String, String, String, Integer, Boolean> selectedOrgUnit, OrgUnitCascadeAdapter adapter) {
        this.levelOrgUnit = organisationUnitModels;
        Collections.sort(levelOrgUnit,
                (Quartet<String, String, String, Boolean> ou1, Quartet<String, String, String, Boolean> ou2) ->
                        ou1.val1().compareTo(ou2.val1()));

        ArrayList<String> data = new ArrayList<>();
        data.add(String.format("Select %s", getAdapterPosition() + 1));

        if (binding.levelText.getText() == null || binding.levelText.getText().toString().isEmpty())
            binding.levelText.setText(String.format("Select %s", getAdapterPosition() + 1));

        for (Quartet<String, String, String, Boolean> trio : levelOrgUnit)
            if (parent.isEmpty() || trio.val2().equals(parent)) //Only if ou is child of parent or is root
                data.add(trio.val1());

        if (data.size() > 1 && selectedUid == null) {
            itemView.setVisibility(View.VISIBLE);
            setMenu(data, adapter);
            binding.levelText.setOnClickListener(view -> menu.show());
        } else if (data.size() <= 1)
            itemView.setVisibility(View.GONE);
    }

    private void setMenu(ArrayList<String> data, OrgUnitCascadeAdapter adapter) {
        menu = new PopupMenu(binding.levelText.getContext(), binding.levelText, Gravity.BOTTOM);

        for (String label : data)
            menu.getMenu().add(Menu.NONE, Menu.NONE, data.indexOf(label), label);

        menu.setOnMenuItemClickListener(item -> {
            selectedUid = item.getOrder() == 0 ? "" : levelOrgUnit.get(item.getOrder() - 1).val0();
            binding.levelText.setText(data.get(item.getOrder()));
            adapter.setSelectedLevel(getAdapterPosition() + 1, selectedUid, levelOrgUnit.get(item.getOrder()-1).val3());
            return false;
        });
    }
}
