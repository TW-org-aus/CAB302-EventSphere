package com.eventsphere.app.service;

import com.eventsphere.app.dao.IPreferenceDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Preference;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

// In-memory IPreferenceDAO so UserService can be tested without a database.
class MockPreferenceDAO implements IPreferenceDAO {

    // Recorded calls, for assertions.
    boolean upsertAddressCalled;
    Integer lastCategoriesUserId;
    Set<Category> lastCategories;

    private final Map<Integer, Preference> saved = new HashMap<>();

    @Override
    public Preference findByUser(int userId) {
        Preference preference = saved.get(userId);
        return preference != null ? preference : new Preference(userId, null, null, new LinkedHashSet<>());
    }

    @Override
    public void upsertAddress(int userId, String address) {
        // The sign-up flow no longer collects an address string; this should never fire.
        upsertAddressCalled = true;
    }

    @Override
    public void upsertBio(int userId, String bio) {
        saved.computeIfAbsent(userId, id -> findByUser(id)).setBio(bio);
    }

    @Override
    public void replaceCategories(int userId, Collection<Category> categories) {
        lastCategoriesUserId = userId;
        lastCategories = new LinkedHashSet<>(categories);
        saved.computeIfAbsent(userId, id -> findByUser(id)).setCategories(categories);
    }
}
