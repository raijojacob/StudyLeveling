package ui;

import db.UserDAO;

import javax.swing.*;
import java.awt.*;

public class SignUpPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JLabel messageLabel;
    private UserDAO UserDAO;

    public SignUpPanel(AuthFrame parentFrame) {
        UserDAO = new UserDAO();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // UI Components
        JLabel titleLabel = new JLabel("Create Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        confirmPasswordField = new JPasswordField(15);
        
        JButton registerButton = new JButton("Register");
        JButton switchButton = new JButton("Already have an account? Log In");
        switchButton.setContentAreaFilled(false);
        switchButton.setBorderPainted(false);
        
        messageLabel = new JLabel("", SwingConstants.CENTER);

        // Layout Assembly
        gbc.gridx = 0; gbc.gridy = 0;
        add(titleLabel, gbc);

        gbc.gridy = 1;
        add(new JLabel("Choose Username:"), gbc);
        gbc.gridy = 2;
        add(usernameField, gbc);

        gbc.gridy = 3;
        add(new JLabel("Choose Password:"), gbc);
        gbc.gridy = 4;
        add(passwordField, gbc);

        gbc.gridy = 5;
        add(new JLabel("Confirm Password:"), gbc);
        gbc.gridy = 6;
        add(confirmPasswordField, gbc);

        gbc.gridy = 7;
        add(registerButton, gbc);

        gbc.gridy = 8;
        add(messageLabel, gbc);

        gbc.gridy = 9;
        add(switchButton, gbc);

        // Event Listeners
        switchButton.addActionListener(e -> {
            messageLabel.setText("");
            parentFrame.showLogin();
        });

        registerButton.addActionListener(e -> attemptRegister());
    }

    private void attemptRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showMessage("Please fill all fields.", Color.RED);
            return;
        }

        if (!password.equals(confirm)) {
            showMessage("Passwords do not match.", Color.RED);
            return;
        }

        if (password.length() < 6) {
            showMessage("Password must be at least 6 characters.", Color.RED);
            return;
        }

        // Try registering and catch any underlying database/connection exceptions
        try {
            boolean success = new db.UserDAO().registerUserWithFeedback(username, password);
            if (success) {
                showMessage("Account created! You can now log in.", new Color(0, 153, 0));
                usernameField.setText("");
                passwordField.setText("");
                confirmPasswordField.setText("");
            } else {
                showMessage("Username already taken.", Color.RED);
            }
        } catch (Exception e) {
            // This will show the EXACT database error on your screen
            showMessage("DB Error: " + e.getMessage(), Color.RED);
            e.printStackTrace();
        }
    }

    private void showMessage(String text, Color color) {
        messageLabel.setForeground(color);
        messageLabel.setText(text);
    }
}