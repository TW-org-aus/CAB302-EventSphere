package com.eventsphere.app.model;

import java.time.Instant;
import java.util.Objects;

/** Domain model for a user notification, linking to the event, comment, or conversation that triggered it. */
public class Notification {

    private final int notificationId;
    private final int userId;
    private final NotificationType type;
    private final Integer relatedEventId;
    private final Integer relatedCommentId;
    private final Integer relatedConversationId;
    private final String message;
    private final Instant createdAt;
    private boolean read;

    /** Creates a fully-hydrated Notification as read from the database. */
    public Notification(int notificationId, int userId, NotificationType type,
                        Integer relatedEventId, Integer relatedCommentId, Integer relatedConversationId,
                        String message, Instant createdAt, boolean read) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.type = Objects.requireNonNull(type, "type");
        this.relatedEventId = relatedEventId;
        this.relatedCommentId = relatedCommentId;
        this.relatedConversationId = relatedConversationId;
        this.message = Objects.requireNonNull(message, "message");
        this.createdAt = createdAt;
        this.read = read;
    }

    public int getNotificationId() { return notificationId; }
    public int getUserId() { return userId; }
    public NotificationType getType() { return type; }
    public Integer getRelatedEventId() { return relatedEventId; }
    public Integer getRelatedCommentId() { return relatedCommentId; }
    public Integer getRelatedConversationId() { return relatedConversationId; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isRead() { return read; }

    public void setRead(boolean read) { this.read = read; }

    @Override
    public boolean equals(Object other) {
        return other instanceof Notification notification && notificationId == notification.notificationId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(notificationId);
    }
}
