package com.unexceptional.beast.banko.newVersion.ui.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.database.DatabaseHelper;
import com.unexceptional.beast.banko.database.model.Account;
import com.unexceptional.beast.banko.database.model.Bank;
import com.unexceptional.beast.banko.database.model.Budget;
import com.unexceptional.beast.banko.database.model.Category;
import com.unexceptional.beast.banko.database.model.Currency;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.db.entity.BankEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.BudgetEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.CategoryEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.CurrencyEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.NotificationEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.ProductEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.SettingsEntity;
import com.unexceptional.beast.banko.newVersion.networking.BackupManagement;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.other.NotificationPublisher;
import com.unexceptional.beast.banko.newVersion.ui.dialog.LoadingDialog;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CurrencyViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.NotificationViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SplashActivity extends PassActivity {

    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }catch (Exception e){

        }

        loadingDialog= new LoadingDialog(this);
        //in order to make sure it works after reboot when sb stops autostart of app
        new NotificationPublisher().scheduleNotification(this);

        //preferences that we need settings here

        if (passwordCheck){
            if(preferences.getBoolean(getString(R.string.key_create_notification_channels), true)) {
                createNotificationChannels();
                preferences.edit().putBoolean(getString(R.string.key_create_notification_channels), false).apply();
            }


            if (preferences.getBoolean(getResources().getString(R.string.key_welcome_undone), true)) {
                //if he is new doesnt need that tut
                preferences.edit().putBoolean(getString(R.string.key_show_new_version_tut), false).apply();

                startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
                finish();
            } else  if (preferences.getBoolean(getResources().getString(R.string.key_welcome_settings_undone), true)) {
                startActivity(new Intent(SplashActivity.this, WelcomeSettingsActivity.class));
                finish();
            } else  if (preferences.getBoolean(getResources().getString(R.string.key_show_new_version_tut), true)) {
                preferences.edit().putBoolean(getString(R.string.key_show_new_version_tut), false).apply();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);

                Intent intent2 = new Intent(this, TutorialActivity.class);
                intent2.putExtra(Constants.KEY_TUTORIAL_TYPE, Constants.TUTORIAL_TYPE_NEW_VERSION);
                startActivity(intent2);
                finish();
            }else {
                //for old users to provide settings ...
                new AppExecutors().diskIO().execute(() ->{

                    SettingsEntity settingsEntity= ((MyApplication)getApplicationContext()).getSettingsRepository().getSettings();
                    if(settingsEntity==null) {
                        settingsEntity= new SettingsEntity();

                        settingsEntity.setId(1);
                        Locale actualLocale= Locale.getDefault();
                        settingsEntity.setCurrency(preferences.getLong(getString(R.string.key_default_currency_id_new),1));
                        settingsEntity.setLanguage(actualLocale.getLanguage());
                        settingsEntity.setLocale(actualLocale.getCountry());
                        settingsEntity.setTimestamp(Calendar.getInstance().getTimeInMillis());
                        ((MyApplication)getApplicationContext()).getSettingsRepository().insert(settingsEntity);

                    }
                });

                if(preferences.getBoolean(getString(R.string.key_room_database_need_populate), true))
                    populateRoomDatabase();
                else if (preferences.getBoolean(getString(R.string.key_add_other_currencies), true)){


                    List<CurrencyEntity> currencyEntities= new ArrayList<>();

                    List<String> strings = Arrays.asList(getResources().getStringArray(R.array.OtherCurrencyShortcuts));




                    for (int i=0; i<strings.size(); i++){
                        currencyEntities.add(new CurrencyEntity(9 +i, strings.get(i)));
                    }


                    ViewModelProviders.of(this).get(CurrencyViewModel.class).insertAll(currencyEntities);
                    preferences.edit().putBoolean(getString(R.string.key_add_other_currencies),false).apply();
                }

                String token = preferences.getString(getString(R.string.key_dropbox_access_token), "");

                if (token!=null && !token.equals("")) {
                    //BackupManagement.checkFullBackup(token, loadingDialog, this);
                    checkFullBackup(token);
                    return;
                } else
                    startActivity(new Intent(this, MainActivity.class));


                finish();
            }
        }
    }

    private void createNotificationChannels(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = this.getSystemService(NotificationManager.class);

            List<NotificationChannel> channels= notificationManager.getNotificationChannels();

            for (NotificationChannel channel: channels){
                notificationManager.deleteNotificationChannel(channel.getId());
            }

            NotificationChannel promoChannel = new NotificationChannel(
                    getString(R.string.notification_channel_default),
                    getString(R.string.notification_channel_default_name),
                    NotificationManager.IMPORTANCE_HIGH);

            promoChannel.setDescription(getString(R.string.notification_channel_default_desc));
            promoChannel.setShowBadge(true);

            notificationManager.createNotificationChannel(promoChannel);

        }
    }

    //migration from old DB
    private SparseArray<Long> createBankBlueprint(DatabaseHelper db){
        SparseArray<Long> blueprint= new SparseArray<>();

        List<Bank> banks= db.getAllBanks();
        for (Bank bank: banks){
            if (bank.getmId()==1){
                blueprint.append(1, 101L);
            }else if(bank.getmId()==2){
                blueprint.append(2, 100L);
            }else
                switch (bank.getmIconName()){
                    case "bank_millennium":
                        blueprint.append(bank.getmId(), 1L);
                        break;
                    case "bank_mbank":
                        blueprint.append(bank.getmId(), 2L);
                        break;
                    case "bank_getin_bank":
                        blueprint.append(bank.getmId(), 3L);
                        break;
                    case "bank_getin_noble":
                        blueprint.append(bank.getmId(), 3L);
                        break;
                    case "bank_ing":
                        blueprint.append(bank.getmId(), 4L);
                        break;
                    case "bank_credit_agricole":
                        blueprint.append(bank.getmId(), 5L);
                        break;
                    case "bank_alior":
                        blueprint.append(bank.getmId(), 6L);
                        break;
                    case "bank_bgz":
                        blueprint.append(bank.getmId(), 7L);
                        break;
                    case "bank_bgz_optima":
                        blueprint.append(bank.getmId(), 8L);
                        break;
                    case "bank_city":
                        blueprint.append(bank.getmId(), 9L);
                        break;
                    case "bank_eurobank":
                        blueprint.append(bank.getmId(), 10L);
                        break;
                    case "bank_idea":
                        blueprint.append(bank.getmId(), 11L);
                        break;
                    case "bank_nest":
                        blueprint.append(bank.getmId(), 13L);
                        break;
                    case "bank_pko":
                        blueprint.append(bank.getmId(), 14L);
                        break;
                    case "bank_santander":
                        blueprint.append(bank.getmId(), 15L);
                        break;
                    case "bank_tmobile":
                        blueprint.append(bank.getmId(), 17L);
                        break;
                    case "bank_pekao":
                        blueprint.append(bank.getmId(), 18L);
                        break;
                    case "bank_toyota":
                        blueprint.append(bank.getmId(), 19L);
                        break;
                    case "bank_pocztowy":
                        blueprint.append(bank.getmId(), 20L);
                        break;
                    default:
                        blueprint.append(bank.getmId(), 102L);
                        break;
                }

        }
        return blueprint;
    }

    private void populateRoomDatabase( ){
        List<BankEntity> bankEntities= new ArrayList<>();

        bankEntities.add(new BankEntity(1, "Bank Millennium"));
        bankEntities.add(new BankEntity(2, "mBank"));
        bankEntities.add(new BankEntity(3, "Getin Bank"));
        bankEntities.add(new BankEntity(4, "ING Bank Śląski"));
        bankEntities.add(new BankEntity(5, "Credit Agricole"));
        bankEntities.add(new BankEntity(6, "Alior Bank"));
        bankEntities.add(new BankEntity(7, "BNP Paribas"));
        bankEntities.add(new BankEntity(8, "BGŻ Optima"));
        bankEntities.add(new BankEntity(9, "Citi Handlowy"));
        bankEntities.add(new BankEntity(10, "Eurobank"));
        bankEntities.add(new BankEntity(11, "Idea Bank"));
        bankEntities.add(new BankEntity(12, "Inteligo"));
        bankEntities.add(new BankEntity(13, "Nest Bank"));
        bankEntities.add(new BankEntity(14, "PKO Bank Polski"));
        bankEntities.add(new BankEntity(15, "Santander Bank Polska"));
        bankEntities.add(new BankEntity(16, "Santander Consumer Bank"));
        bankEntities.add(new BankEntity(17, "T-mobile Usługi Bankowe"));
        bankEntities.add(new BankEntity(18, "Bank Pekao"));
        bankEntities.add(new BankEntity(19, "Toyota Bank"));
        bankEntities.add(new BankEntity(20, "Bank Pocztowy"));

        bankEntities.add(new BankEntity(100, getString(R.string.cash)));
        bankEntities.add(new BankEntity(101, getString(R.string.debt)));
        bankEntities.add(new BankEntity(102, getString(R.string.other)));

        DatabaseHelper db= DatabaseHelper.getInstance(MyApplication.getContext());

        SparseArray<Long> blueprint= createBankBlueprint(db);
        List<ProductEntity> productEntities=  productsToRoom(db, blueprint);

        ((MyApplication)getApplicationContext()).getUltraRepository().setDatabase(
                blueprint,
                bankEntities,
                currenciesToRoom(db),
                productEntities,
                categoriesToRoom(db),
                db.getExpensesLimited(999999),
                db.getIncomesLimited(999999),
                db.getTransfersLimited(999999),
                budgetsToRoom(db)

        );
        preferences.edit().putBoolean(getString(R.string.key_room_database_need_populate),false).apply();
    }

    private List<CurrencyEntity> currenciesToRoom(DatabaseHelper db){
        int oldCurrencyId= preferences.getInt(getString(R.string.key_default_currency_id), 1);
        preferences.edit().putLong(getString(R.string.key_default_currency_id_new), oldCurrencyId).apply();

        List<CurrencyEntity> currencyEntities= new ArrayList<>();
        for (Currency currency: db.getAllCurrencies()){
            currencyEntities.add(new CurrencyEntity(
                    currency.getmId(),
                    currency.getmName(),
                    currency.getmSymbol(),
                    currency.getmShortcut(),
                    currency.getmExchangeRate()
            ));
        }

        List<String> strings = Arrays.asList(getResources().getStringArray(R.array.OtherCurrencyShortcuts));

        for (int i=0; i<strings.size(); i++){
            currencyEntities.add(new CurrencyEntity(9 +i, strings.get(i)));
        }



        return currencyEntities;
    }

    private List<CategoryEntity> categoriesToRoom(DatabaseHelper db){
        List<CategoryEntity> categoryEntities= new ArrayList<>();
        for (Category category: db.getAllCategories()){
            if (category.getmId()!=1)
                categoryEntities.add(new CategoryEntity(
                        category.getmId(),
                        category.getmParentCategoryId(),
                        category.getmName(),
                        category.getmIconName(),
                        category.ismIsInactive(),
                        category.getmOrder(),
                        1
                ));
            else
                categoryEntities.add(new CategoryEntity(
                        category.getmId(),
                        category.getmParentCategoryId(),
                        category.getmName(),
                        category.getmIconName(),
                        category.ismIsInactive(),
                        category.getmOrder(),
                        0
                ));
        }


        return categoryEntities;
    }

    private List<ProductEntity> productsToRoom(DatabaseHelper db, SparseArray<Long> blueprint){
       List<ProductEntity> productEntities= new ArrayList<>();

        for (Account account: db.getAllAccounts()){
            int productType=10;
            Date endDate= account.ismInactive() ? Calendar.getInstance().getTime() : null;

            if(account.getmBankId()==1){//debt
                productType=40;
                endDate=null;
            }

           productEntities.add(new ProductEntity(
                   account.getmId(),
                   blueprint.get(account.getmBankId()),
                   account.getmCurrencyId(),
                   productType,
                   account.getmName(),
                   account.getmDescription(),
                   account.ismInactive(),
                   null,
                   endDate,
                   (double) Math.round(account.getmBalance() * 100) / 100,
                   account.ismFavorite(),
                   account.getmColor()
           ));
        }

        return productEntities;
    }

    private List<BudgetEntity> budgetsToRoom(DatabaseHelper db){
        List<BudgetEntity> budgetEntities= new ArrayList<>();
        for(Budget budget: db.getAllBudgets()){
            budgetEntities.add(new BudgetEntity(
                    budget.getmId(),
                    budget.getmName(),
                    budget.getmAmount(),
                    (short) budget.getmDateOption(),
                    budget.getmIconName(),
                    budget.ismInactive(),
                    budget.getCategoryIds()
            ));
        }
        return budgetEntities;
    }


    //auto backup stuff now useless
    private void checkFullBackup(String token){
        loadingDialog.showDialog(getString(R.string.sync));

        new AppExecutors().diskIO().execute(() -> {
            NotificationEntity notification= ViewModelProviders.of(this).get(NotificationViewModel.class)
                    .getNotificationByType(Constants.NOTIFICATION_TYPE_BACKUP);
            if (notification!=null) {
                compareTimestamps(token);
            } else {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

    }

    public void compareTimestamps(String token) {
        new AppExecutors().diskIO().execute(() -> {
            long dbTimestamp= ((MyApplication)getApplicationContext()).getSettingsRepository().getSettings().getTimestamp();
            long dropboxTimestamp= BackupManagement.getDropboxTimestamp(token, this);

            if (dropboxTimestamp>dbTimestamp) {

                File file = BackupManagement.importDbDropbox(token, this);
                if (file !=null) {
                    BackupManagement.writeToDatabase(file, this);
                    notifyUser(false);
                } else
                    notifyUser(true);
            }else if (dropboxTimestamp==0)
                notifyUser(true);
            else
                notifyUser(false);


        });

    }


    private void notifyUser(boolean fail){
        new AppExecutors().mainThread().execute(()->{
            try {
                loadingDialog.hideDialog();
            }catch ( Exception ignored){

            }

            if(fail){
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.warning_sync_failed))
                        .setPositiveButton(getString(R.string.go_to_app), (dialog, which) ->{
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            finish();
                        })
                        .setNeutralButton(getString(R.string.exit), (dialog, which) ->{
                            finish();
                        })
                        .setNegativeButton(getString(R.string.retry), (dialog, which) ->{
                            checkFullBackup(preferences.getString(getString(R.string.key_dropbox_access_token), ""));
                        })
                        .show();

            }else{
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }

        });
    }

}