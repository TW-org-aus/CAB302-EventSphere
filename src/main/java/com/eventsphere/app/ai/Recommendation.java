package com.eventsphere.app.ai;

import com.eventsphere.app.model.Event;

/**
 * One recommended event and the reason the model gave for it.
 *
 * The reason is shown on the card, so the user can see why something was
 * suggested rather than being handed an unexplained list.
 */
public record Recommendation(Event event, String reason) { }