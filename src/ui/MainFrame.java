package ui;

import logic.UserSession;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private SidebarPanel sidebar;
    private DashboardPanel dashboardPanel; // Keep a reference to refresh it

    public MainFrame() {
        String username = UserSession.getInstance().getUsername();
        setTitle("StudyLeveling // Offline Sys v1.4 [" + username + "]");
        setSize(1180, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Initialize and store reference to DashboardPanel
        dashboardPanel = new DashboardPanel(this);

        // Register View Panels with correct unique route names
        contentPanel.add(dashboardPanel, "DASHBOARD");
        contentPanel.add(new DailyDashboardPanel(this), "DAILY_PATH");
        contentPanel.add(new PomodoroPanel(), "POMODORO");
        contentPanel.add(new TaskManagerPanel(), "TASKS");
        contentPanel.add(new TaskManagerPanel(), "PDF_IMPORT"); // Handles both routes cleanly
        contentPanel.add(new StatsPanel(), "STATS");

        sidebar = new SidebarPanel(this);

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Default view
        cardLayout.show(contentPanel, "DASHBOARD");
        sidebar.setActiveView("DASHBOARD");
    }

    public void switchView(String viewName) {
        cardLayout.show(contentPanel, viewName);
        if (sidebar != null) {
            sidebar.setActiveView(viewName);
        }
        
        // Automatically refresh dashboard tasks and metrics whenever navigating to it
        if (viewName.equals("DASHBOARD") && dashboardPanel != null) {
            dashboardPanel.refreshDashboard();
        }
    }

    public void performLogout() {
        UserSession.getInstance().logout();
        this.dispose();
        AuthFrame authFrame = new AuthFrame();
        authFrame.setVisible(true);
    }
}