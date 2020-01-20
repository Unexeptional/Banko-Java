package com.unexceptional.beast.banko.newVersion.ui.appWidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.opengl.Visibility;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.db.entity.ProductEntity;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.ui.activity.FloatingActivity;
import com.unexceptional.beast.banko.other.MyApplication;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link AddTransactionWidgetConfigureActivity AddTransactionWidgetConfigureActivity}
 */
public class AddTransactionWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        long productId= AddTransactionWidgetConfigureActivity.loadPrefs(context, appWidgetId);

        new AppExecutors().diskIO().execute(() -> {
            ProductEntity productEntity= ((MyApplication)context.getApplicationContext()).getProductRepository().getProductRapid(productId);
            if(productEntity!=null) {
                // Construct the RemoteViews object
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.add_transaction_widget);
                views.setTextViewText(R.id.product_name, productEntity.getTitle());
                views.setTextViewText(R.id.product_balance, MyApplication.money.format(productEntity.getBalance()));
                views.setImageViewResource(R.id.product_icon, MyApplication.getBankIconId(productEntity.getBankId()));

                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.key_show_widget_balance), true)){
                    views.setViewVisibility(R.id.product_balance, View.VISIBLE);
                }else {
                    views.setViewVisibility(R.id.product_balance, View.GONE);
                }


                //Set on widget click
                Intent intent = new Intent(context, FloatingActivity.class);
                intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_TRANSACTION_FRAGMENT);
                intent.putExtra(Constants.KEY_TRANSACTION_SHORTCUT_MODE, false);
                intent.putExtra(Constants.KEY_PRODUCT_ID, productEntity.getId());
                intent.putExtra(Constants.KEY_COMING_FROM_WIDGET,true);

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);


                PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) productEntity.getId(), intent, 0);
                views.setOnClickPendingIntent(R.id.background, pendingIntent);

                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views);

            }

        });
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            AddTransactionWidgetConfigureActivity.deletePrefs(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

