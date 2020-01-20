package com.unexceptional.beast.banko.newVersion.ui.adapter;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.TransactionItemBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.TransactionClickCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.TransactionEntity;
import com.unexceptional.beast.banko.newVersion.db.model.Transaction;
import com.unexceptional.beast.banko.other.MyApplication;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Transaction} and makes a call to the
 * specified {@link TransactionClickCallback}.
 */
public class TransactionViewAdapter extends RecyclerView.Adapter<TransactionViewAdapter.TransactionViewHolder> {

    private List<? extends Transaction> mTransactionList;
    private SharedPreferences preferences;

    @Nullable
    private final TransactionClickCallback mTransactionClickCallback;

    public TransactionViewAdapter(@Nullable TransactionClickCallback clickCallback) {
        mTransactionClickCallback = clickCallback;
        preferences= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        setHasStableIds(true);
    }

    public void setTransactionList(final List<? extends Transaction> productList) {
        if (mTransactionList == null) {
            mTransactionList = productList;
            notifyItemRangeInserted(0, productList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mTransactionList.size();
                }

                @Override
                public int getNewListSize() {
                    return productList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mTransactionList.get(oldItemPosition).getId() ==
                            productList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Transaction newTransaction = productList.get(newItemPosition);
                    Transaction oldTransaction = mTransactionList.get(oldItemPosition);
                    return newTransaction.getId() == oldTransaction.getId()
                            && Objects.equals(newTransaction.getDescription(), oldTransaction.getDescription())
                            && Objects.equals(newTransaction.getAmount(), oldTransaction.getAmount())
                            && Objects.equals(newTransaction.getCategoryId(), oldTransaction.getCategoryId())
                            && Objects.equals(newTransaction.getCurrencyId(), oldTransaction.getCurrencyId())
                            && Objects.equals(newTransaction.getDate(), oldTransaction.getDate())
                            && Objects.equals(newTransaction.getIconName(), oldTransaction.getIconName())
                            && Objects.equals(newTransaction.getSubCategoryId(), oldTransaction.getSubCategoryId())
                            && Objects.equals(newTransaction.getFromBankId(), oldTransaction.getFromBankId())
                            && Objects.equals(newTransaction.getFromProductId(), oldTransaction.getFromProductId())
                            && Objects.equals(newTransaction.getToBankId(), oldTransaction.getToBankId())
                            && Objects.equals(newTransaction.getToProductId(), oldTransaction.getToProductId())
                            && Objects.equals(newTransaction.getRepeatTimes(), oldTransaction.getRepeatTimes())
                            && Objects.equals(newTransaction.getRepeatDateOption(), oldTransaction.getRepeatDateOption())
                            && Objects.equals(newTransaction.getParentTransactionId(), oldTransaction.getParentTransactionId())
                            && Objects.equals(newTransaction.getKidsAmount(), oldTransaction.getKidsAmount())
                            && Objects.equals(newTransaction.getTransactionType(), oldTransaction.getTransactionType());
                }
            });
            mTransactionList = productList;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TransactionItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.transaction_item,
                        parent, false);
        binding.setCallback(mTransactionClickCallback);
        return new TransactionViewHolder(binding);
    }


    @Override
    public int getItemCount() {
        return mTransactionList == null ? 0 : mTransactionList.size();
    }

    @Override
    public long getItemId(int position) {
        return mTransactionList.get(position).getId();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {

        final TransactionItemBinding binding;

        TransactionViewHolder(TransactionItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    //MY STUFF
    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {

        Transaction transaction= mTransactionList.get(position);
        holder.binding.setTransaction(transaction);
        holder.binding.executePendingBindings();

        setDates(transaction, holder.binding);
        setAmount(transaction, holder.binding);


        if (transaction.getTransactionType()== Constants.TRANSACTION_TYPE_TRANSFER)
           setTransfer(transaction, holder.binding);
        else
            setExpenseIncome(transaction, holder.binding);
    }

    private void setExpenseIncome(Transaction transaction, TransactionItemBinding binding){
        int drawableId;

        if(preferences.getBoolean(MyApplication.getContext().getString(R.string.key_tr_list_group_transactions), true)){
            if(transaction.getKidsAmount() >0){
                binding.title.setText(MyApplication.getContext().getString(R.string.few_categories));
                if(transaction.getTransactionType()== Constants.TRANSACTION_TYPE_EXPENSE )
                    drawableId= R.drawable.expense_icon;
                else
                    drawableId= R.drawable.income_icon;
            }else {
                setTitle(transaction, binding);

                drawableId= MyApplication.getDrawableId(transaction.getIconName());
            }
        }else {
           setTitle(transaction, binding);

            drawableId= MyApplication.getDrawableId(transaction.getIconName());
        }

        if(drawableId!=0)
            Picasso.get().load(drawableId).into(binding.transactionIcon);

    }

    private void setTitle(Transaction transaction, TransactionItemBinding binding){
        if(transaction.getSubCategoryName()!=null && !transaction.getSubCategoryName().equals(""))
            binding.title.setText(String.format("%s (%s)", transaction.getCategoryName(), transaction.getSubCategoryName()));
        else
            binding.title.setText(transaction.getCategoryName());
    }

    private void setTransfer(Transaction transaction, TransactionItemBinding binding){
        if(transaction.getSubCategoryName()!=null && !transaction.getSubCategoryName().equals(""))
            binding.title.setText(String.format("%s (%s)", transaction.getCategoryName(), transaction.getSubCategoryName()));
        else if (!transaction.getCategoryName().equals(""))
            binding.title.setText(transaction.getCategoryName());
        else
            binding.title.setText(MyApplication.getContext().getString(R.string.transfer));

        int id= MyApplication.getBankIconId(transaction.getFromBankId());
        if(id!=0)
            Picasso.get().load(id).into(binding.transactionIcon);
    }

    private void setAmount(Transaction transaction, TransactionItemBinding binding){

        binding.amount.setText(String.format("%s %s", MyApplication.money.format(transaction.getAmount()), transaction.getCurrencySymbol()));

        if(transaction.getTransactionType()==1)
            binding.amount.setTextColor(Color.RED);
        else if (transaction.getTransactionType()==2)
            binding.amount.setTextColor(Color.parseColor("#32CD32"));
        else
            binding.amount.setTextColor(Color.GRAY);
    }

    private void setDates(Transaction transaction, TransactionItemBinding binding){
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        if(transaction.getDate()!=null) {
            binding.transCreatedAt.setText(dateFormatGmt.format(transaction.getDate()));
            if (Calendar.getInstance().getTime().after(transaction.getDate()))
                binding.plannedView.setVisibility(View.GONE);
            else
                binding.plannedView.setVisibility(View.VISIBLE);
        }

    }
}
