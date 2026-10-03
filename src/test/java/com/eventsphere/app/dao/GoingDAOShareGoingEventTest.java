package com.eventsphere.app.dao;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class GoingDAOShareGoingEventTest {
    private Connection connection;
    private UserDAO users;
    private GoingDAO going;
    private int angg;
    private int zuko;
    private int concert;
    private int market;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        new DBController(connection);
        users = new UserDAO(connection);
        going = new GoingDAO(connection);

        angg = users.insert("Angg", "Air", "angg@gmail.com", "hash", null, null);
        zuko = users.insert("Zuko", "Fire", "zuko@gmail.com", "hash", null, null);
        int sourceId = new SourceDAO(connection).insert("Test", "https://example.com");
        EventDAO events = new EventDAO(connection);
        concert = events.insert(event("Concert", sourceId));
        market = events.insert(event("Market", sourceId));
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    private static Event event(String title, int sourceId) {
        return new Event(title, null, Category.MUSIC, Instant.parse("2026-09-01T09:30:00Z"),null, null, null, null, null, null, null, sourceId);
    }

    @Test
    void trueWhenBothGoingSameEvent() {
        going.markGoing(angg, concert);
        going.markGoing(zuko, concert);
        assertTrue(going.shareGoingEvent(angg, zuko));
        assertTrue(going.shareGoingEvent(zuko, angg));
    }

    @Test
    void falseWhenNotGoingSameEvent() {
        going.markGoing(angg, concert);
        going.markGoing(zuko, market);
        assertFalse(going.shareGoingEvent(angg, zuko));
    }

    @Test
    void falseWhenAUserDeletedAccount() {
        going.markGoing(angg, concert);
        going.markGoing(zuko, concert);
        users.deactivate(zuko);
        assertFalse(going.shareGoingEvent(angg, zuko));
    }
}
