package com.eventsphere.app.service;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.dao.ConversationDAO;
import com.eventsphere.app.dao.MessageDAO;
import com.eventsphere.app.dao.EventDAO;
import com.eventsphere.app.dao.UserDAO;
import com.eventsphere.app.dao.GoingDAO;
import com.eventsphere.app.dao.SourceDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Message;
import com.eventsphere.app.model.Conversation;
import com.eventsphere.app.model.Event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessagingServiceTest {
    private Connection connection;
    private ConversationDAO conversations;
    private UserDAO users;
    private GoingDAO going;
    private MessagingService messaging;
    private int angg;
    private int zuko;
    private int eventId;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        new DBController(connection);

        users = new UserDAO(connection);
        going = new GoingDAO(connection);
        conversations = new ConversationDAO(connection);
        messaging = new MessagingService(conversations, new MessageDAO(connection), users, going);

        angg = users.insert("Angg", "Air", "angg@gmail.com", "hash");
        zuko = users.insert("Zuko", "Fire", "zuko@gmail.com", "hash");
        int sourceId = new SourceDAO(connection).insert("Test", "https://example.com");
        eventId = new EventDAO(connection).insert(new Event("Test Event", null, Category.MUSIC, Instant.parse("2026-09-01T09:30:00Z"), null, null, null, null, null, null, null, sourceId));
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void conversationOnlyOpenWhenBothGoing() {
        going.markGoing(angg, eventId);
        assertTrue(messaging.openConversation(angg, zuko).isEmpty());

        going.markGoing(zuko, eventId);
        assertTrue(messaging.openConversation(angg, zuko).isPresent());
    }

    @Test
    void openingAfterClosingShowsTheSameConversation() {
        going.markGoing(angg, eventId);
        going.markGoing(zuko, eventId);
        Conversation first = messaging.openConversation(angg, zuko).orElseThrow();
        Conversation second = messaging.openConversation(angg, zuko).orElseThrow();
        assertEquals(first, second);
    }

    @Test
    void oldMessageShowsInTheInbox() {
        Conversation chat = conversations.findOrCreate(angg, zuko);
        messaging.send(chat.getConversationId(), angg, " See you there ");

        MessagingService.ConversationPreview preview = messaging.inboxFor(zuko).get(0);
        assertEquals("Angg Air", preview.other().getFullName());
        assertEquals("See you there", preview.lastMessage());
    }

    @Test
    void aNewMessageThreadIsShownBeforeOtherMessages() {
        going.markGoing(angg, eventId);
        going.markGoing(zuko, eventId);
        messaging.openConversation(angg, zuko);

        MessagingService.ConversationPreview preview = messaging.inboxFor(zuko).get(0);
        assertEquals("Angg Air", preview.other().getFullName());
        assertEquals("", preview.lastMessage());
    }

    @Test
    void onlyTwoPeopleInConversation() {
        int appa = users.insert("Appa", "Air", "appa@gmail.com", "hash");
        conversations.findOrCreate(angg, zuko);

        assertEquals(1, messaging.inboxFor(angg).size());
        assertEquals(1, messaging.inboxFor(zuko).size());
        assertTrue(messaging.inboxFor(appa).isEmpty());
    }

    @Test
    void noBlankMessages() {
        Conversation chat = conversations.findOrCreate(angg, zuko);
        assertEquals(MessagingService.EMPTY_MESSAGE, messaging.send(chat.getConversationId(), angg, " ").orElseThrow());
        assertTrue(messaging.openChat(chat.getConversationId(), angg).isEmpty());
    }

    @Test
    void outsiderCannotSendOrRead() {
        int appa = users.insert("Appa", "Air", "appa@gmail.com", "hash");
        Conversation chat = conversations.findOrCreate(angg, zuko);

        assertEquals(MessagingService.NOT_A_PARTICIPANT, messaging.send(chat.getConversationId(), appa, "hi").orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> messaging.openChat(chat.getConversationId(), appa));
        assertTrue(messaging.openChat(chat.getConversationId(), angg).isEmpty());
    }

    @Test
    void openingChatOnlyMarksOtherPersonsMessageAsRead() {
        Conversation chat = conversations.findOrCreate(angg, zuko);
        messaging.send(chat.getConversationId(), angg, "Hi");
        messaging.send(chat.getConversationId(), zuko, "Hey");

        List<Message> thread = messaging.openChat(chat.getConversationId(), zuko);
        assertNotNull(thread.get(0).getReadAt());
        assertNull(thread.get(1).getReadAt());
    }
}
