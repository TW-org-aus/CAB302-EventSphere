package com.eventsphere.app.seeding;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.Database.Database;
import com.eventsphere.app.dao.CommentDAO;
import com.eventsphere.app.dao.EventDAO;
import com.eventsphere.app.dao.GoingDAO;
import com.eventsphere.app.dao.SourceDAO;
import com.eventsphere.app.dao.UserDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.Source;
import com.eventsphere.app.model.User;

import java.time.temporal.ChronoUnit;
import java.sql.Connection;
import java.time.Instant;
import java.util.Optional;

public class DatabaseSeeder {

    private static final String SEED_SOURCE_NAME = "Manual Seed";
    private static final String SEED_SOURCE_URL = "n/a";

    private static final String USER_ONE_EMAIL = "seed.user1@example.com";
    private static final String USER_TWO_EMAIL = "seed.user2@example.com";

    private static final String SEED_EVENT_TITLE = "EventSphere Demo Community Night";

    public static void main(String[] args) {

        Connection connection = Database.DBConnect();

        // Makes sure all tables exist before inserting anything.
        new DBController(connection);

        SourceDAO sourceDAO = new SourceDAO(connection);
        UserDAO userDAO = new UserDAO(connection);
        EventDAO eventDAO = new EventDAO(connection);
        GoingDAO goingDAO = new GoingDAO(connection);
        CommentDAO commentDAO = new CommentDAO(connection);

        try {

            // 1. Source MUST exist before Events.
            int sourceId = findOrCreateSource(sourceDAO);

            // 2. Create/reuse two demo users.
            int userOneId = findOrCreateUser(
                    userDAO,
                    "Demo",
                    "UserOne",
                    USER_ONE_EMAIL
            );

            int userTwoId = findOrCreateUser(
                    userDAO,
                    "Demo",
                    "UserTwo",
                    USER_TWO_EMAIL
            );

            // 3. Find the seed event if it already exists.
            // search() filters in SQL, so this doesn't load every ingested event to find one title.
            Optional<Event> existingEvent = eventDAO.search(SEED_EVENT_TITLE, null, null, null)
                    .stream()
                    .filter(event -> SEED_EVENT_TITLE.equals(event.getTitle()))
                    .findFirst();

            int eventId;

            if (existingEvent.isPresent()) {

                eventId = existingEvent.get().getEventId();

                System.out.println("Seed event already exists. Reusing EventID: " + eventId);

            } else {

                Event seedEvent = new Event(
                        SEED_EVENT_TITLE,
                        "A demo community event used for EventSphere development and testing.",
                        Category.COMMUNITY,
                        Instant.parse("2026-10-10T09:00:00Z"),
                        Instant.parse("2026-10-10T12:00:00Z"),
                        "Brisbane Demo Venue",
                        "Brisbane QLD",
                        -27.4698,
                        153.0251,
                        null,
                        null,
                        sourceId
                );

                eventId = eventDAO.insert(seedEvent);

                System.out.println("Created seed event with EventID: " + eventId);

                // Only insert this comment when the event is first created.
                commentDAO.insert(
                        userOneId,
                        eventId,
                        "Looking forward to this event!",
                        null
                );
            }

            // 4. Both users attend the SAME event.
            // markGoing uses INSERT OR IGNORE, so repeating this is safe.
            goingDAO.markGoing(userOneId, eventId);
            goingDAO.markGoing(userTwoId, eventId);

            // 5. Additional upcoming Brisbane events so list and map views have data.
            // Dates are relative to the present so a freshly seeded database always has future events.
            seedEventIfAbsent(eventDAO, sourceId, "Boiler Room Brisbane",
                    "Warehouse party", Category.NIGHTLIFE, 3,
                    "Brisbane Showgrounds", "600 Gregory Terrace, Bowen Hills", -27.4503, 153.0331, "boiler-room.jpeg");

            seedEventIfAbsent(eventDAO, sourceId, "Sunrise Run Club 5k",
                    "River run, coffee and vibes", Category.COMMUNITY, 5,
                    "Riverwalk", "New Farm", -27.4682, 153.0436, "runclub.jpg");

            seedEventIfAbsent(eventDAO, sourceId, "South Bank Night Market",
                    "Food stalls and live music", Category.FOOD_DRINK, 8,
                    "South Bank Parklands", "Stanley St Plaza, South Brisbane", -27.4764, 153.0212, "nightmarket.jpg");

            seedEventIfAbsent(eventDAO, sourceId, "Trivia Night",
                    "Teams of four, first round is free", Category.COMMUNITY, 11,
                    "Botanic Bar", "P Block, Level 3/2 George St, Brisbane City", -27.4772, 153.0283, "trivia.jpeg");

            seedEventIfAbsent(eventDAO, sourceId, "Brisbane Car Meet",
                    "Car meet and social night for enthusiasts", Category.COMMUNITY, 14,
                    "Motorculture HQ", "84 Dunhill Crescent, Morningside", -27.4633, 153.0732, "edit2.png");


            System.out.println("Database seeding completed successfully.");

        } finally {

            Database.close();
        }
    }
    private static void seedEventIfAbsent(EventDAO eventDAO, int sourceId,
                                          String title, String description,
                                          Category category, int daysFromNow,
                                          String venueName, String address,
                                          double lat, double lng,
                                          String imageUrl) {
        boolean exists = eventDAO.search(title, null, null, null)
                .stream()
                .anyMatch(event -> title.equals(event.getTitle()));

        if (exists) {
            System.out.println("Event already exists, skipping: " + title);
            return;
        }

        int id = eventDAO.insert(new Event(
                title, description, category,
                Instant.now().plus(daysFromNow, ChronoUnit.DAYS), null,
                venueName, address, lat, lng, imageUrl, null, sourceId));

        System.out.println("Created event with EventID: " + id + " — " + title);
    }

    private static int findOrCreateSource(SourceDAO sourceDAO) {

        for (Source source : sourceDAO.findAll()) {

            if (SEED_SOURCE_NAME.equals(source.getSiteName())) {

                System.out.println(
                        "Seed source already exists. Reusing SourceID: "
                                + source.getSourceId()
                );

                return source.getSourceId();
            }
        }

        int sourceId = sourceDAO.insert(
                SEED_SOURCE_NAME,
                SEED_SOURCE_URL
        );

        System.out.println("Created seed source with SourceID: " + sourceId);

        return sourceId;
    }


    private static int findOrCreateUser(
            UserDAO userDAO,
            String firstName,
            String lastName,
            String email) {

        Optional<User> existingUser = userDAO.findByEmail(email);

        if (existingUser.isPresent()) {

            System.out.println(
                    "Seed user already exists. Reusing UserID: "
                            + existingUser.get().getUserId()
            );

            return existingUser.get().getUserId();
        }

        int userId = userDAO.insert(
                firstName,
                lastName,
                email,

                // Temporary placeholder until AuthService/PasswordHasher exists.
                "seedPasswordHash"
        );

        System.out.println("Created seed user with UserID: " + userId);
        System.out.println("  Note: PasswordHash is a placeholder, so this user can't log in once hashing lands.");

        return userId;
    }
}
