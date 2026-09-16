package com.eventsphere.app.dao;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventDAOTicketmasterTest {

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
        sourceId = new SourceDAO(connection).insert("Ticketmaster", "https://www.ticketmaster.com.au");
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    private Event event(String title, String ticketmasterId) {
        return new Event(title, null, Category.MUSIC, START, null, null, null,
                null, null, null, null, sourceId, ticketmasterId);
    }

    @Test
    void ticketmasterIdIsNullableAndNullsDoNotCollide() {
        int first = events.insert(event("Scraped one", null));
        int second = events.insert(event("Scraped two", null));

        assertNull(events.findById(first).orElseThrow().getTicketmasterId());
        assertNull(events.findById(second).orElseThrow().getTicketmasterId());
    }

    @Test
    void upsertUpdatesInPlaceAndKeepsEventId() {
        events.upsertByTicketmasterId(event("Old title", "tm-1"));
        int eventId = events.findAll().get(0).getEventId();

        events.upsertByTicketmasterId(event("New title", "tm-1"));

        List<Event> all = events.findAll();
        assertEquals(1, all.size());
        assertEquals(eventId, all.get(0).getEventId());
        assertEquals("New title", all.get(0).getTitle());
        assertEquals("tm-1", all.get(0).getTicketmasterId());
    }

    @Test
    void upsertWithoutTicketmasterIdIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> events.upsertByTicketmasterId(event("No id", null)));
    }

    @Test
    void sourceLastSyncedAtRoundTrips() {
        SourceDAO sources = new SourceDAO(connection);
        assertTrue(sources.findLastSyncedAt(sourceId).isEmpty());

        Instant syncedAt = Instant.parse("2026-09-16T01:02:03Z");
        sources.updateLastSyncedAt(sourceId, syncedAt);

        assertEquals(syncedAt, sources.findLastSyncedAt(sourceId).orElseThrow());
    }

    // A database.db created before this change has Events and Source without the new columns.
    @Test
    void migrationAddsColumnsToAnOlderDatabase() throws Exception {
        try (Connection old = DriverManager.getConnection("jdbc:sqlite::memory:");
             Statement statement = old.createStatement()) {
            statement.executeUpdate("CREATE TABLE Source (SourceID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "SiteName TEXT NOT NULL, SiteURL TEXT NOT NULL)");
            statement.executeUpdate("CREATE TABLE Events (EventID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "Title TEXT NOT NULL, SourceID INTEGER NOT NULL)");

            new DBController(old);

            assertTrue(hasColumn(old, "Events", "TicketmasterID"));
            assertTrue(hasColumn(old, "Source", "LastSyncedAt"));
        }
    }

    private static boolean hasColumn(Connection connection, String table, String column) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet columns = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (columns.next()) {
                if (columns.getString("name").equals(column)) {
                    return true;
                }
            }
            return false;
        }
    }
}
