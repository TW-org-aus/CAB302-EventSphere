package com.eventsphere.app.dao;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.model.Source;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SourceDAOTest {

    private Connection connection;
    private SourceDAO sources;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        }
        new DBController(connection);
        sources = new SourceDAO(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void insertReturnsGeneratedId() {
        int id = sources.insert("Ticketmaster", "https://ticketmaster.com.au");
        assertTrue(id > 0);
    }

    @Test
    void findAllReturnsAllSources() {
        sources.insert("Ticketmaster", "https://ticketmaster.com.au");
        sources.insert("Eventbrite", "https://eventbrite.com.au");
        assertEquals(2, sources.findAll().size());
    }

    @Test
    void findAllEmptyWhenNoSources() {
        assertTrue(sources.findAll().isEmpty());
    }

    @Test
    void findBySiteNameReturnsPresentForExisting() {
        sources.insert("Ticketmaster", "https://ticketmaster.com.au");
        assertTrue(sources.findBySiteName("Ticketmaster").isPresent());
    }

    @Test
    void findBySiteNameReturnsEmptyForUnknown() {
        assertTrue(sources.findBySiteName("Unknown").isEmpty());
    }

    @Test
    void findBySiteNameReturnsCorrectSource() {
        sources.insert("Ticketmaster", "https://ticketmaster.com.au");
        Source s = sources.findBySiteName("Ticketmaster").orElseThrow();
        assertEquals("Ticketmaster", s.getSiteName());
        assertEquals("https://ticketmaster.com.au", s.getSiteUrl());
    }

    @Test
    void findLastSyncedAtEmptyBeforeUpdate() {
        int id = sources.insert("Ticketmaster", "https://ticketmaster.com.au");
        assertTrue(sources.findLastSyncedAt(id).isEmpty());
    }

    @Test
    void updateLastSyncedAtAndReadItBack() {
        int id = sources.insert("Ticketmaster", "https://ticketmaster.com.au");
        Instant now = Instant.parse("2026-10-01T12:00:00Z");
        sources.updateLastSyncedAt(id, now);

        Instant stored = sources.findLastSyncedAt(id).orElseThrow();
        assertEquals(now, stored);
    }

    @Test
    void findAllOrderedBySourceId() {
        int first  = sources.insert("Alpha", "https://alpha.com");
        int second = sources.insert("Beta",  "https://beta.com");

        List<Source> all = sources.findAll();
        assertEquals(first,  all.get(0).getSourceId());
        assertEquals(second, all.get(1).getSourceId());
    }
}
