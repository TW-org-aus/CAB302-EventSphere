package com.eventsphere.app.dao;

import com.eventsphere.app.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reference pattern for all DAOs: concrete class, no interface,
 * Connection injected via constructor. Tests pass a
 * jdbc:sqlite::memory: connection; production passes Database.DBConnect().
 */
public class UserDAO {

    /** Every column of Users, in the order mapRow reads them. */
    private static String COLUMNS =
            "UserID, FirstName, LastName, Email, PasswordHash, " +
            "HomeLat, HomeLong, DateCreated, IsActive, NotifyEnabled";

    private static Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    //Inserts a user and returns the generated UserID. DateCreated, IsActive and NotifyEnabled are left to their schema defaults (today, 1, 1).
    public int insert(String firstName, String lastName, String email, String passwordHash) {
        return insert(firstName, lastName, email, passwordHash, null, null);
    }

    // As above, with an optional home location. Pass nulls is user has no home location.
    public int insert(String firstName, String lastName, String email, String passwordHash,
                      Double homeLat, Double homeLong) {
        String sql = "INSERT INTO Users (FirstName, LastName, Email, PasswordHash, HomeLat, HomeLong) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setString(4, passwordHash);
            setNullableDouble(ps, 5, homeLat);
            setNullableDouble(ps, 6, homeLong);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Insert into Users did not return a generated key.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert user: " + email, e);
        }
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT " + COLUMNS + " FROM Users WHERE Email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by email: " + email, e);
        }
    }

    public Optional<User> findById(int userId) {
        String sql = "SELECT " + COLUMNS + " FROM Users WHERE UserID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id: " + userId, e);
        }
    }

    // Active users only, ordered by surname then first name.
    public List<User> findAllActive() {
        String sql = "SELECT " + COLUMNS + " FROM Users WHERE IsActive = 1 ORDER BY LastName, FirstName";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapRow(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list active users", e);
        }
    }

    //replaces user objects feilds with the feilds in the new user object provided
    public void update(User user) {
        String sql = "UPDATE Users SET FirstName = ?, LastName = ?, Email = ?, PasswordHash = ?, " +
                "HomeLat = ?, HomeLong = ?, IsActive = ?, NotifyEnabled = ? WHERE UserID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            setNullableDouble(ps, 5, user.getHomeLat());
            setNullableDouble(ps, 6, user.getHomeLong());
            ps.setInt(7, user.isActive() ? 1 : 0);
            ps.setInt(8, user.isNotifyEnabled() ? 1 : 0);
            ps.setInt(9, user.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user: " + user.getUserId(), e);
        }
    }

    public void setHomeLocation(int userId, Double homeLat, Double homeLong) {
        String sql = "UPDATE Users SET HomeLat = ?, HomeLong = ? WHERE UserID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setNullableDouble(ps, 1, homeLat);
            setNullableDouble(ps, 2, homeLong);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set home location for user: " + userId, e);
        }
    }

    public void setNotifyEnabled(int userId, boolean enabled) {
        String sql = "UPDATE Users SET NotifyEnabled = ? WHERE UserID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, enabled ? 1 : 0);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set NotifyEnabled for user: " + userId, e);
        }
    }

    // Soft delete. Rows are kept so comments and messages keep their author.
    public void deactivate(int userId) {
        String sql = "UPDATE Users SET IsActive = 0 WHERE UserID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to deactivate user: " + userId, e);
        }
    }


    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("UserID"),
                rs.getString("FirstName"),
                rs.getString("LastName"),
                rs.getString("Email"),
                rs.getString("PasswordHash"),
                getNullableDouble(rs, "HomeLat"),
                getNullableDouble(rs, "HomeLong"),
                parseDate(rs.getString("DateCreated")),
                rs.getInt("IsActive") == 1,
                rs.getInt("NotifyEnabled") == 1
        );
    }


    // user does not HAVE to have a home lat and long it could be null.
    private static void setNullableDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.REAL);
        } else {
            ps.setDouble(index, value);
        }
    }

    private static Double getNullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

   // helper method to parse date since SQLite has no date type. DateCreated is of type "text" written by the schema

    private static LocalDate parseDate(String raw) throws SQLException {
        if (raw == null) {
            return null;
        }
        String datePart = raw.length() > 10 ? raw.substring(0, 10) : raw;
        try {
            return LocalDate.parse(datePart);
        } catch (DateTimeParseException e) {
            throw new SQLException("Unreadable DateCreated value: " + raw, e);
        }
    }
}
