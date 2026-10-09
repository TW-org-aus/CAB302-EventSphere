package com.eventsphere.app.service;

import com.eventsphere.app.dao.IEventDAO;
import com.eventsphere.app.dao.IGoingDAO;
import com.eventsphere.app.dao.INotificationDAO;
import com.eventsphere.app.model.Notification;
import com.eventsphere.app.model.NotificationType;

import java.util.List;

public class NotificationService {

    private final INotificationDAO notifications;
    private final IGoingDAO going;
    private final IEventDAO events;

    public NotificationService(INotificationDAO notifications, IGoingDAO going, IEventDAO events) {
        this.notifications = notifications;
        this.going = going;
        this.events = events;
    }

    public List<Notification> getForUser(int userId) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void markAllRead(int userId) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void create(int userId, NotificationType type, Integer eventId,
                       Integer commentId, Integer conversationId, String message) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void checkAndInsertReminders(int userId) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
