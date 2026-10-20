package com.eventsphere.app.ai;

import com.eventsphere.app.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationCacheTest {

    private static final int USER = 7;

    private RecommendationCache cache;
    private List<Recommendation> ranked;

    @BeforeEach
    void setUp() {
        cache = new RecommendationCache();
        ranked = List.of(new Recommendation(null, "because music"));
    }

    // Mirrors how InterestProfileService builds one: EnumMap counts, unmodifiable stated set.
    private static InterestProfile profile(Set<Category> stated, Map<Category, Integer> attended,
                                           Map<Category, Integer> liked, String bio) {
        Map<Category, Integer> attendedCounts = new EnumMap<>(Category.class);
        attendedCounts.putAll(attended);
        Map<Category, Integer> likedCounts = new EnumMap<>(Category.class);
        likedCounts.putAll(liked);
        return new InterestProfile(Set.copyOf(stated), attendedCounts, likedCounts, bio, null, null);
    }

    private static InterestProfile base() {
        return profile(Set.of(Category.MUSIC), Map.of(Category.MUSIC, 2),
                Map.of(Category.COMEDY, 1), "runs a lot");
    }

    @Test
    void emptyCacheMisses() {
        assertNull(cache.get(USER, base()));
    }

    @Test
    void sameUserAndEqualProfileHits() {
        cache.put(USER, base(), ranked);

        // A separate but equal instance, because the landing page rebuilds the profile
        // from the database on every render and never holds the cached one.
        InterestProfile rebuilt = base();
        assertNotSame(base(), rebuilt);
        assertSame(ranked, cache.get(USER, rebuilt));
    }

    @Test
    void markingGoingMisses() {
        cache.put(USER, base(), ranked);

        InterestProfile afterGoing = profile(Set.of(Category.MUSIC), Map.of(Category.MUSIC, 3),
                Map.of(Category.COMEDY, 1), "runs a lot");
        assertNull(cache.get(USER, afterGoing));
    }

    @Test
    void unmarkingGoingBackToTheStoredStateHits() {
        cache.put(USER, base(), ranked);

        InterestProfile afterGoing = profile(Set.of(Category.MUSIC), Map.of(Category.MUSIC, 3),
                Map.of(Category.COMEDY, 1), "runs a lot");
        assertNull(cache.get(USER, afterGoing));
        assertSame(ranked, cache.get(USER, base()));
    }

    @Test
    void likingAnEventMisses() {
        cache.put(USER, base(), ranked);

        InterestProfile afterLike = profile(Set.of(Category.MUSIC), Map.of(Category.MUSIC, 2),
                Map.of(Category.COMEDY, 2), "runs a lot");
        assertNull(cache.get(USER, afterLike));
    }

    @Test
    void changingPreferredCategoriesMisses() {
        cache.put(USER, base(), ranked);

        InterestProfile afterPrefs = profile(Set.of(Category.MUSIC, Category.TECH),
                Map.of(Category.MUSIC, 2), Map.of(Category.COMEDY, 1), "runs a lot");
        assertNull(cache.get(USER, afterPrefs));
    }

    @Test
    void editingTheBioMisses() {
        cache.put(USER, base(), ranked);

        InterestProfile afterBio = profile(Set.of(Category.MUSIC), Map.of(Category.MUSIC, 2),
                Map.of(Category.COMEDY, 1), "runs a lot, sleeps little");
        assertNull(cache.get(USER, afterBio));
    }

    @Test
    void anotherUserMissesEvenWithAnIdenticalProfile() {
        cache.put(USER, base(), ranked);

        assertNull(cache.get(USER + 1, base()));
    }

    @Test
    void clearDropsTheEntry() {
        cache.put(USER, base(), ranked);
        cache.clear();

        assertNull(cache.get(USER, base()));
    }

    @Test
    void putReplacesThePreviousEntry() {
        cache.put(USER, base(), ranked);

        InterestProfile newer = profile(Set.of(Category.MUSIC), Map.of(Category.MUSIC, 3),
                Map.of(Category.COMEDY, 1), "runs a lot");
        List<Recommendation> newerRanked = List.of(new Recommendation(null, "because comedy"));
        cache.put(USER, newer, newerRanked);

        assertSame(newerRanked, cache.get(USER, newer));
        assertNull(cache.get(USER, base()));
    }

    @Test
    void profileWithNoSignalsStillHits() {
        InterestProfile nothing = profile(Set.of(), Map.of(), Map.of(), null);
        cache.put(USER, nothing, ranked);

        assertSame(ranked, cache.get(USER, profile(Set.of(), Map.of(), Map.of(), null)));
    }
}