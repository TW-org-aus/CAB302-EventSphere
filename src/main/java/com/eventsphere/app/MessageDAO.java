// MessageDAO.java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    private final Connection connection;

    public MessageDAO(Connection connection) {
        this.connection = connection;
    }

    public int insert(int conversationId, int senderId, String content) {
        String sql = "INSERT INTO Messages (ConversationID, SenderID, Content, SentAt, Read) " +
                "VALUES (?, ?, ?, ?, 0)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, conversationId);
            ps.setInt(2, senderId);
            ps.setString(3, content);
            ps.setTimestamp(4, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Insert into Messages did not return a generated key.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert message into conversation: " + conversationId, e);
        }
    }

    public List<Message> findByConversation(int conversationId) {
        String sql = "SELECT * FROM Messages WHERE ConversationID = ? ORDER BY SentAt ASC";
        List<Message> messages = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find messages for conversation: " + conversationId, e);
        }
        return messages;
    }

    public void markRead(int messageId) {
        String sql = "UPDATE Messages SET Read = 1 WHERE MessageID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark message read: " + messageId, e);
        }
    }

    private Message mapRow(ResultSet rs) throws SQLException {
        return new Message(
                rs.getInt("MessageID"),
                rs.getInt("ConversationID"),
                rs.getInt("SenderID"),
                rs.getString("Content"),
                rs.getTimestamp("SentAt").toLocalDateTime(),
                rs.getBoolean("Read")
        );
    }
}