package com.eventsphere.app.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;

class EventServiceTest {

    private static final Instant START = Instant.parse("2026-10-01T09:30:00Z");
    private static final double USER_LAT = -27.4698;
    private static final double USER_LNG = 153.0251;

    private static final ZoneId BRISBANE = ZoneId.of("Australia/Brisbane");
    // Midday on Wednesday 16 September 2026, Brisbane time.
    private static final Clock WEDNESDAY =
            Clock.fixed(Instant.parse("2026-09-16T02:00:00Z"), BRISBANE);

    private Event eventAt(String title, Double lat, Double lng) {
        return new Event(title, null, Category.MUSIC, START, null, null, null,
                lat, lng, null, null, 1, null);
    }

    private Event eventWithLikes(String title, int likes) {
        return new Event(0, title, null, Category.MUSIC, START, null, null, null,
                null, null, null, null, null, false, 1, null, likes, 0);
    }

    private Event eventThatHasOccurred(String title, boolean hasOccurred) {
        return new Event(0, title, null, Category.MUSIC, START, null, null, null,
                null, null, null, null, null, hasOccurred, 1, null, 0, 0);
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

    // ----- logic moved out of LandingPageController -----

    @Test
    void topByLikesOrdersByLikesDescendingAndAppliesTheLimit() {
        MockEventDAO dao = new MockEventDAO();
        dao.setUpcoming(
                eventWithLikes("Quiet", 1),
                eventWithLikes("Popular", 9),
                eventWithLikes("Middling", 5));

        List<Event> result = new EventService(dao).topByLikes(2);

        assertEquals(List.of("Popular", "Middling"),
                result.stream().map(Event::getTitle).toList());
    }

    @Test
    void topByLikesReturnsEverythingWhenFewerEventsThanTheLimit() {
        MockEventDAO dao = new MockEventDAO();
        dao.setUpcoming(eventWithLikes("Only one", 3));

        assertEquals(1, new EventService(dao).topByLikes(3).size());
    }

    @Test
    void allFilterReturnsUpcoming() {
        MockEventDAO dao = new MockEventDAO();
        dao.setUpcoming(eventWithLikes("Tonight", 0), eventWithLikes("Tomorrow", 0));

        List<Event> result = new EventService(dao).findByFilter(EventService.FILTER_ALL);

        assertEquals(2, result.size());
    }

    @Test
    void weekendFilterSearchesFridayMidnightToMondayMidnight() {
        MockEventDAO dao = new MockEventDAO();

        new EventService(dao, WEDNESDAY).findByFilter(EventService.FILTER_WEEKEND);

        // Brisbane is UTC+10, so local midnight is 14:00Z the day before.
        assertEquals(Instant.parse("2026-09-17T14:00:00Z"), dao.lastSearchFrom);
        assertEquals(Instant.parse("2026-09-20T14:00:00Z"), dao.lastSearchTo);
    }

    @Test
    void weekendFilterOnTheFridayItselfStartsThatSameDay() {
        MockEventDAO dao = new MockEventDAO();
        Clock friday = Clock.fixed(Instant.parse("2026-09-18T02:00:00Z"), BRISBANE);

        new EventService(dao, friday).findByFilter(EventService.FILTER_WEEKEND);

        assertEquals(Instant.parse("2026-09-17T14:00:00Z"), dao.lastSearchFrom);
    }

    @Test
    void categoryFilterDropsEventsThatHaveAlreadyHappened() {
        MockEventDAO dao = new MockEventDAO();
        dao.setByCategory(
                eventThatHasOccurred("Finished", true),
                eventThatHasOccurred("Still to come", false));

        List<Event> result = new EventService(dao).findByFilter("Music");

        assertEquals(List.of("Still to come"),
                result.stream().map(Event::getTitle).toList());
        assertEquals(Category.MUSIC, dao.lastCategoryRequested);
    }

    // ----- findById -----

    @Test
    void findByIdReturnsTheEventWhenFound() {
        MockEventDAO dao = new MockEventDAO();
        Event event = eventWithLikes("Car Meet", 0);
        dao.setById(event);

        assertEquals(event, new EventService(dao).findById(event.getEventId()));
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        MockEventDAO dao = new MockEventDAO();

        assertNull(new EventService(dao).findById(404));
    }

        // ----- keyword search -----

    @Test
    void searchTrimsTheKeywordBeforePassingItToTheDao() {
        MockEventDAO dao = new MockEventDAO();

        new EventService(dao, WEDNESDAY).search("  jazz  ");

        assertEquals("jazz", dao.lastSearchText);
    }

    @Test
    void searchOnlyLooksAtEventsFromNowOn() {
        MockEventDAO dao = new MockEventDAO();

        new EventService(dao, WEDNESDAY).search("jazz");

        assertEquals(WEDNESDAY.instant(), dao.lastSearchFrom);
        assertNull(dao.lastSearchTo);
    }

    @Test
    void blankSearchFallsBackToUpcomingWithoutSearching() {
        MockEventDAO dao = new MockEventDAO();
        dao.setUpcoming(eventWithLikes("Tonight", 0), eventWithLikes("Tomorrow", 0));

        List<Event> result = new EventService(dao, WEDNESDAY).search("   ");

        assertEquals(2, result.size());
        assertNull(dao.lastSearchText);
    }

    @Test
    void nullSearchFallsBackToUpcomingWithoutSearching() {
        MockEventDAO dao = new MockEventDAO();
        dao.setUpcoming(eventWithLikes("Tonight", 0));

        List<Event> result = new EventService(dao, WEDNESDAY).search(null);

        assertEquals(1, result.size());
        assertNull(dao.lastSearchText);
    }

        // ----- combined filter (category / date / location) -----

    private static final Clock SATURDAY =
            Clock.fixed(Instant.parse("2026-09-19T02:00:00Z"), BRISBANE);   // midday Sat 19 Sep

    @Test
    void filterPassesCategoryToTheDao() {
        MockEventDAO dao = new MockEventDAO();

        new EventService(dao, WEDNESDAY).filter(Category.MUSIC, DateRange.ANY, null, null, null);

        assertEquals(Category.MUSIC, dao.lastSearchCategory);
    }

    @Test
    void anyTimeSearchesFromNowWithNoEnd() {
        MockEventDAO dao = new MockEventDAO();

        new EventService(dao, WEDNESDAY).filter(null, DateRange.ANY, null, null, null);

        assertEquals(WEDNESDAY.instant(), dao.lastSearchFrom);
        assertNull(dao.lastSearchTo);
    }

    @Test
    void todayEndsAtLocalMidnight() {
        MockEventDAO dao = new MockEventDAO();

        new EventService(dao, WEDNESDAY).filter(null, DateRange.TODAY, null, null, null);

        assertEquals(WEDNESDAY.instant(), dao.lastSearchFrom);
        assertEquals(Instant.parse("2026-09-16T14:00:00Z"), dao.lastSearchTo);
    }

    @Test
    void next7DaysEndsAWeekFromNow() {
        MockEventDAO dao = new MockEventDAO();

        new EventService(dao, WEDNESDAY).filter(null, DateRange.NEXT_7_DAYS, null, null, null);

        assertEquals(Instant.parse("2026-09-23T02:00:00Z"), dao.lastSearchTo);
    }

    @Test
    void thisWeekendMidweekRunsFridayToMonday() {
        MockEventDAO dao = new MockEventDAO();

        new EventService(dao, WEDNESDAY).filter(null, DateRange.THIS_WEEKEND, null, null, null);

        assertEquals(Instant.parse("2026-09-17T14:00:00Z"), dao.lastSearchFrom);
        assertEquals(Instant.parse("2026-09-20T14:00:00Z"), dao.lastSearchTo);
    }

    @Test
    void thisWeekendOnSaturdayStartsNowAndEndsThisMonday() {
        MockEventDAO dao = new MockEventDAO();

        new EventService(dao, SATURDAY).filter(null, DateRange.THIS_WEEKEND, null, null, null);

        assertEquals(SATURDAY.instant(), dao.lastSearchFrom);
        assertEquals(Instant.parse("2026-09-20T14:00:00Z"), dao.lastSearchTo);
    }

    @Test
    void weekendButtonOnSaturdayStillCoversThisWeekend() {
        MockEventDAO dao = new MockEventDAO();

        new EventService(dao, SATURDAY).findByFilter(EventService.FILTER_WEEKEND);

        assertEquals(Instant.parse("2026-09-17T14:00:00Z"), dao.lastSearchFrom);
        assertEquals(Instant.parse("2026-09-20T14:00:00Z"), dao.lastSearchTo);
    }

    @Test
    void radiusKeepsOnlyNearbyEvents() {
        MockEventDAO dao = new MockEventDAO();
        dao.setUpcoming(eventAt("Far", -33.8688, 151.2093), eventAt("Near", -27.47, 153.03));

        List<Event> result = new EventService(dao, WEDNESDAY)
                .filter(null, DateRange.ANY, 50.0, USER_LAT, USER_LNG);

        assertEquals(List.of("Near"), result.stream().map(Event::getTitle).toList());
    }

    @Test
    void radiusIsIgnoredWithoutHomeCoordinates() {
        MockEventDAO dao = new MockEventDAO();
        dao.setUpcoming(eventAt("Far", -33.8688, 151.2093), eventAt("Near", -27.47, 153.03));

        List<Event> result = new EventService(dao, WEDNESDAY)
                .filter(null, DateRange.ANY, 50.0, null, null);

        assertEquals(2, result.size());
    }

    @Test
    void filterDropsEventsThatHaveAlreadyHappened() {
        MockEventDAO dao = new MockEventDAO();
        dao.setUpcoming(eventThatHasOccurred("Finished", true), eventThatHasOccurred("Still to come", false));

        List<Event> result = new EventService(dao, WEDNESDAY).filter(null, DateRange.ANY, null, null, null);

        assertEquals(List.of("Still to come"), result.stream().map(Event::getTitle).toList());
    }
}
