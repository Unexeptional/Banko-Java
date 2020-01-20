package com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased.transaction;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.db.entity.ProductEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.TransactionEntity;
import com.unexceptional.beast.banko.newVersion.ui.fragment.BasicFragment;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CurrencyViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.TransactionViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

//takes care of stuff for both sub and main transaction methods
public abstract class AbstractTransactionFragment extends BasicFragment {

    ViewDataBinding abstractBinding;
    TransactionViewModel viewModel;
    TransactionEntity activeTransaction, oldTransaction;
    ProductEntity fromProduct, toProduct;
    double fromRate, toRate, mainRate;
    private TextView amountText;
    private InterstitialAd mInterstitialAd;
    DecimalFormat moneyFormat= MyApplication.money;

    AbstractClicks abstractClicks= new AbstractClicks() {
        @Override
        public void numberClick(String character) {
            String text= amountText.getText().toString();
            if(text.equals("0") || text.equals("0,00") || text.equals("0.00"))
                amountText.setText(character);
            else
                amountText.setText(String.format("%s%s", text, character));
        }

        @Override
        public void actionClick(String character) {
            String text= amountText.getText().toString();

            if (isAction(text)) {
                if(calculate(text))
                    amountText.setText(String.format("%s%s", amountText.getText().toString(), character));
            } else
                amountText.setText(String.format("%s%s", text, character));

        }

        @Override
        public void buttonC() {
            String text= amountText.getText().toString();
            if(text.length()>0 && !text.equals("0")){
                amountText.setText(text.substring(0, text.length() - 1));
            } else
                amountText.setText("0");
        }

        @Override
        public void buttonDot() {
            String text= amountText.getText().toString();

            char i = DecimalFormatSymbols.getInstance().getDecimalSeparator();

            if(text.indexOf(i)==-1 ||
                    text.lastIndexOf(i)< text.indexOf("*")
                            || text.lastIndexOf(i)< text.indexOf("/")
                            || text.lastIndexOf(i)< text.indexOf("-")
                            || text.lastIndexOf(i)< text.indexOf("+") )
                amountText.setText(String.format("%s%s", amountText.getText(), i));
        }

        @Override
        public void button0() {
            String text= amountText.getText().toString();
            if(text.length()>0 && !text.equals("0")){
                amountText.setText(String.format("%s%s", text, "0"));
            } else
                amountText.setText("0");
        }

        @Override
        public void calculateAmount() {
            calculate(amountText.getText().toString().replaceAll("\\s",""));
        }

        @Override
        public boolean clearAmount(View v) {
            amountText.setText("0");
            return true;
        }

        @Override
        public boolean copyNumber(View v, boolean from) {
            if (from) {
                if (fromProduct != null)
                    copyToClipboard(fromProduct.getDescription());

            }else {
                if (toProduct!=null)
                    copyToClipboard(toProduct.getDescription());

            }

            return true;
        }

    };

    private void copyToClipboard(String text){
        ClipboardManager cm = (ClipboardManager) MyApplication.getContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData myClip;
            myClip = ClipData.newPlainText("text", text);
            cm.setPrimaryClip(myClip);
            Toast.makeText(getActivity(), R.string.warning_copied_to_clipboard, Toast.LENGTH_SHORT).show();
        }
    }

    abstract @LayoutRes int provideActivityLayout();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);

        MobileAds.initialize(getActivity(),
                "ca-app-pub-1567773436889509~1587460751");

        mInterstitialAd = new InterstitialAd(Objects.requireNonNull(getActivity()));
        mInterstitialAd.setAdUnitId("ca-app-pub-1567773436889509/1205468095");
        mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        abstractBinding= DataBindingUtil.inflate(inflater, provideActivityLayout(), container, false);

        return abstractBinding.getRoot();
    }

    void setAmounts(TextView amountText){
        calculate(amountText.getText().toString());

        //from string
        String amountString= amountText.getText().toString().replaceAll("\\s","").replace(",", ".");
        double amount= (double) Math.round(Double.parseDouble(amountString) * 100) / 100;
        activeTransaction.setAmount(amount);

        //get amount in default currency
        long defaultCurr= preferences.getLong(getString(R.string.key_default_currency_id_new),1);
        if(defaultCurr!=activeTransaction.getCurrencyId())
            amount= amount*mainRate;

        //for products
        if(activeTransaction.getFromProductId()!=0)
            activeTransaction.setFromAmount(amount/fromRate);
        else
            activeTransaction.setFromAmount(0);

        if (activeTransaction.getToProductId()!=0)
            activeTransaction.setToAmount(amount/toRate);
        else
            activeTransaction.setToAmount(0);
    }

    @SuppressLint("ClickableViewAccessibility")
    void setCalcObserver(TextView amountText, ImageButton equals, ImageButton save){
        this.amountText= amountText;

        amountText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text= s.toString();
                if(isAction(text)) {
                    equals.setVisibility(View.VISIBLE);
                    save.setVisibility(View.GONE);
                }else {
                    equals.setVisibility(View.GONE);
                    save.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        amountText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(amountText.getCompoundDrawables()[DRAWABLE_RIGHT]!=null)
                    if(event.getRawX() >= (amountText.getRight() - 50 -amountText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()))
                        amountText.setText("0");

            }
            return false;
        });
    }

    private boolean isAction(String text){
        return text.contains("*") || text.contains("/") || text.contains("+") || text.contains("-");
    }

    private boolean calculate(String text){
        text= text.replace(",", ".");

        double first;
        double second;
        double equals=0;
        int index;
        String character;

        if(text.contains("-"))
            character="-";
        else if (text.contains("+"))
            character="+";
        else if (text.contains("*"))
            character="*";
        else if(text.contains("/"))
            character="/";
        else
            return false;



        index= text.indexOf(character);

        try {
            first= Double.parseDouble(text.substring(0, index));
        }catch (Exception e){
            first=0;
        }

        try {
            second= Double.parseDouble(text.substring(index+1));
        }catch (Exception e){
            second=0;
        }

        switch (character){
            case "-":
                equals=first-second;
                break;
            case "+":
                equals=first+second;
                break;
            case "/":
                if(second!=0)
                    equals=first/second;
                break;
            case "*":
                equals=first*second;
                break;
        }
        if(equals<0)
            equals=0;

        amountText.setText(moneyFormat.format(equals));
        return true;
    }

    void showSnackBar(String text, String buttonText){
        final Snackbar snackbar = Snackbar
                .make(abstractBinding.getRoot() , text, Snackbar.LENGTH_LONG);
        if(buttonText!=null)
            snackbar.setAction(buttonText, v -> {
            });

        snackbar.show();
    }

/*
    private void showChangeRateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        View dialogView = View.inflate(getActivity(), R.layout.dialog_edit_text, null);
        builder.setView(dialogView);

        final TextInputEditText editText =  dialogView.findViewById(R.id.dialog_edit_text);
        Button btnAccept =  dialogView.findViewById(R.id.btn_accept);
        Button btnDecline =  dialogView.findViewById(R.id.btn_decline);

        final AlertDialog dialog = builder.create();

        editText.setText(moneyFormat.format(mainRate));

        btnAccept.setOnClickListener(v -> {
            String rate= Objects.requireNonNull(editText.getText()).toString().replace(",", ".");
            mainRate= Double.parseDouble(rate);
            dialog.dismiss();
        });

        btnDecline.setOnClickListener(v -> dialog.dismiss());

        dialog.show();{

        }
    }
*/


    void getExchangeRate(long currencyId, boolean from){
        ViewModelProviders.of(this).get(CurrencyViewModel.class).getCurrency(currencyId)
                .observe(this, currencyEntity -> {
                    if(from)
                        fromRate = currencyEntity.getExchangeRate();
                    else
                        toRate= currencyEntity.getExchangeRate();
                });

    }

    //full ad
    private boolean fullAdTimeout() {
        return System.currentTimeMillis() - preferences.getLong(getString(R.string.key_full_ad_display_time), 0L)
                > Constants.FULL_AD_VALID_TIMEOUT;

    }

    private boolean adsDisabledTimeout() {
        return System.currentTimeMillis() - preferences.getLong(getString(R.string.key_ads_disabled_timeout), 0L)
                > 0;
    }

    void showFullAd() {
        if(fullAdTimeout())
            if (adsDisabledTimeout())
                if(mInterstitialAd.isLoaded()){
                    mInterstitialAd.show();
                    preferences.edit().putLong(getString(R.string.key_full_ad_display_time), System.currentTimeMillis()).apply();
                }
    }

    public interface AbstractClicks{
        void numberClick(String character);
        void actionClick(String character);
        void buttonC();
        void buttonDot();
        void button0();
        void calculateAmount();
        boolean clearAmount(View v);
        boolean copyNumber(View v, boolean from);
    }
}
