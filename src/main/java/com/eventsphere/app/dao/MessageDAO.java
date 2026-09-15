package com.eventsphere.app.dao;

import com.eventsphere.app.model.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.eventsphere.app.dao.DaoHelpers.parseTimestamp;

public class MessageDAO implements IMessageDAO {

    // will be used in message server or will be used in hosted db if sockets get scrapped
    static final String COLUMNS = "MessageID, ConversationID, SenderID, Content, SentAt, ReadAt";

    private final Connection connection;

    public MessageDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int insert(int conversationId, int senderId, String content) {
        String sql = "INSERT INTO Messages (ConversationID, SenderID, Content) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, conversationId);
            ps.setInt(2, senderId);
            ps.setString(3, content);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Insert into Messages did not return a generated key.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert message for conversation: " + conversationId, e);
        }
    }

    @Override
    public List<Message> findByConversation(int conversationId) {
        String sql = "SELECT " + COLUMNS + " FROM Messages WHERE ConversationID = ? " +
                "ORDER BY SentAt ASC, MessageID ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Message> messages = new ArrayList<>();
                while (rs.next()) {
                    messages.add(mapRow(rs));
                }
                return messages;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find messages for conversation: " + conversationId, e);
        }
    }

    @Override
    public void markRead(int conversationId, int readerId) {
        String sql = "UPDATE Messages SET ReadAt = datetime('now') " +
                "WHERE ConversationID = ? AND SenderID <> ? AND ReadAt IS NULL";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            ps.setInt(2, readerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark messages read for conversation: " + conversationId, e);
        }
    }

    static Message mapRow(ResultSet rs) throws SQLException {
        return new Message(
                rs.getInt("MessageID"),
                rs.getInt("ConversationID"),
                rs.getInt("SenderID"),
                rs.getString("Content"),
                parseTimestamp(rs.getString("SentAt")),
                parseTimestamp(rs.getString("ReadAt"))
        );
    }
}
