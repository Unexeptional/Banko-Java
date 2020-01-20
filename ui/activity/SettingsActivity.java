package com.unexceptional.beast.banko.newVersion.ui.activity;


import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.db.entity.NotificationEntity;
import com.unexceptional.beast.banko.newVersion.ui.fragment.KeyboardFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.SettingsFragment;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.NotificationViewModel;

import java.util.Calendar;
import java.util.Locale;

public class SettingsActivity extends BasicActivity implements
        KeyboardFragment.OnPassEnteredListener,
        SettingsFragment.OnFragmentInteractionListener{

    static final int SET_AUTO_BACKUP_LOCATION = 27;

    private SettingsFragment settingsFragment;


    @Override
    protected int provideActivityLayout() {
        return R.layout.activity_settings2;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsFragment = (com.unexceptional.beast.banko.newVersion.ui.fragment.SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.settings_fragment);
    }

    private void startFragment(Fragment fragment){
        if(getSupportFragmentManager().findFragmentByTag(fragment.toString())==null){
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            fragmentTransaction.add(R.id.fragment_container, fragment, fragment.toString()).addToBackStack(fragment.toString()).commit();
        }
    }


    @Override
    public void finish() {
        //db.refreshAppWidgets();
        super.finish();
    }

    //PASSWORD
    @Override
    public void onPassEntered(String pass) {
        boolean isPassEnabled = preferences.getBoolean(getResources().getString(R.string.key_enable_pass), false);
        boolean setSwitch;

        if(!isPassEnabled) {
            preferences.edit().putString((Constants.KEY_PASS), pass).apply();
            getSupportFragmentManager().popBackStack();
            setSwitch= true;
        } else {
            String passCode = preferences.getString(Constants.KEY_PASS, Constants.KEY_DEFAULT_PASS);

            if (pass.equals(passCode)) {
                preferences.edit().putBoolean(getResources().getString(R.string.key_enable_pass), false).remove(Constants.KEY_PASS).apply();
                getSupportFragmentManager().popBackStack();
                setSwitch= false;
            } else {
                Toast.makeText(this, R.string.warning_wrong_passcode, Toast.LENGTH_SHORT).show();
                setSwitch= true;
            }
        }
        preferences.edit().putBoolean(getString(R.string.key_enable_pass), setSwitch).apply();
        settingsFragment.setPasswordSwitch(setSwitch);
    }

    @Override
    public void onDissmissed() {
        settingsFragment.setPasswordSwitch(preferences.getBoolean(getString(R.string.key_enable_pass),false));
    }

    @Override
    public void startPassFragment() {
        startFragment(new KeyboardFragment());
    }


    @Override
    public void setLocale(Locale locale) {
        updateLocale(locale);
    }

}
