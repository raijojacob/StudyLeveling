import com.formdev.flatlaf.FlatDarkLaf;
import db.DatabaseManager;
import ui.AuthFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Apply FlatLaf modern dark theme
        FlatDarkLaf.setup();

        // Ensure database and tables exist
        DatabaseManager.initializeDatabase();

        // Launch UI
        SwingUtilities.invokeLater(() -> {
            AuthFrame authFrame = new AuthFrame();
            authFrame.setVisible(true);
        });
    }
}