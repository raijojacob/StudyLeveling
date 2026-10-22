package db;

import logic.UserSession;
import models.Task;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public List<Task> getTasksForUser(int userId) {
        List<Task> tasks = new ArrayList<>();
        String query = "SELECT id, title, due_date, completed FROM tasks WHERE user_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task();
                    task.setId(rs.getInt("id"));
                    task.setTitle(rs.getString("title"));
                    task.setCompleted(rs.getBoolean("completed"));
                    String dateStr = rs.getString("due_date");
                    if (dateStr != null && !dateStr.isEmpty()) {
                        try {
                            task.setDueDate(LocalDate.parse(dateStr));
                        } catch (Exception e) {
                            task.setDueDate(LocalDate.now());
                        }
                    }
                    tasks.add(task);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public void addTask(int userId, String title, LocalDate dueDate) {
        String query = "INSERT INTO tasks (user_id, title, due_date, completed) VALUES (?, ?, ?, 0)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, title);
            pstmt.setString(3, dueDate != null ? dueDate.toString() : LocalDate.now().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Overloaded method to accept date strings safely from UI inputs
    public void addTask(int userId, String title, String dueDateStr) {
        LocalDate date;
        try {
            date = (dueDateStr != null && !dueDateStr.isEmpty() && !dueDateStr.equalsIgnoreCase("Today")) 
                   ? LocalDate.parse(dueDateStr) 
                   : LocalDate.now();
        } catch (Exception e) {
            date = LocalDate.now();
        }
        addTask(userId, title, date);
    }

    public void createTask(Task task) {
        LocalDate date = task.getDueDate() != null ? task.getDueDate() : LocalDate.now();
        addTask(task.getUserId(), task.getTitle(), date);
    }

    public void updateTaskCompletion(int taskId, boolean completed) {
        String query = "UPDATE tasks SET completed = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, completed ? 1 : 0);
            pstmt.setInt(2, taskId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTask(int taskId) {
        String query = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, taskId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}