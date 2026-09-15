package com.eventsphere.app.dao;

import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GoingDAO implements IGoingDAO {

    private final Connection connection;

    public GoingDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean markGoing(int userId, int eventId) {
        String sql = "INSERT OR IGNORE INTO Going (UserID, EventID) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark going: user " + userId + ", event " + eventId, e);
        }
    }

    @Override
    public boolean unmarkGoing(int userId, int eventId) {
        String sql = "DELETE FROM Going WHERE UserID = ? AND EventID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to unmark going: user " + userId + ", event " + eventId, e);
        }
    }

    @Override
    public boolean isGoing(int userId, int eventId) {
        String sql = "SELECT 1 FROM Going g JOIN Users u ON u.UserID = g.UserID " +
                "WHERE g.UserID = ? AND g.EventID = ? AND u.IsActive = 1 LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check going: user " + userId + ", event " + eventId, e);
        }
    }

    @Override
    public List<Event> findEventsForUser(int userId) {
        String sql = "SELECT e.* FROM Events e JOIN Going g ON g.EventID = e.EventID " +
                "WHERE g.UserID = ? ORDER BY e.StartTime ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Event> events = new ArrayList<>();
                while (rs.next()) {
                    events.add(EventDAO.mapRow(rs));
                }
                return events;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find events for user: " + userId, e);
        }
    }

    @Override
    public List<User> findUsersForEvent(int eventId) {
        String sql = "SELECT u.* FROM Users u JOIN Going g ON g.UserID = u.UserID " +
                "WHERE g.EventID = ? AND u.IsActive = 1 ORDER BY u.LastName, u.FirstName";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(UserDAO.mapRow(rs));
                }
                return users;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find users for event: " + eventId, e);
        }
    }

    @Override
    //for notifications
    public void setMuted(int userId, int eventId, boolean muted) {
        String sql = "UPDATE Going SET MuteNotify = ? WHERE UserID = ? AND EventID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, muted ? 1 : 0);
            ps.setInt(2, userId);
            ps.setInt(3, eventId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set muted: user " + userId + ", event " + eventId, e);
        }
    }

    @Override
    //for notifications
    public boolean isMuted(int userId, int eventId) {
        String sql = "SELECT MuteNotify FROM Going WHERE UserID = ? AND EventID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("MuteNotify") == 1;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check muted: user " + userId + ", event " + eventId, e);
        }
    }
}
