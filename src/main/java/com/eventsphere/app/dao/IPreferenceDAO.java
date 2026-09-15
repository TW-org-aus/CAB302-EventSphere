package com.eventsphere.app.dao;

import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Preference;

import java.util.Collection;

public interface IPreferenceDAO {

    // Never returns null: returns no city, no categories when none is saved.
    Preference findByUser(int userId);

    // city may be null.
    void upsertCity(int userId, String city);

    // Replaces the whole set with new ones and creates the Preferences row if missing.(even if there are more old than new the old ones will still be gone :(
    void replaceCategories(int userId, Collection<Category> categories);
}
