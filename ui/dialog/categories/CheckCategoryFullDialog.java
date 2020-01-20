package com.unexceptional.beast.banko.newVersion.ui.dialog.categories;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LongSparseArray;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.CheckCategoryItemBinding;
import com.unexceptional.beast.banko.databinding.CheckKidItemBinding;
import com.unexceptional.beast.banko.newVersion.callback.CheckCategoryCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.CategoryEntity;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CategoryListViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.ArrayList;
import java.util.List;

public class CheckCategoryFullDialog {

    private AppCompatActivity activity;
    private AlertDialog dialog;
    private CheckCategoryCallback callback;
    private  ArrayList<String> stringList;

    public CheckCategoryFullDialog(AppCompatActivity activity, CheckCategoryCallback callback, ArrayList<String> stringList) {
        this.activity = activity;
        this.callback = callback;
        this.stringList =stringList;
    }

    public void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        List<CheckCategoryEntity> checkedItems= new ArrayList<>();

        View dialogView = View.inflate(activity, R.layout.dialog_check, null);
        builder.setView(dialogView);

        //bind views
        RecyclerView recyclerView= dialogView.findViewById(R.id.check_rv);
        Button selectNone= dialogView.findViewById(R.id.select_none);
        Button selectAll= dialogView.findViewById(R.id.select_all);
        Button ok= dialogView.findViewById(R.id.btn_ok);

        ParentCheckViewAdapter adapter= new ParentCheckViewAdapter(callback);
        recyclerView.setAdapter(adapter);


        ok.setOnClickListener(v -> dialog.dismiss());
        selectNone.setOnClickListener(v -> {
            for (CheckCategoryEntity checkedItem: checkedItems)
                checkedItem.setChecked(false);
            adapter.setItems(doSuperParents(checkedItems));
            //aby wywołać bindviewholder
            recyclerView.setAdapter(adapter);
            //callback.sendItems(checkedItems);
        });
        selectAll.setOnClickListener(v -> {
            for (CheckCategoryEntity checkedItem: checkedItems)
                checkedItem.setChecked(true);
            adapter.setItems(doSuperParents(checkedItems));
            //aby wywołać bindviewholder
            recyclerView.setAdapter(adapter);
            //callback.sendItems(checkedItems);
        });

        //get items
        LiveData<List<CategoryEntity>> liveData=  ViewModelProviders.of(activity).get(CategoryListViewModel.class).getAllCategories();

        liveData.observe(activity, categories -> {
            if (categories != null) {
                for (CategoryEntity category: categories)
                    checkedItems.add(new CheckCategoryEntity(category));

                for (String string: stringList){
                    for (CheckCategoryEntity checkedItem: checkedItems){
                        if (Long.parseLong(string)== checkedItem.getId())
                            checkedItem.setChecked(true);
                    }
                }

                liveData.removeObservers(activity);

                adapter.setItems(doSuperParents(checkedItems));
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    private List<SuperParentCategory> doSuperParents(List<CheckCategoryEntity> categoryEntities){

        List<SuperParentCategory> active= new ArrayList<>();
        LongSparseArray<List<CheckCategoryEntity>> children= new LongSparseArray<>();

        for(CheckCategoryEntity category: categoryEntities) {
            long parentId= category.getParentId();

            if (parentId>0)
                if(children.containsKey(parentId)) {
                    List<CheckCategoryEntity> kids= children.get(parentId);
                    if(kids!=null)
                        kids.add(category);
                    children.append(parentId, kids);
                } else {
                    List<CheckCategoryEntity> kids= new ArrayList<>();
                    kids.add(category);
                    children.append(parentId, kids);
                }
        }

        for(CheckCategoryEntity category: categoryEntities) {
            if (category.getParentId()==0 && category.getId()!=1)//no debts included here?
                if(children.get(category.getId())!=null)
                    active.add(new SuperParentCategory(category, children.get(category.getId())));
                else
                    active.add(new SuperParentCategory(category, new ArrayList<>()));
        }
        return active;
    }

    private class ParentCheckViewAdapter extends RecyclerView.Adapter<ParentCheckViewAdapter.CheckFullViewHolder> {

        private List<SuperParentCategory> mParentCategories;

        @Nullable
        private final CheckCategoryCallback mCheckCategoryCallback;

        ParentCheckViewAdapter(@Nullable CheckCategoryCallback clickCallback) {
            mCheckCategoryCallback = clickCallback;
            setHasStableIds(true);
        }

        void setItems(final List<SuperParentCategory> parentCategories) {
            if (mParentCategories == null) {
                mParentCategories = parentCategories;
                notifyItemRangeInserted(0, parentCategories.size());
            } else {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return mParentCategories.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return parentCategories.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return mParentCategories.get(oldItemPosition).getId() ==
                                parentCategories.get(newItemPosition).getId();
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        SuperParentCategory oldItem = parentCategories.get(newItemPosition);
                        SuperParentCategory newItem = mParentCategories.get(oldItemPosition);
                        return oldItem.getId() == newItem.getId()
                                && oldItem.isChecked() == newItem.isChecked();
                    }
                });
                mParentCategories = parentCategories;
                result.dispatchUpdatesTo(this);
            }
        }


        @Override
        public int getItemCount() {
            return mParentCategories == null ? 0 : mParentCategories.size();
        }

        @Override
        public long getItemId(int position) {
            return mParentCategories.get(position).getId();
        }

        private void setVisuals(SuperParentCategory superParentCategory, CheckCategoryItemBinding binding, KidCheckViewAdapter kidCheckViewAdapter){
            boolean value= superParentCategory.isChecked();

            if (superParentCategory.isChecked()){
                binding.itemCheckedBackground.setVisibility(View.VISIBLE);
                binding.itemCheckedForeground.setVisibility(View.VISIBLE);
            } else{
                binding.itemCheckedForeground.setVisibility(View.GONE);
                binding.itemCheckedBackground.setVisibility(View.GONE);
            }


            if(value)
                for (CheckCategoryEntity checkCategoryEntity: superParentCategory.getChildren())
                    checkCategoryEntity.setChecked(true);

            kidCheckViewAdapter.setItems(superParentCategory.getChildren());

        }

        @NonNull
        @Override
        public CheckFullViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CheckCategoryItemBinding binding = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.getContext()), R.layout.check_category_item,
                            parent, false);
            return new CheckFullViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull CheckFullViewHolder holder, int position) {
            SuperParentCategory parentCategory= mParentCategories.get(position);

            holder.binding.setCategory(parentCategory);
            holder.binding.executePendingBindings();

            KidCheckViewAdapter kidCheckViewAdapter = new KidCheckViewAdapter(() -> callback.sendItems(mParentCategories));

            int id= MyApplication.getDrawableId(parentCategory.getIconName());

            if(id!=0)
                Picasso.get().load(id).into(holder.binding.categoryIcon);

            setVisuals(parentCategory, holder.binding, kidCheckViewAdapter);

            if(parentCategory.getChildren()!=null && !parentCategory.getChildren().isEmpty()){
                holder.binding.kidsList.setVisibility(View.VISIBLE);

                holder.binding.kidsList.setAdapter(kidCheckViewAdapter);
                holder.binding.kidsList.setLayoutManager(new GridLayoutManager(activity, 4));
                kidCheckViewAdapter.setItems(parentCategory.getChildren());
            }else {
                holder.binding.kidsList.setVisibility(View.GONE);
            }

            if(mCheckCategoryCallback!=null)
                holder.binding.card.setOnClickListener(v ->{
                    boolean value= parentCategory.isChecked();
                    parentCategory.setChecked(!value);
                    setVisuals(parentCategory, holder.binding, kidCheckViewAdapter);

                    callback.sendItems(mParentCategories);
                });
        }

        class CheckFullViewHolder extends RecyclerView.ViewHolder {

            final CheckCategoryItemBinding binding;

            CheckFullViewHolder(CheckCategoryItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

    }

    private class KidCheckViewAdapter extends RecyclerView.Adapter<KidCheckViewAdapter.IconViewHolder> {

        private List<CheckCategoryEntity> mCheckCategoryEntitys;

        @Nullable
        private final KidToParent kidToParent;

        KidCheckViewAdapter(@Nullable KidToParent clickCallback) {
            kidToParent = clickCallback;
            setHasStableIds(true);
        }

        void setItems(final List<CheckCategoryEntity> checkedItems) {
            if (mCheckCategoryEntitys == null) {
                mCheckCategoryEntitys = checkedItems;
                notifyItemRangeInserted(0, checkedItems.size());
            } else {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return mCheckCategoryEntitys.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return checkedItems.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return mCheckCategoryEntitys.get(oldItemPosition).getId() ==
                                checkedItems.get(newItemPosition).getId();
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        CheckCategoryEntity oldItem = checkedItems.get(newItemPosition);
                        CheckCategoryEntity newItem = mCheckCategoryEntitys.get(oldItemPosition);
                        return false;/*oldItem.getId() == newItem.getId()
                                && oldItem.isChecked() == newItem.isChecked();*/
                    }
                });
                mCheckCategoryEntitys = checkedItems;
                result.dispatchUpdatesTo(this);
            }
        }


        @Override
        public int getItemCount() {
            return mCheckCategoryEntitys == null ? 0 : mCheckCategoryEntitys.size();
        }

        @Override
        public long getItemId(int position) {
            return mCheckCategoryEntitys.get(position).getId();
        }

        @Override
        public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
            CheckCategoryEntity checkedItem= mCheckCategoryEntitys.get(position);
            holder.binding.executePendingBindings();

            int id= MyApplication.getDrawableId(checkedItem.getIconName());


            if(id!=0)
                Picasso.get().load(id).into(holder.binding.pickIcon);

            setVisuals(checkedItem, holder.binding);

            if(kidToParent !=null)
                holder.binding.pickIcon.setOnClickListener(v ->{
                    if(checkedItem.isChecked())
                        checkedItem.setChecked(false);
                    else
                        checkedItem.setChecked(true);
                    setVisuals(checkedItem, holder.binding);
                    kidToParent.sendItems();
                });

        }

        @NonNull
        @Override
        public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CheckKidItemBinding binding = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.getContext()), R.layout.check_kid_item,
                            parent, false);
            return new IconViewHolder(binding);
        }

        class IconViewHolder extends RecyclerView.ViewHolder {

            final CheckKidItemBinding binding;

            IconViewHolder(CheckKidItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

        private void setVisuals(CheckCategoryEntity checkedItem, CheckKidItemBinding binding){
            if (checkedItem.isChecked()){
                binding.itemCheckedBackground.setVisibility(View.VISIBLE);
                binding.itemCheckedForeground.setVisibility(View.VISIBLE);
            } else{
                binding.itemCheckedForeground.setVisibility(View.GONE);
                binding.itemCheckedBackground.setVisibility(View.GONE);
            }

        }
    }

    public class CheckCategoryEntity extends CategoryEntity{
        private boolean checked;

        CheckCategoryEntity(long id, long parentId, String title, String iconName, int type, boolean checked) {
            super(id, parentId, title, iconName, type);
            this.checked = checked;
        }

        CheckCategoryEntity(CategoryEntity categoryEntity) {
            super(categoryEntity.getId(), categoryEntity.getParentId(), categoryEntity.getTitle(), categoryEntity.getIconName(), categoryEntity.getType());
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }
    }

    public class SuperParentCategory extends CheckCategoryEntity {

        private List<CheckCategoryEntity> children;

        SuperParentCategory(CheckCategoryEntity checkCategoryEntity, List<CheckCategoryEntity> kids) {
            super(checkCategoryEntity.getId(), checkCategoryEntity.getParentId(), checkCategoryEntity.getTitle(),
                    checkCategoryEntity.getIconName(), checkCategoryEntity.getType(), checkCategoryEntity.isChecked());
            children= kids;
        }

        public List<CheckCategoryEntity> getChildren() {
            return children;
        }

        public void setChildren(List<CheckCategoryEntity> children) {
            this.children = children;
        }
    }

    interface KidToParent{
        void sendItems();
    }
}
