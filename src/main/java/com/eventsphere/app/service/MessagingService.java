package com.eventsphere.app.service;

import com.eventsphere.app.dao.IConversationDAO;
import com.eventsphere.app.dao.IGoingDAO;
import com.eventsphere.app.dao.IMessageDAO;
import com.eventsphere.app.dao.INotificationDAO;
import com.eventsphere.app.dao.IUserDAO;
import com.eventsphere.app.model.Conversation;
import com.eventsphere.app.model.Message;
import com.eventsphere.app.model.NotificationType;
import com.eventsphere.app.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessagingService {
    static final String EMPTY_MESSAGE = "Message cannot be empty";
    static final String NOT_A_PARTICIPANT = "You are not part of this conversation";
    // A row of the list of conversations with the last message
    public record ConversationPreview(Conversation conversation, User other, String lastMessage) { }

    private final IConversationDAO conversations;
    private final IMessageDAO messages;
    private final IUserDAO users;
    private final IGoingDAO going;
    private final INotificationDAO notifications;

    public MessagingService(IConversationDAO conversations, IMessageDAO messages, IUserDAO users, IGoingDAO going, INotificationDAO notifications) {
        this.conversations = conversations;
        this.messages = messages;
        this.users = users;
        this.going = going;
        this.notifications = notifications;
    }
    // Empty unless both users are going to same event
    public Optional<Conversation> openConversation(int userId, int otherUserId) {
        if (userId == otherUserId || !going.shareGoingEvent(userId, otherUserId)) {
            return Optional.empty();
        }
        return Optional.of(conversations.findOrCreate(userId, otherUserId));
    }
    // orders chats by most recent
    public List<ConversationPreview> inboxFor(int userId) {
        List<ConversationPreview> previews = new ArrayList<>();
        for (Conversation conversation : conversations.findByUser(userId)) {
            List<Message> thread = messages.findByConversation(conversation.getConversationId());
            String lastMessage = thread.isEmpty() ? "" : thread.get(thread.size() - 1).getContent();
            users.findById(conversation.otherUserId(userId)).ifPresent(other -> previews.add(new ConversationPreview(conversation, other, lastMessage)));
        }
        return previews;
    }
    // marking as read
    public List<Message> openChat(int conversationId, int userId) {
        if (!conversations.isParticipant(conversationId, userId)) {
            throw new IllegalArgumentException(NOT_A_PARTICIPANT);
        }
        messages.markRead(conversationId, userId);
        return messages.findByConversation(conversationId);
    }

    public Optional<String> send(int conversationId, int senderId, String content) {
        if (!conversations.isParticipant(conversationId, senderId)) {
            return Optional.of(NOT_A_PARTICIPANT);
        }
        String text = content == null ? "" : content.strip();
        if (text.isEmpty()) {
            return Optional.of(EMPTY_MESSAGE);
        }
        messages.insert(conversationId, senderId, text);
        conversations.findById(conversationId).ifPresent(convo -> {
            int recipientId = convo.otherUserId(senderId);
            String senderName = users.findById(senderId).map(User::getFirstName).orElse("Someone");
            notifications.insert(recipientId, NotificationType.NEW_MESSAGE, null, null, conversationId,
                    senderName + " sent you a message");
        });
        return Optional.empty();
    }
}
