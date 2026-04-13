package com.sis.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);

    private final DatabaseManager db;

    SchemaInitializer(DatabaseManager db) {
        this.db = db;
    }

    void createSchema() {
        log.info("Creating database schema (idempotent)");
        String[] ddl = {
            // ── colleges ────────────────────────────────────────────────────
            "CREATE TABLE IF NOT EXISTS college (" +
            "  code TEXT PRIMARY KEY COLLATE NOCASE," +
            "  name TEXT NOT NULL UNIQUE" +
            ")",
            // ── programs ─────────────────────────────────────────────────────
            // Uses INTEGER PK because several programs share the same code
            // (e.g., BSCE for Civil / Ceramics / Computer Engineering).
            "CREATE TABLE IF NOT EXISTS program (" +
            "  id           INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  code         TEXT    NOT NULL," +
            "  name         TEXT    NOT NULL UNIQUE," +
            "  college_code TEXT    NOT NULL," +
            "  FOREIGN KEY (college_code) REFERENCES college(code)" +
            "    ON UPDATE CASCADE ON DELETE RESTRICT" +
            ")",
            // ── students ─────────────────────────────────────────────────────
            "CREATE TABLE IF NOT EXISTS student (" +
            "  id         TEXT    PRIMARY KEY COLLATE NOCASE," +  // YYYY-NNNN
            "  firstname  TEXT    NOT NULL," +
            "  lastname   TEXT    NOT NULL," +
            "  program_id INTEGER NOT NULL," +
            "  year       INTEGER NOT NULL CHECK(year BETWEEN 1 AND 5)," +
            "  gender     TEXT    NOT NULL CHECK(gender IN ('Male','Female','Other'))," +
            "  FOREIGN KEY (program_id) REFERENCES program(id)" +
            "    ON UPDATE CASCADE ON DELETE RESTRICT" +
            ")",
            // ── indexes ──────────────────────────────────────────────────────
            "CREATE INDEX IF NOT EXISTS idx_student_program  ON student(program_id)",
            "CREATE INDEX IF NOT EXISTS idx_student_lastname ON student(lastname)",
            "CREATE INDEX IF NOT EXISTS idx_student_fname   ON student(firstname)",
            "CREATE INDEX IF NOT EXISTS idx_program_college ON program(college_code)",
            "CREATE INDEX IF NOT EXISTS idx_program_code    ON program(code)"
        };
        try (Connection c = db.createRawConnection(); Statement st = c.createStatement()) {
            for (String sql : ddl) st.execute(sql);
            log.info("Database schema created successfully");
        } catch (SQLException e) {
            log.error("Schema creation failed", e);
            throw new RuntimeException("Schema creation failed: " + e.getMessage(), e);
        }
    }
}