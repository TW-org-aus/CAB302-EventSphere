package com.eventsphere.app.service;

import com.eventsphere.app.dao.IEventDAO;
import com.eventsphere.app.dao.IGoingDAO;
import com.eventsphere.app.dao.INotificationDAO;
import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.Notification;
import com.eventsphere.app.model.NotificationType;

import java.time.Instant;
import java.util.List;

public class NotificationService {

    private static final long SECONDS_12H = 12 * 3600L;
    private static final long SECONDS_1D  = 24 * 3600L;
    private static final long SECONDS_7D  = 7  * SECONDS_1D;

    private final INotificationDAO notifications;
    private final IGoingDAO going;
    private final IEventDAO events;

    public NotificationService(INotificationDAO notifications, IGoingDAO going, IEventDAO events) {
        this.notifications = notifications;
        this.going = going;
        this.events = events;
    }

    public List<Notification> getForUser(int userId) {
        return notifications.findByUser(userId);
    }

    public void markAllRead(int userId) {
        notifications.markAllRead(userId);
    }

    public void create(int userId, NotificationType type, Integer eventId,
                       Integer commentId, Integer conversationId, String message) {
        notifications.insert(userId, type, eventId, commentId, conversationId, message);
    }

    public void checkAndInsertReminders(int userId) {
        Instant now = Instant.now();
        for (Event event : going.findEventsForUser(userId)) {
            long until = event.getStartTime().getEpochSecond() - now.getEpochSecond();
            if (until <= 0 || until > SECONDS_7D) continue;

            NotificationType type;
            String message;
            if (until > SECONDS_1D) {
                type = NotificationType.EVENT_REMINDER_WEEK;
                message = event.getTitle() + " is happening in 1 week";
            } else if (until > SECONDS_12H) {
                type = NotificationType.EVENT_REMINDER_DAY;
                message = event.getTitle() + " is happening tomorrow";
            } else {
                type = NotificationType.EVENT_REMINDER_12H;
                message = event.getTitle() + " is happening in 12 hours";
            }

            if (!notifications.existsForEvent(userId, type, event.getEventId())) {
                notifications.insert(userId, type, event.getEventId(), null, null, message);
            }
        }
    }
}
