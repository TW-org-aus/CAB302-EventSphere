package com.eventsphere.app.ingestion;

import com.eventsphere.app.model.Category;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Locale;
import java.util.Map;

import static java.util.Map.entry;

// Genre mapper is here because ticketmaster genre's do NOT match up with the ones we have fully so this class
//attempts to fix that by doing a rough genre mapping

// Maps a Ticketmaster classification (segment > genre > sub-genre) onto one of the fixed Categories.
/** Maps Ticketmaster segment/genre strings to app Category values; genre is checked before segment since some categories only appear at genre level. */
public class GenreCategoryMapper {


    // Map.ofEntries rather than Map.of because Map.of only has overloads up to 10 pairs.
    private static final Map<String, Category> BY_GENRE = Map.ofEntries(
            entry("family", Category.FAMILY),
            entry("children's theatre", Category.FAMILY),
            entry("community/civic", Category.COMMUNITY),
            entry("fairs & festivals", Category.FESTIVALS),
            entry("food & drink", Category.FOOD_DRINK),
            entry("nightlife", Category.NIGHTLIFE),
            entry("health/wellness", Category.FITNESS_WELLNESS),
            // Comedy sits under the Arts & Theatre segment, so it must match at genre level.
            entry("comedy", Category.COMEDY));

    private static final Map<String, Category> BY_SEGMENT = Map.of(
            "music", Category.MUSIC,
            "sports", Category.SPORTS,
            "arts & theatre", Category.ARTS_THEATRE,
            "film", Category.FILM);

    private GenreCategoryMapper() { }

    // Takes the event's "classifications" array. Anything unrecognised, including a missing array, is OTHER.
    static Category map(JsonNode classifications) {
        JsonNode classification = primary(classifications);

        Category byGenre = BY_GENRE.get(name(classification, "genre"));
        if (byGenre != null) {
            return byGenre;
        }
        Category bySegment = BY_SEGMENT.get(name(classification, "segment"));
        return bySegment != null ? bySegment : Category.OTHER;
    }

    // The entry flagged primary, else the first one.
    private static JsonNode primary(JsonNode classifications) {
        for (JsonNode classification : classifications) {
            if (classification.path("primary").asBoolean(false)) {
                return classification;
            }
        }
        return classifications.path(0);
    }

    private static String name(JsonNode classification, String level) {
        return classification.path(level).path("name").asText("").strip().toLowerCase(Locale.ROOT);
    }
}
