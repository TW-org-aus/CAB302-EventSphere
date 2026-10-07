package com.eventsphere.app.ai;

import com.eventsphere.app.model.Event;

// Gives a recommended event and a reason for the model's decision.

public record Recommendation(Event event, String reason) { }