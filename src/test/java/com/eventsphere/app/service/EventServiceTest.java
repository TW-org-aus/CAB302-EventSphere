package com.eventsphere.app.service;

import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventServiceTest {

    private static final Instant START = Instant.parse("2026-10-01T09:30:00Z");
    private static final double USER_LAT = -27.4698;
    private static final double USER_LNG = 153.0251;

    private Event eventAt(String title, Double lat, Double lng) {
        return new Event(title, null, Category.MUSIC, START, null, null, null,
                lat, lng, null, null, 1, null);
    }

    @Test
    void withinRadiusExcludesFarEventsAndOrdersByDistance() {
        Event near = eventAt("Near", -27.47, 153.03);
        Event far = eventAt("Far", -33.8688, 151.2093);
        Event noCoords = eventAt("No coords", null, null);

        EventService service = new EventService(null);
        List<Event> result = service.withinRadius(
                List.of(far, noCoords, near), USER_LAT, USER_LNG, 50);

        assertEquals(1, result.size());
        assertEquals("Near", result.get(0).getTitle());
    }

    @Test
    void sortByDistancePutsCoordlessEventsLast() {
        Event near = eventAt("Near", -27.47, 153.03);
        Event far = eventAt("Far", -33.8688, 151.2093);
        Event noCoords = eventAt("No coords", null, null);

        EventService service = new EventService(null);
        List<Event> result = service.sortByDistance(
                List.of(far, noCoords, near), USER_LAT, USER_LNG);

        assertEquals(List.of("Near", "Far", "No coords"),
                result.stream().map(Event::getTitle).toList());
    }
}