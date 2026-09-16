package com.eventsphere.app.ingestion;

import com.eventsphere.app.Database.Database;
import com.eventsphere.app.dao.EventDAO;
import com.eventsphere.app.dao.IEventDAO;
import com.eventsphere.app.dao.ISourceDAO;
import com.eventsphere.app.dao.SourceDAO;
import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.Source;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Batch job that pulls upcoming Events from Ticketmaster and upserts them by Ticketmaster id.
public class TicketmasterIngestor {

    static final String SOURCE_NAME = "Ticketmaster";
    static final String SOURCE_URL = "https://www.ticketmaster.com.au";

    private static final List<String> CITIES = List.of("Brisbane", "Sydney", "Melbourne");
    private static final Duration WINDOW = Duration.ofDays(60);
    private static final Duration STALE_AFTER = Duration.ofHours(24);
    private static final int PAGE_SIZE = 200;
    // so to limit/burnout api usage
    private static final int MAX_RESULTS = 500;
    // Keeps us under the API's limit of 5 requests a second.
    private static final long REQUEST_GAP_MS = 250;

    private final TicketmasterClient client;
    private final Connection connection;
    private final IEventDAO events;
    private final ISourceDAO sources;

    public TicketmasterIngestor(TicketmasterClient client, Connection connection) {
        this.client = client;
        this.connection = connection;
        this.events = new EventDAO(connection);
        this.sources = new SourceDAO(connection);
    }


    public static void refreshInBackgroundIfStale() {
        Thread.ofPlatform().daemon().name("ticketmaster-ingest").start(() -> {
            try (Connection connection = Database.openConnection()) {
                new TicketmasterIngestor(new TicketmasterClient(), connection).runIfStale();
            } catch (Exception e) {
                System.err.println("Ticketmaster ingestion skipped: " + e.getMessage());
            }
        });
    }


    public boolean runIfStale() throws InterruptedException {
        int sourceId = ticketmasterSourceId();
        Optional<Instant> lastSynced = sources.findLastSyncedAt(sourceId);
        if (lastSynced.isPresent() && lastSynced.get().isAfter(Instant.now().minus(STALE_AFTER))) {
            return false;
        }
        run(sourceId);
        return true;
    }


    public int run() throws InterruptedException {
        return run(ticketmasterSourceId());
    }

    // LastSyncedAt only moves when every city succeeds, so a partly failed run retries on next launch.
    private int run(int sourceId) throws InterruptedException {
        Instant from = Instant.now();
        Instant to = from.plus(WINDOW);
        int total = 0;
        boolean allSucceeded = true;

        for (String city : CITIES) {
            try {
                int count = ingestCity(city, sourceId, from, to);
                total += count;
                System.out.println("Ticketmaster: upserted " + count + " events for " + city);
            } catch (IOException | RuntimeException e) {
                allSucceeded = false;
                System.err.println("Ticketmaster: " + city + " failed: " + e.getMessage());
            }
        }

        if (allSucceeded) {
            sources.updateLastSyncedAt(sourceId, Instant.now());
        }
        return total;
    }

    private int ingestCity(String city, int sourceId, Instant from, Instant to)
            throws IOException, InterruptedException {
        int count = 0;
        int totalPages = 1;
        for (int page = 0; page < totalPages && page * PAGE_SIZE < MAX_RESULTS; page++) {
            Thread.sleep(REQUEST_GAP_MS);
            TicketmasterClient.EventPage result = client.fetchEvents(city, from, to, page, PAGE_SIZE);
            totalPages = result.totalPages();
            count += upsertPage(result.events(), sourceId);
        }
        return count;
    }

    // One transaction per page, so faliure only effects one page and not the whole section
    private int upsertPage(JsonNode page, int sourceId) {
        List<Event> mapped = new ArrayList<>();
        for (JsonNode node : page) {
            TicketmasterEventMapper.map(node, sourceId).ifPresent(mapped::add);
        }

        try {
            connection.setAutoCommit(false);
            try {
                for (Event event : mapped) {
                    events.upsertByTicketmasterId(event);
                }
                connection.commit();
            } catch (RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to commit a page of Ticketmaster events", e);
        }
        return mapped.size();
    }

    private int ticketmasterSourceId() {
        return sources.findBySiteName(SOURCE_NAME)
                .map(Source::getSourceId)
                .orElseGet(() -> sources.insert(SOURCE_NAME, SOURCE_URL));
    }
}
