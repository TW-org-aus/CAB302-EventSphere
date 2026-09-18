package com.eventsphere.app.places;

import java.io.IOException;
import java.util.List;

// Wraps the Google Places API calls the sign-up address field needs.

public interface IPlacesClient {

    // Address suggestions for the given partial input
    List<Suggestion> autocomplete(String input, String sessionToken) throws IOException, InterruptedException;

    // Resolves a chosen suggestion's coordinates
    PlaceLocation fetchDetails(String placeId, String sessionToken) throws IOException, InterruptedException;

}
