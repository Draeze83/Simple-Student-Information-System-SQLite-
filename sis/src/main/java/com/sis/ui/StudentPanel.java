package com.sis.ui;

import com.sis.model.Program;
import com.sis.model.Student;
import com.sis.service.StudentService;
import com.sis.ui.dialog.StudentDialog;
import com.sis.util.CsvExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class StudentPanel extends JPanel {

    private static final int PAGE_SIZE = 25;
    private static final String[] COLS =
            {"Student ID", "Last Name", "First Name", "Course", "Year Level", "Gender"};
    private static final String[] SORT_KEYS =
            {"id", "lastname", "firstname", "program", "year", "gender"};

    private final StudentService studentService = new StudentService();

    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable     table     = new JTable(model);
    private final JTextField txtSearch = new JTextField(22);

    private int     currentPage = 0;
    private int     totalPages  = 1;
    private String  sortKey     = "id";
    private boolean sortAsc     = true;

    private final JLabel  lblStatus = new JLabel();
    private final JButton btnFirst  = new JButton("<<");
    private final JButton btnPrev   = new JButton("<");
    private final JButton btnNext   = new JButton(">");
    private final JButton btnLast   = new JButton(">>");
    private final JLabel  lblPage   = new JLabel();

    // Toolbar action buttons 
    private final JButton btnAdd    = new JButton("Add Student");
    private final JButton btnEdit   = new JButton("Edit");
    private final JButton btnDelete = new JButton("Delete");
    private final JButton btnExport = new JButton("Export CSV");

    // Detail label shown when a row is selected
    private final JLabel lblDetail = new JLabel(" ");

    private List<Student> currentRows = new ArrayList<>();
    private final Timer searchDebounceTimer;
    private SwingWorker<RefreshData, Void> refreshWorker;
    private long refreshToken = 0L;

    public StudentPanel() {
        setLayout(new BorderLayout(4, 4));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(buildToolbar(),         BorderLayout.NORTH);
        add(buildTablePanel(),      BorderLayout.CENTER);
        add(buildSouthPanel(),      BorderLayout.SOUTH);

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
        table.getTableHeader().setResizingAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(false);
        ThemeManager.styleTableHeader(table.getTableHeader());

        // Apply renderers to all columns
        for (int i = 0; i < COLS.length; i++) {
            if (i == 4 || i == 5) { // Year Level, Gender → center
                table.getColumnModel().getColumn(i).setCellRenderer(ThemeManager.centerRenderer());
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(ThemeManager.bodyRenderer());
            }
        }

        // Column-header click → sort
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                if (col >= 0 && col < SORT_KEYS.length) {
                    if (sortKey.equals(SORT_KEYS[col])) sortAsc = !sortAsc;
                    else { sortKey = SORT_KEYS[col]; sortAsc = true; }
                    currentPage = 0; refresh();
                }
            }
        });

        // Row selection → show full detail
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateDetail();
        });

        // Double-click → edit
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
        if (row < 0) { lblDetail.setText(" "); return; }
        StringBuilder sb = new StringBuilder("<html>");
        for (int c = 0; c < COLS.length; c++) {
            Object val = model.getValueAt(row, c);
            sb.append("<b>").append(COLS[c]).append(":</b> ")
              .append(htmlEscape(val != null ? val.toString() : ""))
              .append("&nbsp;&nbsp;&nbsp;");
        }
        sb.append("</html>");
        lblDetail.setText(sb.toString());
    }

    private static String htmlEscape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private JPanel buildToolbar() {
        JButton btnSearch = new JButton("Search");

        Font btnFont = btnAdd.getFont().deriveFont(12f);
        for (JButton b : new JButton[]{btnAdd, btnEdit, btnDelete, btnSearch, btnExport}) b.setFont(btnFont);

        btnAdd.addActionListener(e    -> doAdd());
        btnEdit.addActionListener(e   -> doEdit());
        btnDelete.addActionListener(e -> doDelete());
        btnExport.addActionListener(e -> doExport());
        btnSearch.addActionListener(e -> { currentPage = 0; refresh(); });
        txtSearch.setFont(txtSearch.getFont().deriveFont(12f));
        txtSearch.addActionListener(e -> { currentPage = 0; refresh(); });
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { searchDebounceTimer.restart(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { searchDebounceTimer.restart(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        JLabel lbl = new JLabel("Search (ID / name / course / college):");
        lbl.setFont(lbl.getFont().deriveFont(12f));

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(lbl);
        p.add(txtSearch); p.add(btnSearch);
        p.add(Box.createHorizontalStrut(20));
        p.add(btnAdd); p.add(btnEdit); p.add(btnDelete);
        p.add(Box.createHorizontalStrut(10));
        p.add(btnExport);
        return p;
    }

    private JPanel buildSouthPanel() {
        Font f = btnFirst.getFont().deriveFont(12f);
        for (JButton b : new JButton[]{btnFirst,btnPrev,btnNext,btnLast}) b.setFont(f);
        btnFirst.addActionListener(e -> { currentPage = 0;             refresh(); });
        btnPrev .addActionListener(e -> { if (currentPage > 0) currentPage--; refresh(); });
        btnNext .addActionListener(e -> { if (currentPage < totalPages-1) currentPage++; refresh(); });
        btnLast .addActionListener(e -> { currentPage = totalPages-1;  refresh(); });

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

        lblPage.setFont(f);
        lblStatus.setFont(f);

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
        setLoading(true);
        String search = txtSearch.getText();

        // Capture the selected student ID on the EDT before the worker clears the table.
        // Used after repopulation to reselect the same row.
        final String selectedId = (table.getSelectedRow() >= 0 && !currentRows.isEmpty())
                ? currentRows.get(table.getSelectedRow()).getId()
                : null;

        refreshWorker = new SwingWorker<>() {
            @Override
            protected RefreshData doInBackground() throws Exception {
                int total = studentService.countStudents(search);
                int pages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
                int page = Math.min(currentPage, pages - 1);
                List<Student> rows = studentService.getStudents(page, PAGE_SIZE, sortKey, sortAsc, search);
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
                    for (Student s : data.rows) {
                        model.addRow(new Object[]{
                                s.getId(),
                                s.getLastname(),
                                s.getFirstname(),
                                s.getProgramCode() + " - " + s.getProgramName(),
                                s.getYear(),
                                s.getGender()
                        });
                    }
                    int from = currentPage * PAGE_SIZE + 1;
                    int to = Math.min(from + data.rows.size() - 1, data.total);
                    if (data.total == 0) { from = 0; to = 0; }
                    lblPage.setText("Page " + (currentPage + 1) + " of " + totalPages +
                            "  |  Records " + from + "-" + to + " of " + data.total);
                    lblStatus.setText("Total: " + data.total + " student(s)");
                    btnFirst.setEnabled(currentPage > 0);
                    btnPrev.setEnabled(currentPage > 0);
                    btnNext.setEnabled(currentPage < totalPages - 1);
                    btnLast.setEnabled(currentPage < totalPages - 1);

                    // Restore previous selection if the student is still on this page.
                    // The ListSelectionListener fires automatically and updates lblDetail.
                    boolean reselected = false;
                    if (selectedId != null) {
                        for (int i = 0; i < currentRows.size(); i++) {
                            if (selectedId.equals(currentRows.get(i).getId())) {
                                table.setRowSelectionInterval(i, i);
                                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                                reselected = true;
                                break;
                            }
                        }
                    }
                    if (!reselected) lblDetail.setText(" ");

                } catch (Exception ex) {
                    showError(unwrapMessage(ex));
                } finally {
                    setLoading(false);
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
        // Worker 1: load programs off the EDT so the dialog can be populated
        setLoading(true);
        new SwingWorker<List<Program>, Void>() {
            @Override
            protected List<Program> doInBackground() throws Exception {
                return studentService.getPrograms();
            }
            @Override
            protected void done() {
                setLoading(false);
                try {
                    List<Program> programs = get();
                    StudentDialog dlg = new StudentDialog(getFrame(), "Add Student", null, programs, studentService);
                    dlg.setVisible(true);
                    if (!dlg.isConfirmed()) return;
                    Student s = dlg.getStudent();
                    // Worker 2: persist the new student off the EDT
                    setLoading(true);
                    new SwingWorker<Void, Void>() {
                        @Override protected Void doInBackground() throws Exception {
                            studentService.addStudent(s);
                            return null;
                        }
                        @Override protected void done() {
                            setLoading(false);
                            try { get(); refresh(); }
                            catch (Exception ex) {
                                showError(unwrapMessage(ex));
                            }
                        }
                    }.execute();
                } catch (Exception ex) {
                    showError(unwrapMessage(ex));
                }
            }
        }.execute();
    }

    private void doEdit() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a student to edit."); return; }
        final String sid = getSelectedStudent(row).getId();

        // Worker 1: load existing student + program list off the EDT
        setLoading(true);
        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                Student existing = studentService.getStudent(sid);
                List<Program> progs = studentService.getPrograms();
                return new Object[]{existing, progs};
            }
            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                setLoading(false);
                try {
                    Object[] result   = get();
                    Student  existing = (Student)       result[0];
                    List<Program> progs = (List<Program>) result[1];
                    if (existing == null) { showError("Student not found."); return; }
                    StudentDialog dlg = new StudentDialog(getFrame(), "Edit Student", existing, progs, studentService);
                    dlg.setVisible(true);
                    if (!dlg.isConfirmed()) return;
                    Student updated = dlg.getStudent();
                    // Worker 2: persist the update off the EDT
                    setLoading(true);
                    new SwingWorker<Void, Void>() {
                        @Override protected Void doInBackground() throws Exception {
                            studentService.updateStudent(updated);
                            return null;
                        }
                        @Override protected void done() {
                            setLoading(false);
                            try { get(); refresh(); }
                            catch (Exception ex) {
                                showError(unwrapMessage(ex));
                            }
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
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a student to delete."); return; }
        final String sid  = getSelectedStudent(row).getId();
        final String name = getSelectedStudent(row).getLastname() + ", " + getSelectedStudent(row).getFirstname();
        int opt = JOptionPane.showConfirmDialog(this,
                "Delete student \"" + name + "\" (" + sid + ")?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) return;

        // Worker: delete off the EDT
        setLoading(true);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                studentService.deleteStudent(sid);
                return null;
            }
            @Override protected void done() {
                setLoading(false);
                try { get(); refresh(); }
                catch (Exception ex) {
                    showError(unwrapMessage(ex));
                }
            }
        }.execute();
    }

    private Frame getFrame() { return (Frame) SwingUtilities.getWindowAncestor(this); }
    private void  showError(String m) { JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE); }

    private Student getSelectedStudent(int row) {
        return currentRows.get(row);
    }

    private String unwrapMessage(Exception ex) {
        Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;
        return (cause.getMessage() != null) ? cause.getMessage()
                                            : "An unexpected error occurred.";
    }

    private void setLoading(boolean loading) {
        btnAdd.setEnabled(!loading);
        btnEdit.setEnabled(!loading);
        btnDelete.setEnabled(!loading);
        btnExport.setEnabled(!loading);
        table.setEnabled(!loading);
        if (loading) lblStatus.setText("Loading...");
    }

    private void doExport() {
        String search = txtSearch.getText();
        setLoading(true);
        new SwingWorker<java.util.List<Student>, Void>() {
            @Override
            protected java.util.List<Student> doInBackground() throws Exception {
                int total = studentService.countStudents(search);
                return studentService.getStudents(0, Math.max(total, 1), sortKey, sortAsc, search);
            }
            @Override
            protected void done() {
                setLoading(false);
                try {
                    java.util.List<Student> all = get();
                    CsvExporter.export(
                            getFrame(),
                            "students",
                            new String[]{"Student ID", "First Name", "Last Name",
                                         "Program Code", "Program Name",
                                         "College Code", "College Name",
                                         "Year Level", "Gender"},
                            all,
                            s -> new String[]{
                                    s.getId(), s.getFirstname(), s.getLastname(),
                                    s.getProgramCode(), s.getProgramName(),
                                    s.getCollegeCode(), s.getCollegeName(),
                                    String.valueOf(s.getYear()), s.getGender()
                            });
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
        lblDetail.setForeground(Color.DARK_GRAY);
        lblDetail.setOpaque(true);
        lblDetail.setBackground(ThemeManager.CONTENT_BG);
    }

    private static class RefreshData {
        private final int total;
        private final int totalPages;
        private final int page;
        private final List<Student> rows;

        private RefreshData(int total, int totalPages, int page, List<Student> rows) {
            this.total = total;
            this.totalPages = totalPages;
            this.page = page;
            this.rows = rows;
        }
    }
}
