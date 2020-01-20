package com.unexceptional.beast.banko.newVersion.ui.fragment.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.BudgetItemBinding;
import com.unexceptional.beast.banko.databinding.BudgetListBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.BudgetClickCallback;
import com.unexceptional.beast.banko.newVersion.callback.DateNavigationCallback;
import com.unexceptional.beast.banko.newVersion.db.converter.DateTypeConverter;
import com.unexceptional.beast.banko.newVersion.db.entity.BudgetEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.TransactionEntity;
import com.unexceptional.beast.banko.newVersion.db.model.Budget;
import com.unexceptional.beast.banko.newVersion.ui.activity.FloatingActivity;
import com.unexceptional.beast.banko.newVersion.ui.fragment.dateNavigation.BudgetDateNavigationFragment;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.BudgetListViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.TransactionListViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BudgetListFragment extends BasicListFragment {

    private long fromDate, toDate;
    private BudgetListBinding mBinding;
    private BudgetViewAdapter viewAdapter;
    private BudgetClickCallback mListener;
    private TransactionListViewModel transactionListViewModel;
    private BudgetListViewModel budgetListViewModel;

    private DateNavigationCallback dateNavigationCallback= this::reloadItems;


    //new ERA!
    @Override
    void setViewModels() {
        transactionListViewModel= ViewModelProviders.of(this).get(TransactionListViewModel.class);
        budgetListViewModel= ViewModelProviders.of(this).get(BudgetListViewModel.class);
    }

    @Override
    void setAdapters(){
        viewAdapter= new BudgetViewAdapter(mListener);
        mBinding.list.setAdapter(viewAdapter);
    }

    @Override
    public void reloadItems(){
        getFilters();

        budgetListViewModel.getAllBudgets().observe(this, budgetEntities ->    {
            if(budgetEntities!=null && !budgetEntities.isEmpty()){
                List<BudgetEntity> budgets= new ArrayList<>();
                for (BudgetEntity budget: budgetEntities)
                    if (budget.getDateOption()==preferences.getInt(getString(R.string.key_budget_list_date_option),0))
                        budgets.add(budget);

                viewAdapter.setBudgetList(budgets);

                setEmptyText(mBinding.emptyListText);
            }else {
                viewAdapter.setBudgetList(new ArrayList<>());
                setEmptyText(mBinding.emptyListText, getString(R.string.budget_list_empty_text), Constants.TUTORIAL_TYPE_BUDGETS);
            }
        });

    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BudgetListFragment() {
    }

    public static BudgetListFragment newInstance( ) {
        BudgetListFragment fragment = new BudgetListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.budget_list,container, false);
        setLinearGridLayoutManager(mBinding.list, 2);
        setAdapters();

        BudgetDateNavigationFragment dateNavigationFragment = (BudgetDateNavigationFragment)
                getChildFragmentManager().findFragmentById(R.id.static_date_nav_budget_list);
        if(dateNavigationFragment!=null)
        dateNavigationFragment.setHostBudgetList(dateNavigationCallback);

        return mBinding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        setClicks();
        setVisuals();
        reloadItems();
    }

    private void setClicks(){
        mBinding.newItem.setOnClickListener(v ->{
            Intent intent= new Intent(getActivity(), FloatingActivity.class);
            intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_BUDGET_FRAGMENT);
            startActivity(intent);
        });
    }

    private void setVisuals(){

    }



    private void getFilters(){
        fromDate= preferences.getLong(getString(R.string.key_budget_list_from_date_millis), 0);
        toDate= preferences.getLong(getString(R.string.key_budget_list_to_date_millis), 0);
    }



    //listener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BudgetClickCallback) {
            mListener = (BudgetClickCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement BankClickCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @NonNull
    @Override
    public String toString() {
        return Constants.TAG_BUDGET_LIST_FRAGMENT;
    }

    public class BudgetViewAdapter extends RecyclerView.Adapter<BudgetViewAdapter.BudgetViewHolder> {

        private List<? extends Budget> mBudgetList;

        @Nullable
        private final BudgetClickCallback mBudgetClickCallback;

        BudgetViewAdapter(@Nullable BudgetClickCallback clickCallback) {
            mBudgetClickCallback = clickCallback;
            setHasStableIds(true);
        }

        void setBudgetList(final List<? extends Budget> budgetList) {
            if (mBudgetList == null) {
                mBudgetList = budgetList;
                notifyItemRangeInserted(0, budgetList.size());
            } else {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return mBudgetList.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return budgetList.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return mBudgetList.get(oldItemPosition).getId() ==
                                budgetList.get(newItemPosition).getId();
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        Budget newBudget = budgetList.get(newItemPosition);
                        Budget oldBudget = mBudgetList.get(oldItemPosition);
                        return false;
//                                newBudget.getId() == oldBudget.getId()
//                                        && Objects.equals(newBudget.isInActive(), oldBudget.isInActive()
//                                        && Objects.equals(newBudget.getTitle(), oldBudget.getTitle()));


                    }
                });
                mBudgetList = budgetList;
                result.dispatchUpdatesTo(this);
            }
        }

        @NonNull
        @Override
        public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            BudgetItemBinding binding = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.getContext()), R.layout.budget_item,
                            parent, false);
            binding.setCallback(mBudgetClickCallback);
            return new BudgetViewHolder(binding);
        }


        @Override
        public int getItemCount() {
            return mBudgetList == null ? 0 : mBudgetList.size();
        }

        @Override
        public long getItemId(int position) {
            return mBudgetList.get(position).getId();
        }

        class BudgetViewHolder extends RecyclerView.ViewHolder {

            final BudgetItemBinding binding;

            BudgetViewHolder(BudgetItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }


        @Override
        public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position)    {
            Budget budget= mBudgetList.get(position);
            holder.binding.setBudget(budget);
            holder.binding.executePendingBindings();

            DecimalFormat money= MyApplication.money;

            if(fromDate==0 || toDate==0) {

                fromDate= DateTypeConverter.toLong(budget.getStartDate());
                toDate= DateTypeConverter.toLong(budget.getEndDate());
            }

            transactionListViewModel.getBudgetActualAmount(DateTypeConverter.toDate(fromDate), DateTypeConverter.toDate(toDate), budget.getCategoryIds())
                    .observe(BudgetListFragment.this, transactionEntities -> {
                        if(transactionEntities!=null){
                            double actualAmount= 0d;
                            for (TransactionEntity transaction: transactionEntities)
                                actualAmount+= transaction.getAmount();

                            setActualAmount(money, actualAmount, holder.binding, budget);
                        }
                    });

            holder.binding.title.setText(budget.getTitle());

            int id= MyApplication.getDrawableId(budget.getIconName());
            if(id!=0)
                Picasso.get().load(id).into(holder.binding.icon);

            holder.binding.budgetMaxAmount.setText( money.format( budget.getAmount()));
        }

        private void setActualAmount(DecimalFormat money, double actualAmount, BudgetItemBinding binding, Budget budget){
            binding.budgetActualAmount.setText(String.format("%s: %s", getString(R.string.budget_spent), money.format(actualAmount)));

            setDates(binding, budget);

            double left= budget.getAmount()- actualAmount;
            binding.budgetLeft.setText(String.format("%s: %s", getString(R.string.budget_left), money.format(left)));

            setBudget4day(binding, budget, money, left);
            setPercentage(actualAmount, budget, binding);

        }

        private void setDates(BudgetItemBinding binding, Budget budget){
            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

            if(budget.getStartDate()!=null)
                binding.startDate.setText(dateFormatGmt.format(budget.getStartDate()));
            else
                binding.startDate.setText("");

            if(budget.getEndDate()!=null)
                binding.endDate.setText(dateFormatGmt.format(budget.getEndDate()));
            else
                binding.endDate.setText("");
        }

        private void setPercentage(double actualAmount, Budget budget, BudgetItemBinding binding){
            int percentage= (int) ((actualAmount/ budget.getAmount())*100);
            binding.budgetPercentage.setText(String.format(Locale.getDefault(), "%d %%", percentage));

            binding.budgetProgress.setProgress(percentage);
            if(percentage<30)
                binding.budgetProgress.setProgressDrawable(getResources().getDrawable(R.drawable.progress_drawable_green));
            else if(percentage <60)
                binding.budgetProgress.setProgressDrawable(getResources().getDrawable(R.drawable.progress_drawable_yellow));
            else if(percentage <99)
                binding.budgetProgress.setProgressDrawable(getResources().getDrawable(R.drawable.progress_drawable_orange));
            else
                binding.budgetProgress.setProgressDrawable(getResources().getDrawable(R.drawable.progress_drawable_red));
        }

        private void setBudget4day(BudgetItemBinding binding, Budget budget, DecimalFormat money, double left){
            Calendar calendar= Calendar.getInstance();
            if (calendar.getTimeInMillis() > fromDate && calendar.getTimeInMillis() < toDate)
                if (budget.getDateOption()==3 ){
                    binding.budgetLeft4Days.setVisibility(View.VISIBLE);
                    double left4days = left/(calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH) +1);
                    binding.budgetLeft4Days.setText(String.format("%s%s", money.format(left4days), getString(R.string.per_day)));

                }else if (budget.getDateOption()==2){
                    binding.budgetLeft4Days.setVisibility(View.VISIBLE);
                    double left4days = left/(calendar.getActualMaximum(Calendar.DAY_OF_WEEK) - calendar.get(Calendar.DAY_OF_WEEK) + 2);
                    binding.budgetLeft4Days.setText(String.format("%s%s", money.format(left4days), getString(R.string.per_day)));

                }else
                    binding.budgetLeft4Days.setVisibility(View.GONE);
            else
                binding.budgetLeft4Days.setVisibility(View.GONE);
        }

    }

}
