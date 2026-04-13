package com.sis.service;

import com.sis.db.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Base class for service-layer integration tests.
 *
 * <p>Sets up a fresh file-based SQLite database before each test method and
 * tears it down afterwards so every test starts from a clean slate.
 *
 * <p>The database path is injected via the {@code db.path} system property.
 * {@link com.sis.config.AppConfig#getDbUrl()} re-reads this property on every
 * call, so no AppConfig singleton reset is needed — only the
 * {@link DatabaseManager} singleton needs to be recreated between tests.
 */
abstract class ServiceIntegrationBase {

    private File tmpDbFile;

    @BeforeEach
    void setUpDatabase() throws SQLException, IOException {
        // Point AppConfig at a fresh temp database for this test.
        // AppConfig.getDbUrl() checks System.getProperty("db.path") on every call.
        tmpDbFile = File.createTempFile("sis_test_", ".db");
        tmpDbFile.deleteOnExit();
        System.setProperty("db.path", tmpDbFile.getAbsolutePath());

        // Reset the DatabaseManager singleton so the next getInstance() creates
        // a new manager pointing at the new db.path
        resetDatabaseManager();
        DatabaseManager.getInstance().initialize();
    }

    @AfterEach
    void tearDownDatabase() {
        try { DatabaseManager.getInstance().shutdown(); } catch (Exception ignored) {}
        resetDatabaseManager();
        if (tmpDbFile != null) tmpDbFile.delete();
    }

    /**
     * Resets the {@link DatabaseManager} singleton's static {@code instance} field
     * so each test gets a fresh, correctly-initialised manager.
     */
    private static void resetDatabaseManager() {
        try {
            java.lang.reflect.Field f = DatabaseManager.class.getDeclaredField("instance");
            f.setAccessible(true);
            f.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Could not reset DatabaseManager singleton", e);
        }
    }
}
