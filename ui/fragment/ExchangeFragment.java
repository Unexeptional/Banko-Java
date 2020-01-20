package com.unexceptional.beast.banko.newVersion.ui.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.amulyakhare.textdrawable.TextDrawable;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.ExchangeFragmentBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.PickCurrencyCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.CurrencyEntity;
import com.unexceptional.beast.banko.newVersion.ui.activity.FloatingActivity;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickCurrencyDialog;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CurrencyViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link ExchangeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExchangeFragment extends BasicFragment {

    private ExchangeFragmentBinding mBinding;
    private CurrencyViewModel viewModel;
    private CurrencyEntity fromCurr, toCurr, defaultCurr;

    public ExchangeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static ExchangeFragment newInstance() {
        ExchangeFragment fragment = new ExchangeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(CurrencyViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.exchange_fragment, container, false);

        long defaultCurrId = preferences.getLong(getString(R.string.key_default_currency_id_new), 1);

        viewModel.getCurrency(defaultCurrId)
                .observe(this, currencyEntity -> {
                    if(currencyEntity!=null){
                        defaultCurr= currencyEntity;
                    }
                });

        setCurrency(preferences.getLong(getString(R.string.key_exchange_from_currency_id), defaultCurrId), true);
        setCurrency(preferences.getLong(getString(R.string.key_exchange_to_currency_id), defaultCurrId), false);

        setClicks();

        new PickCurrencyDialog((FloatingActivity) getActivity()).triggerUpdate();

        return mBinding.getRoot();
    }


    private void currencyPick(boolean from) {
        PickCurrencyCallback callback= currency -> setCurrency(currency.getId(), from);

        new PickCurrencyDialog((FloatingActivity) getActivity(), callback, true).showDialog();
    }

    private void setCurrency(long currencyId, boolean from){
        if (from)
            viewModel.getCurrency(currencyId)
                    .observe(this, currencyEntity -> {
                        if(currencyEntity!=null){
                            fromCurr = currencyEntity;
                            preferences.edit().putLong(getString(R.string.key_exchange_from_currency_id), currencyEntity.getId()).apply();
                            setCurrencyVisual(true);
                            calculateToValue();
                        }else
                            fromCurr = null;
                    });
        else
            viewModel.getCurrency(currencyId)
                    .observe(this, currencyEntity -> {
                        if(currencyEntity!=null){
                            toCurr = currencyEntity;
                            preferences.edit().putLong(getString(R.string.key_exchange_to_currency_id), currencyEntity.getId()).apply();
                            setCurrencyVisual(false);
                            calculateToValue();
                        }else
                            toCurr =null;
                    });

    }

    private void setCurrencyVisual(boolean from){
        if (from){
            TextDrawable myDrawable = TextDrawable.builder().beginConfig()
                    .textColor(Color.WHITE)
                    .fontSize(50)
                    .useFont(Typeface.SANS_SERIF)
                    .toUpperCase()
                    .endConfig()
                    .buildRound(fromCurr.getShortcut(), Color.GRAY);

            mBinding.fromBank.setImageDrawable(myDrawable);
        }else {
            TextDrawable myDrawable = TextDrawable.builder().beginConfig()
                    .textColor(Color.WHITE)
                    .fontSize(50)
                    .useFont(Typeface.DEFAULT)
                    .toUpperCase()
                    .endConfig()
                    .buildRound(toCurr.getShortcut(), Color.GRAY);

            mBinding.toBank.setImageDrawable(myDrawable);
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private void setClicks(){
        mBinding.floatingPad.setOnClickListener(v ->
                Objects.requireNonNull(getActivity()).onBackPressed());

        mBinding.fromBank.setOnClickListener(v -> currencyPick(true));
        mBinding.toBank.setOnClickListener(v -> currencyPick(false));
        mBinding.swapBanks.setOnClickListener(v -> {
            CurrencyEntity tempCurr= fromCurr;
            String tempValue= mBinding.fromValue.getText().toString();
            mBinding.fromValue.setText(mBinding.toValue.getText().toString());
            mBinding.toValue.setText(tempValue);
            setCurrency(toCurr.getId(), true);
            setCurrency(tempCurr.getId(), false);
        });
       /* mBinding.equals.setOnClickListener(v ->{

        });*/

        mBinding.fromValue.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mBinding.fromValue.setCompoundDrawablesWithIntrinsicBounds(0,
                        0, R.drawable.outline_clear_black_24,0);

               calculateToValue();

            }
        });



          mBinding.fromValue.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if(event.getAction() == MotionEvent.ACTION_UP) {

                if(    mBinding.fromValue.getCompoundDrawables()[DRAWABLE_RIGHT]!=null)
                    if(event.getRawX() >= (    mBinding.fromValue.getRight() - 50 -    mBinding.fromValue.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                          mBinding.fromValue.setText("");
                    }
            }
            return false;
        });

    }
    private void calculateToValue(){
        if (fromCurr!=null && toCurr!=null){
            String amountString= mBinding.fromValue.getText().toString().replace(",", ".");
            if (amountString.equals(""))
                amountString="0";

            double amount= Double.parseDouble(amountString);
            double rate= fromCurr.getExchangeRate()/toCurr.getExchangeRate();
            mBinding.rate.setText(String.format("1 %s= %s %s", fromCurr.getShortcut(), String.valueOf(rate), toCurr.getShortcut()));
            rate= Math.round(rate*10000d)/10000d;

            mBinding.toValue.setText(String.valueOf(amount*rate));

        }

    }



    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_EXCHANGE_FRAGMENT;
    }

}
