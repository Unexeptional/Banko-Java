package com.unexceptional.beast.banko.newVersion.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;

import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;
import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.ChildCategoryItemBinding;
import com.unexceptional.beast.banko.databinding.ParentCategoryItemBinding;
import com.unexceptional.beast.banko.newVersion.callback.CategoryClickCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.CategoryEntity;
import com.unexceptional.beast.banko.newVersion.ui.fragment.list.CategoryListFragment;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.List;
import java.util.Objects;

public class ExpandableCategoryViewAdapter extends AbstractExpandableItemAdapter<ExpandableCategoryViewAdapter.MyGroupViewHolder,
        ExpandableCategoryViewAdapter.MyChildViewHolder> {
    private List<CategoryListFragment.ParentCategory> mParentCategories;

    @Nullable
    private final CategoryClickCallback mCategoryClickCallback;

    public ExpandableCategoryViewAdapter(@Nullable CategoryClickCallback clickCallback) {
        mCategoryClickCallback = clickCallback;
        setHasStableIds(true);
    }

    public void setParentCategories(final List<CategoryListFragment.ParentCategory> parentCategories) {
        mParentCategories = parentCategories;
        notifyDataSetChanged();
    }


    @Override
    public int getGroupCount() {
        return mParentCategories == null ? 0 : mParentCategories.size();
    }

    @Override
    public int getChildCount(int groupPosition) {
        return mParentCategories.get(groupPosition).getChildren().size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        // This method need to return unique value within all group items.
        return mParentCategories.get(groupPosition).getId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // This method need to return unique value within the group.
        return mParentCategories.get(groupPosition).getChildren().get(childPosition).getId();
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(@NonNull ExpandableCategoryViewAdapter.MyGroupViewHolder holder, int groupPosition, int x, int y, boolean expand) {
      boolean value= x > holder.binding.arrow.getLeft() && x < holder.binding.arrow.getRight();
      if(value)
          if(expand)
            Picasso.get().load(R.drawable.baseline_keyboard_arrow_up_black_36).into(holder.binding.arrow);
          else
            Picasso.get().load(R.drawable.outline_keyboard_arrow_down_black_36).into(holder.binding.arrow);

        return value;
    }



    class MyChildViewHolder extends AbstractExpandableItemViewHolder{

        final ChildCategoryItemBinding binding;

        MyChildViewHolder(ChildCategoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    @Override
    @NonNull
    public ExpandableCategoryViewAdapter.MyChildViewHolder onCreateChildViewHolder(@NonNull ViewGroup parent, int viewType) {
        ChildCategoryItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.child_category_item,
                        parent, false);
        binding.setCallback(mCategoryClickCallback);
        return new ExpandableCategoryViewAdapter.MyChildViewHolder(binding);
    }

    class MyGroupViewHolder extends  AbstractExpandableItemViewHolder {

        final ParentCategoryItemBinding binding;

        MyGroupViewHolder(ParentCategoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    @NonNull
    @Override
    public ExpandableCategoryViewAdapter.MyGroupViewHolder onCreateGroupViewHolder(@NonNull ViewGroup parent, int viewType) {
        ParentCategoryItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.parent_category_item,
                        parent, false);
        binding.setCallback(mCategoryClickCallback);
        return new ExpandableCategoryViewAdapter.MyGroupViewHolder(binding);
    }

    //GROUP
    @Override
    public void onBindGroupViewHolder(@NonNull ExpandableCategoryViewAdapter.MyGroupViewHolder holder,
                                      int groupPosition, int viewType) {
        final CategoryListFragment.ParentCategory group = mParentCategories.get(groupPosition);
        holder.binding.setCategory(group);

        if (group.getChildren().isEmpty())
            holder.binding.arrow.setVisibility(View.GONE);
        else
            holder.binding.arrow.setVisibility(View.VISIBLE);

        int id= MyApplication.getDrawableId(group.getIconName());
        if(id!=0)
            Picasso.get().load(id).into(holder.binding.categoryItemIcon);
    }

    //KIDS
    @Override
    public void onBindChildViewHolder(@NonNull ExpandableCategoryViewAdapter.MyChildViewHolder holder,
                                      int groupPosition, int childPosition, int viewType) {
        final CategoryEntity child = mParentCategories.get(groupPosition).getChildren().get(childPosition);
        holder.binding.setCategory(child);

        int id= MyApplication.getDrawableId(child.getIconName());
        if(id!=0)
            Picasso.get().load(id).into(holder.binding.categoryItemIcon);

    }
}

