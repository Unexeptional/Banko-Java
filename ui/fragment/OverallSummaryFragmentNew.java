package com.unexceptional.beast.banko.newVersion.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.OverallSummaryFragmentBinding;
import com.unexceptional.beast.banko.newVersion.ui.activity.MainActivity;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class OverallSummaryFragmentNew extends Fragment {

    private OverallSummaryFragmentBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.overall_summary_fragment, container, false);


        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.pieChart.setDrawHoleEnabled(false);
        mBinding.pieChart.getLegend().setEnabled(false);
        mBinding.pieChart.setRotationEnabled(false);
        mBinding.pieChart.getDescription().setEnabled(false);

        mBinding.showTransactions.setOnClickListener(v -> {

            ((MainActivity) Objects.requireNonNull(getActivity())).selectNavigation(2);
        });
    }

    //this can be shorter but easy
    public void reload(float expValue, float incValue){
        final List<PieEntry> entries = new ArrayList<>();

        entries.add(new PieEntry(incValue, ""));
        entries.add(new PieEntry(expValue, ""));

        if(incValue==0 && expValue==0){
            entries.clear();
            entries.add(new PieEntry(144, ""));

        }

        PieDataSet set = new PieDataSet(entries, "");
        PieData data = new PieData(set);
        set.setColors(Color.parseColor("#32CD32"), Color.RED);
        set.setDrawValues(false);
        set.setSliceSpace(3);

        mBinding.pieChart.setData(data);

        mBinding.pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                mBinding.showTransactions.setVisibility(View.VISIBLE);
                if(e==entries.get(0)){
                    mBinding.income.setTextSize(16);
                    mBinding.expense.setTextSize(14);
                    mBinding.incomeTxt.setTextSize(16);
                    mBinding.expenseTxt.setTextSize(14);
                } else {
                    mBinding.income.setTextSize(14);
                    mBinding.expense.setTextSize(16);
                    mBinding.incomeTxt.setTextSize(14);
                    mBinding.expenseTxt.setTextSize(16);
                }
            }

            @Override
            public void onNothingSelected() {
                mBinding.income.setTextSize(14);
                mBinding.expense.setTextSize(14);
                mBinding.incomeTxt.setTextSize(14);
                mBinding.expenseTxt.setTextSize(14);
                mBinding.showTransactions.setVisibility(View.GONE);
            }
        });


        mBinding.income.setText(MyApplication.money.format(incValue));
        mBinding.income.setTextColor(Color.parseColor("#32CD32"));

        mBinding.expense.setText(MyApplication.money.format(expValue));
        mBinding.expense.setTextColor(Color.RED);

        mBinding.equal.setText(MyApplication.money.format(incValue - expValue));
        if(expValue>incValue){
            mBinding.equal.setTextColor(Color.RED);
        } else  mBinding.equal.setTextColor(Color.parseColor("#32CD32"));

        mBinding.pieChart.animateXY(300, 300);//does refresh too
    }
}
