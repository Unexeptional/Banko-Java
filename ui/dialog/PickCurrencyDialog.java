package com.unexceptional.beast.banko.newVersion.ui.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.PickCurrencyCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.CurrencyEntity;
import com.unexceptional.beast.banko.newVersion.db.model.Currency;
import com.unexceptional.beast.banko.newVersion.networking.CurrencyDownload.CurrencyExchangeService;
import com.unexceptional.beast.banko.newVersion.networking.CurrencyDownload.CurrencyExchangeUpdate;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CurrencyViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PickCurrencyDialog {

    private AppCompatActivity activity;
    private AlertDialog dialog;
    private PickCurrencyCallback callback;
    private SharedPreferences preferences;
    private boolean update;
    private List<? extends Currency> currencyList;

    public PickCurrencyDialog(AppCompatActivity activity, PickCurrencyCallback callback, boolean update) {
        this.activity = activity;
        this.callback = callback;
        this.preferences= PreferenceManager.getDefaultSharedPreferences(activity);
        this.update=update;
    }

    public PickCurrencyDialog(AppCompatActivity activity){
        this.activity= activity;
        this.preferences= PreferenceManager.getDefaultSharedPreferences(activity);
    }

    public void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View dialogView = View.inflate(activity, R.layout.dialog_pick_currency, null);
        builder.setView(dialogView);

        //bind views
        RecyclerView recyclerView= dialogView.findViewById(R.id.pick_single);
        ProgressBar prograssBar= dialogView.findViewById(R.id.progressBar_cyclic);
        EditText editText= dialogView.findViewById(R.id.search);
        //setRecycler
        PickCurrencyViewAdapter adapter= new PickCurrencyViewAdapter(callback);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 3));

        LiveData<List<CurrencyEntity>> liveData;
        CurrencyViewModel currencyViewModel= ViewModelProviders.of(activity).get(CurrencyViewModel.class);
        liveData= currencyViewModel.getAllCurrencies();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (!text.equals("")){
                    List<Currency> currencies= new ArrayList<>();
                    for (Currency entity: currencyList){
                        String name;
                        try {
                            name = java.util.Currency.getInstance(entity.getShortcut()).getDisplayName();
                        }catch ( Exception e){
                            name="";
                        }

                        if(entity.getShortcut().contains(text) || name.contains(text.toLowerCase()))
                            currencies.add(entity);
                    }
                    adapter.setCurrencyList(currencies);
                    adapter.notifyDataSetChanged();
                }else
                    adapter.setCurrencyList(currencyList);
            }
        });




        liveData.observe(activity, currencies -> {
            if (currencies != null) {
                if (update && !areCurrenciesValid()) {
                    prograssBar.setVisibility(View.VISIBLE);
                    editText.setVisibility(View.GONE);
                    updateRates(currencies, currencyViewModel);
                } else {
                    prograssBar.setVisibility(View.GONE);
                    editText.setVisibility(View.VISIBLE);
                    currencyList = currencies;
                    adapter.setCurrencyList(currencies);
                }
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    public void triggerUpdate(){
        LiveData<List<CurrencyEntity>> liveData;
        CurrencyViewModel currencyViewModel= ViewModelProviders.of(activity).get(CurrencyViewModel.class);
        liveData= currencyViewModel.getAllCurrencies();

        liveData.observe(activity, currencies -> {
            if (currencies != null) {
                if ( !areCurrenciesValid())
                    updateRates(currencies, currencyViewModel);

            }
        });
    }

    private void updateRates(List<CurrencyEntity> currencyEntities, CurrencyViewModel currencyViewModel){
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(chain -> {
            Request newRequest  = chain.request().newBuilder()
                    .build();
            return chain.proceed(newRequest);
        }).build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://data.fixer.io/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();



        CurrencyExchangeService service = retrofit.create(CurrencyExchangeService.class);
            Call<CurrencyExchangeUpdate> call = service.loadCurrencyExchange();
            call.enqueue(new Callback<CurrencyExchangeUpdate>() {
                @Override
                public void onResponse(@NonNull Call<CurrencyExchangeUpdate> call, @NonNull Response<CurrencyExchangeUpdate> response) {
                    if(response.body()!=null){
                        Toast.makeText(activity, R.string.warning_currencies_updated_success, Toast.LENGTH_LONG).show();
                        CurrencyExchangeUpdate currencyExchange = response.body();
                        preferences.edit().putLong(activity.getString(R.string.key_currencies_update_time), System.currentTimeMillis()).apply();
                        currencyExchange.updateCurrencies(currencyEntities, currencyViewModel);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CurrencyExchangeUpdate> call, @NonNull Throwable t) {
                    Toast.makeText(activity, R.string.warning_currency_update_fail, Toast.LENGTH_LONG).show();
                }
            });
    }

    private boolean areCurrenciesValid() {
        if (isOnline())
            return System.currentTimeMillis() - preferences.getLong(activity.getString(R.string.key_currencies_update_time), 0L)
                < Constants.CURRENCIES_VALID_TIMEOUT;
        else{
            Toast.makeText(activity, R.string.warning_currency_update_fail, Toast.LENGTH_LONG).show();
            return true;
        }

    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void hideDialog(){
        dialog.dismiss();
    }

    private class PickCurrencyViewAdapter extends PickAdapterIconTitle {

        private List<? extends Currency> mCurrencyList;

        @Nullable
        private final PickCurrencyCallback mPickCurrencyCallback;

        PickCurrencyViewAdapter(@Nullable PickCurrencyCallback clickCallback) {
            mPickCurrencyCallback = clickCallback;
            setHasStableIds(true);
        }

        void setCurrencyList(final List<? extends Currency> currencyList) {
            if (mCurrencyList == null) {
                mCurrencyList = currencyList;
                notifyItemRangeInserted(0, currencyList.size());
            } else {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return mCurrencyList.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return currencyList.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return mCurrencyList.get(oldItemPosition).getId() ==
                                currencyList.get(newItemPosition).getId();
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        Currency newCurrency = currencyList.get(newItemPosition);
                        Currency oldCurrency = mCurrencyList.get(oldItemPosition);
                        return newCurrency.getId() == oldCurrency.getId()
                                && newCurrency.getShortcut().equals(oldCurrency.getShortcut())
                                && Objects.equals(newCurrency.getTitle(), oldCurrency.getTitle());
                    }
                });
                mCurrencyList = currencyList;
                result.dispatchUpdatesTo(this);
            }
        }

        @Override
        public int getItemCount() {
            return mCurrencyList == null ? 0 : mCurrencyList.size();
        }

        @Override
        public long getItemId(int position) {
            return mCurrencyList.get(position).getId();
        }

        @Override
        public void onBindViewHolder(@NonNull IconTitleViewHolder holder, int position) {
            Currency currency= mCurrencyList.get(position);

            try {
                holder.binding.title.setText(java.util.Currency.getInstance(currency.getShortcut()).getDisplayName());
            }catch ( Exception e){
                holder.binding.title.setText("");
            }


            TextDrawable myDrawable = TextDrawable.builder().beginConfig()
                    .textColor(Color.WHITE)
                    .useFont(Typeface.DEFAULT)
                    .fontSize(50)
                    .toUpperCase()
                    .endConfig()
                    .buildRound(currency.getShortcut(), Color.GRAY);
            holder.binding.pickIcon.setImageDrawable(myDrawable);

            if(mPickCurrencyCallback!=null) {
                holder.binding.pickIcon.setOnClickListener(v -> {
                    mPickCurrencyCallback.onCurrencyItemPick(mCurrencyList.get(position));
                    dialog.dismiss();
                });
            }
        }
    }
}