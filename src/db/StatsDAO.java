package db;

import logic.UserSession;
import models.UserStats;

import java.sql.*;
import java.time.LocalDate;

public class StatsDAO {

    public UserStats getUserStats() {
        int userId = UserSession.getInstance().getUserId();
        String query = "SELECT * FROM user_stats WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Date sqlDate = rs.getDate("last_completed_date");
                    LocalDate lastDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                    return new UserStats(
                            rs.getInt("user_id"),
                            rs.getInt("total_xp"),
                            rs.getInt("current_streak"),
                            lastDate
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addXpAndUpdateStreak(int xpToAdd) {
        int userId = UserSession.getInstance().getUserId();
        UserStats stats = getUserStats();
        
        if (stats == null) return;

        LocalDate today = LocalDate.now();
        LocalDate lastDate = stats.getLastCompletedDate();
        int newStreak = stats.getCurrentStreak();

        // Streak Logic
        if (lastDate == null || lastDate.isBefore(today.minusDays(1))) {
            // Missed a day or first time, reset to 1
            newStreak = 1;
        } else if (lastDate.equals(today.minusDays(1))) {
            // Completed yesterday, increment streak
            newStreak += 1;
        }
        // If lastDate is today, the streak remains the same

        String updateQuery = "UPDATE user_stats SET total_xp = total_xp + ?, current_streak = ?, last_completed_date = ? WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            pstmt.setInt(1, xpToAdd);
            pstmt.setInt(2, newStreak);
            pstmt.setDate(3, Date.valueOf(today));
            pstmt.setInt(4, userId);
            
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}