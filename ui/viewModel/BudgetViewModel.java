package com.unexceptional.beast.banko.newVersion.ui.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.unexceptional.beast.banko.newVersion.db.entity.BudgetEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.ProductEntity;
import com.unexceptional.beast.banko.newVersion.db.repository.BudgetRepository;
import com.unexceptional.beast.banko.newVersion.db.repository.ProductRepository;
import com.unexceptional.beast.banko.other.MyApplication;

public class BudgetViewModel extends AndroidViewModel {

    private final BudgetRepository dataRepository;

    public BudgetViewModel(Application application) {
        super(application);
        dataRepository = ((MyApplication) application).getBudgetRepository();
    }

    public LiveData<BudgetEntity> getBudget(long budgetId){
        return dataRepository.getBudget(budgetId);
    }

    public void insert(BudgetEntity budgetEntity){
        dataRepository.insert(budgetEntity);
    }

    public void update(BudgetEntity budgetEntity){
        dataRepository.update(budgetEntity);
    }

    public void delete(BudgetEntity budgetEntity){
        dataRepository.delete(budgetEntity);
    }

}
