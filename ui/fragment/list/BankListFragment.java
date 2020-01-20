package com.unexceptional.beast.banko.newVersion.ui.fragment.list;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.unexceptional.beast.banko.databinding.BankItemBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.BankClickCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.BankEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.CurrencyEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.ProductEntity;
import com.unexceptional.beast.banko.newVersion.db.model.Bank;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.ui.activity.FloatingActivity;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.BankListViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CurrencyViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link BankClickCallback}
 * interface.
 */
public class BankListFragment extends SemiComplexListFragment {

    private BankViewAdapter activeViewAdapter, inactiveViewAdapter;
    private BankClickCallback mListener;
    private BankListViewModel viewModel;
    private CurrencyViewModel currencyViewModel;
    private double allMoney=0d;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BankListFragment() {
    }

    public static BankListFragment newInstance( ) {
        BankListFragment fragment = new BankListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void getBanksBalance(List<BankEntity> bankEntities, long defaultCurr, boolean active){
        for (BankEntity bank: bankEntities) {
            bank.setActive(active);
            double balance=0;
            for (ProductEntity product: viewModel.getBankProducts(bank.getId())){
                if (defaultCurr!= product.getCurrencyId()){
                    CurrencyEntity productCurrency= currencyViewModel.getCurrencyRapid(product.getCurrencyId());
                    balance+= product.getBalance()*productCurrency.getExchangeRate();

                }else
                    balance+= product.getBalance();
            }
            bank.setBalance(balance);
            allMoney+=balance;
        }
    }


    //from basic
    @Override
    void setViewModels() {
        viewModel = ViewModelProviders.of(this).get(BankListViewModel.class);
        currencyViewModel= ViewModelProviders.of(BankListFragment.this).get(CurrencyViewModel.class);
    }

    @Override
    protected void setAdapters() {
        activeViewAdapter = new BankViewAdapter(mListener);
        mBinding.activeList.setAdapter(activeViewAdapter);

        inactiveViewAdapter = new BankViewAdapter(mListener);
        mBinding.inactiveList.setAdapter(inactiveViewAdapter);
    }

    @Override
    public void reloadItems() {
        long defaultCurr= preferences.getLong(getString(R.string.key_default_currency_id_new),1);
        allMoney=0d;

        viewModel.getActiveBanksNoDebt().observe(this, bankEntities -> {
            if (bankEntities!=null){
                new AppExecutors().diskIO().execute(() -> {
                    getBanksBalance(bankEntities, defaultCurr, true);

                    new AppExecutors().mainThread().execute(() -> {
                        activeViewAdapter.setBankList(bankEntities);
                        mBinding.allMoney.setText(MyApplication.money.format(allMoney));
                    });
                });

            }else
                mBinding.setIsLoading(true);


            mBinding.executePendingBindings();
        });

        viewModel.getInactiveBanks().observe(this, bankEntities -> {
            if (bankEntities!=null){
                new AppExecutors().diskIO().execute(() -> {
                    getBanksBalance(bankEntities, defaultCurr, false);


                    new AppExecutors().mainThread().execute(() -> {
                        inactiveViewAdapter.setBankList(bankEntities);
                        mBinding.allMoney.setText(MyApplication.money.format(allMoney));
                    });

                });
                if(bankEntities.isEmpty())
                    mBinding.inactiveButton.setVisibility(View.GONE);
                else
                    mBinding.inactiveButton.setVisibility(View.VISIBLE);
            }else
                mBinding.setIsLoading(true);


            mBinding.executePendingBindings();
        });
    }

    //From semi complex
    @Override
    protected void setEmptyText(boolean value) {
        super.setEmptyText(value);
     mBinding.emptyListText.setText(R.string.bank_list_empty_text);
    }

    @Override
    protected String setInactiveButtonText() {
        return getString(R.string.inactive);
    }

    @Override
    protected void setNewItemButton() {
        mBinding.newItem.setOnClickListener(v -> newProduct());
        mBinding.newItem.setText(getString(R.string.new_account));
    }

    private void newProduct(){
        Intent intent= new Intent(getActivity(), FloatingActivity.class);
        intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_PRODUCT_FRAGMENT);

        startActivity(intent);
    }

    //from basic
    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_BANK_LIST_FRAGMENT;
    }

    //listener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BankClickCallback) {
            mListener = (BankClickCallback) context;
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


    /**
     * {@link RecyclerView.Adapter} that can display a {@link Bank} and makes a call to the
     * specified {@link BankClickCallback}.
     */
    public class BankViewAdapter extends RecyclerView.Adapter<BankViewAdapter.BankViewHolder> {

        private List<? extends Bank> mBankList;

        @Nullable
        private final BankClickCallback mBankClickCallback;

        BankViewAdapter(@Nullable BankClickCallback clickCallback) {
            mBankClickCallback = clickCallback;
            setHasStableIds(true);
        }

        void setBankList(final List<? extends Bank> bankList) {
            if (mBankList == null) {
                mBankList = bankList;
                notifyItemRangeInserted(0, bankList.size());
            } else {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return mBankList.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return bankList.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return mBankList.get(oldItemPosition).getId() ==
                                bankList.get(newItemPosition).getId();
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        Bank newBank = bankList.get(newItemPosition);
                        Bank oldBank = mBankList.get(oldItemPosition);
                        return newBank.getId() == oldBank.getId()
                                && Objects.equals(newBank.isActive(), oldBank.isActive()
                                && Objects.equals(newBank.getTitle(), oldBank.getTitle()));
                    }
                });
                mBankList = bankList;
                result.dispatchUpdatesTo(this);
            }
        }

        @NonNull
        @Override
        public BankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            BankItemBinding binding = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.getContext()), R.layout.bank_item,
                            parent, false);
            binding.setCallback(mBankClickCallback);
            return new BankViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull BankViewHolder holder, int position) {
            Bank bank= mBankList.get(position);
            holder.binding.setBank(bank);
            holder.binding.executePendingBindings();

            if(bank.getBalance()<0d)
                holder.binding.balance.setTextColor(Color.RED);
            else
                holder.binding.balance.setTextColor(Color.parseColor("#32CD32"));

            holder.binding.balance.setText(String.valueOf(MyApplication.money.format(bank.getBalance())));

            int id= MyApplication.getBankIconId(mBankList.get(position).getId());
            if(id!=0)
                Picasso.get().load(id).into(holder.binding.bankItemIcon);
        }

        @Override
        public int getItemCount() {
            return mBankList == null ? 0 : mBankList.size();
        }

        @Override
        public long getItemId(int position) {
            return mBankList.get(position).getId();
        }

        class BankViewHolder extends RecyclerView.ViewHolder {

            final BankItemBinding binding;

            BankViewHolder(BankItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
