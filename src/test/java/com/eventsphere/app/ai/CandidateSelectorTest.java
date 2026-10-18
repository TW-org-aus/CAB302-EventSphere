package com.eventsphere.app.ai;

import com.eventsphere.app.dao.IEventDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import com.eventsphere.app.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CandidateSelectorTest {

    // Brisbane CBD — used as the profile home location.
    private static final double BRISBANE_LAT = -27.47;
    private static final double BRISBANE_LNG = 153.02;

    private StubEventDAO dao;
    private CandidateSelector selector;

    @BeforeEach
    void setUp() {
        dao = new StubEventDAO();
        selector = new CandidateSelector(new EventService(dao));
    }

    @Test
    void noLocationCallsFindUpcoming() {
        Event e = eventAt(1, null, null);
        dao.upcoming = List.of(e);

        InterestProfile profile = new InterestProfile(Set.of(), Map.of(), Map.of(), null, null, null);
        List<Event> result = selector.candidatesFor(profile);

        assertEquals(1, result.size());
        assertEquals(e, result.get(0));
    }

    @Test
    void withLocationFiltersToRadius() {
        Event nearby = eventAt(1, BRISBANE_LAT, BRISBANE_LNG);
        Event farAway = eventAt(2, 0.0, 0.0);
        dao.upcoming = List.of(nearby, farAway);

        InterestProfile profile = new InterestProfile(Set.of(), Map.of(), Map.of(), null, BRISBANE_LAT, BRISBANE_LNG);
        List<Event> result = selector.candidatesFor(profile);

        assertEquals(List.of(nearby), result);
    }

    @Test
    void capsResultsAt40() {
        List<Event> big = new ArrayList<>();
        for (int i = 1; i <= 50; i++) big.add(eventAt(i, null, null));
        dao.upcoming = big;

        InterestProfile profile = new InterestProfile(Set.of(), Map.of(), Map.of(), null, null, null);
        assertEquals(40, selector.candidatesFor(profile).size());
    }

    @Test
    void daoExceptionReturnsEmptyList() {
        dao.shouldThrow = true;

        InterestProfile profile = new InterestProfile(Set.of(), Map.of(), Map.of(), null, null, null);
        assertTrue(selector.candidatesFor(profile).isEmpty());
    }

    // ---- stub ----

    private static Event eventAt(int id, Double lat, Double lng) {
        return new Event(id, "Event " + id, null, Category.MUSIC,
                Instant.now().plusSeconds(3600), null,
                "Venue", "Address", lat, lng,
                null, null, Instant.now(), false, 1, null, 0, 0);
    }

    private static class StubEventDAO implements IEventDAO {
        List<Event> upcoming = List.of();
        boolean shouldThrow = false;

        @Override
        public List<Event> findUpcoming() {
            if (shouldThrow) throw new RuntimeException("DB down");
            return upcoming;
        }

        @Override public List<Event> findByCategory(Category category) { return List.of(); }
        @Override public List<Event> search(String text, Category category, Instant from, Instant to) { return List.of(); }
        @Override public int insert(Event event) { throw new UnsupportedOperationException(); }
        @Override public void upsertByTicketmasterId(Event event) { throw new UnsupportedOperationException(); }
        @Override public Event findById(int eventId) { return null; }
        @Override public List<Event> findAll() { return List.of(); }
    }
}
