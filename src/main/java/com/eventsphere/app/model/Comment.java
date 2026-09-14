package com.eventsphere.app.model;

import java.time.Instant;
import java.util.Objects;

public class Comment {

    private final int commentId;
    private final int userId;
    private final int eventId;
    private String content;
    private final Instant createdAt;
    // Null until the author edits the comment; the UI uses it for the "edited" marker.
    private Instant updatedAt;
    // Null = parent comment, non-null = tagged reply.
    private final Integer replyToCommentId;

    public Comment(int commentId, int userId, int eventId, String content,
                   Instant createdAt, Instant updatedAt, Integer replyToCommentId) {
        this.commentId = commentId;
        this.userId = userId;
        this.eventId = eventId;
        this.content = Objects.requireNonNull(content, "content");
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.replyToCommentId = replyToCommentId;
    }

    public int getCommentId() { return commentId; }
    public int getUserId() { return userId; }
    public int getEventId() { return eventId; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Integer getReplyToCommentId() { return replyToCommentId; }

    public void setContent(String content) { this.content = Objects.requireNonNull(content, "content"); }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object other) {
        return other instanceof Comment comment && commentId == comment.commentId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(commentId);
    }
}
