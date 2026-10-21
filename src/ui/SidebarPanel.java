package ui;

import db.StatsDAO;
import models.UserStats;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SidebarPanel extends JPanel {
    private MainFrame parentFrame;
    private List<NavButtonWrapper> navButtons = new ArrayList<>();

    private static class NavButtonWrapper {
        JButton button;
        String targetView;
        JLabel iconLabel;
        JLabel textLabel;

        public NavButtonWrapper(JButton button, String targetView, JLabel iconLabel, JLabel textLabel) {
            this.button = button;
            this.targetView = targetView;
            this.iconLabel = iconLabel;
            this.textLabel = textLabel;
        }
    }

    public SidebarPanel(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(220, 0));
        setBackground(new Color(15, 20, 24)); // Dark HUD obsidian background
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(30, 45, 55)));

        // Top Header
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 15, 15));

        JLabel titleLabel = new JLabel("STUDY LEVELLING");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(new Color(225, 235, 245));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(titleLabel);
        topPanel.add(Box.createVerticalStrut(20));

        // Navigation Menu Container
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);
        navPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        // SYSTEM SECTION (Study Material renamed to Add Task)
        navPanel.add(createSectionLabel("SYSTEM"));
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(createNavButton("◫", "Dashboard", "DASHBOARD"));
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(createNavButton("▤", "Task Manager", "PDF_IMPORT"));
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(createNavButton("🎯", "Today's Work", "DAILY_PATH"));
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(createNavButton("⏱", "Focus Timer", "POMODORO"));

        navPanel.add(Box.createVerticalStrut(18));

        // PROGRESS SECTION
        navPanel.add(createSectionLabel("PROGRESS"));
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(createNavButton("☵", "Statistics", "STATS"));

        topPanel.add(navPanel);
        add(topPanel, BorderLayout.NORTH);

        // Bottom Footer Widget (User Level & Logout)
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        UserStats stats = new StatsDAO().getUserStats();
        int xp = (stats != null) ? stats.getTotalXp() : 0;
        int level = (xp / 100) + 1;
        int nextLevelXp = level * 100;

        JLabel levelLabel = new JLabel("LVL " + level + "    " + xp + " / " + nextLevelXp + " XP");
        levelLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        levelLabel.setForeground(new Color(56, 189, 248));
        levelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JProgressBar levelBar = new JProgressBar(0, 100);
        levelBar.setValue((int)(((double)(xp % 100) / 100.0) * 100));
        levelBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        levelBar.setForeground(new Color(56, 189, 248));
        levelBar.setBackground(new Color(25, 35, 45));
        levelBar.setBorderPainted(false);
        levelBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton logoutBtn = createLogoutButton("➔", "Logout");
        logoutBtn.addActionListener(e -> parentFrame.performLogout());

        bottomPanel.add(levelLabel);
        bottomPanel.add(Box.createVerticalStrut(6));
        bottomPanel.add(levelBar);
        bottomPanel.add(Box.createVerticalStrut(15));
        bottomPanel.add(logoutBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        // Initial highlight
        setActiveView("DASHBOARD");
    }

    private JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(100, 120, 135));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton createNavButton(String iconSymbol, String text, String targetView) {
        JButton btn = new JButton();
        btn.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 2));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(iconSymbol);
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));

        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        btn.add(iconLabel);
        btn.add(textLabel);

        navButtons.add(new NavButtonWrapper(btn, targetView, iconLabel, textLabel));

        if (targetView != null) {
            btn.addActionListener(e -> {
                parentFrame.switchView(targetView);
                setActiveView(targetView);
            });
        }
        return btn;
    }

    private JButton createLogoutButton(String iconSymbol, String text) {
        JButton btn = new JButton();
        btn.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 2));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);

        JLabel iconLabel = new JLabel(iconSymbol);
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
        iconLabel.setForeground(new Color(248, 113, 113));

        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textLabel.setForeground(new Color(248, 113, 113));

        btn.add(iconLabel);
        btn.add(textLabel);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 11, 4, 8));
        return btn;
    }

    public void setActiveView(String viewName) {
        for (NavButtonWrapper wrapper : navButtons) {
            if (wrapper.targetView.equals(viewName)) {
                wrapper.button.setBackground(new Color(24, 40, 52));
                wrapper.button.setOpaque(true);
                wrapper.button.setContentAreaFilled(true);
                wrapper.iconLabel.setForeground(new Color(56, 189, 248));
                wrapper.textLabel.setForeground(new Color(56, 189, 248));
                wrapper.button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(56, 189, 248)),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)
                ));
            } else {
                wrapper.button.setOpaque(false);
                wrapper.button.setContentAreaFilled(false);
                wrapper.iconLabel.setForeground(new Color(170, 185, 200));
                wrapper.textLabel.setForeground(new Color(170, 185, 200));
                wrapper.button.setBorder(BorderFactory.createEmptyBorder(4, 11, 4, 8));
            }
        }
    }
}