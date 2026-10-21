package ui;

import db.StatsDAO;
import logic.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SidebarPanel extends JPanel {
    private MainFrame parentFrame;
    
    // Tracking nav buttons for active state highlighting
    private Map<String, JButton> navButtons = new HashMap<>();
    private String currentActiveView = "DASHBOARD";
    
    // Bottom panel components to update dynamically
    private JLabel operatorNameLbl;
    private JLabel levelLbl;
    private JLabel xpLbl;
    private JProgressBar xpBar;

    public SidebarPanel(MainFrame parentFrame) {
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout());
        setBackground(new Color(15, 20, 24)); // Darker sidebar background
        setPreferredSize(new Dimension(240, 0)); // Fixed width for sidebar
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(38, 52, 64))); // Right border

        // 1. Top Logo Section
        add(createLogoPanel(), BorderLayout.NORTH);

        // 2. Middle Navigation Section
        add(createNavPanel(), BorderLayout.CENTER);

        // 3. Bottom User Profile & Stats Section
        add(createUserStatsPanel(), BorderLayout.SOUTH);

        // Fetch initial data
        refreshSidebarStats();
    }

    private JPanel createLogoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 20, 25, 20));

        JLabel title = new JLabel("STUDY LEVELLING");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("// LOCAL HUD SYSTEM");
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 9));
        subtitle.setForeground(new Color(56, 189, 248));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subtitle);

        return panel;
    }

    private JPanel createNavPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 10, 0, 10));

        // Create Navigation Buttons (Ensure these strings match your MainFrame CardLayout constraints)
        panel.add(createNavButton("DASHBOARD", "DASHBOARD"));
        panel.add(Box.createVerticalStrut(5));
        panel.add(createNavButton("DAILY TASKS", "DAILY_PATH"));
        panel.add(Box.createVerticalStrut(5));
        panel.add(createNavButton("TASK MANAGER", "TASKS"));
        panel.add(Box.createVerticalStrut(5));
        panel.add(createNavButton("TIMER ENGINE", "POMODORO"));
        panel.add(Box.createVerticalStrut(5));
        panel.add(createNavButton("TELEMETRY", "STATS"));

        return panel;
    }

    private JButton createNavButton(String label, String viewName) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(new Color(140, 160, 175));
        btn.setBackground(new Color(15, 20, 24));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect for HUD feel, respecting active state
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setForeground(Color.WHITE);
                btn.setBackground(new Color(24, 32, 38));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!viewName.equals(currentActiveView)) {
                    btn.setForeground(new Color(140, 160, 175));
                    btn.setBackground(new Color(15, 20, 24));
                }
            }
        });

        btn.addActionListener(e -> {
            if(parentFrame != null) {
                parentFrame.switchView(viewName); 
            }
        });

        navButtons.put(viewName, btn);
        return btn;
    }

    /**
     * Called by MainFrame to visually highlight the active sidebar button.
     */
    public void setActiveView(String viewName) {
        this.currentActiveView = viewName;
        
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equals(viewName)) {
                btn.setForeground(Color.WHITE);
                btn.setBackground(new Color(24, 32, 38)); // Active state
            } else {
                btn.setForeground(new Color(140, 160, 175));
                btn.setBackground(new Color(15, 20, 24)); // Inactive state
            }
        }
    }

    private JPanel createUserStatsPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(38, 52, 64)),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Operator Header
        operatorNameLbl = new JLabel("OPERATOR");
        operatorNameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        operatorNameLbl.setForeground(Color.WHITE);
        operatorNameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Level & XP Row
        JPanel statsRow = new JPanel(new BorderLayout());
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        levelLbl = new JLabel("LVL 1");
        levelLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        levelLbl.setForeground(new Color(168, 85, 247)); // Purple Tier Color
        
        xpLbl = new JLabel("0 XP");
        xpLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        xpLbl.setForeground(new Color(140, 160, 175));

        statsRow.add(levelLbl, BorderLayout.WEST);
        statsRow.add(xpLbl, BorderLayout.EAST);

        // HUD Progress Bar
        xpBar = new JProgressBar(0, 500); // 500 XP per level
        xpBar.setValue(0);
        xpBar.setStringPainted(false);
        xpBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 6));
        xpBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        xpBar.setBackground(new Color(24, 32, 38));
        xpBar.setForeground(new Color(56, 189, 248)); // Sapphire Blue
        xpBar.setBorderPainted(false);
        xpBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Logout Button
        JButton logoutBtn = new JButton("LOGOUT SYSTEM");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 9));
        logoutBtn.setForeground(new Color(248, 113, 113)); // Red accent for logout
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setMargin(new Insets(0, 0, 0, 0));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        logoutBtn.addActionListener(e -> {
            UserSession.getInstance().logout();
            parentFrame.dispose();
            new AuthFrame().setVisible(true);
        });

        // Assembly
        bottomPanel.add(operatorNameLbl);
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(statsRow);
        bottomPanel.add(Box.createVerticalStrut(4));
        bottomPanel.add(xpBar);
        bottomPanel.add(Box.createVerticalStrut(15));
        bottomPanel.add(logoutBtn);

        return bottomPanel;
    }

    /**
     * Call this method from MainFrame whenever tasks are completed or XP is gained
     * to instantly update the sidebar progress bar.
     */
    public void refreshSidebarStats() {
        int userId = UserSession.getInstance().getUserId();
        String username = UserSession.getInstance().getUsername();

        if (userId != -1) {
            StatsDAO statsDAO = new StatsDAO();
            int xp = statsDAO.getXp(userId);
            int level = (xp / 500) + 1; // 500 XP per level
            int currentLevelXp = xp % 500; // Remaining XP for the progress bar

            operatorNameLbl.setText((username != null ? username.toUpperCase() : "UNKNOWN"));
            levelLbl.setText("LVL " + level);
            xpLbl.setText(String.format("%,d XP", xp));
            
            xpBar.setMaximum(500);
            xpBar.setValue(currentLevelXp);
        }
    }
}