package com.classapp.gui;

import com.classapp.ClassRoom;
import com.classapp.TaskStatus;
import com.classapp.User;
import com.classapp.controller.ClassController;
import com.classapp.controller.ClassController.ClassTaskDisplay;
import com.classapp.dao.Rows.ClassTaskRow;
import com.classapp.dao.Rows.GroupRow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ClassFrame extends JFrame {

    private final ClassController controller;

    private final DefaultListModel<User> memberListModel = new DefaultListModel<>();
    private final JList<User> memberList = new JList<>(memberListModel);

    private final DefaultListModel<GroupRow> groupListModel = new DefaultListModel<>();
    private final JList<GroupRow> groupList = new JList<>(groupListModel);
    private final JButton joinLeaveButton = UiKit.secondaryButton("Join Selected Group");
    private GroupRow myGroup;

    private final DefaultTableModel taskTableModel =
            new DefaultTableModel(new Object[]{"Task", "Description", "My Status"}, 0) {
                @Override public boolean isCellEditable(int row, int col) { return col == 2; }
            };
    private final JTable taskTable = new JTable(taskTableModel);
    private List<ClassTaskDisplay> currentTasks;

    public ClassFrame(ClassController controller, ClassRoom classRoom, User currentUser) {
        super(classRoom.getClassName() + "  \u00b7  ClassApp");
        this.controller = controller;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UiKit.BACKGROUND);
        setSize(680, 520);
        setLocation(260, 60);
        setLayout(new BorderLayout());
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        header.add(UiKit.title(classRoom.getClassName()));
        header.add(UiKit.subtitle("Class ID " + classRoom.getIdClass() + "  \u2014  share this so others can join"));
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UiKit.FONT_LABEL);
        tabs.addTab("Members", buildMembersPanel());
        tabs.addTab("Groups", buildGroupsPanel());
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
        card.add(UiKit.fieldLabel("CLASS MEMBERS"), BorderLayout.NORTH);

        UiKit.styleList(memberList);
        UiKit.renderListWith(memberList, u -> u.getName() + "  (" + u.getUsername() + ")");
        JScrollPane scroll = new JScrollPane(memberList);
        scroll.setBorder(BorderFactory.createLineBorder(UiKit.BORDER));
        card.add(scroll, BorderLayout.CENTER);

        JButton refreshButton = UiKit.secondaryButton("Refresh");
        refreshButton.addActionListener(e -> controller.refreshMembers());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(refreshButton);
        card.add(buttons, BorderLayout.SOUTH);

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    // ---------- Groups tab ----------

    private JPanel buildGroupsPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        outer.setOpaque(false);

        JPanel card = UiKit.card();
        card.setLayout(new BorderLayout(0, 10));
        card.add(UiKit.fieldLabel("GROUPS IN THIS CLASS  \u2014  double-click to open"), BorderLayout.NORTH);

        UiKit.styleList(groupList);
        UiKit.renderListWith(groupList, GroupRow::groupName);
        JScrollPane scroll = new JScrollPane(groupList);
        scroll.setBorder(BorderFactory.createLineBorder(UiKit.BORDER));
        card.add(scroll, BorderLayout.CENTER);

        JButton createButton = UiKit.primaryButton("Create Group");
        JButton refreshButton = UiKit.secondaryButton("Refresh");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(createButton);
        buttons.add(joinLeaveButton);
        buttons.add(refreshButton);
        card.add(buttons, BorderLayout.SOUTH);

        createButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "New group name:");
            if (name == null || name.trim().isEmpty()) return;
            controller.onCreateGroup(name.trim());
        });

        joinLeaveButton.addActionListener(e -> {
            if (myGroup != null) {
                controller.onLeaveGroup();
            } else {
                GroupRow selected = groupList.getSelectedValue();
                if (selected == null) return;
                controller.onJoinGroup(selected.idGroup());
            }
        });

        refreshButton.addActionListener(e -> controller.refreshGroups());
        groupList.addListSelectionListener(e -> updateJoinLeaveButton());

        groupList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    GroupRow selected = groupList.getSelectedValue();
                    if (selected != null) {
                        controller.onOpenGroup(selected);
                    }
                }
            }
        });

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    private void updateJoinLeaveButton() {
        if (myGroup != null) {
            joinLeaveButton.setText("Leave " + myGroup.groupName());
            joinLeaveButton.setEnabled(true);
        } else {
            joinLeaveButton.setText("Join Selected Group");
            joinLeaveButton.setEnabled(groupList.getSelectedValue() != null);
        }
    }

    // ---------- Tasks tab ----------

    private JPanel buildTasksPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        outer.setOpaque(false);

        JPanel card = UiKit.card();
        card.setLayout(new BorderLayout(0, 10));
        card.add(UiKit.fieldLabel("CLASS TASKS"), BorderLayout.NORTH);

        UiKit.styleTable(taskTable);
        JComboBox<TaskStatus> statusEditor = new JComboBox<>(TaskStatus.values());
        taskTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(statusEditor));
        JScrollPane scroll = new JScrollPane(taskTable);
        scroll.setBorder(BorderFactory.createLineBorder(UiKit.BORDER));
        card.add(scroll, BorderLayout.CENTER);

        JButton addTaskButton = UiKit.primaryButton("Add Task");
        JButton saveButton = UiKit.secondaryButton("Save My Statuses");
        JButton editButton = UiKit.secondaryButton("Edit Selected");
        JButton deleteButton = UiKit.dangerButton("Delete Selected");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(addTaskButton);
        buttons.add(saveButton);
        buttons.add(editButton);
        buttons.add(deleteButton);
        card.add(buttons, BorderLayout.SOUTH);

        addTaskButton.addActionListener(e -> {
            TaskDialog.Result result = TaskDialog.show(this, "Add Class Task", "", "");
            if (result != null) {
                controller.onCreateTask(result.title(), result.description());
            }
        });

        saveButton.addActionListener(e -> {
            if (currentTasks == null) return;
            for (int row = 0; row < currentTasks.size(); row++) {
                ClassTaskRow task = currentTasks.get(row).task();
                TaskStatus newStatus = (TaskStatus) taskTableModel.getValueAt(row, 2);
                controller.onUpdateMyTaskStatus(task.idTask(), newStatus);
            }
        });

        editButton.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a task first."); return; }
            ClassTaskRow task = currentTasks.get(row).task();
            TaskDialog.Result result = TaskDialog.show(this, "Edit Task", task.title(), task.description());
            if (result != null) {
                controller.onEditTask(task.idTask(), result.title(), result.description());
            }
        });

        deleteButton.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a task first."); return; }
            ClassTaskRow task = currentTasks.get(row).task();
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

    public void setMembers(List<User> members) {
        memberListModel.clear();
        for (User u : members) {
            memberListModel.addElement(u);
        }
    }

    public void setGroups(List<GroupRow> groups, GroupRow myGroup) {
        this.myGroup = myGroup;
        groupListModel.clear();
        for (GroupRow g : groups) {
            groupListModel.addElement(g);
        }
        updateJoinLeaveButton();
    }

    public void setTasks(List<ClassTaskDisplay> tasks) {
        this.currentTasks = tasks;
        taskTableModel.setRowCount(0);
        for (ClassTaskDisplay t : tasks) {
            taskTableModel.addRow(new Object[]{t.task().title(), t.task().description(), t.myStatus()});
        }
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
