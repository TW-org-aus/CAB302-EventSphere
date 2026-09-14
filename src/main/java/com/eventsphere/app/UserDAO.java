import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

/**
 * Reference pattern for all DAOs: concrete class, no interface,
 * Connection injected via constructor. Tests pass a
 * jdbc:sqlite::memory: connection; production passes Database.DBConnect().
 */
public class UserDAO {

    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public int insert(String email, String passwordHash, String username) {
        String sql = "INSERT INTO Users (Email, PasswordHash, Username, NotifyEnabled, Active, CreatedAt) " +
                "VALUES (?, ?, ?, 1, 1, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, email);
            ps.setString(2, passwordHash);
            ps.setString(3, username);
            ps.setTimestamp(4, Timestamp.valueOf(java.time.LocalDateTime.now()));
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
        String sql = "SELECT * FROM Users WHERE Email = ?";
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
        String sql = "SELECT * FROM Users WHERE UserID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id: " + userId, e);
        }
    }

    public void update(User user) {
        String sql = "UPDATE Users SET Email = ?, PasswordHash = ?, Username = ? WHERE UserID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getUsername());
            ps.setInt(4, user.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user: " + user.getUserId(), e);
        }
    }

    public void setNotifyEnabled(int userId, boolean enabled) {
        String sql = "UPDATE Users SET NotifyEnabled = ? WHERE UserID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, enabled);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set notifyEnabled for user: " + userId, e);
        }
    }

    public void deactivate(int userId) {
        String sql = "UPDATE Users SET Active = 0 WHERE UserID = ?";
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
                rs.getString("Email"),
                rs.getString("PasswordHash"),
                rs.getString("Username"),
                rs.getBoolean("NotifyEnabled"),
                rs.getBoolean("Active"),
                rs.getTimestamp("CreatedAt").toLocalDateTime()
        );
    }
}