package com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.ProductFragmentBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.PickBankCallback;
import com.unexceptional.beast.banko.newVersion.callback.PickCurrencyCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.ProductEntity;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.ui.activity.FloatingActivity;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickBankDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickCurrencyDialog;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CurrencyViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.TaskViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.TransactionViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link ProductFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductFragment extends AbstractProductFragment {

    private ProductFragmentBinding mBinding;
    private ProductEntity activeProduct;
    private int prodType;
    private boolean modify;
    private long oldBankId;


    public ProductFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static ProductFragment newInstance(long bankId, long productId) {
        ProductFragment fragment = new ProductFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.KEY_BANK_ID, bankId);
        args.putLong(Constants.KEY_PRODUCT_ID, productId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.product_fragment, container, false);


        if(getArguments()!=null){
            activeProduct= new ProductEntity();
            activeProduct.setBankId(getArguments().getLong(Constants.KEY_BANK_ID, 0));

            long productId= getArguments().getLong(Constants.KEY_PRODUCT_ID, 0);
            if(productId!=0)
                getProduct(productId);
            else
                setNewProduct();

        }

        setClicks();

        return mBinding.getRoot();
    }

    private void getProduct(long productId){

        viewModel.getProduct(productId).observe(this, productEntity -> {
            if(productEntity!=null){
                modify=true;
                activeProduct= productEntity;
                oldBankId =productEntity.getBankId();
                int type= activeProduct.getProductType();
                setGeneralTypeSpinner();
                prodType= type%10;
                mBinding.generalTypeSpinner.setSelection(type/10 - 1);
                setProductVisuals();
            }else
                setNewProduct();

        });
    }

    private void setNewProduct(){
        setCurrency(preferences.getLong(getString(R.string.key_default_currency_id_new), 1));
        setNewProductVisual();
    }

    private void setProductVisuals( ){
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        mBinding.setProduct(activeProduct);

        mBinding.closedProduct.setChecked(activeProduct.isInactive());

        mBinding.productBalance.setText(MyApplication.money.format(activeProduct.getBalance()));

        setCurrency(activeProduct.getCurrencyId());
        mBinding.pickCurrency.setEnabled(false);

        if(activeProduct.getBankId()>0) {
            int id = MyApplication.getBankIconId(activeProduct.getBankId());
            if (id != 0)
                Picasso.get().load(id).into(mBinding.pickColor);
        }

        if (activeProduct.getStartDate()!=null)
            mBinding.productStartDate.setText(dateFormatGmt.format(activeProduct.getStartDate()));
        if (activeProduct.getEndDate()!=null)
            mBinding.productEndDate.setText(dateFormatGmt.format(activeProduct.getEndDate()));
    }

    private void setNewProductVisual(){
        mBinding.btnDelete.setVisibility(View.GONE);
        if(activeProduct.getBankId()>0) {
            int id = MyApplication.getBankIconId(activeProduct.getBankId());
            if (id != 0)
                Picasso.get().load(id).into(mBinding.pickColor);
        }
        mBinding.productBalance.setText("0");
        setGeneralTypeSpinner();
    }

    private void setClicks(){
        mBinding.productStartDate.setOnClickListener(v -> showDatePicker(false));
        mBinding.productEndDate.setOnClickListener(v -> showDatePicker(true));

        mBinding.floatingPad.setOnClickListener(v ->
                Objects.requireNonNull(getActivity()).onBackPressed());
        mBinding.btnSave.setOnClickListener(v -> onSave());

        mBinding.btnDelete.setOnClickListener(v -> new AppExecutors().diskIO().execute(() -> {
            if(canChange(activeProduct))
                showDeleteDialog(activeProduct, viewModel);
            else
                showSnackBar(activeProduct, mBinding.getRoot());
        }));

        mBinding.pickColor.setOnClickListener(v -> new PickBankDialog((FloatingActivity) getActivity(), pickBankCallback, false).showDialog());

        mBinding.closedProduct.setOnCheckedChangeListener((buttonView, isChecked) -> {
            activeProduct.setInactive(isChecked);
            if (isChecked)
                mBinding.productEndDateLayout.setVisibility(View.VISIBLE);
            else{
                mBinding.productEndDateLayout.setVisibility(View.GONE);
                activeProduct.setEndDate(null);
            }

        });

        mBinding.pickCurrency.setOnClickListener(v -> {
            PickCurrencyCallback callback = currency -> setCurrency(currency.getId());

            new PickCurrencyDialog((FloatingActivity) getActivity(), callback, true).showDialog();
        });
    }

    private void setCurrency(long currencyId){
        ViewModelProviders.of(this).get(CurrencyViewModel.class).getCurrency(currencyId)
                .observe(this, currencyEntity -> {
                    if(currencyEntity!=null){
                        activeProduct.setCurrencyId(currencyId);
                        mBinding.pickCurrency.setText(currencyEntity.getShortcut());
                    }
                });
    }

    private PickBankCallback pickBankCallback= bank -> {
        activeProduct.setBankId(bank.getId());
        int id = MyApplication.getBankIconId(bank.getId());
        if (id != 0){
            mBinding.pickColor.setBackgroundColor(getResources().getColor(R.color.white));
            Picasso.get().load(id).into(mBinding.pickColor);
        }
    };

    private void showDatePicker(boolean endDateMode){
        Calendar calendar= Calendar.getInstance(TimeZone.getTimeZone("UTC"));


        DatePickerDialog.OnDateSetListener dateSetListener = (view1, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            Date date = calendar.getTime();

            setDates(endDateMode, date);


        };


        new DatePickerDialog(Objects.requireNonNull(getActivity()), dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void setDates(boolean endDateMode, Date date){
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        if (endDateMode){
            if(activeProduct.getStartDate()!=null)
                if (date.before(activeProduct.getStartDate())){
                    mBinding.productEndDateLayout.setError(getString(R.string.error_open_date_after_close_date));
                    date=null;
                }

            activeProduct.setEndDate(date);
            if(date!=null){
                mBinding.productEndDateLayout.setError(null);
                mBinding.productEndDate.setText(dateFormatGmt.format(date));
            }
        } else{
            if(activeProduct.getEndDate()!=null)
                if (date.after(activeProduct.getEndDate())){
                    mBinding.productStartDateLayout.setError(getString(R.string.error_open_date_after_close_date));
                    date=null;
                }


            activeProduct.setStartDate(date);
            if(date!=null){
                mBinding.productStartDateLayout.setError(null);
                mBinding.productStartDate.setText(dateFormatGmt.format(date));
            }
        }
    }

    private void setGeneralTypeSpinner(){
        Spinner spinner= mBinding.generalTypeSpinner;
        ArrayAdapter<CharSequence> adapterDate= ArrayAdapter.
                createFromResource(Objects.requireNonNull(getActivity()),
                        R.array.GeneralProductType, android.R.layout.simple_spinner_item);
        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterDate);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activeProduct.setProductType((position+1)*10);
                setProductTypeSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setProductTypeSpinner(){
        Spinner spinner= mBinding.productTypeSpinner;

        int generalProductType= activeProduct.getProductType()/10;
        @ArrayRes int arrayResId;
        switch (generalProductType){
            case 1:
                arrayResId= R.array.AccountTypes;
                break;
            case 2:
                arrayResId= R.array.CardTypes;
                break;
            case 3:
                arrayResId= R.array.DepositTypes;
                break;
            default:
                arrayResId= R.array.AllProductTypes;

        }
        ArrayAdapter<CharSequence> adapterDate= ArrayAdapter.
                createFromResource(Objects.requireNonNull(getActivity()), arrayResId, android.R.layout.simple_spinner_item);
        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterDate);
        spinner.setSelection(prodType);
        prodType=0;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[]  array= getResources().getStringArray(arrayResId);
                activeProduct.setProductType(generalProductType * 10 + position);
                if (canRewriteName()){
                    mBinding.productTitle.setText(array[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private boolean canRewriteName(){
        return (mBinding.productTitle.getText()!=null
                && mBinding.productTitle.getText().toString().equals(""))
                || titleFromArray();
    }

    private boolean titleFromArray(){
       String[] array= getResources().getStringArray(R.array.AllProductTypes);
        for (String s : array) {
            if (s.equals(Objects.requireNonNull(mBinding.productTitle.getText()).toString()))
                return true;
        }
        return false;
    }

    //SAVING
    private void onSave(){

        //CHECK
        if(mBinding.productTitle.getText()==null || mBinding.productTitle.getText().toString().equals("")){
            mBinding.productTitle.setError(getString(R.string.enter_valid_name));
            return;
        }

        if(mBinding.productBalance.getText()==null || mBinding.productBalance.getText().toString().equals("")){
            mBinding.productBalance.setError(getString(R.string.enter_valid_balance));
            return;
        }

        if(activeProduct.isInactive())
            if (activeProduct.getEndDate()==null){
                mBinding.productEndDate.setError(getString(R.string.pick_date));
                return;
            }

        if(activeProduct.getBankId()==0){
            mBinding.pickColor.setBackgroundColor(getResources().getColor(R.color.red));
            return;
        }

        String balance= mBinding.productBalance.getText().toString().replaceAll("\\s","").replace(",", ".");

        //SET
        activeProduct.setBalance((double) Math.round(Double.parseDouble(balance) * 100) / 100);
        activeProduct.setTitle(mBinding.productTitle.getText().toString());

        if(mBinding.productDesc.getText()!=null)
            activeProduct.setDescription(mBinding.productDesc.getText().toString());


        if(modify) {
            viewModel.update(activeProduct);
            if(activeProduct.getBankId()!= oldBankId){
                ViewModelProviders.of(this).get(TransactionViewModel.class).changeBankId(activeProduct.getId(), activeProduct.getBankId());
                ViewModelProviders.of(this).get(TaskViewModel.class).changeBankId(activeProduct.getId(), activeProduct.getBankId());
            }
        } else{
            ColorGenerator generator= ColorGenerator.MATERIAL;
            int color= generator.getRandomColor();
            activeProduct.setColor(color);
            viewModel.insert(activeProduct);
        }

        Objects.requireNonNull(getActivity()).onBackPressed();
    }


    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_PRODUCT_FRAGMENT;
    }

}
