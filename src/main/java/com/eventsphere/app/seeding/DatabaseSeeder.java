package com.eventsphere.app.seeding;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.Database.Database;
import com.eventsphere.app.dao.*;
import com.eventsphere.app.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class DatabaseSeeder {

    private static final String SEED_FILE = "seed-data.json";

    private static final ObjectMapper JSON = new ObjectMapper();

    // Placeholder until AuthService/PasswordHasher exists. Seeded users cannot log in.
    private static final String SEED_PASSWORD_HASH = "seedPasswordHash";

    private final SourceDAO sources;
    private final UserDAO users;
    private final PreferenceDAO preferences;
    private final EventDAO events;
    private final GoingDAO going;
    private final LikeDAO likes;
    private final CommentDAO comments;
    private final ConversationDAO conversations;
    private final MessageDAO messages;
    private final NotificationDAO notifications;

    // JSON key to generated database id, filled in as each section is seeded.
    private final Map<String, Integer> userIds = new HashMap<>();
    private final Map<String, Integer> eventIds = new HashMap<>();
    private final Map<String, Integer> commentIds = new HashMap<>();
    private final Map<String, Integer> conversationIds = new HashMap<>();

    private final List<String> created = new ArrayList<>();

    private DatabaseSeeder(Connection connection) {
        this.sources = new SourceDAO(connection);
        this.users = new UserDAO(connection);
        this.preferences = new PreferenceDAO(connection);
        this.events = new EventDAO(connection);
        this.going = new GoingDAO(connection);
        this.likes = new LikeDAO(connection);
        this.comments = new CommentDAO(connection);
        this.conversations = new ConversationDAO(connection);
        this.messages = new MessageDAO(connection);
        this.notifications = new NotificationDAO(connection);
    }

    // For running the seeder by hand. Opens its own connection and closes it after.
    // The app calls seed() directly instead, on the connection it already holds.
    public static void main(String[] args) {

        Connection connection = Database.DBConnect();

        try {
            // Makes sure all tables exist before inserting anything.
            new DBController(connection);

            seed(connection);

        } finally {

            Database.close();
        }
    }


    public static void seed(Connection connection) {
        new DatabaseSeeder(connection).run(readSeedFile());
    }

    private static JsonNode readSeedFile() {
        try (InputStream in = DatabaseSeeder.class.getResourceAsStream(SEED_FILE)) {
            if (in == null) {
                throw new IllegalStateException(SEED_FILE + " is missing from the resources folder");
            }
            return JSON.readTree(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + SEED_FILE, e);
        }
    }

    private void run(JsonNode root) {
        // Source must exist before Events, Users before everything that references them.
        int sourceId = seedSource(root.path("source"));
        seedUsers(root.path("users"));
        seedEvents(root.path("events"), sourceId);
        seedGoing(root.path("going"));
        seedLikes(root.path("likes"));
        seedComments(root.path("comments"));
        seedConversations(root.path("conversations"));
        seedMessages(root.path("messages"));
        seedNotifications(root.path("notifications"));

        if (!created.isEmpty()) {
            System.out.println("Seeded " + String.join(", ", created) + ".");
        }
    }

    // ----- sections -----

    private int seedSource(JsonNode node) {
        String siteName = node.path("siteName").asText();

        for (Source source : sources.findAll()) {
            if (siteName.equals(source.getSiteName())) {
                return source.getSourceId();
            }
        }

        int sourceId = sources.insert(siteName, node.path("siteUrl").asText());
        created.add("1 source");
        return sourceId;
    }

    private void seedUsers(JsonNode list) {
        int n = 0;
        for (JsonNode node : list) {
            String email = node.path("email").asText();

            Optional<User> existing = users.findByEmail(email);
            int userId;
            if (existing.isPresent()) {
                userId = existing.get().getUserId();
            } else {
                userId = users.insert(
                        node.path("firstName").asText(),
                        node.path("lastName").asText(),
                        email,
                        SEED_PASSWORD_HASH,
                        optionalDouble(node, "homeLat"),
                        optionalDouble(node, "homeLong"));
                n++;
            }
            userIds.put(node.path("key").asText(), userId);

            seedPreferences(node, userId);
        }
        record(n, "user");
    }

    // upsertCity and replaceCategories both overwrite, so this needs no existence check.
    private void seedPreferences(JsonNode userNode, int userId) {
        if (userNode.hasNonNull("city")) {
            preferences.upsertCity(userId, userNode.path("city").asText());
        }
        JsonNode interests = userNode.path("interests");
        if (interests.isArray() && !interests.isEmpty()) {
            List<Category> categories = new ArrayList<>();
            for (JsonNode interest : interests) {
                categories.add(Category.fromDbValue(interest.asText()));
            }
            preferences.replaceCategories(userId, categories);
        }
    }

    private void seedEvents(JsonNode list, int sourceId) {
        int n = 0;
        for (JsonNode node : list) {
            String title = node.path("title").asText();

            Optional<Event> existing = events.search(title, null, null, null).stream()
                    .filter(event -> title.equals(event.getTitle()))
                    .findFirst();

            int eventId;
            if (existing.isPresent()) {
                eventId = existing.get().getEventId();
            } else {
                Instant start = Instant.now().plus(node.path("startInDays").asLong(), ChronoUnit.DAYS);
                Instant end = node.hasNonNull("durationHours")
                        ? start.plus(node.path("durationHours").asLong(), ChronoUnit.HOURS)
                        : null;

                eventId = events.insert(new Event(
                        title,
                        optionalText(node, "description"),
                        Category.fromDbValue(node.path("category").asText()),
                        start,
                        end,
                        optionalText(node, "venueName"),
                        optionalText(node, "address"),
                        optionalDouble(node, "lat"),
                        optionalDouble(node, "lng"),
                        optionalText(node, "imageUrl"),
                        optionalText(node, "ticketUrl"),
                        sourceId));
                n++;
            }
            eventIds.put(node.path("key").asText(), eventId);
        }
        record(n, "event");
    }

    // markGoing uses INSERT OR IGNORE, so repeating this is safe.
    private void seedGoing(JsonNode list) {
        int n = 0;
        for (JsonNode node : list) {
            int userId = userId(node.path("user").asText());
            int eventId = eventId(node.path("event").asText());

            if (going.markGoing(userId, eventId)) {
                n++;
            }
            if (node.path("muted").asBoolean(false)) {
                going.setMuted(userId, eventId, true);
            }
        }
        record(n, "going row");
    }

    private void seedLikes(JsonNode list) {
        int n = 0;
        for (JsonNode node : list) {
            if (likes.like(userId(node.path("user").asText()), eventId(node.path("event").asText()))) {
                n++;
            }
        }
        record(n, "like");
    }

    // Comments are matched on author plus content within the event. Replies are seeded after the comment they point at.
    private void seedComments(JsonNode list) {
        int n = 0;
        for (JsonNode node : list) {
            int userId = userId(node.path("user").asText());
            int eventId = eventId(node.path("event").asText());
            String content = node.path("content").asText();

            Optional<Comment> existing = comments.findByEvent(eventId).stream()
                    .filter(comment -> comment.getUserId() == userId && content.equals(comment.getContent()))
                    .findFirst();

            int commentId;
            if (existing.isPresent()) {
                commentId = existing.get().getCommentId();
            } else {
                Integer replyTo = node.hasNonNull("replyTo")
                        ? commentIds.get(node.path("replyTo").asText())
                        : null;
                commentId = comments.insert(userId, eventId, content, replyTo);
                n++;
            }
            commentIds.put(node.path("key").asText(), commentId);
        }
        record(n, "comment");
    }

    // findOrCreate is idempotent, so this counts only the conversations that were new.
    private void seedConversations(JsonNode list) {
        int n = 0;
        for (JsonNode node : list) {
            int userA = userId(node.path("userA").asText());
            int userB = userId(node.path("userB").asText());

            boolean existedBefore = conversations.findByUser(userA).stream()
                    .anyMatch(conversation -> conversation.otherUserId(userA) == userB);

            Conversation conversation = conversations.findOrCreate(userA, userB);
            if (!existedBefore) {
                n++;
            }
            conversationIds.put(node.path("key").asText(), conversation.getConversationId());
        }
        record(n, "conversation");
    }

    private void seedMessages(JsonNode list) {
        int n = 0;
        for (JsonNode node : list) {
            int conversationId = conversationId(node.path("conversation").asText());
            int senderId = userId(node.path("sender").asText());
            String content = node.path("content").asText();

            boolean exists = messages.findByConversation(conversationId).stream()
                    .anyMatch(message -> message.getSenderId() == senderId
                            && content.equals(message.getContent()));

            if (!exists) {
                messages.insert(conversationId, senderId, content);
                n++;
            }
        }
        record(n, "message");
    }

    private void seedNotifications(JsonNode list) {
        int n = 0;
        for (JsonNode node : list) {
            int userId = userId(node.path("user").asText());
            NotificationType type = NotificationType.fromDbValue(node.path("type").asText());

            Integer eventId = node.hasNonNull("event") ? eventId(node.path("event").asText()) : null;
            Integer commentId = node.hasNonNull("comment") ? commentIds.get(node.path("comment").asText()) : null;
            Integer conversationId = node.hasNonNull("conversation")
                    ? conversationId(node.path("conversation").asText())
                    : null;

            boolean exists = notifications.findByUser(userId).stream()
                    .anyMatch(notification -> notification.getType() == type
                            && Objects.equals(notification.getRelatedEventId(), eventId)
                            && Objects.equals(notification.getRelatedCommentId(), commentId)
                            && Objects.equals(notification.getRelatedConversationId(), conversationId));

            if (!exists) {
                notifications.insert(userId, type, eventId, commentId, conversationId);
                n++;
            }
        }
        record(n, "notification");
    }

    // ----- helpers -----

    private void record(int count, String noun) {
        if (count > 0) {
            created.add(count + " " + noun + (count == 1 ? "" : "s"));
        }
    }

    private int userId(String key) {
        return require(userIds, key, "user");
    }

    private int eventId(String key) {
        return require(eventIds, key, "event");
    }

    private int conversationId(String key) {
        return require(conversationIds, key, "conversation");
    }

    private static int require(Map<String, Integer> ids, String key, String what) {
        Integer id = ids.get(key);
        if (id == null) {
            throw new IllegalStateException(
                    "seed-data.json refers to unknown " + what + " key \"" + key + "\"");
        }
        return id;
    }

    private static String optionalText(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.path(field).asText() : null;
    }

    private static Double optionalDouble(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.path(field).asDouble() : null;
    }
}
