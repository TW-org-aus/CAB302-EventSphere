package com.eventsphere.app.dao;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.Notification;
import com.eventsphere.app.model.NotificationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationDAOTest {

    private static final Instant EVENT_START = Instant.parse("2027-01-01T10:00:00Z");

    private Connection connection;
    private NotificationDAO notifications;
    private int userId;
    private int otherUserId;
    private int eventId;
    private int otherEventId;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        }
        new DBController(connection);

        UserDAO users = new UserDAO(connection);
        userId = users.insert("Alice", "Smith", "alice@example.com", "hash", null, null, null);
        otherUserId = users.insert("Bob", "Jones", "bob@example.com", "hash", null, null, null);

        int sourceId = new SourceDAO(connection).insert("Test", "https://example.com");
        EventDAO events = new EventDAO(connection);
        eventId = events.insert(new Event("Concert", null, Category.MUSIC, EVENT_START, null, null, null, null, null, null, null, sourceId));
        otherEventId = events.insert(new Event("Market", null, Category.MUSIC, EVENT_START, null, null, null, null, null, null, null, sourceId));

        notifications = new NotificationDAO(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    // --- insert / findByUser ---

    @Test
    void insertReturnsGeneratedId() {
        int id = notifications.insert(userId, NotificationType.NEW_MESSAGE, null, null, null, "You have a new message");
        assertTrue(id > 0);
    }

    @Test
    void insertedNotificationIsReturnedByFindByUser() {
        notifications.insert(userId, NotificationType.NEW_MESSAGE, null, null, null, "You have a new message");

        List<Notification> found = notifications.findByUser(userId);

        assertEquals(1, found.size());
        Notification n = found.get(0);
        assertEquals(userId, n.getUserId());
        assertEquals(NotificationType.NEW_MESSAGE, n.getType());
        assertEquals("You have a new message", n.getMessage());
        assertFalse(n.isRead());
    }

    @Test
    void insertStoresRelatedEventId() {
        notifications.insert(userId, NotificationType.EVENT_REMINDER_WEEK, eventId, null, null, "Concert is happening in 1 week");

        Notification n = notifications.findByUser(userId).get(0);
        assertEquals(eventId, n.getRelatedEventId());
        assertNull(n.getRelatedCommentId());
        assertNull(n.getRelatedConversationId());
    }

    @Test
    void insertWithAllNullRelatedIds() {
        notifications.insert(userId, NotificationType.NEW_MESSAGE, null, null, null, "msg");

        Notification n = notifications.findByUser(userId).get(0);
        assertNull(n.getRelatedEventId());
        assertNull(n.getRelatedCommentId());
        assertNull(n.getRelatedConversationId());
    }

    @Test
    void findByUserReturnsNewestFirst() {
        int first = notifications.insert(userId, NotificationType.NEW_MESSAGE, null, null, null, "first");
        int second = notifications.insert(userId, NotificationType.NEW_MESSAGE, null, null, null, "second");

        List<Notification> found = notifications.findByUser(userId);

        // Both inserted in same second, so secondary sort on NotificationID DESC applies.
        assertTrue(found.get(0).getNotificationId() > found.get(1).getNotificationId());
        assertEquals(second, found.get(0).getNotificationId());
        assertEquals(first, found.get(1).getNotificationId());
    }

    @Test
    void findByUserIsScopedToUser() {
        notifications.insert(userId, NotificationType.NEW_MESSAGE, null, null, null, "for alice");
        notifications.insert(otherUserId, NotificationType.NEW_MESSAGE, null, null, null, "for bob");

        assertEquals(1, notifications.findByUser(userId).size());
        assertEquals("for alice", notifications.findByUser(userId).get(0).getMessage());
        assertEquals(1, notifications.findByUser(otherUserId).size());
    }

    @Test
    void findByUserReturnsEmptyListForUserWithNoNotifications() {
        assertTrue(notifications.findByUser(userId).isEmpty());
    }

    @Test
    void allNotificationTypesCanBeInserted() {
        for (NotificationType type : NotificationType.values()) {
            Integer relatedEventId = (type == NotificationType.COMMENT_REPLY ||
                    type == NotificationType.EVENT_REMINDER_WEEK ||
                    type == NotificationType.EVENT_REMINDER_DAY ||
                    type == NotificationType.EVENT_REMINDER_12H ||
                    type == NotificationType.EVENT_UPDATED) ? eventId : null;
            assertDoesNotThrow(() ->
                notifications.insert(userId, type, relatedEventId, null, null, "test " + type.getDbValue())
            );
        }
        assertEquals(NotificationType.values().length, notifications.findByUser(userId).size());
    }

    // --- markRead ---

    @Test
    void markReadSetsIsReadFlag() {
        int id = notifications.insert(userId, NotificationType.NEW_MESSAGE, null, null, null, "msg");
        assertFalse(notifications.findByUser(userId).get(0).isRead());

        notifications.markRead(id);

        assertTrue(notifications.findByUser(userId).get(0).isRead());
    }

    @Test
    void markReadDoesNotAffectOtherNotifications() {
        int id1 = notifications.insert(userId, NotificationType.NEW_MESSAGE, null, null, null, "one");
        int id2 = notifications.insert(userId, NotificationType.NEW_MESSAGE, null, null, null, "two");

        notifications.markRead(id1);

        List<Notification> found = notifications.findByUser(userId);
        Notification n1 = found.stream().filter(n -> n.getNotificationId() == id1).findFirst().orElseThrow();
        Notification n2 = found.stream().filter(n -> n.getNotificationId() == id2).findFirst().orElseThrow();
        assertTrue(n1.isRead());
        assertFalse(n2.isRead());
    }

    // --- markAllRead ---

    @Test
    void markAllReadSetsAllUnreadForUser() {
        notifications.insert(userId, NotificationType.NEW_MESSAGE, null, null, null, "one");
        notifications.insert(userId, NotificationType.NEW_MESSAGE, null, null, null, "two");
        notifications.insert(userId, NotificationType.NEW_MESSAGE, null, null, null, "three");

        notifications.markAllRead(userId);

        notifications.findByUser(userId).forEach(n -> assertTrue(n.isRead()));
    }

    @Test
    void markAllReadDoesNotAffectOtherUser() {
        notifications.insert(userId, NotificationType.NEW_MESSAGE, null, null, null, "alice");
        notifications.insert(otherUserId, NotificationType.NEW_MESSAGE, null, null, null, "bob");

        notifications.markAllRead(userId);

        assertFalse(notifications.findByUser(otherUserId).get(0).isRead());
    }

    @Test
    void markAllReadOnUserWithNoUnreadNotificationsDoesNotThrow() {
        int id = notifications.insert(userId, NotificationType.NEW_MESSAGE, null, null, null, "msg");
        notifications.markRead(id);

        assertDoesNotThrow(() -> notifications.markAllRead(userId));

        assertTrue(notifications.findByUser(userId).get(0).isRead());
    }

    @Test
    void markAllReadOnUserWithNoNotificationsDoesNotThrow() {
        assertDoesNotThrow(() -> notifications.markAllRead(userId));
    }

    // --- existsForEvent ---

    @Test
    void existsForEventReturnsTrueWhenRowExists() {
        notifications.insert(userId, NotificationType.EVENT_REMINDER_WEEK, eventId, null, null, "Concert is happening in 1 week");

        assertTrue(notifications.existsForEvent(userId, NotificationType.EVENT_REMINDER_WEEK, eventId));
    }

    @Test
    void existsForEventReturnsFalseWhenNoRow() {
        assertFalse(notifications.existsForEvent(userId, NotificationType.EVENT_REMINDER_WEEK, eventId));
    }

    @Test
    void existsForEventIsScopedToNotificationType() {
        notifications.insert(userId, NotificationType.EVENT_REMINDER_WEEK, eventId, null, null, "week");

        assertFalse(notifications.existsForEvent(userId, NotificationType.EVENT_REMINDER_DAY, eventId));
        assertFalse(notifications.existsForEvent(userId, NotificationType.EVENT_REMINDER_12H, eventId));
    }

    @Test
    void existsForEventIsScopedToUser() {
        notifications.insert(userId, NotificationType.EVENT_REMINDER_WEEK, eventId, null, null, "week");

        assertFalse(notifications.existsForEvent(otherUserId, NotificationType.EVENT_REMINDER_WEEK, eventId));
    }

    @Test
    void existsForEventIsScopedToEvent() {
        notifications.insert(userId, NotificationType.EVENT_REMINDER_WEEK, eventId, null, null, "concert");

        assertFalse(notifications.existsForEvent(userId, NotificationType.EVENT_REMINDER_WEEK, otherEventId));
    }

    @Test
    void existsForEventReturnsFalseWhenRelatedEventIdIsNull() {
        notifications.insert(userId, NotificationType.NEW_MESSAGE, null, null, null, "msg");

        // RelatedEventID is null, so no event match is possible.
        assertFalse(notifications.existsForEvent(userId, NotificationType.NEW_MESSAGE, eventId));
    }
}
