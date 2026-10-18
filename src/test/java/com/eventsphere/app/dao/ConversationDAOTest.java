package com.eventsphere.app.dao;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.model.Conversation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConversationDAOTest {

    private Connection connection;
    private ConversationDAO conversations;
    private int alice;
    private int bob;
    private int carol;

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
        carol = users.insert("Carol", "C", "carol@example.com", "hash", null, null);
        conversations = new ConversationDAO(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void findOrCreateCreatesNewConversation() {
        Conversation c = conversations.findOrCreate(alice, bob);
        assertTrue(c.getConversationId() > 0);
    }

    @Test
    void findOrCreateReturnsSameConversationOnSecondCall() {
        Conversation first  = conversations.findOrCreate(alice, bob);
        Conversation second = conversations.findOrCreate(alice, bob);
        assertEquals(first, second);
    }

    @Test
    void findOrCreateNormalisesArgumentOrder() {
        Conversation ab = conversations.findOrCreate(alice, bob);
        Conversation ba = conversations.findOrCreate(bob, alice);
        assertEquals(ab, ba);
    }

    @Test
    void findOrCreateThrowsForSameUser() {
        assertThrows(IllegalArgumentException.class, () -> conversations.findOrCreate(alice, alice));
    }

    @Test
    void findByUserReturnsConversationsForThatUser() {
        conversations.findOrCreate(alice, bob);
        conversations.findOrCreate(alice, carol);

        List<Conversation> aliceConvos = conversations.findByUser(alice);
        assertEquals(2, aliceConvos.size());
    }

    @Test
    void findByUserExcludesDeactivatedPartner() {
        conversations.findOrCreate(alice, bob);
        new UserDAO(connection).deactivate(bob);

        List<Conversation> aliceConvos = conversations.findByUser(alice);
        assertTrue(aliceConvos.isEmpty());
    }

    @Test
    void isParticipantTrueForBothUsers() {
        Conversation c = conversations.findOrCreate(alice, bob);
        assertTrue(conversations.isParticipant(c.getConversationId(), alice));
        assertTrue(conversations.isParticipant(c.getConversationId(), bob));
    }

    @Test
    void isParticipantFalseForOutsider() {
        Conversation c = conversations.findOrCreate(alice, bob);
        assertFalse(conversations.isParticipant(c.getConversationId(), carol));
    }

    @Test
    void findByIdReturnsPresentForExisting() {
        Conversation c = conversations.findOrCreate(alice, bob);
        assertTrue(conversations.findById(c.getConversationId()).isPresent());
    }

    @Test
    void findByIdReturnsEmptyForUnknown() {
        assertTrue(conversations.findById(9999).isEmpty());
    }
}
