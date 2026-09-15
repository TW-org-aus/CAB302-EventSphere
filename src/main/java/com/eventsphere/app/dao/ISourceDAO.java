package com.eventsphere.app.dao;

import com.eventsphere.app.model.Source;

import java.util.List;

public interface ISourceDAO {

    // Returns the generated SourceID.
    int insert(String siteName, String siteUrl);

    // All sources, ordered by SourceID.
    List<Source> findAll();
}
