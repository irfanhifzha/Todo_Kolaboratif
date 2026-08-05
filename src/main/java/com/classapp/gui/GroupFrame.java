package com.classapp.gui;

import com.classapp.TaskStatus;
import com.classapp.User;
import com.classapp.controller.GroupController;
import com.classapp.controller.GroupController.GroupTaskDisplay;
import com.classapp.dao.Rows.GroupRow;
import com.classapp.dao.Rows.GroupTaskRow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class GroupFrame extends JFrame {

    private final GroupController controller;

    private final DefaultListModel<User> memberListModel = new DefaultListModel<>();
    private final JList<User> memberList = new JList<>(memberListModel);
    private JButton leaveButton;

    private final DefaultTableModel taskTableModel =
            new DefaultTableModel(new Object[]{"Task", "Description", "Status"}, 0) {
                @Override public boolean isCellEditable(int row, int col) { return col == 2 && isMember; }
            };
    private final JTable taskTable = new JTable(taskTableModel);
    private List<GroupTaskDisplay> currentTasks;

    private JButton addTaskButton;
    private JButton saveButton;
    private JButton editButton;
    private JButton deleteButton;
    private boolean isMember = true;

    public GroupFrame(GroupController controller, GroupRow group, User currentUser) {
        super(group.groupName() + "  \u00b7  " + group.className() + "  \u00b7  ClassApp");
        this.controller = controller;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UiKit.BACKGROUND);
        setSize(680, 500);
        setLocation(340, 90);
        setLayout(new BorderLayout());
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        header.add(UiKit.title(group.groupName()));
        header.add(UiKit.subtitle("Part of " + group.className()));
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UiKit.FONT_LABEL);
        tabs.addTab("Members", buildMembersPanel());
        tabs.addTab("Tasks", buildTasksPanel());
        add(tabs, BorderLayout.CENTER);
    }

    // ---------- Members tab ----------

    private JPanel buildMembersPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        outer.setOpaque(false);

        JPanel card = UiKit.card();
        card.setLayout(new BorderLayout(0, 10));
        card.add(UiKit.fieldLabel("GROUP MEMBERS"), BorderLayout.NORTH);

        UiKit.styleList(memberList);
        UiKit.renderListWith(memberList, u -> u.getName() + "  (" + u.getUsername() + ")");
        JScrollPane scroll = new JScrollPane(memberList);
        scroll.setBorder(BorderFactory.createLineBorder(UiKit.BORDER));
        card.add(scroll, BorderLayout.CENTER);

        leaveButton = UiKit.dangerButton("Leave Group");
        JButton refreshButton = UiKit.secondaryButton("Refresh");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(leaveButton);
        buttons.add(refreshButton);
        card.add(buttons, BorderLayout.SOUTH);

        leaveButton.addActionListener(e -> controller.onLeaveGroup());
        refreshButton.addActionListener(e -> controller.refreshMembers());

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    // ---------- Tasks tab ----------

    private JPanel buildTasksPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        outer.setOpaque(false);

        JPanel card = UiKit.card();
        card.setLayout(new BorderLayout(0, 10));
        card.add(UiKit.fieldLabel("GROUP TASKS"), BorderLayout.NORTH);

        UiKit.styleTable(taskTable);
        JComboBox<TaskStatus> statusEditor = new JComboBox<>(TaskStatus.values());
        taskTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(statusEditor));
        JScrollPane scroll = new JScrollPane(taskTable);
        scroll.setBorder(BorderFactory.createLineBorder(UiKit.BORDER));
        card.add(scroll, BorderLayout.CENTER);

        addTaskButton = UiKit.primaryButton("Add Task");
        saveButton = UiKit.secondaryButton("Save Status Changes");
        editButton = UiKit.secondaryButton("Edit Selected");
        deleteButton = UiKit.dangerButton("Delete Selected");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(addTaskButton);
        buttons.add(saveButton);
        buttons.add(editButton);
        buttons.add(deleteButton);
        card.add(buttons, BorderLayout.SOUTH);

        addTaskButton.addActionListener(e -> {
            TaskDialog.Result result = TaskDialog.show(this, "Add Group Task", "", "");
            if (result != null) {
                controller.onCreateTask(result.title(), result.description());
            }
        });

        saveButton.addActionListener(e -> {
            if (currentTasks == null) return;
            for (int row = 0; row < currentTasks.size(); row++) {
                GroupTaskRow task = currentTasks.get(row).task();
                TaskStatus newStatus = (TaskStatus) taskTableModel.getValueAt(row, 2);
                controller.onUpdateTaskStatus(task.idTask(), newStatus);
            }
        });

        editButton.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a task first."); return; }
            GroupTaskRow task = currentTasks.get(row).task();
            TaskDialog.Result result = TaskDialog.show(this, "Edit Task", task.title(), task.description());
            if (result != null) {
                controller.onEditTask(task.idTask(), result.title(), result.description());
            }
        });

        deleteButton.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a task first."); return; }
            GroupTaskRow task = currentTasks.get(row).task();
            int confirm = JOptionPane.showConfirmDialog(this, "Delete \"" + task.title() + "\"?",
                    "Confirm delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                controller.onDeleteTask(task.idTask());
            }
        });

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    // ---------- Controller -> View updates ----------

    public void setMembers(List<User> members, boolean isMember) {
        this.isMember = isMember;
        memberListModel.clear();
        for (User u : members) {
            memberListModel.addElement(u);
        }
        if (addTaskButton != null) {
            addTaskButton.setEnabled(isMember);
            saveButton.setEnabled(isMember);
            editButton.setEnabled(isMember);
            deleteButton.setEnabled(isMember);
        }
        if (leaveButton != null) {
            leaveButton.setEnabled(isMember);
        }
    }

    public void setTasks(List<GroupTaskDisplay> tasks) {
        this.currentTasks = tasks;
        taskTableModel.setRowCount(0);
        for (GroupTaskDisplay t : tasks) {
            taskTableModel.addRow(new Object[]{t.task().title(), t.task().description(), t.status()});
        }
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
