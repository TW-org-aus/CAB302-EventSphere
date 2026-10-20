package com.eventsphere.app.ai;

import java.util.List;

/**
 * Keeps the last ranking in memory for the logged-in user, so reopening the browse
 * screen or changing a filter does not spend another AI call.
 * Router owns one instance for the life of the app.
 */
public class RecommendationCache {

    private Integer userId;
    private InterestProfile profile;
    private List<Recommendation> recommendations;

    // Cached ranking for this user and profile, null when there is nothing to reuse
    public synchronized List<Recommendation> get(int userId, InterestProfile profile) {
        boolean hit = this.userId != null
                && this.userId == userId
                && this.profile.equals(profile);
        return hit ? recommendations : null;
    }

    // Written from the ranking thread and read on the FX thread
    public synchronized void put(int userId, InterestProfile profile,
                                 List<Recommendation> recommendations) {
        this.userId = userId;
        this.profile = profile;
        this.recommendations = recommendations;
    }

    // Drops the entry so a user's ranking is not held in memory past their session
    public synchronized void clear() {
        userId = null;
        profile = null;
        recommendations = null;
    }
}