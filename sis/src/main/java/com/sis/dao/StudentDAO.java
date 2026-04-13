package com.sis.dao;

import com.sis.db.DatabaseManager;
import com.sis.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StudentDAO {

    // Whitelist maps UI column labels → safe SQL expressions
    private static final Map<String, String> SORT_MAP = Map.of(
            "id",       "s.id",
            "lastname", "s.lastname",
            "firstname","s.firstname",
            "program",  "p.name",
            "year",     "s.year",
            "gender",   "s.gender",
            "college",  "c.name"
    );

    private final DatabaseManager db = DatabaseManager.getInstance();

    // ----------------------------------------------------------------- CREATE
    public void create(Student s) throws SQLException {
        String sql = "INSERT INTO student(id,firstname,lastname,program_id,year,gender)" +
                     " VALUES(?,?,?,?,?,?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getId().trim());
            ps.setString(2, s.getFirstname().trim());
            ps.setString(3, s.getLastname().trim());
            ps.setInt(4, s.getProgramId());
            ps.setInt(5, s.getYear());
            ps.setString(6, s.getGender());
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ READ
    public Student read(String id) throws SQLException {
        String sql = buildSelectBase() + " WHERE s.id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- UPDATE
    public void update(Student s) throws SQLException {
        String sql = "UPDATE student SET firstname=?,lastname=?,program_id=?,year=?,gender=?" +
                     " WHERE id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getFirstname().trim());
            ps.setString(2, s.getLastname().trim());
            ps.setInt(3, s.getProgramId());
            ps.setInt(4, s.getYear());
            ps.setString(5, s.getGender());
            ps.setString(6, s.getId());
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Student not found for update: " + s.getId());
            }
        }
    }

    // ---------------------------------------------------------------- DELETE
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM student WHERE id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            int deleted = ps.executeUpdate();
            if (deleted == 0) {
                throw new SQLException("Student not found for delete: " + id);
            }
        }
    }

    // ------------------------------------------------------------------ LIST
    public List<Student> list(int page, int pageSize,
                              String sortKey, boolean ascending,
                              String search) throws SQLException {
        String col = SORT_MAP.getOrDefault(sortKey, "s.id");
        String dir = ascending ? "ASC" : "DESC";
        String like = "%" + search.trim() + "%";

        String sql = buildSelectBase() +
                     " WHERE s.id LIKE ? OR s.firstname LIKE ? OR s.lastname LIKE ?" +
                     "    OR p.name LIKE ? OR p.code LIKE ? OR c.name LIKE ?" +
                     " ORDER BY " + col + " " + dir +
                     " LIMIT ? OFFSET ?";

        List<Student> result = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 6; i++) ps.setString(i, like);
            ps.setInt(7, pageSize);
            ps.setInt(8, page * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public int count(String search) throws SQLException {
        String like = "%" + search.trim() + "%";
        String sql = "SELECT COUNT(*) FROM student s" +
                     " JOIN program p ON s.program_id=p.id" +
                     " JOIN college c ON p.college_code=c.code" +
                     " WHERE s.id LIKE ? OR s.firstname LIKE ? OR s.lastname LIKE ?" +
                     "    OR p.name LIKE ? OR p.code LIKE ? OR c.name LIKE ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 6; i++) ps.setString(i, like);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // --------------------------------------------------------- ID generation
    public String generateNextId(int enrollYear) throws SQLException {
        // SUBSTR(id,6) picks the NNNN part after "YYYY-"
        String sql = "SELECT MAX(CAST(SUBSTR(id,6) AS INTEGER))" +
                     " FROM student WHERE id LIKE ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, enrollYear + "-%");
            try (ResultSet rs = ps.executeQuery()) {
                int seq = rs.next() ? rs.getInt(1) : 0;
                return String.format("%d-%04d", enrollYear, seq + 1);
            }
        }
    }

    // --------------------------------------------------------------- helpers
    private String buildSelectBase() {
        return "SELECT s.id, s.firstname, s.lastname," +
               "       p.id AS program_id, p.code AS program_code, p.name AS program_name," +
               "       c.code AS college_code, c.name AS college_name," +
               "       s.year, s.gender" +
               " FROM student s" +
               " JOIN program p ON s.program_id = p.id" +
               " JOIN college c ON p.college_code = c.code";
    }

    private Student map(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getString("id"));
        s.setFirstname(rs.getString("firstname"));
        s.setLastname(rs.getString("lastname"));
        s.setProgramId(rs.getInt("program_id"));
        s.setProgramCode(rs.getString("program_code"));
        s.setProgramName(rs.getString("program_name"));
        s.setCollegeCode(rs.getString("college_code"));
        s.setCollegeName(rs.getString("college_name"));
        s.setYear(rs.getInt("year"));
        s.setGender(rs.getString("gender"));
        return s;
    }
}
