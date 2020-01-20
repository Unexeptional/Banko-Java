package com.unexceptional.beast.banko.newVersion.ui.viewModel;

import android.app.Application;

import com.unexceptional.beast.banko.newVersion.db.entity.ProductEntity;
import com.unexceptional.beast.banko.newVersion.db.repository.ProductRepository;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

public class ProductListViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<ProductEntity>> allProducts;
    private final MediatorLiveData<List<ProductEntity>> allProductsNoDebt;

    private ProductRepository productRepository;

    public ProductListViewModel(Application application) {
        super(application);
        allProducts = new MediatorLiveData<>();
        allProducts.setValue(null);

        allProductsNoDebt = new MediatorLiveData<>();
        allProductsNoDebt.setValue(null);

        productRepository = ((MyApplication) application).getProductRepository();

        LiveData<List<ProductEntity>> allProducts = productRepository.getAllProducts();
        LiveData<List<ProductEntity>> allProductsNoDebt = productRepository.getAllProductsNoDebt();

        // observe the changes of the products from the database and forward them
        this.allProducts.addSource(allProducts, this.allProducts::setValue);
        this.allProductsNoDebt.addSource(allProductsNoDebt, this.allProductsNoDebt::setValue);
    }

    public LiveData<List<ProductEntity>> getAllProducts() { return allProducts; }

    public LiveData<List<ProductEntity>> getAllProductsNoDebt() { return allProductsNoDebt; }

    public LiveData<List<ProductEntity>> getBankProducts(long bankId) {
        return productRepository.getBankProducts(bankId);
    }

    public LiveData<List<ProductEntity>> getBankActiveProducts(long bankId) {
        return productRepository.getBankActiveProducts(bankId);
    }


    public LiveData<List<ProductEntity>> getAllActive() {
        return productRepository.getAllActive();
    }


}
