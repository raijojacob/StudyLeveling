package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    
    private static final String URL = "jdbc:mysql://localhost:3306/studyleveling?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "Bhramarah"; 

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        // 1. Users table (Must be created first since other tables reference it)
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """;

        // 2. Tasks table (Linked to users)
        String createTasksTable = """
            CREATE TABLE IF NOT EXISTS tasks (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                title VARCHAR(255) NOT NULL,
                description TEXT,
                target_date DATE,
                is_completed TINYINT(1) DEFAULT 0,
                is_auto_generated TINYINT(1) DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );
            """;

        // 3. User Stats table for XP and Streaks (Linked to users)
        String createStatsTable = """
            CREATE TABLE IF NOT EXISTS user_stats (
                user_id INT PRIMARY KEY,
                total_xp INT DEFAULT 0,
                current_streak INT DEFAULT 0,
                last_completed_date DATE,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Execute table creation in exact dependency order
            stmt.execute(createUsersTable);
            stmt.execute(createTasksTable);
            stmt.execute(createStatsTable);

            System.out.println("Database connected. Tables verified and ready.");

        } catch (SQLException e) {
            System.err.println("Database connection failed. Ensure MySQL is running and credentials are correct.");
            e.printStackTrace();
        }
    }
}