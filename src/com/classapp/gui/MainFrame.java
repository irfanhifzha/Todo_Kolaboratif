package com.classapp.gui;

import com.classapp.ClassRoom;
import com.classapp.Membership;
import com.classapp.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashSet;
import java.util.Set;

public class MainFrame extends JFrame {

    private final User currentUser;

    private final DefaultListModel<ClassRoom> listModel = new DefaultListModel<>();
    private final JList<ClassRoom> classList = new JList<>(listModel);

    public MainFrame(User currentUser) {
        super("Class App - " + currentUser.getName());
        this.currentUser = currentUser;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(new JLabel("  My Classes (double-click to open)"), BorderLayout.NORTH);
        add(new JScrollPane(classList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton createButton = new JButton("Create Class");
        JButton joinButton = new JButton("Join Class by ID");
        JButton refreshButton = new JButton("Refresh");
        buttonPanel.add(createButton);
        buttonPanel.add(joinButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        createButton.addActionListener(e -> createClass());
        joinButton.addActionListener(e -> joinClass());
        refreshButton.addActionListener(e -> loadClasses());

        classList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ClassRoom selected = classList.getSelectedValue();
                    if (selected != null) {
                        new ClassFrame(selected, currentUser).setVisible(true);
                    }
                }
            }
        });

        loadClasses();
    }

    private void loadClasses() {
        listModel.clear();
        // A user can have more than one Membership row per class (a bare
        // one plus one per group they've joined), so dedupe by class ID.
        Set<Integer> seen = new LinkedHashSet<>();
        for (Membership m : currentUser.getMemberships()) {
            ClassRoom c = m.getClassRoom();
            if (seen.add(c.getIdClass())) {
                listModel.addElement(c);
            }
        }
    }

    private void createClass() {
        String name = JOptionPane.showInputDialog(this, "New class name:");
        if (name == null || name.trim().isEmpty()) return;

        currentUser.createClass(name.trim());
        loadClasses();
    }

    private void joinClass() {
        String idText = JOptionPane.showInputDialog(this, "Class ID to join:");
        if (idText == null || idText.trim().isEmpty()) return;

        try {
            int classId = Integer.parseInt(idText.trim());
            ClassRoom classRoom = ClassRoom.load(classId);
            if (classRoom == null) {
                JOptionPane.showMessageDialog(this, "No class with that ID.");
                return;
            }
            currentUser.joinClass(classRoom);
            loadClasses();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Class ID must be a number.");
        }
    }
}
