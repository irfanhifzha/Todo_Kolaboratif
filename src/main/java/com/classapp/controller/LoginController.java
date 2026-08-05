package com.classapp.controller;

import com.classapp.User;
import com.classapp.app.Application;
import com.classapp.gui.LoginFrame;

import java.sql.SQLException;

/**
 * Controls LoginFrame. On a successful login or registration it decides the next
 * view (MainFrame, via MainController) and closes the login window - the view
 * itself never does this. This is the entry point Controller for the whole app.
 */
public class LoginController {

    private final Application app;
    private LoginFrame view;

    public LoginController(Application app) {
        this.app = app;
    }

    public void start() {
        view = new LoginFrame(this);
        view.setVisible(true);
    }

    public void onLogin(String username, String password) {
        try {
            User user = app.login(username, password);
            if (user == null) {
                view.showError("Invalid username or password.");
                return;
            }
            goToMain(user);
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    public void onRegister(String name, String username, String password) {
        try {
            User user = app.register(name, username, password);
            goToMain(user);
        } catch (ValidationException ex) {
            view.showError(ex.getMessage());
        } catch (SQLException ex) {
            view.showError("Database error: " + ex.getMessage());
        }
    }

    private void goToMain(User user) {
        MainController mainController = new MainController(app, user);
        mainController.start();
        view.dispose();
    }
}
