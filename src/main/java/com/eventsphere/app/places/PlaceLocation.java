package com.eventsphere.app.places;

// Coordinates resolved for a selected place.
public class PlaceLocation {

    private final double lat;
    private final double lng;

    public PlaceLocation(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() { return lat; }
    public double getLng() { return lng; }
}
