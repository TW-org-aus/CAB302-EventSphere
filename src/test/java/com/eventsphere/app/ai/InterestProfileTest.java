package com.eventsphere.app.ai;

import com.eventsphere.app.model.Category;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InterestProfileTest {

    private static InterestProfile empty() {
        return new InterestProfile(Set.of(), Map.of(), Map.of(), null, null, null);
    }

    @Test
    void isEmptyWhenNothingSet() {
        assertTrue(empty().isEmpty());
    }

    @Test
    void notEmptyWhenStatedInterestPresent() {
        InterestProfile p = new InterestProfile(Set.of(Category.MUSIC), Map.of(), Map.of(), null, null, null);
        assertFalse(p.isEmpty());
    }

    @Test
    void notEmptyWhenBioPresent() {
        InterestProfile p = new InterestProfile(Set.of(), Map.of(), Map.of(), "loves jazz", null, null);
        assertFalse(p.isEmpty());
    }

    @Test
    void notEmptyWhenAttendedPresent() {
        InterestProfile p = new InterestProfile(Set.of(), Map.of(Category.SPORTS, 2), Map.of(), null, null, null);
        assertFalse(p.isEmpty());
    }

    @Test
    void hasLocationOnlyWhenBothCoordinatesSet() {
        assertFalse(empty().hasLocation());
        assertFalse(new InterestProfile(Set.of(), Map.of(), Map.of(), null, -27.47, null).hasLocation());
        assertTrue(new InterestProfile(Set.of(), Map.of(), Map.of(), null, -27.47, 153.02).hasLocation());
    }

    @Test
    void rankedCategoriesScoresAttendedHighest() {
        // attended×3, liked×2, stated×1
        // MUSIC: attended 1 → score 3
        // SPORTS: liked 1 → score 2
        // COMEDY: stated → score 1
        Map<Category, Integer> attended = new EnumMap<>(Category.class);
        attended.put(Category.MUSIC, 1);
        Map<Category, Integer> liked = new EnumMap<>(Category.class);
        liked.put(Category.SPORTS, 1);

        InterestProfile p = new InterestProfile(Set.of(Category.COMEDY), attended, liked, null, null, null);
        List<Category> ranked = p.rankedCategories(3);

        assertEquals(List.of(Category.MUSIC, Category.SPORTS, Category.COMEDY), ranked);
    }

    @Test
    void rankedCategoriesRespectsLimit() {
        Map<Category, Integer> attended = new EnumMap<>(Category.class);
        attended.put(Category.MUSIC, 1);
        attended.put(Category.SPORTS, 1);
        attended.put(Category.COMEDY, 1);

        InterestProfile p = new InterestProfile(Set.of(), attended, Map.of(), null, null, null);
        assertEquals(2, p.rankedCategories(2).size());
    }

    @Test
    void rankedCategoriesAccumulatesAcrossSources() {
        // MUSIC: attended 1 (×3=3) + liked 1 (×2=2) = 5; SPORTS: stated (×1=1)
        Map<Category, Integer> attended = new EnumMap<>(Category.class);
        attended.put(Category.MUSIC, 1);
        Map<Category, Integer> liked = new EnumMap<>(Category.class);
        liked.put(Category.MUSIC, 1);

        InterestProfile p = new InterestProfile(Set.of(Category.SPORTS), attended, liked, null, null, null);
        List<Category> ranked = p.rankedCategories(2);

        assertEquals(Category.MUSIC, ranked.get(0));
        assertEquals(Category.SPORTS, ranked.get(1));
    }
}
