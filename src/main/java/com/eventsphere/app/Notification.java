// Notification.java
import java.time.LocalDateTime;

public class Notification {
    private int notificationId;
    private int userId;
    private String category;
    private String content;
    private LocalDateTime createdAt;
    private boolean read;

    public Notification(int notificationId, int userId, String category, String content,
                         LocalDateTime createdAt, boolean read) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.category = category;
        this.content = content;
        this.createdAt = createdAt;
        this.read = read;
    }

    public int getNotificationId() { return notificationId; }
    public int getUserId() { return userId; }
    public String getCategory() { return category; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isRead() { return read; }
}