package ui;

import db.UserDAO;
import logic.SystemLogger;
import logic.UserSession;
import models.User;

import javax.swing.*;
import java.awt.*;

public class AuthFrame extends JFrame {
    private JTabbedPane tabbedPane;
    
    // Login components
    private JTextField loginUserField;
    private JPasswordField loginPassField;

    // Register components
    private JTextField regUserField;
    private JPasswordField regPassField;
    private JPasswordField regConfirmPassField;

    private UserDAO userDAO;

    public AuthFrame() {
        userDAO = new UserDAO();
        setTitle("StudyLeveling // System Authentication");
        setSize(420, 530);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main container with dark obsidian canvas
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(18, 24, 29));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Header Title
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel title = new JLabel("STUDY LEVELLING");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("// SECURE LOCAL OFFLINE AUTHENTICATION");
        sub.setFont(new Font("Segoe UI", Font.BOLD, 10));
        sub.setForeground(new Color(56, 189, 248));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);
        header.add(Box.createVerticalStrut(20));

        mainPanel.add(header, BorderLayout.NORTH);

        // Tabbed Pane for Login / Register
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        tabbedPane.addTab("LOGIN", createLoginPanel());
        tabbedPane.addTab("REGISTER", createRegisterPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    public void showLogin() {
        if (tabbedPane != null) tabbedPane.setSelectedIndex(0);
    }

    public void showSignUp() {
        if (tabbedPane != null) tabbedPane.setSelectedIndex(1);
    }

    public void onLoginSuccess() {
        dispose();
        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(24, 32, 38));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(38, 52, 64), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        JLabel uLbl = new JLabel("USERNAME");
        uLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        uLbl.setForeground(new Color(140, 160, 175));
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(uLbl, gbc);

        loginUserField = new JTextField();
        styleTextField(loginUserField);
        gbc.gridy = 1;
        panel.add(loginUserField, gbc);

        JLabel pLbl = new JLabel("PASSCODE");
        pLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pLbl.setForeground(new Color(140, 160, 175));
        gbc.gridy = 2;
        panel.add(pLbl, gbc);

        loginPassField = new JPasswordField();
        styleTextField(loginPassField);
        gbc.gridy = 3;
        panel.add(loginPassField, gbc);

        JButton loginBtn = new JButton("AUTHENTICATE");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loginBtn.setBackground(new Color(28, 176, 246));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(e -> handleLogin());

        gbc.gridy = 4;
        gbc.insets = new Insets(15, 0, 0, 0);
        panel.add(loginBtn, gbc);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(24, 32, 38));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(38, 52, 64), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);

        JLabel uLbl = new JLabel("CHOOSE USERNAME");
        uLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        uLbl.setForeground(new Color(140, 160, 175));
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(uLbl, gbc);

        regUserField = new JTextField();
        styleTextField(regUserField);
        gbc.gridy = 1;
        panel.add(regUserField, gbc);

        JLabel pLbl = new JLabel("CHOOSE PASSCODE");
        pLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pLbl.setForeground(new Color(140, 160, 175));
        gbc.gridy = 2;
        panel.add(pLbl, gbc);

        regPassField = new JPasswordField();
        styleTextField(regPassField);
        gbc.gridy = 3;
        panel.add(regPassField, gbc);

        JLabel cpLbl = new JLabel("CONFIRM PASSCODE");
        cpLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cpLbl.setForeground(new Color(140, 160, 175));
        gbc.gridy = 4;
        panel.add(cpLbl, gbc);

        regConfirmPassField = new JPasswordField();
        styleTextField(regConfirmPassField);
        gbc.gridy = 5;
        panel.add(regConfirmPassField, gbc);

        JButton regBtn = new JButton("INITIALIZE ACCOUNT");
        regBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        regBtn.setBackground(new Color(34, 197, 94));
        regBtn.setForeground(Color.WHITE);
        regBtn.setFocusPainted(false);
        regBtn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        regBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        regBtn.addActionListener(e -> handleRegister());

        gbc.gridy = 6;
        gbc.insets = new Insets(12, 0, 0, 0);
        panel.add(regBtn, gbc);

        return panel;
    }

    private void styleTextField(JComponent field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(new Color(15, 20, 24));
        field.setForeground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(38, 52, 64), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    private void handleLogin() {
        String username = loginUserField.getText().trim();
        String password = new String(loginPassField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showHudMessage("Error", "Please enter both username and password.", new Color(248, 113, 113));
            return;
        }

        User user = userDAO.authenticateUser(username, password);
        if (user != null) {
            UserSession.getInstance().login(user.getId(), user.getUsername());
            SystemLogger.log("Operator authenticated: " + username);
            onLoginSuccess();
        } else {
            showHudMessage("Access Denied", "Invalid operator credentials.", new Color(248, 113, 113));
        }
    }

    private void handleRegister() {
        String username = regUserField.getText().trim();
        String password = new String(regPassField.getPassword());
        String confirmPassword = new String(regConfirmPassField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showHudMessage("Error", "Please fill in all registration fields.", new Color(248, 113, 113));
            return;
        }

        if (!password.equals(confirmPassword)) {
            showHudMessage("Error", "Passcodes do not match. Please re-enter.", new Color(248, 113, 113));
            regPassField.setText("");
            regConfirmPassField.setText("");
            return;
        }

        boolean success = userDAO.registerUser(username, password);
        if (success) {
            showHudMessage("Success", "Account initialized successfully!<br>You may now log in.", new Color(34, 197, 94));
            showLogin();
            loginUserField.setText(username);
            regPassField.setText("");
            regConfirmPassField.setText("");
        } else {
            showHudMessage("Registration Error", "Username already exists or database error.", new Color(248, 113, 113));
        }
    }

    private void showHudMessage(String title, String message, Color accentColor) {
        JDialog dialog = new JDialog(this, title, true);
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
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}