package com.eventsphere.app.ai;

import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EventRecommenderTest {

    private MockAiClient ai;
    private EventRecommender recommender;

    private static final InterestProfile WITH_INTERESTS =
            new InterestProfile(Set.of(Category.MUSIC), Map.of(), Map.of(), null, null, null);
    private static final InterestProfile EMPTY_PROFILE =
            new InterestProfile(Set.of(), Map.of(), Map.of(), null, null, null);

    @BeforeEach
    void setUp() {
        ai = new MockAiClient();
        recommender = new EventRecommender(ai);
    }

    @Test
    void emptyCandidatesReturnsEmpty() {
        List<Recommendation> result = recommender.recommend(WITH_INTERESTS, List.of());
        assertTrue(result.isEmpty());
        assertEquals(0, ai.callCount);
    }

    @Test
    void nullAiReturnsUnranked() {
        EventRecommender noAi = new EventRecommender((IAiClient) null);
        List<Event> candidates = List.of(event(1), event(2));
        List<Recommendation> result = noAi.recommend(WITH_INTERESTS, candidates);

        assertEquals(2, result.size());
        assertNull(result.get(0).reason());
    }

    @Test
    void emptyProfileReturnsUnranked() {
        List<Event> candidates = List.of(event(1), event(2));
        List<Recommendation> result = recommender.recommend(EMPTY_PROFILE, candidates);

        assertEquals(2, result.size());
        assertEquals(0, ai.callCount);
    }

    @Test
    void parsesValidJson() {
        ai.setReply("{\"ranked\":[{\"id\":2,\"reason\":\"Great match\"},{\"id\":1,\"reason\":\"Also good\"}]}");
        List<Recommendation> result = recommender.recommend(WITH_INTERESTS, List.of(event(1), event(2)));

        assertEquals(2, result.size());
        assertEquals(2, result.get(0).event().getEventId());
        assertEquals("Great match", result.get(0).reason());
        assertEquals(1, result.get(1).event().getEventId());
    }

    @Test
    void stripsCodeFencesBeforeParsing() {
        ai.setReply("```json\n{\"ranked\":[{\"id\":1,\"reason\":\"Nice\"}]}\n```");
        List<Recommendation> result = recommender.recommend(WITH_INTERESTS, List.of(event(1)));

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).event().getEventId());
    }

    @Test
    void unknownIdInResponseIsIgnored() {
        ai.setReply("{\"ranked\":[{\"id\":99,\"reason\":\"Ghost\"},{\"id\":1,\"reason\":\"Real\"}]}");
        List<Recommendation> result = recommender.recommend(WITH_INTERESTS, List.of(event(1)));

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).event().getEventId());
    }

    @Test
    void aiThrowsFallsBackToUnranked() {
        ai.setShouldThrow(true);
        List<Event> candidates = List.of(event(1), event(2));
        List<Recommendation> result = recommender.recommend(WITH_INTERESTS, candidates);

        assertEquals(2, result.size());
        assertNull(result.get(0).reason());
    }

    @Test
    void resultCappedAtEight() {
        StringBuilder json = new StringBuilder("{\"ranked\":[");
        List<Event> candidates = new java.util.ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            candidates.add(event(i));
            if (i > 1) json.append(',');
            json.append("{\"id\":").append(i).append(",\"reason\":\"r\"}");
        }
        json.append("]}");
        ai.setReply(json.toString());

        List<Recommendation> result = recommender.recommend(WITH_INTERESTS, candidates);
        assertEquals(8, result.size());
    }

    private static Event event(int id) {
        return new Event(id, "Event " + id, null, Category.MUSIC,
                Instant.now().plusSeconds(3600), null,
                "Venue", "Addr", null, null,
                null, null, Instant.now(), false, 1, null, 0, 0);
    }
}
