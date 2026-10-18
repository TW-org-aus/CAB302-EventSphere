package com.eventsphere.app.dao;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.model.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageDAOTest {

    private Connection connection;
    private MessageDAO messages;
    private int alice;
    private int bob;
    private int conversationId;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        }
        new DBController(connection);

        UserDAO users = new UserDAO(connection);
        alice = users.insert("Alice", "A", "alice@example.com", "hash", null, null);
        bob   = users.insert("Bob",   "B", "bob@example.com",   "hash", null, null);
        conversationId = new ConversationDAO(connection).findOrCreate(alice, bob).getConversationId();
        messages = new MessageDAO(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void insertReturnsGeneratedId() {
        int id = messages.insert(conversationId, alice, "Hello");
        assertTrue(id > 0);
    }

    @Test
    void findByConversationReturnsMessagesInOrder() {
        messages.insert(conversationId, alice, "First");
        messages.insert(conversationId, bob,   "Second");

        List<Message> found = messages.findByConversation(conversationId);
        assertEquals(2, found.size());
        assertEquals("First",  found.get(0).getContent());
        assertEquals("Second", found.get(1).getContent());
    }

    @Test
    void findByConversationEmptyWhenNoMessages() {
        assertTrue(messages.findByConversation(conversationId).isEmpty());
    }

    @Test
    void markReadSetsReadAtOnOtherPersonsMessages() {
        messages.insert(conversationId, alice, "Hi Bob");
        messages.markRead(conversationId, bob);

        Message msg = messages.findByConversation(conversationId).get(0);
        assertNotNull(msg.getReadAt());
    }

    @Test
    void markReadDoesNotSetReadAtOnSendersOwnMessages() {
        messages.insert(conversationId, alice, "Hi Bob");
        // Alice reads — but the message was sent by Alice, so it should not be marked.
        messages.markRead(conversationId, alice);

        Message msg = messages.findByConversation(conversationId).get(0);
        assertNull(msg.getReadAt());
    }

    @Test
    void markReadOnlyAffectsUnreadMessages() {
        messages.insert(conversationId, alice, "First");
        messages.markRead(conversationId, bob);
        // Second message sent after the first read
        messages.insert(conversationId, alice, "Second");

        List<Message> found = messages.findByConversation(conversationId);
        assertNotNull(found.get(0).getReadAt());
        assertNull(found.get(1).getReadAt());
    }
}
