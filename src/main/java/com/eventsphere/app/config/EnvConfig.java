package com.eventsphere.app.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

// ENV reader
public final class EnvConfig {

    private static final Map<String, String> VALUES = load(Path.of(".env"));

    private EnvConfig() { }

    public static String get(String key) {
        String fromEnvironment = System.getenv(key);
        return fromEnvironment != null ? fromEnvironment : VALUES.get(key);
    }

    public static String require(String key) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(key + " is not set. Add it to .env in the project root.");
        }
        return value;
    }

    private static Map<String, String> load(Path file) {
        Map<String, String> values = new HashMap<>();
        if (!Files.exists(file)) {
            return values;
        }
        try {
            for (String line : Files.readAllLines(file)) {
                String trimmed = line.strip();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int equals = trimmed.indexOf('=');
                if (equals <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, equals).strip();
                String value = stripQuotes(trimmed.substring(equals + 1).strip());
                values.put(key, value);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + file.toAbsolutePath(), e);
        }
        return values;
    }

    private static String stripQuotes(String value) {
        boolean quoted = value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")));
        return quoted ? value.substring(1, value.length() - 1) : value;
    }
}
