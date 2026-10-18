package com.eventsphere.app.ai;

import com.eventsphere.app.dao.IGoingDAO;
import com.eventsphere.app.dao.ILikeDAO;
import com.eventsphere.app.dao.IPreferenceDAO;
import com.eventsphere.app.dao.IUserDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.Preference;
import com.eventsphere.app.model.User;
import com.eventsphere.app.service.UserService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InterestProfileServiceTest {

    private static final User USER = new User(1, "Aang", "Air", "aang@example.com",
            "hash", null, null, LocalDate.now(), true, true, null);

    @Test
    void happyPathBuildsFullProfile() {
        Preference prefs = new Preference(1, null, "loves music", Set.of(Category.MUSIC));
        Event goingEvent = event(1, Category.SPORTS);
        Event likedEvent = event(2, Category.COMEDY);

        InterestProfileService service = new InterestProfileService(
                userServiceWith(prefs),
                goingDao(List.of(goingEvent), false),
                likeDao(List.of(likedEvent), false));

        InterestProfile profile = service.buildFor(USER);

        assertTrue(profile.statedInterests().contains(Category.MUSIC));
        assertEquals("loves music", profile.bio());
        assertEquals(1, profile.attendedByCategory().getOrDefault(Category.SPORTS, 0));
        assertEquals(1, profile.likedByCategory().getOrDefault(Category.COMEDY, 0));
    }

    @Test
    void goingDaoFailureDegradesgracefully() {
        InterestProfileService service = new InterestProfileService(
                userServiceWith(new Preference(1, null, null, Set.of())),
                goingDao(List.of(), true),
                likeDao(List.of(), false));

        InterestProfile profile = service.buildFor(USER);
        assertTrue(profile.attendedByCategory().isEmpty());
    }

    @Test
    void likesDaoFailureDegradesgracefully() {
        InterestProfileService service = new InterestProfileService(
                userServiceWith(new Preference(1, null, null, Set.of())),
                goingDao(List.of(), false),
                likeDao(List.of(), true));

        InterestProfile profile = service.buildFor(USER);
        assertTrue(profile.likedByCategory().isEmpty());
    }

    @Test
    void eventWithNullCategoryNotCounted() {
        Event noCategory = event(1, null);

        InterestProfileService service = new InterestProfileService(
                userServiceWith(new Preference(1, null, null, Set.of())),
                goingDao(List.of(noCategory), false),
                likeDao(List.of(), false));

        InterestProfile profile = service.buildFor(USER);
        assertTrue(profile.attendedByCategory().isEmpty());
    }

    // ---- helpers ----

    private static Event event(int id, Category category) {
        return new Event(id, "Event", null, category,
                Instant.now(), null, null, null, null, null,
                null, null, Instant.now(), false, 1, null, 0, 0);
    }

    private static UserService userServiceWith(Preference prefs) {
        return new UserService(new StubUserDAO(), new IPreferenceDAO() {
            @Override public Preference findByUser(int userId) { return prefs; }
            @Override public void upsertAddress(int userId, String address) {}
            @Override public void upsertBio(int userId, String bio) {}
            @Override public void replaceCategories(int userId, Collection<Category> categories) {}
        });
    }

    private static IGoingDAO goingDao(List<Event> events, boolean shouldThrow) {
        return new IGoingDAO() {
            @Override public List<Event> findEventsForUser(int userId) {
                if (shouldThrow) throw new RuntimeException("DB down");
                return events;
            }
            @Override public boolean markGoing(int u, int e) { return false; }
            @Override public boolean unmarkGoing(int u, int e) { return false; }
            @Override public boolean isGoing(int u, int e) { return false; }
            @Override public boolean shareGoingEvent(int a, int b) { return false; }
            @Override public List<User> findUsersForEvent(int eventId) { return List.of(); }
            @Override public void setMuted(int u, int e, boolean m) {}
            @Override public boolean isMuted(int u, int e) { return false; }
        };
    }

    private static ILikeDAO likeDao(List<Event> events, boolean shouldThrow) {
        return new ILikeDAO() {
            @Override public List<Event> findEventsLikedByUser(int userId) {
                if (shouldThrow) throw new RuntimeException("DB down");
                return events;
            }
            @Override public boolean like(int u, int e) { return false; }
            @Override public boolean unlike(int u, int e) { return false; }
            @Override public boolean isLikedBy(int u, int e) { return false; }
            @Override public int countForEvent(int e) { return 0; }
        };
    }

    private static class StubUserDAO implements IUserDAO {
        @Override public int insert(String fn, String ln, String em, String pw, Double lat, Double lng) { throw new UnsupportedOperationException(); }
        @Override public int insert(String fn, String ln, String em, String pw, Double lat, Double lng, String username) { throw new UnsupportedOperationException(); }
        @Override public Optional<User> findByEmail(String email) { return Optional.empty(); }
        @Override public Optional<User> findById(int userId) { return Optional.empty(); }
        @Override public List<User> findAllActive() { return List.of(); }
        @Override public void update(User user) {}
        @Override public void setHomeLocation(int userId, Double lat, Double lng) {}
        @Override public void setNotifyEnabled(int userId, boolean enabled) {}
        @Override public void deactivate(int userId) {}
        @Override public boolean isUsernameTaken(String username, int excludeUserId) { return false; }
        @Override public void setUsername(int userId, String username) {}
    }
}
