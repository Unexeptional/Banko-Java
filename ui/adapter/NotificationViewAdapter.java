package com.unexceptional.beast.banko.newVersion.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.BankItemBinding;
import com.unexceptional.beast.banko.databinding.NotificationItemBinding;
import com.unexceptional.beast.banko.newVersion.callback.BankClickCallback;
import com.unexceptional.beast.banko.newVersion.callback.NotificationClickCallback;
import com.unexceptional.beast.banko.newVersion.db.model.Bank;
import com.unexceptional.beast.banko.newVersion.db.model.Notification;
import com.unexceptional.beast.banko.other.MyApplication;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Bank} and makes a call to the
 * specified {@link NotificationClickCallback}.
 */
public class NotificationViewAdapter extends RecyclerView.Adapter<NotificationViewAdapter.NotificationViewHolder> {

    private List<? extends Notification> mNotificationList;

    @Nullable
    private final NotificationClickCallback mNotificationClickCallback;

    public NotificationViewAdapter(@Nullable NotificationClickCallback clickCallback) {
        mNotificationClickCallback = clickCallback;
        setHasStableIds(true);
    }

    public void setNotificationList(final List<? extends Notification> notificationList) {
        if (mNotificationList == null) {
            mNotificationList = notificationList;
            notifyItemRangeInserted(0, notificationList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mNotificationList.size();
                }

                @Override
                public int getNewListSize() {
                    return notificationList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mNotificationList.get(oldItemPosition).getId() ==
                            notificationList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Notification newNotification = notificationList.get(newItemPosition);
                    Notification oldNotification = mNotificationList.get(oldItemPosition);
                    return newNotification.getId() == oldNotification.getId()
                            && Objects.equals(newNotification.isDeleted(), oldNotification.isDeleted()
                            && Objects.equals(newNotification.getTitle(), oldNotification.getTitle()));
                }
            });
            mNotificationList = notificationList;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        NotificationItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.notification_item,
                        parent, false);
        binding.setCallback(mNotificationClickCallback);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification= mNotificationList.get(position);
        holder.binding.setNotification(notification);
        holder.binding.executePendingBindings();

        int id= MyApplication.getDrawableId(notification.getIconName());
        if(id!=0)
            Picasso.get().load(id).into(holder.binding.notificationItemIcon);
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        holder.binding.date.setText(dateFormatGmt.format(notification.getDate()));
    }


    @Override
    public int getItemCount() {
        return mNotificationList == null ? 0 : mNotificationList.size();
    }

    @Override
    public long getItemId(int position) {
        return mNotificationList.get(position).getId();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        final NotificationItemBinding binding;

        NotificationViewHolder(NotificationItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
