package com.sis.dao;

import com.sis.db.DatabaseManager;
import com.sis.model.College;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CollegeDAO {

    // Whitelisted sort columns mapped to actual SQL column expressions
    private static final Set<String> SORT_WHITELIST = Set.of("code", "name");

    private final DatabaseManager db = DatabaseManager.getInstance();

    // ----------------------------------------------------------------- CREATE
    public void create(College c) throws SQLException {
        String sql = "INSERT INTO college(code,name) VALUES(?,?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCode().trim().toUpperCase());
            ps.setString(2, c.getName().trim());
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ READ
    public College read(String code) throws SQLException {
        String sql = "SELECT code,name FROM college WHERE code=?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- UPDATE
    /** Only the name is updatable (code is PK; change via delete+recreate). */
    public void update(String originalCode, College updated) throws SQLException {
        String sql = "UPDATE college SET code=?, name=? WHERE code=?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, updated.getCode().trim().toUpperCase());
            ps.setString(2, updated.getName().trim());
            ps.setString(3, originalCode);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("College not found for update: " + originalCode);
        }
    }

    // ---------------------------------------------------------------- DELETE
    public void delete(String code) throws SQLException {
        String sql = "DELETE FROM college WHERE code=?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("College not found for delete: " + code);
        }
    }

    // ------------------------------------------------------------------ LIST
    public List<College> list(int page, int pageSize,
                              String sortCol, boolean ascending,
                              String search) throws SQLException {
        String col = SORT_WHITELIST.contains(sortCol) ? sortCol : "code";
        String dir = ascending ? "ASC" : "DESC";
        String like = "%" + search.trim() + "%";

        String sql = "SELECT code,name FROM college" +
                     " WHERE code LIKE ? OR name LIKE ?" +
                     " ORDER BY " + col + " " + dir +
                     " LIMIT ? OFFSET ?";

        List<College> result = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setInt(3, pageSize);
            ps.setInt(4, page * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public int count(String search) throws SQLException {
        String like = "%" + search.trim() + "%";
        String sql = "SELECT COUNT(*) FROM college WHERE code LIKE ? OR name LIKE ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<College> findAll() throws SQLException {
        String sql = "SELECT code,name FROM college ORDER BY name";
        List<College> result = new ArrayList<>();
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) result.add(map(rs));
        }
        return result;
    }

    private College map(ResultSet rs) throws SQLException {
        return new College(rs.getString("code"), rs.getString("name"));
    }
}
