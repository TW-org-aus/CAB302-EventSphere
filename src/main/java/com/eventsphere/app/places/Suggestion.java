package com.eventsphere.app.places;

// One row of the Places autocomplete dropdown: the place's id (used to fetch details)
// and the human-readable text shown to the user.
public record Suggestion(String placeId, String text) {
}
