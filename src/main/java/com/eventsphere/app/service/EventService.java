package com.eventsphere.app.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.eventsphere.app.dao.IEventDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;

public class EventService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    // Filter values the landing page buttons carry as their FXML userData.
    // Anything else is read as a Category db value.
    public static final String FILTER_ALL = "ALL";
    public static final String FILTER_WEEKEND = "WEEKEND";

    private final IEventDAO events;
    private final Clock clock;

    public EventService(IEventDAO events) {
        this(events, Clock.systemDefaultZone());
    }

    // Tests pass a fixed Clock so the weekend window does not move with the real date.
    public EventService(IEventDAO events, Clock clock) {
        this.events = events;
        this.clock = clock;
    }

    public List<Event> findUpcoming() {
        return events.findUpcoming();
    }

    public Event findById(int eventId) {
        return events.findById(eventId);
    }

    // Backing list for the hero carousel: the most liked upcoming events, most liked first.
    public List<Event> topByLikes(int limit) {
        return events.findUpcoming().stream()
                .sorted(Comparator.comparingInt(Event::getLikesCount).reversed())
                .limit(limit)
                .toList();
    }

    // Resolves one of the landing page filter buttons to the events it should show.
    public List<Event> findByFilter(String filterValue) {
        if (FILTER_ALL.equals(filterValue)) {
            return events.findUpcoming();
        }
        if (FILTER_WEEKEND.equals(filterValue)) {
            return events.search(null, null, weekendStart(), weekendEnd());
        }
        // findByCategory returns past events too, so they are dropped here.
        return events.findByCategory(Category.fromDbValue(filterValue)).stream()
                .filter(event -> !event.hasOccurred())
                .toList();
    }

    // The coming weekend: midnight Friday to midnight Monday. Counting back from the next Monday
    // means a Saturday or Sunday still gets *this* weekend rather than jumping to next week's.
    private ZonedDateTime weekendEndDate() {
        return ZonedDateTime.now(clock)
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .truncatedTo(ChronoUnit.DAYS);
    }

    Instant weekendStart() {
        return weekendEndDate().minusDays(3).toInstant();
    }

    Instant weekendEnd() {
        return weekendEndDate().toInstant();
    }

    // Combined landing page filter. category null = any category; range ANY = anything from now on;
    // radiusKm null, or no home coordinates, = any distance. Radius results come back nearest first.
    public List<Event> filter(Category category, DateRange range, Double radiusKm,
                              Double homeLat, Double homeLng) {
        Instant now = Instant.now(clock);
        Instant from = now;
        Instant to = null;

        switch (range == null ? DateRange.ANY : range) {
            case TODAY -> to = ZonedDateTime.now(clock).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant();
            case THIS_WEEKEND -> {
                Instant start = weekendStart();
                from = start.isAfter(now) ? start : now;
                to = weekendEnd();
            }
            case NEXT_7_DAYS -> to = now.plus(7, ChronoUnit.DAYS);
            case NEXT_30_DAYS -> to = now.plus(30, ChronoUnit.DAYS);
            case ANY -> { }
        }

        List<Event> results = events.search(null, category, from, to).stream()
                .filter(event -> !event.hasOccurred())
                .toList();

        if (radiusKm != null && homeLat != null && homeLng != null) {
            return withinRadius(results, homeLat, homeLng, radiusKm);
        }
        return results;
    }

    public List<Event> findUpcomingNearby(double userLat, double userLng, double radiusKm) {
        return withinRadius(events.findUpcoming(), userLat, userLng, radiusKm);
    }

    public List<Event> withinRadius(List<Event> source, double userLat, double userLng, double radiusKm) {
        List<Event> result = new ArrayList<>();
        for (Event event : source) {
            Double distance = distanceKm(userLat, userLng, event);
            if (distance != null && distance <= radiusKm) {
                result.add(event);
            }
        }
        result.sort(Comparator.comparingDouble(event -> distanceKm(userLat, userLng, event)));
        return result;
    }

    // Keep coordless events at the end so the UI can still show them.
    public List<Event> sortByDistance(List<Event> source, double userLat, double userLng) {
        List<Event> result = new ArrayList<>(source);
        result.sort(Comparator.comparing(
                event -> distanceKm(userLat, userLng, event),
                Comparator.nullsLast(Comparator.naturalOrder())));
        return result;
    }

    static Double distanceKm(double userLat, double userLng, Event event) {
        if (event.getLat() == null || event.getLng() == null) {
            return null;
        }

        double lat1 = Math.toRadians(userLat);
        double lat2 = Math.toRadians(event.getLat());
        double deltaLat = Math.toRadians(event.getLat() - userLat);
        double deltaLng = Math.toRadians(event.getLng() - userLng);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    // Keyword search for the nav bar. The DAO matches title, description, venue name and
    // address. Only events from now on are returned. A blank keyword shows everything upcoming.
    public List<Event> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return events.findUpcoming();
        }
        return events.search(keyword.trim(), null, Instant.now(clock), null);
    }
}
