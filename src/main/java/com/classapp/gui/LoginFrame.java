package com.classapp.gui;

import com.classapp.controller.LoginController;

import javax.swing.*;
import java.awt.*;

/**
 * Purely presentational: every button just calls back into its LoginController.
 * The view never decides what happens next - the controller does.
 */
public class LoginFrame extends JFrame {

    private final LoginController controller;

    private final JTextField nameField = new JTextField(18);
    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JLabel messageLabel = new JLabel(" ");

    public LoginFrame(LoginController controller) {
        super("ClassApp");
        this.controller = controller;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(UiKit.BACKGROUND);
        setSize(420, 480);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        add(buildCard());
    }

    private JPanel buildCard() {
        JPanel card = UiKit.card();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(340, 400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);
        int row = 0;

        gbc.gridy = row++;
        card.add(UiKit.title("Welcome to ClassApp"), gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 4, 18, 4);
        card.add(UiKit.subtitle("Log in, or register a new account"), gbc);

        gbc.insets = new Insets(6, 4, 2, 4);
        gbc.gridy = row++;
        card.add(UiKit.fieldLabel("FULL NAME (for new accounts)"), gbc);
        gbc.gridy = row++;
        UiKit.styleTextField(nameField);
        card.add(nameField, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(12, 4, 2, 4);
        card.add(UiKit.fieldLabel("USERNAME"), gbc);
        gbc.gridy = row++;
        gbc.insets = new Insets(6, 4, 2, 4);
        UiKit.styleTextField(usernameField);
        card.add(usernameField, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(12, 4, 2, 4);
        card.add(UiKit.fieldLabel("PASSWORD"), gbc);
        gbc.gridy = row++;
        gbc.insets = new Insets(6, 4, 2, 4);
        UiKit.styleTextField(passwordField);
        card.add(passwordField, gbc);

        JButton loginButton = UiKit.primaryButton("Log In");
        JButton registerButton = UiKit.secondaryButton("Register New Account");

        gbc.gridy = row++;
        gbc.insets = new Insets(20, 4, 8, 4);
        card.add(loginButton, gbc);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 4, 8, 4);
        card.add(registerButton, gbc);

        gbc.gridy = row++;
        messageLabel.setForeground(UiKit.DANGER);
        messageLabel.setFont(UiKit.FONT_BODY);
        card.add(messageLabel, gbc);

        loginButton.addActionListener(e ->
                controller.onLogin(usernameField.getText().trim(), new String(passwordField.getPassword())));
        registerButton.addActionListener(e ->
                controller.onRegister(nameField.getText().trim(), usernameField.getText().trim(),
                        new String(passwordField.getPassword())));

        return card;
    }

    public void showError(String message) {
        messageLabel.setText(message);
    }
}
