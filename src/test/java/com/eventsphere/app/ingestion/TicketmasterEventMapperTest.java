package com.eventsphere.app.ingestion;

import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketmasterEventMapperTest {

    private static final ObjectMapper JSON = new ObjectMapper();


    private static final String FULL_EVENT = """
            {
              "id": "Z698xZb_Z16v7eGkFy",
              "name": "Test Concert",
              "url": "https://www.ticketmaster.com.au/event/abc",
              "info": "Doors open 7pm",
              "images": [
                {"ratio": "4_3", "url": "https://img/4_3_big.jpg", "width": 2048, "fallback": false},
                {"ratio": "16_9", "url": "https://img/16_9_small.jpg", "width": 640, "fallback": false},
                {"ratio": "16_9", "url": "https://img/16_9_large.jpg", "width": 1024, "fallback": false},
                {"ratio": "16_9", "url": "https://img/placeholder.jpg", "width": 2048, "fallback": true}
              ],
              "dates": {
                "start": {"localDate": "2026-10-01", "localTime": "19:30:00", "dateTime": "2026-10-01T09:30:00Z"},
                "timezone": "Australia/Brisbane",
                "status": {"code": "onsale"}
              },
              "classifications": [
                {"primary": true, "segment": {"name": "Music"}, "genre": {"name": "Rock"}}
              ],
              "_embedded": {
                "venues": [{
                  "name": "Riverstage",
                  "postalCode": "4000",
                  "timezone": "Australia/Brisbane",
                  "city": {"name": "Brisbane"},
                  "state": {"name": "Queensland", "stateCode": "QLD"},
                  "address": {"line1": "59 Gardens Point Rd"},
                  "location": {"longitude": "153.0301", "latitude": "-27.4776"}
                }]
              }
            }
            """;

    private static JsonNode parse(String json) throws Exception {
        return JSON.readTree(json);
    }

    @Test
    void mapsEventVenueAndImage() throws Exception {
        Event event = TicketmasterEventMapper.map(parse(FULL_EVENT), 7).orElseThrow();

        assertEquals("Z698xZb_Z16v7eGkFy", event.getTicketmasterId());
        assertEquals("Test Concert", event.getTitle());
        assertEquals("Doors open 7pm", event.getDescription());
        assertEquals(Category.MUSIC, event.getCategory());
        assertEquals(Instant.parse("2026-10-01T09:30:00Z"), event.getStartTime());
        assertNull(event.getEndTime());
        assertEquals("Riverstage", event.getVenueName());
        assertEquals("59 Gardens Point Rd, Brisbane QLD 4000", event.getAddress());
        assertEquals(-27.4776, event.getLat());
        assertEquals(153.0301, event.getLng());
        assertEquals("https://img/16_9_large.jpg", event.getImageUrl());
        assertEquals("https://www.ticketmaster.com.au/event/abc", event.getTicketUrl());
        assertEquals(7, event.getSourceId());
        assertEquals(0, event.getEventId());
    }

    @Test
    void dateOnlyStartUsesEventTimeZoneMidnight() throws Exception {
        JsonNode node = parse("""
                {"id": "x", "name": "Festival",
                 "dates": {"start": {"localDate": "2026-12-05"}, "timezone": "Australia/Brisbane"}}
                """);
        Event event = TicketmasterEventMapper.map(node, 1).orElseThrow();
        // Brisbane is UTC+10 with no daylight saving.
        assertEquals(Instant.parse("2026-12-04T14:00:00Z"), event.getStartTime());
    }

    @Test
    void cancelledEventIsSkipped() throws Exception {
        JsonNode node = parse("""
                {"id": "x", "name": "Called off",
                 "dates": {"start": {"dateTime": "2026-10-01T09:30:00Z"}, "status": {"code": "cancelled"}}}
                """);
        assertTrue(TicketmasterEventMapper.map(node, 1).isEmpty());
    }

    @Test
    void eventWithoutStartDateIsSkipped() throws Exception {
        JsonNode node = parse("""
                {"id": "x", "name": "TBA", "dates": {"start": {"dateTBA": true}}}
                """);
        assertTrue(TicketmasterEventMapper.map(node, 1).isEmpty());
    }

    @Test
    void missingVenueAndImagesLeaveFieldsNull() throws Exception {
        JsonNode node = parse("""
                {"id": "x", "name": "Bare", "dates": {"start": {"dateTime": "2026-10-01T09:30:00Z"}}}
                """);
        Optional<Event> mapped = TicketmasterEventMapper.map(node, 1);
        Event event = mapped.orElseThrow();
        assertNull(event.getVenueName());
        assertNull(event.getAddress());
        assertNull(event.getLat());
        assertNull(event.getImageUrl());
        assertEquals(Category.OTHER, event.getCategory());
    }

    @Test
    void onlyPlaceholderImagesGiveNoImage() throws Exception {
        JsonNode images = parse("""
                [{"ratio": "16_9", "url": "https://img/placeholder.jpg", "width": 2048, "fallback": true}]
                """);
        assertNull(TicketmasterEventMapper.pickImageUrl(images));
    }
}
