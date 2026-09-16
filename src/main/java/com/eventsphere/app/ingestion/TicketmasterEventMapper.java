package com.eventsphere.app.ingestion;

import com.eventsphere.app.model.Event;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Turns one Ticketmaster event node into an unsaved Event.
public class TicketmasterEventMapper {

    private TicketmasterEventMapper() { }

    // Empty when the event is cancelled or is missing an id, a name, or a usable start date.
    static Optional<Event> map(JsonNode event, int sourceId) {
        String ticketmasterId = text(event, "id");
        String title = text(event, "name");
        JsonNode dates = event.path("dates");
        if (ticketmasterId == null || title == null || "cancelled".equals(text(dates.path("status"), "code"))) {
            return Optional.empty();
        }

        JsonNode venue = event.path("_embedded").path("venues").path(0);
        ZoneId zone = zoneOf(dates, venue);
        Instant start = parseInstant(dates.path("start"), zone);
        if (start == null) {
            return Optional.empty();
        }

        JsonNode location = venue.path("location");
        return Optional.of(new Event(
                title,
                firstText(event, "description", "info", "pleaseNote"),
                GenreCategoryMapper.map(event.path("classifications")),
                start,
                parseInstant(dates.path("end"), zone),
                text(venue, "name"),
                address(venue),
                parseDouble(text(location, "latitude")),
                parseDouble(text(location, "longitude")),
                pickImageUrl(event.path("images")),
                text(event, "url"),
                sourceId,
                ticketmasterId));
    }

    // Widest 16:9 image, just the ratio that fits most things for landcape. Falls back to the widest of any ratio.
    // Ticketmaster marks its generic placeholder images "fallback"; those are skipped.
    static String pickImageUrl(JsonNode images) {
        JsonNode best_img = null;
        for (JsonNode image : images) {
            if (image.path("fallback").asBoolean(false) || text(image, "url") == null) {
                continue;
            }
            if (best_img == null || imageScore(image) > imageScore(best_img)) {
                best_img = image;
            }
        }
        return best_img == null ? null : text(best_img, "url");
    }

    private static long imageScore(JsonNode image) {
        long ratioBonus = "16_9".equals(image.path("ratio").asText()) ? 1_000_000L : 0L;
        return ratioBonus + image.path("width").asLong(0);
    }


    static String address(JsonNode venue) {
        List<String> locality = new ArrayList<>();
        addIfPresent(locality, text(venue.path("city"), "name"));
        addIfPresent(locality, text(venue.path("state"), "stateCode"));
        addIfPresent(locality, text(venue, "postalCode"));

        List<String> parts = new ArrayList<>();
        addIfPresent(parts, text(venue.path("address"), "line1"));
        if (!locality.isEmpty()) {
            parts.add(String.join(" ", locality));
        }
        return parts.isEmpty() ? null : String.join(", ", parts);
    }

    // Prefers the UTC dateTime. Without it, reads localDate (and localTime, else midnight) in the event's
    // time zone.
    static Instant parseInstant(JsonNode date, ZoneId zone) {
        try {
            String dateTime = text(date, "dateTime");
            if (dateTime != null) {
                return Instant.parse(dateTime);
            }
            String localDate = text(date, "localDate");
            if (localDate == null) {
                return null;
            }
            String localTime = text(date, "localTime");
            LocalTime time = localTime == null ? LocalTime.MIDNIGHT : LocalTime.parse(localTime);
            return LocalDate.parse(localDate).atTime(time).atZone(zone).toInstant();
        } catch (DateTimeException e) {
            return null;
        }
    }


    private static ZoneId zoneOf(JsonNode dates, JsonNode venue) {
        for (String zone : new String[] { text(dates, "timezone"), text(venue, "timezone") }) {
            if (zone != null) {
                try {
                    return ZoneId.of(zone);
                } catch (DateTimeException ignored) {
                    // Unknown zone id; try the next one.
                }
            }
        }
        return ZoneOffset.UTC;
    }

    private static Double parseDouble(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = text(node, field);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    // The field as trimmed text, or null when it's missing, JSON null, or blank.
    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText().strip();
        return text.isEmpty() ? null : text;
    }

    private static void addIfPresent(List<String> parts, String value) {
        if (value != null) {
            parts.add(value);
        }
    }
}
