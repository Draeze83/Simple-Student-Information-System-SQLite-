package com.sis.db;

import com.sis.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);

    private static DatabaseManager instance;
    private ConnectionPool pool;

    private DatabaseManager() {}

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    // ---------------------------------------------------------------- public API
    public Connection getConnection() throws SQLException {
        if (pool == null)
            throw new SQLException("Database not initialised — call initialize() first.");
        return pool.borrow();
    }

    public void initialize() throws SQLException {
        // Bootstrap connection: schema creation before pool exists
        new SchemaInitializer(this).createSchema();
        // Now start the pool so DAOs (used by seeder) can call getConnection()
        pool = new ConnectionPool(this);
        new DatabaseSeeder(this).seedIfEmpty();
        log.info("Database initialised. URL: {}", AppConfig.getInstance().getDbUrl());
    }

    public void shutdown() {
        if (pool != null) pool.shutdown();
    }

    Connection createRawConnection() throws SQLException {
        try { Class.forName("org.sqlite.JDBC"); }
        catch (ClassNotFoundException e) {
            log.error("SQLite JDBC driver not found on classpath", e);
            throw new SQLException("SQLite JDBC driver not found", e);
        }
        String url = AppConfig.getInstance().getDbUrl();
        Connection conn = DriverManager.getConnection(url);
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute("PRAGMA journal_mode = WAL");
            st.execute("PRAGMA synchronous = NORMAL");
            st.execute("PRAGMA cache_size = -32000");
            st.execute("PRAGMA temp_store = MEMORY");
        }
        return conn;
    }
}
