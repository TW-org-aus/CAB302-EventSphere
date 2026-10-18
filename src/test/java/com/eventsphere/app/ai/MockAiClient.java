package com.eventsphere.app.ai;

import java.io.IOException;

class MockAiClient implements IAiClient {

    private String reply = "";
    private boolean shouldThrow = false;
    int callCount = 0;

    void setReply(String reply) { this.reply = reply; }
    void setShouldThrow(boolean shouldThrow) { this.shouldThrow = shouldThrow; }

    @Override
    public String complete(String systemPrompt, String userPrompt, int maxTokens)
            throws IOException, InterruptedException {
        callCount++;
        if (shouldThrow) throw new IOException("AI unavailable");
        return reply;
    }
}
