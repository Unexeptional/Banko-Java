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

public class BudgetDateNavigationFragment extends DateNavigationFragment {

    public BudgetDateNavigationFragment() {
    }

    public static BudgetDateNavigationFragment newInstance(Bundle bundle) {
        BudgetDateNavigationFragment fragment = new BudgetDateNavigationFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void navClick(int value){
        switch (getContent()){

            case 1:{//DAY
                fromCalendar.add(Calendar.DAY_OF_YEAR, value);
                toCalendar.add(Calendar.DAY_OF_YEAR, value);
                break;
            }
            case 2:{//week
                fromCalendar.add(Calendar.WEEK_OF_YEAR, value);
                toCalendar.add(Calendar.WEEK_OF_YEAR, value);
                break;
            }
            case 3:{// MONTH
                fromCalendar.add(Calendar.MONTH, value);
                toCalendar.add(Calendar.MONTH, value);
                toCalendar.set(Calendar.DAY_OF_MONTH, toCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
            }
            case 4:{//year
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
                setVisualNavigationHelper(View.GONE, getString(R.string.one_time_budgets));
                break;
            }
            case 1:{//DAY
                setVisualNavigationHelper(View.VISIBLE, Constants.SDF_UI_DATE.format(fromCalendar.getTime()));
                break;
            }
            case 2:{//week
                setVisualNavigationHelper(View.VISIBLE, String.format("%s -> %s",
                        Constants.SDF_UI_WEEK.format(fromCalendar.getTime()),
                        Constants.SDF_UI_WEEK.format(toCalendar.getTime())));
                break;
            }
            case 3:{// MONTH
                setVisualNavigationHelper(View.VISIBLE, String.format("%s  %s",
                        Constants.SDF_UI_MONTH.format(fromCalendar.getTime()),
                        Constants.SDF_UI_YEAR.format(fromCalendar.getTime())));
                break;
            }
            case 4:{//year
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
        String[] dateOptions = getResources().getStringArray(R.array.BudgetDateOptions);
        mBuilder.setSingleChoiceItems(dateOptions, getContent() ,
                (dialogInterface, i) -> {

                    if (host == Host.BUDGET_LIST) {
                        preferences.edit().putInt(getString(R.string.key_budget_list_date_option), i).apply();
                    }

                    switch (i) {
                        case 0:{ //EVERYTHING
                            fromCalendar=null;
                            toCalendar=null;
                            break;
                        }
                        case 1:{//DAY
                            fromCalendar= Calendar.getInstance();
                            toCalendar= Calendar.getInstance();
                            break;
                        }
                        case 2:{// WEEK
                            fromCalendar= Calendar.getInstance();
                            fromCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                            toCalendar= Calendar.getInstance();
                            toCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                            break;
                        }
                        case 3:{//MONTH
                            fromCalendar= Calendar.getInstance();
                            fromCalendar.set(Calendar.DAY_OF_MONTH,  1);
                            toCalendar= Calendar.getInstance();
                            toCalendar.set(Calendar.DATE, toCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                            break;
                        }
                        case 4:{//YEAR
                            fromCalendar= Calendar.getInstance();
                            fromCalendar.set(Calendar.DAY_OF_YEAR,  1);
                            toCalendar= Calendar.getInstance();
                            toCalendar.set(Calendar.DAY_OF_YEAR, toCalendar.getActualMaximum(Calendar.DAY_OF_YEAR));
                            break;
                        }

                        case 5:{//PICK DATES
                            fromCalendar= Calendar.getInstance();
                            toCalendar= Calendar.getInstance();

                            showDatePicker(toCalendar);
                            showDatePicker(fromCalendar);
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