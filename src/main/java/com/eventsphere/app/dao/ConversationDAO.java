package com.eventsphere.app.dao;

import com.eventsphere.app.model.Conversation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.eventsphere.app.dao.DaoHelpers.parseTimestamp;

public class ConversationDAO implements IConversationDAO {


    static final String COLUMNS = "ConversationID, User1ID, User2ID, CreatedAt";

    private final Connection connection;

    public ConversationDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Conversation findOrCreate(int user1, int user2) {
        if (user1 == user2) {
            throw new IllegalArgumentException("Cannot open a conversation with yourself: " + user1);
        }
        // the user with the lower user number is ALWAYS assigned as "user1" and user1 is always ordered before user2 so there are no duplicates
        // professional developers recommended this is how to ensure a conversation only appears once between 2 people and is unique
        int lo = Math.min(user1, user2);
        int hi = Math.max(user1, user2);

        String insertSql = "INSERT OR IGNORE INTO Conversations (User1ID, User2ID) VALUES (?, ?)";
        String selectSql = "SELECT " + COLUMNS + " FROM Conversations WHERE User1ID = ? AND User2ID = ?";
        try {
            try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                ps.setInt(1, lo);
                ps.setInt(2, hi);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
                ps.setInt(1, lo);
                ps.setInt(2, hi);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapRow(rs);
                    }
                    throw new SQLException("Conversation not found after insert for users: " + lo + ", " + hi);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find or create conversation for users: " + lo + ", " + hi, e);
        }
    }

    @Override
    public List<Conversation> findByUser(int userId) {
        String sql = "SELECT c.ConversationID, c.User1ID, c.User2ID, c.CreatedAt " +
                "FROM Conversations c " +
                "JOIN Users other ON other.UserID = CASE WHEN c.User1ID = ? THEN c.User2ID ELSE c.User1ID END " +
                "WHERE (c.User1ID = ? OR c.User2ID = ?) AND other.IsActive = 1 " +
                "ORDER BY COALESCE((SELECT MAX(m.SentAt) FROM Messages m WHERE m.ConversationID = c.ConversationID), c.CreatedAt) DESC, " +
                "c.ConversationID DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Conversation> conversations = new ArrayList<>();
                while (rs.next()) {
                    conversations.add(mapRow(rs));
                }
                return conversations;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find conversations for user: " + userId, e);
        }
    }

    static Conversation mapRow(ResultSet rs) throws SQLException {
        return new Conversation(
                rs.getInt("ConversationID"),
                rs.getInt("User1ID"),
                rs.getInt("User2ID"),
                parseTimestamp(rs.getString("CreatedAt"))
        );
    }
}
