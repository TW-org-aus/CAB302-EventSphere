package com.eventsphere.app.model;

import java.time.Instant;

/** Domain model for a private 1:1 message thread; user1Id is always the lesser id so (A,B) and (B,A) cannot both exist. */
public class Conversation {

    private final int conversationId;
    private final int user1Id;
    private final int user2Id;
    private final Instant createdAt;

    /** Creates a Conversation; throws IllegalArgumentException if user1Id is not strictly less than user2Id. */
    public Conversation(int conversationId, int user1Id, int user2Id, Instant createdAt) {
        if (user1Id >= user2Id) {
            throw new IllegalArgumentException(
                    "user1Id must be less than user2Id, got " + user1Id + " and " + user2Id);
        }
        this.conversationId = conversationId;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.createdAt = createdAt;
    }

    public int getConversationId() { return conversationId; }
    public int getUser1Id() { return user1Id; }
    public int getUser2Id() { return user2Id; }
    public Instant getCreatedAt() { return createdAt; }

    /** Returns the id of the other participant; throws IllegalArgumentException when userId is not in this conversation. */
    public int otherUserId(int userId) {
        if (userId == user1Id) {
            return user2Id;
        }
        if (userId == user2Id) {
            return user1Id;
        }
        throw new IllegalArgumentException("User " + userId + " is not in conversation " + conversationId);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Conversation conversation && conversationId == conversation.conversationId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(conversationId);
    }
}
