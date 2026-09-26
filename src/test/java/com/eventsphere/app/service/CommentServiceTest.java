package com.eventsphere.app.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.dao.CommentDAO;
import com.eventsphere.app.dao.EventDAO;
import com.eventsphere.app.dao.SourceDAO;
import com.eventsphere.app.dao.UserDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Comment;
import com.eventsphere.app.model.Event;

// SCRUM-58: comments, plus the one-reply-per-comment / no-reply-to-a-reply rules.
class CommentServiceTest {

    private static final Instant START = Instant.parse("2026-10-01T09:30:00Z");

    private Connection connection;
    private CommentService comments;
    private int userId;
    private int otherUserId;
    private int eventId;
    private int otherEventId;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
        }
        new DBController(connection);

        UserDAO users = new UserDAO(connection);
        comments = new CommentService(new CommentDAO(connection), users);

        int sourceId = new SourceDAO(connection).insert("Test", "https://example.com");
        userId = users.insert("Ada", "Lovelace", "ada@example.com", "hash");
        otherUserId = users.insert("Grace", "Hopper", "grace@example.com", "hash");

        EventDAO events = new EventDAO(connection);
        eventId = events.insert(event("Test Event", sourceId));
        otherEventId = events.insert(event("Other Event", sourceId));
    }

    private static Event event(String title, int sourceId) {
        return new Event(title, null, Category.MUSIC, START, null, null, null,
                null, null, null, null, sourceId);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    private Comment commentById(int commentId) {
        return comments.commentsForEvent(eventId).stream()
                .filter(comment -> comment.getCommentId() == commentId)
                .findFirst()
                .orElseThrow();
    }

    @Test
    void postingATopLevelCommentStoresItAndReturnsItsId() {
        int commentId = comments.postComment(userId, eventId, "Looking forward to this!", null);

        Comment stored = commentById(commentId);
        assertEquals("Looking forward to this!", stored.getContent());
        assertEquals(userId, stored.getUserId());
        assertNull(stored.getReplyToCommentId());
    }

    @Test
    void postingTrimsSurroundingWhitespace() {
        int commentId = comments.postComment(userId, eventId, "   spaced out   ", null);

        assertEquals("spaced out", commentById(commentId).getContent());
    }

    @Test
    void blankCommentsAreRejected() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> comments.postComment(userId, eventId, "   ", null));

        assertEquals(CommentService.EMPTY_CONTENT, error.getMessage());
    }

    @Test
    void commentsAreScopedToTheirEvent() {
        comments.postComment(userId, eventId, "Here", null);
        comments.postComment(userId, otherEventId, "There", null);

        assertEquals(1, comments.commentsForEvent(eventId).size());
        assertEquals("Here", comments.commentsForEvent(eventId).get(0).getContent());
    }

    @Test
    void aReplyIsStoredAgainstItsParentComment() {
        int parentId = comments.postComment(userId, eventId, "Anyone going?", null);

        int replyId = comments.postComment(otherUserId, eventId, "Yes, see you there.", parentId);

        Comment reply = commentById(replyId);
        assertEquals(parentId, (int) reply.getReplyToCommentId());
        assertEquals(otherUserId, reply.getUserId());
    }

    @Test
    void aCommentCanOnlyHaveOneReply() {
        int parentId = comments.postComment(userId, eventId, "Anyone going?", null);
        comments.postComment(otherUserId, eventId, "Yes, see you there.", parentId);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> comments.postComment(otherUserId, eventId, "Me too!", parentId));

        assertEquals(CommentService.REPLY_ALREADY_EXISTS, error.getMessage());
        assertEquals(2, comments.commentsForEvent(eventId).size());
    }

    @Test
    void aReplyCannotBeRepliedTo() {
        int parentId = comments.postComment(userId, eventId, "Anyone going?", null);
        int replyId = comments.postComment(otherUserId, eventId, "Yes, see you there.", parentId);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> comments.postComment(userId, eventId, "Great!", replyId));

        assertEquals(CommentService.REPLY_TO_REPLY, error.getMessage());
        assertEquals(2, comments.commentsForEvent(eventId).size());
    }

    @Test
    void theReplyTargetMustBelongToTheSameEvent() {
        int commentOnOtherEvent = comments.postComment(userId, otherEventId, "Elsewhere", null);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> comments.postComment(userId, eventId, "Wrong place", commentOnOtherEvent));

        assertEquals(CommentService.REPLY_TARGET_MISSING, error.getMessage());
    }

    @Test
    void aDeletedReplyTargetIsReportedInsteadOfThrown() {
        int parentId = comments.postComment(userId, eventId, "Anyone going?", null);
        new CommentDAO(connection).delete(parentId);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> comments.postComment(otherUserId, eventId, "Still going?", parentId));

        assertEquals(CommentService.REPLY_TARGET_MISSING, error.getMessage());
    }

    @Test
    void authorNamesForResolvesEachAuthorOnce() {
        int parentId = comments.postComment(userId, eventId, "One", null);
        comments.postComment(userId, eventId, "Two", null);
        comments.postComment(otherUserId, eventId, "Three", parentId);

        Map<Integer, String> names = comments.authorNamesFor(comments.commentsForEvent(eventId));

        assertEquals("Ada Lovelace", names.get(userId));
        assertEquals("Grace Hopper", names.get(otherUserId));
        assertEquals(2, names.size());
    }
}
