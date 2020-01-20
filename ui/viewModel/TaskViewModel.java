package com.unexceptional.beast.banko.newVersion.ui.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.unexceptional.beast.banko.newVersion.db.entity.TaskEntity;
import com.unexceptional.beast.banko.newVersion.db.repository.TaskRepository;
import com.unexceptional.beast.banko.other.MyApplication;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository dataRepository;

    public TaskViewModel(Application application) {
        super(application);
        dataRepository = ((MyApplication) application).getTaskRepository();
    }

    public LiveData<TaskEntity> getTask(long taskId){
        return dataRepository.getTask(taskId);
    }

    public void changeBankId(long productId, long bankId){
        dataRepository.changeBankId(productId, bankId);
    }

    public void insert(TaskEntity taskEntity){
        dataRepository.insert(taskEntity);
    }

    public void update(TaskEntity taskEntity){
        dataRepository.update(taskEntity);
    }

    public void delete(TaskEntity taskEntity){
        dataRepository.delete(taskEntity);
    }

}
