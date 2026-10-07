package com.eventsphere.app.ai;

import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ranks candidate events against a user's interest profile using the AI client.
 * Falls back to candidate order whenever ranking is unavailable, so
 * landing page always has something to show.
 */

public class EventRecommender {

    private static final String SYSTEM_PROMPT = """
            You recommend events to a user based on their interests.
            You will be given a profile and a numbered list of candidate events.
            Pick the best matches and order them best first.
            Reply with JSON only, no preamble and no code fences, in exactly this shape:
            {"ranked":[{"id":12,"reason":"short reason"}]}
            Only use ids from the candidate list. Give at most 8 results.
            Each reason must be one short sentence addressed to the user,
            explaining the match in terms of their interests.
            """;

    private static final int MAX_TOKENS = 700;
    private static final int MAX_RESULTS = 8;

    private final AiClient ai;
    private final ObjectMapper json = new ObjectMapper();

    public EventRecommender(AiClient ai) {
        this.ai = ai;
    }

    // Recommended events for this profile.
    // Never throws as unavailable model returns the candidates unranked.
    public List<Recommendation> recommend(InterestProfile profile, List<Event> candidates) {
        if (candidates.isEmpty()) {
            return List.of();
        }
        if (ai == null || profile.isEmpty()) {
            return unranked(candidates);
        }

        try {
            String reply = ai.complete(SYSTEM_PROMPT, buildPrompt(profile, candidates), MAX_TOKENS);
            List<Recommendation> ranked = parse(reply, candidates);
            return ranked.isEmpty() ? unranked(candidates) : ranked;
        } catch (Exception e) {
            System.err.println("Could not rank recommendations: " + e.getMessage());
            return unranked(candidates);
        }
    }

    private String buildPrompt(InterestProfile profile, List<Event> candidates) {
        StringBuilder prompt = new StringBuilder("User profile:\n");

        List<Category> ranked = profile.rankedCategories(5);
        if (!ranked.isEmpty()) {
            prompt.append("Interests, strongest first: ")
                    .append(ranked.stream().map(Category::getDbValue).toList())
                    .append('\n');
        }
        if (profile.bio() != null && !profile.bio().isBlank()) {
            prompt.append("About them: ").append(profile.bio()).append('\n');
        }
        if (!profile.attendedByCategory().isEmpty()) {
            prompt.append("Events attended by category: ")
                    .append(describe(profile.attendedByCategory())).append('\n');
        }
        if (!profile.likedByCategory().isEmpty()) {
            prompt.append("Events liked by category: ")
                    .append(describe(profile.likedByCategory())).append('\n');
        }

        prompt.append("\nCandidates:\n");
        for (Event event : candidates) {
            prompt.append(event.getEventId()).append(" | ")
                    .append(event.getTitle()).append(" | ")
                    .append(event.getCategory() == null ? "Uncategorised" : event.getCategory().getDbValue())
                    .append(" | ")
                    .append(event.getVenueName() == null ? "" : event.getVenueName())
                    .append('\n');
        }
        return prompt.toString();
    }

    private static String describe(Map<Category, Integer> counts) {
        return counts.entrySet().stream()
                .map(entry -> entry.getValue() + " " + entry.getKey().getDbValue())
                .toList()
                .toString();
    }

    // Reads the model's JSON into Recommendations.
    // Ids are checked against the candidates, unknown id would be a null event.
    private List<Recommendation> parse(String reply, List<Event> candidates) throws Exception {
        Map<Integer, Event> byId = new LinkedHashMap<>();
        for (Event event : candidates) {
            byId.put(event.getEventId(), event);
        }

        JsonNode root = json.readTree(stripFences(reply));
        JsonNode ranked = root.path("ranked");
        if (!ranked.isArray()) {
            return List.of();
        }

        List<Recommendation> results = new ArrayList<>();
        for (JsonNode node : ranked) {
            Event event = byId.get(node.path("id").asInt(-1));
            if (event == null) {
                continue;   // if id not in candidate list, ignore it
            }
            String reason = node.path("reason").asText("").strip();
            results.add(new Recommendation(event, reason));
            if (results.size() == MAX_RESULTS) {
                break;
            }
        }
        return results;
    }

    // Model sometimes wraps JSON in ```json fences despite being asked not to.
    private static String stripFences(String reply) {
        String trimmed = reply.strip();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                return trimmed.substring(firstNewline + 1, lastFence).strip();
            }
        }
        return trimmed;
    }

    private static List<Recommendation> unranked(List<Event> candidates) {
        return candidates.stream()
                .limit(MAX_RESULTS)
                .map(event -> new Recommendation(event, null))
                .toList();
    }
}