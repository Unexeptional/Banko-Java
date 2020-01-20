package com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased.transaction;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.TransactionFragmentBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.PickBankCallback;
import com.unexceptional.beast.banko.newVersion.callback.PickCategoryCallback;
import com.unexceptional.beast.banko.newVersion.callback.PickCurrencyCallback;
import com.unexceptional.beast.banko.newVersion.callback.PickProductCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.NotificationEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.TransactionEntity;
import com.unexceptional.beast.banko.newVersion.db.model.Transaction;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.ui.activity.FloatingActivity;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickBankDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickCurrencyDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.categories.PickParentCategoryDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickProductDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.categories.PickSubCategoryDialog;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CategoryListViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CategoryViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CurrencyViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.NotificationViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.ProductViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link TransactionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransactionFragment extends AbstractTransactionFragment {

    private static final String duplicate= "duplicate";
    private static final String split= "split";
    private static final String subTransaction= "subTransaction";

    public enum Mode{
        DUPLICATE,
        MODIFY,
        NEW
    }

    private Mode mode;
    private TransactionFragmentBinding mBinding;
    private Calendar calendar= Calendar.getInstance();
    private OnFragmentInteractionListener mListener;


    //PICK
    private PickProductCallback fromProductCallback= product ->
            setProduct(product.getId(), true);

    private PickProductCallback toProductCallback= product ->
            setProduct(product.getId(), false);

    private void pickSubCategory(long parentId, boolean from){
        PickCategoryCallback subCategoryPick= product -> setCategory(product.getId(), from);

        ViewModelProviders.of(TransactionFragment.this).get(CategoryListViewModel.class).getChildCategories(parentId).observe(TransactionFragment.this, categoryEntities -> {
                    if(categoryEntities!=null && !categoryEntities.isEmpty()){
                        new PickSubCategoryDialog((FloatingActivity) getActivity(), subCategoryPick, parentId).showDialog();
                    }else {
                        activeTransaction.setSubCategoryId(0);
                        activeTransaction.setSubCategoryName("");
                        setCategory(parentId, from);
                    }
                });
    }

    private void pickCategory(boolean from){
        PickCategoryCallback parentCategoryCallback= category -> {
            if(category.getId()==1) {
                activeTransaction.setSubCategoryId(0);
                activeTransaction.setSubCategoryName("");
                setCategory(1, from);

                new PickProductDialog((FloatingActivity) getActivity(), from ? fromProductCallback : toProductCallback,101).showDialog();
            } else {
                activeTransaction.setSubCategoryId(0);
                setCategory(category.getId(), from);
                pickSubCategory(category.getId(), from);
            }
        };

        new PickParentCategoryDialog((FloatingActivity) getActivity(), parentCategoryCallback, activeTransaction.getTransactionType()).showDialog();
    }

    private void pickBank(boolean from){
        PickBankCallback fromBankCallback= bank ->
                new PickProductDialog((FloatingActivity) getActivity(), fromProductCallback, bank.getId()).showDialog();

        PickBankCallback toBankCallback= bank ->
                new PickProductDialog((FloatingActivity) getActivity(), toProductCallback, bank.getId()).showDialog();


        new PickBankDialog((FloatingActivity) getActivity(), from ? fromBankCallback : toBankCallback, true).showDialog();
    }

    private BindClicks bindClicks= new BindClicks() {
        @Override
        public void fromLayout() {
            switch (activeTransaction.getTransactionType()){
                case Constants.TRANSACTION_TYPE_TRANSFER:
                case Constants.TRANSACTION_TYPE_EXPENSE:
                    if(preferences.getBoolean(getString(R.string.key_show_bank_list), false))
                        pickBank(true);
                    else
                        new PickProductDialog((FloatingActivity) getActivity(), fromProductCallback,0).showDialog();
                    break;
                case Constants.TRANSACTION_TYPE_INCOME:
                    pickCategory(true);
                    break;

            }
        }

        @Override
        public void toLayout() {
            switch (activeTransaction.getTransactionType()){
                case Constants.TRANSACTION_TYPE_EXPENSE:
                    pickCategory(false);
                    break;
                case Constants.TRANSACTION_TYPE_INCOME:
                case Constants.TRANSACTION_TYPE_TRANSFER:
                    if(preferences.getBoolean(getString(R.string.key_show_bank_list), false))
                        pickBank(false);
                     else
                        new PickProductDialog((FloatingActivity) getActivity(), toProductCallback,0).showDialog();

                    break;
            }

        }

        @Override
        public void showDeleteDialog() {
            if(mode== Mode.MODIFY)
                new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                        .setTitle(getString(R.string.warning_title_delete_transaction))
                        .setPositiveButton(android.R.string.yes, (dialog, which) ->
                                deleteTransactionRoom())

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
        public void dateDialog() {
            if(activeTransaction.getKidsAmount()==0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                if(getActivity()!=null) {
                    View dialogView = View.inflate(getActivity(), R.layout.dialog_nt_date, null);
                    builder.setView(dialogView);

                    Button btnPickDate =  dialogView.findViewById(R.id.btn_pick_date);
                    Button btnPickTime =  dialogView.findViewById(R.id.btn_pick_time);
                    Button btnRepeat =  dialogView.findViewById(R.id.btn_repeat);
                    final Button btnToday =  dialogView.findViewById(R.id.btn_today);
                    final Button btnTomorrow =  dialogView.findViewById(R.id.btn_tomorrow);
                    final Button btnYesterday =  dialogView.findViewById(R.id.btn_yesterday);

                    final AlertDialog dialog = builder.create();

                    final DatePickerDialog.OnDateSetListener date= (view, year, month, day) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, day);
                        calendar.set(Calendar.HOUR_OF_DAY, 8);
                        calendar.set(Calendar.MINUTE, 0);
                        updateDate(calendar);

                        dialog.cancel();

                    };

                    btnPickDate.setOnClickListener(v -> new DatePickerDialog(Objects.requireNonNull(getActivity()), date ,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)).show());

                    btnRepeat.setOnClickListener(v -> {
                        showRepeatDialog();
                        dialog.cancel();
                    });

                    btnYesterday.setOnClickListener(v -> {
                        calendar=Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_YEAR, -1);
                        updateDate(calendar);
                        dialog.cancel();
                    });

                    btnToday.setOnClickListener(v -> {
                        calendar= Calendar.getInstance();
                        updateDate(calendar);
                        dialog.cancel();
                    });
                    btnTomorrow.setOnClickListener(v -> {
                        calendar=Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                        updateDate(calendar);
                        dialog.cancel();
                    });
                    btnPickTime.setOnClickListener(v -> {
                        int hour= calendar.get(Calendar.HOUR_OF_DAY);
                        int minute= calendar.get(Calendar.MINUTE);
                        TimePickerDialog mTimePicker;
                        mTimePicker = new TimePickerDialog(getActivity(), (timePicker, selectedHour, selectedMinute) -> {

                            calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                            calendar.set(Calendar.MINUTE, selectedMinute);
                            updateDate(calendar);
                        }, hour, minute, true);
                        mTimePicker.setTitle(getString(R.string.select_time));
                        mTimePicker.show();
                    });

                    //Today
                    if((calendar.get(Calendar.YEAR)==Calendar.getInstance().get(Calendar.YEAR)&&
                            (calendar.get(Calendar.MONTH)==Calendar.getInstance().get(Calendar.MONTH)))){
                        if(calendar.get(Calendar.DATE)==(Calendar.getInstance().get(Calendar.DATE))) {
                            btnToday.setBackgroundColor(getResources().getColor(R.color.red));
                        }else  if(calendar.get(Calendar.DATE)==(Calendar.getInstance().get(Calendar.DATE) -1)){
                            btnYesterday.setBackgroundColor(getResources().getColor(R.color.red));
                        }else  if(calendar.get(Calendar.DATE)==(Calendar.getInstance().get(Calendar.DATE) +1)){
                            btnTomorrow.setBackgroundColor(getResources().getColor(R.color.red));
                        }
                    }
                    dialog.show();
                }
            } else
                Toast.makeText(getActivity(), R.string.warning_transaction_with_kids, Toast.LENGTH_SHORT).show();


        }

        @Override
        public void save() {
            if (onSave(""))
                mListener.closeRealisingSynch();

        }

        @Override
        public void typeDialog() {
            if(activeTransaction.getKidsAmount()==0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

                if(getActivity()!=null) {
                    View dialogView = View.inflate(getActivity(), R.layout.dialog_nt_type, null);
                    builder.setView(dialogView);

                    Button btnExpense =  dialogView.findViewById(R.id.btn_expense);
                    Button btnTransfer =  dialogView.findViewById(R.id.btn_transfer);
                    Button btnIncome =  dialogView.findViewById(R.id.btn_income);

                    final AlertDialog dialog = builder.create();

                    btnExpense.setOnClickListener(v -> {
                        if(activeTransaction.getTransactionType()==Constants.TRANSACTION_TYPE_INCOME)
                            swapProducts();
                        else
                            setType(Constants.TRANSACTION_TYPE_EXPENSE);

                        dialog.cancel();
                    });

                    btnTransfer.setOnClickListener(v -> {
                        setType(Constants.TRANSACTION_TYPE_TRANSFER);
                        dialog.cancel();
                    });

                    btnIncome.setOnClickListener(v -> {
                        if(activeTransaction.getTransactionType()==Constants.TRANSACTION_TYPE_EXPENSE)
                            swapProducts();
                        else
                            setType(Constants.TRANSACTION_TYPE_INCOME);
                        dialog.cancel();
                    });

                    if(activeTransaction.getTransactionType()==Constants.TRANSACTION_TYPE_EXPENSE) {
                        btnExpense.setBackgroundColor(getResources().getColor(R.color.account_red));
                    }else  if(activeTransaction.getTransactionType()==Constants.TRANSACTION_TYPE_TRANSFER){
                        btnTransfer.setBackgroundColor(getResources().getColor(R.color.silver));
                    }else  if(activeTransaction.getTransactionType()==Constants.TRANSACTION_TYPE_INCOME){
                        btnIncome.setBackgroundColor(getResources().getColor(R.color.green));
                    }

                    dialog.show();
                }
            } else
                Toast.makeText(getActivity(), R.string.warning_transaction_with_kids, Toast.LENGTH_SHORT).show();

        }

        @Override
        public void swap() {
            if(activeTransaction.getKidsAmount()==0)
                swapProducts();
            else
                Toast.makeText(getActivity(), R.string.warning_transaction_with_kids, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void currencyPick() {
            PickCurrencyCallback callback= currency -> {
                setCurrency(currency.getId());
                if(currency.getId() != preferences.getLong(getString(R.string.key_default_currency_id_new),1))
                    showSnackBar(String.format("%s: %s", getString(R.string.exchange_rate),
                            moneyFormat.format(currency.getExchangeRate())), null);
            };

            new PickCurrencyDialog((FloatingActivity) getActivity(), callback, true).showDialog();
        }

        @Override
        public void showMoreDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

            if(getActivity()!=null) {
                View dialogView = View.inflate(getActivity(), R.layout.dialog_nt_more, null);
                builder.setView(dialogView);

                Button btnSplit =  dialogView.findViewById(R.id.btn_split);
                Button btnDelete =  dialogView.findViewById(R.id.btn_delete);
                Button btnAddSubTransaction =  dialogView.findViewById(R.id.btn_add_sub);
                Button btnDuplicate =  dialogView.findViewById(R.id.btn_duplicate);


                final AlertDialog dialog = builder.create();

                btnSplit.setOnClickListener(v -> {
                    dialog.cancel();
                    onSave(split);
                });

                btnDelete.setOnClickListener(v -> {
                    dialog.cancel();
                    if(oldTransaction!=null)
                        bindClicks.showDeleteDialog();
                    else
                        getActivity().onBackPressed();
                });

                btnAddSubTransaction.setOnClickListener(v -> {
                    dialog.cancel();
                    onSave(subTransaction);
                });

                btnDuplicate.setOnClickListener(v -> {
                    dialog.cancel();
                    onSave(duplicate);
                });

                dialog.show();
            }
        }

        @Override
        public void duplicate() {
           onSave(duplicate);
        }

        @Override
        public void split() {
            onSave(split);
        }

        @Override
        public void subTransaction() {
           onSave(subTransaction);
        }
    };

    public TransactionFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static TransactionFragment newInstance(long transactionId, boolean shortcutMode, Serializable mode,
                                                  long productId, int transactionType, long categoryId, long debtAccId) {
        TransactionFragment fragment = new TransactionFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.KEY_TRANSACTION_ID, transactionId);
        args.putBoolean(Constants.KEY_TRANSACTION_SHORTCUT_MODE, shortcutMode);
        args.putSerializable(Constants.KEY_TRANSACTION_MODE, mode);

        //optional
        args.putLong(Constants.KEY_PRODUCT_ID, productId);
        args.putLong(Constants.KEY_CATEGORY_ID, categoryId);
        args.putLong(Constants.KEY_DEBT_ACCOUNT_ID, debtAccId);
        args.putInt(Constants.KEY_TRANSACTION_TYPE, transactionType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int provideActivityLayout() {
        return R.layout.transaction_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding= (TransactionFragmentBinding) abstractBinding;
        setCalcObserver(mBinding.amountText, mBinding.buttonEquals, mBinding.buttonAccept);

        mBinding.transactionDescription.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    activeTransaction.setDescription(mBinding.transactionDescription.getText().toString());
                }catch (Exception e){

                }
            }
        });


        //required stuff(oldTransaction and so on)
        if(getArguments()!=null){
            long transactionId= getArguments().getLong(Constants.KEY_TRANSACTION_ID, 0);
            mBinding.setShortcutMode(getArguments().getBoolean(Constants.KEY_TRANSACTION_SHORTCUT_MODE, true));

            mode= (Mode) getArguments().getSerializable(Constants.KEY_TRANSACTION_MODE);
            if (mode==null)
                mode= Mode.NEW;

            switch (mode){
                case NEW:
                    setNewTransaction();
                    break;
                case DUPLICATE:
                    setDuplicateTransaction(transactionId);
                    break;
                case MODIFY:
                    setOldTransaction(transactionId);
                    break;
            }
        }

        mBinding.setCallback(bindClicks);
        mBinding.setAbstractCallback(abstractClicks);
    }

    private void setDuplicateTransaction(long transactionId){
        viewModel.getTransaction(transactionId).observe(this, transactionEntity -> {
            if(transactionEntity!=null){
                activeTransaction= new TransactionEntity(transactionEntity);
                activeTransaction.setRepeatTimes(0);
                activeTransaction.setRepeatTimes(0);
                activeTransaction.setKidsAmount(0);
                activeTransaction.setId(0);

                setProduct(activeTransaction.getFromProductId(), true);
                setProduct(activeTransaction.getToProductId(), false);
                mBinding.amountText.setText(moneyFormat.format(activeTransaction.getAmount()));

                setTransactionVisuals();
            }
        });
    }

    private void setOldTransaction(long transactionId){
        viewModel.getTransaction(transactionId).observe(this, transactionEntity -> {
            if(transactionEntity!=null){
                activeTransaction= transactionEntity;
                oldTransaction= new TransactionEntity(transactionEntity);

                //for exchangeRateChange
                setProduct(activeTransaction.getFromProductId(), true);
                setProduct(activeTransaction.getToProductId(), false);
                mBinding.amountText.setText(moneyFormat.format(activeTransaction.getAmount()));
                calendar= Calendar.getInstance();
                calendar.setTime(activeTransaction.getDate());

                setTransactionVisuals();
            }
        });
    }

    private void setNewTransaction(){
        activeTransaction= new TransactionEntity();
        setDefaults();
        setExtraBundle();
        setTransactionVisuals();
    }

    private void setDefaults(){
        updateDate(Calendar.getInstance());
        setCategory(preferences.getLong(getString(R.string.key_default_sub_category_id_new),
                preferences.getLong(getString(R.string.key_default_category_id_new), 0)), false);
        setCurrency(preferences.getLong(getString(R.string.key_default_currency_id_new), 1));
        setType(Constants.TRANSACTION_TYPE_EXPENSE);
    }

    private void setExtraBundle(){
        if (getArguments() != null) {
            Bundle bundle= getArguments();
            long defaultProdId =preferences.getLong(getString(R.string.key_default_account_id_new), 0);
            long productId= bundle.getLong(Constants.KEY_PRODUCT_ID);
            long categoryId= bundle.getLong(Constants.KEY_CATEGORY_ID);
            long debtAccId= bundle.getLong(Constants.KEY_DEBT_ACCOUNT_ID);


            if (productId==0)
                productId=defaultProdId;

            setType(bundle.getInt(Constants.KEY_TRANSACTION_TYPE));

            switch (activeTransaction.getTransactionType()){
                case Constants.TRANSACTION_TYPE_TRANSFER:
                case Constants.TRANSACTION_TYPE_EXPENSE:
                    setProduct(productId,true);
                    setCategory(categoryId,false);
                    if(debtAccId!=0)
                        setProduct(debtAccId,false);

                   break;
                case Constants.TRANSACTION_TYPE_INCOME:
                    setProduct(productId,false);

                    setCategory(categoryId,true);
                    if(debtAccId!=0)
                        setProduct(debtAccId,true);
                   break;

            }
        }
    }

    private void setTransactionVisuals( ){
        mBinding.setTransaction(activeTransaction);

        setTypeVisual();
        switch (activeTransaction.getTransactionType()){
            case Constants.TRANSACTION_TYPE_EXPENSE:
                setProductVisual(true);
                setCategoryVisual(false);
                break;
            case Constants.TRANSACTION_TYPE_INCOME:
                setCategoryVisual(true);
                setProductVisual(false);
                break;
            case Constants.TRANSACTION_TYPE_TRANSFER:
                setProductVisual(true);
                setProductVisual(false);
                break;
        }

        setCurrencyVisual();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        mBinding.transCreatedAt.setText(dateFormat.format(activeTransaction.getDate()));

        if(activeTransaction.getKidsAmount()>0){
            mBinding.typeImage.setEnabled(false);
            mBinding.swapBanks.setEnabled(false);
            mBinding.btnCurrency.setEnabled(false);
            switch (activeTransaction.getTransactionType()){
                case Constants.TRANSACTION_TYPE_TRANSFER:
                    mBinding.fromLayout.setEnabled(false);
                    mBinding.toLayout.setEnabled(false);
                    break;
                case Constants.TRANSACTION_TYPE_EXPENSE:
                    mBinding.fromLayout.setEnabled(false);
                    break;
                case Constants.TRANSACTION_TYPE_INCOME:
                    mBinding.toLayout.setEnabled(false);
                    break;
            }


        }
    }



    //PRODUCT
    private void setProduct(long productId, boolean from){
        if(from){
            ViewModelProviders.of(this).get(ProductViewModel.class).getProduct(productId).observe(this, productEntity -> {
                if (productEntity!=null) {
                    fromProduct=productEntity;
                    activeTransaction.setFromProductId(productEntity.getId());
                    activeTransaction.setFromBankId(productEntity.getBankId());
                    getExchangeRate(productEntity.getCurrencyId(), true);
                }else {
                    fromProduct=null;
                    activeTransaction.setFromProductId(0);
                    activeTransaction.setFromBankId(0);
                    fromRate=0;
                }
                setTransactionVisuals();
            });

        }else {
            ViewModelProviders.of(this).get(ProductViewModel.class).getProduct(productId).observe(this, productEntity -> {
                if (productEntity!=null) {
                    toProduct=productEntity;
                    activeTransaction.setToProductId(productEntity.getId());
                    activeTransaction.setToBankId(productEntity.getBankId());
                    getExchangeRate(productEntity.getCurrencyId(), false);
                }else {
                    toProduct=null;
                    activeTransaction.setToProductId(0);
                    activeTransaction.setToBankId(0);
                    toRate=0;
                }
                setTransactionVisuals();
            });
        }

    }

    private void setProductVisual(boolean from){
        if(from){
            int id = MyApplication.getBankIconId(activeTransaction.getFromBankId());
            if (id != 0)
                Picasso.get().load(id).into(mBinding.fromBank);
            else
                Picasso.get().load(R.drawable.baseline_account_balance_black_24).into(mBinding.fromBank);

            ViewModelProviders.of(this).get(ProductViewModel.class).getProduct(activeTransaction.
                    getFromProductId()).observe(this, productEntity -> {
                if (productEntity!=null)
                    mBinding.fromProduct.setText(productEntity.getTitle());
                else
                    mBinding.fromProduct.setText(getString(R.string.account));
            });
        }else {
            int id = MyApplication.getBankIconId(activeTransaction.getToBankId());
            if (id != 0)
                Picasso.get().load(id).into(mBinding.toBank);
            else
                Picasso.get().load(R.drawable.baseline_account_balance_black_24).into(mBinding.toBank);

            ViewModelProviders.of(this).get(ProductViewModel.class).getProduct(activeTransaction.
                    getToProductId()).observe(this, productEntity -> {
                if (productEntity!=null)
                    mBinding.toProduct.setText(productEntity.getTitle());
                else
                    mBinding.toProduct.setText(getString(R.string.account));
            });
        }
    }

    private void swapProducts(){
        long fromProduct= activeTransaction.getFromProductId();
        setProduct(activeTransaction.getToProductId(), true);
        setProduct(fromProduct, false);
        swapType();
    }

    //CATEGORY
    private void setCategory(long categoryId, boolean from){
        ViewModelProviders.of(this).get(CategoryViewModel.class).getCategory(categoryId).observe(this, categoryEntity -> {
            if (categoryEntity!=null) {
                if(categoryEntity.getParentId()>0){
                    activeTransaction.setSubCategoryId(categoryEntity.getId());
                    activeTransaction.setSubCategoryName(categoryEntity.getTitle());
                    setCategory(categoryEntity.getParentId(), from);
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

            setTransactionVisuals();
        });
    }

    private void setCategoryVisual(boolean from){
        if(activeTransaction.getCategoryId()==1){
            setProductVisual(from);
        }else {
            if(from){
                int id = MyApplication.getDrawableId(activeTransaction.getIconName());
                if (id != 0)
                    Picasso.get().load(id).into(mBinding.fromBank);
                else
                    Picasso.get().load(R.drawable.outline_category_black_24).into(mBinding.fromBank);

                if(activeTransaction.getSubCategoryId()!=0)
                    mBinding.fromProduct.setText(activeTransaction.getSubCategoryName());
                else if (activeTransaction.getCategoryId()!=0)
                    mBinding.fromProduct.setText(activeTransaction.getCategoryName());
                else
                    mBinding.fromProduct.setText(getString(R.string.category));
            }else {
                int id = MyApplication.getDrawableId(activeTransaction.getIconName());
                if (id != 0)
                    Picasso.get().load(id).into(mBinding.toBank);
                else
                    Picasso.get().load(R.drawable.outline_category_black_24).into(mBinding.toBank);

                if(activeTransaction.getSubCategoryId()!=0)
                    mBinding.toProduct.setText(activeTransaction.getSubCategoryName());
                else if (activeTransaction.getCategoryId()!=0)
                    mBinding.toProduct.setText(activeTransaction.getCategoryName());
                else
                    mBinding.toProduct.setText(getString(R.string.category));
            }
        }
    }

    //TYPE
    private void swapType(){
        if(activeTransaction.getTransactionType()==Constants.TRANSACTION_TYPE_INCOME)
            setType(Constants.TRANSACTION_TYPE_EXPENSE);
        else  if(activeTransaction.getTransactionType()==Constants.TRANSACTION_TYPE_EXPENSE)
            setType(Constants.TRANSACTION_TYPE_INCOME);
    }

    private void setType(int transactionType){
        activeTransaction.setTransactionType(transactionType);

        setTransactionVisuals();
    }

    private void setTypeVisual(){
        switch (activeTransaction.getTransactionType()){
            case Constants.TRANSACTION_TYPE_TRANSFER:
                mBinding.typeImage.setImageResource(R.drawable.baseline_repeat_black_48);
                mBinding.amountText.setTextColor(getResources().getColor(R.color.silver));
                break;
            case Constants.TRANSACTION_TYPE_EXPENSE:
                mBinding.typeImage.setImageResource(R.drawable.outline_remove_black_48);
                mBinding.amountText.setTextColor(getResources().getColor(R.color.account_red));
                break;
            case Constants.TRANSACTION_TYPE_INCOME:
                mBinding.typeImage.setImageResource(R.drawable.outline_add_black_48);
                mBinding.amountText.setTextColor(getResources().getColor(R.color.green));
                break;
        }
    }

    private void setCurrency(long currencyId){
        ViewModelProviders.of(this).get(CurrencyViewModel.class).getCurrency(currencyId)
                .observe(this, currencyEntity -> {
                    if(currencyEntity!=null){
                        activeTransaction.setCurrencyId(currencyId);
                        activeTransaction.setCurrencySymbol(currencyEntity.getSymbol());
                        setCurrencyVisual();
                    }
                });
    }

    private void setCurrencyVisual(){
        ViewModelProviders.of(this).get(CurrencyViewModel.class).getCurrency(activeTransaction.getCurrencyId())
                .observe(this, currencyEntity -> {
                    if(currencyEntity!=null){
                        mBinding.btnCurrency.setText(currencyEntity.getShortcut());
                        mainRate= currencyEntity.getExchangeRate();
                    }
                });
    }


    //DATE
    private void showRepeatDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        if(getActivity()!=null) {
            View dialogView = View.inflate(getActivity(), R.layout.dialog_n_t_repeat, null);
            builder.setView(dialogView);

            final EditText inputTimes =  dialogView.findViewById(R.id.input_repeat_times);
            Spinner spinnerDate= dialogView.findViewById(R.id.spinner_date_option);

            Button btnAccept= dialogView.findViewById(R.id.btn_accept);
            Button btnDecline= dialogView.findViewById(R.id.btn_decline);

            inputTimes.setText(String.valueOf(activeTransaction.getRepeatTimes()));

            final AlertDialog dialog = builder.create();

            ArrayAdapter<CharSequence> adapter= ArrayAdapter.createFromResource(Objects.requireNonNull(getActivity()), R.array.RepeatSpinnerDateOption,
                    android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDate.setAdapter(adapter);
            spinnerDate.setSelection(activeTransaction.getRepeatDateOption());
            spinnerDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    activeTransaction.setRepeatDateOption(i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            btnAccept.setOnClickListener(view -> {
                activeTransaction.setRepeatTimes(Integer.parseInt(inputTimes.getText().toString()));
                dialog.cancel();
            });
            btnDecline.setOnClickListener(view -> dialog.cancel());

            dialog.show();
        }
    }

    private void updateDate(Calendar calendar) {
        activeTransaction.setDate(calendar.getTime());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

        mBinding.transCreatedAt.setText(dateFormat.format(calendar.getTime()));

        if((calendar.get(Calendar.YEAR)==Calendar.getInstance().get(Calendar.YEAR)&&
                (calendar.get(Calendar.MONTH)==Calendar.getInstance().get(Calendar.MONTH)))){

            if(calendar.get(Calendar.DATE)==Calendar.getInstance().get(Calendar.DATE)){
                mBinding.transCreatedAt.setText(String.format(Locale.getDefault(), "%s  %s", getString(R.string.today), dateFormat.format(calendar.getTime())));
            }else if(calendar.get(Calendar.DATE)==Calendar.getInstance().get(Calendar.DATE)-1){
                mBinding.transCreatedAt.setText(String.format(Locale.getDefault(), "%s  %s", getString(R.string.yesterday), dateFormat.format(calendar.getTime())));
            }else   if(calendar.get(Calendar.DATE)==Calendar.getInstance().get(Calendar.DATE)+1){
                mBinding.transCreatedAt.setText(String.format(Locale.getDefault(), "%s  %s", getString(R.string.tomorrow), dateFormat.format(calendar.getTime())));
            }
        }
    }

    //SAVING CHANGES
    private boolean onSave(String code){
        activeTransaction.setDescription(mBinding.transactionDescription.getText().toString());

        //check this whole acc/cat mess and set texts
        switch (activeTransaction.getTransactionType()){
            case Constants.TRANSACTION_TYPE_TRANSFER:
                if (activeTransaction.getFromProductId()==0) {
                    Toast.makeText(getActivity(), R.string.pick_account, Toast.LENGTH_SHORT).show();
                    return false;
                }

                if(activeTransaction.getToProductId()==0){
                    Toast.makeText(getActivity(),  R.string.pick_account, Toast.LENGTH_SHORT).show();
                    return false;
                }else
                    activeTransaction.setSubCategoryName(toProduct.getTitle());

                activeTransaction.setCategoryId(0);
                activeTransaction.setSubCategoryId(0);
                activeTransaction.setCategoryName(getString(R.string.transfer));

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
                }else
                    activeTransaction.setSubCategoryName(toProduct.getTitle());

                break;
            case Constants.TRANSACTION_TYPE_INCOME:
                if(activeTransaction.getCategoryId()==0){
                    Toast.makeText(getActivity(), R.string.pick_category, Toast.LENGTH_SHORT).show();
                    return false;
                }
                if(activeTransaction.getToProductId()==0){
                    Toast.makeText(getActivity(),  R.string.pick_account, Toast.LENGTH_SHORT).show();
                    return false;
                }
                if(activeTransaction.getCategoryId()!=1){
                    activeTransaction.setFromBankId(0);
                    activeTransaction.setFromProductId(0);
                }else
                    activeTransaction.setSubCategoryName(fromProduct.getTitle());

                break;
        }

        setAmounts(mBinding.amountText);
        showFullAd();

        if(mode== Mode.MODIFY) {
            if (checkExtraStuff(code)){
                viewModel.update(activeTransaction, oldTransaction);
                doExtraStuff(code, activeTransaction.getId());
            }

        } else {
            if (checkExtraStuff(code)){
                new AppExecutors().diskIO().execute(() -> {
                    long id= viewModel.insert(activeTransaction);
                    doExtraStuff(code, id);
                });
            }
        }
        return true;
    }

    private boolean  checkExtraStuff(String code){
        switch (code) {
            case duplicate:
                break;
            case split:
                if (activeTransaction.getTransactionType() == Constants.TRANSACTION_TYPE_TRANSFER || activeTransaction.getCategoryId() == 1) {
                    Toast.makeText(getActivity(), R.string.warning_transfer_debt_with_split, Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case subTransaction:
                if (activeTransaction.getTransactionType() == Constants.TRANSACTION_TYPE_TRANSFER) {
                    Toast.makeText(getActivity(), R.string.warning_transfer_with_subtransaction, Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
        }

        return true;
    }

    private void doExtraStuff(String code, long id){
        switch (code) {
            case duplicate:
                mListener.duplicate(id);
                break;
            case split:
                mListener.split(id);
                break;
            case subTransaction:
                mListener.subTransaction(id);
                break;
        }

    }

    private void deleteTransactionRoom(){
        if (activeTransaction.getKidsAmount()>0)
            Toast.makeText(getActivity(), getString(R.string.warning_transaction_with_kids), Toast.LENGTH_SHORT).show();
        else{
            viewModel.delete(oldTransaction);
            Objects.requireNonNull(getActivity()).onBackPressed();
        }
    }

    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_TRANSACTION_FRAGMENT;
    }

    public interface BindClicks{
        void fromLayout();
        void toLayout();
        void showDeleteDialog();
        void edit();
        void dissmiss();
        void dateDialog();
        void save();
        void typeDialog();
        void swap();
        void currencyPick();
        void showMoreDialog();
        void duplicate();
        void split();
        void subTransaction();
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
        void duplicate(long id);
        void split(long id);
        void subTransaction(long id);
        void closeRealisingSynch();
    }
}
