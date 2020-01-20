package com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.DebtFragmentBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.db.entity.ProductEntity;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.ui.fragment.BasicFragment;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.ProductViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link DebtFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DebtFragment extends AbstractProductFragment {

    private DebtFragmentBinding mBinding;
    private ProductEntity activeDebt;
    private boolean modify;


    public DebtFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static DebtFragment newInstance(long productId) {
        DebtFragment fragment = new DebtFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.KEY_PRODUCT_ID, productId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.debt_fragment, container, false);

        if(getArguments()!=null){
            activeDebt= new ProductEntity();

            long productId= getArguments().getLong(Constants.KEY_PRODUCT_ID, 0);
            if(productId!=0)
                getDebt(productId);
            else
                setNewDebt();

        }

        setClicks();

        return mBinding.getRoot();
    }

    private void getDebt(long productId){
        modify=true;
        viewModel.getProduct(productId).observe(this, productEntity -> {
            if(productEntity!=null){
                activeDebt= productEntity;
                setDebtVisuals();
            }else
                setNewDebt();

        });
    }

    private void setNewDebt(){

        activeDebt.setBankId(101);
        activeDebt.setProductType(40);
        activeDebt.setCurrencyId(preferences.getLong(getString(R.string.key_default_currency_id_new), 1));
        setNewDebtVisual();
    }

    private void setDebtVisuals( ){
        mBinding.setDebt(activeDebt);

        mBinding.closedDebt.setChecked(activeDebt.isInactive());

        mBinding.debtBalance.setText(MyApplication.money.format(activeDebt.getBalance()));

    }

    private void setNewDebtVisual(){
        mBinding.btnDelete.setVisibility(View.GONE);
        mBinding.debtBalance.setText("0");
    }

    private void setClicks(){

        mBinding.floatingPad.setOnClickListener(v ->
                Objects.requireNonNull(getActivity()).onBackPressed());
        mBinding.btnSave.setOnClickListener(v -> onSave());

        mBinding.btnDelete.setOnClickListener(v -> new AppExecutors().diskIO().execute(() -> {
            if(canChange(activeDebt))
                showDeleteDialog(activeDebt, viewModel);
            else
                showSnackBar(activeDebt, mBinding.getRoot());
        }));

        mBinding.closedDebt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            activeDebt.setInactive(isChecked);
        });
    }

    //SAVING
    private void onSave(){

        //CHECK
        if(mBinding.debtTitle.getText()==null || mBinding.debtTitle.getText().toString().equals("")){
            mBinding.debtTitle.setError(getString(R.string.enter_valid_name));
            return;
        }

        if(mBinding.debtBalance.getText()==null || mBinding.debtBalance.getText().toString().equals("")){
            mBinding.debtBalance.setError(getString(R.string.enter_valid_balance));
            return;
        }

        String balance= mBinding.debtBalance.getText().toString().replaceAll("\\s","").replace(",", ".");

        //SET
        activeDebt.setBalance((double) Math.round(Double.parseDouble(balance) * 100) / 100);
        activeDebt.setTitle(mBinding.debtTitle.getText().toString());
        activeDebt.setEndDate(null);

        if(mBinding.debtDesc.getText()!=null)
            activeDebt.setDescription(mBinding.debtDesc.getText().toString());


        if(modify)
            viewModel.update(activeDebt);
        else{
            ColorGenerator generator= ColorGenerator.MATERIAL;
            int color= generator.getRandomColor();
            activeDebt.setColor(color);
            viewModel.insert(activeDebt);
        }

        Objects.requireNonNull(getActivity()).onBackPressed();
    }

    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_DEBT_FRAGMENT;
    }

}
