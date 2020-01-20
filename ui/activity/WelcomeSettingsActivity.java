package com.unexceptional.beast.banko.newVersion.ui.activity;
/*
 * CALL IT DONE 1.11.2018
 */

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.ActivityWelcomeSettingsBinding;
import com.unexceptional.beast.banko.newVersion.callback.PickCurrencyCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.BankEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.CategoryEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.CurrencyEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.ProductEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.SettingsEntity;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickCurrencyDialog;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CurrencyViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WelcomeSettingsActivity extends BasicActivity {

    private static final String CATEGORY_MODE_NONE= "none";
    private static final String CATEGORY_MODE_ADULT= "adult";
    private static final String CATEGORY_MODE_YOUNG= "young";
    private static final String CATEGORY_MODE_STUDENT= "student";

    private String activeCode= CATEGORY_MODE_ADULT;

    private ActivityWelcomeSettingsBinding mBinding;
    private Locale actualLocale;
    private static long currencyId;

    @Override
    protected int provideActivityLayout() {
        return R.layout.activity_welcome_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }catch (Exception e){

        }

        mBinding= (ActivityWelcomeSettingsBinding) dataBinding;

        makeCurrencies();
        setClicks();


        Locale locale= Locale.getDefault();
        List<String> strings = Arrays.asList(getResources().getStringArray(R.array.LocalizationOptionsDesc));
        long defaultCurr=1;
        if (strings.contains(locale.toString())) {
            actualLocale= locale;
            switch (strings.indexOf(locale.toString())){
                case 0:
                    defaultCurr=2;
                    break;
                case 1:
                    defaultCurr=1;
                    break;
                case 2:
                    defaultCurr=4;
                    break;
            }
        } else {
            defaultCurr=2;
            List<String> langStrings = Arrays.asList(getResources().getStringArray(R.array.LanguageOptionsDesc));
            if (langStrings.contains(locale.toString()))
                actualLocale= new Locale(locale.getLanguage());
            else
                actualLocale= new Locale(langStrings.get(0));
        }

        setVisuals();
        setCategoryPack(CATEGORY_MODE_ADULT);

        setCurrency(preferences.getLong(getString(R.string.key_default_currency_id_new), defaultCurr));
    }

    private void makeCurrencies() {

        List<CurrencyEntity> currencyEntities= new ArrayList<>();

        currencyEntities.add(new CurrencyEntity(1,getString(R.string.polish_zloty), "zł", "PLN", 1));
        currencyEntities.add(new CurrencyEntity(2,getString(R.string.usa_dollar), "$", "USD", 1));
        currencyEntities.add(new CurrencyEntity(3,getString(R.string.euro), "€", "EUR", 1));
        currencyEntities.add(new CurrencyEntity(4,getString(R.string.great_british_pound), "£", "GBP", 1));
        currencyEntities.add(new CurrencyEntity(5,getString(R.string.swiss_franc), "CHF", "CHF", 1));
        currencyEntities.add(new CurrencyEntity(6,getString(R.string.canadian_dollar), "$", "CAD", 1));
        currencyEntities.add(new CurrencyEntity(7,getString(R.string.yapan_yen), "¥", "JPY", 1));
        currencyEntities.add(new CurrencyEntity(8,getString(R.string.australia_dollar), "$", "AUD", 1));

        List<String> strings = Arrays.asList(getResources().getStringArray(R.array.OtherCurrencyShortcuts));

        for (int i=0; i<strings.size(); i++){
            currencyEntities.add(new CurrencyEntity(9 +i, strings.get(i)));
        }


        ViewModelProviders.of(this).get(CurrencyViewModel.class).insertAll(currencyEntities);
    }
    
    private void setVisuals(){
        if (actualLocale.getCountry()!=null && !actualLocale.getCountry().equals(""))
            mBinding.locationTitle.setText(String.format("%s: %s", getString(R.string.location), actualLocale.getDisplayCountry()));
        else {
            mBinding.locationTitle.setText(String.format("%s: %s", getString(R.string.location), getString(R.string.other)));
            mBinding.languageTitle.setText(String.format("%s: %s", getString(R.string.language), actualLocale.getDisplayLanguage()));
            mBinding.languageTitle.setVisibility(View.VISIBLE);
        }
    }

    private void setClicks(){
        mBinding.languageTitle.setOnClickListener(v -> showLanguageOptions());

        mBinding.locationTitle.setOnClickListener(v -> showLocationSettings());

        mBinding.settingsDefaultCurrency.setOnClickListener(v -> {
            PickCurrencyCallback callback = currency -> {
                setCurrency(currency.getId());

            };

            preferences.edit().putLong(getString(R.string.key_currencies_update_time),0).apply();
            new PickCurrencyDialog(this, callback, false).showDialog();
        });

        mBinding.btnFinish.setOnClickListener(v -> {
            preferences.edit().putBoolean(getResources().getString(R.string.key_welcome_settings_undone), false).apply();
            setAppDatabase();
            startActivity(new Intent(WelcomeSettingsActivity.this, MainActivity.class));
            finish();
        });

        mBinding.btnNoCategories.setOnClickListener(v -> setCategoryPack(CATEGORY_MODE_NONE));
        mBinding.btnAdultCategories.setOnClickListener(v -> setCategoryPack(CATEGORY_MODE_ADULT));
        mBinding.btnYoungCategories.setOnClickListener(v -> setCategoryPack(CATEGORY_MODE_YOUNG));
        mBinding.btnStudentCategories.setOnClickListener(v -> setCategoryPack(CATEGORY_MODE_STUDENT));
    }

    private void showLocationSettings() {
        final String[] contentOptions = getResources().getStringArray(R.array.LocalizationOptions);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle(R.string.show_at_start);

        Locale locale= Locale.getDefault();
        List<String> strings = Arrays.asList(getResources().getStringArray(R.array.LocalizationOptionsDesc));
        int location=0;

        if (strings.contains(locale.toString()))
            location= strings.indexOf(locale.toString());

        mBuilder.setSingleChoiceItems(contentOptions, location,
                (dialogInterface, i) -> {
                    String localeString= strings.get(i);
                    if (localeString.contains("_")){
                        switch (i){
                            case 0:
                                setCurrency(2);
                                break;
                            case 1:
                                setCurrency(1);
                                break;
                            case 2:
                                setCurrency(4);
                                break;
                        }




                        String language= localeString.substring(0, localeString.indexOf("_"));
                        String country= localeString.substring(localeString.indexOf("_") +1);
                        setLocale(country, language);
                        mBinding.languageTitle.setVisibility(View.GONE);

                    }else{
                        showLanguageOptions();
                    }


                    dialogInterface.dismiss();

                });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void showLanguageOptions() {
        final String[] contentOptions = getResources().getStringArray(R.array.LanguageOptions);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle(R.string.language);

        Locale locale= Locale.getDefault();
        List<String> strings = Arrays.asList(getResources().getStringArray(R.array.LanguageOptionsDesc));
        int languagePosition=0;

        if (strings.contains(locale.toString()))
            languagePosition= strings.indexOf(locale.toString());

        mBuilder.setSingleChoiceItems(contentOptions, languagePosition,
                (dialogInterface, i) -> {
                    String language= strings.get(i);
                    setLocale("", language);
                    dialogInterface.dismiss();
                });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void setIconPackHelper(Button silver, Button[] whiteButtons){
        silver.setBackground(getResources().getDrawable(R.drawable.button_light_gray_background_round_shape));
        for(Button button: whiteButtons)
            button.setBackground(getResources().getDrawable(R.drawable.button_white_background_round_shape));
    }

    private void setCategoryPack(String code){
        switch (code){
            case CATEGORY_MODE_ADULT:
                setIconPackHelper(mBinding.btnAdultCategories, new Button[]{mBinding.btnStudentCategories,
                        mBinding.btnNoCategories, mBinding.btnYoungCategories});
                break;
            case CATEGORY_MODE_STUDENT:
                setIconPackHelper(mBinding.btnStudentCategories, new Button[]{mBinding.btnAdultCategories,
                        mBinding.btnNoCategories, mBinding.btnYoungCategories});
                break;
            case CATEGORY_MODE_YOUNG:
                setIconPackHelper(mBinding.btnYoungCategories, new Button[]{mBinding.btnAdultCategories,
                        mBinding.btnNoCategories, mBinding.btnStudentCategories });
                break;
            case CATEGORY_MODE_NONE:
                setIconPackHelper(mBinding.btnNoCategories, new Button[]{mBinding.btnAdultCategories,
                        mBinding.btnYoungCategories, mBinding.btnStudentCategories });
                break;
        }
        activeCode=code;
    }

    private void setCurrency(long currencyId){
        WelcomeSettingsActivity.currencyId = currencyId;
        preferences.edit().putLong(getString(R.string.key_default_currency_id_new), currencyId).apply();
        setCurrencyVisual(currencyId);
    }

    private void setLocale(String country, String language){
        ((MyApplication)getApplicationContext()).getSettingsRepository().updateLocale(country);
        ((MyApplication)getApplicationContext()).getSettingsRepository().updateLanguage(language);
        updateLocale(new Locale(language, country));
    }

    private void setCurrencyVisual(long currencyId){
        new AppExecutors().diskIO().execute(() ->{
            CurrencyEntity currencyEntity= ViewModelProviders.of(this).get(CurrencyViewModel.class).getCurrencyRapid(currencyId);

            if(currencyEntity!=null){
                new AppExecutors().mainThread().execute(() -> {
                    mBinding.settingsDefaultCurrency.setText(currencyEntity.getShortcut());
                }); }

        });
    }

    void setAppDatabase(){
        ((MyApplication)getApplicationContext()).getUltraRepository().setNewDatabase(
                makeBanks(),
                makeProducts(),
                makeCategories()
        );

        SettingsEntity settingsEntity= new SettingsEntity();
        settingsEntity.setCurrency(currencyId);
        settingsEntity.setLanguage(actualLocale.getLanguage());
        settingsEntity.setLocale(actualLocale.getCountry());
        settingsEntity.setTimestamp(Calendar.getInstance().getTimeInMillis());
        ((MyApplication)getApplicationContext()).getSettingsRepository().insert(settingsEntity);

        preferences.edit().putBoolean(getString(R.string.key_room_database_need_populate),false).apply();
    }

    public  List<CategoryEntity> makeCategories() {
        switch (activeCode){
            case CATEGORY_MODE_ADULT:
                return makeAdultCategories();
            case CATEGORY_MODE_STUDENT:
                return makeStudentCategories();
            case CATEGORY_MODE_YOUNG:
                return makeYoungCategories();
            case CATEGORY_MODE_NONE:
                return makeNoCategories();
            default:
                return makeAdultCategories();
        }
    }

    public  List<CategoryEntity> makeAdultCategories() {
        List<CategoryEntity> categoryEntities= new ArrayList<>();

        long parentId=1;
        categoryEntities.add(new CategoryEntity(1, 0, getString(R.string.debt), "debt", false, 0,0));

        //incomes
        categoryEntities.add(new CategoryEntity(31, 0, getString(R.string.salary), "color_money_bag", 2));

        //car
        categoryEntities.add(new CategoryEntity(2, 0, getString(R.string.car), "color_car", 1));
        parentId=2;
        categoryEntities.add(new CategoryEntity(3, parentId, getString(R.string.fuel), "color_fuel", 1));
        categoryEntities.add(new CategoryEntity(4, parentId, getString(R.string.repair), "color_car_repair", 1));
        categoryEntities.add(new CategoryEntity(5, parentId, getString(R.string.parking), "color_parking", 1));
        categoryEntities.add(new CategoryEntity(6, parentId, getString(R.string.other), "color_car", 1));

        //food
        categoryEntities.add(new CategoryEntity(7, 0, getString(R.string.food), "color_grocery", 1));parentId=7;
        categoryEntities.add(new CategoryEntity(8, parentId, getString(R.string.eating_outside), "color_restaurant", 1));
        categoryEntities.add(new CategoryEntity(9, parentId, getString(R.string.shopping), "color_shopping", 1));
        categoryEntities.add(new CategoryEntity(10, parentId, getString(R.string.other), "color_grocery", 1));

        //home
        categoryEntities.add(new CategoryEntity(11, 0, getString(R.string.house_keeping), "color_home", 1));
        parentId=11;
        categoryEntities.add(new CategoryEntity(12, parentId, getString(R.string.rtv), "color_rtv", 1));
        categoryEntities.add(new CategoryEntity(13, parentId, getString(R.string.agd), "color_agd", 1));
        categoryEntities.add(new CategoryEntity(14, parentId, getString(R.string.bills), "color_bills", 1));
        categoryEntities.add(new CategoryEntity(15, parentId, getString(R.string.mortgage), "color_money_stack", 1));
        categoryEntities.add(new CategoryEntity(16, parentId, getString(R.string.rent), "color_rent", 1));
        categoryEntities.add(new CategoryEntity(17, parentId, getString(R.string.other), "color_home", 1));

        //entertainment
        categoryEntities.add(new CategoryEntity(18, 0, getString(R.string.entertainment), "color_entertainment", 1));
        parentId=18;
        categoryEntities.add(new CategoryEntity(19, parentId, getString(R.string.games), "color_games", 1));
        categoryEntities.add(new CategoryEntity(20, parentId, getString(R.string.party), "color_party", 1));
        categoryEntities.add(new CategoryEntity(21, parentId, getString(R.string.travel), "color_travel", 1));
        categoryEntities.add(new CategoryEntity(22, parentId, getString(R.string.other), "color_entertainment", 1));

        //health
        categoryEntities.add(new CategoryEntity(23, 0, getString(R.string.health), "color_health_bag", 1));
        parentId=23;
        categoryEntities.add(new CategoryEntity(24, parentId, getString(R.string.doctor), "color_doctor", 1));
        categoryEntities.add(new CategoryEntity(25, parentId, getString(R.string.pharmacy), "color_pharmacy2", 1));
        categoryEntities.add(new CategoryEntity(26, parentId, getString(R.string.other), "color_health_bag", 1));

        //other
        categoryEntities.add(new CategoryEntity(27, 0, getString(R.string.electronics), "color_electricity", 1));
        categoryEntities.add(new CategoryEntity(28, 0, getString(R.string.cosmetics), "color_makeup", 1));
        categoryEntities.add(new CategoryEntity(29, 0, getString(R.string.clothes), "color_clothes", 1));
        categoryEntities.add(new CategoryEntity(30, 0, getString(R.string.gifts), "color_gift3", 1));

        return categoryEntities;
    }

    public  List<CategoryEntity> makeStudentCategories() {
        List<CategoryEntity> categoryEntities= new ArrayList<>();

        long parentId=1;
        categoryEntities.add(new CategoryEntity(1, 0, getString(R.string.debt), "debt", false, 0,0));

        categoryEntities.add(new CategoryEntity(17, 0, getString(R.string.salary), "color_money_bag", 2));

        categoryEntities.add(new CategoryEntity(2, 0, getString(R.string.scholarship), "color_money_stack", 1));
        categoryEntities.add(new CategoryEntity(3, 0, getString(R.string.salary), "color_workout", 2));
        categoryEntities.add(new CategoryEntity(4, 0, getString(R.string.pocket_money), "color_home", 2));

        //entertainment
        categoryEntities.add(new CategoryEntity(5, 0, getString(R.string.entertainment), "color_entertainment", 1));
        parentId=5;
        categoryEntities.add(new CategoryEntity(6, parentId, getString(R.string.games), "color_games", 1));
        categoryEntities.add(new CategoryEntity(7, parentId, getString(R.string.alcohol), "color_beer", 1));
        categoryEntities.add(new CategoryEntity(8, parentId, getString(R.string.events), "color_travel", 1));
        categoryEntities.add(new CategoryEntity(9, parentId, getString(R.string.other), "color_entertainment", 1));

        //other
        categoryEntities.add(new CategoryEntity(10, 0, getString(R.string.food), "color_food", 1));
        categoryEntities.add(new CategoryEntity(11, 0, getString(R.string.transport), "color_car", 1));
        categoryEntities.add(new CategoryEntity(12, 0, getString(R.string.education), "color_paper", 1));
        categoryEntities.add(new CategoryEntity(13, 0, getString(R.string.house_keeping), "color_laundry", 1));
        categoryEntities.add(new CategoryEntity(14, 0, getString(R.string.electronics), "color_electricity", 1));
        categoryEntities.add(new CategoryEntity(15, 0, getString(R.string.cosmetics), "color_makeup", 1));
        categoryEntities.add(new CategoryEntity(16, 0, getString(R.string.clothes), "color_clothes", 1));

        return categoryEntities;
    }

    public  List<CategoryEntity>  makeYoungCategories() {
        List<CategoryEntity> categoryEntities= new ArrayList<>();

        categoryEntities.add(new CategoryEntity(1, 0, getString(R.string.debt), "debt", false, 0,0));

        categoryEntities.add(new CategoryEntity(2, 0, getString(R.string.food), "color_food", 1));
        categoryEntities.add(new CategoryEntity(3, 0, getString(R.string.games), "color_games", 1));
        categoryEntities.add(new CategoryEntity(4, 0, getString(R.string.sport), "color_ball", 1));
        categoryEntities.add(new CategoryEntity(5, 0, getString(R.string.passion), "color_chocolate", 1));
        categoryEntities.add(new CategoryEntity(6, 0, getString(R.string.books), "color_paper", 1));
        categoryEntities.add(new CategoryEntity(7, 0, getString(R.string.travel), "color_travel", 1));
        categoryEntities.add(new CategoryEntity(8, 0, getString(R.string.cosmetics), "color_makeup", 1));
        categoryEntities.add(new CategoryEntity(9, 0, getString(R.string.clothes), "color_clothes", 1));
        categoryEntities.add(new CategoryEntity(10, 0, getString(R.string.electronics), "color_electricity", 1));
        categoryEntities.add(new CategoryEntity(11, 0, getString(R.string.gifts), "color_gift3", 1));
        categoryEntities.add(new CategoryEntity(12, 0, getString(R.string.part_time_job), "color_workout", 2));
        categoryEntities.add(new CategoryEntity(13, 0, getString(R.string.pocket_money), "color_home", 2));

        return categoryEntities;
    }

    public  List<CategoryEntity>  makeNoCategories(){
        List<CategoryEntity> categoryEntities= new ArrayList<>();

        categoryEntities.add(new CategoryEntity(1, 0, getString(R.string.debt), "debt", false, 0,0));

        return categoryEntities;
    }

    private List<ProductEntity> makeProducts(){
        List<ProductEntity> productEntities= new ArrayList<>();
        long currencyId= preferences.getLong(getString(R.string.key_default_currency_id_new), 1);
        ColorGenerator generator= ColorGenerator.MATERIAL;

        int color= generator.getRandomColor();
        productEntities.add(new ProductEntity(100, currencyId, 10, getString(R.string.wallet), color));
        color= generator.getRandomColor();
        productEntities.add(new ProductEntity(101, currencyId, 40, getString(R.string.default_debt), color));


        return productEntities;
    }

    private List<BankEntity> makeBanks() {
        List<BankEntity> bankEntities= new ArrayList<>();
        if (actualLocale.getCountry().equals("PL")){
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
        }else if(actualLocale.getCountry().equals("GB")){
            bankEntities.add(new BankEntity(1, "HSBC Holdings"));
            bankEntities.add(new BankEntity(2, "Barclays"));
            bankEntities.add(new BankEntity(3, "Lloyds Banking Group"));
            bankEntities.add(new BankEntity(4, "Royal Bank Of Scotland"));
            bankEntities.add(new BankEntity(5, "Standard Chartered"));
            bankEntities.add(new BankEntity(6, "NatWest"));
            bankEntities.add(new BankEntity(7, "Santander UK"));
            bankEntities.add(new BankEntity(8, "CYBG PLC"));
            bankEntities.add(new BankEntity(9, "Virgin Money Holdings (UK)"));
            bankEntities.add(new BankEntity(10, "The Co-operative Bank"));
            bankEntities.add(new BankEntity(11, "Metro Bank"));
            bankEntities.add(new BankEntity(12, "Tesco Bank"));
        }else {
            for (int i=1; i<=20; i++)
                bankEntities.add(new BankEntity(i, getString(R.string.bank) + " " + i));

        }

        bankEntities.add(new BankEntity(100, getString(R.string.cash)));
        bankEntities.add(new BankEntity(101, getString(R.string.debt)));
        bankEntities.add(new BankEntity(102, getString(R.string.other)));

        return bankEntities;
    }
}
