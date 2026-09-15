// Comment.java
import java.time.LocalDateTime;

public class Comment {
    private int commentId;
    private int eventId;
    private int userId;
    private String content;
    private LocalDateTime createdAt;

    public Comment(int commentId, int eventId, int userId, String content, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.eventId = eventId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getCommentId() { return commentId; }
    public int getEventId() { return eventId; }
    public int getUserId() { return userId; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}