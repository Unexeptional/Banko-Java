package com.unexceptional.beast.banko.newVersion.ui.fragment.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.BankClickCallback;
import com.unexceptional.beast.banko.newVersion.callback.ProductClickCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.CurrencyEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.ProductEntity;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.ui.activity.FloatingActivity;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CurrencyViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.ProductListViewModel;
import com.unexceptional.beast.banko.newVersion.ui.adapter.ProductViewAdapter;
import com.unexceptional.beast.banko.other.MyApplication;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link BankClickCallback}
 * interface.
 */
public class ProductListFragment extends SemiComplexListFragment {

    private ProductViewAdapter activeViewAdapter, inactiveViewAdapter;
    private ProductClickCallback mListener;
    private long bankId;
    private Observer<List<ProductEntity>> observer;
    private double allMoney;
    private ProductListViewModel productListViewModel;
    private CurrencyViewModel currencyViewModel;

    public ProductListFragment() {
    }

    public static ProductListFragment newInstance(long bankId ) {
        ProductListFragment fragment = new ProductListFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.KEY_BANK_ID, bankId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        observer= productEntities -> {
            if (productEntities != null) {
                allMoney=0;
                long defaultCurr= preferences.getLong(getString(R.string.key_default_currency_id_new),1);
                mBinding.setIsLoading(false);
                List<ProductEntity> active= new ArrayList<>();
                List<ProductEntity> inactive= new ArrayList<>();

                for(ProductEntity product: productEntities) {
                    if(product.isInactive())
                        inactive.add(product);
                     else
                        active.add(product);


                    new AppExecutors().diskIO().execute(() -> {
                        if (defaultCurr!= product.getCurrencyId()){
                            CurrencyEntity productCurrency= currencyViewModel.getCurrencyRapid(product.getCurrencyId());
                            allMoney+= product.getBalance()*productCurrency.getExchangeRate();

                        }else
                            allMoney+= product.getBalance();

                        new AppExecutors().mainThread().execute(() -> mBinding.allMoney.setText(MyApplication.money.format(allMoney)));

                    });
                }


                activeViewAdapter.setProductList(active);
                inactiveViewAdapter.setProductList(inactive);

                setEmptyText(productEntities.isEmpty());

                if(inactive.isEmpty())
                    mBinding.inactiveButton.setVisibility(View.GONE);
                else
                    mBinding.inactiveButton.setVisibility(View.VISIBLE);


            } else {
                mBinding.setIsLoading(true);
            }

            mBinding.executePendingBindings();
        };
    }

    //from basic list
    @Override
    void setViewModels() {
        productListViewModel = ViewModelProviders.of(this).get(ProductListViewModel.class);
        currencyViewModel = ViewModelProviders.of(ProductListFragment.this).get(CurrencyViewModel.class);

    }

    @Override
    protected void setAdapters() {
        activeViewAdapter = new ProductViewAdapter(mListener);
        mBinding.activeList.setAdapter(activeViewAdapter);

        inactiveViewAdapter = new ProductViewAdapter(mListener);
        mBinding.inactiveList.setAdapter(inactiveViewAdapter);
    }

    @Override
    public void reloadItems() {
        if(getArguments()!=null)
            bankId= getArguments().getLong(Constants.KEY_BANK_ID);


        if(bankId!=0)
            productListViewModel.getBankProducts(bankId).observe(this, observer);
        else
            productListViewModel.getAllProductsNoDebt().observe(this, observer);
    }

    //from semi_complex
    @Override
    protected void setEmptyText(boolean value) {
        super.setEmptyText(value);
     mBinding.emptyListText.setText(R.string.product_list_empty_text);
    }

    @Override
    protected String setInactiveButtonText() {
        return getString(R.string.inactive);
    }

    @Override
    protected void setNewItemButton() {
        mBinding.newItem.setOnClickListener(v -> newProduct());
        if(bankId==101)
            mBinding.newItem.setText(getString(R.string.new_debt));
        else
            mBinding.newItem.setText(getString(R.string.new_account));
    }

    private void newProduct(){
        Intent intent= new Intent(getActivity(), FloatingActivity.class);

        if(bankId==101)
            intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_DEBT_FRAGMENT);
        else {
            intent.putExtra(Constants.KEY_BANK_ID, bankId);
            intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_PRODUCT_FRAGMENT);
        }

        startActivity(intent);
    }

    //from basic
    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_PRODUCT_LIST_FRAGMENT;
    }

    //listener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ProductClickCallback) {
            mListener = (ProductClickCallback) context;
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
}
