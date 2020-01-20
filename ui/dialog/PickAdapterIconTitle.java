package com.unexceptional.beast.banko.newVersion.ui.dialog;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.PickIconItemBinding;
import com.unexceptional.beast.banko.databinding.PickIconTitleItemBinding;
import com.unexceptional.beast.banko.other.MyApplication;

public abstract class PickAdapterIconTitle extends RecyclerView.Adapter<IconTitleViewHolder> {


    @NonNull
    @Override
    public IconTitleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PickIconTitleItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.pick_icon_title_item,
                        parent, false);
        return new IconTitleViewHolder(binding);
    }
}


