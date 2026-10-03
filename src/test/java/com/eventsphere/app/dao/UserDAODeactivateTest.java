package com.eventsphere.app.dao;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.User;
import com.eventsphere.app.model.Event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAODeactivateTest {
    private Connection connection;
    private UserDAO users;
    private int eventId;

    @BeforeEach
    public void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
        }
        new DBController(connection);
        users = new UserDAO(connection);

        int sourceId = new SourceDAO(connection).insert("Test", "https://example.com");
        eventId = new EventDAO(connection).insert(new Event("Test Event", null, Category.MUSIC, Instant.parse("2026-08-01T09:30:00Z"), null, null, null, null, null, null, null, sourceId));
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void deletesPersonalData() {
        int userId = users.insert("Angg", "Air", "angg@email.com", "SecretPassword2", 153.0287, -27.4699);
        LikeDAO likes = new LikeDAO(connection);
        GoingDAO going = new GoingDAO(connection);
        PreferenceDAO preferences = new PreferenceDAO(connection);
        CommentDAO comments = new CommentDAO(connection);

        likes.like(userId, eventId);
        going.markGoing(userId, eventId);
        preferences.upsertBio(userId, "Hello");
        preferences.replaceCategories(userId, Set.of(Category.MUSIC));
        comments.insert(userId, eventId, "See you there", null);

        users.deactivate(userId);

        User user = users.findById(userId).orElseThrow();
        assertFalse(user.isActive());
        assertNull(user.getHomeLat());
        assertNull(user.getHomeLong());
        assertFalse(likes.isLikedBy(userId, eventId));
        assertEquals(0, likes.countForEvent(eventId));
        assertTrue(going.findEventsForUser(userId).isEmpty());
        assertNull(preferences.findByUser(userId).getBio());
        assertTrue(preferences.findByUser(userId).getCategories().isEmpty());
        assertEquals(1, comments.findByEvent(eventId).size());
    }
}
