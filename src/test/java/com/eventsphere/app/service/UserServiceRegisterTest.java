package com.eventsphere.app.service;

import com.eventsphere.app.model.Category;
import com.eventsphere.app.places.PlaceLocation;
import com.eventsphere.app.places.Suggestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Covers the sign-up/register path
class UserServiceRegisterTest {

    private MockUserDAO users;
    private MockPreferenceDAO preferences;
    private UserService userService;

    @BeforeEach
    void setUp() {
        users = new MockUserDAO();
        preferences = new MockPreferenceDAO();
        userService = new UserService(users, preferences);
    }

    @Test
    void registerStoresCoordinatesResolvedFromAStubbedPlacesLookup() {
        StubPlacesClient places = new StubPlacesClient(
                List.of(new Suggestion("ChIJP8CqPKVajTQR7wgLnGeoBBw",
                        "Riverside Centre, Eagle Street, Brisbane City QLD, Australia")),
                new PlaceLocation(-27.4689123, 153.0273456));

        // Simulates the sign-up flow that SignUpController drives
        String sessionToken = "session-token-1";
        Suggestion chosen = places.autocomplete("Riversi", sessionToken).get(0);
        PlaceLocation location = places.fetchDetails(chosen.getPlaceId(), sessionToken);

        RegisterResult result = userService.register("Ada", "Lovelace", "ada@example.com",
                "supersecret1", location.getLat(), location.getLng(), Set.of(Category.TECH));

        assertTrue(result.isSuccess());
        assertEquals(-27.4689123, users.lastHomeLat);
        assertEquals(153.0273456, users.lastHomeLong);

        // The same session token opened the lookup and closed it via fetchDetails.
        assertEquals(sessionToken, places.lastAutocompleteSessionToken);
        assertEquals(sessionToken, places.lastDetailsSessionToken);
        assertEquals("ChIJP8CqPKVajTQR7wgLnGeoBBw", places.lastDetailsPlaceId);
    }

    @Test
    void registerNeverPersistsAddressText() {
        RegisterResult result = userService.register("Ada", "Lovelace", "ada2@example.com",
                "supersecret1", -27.4689123, 153.0273456, Set.of(Category.TECH));

        assertTrue(result.isSuccess());
        // upsertAddress must never be called cause the address string is not collected anymo re.
        assertFalse(preferences.upsertAddressCalled);
        assertEquals(Set.of(Category.TECH), preferences.lastCategories);
    }

    @Test
    void registerWithNoSuggestionPickedStoresNullCoordinates() {
        RegisterResult result = userService.register("Ada", "Lovelace", "ada3@example.com",
                "supersecret1", null, null, Set.of(Category.TECH));

        assertTrue(result.isSuccess());
        assertNull(users.lastHomeLat);
        assertNull(users.lastHomeLong);
    }
}
