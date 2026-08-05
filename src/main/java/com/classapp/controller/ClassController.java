package com.classapp.controller;

import com.classapp.ClassRoom;
import com.classapp.TaskStatus;
import com.classapp.User;
import com.classapp.app.Application;
import com.classapp.dao.Rows.ClassTaskRow;
import com.classapp.dao.Rows.GroupRow;
import com.classapp.gui.ClassFrame;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Controls ClassFrame: members, groups, and class tasks for one classroom. */
public class ClassController {

    /** What ClassFrame needs to show one row of the task table: the task plus this user's status on it. */
    public record ClassTaskDisplay(ClassTaskRow task, TaskStatus myStatus) {}

    private final Application app;
    private final User currentUser;
    private final ClassRoom classRoom;
    private ClassFrame view;

    public ClassController(Application app, User currentUser, ClassRoom classRoom) {
        this.app = app;
        this.currentUser = currentUser;
        this.classRoom = classRoom;
    }

    public void start() {
        view = new ClassFrame(this, classRoom, currentUser);
        view.setVisible(true);
        refreshMembers();
        refreshGroups();
        refreshTasks();
    }

    public void refreshMembers() {
        try {
            view.setMembers(app.listClassMembers(classRoom.getIdClass()));
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    public void refreshGroups() {
        try {
            GroupRow myGroup = app.findMyGroupInClass(currentUser, classRoom.getIdClass());
            view.setGroups(app.listClassGroups(classRoom.getIdClass()), myGroup);
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    public void refreshTasks() {
        try {
            List<ClassTaskRow> tasks = app.listClassTasks(classRoom.getIdClass());
            List<ClassTaskDisplay> display = new ArrayList<>();
            for (ClassTaskRow task : tasks) {
                TaskStatus status = app.getMyClassTaskStatus(currentUser.getIdUser(), task.idTask());
                display.add(new ClassTaskDisplay(task, status));
            }
            view.setTasks(display);
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    public void onCreateGroup(String groupName) {
        try {
            app.createGroup(classRoom.getIdClass(), groupName);
            refreshGroups();
            refreshMembers();
        } catch (ValidationException ex) {
            view.showError(ex.getMessage());
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    public void onJoinGroup(int groupId) {
        try {
            app.joinGroup(currentUser, classRoom.getIdClass(), groupId);
            refreshGroups();
            refreshMembers();
        } catch (ValidationException ex) {
            view.showError(ex.getMessage());
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    public void onLeaveGroup() {
        try {
            app.leaveGroup(currentUser, classRoom.getIdClass());
            refreshGroups();
            refreshMembers();
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    /** Opens a group in its own window; this class window stays open behind it. */
    public void onOpenGroup(GroupRow group) {
        GroupController groupController = new GroupController(app, currentUser, group, this);
        groupController.start();
    }

    public void onCreateTask(String title, String description) {
        try {
            app.createClassTask(classRoom.getIdClass(), title, description);
            refreshTasks();
        } catch (ValidationException ex) {
            view.showError(ex.getMessage());
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    public void onEditTask(int taskId, String title, String description) {
        try {
            app.editClassTask(taskId, title, description, classRoom.getIdClass());
            refreshTasks();
        } catch (ValidationException ex) {
            view.showError(ex.getMessage());
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    public void onDeleteTask(int taskId) {
        try {
            app.deleteClassTask(taskId);
            refreshTasks();
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    public void onUpdateMyTaskStatus(int taskId, TaskStatus status) {
        try {
            app.setMyClassTaskStatus(currentUser.getIdUser(), taskId, status);
            refreshTasks();
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }
}
