package com.eventsphere.app.dao;

import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IEventDAO {

    // Returns the generated EventID. The id on the passed Event is ignored (an unsaved event has id 0).
    int insert(Event event);

    // Inserts the Event, or updates the row that already has its Ticketmaster id. The EventID of an
    // existing row never changes.
    void upsertByTicketmasterId(Event event);

    Event findById(int eventId);


    List<Event> findAll();

    // Events in the given category, ordered by StartTime ascending.
    List<Event> findByCategory(Category category);

    // Events with StartTime at or after now, ordered by StartTime ascending.
    List<Event> findUpcoming();

    // Every parameter may be null, and a null skips that filter.
    List<Event> search(String text, Category category, Instant from, Instant to);
}
