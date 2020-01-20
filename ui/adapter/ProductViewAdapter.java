package com.unexceptional.beast.banko.newVersion.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.amulyakhare.textdrawable.TextDrawable;
import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.ProductItemBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.ProductClickCallback;
import com.unexceptional.beast.banko.newVersion.db.model.Bank;
import com.unexceptional.beast.banko.newVersion.db.model.Product;
import com.unexceptional.beast.banko.other.MyApplication;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.ColumnInfo;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Product} and makes a call to the
 * specified {@link ProductClickCallback}.
 */
public class ProductViewAdapter extends RecyclerView.Adapter<ProductViewAdapter.ProductViewHolder> {

    private List<? extends Product> mProductList;

    @Nullable
    private final ProductClickCallback mProductClickCallback;

    public ProductViewAdapter(@Nullable ProductClickCallback clickCallback) {
        mProductClickCallback = clickCallback;
        setHasStableIds(true);
    }

    public void setProductList(final List<? extends Product> productList) {
        if (mProductList == null) {
            mProductList = productList;
            notifyItemRangeInserted(0, productList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mProductList.size();
                }

                @Override
                public int getNewListSize() {
                    return productList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mProductList.get(oldItemPosition).getId() ==
                            productList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Product newProduct = productList.get(newItemPosition);
                    Product oldProduct = mProductList.get(oldItemPosition);
                    return newProduct.getId() == oldProduct.getId()
                            && Objects.equals(newProduct.getDescription(), oldProduct.getDescription())
                            && Objects.equals(newProduct.getBankId(), oldProduct.getBankId())
                            && Objects.equals(newProduct.getBalance(), oldProduct.getBalance())
                            && Objects.equals(newProduct.getStartDate(), oldProduct.getStartDate())
                            && Objects.equals(newProduct.getEndDate(), oldProduct.getEndDate())
                            && Objects.equals(newProduct.getProductType(), oldProduct.getProductType())
                            && Objects.equals(newProduct.isInactive(), oldProduct.isInactive())
                            && Objects.equals(newProduct.getTitle(), oldProduct.getTitle());
                }
            });
            mProductList = productList;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ProductItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.product_item,
                        parent, false);
        binding.setCallback(mProductClickCallback);
        return new ProductViewHolder(binding);
    }


    @Override
    public int getItemCount() {
        return mProductList == null ? 0 : mProductList.size();
    }

    @Override
    public long getItemId(int position) {
        return mProductList.get(position).getId();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        final ProductItemBinding binding;

        ProductViewHolder(ProductItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    //MY STUFF
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {

        Product product= mProductList.get(position);
        holder.binding.setProduct(product);
        holder.binding.executePendingBindings();

        setDates(product, holder.binding);
        setIcon(product, holder.binding);


        int id2= getTypeIcon(product.getProductType());
        if(id2!=0)
            Picasso.get().load(id2).into(holder.binding.typeIcon);

        if(product.getBalance()<0d)
            holder.binding.balance.setTextColor(Color.RED);
        else
            holder.binding.balance.setTextColor(Color.parseColor("#32CD32"));

        holder.binding.balance.setText(MyApplication.money.format(product.getBalance()));
    }

    private void setDates(Product product, ProductItemBinding binding){
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        if(product.getStartDate()!=null)
            binding.prodStartDate.setText(dateFormatGmt.format(product.getStartDate()));

        if(product.getEndDate()!=null)
            binding.prodEndDate.setText(dateFormatGmt.format(product.getEndDate()));

    }

    private static int getTypeIcon(int productType){
        String iconName= "settings_" + String.valueOf(productType);

        if (iconName.length() > 0 && iconName.charAt(iconName.length() - 1) == 'x') {
            iconName = iconName.substring(0, iconName.length() - 1);
        }
        return MyApplication.getDrawableId(iconName);

    }

    private void setIcon(Product product, ProductItemBinding binding){
        Context context= MyApplication.getContext();

        if (product.getBankId()>=100 || PreferenceManager.getDefaultSharedPreferences(context).
                getBoolean(context.getString(R.string.key_show_bank_list), false)){
            if(!product.getTitle().equals("")) {
                TextDrawable myDrawable = TextDrawable.builder().beginConfig()
                        .textColor(Color.WHITE)
                        .useFont(Typeface.DEFAULT)
                        .toUpperCase()
                        .endConfig()
                        .buildRound(product.getTitle().substring(0, 1), product.getColor());
                binding.bankItemIcon.setImageDrawable(myDrawable);
            }
        }else {
            int id= MyApplication.getBankIconId(product.getBankId());
            if(id!=0)
                Picasso.get().load(id).into(binding.bankItemIcon);
        }

    }
}
