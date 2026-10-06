package com.eventsphere.app.ai;

import com.eventsphere.app.dao.IGoingDAO;
import com.eventsphere.app.dao.ILikeDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.Preference;
import com.eventsphere.app.model.User;
import com.eventsphere.app.service.UserService;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Assembles a {@link InterestProfile} from everything the app already knows about a user.
 *
 * Reads are defensive: a failure in any one source degrades the profile rather than
 * breaking it, because a recommendation built on partial information is still better
 * than an error on the landing page.
 */
public class InterestProfileService {

    private final UserService users;
    private final IGoingDAO going;
    private final ILikeDAO likes;

    public InterestProfileService(UserService users, IGoingDAO going, ILikeDAO likes) {
        this.users = users;
        this.going = going;
        this.likes = likes;
    }

    public InterestProfile buildFor(User user) {
        Set<Category> stated = Set.of();
        String bio = null;

        try {
            Preference preference = users.getPreferences(user.getUserId());
            stated = preference.getCategories();
            bio = preference.getBio();
        } catch (Exception e) {
            System.err.println("Could not read preferences for user "
                    + user.getUserId() + ": " + e.getMessage());
        }

        // Going covers both past and future events: having been to a thing and intending to go
        // are both stronger signals than a like.
        Map<Category, Integer> attended =
                countByCategory(safely(() -> going.findEventsForUser(user.getUserId()),
                        "going events", user.getUserId()));

        Map<Category, Integer> liked =
                countByCategory(safely(() -> likes.findEventsLikedByUser(user.getUserId()),
                        "liked events", user.getUserId()));

        return new InterestProfile(stated, attended, liked, bio,
                user.getHomeLat(), user.getHomeLong());
    }

    // Events with no category are skipped rather than bucketed into "OTHER", which would
    // make a missing value look like a stated taste for "Other", skewing the profile.
    private static Map<Category, Integer> countByCategory(List<Event> events) {
        Map<Category, Integer> counts = new EnumMap<>(Category.class);
        for (Event event : events) {
            if (event.getCategory() != null) {
                counts.merge(event.getCategory(), 1, Integer::sum);
            }
        }
        return counts;
    }

    private static List<Event> safely(EventSupplier supplier, String what, int userId) {
        try {
            return supplier.get();
        } catch (Exception e) {
            System.err.println("Could not read " + what + " for user " + userId + ": " + e.getMessage());
            return List.of();
        }
    }

    @FunctionalInterface
    private interface EventSupplier {
        List<Event> get() throws Exception;
    }
}