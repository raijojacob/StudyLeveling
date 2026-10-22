package db;

import models.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;

public class UserDAO {

    public boolean registerUserWithFeedback(String username, String password) throws Exception {
        String hashedPassword = hashPassword(password);
        String insertUserQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
        String insertStatsQuery = "INSERT INTO user_stats (user_id, total_xp, current_streak) VALUES (?, 0, 0)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmtUser = conn.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS)) {

            pstmtUser.setString(1, username);
            pstmtUser.setString(2, hashedPassword);
            pstmtUser.executeUpdate();

            try (ResultSet generatedKeys = pstmtUser.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newUserId = generatedKeys.getInt(1);
                    try (PreparedStatement pstmtStats = conn.prepareStatement(insertStatsQuery)) {
                        pstmtStats.setInt(1, newUserId);
                        pstmtStats.executeUpdate();
                    }
                }
            }
            return true;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                return false; // Duplicate username
            }
            throw e; // Throw any other database error so the UI displays it
        }
    }
    // Attempts to log the user in. Returns the User object if successful, null if failed.
    public User authenticateUser(String username, String password) {
        String hashedPassword = hashPassword(password);
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Login failed (wrong username or password)
    }

    // Registers a new user. Returns true if successful, false if username already exists.
    public boolean registerUser(String username, String password) {
        String hashedPassword = hashPassword(password);
        String insertUserQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
        String insertStatsQuery = "INSERT INTO user_stats (user_id, total_xp, current_streak) VALUES (?, 0, 0)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmtUser = conn.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS)) {

            pstmtUser.setString(1, username);
            pstmtUser.setString(2, hashedPassword);
            pstmtUser.executeUpdate();

            // Retrieve the auto-generated ID to create their empty stats row
            try (ResultSet generatedKeys = pstmtUser.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newUserId = generatedKeys.getInt(1);

                    try (PreparedStatement pstmtStats = conn.prepareStatement(insertStatsQuery)) {
                        pstmtStats.setInt(1, newUserId);
                        pstmtStats.executeUpdate();
                    }
                }
            }
            return true;

        } catch (SQLException e) {
            // Print the exact SQL error code and message to your console
            System.err.println("SQL ERROR CODE: " + e.getErrorCode());
            System.err.println("SQL MESSAGE: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to hash passwords using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
}
