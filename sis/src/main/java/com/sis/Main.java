package com.sis;

import com.sis.db.DatabaseManager;
import com.sis.ui.MainFrame;
import com.sis.ui.ThemeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // System look-and-feel for native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        ThemeManager.applyUIDefaults();

        // Initialise DB on calling thread — fast-fail on startup errors
        try {
            DatabaseManager.getInstance().initialize();
        } catch (Exception e) {
            log.error("Database initialisation failed", e);
            JOptionPane.showMessageDialog(null,
                    "Database initialization failed:\n" + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Close pooled connections cleanly on JVM exit (window close, Ctrl+C, etc.)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown hook fired — closing connection pool.");
            DatabaseManager.getInstance().shutdown();
        }, "db-shutdown"));

        // Launch GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
