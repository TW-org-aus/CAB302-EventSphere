package com.eventsphere.app.dao;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LikeDAOTest {

    private Connection connection;
    private LikeDAO likes;
    private int userId;
    private int eventId;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        }
        new DBController(connection);

        userId = new UserDAO(connection).insert("Alice", "Smith", "alice@example.com", "hash", null, null);
        int sourceId = new SourceDAO(connection).insert("Test", "https://example.com");
        eventId = new EventDAO(connection).insert(
                new Event("Concert", null, Category.MUSIC, Instant.parse("2027-06-01T10:00:00Z"),
                        null, null, null, null, null, null, null, sourceId));
        likes = new LikeDAO(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void likeReturnsTrueFirstTime() {
        assertTrue(likes.like(userId, eventId));
    }

    @Test
    void likeReturnsFalseOnDuplicate() {
        likes.like(userId, eventId);
        assertFalse(likes.like(userId, eventId));
    }

    @Test
    void unlikeReturnsTrueWhenLikeExists() {
        likes.like(userId, eventId);
        assertTrue(likes.unlike(userId, eventId));
    }

    @Test
    void unlikeReturnsFalseWhenNotLiked() {
        assertFalse(likes.unlike(userId, eventId));
    }

    @Test
    void isLikedByTrueAfterLike() {
        likes.like(userId, eventId);
        assertTrue(likes.isLikedBy(userId, eventId));
    }

    @Test
    void isLikedByFalseAfterUnlike() {
        likes.like(userId, eventId);
        likes.unlike(userId, eventId);
        assertFalse(likes.isLikedBy(userId, eventId));
    }

    @Test
    void findEventsLikedByUserReturnsLikedEvent() {
        likes.like(userId, eventId);
        List<Event> found = likes.findEventsLikedByUser(userId);
        assertEquals(1, found.size());
        assertEquals(eventId, found.get(0).getEventId());
    }

    @Test
    void findEventsLikedByUserEmptyWhenNoLikes() {
        assertTrue(likes.findEventsLikedByUser(userId).isEmpty());
    }

    @Test
    void findEventsLikedByUserDoesNotReturnUnlikedEvent() {
        int sourceId = new SourceDAO(connection).findBySiteName("Test").orElseThrow().getSourceId();
        int otherEvent = new EventDAO(connection).insert(
                new Event("Other", null, Category.SPORTS, Instant.parse("2027-07-01T10:00:00Z"),
                        null, null, null, null, null, null, null, sourceId));

        likes.like(userId, eventId);
        // otherEvent not liked

        List<Event> found = likes.findEventsLikedByUser(userId);
        assertEquals(1, found.size());
        assertEquals(eventId, found.get(0).getEventId());
    }
}
