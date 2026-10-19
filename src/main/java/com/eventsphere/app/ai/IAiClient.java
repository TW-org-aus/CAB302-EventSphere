package com.eventsphere.app.ai;

import java.io.IOException;

/** Contract for the AI chat-completion backend; implementations wrap OpenAI-compatible endpoints. */
public interface IAiClient {
    String complete(String systemPrompt, String userPrompt, int maxTokens)
            throws IOException, InterruptedException;
}
