package com.eventsphere.app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LikeDAO implements ILikeDAO {

    private final Connection connection;

    public LikeDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean like(int userId, int eventId) {
        // OR IGNORE means a duplicate like fires no INSERT, so the likes counter does not count a duplicate like
        String sql = "INSERT OR IGNORE INTO Likes (UserID, EventsID) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to like event: user " + userId + ", event " + eventId, e);
        }
    }

    @Override
    public boolean unlike(int userId, int eventId) {
        String sql = "DELETE FROM Likes WHERE UserID = ? AND EventsID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to unlike event: user " + userId + ", event " + eventId, e);
        }
    }

    @Override
    public boolean isLikedBy(int userId, int eventId) {
        String sql = "SELECT 1 FROM Likes WHERE UserID = ? AND EventsID = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check like: user " + userId + ", event " + eventId, e);
        }
    }

    @Override
    public int countForEvent(int eventId) {
        String sql = "SELECT LikesCount FROM Events WHERE EventID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("LikesCount") : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count likes for event: " + eventId, e);
        }
    }
}
