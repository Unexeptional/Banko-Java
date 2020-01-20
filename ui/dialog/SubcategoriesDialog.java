package com.unexceptional.beast.banko.newVersion.ui.dialog;

import android.annotation.SuppressLint;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LongSparseArray;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.SubCategoriesDialogBinding;
import com.unexceptional.beast.banko.newVersion.db.entity.CurrencyEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.TransactionEntity;
import com.unexceptional.beast.banko.newVersion.other.CategoryObject;
import com.unexceptional.beast.banko.newVersion.ui.fragment.AbstractPieChart;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CurrencyViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.TransactionListViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubcategoriesDialog extends AbstractPieChart {

    private AppCompatActivity activity;
    private long parentId;
    private SubCategoriesDialogBinding mBinding;

    public SubcategoriesDialog(AppCompatActivity activity, long parentId, long fromDate, long toDate, int transactionType) {
        this.activity = activity;
        this.parentId =parentId;
        this.preferences= PreferenceManager.getDefaultSharedPreferences(activity);
        this.fromDate= fromDate;
        this.toDate=toDate;
        this.transactionType=transactionType;
    }

    public void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        mBinding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout. sub_categories_dialog,
                null, false);
        builder.setView(mBinding.getRoot());

        pieChart= mBinding.categoryPieChart;
        setChart();
        setLegendRecycler(mBinding.legendList);

        getCurrencies();

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getCurrencies(){
        ViewModelProviders.of(activity).get(CurrencyViewModel.class).getAllCurrencies().
                observe(activity, currencyEntities -> {

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
        allMoney =0;

        ViewModelProviders.of(activity).get(TransactionListViewModel.class).
                getTransactions(getQuery()).observe(activity, transactionEntities -> {
            if(transactionEntities!=null){
                for (TransactionEntity transaction: transactionEntities) {

                    double exchangeRate= currencyArray.get(transaction.getCurrencyId());

                    allMoney += transaction.getAmount()*exchangeRate;
                    setMap(transaction, expensesMap, exchangeRate);

                }

                settingChart(expensesMap);
            }
        });

    }

    private void setMap(TransactionEntity transaction, HashMap<Long, CategoryObject> map, double exchangeRate){
        long id= transaction.getSubCategoryId();
        String name;
        if (id==0)
            name=transaction.getCategoryName();
        else
           name=transaction.getSubCategoryName();

        if (map.containsKey(id)) {
            CategoryObject categoryObject= map.get(id);
            if (categoryObject!=null)
                categoryObject.addAmount(transaction.getAmount()*exchangeRate);

            map.put(id, categoryObject);
        } else {
            map.put(id, new CategoryObject(name, transaction.getIconName(), transaction.getAmount()*exchangeRate));
        }
    }

    private void settingChart(HashMap<Long, CategoryObject> categoriesMap){
        List<PieEntry> entries = new ArrayList<>();
        List<PieChartLegend> legendList = new ArrayList<>();

        for(Map.Entry<Long, CategoryObject> map  :  categoriesMap.entrySet() ) {
            CategoryObject categoryObject= map.getValue();
            if (categoryObject != null) {
                entries.add(new PieEntry((float) categoryObject.getAmount(), categoryObject.getTitle()));
               legendList.add(new PieChartLegend(map.getKey(), categoryObject.getTitle(),categoryObject.getIconName(), categoryObject.getAmount() ));
            }
        }


        if(entries.isEmpty()){
            entries.add(new PieEntry( 144, "default category"));
            entries.add(new PieEntry( 144, "default category"));
            entries.add(new PieEntry( 144, "default category"));
        }


        Collections.sort(entries, (obj1, obj2) -> Float.compare(obj2.getValue(), obj1.getValue()));
        Collections.sort(legendList, (obj1, obj2) -> Double.compare(obj2.getAmount(), obj1.getAmount()));

        //colors
        List<Integer> colors = setColors(activity);
        int legendItemId=0;
        for(PieChartLegend legend: legendList){
            legend.setColor(colors.get(legendItemId));
            legendItemId++;
        }

        adapter.setLegendList(legendList);

        pieChart.setData(new PieData(setPieDataSet(entries, colors)));

        //chart listener
        setValueSelectedListener(legendList);
        pieChart.setOnChartValueSelectedListener(onChartValueSelectedListener);
        onChartValueSelectedListener.onNothingSelected();

        //legend callback
        legendClickCallback= position -> {
            onChartValueSelectedListener.onValueSelected(entries.get(position),
                    new Highlight(position,position,position));
            pieChart.highlightValue(null);
        };

        //refresh
        pieChart.animateXY(300, 300);//does refresh too
    }

    private void setValueSelectedListener(List<PieChartLegend> legendList){
        onChartValueSelectedListener= new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieChartLegend legend= legendList.get((int)h.getX());

                mBinding.sum.setText(MyApplication.money.format(legend.getAmount()));
                mBinding.desc.setText(legend.getCategoryName());

                int drawableId= MyApplication.getDrawableId(legend.getCategoryIconName());
                if(drawableId!=0)
                    Picasso.get().load(drawableId).into(mBinding.categorySummaryIcon);
            }

            @Override
            public void onNothingSelected() {
                mBinding.sum.setText(MyApplication.money.format(allMoney));
                mBinding.desc.setText(activity.getString(R.string.sum));
                mBinding.categorySummaryIcon.setImageDrawable(null);
            }
        };
    }

    private SupportSQLiteQuery getQuery(){
        StringBuilder builder= new StringBuilder();
        builder.append("select * from table_transactions where ");

        //just to make sure query starts well
        builder.append("transaction_type = ").append(transactionType).append(" ");

        if(preferences.getBoolean(activity.getString(R.string.key_hide_debt_in_chart),true))
            builder.append(" AND " + "category_id " + "!= ").append(1).append(" ");

        builder.append(" AND " + "category_id " + "= ").append(parentId).append(" ");


        if(fromDate!=0 && toDate!= 0)
            builder.append("AND " + "date" + " BETWEEN ").append(fromDate).append(" AND ").append(toDate);

        return new SimpleSQLiteQuery(builder.toString());
    }
}