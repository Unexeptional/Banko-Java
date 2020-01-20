package com.unexceptional.beast.banko.newVersion.ui.fragment.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.TransactionListBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.DateNavigationCallback;
import com.unexceptional.beast.banko.newVersion.callback.TransactionClickCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.CurrencyEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.NotificationEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.TransactionEntity;
import com.unexceptional.beast.banko.newVersion.db.model.Transaction;
import com.unexceptional.beast.banko.newVersion.networking.BackupManagement;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.ui.activity.MainActivity;
import com.unexceptional.beast.banko.newVersion.ui.adapter.TransactionViewAdapter;
import com.unexceptional.beast.banko.newVersion.ui.dialog.LoadingDialog;
import com.unexceptional.beast.banko.newVersion.ui.fragment.dateNavigation.SimpleDateNavigationFragment;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CurrencyViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.NotificationViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.TransactionListViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TransactionListFragment extends BasicListFragment {

    private TransactionListBinding mBinding;
    private TransactionViewAdapter viewAdapter;
    private TransactionClickCallback mListener;
    private LiveData<List<TransactionEntity >> liveData;
    private double expenses=0, incomes=0, transfers=0;
    private TransactionListViewModel transactionListViewModel;

    private DateNavigationCallback dateNavigationCallback= this::reloadItems;


    @Override
    void setViewModels() {
        transactionListViewModel = ViewModelProviders.of(this).get(TransactionListViewModel.class);
    }

    @Override
    void setAdapters(){
        viewAdapter= new TransactionViewAdapter(mListener);
        mBinding.list.setAdapter(viewAdapter);
    }

    @Override
    public void reloadItems(){
        if(liveData!=null)
            liveData.removeObservers(this);

        liveData= transactionListViewModel.getTransactions(
                getQuery());

        liveData.observe(this, transactionEntities -> {
            if(transactionEntities!=null && !transactionEntities.isEmpty()){
                expenses=0;
                incomes=0;
                transfers=0;
                List<TransactionEntity> list= new ArrayList<>();

                CurrencyViewModel currencyViewModel= ViewModelProviders.of(TransactionListFragment.this).get(CurrencyViewModel.class);

                long defaultCurr= preferences.getLong(getString(R.string.key_default_currency_id_new),1);
                boolean showPlanned= preferences.getBoolean(getString(R.string.key_tr_list_show_planned), true);



                for (TransactionEntity transaction:transactionEntities){

                    if (showPlanned)
                        calculateSummary(transaction, currencyViewModel, defaultCurr);
                    else if (transaction.getDate().before(Calendar.getInstance().getTime()))
                        calculateSummary(transaction, currencyViewModel, defaultCurr);

/*
                    switch (transaction.getTransactionType()){
                        case Constants.TRANSACTION_TYPE_EXPENSE:
                            if (defaultCurr!= transaction.getCurrencyId()){
                                new AppExecutors().diskIO().execute(() -> {
                                    CurrencyEntity productCurrency= currencyViewModel.getCurrencyRapid(transaction.getCurrencyId());
                                    expenses+= transaction.getAmount()*productCurrency.getExchangeRate();
                                    new AppExecutors().mainThread().execute(() -> mBinding.expenses.setText(MyApplication.money.format(expenses)));
                                });
                            }else
                                expenses+= transaction.getAmount();
                            break;
                        case Constants.TRANSACTION_TYPE_INCOME:
                            if (defaultCurr!= transaction.getCurrencyId()){
                                new AppExecutors().diskIO().execute(() -> {
                                    CurrencyEntity productCurrency= currencyViewModel.getCurrencyRapid(transaction.getCurrencyId());
                                    incomes+= transaction.getAmount()*productCurrency.getExchangeRate();
                                    new AppExecutors().mainThread().execute(() -> mBinding.incomes.setText(MyApplication.money.format(incomes)));
                                });
                            }else
                                incomes+= transaction.getAmount();
                            break;
                        case Constants.TRANSACTION_TYPE_TRANSFER:
                            if (defaultCurr!= transaction.getCurrencyId()){
                                new AppExecutors().diskIO().execute(() -> {
                                    CurrencyEntity productCurrency= currencyViewModel.getCurrencyRapid(transaction.getCurrencyId());
                                    transfers+= transaction.getAmount()*productCurrency.getExchangeRate();
                                    new AppExecutors().mainThread().execute(() -> mBinding.transfers.setText(MyApplication.money.format(transfers)));
                                });
                            }else
                                transfers+= transaction.getAmount();
                            break;
                    }
*/



                    //grouping or not
                    if (preferences.getBoolean(getString(R.string.key_tr_list_group_transactions), true)){
                        LongSparseArray<Double> sparseArray= new LongSparseArray<>();

                        for (TransactionEntity transactionEntity:transactionEntities){
                            long id= transactionEntity.getParentTransactionId();
                            if(id>0) {
                                if (sparseArray.containsKey(id)) {
                                    Double amount= sparseArray.get(id);
                                    if(amount!=null)
                                        amount+=transactionEntity.getAmount();

                                    sparseArray.append(transactionEntity.getParentTransactionId(), amount);
                                }else
                                    sparseArray.append(transactionEntity.getParentTransactionId(), transactionEntity.getAmount());
                            }
                        }

                        if(transaction.getKidsAmount()>0) {
                            if(sparseArray.get(transaction.getId())!=null)
                                transaction.setAmount(transaction.getAmount() + sparseArray.get(transaction.getId()));
                        }
                         if(transaction.getParentTransactionId()==0)
                             if (showPlanned)
                                 list.add(transaction);
                             else if (transaction.getDate().before(Calendar.getInstance().getTime()))
                                     list.add(transaction);

                    }else{
                        if (showPlanned)
                            list.add(transaction);
                        else if (transaction.getDate().before(Calendar.getInstance().getTime()))
                            list.add(transaction);
                    }
                }

                viewAdapter.setTransactionList(list);
                setEmptyText(mBinding.emptyListText);
            }else {
                expenses=0;
                incomes=0;
                transfers=0;
                viewAdapter.setTransactionList(new ArrayList<>());
                setEmptyText(mBinding.emptyListText, getString(R.string.transaction_list_empty_text), Constants.TUTORIAL_TYPE_NEW_TRANSACTION);
            }

            setSummary();
        });

    }

    private void calculateSummary(Transaction transaction, CurrencyViewModel currencyViewModel, long defaultCurr){
        switch (transaction.getTransactionType()){
            case Constants.TRANSACTION_TYPE_EXPENSE:
                if (defaultCurr!= transaction.getCurrencyId()){
                    new AppExecutors().diskIO().execute(() -> {
                        CurrencyEntity productCurrency= currencyViewModel.getCurrencyRapid(transaction.getCurrencyId());
                        expenses+= transaction.getAmount()*productCurrency.getExchangeRate();
                        new AppExecutors().mainThread().execute(() -> mBinding.expenses.setText(MyApplication.money.format(expenses)));
                    });
                }else
                    expenses+= transaction.getAmount();
                break;
            case Constants.TRANSACTION_TYPE_INCOME:
                if (defaultCurr!= transaction.getCurrencyId()){
                    new AppExecutors().diskIO().execute(() -> {
                        CurrencyEntity productCurrency= currencyViewModel.getCurrencyRapid(transaction.getCurrencyId());
                        incomes+= transaction.getAmount()*productCurrency.getExchangeRate();
                        new AppExecutors().mainThread().execute(() -> mBinding.incomes.setText(MyApplication.money.format(incomes)));
                    });
                }else
                    incomes+= transaction.getAmount();
                break;
            case Constants.TRANSACTION_TYPE_TRANSFER:
                if (defaultCurr!= transaction.getCurrencyId()){
                    new AppExecutors().diskIO().execute(() -> {
                        CurrencyEntity productCurrency= currencyViewModel.getCurrencyRapid(transaction.getCurrencyId());
                        transfers+= transaction.getAmount()*productCurrency.getExchangeRate();
                        new AppExecutors().mainThread().execute(() -> mBinding.transfers.setText(MyApplication.money.format(transfers)));
                    });
                }else
                    transfers+= transaction.getAmount();
                break;
        }
    }

    private void setSummary(){
        DecimalFormat money= MyApplication.money;
        mBinding.expenses.setText(money.format(expenses));
        mBinding.incomes.setText(money.format(incomes));
        mBinding.transfers.setText(money.format(transfers));
    }


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TransactionListFragment() {
    }

    public static TransactionListFragment newInstance( ) {
        TransactionListFragment fragment = new TransactionListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @NonNull
    @Override
    public String toString() {
        return Constants.TAG_TRANSACTION_LIST_FRAGMENT;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.transaction_list,container, false);
        setLinearGridLayoutManager(mBinding.list, 2);
        setAdapters();
        setClicks();

        SimpleDateNavigationFragment dateNavigationFragment = (SimpleDateNavigationFragment)
                getChildFragmentManager().findFragmentById(R.id.static_date_nav_transaction_list);
        if(dateNavigationFragment!=null)
        dateNavigationFragment.setHostTransactionList(dateNavigationCallback);

        return mBinding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setFilter();
        setVisuals();
        reloadItems();
    }

    private void setFilter(){
        mBinding.getRoot().findViewById(R.id.clear_filter).setOnClickListener(v -> {
            clearFilter();
            reloadItems();
        });
    }

    private void setClicks(){
        mBinding.listWrapper.setOnRefreshListener(() -> {
            String token = preferences.getString(getString(R.string.key_dropbox_access_token), "");
            boolean fullSync= preferences.getBoolean(getString(R.string.key_full_dropbox_sync), false);

            if (token!=null && !token.equals("")) {
                if (fullSync)
                    compareTimestamps(token);
                else
                    Toast.makeText(getActivity(), getString(R.string.enable_full_sync), Toast.LENGTH_SHORT).show();

            } else {
                mBinding.listWrapper.setRefreshing(false);
                Toast.makeText(getActivity(), getString(R.string.not_logged_in), Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void compareTimestamps(String token) {
        Context context = getActivity();
        if (context!=null)
        new AppExecutors().diskIO().execute(() -> {
            long dbTimestamp= ((MyApplication)context.getApplicationContext()).getSettingsRepository().getSettings().getTimestamp();
            long dropboxTimestamp= BackupManagement.getDropboxTimestamp(token, context);

            if (dropboxTimestamp>dbTimestamp) {

                File file = BackupManagement.importDbDropbox(token, context);
                if (file !=null) {
                    BackupManagement.writeToDatabase(file, context);
                    if (this.isAdded())
                        notifyUser( getString(R.string.warning_db_import_success));
                } else {
                    if (this.isAdded())
                        notifyUser( getString(R.string.warning_db_import_fail));
                }

            }else if (dropboxTimestamp==0) {
                if (this.isAdded())
                    notifyUser( getString(R.string.warning_db_import_fail));
            } else {
                if (this.isAdded())
                    notifyUser( getString(R.string.synced));
            }
        });

    }

    private void notifyUser(String text){

        new AppExecutors().mainThread().execute(()->{
            if (isAdded() && getActivity()!=null){
                Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
               // loadingDialog.hideDialog();
                mBinding.listWrapper.setRefreshing(false);
            }
        });
    }


    private void clearFilter(){
        preferences.edit()
                .remove(getString(R.string.key_tr_list_product_id))
                .remove(getString(R.string.key_tr_list_bank_id))
                .remove(getString(R.string.key_tr_list_category_id_new))
                .remove(getString(R.string.key_tr_list_sub_category_id_new)).apply();
    }

    private void setVisuals(){

    }


    private SupportSQLiteQuery getQuery(){
        long fromDate= preferences.getLong(getString(R.string.key_tr_list_from_date_millis), 0);
        long toDate= preferences.getLong(getString(R.string.key_tr_list_to_date_millis), 0);

        //initiate
        StringBuilder builder= new StringBuilder();
        builder.append("select * from table_transactions where ");
        builder.append("id " + " != 0 ");

        //debt
        if(preferences.getBoolean(getResources().getString(R.string.key_tr_list_hide_debts),false))
            builder.append(" AND " + "category_id " + " !=1 ");

        long categoryId= preferences.getLong(getResources().getString(R.string.key_tr_list_category_id_new),0);
        long subCategoryId= preferences.getLong(getResources().getString(R.string.key_tr_list_sub_category_id_new),0);

        //category
        if(categoryId!=0)
            builder.append(" AND " + "category_id= ").append(categoryId);

        if(subCategoryId!=0)
            builder.append(" AND " + "sub_category_id= ").append(subCategoryId);

        //bank
        long bankId= preferences.getLong(getResources().getString(R.string.key_tr_list_bank_id),0);
        long productId= preferences.getLong(getResources().getString(R.string.key_tr_list_product_id),0);
        if(bankId!=0){
            builder.append(" AND ((" + "from_bank_id= ").append(bankId);

            if(productId!=0)
                builder.append(" AND from_product_id= ").append(productId);

            builder.append(")").append( "OR");

            builder.append(" ( to_bank_id= ").append(bankId);

            if(productId!=0)
                builder.append(" AND to_product_id= ").append(productId);

            builder.append("))");

        }

        //toggles
        if(!preferences.getBoolean(getString(R.string.key_tr_list_show_expenses), true))
            builder.append(" AND " + "transaction_type!= ").append(Constants.TRANSACTION_TYPE_EXPENSE);

        if(!preferences.getBoolean(getString(R.string.key_tr_list_show_incomes), true))
            builder.append(" AND " + "transaction_type!= ").append(Constants.TRANSACTION_TYPE_INCOME);

        if(!preferences.getBoolean(getString(R.string.key_tr_list_show_transfers), true))
            builder.append(" AND " + "transaction_type!= ").append(Constants.TRANSACTION_TYPE_TRANSFER);


        //dates
        if(fromDate!=0 && toDate!=0)
            builder.append(" AND date BETWEEN ").append(fromDate).append(" AND ").append(toDate);

        //order
        builder.append(" ORDER BY date DESC");

        mBinding.setIsFilterActive(isFilterActive());

        return new SimpleSQLiteQuery(builder.toString());
    }

    private boolean isFilterActive(){
        return
                preferences.getLong(getString(R.string.key_tr_list_category_id_new), 0) >0 ||
                preferences.getLong(getString(R.string.key_tr_list_sub_category_id_new), 0) >0 ||
                preferences.getLong(getString(R.string.key_tr_list_bank_id), 0) >0 ||
                preferences.getLong(getString(R.string.key_tr_list_product_id), 0) >0;
    }



    //listener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof TransactionClickCallback) {
            mListener = (TransactionClickCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement TransactionClickCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
