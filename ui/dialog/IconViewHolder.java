package com.unexceptional.beast.banko.newVersion.ui.dialog;

import com.unexceptional.beast.banko.databinding.PickIconItemBinding;

import androidx.recyclerview.widget.RecyclerView;

class IconViewHolder extends RecyclerView.ViewHolder {

    final PickIconItemBinding binding;

    IconViewHolder(PickIconItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}

