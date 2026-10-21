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
        // Fallback query or table join depending on your schema. 
        // If you store files in a `documents` table, query them here:
        String query = "SELECT file_name, completed_modules, total_modules FROM documents WHERE user_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    docs.add(new DocumentRecord(
                        rs.getString("file_name"),
                        rs.getInt("completed_modules"),
                        rs.getInt("total_modules")
                    ));
                }
            }
        } catch (SQLException e) {
            // Table might not exist yet or empty, return empty list safely
        }
        
        return docs;
    }
}