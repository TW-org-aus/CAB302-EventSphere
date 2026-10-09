package com.eventsphere.app.service;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.dao.EventDAO;
import com.eventsphere.app.dao.GoingDAO;
import com.eventsphere.app.dao.NotificationDAO;
import com.eventsphere.app.dao.SourceDAO;
import com.eventsphere.app.dao.UserDAO;
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
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private Connection connection;
    private NotificationService service;
    private NotificationDAO notificationDAO;
    private GoingDAO goingDAO;
    private EventDAO eventDAO;
    private int userId;
    private int otherUserId;
    private int sourceId;

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

        sourceId = new SourceDAO(connection).insert("Test", "https://example.com");
        eventDAO = new EventDAO(connection);
        goingDAO = new GoingDAO(connection);
        notificationDAO = new NotificationDAO(connection);
        service = new NotificationService(notificationDAO, goingDAO, eventDAO);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    private int futureEvent(String title, Instant start) {
        return eventDAO.insert(new Event(title, null, Category.MUSIC, start, null, null, null, null, null, null, null, sourceId));
    }

    // --- getForUser ---

    @Test
    void getForUserReturnsEmptyListWhenNoneExist() {
        assertTrue(service.getForUser(userId).isEmpty());
    }

    @Test
    void getForUserReturnsAllNotificationsForUser() {
        service.create(userId, NotificationType.NEW_MESSAGE, null, null, null, "msg 1");
        service.create(userId, NotificationType.NEW_MESSAGE, null, null, null, "msg 2");

        assertEquals(2, service.getForUser(userId).size());
    }

    @Test
    void getForUserDoesNotReturnOtherUsersNotifications() {
        service.create(userId, NotificationType.NEW_MESSAGE, null, null, null, "for alice");
        service.create(otherUserId, NotificationType.NEW_MESSAGE, null, null, null, "for bob");

        assertEquals(1, service.getForUser(userId).size());
        assertEquals("for alice", service.getForUser(userId).get(0).getMessage());
    }

    // --- create ---

    @Test
    void createStoresNotificationWithCorrectType() {
        int eventId = futureEvent("Concert", Instant.now().plus(5, ChronoUnit.DAYS));
        service.create(userId, NotificationType.COMMENT_REPLY, eventId, null, null, "Alice replied");

        Notification n = service.getForUser(userId).get(0);
        assertEquals(NotificationType.COMMENT_REPLY, n.getType());
        assertEquals(eventId, n.getRelatedEventId());
        assertEquals("Alice replied", n.getMessage());
        assertFalse(n.isRead());
    }

    @Test
    void createStoresNullRelatedIds() {
        service.create(userId, NotificationType.NEW_MESSAGE, null, null, null, "hi");

        Notification n = service.getForUser(userId).get(0);
        assertNull(n.getRelatedEventId());
        assertNull(n.getRelatedCommentId());
        assertNull(n.getRelatedConversationId());
    }

    // --- markAllRead ---

    @Test
    void markAllReadMarksEveryUnreadNotification() {
        service.create(userId, NotificationType.NEW_MESSAGE, null, null, null, "one");
        service.create(userId, NotificationType.NEW_MESSAGE, null, null, null, "two");
        service.create(userId, NotificationType.NEW_MESSAGE, null, null, null, "three");
        service.getForUser(userId).forEach(n -> assertFalse(n.isRead()));

        service.markAllRead(userId);

        service.getForUser(userId).forEach(n -> assertTrue(n.isRead()));
    }

    @Test
    void markAllReadDoesNotAffectOtherUser() {
        service.create(userId, NotificationType.NEW_MESSAGE, null, null, null, "alice");
        service.create(otherUserId, NotificationType.NEW_MESSAGE, null, null, null, "bob");

        service.markAllRead(userId);

        assertFalse(service.getForUser(otherUserId).get(0).isRead());
    }

    @Test
    void markAllReadOnUserWithNoNotificationsDoesNotThrow() {
        assertDoesNotThrow(() -> service.markAllRead(userId));
    }

    // --- checkAndInsertReminders ---

    @Test
    void noRemindersInsertedForUserWithNoGoingEvents() {
        service.checkAndInsertReminders(userId);
        assertTrue(service.getForUser(userId).isEmpty());
    }

    @Test
    void weekReminderInsertedForEventThreeDaysAway() {
        Instant inThreeDays = Instant.now().plus(3, ChronoUnit.DAYS);
        int eventId = futureEvent("Concert", inThreeDays);
        goingDAO.markGoing(userId, eventId);

        service.checkAndInsertReminders(userId);

        List<Notification> found = service.getForUser(userId);
        assertEquals(1, found.size());
        assertEquals(NotificationType.EVENT_REMINDER_WEEK, found.get(0).getType());
        assertEquals(eventId, found.get(0).getRelatedEventId());
        assertTrue(found.get(0).getMessage().contains("Concert"));
        assertTrue(found.get(0).getMessage().contains("1 week"));
    }

    @Test
    void dayReminderInsertedForEventEighteenHoursAway() {
        Instant in18Hours = Instant.now().plus(18, ChronoUnit.HOURS);
        int eventId = futureEvent("Market", in18Hours);
        goingDAO.markGoing(userId, eventId);

        service.checkAndInsertReminders(userId);

        List<Notification> found = service.getForUser(userId);
        assertEquals(1, found.size());
        assertEquals(NotificationType.EVENT_REMINDER_DAY, found.get(0).getType());
        assertTrue(found.get(0).getMessage().contains("tomorrow"));
    }

    @Test
    void twelveHourReminderInsertedForEventSixHoursAway() {
        Instant in6Hours = Instant.now().plus(6, ChronoUnit.HOURS);
        int eventId = futureEvent("Festival", in6Hours);
        goingDAO.markGoing(userId, eventId);

        service.checkAndInsertReminders(userId);

        List<Notification> found = service.getForUser(userId);
        assertEquals(1, found.size());
        assertEquals(NotificationType.EVENT_REMINDER_12H, found.get(0).getType());
        assertTrue(found.get(0).getMessage().contains("12 hours"));
    }

    @Test
    void noReminderForEventMoreThanSevenDaysAway() {
        Instant in14Days = Instant.now().plus(14, ChronoUnit.DAYS);
        int eventId = futureEvent("FarFuture", in14Days);
        goingDAO.markGoing(userId, eventId);

        service.checkAndInsertReminders(userId);

        assertTrue(service.getForUser(userId).isEmpty());
    }

    @Test
    void noReminderForPastEvent() {
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        int eventId = futureEvent("PastEvent", yesterday);
        goingDAO.markGoing(userId, eventId);

        service.checkAndInsertReminders(userId);

        assertTrue(service.getForUser(userId).isEmpty());
    }

    @Test
    void checkAndInsertRemindersIsIdempotent() {
        Instant inThreeDays = Instant.now().plus(3, ChronoUnit.DAYS);
        int eventId = futureEvent("Concert", inThreeDays);
        goingDAO.markGoing(userId, eventId);

        service.checkAndInsertReminders(userId);
        service.checkAndInsertReminders(userId);
        service.checkAndInsertReminders(userId);

        assertEquals(1, service.getForUser(userId).size());
    }

    @Test
    void reminderIsOnlySentToTheGoingUser() {
        Instant inThreeDays = Instant.now().plus(3, ChronoUnit.DAYS);
        int eventId = futureEvent("Concert", inThreeDays);
        goingDAO.markGoing(userId, eventId);
        // otherUser is NOT going

        service.checkAndInsertReminders(userId);
        service.checkAndInsertReminders(otherUserId);

        assertEquals(1, service.getForUser(userId).size());
        assertTrue(service.getForUser(otherUserId).isEmpty());
    }

    @Test
    void remindersForMultipleGoingEventsAllInserted() {
        Instant inTwoDays = Instant.now().plus(2, ChronoUnit.DAYS);
        Instant inFourDays = Instant.now().plus(4, ChronoUnit.DAYS);
        int event1 = futureEvent("EventA", inTwoDays);
        int event2 = futureEvent("EventB", inFourDays);
        goingDAO.markGoing(userId, event1);
        goingDAO.markGoing(userId, event2);

        service.checkAndInsertReminders(userId);

        List<Notification> found = service.getForUser(userId);
        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(n -> n.getType() == NotificationType.EVENT_REMINDER_WEEK));
    }

    @Test
    void weekAndDayRemindersAreSeparateInsertionsAtDifferentTimes() {
        // Simulate: event was 3 days away before (week reminder sent), now 18h away.
        Instant in18Hours = Instant.now().plus(18, ChronoUnit.HOURS);
        int eventId = futureEvent("Concert", in18Hours);
        goingDAO.markGoing(userId, eventId);

        // Manually pre-insert the week reminder (as if from a prior session).
        notificationDAO.insert(userId, NotificationType.EVENT_REMINDER_WEEK, eventId, null, null, "Concert is happening in 1 week");

        service.checkAndInsertReminders(userId);

        List<Notification> found = service.getForUser(userId);
        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(n -> n.getType() == NotificationType.EVENT_REMINDER_WEEK));
        assertTrue(found.stream().anyMatch(n -> n.getType() == NotificationType.EVENT_REMINDER_DAY));
    }

    @Test
    void reminderMessageContainsEventTitle() {
        Instant inThreeDays = Instant.now().plus(3, ChronoUnit.DAYS);
        int eventId = futureEvent("Summer Fest", inThreeDays);
        goingDAO.markGoing(userId, eventId);

        service.checkAndInsertReminders(userId);

        String message = service.getForUser(userId).get(0).getMessage();
        assertTrue(message.contains("Summer Fest"));
    }

    @Test
    void eventJustUnderSevenDaysTriggersWeekReminder() {
        // 6 days 23 hours — inside the (1 day, 7 days] WEEK window.
        Instant in6Days23Hours = Instant.now().plus(7, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS);
        int eventId = futureEvent("Boundary", in6Days23Hours);
        goingDAO.markGoing(userId, eventId);

        service.checkAndInsertReminders(userId);

        List<Notification> found = service.getForUser(userId);
        assertFalse(found.isEmpty(), "event inside 7-day window should get WEEK reminder");
        assertEquals(NotificationType.EVENT_REMINDER_WEEK, found.get(0).getType());
    }

    @Test
    void eventMoreThanSevenDaysAwayDoesNotTriggerAnyReminder() {
        Instant inEightDays = Instant.now().plus(8, ChronoUnit.DAYS);
        int eventId = futureEvent("TooFar", inEightDays);
        goingDAO.markGoing(userId, eventId);

        service.checkAndInsertReminders(userId);

        assertTrue(service.getForUser(userId).isEmpty());
    }
}
