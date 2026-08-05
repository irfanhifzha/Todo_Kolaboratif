package com.classapp.controller;

import com.classapp.TaskStatus;
import com.classapp.User;
import com.classapp.app.Application;
import com.classapp.dao.Rows.GroupRow;
import com.classapp.dao.Rows.GroupTaskRow;
import com.classapp.gui.GroupFrame;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Controls GroupFrame: members and group tasks for one group. */
public class GroupController {

    /** What GroupFrame needs to show one row of the task table: the task plus its shared status. */
    public record GroupTaskDisplay(GroupTaskRow task, TaskStatus status) {}

    private final Application app;
    private final User currentUser;
    private final GroupRow group;
    private final ClassController parent;
    private GroupFrame view;

    public GroupController(Application app, User currentUser, GroupRow group, ClassController parent) {
        this.app = app;
        this.currentUser = currentUser;
        this.group = group;
        this.parent = parent;
    }

    public void start() {
        view = new GroupFrame(this, group, currentUser);
        view.setVisible(true);
        refreshMembers();
        refreshTasks();
    }

    public void refreshMembers() {
        try {
            List<User> members = app.listGroupMembers(group.idGroup());
            boolean isMember = members.stream().anyMatch(u -> u.getIdUser() == currentUser.getIdUser());
            view.setMembers(members, isMember);
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    public void refreshTasks() {
        try {
            List<GroupTaskRow> tasks = app.listGroupTasks(group.idGroup());
            List<GroupTaskDisplay> display = new ArrayList<>();
            for (GroupTaskRow task : tasks) {
                display.add(new GroupTaskDisplay(task, app.getGroupTaskStatus(task.idTask())));
            }
            view.setTasks(display);
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    public void onLeaveGroup() {
        try {
            app.leaveGroup(currentUser, group.idClass());
            view.dispose();
            parent.refreshGroups();
            parent.refreshMembers();
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    public void onCreateTask(String title, String description) {
        try {
            app.createGroupTask(group.idGroup(), title, description);
            refreshTasks();
        } catch (ValidationException ex) {
            view.showError(ex.getMessage());
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    public void onEditTask(int taskId, String title, String description) {
        try {
            app.editGroupTask(taskId, title, description, group.idGroup());
            refreshTasks();
        } catch (ValidationException ex) {
            view.showError(ex.getMessage());
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    public void onDeleteTask(int taskId) {
        try {
            app.deleteGroupTask(taskId);
            refreshTasks();
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    public void onUpdateTaskStatus(int taskId, TaskStatus status) {
        try {
            app.setGroupTaskStatus(taskId, status);
            refreshTasks();
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }
}
