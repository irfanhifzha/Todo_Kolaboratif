package com.classapp.gui;

import javax.swing.*;
import java.awt.*;

/** A small reusable "Title + Description" dialog, used for both adding and editing tasks. */
final class TaskDialog {
    private TaskDialog() {}

    public record Result(String title, String description) {}

    /** Shows the dialog pre-filled with the given values; returns null if the user cancelled. */
    static Result show(Component parent, String dialogTitle, String initialTitle, String initialDescription) {
        JTextField titleField = new JTextField(initialTitle, 22);
        UiKit.styleTextField(titleField);

        JTextArea descArea = new JTextArea(initialDescription, 5, 22);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(UiKit.FONT_BODY);

        while (true) {
            JPanel panel = new JPanel(new BorderLayout(6, 6));
            JPanel fields = new JPanel(new GridLayout(0, 1, 4, 4));
            fields.add(UiKit.fieldLabel("TITLE"));
            fields.add(titleField);
            fields.add(UiKit.fieldLabel("DESCRIPTION (optional)"));
            panel.add(fields, BorderLayout.NORTH);
            panel.add(new JScrollPane(descArea), BorderLayout.CENTER);

            int result = JOptionPane.showConfirmDialog(parent, panel, dialogTitle,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return null;
            }

            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Task title cannot be empty.", "Invalid title", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            return new Result(title, descArea.getText().trim());
        }
    }
}
