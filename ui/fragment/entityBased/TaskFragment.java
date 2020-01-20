package com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.TaskFragmentBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.PickBankCallback;
import com.unexceptional.beast.banko.newVersion.callback.PickProductCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.TaskEntity;
import com.unexceptional.beast.banko.newVersion.ui.activity.FloatingActivity;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickBankDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickProductDialog;
import com.unexceptional.beast.banko.newVersion.ui.fragment.BasicFragment;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.ProductViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.TaskViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link TaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskFragment extends BasicFragment {

    private TaskFragmentBinding mBinding;
    private TaskViewModel viewModel;
    private TaskEntity activeTask;
    private boolean modify;


    public TaskFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static TaskFragment newInstance(long taskId, int dateOption) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.KEY_TASK_ID, taskId);
        args.putInt(Constants.KEY_DATE_OPTION, dateOption);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
        viewModel = ViewModelProviders.of(this).get(TaskViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.task_fragment, container, false);

        if(getArguments()!=null){
            activeTask= new TaskEntity();

            long taskId= getArguments().getLong(Constants.KEY_TASK_ID, 0);

            if(taskId!=0)
                getTask(taskId);
            else
                setNewTaskVisual();

        }

        setClicks();

        return mBinding.getRoot();
    }

    private void getTask(long taskId){

        viewModel.getTask(taskId).observe(this, taskEntity -> {
            if(taskEntity!=null){
                activeTask= taskEntity;
                modify=true;
                setTaskVisuals();
            }

        });
    }

    private void setTaskVisuals( ){
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        mBinding.setTask(activeTask);

        //amounts
        mBinding.taskBalance.setText(MyApplication.money.format(activeTask.getAmount()));
        mBinding.actualIntAmount.setText(String.valueOf(activeTask.getAmount()));

        //month last day
        mBinding.monthLastDay.setText(String.valueOf(activeTask.getMonthLastDay()));

        setProduct(activeTask.getProductId());

        //dates
        if (activeTask.getStartDate()!=null)
            mBinding.taskStartDate.setText(dateFormatGmt.format(activeTask.getStartDate()));
        if (activeTask.getEndDate()!=null)
            mBinding.taskEndDate.setText(dateFormatGmt.format(activeTask.getEndDate()));

        //spinners
        setDateOptionSpinner();
        setTaskTypeSpinner();
        mBinding.dateOptionSpinner.setSelection(activeTask.getDateOption());
        mBinding.taskTypeSpinner.setSelection(activeTask.getTaskType()-1);
    }

    private void setNewTaskVisual(){
        if(getArguments()!=null)
            activeTask.setDateOption(getArguments().getInt(Constants.KEY_DATE_OPTION, 0));

        mBinding.btnDelete.setVisibility(View.GONE);
        setDateOptionSpinner();
        mBinding.dateOptionSpinner.setSelection(activeTask.getDateOption());
        activeTask.setMonthLastDay(31);
        mBinding.monthLastDay.setText(getString(R.string.max));
        setTaskTypeSpinner();
    }

    private void setClicks(){
        mBinding.taskStartDate.setOnClickListener(v -> showDatePicker(false));
        mBinding.taskEndDate.setOnClickListener(v -> showDatePicker(true));

        mBinding.floatingPad.setOnClickListener(v ->
                Objects.requireNonNull(getActivity()).onBackPressed());
        mBinding.btnSave.setOnClickListener(v -> onSave());

        mBinding.btnDelete.setOnClickListener(v -> showDeleteDialog());

        mBinding.pickProduct.setOnClickListener(v -> {
            PickProductCallback productCallback= product -> {
                if(product.getId()>0){
                    setProduct(product.getId());
                }
            };

            if(preferences.getBoolean(getString(R.string.key_show_bank_list), false)){
                PickBankCallback fromBankCallback= bank -> {
                    if(bank.getId()>0)
                        new PickProductDialog((FloatingActivity) getActivity(), productCallback, bank.getId()).showDialog();
                };

                new PickBankDialog((FloatingActivity) getActivity(), fromBankCallback, true).showDialog();

            } else
                new PickProductDialog((FloatingActivity) getActivity(), productCallback,0).showDialog();

        });

        mBinding.plus.setOnClickListener(v ->{
            int amount=(int) activeTask.getAmount();
            activeTask.setAmount(amount+1);
            mBinding.actualIntAmount.setText(String.valueOf(activeTask.getAmount()));
        });

        mBinding.minus.setOnClickListener(v ->{
            if(activeTask.getAmount()>0) {
                activeTask.setAmount(activeTask.getAmount()-1);
                mBinding.actualIntAmount.setText(String.valueOf(activeTask.getAmount()));
            }
        });

        mBinding.taskTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                activeTask.setTitle(s.toString());
            }
        });

        mBinding.taskDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                activeTask.setDescription(s.toString());
            }
        });

        //month last day
        mBinding.plusMonth.setOnClickListener(v ->{
            int monthLastDay= activeTask.getMonthLastDay();
            if (monthLastDay<30){
                activeTask.setMonthLastDay(monthLastDay+1);
                mBinding.monthLastDay.setText(String.valueOf(activeTask.getMonthLastDay()));
            }else {
                activeTask.setMonthLastDay(31);
                mBinding.monthLastDay.setText(getString(R.string.max));
            }

        });

        mBinding.minusMonth.setOnClickListener(v ->{
            if(activeTask.getMonthLastDay()>2) {
                activeTask.setMonthLastDay(activeTask.getMonthLastDay()-1);
                mBinding.monthLastDay.setText(String.valueOf(activeTask.getMonthLastDay()));
            }
        });

        mBinding.maxMonth.setOnClickListener(v ->{
            activeTask.setMonthLastDay(31);
            mBinding.monthLastDay.setText(getString(R.string.max));
        });
    }

    private void setBankVisual(){
        int id = MyApplication.getBankIconId(activeTask.getBankId());
        if (id != 0){
            mBinding.pickProduct.setBackgroundColor(getResources().getColor(R.color.white));
            mBinding.pickProduct.setCompoundDrawablesWithIntrinsicBounds(0,id,0,0);
        }else {
            mBinding.pickProduct.setBackgroundColor(getResources().getColor(R.color.red));
            mBinding.pickProduct.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.baseline_account_balance_black_36,0,0);
        }
    }

    private void setProduct(long productId){
        ViewModelProviders.of(this).get(ProductViewModel.class).getProduct(productId).observe(this, productEntity -> {
            if (productEntity!=null) {
                activeTask.setProductId(productEntity.getId());
                activeTask.setBankId(productEntity.getBankId());
                setBankVisual();
                mBinding.pickProduct.setText(productEntity.getTitle());
            }else {
                activeTask.setProductId(0);
                activeTask.setBankId(0);
                mBinding.pickProduct.setText(getString(R.string.pick_account));
            }
        });
    }

    private void showDatePicker(boolean endDateMode){
        Calendar calendar= Calendar.getInstance(TimeZone.getTimeZone("UTC"));


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
            if(activeTask.getStartDate()!=null)
                if (date.before(activeTask.getStartDate())){
                    mBinding.taskEndDateLayout.setError(getString(R.string.error_open_date_after_close_date));
                    date=null;
                }

            activeTask.setEndDate(date);
            if(date!=null){
                mBinding.taskEndDateLayout.setError(null);
                mBinding.taskEndDate.setText(dateFormatGmt.format(date));
            }
        } else{
            if(activeTask.getEndDate()!=null)
                if (date.after(activeTask.getEndDate())){
                    mBinding.taskStartDateLayout.setError(getString(R.string.error_open_date_after_close_date));
                    date=null;
                }


            activeTask.setStartDate(date);
            if(date!=null){
                mBinding.taskStartDateLayout.setError(null);
                mBinding.taskStartDate.setText(dateFormatGmt.format(date));
            }
        }
    }

    private void setDateOptionSpinner(){
        Spinner spinner= mBinding.dateOptionSpinner;
        ArrayAdapter<CharSequence> adapterDate= ArrayAdapter.
                createFromResource(Objects.requireNonNull(getActivity()),
                        R.array.TaskDateOptions, android.R.layout.simple_spinner_item);
        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterDate);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activeTask.setDateOption(position);
                mBinding.setTask(activeTask);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setTaskTypeSpinner(){
        Spinner spinner= mBinding.taskTypeSpinner;
        ArrayAdapter<CharSequence> adapterDate= ArrayAdapter.
                createFromResource(Objects.requireNonNull(getActivity()),
                        R.array.TaskTypeOptions, android.R.layout.simple_spinner_item);
        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterDate);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activeTask.setTaskType((short)(position+1));
                mBinding.setTask(activeTask);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }



    //SAVING
    private void onSave(){
        //CHECK
        if(mBinding.taskTitle.getText()==null || mBinding.taskTitle.getText().toString().equals("")){
            mBinding.taskTitle.setError(getString(R.string.enter_valid_name));
            return;
        }

        if (activeTask.getDateOption()==0){
            if (activeTask.getStartDate()==null){
                mBinding.taskStartDate.setError(getString(R.string.pick_date));
                return;
            }
            if (activeTask.getEndDate()==null){
                mBinding.taskEndDate.setError(getString(R.string.pick_date));
                return;
            }
        }else {
            activeTask.setStartDate(null);
            activeTask.setEndDate(null);
        }

        if (activeTask.getDateOption() != 1)
            activeTask.setMonthLastDay(0);


        switch (activeTask.getTaskType()){
            case 1:

                break;

            case 2:
                if(activeTask.getAmount()==0){
                    mBinding.actualIntAmount.setError(getString(R.string.enter_valid_balance));
                    return;
                }
                break;

            case 3:
                if(mBinding.taskBalance.getText()==null || mBinding.taskBalance.getText().toString().equals("")){
                    mBinding.taskBalance.setError(getString(R.string.enter_valid_balance));
                    return;
                }

                String balance= mBinding.taskBalance.getText().toString().replaceAll("\\s","").replace(",", ".");
                activeTask.setAmount((double) Math.round(Double.parseDouble(balance) * 100) / 100);
                break;
        }

        if(activeTask.getProductId()==0){
            mBinding.pickProduct.setBackgroundColor(getResources().getColor(R.color.red));
            return;
        }

        //save
        if(modify)
            updateTaskRoom();
        else
           createTaskRoom();

    }

    private void createTaskRoom(){
        viewModel.insert(activeTask);
        Objects.requireNonNull(getActivity()).onBackPressed();
    }

    private void updateTaskRoom(){
        viewModel.update(activeTask);
        Objects.requireNonNull(getActivity()).onBackPressed();
    }

    private void showDeleteDialog(){
        new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setTitle(getString(R.string.warning_title_delete_task))
                .setPositiveButton(android.R.string.yes, (dialog, which) ->
                        deleteTaskRoom())

                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void deleteTaskRoom(){
        viewModel.delete(activeTask);
        Objects.requireNonNull(getActivity()).onBackPressed();
    }

    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_TASK_FRAGMENT;
    }

}
