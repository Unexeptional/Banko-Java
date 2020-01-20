package com.unexceptional.beast.banko.newVersion.ui.dialog.categories;

import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.newVersion.callback.PickCategoryCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.CategoryEntity;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CategoryListViewModel;

import java.util.List;

public class PickSubCategoryDialog extends AbstractPickCategoryDialog{

    private long parentId;

    public PickSubCategoryDialog(AppCompatActivity activity, PickCategoryCallback callback, long parentId) {
        this.activity = activity;
        this.callback = callback;
        this.parentId=parentId;
    }

    public void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View dialogView = View.inflate(activity, R.layout.dialog_pick_single2, null);
        builder.setView(dialogView);

        //bind views
        RecyclerView recyclerView= dialogView.findViewById(R.id.pick_single);
        Button selectNOne= dialogView.findViewById(R.id.select_none);
        selectNOne.setVisibility(View.VISIBLE);
        //setRecycler
        PickCategoryViewAdapter adapter= new PickCategoryViewAdapter(callback);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 3));

        selectNOne.setOnClickListener(v -> {
            dialog.dismiss();
        });

        LiveData<List<CategoryEntity>> liveData;

        liveData= ViewModelProviders.of(activity).get(CategoryListViewModel.class).getChildCategories(parentId);


        liveData.observe(activity, categories -> {
            if (categories != null) {
               // liveData.removeObservers(activity);
                adapter.setCategoryList(categories);
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    public void hideDialog(){
        dialog.dismiss();
    }
}