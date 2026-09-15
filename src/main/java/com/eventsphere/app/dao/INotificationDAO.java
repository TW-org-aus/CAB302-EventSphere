package com.eventsphere.app.dao;

import com.eventsphere.app.model.Notification;
import com.eventsphere.app.model.NotificationType;

import java.util.List;

public interface INotificationDAO {

    // Writes the notification unconditionally.
    int insert(int userId, NotificationType type, Integer relatedEventId,
               Integer relatedCommentId, Integer relatedConversationId);

    // Ordered newest first.
    List<Notification> findByUser(int userId);

    void markRead(int notificationId);
}
