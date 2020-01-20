package com.unexceptional.beast.banko.newVersion.ui.viewModel;

import android.app.Application;

import com.unexceptional.beast.banko.newVersion.db.entity.ProductEntity;
import com.unexceptional.beast.banko.newVersion.db.repository.ProductRepository;
import com.unexceptional.beast.banko.other.MyApplication;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class ProductViewModel extends AndroidViewModel {

    private final ProductRepository dataRepository;

    public ProductViewModel(Application application) {
        super(application);
        dataRepository = ((MyApplication) application).getProductRepository();
    }

    public LiveData<ProductEntity> getProduct(long productId){
        return dataRepository.getProduct(productId);
    }

    public ProductEntity getProductRapid(long productId){
        return dataRepository.getProductRapid(productId);
    }

    public void insert(ProductEntity productEntity){
        dataRepository.insert(productEntity);
    }

    public void update(ProductEntity productEntity){
        dataRepository.update(productEntity);
    }

    public void deleteAll(){
        dataRepository.deleteAll();
    }

    public void delete(ProductEntity productEntity){
        dataRepository.delete(productEntity);
    }

}
