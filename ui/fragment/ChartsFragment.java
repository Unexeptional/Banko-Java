package com.unexceptional.beast.banko.newVersion.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.FragmentChartsBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.DateNavigationCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.CurrencyEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.TransactionEntity;
import com.unexceptional.beast.banko.newVersion.other.CategoryObject;
import com.unexceptional.beast.banko.newVersion.ui.fragment.dateNavigation.SimpleDateNavigationFragment;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CurrencyViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.TransactionListViewModel;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChartsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChartsFragment extends BasicFragment {

    private DateNavigationCallback dateNavigationCallback= this::reloadItems;
    private long fromDate, toDate;
    private float expenses=0;
    private float incomes=0;
    private OverallSummaryFragmentNew summaryChart;
    private CategorySummaryFragment expensesChart;
    private CategorySummaryFragment incomesChart;

    public ChartsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static ChartsFragment newInstance() {
        ChartsFragment fragment = new ChartsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentChartsBinding mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_charts, container, false);

        SimpleDateNavigationFragment dateNavigationFragment = (SimpleDateNavigationFragment)
                getChildFragmentManager().findFragmentById(R.id.static_date_nav_charts);
        if(dateNavigationFragment!=null)
            dateNavigationFragment.setHostCharts(dateNavigationCallback);

        summaryChart = (OverallSummaryFragmentNew)
                getChildFragmentManager().findFragmentById(R.id.overall_summary_fragment);

        expensesChart = (CategorySummaryFragment)
                getChildFragmentManager().findFragmentById(R.id.category_summary_fragment);

        incomesChart = (CategorySummaryFragment)
                getChildFragmentManager().findFragmentById(R.id.category_summary_fragment_incomes);



        reloadItems();
        return mBinding.getRoot();
    }

    public void reloadItems(){
        getFilters();
        expenses=0;
        incomes=0;

        getCurrencies();
    }

    private void getCurrencies(){
        ViewModelProviders.of(this).get(CurrencyViewModel.class).getAllCurrencies().
                observe(this, currencyEntities -> {

            if(currencyEntities!=null){
                LongSparseArray<Double> currencyArray= new   LongSparseArray<>();
                for (CurrencyEntity currency: currencyEntities)
                    currencyArray.append(currency.getId(), currency.getExchangeRate());

                getTransactions(currencyArray);
            }
        });
    }

    private void getTransactions(LongSparseArray<Double> currencyArray){

        @SuppressLint("UseSparseArrays") HashMap<Long,CategoryObject> expensesMap= new HashMap<>();
        @SuppressLint("UseSparseArrays") HashMap<Long,CategoryObject> incomesMap= new HashMap<>();

        ViewModelProviders.of(this).get(TransactionListViewModel.class).
                getTransactions(getQuery()).observe(this, transactionEntities -> {
            if(transactionEntities!=null){

                for (TransactionEntity transaction: transactionEntities) {
                    double exchangeRate;
                    try {
                        exchangeRate = currencyArray.get(transaction.getCurrencyId());
                    }catch (Exception e){
                        exchangeRate=1;
                    }


                    if(transaction.getTransactionType()== Constants.TRANSACTION_TYPE_EXPENSE){
                        expenses+= transaction.getAmount()*exchangeRate;
                        setMap(transaction, expensesMap, exchangeRate);

                    } else if (transaction.getTransactionType()== Constants.TRANSACTION_TYPE_INCOME){
                        incomes+=transaction.getAmount()*exchangeRate;
                        setMap(transaction, incomesMap, exchangeRate);
                    }
                }

                if(summaryChart!=null)
                    summaryChart.reload(expenses, incomes);

                if(expensesChart !=null) {
                    expensesChart.setDates(fromDate, toDate);
                    expensesChart.setTransactionType(Constants.TRANSACTION_TYPE_EXPENSE);
                    expensesChart.settingChart(expensesMap, expenses);
                }

                if(incomesChart !=null) {
                    incomesChart.setDates(fromDate, toDate);
                    incomesChart.setTransactionType(Constants.TRANSACTION_TYPE_INCOME);
                    incomesChart.settingChart(incomesMap, incomes);
                }

            }
        });

    }

    private void setMap(TransactionEntity transaction, HashMap<Long,CategoryObject> map, double exchangeRate){
        long id= transaction.getCategoryId();

        if (map.containsKey(id)) {
            CategoryObject categoryObject= map.get(id);
            if (categoryObject!=null)
                categoryObject.addAmount(transaction.getAmount()*exchangeRate);

            map.put(id, categoryObject);
        } else {
            map.put(id, new CategoryObject(transaction.getCategoryName(), transaction.getIconName(), transaction.getAmount()*exchangeRate));
        }
    }

    private SupportSQLiteQuery getQuery(){
        StringBuilder builder= new StringBuilder();
        builder.append("select * from table_transactions where ");

        //just to make sure query starts well
        builder.append("id " + " != 0 ");

        if(preferences.getBoolean(getResources().getString(R.string.key_hide_debt_in_chart),true))
            builder.append(" AND " + "category_id " + " !=1 ");


        if(fromDate!=0 && toDate!= 0)
        builder.append("AND " + "date" + " BETWEEN ").append(fromDate).append(" AND ").append(toDate);

        return new SimpleSQLiteQuery(builder.toString());
    }

    private void getFilters(){
        fromDate= preferences.getLong(getString(R.string.key_charts_from_date_millis), 0);
        toDate= preferences.getLong(getString(R.string.key_charts_to_date_millis), 0);
    }

    @NonNull
    @Override
    public String toString() {
        return Constants.TAG_CHARTS_FRAGMENT;
    }


}
