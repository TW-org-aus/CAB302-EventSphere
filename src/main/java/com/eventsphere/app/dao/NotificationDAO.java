package com.eventsphere.app.dao;

import com.eventsphere.app.model.Notification;
import com.eventsphere.app.model.NotificationType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.eventsphere.app.dao.DaoHelpers.*;

public class NotificationDAO implements INotificationDAO {


    static final String COLUMNS =
            "NotificationID, UserID, Type, RelatedEventID, RelatedCommentID, RelatedConversationID, " +
            "CreatedAt, IsRead";

    private final Connection connection;

    public NotificationDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int insert(int userId, NotificationType type, Integer relatedEventId,
                       Integer relatedCommentId, Integer relatedConversationId) {
        String sql = "INSERT INTO Notifications (UserID, Type, RelatedEventID, RelatedCommentID, RelatedConversationID) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, type.getDbValue());
            setNullableInteger(ps, 3, relatedEventId);
            setNullableInteger(ps, 4, relatedCommentId);
            setNullableInteger(ps, 5, relatedConversationId);
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

    @Override
    public List<Notification> findByUser(int userId) {
        String sql = "SELECT " + COLUMNS + " FROM Notifications WHERE UserID = ? " +
                "ORDER BY CreatedAt DESC, NotificationID DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Notification> notifications = new ArrayList<>();
                while (rs.next()) {
                    notifications.add(mapRow(rs));
                }
                return notifications;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find notifications for user: " + userId, e);
        }
    }

    @Override
    public void markRead(int notificationId) {
        String sql = "UPDATE Notifications SET IsRead = 1 WHERE NotificationID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark notification read: " + notificationId, e);
        }
    }

    static Notification mapRow(ResultSet rs) throws SQLException {
        return new Notification(
                rs.getInt("NotificationID"),
                rs.getInt("UserID"),
                NotificationType.fromDbValue(rs.getString("Type")),
                getNullableInteger(rs, "RelatedEventID"),
                getNullableInteger(rs, "RelatedCommentID"),
                getNullableInteger(rs, "RelatedConversationID"),
                parseTimestamp(rs.getString("CreatedAt")),
                rs.getInt("IsRead") == 1
        );
    }
}
