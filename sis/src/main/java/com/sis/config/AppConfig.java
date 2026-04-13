package com.sis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    private static final String CONFIG_FILE = "config.properties";

    private static final AppConfig INSTANCE = new AppConfig();

    private final Properties props = new Properties();

    private AppConfig() {
        load();
    }

    public static AppConfig getInstance() {
        return INSTANCE;
    }

    public String getDbUrl() {
        String path = System.getProperty("db.path", props.getProperty("db.path", "student_info_system.db"));
        if (path == null || path.isBlank()) {
            path = "student_info_system.db";
        }
        return "jdbc:sqlite:" + path.trim();
    }

    private void load() {
        // Try external override file next to the JAR / in the working directory
        File external = new File(CONFIG_FILE);
        if (external.exists()) {
            try (InputStream in = new FileInputStream(external)) {
                props.load(in);
                log.info("Configuration loaded from external file: {}", external.getAbsolutePath());
                return;
            } catch (IOException e) {
                log.warn("Could not read external config.properties ({}), falling back to classpath.", e.getMessage());
            }
        }

        // 2. Fall back to bundled classpath resource
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                props.load(in);
                log.info("Configuration loaded from classpath resource.");
            } else {
                log.warn("config.properties not found — using built-in defaults.");
            }
        } catch (IOException e) {
            log.error("Failed to load bundled config.properties", e);
        }
    }
}
