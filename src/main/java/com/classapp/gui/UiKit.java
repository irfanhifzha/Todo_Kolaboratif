package com.classapp.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Small, shared look-and-feel helpers so LoginFrame/MainFrame/ClassFrame/GroupFrame
 * all read as one consistent app instead of four separately-styled windows.
 */
final class UiKit {
    private UiKit() {}

    static final Color BACKGROUND = new Color(0xF4F5F9);
    static final Color CARD = Color.WHITE;
    static final Color BORDER = new Color(0xE3E5EC);
    static final Color PRIMARY = new Color(0x4F46E5);
    static final Color PRIMARY_HOVER = new Color(0x4338CA);
    static final Color TEXT = new Color(0x1F2430);
    static final Color MUTED = new Color(0x6B7280);
    static final Color DANGER = new Color(0xDC2626);

    static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 20);
    static final Font FONT_SUBTITLE = new Font("SansSerif", Font.PLAIN, 13);
    static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD, 12);
    static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 13);

    /** A filled, rounded, primary-colored button. */
    static JButton primaryButton(String text) {
        return new RoundedButton(text, PRIMARY, PRIMARY_HOVER, Color.WHITE);
    }

    /** A rounded button with a light background - used for secondary actions. */
    static JButton secondaryButton(String text) {
        return new RoundedButton(text, new Color(0xEDEEF5), new Color(0xE1E3EE), TEXT);
    }

    /** A rounded button in a red tone - used for destructive actions like Delete/Leave. */
    static JButton dangerButton(String text) {
        return new RoundedButton(text, new Color(0xFDEBEC), new Color(0xFBDADC), DANGER);
    }

    static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_TITLE);
        label.setForeground(TEXT);
        return label;
    }

    static JLabel subtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SUBTITLE);
        label.setForeground(MUTED);
        return label;
    }

    static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(MUTED);
        return label;
    }

    static void styleTextField(JTextComponent field) {
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(6, 8, 6, 8)));
    }

    /** A white "card" panel with a subtle border and inner padding - the base building block of every screen. */
    static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        return panel;
    }

    static void styleTable(JTable table) {
        table.setRowHeight(26);
        table.setFont(FONT_BODY);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(0xEEF0FF));
        table.setSelectionForeground(TEXT);
        table.getTableHeader().setFont(FONT_LABEL);
        table.getTableHeader().setBackground(BACKGROUND);
        table.getTableHeader().setForeground(MUTED);
    }

    /**
     * Renders each item of a JList using a custom label function, since the given POJOs
     * (ClassRoom, User, ...) don't override toString(). No POJO changes needed.
     */
    static <T> void renderListWith(JList<T> list, java.util.function.Function<T, String> label) {
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object value, int index,
                                                            boolean isSelected, boolean cellHasFocus) {
                @SuppressWarnings("unchecked")
                String text = value == null ? "" : label.apply((T) value);
                return super.getListCellRendererComponent(l, text, index, isSelected, cellHasFocus);
            }
        });
    }

    static void styleList(JList<?> list) {
        list.setFont(FONT_BODY);
        list.setSelectionBackground(new Color(0xEEF0FF));
        list.setSelectionForeground(TEXT);
        list.setBorder(new EmptyBorder(4, 4, 4, 4));
    }

    /** A simple rounded-rectangle JButton (Swing has no built-in rounded style). */
    private static class RoundedButton extends JButton {
        private final Color base;
        private final Color hover;
        RoundedButton(String text, Color base, Color hover, Color fg) {
            super(text);
            this.base = base;
            this.hover = hover;
            setForeground(fg);
            setFont(FONT_BUTTON);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorder(new EmptyBorder(8, 18, 8, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() || getModel().isPressed() ? hover : base);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
