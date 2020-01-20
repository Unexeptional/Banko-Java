package com.unexceptional.beast.banko.newVersion.ui.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.unexceptional.beast.banko.newVersion.db.entity.CategoryEntity;
import com.unexceptional.beast.banko.newVersion.db.repository.CategoryRepository;
import com.unexceptional.beast.banko.other.MyApplication;

public class CategoryViewModel extends AndroidViewModel {

    private final CategoryRepository dataRepository;

    public CategoryViewModel(Application application) {
        super(application);
        dataRepository = ((MyApplication) application).getCategoryRepository();
    }

    public LiveData<CategoryEntity> getCategory(long categoryId){
        return dataRepository.getCategory(categoryId);
    }

    public boolean hasKids(long parentId){
        return dataRepository.hasKids(parentId);
    }

    public void insert(CategoryEntity categoryEntity){
        dataRepository.insert(categoryEntity);
    }

    public void update(CategoryEntity categoryEntity){
        dataRepository.update(categoryEntity);
    }

    public void delete(CategoryEntity categoryEntity){
        dataRepository.delete(categoryEntity);
    }

}
