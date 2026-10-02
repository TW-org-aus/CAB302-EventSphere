package com.eventsphere.app.ai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Tidies event descriptions for display. Ingested descriptions arrive with HTML
// tags, entities and marketing boilerplate that read badly on the event page.
// Results are cached per event id for the life of the run, so reopening a page costs nothing.
// Any failure returns original text, so the page always shows something even with no API key.
public class DescriptionCleaner {

    private static final String SYSTEM_PROMPT = """
            You tidy event descriptions for an event listings app.
            Strip HTML tags and entities, remove ticketing boilerplate and
            all-caps shouting, and fix broken spacing.
            Keep all real details: what the event is, who is performing,
            what is included, age limits, what to bring, and summarise any cultural significance details included.
            Reply with the cleaned description only, no preamble, no quotes.
            If the text is already clean, reply with it unchanged.
            Keep it under 100 words.
            """;

    private static final int MAX_TOKENS = 300;

    private final AiClient ai;
    private final Map<Integer, String> cache = new ConcurrentHashMap<>();

    public DescriptionCleaner(AiClient ai) {
        this.ai = ai;
    }

    // Cleaned description for the event, or the original when cleaning is unavailable.
    // Blocking: call this off the JavaFX thread.
    public String clean(int eventId, String original) {
        if (original == null || original.isBlank()) {
            return original;
        }
        if (ai == null) {
            return original;
        }

        String cached = cache.get(eventId);
        if (cached != null) {
            return cached;
        }

        try {
            String cleaned = ai.complete(SYSTEM_PROMPT, original, MAX_TOKENS);
            if (cleaned.isBlank()) {
                return original;
            }
            cache.put(eventId, cleaned);
            return cleaned;
        } catch (Exception e) {
            System.err.println("Could not clean description for event " + eventId
                    + ": " + e.getMessage());
            return original;
        }
    }
}