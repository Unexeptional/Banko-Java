package com.unexceptional.beast.banko.newVersion.ui.dialog.categories;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.PickCategoryCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.CategoryEntity;
import com.unexceptional.beast.banko.newVersion.ui.activity.SecondActivity;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CategoryListViewModel;

import java.util.List;

public class PickParentCategoryDialog extends AbstractPickCategoryDialog {

    private int type;

    public PickParentCategoryDialog(AppCompatActivity activity, PickCategoryCallback callback, int type) {
        this.activity = activity;
        this.callback = callback;
        this.type=type;
    }

    public void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View dialogView = View.inflate(activity, R.layout.dialog_pick_parent_category, null);
        builder.setView(dialogView);

        //bind views
        RecyclerView recyclerView= dialogView.findViewById(R.id.pick_single);
        Button oppositeType= dialogView.findViewById(R.id.show_opposite_type);
        Button manageCat= dialogView.findViewById(R.id.manage_categories);

        //setRecycler
        PickCategoryViewAdapter adapter= new PickCategoryViewAdapter(callback);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 3));

        manageCat.setOnClickListener(v -> {
            Intent intent= new Intent(activity, SecondActivity.class);
            intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_CATEGORY_LIST_FRAGMENT);
            activity.startActivity(intent);
        });

        if(type>0){
            oppositeType.setVisibility(View.VISIBLE);
            if(type==1)
                oppositeType.setText(activity.getString(R.string.incomes));
            else
                oppositeType.setText(activity.getString(R.string.expenses));

            oppositeType.setOnClickListener(v -> {
                type= type == 1 ? 2:1;
                if(type==1)
                    oppositeType.setText(activity.getString(R.string.incomes));
                else
                    oppositeType.setText(activity.getString(R.string.expenses));

                setLiveData(adapter);
            });
        }
        setLiveData(adapter);

        dialog = builder.create();
        dialog.show();
    }

    private void setLiveData(PickCategoryViewAdapter adapter){
        LiveData<List<CategoryEntity>> liveData;

        if(type==1)
            liveData= ViewModelProviders.of(activity).get(CategoryListViewModel.class).getExpenseCategories();
        else if (type==2)
            liveData= ViewModelProviders.of(activity).get(CategoryListViewModel.class).getIncomeCategories();
        else
            liveData= ViewModelProviders.of(activity).get(CategoryListViewModel.class).getParentCategories();

        getItems(liveData, adapter);
    }

    private void getItems(LiveData<List<CategoryEntity>> liveData, PickCategoryViewAdapter adapter){
        liveData.observe(activity, categories -> {
            if (categories != null) {
                if (!PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(activity.getString(R.string.key_allow_debts), true)) {
                    try {
                        categories.remove(0);
                    }catch (Exception e){

                    }

                }

                //liveData.removeObservers(activity);
                adapter.setCategoryList(categories);
            }
        });

    }
}