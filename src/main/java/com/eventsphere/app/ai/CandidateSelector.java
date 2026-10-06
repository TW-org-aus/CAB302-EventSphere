package com.eventsphere.app.ai;

import com.eventsphere.app.model.Event;
import com.eventsphere.app.service.EventService;

import java.util.List;

// This selector narrows the pool of events the AI ranker chooses from to reduce token usage.

public class CandidateSelector {

    private static final int MAX_CANDIDATES = 40;

    // Approximate distance for Brisbane events.
    private static final double RADIUS_KM = 30.0;

    private final EventService events;

    public CandidateSelector(EventService events) {
        this.events = events;
    }

    // Upcoming events worth ranking for this user.
    // Falls back to all upcoming events when the user has no saved home location.

    public List<Event> candidatesFor(InterestProfile profile) {
        List<Event> pool;
        try {
            pool = profile.hasLocation()
                    ? events.findUpcomingNearby(profile.homeLat(), profile.homeLng(), RADIUS_KM)
                    : events.findUpcoming();
        } catch (Exception e) {
            System.err.println("Could not load candidate events: " + e.getMessage());
            return List.of();
        }

        return pool.size() > MAX_CANDIDATES
                ? List.copyOf(pool.subList(0, MAX_CANDIDATES))
                : pool;
    }
}