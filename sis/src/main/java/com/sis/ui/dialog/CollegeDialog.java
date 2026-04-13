package com.sis.ui.dialog;

import com.sis.model.College;

import javax.swing.*;
import java.awt.*;

public class CollegeDialog extends JDialog {

    private final JTextField txtCode = new JTextField(10);
    private final JTextField txtName = new JTextField(35);
    private boolean confirmed = false;
    private final boolean editMode;

    public CollegeDialog(Frame parent, String title, College existing) {
        super(parent, title, true);
        this.editMode = (existing != null);
        buildUI();
        if (existing != null) {
            txtCode.setText(existing.getCode());
            txtCode.setEditable(false);      // PK not changeable after creation
            txtCode.setBackground(UIManager.getColor("TextField.inactiveBackground"));
            txtName.setText(existing.getName());
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

        lc.gridx = 0; lc.gridy = 0; form.add(new JLabel("College Code:"), lc);
        fc.gridx = 1; fc.gridy = 0; form.add(txtCode, fc);
        lc.gridy = 1;               form.add(new JLabel("College Name:"), lc);
        fc.gridy = 1;               form.add(txtName, fc);

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
        if (code.isEmpty()) { err("College code is required."); return; }
        if (code.length() > 10) { err("College code must be 10 characters or fewer."); return; }
        if (!code.matches("[A-Z0-9]+")) { err("College code must contain only letters/digits."); return; }
        if (name.isEmpty()) { err("College name is required."); return; }
        if (name.length() > 100) { err("College name is too long (max 100 chars)."); return; }
        confirmed = true;
        dispose();
    }

    private void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isConfirmed() { return confirmed; }

    public College getCollege() {
        return new College(txtCode.getText().trim().toUpperCase(),
                           txtName.getText().trim());
    }
}
