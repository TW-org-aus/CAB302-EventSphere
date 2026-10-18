package com.eventsphere.app.ai;

import java.io.IOException;

public interface IAiClient {
    String complete(String systemPrompt, String userPrompt, int maxTokens)
            throws IOException, InterruptedException;
}
