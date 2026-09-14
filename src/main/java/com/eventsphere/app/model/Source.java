package com.eventsphere.app.model;

import java.util.Objects;

// the site from where every event comes from (since we are just using ticketmaster and seeding data this class is not too useful but will be in a prod env)
public class Source {

    private final int sourceId;
    private final String siteName;
    private final String siteUrl;

    public Source(int sourceId, String siteName, String siteUrl) {
        this.sourceId = sourceId;
        this.siteName = Objects.requireNonNull(siteName, "siteName");
        this.siteUrl = Objects.requireNonNull(siteUrl, "siteUrl");
    }

    public int getSourceId() { return sourceId; }
    public String getSiteName() { return siteName; }
    public String getSiteUrl() { return siteUrl; }

    @Override
    public boolean equals(Object other) {
        return other instanceof Source source && sourceId == source.sourceId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(sourceId);
    }
}
