package com.unexceptional.beast.banko.newVersion.ui.dialog;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.PickIconItemBinding;
import com.unexceptional.beast.banko.other.MyApplication;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

abstract class BasicPickAdapter extends RecyclerView.Adapter<IconViewHolder> {


    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PickIconItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.pick_icon_item,
                        parent, false);
        return new IconViewHolder(binding);
    }

    int getDrawable(String iconName){
        if (iconName != null && iconName.length() > 0 && iconName.charAt(iconName.length() - 1) == 'x') {
            iconName = iconName.substring(0, iconName.length() - 1);
        }
        return MyApplication.getDrawableId(iconName);

    }
}


