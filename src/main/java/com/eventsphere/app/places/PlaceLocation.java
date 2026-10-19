package com.eventsphere.app.places;

/** Lat/lng pair resolved for a selected autocomplete suggestion via the Places details call. */
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
