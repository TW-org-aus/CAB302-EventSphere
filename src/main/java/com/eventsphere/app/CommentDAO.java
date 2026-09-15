// CommentDAO.java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Does NOT maintain Events.CommentsCount — a DB trigger increments/decrements
 * that on insert/delete. This DAO only reads and writes Comments rows.
 */
public class CommentDAO {

    private final Connection connection;

    public CommentDAO(Connection connection) {
        this.connection = connection;
    }

    public int insert(int eventId, int userId, String content) {
        String sql = "INSERT INTO Comments (EventID, UserID, Content, CreatedAt) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ps.setString(3, content);
            ps.setTimestamp(4, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Insert into Comments did not return a generated key.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert comment on event: " + eventId, e);
        }
    }

    public List<Comment> findByEvent(int eventId) {
        String sql = "SELECT * FROM Comments WHERE EventID = ? ORDER BY CreatedAt ASC";
        List<Comment> comments = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find comments for event: " + eventId, e);
        }
        return comments;
    }

    public void update(int commentId, String content) {
        String sql = "UPDATE Comments SET Content = ? WHERE CommentID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, content);
            ps.setInt(2, commentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update comment: " + commentId, e);
        }
    }

    public void delete(int commentId) {
        String sql = "DELETE FROM Comments WHERE CommentID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete comment: " + commentId, e);
        }
    }

    private Comment mapRow(ResultSet rs) throws SQLException {
        return new Comment(
                rs.getInt("CommentID"),
                rs.getInt("EventID"),
                rs.getInt("UserID"),
                rs.getString("Content"),
                rs.getTimestamp("CreatedAt").toLocalDateTime()
        );
    }
}