package com.sis.ui.dialog;

import com.sis.model.Program;
import com.sis.model.Student;
import com.sis.service.StudentService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class StudentDialog extends JDialog {

    private final JLabel     lblId      = new JLabel();
    private final JTextField txtFirst   = new JTextField(20);
    private final JTextField txtLast    = new JTextField(20);
    private final JComboBox<Integer>  cmbYear;
    private final JComboBox<String>   cmbGender;
    private final JComboBox<Integer>  cmbEnrollYear;

    // Searchable course components
    private final JTextField txtCourseSearch = new JTextField();
    private final JComboBox<Program> cmbCourse;
    private final List<Program> allPrograms;

    private boolean confirmed  = false;
    private final boolean editMode;
    private String generatedId = "";
    private final StudentService studentService;

    public StudentDialog(Frame parent, String title,
                         Student existing, List<Program> programs,
                         StudentService service) {
        super(parent, title, true);
        this.editMode      = (existing != null);
        this.allPrograms   = new ArrayList<>(programs);
        this.studentService = service;

        // Enroll-year combo (add mode only)
        int currentYear = Year.now().getValue();
        Integer[] years = new Integer[currentYear - 2018];
        for (int i = 0; i < years.length; i++) years[i] = currentYear - i;
        cmbEnrollYear = new JComboBox<>(years);

        // Course combo
        cmbCourse = new JComboBox<>(programs.toArray(new Program[0]));

        // Year level
        cmbYear = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});

        // Gender
        cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});

        buildUI();

        if (existing != null) {
            lblId.setText(existing.getId());
            txtFirst.setText(existing.getFirstname());
            txtLast.setText(existing.getLastname());
            cmbYear.setSelectedItem(existing.getYear());
            cmbGender.setSelectedItem(existing.getGender());
            for (int i = 0; i < cmbCourse.getItemCount(); i++) {
                if (cmbCourse.getItemAt(i).getId() == existing.getProgramId()) {
                    cmbCourse.setSelectedIndex(i);
                    txtCourseSearch.setText(cmbCourse.getItemAt(i).toString());
                    break;
                }
            }
        } else {
            refreshGeneratedId();
            cmbEnrollYear.addActionListener(e -> refreshGeneratedId());
        }

        // Wire search field → filter combo
        txtCourseSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filterCourse(); }
            public void removeUpdate(DocumentEvent e)  { filterCourse(); }
            public void changedUpdate(DocumentEvent e) {}
        });

        // No-op: combo updates are driven by the search field listener only

        pack();
        setMinimumSize(new Dimension(520, 300));
        setLocationRelativeTo(parent);
    }

    private void filterCourse() {
        String term = txtCourseSearch.getText().trim().toLowerCase();
        Program selected = (Program) cmbCourse.getSelectedItem();
        cmbCourse.removeAllItems();
        for (Program p : allPrograms) {
            if (term.isEmpty()
                    || p.getCode().toLowerCase().contains(term)
                    || p.getName().toLowerCase().contains(term)
                    || p.getCollegeName().toLowerCase().contains(term)) {
                cmbCourse.addItem(p);
            }
        }
        // Restore previous selection if still in list
        if (selected != null) {
            for (int i = 0; i < cmbCourse.getItemCount(); i++) {
                if (cmbCourse.getItemAt(i).getId() == selected.getId()) {
                    cmbCourse.setSelectedIndex(i);
                    break;
                }
            }
        }
        if (cmbCourse.getItemCount() > 0 && cmbCourse.getSelectedIndex() < 0) {
            cmbCourse.setSelectedIndex(0);
        }
    }

    private void buildUI() {
        Font f12 = new Font(Font.DIALOG, Font.PLAIN, 12);
        Font f12b = new Font(Font.DIALOG, Font.BOLD, 12);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST; lc.insets = new Insets(4,4,4,8);
        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL; fc.weightx = 1; fc.insets = new Insets(4,0,4,4);

        int row = 0;

        if (!editMode) {
            lc.gridx = 0; lc.gridy = row; form.add(label("Enroll Year:", f12), lc);
            fc.gridx = 1; fc.gridy = row;
            cmbEnrollYear.setFont(f12);
            form.add(cmbEnrollYear, fc);
            row++;
        }

        lc.gridy = row; form.add(label("Student ID:", f12), lc);
        fc.gridy = row;
        lblId.setFont(f12b);
        form.add(lblId, fc);
        row++;

        lc.gridy = row; form.add(label("First Name:", f12), lc);
        fc.gridy = row; txtFirst.setFont(f12); form.add(txtFirst, fc);
        row++;

        lc.gridy = row; form.add(label("Last Name:", f12), lc);
        fc.gridy = row; txtLast.setFont(f12); form.add(txtLast, fc);
        row++;

        // Course: search field + combo in a sub-panel
        lc.gridy = row; form.add(label("Course:", f12), lc);
        fc.gridy = row;
        JPanel coursePanel = new JPanel(new BorderLayout(4, 2));
        txtCourseSearch.setFont(f12);
        txtCourseSearch.setToolTipText("Type to filter courses by code, name, or college");
        JLabel searchHint = new JLabel("Filter:");
        searchHint.setFont(f12);
        JPanel searchRow = new JPanel(new BorderLayout(4, 0));
        searchRow.add(searchHint, BorderLayout.WEST);
        searchRow.add(txtCourseSearch, BorderLayout.CENTER);
        cmbCourse.setFont(f12);
        coursePanel.add(searchRow, BorderLayout.NORTH);
        coursePanel.add(cmbCourse, BorderLayout.CENTER);
        form.add(coursePanel, fc);
        row++;

        lc.gridy = row; form.add(label("Year Level:", f12), lc);
        fc.gridy = row; cmbYear.setFont(f12); form.add(cmbYear, fc);
        row++;

        lc.gridy = row; form.add(label("Gender:", f12), lc);
        fc.gridy = row; cmbGender.setFont(f12); form.add(cmbGender, fc);

        JButton btnOk     = new JButton(editMode ? "Update" : "Add");
        JButton btnCancel = new JButton("Cancel");
        btnOk.setFont(f12); btnCancel.setFont(f12);
        btnOk.addActionListener(e -> onOk());
        btnCancel.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnOk); buttons.add(btnCancel);

        setLayout(new BorderLayout());
        add(form,    BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnOk);
    }

    private JLabel label(String text, Font f) {
        JLabel l = new JLabel(text);
        l.setFont(f);
        return l;
    }

    private void refreshGeneratedId() {
        int enrollYear = (Integer) cmbEnrollYear.getSelectedItem();
        lblId.setText("Generating\u2026");
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                return studentService.generateNextId(enrollYear);
            }
            @Override protected void done() {
                try {
                    generatedId = get();
                    lblId.setText(generatedId);
                } catch (Exception ex) {
                    generatedId = "";
                    lblId.setText("Error");
                }
            }
        }.execute();
    }

    private void onOk() {
        String first = txtFirst.getText().trim();
        String last  = txtLast.getText().trim();
        if (first.isEmpty())                         { err("First name is required."); return; }
        if (first.length() > 60)                     { err("First name is too long."); return; }
        if (!first.matches("[\\p{L} .'\\-]+"))       { err("First name contains invalid characters."); return; }
        if (last.isEmpty())                          { err("Last name is required."); return; }
        if (last.length() > 60)                      { err("Last name is too long."); return; }
        if (!last.matches("[\\p{L} .'\\-]+"))        { err("Last name contains invalid characters."); return; }
        if (cmbCourse.getSelectedItem() == null)     { err("Please select a course."); return; }
        // Guard against pressing Add before the async ID generator finishes or if it failed
        if (!editMode) {
            String idText = lblId.getText();
            if ("Generating\u2026".equals(idText)) {
                err("Student ID is still being generated. Please wait a moment and try again."); return;
            }
            if ("Error".equals(idText) || generatedId == null || generatedId.isEmpty()) {
                err("Student ID could not be generated. Please change the enrolment year or restart the application."); return;
            }
        }
        confirmed = true;
        dispose();
    }

    private void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isConfirmed() { return confirmed; }

    public Student getStudent() {
        Student s = new Student();
        s.setId(editMode ? lblId.getText() : generatedId);
        s.setFirstname(txtFirst.getText().trim());
        s.setLastname(txtLast.getText().trim());
        Program p = (Program) cmbCourse.getSelectedItem();
        s.setProgramId(p.getId());
        s.setProgramCode(p.getCode());
        s.setProgramName(p.getName());
        s.setYear((Integer) cmbYear.getSelectedItem());
        s.setGender((String) cmbGender.getSelectedItem());
        return s;
    }
}
