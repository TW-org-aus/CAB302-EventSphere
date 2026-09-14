package com.eventsphere.app.model;


public enum NotificationType {
    COMMENT_REPLY("CommentReply"),
    NEW_MESSAGE("NewMessage"),
    EVENT_REMINDER("EventReminder");

    private final String dbValue;

    NotificationType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() { return dbValue; }

    public static NotificationType fromDbValue(String dbValue) {
        for (NotificationType type : values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown notification type: " + dbValue);
    }
}
