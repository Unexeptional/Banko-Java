package com.unexceptional.beast.banko.newVersion.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProviders;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.newVersion.db.AppRoomDatabase;
import com.unexceptional.beast.banko.newVersion.db.entity.SettingsEntity;
import com.unexceptional.beast.banko.newVersion.networking.BackupManagement;
import com.unexceptional.beast.banko.newVersion.networking.MyWorker;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.other.NotificationPublisher;
import com.unexceptional.beast.banko.newVersion.ui.dialog.LoadingDialog;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.NotificationViewModel;
import com.unexceptional.beast.banko.other.MyApplication;
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import static com.unexceptional.beast.banko.newVersion.db.AppRoomDatabase.DATABASE_NAME;


public abstract class BasicActivity extends LocaleAwareCompatActivity {

    protected ViewDataBinding dataBinding;
    protected SharedPreferences preferences;
    protected boolean initial= true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences= PreferenceManager.getDefaultSharedPreferences(this);
        dataBinding= DataBindingUtil.setContentView(this, provideActivityLayout());

        //StatusColorControl
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        String token = preferences.getString(getString(R.string.key_dropbox_access_token), "");

        if (token !=null && !token.equals("")){
            ((MyApplication)getApplicationContext()).getUltraRepository().sthChanged(new SimpleSQLiteQuery("select * from table_transactions"))
                    .observe(this, aBoolean -> {
                        if (!initial){
                            WorkManager.getInstance().cancelAllWorkByTag("send");
                            OneTimeWorkRequest simpleRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
                                    .addTag("send")
                                    .build();
                            WorkManager.getInstance().enqueue(simpleRequest);
                        }else
                            initial= false;
                    });




        }

    }

    protected abstract @LayoutRes
    int provideActivityLayout();




    //DROPBOX EXPORT IMPORT
    public void exportDbDropbox() {
        LoadingDialog loadingDialog= new LoadingDialog(this);
        loadingDialog.showDialog(null);

        String token = preferences.getString(getString(R.string.key_dropbox_access_token), "");

        if (token!=null && !token.equals(""))
            new AppExecutors().networkIO().execute(()->{
                if (BackupManagement.sendDbToDropbox(token, this))
                    notifyUser(getString(R.string.warning_db_export_success), loadingDialog);
                else
                    notifyUser(getString(R.string.warning_db_export_fail), loadingDialog);

            });
    }


    protected void notifyUser(String text, LoadingDialog loadingDialog){
        new AppExecutors().mainThread().execute(()->{
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            loadingDialog.hideDialog();
        });
    }


    //TRADITIONAL EXPORT IMPORT
    //BACKUP STUFF
    static final int EXPORT_DB = 24;
    static final int IMPORT_DB = 25;
    static final int IMPORT_CSV = 26;

    private void refreshDatabase(){
        //refresh database
        //this one crashes when migration needed but really does work in order to do stuff properly
        //((MyApplication)getApplicationContext()).getUltraRepository().closeDatabase();
        AppRoomDatabase.getInstance(this, new AppExecutors());

        ((MyApplication)getApplicationContext()).getUltraRepository().triggerRefreshAfterBackup();

        new AppExecutors().diskIO().execute(() ->{

            try {
                SettingsEntity settingsEntity= ((MyApplication)getApplicationContext()).getSettingsRepository().getSettings();
                if(settingsEntity!=null) {
                    preferences.edit().putLong(getString(R.string.key_default_currency_id_new), settingsEntity.getCurrency()).apply();
                    new AppExecutors().mainThread().execute(() ->{
                        Locale actualLocale= Locale.getDefault();
                        Locale backupLocale= new Locale(settingsEntity.getLanguage(), settingsEntity.getLocale());
                       // Log.i("kura", backupLocale.getCountry());
                        //if (!actualLocale.equals(backupLocale))
                        //    updateLocale(backupLocale);
                    });

                }
            }catch (Exception e){
                Intent intent = new Intent(this, SplashActivity.class);
                startActivity(intent);
                System.exit(0);
            }
        });


        new NotificationPublisher().scheduleNotification(this);
    }

    public void exportDb(Uri uri) {

        long time= Calendar.getInstance().getTimeInMillis();

        ((MyApplication)getApplicationContext()).getSettingsRepository().updateTimestamp(time);

        try {
            ParcelFileDescriptor fileDescriptorDatabase = this.getContentResolver().openFileDescriptor
                    (Uri.fromFile(getDatabasePath(DATABASE_NAME)), "r");
            assert fileDescriptorDatabase != null;
            FileInputStream currentDb = new FileInputStream(fileDescriptorDatabase.getFileDescriptor());

            try {
                ParcelFileDescriptor fileDescriptorBackup = this.getContentResolver().openFileDescriptor(uri, "w");
                assert fileDescriptorBackup != null;
                FileOutputStream backup =
                        new FileOutputStream(fileDescriptorBackup.getFileDescriptor());

                byte[] buffer = new byte[1024];
                int len;
                while ((len = currentDb.read(buffer)) != -1) {
                    backup.write(buffer, 0, len);

                }

                Toast.makeText(this, getResources().getString(R.string.warning_db_export_success), Toast.LENGTH_LONG).show();

                fileDescriptorBackup.close();
                backup.close();
            } catch (FileNotFoundException e) {
                Toast.makeText(this, getResources().getString(R.string.warning_db_export_fail), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getResources().getString(R.string.warning_db_export_fail), Toast.LENGTH_LONG).show();
        }
    }

    public void importDb(Uri uri) {

        try {
            ParcelFileDescriptor fileDescriptorDatabase = this.getContentResolver().openFileDescriptor
                    (Uri.fromFile(getDatabasePath(DATABASE_NAME)), "w");
            assert fileDescriptorDatabase != null;
            FileOutputStream currentDb = new FileOutputStream(fileDescriptorDatabase.getFileDescriptor());

            try {
                ParcelFileDescriptor fileDescriptorBackup = this.getContentResolver().openFileDescriptor(uri, "r");
                assert fileDescriptorBackup != null;
                FileInputStream backup =
                        new FileInputStream(fileDescriptorBackup.getFileDescriptor());



                byte[] buffer = new byte[1024];
                int len;
                while ((len = backup.read(buffer)) != -1) {
                    currentDb.write(buffer, 0, len);
                }

                Toast.makeText(this, getResources().getString(R.string.warning_db_import_success), Toast.LENGTH_LONG).show();

                fileDescriptorBackup.close();
                backup.close();


                refreshDatabase();

            } catch (FileNotFoundException e) {
                Toast.makeText(this, getResources().getString(R.string.warning_db_import_fail), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getResources().getString(R.string.warning_db_import_fail), Toast.LENGTH_LONG).show();
        }
    }

}
