package com.eventsphere.app.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

// Shared JDBC helpers for the DAO classes.
 class DaoHelpers {

    // sqlite has a weird date time so this formates it for java classes to use
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DaoHelpers() { }

    static String toDbTimestamp(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant.truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC).format(TIMESTAMP);
    }

    static Instant parseTimestamp(String raw) throws SQLException {
        if (raw == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(raw, TIMESTAMP).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new SQLException("Unreadable timestamp value: " + raw, e);
        }
    }

    // SQLite has no date type; DATE columns come back as text.
    static LocalDate parseDate(String raw) throws SQLException {
        if (raw == null) {
            return null;
        }
        String datePart = raw.length() > 10 ? raw.substring(0, 10) : raw;
        try {
            return LocalDate.parse(datePart);
        } catch (DateTimeParseException e) {
            throw new SQLException("Unreadable date value: " + raw, e);
        }
    }

    static void setNullableTimestamp(PreparedStatement ps, int index, Instant value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, toDbTimestamp(value));
        }
    }

    static void setNullableInteger(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    static Integer getNullableInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    static void setNullableDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.REAL);
        } else {
            ps.setDouble(index, value);
        }
    }

    static Double getNullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }
}
