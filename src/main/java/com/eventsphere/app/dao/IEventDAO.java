package com.eventsphere.app.dao;

import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** DAO for event listings; supports full insert, Ticketmaster upsert, and multi-filter search. */
public interface IEventDAO {

    /** Inserts a new event and returns the generated EventID; the id on the passed Event is ignored. */
    int insert(Event event);

    /** Inserts the event, or updates the existing row that already holds its Ticketmaster id; the row's EventID never changes. */
    void upsertByTicketmasterId(Event event);

    Event findById(int eventId);

    List<Event> findAll();

    /** Returns events in the given category, ordered by StartTime ascending. */
    List<Event> findByCategory(Category category);

    /** Returns events whose StartTime is at or after now, ordered by StartTime ascending. */
    List<Event> findUpcoming();

    /** Full-text + filter search; any parameter may be null and a null parameter skips that filter. */
    List<Event> search(String text, Category category, Instant from, Instant to);
}
