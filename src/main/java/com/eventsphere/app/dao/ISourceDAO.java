package com.eventsphere.app.dao;

import com.eventsphere.app.model.Source;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** DAO for event sources (e.g. Ticketmaster, seed data); tracks the last ingestion sync timestamp. */
public interface ISourceDAO {

    /** Inserts a new source row and returns the generated SourceID. */
    int insert(String siteName, String siteUrl);

    /** Returns all sources ordered by SourceID. */
    List<Source> findAll();

    Optional<Source> findBySiteName(String siteName);

    /** Returns when the last full pull from this source completed; empty if it has never synced. */
    Optional<Instant> findLastSyncedAt(int sourceId);

    void updateLastSyncedAt(int sourceId, Instant syncedAt);
}
