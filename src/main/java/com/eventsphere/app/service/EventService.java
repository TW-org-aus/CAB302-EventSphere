package com.eventsphere.app.service;

import com.eventsphere.app.dao.IEventDAO;
import com.eventsphere.app.model.Event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EventService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final IEventDAO events;

    public EventService(IEventDAO events) {
        this.events = events;
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
}