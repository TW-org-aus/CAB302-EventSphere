package com.eventsphere.app.ai;

import com.eventsphere.app.config.EnvConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// Client for OpenAI-compatible chat completions endpoint.
// Same shape so switching is two values in .env rather than a code change if limits are reached or models change.
public class AiClient {

    private static final String DEFAULT_BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/openai/";
    private static final String DEFAULT_MODEL = "gemini-3.8-flash";

    private final HttpClient http;
    private final ObjectMapper json = new ObjectMapper();
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    // Reads configuration from .env. Throws if AI_API_KEY is missing.
    public AiClient() {
        this(EnvConfig.require("AI_API_KEY"),
                orDefault(EnvConfig.get("AI_BASE_URL"), DEFAULT_BASE_URL),
                orDefault(EnvConfig.get("AI_MODEL"), DEFAULT_MODEL));
    }

    // Explicit configuration, so tests can run without a .env.
    public AiClient(String apiKey, String baseUrl, String model) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.model = model;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // True when a key is configured, so calls can skip AI features cleanly.
    public static boolean isConfigured() {
        String key = EnvConfig.get("AI_API_KEY");
        return key != null && !key.isBlank();
    }

    // Sends one prompt and returns the model's text reply.
    // MaxTokens caps the response length, which also caps cost and latency.
    public String complete(String systemPrompt, String userPrompt, int maxTokens)
            throws IOException, InterruptedException {

        ObjectNode body = json.createObjectNode();
        body.put("model", model);
        body.put("max_tokens", maxTokens);

        ArrayNode messages = body.putArray("messages");
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            ObjectNode system = messages.addObject();
            system.put("role", "system");
            system.put("content", systemPrompt);
        }
        ObjectNode user = messages.addObject();
        user.put("role", "user");
        user.put("content", userPrompt);

        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "chat/completions"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("AI request failed with status " + response.statusCode()
                    + ": " + response.body());
        }

        JsonNode root = json.readTree(response.body());
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (content.isMissingNode()) {
            throw new IOException("AI response had no content: " + response.body());
        }
        return content.asText().strip();
    }

    private static String orDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}