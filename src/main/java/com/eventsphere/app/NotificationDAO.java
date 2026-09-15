// NotificationDAO.java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    private final Connection connection;

    public NotificationDAO(Connection connection) {
        this.connection = connection;
    }

    public int insert(int userId, String category, String content) {
        String sql = "INSERT INTO Notifications (UserID, Category, Content, CreatedAt, Read) " +
                "VALUES (?, ?, ?, ?, 0)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, category);
            ps.setString(3, content);
            ps.setTimestamp(4, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Insert into Notifications did not return a generated key.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert notification for user: " + userId, e);
        }
    }

    public List<Notification> findByUser(int userId) {
        String sql = "SELECT * FROM Notifications WHERE UserID = ? ORDER BY CreatedAt DESC";
        List<Notification> notifications = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find notifications for user: " + userId, e);
        }
        return notifications;
    }

    public void markRead(int notificationId) {
        String sql = "UPDATE Notifications SET Read = 1 WHERE NotificationID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark notification read: " + notificationId, e);
        }
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        return new Notification(
                rs.getInt("NotificationID"),
                rs.getInt("UserID"),
                rs.getString("Category"),
                rs.getString("Content"),
                rs.getTimestamp("CreatedAt").toLocalDateTime(),
                rs.getBoolean("Read")
        );
    }
}