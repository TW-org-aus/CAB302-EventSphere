package com.eventsphere.app.service;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.dao.EventDAO;
import com.eventsphere.app.dao.GoingDAO;
import com.eventsphere.app.dao.SourceDAO;
import com.eventsphere.app.dao.UserDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoingServiceTest {

    private static final Instant START = Instant.parse("2026-10-01T09:30:00Z");

    private Connection connection;
    private UserDAO users;
    private GoingDAO goingDao;
    private GoingService going;
    private int eventId;
    private int otherEventId;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
        }
        new DBController(connection);

        users = new UserDAO(connection);
        goingDao = new GoingDAO(connection);
        going = new GoingService(goingDao);

        int sourceId = new SourceDAO(connection).insert("Test", "https://example.com");
        EventDAO events = new EventDAO(connection);
        eventId = events.insert(event("Test Event", sourceId));
        otherEventId = events.insert(event("Other Event", sourceId));
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    private static Event event(String title, int sourceId) {
        return new Event(title, null, Category.MUSIC, START, null, null, null,
                null, null, null, null, sourceId);
    }

    private int user(String first, String last) {
        return users.insert(first, last, first.toLowerCase() + "." + last.toLowerCase() + "@example.com", "hash", null, null, null);
    }

    private static List<String> names(List<User> attendees) {
        return attendees.stream().map(User::getFullName).toList();
    }

    private static User userNamed(String first, String last) {
        return new User(1, first, last, "x@example.com", "hash", null, null, LocalDate.now(), true, true, null);
    }

    // ----- attendeesFor -----

    @Test
    void emptyWhenNobodyIsGoing() {
        assertTrue(going.attendeesFor(eventId).isEmpty());
    }

    @Test
    void attendeesAreOrderedBySurnameThenFirstName() {
        goingDao.markGoing(user("Grace", "Hopper"), eventId);
        goingDao.markGoing(user("Ada", "Lovelace"), eventId);
        goingDao.markGoing(user("Charles", "Babbage"), eventId);
        goingDao.markGoing(user("Anne", "Hopper"), eventId);

        assertEquals(List.of("Charles Babbage", "Anne Hopper", "Grace Hopper", "Ada Lovelace"),
                names(going.attendeesFor(eventId)));
    }

    @Test
    void deactivatedUsersAreLeftOut() {
        int ada = user("Ada", "Lovelace");
        int grace = user("Grace", "Hopper");
        goingDao.markGoing(ada, eventId);
        goingDao.markGoing(grace, eventId);

        users.deactivate(grace);

        assertEquals(List.of("Ada Lovelace"), names(going.attendeesFor(eventId)));
    }

    @Test
    void attendeesAreScopedToTheirEvent() {
        goingDao.markGoing(user("Ada", "Lovelace"), eventId);
        goingDao.markGoing(user("Grace", "Hopper"), otherEventId);

        assertEquals(List.of("Ada Lovelace"), names(going.attendeesFor(eventId)));
    }

    @Test
    void unmarkingGoingRemovesThemFromTheList() {
        int ada = user("Ada", "Lovelace");
        goingDao.markGoing(ada, eventId);

        goingDao.unmarkGoing(ada, eventId);

        assertTrue(going.attendeesFor(eventId).isEmpty());
    }

    @Test
    void markingGoingTwiceOnlyListsThemOnce() {
        int ada = user("Ada", "Lovelace");
        goingDao.markGoing(ada, eventId);
        goingDao.markGoing(ada, eventId);

        assertEquals(1, going.attendeesFor(eventId).size());
    }

    // ----- headingFor -----

    @Test
    void headingForZeroSaysNobodyIsGoing() {
        assertEquals(GoingService.NOBODY_GOING, GoingService.headingFor(0));
    }

    @Test
    void headingForOneIsSingular() {
        assertEquals("1 person going", GoingService.headingFor(1));
    }

    @Test
    void headingForManyIsPlural() {
        assertEquals("3 people going", GoingService.headingFor(3));
    }

    // ----- initialsFor -----

    @Test
    void initialsAreUppercaseFirstLetters() {
        assertEquals("AL", GoingService.initialsFor(userNamed("ada", "lovelace")));
    }

    @Test
    void initialsSkipABlankSurname() {
        assertEquals("A", GoingService.initialsFor(userNamed("Ada", " ")));
    }

    @Test
    void initialsFallBackToQuestionMarkWithNoName() {
        assertEquals("?", GoingService.initialsFor(userNamed("", null)));
    }
}