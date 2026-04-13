package com.sis.dao;

import com.sis.db.DatabaseManager;
import com.sis.model.Program;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ProgramDAO {

    private static final Set<String> SORT_WHITELIST =
            Set.of("p.code", "p.name", "c.name", "c.code");

    private final DatabaseManager db = DatabaseManager.getInstance();

    // ----------------------------------------------------------------- CREATE
    public void create(Program p) throws SQLException {
        String sql = "INSERT INTO program(code,name,college_code) VALUES(?,?,?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getCode().trim().toUpperCase());
            ps.setString(2, p.getName().trim());
            ps.setString(3, p.getCollegeCode().trim().toUpperCase());
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ READ
    public Program read(int id) throws SQLException {
        String sql = "SELECT p.id,p.code,p.name,p.college_code,c.name AS cname" +
                     " FROM program p JOIN college c ON p.college_code=c.code" +
                     " WHERE p.id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- UPDATE
    public void update(Program p) throws SQLException {
        String sql = "UPDATE program SET code=?,name=?,college_code=? WHERE id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getCode().trim().toUpperCase());
            ps.setString(2, p.getName().trim());
            ps.setString(3, p.getCollegeCode().trim().toUpperCase());
            ps.setInt(4, p.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Program not found for update: " + p.getId());
        }
    }

    // ---------------------------------------------------------------- DELETE
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM program WHERE id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Program not found for delete: " + id);
        }
    }

    // ------------------------------------------------------------------ LIST
    public List<Program> list(int page, int pageSize,
                              String sortCol, boolean ascending,
                              String search) throws SQLException {
        String col = SORT_WHITELIST.contains(sortCol) ? sortCol : "p.name";
        String dir = ascending ? "ASC" : "DESC";
        String like = "%" + search.trim() + "%";

        String sql = "SELECT p.id,p.code,p.name,p.college_code,c.name AS cname" +
                     " FROM program p JOIN college c ON p.college_code=c.code" +
                     " WHERE p.code LIKE ? OR p.name LIKE ? OR c.name LIKE ?" +
                     " ORDER BY " + col + " " + dir +
                     " LIMIT ? OFFSET ?";

        List<Program> result = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setInt(4, pageSize);
            ps.setInt(5, page * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public int count(String search) throws SQLException {
        String like = "%" + search.trim() + "%";
        String sql = "SELECT COUNT(*) FROM program p" +
                     " JOIN college c ON p.college_code=c.code" +
                     " WHERE p.code LIKE ? OR p.name LIKE ? OR c.name LIKE ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like); ps.setString(2, like); ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<Program> findAll() throws SQLException {
        String sql = "SELECT p.id,p.code,p.name,p.college_code,c.name AS cname" +
                     " FROM program p JOIN college c ON p.college_code=c.code" +
                     " ORDER BY c.name, p.name";
        List<Program> result = new ArrayList<>();
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) result.add(map(rs));
        }
        return result;
    }

    private Program map(ResultSet rs) throws SQLException {
        return new Program(
                rs.getInt("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("college_code"),
                rs.getString("cname"));
    }
}
