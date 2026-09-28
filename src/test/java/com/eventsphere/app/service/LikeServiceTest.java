package com.eventsphere.app.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.dao.EventDAO;
import com.eventsphere.app.dao.LikeDAO;
import com.eventsphere.app.dao.SourceDAO;
import com.eventsphere.app.dao.UserDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;

// SCRUM-58: likes are stored per (user, event) and the trigger-maintained count follows.
class LikeServiceTest {

    private static final Instant START = Instant.parse("2026-10-01T09:30:00Z");

    private Connection connection;
    private LikeDAO likeDao;
    private LikeService likes;
    private int userId;
    private int eventId;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
        }
        new DBController(connection);

        likeDao = new LikeDAO(connection);
        likes = new LikeService(likeDao);

        int sourceId = new SourceDAO(connection).insert("Test", "https://example.com");
        userId = new UserDAO(connection).insert("Ada", "Lovelace", "ada@example.com", "hash");
        eventId = new EventDAO(connection).insert(new Event("Test Event", null, Category.MUSIC,
                START, null, null, null, null, null, null, null, sourceId));
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void likingAddsALikeAndShowsTheNewCount() {
        LikeResult result = likes.toggleLike(userId, eventId);

        assertTrue(result.liked());
        assertEquals(1, result.count());
        assertTrue(likeDao.isLikedBy(userId, eventId));
    }

    @Test
    void togglingAgainRemovesTheLikeAndDropsTheCount() {
        likes.toggleLike(userId, eventId);

        LikeResult result = likes.toggleLike(userId, eventId);

        assertFalse(result.liked());
        assertEquals(0, result.count());
        assertFalse(likeDao.isLikedBy(userId, eventId));
    }

    @Test
    void theSameUserCannotLikeTheSameEventTwice() {
        // A duplicate row is dropped by INSERT OR IGNORE, so the insert-count trigger only
        // fires once and the stored count stays at 1.
        assertTrue(likeDao.like(userId, eventId));
        assertFalse(likeDao.like(userId, eventId));

        assertEquals(1, likes.stateFor(userId, eventId).count());
        // Toggling once after a duplicate attempt still unlikes.
        assertFalse(likes.toggleLike(userId, eventId).liked());
        assertEquals(0, likes.stateFor(userId, eventId).count());
    }

    @Test
    void stateWithoutALoggedInUserIsNotLikedButKeepsTheStoredCount() {
        likes.toggleLike(userId, eventId);

        LikeResult result = likes.stateFor(null, eventId);

        assertFalse(result.liked());
        assertEquals(1, result.count());
    }

    @Test
    void likesAndCountsAreScopedToOneEvent() {
        int sourceId = new SourceDAO(connection).insert("Other", "https://example.com/other");
        int otherEventId = new EventDAO(connection).insert(new Event("Other Event", null, Category.MUSIC,
                START, null, null, null, null, null, null, null, sourceId));

        likes.toggleLike(userId, eventId);

        assertFalse(likes.stateFor(userId, otherEventId).liked());
        assertEquals(0, likes.stateFor(userId, otherEventId).count());
    }
}
