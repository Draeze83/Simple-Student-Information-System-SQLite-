package com.sis.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final StudentPanel studentPanel = new StudentPanel();
    private final ProgramPanel programPanel = new ProgramPanel();
    private final CollegePanel collegePanel = new CollegePanel();

    public MainFrame() {
        super("Student Information System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 680);
        setMinimumSize(new Dimension(950, 520));
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(tabs.getFont().deriveFont(13f));
        tabs.setBackground(Color.WHITE);
        tabs.setOpaque(true);
        tabs.addTab("🎓  Students", studentPanel);
        tabs.addTab("📚  Programs", programPanel);
        tabs.addTab("🏛  Colleges",  collegePanel);

        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 0) studentPanel.refresh();
            else if (idx == 1) programPanel.refresh();
            else if (idx == 2) collegePanel.refresh();
        });

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(0xEBEDEF));
        topBar.setOpaque(true);
        topBar.add(tabs, BorderLayout.CENTER);

        add(topBar, BorderLayout.CENTER);
    }
}
