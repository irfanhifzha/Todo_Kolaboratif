package com.classapp.gui;

import com.classapp.ClassRoom;
import com.classapp.User;
import com.classapp.controller.MainController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MainFrame extends JFrame {

    private final MainController controller;

    private final DefaultListModel<ClassRoom> listModel = new DefaultListModel<>();
    private final JList<ClassRoom> classList = new JList<>(listModel);

    public MainFrame(MainController controller, User currentUser) {
        super("ClassApp");
        this.controller = controller;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(UiKit.BACKGROUND);
        setSize(560, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeader(currentUser), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    private JPanel buildHeader(User currentUser) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(UiKit.title("Hi, " + currentUser.getName()));
        left.add(UiKit.subtitle("Here are the classes you're part of"));
        header.add(left, BorderLayout.WEST);

        JButton logoutButton = UiKit.secondaryButton("Log Out");
        logoutButton.addActionListener(e -> controller.onLogout());
        header.add(logoutButton, BorderLayout.EAST);

        return header;
    }

    private JPanel buildBody() {
        JPanel body = UiKit.card();
        body.setLayout(new BorderLayout(0, 12));

        JLabel listCaption = UiKit.fieldLabel("MY CLASSES  \u2014  double-click to open");
        body.add(listCaption, BorderLayout.NORTH);

        UiKit.styleList(classList);
        UiKit.renderListWith(classList, c -> c.getClassName() + "   (ID " + c.getIdClass() + ")");
        JScrollPane scroll = new JScrollPane(classList);
        scroll.setBorder(BorderFactory.createLineBorder(UiKit.BORDER));
        body.add(scroll, BorderLayout.CENTER);

        JButton createButton = UiKit.primaryButton("Create Class");
        JButton joinButton = UiKit.secondaryButton("Join Class by ID");
        JButton refreshButton = UiKit.secondaryButton("Refresh");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(createButton);
        buttons.add(joinButton);
        buttons.add(refreshButton);
        body.add(buttons, BorderLayout.SOUTH);

        createButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "New class name:");
            if (name == null || name.trim().isEmpty()) return;
            controller.onCreateClass(name.trim());
        });

        joinButton.addActionListener(e -> {
            String idText = JOptionPane.showInputDialog(this, "Class ID to join:");
            if (idText == null || idText.trim().isEmpty()) return;
            try {
                controller.onJoinClass(Integer.parseInt(idText.trim()));
            } catch (NumberFormatException ex) {
                showError("Class ID must be a number.");
            }
        });

        refreshButton.addActionListener(e -> controller.refreshClasses());

        classList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ClassRoom selected = classList.getSelectedValue();
                    if (selected != null) {
                        controller.onOpenClass(selected);
                    }
                }
            }
        });

        return body;
    }

    public void setClasses(List<ClassRoom> classes) {
        listModel.clear();
        for (ClassRoom c : classes) {
            listModel.addElement(c);
        }
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
