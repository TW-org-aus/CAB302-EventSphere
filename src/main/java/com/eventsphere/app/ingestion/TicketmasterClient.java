package com.eventsphere.app.ingestion;

import com.eventsphere.app.config.EnvConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class TicketmasterClient {

    private static final String BASE_URL = "https://app.ticketmaster.com/discovery/v2/events.json";

    private final HttpClient http;
    private final ObjectMapper json = new ObjectMapper();
    private final String apiKey;


    // first constructor uses .env
    public TicketmasterClient() {
        this(EnvConfig.require("TICKETMASTER_API_KEY"));
    }

    //second constructor gets key passed from first constructor above --> this is mainly
    // here because of testing, you can run tests with this class without needing an env
    //connection :)
    public TicketmasterClient(String apiKey) {
        this.apiKey = apiKey;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // One page of events for a city. Returns the "events" array, or a missing node when nothing matched
    // (iterating a missing node yields nothing, so callers can loop over it without a null check).
    public JsonNode fetchEvents(String city, int page, int size) throws IOException, InterruptedException {
        String url = BASE_URL
                + "?apikey=" + encode(apiKey)
                + "&countryCode=AU"
                + "&city=" + encode(city)
                + "&size=" + size
                + "&page=" + page
                + "&sort=date,asc";

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            // Leave the URL out of the message --> IT CONTAINS THE API KEY
            throw new IOException("Ticketmaster returned HTTP " + response.statusCode());
        }
        return json.readTree(response.body()).path("_embedded").path("events");
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
