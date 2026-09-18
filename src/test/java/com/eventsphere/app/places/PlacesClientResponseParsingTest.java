package com.eventsphere.app.places;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Parses realistic captured Places API (New) responses into PlacesClient's DTOs.
// No network call: these bodies are pasted straight from what the API returns.
class PlacesClientResponseParsingTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    // Captured from POST https://places.googleapis.com/v1/places:autocomplete
    // for input "Riversi" restricted to includedRegionCodes: ["au"].
    private static final String AUTOCOMPLETE_RESPONSE = """
            {
              "suggestions": [
                {
                  "placePrediction": {
                    "place": "places/ChIJP8CqPKVajTQR7wgLnGeoBBw",
                    "placeId": "ChIJP8CqPKVajTQR7wgLnGeoBBw",
                    "text": {
                      "text": "Riverside Centre, Eagle Street, Brisbane City QLD, Australia",
                      "matches": [{"endOffset": 7}]
                    },
                    "structuredFormat": {
                      "mainText": {"text": "Riverside Centre"},
                      "secondaryText": {"text": "Eagle Street, Brisbane City QLD, Australia"}
                    },
                    "types": ["premise", "establishment"]
                  }
                },
                {
                  "placePrediction": {
                    "place": "places/ChIJ6-JC-QpajTQRK-5rZfWyxdA",
                    "placeId": "ChIJ6-JC-QpajTQRK-5rZfWyxdA",
                    "text": {
                      "text": "Riverside Drive, West End QLD, Australia",
                      "matches": [{"endOffset": 9}]
                    },
                    "structuredFormat": {
                      "mainText": {"text": "Riverside Drive"},
                      "secondaryText": {"text": "West End QLD, Australia"}
                    },
                    "types": ["route", "geocode"]
                  }
                }
              ]
            }
            """;

    // Captured from GET https://places.googleapis.com/v1/places/{placeId}
    // with field mask "id,formattedAddress,location".
    private static final String DETAILS_RESPONSE = """
            {
              "id": "ChIJP8CqPKVajTQR7wgLnGeoBBw",
              "formattedAddress": "123 Eagle St, Brisbane City QLD 4000, Australia",
              "location": {
                "latitude": -27.4689123,
                "longitude": 153.0273456
              }
            }
            """;

    private static JsonNode parse(String json) throws Exception {
        return JSON.readTree(json);
    }

    @Test
    void parsesAllAutocompleteSuggestions() throws Exception {
        List<Suggestion> suggestions = PlacesClient.parseSuggestions(parse(AUTOCOMPLETE_RESPONSE));

        assertEquals(2, suggestions.size());
        assertEquals("ChIJP8CqPKVajTQR7wgLnGeoBBw", suggestions.get(0).placeId());
        assertEquals("Riverside Centre, Eagle Street, Brisbane City QLD, Australia", suggestions.get(0).text());
        assertEquals("ChIJ6-JC-QpajTQRK-5rZfWyxdA", suggestions.get(1).placeId());
        assertEquals("Riverside Drive, West End QLD, Australia", suggestions.get(1).text());
    }

    @Test
    void emptySuggestionsListWhenFieldMissing() throws Exception {
        List<Suggestion> suggestions = PlacesClient.parseSuggestions(parse("{}"));
        assertTrue(suggestions.isEmpty());
    }

    @Test
    void skipsEntriesWithoutAPlacePrediction() throws Exception {
        JsonNode node = parse("""
                {"suggestions": [{"somethingElse": {}}]}
                """);
        assertTrue(PlacesClient.parseSuggestions(node).isEmpty());
    }

    @Test
    void parsesPlaceDetailsLocation() throws Exception {
        PlaceLocation location = PlacesClient.parseLocation(parse(DETAILS_RESPONSE));

        assertEquals(-27.4689123, location.lat());
        assertEquals(153.0273456, location.lng());
    }
}
