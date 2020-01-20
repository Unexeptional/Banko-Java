package com.unexceptional.beast.banko.newVersion.ui.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.CurrencyItemBinding;
import com.unexceptional.beast.banko.newVersion.callback.NotificationClickCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.CurrencyEntity;
import com.unexceptional.beast.banko.newVersion.db.model.Bank;
import com.unexceptional.beast.banko.newVersion.db.model.Currency;
import com.unexceptional.beast.banko.other.MyApplication;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Bank} and makes a call to the
 * specified {@link NotificationClickCallback}.
 */
public class CurrencyViewAdapter extends RecyclerView.Adapter<CurrencyViewAdapter.NotificationViewHolder> {

    private List<? extends Currency> mCurrencyList;

    public CurrencyViewAdapter() {
        setHasStableIds(true);
    }

    public void setNotificationList(final List<? extends Currency> notificationList) {
        if (mCurrencyList == null) {
            mCurrencyList = notificationList;
            notifyItemRangeInserted(0, notificationList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mCurrencyList.size();
                }

                @Override
                public int getNewListSize() {
                    return notificationList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mCurrencyList.get(oldItemPosition).getId() ==
                            notificationList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Currency newNotification = notificationList.get(newItemPosition);
                    Currency oldNotification = mCurrencyList.get(oldItemPosition);
                    return newNotification.getId() == oldNotification.getId()
                            && newNotification.getExchangeRate() == oldNotification.getExchangeRate()
                            && Objects.equals(newNotification.getTitle(), oldNotification.getTitle());
                }
            });
            mCurrencyList = notificationList;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CurrencyItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.currency_item,
                        parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Currency currency= mCurrencyList.get(position);
        holder.binding.setCurrency(currency);
        holder.binding.executePendingBindings();

        try {
            holder.binding.name.setText(java.util.Currency.getInstance(currency.getShortcut()).getDisplayName());
        }catch ( Exception e){
            holder.binding.name.setText("");
        }

        TextDrawable myDrawable = TextDrawable.builder().beginConfig()
                .textColor(Color.WHITE)
                .useFont(Typeface.DEFAULT)
                .fontSize(50)
                .toUpperCase()
                .endConfig()
                .buildRound(currency.getShortcut(), Color.GRAY);
        holder.binding.notificationItemIcon.setImageDrawable(myDrawable);

        holder.binding.date.setText(MyApplication.currencyRate.format(currency.getExchangeRate()));
    }


    @Override
    public int getItemCount() {
        return mCurrencyList == null ? 0 : mCurrencyList.size();
    }

    @Override
    public long getItemId(int position) {
        return mCurrencyList.get(position).getId();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        final CurrencyItemBinding binding;

        NotificationViewHolder(CurrencyItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
