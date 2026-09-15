package com.eventsphere.app.dao;

import com.eventsphere.app.model.Message;

import java.util.List;

public interface IMessageDAO {

    // Returns the generated MessageID. Doesn't check that senderId belongs to the conversation the service does.
    int insert(int conversationId, int senderId, String content);

    // All messages in the conversation, oldest first.
    List<Message> findByConversation(int conversationId);

    // Marks every unread message the reader didn't send, e.g. when the chat opens.
    void markRead(int conversationId, int readerId);
}
