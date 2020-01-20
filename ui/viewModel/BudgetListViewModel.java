package com.unexceptional.beast.banko.newVersion.ui.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.unexceptional.beast.banko.newVersion.db.entity.BudgetEntity;
import com.unexceptional.beast.banko.newVersion.db.repository.BudgetRepository;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.List;

public class BudgetListViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<BudgetEntity>> allBudgets;

    private BudgetRepository budgetRepository;

    public BudgetListViewModel(Application application) {
        super(application);
        allBudgets = new MediatorLiveData<>();
        allBudgets.setValue(null);

        budgetRepository = ((MyApplication) application).getBudgetRepository();

        LiveData<List<BudgetEntity>> allBanks = budgetRepository.getAllBudgets();

        // observe the changes of the categories from the database and forward them
        this.allBudgets.addSource(allBanks, this.allBudgets::setValue);
    }

    public LiveData<List<BudgetEntity>> getAllBudgets() { return allBudgets; }

}
