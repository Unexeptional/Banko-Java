package com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.CategoryFragmentBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.db.entity.CategoryEntity;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.ui.activity.MainActivity;
import com.unexceptional.beast.banko.newVersion.ui.activity.FloatingActivity;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickIconDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.categories.PickParentCategoryDialog;
import com.unexceptional.beast.banko.newVersion.ui.fragment.BasicFragment;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CategoryViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.TransactionViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link CategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryFragment extends BasicFragment {

    private CategoryFragmentBinding mBinding;
    private CategoryViewModel viewModel;
    private CategoryEntity activeCategory;
    private boolean modify;

    public CategoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static CategoryFragment newInstance(long categoryId, boolean subMode) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.KEY_CATEGORY_ID, categoryId);
        args.putBoolean(Constants.KEY_CATEGORY_SUB_MODE, subMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(CategoryViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.category_fragment, container, false);



        if(getArguments()!=null){
            activeCategory= new CategoryEntity();

            long categoryId= getArguments().getLong(Constants.KEY_CATEGORY_ID, 0);
            mBinding.setSubMode(getArguments().getBoolean(Constants.KEY_CATEGORY_SUB_MODE));

            if(categoryId!=0)
                getCategory(categoryId);
            else
                setNewCategory();

        }

        setClicks();

        return mBinding.getRoot();
    }

    private void getCategory(long categoryId){
        viewModel.getCategory(categoryId).observe(this, categoryEntity -> {
            if(categoryEntity!=null){
                activeCategory= categoryEntity;

                modify=true;
                setCategoryVisuals();
                setTypeSpinner();
                mBinding.typeSpinner.setSelection(activeCategory.getType());
            }

        });
    }

    private void setNewCategory(){
        setNewCategoryVisual();
    }

    private void setCategoryVisuals( ){
        mBinding.setCategory(activeCategory);

        setIconVisual();
        setParentCategory(activeCategory.getParentId());

        mBinding.closedCategory.setChecked(activeCategory.isInactive());

    }

    private void setNewCategoryVisual(){
        mBinding.btnDelete.setVisibility(View.GONE);
        setTypeSpinner();
    }

    private void setClicks(){
        mBinding.floatingPad.setOnClickListener(v ->
                Objects.requireNonNull(getActivity()).onBackPressed());
        mBinding.btnSave.setOnClickListener(v -> onSave());

        mBinding.btnDelete.setOnClickListener(v -> {
            new AppExecutors().diskIO().execute(() -> {
                if(canChange())
                    showDeleteDialog();
                else
                    showSnackBar();
            });
        });

        mBinding.closedCategory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            activeCategory.setInactive(isChecked);
        });

        mBinding.parentCategory.setOnClickListener(v ->
                new AppExecutors().diskIO().execute(() -> {
                    if (canChange()) {
                        new AppExecutors().mainThread().execute(() ->
                                new PickParentCategoryDialog((FloatingActivity) getActivity(), category -> setParentCategory(category.getId()), 0).showDialog());

                    } else
                        showSnackBar();
        }));

        mBinding.pickIcon.setOnClickListener(v ->
                new PickIconDialog((FloatingActivity) getActivity(), iconName -> {
                    activeCategory.setIconName(iconName);
                    setIconVisual();
                }).showDialog());

    }

    private void setTypeSpinner(){
        Spinner spinner= mBinding.typeSpinner;
        ArrayAdapter<CharSequence> adapterDate= ArrayAdapter.
                createFromResource(Objects.requireNonNull(getActivity()),
                        R.array.CategoryType, android.R.layout.simple_spinner_item);
        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterDate);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activeCategory.setType(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void setIconVisual(){
        int id= MyApplication.getDrawableId(activeCategory.getIconName());
        if(id!=0)
            Picasso.get().load(id).into(mBinding.pickIcon);
    }

    private void setParentCategory(long categoryId){
        ViewModelProviders.of(this).get(CategoryViewModel.class).getCategory(categoryId).observe(this, categoryEntity -> {
            if (categoryEntity!=null){
                mBinding.setSubMode(true);
                activeCategory.setParentId(categoryEntity.getId());
                int id= MyApplication.getDrawableId(categoryEntity.getIconName());
                if(id!=0)
                    Picasso.get().load(id).into(mBinding.parentCategory);

            }else{
                Picasso.get().load( R.drawable.outline_category_black_24).into(mBinding.parentCategory);
                mBinding.setSubMode(false);
            }
        });
    }

    //SAVING
    private void onSave(){

        //CHECK
        if(mBinding.categoryTitle.getText()==null || mBinding.categoryTitle.getText().toString().equals("")){
            mBinding.categoryTitle.setError(getString(R.string.enter_valid_name));
            return;
        }

        if(activeCategory.getIconName()==null || activeCategory.getIconName().equals("")) {
            Toast.makeText(getActivity(), getString(R.string.pick_icon), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mBinding.getSubMode())
            if (activeCategory.getParentId()==0) {
                Toast.makeText(getActivity(), getString(R.string.no_parent_category), Toast.LENGTH_SHORT).show();
                return;
            }


        //SET
        activeCategory.setTitle(mBinding.categoryTitle.getText().toString());

        if(modify)
            viewModel.update(activeCategory);
        else
            viewModel.insert(activeCategory);

        Objects.requireNonNull(getActivity()).onBackPressed();
    }

    private void showDeleteDialog(){
        new AppExecutors().mainThread().execute(() -> new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setTitle(getString(R.string.warning_title_delete_category))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    viewModel.delete(activeCategory);
                    Objects.requireNonNull(getActivity()).onBackPressed();
                })

                .setNegativeButton(android.R.string.no, null)
                .show());
    }

    private void showSnackBar(){
        final Snackbar snackbar = Snackbar
                .make(mBinding.getRoot() , getString(R.string.category_has_transactions_or_subs), Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.show_transactions, view -> {
            Intent intent= new Intent(getActivity(), MainActivity.class);
            preferences.edit().putInt(getString(R.string.key_temp_select_main_nav), 2).apply();
            preferences.edit().putInt(getString(R.string.key_tr_list_date_option), 0).apply();
            preferences.edit().putLong(getString(R.string.key_tr_list_from_date_millis), 0).apply();
            preferences.edit().putLong(getString(R.string.key_tr_list_to_date_millis), 0).apply();

            if (activeCategory.getParentId()>0) {
                preferences.edit().putLong(getString(R.string.key_tr_list_sub_category_id_new), activeCategory.getId()).apply();
                preferences.edit().putLong(getString(R.string.key_tr_list_category_id_new), activeCategory.getParentId()).apply();
            } else {
                preferences.edit().putLong(getString(R.string.key_tr_list_category_id_new), activeCategory.getId()).apply();
                preferences.edit().putLong(getString(R.string.key_tr_list_sub_category_id_new), 0).apply();
            }

            startActivity(intent);
        });
        snackbar.show();
    }

    private boolean canChange(){
        if (activeCategory.getParentId()>0) {
            if (activeCategory.getId()>0)
                return !ViewModelProviders.of(CategoryFragment.this).get(TransactionViewModel.class).subCategoryHasTransactions(activeCategory.getId());
            else
                return true;
        } else {
            return !ViewModelProviders.of(CategoryFragment.this).get(TransactionViewModel.class).categoryHasTransactions(activeCategory.getId())
                    && !hasKids();
        }
    }

    private boolean hasKids(){
        if(activeCategory.getId()>0)
            return viewModel.hasKids(activeCategory.getId());
        else
            return false;
    }

    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_CATEGORY_FRAGMENT;
    }

}
