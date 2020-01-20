package com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased.transaction;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.SubTransactionFragmentBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.PickCategoryCallback;
import com.unexceptional.beast.banko.newVersion.callback.PickProductCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.TransactionEntity;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.ui.activity.FloatingActivity;
import com.unexceptional.beast.banko.newVersion.ui.dialog.categories.PickParentCategoryDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickProductDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.categories.PickSubCategoryDialog;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CategoryListViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CategoryViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CurrencyViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.ProductViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link SubTransactionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SubTransactionFragment extends AbstractTransactionFragment {

    public enum Mode{
        SPLIT,
        NEW_SUB,
        MODIFY,
        DUPLICATE
    }

    private SubTransactionFragmentBinding mBinding;
    private Mode mode;
    private OnFragmentInteractionListener mListener;

    private BindClicks bindClicks= new BindClicks() {
        @Override
        public void setLayout() {

            PickCategoryCallback subCategoryPick= category -> setCategory(category.getId());

            PickProductCallback debtCallback= product -> setDebt(product.getId());

            PickCategoryCallback categoryCallback= category -> {
                if(category.getId()==1) {
                    setCategory(1);
                    new PickProductDialog((FloatingActivity) getActivity(), debtCallback,101).showDialog();
                } else {
                    ViewModelProviders.of(SubTransactionFragment.this).get(CategoryListViewModel.class).getChildCategories(category.getId())
                            .observe(SubTransactionFragment.this, categoryEntities -> {
                                if(categoryEntities!=null && !categoryEntities.isEmpty()){
                                    new PickSubCategoryDialog((FloatingActivity) getActivity(), subCategoryPick, category.getId()).showDialog();
                                }
                                 setCategory(category.getId());
                            });
                }
            };

            new PickParentCategoryDialog((FloatingActivity) getActivity(), categoryCallback, activeTransaction.getTransactionType()).showDialog();

        }

        @Override
        public void showDeleteDialog() {
            if(mode== Mode.MODIFY)
                new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                        .setTitle(getString(R.string.warning_title_delete_transaction))
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            viewModel.delete(oldTransaction);
                            viewModel.updateKids(activeTransaction.getParentTransactionId(), -1);
                            Objects.requireNonNull(getActivity()).onBackPressed();
                        })

                        .setNegativeButton(android.R.string.no, null)
                        .show();
            else
                Objects.requireNonNull(getActivity()).onBackPressed();
        }

        @Override
        public void edit() {
            mBinding.setShortcutMode(false);
        }

        @Override
        public void dissmiss() {
            Objects.requireNonNull(getActivity()).finish();
        }

        @Override
        public void save() {
            if (onSave(false))
                mListener.closeRealisingSynch();
        }

        @Override
        public void duplicate() {
            onSave(true);
        }
    };

    public SubTransactionFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static SubTransactionFragment newInstance(long transactionId, boolean shortcutMode, Serializable mode) {
        SubTransactionFragment fragment = new SubTransactionFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.KEY_TRANSACTION_ID, transactionId);
        args.putBoolean(Constants.KEY_TRANSACTION_SHORTCUT_MODE, shortcutMode);
        args.putSerializable(Constants.KEY_TRANSACTION_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int provideActivityLayout() {
        return R.layout.sub_transaction_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding= (SubTransactionFragmentBinding) abstractBinding;
        setCalcObserver(mBinding.amountText, mBinding.buttonEquals, mBinding.buttonAccept);

        //required stuff(oldTransaction and so on)
        if(getArguments()!=null){
            long transactionId= getArguments().getLong(Constants.KEY_TRANSACTION_ID, 0);
            mBinding.setShortcutMode(getArguments().getBoolean(Constants.KEY_TRANSACTION_SHORTCUT_MODE, true));

            mode= (Mode) getArguments().getSerializable(Constants.KEY_TRANSACTION_MODE);
            if (mode==null)
                mode= Mode.NEW_SUB;

            switch (mode){
                case SPLIT:
                    setSplitTransaction(transactionId);
                    break;
                case NEW_SUB:
                    setSubTransaction(transactionId);
                    break;
                case MODIFY:
                    setOldTransaction(transactionId);
                    break;
                case DUPLICATE:
                    setDuplicateTransaction(transactionId);
                    break;
            }
        }


        mBinding.setCallback(bindClicks);
        mBinding.setAbstractCallback(abstractClicks);
    }

    private void setSplitTransaction(long parentId){
        viewModel.getTransaction(parentId).observe(this, transactionEntity -> {
            if(transactionEntity!=null){
                oldTransaction= transactionEntity;
                activeTransaction= new TransactionEntity(transactionEntity);

                activeTransaction.setParentTransactionId(parentId);
                activeTransaction.setRepeatTimes(0);
                activeTransaction.setRepeatDateOption(0);
                activeTransaction.setKidsAmount(0);
                activeTransaction.setId(0);

                getExchangeRates();
                setTexts();
                setTransactionVisuals();
            }
        });
    }

    private void setSubTransaction(long parentId){
        viewModel.getTransaction(parentId).observe(this, transactionEntity -> {
            if(transactionEntity!=null){
                activeTransaction= new TransactionEntity(transactionEntity);

                activeTransaction.setParentTransactionId(parentId);
                activeTransaction.setRepeatTimes(0);
                activeTransaction.setRepeatDateOption(0);
                activeTransaction.setKidsAmount(0);
                activeTransaction.setId(0);


                getExchangeRates();
                setTexts();
                setTransactionVisuals();
            }
        });
    }

    private void setOldTransaction(long transactionId){
        viewModel.getTransaction(transactionId).observe(this, transactionEntity -> {
            if(transactionEntity!=null){
                activeTransaction= transactionEntity;
                oldTransaction= new TransactionEntity(transactionEntity);

                getExchangeRates();
                setTexts();
                setTransactionVisuals();
            }
        });
    }

    private void setDuplicateTransaction(long transactionId){
        viewModel.getTransaction(transactionId).observe(this, transactionEntity -> {
            if(transactionEntity!=null){
                activeTransaction= new TransactionEntity(transactionEntity);
                activeTransaction.setId(0);

                getExchangeRates();
                setTexts();
                setTransactionVisuals();
            }
        });
    }

    private void setTexts(){
        mBinding.transactionDescription.setText(activeTransaction.getDescription());
        mBinding.amountText.setText(MyApplication.money.format(activeTransaction.getAmount()));
    }

    //refreshing stuff visually. Text not allowed here (would get deleted bc not set automatically when entered)
    private void setTransactionVisuals( ){
        mBinding.setTransaction(activeTransaction);
        setCategoryVisual();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        mBinding.transCreatedAt.setText(dateFormat.format(activeTransaction.getDate()));
    }

    //DEBT
    private void setDebt(long productId){
        if (activeTransaction.getTransactionType()==Constants.TRANSACTION_TYPE_EXPENSE){
            ViewModelProviders.of(this).get(ProductViewModel.class).getProduct(productId)
                    .observe(this, productEntity -> {
                        if (productEntity!=null) {
                            activeTransaction.setToProductId(productEntity.getId());
                            activeTransaction.setToBankId(productEntity.getBankId());
                            activeTransaction.setSubCategoryName(productEntity.getTitle());
                            getExchangeRate(productEntity.getCurrencyId(), false);
                        }else {
                            activeTransaction.setToProductId(0);
                            activeTransaction.setToBankId(0);
                            activeTransaction.setSubCategoryName("");
                            toRate=0;
                        }
                        setTransactionVisuals();
                    });
        }else {
            ViewModelProviders.of(this).get(ProductViewModel.class).getProduct(productId)
                    .observe(this, productEntity -> {
                        if (productEntity!=null) {
                            activeTransaction.setFromProductId(productEntity.getId());
                            activeTransaction.setFromBankId(productEntity.getBankId());
                            activeTransaction.setSubCategoryName(productEntity.getTitle());
                            getExchangeRate(productEntity.getCurrencyId(), true);
                        }else {
                            activeTransaction.setFromProductId(0);
                            activeTransaction.setFromBankId(0);
                            activeTransaction.setSubCategoryName("");
                            fromRate=0;
                        }
                        setTransactionVisuals();
                    });

        }
    }

    private void setDebtVisual(){
        long productId;
        long bankId;
        if (activeTransaction.getTransactionType()==Constants.TRANSACTION_TYPE_EXPENSE){
            productId= activeTransaction.getToProductId();
            bankId= activeTransaction.getToBankId();
        }else {
            productId= activeTransaction.getFromProductId();
            bankId= activeTransaction.getFromBankId();
        }

        int id = MyApplication.getBankIconId(bankId);
        if (id != 0)
            Picasso.get().load(id).into(mBinding.toBank);
        else
            Picasso.get().load(R.drawable.baseline_account_balance_black_24).into(mBinding.toBank);

        ViewModelProviders.of(this).get(ProductViewModel.class).getProduct(productId).observe(this, productEntity -> {
            if (productEntity!=null)
                mBinding.toProduct.setText(productEntity.getTitle());
            else
                mBinding.toProduct.setText(getString(R.string.account));
        });
    }

    //CATEGORY
    private void setCategory(long categoryId){
        ViewModelProviders.of(this).get(CategoryViewModel.class).getCategory(categoryId).observe(this, categoryEntity -> {
            if (categoryEntity!=null) {
                if(categoryEntity.getParentId()>0){
                    activeTransaction.setSubCategoryId(categoryEntity.getId());
                    activeTransaction.setSubCategoryName(categoryEntity.getTitle());
                    setCategory(categoryEntity.getParentId());
                }else {
                    activeTransaction.setCategoryId(categoryEntity.getId());
                    activeTransaction.setCategoryName(categoryEntity.getTitle());
                    activeTransaction.setIconName(categoryEntity.getIconName());
                    if (activeTransaction.getSubCategoryId()==0)
                        activeTransaction.setSubCategoryName("");
                }
            }else {
                activeTransaction.setCategoryId(0);
                activeTransaction.setCategoryName("");
                activeTransaction.setIconName("");
                activeTransaction.setSubCategoryId(0);
                activeTransaction.setSubCategoryName("");
            }



            if (categoryId==1)
                setDebt(preferences.getLong(getString(R.string.key_default_debt_account_id_new), 0));

            setTransactionVisuals();
        });
    }

    private void setCategoryVisual(){
        if(activeTransaction.getCategoryId()==1){
            setDebtVisual();
        }else {
            int id = MyApplication.getDrawableId(activeTransaction.getIconName());
            if (id != 0)
                Picasso.get().load(id).into(mBinding.toBank);
            else
                Picasso.get().load(R.drawable.outline_category_black_24).into(mBinding.toBank);

            if(activeTransaction.getSubCategoryId()!=0)
                mBinding.toProduct.setText(String.format("%s (%s)", activeTransaction.getCategoryName(), activeTransaction.getSubCategoryName()));
            else if (activeTransaction.getCategoryId()!=0)
                mBinding.toProduct.setText(activeTransaction.getCategoryName());
            else
                mBinding.toProduct.setText(getString(R.string.category));
        }
    }


    private void getExchangeRates(){


        ViewModelProviders.of(this).get(ProductViewModel.class).getProduct(activeTransaction.getFromProductId()).observe(this, productEntity -> {
            if (productEntity!=null)
                getExchangeRate(productEntity.getCurrencyId(), true);
        });

        ViewModelProviders.of(this).get(ProductViewModel.class).getProduct(activeTransaction.getToProductId()).observe(this, productEntity -> {
            if (productEntity!=null)
               getExchangeRate(productEntity.getCurrencyId(), false);
        });

        ViewModelProviders.of(this).get(CurrencyViewModel.class).getCurrency(activeTransaction.getCurrencyId())
                .observe(this, currencyEntity -> {
                    if(currencyEntity!=null){
                        mainRate= currencyEntity.getExchangeRate();
                    }
                });
    }

    //SAVING CHANGES
    private boolean onSave(boolean duplicate){
        activeTransaction.setDescription(mBinding.transactionDescription.getText().toString());

        //check this whole acc/cat mess
        switch (activeTransaction.getTransactionType()){
            case Constants.TRANSACTION_TYPE_TRANSFER:
                if (activeTransaction.getFromProductId()==0) {
                    Toast.makeText(getActivity(), R.string.pick_account, Toast.LENGTH_SHORT).show();
                    return false;
                }

                if(activeTransaction.getToProductId()==0){
                    Toast.makeText(getActivity(), R.string.pick_account, Toast.LENGTH_SHORT).show();
                    return false;
                }

                activeTransaction.setCategoryId(0);
                activeTransaction.setSubCategoryId(0);

                break;
            case Constants.TRANSACTION_TYPE_EXPENSE:
                if(activeTransaction.getCategoryId()==0){
                    Toast.makeText(getActivity(), R.string.pick_category, Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (activeTransaction.getFromProductId()==0) {
                    Toast.makeText(getActivity(), R.string.pick_account, Toast.LENGTH_SHORT).show();
                    return false;
                }

                if(activeTransaction.getCategoryId()!=1){
                    activeTransaction.setToBankId(0);
                    activeTransaction.setToProductId(0);
                }

                break;
            case Constants.TRANSACTION_TYPE_INCOME:
                if(activeTransaction.getCategoryId()==0){
                    Toast.makeText(getActivity(), R.string.pick_category, Toast.LENGTH_SHORT).show();
                    return false;
                }
                if(activeTransaction.getToProductId()==0){
                    Toast.makeText(getActivity(), R.string.pick_account, Toast.LENGTH_SHORT).show();
                    return false;
                }
                if(activeTransaction.getCategoryId()!=1){
                    activeTransaction.setFromBankId(0);
                    activeTransaction.setFromProductId(0);
                }
                break;
        }

        setAmounts(mBinding.amountText);
        showFullAd();
        if(mode== Mode.MODIFY) {
            viewModel.update(activeTransaction, oldTransaction);
            if (duplicate)
                mListener.duplicateSub(activeTransaction.getId());

        } else {
            new AppExecutors().diskIO().execute(() -> {
                long id= viewModel.insert(activeTransaction);

                if (mode== Mode.SPLIT){
                    TransactionEntity newTransaction= new TransactionEntity(oldTransaction);

                    double tempAmount= oldTransaction.getAmount();
                    double tempFromAmount= oldTransaction.getFromAmount();
                    double tempToAmount= oldTransaction.getToAmount();

                    tempAmount-= activeTransaction.getAmount();
                    tempFromAmount-= activeTransaction.getFromAmount();
                    tempToAmount-= activeTransaction.getToAmount();

                    if(tempAmount<0)
                        tempAmount=0;
                    if(tempFromAmount<0)
                        tempFromAmount=0;
                    if(tempToAmount<0)
                        tempToAmount=0;

                    oldTransaction.setAmount(tempAmount);
                    oldTransaction.setFromAmount(tempFromAmount);
                    oldTransaction.setToAmount(tempToAmount);

                    oldTransaction.setKidsAmount(oldTransaction.getKidsAmount() + 1);

                    viewModel.update(oldTransaction, newTransaction);
                }else
                    viewModel.updateKids(activeTransaction.getParentTransactionId(), 1);

                if (duplicate)
                    mListener.duplicateSub(id);
            });
        }
        return true;
    }

    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_SUB_TRANSACTION_FRAGMENT;
    }

    public interface BindClicks{
        void setLayout();
        void showDeleteDialog();
        void edit();
        void dissmiss();
        void save();
        void duplicate();
    }

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void duplicateSub(long id);
        void closeRealisingSynch();
    }
}
