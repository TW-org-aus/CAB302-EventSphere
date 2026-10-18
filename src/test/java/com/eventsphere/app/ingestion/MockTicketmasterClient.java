package com.eventsphere.app.ingestion;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

class MockTicketmasterClient implements ITicketmasterClient {

    private static final ObjectMapper JSON = new ObjectMapper();

    // Cities that should throw IOException when fetched.
    private final java.util.Set<String> failingCities = new java.util.HashSet<>();
    private int totalPages = 1;

    void failForCity(String city) { failingCities.add(city); }
    void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    @Override
    public TicketmasterClient.EventPage fetchEvents(String city, Instant from, Instant to, int page, int size)
            throws IOException, InterruptedException {
        if (failingCities.contains(city)) {
            throw new IOException("Ticketmaster returned HTTP 500");
        }
        // Return an empty events array so TicketmasterEventMapper is not exercised here.
        ArrayNode emptyEvents = JSON.createArrayNode();
        return new TicketmasterClient.EventPage(emptyEvents, totalPages);
    }
}
