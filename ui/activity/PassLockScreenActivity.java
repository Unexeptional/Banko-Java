/*
 * Copyright (c) 2014 - 2015 Oleksandr Tyshkovets <olexandr.tyshkovets@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, color_software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * CALL IT DONE 1.11.2018
 * both with passactivity are not mine and i dont know what is going on
 * but works
 */
package com.unexceptional.beast.banko.newVersion.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.ui.fragment.KeyboardFragment;

import java.util.Objects;

/**
 * Activity for displaying and managing the passcode lock screen.
 * @author Oleksandr Tyshkovets <olexandr.tyshkovets@gmail.com>
 */
public class PassLockScreenActivity extends AppCompatActivity
        implements KeyboardFragment.OnPassEnteredListener {

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setContentView(R.layout.passcode_lockscreen);
    }

    @Override
    public void onPassEntered(String pass) {
        String passCode = preferences.getString(Constants.KEY_PASS, Constants.KEY_DEFAULT_PASS);

        if (pass.equals(passCode)) {

            if (Constants.KEY_DISABLE_PASS.equals(getIntent().getStringExtra(Constants.KEY_DISABLE_PASS))) {
                setResult(RESULT_OK);
                finish();
                return;
            }
            startActivity(new Intent()
                    .setClassName(this, getIntent().getStringExtra(Constants.KEYPASS_CLASS_CALLER))
                    .setAction(getIntent().getAction())
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .putExtra(Constants.KEY_SKIP_PASS_SCREEN, true)
                    .putExtras(Objects.requireNonNull(getIntent().getExtras()))

            );
            //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            Toast.makeText(this, R.string.warning_wrong_passcode, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDissmissed() {

    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);

        if (Constants.KEY_DISABLE_PASS.equals(getIntent().getStringExtra(Constants.KEY_DISABLE_PASS))) {
            finish();
            return;
        }

        //GnuCashApplication.PASSCODE_SESSION_INIT_TIME = System.currentTimeMillis() - GnuCashApplication.SESSION_TIMEOUT;
        startActivity(new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        );
    }
}
