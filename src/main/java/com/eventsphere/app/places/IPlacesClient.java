package com.eventsphere.app.places;

import java.io.IOException;
import java.util.List;

// Wraps the Google Places API (New) calls the sign-up address field needs.
// Depend on this interface, not PlacesClient directly, so callers can be tested
// with a stub instead of hitting the real API.
public interface IPlacesClient {

    // Address suggestions for the given partial input, restricted to Australia.
    // sessionToken must be the same value for every autocomplete call in one
    // lookup session, then reused once more on the matching fetchDetails call.
    List<Suggestion> autocomplete(String input, String sessionToken) throws IOException, InterruptedException;

    // Resolves a chosen suggestion's coordinates and closes the billing session
    // started by autocomplete(). Pass the same sessionToken used for that lookup.
    PlaceLocation fetchDetails(String placeId, String sessionToken) throws IOException, InterruptedException;
}
