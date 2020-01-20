package com.unexceptional.beast.banko.newVersion.ui.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.unexceptional.beast.banko.newVersion.db.entity.CurrencyEntity;
import com.unexceptional.beast.banko.newVersion.db.repository.CurrencyRepository;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.List;

public class CurrencyViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<CurrencyEntity>> allCurrencies;

    private CurrencyRepository dataRepository;

    public CurrencyViewModel(Application application) {
        super(application);
        allCurrencies = new MediatorLiveData<>();
        allCurrencies.setValue(null);

        dataRepository = ((MyApplication) application).getCurrencyRepository();

        LiveData<List<CurrencyEntity>> allBanks = dataRepository.getAll();

        this.allCurrencies.addSource(allBanks, this.allCurrencies::setValue);
    }

    public LiveData<List<CurrencyEntity>> getAllCurrencies() { return allCurrencies; }

    public void insertAll(List<CurrencyEntity> currencyEntities){
        dataRepository.insertAll(currencyEntities);
    }
    public void updateAll(List<CurrencyEntity> currencyEntities){
        dataRepository.updateAll(currencyEntities);
    }

    public LiveData<CurrencyEntity> getCurrency(long currencyId){
        return dataRepository.getCurrency(currencyId);
    }

    public CurrencyEntity getCurrencyRapid(long currencyId){
        return dataRepository.getCurrencyRapid(currencyId);
    }
}
