package com.unexceptional.beast.banko.newVersion.ui.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.unexceptional.beast.banko.newVersion.db.entity.CategoryEntity;
import com.unexceptional.beast.banko.newVersion.db.repository.CategoryRepository;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.List;

public class CategoryListViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<CategoryEntity>> parentCategories;

    private final MediatorLiveData<List<CategoryEntity>> allCategories;

    private CategoryRepository categoryRepository;

    public CategoryListViewModel(Application application) {
        super(application);
        parentCategories = new MediatorLiveData<>();
        parentCategories.setValue(null);

        allCategories = new MediatorLiveData<>();
        allCategories.setValue(null);

        categoryRepository = ((MyApplication) application).getCategoryRepository();

        LiveData<List<CategoryEntity>> parentCategories = categoryRepository.getParentCategories();

        // observe the changes of the categories from the database and forward them
        this.parentCategories.addSource(parentCategories, this.parentCategories::setValue);

        LiveData<List<CategoryEntity>> allCategories = categoryRepository.getAllCategories();

        // observe the changes of the categories from the database and forward them
        this.allCategories.addSource(allCategories, this.allCategories::setValue);
    }

    public LiveData<List<CategoryEntity>> getParentCategories() { return parentCategories; }

    public LiveData<List<CategoryEntity>> getExpenseCategories() {
        return categoryRepository.getExpenseCategories();
    }

    public LiveData<List<CategoryEntity>> getIncomeCategories() {
        return categoryRepository.getIncomeCategories();
    }

    public LiveData<List<CategoryEntity>> getAllCategories() { return allCategories; }

    public LiveData<List<CategoryEntity>> getChildCategories(long parentId) {
        return categoryRepository.getChildCategories(parentId);
    }


}
