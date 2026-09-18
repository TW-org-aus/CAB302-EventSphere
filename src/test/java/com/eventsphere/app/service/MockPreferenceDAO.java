package com.eventsphere.app.service;

import com.eventsphere.app.dao.IPreferenceDAO;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Preference;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

// In-memory IPreferenceDAO so UserService can be tested without a database.
class MockPreferenceDAO implements IPreferenceDAO {

    // Recorded calls, for assertions.
    boolean upsertAddressCalled;
    Integer lastCategoriesUserId;
    Set<Category> lastCategories;

    @Override
    public Preference findByUser(int userId) {
        return new Preference(userId, null, new LinkedHashSet<>());
    }

    @Override
    public void upsertAddress(int userId, String address) {
        // The sign-up flow no longer collects an address string; this should never fire.
        upsertAddressCalled = true;
    }

    @Override
    public void replaceCategories(int userId, Collection<Category> categories) {
        lastCategoriesUserId = userId;
        lastCategories = new LinkedHashSet<>(categories);
    }
}
