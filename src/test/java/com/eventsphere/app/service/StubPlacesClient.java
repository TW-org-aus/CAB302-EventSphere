package com.eventsphere.app.service;

import com.eventsphere.app.places.IPlacesClient;
import com.eventsphere.app.places.PlaceLocation;
import com.eventsphere.app.places.Suggestion;

import java.io.IOException;
import java.util.List;


class StubPlacesClient implements IPlacesClient {

    private final List<Suggestion> suggestionsToReturn;
    private final PlaceLocation locationToReturn;

    String lastAutocompleteInput;
    String lastAutocompleteSessionToken;
    String lastDetailsPlaceId;
    String lastDetailsSessionToken;

    StubPlacesClient(List<Suggestion> suggestionsToReturn, PlaceLocation locationToReturn) {
        this.suggestionsToReturn = suggestionsToReturn;
        this.locationToReturn = locationToReturn;
    }

    @Override
    public List<Suggestion> autocomplete(String input, String sessionToken) {
        lastAutocompleteInput = input;
        lastAutocompleteSessionToken = sessionToken;
        return suggestionsToReturn;
    }

    @Override
    public PlaceLocation fetchDetails(String placeId, String sessionToken) {
        lastDetailsPlaceId = placeId;
        lastDetailsSessionToken = sessionToken;
        return locationToReturn;
    }


}
