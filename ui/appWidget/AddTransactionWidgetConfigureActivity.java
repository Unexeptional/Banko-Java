package com.unexceptional.beast.banko.newVersion.ui.appWidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.newVersion.callback.PickBankCallback;
import com.unexceptional.beast.banko.newVersion.callback.PickProductCallback;
import com.unexceptional.beast.banko.newVersion.db.model.Product;
import com.unexceptional.beast.banko.newVersion.ui.activity.BasicActivity;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickBankDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickProductDialog;

/**
 * The configuration screen for the {@link AddTransactionWidget AddTransactionWidget} AppWidget.
 */
public class AddTransactionWidgetConfigureActivity extends BasicActivity {

    private static final String PREFS_NAME = "com.unexceptional.beast.banko.newVersion.ui.appWidget.AddTransactionWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public AddTransactionWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void savePrefs(Context context, int appWidgetId, Product product) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putLong(context.getString(R.string.key_widget_product_id_) + appWidgetId, product.getId());
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static long loadPrefs(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getLong(context.getString(R.string.key_widget_product_id_) + appWidgetId, 0);
    }

    static void deletePrefs(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        PickProductCallback productCallback= product -> {
            if(product.getId()>0){
                final Context context = AddTransactionWidgetConfigureActivity.this;

                // When the button is clicked, store the string locally
                savePrefs(context, mAppWidgetId, product);

                // It is the responsibility of the configuration activity to update the app widget
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                AddTransactionWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        };

        if(preferences.getBoolean(getString(R.string.key_show_bank_list), false)){
            PickBankCallback fromBankCallback= bank -> {
                if(bank.getId()>0)
                    new PickProductDialog(this, productCallback, bank.getId()).showDialog();
            };

            new PickBankDialog(this, fromBankCallback, true).showDialog();

        } else
            new PickProductDialog(this, productCallback,0).showDialog();
    }

    @Override
    protected int provideActivityLayout() {
        return R.layout.add_transaction_widget_configure;
    }
}

