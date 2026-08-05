package com.classapp.controller;

import com.classapp.ClassRoom;
import com.classapp.User;
import com.classapp.app.Application;
import com.classapp.gui.MainFrame;

import java.sql.SQLException;

/** Controls MainFrame: the logged-in user's home screen (their classes). */
public class MainController {

    private final Application app;
    private final User currentUser;
    private MainFrame view;

    public MainController(Application app, User currentUser) {
        this.app = app;
        this.currentUser = currentUser;
    }

    public void start() {
        view = new MainFrame(this, currentUser);
        view.setVisible(true);
        refreshClasses();
    }

    public void refreshClasses() {
        try {
            view.setClasses(app.listMyClasses(currentUser));
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    public void onCreateClass(String className) {
        try {
            app.createClass(currentUser, className);
            refreshClasses();
        } catch (ValidationException ex) {
            view.showError(ex.getMessage());
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    public void onJoinClass(int classId) {
        try {
            ClassRoom classRoom = app.joinClassById(currentUser, classId);
            if (classRoom == null) {
                view.showError("No class with that ID.");
                return;
            }
            refreshClasses();
        } catch (ValidationException ex) {
            view.showError(ex.getMessage());
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    /** Opens a class in its own window; the home screen stays open behind it. */
    public void onOpenClass(ClassRoom classRoom) {
        ClassController classController = new ClassController(app, currentUser, classRoom);
        classController.start();
    }

    public void onLogout() {
        view.dispose();
        new LoginController(app).start();
    }
}
