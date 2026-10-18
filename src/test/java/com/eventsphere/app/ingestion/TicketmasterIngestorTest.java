package com.eventsphere.app.ingestion;

import com.eventsphere.app.dao.IEventDAO;
import com.eventsphere.app.dao.ISourceDAO;
import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.Source;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TicketmasterIngestorTest {

    private Connection connection;
    private MockTicketmasterClient client;
    private StubSourceDAO sources;
    private StubEventDAO events;
    private TicketmasterIngestor ingestor;

    @BeforeEach
    void setUp() throws Exception {
        // In-memory connection only needed for transaction management in upsertPage().
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        client = new MockTicketmasterClient();
        sources = new StubSourceDAO();
        events = new StubEventDAO();
        ingestor = new TicketmasterIngestor(client, events, sources, connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void runIfStaleSkipsWhenFresh() throws InterruptedException {
        sources.lastSyncedAt = Optional.of(Instant.now().minusSeconds(60));
        boolean ran = ingestor.runIfStale();
        assertFalse(ran);
        assertFalse(sources.syncedAtUpdated);
    }

    @Test
    void runIfStaleRunsWhenStale() throws InterruptedException {
        sources.lastSyncedAt = Optional.of(Instant.now().minusSeconds(48 * 3600));
        boolean ran = ingestor.runIfStale();
        assertTrue(ran);
        assertTrue(sources.syncedAtUpdated);
    }

    @Test
    void runIfStaleRunsWhenNeverSynced() throws InterruptedException {
        sources.lastSyncedAt = Optional.empty();
        boolean ran = ingestor.runIfStale();
        assertTrue(ran);
    }

    @Test
    void allCitiesSucceedUpdatesLastSyncedAt() throws InterruptedException {
        ingestor.run();
        assertTrue(sources.syncedAtUpdated);
    }

    @Test
    void oneCityFailsDoesNotUpdateLastSyncedAt() throws InterruptedException {
        client.failForCity("Brisbane");
        ingestor.run();
        assertFalse(sources.syncedAtUpdated);
    }

    @Test
    void allCitiesFailDoesNotUpdateLastSyncedAt() throws InterruptedException {
        client.failForCity("Brisbane");
        client.failForCity("Sydney");
        client.failForCity("Melbourne");
        ingestor.run();
        assertFalse(sources.syncedAtUpdated);
    }

    // ---- stubs ----

    private static class StubSourceDAO implements ISourceDAO {
        Optional<Instant> lastSyncedAt = Optional.empty();
        boolean syncedAtUpdated = false;

        @Override
        public int insert(String siteName, String siteUrl) { return 1; }

        @Override
        public List<Source> findAll() { return List.of(); }

        @Override
        public Optional<Source> findBySiteName(String siteName) {
            return Optional.of(new Source(1, siteName, "https://example.com"));
        }

        @Override
        public Optional<Instant> findLastSyncedAt(int sourceId) { return lastSyncedAt; }

        @Override
        public void updateLastSyncedAt(int sourceId, Instant syncedAt) { syncedAtUpdated = true; }
    }

    private static class StubEventDAO implements IEventDAO {
        final List<Event> upserted = new ArrayList<>();

        @Override public void upsertByTicketmasterId(Event event) { upserted.add(event); }
        @Override public List<Event> findUpcoming() { return List.of(); }
        @Override public List<Event> findByCategory(com.eventsphere.app.model.Category c) { return List.of(); }
        @Override public List<Event> search(String t, com.eventsphere.app.model.Category c, Instant f, Instant to) { return List.of(); }
        @Override public int insert(Event event) { return 0; }
        @Override public Event findById(int eventId) { return null; }
        @Override public List<Event> findAll() { return List.of(); }
    }
}
