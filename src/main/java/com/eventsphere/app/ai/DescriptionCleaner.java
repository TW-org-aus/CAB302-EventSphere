package com.eventsphere.app.ai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.eventsphere.app.model.Event;

// Tidies event descriptions for display. Ingested descriptions arrive with HTML
// tags, entities and marketing boilerplate that read badly on the event page.
// Results are cached per event id for the life of the run, so reopening a page costs nothing.
// Any failure returns original text, so the page always shows something even with no API key.
public class DescriptionCleaner {

    private static final String SYSTEM_PROMPT = """
             You write event descriptions for an event listings app.
                    You will be given the raw description plus metadata about the event.
                    Strip HTML tags and entities, remove ticketing boilerplate and
                    all-caps shouting, and fix broken spacing.
                    Keep all real details: what the event is, who is performing,
                    what is included, age limits, what to bring, and any cultural significance.
                    If the raw description is thin, expand naturally using the metadata provided —
                    describe the category, venue, or likely atmosphere — but never invent
                    specific facts (lineups, prices, times) that are not in the input.
                    Reply with the cleaned description only, no preamble, no quotes.
                    Write between 50 and 150 words.
            """;

    private static final int MAX_TOKENS = 400;

    private final AiClient ai;
    private final Map<Integer, String> cache = new ConcurrentHashMap<>();

    public DescriptionCleaner(AiClient ai) {
        this.ai = ai;
    }

    // Cleaned description for the event, or the original when cleaning is unavailable.
    // Blocking: call this off the JavaFX thread.

    public String clean(Event event) {
        String original = event.getDescription();
        if (original == null || original.isBlank()) return original;
        if (ai == null) return original;

        String cached = cache.get(event.getEventId());
        if (cached != null) return cached;

        try {
            String userMessage = buildMessage(event);
            String cleaned = ai.complete(SYSTEM_PROMPT, userMessage, MAX_TOKENS);
            if (cleaned.isBlank()) return original;
            cache.put(event.getEventId(), cleaned);
            return cleaned;
        } catch (Exception e) {
            System.err.println("Could not clean description for event " + event.getEventId()
                    + ": " + e.getMessage());
            return original;
        }
    }

    // just a lil method I cooked up to help format the message nice and clean on a silver platter for Mr gemini
    private String buildMessage(Event event) {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(event.getTitle()).append("\n");
        if (event.getCategory() != null) {
            sb.append("Category: ").append(event.getCategory()).append("\n");
        }
        if (event.getVenueName() != null) {
            sb.append("Venue: ").append(event.getVenueName()).append("\n");
        }
        sb.append("\nRaw description:\n").append(event.getDescription());
        return sb.toString();
    }
}