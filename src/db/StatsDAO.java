package db;

import logic.UserSession;
import models.UserStats;

import java.sql.*;
import java.time.LocalDate;

public class StatsDAO {

    public UserStats getUserStats() {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1) return new UserStats(0, 0, (java.util.Date) null);

        return getUserStats(userId);
    }

    public UserStats getUserStats(int userId) {
        String query = "SELECT total_xp, current_streak, last_completed_date FROM stats WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new UserStats(
                            rs.getInt("total_xp"),
                            rs.getInt("current_streak"),
                            rs.getDate("last_completed_date")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new UserStats(0, 0, (java.util.Date) null);
    }

    // Overloaded single-parameter method called by PomodoroEngine / PomodoroPanel
    public void addXpAndUpdateStreak(int xpGained) {
        int userId = UserSession.getInstance().getUserId();
        if (userId != -1) {
            addXpAndUpdateStreak(userId, xpGained);
        }
    }

    // Two-parameter method to update XP and streak state in MySQL
    public void addXpAndUpdateStreak(int userId, int xpGained) {
        String checkQuery = "SELECT total_xp, current_streak, last_completed_date FROM stats WHERE user_id = ?";
        String insertQuery = "INSERT INTO stats (user_id, total_xp, current_streak, last_completed_date) VALUES (?, ?, 1, CURDATE())";
        String updateQuery = "UPDATE stats SET total_xp = total_xp + ?, current_streak = ?, last_completed_date = CURDATE() WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            boolean exists = false;
            int currentStreak = 0;
            Date lastDate = null;

            try (PreparedStatement checkPstmt = conn.prepareStatement(checkQuery)) {
                checkPstmt.setInt(1, userId);
                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (rs.next()) {
                        exists = true;
                        currentStreak = rs.getInt("current_streak");
                        lastDate = rs.getDate("last_completed_date");
                    }
                }
            }

            // Streak calculation logic based on last activity date
            LocalDate today = LocalDate.now();
            if (lastDate != null) {
                LocalDate lastCompleted = lastDate.toLocalDate();
                if (lastCompleted.equals(today.minusDays(1))) {
                    currentStreak++; // Continued daily streak
                } else if (!lastCompleted.equals(today)) {
                    currentStreak = 1; // Streak reset due to missed days
                }
            } else {
                currentStreak = 1;
            }

            if (exists) {
                try (PreparedStatement updatePstmt = conn.prepareStatement(updateQuery)) {
                    updatePstmt.setInt(1, xpGained);
                    updatePstmt.setInt(2, currentStreak);
                    updatePstmt.setInt(3, userId);
                    updatePstmt.executeUpdate();
                }
            } else {
                try (PreparedStatement insertPstmt = conn.prepareStatement(insertQuery)) {
                    insertPstmt.setInt(1, userId);
                    insertPstmt.setInt(2, xpGained);
                    insertPstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addXp(int userId, int xpToAdd) {
        addXpAndUpdateStreak(userId, xpToAdd);
    }
}