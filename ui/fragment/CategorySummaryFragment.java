package com.unexceptional.beast.banko.newVersion.ui.fragment;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.CategorySummaryFragmentBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.other.CategoryObject;
import com.unexceptional.beast.banko.newVersion.ui.activity.MainActivity;
import com.unexceptional.beast.banko.newVersion.ui.dialog.SubcategoriesDialog;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CategoryViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class CategorySummaryFragment extends AbstractPieChart {


    private CategorySummaryFragmentBinding mBinding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.category_summary_fragment, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pieChart=mBinding.categoryPieChart;
        setChart();
        setLegendRecycler(mBinding.legendList);
        mBinding.legendList.setNestedScrollingEnabled(false);
    }

    public void setTransactionType(int transactionType) {
        this.transactionType = transactionType;
        if (transactionType== Constants.TRANSACTION_TYPE_EXPENSE)
            mBinding.title.setText(R.string.expenses);
        else
            mBinding.title.setText(R.string.incomes);
    }

    private void setValueSelectedListener(List<PieChartLegend> legendList){
        if(legendList!=null && !legendList.isEmpty())
            onChartValueSelectedListener= new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    PieChartLegend legend= legendList.get((int)h.getX());

                    mBinding.sum.setText(MyApplication.money.format(legend.getAmount()));
                    mBinding.desc.setText(legend.getCategoryName());

                    int drawableId= MyApplication.getDrawableId(legend.getCategoryIconName());
                    if(drawableId!=0)
                        Picasso.get().load(drawableId).into(mBinding.categorySummaryIcon);

                    mBinding.showTransactions.setOnClickListener(v -> {
                        preferences.edit().putLong(getString(R.string.key_tr_list_category_id_new), legend.getCategoryId()).apply();
                        goToTransactionList();
                    });

                    new AppExecutors().diskIO().execute(() -> {
                        if(ViewModelProviders.of(CategorySummaryFragment.this).get(CategoryViewModel.class).hasKids(legend.getCategoryId())) {
                            new AppExecutors().mainThread().execute(() -> {
                                mBinding.showSubcategories.setOnClickListener(v ->
                                        new SubcategoriesDialog((MainActivity) getActivity(), legend.getCategoryId(), fromDate, toDate, transactionType).showDialog());
                                mBinding.showSubcategories.setVisibility(View.VISIBLE);
                            });
                        }else {
                            new AppExecutors().mainThread().execute(() ->
                                    mBinding.showSubcategories.setVisibility(View.INVISIBLE));
                        }

                    });
                }

                @Override
                public void onNothingSelected() {
                    mBinding.sum.setText(MyApplication.money.format(allMoney));
                    mBinding.desc.setText(getString(R.string.sum));
                    mBinding.categorySummaryIcon.setImageDrawable(null);
                    mBinding.showSubcategories.setVisibility(View.INVISIBLE);

                    mBinding.showTransactions.setOnClickListener(v ->
                            goToTransactionList());
                }
            };
        else
            onChartValueSelectedListener= new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {

                }

                @Override
                public void onNothingSelected() {
                    mBinding.sum.setText(MyApplication.money.format(0));
                    mBinding.desc.setText(getString(R.string.sum));
                    mBinding.categorySummaryIcon.setImageDrawable(null);
                    mBinding.showSubcategories.setVisibility(View.INVISIBLE);
                }
            };
    }

    private void goToTransactionList(){
        preferences.edit().putInt(getString(R.string.key_tr_list_date_option),
                preferences.getInt(getString(R.string.key_charts_date_option), 0)).apply();
        preferences.edit().putLong(getString(R.string.key_tr_list_from_date_millis), fromDate).apply();
        preferences.edit().putLong(getString(R.string.key_tr_list_to_date_millis), toDate).apply();
        ((MainActivity) Objects.requireNonNull(getActivity())).selectNavigation(2);
    }

    void settingChart(HashMap<Long, CategoryObject> categoriesMap, double expenses){
        allMoney=expenses;

        List<PieEntry> entries = new ArrayList<>();
        List<PieChartLegend> legendList = new ArrayList<>();

        for(Map.Entry<Long, CategoryObject> map  :  categoriesMap.entrySet() ) {
            CategoryObject categoryObject= map.getValue();
            if (map.getValue() != null) {
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

        //color the legend list
        List<Integer> colors = setColors(getActivity());
        int legendItemId=0;
        for(PieChartLegend legend: legendList){
            legend.setColor(colors.get(legendItemId));
            legendItemId++;
        }

        adapter.setLegendList(legendList);

        pieChart.setData(new PieData(setPieDataSet(entries, colors)));

        setValueSelectedListener(legendList);
        pieChart.setOnChartValueSelectedListener(onChartValueSelectedListener);
        onChartValueSelectedListener.onNothingSelected();

        //legend callback
        legendClickCallback= position -> {
            onChartValueSelectedListener.onValueSelected(entries.get(position),
                    new Highlight(position,position,position));
            pieChart.highlightValue(null);
        };


        pieChart.animateXY(300, 300);//does refresh too
    }

    public void setDates(long fromDate, long toDate){
        this.fromDate= fromDate;
        this.toDate= toDate;
    }
}
