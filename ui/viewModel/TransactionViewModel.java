package com.unexceptional.beast.banko.newVersion.ui.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.unexceptional.beast.banko.newVersion.db.entity.TransactionEntity;
import com.unexceptional.beast.banko.newVersion.db.repository.TransactionRepository;
import com.unexceptional.beast.banko.newVersion.db.repository.UltraRepository;
import com.unexceptional.beast.banko.other.MyApplication;

public class TransactionViewModel extends AndroidViewModel {

    private final TransactionRepository transactionRepository;
    private final UltraRepository ultraRepository;

    public TransactionViewModel(Application application) {
        super(application);
        transactionRepository = ((MyApplication) application).getTransactionRepository();
        ultraRepository = ((MyApplication) application).getUltraRepository();
    }

    public LiveData<TransactionEntity> getTransaction(long transactionId){
        return transactionRepository.getTransaction(transactionId);
    }

    public boolean categoryHasTransactions(long categoryId){
        return transactionRepository.categoryHasTransactions(categoryId);
    }

    public boolean subCategoryHasTransactions(long subCategoryId){
        return transactionRepository.subCategoryHasTransactions(subCategoryId);
    }

    public boolean productHasTransactions(long productId){
        return transactionRepository.productHasTransactions(productId);
    }

    public long insert(TransactionEntity transactionEntity){
        return ultraRepository.insertTransaction(transactionEntity);
    }

    public void delete(TransactionEntity transactionEntity){
        transactionRepository.delete(transactionEntity);
        ultraRepository.undoOldTransaction(transactionEntity);
    }

    public void update(TransactionEntity transaction, TransactionEntity oldTransaction){
        ultraRepository.undoOldTransaction(oldTransaction);
        ultraRepository.doTransaction(transaction);

        transactionRepository.update(transaction);
     }

    public void updateKids(long transactionId, int value){
       transactionRepository.updateKids(transactionId, value);
    }

    public void changeBankId(long productId, long bankId){
        transactionRepository.changeBankId(productId, bankId);
    }

}
