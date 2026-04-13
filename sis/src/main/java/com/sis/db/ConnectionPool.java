package com.sis.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

class ConnectionPool {

    private static final Logger log = LoggerFactory.getLogger(ConnectionPool.class);

    private static final int POOL_SIZE       = 3;
    private static final int TIMEOUT_SECONDS = 5;

    private final DatabaseManager         db;
    private final BlockingQueue<Connection> available;
    private final List<Connection>          all;
    private volatile boolean                closed;

    ConnectionPool(DatabaseManager db) throws SQLException {
        this.db        = db;
        this.available = new ArrayBlockingQueue<>(POOL_SIZE);
        this.all       = new ArrayList<>(POOL_SIZE);
        this.closed    = false;
        initPool();
    }

    // ---------------------------------------------------------------- public API
    Connection borrow() throws SQLException {
        if (closed) {
            throw new SQLException("Connection pool has been shut down.");
        }
        try {
            Connection conn = available.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (conn == null)
                throw new SQLException("Connection pool exhausted: no connection available after "
                        + TIMEOUT_SECONDS + "s.");

            // Validate the connection; replace it if stale (e.g. after a sleep)
            if (!isValid(conn)) {
                log.warn("Stale connection detected — replacing.");
                synchronized (all) { all.remove(conn); }
                closeQuietly(conn);
                conn = db.createRawConnection();
                synchronized (all) { all.add(conn); }
                if (!isValid(conn)) {
                    closeQuietly(conn);
                    throw new SQLException("Failed to obtain a valid pooled connection.");
                }
            }
            return new PooledConnection(conn, available);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for a pooled connection.", e);
        }
    }

    void shutdown() {
        closed = true;
        for (Connection c : all) closeQuietly(c);
        all.clear();
        available.clear();
        log.info("Connection pool shut down ({} connections closed).", POOL_SIZE);
    }

    private void initPool() throws SQLException {
        for (int i = 0; i < POOL_SIZE; i++) {
            Connection c = db.createRawConnection();
            all.add(c);
            available.add(c);
        }
        log.info("Connection pool initialised ({} connections).", POOL_SIZE);
    }

    private static boolean isValid(Connection c) {
        try { return c != null && !c.isClosed() && c.isValid(1); }
        catch (SQLException e) { return false; }
    }

    private static void closeQuietly(Connection c) {
        try { if (c != null) c.close(); } catch (SQLException ignored) {}
    }
}
