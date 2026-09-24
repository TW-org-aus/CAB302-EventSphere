package com.eventsphere.app.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;

// Keyword search (SCRUM-59): matches title, description, venue name and address.
class EventDAOSearchTest {

    private static final Instant START = Instant.parse("2026-10-01T09:30:00Z");

    private Connection connection;
    private EventDAO events;
    private int sourceId;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
        }
        new DBController(connection);
        events = new EventDAO(connection);
        sourceId = new SourceDAO(connection).insert("Test", "https://example.com");

        insert("Jazz Night", "Live trio all evening", "The Tivoli", "52 Costin St, Fortitude Valley");
        insert("Farmers Market", "Local produce and jazz buskers", "Jan Powers", "Brisbane Powerhouse");
        insert("Comedy Hour", "Stand-up showcase", "Sit Down Comedy Club", "Paddington");
        insert("Book Swap", null, null, null);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    private void insert(String title, String description, String venue, String address) {
        events.insert(new Event(title, description, Category.MUSIC, START, null, venue, address,
                null, null, null, null, sourceId));
    }

    private List<String> titlesMatching(String text) {
        return events.search(text, null, null, null).stream().map(Event::getTitle).toList();
    }

    @Test
    void matchesOnTitle() {
        assertEquals(List.of("Book Swap"), titlesMatching("book swap"));
    }

    @Test
    void matchesOnDescription() {
        assertEquals(List.of("Farmers Market"), titlesMatching("produce"));
    }

    @Test
    void matchesOnVenueName() {
        assertEquals(List.of("Jazz Night"), titlesMatching("tivoli"));
    }

    @Test
    void matchesOnAddress() {
        assertEquals(List.of("Comedy Hour"), titlesMatching("paddington"));
    }

    @Test
    void isCaseInsensitiveAndMatchesAcrossFields() {
        // "Jazz" is in one title and in another event's description.
        assertEquals(Set.of("Jazz Night", "Farmers Market"), new HashSet<>(titlesMatching("JAZZ")));
    }

    @Test
    void partialWordsMatch() {
        assertEquals(List.of("Farmers Market"), titlesMatching("farm"));
    }

    @Test
    void noMatchReturnsEmptyList() {
        assertTrue(titlesMatching("opera").isEmpty());
    }
}
