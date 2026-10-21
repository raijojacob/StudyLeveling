package ui;

import db.UserDAO;
import logic.SystemLogger;
import logic.UserSession;
import models.User;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private UserDAO userDAO;
    private AuthFrame parentFrame;

    public LoginPanel(AuthFrame parentFrame) {
        this.parentFrame = parentFrame;
        userDAO = new UserDAO();

        setLayout(new GridBagLayout());
        setBackground(new Color(24, 32, 38)); // Slate Card Background
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(38, 52, 64), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        // UI Components
        JLabel titleLabel = new JLabel("OPERATOR LOGIN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("ENTER CREDENTIALS TO ACCESS SYSTEM", SwingConstants.CENTER);
        subLabel.setFont(new Font("Segoe UI", Font.BOLD, 9));
        subLabel.setForeground(new Color(56, 189, 248));

        JLabel uLbl = new JLabel("USERNAME");
        uLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        uLbl.setForeground(new Color(140, 160, 175));

        usernameField = new JTextField();
        styleTextField(usernameField);

        JLabel pLbl = new JLabel("PASSCODE");
        pLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        pLbl.setForeground(new Color(140, 160, 175));

        passwordField = new JPasswordField();
        styleTextField(passwordField);

        JButton loginButton = new JButton("AUTHENTICATE");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loginButton.setBackground(new Color(28, 176, 246)); // Sapphire Blue
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton switchButton = new JButton("Need an account? Initialize Sign Up");
        switchButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        switchButton.setForeground(new Color(140, 160, 175));
        switchButton.setContentAreaFilled(false);
        switchButton.setBorderPainted(false);
        switchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Layout Assembly
        gbc.gridx = 0; gbc.gridy = 0;
        add(titleLabel, gbc);

        gbc.gridy = 1;
        add(subLabel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(15, 0, 4, 0);
        add(uLbl, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 8, 0);
        add(usernameField, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(4, 0, 4, 0);
        add(pLbl, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 15, 0);
        add(passwordField, gbc);

        gbc.gridy = 6;
        add(loginButton, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(10, 0, 0, 0);
        add(switchButton, gbc);

        // Event Listeners
        switchButton.addActionListener(e -> parentFrame.showSignUp());
        loginButton.addActionListener(e -> attemptLogin());
    }

    private void styleTextField(JComponent field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(new Color(15, 20, 24));
        field.setForeground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(38, 52, 64), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showHudMessage("Error", "Please fill in all required fields.", new Color(248, 113, 113));
            return;
        }

        User user = userDAO.authenticateUser(username, password);
        if (user != null) {
            UserSession.getInstance().login(user.getId(), user.getUsername());
            SystemLogger.log("Operator authenticated: " + username);
            parentFrame.onLoginSuccess();
        } else {
            showHudMessage("Access Denied", "Invalid operator username or passcode.", new Color(248, 113, 113));
        }
    }

    private void showHudMessage(String title, String message, Color accentColor) {
        JDialog dialog = new JDialog(parentFrame, title, true);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(24, 32, 38));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(38, 52, 64), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLbl = new JLabel("⚠ " + title.toUpperCase());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(accentColor);

        JLabel msgLbl = new JLabel("<html><div style='width:240px; color: #E2E8F0;'>" + message + "</div></html>");
        msgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton okBtn = new JButton("ACKNOWLEDGE");
        okBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        okBtn.setBackground(new Color(28, 176, 246));
        okBtn.setForeground(Color.WHITE);
        okBtn.setFocusPainted(false);
        okBtn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        okBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okBtn.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(okBtn);

        panel.add(titleLbl, BorderLayout.NORTH);
        panel.add(msgLbl, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
    }
}