package com.unexceptional.beast.banko.newVersion.ui.fragment.dateNavigation;
/*
 * CALL IT DONE 1.11.2018
 */

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.newVersion.Constants;

import java.util.Calendar;
import java.util.Objects;

public class TaskDateNavigationFragment extends DateNavigationFragment {

    public TaskDateNavigationFragment() {
    }

    public static TaskDateNavigationFragment newInstance(Bundle bundle) {
        TaskDateNavigationFragment fragment = new TaskDateNavigationFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void navClick(int value){
        switch (getContent()){
            case 1:{// MONTH
                fromCalendar.add(Calendar.MONTH, value);
                toCalendar.add(Calendar.MONTH, value);
                toCalendar.set(Calendar.DAY_OF_MONTH, toCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
            }
            case 2:{//year
                fromCalendar.add(Calendar.YEAR, value);
                toCalendar.add(Calendar.YEAR, value);
                toCalendar.set(Calendar.DAY_OF_YEAR, toCalendar.getActualMaximum(Calendar.DAY_OF_YEAR));
                break;
            }

        }
        updateDate();
    }

    /**
     * Setting visibility of arrows and text in the middle according to preferences
     */
    protected void setVisualNavigation(){

        switch (getContent()){
            case 0:{ //ONE TIME
                setVisualNavigationHelper(View.GONE, getString(R.string.one_time_tasks));
                break;
            }
            case 1:{// MONTH
                setVisualNavigationHelper(View.VISIBLE, String.format("%s  %s",
                        Constants.SDF_UI_MONTH.format(fromCalendar.getTime()),
                        Constants.SDF_UI_YEAR.format(fromCalendar.getTime())));
                break;
            }
            case 2:{//year
                setVisualNavigationHelper(View.VISIBLE, Constants.SDF_UI_YEAR.format(fromCalendar.getTime()));
                break;
            }
        }
    }

    /**
     * Dialog to choose date option
     */
    protected void showDateOptions(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        mBuilder.setTitle(R.string.pick_date_range);
        String[] dateOptions = getResources().getStringArray(R.array.TaskDateOptions);
        mBuilder.setSingleChoiceItems(dateOptions, getContent() ,
                (dialogInterface, i) -> {

                        preferences.edit().putInt(getString(R.string.key_task_list_date_option), i).apply();

                    switch (i) {
                        case 0:{ //one time
                            fromCalendar=null;
                            toCalendar=null;
                            break;
                        }
                        case 1:{
                            fromCalendar= Calendar.getInstance();
                            fromCalendar.set(Calendar.DAY_OF_MONTH,  1);
                            toCalendar= Calendar.getInstance();
                            toCalendar.set(Calendar.DATE, toCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                            break;
                        }
                        case 2:{
                            fromCalendar= Calendar.getInstance();
                            fromCalendar.set(Calendar.DAY_OF_YEAR,  1);
                            toCalendar= Calendar.getInstance();
                            toCalendar.set(Calendar.DAY_OF_YEAR, toCalendar.getActualMaximum(Calendar.DAY_OF_YEAR));
                            break;
                        }
                    }
                    updateDate();
                    dialogInterface.dismiss();
                });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

}