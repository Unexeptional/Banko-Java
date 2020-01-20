package com.unexceptional.beast.banko.newVersion.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.ChartLegendItemBinding;
import com.unexceptional.beast.banko.newVersion.db.model.Notification;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public abstract class AbstractPieChart extends Fragment {

    protected long fromDate, toDate;
    protected OnChartValueSelectedListener onChartValueSelectedListener;
    protected LegendClickCallback legendClickCallback;
    protected PieChart pieChart;
    protected int transactionType;
    protected SharedPreferences preferences;
    protected double allMoney;
    protected PieChartLegendAdapter adapter;

    protected List<Integer> setColors(Context context){
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(context.getResources().getColor(R.color.account_red));
        colors.add(context.getResources().getColor(R.color.account_blue));
        colors.add(context.getResources().getColor(R.color.account_green));
        colors.add(context.getResources().getColor(R.color.account_gold));
        colors.add(context.getResources().getColor(R.color.account_purple));
        colors.add(context.getResources().getColor(R.color.maroon));
        colors.add(context.getResources().getColor(R.color.abs__holo_blue_light));
        colors.add(context.getResources().getColor(R.color.black));
        colors.add(context.getResources().getColor(R.color.darker_blue));
        colors.add(context.getResources().getColor(R.color.colorAccent));
        colors.add(context.getResources().getColor(R.color.theme_primary));
        colors.add(context.getResources().getColor(R.color.light_gray));
        colors.add(context.getResources().getColor(R.color.theme_accent));
        colors.add(context.getResources().getColor(R.color.dark_blue));
        colors.add(context.getResources().getColor(R.color.colorFAB2));
        colors.add(context.getResources().getColor(R.color.silver));
        colors.add(context.getResources().getColor(R.color.colorFAB1));
        colors.add(context.getResources().getColor(R.color.colorAccent));
        //duplicated11 to make sure its enough and doesnt crash when everything like a lot of categoryIds
        colors.add(context.getResources().getColor(R.color.account_red));
        colors.add(context.getResources().getColor(R.color.account_blue));
        colors.add(context.getResources().getColor(R.color.account_green));
        colors.add(context.getResources().getColor(R.color.account_gold));
        colors.add(context.getResources().getColor(R.color.account_purple));
        colors.add(context.getResources().getColor(R.color.maroon));
        colors.add(context.getResources().getColor(R.color.abs__holo_blue_light));
        colors.add(context.getResources().getColor(R.color.black));
        colors.add(context.getResources().getColor(R.color.darker_blue));
        colors.add(context.getResources().getColor(R.color.colorAccent));
        colors.add(context.getResources().getColor(R.color.theme_primary));
        colors.add(context.getResources().getColor(R.color.light_gray));
        colors.add(context.getResources().getColor(R.color.theme_accent));
        colors.add(context.getResources().getColor(R.color.dark_blue));
        colors.add(context.getResources().getColor(R.color.colorFAB2));
        colors.add(context.getResources().getColor(R.color.silver));
        colors.add(context.getResources().getColor(R.color.colorFAB1));
        colors.add(context.getResources().getColor(R.color.colorAccent));
        return colors;
    }

    protected void setChart(){
        pieChart.setDrawHoleEnabled(true);
        pieChart.getLegend().setEnabled(false);
        pieChart.setRotationEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setEntryLabelColor(Color.BLACK);
    }

    protected void setLegendRecycler(RecyclerView recycler){
        adapter= new PieChartLegendAdapter();
        recycler.setAdapter(adapter);
    }

    protected PieDataSet setPieDataSet(List<PieEntry> entries, List<Integer> colors){
        PieDataSet pieDataSet = new PieDataSet(entries, "");
        pieDataSet.setColors(colors);
        pieDataSet.setDrawValues(false);
        pieDataSet.setSliceSpace(5);
        pieDataSet.setValueTextSize(12);
        pieDataSet.setValueLinePart1Length(0.3f);
        return pieDataSet;
    }

    protected class PieChartLegend  {
        private long categoryId;
        private String categoryName;
        private String categoryIconName;
        private double amount;
        private int color;

        public PieChartLegend(long categoryId, String categoryName, String categoryIconName, double amount) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.categoryIconName = categoryIconName;
            this.amount = amount;
        }

        public long getCategoryId() {
            return categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public double getAmount() {
            return amount;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public String getCategoryIconName() {
            return categoryIconName;
        }
    }

    public class PieChartLegendAdapter extends RecyclerView.Adapter<PieChartLegendAdapter.PieChartViewHolder> {

        private List<PieChartLegend> mLegendList;

        PieChartLegendAdapter() {
        }

        public void setLegendList(List<PieChartLegend> legendList) {
            if (this.mLegendList == null) {
                this.mLegendList = legendList;
                notifyItemRangeInserted(0, legendList.size());
            } else {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return mLegendList.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return legendList.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return mLegendList.get(oldItemPosition).getCategoryId() ==
                                legendList.get(newItemPosition).getCategoryId();
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        PieChartLegend newLegend = legendList.get(newItemPosition);
                        PieChartLegend oldLegend = mLegendList.get(oldItemPosition);
                        return newLegend.getCategoryId() == oldLegend.getCategoryId()
                                && newLegend.getAmount()== oldLegend.getAmount()
                                && newLegend.getColor()== oldLegend.getColor();
                    }
                });
                mLegendList = legendList;
                result.dispatchUpdatesTo(this);
            }
        }

        @NonNull
        @Override
        public PieChartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ChartLegendItemBinding binding = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.getContext()), R.layout.chart_legend_item,
                            parent, false);
            return new PieChartViewHolder(binding);
        }

       @Override
        public int getItemCount() {
           return mLegendList == null ? 0 : mLegendList.size();
        }

        class PieChartViewHolder extends RecyclerView.ViewHolder {

            final ChartLegendItemBinding binding;

            PieChartViewHolder(ChartLegendItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final PieChartViewHolder holder, int position) {
            final PieChartLegend pieChartLegend = mLegendList.get(holder.getAdapterPosition());

            holder.binding.legendBalance.setText(MyApplication.money.format(pieChartLegend.getAmount()));
            holder.binding.btnCategory.setText(pieChartLegend.getCategoryName());
            holder.binding.legendColor.setBackgroundColor(pieChartLegend.getColor());
            holder.binding.card.setOnClickListener(v -> legendClickCallback.legendClick(position));
        }

    }


    protected interface LegendClickCallback{
        void legendClick(int position);
    }
}
