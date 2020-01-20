package com.unexceptional.beast.banko.newVersion.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.opencsv.CSVReader;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.ActivitySecond2Binding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.CategoryClickCallback;
import com.unexceptional.beast.banko.newVersion.callback.NotificationClickCallback;
import com.unexceptional.beast.banko.newVersion.callback.ProductClickCallback;
import com.unexceptional.beast.banko.newVersion.callback.TaskClickCallback;
import com.unexceptional.beast.banko.newVersion.db.AppRoomDatabase;
import com.unexceptional.beast.banko.newVersion.db.entity.ParticipationTaskEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.SettingsEntity;
import com.unexceptional.beast.banko.newVersion.db.model.Category;
import com.unexceptional.beast.banko.newVersion.db.model.Notification;
import com.unexceptional.beast.banko.newVersion.db.model.Product;
import com.unexceptional.beast.banko.newVersion.db.model.Task;
import com.unexceptional.beast.banko.newVersion.networking.BackupManagement;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.other.NotificationPublisher;
import com.unexceptional.beast.banko.newVersion.ui.dialog.LoadingDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickBankDialog;
import com.unexceptional.beast.banko.newVersion.ui.fragment.SettingsFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.SyncFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased.transaction.TransactionFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.list.CategoryListFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.list.NotificationListFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.list.ProductListFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.list.TaskListFragment;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.ParticipationTaskViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.unexceptional.beast.banko.newVersion.db.AppRoomDatabase.DATABASE_NAME;

public class SecondActivity extends UIActivity implements
        ProductClickCallback,
        CategoryClickCallback,
        NotificationClickCallback,
        TaskClickCallback,
        SyncFragment.OnFragmentInteractionListener{

    ActivitySecond2Binding mBinding;

    @Override
    protected int provideActivityLayout() {
        return R.layout.activity_second_2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding= (ActivitySecond2Binding) dataBinding;
        setClicks();
        getStuffFromIntent();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(getSupportFragmentManager().getBackStackEntryCount()==0)
            finish();
    }

    @Override
    protected void setClicks() {
        super.setClicks();
        filter.setOnClickListener(v ->{
            TaskListFragment taskListFragment= (TaskListFragment) getSupportFragmentManager().findFragmentByTag(Constants.TAG_TASK_LIST_FRAGMENT);
            if(taskListFragment!=null)
                new PickBankDialog(this, bank -> {
                    preferences.edit().putLong(getString(R.string.key_task_list_bank_id), bank.getId()).apply();
                    taskListFragment.reloadItems();
                }, true).showDialog();
            drawerLayout.closeDrawers();
        });

    }

    private void getStuffFromIntent(){
        if (getIntent()!=null){
            String fragmentTag= getIntent().getStringExtra(Constants.KEY_FRAGMENT_TAG);

            switch (fragmentTag){
                case Constants.TAG_PRODUCT_LIST_FRAGMENT:
                    startFragment(ProductListFragment.newInstance(getIntent().getLongExtra(Constants.KEY_BANK_ID, 0)));
                    break;
                case Constants.TAG_SETTINGS_FRAGMENT:
                    startFragment(SettingsFragment.newInstance());
                    break;
                case Constants.TAG_CATEGORY_LIST_FRAGMENT:
                    startFragment(CategoryListFragment.newInstance());
                    break;
                case Constants.TAG_NOTIFICATION_LIST_FRAGMENT:
                    startFragment(NotificationListFragment.newInstance());
                    break;
                case Constants.TAG_TASK_LIST_FRAGMENT:
                    startFragment(TaskListFragment.newInstance());
                    break;
                case Constants.TAG_SYNC_FRAGMENT:
                    startFragment(SyncFragment.newInstance());
                    break;
            }
        }
    }

    private void startFragment(Fragment fragment){
        if(getSupportFragmentManager().findFragmentByTag(fragment.toString())==null){
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment, fragment.toString()).
                    addToBackStack(fragment.toString()).commit();
        }

    }

    @Override
    public void onProductItemClick(Product product) {
        Intent intent= new Intent(SecondActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_PRODUCT_ID, product.getId());

        if (product.getBankId()==101)
            intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_DEBT_SHORTCUTS_FRAGMENT);
        else
            intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_PRODUCT_SHORTCUTS_FRAGMENT);


        startActivity(intent);
    }

    @Override
    public void onCategoryItemClick(Category category) {
        Intent intent= new Intent(SecondActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_CATEGORY_ID, category.getId());
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_CATEGORY_FRAGMENT);
        intent.putExtra(Constants.KEY_CATEGORY_SUB_MODE, category.getParentId()>0);
        startActivity(intent);
    }

    @Override
    public void onNotificationItemClick(Notification notification) {
        switch (notification.getType()){
            case Constants.NOTIFICATION_TYPE_TRANSACTION:
                Intent intent= new Intent(SecondActivity.this, FloatingActivity.class);
                intent.putExtra(Constants.KEY_TRANSACTION_ID, notification.getHostId());
                intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_TRANSACTION_FRAGMENT);
                intent.putExtra(Constants.KEY_TRANSACTION_MODE, TransactionFragment.Mode.MODIFY);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onTaskItemClick(Task task) {
        Intent intent= new Intent(SecondActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_TASK_ID, task.getId());
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_TASK_FRAGMENT);
        startActivity(intent);
    }

    @Override
    public void updateTask(ParticipationTaskEntity task) {
        ViewModelProviders.of(this).get(ParticipationTaskViewModel.class).insert(task);
    }

    @Override
    public void exportDB() {
        BackupManagement.exportDbStart(this);
    }

    @Override
    public void importDB() {
        BackupManagement.importDbStart(this);
    }

    @Override
    public void importFromCSV() {
        BackupManagement.importCSVStart(this);
    }

    @Override
    public void exportDropbox() {
        exportDbDropbox();
    }

    @Override
    public void importDropbox() {
        LoadingDialog loadingDialog= new LoadingDialog(this);
        loadingDialog.showDialog(null);

        new AppExecutors().diskIO().execute(() -> {

            String token = preferences.getString(getString(R.string.key_dropbox_access_token), "");


            File file = BackupManagement.importDbDropbox(token, this);
            if (file !=null) {
                BackupManagement.writeToDatabase(file, this);
                notifyUser(getString(R.string.warning_db_import_fail), loadingDialog);
            }else
                notifyUser(getString(R.string.warning_db_import_success), loadingDialog);
        });

    }

    //BACKUP

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
            }

            if(requestCode == EXPORT_DB){
                exportDb(uri);
            }
            else if (requestCode == IMPORT_CSV){
                try {

                    assert uri != null;
                    ParcelFileDescriptor fileDescriptorBackup = this.getContentResolver().openFileDescriptor(uri, "r");

                    assert fileDescriptorBackup != null;
                    CSVReader reader = new CSVReader(new FileReader(fileDescriptorBackup.getFileDescriptor()));
                    String[] nextLine;
                    while ((nextLine = reader.readNext()) != null) {
                        // nextLine[] is an array of values from the line
                        Toast.makeText(this, nextLine[0] + nextLine[1] + "etc...", Toast.LENGTH_SHORT).show();
                        //System.out.println(nextLine[0] + nextLine[1] + "etc...");
                    }
                } catch (IOException e) {

                }
                //Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
            }
            else if(requestCode == IMPORT_DB){
                if(getResources().getConfiguration().locale.getCountry().equals("PL")){
                    showBackupDialog(uri);
                }else
                    importDb(uri);

            }
        }
    }

    private void showBackupDialog(Uri uri){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = View.inflate(this, R.layout.dialog_backup, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.backup_v14).setOnClickListener(view -> {
            Intent intent= new Intent(this, DatabaseUpgradeActivity.class);
            assert uri != null;
            intent.putExtra(Constants.KEY_IMPORT_DB_URI, uri.toString());
            startActivity(intent);
            finish();
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.backup_r).setOnClickListener(view -> {
            importDb(uri);
            dialog.dismiss();
        });

        dialog.show();
    }
}
