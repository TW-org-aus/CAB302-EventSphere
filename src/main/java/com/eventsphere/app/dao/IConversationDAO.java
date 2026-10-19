package com.eventsphere.app.dao;

import com.eventsphere.app.model.Conversation;

import java.util.List;
import java.util.Optional;

/** DAO for 1:1 message threads; enforces user ordering so (A,B) and (B,A) share a row. */
public interface IConversationDAO {

    /** Returns the existing conversation between the two users, or creates one; argument order does not matter. The Going-event check is enforced by MessagingService, not here. */
    Conversation findOrCreate(int userA, int userB);

    /** Returns conversations involving userId, hiding threads whose other participant is deactivated. */
    List<Conversation> findByUser(int userId);

    /** Returns true if userId is a participant in the conversation; used to gate reads and sends. */
    boolean isParticipant(int conversationId, int userId);

    Optional<Conversation> findById(int conversationId);
}
