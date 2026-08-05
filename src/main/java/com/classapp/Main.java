package com.classapp;

import com.classapp.app.Application;
import com.classapp.controller.LoginController;
import com.classapp.db.Database;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Touch the DB once up front so any connection error shows immediately in the console.
        Database.getConnection();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to the default look and feel
        }

        Application application = new Application();

        SwingUtilities.invokeLater(() -> new LoginController(application).start());

        Runtime.getRuntime().addShutdownHook(new Thread(Database::close));
    }
}
