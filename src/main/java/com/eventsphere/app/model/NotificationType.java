package com.eventsphere.app.model;


/** Enum of notification types stored in the database; each value maps to a string column value via getDbValue(). */
public enum NotificationType {
    COMMENT_REPLY("CommentReply"),
    NEW_MESSAGE("NewMessage"),
    EVENT_REMINDER_WEEK("EventReminderWeek"),
    EVENT_REMINDER_DAY("EventReminderDay"),
    EVENT_REMINDER_12H("EventReminder12H"),
    EVENT_UPDATED("EventUpdated");

    private final String dbValue;

    NotificationType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() { return dbValue; }

    /** Returns the NotificationType whose database value matches the given string, or throws IllegalArgumentException. */
    public static NotificationType fromDbValue(String dbValue) {
        for (NotificationType type : values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown notification type: " + dbValue);
    }
}
