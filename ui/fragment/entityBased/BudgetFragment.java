package com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased;
/*
 * CALL IT DONE 1.11.2018
 */

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.lifecycle.ViewModelProviders;

import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.BudgetFragmentBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.CheckCategoryCallback;
import com.unexceptional.beast.banko.newVersion.callback.PickIconCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.BudgetEntity;
import com.unexceptional.beast.banko.newVersion.ui.activity.FloatingActivity;
import com.unexceptional.beast.banko.newVersion.ui.dialog.categories.CheckCategoryFullDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickIconDialog;
import com.unexceptional.beast.banko.newVersion.ui.fragment.BasicFragment;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.BudgetViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class BudgetFragment extends BasicFragment {


    private BudgetFragmentBinding mBinding;
    private BudgetViewModel viewModel;
    private BudgetEntity activeBudget;
    private boolean modify;
    private ArrayList<String> stringList= new ArrayList<>();

    public BudgetFragment() {

    }

    public static BudgetFragment newInstance(long budgetId) {
        BudgetFragment fragment = new BudgetFragment();
        Bundle args = new Bundle();
        args.putLong(com.unexceptional.beast.banko.newVersion.Constants.KEY_BUDGET_ID, budgetId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Has two faces depending on modify
     * when adding new u also set first account (dateOption, currency etc)
     * when modifying gives u a chance to pick default account
     */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(BudgetViewModel.class);

    }

    @NonNull
    @Override
    public String toString() {
        return Constants.TAG_BUDGET_FRAGMENT;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mBinding= DataBindingUtil.inflate(inflater, R.layout.budget_fragment, container, false);

        if(getArguments()!=null){
            activeBudget= new BudgetEntity();
            long budgetId= getArguments().getLong(Constants.KEY_BUDGET_ID, 0);

            if(budgetId!=0)
                getBudget(budgetId);
            else
                setNewBudgetVisual();

        }

        setClicks();

        return mBinding.getRoot();
    }

    private void getBudget(long budgetId){
        modify=true;
        viewModel.getBudget(budgetId).observe(this, budgetEntity -> {
            if(budgetEntity!=null){
                activeBudget= budgetEntity;
                setBudgetVisuals();
            }

        });
    }

    private void setBudgetVisuals( ){
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        mBinding.setBudget(activeBudget);

        String[] categoryIdsSA= activeBudget.getCategoryIds().split("\\s*, \\s*");
        stringList = new ArrayList<>(Arrays.asList(categoryIdsSA));

        mBinding.budgetTextCategories.setText(String.format(Locale.getDefault(), "%s (%s)", getString(R.string.categories),  String.valueOf(stringList.size())));

        mBinding.budgetMaxAmount.setText(MyApplication.money.format(activeBudget.getAmount()));

        setDateOptionSpinner();

        setIconVisual();


        if (activeBudget.getStartDate()!=null)
            mBinding.productStartDate.setText(dateFormatGmt.format(activeBudget.getStartDate()));
        if (activeBudget.getEndDate()!=null)
            mBinding.productEndDate.setText(dateFormatGmt.format(activeBudget.getEndDate()));

    }

    private void setIconVisual(){
        int id = MyApplication.getDrawableId(activeBudget.getIconName());
        if (id != 0)
            Picasso.get().load(id).into(mBinding.icon);
    }

    private void setNewBudgetVisual(){
        mBinding.btnDelete.setVisibility(View.GONE);

        mBinding.budgetMaxAmount.setText("0");
    }

    private void setClicks(){
        mBinding.productStartDate.setOnClickListener(v -> showDatePicker(false));

        mBinding.productEndDate.setOnClickListener(v -> showDatePicker(true));

        mBinding.budgetTextCategories.setOnClickListener(v -> {
            CheckCategoryCallback callback= itemsIds -> {
                stringList.clear();

                for (CheckCategoryFullDialog.SuperParentCategory parent: itemsIds) {
                    if(parent.isChecked())
                        stringList.add(String.valueOf(parent.getId()));
                    else if(parent.getChildren()!=null && !parent.getChildren().isEmpty()){
                        for (CheckCategoryFullDialog.CheckCategoryEntity kid: parent.getChildren()){
                            if (kid.isChecked())
                                stringList.add(String.valueOf(kid.getId()));
                        }
                    }
                }

                activeBudget.setCategoryIds( TextUtils.join(", ", stringList));
                mBinding.budgetTextCategories.setText(String.format(Locale.getDefault(), "%s (%s)", getString(R.string.categories), String.valueOf(stringList.size())));

            };

            new CheckCategoryFullDialog((FloatingActivity) getActivity(), callback, stringList).showDialog();
        });

        mBinding.floatingPad.setOnClickListener(v -> Objects.requireNonNull(getActivity()).onBackPressed());

        mBinding.btnSave.setOnClickListener(v -> onSave());

        mBinding.btnDelete.setOnClickListener(v -> showDeleteDialog());

        mBinding.icon.setOnClickListener(v -> {
            PickIconCallback pickIconCallback= iconName -> {
                activeBudget.setIconName(iconName);
                setIconVisual();
            };

            new PickIconDialog((FloatingActivity) getActivity(), pickIconCallback).showDialog();
        });

        mBinding.closedProduct.setOnCheckedChangeListener((buttonView, isChecked) ->
                activeBudget.setInActive(isChecked));

        setDateOptionSpinner();
    }

    private void showDatePicker(boolean endDateMode){
        Calendar calendar= Calendar.getInstance();


        DatePickerDialog.OnDateSetListener dateSetListener = (view1, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            Date date = calendar.getTime();

            setDates(endDateMode, date);


        };


        new DatePickerDialog(Objects.requireNonNull(getActivity()), dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void setDates(boolean endDateMode, Date date){
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        if (endDateMode){
            if(activeBudget.getStartDate()!=null)
                if (date.before(activeBudget.getStartDate())){
                    mBinding.productEndDateLayout.setError(getString(R.string.error_open_date_after_close_date));
                    date=null;
                }

            activeBudget.setEndDate(date);
            if(date!=null){
                mBinding.productEndDateLayout.setError(null);
                mBinding.productEndDate.setText(dateFormatGmt.format(date));
            }
        } else{
            if(activeBudget.getEndDate()!=null)
                if (date.after(activeBudget.getEndDate())){
                    mBinding.productStartDateLayout.setError(getString(R.string.error_open_date_after_close_date));
                    date=null;
                }


            activeBudget.setStartDate(date);
            if(date!=null){
                mBinding.productStartDateLayout.setError(null);
                mBinding.productStartDate.setText(dateFormatGmt.format(date));
            }
        }
    }

    private void setDateOptionSpinner(){
        Spinner spinner= mBinding.dateOptionSpinner;
        ArrayAdapter<CharSequence> adapterDate= ArrayAdapter.
                createFromResource(Objects.requireNonNull(getActivity()),
                        R.array.BudgetDateOptions, android.R.layout.simple_spinner_item);
        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterDate);

        spinner.setSelection(activeBudget.getDateOption());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activeBudget.setDateOption((short)position);

                if(position==0)
                    mBinding.datesLayout.setVisibility(View.VISIBLE);
                else
                    mBinding.datesLayout.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    //SAVING
    private void onSave(){

        //CHECK
        if(mBinding.budgetTitle.getText()==null || mBinding.budgetTitle.getText().toString().equals("")){
            mBinding.budgetTitle.setError(getString(R.string.enter_valid_name));
            return;
        }

        if(mBinding.budgetMaxAmount.getText()==null || mBinding.budgetMaxAmount.getText().toString().equals("")){
            mBinding.budgetMaxAmount.setError(getString(R.string.enter_valid_balance));
            return;
        }

        if(activeBudget.getDateOption()==0)
            if (activeBudget.getStartDate()==null || activeBudget.getEndDate()==null) {
                Toast.makeText(getActivity(), getString(R.string.warning_set_date), Toast.LENGTH_SHORT).show();
                return;
            }

        if (stringList.isEmpty()){
            Toast.makeText(getActivity(), getString(R.string.warning_pick_at_least_one_category), Toast.LENGTH_SHORT).show();
            return;
        }

        if(activeBudget.getIconName()== null || activeBudget.getIconName().equals("")){
            Toast.makeText(getActivity(), getString(R.string.warning_set_icon), Toast.LENGTH_SHORT).show();
            return;
        }


        String maxAmount= mBinding.budgetMaxAmount.getText().toString().replaceAll("\\s","").replace(",", ".");
        double maxAmountDouble= (double) Math.round(Double.parseDouble(maxAmount) * 100) / 100;

        if (maxAmountDouble==0) {
            Toast.makeText(getActivity(), getString(R.string.warning_set_budget_amount), Toast.LENGTH_SHORT).show();
            return;
        }

        //SET
        activeBudget.setAmount(maxAmountDouble);
        activeBudget.setTitle(mBinding.budgetTitle.getText().toString());

        if(modify)
            viewModel.update(activeBudget);
        else
            viewModel.insert(activeBudget);

        Objects.requireNonNull(getActivity()).onBackPressed();
    }

    private void showDeleteDialog(){
        new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setTitle(getString(R.string.warning_title_delete_product))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    viewModel.delete(activeBudget);
                    Objects.requireNonNull(getActivity()).onBackPressed();
                })

                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}