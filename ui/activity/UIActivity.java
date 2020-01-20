package com.unexceptional.beast.banko.newVersion.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.newVersion.Constants;

//activity showing "drawer"
public abstract class UIActivity extends BasicActivity {

    protected DrawerLayout drawerLayout;
    protected ImageButton filter;
    protected ImageButton expense;
    protected ImageButton income;
    protected ImageButton transfer;
    protected View drawerPin;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drawerLayout= findViewById(R.id.drawer_layout);
        //nie wyłącza światła
        //drawerLayout.setScrimColor(Color.TRANSPARENT);
        //drawerLayout.setStatusBarBackgroundColor(Color.TRANSPARENT);
        filter= findViewById(R.id.filter);
        expense= findViewById(R.id.new_expense);
        income= findViewById(R.id.new_income);
        transfer= findViewById(R.id.new_transfer);
        drawerPin= findViewById(R.id.drawer_pin);
    }

    protected void setClicks(){
        expense.setOnClickListener(v -> {
            newExpense();
            drawerLayout.closeDrawers();
        });
        income.setOnClickListener(v -> {
            newIncome();
            drawerLayout.closeDrawers();
        });
        transfer.setOnClickListener(v -> {
            newTransfer();
            drawerLayout.closeDrawers();
        });

        drawerPin.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));
    }

    private void newExpense(){
        Intent intent= new Intent(UIActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_TRANSACTION_FRAGMENT);
        intent.putExtra(Constants.KEY_TRANSACTION_SHORTCUT_MODE, false);
        intent.putExtra(Constants.KEY_TRANSACTION_TYPE, 1);

        startActivity(intent);
    }


    private void newIncome(){
        Intent intent= new Intent(UIActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_TRANSACTION_FRAGMENT);
        intent.putExtra(Constants.KEY_TRANSACTION_SHORTCUT_MODE, false);
        intent.putExtra(Constants.KEY_TRANSACTION_TYPE, 2);

        startActivity(intent);
    }

    private void newTransfer(){
        Intent intent= new Intent(UIActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_TRANSACTION_FRAGMENT);
        intent.putExtra(Constants.KEY_TRANSACTION_SHORTCUT_MODE, false);
        intent.putExtra(Constants.KEY_TRANSACTION_TYPE, 3);

        startActivity(intent);
    }

}
