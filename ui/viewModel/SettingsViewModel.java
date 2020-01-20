package com.unexceptional.beast.banko.newVersion.ui.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.unexceptional.beast.banko.newVersion.db.entity.SettingsEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.TaskEntity;
import com.unexceptional.beast.banko.newVersion.db.repository.SettingsRepository;
import com.unexceptional.beast.banko.newVersion.db.repository.TaskRepository;
import com.unexceptional.beast.banko.other.MyApplication;

public class SettingsViewModel extends AndroidViewModel {

    private final SettingsRepository dataRepository;

    public SettingsViewModel(Application application) {
        super(application);
        dataRepository = ((MyApplication) application).getSettingsRepository();
    }

    public SettingsEntity getSettings(){
        return dataRepository.getSettings();
    }

    public LiveData<SettingsEntity> getSettingsLive(){
        return dataRepository.getSettingsLive();
    }

    public void updateLocale(String localeString){
        dataRepository.updateLocale(localeString);
    }

    public void updateLanguage(String language){
        dataRepository.updateLanguage(language);
    }
}
