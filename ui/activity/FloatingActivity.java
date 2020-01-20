package com.unexceptional.beast.banko.newVersion.ui.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.ActivityFloatingBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.ProductClickCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.ProductEntity;
import com.unexceptional.beast.banko.newVersion.db.model.Product;
import com.unexceptional.beast.banko.newVersion.networking.MyWorker;
import com.unexceptional.beast.banko.newVersion.ui.fragment.DebtShortcutsFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.ExchangeFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.ProductShortcutsFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased.BudgetFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased.CategoryFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased.DebtFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased.ProductFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased.TaskFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased.transaction.SubTransactionFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased.transaction.TransactionFragment;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.ProductViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Objects;

public class FloatingActivity extends BasicActivity implements
        ProductClickCallback ,
        ProductShortcutsFragment.OnFragmentInteractionListener,
        DebtShortcutsFragment.OnFragmentInteractionListener,
        TransactionFragment.OnFragmentInteractionListener,
        SubTransactionFragment.OnFragmentInteractionListener{

    ActivityFloatingBinding mBinding;
    public boolean comingFromWidget;

    @Override
    protected int provideActivityLayout() {
        return R.layout.activity_floating;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }catch (Exception e){

        }


        mBinding= (ActivityFloatingBinding) dataBinding;

        getStuffFromIntent();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(getSupportFragmentManager().getBackStackEntryCount()==0)
            finish();
    }

    private void getStuffFromIntent(){
        if (getIntent()!=null){
            String fragmentTag= getIntent().getStringExtra(Constants.KEY_FRAGMENT_TAG);

            switch (fragmentTag){
                case Constants.TAG_PRODUCT_SHORTCUTS_FRAGMENT:
                    startFragment(ProductShortcutsFragment.newInstance(0 ,getIntent().getLongExtra(Constants.KEY_PRODUCT_ID, 0)));
                    break;
                case Constants.TAG_DEBT_SHORTCUTS_FRAGMENT:
                    startFragment(DebtShortcutsFragment.newInstance(getIntent().getLongExtra(Constants.KEY_PRODUCT_ID, 0)));
                    break;
                case Constants.TAG_TRANSACTION_FRAGMENT:
                    startFragment(TransactionFragment.newInstance(
                            getIntent().getLongExtra(Constants.KEY_TRANSACTION_ID, 0),
                            getIntent().getBooleanExtra(Constants.KEY_TRANSACTION_SHORTCUT_MODE, true),
                            getIntent().getSerializableExtra(Constants.KEY_TRANSACTION_MODE),
                            //Optional
                            getIntent().getLongExtra(Constants.KEY_PRODUCT_ID, 0),
                            getIntent().getIntExtra(Constants.KEY_TRANSACTION_TYPE, 1),
                            getIntent().getLongExtra(Constants.KEY_CATEGORY_ID, 0),
                            getIntent().getLongExtra(Constants.KEY_DEBT_ACCOUNT_ID, 0)
                            ));

                    comingFromWidget= getIntent().getBooleanExtra(Constants.KEY_COMING_FROM_WIDGET, false);

                    break;

                case Constants.TAG_SUB_TRANSACTION_FRAGMENT:
                    startFragment(SubTransactionFragment.newInstance(
                            getIntent().getLongExtra(Constants.KEY_TRANSACTION_ID, 0),
                            getIntent().getBooleanExtra(Constants.KEY_TRANSACTION_SHORTCUT_MODE, true),
                            getIntent().getSerializableExtra(Constants.KEY_TRANSACTION_MODE)
                    ));

                    comingFromWidget= getIntent().getBooleanExtra(Constants.KEY_COMING_FROM_WIDGET, false);
                    break;
                case Constants.TAG_CATEGORY_FRAGMENT:
                    startFragment(CategoryFragment.newInstance(
                            getIntent().getLongExtra(Constants.KEY_CATEGORY_ID, 0),
                            getIntent().getBooleanExtra(Constants.KEY_CATEGORY_SUB_MODE, false)));
                    break;
                case Constants.TAG_EXCHANGE_FRAGMENT:
                    startFragment(ExchangeFragment.newInstance());
                    break;
                case Constants.TAG_PRODUCT_FRAGMENT:
                    startFragment(ProductFragment.newInstance(
                            getIntent().getLongExtra(Constants.KEY_BANK_ID, 0),
                            getIntent().getLongExtra(Constants.KEY_PRODUCT_ID, 0)));
                    break;

                case Constants.TAG_BUDGET_FRAGMENT:
                    startFragment(BudgetFragment.newInstance(getIntent().getLongExtra(Constants.KEY_BUDGET_ID, 0)));
                    break;

                case Constants.TAG_DEBT_FRAGMENT:
                    startFragment(DebtFragment.newInstance(getIntent().getLongExtra(Constants.KEY_PRODUCT_ID, 0)));
                    break;

                case Constants.TAG_TASK_FRAGMENT:
                    startFragment(TaskFragment.newInstance(getIntent().getLongExtra(Constants.KEY_TASK_ID, 0),
                            getIntent().getIntExtra(Constants.KEY_DATE_OPTION, 1)));
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

    private void addFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.add(R.id.fragment_container, fragment, fragment.toString()).
                addToBackStack(fragment.toString()).commit();
    }

    @Override
    public void onProductItemClick(Product product) {
        addFragment(ProductShortcutsFragment.newInstance(product.getBankId(), product.getId()));
    }

    @Override
    public void edit(Product product) {
        Intent intent= new Intent(FloatingActivity.this, FloatingActivity.class);
        if(product.getBankId()==101){
            intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_DEBT_FRAGMENT);
        }else {
            intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_PRODUCT_FRAGMENT);
            intent.putExtra(Constants.KEY_BANK_ID, product.getBankId());
        }

        intent.putExtra(Constants.KEY_PRODUCT_ID, product.getId());

        startActivity(intent);
    }

    @Override
    public void newTransaction(int transactionType, Product product) {
        Intent intent= new Intent(FloatingActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_TRANSACTION_FRAGMENT);
        intent.putExtra(Constants.KEY_TRANSACTION_SHORTCUT_MODE, false);
        intent.putExtra(Constants.KEY_TRANSACTION_TYPE, transactionType);
        intent.putExtra(Constants.KEY_PRODUCT_ID, product.getId());

        //overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_out_bottom);
        startActivity(intent);
    }

    @Override
    public void transactionList(Product product) {
        Intent intent= new Intent(FloatingActivity.this, MainActivity.class);
        preferences.edit().putInt(getString(R.string.key_temp_select_main_nav), 2).apply();
        preferences.edit().putLong(getString(R.string.key_tr_list_product_id), product.getId()).apply();
        preferences.edit().putLong(getString(R.string.key_tr_list_bank_id), product.getBankId()).apply();
        startActivity(intent);
    }

    @Override
    public void taskList(Product product) {
        Intent intent= new Intent(FloatingActivity.this, SecondActivity.class);
        preferences.edit().putLong(getString(R.string.key_task_list_bank_id), product.getBankId()).apply();
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_TASK_LIST_FRAGMENT);

        startActivity(intent);
    }

    @Override
    public void changeBalance(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = View.inflate(this, R.layout.dialog_edit_text, null);
        builder.setView(dialogView);

        final TextInputEditText editText =  dialogView.findViewById(R.id.dialog_edit_text);
        Button btnAccept =  dialogView.findViewById(R.id.btn_accept);
        Button btnDecline =  dialogView.findViewById(R.id.btn_decline);

        final AlertDialog dialog = builder.create();

        editText.setText(MyApplication.money.format(product.getBalance()));

        btnAccept.setOnClickListener(v -> {
            ProductEntity productEntity= new ProductEntity(product);
            String balance= Objects.requireNonNull(editText.getText()).toString().replace(",", ".");

            try {
                productEntity.setBalance((double) Math.round(Double.parseDouble(balance) * 100) / 100);
            }catch (Exception e){
                productEntity.setBalance(0);
            }

            ViewModelProviders.of(this).get(ProductViewModel.class).update(productEntity);
            dialog.dismiss();
        });

        btnDecline.setOnClickListener(v -> dialog.dismiss());

        dialog.show();{

        }
    }


    //Debt Shortcut
    @Override
    public void editDebt(Product product) {
        Intent intent= new Intent(FloatingActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_DEBT_FRAGMENT);
        intent.putExtra(Constants.KEY_PRODUCT_ID, product.getId());

        startActivity(intent);
    }

    @Override
    public void borrowed(Product product) {
        Intent intent= new Intent(FloatingActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_TRANSACTION_FRAGMENT);
        intent.putExtra(Constants.KEY_TRANSACTION_SHORTCUT_MODE, false);
        intent.putExtra(Constants.KEY_TRANSACTION_TYPE, Constants.TRANSACTION_TYPE_EXPENSE);
        intent.putExtra(Constants.KEY_CATEGORY_ID, (long) 1 );
        intent.putExtra(Constants.KEY_DEBT_ACCOUNT_ID, product.getId());

        startActivity(intent);
    }

    @Override
    public void boughtMe(Product product) {
        Intent intent= new Intent(FloatingActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_TRANSACTION_FRAGMENT);
        intent.putExtra(Constants.KEY_TRANSACTION_SHORTCUT_MODE, false);
        intent.putExtra(Constants.KEY_TRANSACTION_TYPE, Constants.TRANSACTION_TYPE_EXPENSE);
        intent.putExtra(Constants.KEY_PRODUCT_ID, product.getId());

        startActivity(intent);
    }

    @Override
    public void paidDebt(Product product) {
        Intent intent= new Intent(FloatingActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_TRANSACTION_FRAGMENT);
        intent.putExtra(Constants.KEY_TRANSACTION_SHORTCUT_MODE, false);
        intent.putExtra(Constants.KEY_TRANSACTION_TYPE, Constants.TRANSACTION_TYPE_INCOME);
        intent.putExtra(Constants.KEY_CATEGORY_ID, (long) 1 );
        intent.putExtra(Constants.KEY_DEBT_ACCOUNT_ID, product.getId());

        startActivity(intent);
    }

    //Transaction Fragment
    @Override
    public void duplicate(long id) {
        Intent intent= new Intent(FloatingActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_TRANSACTION_FRAGMENT);
        intent.putExtra(Constants.KEY_TRANSACTION_SHORTCUT_MODE, false);
        intent.putExtra(Constants.KEY_TRANSACTION_ID, id);
        intent.putExtra(Constants.KEY_TRANSACTION_MODE, TransactionFragment.Mode.DUPLICATE);
        intent.putExtra(Constants.KEY_COMING_FROM_WIDGET, comingFromWidget);

        startActivity(intent);
        if (comingFromWidget)
            triggerSync();
        finish();
    }

    @Override
    public void split(long id) {
        Intent intent= new Intent(FloatingActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_SUB_TRANSACTION_FRAGMENT);
        intent.putExtra(Constants.KEY_TRANSACTION_SHORTCUT_MODE, false);
        intent.putExtra(Constants.KEY_TRANSACTION_ID, id);
        intent.putExtra(Constants.KEY_TRANSACTION_MODE, SubTransactionFragment.Mode.SPLIT);
        intent.putExtra(Constants.KEY_COMING_FROM_WIDGET, comingFromWidget);

        startActivity(intent);
        if (comingFromWidget)
            triggerSync();
        finish();
    }

    @Override
    public void subTransaction(long id) {
        Intent intent= new Intent(FloatingActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_SUB_TRANSACTION_FRAGMENT);
        intent.putExtra(Constants.KEY_TRANSACTION_SHORTCUT_MODE, false);
        intent.putExtra(Constants.KEY_TRANSACTION_ID, id);
        intent.putExtra(Constants.KEY_TRANSACTION_MODE, SubTransactionFragment.Mode.NEW_SUB);
        intent.putExtra(Constants.KEY_COMING_FROM_WIDGET, comingFromWidget);


        startActivity(intent);
        if (comingFromWidget)
            triggerSync();
        finish();
    }

    @Override
    public void duplicateSub(long id) {
        Intent intent= new Intent(FloatingActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_SUB_TRANSACTION_FRAGMENT);
        intent.putExtra(Constants.KEY_TRANSACTION_SHORTCUT_MODE, false);
        intent.putExtra(Constants.KEY_TRANSACTION_ID, id);
        intent.putExtra(Constants.KEY_TRANSACTION_MODE, SubTransactionFragment.Mode.DUPLICATE);
        intent.putExtra(Constants.KEY_COMING_FROM_WIDGET, comingFromWidget);

        //overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_out_bottom);
        startActivity(intent);
        if (comingFromWidget)
            triggerSync();

        finish();
    }

    //for both sub and transaction fragment
    @Override
    public void closeRealisingSynch() {
        if (comingFromWidget)
            triggerSync();

        onBackPressed();
    }

    private void triggerSync(){

        String token = preferences.getString(getString(R.string.key_dropbox_access_token), "");
        boolean fullSync= preferences.getBoolean(getString(R.string.key_full_dropbox_sync), false);

        if (token !=null && !token.equals("") && fullSync){
            Data data = new Data.Builder()
                    .putBoolean(MyWorker.GO_HAM, true)
                    .build();

            OneTimeWorkRequest simpleRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
                    .setInputData(data)
                    .build();
            WorkManager.getInstance().enqueue(simpleRequest);
        }
    }

}
