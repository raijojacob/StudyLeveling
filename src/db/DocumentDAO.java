package db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {
    
    public static class DocumentRecord {
        private String fileName;
        private int completedModules;
        private int totalModules;

        public DocumentRecord(String fileName, int completedModules, int totalModules) {
            this.fileName = fileName;
            this.completedModules = completedModules;
            this.totalModules = totalModules;
        }

        public String getFileName() { return fileName; }
        public int getCompletedModules() { return completedModules; }
        public int getTotalModules() { return totalModules; }
    }

    public List<DocumentRecord> getDocumentsForUser(int userId) {
        List<DocumentRecord> docs = new ArrayList<>();
        
        // 1. Fetch total completed tasks for this user dynamically
        int completedTasks = 0;
        String taskQuery = "SELECT COUNT(*) FROM tasks WHERE user_id = ? AND completed = 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(taskQuery)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) completedTasks = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 2. Fetch document records and map dynamic completed progress
        String query = "SELECT file_name, total_modules FROM documents WHERE user_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int total = rs.getInt("total_modules");
                    // Dynamically map completed tasks to the document modules (capped at total modules)
                    int completedForDoc = Math.min(completedTasks, total);
                    
                    docs.add(new DocumentRecord(
                        rs.getString("file_name"),
                        completedForDoc,
                        total
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return docs;
    }

    public void insertDocument(int userId, String fileName, int totalModules) {
        String query = "INSERT INTO documents (user_id, file_name, completed_modules, total_modules) VALUES (?, ?, 0, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, fileName);
            pstmt.setInt(3, totalModules);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}