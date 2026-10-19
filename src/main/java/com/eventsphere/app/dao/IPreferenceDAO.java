package com.eventsphere.app.dao;

import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Preference;

import java.util.Collection;

/** DAO for user preferences; always returns a non-null Preference with empty fields when no row exists. */
public interface IPreferenceDAO {

    /** Returns the user's stored preferences; never null — address and bio default to null, categories to empty. */
    Preference findByUser(int userId);

    /** Upserts the home address; null clears it. */
    void upsertAddress(int userId, String address);

    /** Upserts the bio; null clears it. */
    void upsertBio(int userId, String bio);

    /** Replaces the full category set and creates the Preferences row if it is missing. */
    void replaceCategories(int userId, Collection<Category> categories);
}
