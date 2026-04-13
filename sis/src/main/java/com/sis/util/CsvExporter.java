package com.sis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

public class CsvExporter {

    private static final Logger log = LoggerFactory.getLogger(CsvExporter.class);

    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private CsvExporter() {}

    public static <T> void export(Component parent,
                                  String baseName,
                                  String[] headers,
                                  List<T> rows,
                                  Function<T, String[]> mapper) {

        // Suggest a timestamped file name so successive exports don't overwrite each other
        String suggested = baseName + "_" + LocalDateTime.now().format(STAMP) + ".csv";

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export to CSV");
        chooser.setSelectedFile(new File(suggested));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "CSV files (*.csv)", "csv"));

        int choice = chooser.showSaveDialog(parent);
        if (choice != JFileChooser.APPROVE_OPTION) return;

        File target = chooser.getSelectedFile();
        // Ensure .csv extension
        if (!target.getName().toLowerCase().endsWith(".csv")) {
            target = new File(target.getAbsolutePath() + ".csv");
        }
        final File out = target;

        // Run the actual write on a worker thread to avoid blocking the EDT
        final List<T> snapshot = rows;   // already a fresh list from the service
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                writeCsv(out, headers, snapshot, mapper);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    log.info("CSV export complete: {}", out.getAbsolutePath());
                    JOptionPane.showMessageDialog(parent,
                            "Exported " + snapshot.size() + " records to:\n" + out.getAbsolutePath(),
                            "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    log.error("CSV export failed: {}", out.getAbsolutePath(), ex);
                    JOptionPane.showMessageDialog(parent,
                            "Export failed: " + ex.getMessage(),
                            "Export Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ---------------------------------------------------------------- internals

    private static <T> void writeCsv(File out, String[] headers,
                                     List<T> rows, Function<T, String[]> mapper)
            throws IOException {
        try (BufferedWriter w = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8))) {
            // UTF-8 BOM — makes Excel open the file correctly without prompts
            w.write('\uFEFF');
            w.write(csvRow(headers));
            for (T row : rows) {
                w.write(csvRow(mapper.apply(row)));
            }
        }
    }

    private static String csvRow(String[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(',');
            String v = values[i] != null ? values[i] : "";
            if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
                sb.append('"').append(v.replace("\"", "\"\"")).append('"');
            } else {
                sb.append(v);
            }
        }
        sb.append("\r\n");
        return sb.toString();
    }
}
