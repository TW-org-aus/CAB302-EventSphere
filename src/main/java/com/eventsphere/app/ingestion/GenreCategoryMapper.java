package com.eventsphere.app.ingestion;

import com.eventsphere.app.model.Category;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Locale;
import java.util.Map;

// Genre mapper is here because ticketmaster genre's do NOT match up with the ones we have fully so this class
//attempts to fix that by doing a rough genre mapping

// Maps a Ticketmaster classification (see ticketmaster scheama in docs: segment > genre > sub-genre) onto one of the fixed Categories.
// Genre is checked before segment because Family, Community, Food & Drink and Nightlife only appear
// at genre level: Ticketmaster files them under the "Miscellaneous" segment.
public class GenreCategoryMapper {


    private static final Map<String, Category> BY_GENRE = Map.of(
            "family", Category.FAMILY,
            "children's theatre", Category.FAMILY,
            "community/civic", Category.COMMUNITY,
            "fairs & festivals", Category.COMMUNITY,
            "food & drink", Category.FOOD_DRINK,
            "nightlife", Category.NIGHTLIFE);

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
