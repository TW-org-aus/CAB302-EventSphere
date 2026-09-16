package com.eventsphere.app.model;

import java.time.Instant;
import java.util.Objects;

public class Event {

    private final int eventId;
    private final String title;
    private final String description;
    private final Category category;
    private final Instant startTime;
    private final Instant endTime;
    private final String venueName;
    private final String address;
    private final Double lat;
    private final Double lng;
    private final String imageUrl;
    private final String ticketUrl;
    private final Instant createdAt;
    private final boolean hasOccurred;
    private final int sourceId;
    // Ticketmaster's own event id, used to upsert on re-ingest. Null for Events from other sources
    // (a seeded or scraped Event leaves it out).
    private final String ticketmasterId;
    // Maintained by DB triggers on Likes/Comments just for reading only.
    private final int likesCount;
    private final int commentsCount;


    public Event(int eventId, String title, String description, Category category,
                 Instant startTime, Instant endTime, String venueName, String address,
                 Double lat, Double lng, String imageUrl, String ticketUrl,
                 Instant createdAt, boolean hasOccurred, int sourceId, String ticketmasterId,
                 int likesCount, int commentsCount) {
        this.eventId = eventId;
        this.title = Objects.requireNonNull(title, "title");
        this.description = description;
        this.category = category;
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.endTime = endTime;
        this.venueName = venueName;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.imageUrl = imageUrl;
        this.ticketUrl = ticketUrl;
        this.createdAt = createdAt;
        this.hasOccurred = hasOccurred;
        this.sourceId = sourceId;
        this.ticketmasterId = ticketmasterId;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
    }

    // Unsaved Event (e.g. freshly mapped from Ticketmaster). eventId = 0 means "not saved yet";
    // EventDAO.insert should ignore it and return the generated id. createdAt, hasOccurred and
    // the counts are left for the DB defaults to fill in.
    public Event(String title, String description, Category category,
                 Instant startTime, Instant endTime, String venueName, String address,
                 Double lat, Double lng, String imageUrl, String ticketUrl, int sourceId,
                 String ticketmasterId) {
        this(0, title, description, category, startTime, endTime, venueName, address,
                lat, lng, imageUrl, ticketUrl, null, false, sourceId, ticketmasterId, 0, 0);
    }

    // Unsaved Event with no Ticketmaster id, for seeded and scraped Events.
    public Event(String title, String description, Category category,
                 Instant startTime, Instant endTime, String venueName, String address,
                 Double lat, Double lng, String imageUrl, String ticketUrl, int sourceId) {
        this(title, description, category, startTime, endTime, venueName, address,
                lat, lng, imageUrl, ticketUrl, sourceId, null);
    }

    public int getEventId() { return eventId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public String getVenueName() { return venueName; }
    public String getAddress() { return address; }
    public Double getLat() { return lat; }
    public Double getLng() { return lng; }
    public String getImageUrl() { return imageUrl; }
    public String getTicketUrl() { return ticketUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean hasOccurred() { return hasOccurred; }
    public int getSourceId() { return sourceId; }
    public String getTicketmasterId() { return ticketmasterId; }
    public int getLikesCount() { return likesCount; }
    public int getCommentsCount() { return commentsCount; }

    // Unsaved Events (id 0) only equal themselves, so two different ones can share a Set.
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return eventId != 0 && other instanceof Event event && eventId == event.eventId;
    }

    @Override
    public int hashCode() {
        return eventId != 0 ? Integer.hashCode(eventId) : System.identityHashCode(this);
    }
}
