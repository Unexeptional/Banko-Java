package com.unexceptional.beast.banko.newVersion.ui.fragment;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.FragmentAccountShortcuts2Binding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.db.entity.ProductEntity;
import com.unexceptional.beast.banko.newVersion.db.model.Product;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.ProductViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

public class ProductShortcutsFragment extends BasicFragment{

    private ProductEntity product;
    private OnFragmentInteractionListener mListener;
    private FragmentAccountShortcuts2Binding mBinding;


    public ProductShortcutsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment HelloFragment.
     */
    public static ProductShortcutsFragment newInstance(long bankId, long productId) {

        ProductShortcutsFragment fragment = new ProductShortcutsFragment();
        Bundle bundle= new Bundle();
        bundle.putLong(Constants.KEY_BANK_ID, bankId);
        bundle.putLong(Constants.KEY_PRODUCT_ID, productId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.fragment_account_shortcuts_2, container, false);

        getBundle();

        mBinding.setCallback(mListener);
        setClicks();


        return mBinding.getRoot();
    }


    private void getBundle(){
        if (getArguments() != null) {
            //int bankId = getArguments().getInt(Constants.KEY_BANK_ID, 0);
            long productId = getArguments().getLong(Constants.KEY_PRODUCT_ID, 0);
           /* if (bankId!=0)
                bank= db.getBank(bankId);*/

            if(productId!=0){
                ViewModelProviders.of(this).get(ProductViewModel.class).getProduct(productId).observe(this, productEntity -> {
                    if (productEntity!=null){
                        product= productEntity;
                        setProductVisuals();
                    }else
                        Objects.requireNonNull(getActivity()).onBackPressed();
                });

            }
        }

    }

    private void setProductVisuals( ){
        mBinding.setProduct(product);

        int id= MyApplication.getBankIconId(product.getBankId());
        if(id!=0)
            Picasso.get().load(id).into(mBinding.listIcon);

        mBinding.balance.setText(MyApplication.money.format(product.getBalance()));
    }

    private void setClicks(){
        mBinding.accountShortcutPad.setOnClickListener(v -> Objects.requireNonNull(getActivity()).onBackPressed());

        mBinding.copyButton.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) MyApplication.getContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                ClipData myClip;
                myClip = ClipData.newPlainText("text", mBinding.listDescription.getText().toString());
                cm.setPrimaryClip(myClip);
                Toast.makeText(getActivity(), R.string.warning_copied_to_clipboard, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_PRODUCT_SHORTCUTS_FRAGMENT;
    }

    //listener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void edit(Product product);
        void newTransaction(int transactionType, Product product);
        void transactionList(Product product);
        void taskList(Product product);
        void changeBalance(Product product);
    }

}
