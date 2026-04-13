package com.sis.ui;

import com.sis.model.College;
import com.sis.service.CollegeService;
import com.sis.ui.dialog.CollegeDialog;
import com.sis.util.CsvExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class CollegePanel extends JPanel {

    private static final int PAGE_SIZE = 25;
    private static final String[] COLS = {"Code", "College Name"};

    private final CollegeService service = new CollegeService();
    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable  table  = new JTable(model);
    private final JTextField txtSearch = new JTextField(20);

    private int  currentPage = 0;
    private int  totalPages  = 1;
    private String sortCol   = "code";
    private boolean sortAsc  = true;

    private final JLabel lblStatus = new JLabel();
    private final JButton btnFirst = new JButton("<<");
    private final JButton btnPrev  = new JButton("<");
    private final JButton btnNext  = new JButton(">");
    private final JButton btnLast  = new JButton(">>");
    private final JLabel  lblPage  = new JLabel("Page 1 of 1");
    private final JLabel  lblDetail = new JLabel(" ");

    // Toolbar action buttons 
    private final JButton btnAdd    = new JButton("Add College");
    private final JButton btnEdit   = new JButton("Edit");
    private final JButton btnDelete = new JButton("Delete");
    private final JButton btnExport = new JButton("Export CSV");

    private final Timer searchDebounceTimer;
    private SwingWorker<RefreshData, Void> refreshWorker;
    private long refreshToken = 0L;

    private List<College> currentRows = new java.util.ArrayList<>();

    public CollegePanel() {
        setLayout(new BorderLayout(4, 4));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(buildToolbar(),     BorderLayout.NORTH);
        add(buildTablePanel(),  BorderLayout.CENTER);
        add(buildSouthPanel(),  BorderLayout.SOUTH);

        configureTable();
        applyPanelColors();
        searchDebounceTimer = new Timer(300, e -> {
            currentPage = 0;
            refresh();
        });
        searchDebounceTimer.setRepeats(false);
        refresh();
    }

    private void configureTable() {
        table.setFont(table.getFont().deriveFont(12f));
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(false);
        ThemeManager.styleTableHeader(table.getTableHeader());

        table.getColumnModel().getColumn(0).setCellRenderer(ThemeManager.centerRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(ThemeManager.bodyRenderer());

        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                String[] keys = {"code", "name"};
                if (col >= 0 && col < keys.length) {
                    if (sortCol.equals(keys[col])) sortAsc = !sortAsc;
                    else { sortCol = keys[col]; sortAsc = true; }
                    currentPage = 0; refresh();
                }
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateDetail();
        });

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) doEdit();
            }
        });
    }

    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        lblDetail.setFont(lblDetail.getFont().deriveFont(Font.ITALIC, 12f));
        lblDetail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        p.add(lblDetail, BorderLayout.SOUTH);
        return p;
    }

    private void updateDetail() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentRows.size()) { lblDetail.setText(" "); return; }
        College c = currentRows.get(row);
        lblDetail.setText("<html><b>Code:</b> " + htmlEscape(c.getCode()) +
                "&nbsp;&nbsp;&nbsp;<b>College Name:</b> " + htmlEscape(c.getName()) + "</html>");
    }

    private JPanel buildToolbar() {
        JButton btnSearch = new JButton("Search");

        Font f = btnAdd.getFont().deriveFont(12f);
        for (JButton b : new JButton[]{btnAdd, btnEdit, btnDelete, btnSearch, btnExport}) b.setFont(f);

        btnAdd.addActionListener(e    -> doAdd());
        btnEdit.addActionListener(e   -> doEdit());
        btnDelete.addActionListener(e -> doDelete());
        btnExport.addActionListener(e -> doExport());
        btnSearch.addActionListener(e -> { currentPage = 0; refresh(); });
        txtSearch.setFont(f);
        txtSearch.addActionListener(e -> { currentPage = 0; refresh(); });
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { searchDebounceTimer.restart(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { searchDebounceTimer.restart(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        JLabel lbl = new JLabel("Search:");
        lbl.setFont(f);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(lbl); p.add(txtSearch); p.add(btnSearch);
        p.add(Box.createHorizontalStrut(20));
        p.add(btnAdd); p.add(btnEdit); p.add(btnDelete);
        p.add(Box.createHorizontalStrut(10));
        p.add(btnExport);
        return p;
    }

    private JPanel buildSouthPanel() {
        Font f = btnFirst.getFont().deriveFont(12f);
        for (JButton b : new JButton[]{btnFirst,btnPrev,btnNext,btnLast}) b.setFont(f);
        btnFirst.addActionListener(e -> { currentPage = 0;            refresh(); });
        btnPrev .addActionListener(e -> { if (currentPage > 0) currentPage--; refresh(); });
        btnNext .addActionListener(e -> { if (currentPage < totalPages-1) currentPage++; refresh(); });
        btnLast .addActionListener(e -> { currentPage = totalPages-1; refresh(); });
        lblPage.setFont(f); lblStatus.setFont(f);

        JTextField txtJump = new JTextField(4);
        txtJump.setFont(f);
        JButton btnJump = new JButton("Go");
        btnJump.setFont(f);
        btnJump.addActionListener(e -> {
            try {
                int pg = Integer.parseInt(txtJump.getText().trim()) - 1;
                if (pg >= 0 && pg < totalPages) { currentPage = pg; refresh(); }
                else JOptionPane.showMessageDialog(this, "Page out of range (1–" + totalPages + ").");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid page number.");
            }
        });
        JLabel jmpLbl = new JLabel("Jump:");
        jmpLbl.setFont(f);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.add(btnFirst); p.add(btnPrev); p.add(lblPage); p.add(btnNext); p.add(btnLast);
        p.add(Box.createHorizontalStrut(10));
        p.add(jmpLbl); p.add(txtJump); p.add(btnJump);
        p.add(Box.createHorizontalStrut(15)); p.add(lblStatus);
        return p;
    }

    public void refresh() {
        final long myToken = ++refreshToken;
        if (refreshWorker != null && !refreshWorker.isDone()) refreshWorker.cancel(true);
        setLoadingState(true);
        String search = txtSearch.getText();

        // Capture selected code before clearing the table
        final String selectedCode = (table.getSelectedRow() >= 0 && !currentRows.isEmpty())
                ? currentRows.get(table.getSelectedRow()).getCode()
                : null;

        refreshWorker = new SwingWorker<>() {
            @Override
            protected RefreshData doInBackground() throws Exception {
                int total = service.countColleges(search);
                int pages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
                int page = Math.min(currentPage, pages - 1);
                List<College> rows = service.getColleges(page, PAGE_SIZE, sortCol, sortAsc, search);
                return new RefreshData(total, pages, page, rows);
            }

            @Override
            protected void done() {
                if (myToken != refreshToken) return;
                try {
                    RefreshData data = get();
                    currentPage = data.page;
                    totalPages = data.totalPages;
                    model.setRowCount(0);
                    currentRows = data.rows;
                    for (College c : data.rows) {
                        model.addRow(new Object[]{c.getCode(), c.getName()});
                    }
                    lblPage.setText("Page " + (currentPage + 1) + " of " + totalPages);
                    lblStatus.setText("Total: " + data.total + " college(s)");
                    btnFirst.setEnabled(currentPage > 0);
                    btnPrev.setEnabled(currentPage > 0);
                    btnNext.setEnabled(currentPage < totalPages - 1);
                    btnLast.setEnabled(currentPage < totalPages - 1);

                    // Restore previous row selection
                    boolean reselected = false;
                    if (selectedCode != null) {
                        for (int i = 0; i < currentRows.size(); i++) {
                            if (selectedCode.equals(currentRows.get(i).getCode())) {
                                table.setRowSelectionInterval(i, i);
                                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                                reselected = true;
                                break;
                            }
                        }
                    }
                    if (!reselected) lblDetail.setText(" ");

                } catch (Exception ex) {
                    showError("Failed to load colleges: " + unwrapMessage(ex));
                } finally {
                    setLoadingState(false);
                }
            }
        };
        refreshWorker.execute();
    }

    public void applyTheme() {
        ThemeManager.styleTableHeader(table.getTableHeader());
        applyPanelColors();
        table.repaint();
        table.getTableHeader().repaint();
    }

    private void doAdd() {
        CollegeDialog dlg = new CollegeDialog(getFrame(), "Add College", null);
        dlg.setVisible(true);
        if (!dlg.isConfirmed()) return;
        College toCreate = dlg.getCollege();
        setLoadingState(true);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                service.addCollege(toCreate);   // validates + persists
                return null;
            }
            @Override protected void done() {
                setLoadingState(false);
                try { get(); refresh(); }
                catch (Exception ex) { showError(unwrapMessage(ex)); }
            }
        }.execute();
    }

    private void doEdit() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a college to edit."); return; }
        final String code = currentRows.get(row).getCode();
        setLoadingState(true);
        new SwingWorker<College, Void>() {
            @Override protected College doInBackground() throws Exception {
                return service.getCollege(code);
            }
            @Override protected void done() {
                setLoadingState(false);
                try {
                    College existing = get();
                    if (existing == null) { showError("College not found."); return; }
                    CollegeDialog dlg = new CollegeDialog(getFrame(), "Edit College", existing);
                    dlg.setVisible(true);
                    if (!dlg.isConfirmed()) return;
                    College updated = dlg.getCollege();
                    setLoadingState(true);
                    new SwingWorker<Void, Void>() {
                        @Override protected Void doInBackground() throws Exception {
                            service.updateCollege(code, updated);   // validates + persists
                            return null;
                        }
                        @Override protected void done() {
                            setLoadingState(false);
                            try { get(); refresh(); }
                            catch (Exception ex) { showError(unwrapMessage(ex)); }
                        }
                    }.execute();
                } catch (Exception ex) {
                    showError(unwrapMessage(ex));
                }
            }
        }.execute();
    }

    private void doDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a college to delete."); return; }
        College col = currentRows.get(row);
        int opt = JOptionPane.showConfirmDialog(this,
                "Delete college \"" + col.getName() + "\" (" + col.getCode() + ")?\n" +
                "This will fail if programs are still assigned to it.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) return;
        setLoadingState(true);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                service.deleteCollege(col.getCode());
                return null;
            }
            @Override protected void done() {
                setLoadingState(false);
                try { get(); refresh(); }
                catch (Exception ex) { showError(unwrapMessage(ex)); }
            }
        }.execute();
    }

    private Frame  getFrame() { return (Frame) SwingUtilities.getWindowAncestor(this); }
    private void   showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String unwrapMessage(Exception ex) {
        Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;
        return (cause.getMessage() != null) ? cause.getMessage() : "An unexpected error occurred.";
    }

    private static String htmlEscape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void setLoadingState(boolean loading) {
        btnAdd.setEnabled(!loading);
        btnEdit.setEnabled(!loading);
        btnDelete.setEnabled(!loading);
        btnExport.setEnabled(!loading);
        table.setEnabled(!loading);
        if (loading) lblStatus.setText("Loading colleges...");
    }

    private void doExport() {
        String search = txtSearch.getText();
        setLoadingState(true);
        new SwingWorker<List<College>, Void>() {
            @Override
            protected List<College> doInBackground() throws Exception {
                int total = service.countColleges(search);
                return service.getColleges(0, Math.max(total, 1), sortCol, sortAsc, search);
            }
            @Override
            protected void done() {
                setLoadingState(false);
                try {
                    List<College> all = get();
                    CsvExporter.export(
                            getFrame(),
                            "colleges",
                            new String[]{"Code", "College Name"},
                            all,
                            c -> new String[]{c.getCode(), c.getName()});
                } catch (Exception ex) {
                    showError(unwrapMessage(ex));
                }
            }
        }.execute();
    }

    private void applyPanelColors() {
        setBackground(ThemeManager.MENU_BG);
        table.setBackground(ThemeManager.CONTENT_BG);
        table.setForeground(ThemeManager.TEXT_FG);
        table.setSelectionBackground(ThemeManager.TABLE_SELECTION);
        table.getTableHeader().setBackground(ThemeManager.TABLE_HEADER_BG);
        table.getTableHeader().setForeground(ThemeManager.TEXT_FG);
        lblDetail.setOpaque(true);
        lblDetail.setBackground(ThemeManager.CONTENT_BG);
        lblDetail.setForeground(Color.DARK_GRAY);
    }

    private static class RefreshData {
        private final int total;
        private final int totalPages;
        private final int page;
        private final List<College> rows;

        private RefreshData(int total, int totalPages, int page, List<College> rows) {
            this.total = total;
            this.totalPages = totalPages;
            this.page = page;
            this.rows = rows;
        }
    }
}