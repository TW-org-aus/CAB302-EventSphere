package com.eventsphere.app.dao;

import com.eventsphere.app.model.Source;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ISourceDAO {

    // Returns the generated SourceID.
    int insert(String siteName, String siteUrl);

    // All sources, ordered by SourceID.
    List<Source> findAll();

    Optional<Source> findBySiteName(String siteName);

    // When ingestion last finished a full pull from this source. Empty if it never has.
    Optional<Instant> findLastSyncedAt(int sourceId);

    void updateLastSyncedAt(int sourceId, Instant syncedAt);
}
