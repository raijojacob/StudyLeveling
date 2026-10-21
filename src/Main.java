import com.formdev.flatlaf.FlatDarkLaf;
import db.DatabaseManager;
import ui.AuthFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // flatlaf darktheme
        FlatDarkLaf.setup();

        // database intialisation
        DatabaseManager.initializeDatabase();

        // ui
        SwingUtilities.invokeLater(() -> {
            AuthFrame authFrame = new AuthFrame();
            authFrame.setVisible(true);
        });
    }
}