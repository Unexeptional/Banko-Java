package com.unexceptional.beast.banko.newVersion.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.ActivityMainNewBinding;
import com.unexceptional.beast.banko.databinding.DialogTransactionsFilterBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.BankClickCallback;
import com.unexceptional.beast.banko.newVersion.callback.BudgetClickCallback;
import com.unexceptional.beast.banko.newVersion.callback.PickBankCallback;
import com.unexceptional.beast.banko.newVersion.callback.PickCategoryCallback;
import com.unexceptional.beast.banko.newVersion.callback.PickProductCallback;
import com.unexceptional.beast.banko.newVersion.callback.ProductClickCallback;
import com.unexceptional.beast.banko.newVersion.callback.TransactionClickCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.TransactionEntity;
import com.unexceptional.beast.banko.newVersion.db.model.Bank;
import com.unexceptional.beast.banko.newVersion.db.model.Budget;
import com.unexceptional.beast.banko.newVersion.db.model.Product;
import com.unexceptional.beast.banko.newVersion.db.model.Transaction;
import com.unexceptional.beast.banko.newVersion.networking.BackupManagement;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.ui.adapter.SimpleTransactionViewAdapter;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickBankDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickProductDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.categories.PickParentCategoryDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.categories.PickSubCategoryDialog;
import com.unexceptional.beast.banko.newVersion.ui.fragment.ChartsFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.MoreFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased.transaction.SubTransactionFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased.transaction.TransactionFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.list.BankListFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.list.BudgetListFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.list.ProductListFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.list.TransactionListFragment;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CategoryViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.ProductViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.TransactionListViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends UIActivity implements
        MoreFragment.OnFragmentInteractionListener,
        BankClickCallback,
        ProductClickCallback,
        TransactionClickCallback,
        BudgetClickCallback {

    private ActivityMainNewBinding mBinding;
    private AlertDialog transactionsFilter;
    private RewardedVideoAd mRewardedVideoAd;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_accounts:
                        if (preferences.getBoolean(getString(R.string.key_show_bank_list),false)){
                            startFragment(BankListFragment.newInstance());
                        }else {
                            startFragment(ProductListFragment.newInstance(0));
                        }
                        return true;
                    case R.id.navigation_charts:
                        startFragment(ChartsFragment.newInstance());
                        return true;
                    case R.id.navigation_transactions:
                        startFragment(TransactionListFragment.newInstance());
                        return true;
                    case R.id.navigation_budgets:
                        startFragment(BudgetListFragment.newInstance());
                        return true;
                    case R.id.navigation_more:
                        startFragment(MoreFragment.newInstance());
                        return true;
                }
                return false;
            };


    //BASIC
    @Override
    protected int provideActivityLayout() {
        return R.layout.activity_main_new;
    }

    //ACTIVITY
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding= (ActivityMainNewBinding) dataBinding;

        mBinding.navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        setClicks();

        if (savedInstanceState== null)
            selectNavigation(preferences.getInt(getString(R.string.key_show_at_start_option),2));


        //testing
        //AdRequest adRequest= new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();

        MobileAds.initialize(this, "ca-app-pub-1567773436889509~1587460751");

        if ( System.currentTimeMillis() - preferences.getLong(getString(R.string.key_ads_disabled_timeout), 0L) > 0){
            AdRequest adRequest= new AdRequest.Builder().build();
            mBinding.adView.loadAd(adRequest);
            mBinding.adView.setVisibility(View.VISIBLE);
        }else
            mBinding.adView.setVisibility(View.GONE);


        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                mRewardedVideoAd.show();
            }

            @Override
            public void onRewardedVideoAdOpened() {
            }

            @Override
            public void onRewardedVideoStarted() {
            }

            @Override
            public void onRewardedVideoAdClosed() {
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                Toast.makeText(MainActivity.this, getString(R.string.toast_stopped_ads), Toast.LENGTH_SHORT).show();
                // Reward the user.
                Calendar.getInstance().getTimeInMillis();
                mBinding.adView.setVisibility(View.GONE);
                preferences.edit().putLong(getString(R.string.key_ads_disabled_timeout), Calendar.getInstance().getTimeInMillis() + 86400000L).apply();
            }


            
            @Override
            public void onRewardedVideoAdLeftApplication() {
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                Toast.makeText(MainActivity.this, getString(R.string.toast_failed_to_load_ad), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedVideoCompleted() {
            }
        });



        if (preferences.getBoolean(getString(R.string.key_show_tutorial_first), true)){
            showTutorialList();
            preferences.edit().putBoolean(getString(R.string.key_show_tutorial_first), false).apply();

        }else if (preferences.getBoolean(getString(R.string.key_show_rewarded_info), true)){
            stopAds();
            preferences.edit().putBoolean(getString(R.string.key_show_rewarded_info), false).apply();
        }



    }


    @Override
    protected void setClicks(){
        super.setClicks();
        filter.setOnClickListener(v -> {
            switch ( mBinding.navigation.getSelectedItemId()){
                case R.id.navigation_charts:
                    if (preferences.getBoolean(getString(R.string.key_allow_debts), true))
                        showChartsFilter();
                    break;
                case R.id.navigation_transactions:
                    showTransactionListFilter();
                    break;
            }
            mBinding.drawerLayout.closeDrawers();
        });

/*
        mBinding.swipeContainer.setOnRefreshListener(() -> {
            //BackupManagement.importDbDropbox(this);
            initial=true;
            importDbDropbox();
            mBinding.swipeContainer.setRefreshing(false);
        });
*/
    }


    public void selectNavigation(int item) {
        switch (item) {
            case 0:
                mBinding.navigation.setSelectedItemId(R.id.navigation_accounts);
                break;
            case 1:
                mBinding.navigation.setSelectedItemId(R.id.navigation_charts);
                break;
            case 2:
                mBinding.navigation.setSelectedItemId(R.id.navigation_transactions);

                break;
            case 3:
                mBinding.navigation.setSelectedItemId(R.id.navigation_budgets);
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
       int item= preferences.getInt(getString(R.string.key_temp_select_main_nav), 0);
       preferences.edit().remove(getString(R.string.key_temp_select_main_nav)).apply();

      // item= intent.getIntExtra(Constants.KEY_MAIN_NAVIGATION,0);
        selectNavigation(item);
    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()>0){
            getSupportFragmentManager().popBackStack();
            return;
        }

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.toast_closing_app), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
    }

    private void startFragment(Fragment fragment){
        if(getSupportFragmentManager().findFragmentByTag(fragment.toString())==null){
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment, fragment.toString()).commit();
        }
    }

    //MORE FRAGMENT CALLBACKS
    @Override
    public void startSettingsFragment() {
        Intent intent= new Intent(MainActivity.this, SettingsActivity.class);
        //intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_SETTINGS_FRAGMENT);
        startActivity(intent);
    }

    @Override
    public void startTasksFragment() {
        Intent intent= new Intent(MainActivity.this, SecondActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_TASK_LIST_FRAGMENT);
        startActivity(intent);
    }

    @Override
    public void startNotificationsFragment() {
        Intent intent= new Intent(MainActivity.this, SecondActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_NOTIFICATION_LIST_FRAGMENT);
        startActivity(intent);
    }

    @Override
    public void startExchangeFragment() {
        Intent intent= new Intent(MainActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_EXCHANGE_FRAGMENT);
        startActivity(intent);
    }

    @Override
    public void startDebtsFragment() {
        Intent intent= new Intent(MainActivity.this, SecondActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_PRODUCT_LIST_FRAGMENT);
        intent.putExtra(Constants.KEY_BANK_ID, 101L);
        startActivity(intent);
    }

    @Override
    public void showBankPromos() {
        String packageName="pl.rozbijbank";

        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            // Bring user to the market or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void stopAds()/*{

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = View.inflate(this, R.layout.dialog_stop_ads, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btn_decline).setOnClickListener(view -> dialog.dismiss());
        Button showAd= dialogView.findViewById(R.id.btn_accept);


        showAd.setText(R.string.show_ad);
        showAd.setOnClickListener(view -> {
            mRewardedVideoAd.loadAd("ca-app-pub-1567773436889509/2280291286",
                    new AdRequest.Builder().build());
            dialog.dismiss();
            Toast.makeText(this, getString(R.string.toast_loading_ad), Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }*/

    {
        startActivity(new Intent(this, billingtest.class));
    }


    @Override
    public void startLoginFragment() {

    }

    @Override
    public void startRegisterFragment() {

    }

    @Override
    public void startAccountFragment() {

    }

    @Override
    public void startTutorialsFragment() {
        showTutorialList();
    }

    @Override
    public void startAboutFragment() {

    }

    @Override
    public void rateThisApp() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.unexceptional.beast.banko")));
    }

    @Override
    public void sendFeedback() {

    }

    @Override
    public void backupOptions() {
        Intent intent= new Intent(MainActivity.this, SecondActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_SYNC_FRAGMENT);
        startActivity(intent);
    }

    @Override
    public void privacyPolicy() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/bankopolityka")));
    }

    //ENTITY CLICKS
    @Override
    public void onBankItemClick(Bank bank) {
        Intent intent= new Intent(MainActivity.this, SecondActivity.class);
        intent.putExtra(Constants.KEY_BANK_ID, bank.getId());
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_PRODUCT_LIST_FRAGMENT);
        startActivity(intent);
    }

    @Override
    public void onBudgetItemClick(Budget budget) {
        Intent intent= new Intent(MainActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_BUDGET_ID, budget.getId());
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_BUDGET_FRAGMENT);
        startActivity(intent);
    }

    @Override
    public void onProductItemClick(Product product) {
        Intent intent= new Intent(MainActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_PRODUCT_ID, product.getId());
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_PRODUCT_SHORTCUTS_FRAGMENT);
       // overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_out_bottom);
        startActivity(intent);
    }

    @Override
    public void onTransactionItemClick(Transaction transaction) {
        if(transaction.getKidsAmount()>0){
            showSubTransactionsDialog(transaction.getId());
        }else
            transactionClick(transaction);
    }

    //clicks onto dialog (multiple categories)
    private void transactionClick(Transaction transaction){
        Intent intent= new Intent(MainActivity.this, FloatingActivity.class);
        intent.putExtra(Constants.KEY_TRANSACTION_ID, transaction.getId());

        if (transaction.getParentTransactionId()>0) {
            intent.putExtra(Constants.KEY_TRANSACTION_MODE, SubTransactionFragment.Mode.MODIFY);
            intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_SUB_TRANSACTION_FRAGMENT);
        } else {
            intent.putExtra(Constants.KEY_TRANSACTION_MODE, TransactionFragment.Mode.MODIFY);
            intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_TRANSACTION_FRAGMENT);
        }

        //overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_out_bottom);
        startActivity(intent);
    }

    private void showSubTransactionsDialog(long parentId){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = View.inflate(this, R.layout.dialog_transactions, null);
        builder.setView(dialogView);

        RecyclerView recyclerView= dialogView.findViewById(R.id.list);

        TransactionClickCallback callback= this::transactionClick;

        SimpleTransactionViewAdapter adapter= new SimpleTransactionViewAdapter(callback);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        LiveData<List<TransactionEntity>> liveData;
        //get items
        liveData= ViewModelProviders.of(this).get(TransactionListViewModel.class).getFewTransactions(parentId);

        liveData.observe(this, transactions -> {
            if (transactions != null) {
               // liveData.removeObservers(this);
                adapter.setTransactionList(transactions);
            }
        });


        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Tutorials
    protected void showTutorialList(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = View.inflate(this, R.layout.dialog_tutorials, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btn_first_promo).setOnClickListener(view -> showTutActivity(Constants.TUTORIAL_TYPE_NEW_TRANSACTION));
        dialogView.findViewById(R.id.btn_debts).setOnClickListener(view -> showTutActivity(Constants.TUTORIAL_TYPE_DEBT));
        dialogView.findViewById(R.id.btn_tasks).setOnClickListener(view -> showTutActivity(Constants.TUTORIAL_TYPE_TASKS));
        dialogView.findViewById(R.id.btn_budgets).setOnClickListener(view -> showTutActivity(Constants.TUTORIAL_TYPE_BUDGETS));
        dialogView.findViewById(R.id.btn_new_wersion_changes).setOnClickListener(view -> showTutActivity(Constants.TUTORIAL_TYPE_NEW_VERSION));
        dialogView.findViewById(R.id.btn_advanced_transactions).setOnClickListener(view -> showTutActivity(Constants.TUTORIAL_TYPE_ADVANCED_TRANSACTIONS));
        dialogView.findViewById(R.id.btn_quick_panel).setOnClickListener(view -> showTutActivity(Constants.TUTORIAL_TYPE_QUICK_PANEL));
        dialogView.findViewById(R.id.btn_widgets).setOnClickListener(view -> showTutActivity(Constants.TUTORIAL_TYPE_WIDGET));

        if(getResources().getConfiguration().locale.getCountry().equals("PL")){
            dialogView.findViewById(R.id.btn_new_wersion_changes).setVisibility(View.VISIBLE);
        }else
            dialogView.findViewById(R.id.btn_new_wersion_changes).setVisibility(View.GONE);

        dialog.show();
    }

    private void showTutActivity(int tutType){
        Intent intent= new Intent(MainActivity.this, TutorialActivity.class);
        intent.putExtra(Constants.KEY_TUTORIAL_TYPE, tutType);
        startActivity(intent);
    }

    //CHARTS FILTER
    private void reloadChartsFragment(){
        ChartsFragment chartsFragment= (ChartsFragment) getSupportFragmentManager().
                findFragmentByTag(Constants.TAG_CHARTS_FRAGMENT);
        if(chartsFragment!=null)
            chartsFragment.reloadItems();
    }

    protected void showChartsFilter(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //bind views
        View dialogView = View.inflate(this, R.layout.dialog_charts_filter, null);
        builder.setView(dialogView);

        CheckBox overall= dialogView.findViewById(R.id.show_overall_chart);
        CheckBox hideDebts= dialogView.findViewById(R.id.hide_debts);
        CheckBox expenses= dialogView.findViewById(R.id.show_expense_chart);
        CheckBox incomes= dialogView.findViewById(R.id.show_income_chart);

        //set checkboxes
        overall.setChecked(preferences.getBoolean(getString(R.string.key_show_overall_chart), true));
        hideDebts.setChecked(preferences.getBoolean(getString(R.string.key_hide_debt_in_chart), true));


        overall.setOnClickListener(v -> {
            preferences.edit().putBoolean(getString(R.string.key_show_overall_chart), overall.isChecked()).apply();
            reloadChartsFragment();
        });

        hideDebts.setOnClickListener(v -> {
            preferences.edit().putBoolean(getString(R.string.key_hide_debt_in_chart), hideDebts.isChecked()).apply();
            reloadChartsFragment();
        });

        AlertDialog chartsFilter = builder.create();
        chartsFilter.show();
    }

    //TRANSACTIONS FILTER
    private void reloadTransactionListFragment(){
        TransactionListFragment transactionListFragment= (TransactionListFragment) getSupportFragmentManager().
                findFragmentByTag(Constants.TAG_TRANSACTION_LIST_FRAGMENT);
        if(transactionListFragment!=null)
            transactionListFragment.reloadItems();
    }

    private void showTransactionListFilter(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        DialogTransactionsFilterBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout. dialog_transactions_filter,
                null, false);
        builder.setView(binding.getRoot());

        setFilterClicks(binding);
        setFilterVisuals(binding);

        transactionsFilter = builder.create();
        transactionsFilter.show();
    }

    private void setFilterClicks(DialogTransactionsFilterBinding binding){
        binding.btnOk.setOnClickListener(v -> transactionsFilter.dismiss());

        binding.btnClear.setOnClickListener(v -> {
            preferences.edit()
                    .remove(getString(R.string.key_tr_list_product_id))
                    .remove(getString(R.string.key_tr_list_bank_id))
                    .remove(getString(R.string.key_tr_list_category_id_new))
                    .remove(getString(R.string.key_tr_list_sub_category_id_new)).apply();
            transactionsFilter.dismiss();
            reloadTransactionListFragment();
        });

        binding.hideDebts.setOnClickListener(v -> {
            preferences.edit().putBoolean(getString(R.string.key_tr_list_hide_debts), binding.hideDebts.isChecked()).apply();
            reloadTransactionListFragment();
        });

        binding.groupTransactions.setOnClickListener(v -> {
            preferences.edit().putBoolean(getString(R.string.key_tr_list_group_transactions), binding.groupTransactions.isChecked()).apply();
            reloadTransactionListFragment();
        });

        binding.showPlanned.setOnClickListener(v -> {
            preferences.edit().putBoolean(getString(R.string.key_tr_list_show_planned), binding.showPlanned.isChecked()).apply();
            reloadTransactionListFragment();
        });

        binding.filterExpenses.setOnClickListener(v -> {
            preferences.edit().putBoolean(getString(R.string.key_tr_list_show_expenses), binding.filterExpenses.isChecked()).apply();
            reloadTransactionListFragment();
        });

        binding.filterIncomes.setOnClickListener(v -> {
            preferences.edit().putBoolean(getString(R.string.key_tr_list_show_incomes), binding.filterIncomes.isChecked()).apply();
            reloadTransactionListFragment();
        });

        binding.filterTransfers.setOnClickListener(v -> {
            preferences.edit().putBoolean(getString(R.string.key_tr_list_show_transfers), binding.filterTransfers.isChecked()).apply();
            reloadTransactionListFragment();
        });

        binding.filterBank.setOnClickListener(v -> {
            if (preferences.getLong(getResources().getString(R.string.key_tr_list_bank_id),0)==0){
                PickBankCallback fromBankCallback= bank -> setBankAccountFilters(binding, 0,bank.getId());

                new PickBankDialog(this, fromBankCallback, true).showDialog();
            }else {
                setBankAccountFilters(binding, 0, 0);
            }
        });

        binding.filterProduct.setOnClickListener(v -> {
            if (preferences.getLong(getResources().getString(R.string.key_tr_list_product_id),0)==0){
                PickProductCallback productCallback= product -> {
                    if(product.getId()>0){
                        setBankAccountFilters(binding, product.getId(), product.getBankId());
                    }
                };

                new PickProductDialog(this, productCallback, preferences.getLong(getString(R.string.key_tr_list_bank_id),0)).showDialog();
            }else
                setBankAccountFilters(binding, 0, preferences.getLong(getString(R.string.key_tr_list_bank_id), 0));

        });

        binding.categoryIcon.setOnClickListener(v -> {
            if (preferences.getLong(getResources().getString(R.string.key_tr_list_category_id_new),0)==0){
                PickCategoryCallback pickCategoryCallback= category -> setCategoryFilters(binding, category.getId(), 0);

                new PickParentCategoryDialog(this, pickCategoryCallback, 1).showDialog();
            }else
                setCategoryFilters(binding, 0,0);
        });

        binding.subcategory.setOnClickListener(v -> {
            if (preferences.getLong(getResources().getString(R.string.key_tr_list_sub_category_id_new),0)==0){
                PickCategoryCallback subCallback= category -> setCategoryFilters(binding, category.getParentId(), category.getId());
                long parentId= preferences.getLong(getString(R.string.key_tr_list_category_id_new), 0);
                if(parentId>0) {
                    new AppExecutors().diskIO().execute(() -> {

                        if(ViewModelProviders.of(this).get(CategoryViewModel.class).hasKids(parentId))
                            new AppExecutors().mainThread().execute(() -> new PickSubCategoryDialog(MainActivity.this, subCallback, parentId).showDialog());

                    });

                } else
                    binding.categoryIcon.callOnClick();

            }else
                setCategoryFilters(binding,  preferences.getLong(getString(R.string.key_tr_list_category_id_new), 0), 0);
        });
    }

    private void setFilterVisuals(DialogTransactionsFilterBinding binding){
        if(!preferences.getBoolean(getString(R.string.key_allow_debts), true))
            binding.hideDebts.setVisibility(View.GONE);

        binding.hideDebts.setChecked(preferences.getBoolean(getString(R.string.key_tr_list_hide_debts), false));
        binding.groupTransactions.setChecked(preferences.getBoolean(getString(R.string.key_tr_list_group_transactions), true));
        binding.showPlanned.setChecked(preferences.getBoolean(getString(R.string.key_tr_list_show_planned), true));

        binding.filterExpenses.setChecked(preferences.getBoolean(getString(R.string.key_tr_list_show_expenses), true));
        binding.filterIncomes.setChecked(preferences.getBoolean(getString(R.string.key_tr_list_show_incomes), true));
        binding.filterTransfers.setChecked(preferences.getBoolean(getString(R.string.key_tr_list_show_transfers), true));

        setBankProductVisual(binding);
        setCategoryVisual(binding);
    }

    //bank product
    private void setBankAccountFilters(DialogTransactionsFilterBinding binding, long productId, long bankId){
        preferences.edit().putLong(getString(R.string.key_tr_list_product_id), productId).apply();
        preferences.edit().putLong(getString(R.string.key_tr_list_bank_id), bankId).apply();
        setBankProductVisual(binding);
        reloadTransactionListFragment();
    }

    private void setBankProductVisual(DialogTransactionsFilterBinding binding){
        long bankId= preferences.getLong(getResources().getString(R.string.key_tr_list_bank_id),0);
        int id = MyApplication.getBankIconId(bankId);
        if (id != 0)
            Picasso.get().load(id).into(binding.filterBank);
        else
            Picasso.get().load(R.drawable.baseline_account_balance_black_24).into(binding.filterBank);

        long productId= preferences.getLong(getResources().getString(R.string.key_tr_list_product_id),0);
        ViewModelProviders.of(this).get(ProductViewModel.class).getProduct(productId).observe(this, product -> {
            if (product!=null){
                binding.filterProduct.setText(product.getTitle());
            }else {
                binding.filterProduct.setText(R.string.account);
            }
        });
    }

    //category
    private void setCategoryFilters(DialogTransactionsFilterBinding binding, long categoryId, long subcategoryId){
        preferences.edit().putLong(getString(R.string.key_tr_list_category_id_new), categoryId).apply();
        preferences.edit().putLong(getString(R.string.key_tr_list_sub_category_id_new), subcategoryId).apply();
        reloadTransactionListFragment();
        setCategoryVisual(binding);
    }

    private void setCategoryVisual(DialogTransactionsFilterBinding binding){
        long categoryId= preferences.getLong(getResources().getString(R.string.key_tr_list_category_id_new),0);
        long subCategoryId= preferences.getLong(getResources().getString(R.string.key_tr_list_sub_category_id_new),0);

        ViewModelProviders.of(this).get(CategoryViewModel.class).getCategory(categoryId).observe(this, categoryEntity -> {
                    if (categoryEntity!=null){
                        int id = MyApplication.getDrawableId(categoryEntity.getIconName());
                        if (id != 0)
                            Picasso.get().load(id).into(binding.categoryIcon);
                    }else
                        Picasso.get().load(R.drawable.outline_category_black_24).into(binding.categoryIcon);

                });

        ViewModelProviders.of(this).get(CategoryViewModel.class).getCategory(subCategoryId)
                .observe(this, categoryEntity -> {
                    if (categoryEntity!=null)
                        binding.subcategory.setText(categoryEntity.getTitle());
                    else
                        binding.subcategory.setText(R.string.subcategory);
                });
    }
}
