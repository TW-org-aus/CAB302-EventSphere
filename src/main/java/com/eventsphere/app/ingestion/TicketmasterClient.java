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
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TicketmasterClient {

    private static final String BASE_URL = "https://app.ticketmaster.com/discovery/v2/events.json";

    // The Discovery API rejects fractional seconds, so Instant.toString() won't do.
    private static final DateTimeFormatter API_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    // One page of results. events is a missing node when nothing matched (iterating a missing node
    // yields nothing, so callers can loop over it without a null check), and totalPages is then 0.
    public record EventPage(JsonNode events, int totalPages) { }

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

    // One page of events in an Australian city that start between from and to.
    public EventPage fetchEvents(String city, Instant from, Instant to, int page, int size)
            throws IOException, InterruptedException {
        String url = BASE_URL
                + "?apikey=" + encode(apiKey)
                + "&countryCode=AU"
                + "&city=" + encode(city)
                + "&startDateTime=" + encode(API_TIMESTAMP.format(from))
                + "&endDateTime=" + encode(API_TIMESTAMP.format(to))
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
        JsonNode root = json.readTree(response.body());
        return new EventPage(
                root.path("_embedded").path("events"),
                root.path("page").path("totalPages").asInt(0));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
