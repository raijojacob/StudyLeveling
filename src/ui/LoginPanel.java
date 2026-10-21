package ui;

import db.UserDAO;
import logic.UserSession;
import models.User;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;
    private UserDAO userDAO;

    public LoginPanel(AuthFrame parentFrame) {
        userDAO = new UserDAO();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // UI Components
        JLabel titleLabel = new JLabel("Welcome Back", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        JButton loginButton = new JButton("Login");
        JButton switchButton = new JButton("Need an account? Sign Up");
        switchButton.setContentAreaFilled(false);
        switchButton.setBorderPainted(false);
        
        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setForeground(Color.RED);

        // Layout Assembly
        gbc.gridx = 0; gbc.gridy = 0;
        add(titleLabel, gbc);

        gbc.gridy = 1;
        add(new JLabel("Username:"), gbc);
        gbc.gridy = 2;
        add(usernameField, gbc);

        gbc.gridy = 3;
        add(new JLabel("Password:"), gbc);
        gbc.gridy = 4;
        add(passwordField, gbc);

        gbc.gridy = 5;
        add(loginButton, gbc);

        gbc.gridy = 6;
        add(messageLabel, gbc);

        gbc.gridy = 7;
        add(switchButton, gbc);

        // Event Listeners
        switchButton.addActionListener(e -> {
            messageLabel.setText(""); // Clear errors
            parentFrame.showSignUp();
        });

        loginButton.addActionListener(e -> attemptLogin(parentFrame));
    }

    private void attemptLogin(AuthFrame parentFrame) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill all fields.");
            return;
        }

        User user = userDAO.authenticateUser(username, password);
        if (user != null) {
            UserSession.getInstance().login(user.getId(), user.getUsername());
            parentFrame.onLoginSuccess();
        } else {
            messageLabel.setText("Invalid username or password.");
        }
    }
}