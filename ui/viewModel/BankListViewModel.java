package com.unexceptional.beast.banko.newVersion.ui.viewModel;

import android.app.Application;

import com.unexceptional.beast.banko.newVersion.db.entity.BankEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.ProductEntity;
import com.unexceptional.beast.banko.newVersion.db.repository.BankRepository;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

public class BankListViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<BankEntity>> allBanks;

    private BankRepository bankRepository;

    public BankListViewModel(Application application) {
        super(application);
        allBanks = new MediatorLiveData<>();
        allBanks.setValue(null);

        bankRepository = ((MyApplication) application).getBankRepository();

        LiveData<List<BankEntity>> allBanks = bankRepository.getAllBanks();

        // observe the changes of the products from the database and forward them
        this.allBanks.addSource(allBanks, this.allBanks::setValue);
    }

    public LiveData<List<BankEntity>> getAllBanks() { return allBanks; }

    public LiveData<List<BankEntity>> getActiveBanks() {
       return bankRepository.getActiveBanks();
    }

    public LiveData<List<BankEntity>> getActiveBanksNoDebt() {
        return bankRepository.getActiveBanksNoDebt();
    }

    public LiveData<List<BankEntity>> getInactiveBanks() {
        return bankRepository.getInactiveBanks();
    }

    public List<ProductEntity> getBankProducts(long bankId ){
        return bankRepository.getBankProducts(bankId);
    }
}
