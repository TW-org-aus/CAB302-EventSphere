package com.eventsphere.app.dao;

import com.eventsphere.app.model.Source;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.eventsphere.app.dao.DaoHelpers.*;

public class SourceDAO implements ISourceDAO {


    static final String COLUMNS = "SourceID, SiteName, SiteURL";

    private final Connection connection;

    public SourceDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int insert(String siteName, String siteUrl) {
        String sql = "INSERT INTO Source (SiteName, SiteURL) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, siteName);
            ps.setString(2, siteUrl);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Insert into Source did not return a generated key.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert source: " + siteName, e);
        }
    }

    @Override
    public List<Source> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM Source ORDER BY SourceID ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Source> sources = new ArrayList<>();
            while (rs.next()) {
                sources.add(mapRow(rs));
            }
            return sources;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list all sources", e);
        }
    }

    @Override
    public Optional<Source> findBySiteName(String siteName) {
        String sql = "SELECT " + COLUMNS + " FROM Source WHERE SiteName = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, siteName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find source by name: " + siteName, e);
        }
    }

    @Override
    public Optional<Instant> findLastSyncedAt(int sourceId) {
        String sql = "SELECT LastSyncedAt FROM Source WHERE SourceID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, sourceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.ofNullable(parseTimestamp(rs.getString("LastSyncedAt"))) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to read LastSyncedAt for source: " + sourceId, e);
        }
    }

    @Override
    public void updateLastSyncedAt(int sourceId, Instant syncedAt) {
        String sql = "UPDATE Source SET LastSyncedAt = ? WHERE SourceID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, toDbTimestamp(syncedAt));
            ps.setInt(2, sourceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update LastSyncedAt for source: " + sourceId, e);
        }
    }

    // cursor for converting DB types to java types --> Package-private and static so other DAOs can map joined Source rows.
    static Source mapRow(ResultSet rs) throws SQLException {
        return new Source(
                rs.getInt("SourceID"),
                rs.getString("SiteName"),
                rs.getString("SiteURL")
        );
    }
}
