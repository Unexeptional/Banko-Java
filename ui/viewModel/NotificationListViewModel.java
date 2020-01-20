package com.unexceptional.beast.banko.newVersion.ui.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.unexceptional.beast.banko.newVersion.db.entity.NotificationEntity;
import com.unexceptional.beast.banko.newVersion.db.repository.NotificationRepository;
import com.unexceptional.beast.banko.other.MyApplication;

import java.util.Date;
import java.util.List;

public class NotificationListViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<NotificationEntity>> allNotifications;

    private NotificationRepository notificationRepository;

    public NotificationListViewModel(Application application) {
        super(application);
        allNotifications = new MediatorLiveData<>();
        allNotifications.setValue(null);

        notificationRepository = ((MyApplication) application).getNotificationRepository();

        LiveData<List<NotificationEntity>> allNotifications = notificationRepository.getAllNotifications();

        // observe the changes of the products from the database and forward them
        this.allNotifications.addSource(allNotifications, this.allNotifications::setValue);
    }

    public LiveData<List<NotificationEntity>> getAllNotifications() { return allNotifications; }
}
