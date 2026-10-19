package com.eventsphere.app.ai;

import com.eventsphere.app.model.Event;

/** Pairs a recommended event with the AI's one-line reasoning for the suggestion. */
public record Recommendation(Event event, String reason) { }