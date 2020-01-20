package com.unexceptional.beast.banko.newVersion.ui.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

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
import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.PickIconTitleItemBinding;
import com.unexceptional.beast.banko.databinding.ProductItemBinding;
import com.unexceptional.beast.banko.newVersion.callback.PickProductCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.ProductEntity;
import com.unexceptional.beast.banko.newVersion.db.model.Product;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.ProductListViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.List;
import java.util.Objects;

public class PickProductDialog {

    private AppCompatActivity activity;
    private AlertDialog dialog;
    private PickProductCallback callback;
    private long bankId;

    public PickProductDialog(AppCompatActivity activity, PickProductCallback callback, long bankId) {
        this.activity = activity;
        this.callback = callback;
        this.bankId=bankId;
    }

    public void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View dialogView = View.inflate(activity, R.layout.dialog_pick_single2, null);
        builder.setView(dialogView);

        //bind views
        RecyclerView recyclerView= dialogView.findViewById(R.id.pick_single);
        Button selectNOne= dialogView.findViewById(R.id.select_none);
        //setRecycler
        PickProductViewAdapter adapter= new PickProductViewAdapter(callback);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 3));

        selectNOne.setOnClickListener(v -> {
            callback.onProductItemPick(new ProductEntity( ));
            dialog.dismiss();
        });

        LiveData<List<ProductEntity>> liveData;
        //get items
        if(bankId==0){
            liveData= ViewModelProviders.of(activity).get(ProductListViewModel.class).getAllActive();
        }else
            liveData= ViewModelProviders.of(activity).get(ProductListViewModel.class).getBankActiveProducts(bankId);


        liveData.observe(activity, products -> {
            if (products != null) {
                liveData.removeObservers(activity);
                adapter.setProductList(products);
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    public void hideDialog(){
        dialog.dismiss();
    }

    private class PickProductViewAdapter extends PickAdapterIconTitle {

        private List<? extends Product> mProductList;

        @Nullable
        private final PickProductCallback mPickProductCallback;

        PickProductViewAdapter(@Nullable PickProductCallback clickCallback) {
            mPickProductCallback = clickCallback;
            setHasStableIds(true);
        }

        void setProductList(final List<? extends Product> productList) {
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
                                && Objects.equals(newProduct.getTitle(), oldProduct.getTitle());
                    }
                });
                mProductList = productList;
                result.dispatchUpdatesTo(this);
            }
        }

        @Override
        public int getItemCount() {
            return mProductList == null ? 0 : mProductList.size();
        }

        @Override
        public long getItemId(int position) {
            return mProductList.get(position).getId();
        }

        @Override
        public void onBindViewHolder(@NonNull IconTitleViewHolder holder, int position) {
            Product product= mProductList.get(position);

            holder.binding.title.setText(product.getTitle());

            setIcon(product, holder.binding);
            if(mPickProductCallback!=null) {
                holder.binding.pickIcon.setOnClickListener(v -> {
                    mPickProductCallback.onProductItemPick(mProductList.get(position));
                    dialog.dismiss();
                });
            }
        }

        private void setIcon(Product product, PickIconTitleItemBinding binding){
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
                    binding.pickIcon.setImageDrawable(myDrawable);
                }
            }else {
                int id= MyApplication.getBankIconId(product.getBankId());
                if(id!=0)
                    Picasso.get().load(id).into(binding.pickIcon);
            }
        }
    }
}