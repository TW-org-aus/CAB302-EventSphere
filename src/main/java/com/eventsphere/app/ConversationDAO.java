// ConversationDAO.java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ConversationDAO {

    private final Connection connection;

    public ConversationDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Finds the existing conversation between two users regardless of
     * which one is stored as UserA/UserB, or creates one if none exists.
     */
    public int findOrCreate(int userA, int userB) {
        String findSql = "SELECT ConversationID FROM Conversations " +
                "WHERE (UserAID = ? AND UserBID = ?) OR (UserAID = ? AND UserBID = ?)";
        try (PreparedStatement ps = connection.prepareStatement(findSql)) {
            ps.setInt(1, userA);
            ps.setInt(2, userB);
            ps.setInt(3, userB);
            ps.setInt(4, userA);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ConversationID");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to look up conversation between " + userA + " and " + userB, e);
        }

        String insertSql = "INSERT INTO Conversations (UserAID, UserBID, CreatedAt) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userA);
            ps.setInt(2, userB);
            ps.setTimestamp(3, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Insert into Conversations did not return a generated key.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create conversation between " + userA + " and " + userB, e);
        }
    }

    public List<Conversation> findByUser(int userId) {
        String sql = "SELECT * FROM Conversations WHERE UserAID = ? OR UserBID = ? ORDER BY CreatedAt DESC";
        List<Conversation> conversations = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    conversations.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find conversations for user: " + userId, e);
        }
        return conversations;
    }

    private Conversation mapRow(ResultSet rs) throws SQLException {
        return new Conversation(
                rs.getInt("ConversationID"),
                rs.getInt("UserAID"),
                rs.getInt("UserBID"),
                rs.getTimestamp("CreatedAt").toLocalDateTime()
        );
    }
}