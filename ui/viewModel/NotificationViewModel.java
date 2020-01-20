package com.unexceptional.beast.banko.newVersion.ui.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.unexceptional.beast.banko.newVersion.db.entity.NotificationEntity;
import com.unexceptional.beast.banko.newVersion.db.repository.NotificationRepository;
import com.unexceptional.beast.banko.newVersion.other.NotificationPublisher;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.Date;

public class NotificationViewModel extends AndroidViewModel {

    private final NotificationRepository dataRepository;

    public NotificationViewModel(Application application) {
        super(application);
        dataRepository = ((MyApplication) application).getNotificationRepository();
    }

    public LiveData<NotificationEntity> getNotification(long notificationId){
        return dataRepository.getNotification(notificationId);
    }

    public NotificationEntity getReminderNotification(){
        return dataRepository.getReminderNotification();
    }

    public NotificationEntity getAutoBackupNotification(){
        return dataRepository.getAutoBackupNotification();
    }

    public NotificationEntity getNotificationByType(short type){
        return dataRepository.getNotificationByType(type);
    }

    public void insert(NotificationEntity notificationEntity){
        dataRepository.insert(notificationEntity);
        new NotificationPublisher().scheduleNotification(MyApplication.getContext());
    }

    public void update(NotificationEntity notificationEntity){
        dataRepository.update(notificationEntity);
    }

    public void deleteAll(){
        dataRepository.deleteAll();
    }

    public void delete(NotificationEntity notificationEntity){
        dataRepository.delete(notificationEntity);
    }

}
