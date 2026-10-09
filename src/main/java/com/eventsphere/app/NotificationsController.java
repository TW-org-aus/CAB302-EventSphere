package com.eventsphere.app;

import com.eventsphere.app.model.Notification;
import com.eventsphere.app.model.NotificationType;
import com.eventsphere.app.service.NotificationService;
import com.eventsphere.app.service.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class NotificationsController {

    private final NotificationService notificationService;
    private final SessionManager session;

    @FXML private VBox notificationList;

    public NotificationsController(NotificationService notificationService, SessionManager session) {
        this.notificationService = notificationService;
        this.session = session;
    }

    @FXML
    public void initialize() {
        int userId = session.getCurrentUser().orElseThrow().getUserId();
        notificationService.checkAndInsertReminders(userId);

        List<Notification> notifications = notificationService.getForUser(userId);
        for (Notification n : notifications) {
            VBox row = new VBox(4);
            row.getStyleClass().add("event-row");
            row.getChildren().addAll(
                    new Label(typeLabel(n.getType())),
                    labelWith(n.getMessage(), "muted-text", true),
                    labelWith(relativeTime(n.getCreatedAt()), "muted-text", false)
            );
            notificationList.getChildren().add(row);
        }

        notificationService.markAllRead(userId);
    }

    private static Label labelWith(String text, String styleClass, boolean wrap) {
        Label l = new Label(text);
        l.getStyleClass().add(styleClass);
        l.setWrapText(wrap);
        return l;
    }

    private static String typeLabel(NotificationType type) {
        return switch (type) {
            case COMMENT_REPLY -> "New reply to your comment";
            case NEW_MESSAGE -> "New message";
            case EVENT_REMINDER_WEEK, EVENT_REMINDER_DAY, EVENT_REMINDER_12H -> "Event reminder";
            case EVENT_UPDATED -> "Event updated";
        };
    }

    private static String relativeTime(Instant createdAt) {
        if (createdAt == null) return "";
        long secs = Duration.between(createdAt, Instant.now()).getSeconds();
        if (secs < 60) return "just now";
        if (secs < 3600) return (secs / 60) + " minutes ago";
        if (secs < 86400) return (secs / 3600) + " hours ago";
        return (secs / 86400) + " days ago";
    }
}
