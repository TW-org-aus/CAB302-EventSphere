// Event.java
import java.time.LocalDateTime;

public class Event {
    private int eventId;
    private String title;
    private String description;
    private String category;
    private String location;
    private LocalDateTime startTime;
    private int sourceId;
    private int likesCount;
    private int commentsCount;
    private LocalDateTime createdAt;

    public Event(int eventId, String title, String description, String category, String location,
                 LocalDateTime startTime, int sourceId, int likesCount, int commentsCount,
                 LocalDateTime createdAt) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.startTime = startTime;
        this.sourceId = sourceId;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.createdAt = createdAt;
    }

    public int getEventId() { return eventId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public LocalDateTime getStartTime() { return startTime; }
    public int getSourceId() { return sourceId; }
    public int getLikesCount() { return likesCount; }
    public int getCommentsCount() { return commentsCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}