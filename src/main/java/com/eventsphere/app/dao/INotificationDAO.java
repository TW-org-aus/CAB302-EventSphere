package com.eventsphere.app.dao;

import com.eventsphere.app.model.Notification;
import com.eventsphere.app.model.NotificationType;

import java.util.List;

public interface INotificationDAO {

    /** Writes the notification and returns the generated NotificationID. */
    int insert(int userId, NotificationType type, Integer relatedEventId,
               Integer relatedCommentId, Integer relatedConversationId, String message);

    /** All notifications for the user, newest first. */
    List<Notification> findByUser(int userId);

    void markRead(int notificationId);

    /** Marks every unread notification for the user as read. */
    void markAllRead(int userId);

    /**
     * Returns true if any notification of the given type already exists for this
     * user+event pair. Used to deduplicate event reminders.
     */
    boolean existsForEvent(int userId, NotificationType type, int eventId);
}
