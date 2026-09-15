package com.eventsphere.app.dao;

import com.eventsphere.app.model.Source;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    // cursor for converting DB types to java types --> Package-private and static so other DAOs can map joined Source rows.
    static Source mapRow(ResultSet rs) throws SQLException {
        return new Source(
                rs.getInt("SourceID"),
                rs.getString("SiteName"),
                rs.getString("SiteURL")
        );
    }
}
