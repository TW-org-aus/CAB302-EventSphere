package com.eventsphere.app.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class UserDAO {

    private static final String COLUMNS = "UserID, FirstName, LastName, Email, PasswordHash, HomeLat, HomeLong, DataCreated, IsActive, NotifyEnabled";

    private final Connection connection;
    private final PasswordHasher passwordHasher;

    public UserDAO(Connection connection) {
        this(connection, new PasswordHasher());
    }

    public UserDAO(Connection connection, PasswordHasher passwordHasher) {
        this.connection = Objects.requireNonNull(connection, "connection");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher");
    }

    public User insert(String firstName, String lastName, String email,
                       String plainPassword, Double homeLat, Double homeLong) throws DuplicateEmailException, SQLException {
        textNeeded(firstName, "firstName");
        textNeeded(lastName, "lastName");
        textNeeded(email, "email");
        textNeeded(plainPassword, "password");

        String normalisedEmail = normalisedEmail(email);
        String passwordHash = passwordHasher.hash(plainPassword);

        String sql = "INSERT INTO Users (firstName, lastName, Email, PasswordHash, HomeLat, HomeLong) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, firstName.trim());
            statement.setString(2, lastName.trim());
            statement.setString(3, normalisedEmail);
            statement.setString(4, passwordHash);
            setDoubleToNull(statement, 5, homeLat);
            setDoubleToNull(statement, 6, homeLong);
            statement.executeUpdate();

            int userId;
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Succeeded - No UserID was returned");
                }
                userId = keys.getInt(1);
            }

            return findById(userId).orElseThrow(
                    () -> new SQLException("Insert user " + userId + " could not be read back")
            );
        } catch (SQLException ex) {
            if (isEmailUniqueViolation(ex)) {
                throw new DuplicateEmailException(normalisedEmail, ex);
            }
            throw ex;
        }
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        return findIt("SELECT " + COLUMNS + " FROM Users WHERE Email = ?",
                normalisedEmail(email));
    }

    public Optional<User> findById(int userId) throws SQLException {
        return findIt("SELECT " + COLUMNS + " FROM Users WHERE UserID = ?",
                userId);
    }

    public boolean update(int userId, String firstName, String lastName, String email,
                          double homeLat, double homeLong) throws DuplicateEmailException, SQLException {
        textNeeded(firstName, "firstName");
        textNeeded(lastName, "lastName");
        textNeeded(email, "email");

        String normalisedEmail = normalisedEmail(email);
        String sql = "UPDATE Users SET FirstName = ?, LastName = ?, Email = ?, HomeLat = ?, HomeLong = ? " +
                "WHERE UserID = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, firstName.trim());
            statement.setString(2, lastName.trim());
            statement.setString(3, normalisedEmail);
            setDoubleToNull(statement, 4, homeLat);
            setDoubleToNull(statement, 5, homeLong);
            statement.setInt(6, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            if (isEmailUniqueViolation(ex)) {
                throw new DuplicateEmailException(normalisedEmail, ex);
            }
            throw ex;
        }
    }

    public boolean setNotifyEnabled(int userId, boolean enabled) throws SQLException {
        return executeUpdate("UPDATE Users SET NotifyEnabled = ? WHERE UserID = ?", enabled ? 1 : 0, userId);
    }

    public boolean deactivate(int userId) throws SQLException {
        return executeUpdate("UPDATE Users SET IsActive = 0 WHERE UserID = ?", userId);
    }

    private Optional<User> findIt(String sql, Object parameter) throws SQLException {
        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, parameter);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(rowToObject(rs)) : Optional.empty();
            }
        }
    }

    private boolean executeUpdate(String sql, Object... parameters) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            return statement.executeUpdate() > 0;
        }
    }

    private static User rowToObject(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("UserID"),
                rs.getString("FirstName"),
                rs.getString("LastName"),
                rs.getString("Email"),
                rs.getString("PasswordHasher"),
                allowDoubleToNull(rs, "HomeLat"),
                allowDoubleToNull(rs, "HomeLong"),
                getDate(rs, "DateCreated"),
                rs.getInt("IsActive") == 1,
                rs.getInt("NotifyEnabled") == 1
        );
    }

    private static Double allowDoubleToNull(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDate getDate(ResultSet rs, String column) throws SQLException {
        String raw = rs.getString(column);
        if (raw == null || raw.length() < 10) {
            return null;
        }
        try {
            return LocalDate.parse(raw.substring(0, 10));
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private static void setDoubleToNull(PreparedStatement statement, int index, Double value) throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.REAL);
        } else {
            statement.setDouble(index, value);
        }
    }

    private static String normalisedEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static void textNeeded(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    private static boolean isEmailUniqueViolation(SQLException ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof SQLException sqlEx) {
                int code = sqlEx.getErrorCode();
                String message = sqlEx.getMessage() == null ? "" : sqlEx.getMessage().toLowerCase(Locale.ROOT);
                if ((code == 19 || code == 2067) && message.contains("users.email")) {
                    return true;
                }
            }
        }
        return false;
    }
}
