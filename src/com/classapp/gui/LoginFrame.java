package com.classapp.gui;

import com.classapp.User;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JLabel messageLabel = new JLabel(" ");

    public LoginFrame() {
        super("Class App - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        setSize(360, 260);
        setLocationRelativeTo(null);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(new JLabel("Welcome - please log in or register", SwingConstants.CENTER), gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        add(usernameField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);

        JButton loginButton = new JButton("Log In");
        JButton registerButton = new JButton("Register New Account");

        gbc.gridy = 3; gbc.gridx = 0;
        add(loginButton, gbc);
        gbc.gridx = 1;
        add(registerButton, gbc);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        messageLabel.setForeground(Color.RED);
        add(messageLabel, gbc);

        loginButton.addActionListener(e -> doLogin());
        registerButton.addActionListener(e -> doRegister());
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Enter a username and password.");
            return;
        }

        User user = new User(username, password);
        if (!user.login()) {
            messageLabel.setText("Invalid username or password.");
            return;
        }

        new MainFrame(user).setVisible(true);
        dispose();
    }

    private void doRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Enter a username and password.");
            return;
        }

        String fullName = JOptionPane.showInputDialog(this, "Your full name:");
        if (fullName == null || fullName.trim().isEmpty()) {
            messageLabel.setText("Registration cancelled - name is required.");
            return;
        }

        User user = User.register(fullName.trim(), username, password);
        if (user == null) {
            messageLabel.setText("That username is already taken.");
            return;
        }

        JOptionPane.showMessageDialog(this, "Account created! You are now logged in.");
        new MainFrame(user).setVisible(true);
        dispose();
    }
}
