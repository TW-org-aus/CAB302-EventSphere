package com.eventsphere.app.ingestion;

import com.eventsphere.app.model.Category;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenreCategoryMapperTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private static JsonNode classifications(String json) throws Exception {
        return JSON.readTree(json);
    }

    @Test
    void segmentMapsWhenGenreIsNotSpecial() throws Exception {
        JsonNode node = classifications("""
                [{"primary": true, "segment": {"name": "Music"}, "genre": {"name": "Rock"}}]
                """);
        assertEquals(Category.MUSIC, GenreCategoryMapper.map(node));
    }

    @Test
    void genreWinsOverMiscellaneousSegment() throws Exception {
        JsonNode node = classifications("""
                [{"primary": true, "segment": {"name": "Miscellaneous"}, "genre": {"name": "Family"}}]
                """);
        assertEquals(Category.FAMILY, GenreCategoryMapper.map(node));
    }

    @Test
    void matchingIgnoresCaseAndWhitespace() throws Exception {
        JsonNode node = classifications("""
                [{"segment": {"name": "  ARTS & THEATRE "}, "genre": {"name": "Comedy"}}]
                """);
        assertEquals(Category.ARTS_THEATRE, GenreCategoryMapper.map(node));
    }

    @Test
    void primaryClassificationIsUsedOverFirst() throws Exception {
        JsonNode node = classifications("""
                [{"primary": false, "segment": {"name": "Film"}},
                 {"primary": true, "segment": {"name": "Sports"}}]
                """);
        assertEquals(Category.SPORTS, GenreCategoryMapper.map(node));
    }

    @Test
    void unknownOrMissingIsOther() throws Exception {
        JsonNode undefined = classifications("""
                [{"segment": {"name": "Undefined"}, "genre": {"name": "Undefined"}}]
                """);
        assertEquals(Category.OTHER, GenreCategoryMapper.map(undefined));
        assertEquals(Category.OTHER, GenreCategoryMapper.map(MissingNode.getInstance()));
    }
}
