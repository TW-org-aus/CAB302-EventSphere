package com.eventsphere.app.places;

import java.io.IOException;
import java.util.List;

/** Contract for the Google Places API client used by the address autocomplete field on sign-up. */
public interface IPlacesClient {

    /** Returns address suggestions for the given partial input; sessionToken groups autocomplete + details calls for billing. */
    List<Suggestion> autocomplete(String input, String sessionToken) throws IOException, InterruptedException;

    // Resolves a chosen suggestion's coordinates
    PlaceLocation fetchDetails(String placeId, String sessionToken) throws IOException, InterruptedException;

}
