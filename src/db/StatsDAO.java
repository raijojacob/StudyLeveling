package db;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class StatsDAO {

    public int getXp(int userId) {
        String query = "SELECT xp FROM stats WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("xp");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void addXp(int userId, int amount) {
        String checkQuery = "SELECT xp FROM stats WHERE user_id = ?";
        String updateQuery = "UPDATE stats SET xp = xp + ? WHERE user_id = ?";
        String insertQuery = "INSERT INTO stats (user_id, xp) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            boolean exists = false;
            try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                    pstmt.setInt(1, amount);
                    pstmt.setInt(2, userId);
                    pstmt.executeUpdate();
                }
            } else {
                try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                    pstmt.setInt(1, userId);
                    pstmt.setInt(2, amount);
                    pstmt.executeUpdate();
                }
            }

            if (amount > 0) {
                String updateTaskDateQuery = "UPDATE tasks SET due_date = ? WHERE user_id = ? AND completed = 1 ORDER BY id DESC LIMIT 1";
                try (PreparedStatement pstmt = conn.prepareStatement(updateTaskDateQuery)) {
                    pstmt.setString(1, LocalDate.now().toString());
                    pstmt.setInt(2, userId);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getTotalTasksCount(int userId) {
        String query = "SELECT COUNT(*) FROM tasks WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getCompletedTasksCount(int userId) {
        String query = "SELECT COUNT(*) FROM tasks WHERE user_id = ? AND completed = 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double[] getWeeklyHours(int userId) {
        double[] hours = new double[7];
        String query = "SELECT due_date, COUNT(*) as task_count FROM tasks WHERE user_id = ? AND completed = 1 GROUP BY due_date";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                LocalDate today = LocalDate.now();
                while (rs.next()) {
                    String dateStr = rs.getString("due_date");
                    int count = rs.getInt("task_count");
                    if (dateStr != null && !dateStr.isEmpty() && !dateStr.equalsIgnoreCase("Today")) {
                        try {
                            LocalDate date = LocalDate.parse(dateStr);
                            long daysBetween = ChronoUnit.DAYS.between(date, today);
                            if (daysBetween >= 0 && daysBetween < 7) {
                                hours[6 - (int)daysBetween] += (count * 0.5);
                            }
                        } catch (Exception e) {}
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hours;
    }

    public boolean[] getConsistencyMatrix(int userId) {
        boolean[] matrix = new boolean[28];
        String query = "SELECT due_date FROM tasks WHERE user_id = ? AND completed = 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                LocalDate today = LocalDate.now();
                while (rs.next()) {
                    String dateStr = rs.getString("due_date");
                    if (dateStr != null && !dateStr.isEmpty() && !dateStr.equalsIgnoreCase("Today")) {
                        try {
                            LocalDate date = LocalDate.parse(dateStr);
                            long daysBetween = ChronoUnit.DAYS.between(date, today);
                            if (daysBetween >= 0 && daysBetween < 28) {
                                matrix[27 - (int)daysBetween] = true;
                            }
                        } catch (Exception e) {}
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return matrix;
    }
}