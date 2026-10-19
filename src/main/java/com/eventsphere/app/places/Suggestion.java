package com.eventsphere.app.places;

/** One row in the Places autocomplete dropdown: a place id (for the detail call) and its display text. */
public class Suggestion {

    private final String placeId;
    private final String text;

    public Suggestion(String placeId, String text) {
        this.placeId = placeId;
        this.text = text;
    }

    public String getPlaceId() { return placeId; }
    public String getText() { return text; }
}
