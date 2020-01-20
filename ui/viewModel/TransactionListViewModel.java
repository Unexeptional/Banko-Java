package com.unexceptional.beast.banko.newVersion.ui.viewModel;

import android.app.Application;

import com.unexceptional.beast.banko.newVersion.db.entity.TransactionEntity;
import com.unexceptional.beast.banko.newVersion.db.repository.TransactionRepository;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.Date;
import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.sqlite.db.SupportSQLiteQuery;

public class TransactionListViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<TransactionEntity>> allTransactions;

    private TransactionRepository transactionRepository;

    public TransactionListViewModel(Application application) {
        super(application);
        allTransactions = new MediatorLiveData<>();
        allTransactions.setValue(null);

        transactionRepository = ((MyApplication) application).getTransactionRepository();

        LiveData<List<TransactionEntity>> allTransactions = transactionRepository.getAllTransactions();

        // observe the changes of the products from the database and forward them
        this.allTransactions.addSource(allTransactions, this.allTransactions::setValue);
    }

    public LiveData<List<TransactionEntity>> getAllTransactions() { return allTransactions; }

    public LiveData<List<TransactionEntity>> getTransactions(SupportSQLiteQuery query){
        return transactionRepository.getTransactions(query);
    }

    public LiveData<List<TransactionEntity>> getExpenses(Date from, Date to){
        return transactionRepository.getExpenses(from, to);
    }

    public  LiveData<List<TransactionEntity>> getFewTransactions(long parentId){
        return transactionRepository.getFewTransactions(parentId);
    }

    public LiveData<List<TransactionEntity>> getBudgetActualAmount(Date from, Date to, String categoryIds){
        return transactionRepository.getBudgetActualAmount( from, to, categoryIds);
    }

}
