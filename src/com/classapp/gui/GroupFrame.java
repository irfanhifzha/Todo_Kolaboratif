package com.classapp.gui;

import com.classapp.Group;
import com.classapp.GroupTask;
import com.classapp.Membership;
import com.classapp.TaskStatus;
import com.classapp.User;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class GroupFrame extends JFrame {

    private final Group group;
    private final User currentUser;

    private final DefaultListModel<User> memberListModel = new DefaultListModel<>();
    private final JList<User> memberList = new JList<>(memberListModel);

    private final DefaultTableModel taskTableModel =
            new DefaultTableModel(new Object[]{"Task", "Description", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return col == 2 && isMember(); }
    };
    private final JTable taskTable = new JTable(taskTableModel);
    private List<GroupTask> currentTasks;

    private JButton addTaskButton;
    private JButton saveButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton leaveButton;

    public GroupFrame(Group group, User currentUser) {
        super("Group: " + group.getGroupName() + "  (in " + group.getClassRoom().getClassName() + ")");
        this.group = group;
        this.currentUser = currentUser;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(630, 450);
        setLocation(400,50);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Members", buildMembersPanel());
        tabs.addTab("Tasks", buildTasksPanel());
        add(tabs);

        loadMembers();
        loadTasks();
    }

    /** Whether currentUser actually belongs to this group - only members may add tasks or edit status. */
    private boolean isMember() {
        for (Membership m : group.getMemberships()) {
            if (m.getUser().getIdUser() == currentUser.getIdUser()) {
                return true;
            }
        }
        return false;
    }

    /** Re-applies the member-only restriction to the buttons after membership might have changed. */
    private void refreshEditingState() {
        boolean member = isMember();
        addTaskButton.setEnabled(member);
        saveButton.setEnabled(member);
        editButton.setEnabled(member);
        deleteButton.setEnabled(member);
        leaveButton.setEnabled(member);
    }

    // ---------- Members tab ----------

    private JPanel buildMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JScrollPane(memberList), BorderLayout.CENTER);

        leaveButton = new JButton("Leave Group");
        JButton refreshButton = new JButton("Refresh");
        JPanel buttons = new JPanel();
        buttons.add(leaveButton);
        buttons.add(refreshButton);
        panel.add(buttons, BorderLayout.SOUTH);

        leaveButton.addActionListener(e -> {
            currentUser.leaveGroup(group);
            loadMembers();
            JOptionPane.showMessageDialog(this, "You left this group (you're still in the class).");
        });

        refreshButton.addActionListener(e -> loadMembers());

        return panel;
    }

    private void loadMembers() {
        memberListModel.clear();
        for (Membership m : group.getMemberships()) {
            memberListModel.addElement(m.getUser());
        }
        refreshEditingState();
    }

    // ---------- Tasks tab ----------

    private JPanel buildTasksPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JComboBox<TaskStatus> statusEditor = new JComboBox<>(TaskStatus.values());
        taskTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(statusEditor));
        panel.add(new JScrollPane(taskTable), BorderLayout.CENTER);

        addTaskButton = new JButton("Add Group Task");
        saveButton = new JButton("Save Status Changes");
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

                GroupTask task = new GroupTask(title.trim(), description == null ? "" : description.trim());
                group.createTask(task);
                loadTasks();
                break;
            }
        });

        saveButton.addActionListener(e -> {
            for (int row = 0; row < currentTasks.size(); row++) {
                GroupTask task = currentTasks.get(row);
                TaskStatus newStatus = (TaskStatus) taskTableModel.getValueAt(row, 2);
                task.updateGroupStatus(newStatus);
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

            GroupTask task = currentTasks.get(row);

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
            GroupTask task = currentTasks.get(row);
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
        currentTasks = group.getTasks();
        taskTableModel.setRowCount(0);
        for (GroupTask t : currentTasks) {
            taskTableModel.addRow(new Object[]{t.getTitle(), t.getDescription(), t.getStatus()});
        }
    }
}