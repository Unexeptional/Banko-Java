package com.unexceptional.beast.banko.other;
/*
 * CALL IT DONE 1.11.2018
 * DARCZAKKK Jest super
 */

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.db.AppRoomDatabase;
import com.unexceptional.beast.banko.newVersion.db.repository.BankRepository;
import com.unexceptional.beast.banko.newVersion.db.repository.BudgetRepository;
import com.unexceptional.beast.banko.newVersion.db.repository.CategoryRepository;
import com.unexceptional.beast.banko.newVersion.db.repository.CurrencyRepository;
import com.unexceptional.beast.banko.newVersion.db.repository.NotificationRepository;
import com.unexceptional.beast.banko.newVersion.db.repository.ParticipationTaskRepository;
import com.unexceptional.beast.banko.newVersion.db.repository.ProductRepository;
import com.unexceptional.beast.banko.newVersion.db.repository.SettingsRepository;
import com.unexceptional.beast.banko.newVersion.db.repository.TaskRepository;
import com.unexceptional.beast.banko.newVersion.db.repository.TransactionRepository;
import com.unexceptional.beast.banko.newVersion.db.repository.UltraRepository;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.zeugmasolutions.localehelper.LocaleAwareApplication;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MyApplication extends LocaleAwareApplication {
    private static MyApplication instance;

    public static  final SimpleDateFormat SDF_DATE_WITH_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    public static  final SimpleDateFormat SDF_DATE_NO_TIME = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * done mostly for getting context from places u don't want to send context(stackOverflow said that is nice)
     */
    public MyApplication() {
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }

    public static Calendar getCalendar(String createdAt){
        Calendar calendar= Calendar.getInstance();

        if(getDate(createdAt)!=null)
            calendar.setTime(getDate(createdAt));

        return calendar;
    }

    public static Date getDate(String createdAt){
        Date date = null;

        try {
            date = Constants.SDF_DATE_WITH_TIME_SECONDS.parse(createdAt);
        } catch (ParseException e) {

            try {
                date = SDF_DATE_WITH_TIME.parse(createdAt);
            } catch (ParseException e1) {

                try {
                    date = SDF_DATE_NO_TIME.parse(createdAt);
                } catch (ParseException e2) {
                    e2.printStackTrace();
                }
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return date;
    }

    //from ROOOMMM

    private AppExecutors mAppExecutors;

    //public  static  final DecimalFormat money = new DecimalFormat("0.00");
    public  static  final DecimalFormat money = new DecimalFormat("#,##0.00");
    public  static  final DecimalFormat currencyRate = new DecimalFormat("0.0000");

    @Override
    public void onCreate() {
        super.onCreate();
        mAppExecutors = new AppExecutors();

    }

    public static int getDrawableId(String name){
        if (name!=null)
            return instance.getResources().getIdentifier(name, "drawable", MyApplication.getContext().getPackageName());
        else
            return 0;
    }

    public static int getBankIconId(long bankId){
        String iconName= "bank_" + String.valueOf(bankId);

        if (iconName.length() > 0 && iconName.charAt(iconName.length() - 1) == 'x') {
            iconName = iconName.substring(0, iconName.length() - 1);
        }
        return getDrawableId(iconName);

    }

    private static Drawable getMyDrawable(int id){
        return instance.getResources().getDrawable(id);
    }

    private AppRoomDatabase getDatabase() {
        return AppRoomDatabase.getInstance(this, mAppExecutors);
    }


    //get repositories
    public BankRepository getBankRepository(){
        return BankRepository.getInstance(getDatabase());
    }

    public ProductRepository getProductRepository(){
        return ProductRepository.getInstance(getDatabase());
    }

    public SettingsRepository getSettingsRepository(){
        return SettingsRepository.getInstance(getDatabase());
    }

    public CurrencyRepository getCurrencyRepository(){
        return CurrencyRepository.getInstance(getDatabase());
    }

    public UltraRepository getUltraRepository(){
        return UltraRepository.getInstance(getDatabase());
    }

    public TransactionRepository getTransactionRepository(){
        return TransactionRepository.getInstance(getDatabase());
    }

    public CategoryRepository getCategoryRepository(){
        return CategoryRepository.getInstance(getDatabase());
    }

    public BudgetRepository getBudgetRepository(){
        return BudgetRepository.getInstance(getDatabase());
    }

    public NotificationRepository getNotificationRepository(){
        return NotificationRepository.getInstance(getDatabase());
    }

    public TaskRepository getTaskRepository(){
        return TaskRepository.getInstance(getDatabase());
    }

    public ParticipationTaskRepository getParticipationTaskRepository(){
        return ParticipationTaskRepository.getInstance(getDatabase());
    }
}
