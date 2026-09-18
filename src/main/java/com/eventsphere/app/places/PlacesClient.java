package com.eventsphere.app.places;

import com.eventsphere.app.config.EnvConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

// Client for the Places API : autocomplete + place details.
public class PlacesClient implements IPlacesClient {

    private static final String AUTOCOMPLETE_URL = "https://places.googleapis.com/v1/places:autocomplete";
    private static final String DETAILS_URL = "https://places.googleapis.com/v1/places/";

    // Only what sign-up needs.
    private static final String DETAILS_FIELD_MASK = "id,formattedAddress,location";

    private final HttpClient http;
    private final ObjectMapper json = new ObjectMapper();
    private final String apiKey;


    // first constructor uses .env
    public PlacesClient() {
        this(EnvConfig.require("GOOGLE_PLACES_API_KEY"));
    }

    // second constructor gets the key passed in directly -- for router
    // so we don't have to pull api from env in the router class
    public PlacesClient(String apiKey) {
        this.apiKey = apiKey;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public List<Suggestion> autocomplete(String input, String sessionToken) throws IOException, InterruptedException {
        ObjectNode body = json.createObjectNode();
        body.put("input", input);
        body.put("sessionToken", sessionToken);
        body.putArray("includedRegionCodes").add("au");

        HttpRequest request = HttpRequest.newBuilder(URI.create(AUTOCOMPLETE_URL))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .header("X-Goog-Api-Key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {

            throw new IOException("Places autocomplete returned HTTP " + response.statusCode());
        }
        return parseSuggestions(json.readTree(response.body()));
    }








    static List<Suggestion> parseSuggestions(JsonNode root) {
        List<Suggestion> suggestions = new ArrayList<>();
        for (JsonNode item : root.path("suggestions")) {
            JsonNode prediction = item.path("placePrediction");
            if (prediction.isMissingNode()) {
                continue;
            }
            String placeId = prediction.path("placeId").asText(null);
            String text = prediction.path("text").path("text").asText(null);
            if (placeId != null && text != null) {
                suggestions.add(new Suggestion(placeId, text));
            }
        }
        return suggestions;
    }

    @Override
    public PlaceLocation fetchDetails(String placeId, String sessionToken) throws IOException, InterruptedException {
        String url = DETAILS_URL + encode(placeId) + "?sessionToken=" + encode(sessionToken);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask", DETAILS_FIELD_MASK)
                .GET()
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {

            throw new IOException("Places details returned HTTP " + response.statusCode());
        }
        return parseLocation(json.readTree(response.body()));
    }




    static PlaceLocation parseLocation(JsonNode root) {
        JsonNode location = root.path("location");
        return new PlaceLocation(location.path("latitude").asDouble(), location.path("longitude").asDouble());
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
