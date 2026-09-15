package com.eventsphere.app.dao;

import com.eventsphere.app.model.Comment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.eventsphere.app.dao.DaoHelpers.*;

public class CommentDAO implements ICommentDAO {


    static final String COLUMNS =
            "CommentID, UserID, EventsID, CreatedAt, Content, UpdatedAt, ReplyToCommentID";

    private final Connection connection;

    public CommentDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int insert(int userId, int eventId, String content, Integer replyToCommentId) {
        String sql = "INSERT INTO Comments (UserID, EventsID, Content, ReplyToCommentID) " +
                "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            ps.setString(3, content);
            setNullableInteger(ps, 4, replyToCommentId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Insert into Comments did not return a generated key.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert comment for event: " + eventId, e);
        }
    }

    @Override
    public List<Comment> findByEvent(int eventId) {
        String sql = "SELECT " + COLUMNS + " FROM Comments WHERE EventsID = ? " +
                "ORDER BY CreatedAt ASC, CommentID ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Comment> comments = new ArrayList<>();
                while (rs.next()) {
                    comments.add(mapRow(rs));
                }
                return comments;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find comments for event: " + eventId, e);
        }
    }

    @Override
    public void update(Comment comment) {
        String sql = "UPDATE Comments SET Content = ?, UpdatedAt = datetime('now') WHERE CommentID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, comment.getContent());
            ps.setInt(2, comment.getCommentId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update comment: " + comment.getCommentId(), e);
        }
    }

    @Override
    public void delete(int commentId) {
        String sql = "DELETE FROM Comments WHERE CommentID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete comment: " + commentId, e);
        }
    }

    static Comment mapRow(ResultSet rs) throws SQLException {
        return new Comment(
                rs.getInt("CommentID"),
                rs.getInt("UserID"),
                rs.getInt("EventsID"),
                rs.getString("Content"),
                parseTimestamp(rs.getString("CreatedAt")),
                parseTimestamp(rs.getString("UpdatedAt")),
                getNullableInteger(rs, "ReplyToCommentID")
        );
    }
}
