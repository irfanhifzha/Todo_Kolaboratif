package com.classapp.gui;

import com.classapp.ClassRoom;
import com.classapp.ClassTask;
import com.classapp.Group;
import com.classapp.Membership;
import com.classapp.TaskStatus;
import com.classapp.User;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ClassFrame extends JFrame {

    private final ClassRoom classRoom;
    private final User currentUser;

    private final DefaultListModel<User> memberListModel = new DefaultListModel<>();
    private final JList<User> memberList = new JList<>(memberListModel);

    private final DefaultListModel<Group> groupListModel = new DefaultListModel<>();
    private final JList<Group> groupList = new JList<>(groupListModel);

    private final DefaultTableModel taskTableModel =
            new DefaultTableModel(new Object[]{"Task", "Description", "My Status"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return col == 2; }
    };
    private final JTable taskTable = new JTable(taskTableModel);
    private List<ClassTask> currentTasks;
    private JButton editButton;
    private JButton deleteButton;

    public ClassFrame(ClassRoom classRoom, User currentUser) {
        super("Class: " + classRoom.getClassName() + "  (ID " + classRoom.getIdClass() + ")");
        this.classRoom = classRoom;
        this.currentUser = currentUser;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 450);
        setLocation(300,50);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Members", buildMembersPanel());
        tabs.addTab("Groups", buildGroupsPanel());
        tabs.addTab("Tasks", buildTasksPanel());
        add(tabs);

        loadMembers();
        loadGroups();
        loadTasks();
    }

    // ---------- Members tab ----------

    private JPanel buildMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("  Share Class ID " + classRoom.getIdClass() + " so others can join"), BorderLayout.NORTH);
        panel.add(new JScrollPane(memberList), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadMembers());
        JPanel buttons = new JPanel();
        buttons.add(refreshButton);
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private void loadMembers() {
        memberListModel.clear();
        // A user with both a bare membership and a group membership would
        // otherwise show up twice, so dedupe by user ID.
        Set<Integer> seen = new LinkedHashSet<>();
        for (Membership m : classRoom.getMemberships()) {
            if (seen.add(m.getUser().getIdUser())) {
                memberListModel.addElement(m.getUser());
            }
        }
    }

    // ---------- Groups tab ----------

    private final JButton joinLeaveButton = new JButton("Join Selected Group");

    private JPanel buildGroupsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JScrollPane(groupList), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton createButton = new JButton("Create Group");
        JButton refreshButton = new JButton("Refresh");
        buttons.add(createButton);
        buttons.add(joinLeaveButton);
        buttons.add(refreshButton);
        panel.add(buttons, BorderLayout.SOUTH);

        createButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "New group name:");
            if (name == null || name.trim().isEmpty()) return;
            currentUser.createGroup(classRoom, name.trim());
            loadGroups();
            loadMembers();
        });

        joinLeaveButton.addActionListener(e -> {
            Group currentGroup = findCurrentGroup();
            if (currentGroup != null) {
                // Already in a group in this class - the button is in "leave" mode.
                currentUser.leaveGroup(currentGroup);
            } else {
                Group selected = groupList.getSelectedValue();
                if (selected == null) return;
                currentUser.joinGroup(selected);
            }
            loadGroups();
            loadMembers();
        });

        refreshButton.addActionListener(e -> loadGroups());

        groupList.addListSelectionListener(e -> updateJoinLeaveButton());

        groupList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Group selected = groupList.getSelectedValue();
                    if (selected != null) {
                        new GroupFrame(selected, currentUser).setVisible(true);
                    }
                }
            }
        });

        return panel;
    }

    private void loadGroups() {
        groupListModel.clear();
        for (Group g : classRoom.getGroups()) {
            groupListModel.addElement(g);
        }
        updateJoinLeaveButton();
    }

    /** This user's current group in this class (per the one-group-per-class rule), or null if they're in none. */
    private Group findCurrentGroup() {
        for (Membership m : classRoom.getMemberships()) {
            if (m.getUser().getIdUser() == currentUser.getIdUser() && m.getGroup() != null) {
                return m.getGroup();
            }
        }
        return null;
    }

    /** Switches the button between "Join Selected Group" and "Leave from <name>" based on current membership. */
    private void updateJoinLeaveButton() {
        Group currentGroup = findCurrentGroup();
        if (currentGroup != null) {
            joinLeaveButton.setText("Leave from " + currentGroup.getGroupName());
            joinLeaveButton.setEnabled(true);
        } else {
            joinLeaveButton.setText("Join Selected Group");
            joinLeaveButton.setEnabled(groupList.getSelectedValue() != null);
        }
    }

    // ---------- Tasks tab ----------

    private JPanel buildTasksPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JComboBox<TaskStatus> statusEditor = new JComboBox<>(TaskStatus.values());
        taskTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(statusEditor));
        panel.add(new JScrollPane(taskTable), BorderLayout.CENTER);

        JButton addTaskButton = new JButton("Add Class Task");
        JButton saveButton = new JButton("Save My Statuses");
        editButton = new JButton("Edit Selected Task");
        deleteButton = new JButton("Delete Selected Task");
        JPanel buttons = new JPanel();
        buttons.add(addTaskButton);
        buttons.add(saveButton);
        buttons.add(editButton);
        buttons.add(deleteButton);
        panel.add(buttons, BorderLayout.SOUTH);

        addTaskButton.addActionListener(e -> {
            while (true) {
                JTextField titleField = new JTextField(20);
                JTextArea descArea = new JTextArea(5, 20);
                descArea.setLineWrap(true);
                descArea.setWrapStyleWord(true);

                JPanel dialogPanel = new JPanel(new BorderLayout(5, 5));

                JPanel fields = new JPanel(new GridLayout(0, 1, 5, 5));
                fields.add(new JLabel("Task Title:"));
                fields.add(titleField);
                fields.add(new JLabel("Task Description (Optional):"));
                
                dialogPanel.add(fields, BorderLayout.NORTH);
                dialogPanel.add(new JScrollPane(descArea), BorderLayout.CENTER);

                int result = JOptionPane.showConfirmDialog(
                        this,
                        dialogPanel,
                        "Add Task",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

                if (result != JOptionPane.OK_OPTION) {
                    break; // Cancel
                }

                String title = titleField.getText().trim();

                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Task title cannot be empty.",
                            "Invalid Title",
                            JOptionPane.WARNING_MESSAGE
                    );
                    continue; // reopen input modal
                }

                String description = descArea.getText().trim();

                ClassTask task = new ClassTask(title.trim(), description == null ? "" : description.trim());
                classRoom.addTask(task);
                loadTasks();
                break;
            }
        });


        saveButton.addActionListener(e -> {
            for (int row = 0; row < currentTasks.size(); row++) {
                ClassTask task = currentTasks.get(row);
                TaskStatus newStatus = (TaskStatus) taskTableModel.getValueAt(row, 2);
                task.updateUserStatus(currentUser, newStatus);
            }
            JOptionPane.showMessageDialog(this, "Saved.");
            loadTasks();
        });


        editButton.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a task first.");
                return;
            }

            ClassTask task = currentTasks.get(row);

            while (true) {
                JTextField titleField = new JTextField(task.getTitle(), 20);

                JTextArea descArea = new JTextArea(task.getDescription(), 5, 20);
                descArea.setLineWrap(true);
                descArea.setWrapStyleWord(true);

                JPanel dialogPanel = new JPanel(new BorderLayout(5, 5));

                JPanel fields = new JPanel(new GridLayout(0, 1, 5, 5));
                fields.add(new JLabel("Task Title:"));
                fields.add(titleField);
                fields.add(new JLabel("Task Description (Optional):"));

                dialogPanel.add(fields, BorderLayout.NORTH);
                dialogPanel.add(new JScrollPane(descArea), BorderLayout.CENTER);

                int result = JOptionPane.showConfirmDialog(
                        this,
                        dialogPanel,
                        "Edit Task",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

                if (result != JOptionPane.OK_OPTION) {
                    break; // cancel
                }

                String newTitle = titleField.getText().trim();

                if (newTitle.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Task title cannot be empty.",
                            "Invalid Title",
                            JOptionPane.WARNING_MESSAGE
                    );
                    continue; // reopen edit modal
                }

                String newDescription = descArea.getText().trim();

                task.editTask(newTitle, newDescription);
                loadTasks();
                break; // save successful
            }
        });

        
        deleteButton.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a task first.");
                return;
            }
            ClassTask task = currentTasks.get(row);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete \"" + task.getTitle() + "\"?",
                    "Confirm delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                task.deleteTask();
                loadTasks();
            }
        });

        return panel;
    }

    private void loadTasks() {
        currentTasks = classRoom.getTasks();
        taskTableModel.setRowCount(0);
        for (ClassTask t : currentTasks) {
            taskTableModel.addRow(new Object[]{t.getTitle(), t.getDescription(), t.getStatusFor(currentUser)});
        }
    }
}