package com.unexceptional.beast.banko.newVersion.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity;

public abstract class PassActivity extends LocaleAwareCompatActivity {

    protected SharedPreferences preferences;
    protected boolean passwordCheck =false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        showPass();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showPass();
    }

    private void showPass(){
        boolean isPassEnabled = preferences.getBoolean(getResources().getString(R.string.key_enable_pass), false);

        if (isPassEnabled)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

        boolean skipPass= getIntent().getBooleanExtra(Constants.KEY_SKIP_PASS_SCREEN, false);

        String passCode = preferences.getString(Constants.KEY_PASS, Constants.KEY_DEFAULT_PASS);

        assert passCode != null;

        if (isPassEnabled && !isSessionActive() && !passCode.trim().isEmpty() &&!skipPass) {
            Intent intent = new Intent(this, PassLockScreenActivity.class)
                    .setAction(getIntent().getAction())
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .putExtra(Constants.KEYPASS_CLASS_CALLER, this.getClass().getName());
            if (getIntent().getExtras() != null)
                intent.putExtras(getIntent().getExtras());

            startActivity(intent);
            finish();
            //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }else
            passwordCheck =true;
    }

    @Override
    protected void onPause() {
        super.onPause();
       preferences.edit().putLong(getString(R.string.key_session_init_time), System.currentTimeMillis()).apply();
    }

    /**
     * @return {@code true} if passcode session is active, and {@code false} otherwise
     */
    private boolean isSessionActive() {

        return System.currentTimeMillis() - preferences.getLong(getString(R.string.key_session_init_time), 0L)
               < Constants.SESSION_TIMEOUT;
    }
}
