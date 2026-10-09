package com.eventsphere.app.dao;

import com.eventsphere.app.model.Conversation;

import java.util.List;
import java.util.Optional;

public interface IConversationDAO {

    // Argument order doesn't matter. Throws IllegalArgumentException if both ids are the same.
    // NOTE!! Doesn't check that the users share a Going event, that is handeled by messagingService. --> users need to go to same event to msg one another
    Conversation findOrCreate(int userA, int userB);

    // Conversations involving userId, hiding ones whose other participant is deactivated.
    List<Conversation> findByUser(int userId);

    // Is true if userId is one of the people in the conversation, checks before reading or sending.
    boolean isParticipant(int conversationId, int userId);

    Optional<Conversation> findById(int conversationId);
}
