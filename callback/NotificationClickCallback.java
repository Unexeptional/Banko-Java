package com.unexceptional.beast.banko.newVersion.callback;

import com.unexceptional.beast.banko.newVersion.db.model.Notification;

public interface NotificationClickCallback {
    void onNotificationItemClick(Notification notification);
}
