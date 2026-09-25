package com.eventsphere.app.service;

import com.eventsphere.app.dao.IEventDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// In-memory IEventDAO so EventService can be tested without a database.
class MockEventDAO implements IEventDAO {

    private final List<Event> upcoming = new ArrayList<>();
    private final List<Event> byCategory = new ArrayList<>();
    private Event byId;

    String lastSearchText;
    Instant lastSearchFrom;
    Instant lastSearchTo;
    Category lastCategoryRequested;

    void setUpcoming(Event... events) {
        upcoming.clear();
        upcoming.addAll(List.of(events));
    }

    void setByCategory(Event... events) {
        byCategory.clear();
        byCategory.addAll(List.of(events));
    }

    void setById(Event event) {
        byId = event;
    }

    @Override
    public List<Event> findUpcoming() {
        return List.copyOf(upcoming);
    }

    @Override
    public List<Event> findByCategory(Category category) {
        lastCategoryRequested = category;
        return List.copyOf(byCategory);
    }

    @Override
    public List<Event> search(String text, Category category, Instant from, Instant to) {
        lastSearchText = text;
        lastSearchFrom = from;
        lastSearchTo = to;
        return List.copyOf(upcoming);
    }

    @Override
    public int insert(Event event) {
        throw new UnsupportedOperationException("not needed for these tests");
    }

    @Override
    public void upsertByTicketmasterId(Event event) {
        throw new UnsupportedOperationException("not needed for these tests");
    }
    @Override
    public Event findById(int eventId) {
        return byId != null && byId.getEventId() == eventId ? byId : null;
    }
    @Override
    public List<Event> findAll() {
        throw new UnsupportedOperationException("not needed for these tests");
    }
}
