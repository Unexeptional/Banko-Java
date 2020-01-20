package com.unexceptional.beast.banko.newVersion.ui.dialog;

import androidx.recyclerview.widget.RecyclerView;

import com.unexceptional.beast.banko.databinding.PickIconItemBinding;
import com.unexceptional.beast.banko.databinding.PickIconTitleItemBinding;

public class IconTitleViewHolder extends RecyclerView.ViewHolder {

    public final PickIconTitleItemBinding binding;

    IconTitleViewHolder(PickIconTitleItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}

