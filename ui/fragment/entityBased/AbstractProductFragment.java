package com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.newVersion.db.entity.ProductEntity;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.ui.activity.MainActivity;
import com.unexceptional.beast.banko.newVersion.ui.fragment.BasicFragment;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.ProductViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.TransactionViewModel;

import java.util.Objects;


/**
 * Abstract product for both product and debt fragment
 */
abstract class AbstractProductFragment extends BasicFragment {

    ProductViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(ProductViewModel.class);
    }

    boolean canChange(ProductEntity activeProduct){
        if (activeProduct.getId()>0)
            return !ViewModelProviders.of(AbstractProductFragment.this).get(TransactionViewModel.class).productHasTransactions(activeProduct.getId());
        else
            return true;
    }

    void showDeleteDialog(ProductEntity activeProduct, ProductViewModel viewModel){
        new AppExecutors().mainThread().execute(()-> {
        });
    }

    void showSnackBar(ProductEntity activeProduct, View root){
        final Snackbar snackbar = Snackbar
                .make(root , getString(R.string.product_has_transactions), Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.show_transactions, view -> {
            Intent intent= new Intent(getActivity(), MainActivity.class);
            preferences.edit().putInt(getString(R.string.key_temp_select_main_nav), 2).apply();
            preferences.edit().putInt(getString(R.string.key_tr_list_date_option), 0).apply();
            preferences.edit().putLong(getString(R.string.key_tr_list_from_date_millis), 0).apply();
            preferences.edit().putLong(getString(R.string.key_tr_list_to_date_millis), 0).apply();

            preferences.edit().putLong(getString(R.string.key_tr_list_category_id_new),0).apply();
            preferences.edit().putLong(getString(R.string.key_tr_list_sub_category_id_new), 0).apply();
            preferences.edit().putLong(getString(R.string.key_tr_list_product_id), activeProduct.getId()).apply();
            preferences.edit().putLong(getString(R.string.key_tr_list_bank_id), activeProduct.getBankId()).apply();

            startActivity(intent);
        });
        snackbar.show();
    }

}
