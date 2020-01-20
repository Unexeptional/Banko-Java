package com.unexceptional.beast.banko.newVersion.ui.fragment.dateNavigation;
/*
 * CALL IT DONE 1.11.2018
 */

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.FragmentDateOptionsChange2Binding;
import com.unexceptional.beast.banko.newVersion.callback.DateNavigationCallback;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public abstract class DateNavigationFragment extends Fragment {

    public enum Host{
        TRANSACTION_LIST,
        BUDGET_LIST,
        TASK_LIST,
        CHARTS
    }

    protected Host host;

    private DateNavigationCallback dateNavigationCallback;

    private FragmentDateOptionsChange2Binding mBinding;

    protected SharedPreferences preferences;

    protected Calendar fromCalendar, toCalendar;
    protected long fromDate, toDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.fragment_date_options_change_2,container, false);

        setClicks();
        return mBinding.getRoot();
    }

    private void setClicks(){
        mBinding.dateNavText.setOnClickListener(view -> showDateOptions());

        mBinding.dateNavLeft.setOnClickListener(view -> navClick(-1));


        mBinding.dateNavRight.setOnClickListener(view -> navClick(1));
    }


    protected abstract void navClick(int value);

    /**
     * sets host to transaction list and sets calendars
     */
    public void setHostTransactionList(DateNavigationCallback dateNavigationCallback){
        this.dateNavigationCallback = dateNavigationCallback;

        host= Host.TRANSACTION_LIST;

        fromDate= preferences.getLong(getString(R.string.key_tr_list_from_date_millis), 0);
        toDate= preferences.getLong(getString(R.string.key_tr_list_to_date_millis), 0);

        fromCalendar= Calendar.getInstance();
        fromCalendar.setTime(new Date(fromDate));

        toCalendar= Calendar.getInstance();
        toCalendar.setTime(new Date(toDate));

        setVisualNavigation();
    }

    public void setHostBudgetList(DateNavigationCallback dateNavigationCallback){
        this.dateNavigationCallback = dateNavigationCallback;

        host= Host.BUDGET_LIST;

        fromDate= preferences.getLong(getString(R.string.key_budget_list_from_date_millis), 0);
        toDate= preferences.getLong(getString(R.string.key_budget_list_to_date_millis), 0);

        fromCalendar= Calendar.getInstance();
        fromCalendar.setTime(new Date(fromDate));

        toCalendar= Calendar.getInstance();
        toCalendar.setTime(new Date(toDate));

        setVisualNavigation();
    }

    public void setHostTaskList(DateNavigationCallback dateNavigationCallback){
        this.dateNavigationCallback = dateNavigationCallback;

        host= Host.TASK_LIST;

        fromDate= preferences.getLong(getString(R.string.key_task_list_from_date_millis), 0);
        toDate= preferences.getLong(getString(R.string.key_task_list_to_date_millis), 0);

        fromCalendar= Calendar.getInstance();
        fromCalendar.setTime(new Date(fromDate));

        toCalendar= Calendar.getInstance();
        toCalendar.setTime(new Date(toDate));

        setVisualNavigation();
    }



    public void setHostCharts(DateNavigationCallback dateNavigationCallback){
        this.dateNavigationCallback = dateNavigationCallback;

        host= Host.CHARTS;

        fromDate= preferences.getLong(getString(R.string.key_charts_from_date_millis), 0);
        toDate= preferences.getLong(getString(R.string.key_charts_to_date_millis), 0);

        fromCalendar= Calendar.getInstance();
        fromCalendar.setTime(new Date(fromDate));

        toCalendar= Calendar.getInstance();
        toCalendar.setTime(new Date(toDate));

        setVisualNavigation();
    }


    protected int getContent(){
        int selected=0;
        switch (host){
            case TRANSACTION_LIST:
                selected= preferences.getInt(getString(R.string.key_tr_list_date_option), 0);
                break;
            case BUDGET_LIST:
                selected= preferences.getInt(getString(R.string.key_budget_list_date_option), 0);
                break;
            case TASK_LIST:
                selected= preferences.getInt(getString(R.string.key_task_list_date_option), 0);
                break;
            case CHARTS:
                selected= preferences.getInt(getString(R.string.key_charts_date_option), 0);
                break;
        }
        return selected;
    }

    /**
     * Setting visibility of arrows and text in the middle according to preferences
     */
    void setVisualNavigationHelper(int visibility, String text){
        mBinding.dateNavLeft.setVisibility(visibility);
        mBinding.dateNavRight.setVisibility(visibility);
        mBinding.dateNavText.setText(text);
    }

    protected abstract void setVisualNavigation();

    /**
     * Dialog to choose date option
     */
    protected abstract void showDateOptions();

    void showDatePicker(final Calendar calendar){

        DatePickerDialog.OnDateSetListener onDateSetListener= (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            updateDate();
        };
        new DatePickerDialog(Objects.requireNonNull(getActivity()), onDateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Does the magic trick - playing around transaction time with setting first calendar to start of day,
     * second calendar to end of the day
     * and then sending that to preferences
     *
     * Takes care of refreshing host as well
     */
    void updateDate(){
        if (fromCalendar != null && toCalendar != null) {
            fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
            fromCalendar.set(Calendar.MINUTE, 0);
            fromCalendar.set(Calendar.SECOND, 0);
            fromCalendar.set(Calendar.MILLISECOND, 0);

            toCalendar.set(Calendar.HOUR_OF_DAY, 23);
            toCalendar.set(Calendar.MINUTE, 59);
            toCalendar.set(Calendar.SECOND, 59);
            toCalendar.set(Calendar.MILLISECOND, 999);

            fromDate = fromCalendar.getTimeInMillis();
            toDate = toCalendar.getTimeInMillis();
        } else {
            fromDate = 0;
            toDate = 0;
        }

        switch (host){
            case TRANSACTION_LIST:
                preferences.edit().putLong(getString(R.string.key_tr_list_from_date_millis),fromDate).apply();
                preferences.edit().putLong(getString(R.string.key_tr_list_to_date_millis),toDate).apply();
                break;
            case BUDGET_LIST:
                preferences.edit().putLong(getString(R.string.key_budget_list_from_date_millis),fromDate).apply();
                preferences.edit().putLong(getString(R.string.key_budget_list_to_date_millis),toDate).apply();
                break;
            case TASK_LIST:
                preferences.edit().putLong(getString(R.string.key_task_list_from_date_millis),fromDate).apply();
                preferences.edit().putLong(getString(R.string.key_task_list_to_date_millis),toDate).apply();
                break;
            case CHARTS:
                preferences.edit().putLong(getString(R.string.key_charts_from_date_millis),fromDate).apply();
                preferences.edit().putLong(getString(R.string.key_charts_to_date_millis),toDate).apply();
                break;
        }

        dateNavigationCallback.refreshHost();
        setVisualNavigation();
    }
}