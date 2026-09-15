// EventDAO.java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventDAO {

    private final Connection connection;

    public EventDAO(Connection connection) {
        this.connection = connection;
    }

    public int insert(String title, String description, String category, String location,
                       java.time.LocalDateTime startTime, int sourceId) {
        String sql = "INSERT INTO Events (Title, Description, Category, Location, StartTime, SourceID, " +
                "LikesCount, CommentsCount, CreatedAt) VALUES (?, ?, ?, ?, ?, ?, 0, 0, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, category);
            ps.setString(4, location);
            ps.setTimestamp(5, Timestamp.valueOf(startTime));
            ps.setInt(6, sourceId);
            ps.setTimestamp(7, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Insert into Events did not return a generated key.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert event: " + title, e);
        }
    }

    public Optional<Event> findById(int eventId) {
        String sql = "SELECT * FROM Events WHERE EventID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find event by id: " + eventId, e);
        }
    }

    public List<Event> findAll() {
        String sql = "SELECT * FROM Events ORDER BY StartTime ASC";
        List<Event> events = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                events.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all events", e);
        }
        return events;
    }

    public List<Event> findByCategory(String category) {
        String sql = "SELECT * FROM Events WHERE Category = ? ORDER BY StartTime ASC";
        List<Event> events = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find events by category: " + category, e);
        }
        return events;
    }

    public List<Event> findUpcoming() {
        String sql = "SELECT * FROM Events WHERE StartTime >= ? ORDER BY StartTime ASC";
        List<Event> events = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(java.time.LocalDateTime.now()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find upcoming events", e);
        }
        return events;
    }

    public List<Event> search(String keyword) {
        String sql = "SELECT * FROM Events WHERE Title LIKE ? OR Description LIKE ? ORDER BY StartTime ASC";
        List<Event> events = new ArrayList<>();
        String pattern = "%" + keyword + "%";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search events for: " + keyword, e);
        }
        return events;
    }

    private Event mapRow(ResultSet rs) throws SQLException {
        return new Event(
                rs.getInt("EventID"),
                rs.getString("Title"),
                rs.getString("Description"),
                rs.getString("Category"),
                rs.getString("Location"),
                rs.getTimestamp("StartTime").toLocalDateTime(),
                rs.getInt("SourceID"),
                rs.getInt("LikesCount"),
                rs.getInt("CommentsCount"),
                rs.getTimestamp("CreatedAt").toLocalDateTime()
        );
    }
}