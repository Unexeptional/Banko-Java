package com.unexceptional.beast.banko.newVersion.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.database.ConstantsTable;
import com.unexceptional.beast.banko.database.DatabaseHelper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DatabaseUpgradeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_upgrade);

        if(getIntent()!=null){
            if(getIntent().getStringExtra(Constants.KEY_IMPORT_DB_URI)!=null){
                Uri uri= Uri.parse(getIntent().getStringExtra(Constants.KEY_IMPORT_DB_URI));
                new ImportTask(uri, DatabaseUpgradeActivity.this).execute();
            }
        }
    }

    private void importDb(Uri uri) {

        try {
            ParcelFileDescriptor fileDescriptorDatabase = this.getContentResolver().openFileDescriptor
                    (Uri.fromFile(getDatabasePath(ConstantsTable.DATABASE_NAME)), "w");
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

               
                fileDescriptorBackup.close();
                backup.close();
            } catch (FileNotFoundException e) {
                //Toast.makeText(this, getResources().getString(R.string.warning_db_import_fail), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
            //Toast.makeText(this, getResources().getString(R.string.warning_db_import_fail), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ImportTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;
        Uri uri;

        ImportTask(Uri uri, Activity activity) {
            this.uri=uri;
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.warning_progress_importing_database));
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            importDb(uri);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();

            DatabaseHelper.getInstance(DatabaseUpgradeActivity.this).dropInstance();
            
            Intent intent= new Intent(DatabaseUpgradeActivity.this, SplashActivity.class);
            PreferenceManager.getDefaultSharedPreferences(DatabaseUpgradeActivity.this).
                    edit().putBoolean(getString(R.string.key_room_database_need_populate), true).apply();

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
