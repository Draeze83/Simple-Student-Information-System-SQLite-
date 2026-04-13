package com.sis.ui.dialog;

import com.sis.model.College;
import com.sis.model.Program;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ProgramDialog extends JDialog {

    private final JTextField  txtCode    = new JTextField(10);
    private final JTextField  txtName    = new JTextField(55);
    private final JComboBox<College> cmbCollege;
    private boolean confirmed = false;
    private final boolean editMode;

    public ProgramDialog(Frame parent, String title,
                         Program existing, List<College> colleges) {
        super(parent, title, true);
        this.editMode = (existing != null);
        this.cmbCollege = new JComboBox<>(colleges.toArray(new College[0]));
        buildUI();
        if (existing != null) {
            txtCode.setText(existing.getCode());
            txtName.setText(existing.getName());
            for (int i = 0; i < cmbCollege.getItemCount(); i++) {
                if (cmbCollege.getItemAt(i).getCode()
                              .equalsIgnoreCase(existing.getCollegeCode())) {
                    cmbCollege.setSelectedIndex(i);
                    break;
                }
            }
        }
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST; lc.insets = new Insets(4,4,4,8);
        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL; fc.weightx = 1; fc.insets = new Insets(4,0,4,4);

        lc.gridx = 0; lc.gridy = 0; form.add(new JLabel("Program Code:"), lc);
        fc.gridx = 1; fc.gridy = 0; form.add(txtCode, fc);
        lc.gridy = 1;               form.add(new JLabel("Program Name:"), lc);
        fc.gridy = 1;               form.add(txtName, fc);
        lc.gridy = 2;               form.add(new JLabel("College:"), lc);
        fc.gridy = 2;               form.add(cmbCollege, fc);

        JButton btnOk     = new JButton(editMode ? "Update" : "Add");
        JButton btnCancel = new JButton("Cancel");
        btnOk.addActionListener(e -> onOk());
        btnCancel.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnOk); buttons.add(btnCancel);

        setLayout(new BorderLayout());
        add(form,    BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnOk);
    }

    private void onOk() {
        String code = txtCode.getText().trim().toUpperCase();
        String name = txtName.getText().trim();
        if (code.isEmpty()) { err("Program code is required."); return; }
        if (!code.matches("[A-Z0-9]+")) { err("Program code must contain only letters/digits."); return; }
        if (name.isEmpty()) { err("Program name is required."); return; }
        if (cmbCollege.getSelectedItem() == null) { err("Please select a college."); return; }
        confirmed = true;
        dispose();
    }

    private void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isConfirmed() { return confirmed; }

    public Program getProgram() {
        College col = (College) cmbCollege.getSelectedItem();
        Program p = new Program();
        p.setCode(txtCode.getText().trim().toUpperCase());
        p.setName(txtName.getText().trim());
        p.setCollegeCode(col.getCode());
        p.setCollegeName(col.getName());
        return p;
    }
}
