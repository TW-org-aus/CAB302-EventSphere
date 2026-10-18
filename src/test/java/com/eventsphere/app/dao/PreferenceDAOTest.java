package com.eventsphere.app.dao;

import com.eventsphere.app.Database.DBController;
import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Preference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PreferenceDAOTest {

    private Connection connection;
    private PreferenceDAO preferences;
    private int userId;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        }
        new DBController(connection);

        userId = new UserDAO(connection).insert("Alice", "Smith", "alice@example.com", "hash", null, null);
        preferences = new PreferenceDAO(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void findByUserReturnsEmptyPreferenceWhenNoRow() {
        Preference p = preferences.findByUser(userId);
        assertNull(p.getBio());
        assertTrue(p.getCategories().isEmpty());
    }

    @Test
    void upsertBioCreatesRowAndStoresBio() {
        preferences.upsertBio(userId, "I love jazz");
        assertEquals("I love jazz", preferences.findByUser(userId).getBio());
    }

    @Test
    void upsertBioUpdateExistingBio() {
        preferences.upsertBio(userId, "First bio");
        preferences.upsertBio(userId, "Updated bio");
        assertEquals("Updated bio", preferences.findByUser(userId).getBio());
    }

    @Test
    void replaceCategoriesStoresCategories() {
        preferences.replaceCategories(userId, List.of(Category.MUSIC, Category.SPORTS));
        Set<Category> cats = preferences.findByUser(userId).getCategories();
        assertTrue(cats.contains(Category.MUSIC));
        assertTrue(cats.contains(Category.SPORTS));
        assertEquals(2, cats.size());
    }

    @Test
    void replaceCategoriesReplacesExistingCategories() {
        preferences.replaceCategories(userId, List.of(Category.MUSIC, Category.SPORTS));
        preferences.replaceCategories(userId, List.of(Category.COMEDY));
        Set<Category> cats = preferences.findByUser(userId).getCategories();
        assertEquals(Set.of(Category.COMEDY), cats);
    }

    @Test
    void replaceCategoriesWithEmptyListClearsCategories() {
        preferences.replaceCategories(userId, List.of(Category.MUSIC));
        preferences.replaceCategories(userId, List.of());
        assertTrue(preferences.findByUser(userId).getCategories().isEmpty());
    }

    @Test
    void fullRoundTripPreservesBioAndCategories() {
        preferences.upsertBio(userId, "Loves live music");
        preferences.replaceCategories(userId, List.of(Category.MUSIC, Category.FESTIVALS));

        Preference p = preferences.findByUser(userId);
        assertEquals("Loves live music", p.getBio());
        assertTrue(p.getCategories().contains(Category.MUSIC));
        assertTrue(p.getCategories().contains(Category.FESTIVALS));
    }
}
