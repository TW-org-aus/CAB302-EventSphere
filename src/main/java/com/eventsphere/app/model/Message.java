package com.eventsphere.app.model;

import java.time.Instant;
import java.util.Objects;

public class Message {

    private final int messageId;
    private final int conversationId;
    private final int senderId;
    private final String content;
    private final Instant sentAt;
    // Null = unread.
    private Instant readAt;

    public Message(int messageId, int conversationId, int senderId, String content,
                   Instant sentAt, Instant readAt) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = Objects.requireNonNull(content, "content");
        this.sentAt = sentAt;
        this.readAt = readAt;
    }

    public int getMessageId() { return messageId; }
    public int getConversationId() { return conversationId; }
    public int getSenderId() { return senderId; }
    public String getContent() { return content; }
    public Instant getSentAt() { return sentAt; }
    public Instant getReadAt() { return readAt; }

    public void setReadAt(Instant readAt) { this.readAt = readAt; }

    @Override
    public boolean equals(Object other) {
        return other instanceof Message message && messageId == message.messageId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(messageId);
    }
}
