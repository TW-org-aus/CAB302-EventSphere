package com.eventsphere.app.dao;

import com.eventsphere.app.model.Category;
import com.eventsphere.app.model.Event;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.eventsphere.app.dao.DaoHelpers.*;

public class EventDAO implements IEventDAO {


    static final String COLUMNS =
            "EventID, Title, Description, Category, StartTime, EndTime, VenueName, Address, " +
            "EventLat, EventLng, ImageURL, TicketURL, CreatedAt, HasOccured, SourceID, TicketmasterID, " +
            "LikesCount, CommentsCount";

    private static final String INSERT_SQL =
            "INSERT INTO Events (Title, Description, Category, StartTime, EndTime, VenueName, " +
            "Address, EventLat, EventLng, ImageURL, TicketURL, SourceID, TicketmasterID) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final Connection connection;

    public EventDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int insert(Event event) {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
            bindInsertColumns(ps, event);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Insert into Events did not return a generated key.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert event: " + event.getTitle(), e);
        }
    }

    @Override
    public void upsertByTicketmasterId(Event event) {
        if (event.getTicketmasterId() == null) {
            throw new IllegalArgumentException("Event has no Ticketmaster id: " + event.getTitle());
        }
        // DO UPDATE keeps the existing row and EventID, so Likes, Comments and Going stay attached.
        // INSERT OR REPLACE would delete the row first and cascade those away.
        String sql = INSERT_SQL + " ON CONFLICT (TicketmasterID) DO UPDATE SET " +
                "Title = excluded.Title, Description = excluded.Description, Category = excluded.Category, " +
                "StartTime = excluded.StartTime, EndTime = excluded.EndTime, VenueName = excluded.VenueName, " +
                "Address = excluded.Address, EventLat = excluded.EventLat, EventLng = excluded.EventLng, " +
                "ImageURL = excluded.ImageURL, TicketURL = excluded.TicketURL";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindInsertColumns(ps, event);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to upsert event: " + event.getTitle(), e);
        }
    }

    // Binds the 13 placeholders of INSERT_SQL, in column order.
    private static void bindInsertColumns(PreparedStatement ps, Event event) throws SQLException {
        ps.setString(1, event.getTitle());
        ps.setString(2, event.getDescription());
        if (event.getCategory() == null) {
            ps.setNull(3, Types.VARCHAR);
        } else {
            ps.setString(3, event.getCategory().getDbValue());
        }
        ps.setString(4, toDbTimestamp(event.getStartTime()));
        setNullableTimestamp(ps, 5, event.getEndTime());
        ps.setString(6, event.getVenueName());
        ps.setString(7, event.getAddress());
        setNullableDouble(ps, 8, event.getLat());
        setNullableDouble(ps, 9, event.getLng());
        ps.setString(10, event.getImageUrl());
        ps.setString(11, event.getTicketUrl());
        ps.setInt(12, event.getSourceId());
        ps.setString(13, event.getTicketmasterId());
    }

    @Override
    public Optional<Event> findById(int eventId) {
        String sql = "SELECT " + COLUMNS + " FROM Events WHERE EventID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find event by id: " + eventId, e);
        }
    }

    @Override
    public List<Event> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM Events ORDER BY StartTime ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Event> events = new ArrayList<>();
            while (rs.next()) {
                events.add(mapRow(rs));
            }
            return events;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list all events", e);
        }
    }

    @Override
    public List<Event> findByCategory(Category category) {
        String sql = "SELECT " + COLUMNS + " FROM Events WHERE Category = ? ORDER BY StartTime ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, category.getDbValue());
            try (ResultSet rs = ps.executeQuery()) {
                List<Event> events = new ArrayList<>();
                while (rs.next()) {
                    events.add(mapRow(rs));
                }
                return events;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find events by category: " + category, e);
        }
    }

    @Override
    public List<Event> findUpcoming() {
        String sql = "SELECT " + COLUMNS + " FROM Events WHERE StartTime >= datetime('now') ORDER BY StartTime ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Event> events = new ArrayList<>();
            while (rs.next()) {
                events.add(mapRow(rs));
            }
            return events;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find upcoming events", e);
        }
    }

    @Override
    public List<Event> search(String text, Category category, Instant from, Instant to) {
        List<String> clauses = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        if (text != null) {
            clauses.add("(Title LIKE ? OR Description LIKE ? OR VenueName LIKE ? OR Address LIKE ?)");
            String pattern = "%" + text + "%";
            values.add(pattern);
            values.add(pattern);
            values.add(pattern);
            values.add(pattern);
        }
        if (category != null) {
            clauses.add("Category = ?");
            values.add(category.getDbValue());
        }
        if (from != null) {
            clauses.add("StartTime >= ?");
            values.add(toDbTimestamp(from));
        }
        if (to != null) {
            clauses.add("StartTime <= ?");
            values.add(toDbTimestamp(to));
        }

        StringBuilder sql = new StringBuilder("SELECT ").append(COLUMNS).append(" FROM Events");
        if (!clauses.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", clauses));
        }
        sql.append(" ORDER BY StartTime ASC");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < values.size(); i++) {
                ps.setString(i + 1, (String) values.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Event> events = new ArrayList<>();
                while (rs.next()) {
                    events.add(mapRow(rs));
                }
                return events;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search events: " + text, e);
        }
    }


    static Event mapRow(ResultSet rs) throws SQLException {
        String categoryValue = rs.getString("Category");
        Category category = categoryValue == null ? null : Category.fromDbValue(categoryValue);
        return new Event(
                rs.getInt("EventID"),
                rs.getString("Title"),
                rs.getString("Description"),
                category,
                parseTimestamp(rs.getString("StartTime")),
                parseTimestamp(rs.getString("EndTime")),
                rs.getString("VenueName"),
                rs.getString("Address"),
                getNullableDouble(rs, "EventLat"),
                getNullableDouble(rs, "EventLng"),
                rs.getString("ImageURL"),
                rs.getString("TicketURL"),
                parseTimestamp(rs.getString("CreatedAt")),
                rs.getInt("HasOccured") == 1,
                rs.getInt("SourceID"),
                rs.getString("TicketmasterID"),
                rs.getInt("LikesCount"),
                rs.getInt("CommentsCount")
        );
    }
}
