package com.eventsphere.app.ai;

import com.eventsphere.app.model.Category;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Assembles a profile of the user's interests from their stated preferences and
 * their actual behaviour.
 * Attendance and likes are weighted above stated interests when they disagree
 * as we take the actual behaviour as stronger signals for what they want next.
 *
 * @param statedInterests  categories the user picked in their profile
 * @param attendedByCategory how many events they are going to or have been to, per category
 * @param likedByCategory  how many events they have liked, per category
 * @param bio              free text from their profile, or null
 * @param homeLat          saved home latitude, or null if not set
 * @param homeLng          saved home longitude, or null if not set
 */

public record InterestProfile(
        Set<Category> statedInterests,
        Map<Category, Integer> attendedByCategory,
        Map<Category, Integer> likedByCategory,
        String bio,
        Double homeLat,
        Double homeLng) {

    // True when interest profile is empty, so callers can fall back to a plain date sort.
    public boolean isEmpty() {
        return statedInterests.isEmpty()
                && attendedByCategory.isEmpty()
                && likedByCategory.isEmpty()
                && (bio == null || bio.isBlank());
    }

    // True when the user has saved a home location we can measure distance from.
    public boolean hasLocation() {
        return homeLat != null && homeLng != null;
    }

    // The categories worth mentioning in a prompt, by order of strongest signals:
    // attended, liked, then stated. Caps the list so prompt stays small.

    public List<Category> rankedCategories(int limit) {
        Map<Category, Integer> scores = new java.util.EnumMap<>(Category.class);

        attendedByCategory.forEach((category, count) ->
                scores.merge(category, count * 3, Integer::sum));
        likedByCategory.forEach((category, count) ->
                scores.merge(category, count * 2, Integer::sum));
        statedInterests.forEach(category ->
                scores.merge(category, 1, Integer::sum));

        return scores.entrySet().stream()
                .sorted(Map.Entry.<Category, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }
}
