package com.eventsphere.app.places;

// Coordinates resolved for a selected place. This is all sign-up keeps --
// the formatted address Google returns is never persisted.
public record PlaceLocation(double lat, double lng) {
}
