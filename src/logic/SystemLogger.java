package logic;

import javax.swing.JTextArea;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SystemLogger {
    private static final List<String> logs = new ArrayList<>();
    private static JTextArea registeredTextArea;

    static {
        log("System telemetry active.");
        log("Connected to local database.");
    }

    public static void registerTextArea(JTextArea area) {
        registeredTextArea = area;
        refreshDisplay();
    }

    public static void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        String entry = timestamp + " " + message;
        logs.add(0, entry); // Add newest at the top

        if (logs.size() > 50) {
            logs.remove(logs.size() - 1);
        }

        refreshDisplay();
    }

    private static void refreshDisplay() {
        if (registeredTextArea != null) {
            StringBuilder sb = new StringBuilder();
            for (String entry : logs) {
                sb.append(entry).append("\n");
            }
            registeredTextArea.setText(sb.toString());
            registeredTextArea.setCaretPosition(0);
        }
    }
}