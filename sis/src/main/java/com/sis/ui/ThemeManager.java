package com.sis.ui;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class ThemeManager {

    public static final Color MENU_BG          = new Color(0xEBEDEF);
    public static final Color CONTENT_BG       = Color.WHITE;
    public static final Color TEXT_FG          = Color.BLACK;
    public static final Color BORDER           = new Color(180, 180, 180);
    public static final Color TABLE_SELECTION  = new Color(184, 207, 229);
    public static final Color TABLE_ALT_ROW    = new Color(245, 245, 245);
    public static final Color TABLE_HEADER_BG  = new Color(228, 228, 228);

    private ThemeManager() {}

    public static void applyUIDefaults() {
        UIManager.put("Panel.background", MENU_BG);
        UIManager.put("Table.background", CONTENT_BG);
        UIManager.put("Table.foreground", TEXT_FG);
        UIManager.put("Table.selectionBackground", TABLE_SELECTION);
        UIManager.put("Table.selectionForeground", TEXT_FG);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("TabbedPane.background", CONTENT_BG);
        UIManager.put("TabbedPane.foreground", TEXT_FG);
        UIManager.put("TabbedPane.selected", CONTENT_BG);
        UIManager.put("TabbedPane.contentAreaColor", CONTENT_BG);
    }

    // Bold 14pt center renderer to the table header.
    public static void styleTableHeader(JTableHeader header) {
        header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(CENTER);
                setFont(getFont().deriveFont(Font.BOLD, 14f));
            }
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setFont(getFont().deriveFont(Font.BOLD, 14f));
                setHorizontalAlignment(CENTER);
                setBackground(TABLE_HEADER_BG);
                setForeground(TEXT_FG);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0,0,1,1,
                                BORDER),
                        BorderFactory.createEmptyBorder(4,4,4,4)));
                return this;
            }
        });
    }

    public static javax.swing.table.DefaultTableCellRenderer bodyRenderer() {
        return new javax.swing.table.DefaultTableCellRenderer() {
            { setFont(getFont().deriveFont(12f)); }
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setFont(getFont().deriveFont(12f));
                setBackground(sel ? TABLE_SELECTION : (row % 2 == 0 ? CONTENT_BG : TABLE_ALT_ROW));
                setForeground(TEXT_FG);
                setToolTipText(val != null ? val.toString() : null);
                return this;
            }
        };
    }

    public static javax.swing.table.DefaultTableCellRenderer centerRenderer() {
        return new javax.swing.table.DefaultTableCellRenderer() {
            { setHorizontalAlignment(CENTER); setFont(getFont().deriveFont(12f)); }
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setHorizontalAlignment(CENTER);
                setFont(getFont().deriveFont(12f));
                setBackground(sel ? TABLE_SELECTION : (row % 2 == 0 ? CONTENT_BG : TABLE_ALT_ROW));
                setForeground(TEXT_FG);
                setToolTipText(val != null ? val.toString() : null);
                return this;
            }
        };
    }
}
