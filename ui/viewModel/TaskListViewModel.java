package com.unexceptional.beast.banko.newVersion.ui.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.unexceptional.beast.banko.newVersion.db.entity.TaskEntity;
import com.unexceptional.beast.banko.newVersion.db.repository.TaskRepository;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.List;

public class TaskListViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<TaskEntity>> allTasks;

    private TaskRepository taskRepository;

    public TaskListViewModel(Application application) {
        super(application);
        allTasks = new MediatorLiveData<>();
        allTasks.setValue(null);

        taskRepository = ((MyApplication) application).getTaskRepository();

        LiveData<List<TaskEntity>> allBanks = taskRepository.getAllTasks();

        // observe the changes of the categories from the database and forward them
        this.allTasks.addSource(allBanks, this.allTasks::setValue);
    }

    public LiveData<List<TaskEntity>> getAllTasks() { return allTasks; }

    public LiveData<List<TaskEntity>> getActiveTasks(long startDate, long endDate, int dateOption){
        return taskRepository.getActiveTasks(startDate, endDate, dateOption);
    }
    public LiveData<List<TaskEntity>> getActiveTasksBank(long startDate, long endDate, int dateOption, long bankId){
        return taskRepository.getActiveTasksBank(startDate, endDate, dateOption, bankId);
    }

    public LiveData<List<TaskEntity>> getDoneTasks(long startDate, long endDate, int dateOption){
        return taskRepository.getDoneTasks(startDate, endDate, dateOption);
    }
public LiveData<List<TaskEntity>> getDoneTasksBank(long startDate, long endDate, int dateOption, long bankId){
        return taskRepository.getDoneTasksBank(startDate, endDate, dateOption, bankId);
    }


}
