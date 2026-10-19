package com.eventsphere.app.dao;

import com.eventsphere.app.model.Message;

import java.util.List;

/** DAO for messages within a conversation thread; participant checks are enforced by MessagingService. */
public interface IMessageDAO {

    /** Inserts a message and returns the generated MessageID; caller must verify senderId is a participant. */
    int insert(int conversationId, int senderId, String content);

    /** Returns all messages in the conversation, oldest first. */
    List<Message> findByConversation(int conversationId);

    /** Marks every unread message the reader did not send as read, typically called when the chat opens. */
    void markRead(int conversationId, int readerId);
}
