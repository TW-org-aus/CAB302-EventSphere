// LikeDAO.java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * No model class — Likes is a pure join table (EventID, UserID). Does NOT
 * maintain Events.LikesCount — a DB trigger increments/decrements that on
 * insert/delete. This DAO only reads and writes Likes rows.
 */
public class LikeDAO {

    private final Connection connection;

    public LikeDAO(Connection connection) {
        this.connection = connection;
    }

    public void like(int eventId, int userId) {
        String sql = "INSERT INTO Likes (EventID, UserID, CreatedAt) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ps.setTimestamp(3, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to like event: " + eventId + " by user: " + userId, e);
        }
    }

    public void unlike(int eventId, int userId) {
        String sql = "DELETE FROM Likes WHERE EventID = ? AND UserID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to unlike event: " + eventId + " by user: " + userId, e);
        }
    }

    public boolean isLikedBy(int eventId, int userId) {
        String sql = "SELECT 1 FROM Likes WHERE EventID = ? AND UserID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check like status for event: " + eventId + " by user: " + userId, e);
        }
    }

    public int countForEvent(int eventId) {
        String sql = "SELECT COUNT(*) FROM Likes WHERE EventID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count likes for event: " + eventId, e);
        }
    }
}