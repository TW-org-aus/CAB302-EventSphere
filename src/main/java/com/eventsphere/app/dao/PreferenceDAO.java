package com.eventsphere.app.dao;

import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Preference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PreferenceDAO implements IPreferenceDAO {

    private final Connection connection;

    public PreferenceDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Preference findByUser(int userId) {
        String addressSql = "SELECT Address, Bio FROM Preferences WHERE UserID = ?";
        String categoriesSql = "SELECT Category FROM PreferenceCategories WHERE UserID = ?";
        try {
            String address = null;
            String  bio = null;
            boolean hasRow = false;
            try (PreparedStatement ps = connection.prepareStatement(addressSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        hasRow = true;
                        address = rs.getString("Address");
                        bio = rs.getString("Bio");
                    }
                }
            }
            if (!hasRow) {
                return new Preference(userId, null, null, List.of());
            }
            List<Category> categories = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement(categoriesSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        categories.add(Category.fromDbValue(rs.getString("Category")));
                    }
                }
            }
            return new Preference(userId, address, bio, categories);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find preferences for user: " + userId, e);
        }
    }

    @Override
    public void upsertAddress(int userId, String address) {
        String sql = "INSERT INTO Preferences (UserID, Address) VALUES (?, ?) " +
                "ON CONFLICT(UserID) DO UPDATE SET Address = excluded.Address";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, address);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to upsert address for user: " + userId, e);
        }
    }

    @Override
    public void upsertBio(int userId, String bio) {
        String sql = "INSERT INTO Preferences (UserID, Bio) VALUES (?, ?) " +
                "ON CONFLICT(UserID) DO UPDATE SET Bio = excluded.Bio";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, bio);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to upsert bio for user: " + userId, e);
        }
    }

    @Override
    // will take up to 5 catagories to replace
    public void replaceCategories(int userId, Collection<Category> categories) {
        boolean originalAutoCommit;
        try {
            originalAutoCommit = connection.getAutoCommit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to read autoCommit state for user: " + userId, e);
        }

        try {
            connection.setAutoCommit(false);

            // The FK needs the parent preferences row to exist first.
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT OR IGNORE INTO Preferences (UserID) VALUES (?)")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM PreferenceCategories WHERE UserID = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO PreferenceCategories (UserID, Category) VALUES (?, ?)")) {
                for (Category category : categories) {
                    ps.setInt(1, userId);
                    ps.setString(2, category.getDbValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                e.addSuppressed(rollbackEx);
            }
            throw new RuntimeException("Failed to replace categories for user: " + userId, e);
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to restore autoCommit state for user: " + userId, e);
            }
        }
    }
}
