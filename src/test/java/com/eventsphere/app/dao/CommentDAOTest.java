package com.eventsphere.app.dao;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Comment;
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

class CommentDAOTest {

    private Connection connection;
    private CommentDAO comments;
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
        comments = new CommentDAO(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void insertReturnsGeneratedId() {
        int id = comments.insert(userId, eventId, "Great event!", null);
        assertTrue(id > 0);
    }

    @Test
    void findByEventReturnsInsertedComments() {
        comments.insert(userId, eventId, "First comment", null);
        comments.insert(userId, eventId, "Second comment", null);

        List<Comment> found = comments.findByEvent(eventId);
        assertEquals(2, found.size());
    }

    @Test
    void findByEventEmptyWhenNoComments() {
        assertTrue(comments.findByEvent(eventId).isEmpty());
    }

    @Test
    void replyCommentStoresParentId() {
        int parentId = comments.insert(userId, eventId, "Parent", null);
        int replyId = comments.insert(userId, eventId, "Reply", parentId);

        Comment reply = comments.findByEvent(eventId).stream()
                .filter(c -> c.getCommentId() == replyId)
                .findFirst().orElseThrow();

        assertEquals(parentId, reply.getReplyToCommentId());
    }

    @Test
    void topLevelCommentHasNullReplyToId() {
        int id = comments.insert(userId, eventId, "Top level", null);
        Comment comment = comments.findByEvent(eventId).get(0);
        assertNull(comment.getReplyToCommentId());
    }

    @Test
    void updateChangesContent() {
        int id = comments.insert(userId, eventId, "Original", null);
        Comment comment = comments.findByEvent(eventId).get(0);
        comment.setContent("Updated");
        comments.update(comment);

        Comment reloaded = comments.findByEvent(eventId).get(0);
        assertEquals("Updated", reloaded.getContent());
    }

    @Test
    void deleteRemovesComment() {
        int id = comments.insert(userId, eventId, "To delete", null);
        comments.delete(id);
        assertTrue(comments.findByEvent(eventId).isEmpty());
    }

    @Test
    void findByEventDoesNotReturnOtherEventsComments() {
        int sourceId = new SourceDAO(connection).findBySiteName("Test").orElseThrow().getSourceId();
        int otherEvent = new EventDAO(connection).insert(
                new Event("Other", null, Category.SPORTS, Instant.parse("2027-07-01T10:00:00Z"),
                        null, null, null, null, null, null, null, sourceId));

        comments.insert(userId, eventId, "Mine", null);
        comments.insert(userId, otherEvent, "Theirs", null);

        assertEquals(1, comments.findByEvent(eventId).size());
    }
}
