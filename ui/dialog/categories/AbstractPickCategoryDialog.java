package com.unexceptional.beast.banko.newVersion.ui.dialog.categories;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;

import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.databinding.PickIconTitleItemBinding;
import com.unexceptional.beast.banko.newVersion.callback.PickCategoryCallback;
import com.unexceptional.beast.banko.newVersion.db.model.Category;
import com.unexceptional.beast.banko.newVersion.ui.dialog.IconTitleViewHolder;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickAdapterIconTitle;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.List;
import java.util.Objects;

abstract class AbstractPickCategoryDialog {

    protected AppCompatActivity activity;
    protected AlertDialog dialog;
    protected PickCategoryCallback callback;

    protected class PickCategoryViewAdapter extends PickAdapterIconTitle {

        private List<? extends Category> mCategoryList;

        @Nullable
        private final PickCategoryCallback mPickCategoryCallback;

        PickCategoryViewAdapter(@Nullable PickCategoryCallback clickCallback) {
            mPickCategoryCallback = clickCallback;
            setHasStableIds(true);
        }

        void setCategoryList(final List<? extends Category> categoryList) {
            if (mCategoryList == null) {
                mCategoryList = categoryList;
                notifyItemRangeInserted(0, categoryList.size());
            } else {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return mCategoryList.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return categoryList.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return mCategoryList.get(oldItemPosition).getId() ==
                                categoryList.get(newItemPosition).getId();
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        Category newCategory = categoryList.get(newItemPosition);
                        Category oldCategory = mCategoryList.get(oldItemPosition);
                        return newCategory.getId() == oldCategory.getId()
                                && Objects.equals(newCategory.getTitle(), oldCategory.getTitle());
                    }
                });
                mCategoryList = categoryList;
                result.dispatchUpdatesTo(this);
            }
        }

        @Override
        public int getItemCount() {
            return mCategoryList == null ? 0 : mCategoryList.size();
        }

        @Override
        public long getItemId(int position) {
            return mCategoryList.get(position).getId();
        }

        @Override
        public void onBindViewHolder(@NonNull IconTitleViewHolder holder, int position) {
            Category category= mCategoryList.get(position);

            holder.binding.title.setText(category.getTitle());

            setIcon(category, holder.binding);
            if(mPickCategoryCallback!=null) {
                holder.binding.pickIcon.setOnClickListener(v -> {
                    mPickCategoryCallback.onCategoryItemPick(mCategoryList.get(position));
                    dialog.dismiss();
                });
            }
        }

        private void setIcon(Category category, PickIconTitleItemBinding binding) {
            int id = MyApplication.getDrawableId(category.getIconName());
            if (id != 0)
                Picasso.get().load(id).into(binding.pickIcon);
        }
    }
}