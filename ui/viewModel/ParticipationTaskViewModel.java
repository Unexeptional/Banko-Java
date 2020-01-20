package com.unexceptional.beast.banko.newVersion.ui.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.unexceptional.beast.banko.newVersion.db.entity.ParticipationTaskEntity;
import com.unexceptional.beast.banko.newVersion.db.repository.ParticipationTaskRepository;
import com.unexceptional.beast.banko.newVersion.db.repository.TaskRepository;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.Date;

public class ParticipationTaskViewModel extends AndroidViewModel {

    private final ParticipationTaskRepository dataRepository;

    public ParticipationTaskViewModel(Application application) {
        super(application);
        dataRepository = ((MyApplication) application).getParticipationTaskRepository();
    }

    public  ParticipationTaskEntity getParticipationRapid(long taskId, long startDate, long endDate){
        return dataRepository.getParticipationRapid(taskId, startDate, endDate);
    }

    public   LiveData<ParticipationTaskEntity> getParticipation(long taskId, long startDate, long endDate){
        return dataRepository.getParticipation(taskId, startDate, endDate);
    }

    public void insert(ParticipationTaskEntity taskEntity){
        dataRepository.insert(taskEntity);
    }

    public void update(ParticipationTaskEntity taskEntity){
        dataRepository.update(taskEntity);
    }
}
